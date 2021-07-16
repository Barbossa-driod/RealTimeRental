variable "tags" {
  type = map
}

variable "ecs_assume_role_principals" {
  type = list
}

variable "ecs_cloudwatch_log_group_arn" {
  type = string
}

variable "ssm_prefix" {
  type = string
}

variable "inbound_queue_arn" {
  type = string
}

variable "outbound_queue_arn" {
  type = string
}