module "default" {
  source  = "terraform-aws-modules/security-group/aws"
  version = "3.16.0"

  create = var.create

  name        = var.name
  description = "Security group used by ${var.name}"
  vpc_id      = var.vpc_id

  egress_cidr_blocks      = ["0.0.0.0/0"]
  egress_rules            = ["all-all"]
  egress_ipv6_cidr_blocks = null
}
