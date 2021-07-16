variable "tags" {
  type = map
}

variable "ssm_prefix" {
  type = string
}

variable "name" {
  type = string
}

variable "safely_api_username" {
  type = string
}

variable "safely_api_password" {
  type    = string
  default = "CHANGE_ME"
}

variable "inbound_queue_url" {
  type = string
}

variable "inbound_queue_visibility" {
  type = number
}

variable "outbound_queue_url" {
  type = string
}