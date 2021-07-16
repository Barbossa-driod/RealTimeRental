key            = "app/terraform.tfstate"
region         = "us-east-1"
bucket         = "dev-terraform-backend-state-160205188459-us-east-1"
dynamodb_table = "dev-terraform-backend-state-lock"
role_arn       = "arn:aws:iam::160205188459:role/dev-terraform"
encrypt        = true
acl            = "bucket-owner-full-control"
