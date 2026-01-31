$ErrorActionPreference = "Stop"

# Configuration
$ARCHIVE_PATH = "C:\projects\Moshimo\archive\stocks"
$API_BASE_URL = "http://localhost:8080/api/admin"

# 20 MVP stocks from PROJECT_BRIEF.md
$MVP_STOCKS = @(
    # "SPY",
    # "QQQ",
    "GOOGL",
    "AAPL",
    "MSFT",
    "NVDA",
    "TSLA",
    "AMZN",
    "META",
    "BRK.B",
    "JPM",
    "V",
    "WMT",
    "JNJ",
    "PG",
    "XOM",
    "UNH",
    "MA",
    "HD",
    "CVX"
)

Write-Host "===================================================" -ForegroundColor Cyan
Write-Host "Moshimo MVP Stock Data Import Script" -ForegroundColor Cyan
Write-Host "===================================================" -ForegroundColor Cyan
Write-Host ""

# Check if backend is running
Write-Host "Checking backend health..." -ForegroundColor Yellow
try {
    $health = Invoke-RestMethod -Uri "http://localhost:8080/api/health" -Method Get -ErrorAction Stop
    Write-Host "Backend is running" -ForegroundColor Green
    Write-Host ""
} catch {
    Write-Host "Backend is not running" -ForegroundColor Red
    Write-Host "Please start the backend first:" -ForegroundColor Yellow
    Write-Host "cd C:\projects\Moshimo\backend" -ForegroundColor Yellow
    Write-Host ".\mvnw.cmd spring-boot:run" -ForegroundColor Yellow
    exit 1
}

# Import each stock
$totalImported = 0
$totalSkipped = 0
$totalFailed = 0
$successCount = 0
$failCount = 0

foreach ($symbol in $MVP_STOCKS) {
    Write-Host "Processing $symbol..." -ForegroundColor Cyan
    
    $fileName = $symbol
    $csvPath = "$ARCHIVE_PATH\$fileName.csv"
    
    if (-not (Test-Path $csvPath)) {
        Write-Host "  CSV file not found: $csvPath" -ForegroundColor Red
        $failCount++
        continue
    }
    
    Write-Host "  Found: $csvPath" -ForegroundColor Gray
    
    try {
        $body = @{ filePath = $csvPath } | ConvertTo-Json
        $response = Invoke-RestMethod -Uri "$API_BASE_URL/import-csv" -Method Post -Body $body -ContentType "application/json" -ErrorAction Stop
        
        Write-Host "  Imported: $($response.importedPrices) prices" -ForegroundColor Green
        Write-Host "  Skipped: $($response.skippedPrices) (already exist)" -ForegroundColor Yellow
        
        if ($response.failedLines -gt 0) {
            Write-Host "  Failed: $($response.failedLines) lines" -ForegroundColor Red
        }
        
        $totalImported += $response.importedPrices
        $totalSkipped += $response.skippedPrices
        $totalFailed += $response.failedLines
        $successCount++
    } catch {
        Write-Host "  API call failed: $($_.Exception.Message)" -ForegroundColor Red
        $failCount++
    }
    
    Write-Host ""
    Start-Sleep -Milliseconds 500
}

# Summary
Write-Host "===================================================" -ForegroundColor Cyan
Write-Host "Import Summary" -ForegroundColor Cyan
Write-Host "===================================================" -ForegroundColor Cyan
Write-Host "Stocks processed: $successCount succeeded, $failCount failed" -ForegroundColor White
Write-Host "Total prices imported: $totalImported" -ForegroundColor Green
Write-Host "Total prices skipped: $totalSkipped" -ForegroundColor Yellow

if ($totalFailed -gt 0) {
    Write-Host "Total lines failed: $totalFailed" -ForegroundColor Red
} else {
    Write-Host "Total lines failed: $totalFailed" -ForegroundColor Gray
}

Write-Host ""

# Verify database
Write-Host "Verifying database..." -ForegroundColor Yellow
try {
    $health = Invoke-RestMethod -Uri "http://localhost:8080/api/health" -Method Get -ErrorAction Stop
    Write-Host "Database status: $($health.status)" -ForegroundColor Green
    Write-Host "Stocks: $($health.database.totalStocks)" -ForegroundColor White
    Write-Host "Price records: $($health.database.totalPriceRecords)" -ForegroundColor White
} catch {
    Write-Host "Could not verify database" -ForegroundColor Red
}

Write-Host ""
Write-Host "===================================================" -ForegroundColor Cyan
Write-Host "Import Complete!" -ForegroundColor Green
Write-Host "===================================================" -ForegroundColor Cyan