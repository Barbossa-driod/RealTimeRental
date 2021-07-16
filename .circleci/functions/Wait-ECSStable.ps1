function Wait-ECSStable {
    param (
        [ValidateNotNullOrEmpty()]
        [string]$RoleArn,
        [ValidateNotNullOrEmpty()]
        [string]$ServiceName,
        [ValidateNotNullOrEmpty()]
        [string]$ClusterArn,
        [ValidateNotNullOrEmpty()]
        [string]$Region
    )
    
    $ErrorActionPreference = 'Stop'
    $ProgressPreference = 'SilentlyContinue'

    Write-Host "Assuming ECS service deployment IAM role: ${RoleArn}"
    $AssumedRoleCreds = (Use-STSRole -RoleArn $RoleArn -RoleSessionName 'service-stable' -Region $Region).Credentials

    # Load env vars for cli
    $env:AWS_ACCESS_KEY_ID = $AssumedRoleCreds.AccessKeyId
    $env:AWS_SECRET_ACCESS_KEY = $AssumedRoleCreds.SecretAccessKey
    $env:AWS_SESSION_TOKEN = $AssumedRoleCreds.SessionToken

    Write-Host "Waiting for the ECS service to become stable: ${ServiceName}"
    aws ecs wait services-stable --cluster $ClusterArn --services $ServiceName --region $Region
    $AwsCliExitCode = $LASTEXITCODE

    if ($AwsCliExitCode -eq 255) {
        throw "Waiting for ECS service timed out after 40 failed checks. Exit code: ${AwsCliExitCode}"
    }

    if ($AwsCliExitCode -ne 0) {
        throw "Waiting for ECS service failed. Exit code: ${AwsCliExitCode}"
    }
    Write-Host 'ECS service deployment complete!'
}
