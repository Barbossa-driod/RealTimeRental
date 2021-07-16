is_local = true

# Providers
aws_provider_region              = "us-east-1"
aws_provider_allowed_account_ids = ["959685249607"]
aws_provider_assume_role_arn     = "arn:aws:iam::959685249607:role/terraform"

# Remote state
terraform_infrastructure_workspace_name = "terraform-infrastructure-development-us-east-1"
terraform_sharedservices_workspace_name = "terraform-sharedservices-us-east-1"
terraform_legacy_sync_workspace_name    = "legacy-sync-connector-development-us-east-1"

# ECS
ecr_image_tag              = "latest"
ecs_assume_role_principals = ["961063916825"] # Root account id
ecs_cpu                    = 256
ecs_memory                 = 512
ecs_desired_count          = 0

spring_profiles_active                   = "local"
inbound_queue_visibility_timeout_seconds = 180