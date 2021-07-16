output "ssm_prefix" {
  value = local.ssm_prefix
}

output "ssm_parameter_safely_api_username_arn" {
  value = aws_ssm_parameter.safely_api_username.arn
}

# Needed for password reset via AWS CLI
output "ssm_parameter_safely_api_password_name" {
  value = aws_ssm_parameter.safely_api_password.name
}

output "ssm_parameter_safely_api_password_arn" {
  value = aws_ssm_parameter.safely_api_password.arn
}

output "ssm_parameter_inbound_queue_url_arn" {
  value = aws_ssm_parameter.inbound_queue_url.arn
}
