function Get-TCWorkspaceOutputs {
    param (
        [string]$WorkspaceId,
        [string]$TCToken
    )

    $ErrorActionPreference = 'Stop'
    $ProgressPreference = 'SilentlyContinue'

    $WorkspaceOutputs = @{ }

    $TCCurrentStateVersionGetResponse = Invoke-RestMethod `
        -Method Get `
        -Uri "https://app.terraform.io/api/v2/workspaces/${WorkspaceId}/current-state-version?include=outputs" `
        -Headers @{ Authorization = "Bearer ${TCToken}"; 'Content-Type' = 'application/vnd.api+json' }

    
    foreach ($Output in $TCCurrentStateVersionGetResponse.included.attributes) {
        $WorkspaceOutputs.Add($Output.name, $Output.value)
    }

    return $WorkspaceOutputs
}
