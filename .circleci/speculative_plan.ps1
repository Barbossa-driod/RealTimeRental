param (
    [string]$TCOrganization = $env:TERRAFORM_CLOUD_ORGANIZATION,
    [string]$TCToken = $env:TERRAFORM_CLOUD_TOKEN,
    [string]$TerraformContentDirectory = $env:TERRAFORM_CONTENT_DIRECTORY
)

$ErrorActionPreference = 'Stop'
$ProgressPreference = 'SilentlyContinue'

# Param splat
$ParamSplat = @{
    TCOrganization            = $TCOrganization
    TCToken                   = $TCToken
    TerraformContentDirectory = $TerraformContentDirectory
}

# Parameter validation
# Since allowing environment variables as defaults, can't use built-in param validators
foreach ($ParamKey in $ParamSplat.Keys) {
    if ([string]::IsNullOrWhiteSpace($ParamSplat[$ParamKey])) {
        throw [System.ArgumentNullException]::new($ParamKey)
    }
}

# Load functions
$FunctionsPath = Join-Path -Path $PSScriptRoot -ChildPath 'functions'
Get-ChildItem -Path $FunctionsPath -Recurse -File -Filter '*.ps1' | `
    ForEach-Object { . $_.FullName }

Write-Host @"
`n=======================================================================
Terraform Cloud Organization: ${TCOrganization}
=======================================================================`n
"@

$TCWorkspaces = @('realtimerental-connector-development-us-east-1', 'realtimerental-connector-test-us-east-1', 'realtimerental-connector-production-us-east-1')

# Terraform API-driven Run Workflow
# https://www.terraform.io/docs/cloud/run/api.html

foreach ($TCWorkspace in $TCWorkspaces) {
    Write-Host "Creating speculative plan for workspace: ${TCWorkspace}"
    $ParamSplat['TCWorkspace'] = $TCWorkspace

    # Get workspace id
    $TCWorkspaceId = Get-TCWorkspaceId @ParamSplat

    # Create Terraform .tar.gz file for upload
    $TerraformUploadFilePath = New-TCConfigurationContentFile @ParamSplat

    # Create new Terraform configuration version and upload configuration
    $TCConfigurationVersionId = New-TCConfigurationVersion -WorkspaceId $TCWorkspaceId `
        -ConfigurationFilePath $TerraformUploadFilePath -Speculative $true @ParamSplat

    # Create new Terraform run
    $TCRunId = New-TCRun -WorkspaceId $TCWorkspaceId `
        -ConfigurationVersionId $TCConfigurationVersionId @ParamSplat

    # Wait for run to be planned
    Wait-TCRun -TCRunId $TCRunId -DesiredStatus 'planned_and_finished' @ParamSplat
}
