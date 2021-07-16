function Install-AWSDependencies {
    $ErrorActionPreference = 'Stop'
    $ProgressPreference = 'SilentlyContinue'

    # https://docs.aws.amazon.com/cli/latest/userguide/install-cliv2-linux.html
    Write-Host 'Installing AWS CLI v2...'
    apt-get update -y | Out-Null
    if ($LASTEXITCODE -ne 0) { throw "apt-get update failed. Exit code: ${LASTEXITCODE}" }
    apt-get install unzip -y | Out-Null
    if ($LASTEXITCODE -ne 0) { throw "apt-get install unzip failed. Exit code: ${LASTEXITCODE}" }
    $AwsCliDownloadPath = '/tmp/awscliv2.zip'
    Invoke-RestMethod `
        -Method Get `
        -Uri 'https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip' `
        -OutFile $AwsCliDownloadPath | Out-Null

    unzip -o $AwsCliDownloadPath -d /tmp/awscliv2 | Out-Null
    if ($LASTEXITCODE -ne 0) { throw "unzip failed. Exit code: ${LASTEXITCODE}" }
    /tmp/awscliv2/aws/install -u | Out-Null
    if ($LASTEXITCODE -ne 0) { throw "AWS CLI install failed. Exit code: ${LASTEXITCODE}" }
    Write-Host "Successfully installed AWS CLI version $(aws --version)!"
    if ($LASTEXITCODE -ne 0) { throw "aws --version failed. Exit code: ${LASTEXITCODE}" }

    Write-Host 'Installing AWS PowerShell Modules...'
    $RequiredAWSModules = @(
        'AWS.Tools.Common',
        'AWS.Tools.SecurityToken',
        'AWS.Tools.ECS',
        'AWS.Tools.S3',
        'AWS.Tools.CloudFront'
    )

    foreach ($RequiredAWSModule in $RequiredAWSModules) {
        Install-Module -Name $RequiredAWSModule -Force -Confirm:$false | Out-Null
        Import-Module -Name $RequiredAWSModule -Force | Out-Null
        Get-Module -Name $RequiredAWSModule -ListAvailable | Select-Object -Property Name, Version
    }

    Write-Host 'Successfully installed AWS PowerShell modules!'
}
