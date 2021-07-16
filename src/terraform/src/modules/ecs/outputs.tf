output "service_name" {
  value = concat(aws_ecs_service.default.*.name, [""])[0]
}

output "cloudwatch_log_group_arn" {
  value = concat(aws_cloudwatch_log_group.default.*.arn, [""])[0]
}

output "ecs_task_definition_arn" {
  value = concat(aws_ecs_task_definition.default.*.arn, [""])[0]
}
