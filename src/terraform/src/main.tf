# Locals
locals {
  # Handle multiple branches
  workspace_prefix = var.is_local ? "${format("%.20s", terraform.workspace)}-" : ""

  # Create local override
  create_resources = ! var.is_local

  # Common
  name         = "realtimerental-connector"
  service_name = "${local.workspace_prefix}${local.name}"

  # Remote state variables
  vpc_id             = data.terraform_remote_state.terraform-infrastructure.outputs.vpc_id
  vpc_public_subnets = data.terraform_remote_state.terraform-infrastructure.outputs.vpc_public_subnets
  ecs_cluster_arn    = data.terraform_remote_state.terraform-infrastructure.outputs.ecs_cluster_arn
  ecr_repository_url = data.terraform_remote_state.terraform-sharedservices.outputs.realtimerental_connector_ecr_repository_url

  legacy_sync_queue_name = data.terraform_remote_state.legacy-sync.outputs.sqs_inbound_queue_url
  legacy_sync_queue_arn  = data.terraform_remote_state.legacy-sync.outputs.sqs_inbound_queue_arn

  # Tags
  tags = {
    terraform_managed   = true
    terraform_workspace = terraform.workspace
  }
}

# ECS
module "ecs" {
  source                 = "./modules/ecs"
  create                 = local.create_resources
  name                   = local.service_name
  tags                   = local.tags
  repository_url         = local.ecr_repository_url
  ecr_image_tag          = var.ecr_image_tag
  cpu                    = var.ecs_cpu
  memory                 = var.ecs_memory
  desired_count          = var.ecs_desired_count
  task_role_arn          = module.iam.ecs_task_role_arn
  execution_role_arn     = module.iam.ecs_task_execution_role_arn
  cluster                = local.ecs_cluster_arn
  subnets                = local.vpc_public_subnets
  security_groups        = [module.security_groups.default_security_group_id]
  spring_profiles_active = var.spring_profiles_active
  ssm_prefix             = module.ssm.ssm_prefix
}

# IAM
module "iam" {
  source                       = "./modules/iam"
  tags                         = local.tags
  ecs_assume_role_principals   = var.ecs_assume_role_principals
  ecs_cloudwatch_log_group_arn = module.ecs.cloudwatch_log_group_arn
  ssm_prefix                   = module.ssm.ssm_prefix
  inbound_queue_arn            = module.sqs.inbound_queue_arn
  outbound_queue_arn           = local.legacy_sync_queue_arn
}

# Security groups
module "security_groups" {
  source = "./modules/security_groups"
  create = local.create_resources
  name   = local.service_name
  vpc_id = local.vpc_id
}

# SSM
module "ssm" {
  source     = "./modules/ssm"
  tags       = local.tags
  name       = local.name
  ssm_prefix = local.workspace_prefix

  safely_api_username      = "safelyconnector"
  safely_api_password      = "th1s1s4u"
  inbound_queue_url        = module.sqs.inbound_queue_url
  outbound_queue_url       = local.legacy_sync_queue_name
  inbound_queue_visibility = var.inbound_queue_visibility_timeout_seconds
}

# SQS
module "sqs" {
  source                     = "./modules/sqs"
  name                       = local.service_name
  tags                       = local.tags
  visibility_timeout_seconds = var.inbound_queue_visibility_timeout_seconds
  message_retention_seconds  = 604800 # 7 days
  receive_wait_time_seconds  = 20     # maximum long polling time
}
