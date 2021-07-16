resource "null_resource" "terraform_cloud_trigger" {
  count = var.is_local ? 0 : 1

  triggers = {
    timestamp = timestamp()
  }
}
