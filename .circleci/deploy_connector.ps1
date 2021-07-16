param (
    [string]$Version = $env:VERSION,
    [string]$TCOrganization = $env:TERRAFORM_CLOUD_ORGANIZATION,
    [string]$TCWorkspace = $env:TERRAFORM_CLOUD_WORKSPACE,
    [string]$TCToken = $env:TERRAFORM_CLOUD_TOKEN,
    [string]$TerraformContentDirectory = $env:TERRAFORM_CONTENT_DIRECTORY,
    [string]$AwsAccessKey = $env:AWS_ACCESS_KEY_ID,
    [string]$AwsSecretAccessKey = $env:AWS_SECRET_ACCESS_KEY
)

$ErrorActionPreference = 'Stop'
$ProgressPreference = 'SilentlyContinue'

# Param splat
$ParamSplat = @{
    Version                   = $Version
    TCOrganization            = $TCOrganization
    TCWorkspace               = $TCWorkspace
    TCToken                   = $TCToken
    TerraformContentDirectory = $TerraformContentDirectory
    AwsAccessKey              = $AwsAccessKey
    AwsSecretAccessKey        = $AwsSecretAccessKey
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

# Ensure AWS environment variables are set correctly
Reset-AWSEnvironmentCredentials @ParamSplat

Write-Host @"
`n=======================================================================
Version:                      ${Version}
Terraform Cloud Organization: ${TCOrganization}
Terraform Cloud Workspace:    ${TCWorkspace}
=======================================================================`n
"@

# Terraform API-driven Run Workflow
# https://www.terraform.io/docs/cloud/run/api.html

# Get workspace id
$TCWorkspaceId = Get-TCWorkspaceId @ParamSplat

# Create Terraform .tar.gz file for upload
$TerraformUploadFilePath = New-TCConfigurationContentFile @ParamSplat

# Update Docker image tag variable
Set-TCVariable -Name 'ecr_image_tag' -Value $Version @ParamSplat

# Create new Terraform configuration version and upload configuration
$TCConfigurationVersionId = New-TCConfigurationVersion -WorkspaceId $TCWorkspaceId `
    -ConfigurationFilePath $TerraformUploadFilePath @ParamSplat

# Create new Terraform run
$TCRunId = New-TCRun -WorkspaceId $TCWorkspaceId `
    -ConfigurationVersionId $TCConfigurationVersionId @ParamSplat

# Wait for run to be applied
Wait-TCRun -TCRunId $TCRunId @ParamSplat

# Ensure AWS environment variables are set correctly
Reset-AWSEnvironmentCredentials @ParamSplat
