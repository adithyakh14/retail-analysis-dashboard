$ErrorActionPreference = "Stop"

$projectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $projectRoot

if (-not (Test-Path ".\mvnw.cmd")) {
    throw "Missing Maven Wrapper. Ensure mvnw.cmd exists in the project root."
}

Write-Host "Retail backend API is starting at http://localhost:8080"
& ".\mvnw.cmd" spring-boot:run
