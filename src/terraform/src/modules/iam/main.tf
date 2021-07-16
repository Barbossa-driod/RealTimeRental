data "aws_caller_identity" "current" {}
data "aws_region" "current" {}

# ecs task execution role
resource "aws_iam_role" "ecs_task_execution_role" {
  name_prefix        = "realtimerental_ecs_task_exec"
  tags               = var.tags
  assume_role_policy = data.aws_iam_policy_document.ecs_task_execution_role_assume_role_policy.json
  lifecycle {
    create_before_destroy = true
  }
}

resource "aws_iam_policy" "ecs_task_execution_role" {
  name_prefix = "realtimerental_ecs_task_exec"
  policy      = data.aws_iam_policy_document.ecs_task_execution_role.json
}

resource "aws_iam_role_policy_attachment" "ecs_task_execution_role" {
  role       = aws_iam_role.ecs_task_execution_role.name
  policy_arn = aws_iam_policy.ecs_task_execution_role.arn
}

data "aws_iam_policy_document" "ecs_task_execution_role_assume_role_policy" {
  statement {
    actions = ["sts:AssumeRole"]

    principals {
      type        = "Service"
      identifiers = ["ecs-tasks.amazonaws.com"]
    }
  }
}

data "aws_iam_policy_document" "ecs_task_execution_role" {
  statement {
    actions = [
      "ecr:GetAuthorizationToken",
      "ecr:BatchCheckLayerAvailability",
      "ecr:GetDownloadUrlForLayer",
      "ecr:BatchGetImage",
    ]

    resources = ["*"]
  }

  dynamic "statement" {
    for_each = compact([var.ecs_cloudwatch_log_group_arn])
    content {
      actions = [
        "logs:CreateLogStream",
        "logs:PutLogEvents",
      ]

      resources = ["${statement.value}:*"]
    }
  }
}

# ecs task role
resource "aws_iam_role" "ecs_task_role" {
  name_prefix          = "realtimerental_ecs_task"
  tags                 = var.tags
  assume_role_policy   = data.aws_iam_policy_document.ecs_task_role_assume_role_policy.json
  max_session_duration = 43200 # 12 hours

  lifecycle {
    create_before_destroy = true
  }
}

data "aws_iam_policy_document" "ecs_task_role_assume_role_policy" {
  statement {
    actions = ["sts:AssumeRole"]

    principals {
      type        = "Service"
      identifiers = ["ecs-tasks.amazonaws.com"]
    }

    principals {
      type        = "AWS"
      identifiers = var.ecs_assume_role_principals
    }
  }
}

resource "aws_iam_policy" "ecs_task_role" {
  policy = data.aws_iam_policy_document.ecs_task_role.json
}

resource "aws_iam_role_policy_attachment" "ecs_task_role" {
  role       = aws_iam_role.ecs_task_role.name
  policy_arn = aws_iam_policy.ecs_task_role.arn
}

data "aws_iam_policy_document" "ecs_task_role" {
  # SSM
  statement {
    actions   = ["ssm:GetParametersByPath"]
    resources = ["arn:aws:ssm:${data.aws_region.current.name}:${data.aws_caller_identity.current.account_id}:parameter${var.ssm_prefix}/*"]
  }

  # SQS - Consumer
  statement {
    actions = [
      "sqs:GetQueueAttributes",
      "sqs:GetQueueUrl",
      "sqs:ReceiveMessage",
      "sqs:DeleteMessage*",
      "sqs:PurgeQueue",
      "sqs:ChangeMessageVisibility*"
    ]
    resources = [var.inbound_queue_arn]
  }

  # SQS - Producer
  statement {
    actions = [
      "sqs:GetQueueAttributes",
      "sqs:GetQueueUrl",
      "sqs:SendMessage*"
    ]
    resources = [var.outbound_queue_arn]
  }
}

# ecs deployment role
resource "aws_iam_role" "ecs_deployment_role" {
  name_prefix        = "deploy_pipeline" # Required by deploy pipeline users to assume
  tags               = var.tags
  assume_role_policy = data.aws_iam_policy_document.ecs_deployment_role_assume_role_policy.json

  lifecycle {
    create_before_destroy = true
  }
}

resource "aws_iam_policy" "ecs_deployment_role" {
  policy = data.aws_iam_policy_document.ecs_deployment_role.json
}

resource "aws_iam_role_policy_attachment" "ecs_deployment_role" {
  role       = aws_iam_role.ecs_deployment_role.name
  policy_arn = aws_iam_policy.ecs_deployment_role.arn
}

data "aws_iam_policy_document" "ecs_deployment_role_assume_role_policy" {
  statement {
    actions = ["sts:AssumeRole"]

    principals {
      type        = "AWS"
      identifiers = ["961063916825"] # root account ID
    }
  }
}

data "aws_iam_policy_document" "ecs_deployment_role" {
  statement {
    actions   = ["ecs:DescribeServices"]
    resources = ["*"]
  }
}
