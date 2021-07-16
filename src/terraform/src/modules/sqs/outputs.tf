output "inbound_queue_url" {
  value = aws_sqs_queue.inbound.id
}

output "inbound_queue_arn" {
  value = aws_sqs_queue.inbound.arn
}

output "inbound_deadletter_queue_url" {
  value = aws_sqs_queue.inbound_deadletter.id
}

output "inbound_deadletter_queue_arn" {
  value = aws_sqs_queue.inbound_deadletter.arn
}