variable "create" {
  type = bool
}

variable "name" {
  type = string
}

variable "tags" {
  type = map
}

variable "repository_url" {
  type = string
}

variable "ecr_image_tag" {
  type = string
}

variable "cpu" {
  type = number
}

variable "memory" {
  type = number
}

variable "task_role_arn" {
  type = string
}

variable "execution_role_arn" {
  type = string
}

variable "cluster" {
  type = string
}

variable "desired_count" {
  type = number
}

variable "subnets" {
  type = list
}

variable "security_groups" {
  type = list
}

variable "spring_profiles_active" {
  type = string
}

variable "ssm_prefix" {
  type = string
}