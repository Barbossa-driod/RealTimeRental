output "ecs_cluster_arn" {
  value = local.ecs_cluster_arn
}

output "ecs_service_deployment_role_arn" {
  value = module.iam.ecs_service_deployment_role_arn
}

output "ecs_task_role_arn" {
  value = module.iam.ecs_task_role_arn
}

output "region" {
  value = var.aws_provider_region
}

output "ssm_prefix" {
  value = module.ssm.ssm_prefix
}

output "ssm_parameter_safely_api_password_name" {
  value = module.ssm.ssm_parameter_safely_api_password_name
}

output "sqs_inbound_queue_url" {
  value = module.sqs.inbound_queue_url
}

output "sqs_inbound_queue_arn" {
  value = module.sqs.inbound_queue_arn
}

output "sqs_inbound_deadletter_queue_url" {
  value = module.sqs.inbound_deadletter_queue_url
}

output "sqs_inbound_deadletter_queue_arn" {
  value = module.sqs.inbound_deadletter_queue_arn
}
