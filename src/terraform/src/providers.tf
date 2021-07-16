# Providers
provider "aws" {
  region              = var.aws_provider_region
  allowed_account_ids = var.aws_provider_allowed_account_ids

  assume_role {
    role_arn = var.aws_provider_assume_role_arn
  }
}
