# Stock Data Provider Architecture

## Overview
Moshimo uses a multi-provider architecture with automatic fallback to ensure reliable historical stock data. The system combines bulk CSV imports for historical data with API providers for monthly updates.

## Data Sources

### Primary: Bulk CSV Import (Kaggle)
- **Purpose**: Initial load of decades of historical data
- **Source**: [S&P 500 Historical Data (1964-2024)](https://www.kaggle.com/datasets/camnugent/sandp500)
- **Usage**: One-time import via Admin API
- **Endpoint**: `POST /api/admin/import-csv`

### Monthly Updates: API Providers
1. **Twelve Data** (Primary)
   - Free tier: 800 API calls/day
   - Excellent for monthly updates of ~500 stocks
   - Get API key: https://twelvedata.com/

2. **Alpha Vantage** (Backup)
   - Free tier: 25 API calls/day
   - Full historical data available
   - Get API key: https://www.alphavantage.co/support/#api-key

3. **Yahoo Finance** (Last Resort)
   - No API key required
   - Unreliable (endpoints change frequently)
   - Only used if other providers fail

## Architecture

### Adapter Pattern
```
StockDataProvider (interface)
    ├── TwelveDataClient
    ├── AlphaVantageClient
    └── YahooFinanceClient
```

### Automatic Fallback
The system automatically tries providers in order:
1. Twelve Data → Success ✓
2. Twelve Data → Failure → Try Alpha Vantage
3. Alpha Vantage → Failure → Try Yahoo Finance

### Configuration
```yaml
# application-dev.yml
stock:
  data:
    primary-provider: twelveData
    backup-providers:
      - alphaVantage
      - yahoo
    update-enabled: true
    update-cron: "0 0 2 1 * *"  # 2 AM on 1st of month
```

## Setup Instructions

### 1. Get API Keys
```bash
# Twelve Data (recommended)
https://twelvedata.com/
# Sign up → Get API key → 800 free calls/day

# Alpha Vantage (backup)
https://www.alphavantage.co/support/#api-key
# Click "Get Free API Key" → 25 free calls/day
```

### 2. Add to .env File
```env
# .env
TWELVE_DATA_API_KEY=your_twelve_data_key_here
ALPHA_VANTAGE_API_KEY=your_alpha_vantage_key_here
```

### 3. Download Kaggle CSV
```bash
# Download from: https://www.kaggle.com/datasets/camnugent/sandp500
# Place in: C:/data/stocks/sp500-1964-2024.csv
```

### 4. Import Historical Data
```bash
# Start the backend
cd backend
./mvnw spring-boot:run

# Import CSV via API
curl -X POST http://localhost:8080/api/admin/import-csv \
  -H "Content-Type: application/json" \
  -d '{"filePath": "C:/data/stocks/sp500-1964-2024.csv"}'
```

## Admin Endpoints

### Import CSV Data
```http
POST /api/admin/import-csv
Content-Type: application/json

{
  "filePath": "C:/data/stocks/sp500-1964-2024.csv"
}
```

### Manual Price Update (Testing)
```http
POST /api/admin/update-prices
```

### Health Check
```http
GET /api/admin/health
```

## Monthly Updates

### Automatic Schedule
- **When**: 1st of every month at 2:00 AM
- **What**: Fetches last month's data for all stocks
- **How**: Uses configured provider with automatic fallback

### Disable Automatic Updates
```yaml
# application-dev.yml
stock:
  data:
    update-enabled: false
```

## Workflow

### Initial Setup (One-Time)
1. Sign up for API keys (Twelve Data + Alpha Vantage)
2. Add keys to `.env` file
3. Download Kaggle CSV dataset
4. Import CSV via admin endpoint
5. **Remove hardcoded stocks from Flyway V3 migration** (optional cleanup)

### Ongoing Operation
1. Scheduler runs monthly (1st at 2 AM)
2. Fetches previous month's data for each stock
3. Skips duplicate dates automatically
4. Logs success/failure for each stock
5. Automatically falls back to backup providers if needed

## Rate Limiting

### Twelve Data (Primary)
- 800 calls/day
- ~500 stocks = 1-2 days to update all (no problem for monthly)
- 100ms delay between stocks

### Alpha Vantage (Backup)
- 25 calls/day
- Takes ~20 days to update 500 stocks
- Only used if Twelve Data fails

### Yahoo Finance (Last Resort)
- No rate limit
- Unreliable, may break at any time

## CSV Format

Expected format for bulk imports:
```csv
Date,Open,High,Low,Close,Volume,Adj Close,Symbol,Name
2024-01-02,4783.45,4793.75,4767.12,4783.45,3456789,4783.45,AAPL,Apple Inc.
```

## Performance

### CSV Import
- Batch processing: 1000 records per transaction
- Memory efficient: BufferedReader streaming
- Idempotent: Skips existing stock/date combinations

### Monthly Updates
- 100ms delay between stocks (rate limiting)
- Skips existing dates (no duplicates)
- Transactional updates per stock

## Troubleshooting

### "No provider available"
- Check API keys in `.env`
- Verify `application-dev.yml` provider configuration
- Check logs for provider health status

### "Rate limit exceeded"
- Twelve Data: Wait for next day (800/day limit)
- Alpha Vantage: Reduce update frequency or wait

### CSV Import Fails
- Check file path (use forward slashes: `C:/data/...`)
- Verify CSV format matches expected columns
- Check logs for specific parsing errors

## Next Steps

1. ✅ Architecture implemented
2. ✅ All providers created
3. ✅ CSV importer ready
4. ✅ Admin endpoints added
5. ✅ Scheduler configured
6. ⏳ Add API keys to `.env`
7. ⏳ Download Kaggle CSV
8. ⏳ Import historical data
9. ⏳ Remove Flyway V3 hardcoded stocks (optional)
10. ⏳ Test monthly update manually

## Files Reference

### Infrastructure
- `StockDataProvider.java` - Interface for all providers
- `TwelveDataClient.java` - Primary API provider
- `AlphaVantageClient.java` - Backup API provider
- `YahooFinanceClient.java` - Last resort provider
- `CsvStockDataImporter.java` - Bulk CSV import service
- `StockPriceUpdateScheduler.java` - Monthly cron job

### Configuration
- `application-dev.yml` - Provider settings, cron schedule
- `.env` - API keys (keep private!)
- `ApiClientConfig.java` - RestTemplate and ObjectMapper beans

### API
- `AdminController.java` - Admin endpoints for imports and manual updates
