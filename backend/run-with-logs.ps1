# Script to run Spring Boot backend with logs saved to file
# Usage: .\run-with-logs.ps1

$env:TWELVE_DATA_API_KEY = "e5b53764e5c1413d9e480c6e88b0a7a6"

$logFile = "backend-logs-$(Get-Date -Format 'yyyy-MM-dd-HHmmss').txt"

Write-Host "Starting backend with logs saved to: $logFile" -ForegroundColor Green
Write-Host "Logs will be displayed AND saved to file" -ForegroundColor Yellow
Write-Host ""

# Run Maven and tee output to both console and file
.\mvnw.cmd spring-boot:run | Tee-Object -FilePath $logFile
