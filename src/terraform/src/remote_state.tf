# Remote state
data "terraform_remote_state" "terraform-infrastructure" {
  backend = "remote"

  config = {
    organization = "safely"

    workspaces = {
      name = var.terraform_infrastructure_workspace_name
    }
  }
}

data "terraform_remote_state" "terraform-sharedservices" {
  backend = "remote"

  config = {
    organization = "safely"

    workspaces = {
      name = var.terraform_sharedservices_workspace_name
    }
  }
}

data "terraform_remote_state" "legacy-sync" {
  backend = "remote"

  config = {
    organization = "safely"

    workspaces = {
      name = var.terraform_legacy_sync_workspace_name
    }
  }
}
