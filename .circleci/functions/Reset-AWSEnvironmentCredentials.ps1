function Reset-AWSEnvironmentCredentials {
    param (
        [string]$AwsAccessKey,
        [string]$AwsSecretAccessKey
    )

    $ErrorActionPreference = 'Stop'
    $ProgressPreference = 'SilentlyContinue'

    $env:AWS_ACCESS_KEY_ID = $AwsAccessKey
    $env:AWS_SECRET_ACCESS_KEY = $AwsSecretAccessKey
    Remove-Item -Path 'Env:\AWS_SESSION_TOKEN' -Force -Confirm:$false -ErrorAction SilentlyContinue
}
