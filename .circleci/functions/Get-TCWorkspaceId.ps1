function Get-TCWorkspaceId {
    param (
        [string]$TCOrganization,
        [string]$TCWorkspace,
        [string]$TCToken
    )

    $ErrorActionPreference = 'Stop'
    $ProgressPreference = 'SilentlyContinue'

    $TCWorkspaceIdGetResponse = Invoke-RestMethod `
        -Method Get `
        -Uri "https://app.terraform.io/api/v2/organizations/${TCOrganization}/workspaces/${TCWorkspace}" `
        -Headers @{ Authorization = "Bearer ${TCToken}"; 'Content-Type' = 'application/vnd.api+json' }

    $TCWorkspaceId = $TCWorkspaceIdGetResponse.data.id
    Write-Host "Terraform Cloud Workspace ID: ${TCWorkspaceId}"

    return $TCWorkspaceId
}
