$ErrorActionPreference = "Stop"

$projectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $projectRoot

$javaSources = Get-ChildItem -Path "src\main\java" -Recurse -Filter "*.java" | ForEach-Object { $_.FullName }

if (-not (Test-Path "target\classes")) {
    New-Item -ItemType Directory -Path "target\classes" | Out-Null
}

javac -d "target\classes" $javaSources
Copy-Item -Recurse -Force "src\main\resources\*" "target\classes\"

Write-Host "Retail dashboard is starting at http://localhost:8080"
java -cp "target/classes" com.retailproject.RetailDashboardServer
