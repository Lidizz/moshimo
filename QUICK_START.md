# Quick Start Guide - Import Historical Data

## Prerequisites ✅
- ✅ Backend compiled successfully
- ✅ All compilation errors fixed
- ✅ PostgreSQL running in Docker
- ✅ 20 stocks seeded in database
- ✅ Archive folder has CSV files (1980-2024 data)

---

## Step-by-Step Instructions

### 1. Start the Backend

Open PowerShell terminal:

```powershell
cd C:\projects\Moshimo\backend
.\mvnw.cmd spring-boot:run
```

**Wait for this message**:
```
Started BackendApplication in ~11 seconds
Tomcat started on port 8080 (http)
```

Keep this terminal open and running.

---

### 2. Verify Backend is Running

Open **a new PowerShell terminal**:

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/health" -Method Get
```

**Expected output**:
```json
{
  "status": "UP",
  "database": "CONNECTED",
  "version": "0.0.1-SNAPSHOT",
  "totalStocks": 20,
  "totalPriceRecords": 0
}
```

Note: `totalPriceRecords` should be `0` before import.

---

### 3. Run the Import Script

In the same PowerShell terminal (not the one running the backend):

```powershell
cd C:\projects\Moshimo\scripts
.\import-mvp-stocks.ps1
```

**What happens**:
1. Script checks backend health ✓
2. Imports each of the 20 MVP stocks:
   - SPY, QQQ, GOOGL, AAPL, MSFT, NVDA, TSLA, AMZN, META, BRK.B
   - JPM, V, WMT, JNJ, PG, XOM, UNH, MA, HD, CVX
3. Shows progress for each stock:
   ```
   Processing AAPL...
     ✓ Imported: 12000 prices
       Skipped: 0 (already exist)
   ```
4. Displays final summary

**Expected time**: 5-10 minutes for all 20 stocks (~200,000 price records)

**Note about BRK.B**: The file is named `BRK-B.csv` in the archive. The script handles this automatically.

---

### 4. Verify Import Success

After the script completes:

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/health" -Method Get
```

**Expected output** (after import):
```json
{
  "status": "UP",
  "database": "CONNECTED",
  "version": "0.0.1-SNAPSHOT",
  "totalStocks": 20,
  "totalPriceRecords": 200000+  // Will vary, should be > 100,000
}
```

---

### 5. Test the Frontend

Open **a third PowerShell terminal**:

```powershell
cd C:\projects\Moshimo\frontend
npm run dev
```

Open browser: `http://localhost:5173`

**Test a simulation**:
1. Select stock: `AAPL` (Apple)
2. Purchase date: `2020-01-01`
3. Amount: `$1000`
4. Click **"Simulate Investment"**

**Expected result**:
- Chart shows portfolio growth from 2020 to 2024
- Metrics display:
  - Total Invested: $1,000
  - Current Value: $3,000+ (will vary)
  - Gain: $2,000+
  - Return: 200%+
  - CAGR: ~25%
- Holdings table shows:
  - Stock: AAPL
  - Shares: ~7.5 (varies by purchase date price)
  - Purchase Price: ~$133
  - Current Price: ~$400
  - Gain: +200%+

---

## If Something Goes Wrong

### Backend Won't Start

**Check PostgreSQL**:
```powershell
docker ps
```
Should show `moshimo_postgres` container running.

**If not running**:
```powershell
cd C:\projects\Moshimo
docker-compose up -d
```

**Check port 8080**:
```powershell
netstat -an | findstr 8080
```
If something else is using port 8080, stop that service.

---

### Import Script Fails

**Check CSV files exist**:
```powershell
Test-Path C:\projects\Moshimo\archive\stocks\AAPL.csv
```
Should return `True`.

**List available CSV files**:
```powershell
Get-ChildItem C:\projects\Moshimo\archive\stocks -Filter "*.csv" | Select-Object -First 10 Name
```

**Check backend logs** in the first terminal for specific error messages.

---

### Simulation Returns "No Price Data"

**Verify price data imported**:
```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/health" -Method Get
```
Check that `totalPriceRecords` is > 0.

**Check specific stock**:
```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/stocks/AAPL" -Method Get
```
Should return stock details.

**Check date range**:
Make sure purchase date is:
- After the stock's IPO date
- Before today's date
- In YYYY-MM-DD format

---

## Manual Testing

### Test Single Stock Import

To test with just one stock:

```powershell
$body = @{ filePath = "C:\projects\Moshimo\archive\stocks\AAPL.csv" } | ConvertTo-Json
Invoke-RestMethod `
    -Uri "http://localhost:8080/api/admin/import-csv" `
    -Method Post `
    -Body $body `
    -ContentType "application/json"
```

**Expected response**:
```json
{
  "success": true,
  "message": "Import completed successfully",
  "importedPrices": 12000,
  "skippedPrices": 0,
  "failedLines": 0
}
```

---

### Test Simulation API Directly

```powershell
$body = @{
    investments = @(
        @{
            symbol = "AAPL"
            amountUsd = 1000
            purchaseDate = "2020-01-01"
        }
    )
} | ConvertTo-Json -Depth 3

Invoke-RestMethod `
    -Uri "http://localhost:8080/api/portfolio/simulate" `
    -Method Post `
    -Body $body `
    -ContentType "application/json"
```

**Expected response**:
```json
{
  "totalInvested": 1000.00,
  "currentValue": 3000.00+,
  "totalGain": 2000.00+,
  "totalReturn": 200.0+,
  "cagr": 25.0+,
  "holdings": [...],
  "timeSeriesData": [...]
}
```

---

## Next Steps After Import

1. **Enable Monthly Updates** (Optional):
   - Get API key from https://twelvedata.com (free tier: 800 calls/day)
   - Add to `.env` file: `TWELVE_DATA_API_KEY=your_key_here`
   - Change in `application-dev.yml`: `update-enabled: true`
   - Restart backend

2. **Test More Scenarios**:
   - Multiple stocks in one simulation
   - Different time ranges
   - Various investment amounts
   - All 20 MVP stocks

3. **Deploy to Production** (Phase 2):
   - Configure production database
   - Set up CI/CD pipeline
   - Deploy to cloud hosting
   - Configure custom domain

---

## Summary

✅ **Step 1**: Start backend → Wait for "Started BackendApplication"
✅ **Step 2**: Verify health → Check `totalStocks: 20, totalPriceRecords: 0`
✅ **Step 3**: Run import script → Wait for completion (~5-10 min)
✅ **Step 4**: Verify again → Check `totalPriceRecords: 200000+`
✅ **Step 5**: Test frontend → Simulate AAPL investment, see results

**Status**: Ready to import! 🚀
