variable "is_local" {
  type    = bool
  default = false
}

# Providers
variable "aws_provider_region" {
  type        = string
  description = "AWS provider region"
}

variable "aws_provider_allowed_account_ids" {
  type        = list
  description = "List of allowed, white listed, AWS account IDs"
}

variable "aws_provider_assume_role_arn" {
  type        = string
  description = "ARN of the role to assume"
}

# Remote state
variable "terraform_infrastructure_workspace_name" {
  type        = string
  description = "Terraform Cloud workspace name for terraform-infrastructure state"
}

variable "terraform_sharedservices_workspace_name" {
  type        = string
  description = "Terraform Cloud workspace name for terraform-sharedservices state"
}

variable "terraform_legacy_sync_workspace_name" {
  type        = string
  description = "Terraform Cloud workspace name for terraform-legacy-sync state"
}

# ECS
variable "ecr_image_tag" {
  type        = string
  description = "Docker image tag to deploy"
}

variable "ecs_assume_role_principals" {
  type        = list
  description = "ARNs of principals allows to assume the ecs task role"
  default     = []
}

variable "ecs_cpu" {
  type        = number
  description = "ECS task definition CPU"
}

variable "ecs_memory" {
  type        = number
  description = "ECS task definition memory"
}

variable "ecs_desired_count" {
  type        = number
  description = "ECS service desired count"
}

variable "spring_profiles_active" {
  type        = string
  description = "Spring Boot Profiles to be used by the service, comma separated string"
}

variable "inbound_queue_visibility_timeout_seconds" {
  type        = number
  description = "The number of seconds the consumer can processes a message without refreshing the message timeout or finishing before the queue assumes failure"
}