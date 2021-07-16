locals {
  ssm_prefix = "/${var.ssm_prefix}config"
}

resource "aws_ssm_parameter" "safely_api_username" {
  name  = "${local.ssm_prefix}/${var.name}/safely/api/username"
  type  = "String"
  value = var.safely_api_username
  tags  = var.tags
}

resource "aws_ssm_parameter" "safely_api_password" {
  name  = "${local.ssm_prefix}/${var.name}/safely/api/password"
  type  = "SecureString"
  value = var.safely_api_password # updated manually
  tags  = var.tags

  lifecycle {
    ignore_changes = [value]
  }
}

resource "aws_ssm_parameter" "inbound_queue_url" {
  name  = "${local.ssm_prefix}/${var.name}/safely/queue/inbound/name"
  type  = "String"
  value = var.inbound_queue_url
  tags  = var.tags
}

resource "aws_ssm_parameter" "inbound_queue_visibility" {
  name  = "${local.ssm_prefix}/${var.name}/safely/queue/inbound/visibility"
  type  = "String"
  value = var.inbound_queue_visibility
  tags  = var.tags
}

resource "aws_ssm_parameter" "outbound_queue_url" {
  name  = "${local.ssm_prefix}/${var.name}/safely/queue/outbound/name"
  type  = "String"
  value = var.outbound_queue_url
  tags  = var.tags
}
