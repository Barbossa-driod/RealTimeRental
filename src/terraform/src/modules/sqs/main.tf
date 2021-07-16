resource "aws_sqs_queue" "inbound" {
  name_prefix = var.name
  tags        = var.tags

  visibility_timeout_seconds = var.visibility_timeout_seconds
  message_retention_seconds  = var.message_retention_seconds
  receive_wait_time_seconds  = var.receive_wait_time_seconds

  redrive_policy = jsonencode({
    deadLetterTargetArn = aws_sqs_queue.inbound_deadletter.arn
    maxReceiveCount     = var.retry_count
  })
}

resource "aws_sqs_queue" "inbound_deadletter" {
  name_prefix = "${var.name}-deadletter"
  tags        = var.tags

  visibility_timeout_seconds = var.visibility_timeout_seconds
  message_retention_seconds  = var.message_retention_seconds
  receive_wait_time_seconds  = var.receive_wait_time_seconds
}

# TODO: Configure alarm for deadletter queue