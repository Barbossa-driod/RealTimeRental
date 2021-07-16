variable "name" {
  type = string
}

variable "tags" {
  type = map
}

variable "visibility_timeout_seconds" {
  type = number
}

variable "message_retention_seconds" {
  type = number
}

variable "receive_wait_time_seconds" {
  type    = number
  default = 20
}

variable "retry_count" {
  type    = number
  default = 3
}