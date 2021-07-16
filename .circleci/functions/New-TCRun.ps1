function New-TCRun {
    param (
        [string]$WorkspaceId,
        [string]$ConfigurationVersionId,
        [string]$TCToken
    )

    $ErrorActionPreference = 'Stop'
    $ProgressPreference = 'SilentlyContinue'

    $TCRunPostBody = @{
        data = @{
            attributes    = @{
                message = "Queued by CircleCI ${env:CIRCLE_PROJECT_REPONAME}:${env:CIRCLE_BRANCH}:${env:CIRCLE_BUILD_NUM}"
            }
            relationships = @{
                workspace               = @{
                    data = @{
                        id = $WorkspaceId
                    }
                }
                'configuration-version' = @{
                    data = @{
                        id = $ConfigurationVersionId
                    }
                }
            }
        }
    } | ConvertTo-Json -Depth 10 -Compress

    try {
        $TCRunPostResponse = Invoke-RestMethod `
            -Method Post `
            -Uri 'https://app.terraform.io/api/v2/runs' `
            -Headers @{ Authorization = "Bearer ${TCToken}"; 'Content-Type' = 'application/vnd.api+json' } `
            -Body $TCRunPostBody
    }
    catch {
        Write-Warning $_.Exception.Message
        Write-Warning "Error creating run, retrying in 5 seconds..."
        Start-Sleep -Seconds 5
        $TCRunPostResponse = Invoke-RestMethod `
            -Method Post `
            -Uri 'https://app.terraform.io/api/v2/runs' `
            -Headers @{ Authorization = "Bearer ${TCToken}"; 'Content-Type' = 'application/vnd.api+json' } `
            -Body $TCRunPostBody
    }
    
    $TCRunId = $TCRunPostResponse.data.id
    Write-Host "Terraform Cloud Run ID: ${TCRunId}"

    return $TCRunId
}
