# Locals
data "aws_region" "current" {}

data "aws_caller_identity" "current" {}

locals {
  region     = data.aws_region.current.name
  account_id = data.aws_caller_identity.current.account_id
}

# ECS
resource "aws_ecs_task_definition" "default" {
  count = var.create ? 1 : 0

  family = var.name
  container_definitions = templatefile(
    "${path.module}/container_definition.json",
    {
      name                      = jsonencode(var.name)
      image                     = jsonencode("${var.repository_url}:${var.ecr_image_tag}")
      version                   = jsonencode(var.ecr_image_tag)
      cloudwatch_log_group_name = jsonencode(aws_cloudwatch_log_group.default[count.index].name)
      region                    = jsonencode(local.region)
      spring_profiles_active    = jsonencode(var.spring_profiles_active)
      ssm_prefix                = jsonencode(var.ssm_prefix)
    }
  )

  # Resources
  cpu    = var.cpu
  memory = var.memory

  # IAM
  task_role_arn      = var.task_role_arn
  execution_role_arn = var.execution_role_arn

  # Configuration
  network_mode             = "awsvpc"
  requires_compatibilities = ["FARGATE"]
  tags                     = var.tags
}

resource "aws_ecs_service" "default" {
  count = var.create ? 1 : 0

  name            = var.name
  cluster         = var.cluster
  task_definition = aws_ecs_task_definition.default[count.index].arn

  # Resources
  desired_count       = var.desired_count
  scheduling_strategy = "REPLICA"

  # Configuration
  launch_type                        = "FARGATE"
  platform_version                   = "LATEST"
  deployment_minimum_healthy_percent = 100
  deployment_maximum_percent         = 200
  tags                               = var.tags

  network_configuration {
    subnets          = var.subnets
    security_groups  = var.security_groups
    assign_public_ip = true
  }

  lifecycle {
    create_before_destroy = true
  }
}

# Logging
resource "aws_cloudwatch_log_group" "default" {
  count = var.create ? 1 : 0

  name_prefix       = var.name
  retention_in_days = 365
  kms_key_id        = aws_kms_key.default_cloudwatch_log_group[count.index].arn
  tags              = var.tags
}

resource "aws_kms_key" "default_cloudwatch_log_group" {
  count = var.create ? 1 : 0

  description              = "KMS key used to encrypt ${var.name} ECS CloudWatch logs"
  key_usage                = "ENCRYPT_DECRYPT"
  customer_master_key_spec = "SYMMETRIC_DEFAULT"
  policy                   = data.aws_iam_policy_document.default_cloudwatch_log_group[count.index].json
  deletion_window_in_days  = 30
  is_enabled               = true
  enable_key_rotation      = true
  tags                     = var.tags
}

data "aws_iam_policy_document" "default_cloudwatch_log_group" {
  count = var.create ? 1 : 0

  # Grant account full control
  statement {
    principals {
      type        = "AWS"
      identifiers = ["arn:aws:iam::${local.account_id}:root"]
    }

    actions   = ["kms:*"]
    resources = ["*"]
  }

  # Delegate access to CloudWatch Logs
  statement {
    principals {
      type        = "Service"
      identifiers = ["logs.${local.region}.amazonaws.com"]
    }

    actions = [
      "kms:Encrypt*",
      "kms:Decrypt*",
      "kms:ReEncrypt*",
      "kms:GenerateDataKey*",
      "kms:Describe*",
    ]

    resources = ["*"]
  }
}
