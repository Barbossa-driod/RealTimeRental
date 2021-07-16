function New-TCConfigurationVersion {
    param (
        [string]$WorkspaceId,
        [string]$ConfigurationFilePath,
        [bool]$Speculative = $false,
        [string]$TCToken
    )

    $TCConfigurationVersionPostBody = @{
        data = @{
            type       = 'configuration-versions'
            attributes = @{
                'auto-queue-runs' = $false
                speculative       = $Speculative
            }
        }
    } | ConvertTo-Json -Depth 10 -Compress

    $TCConfigurationVersionPostResponse = Invoke-RestMethod `
        -Method Post `
        -Uri "https://app.terraform.io/api/v2/workspaces/${WorkspaceId}/configuration-versions" `
        -Headers @{ Authorization = "Bearer ${TCToken}"; 'Content-Type' = 'application/vnd.api+json' } `
        -Body $TCConfigurationVersionPostBody
    
    $TCConfigurationVersionId = $TCConfigurationVersionPostResponse.data.id
    $TCConfigurationVersionUploadUrl = $TCConfigurationVersionPostResponse.data.attributes.'upload-url'

    Write-Host "Terraform Cloud Configuration Version ID: ${TCConfigurationVersionId}"

    # Upload
    Invoke-RestMethod `
        -Method Put `
        -Uri $TCConfigurationVersionUploadUrl `
        -Headers @{ 'Content-Type' = 'application/octet-stream' } `
        -InFile $ConfigurationFilePath | Out-Null
    
    Write-Host 'Successfully uploaded Terraform configuration!'

    return $TCConfigurationVersionId
}
