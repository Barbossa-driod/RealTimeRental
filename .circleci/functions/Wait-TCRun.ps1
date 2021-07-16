function Wait-TCRun {
    param (
        [string]$TCRunId,
        [string]$DesiredStatus = 'applied',
        [string]$TCToken
    )

    $ErrorActionPreference = 'Stop'
    $ProgressPreference = 'SilentlyContinue'

    $TCRunTimeout = (Get-Date).AddMinutes(30)

    do {
        if ((Get-Date) -gt $TCRunTimeout) {
            throw "Timeout for Terraform Cloud Run ${TCRunId} exceeded"
        }

        Start-Sleep -Seconds 10

        # Get run details
        $TCRunDetailsGetResponse = Invoke-RestMethod `
            -Method Get `
            -Uri "https://app.terraform.io/api/v2/runs/${TCRunId}" `
            -Headers @{ Authorization = "Bearer ${TCToken}" }
        
        $TCRunStatus = $TCRunDetailsGetResponse.data.attributes.status

        $AllowedStatuses = @(
            'pending',
            'plan_queued',
            'planning',
            'planned',
            'planned_and_finished',
            'apply_queued',
            'applying',
            'applied'
        )
    
        if ($TCRunStatus -notin $AllowedStatuses) {
            throw "Terraform Cloud Run ${TCRunId} unexpected status: ${TCRunStatus}"
        }

        Write-Host "Terraform Cloud Run ${TCRunId} status: ${TCRunStatus}"
    } until ($TCRunStatus -eq $DesiredStatus)

    Write-Host "Successfully ran Terraform Cloud Run!"
}
