function New-TCConfigurationContentFile {
    param (
        [string]$TerraformContentDirectory
    )

    $ErrorActionPreference = 'Stop'
    $ProgressPreference = 'SilentlyContinue'

    $DateFormated = Get-Date -UFormat '%Y-%m-%d-%H%M%S%Z'
    $TerraformUploadFileName = "terraform-content-${DateFormated}.tar.gz"
    $TerraformUploadFilePath = Join-Path -Path $PWD -ChildPath $TerraformUploadFileName
    $TerraformContentDirectory = Resolve-Path -Path $TerraformContentDirectory
    $TarCommand = "tar -zcvf ${TerraformUploadFilePath} -C ${TerraformContentDirectory} ."
    Invoke-Expression -Command $TarCommand | Out-Null
    if ($LASTEXITCODE -ne 0) { throw "Running 'tar' command failed. Exit code: ${LASTEXITCODE}" }
    Write-Host "Terraform upload file: ${TerraformUploadFileName}"

    return $TerraformUploadFilePath
}
