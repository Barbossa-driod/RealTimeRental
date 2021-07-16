<#
.SYNOPSIS

Deploys app to the optional environments.

.DESCRIPTION

Calls the CircleCI pipeline for app, deploying to development and production environments in sequence.
The version by default will the the HEAD of master.

.PARAMETER CircleCIToken
Token to authenticate with CircleCI. By default will use CIRCLE_CI_TOKEN environment variable.

.PARAMETER Version
Specifies the version to deploy. Optional, defaults to HEAD of master.

.PARAMETER SkipBuild
Boolean to skip CI build. Optional, defaults to $true

.PARAMETER SkipDevDeploy
Boolean to skip deployment to development environment. Optional, defaults to $false

.PARAMETER SkipTestDeploy
Boolean to skip deployment to test environment. Optional, defaults to $false

.PARAMETER SkipProdDeploy
Boolean to skip deployment to production environment. Optional, defaults to $false

.INPUTS

None. You cannot pipe objects to this script.

.OUTPUTS

None.

.EXAMPLE

PS> ./deploy.ps1 -CircleCIToken <my-token> -SkipBuild $false
#>

param (
    [string]$CircleCIToken = $env:CIRCLE_CI_TOKEN,
    [string]$Version,
    [bool]$SkipBuild = $true,
    [bool]$SkipDevDeploy = $false,
    [bool]$SkipTestDeploy = $false,
    [bool]$SkipProdDeploy = $false
)

$ErrorActionPreference = 'Stop'
$ProgressPreference = 'SilentlyContinue'

# Validate token provided
if ([string]::IsNullOrWhiteSpace($CircleCIToken)) {
    throw [System.ArgumentNullException]::new('CircleCIToken')
}

# https://circleci.com/docs/api/v2/#trigger-a-new-pipeline
$Uri = "https://circleci.com/api/v2/project/gh/Safely-AI/RealTimeRentalConnector/pipeline"
$EmptyPassword = [System.Security.SecureString]::new()
$Creds = [System.Management.Automation.PSCredential]::new($CircleCIToken, $EmptyPassword)

$Headers = @{
    'Content-Type' = 'application/json'
    Accept         = 'application/json'
}

$Body = @{
    branch     = 'master'
    parameters = @{
        version          = $Version # When version is empty, pipeline uses HEAD of master
        skip_build       = $SkipBuild
        skip_dev_deploy  = $SkipDevDeploy
        skip_test_deploy  = $SkipTestDeploy
        skip_prod_deploy = $SkipProdDeploy
    }
} | ConvertTo-Json -Depth 10 -Compress

Invoke-RestMethod `
    -Method Post `
    -Uri $Uri `
    -Credential $Creds `
    -Authentication Basic `
    -Headers $Headers `
    -Body $Body
