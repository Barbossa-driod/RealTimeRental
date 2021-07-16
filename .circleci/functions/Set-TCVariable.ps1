function Set-TCVariable {
    param (
        [string]$Name,
        [string]$Value,
        [string]$TCOrganization,
        [string]$TCWorkspace,
        [string]$TCToken
    )

    $ErrorActionPreference = 'Stop'
    $ProgressPreference = 'SilentlyContinue'

    $TCVariableListResponse = Invoke-RestMethod `
        -Method Get `
        -Uri "https://app.terraform.io/api/v2/vars/?filter%5Borganization%5D%5Bname%5D=${TCOrganization}&filter%5Bworkspace%5D%5Bname%5D=${TCWorkspace}" `
        -Headers @{ Authorization = "Bearer ${TCToken}"; 'Content-Type' = 'application/vnd.api+json' }
    
    $TCVariableId = $TCVariableListResponse.data | `
        Where-Object { $_.attributes.key -eq $Name } | `
        Select-Object -ExpandProperty 'id'
    
    Write-Host "Terraform Cloud ${Name} variable ID: ${TCVariableId}"

    $TCVariableUpdateBody = @{
        data = @{
            id         = $TCVariableId
            type       = 'vars'
            attributes = @{
                value       = $Value
                description = "Value updated by CircleCI ${env:CIRCLE_PROJECT_REPONAME}:${env:CIRCLE_BRANCH}:${env:CIRCLE_BUILD_NUM}"
            }
        }
    } | ConvertTo-Json -Depth 10 -Compress

    Invoke-RestMethod `
        -Method Patch `
        -Uri "https://app.terraform.io/api/v2/vars/${TCVariableId}" `
        -Headers @{ Authorization = "Bearer ${TCToken}"; 'Content-Type' = 'application/vnd.api+json' } `
        -Body $TCVariableUpdateBody | Out-Null
    
    Write-Host "Successfully updated ${Name} variable value: ${Value}"
}
