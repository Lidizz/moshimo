package com.moshimo.backend.infrastructure.seeder;

import com.moshimo.backend.domain.model.AssetType;
import com.moshimo.backend.domain.model.Stock;
import com.moshimo.backend.domain.model.StockPrice;
import com.moshimo.backend.domain.repository.StockPriceRepository;
import com.moshimo.backend.domain.repository.StockRepository;
import com.moshimo.backend.infrastructure.api.StockDataProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Twelve Data Seeder Service - Bulk imports historical stock prices from Twelve Data API.
 * 
 * Features:
 * - Dynamic start date (fetches from earliest available via API)
 * - Batch saves for performance
 * - Stock metadata updates (ipoDate, lastPriceUpdate)
 * - Optional clear existing data
 * - Comprehensive logging and error handling
 * 
 * Usage:
 *   POST /api/admin/import/twelve-data
 *   Body: { "symbols": ["MSFT"], "clearExisting": true }
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TwelveDataSeederService {

    private final StockDataProvider stockDataProvider;  // TwelveDataClient injected
    private final StockRepository stockRepository;
    private final StockPriceRepository stockPriceRepository;

    /**
     * Seed database with historical data for multiple stocks.
     * 
     * @param symbols List of stock tickers to import (e.g., ["MSFT", "AAPL"])
     * @param clearExisting If true, deletes all existing price data first
     * @return Summary of import results (successes, failures, total records)
     */
    @Transactional
    public ImportSummary seedStocks(List<String> symbols, boolean clearExisting) {
        log.info("========================================");
        log.info("Starting Twelve Data import for {} symbols", symbols.size());
        log.info("Clear existing data: {}", clearExisting);
        log.info("Provider: {}", stockDataProvider.getProviderName());
        log.info("========================================");
        
        ImportSummary summary = new ImportSummary();
        
        // Step 1: Clear existing price data for requested symbols only
        if (clearExisting) {
            log.warn("Clearing existing data for {} symbols...", symbols.size());
            for (String symbol : symbols) {
                Optional<Stock> stock = stockRepository.findBySymbol(symbol);
                if (stock.isPresent()) {
                    long deletedCount = stockPriceRepository.countByStock(stock.get());
                    stockPriceRepository.deleteByStock(stock.get());
                    log.info("  ✓ Deleted {} records for {}", deletedCount, symbol);
                }
            }
        }
        
        // Step 2: Import each stock
        LocalDate endDate = LocalDate.now();
        
        for (int i = 0; i < symbols.size(); i++) {
            String symbol = symbols.get(i);
            log.info("[{}/{}] Processing {}...", i + 1, symbols.size(), symbol);
            
            try {
                ImportResult result = importSingleStock(symbol, endDate, clearExisting);
                summary.addResult(result);
                
                log.info("✓ {} - {} records imported ({} to {})", 
                    symbol, 
                    result.recordsImported(), 
                    result.startDate(), 
                    result.endDate());
                
            } catch (Exception e) {
                log.error("✗ {} - Failed: {}", symbol, e.getMessage());
                summary.addFailure(symbol, e.getMessage());
            }
        }
        
        // Step 3: Log summary
        log.info("========================================");
        log.info("Import complete!");
        log.info("Successes: {} stocks", summary.successCount());
        log.info("Failures: {} stocks", summary.failureCount());
        log.info("Total records: {}", summary.totalRecords());
        log.info("========================================");
        
        return summary;
    }

    /**
     * Import historical data for a single stock.
     * 
     * Process:
     * 1. Get start date (earliest available OR last existing date + 1)
     * 2. Ensure stock exists in database
     * 3. Fetch historical prices from start → today
     * 4. Convert to JPA entities and batch save
     * 5. Update stock metadata (ipoDate, lastPriceUpdate)
     */
    private ImportResult importSingleStock(String symbol, LocalDate endDate, boolean clearExisting) {
        // Step 1: Determine start date
        LocalDate startDate;
        
        // Check if we already have data for this symbol
        Optional<Stock> existingStock = stockRepository.findBySymbol(symbol);
        
        if (!clearExisting && existingStock.isPresent()) {
            // Find the last date we have data for this stock
            Optional<LocalDate> lastDate = stockPriceRepository
                .findTopByStockOrderByDateDesc(existingStock.get())
                .map(StockPrice::getDate);
            
            if (lastDate.isPresent()) {
                // Start from the day after the last existing record
                startDate = lastDate.get().plusDays(1);
                log.info("  → Resuming from last known date: {} (last record: {})", 
                    startDate, lastDate.get());
                
                // If we're already up to date, skip this stock
                if (!startDate.isBefore(endDate)) {
                    log.info("  → Stock already up to date, skipping");
                    return new ImportResult(symbol, 0, lastDate.get(), lastDate.get());
                }
            } else {
                // Stock exists but has no price data, get earliest from API
                startDate = stockDataProvider.getEarliestAvailableDate(symbol);
                log.info("  → No existing data, fetching from earliest: {}", startDate);
            }
        } else {
            // clearExisting=true or stock doesn't exist, get earliest from API
            startDate = stockDataProvider.getEarliestAvailableDate(symbol);
            log.info("  → Fetching from earliest available date: {}", startDate);
        }
        
        long totalDays = ChronoUnit.DAYS.between(startDate, endDate);
        
        log.info("  → Date range: {} to {} ({} days)", startDate, endDate, totalDays);
        
        // Step 2: Ensure stock exists in database
        Stock stock = existingStock.orElseGet(() -> createStockPlaceholder(symbol));
        
        log.info("  → Stock entity: {} (ID: {})", stock.getName(), stock.getId());
        
        // Step 3: Fetch data from API
        log.info("  → Fetching historical data...");
        List<StockDataProvider.HistoricalPrice> priceData;
        
        try {
            priceData = stockDataProvider.getHistoricalPrices(symbol, startDate, endDate);
        } catch (StockDataProvider.StockDataException e) {
            throw new RuntimeException("API error: " + e.getMessage(), e);
        }
        
        if (priceData.isEmpty()) {
            throw new RuntimeException("No data returned from API");
        }
        
        log.info("  → Received {} records from API", priceData.size());
        
        // Step 4: Convert to JPA entities
        List<StockPrice> entities = priceData.stream()
            .map(data -> mapToEntity(stock, data))
            .toList();
        
        // Step 5: Batch save (efficient bulk insert)
        log.info("  → Saving to database...");
        stockPriceRepository.saveAll(entities);
        
        // Step 6: Update stock metadata
        boolean metadataUpdated = false;
        
        if (stock.getIpoDate() == null || startDate.isBefore(stock.getIpoDate())) {
            stock.setIpoDate(startDate);
            metadataUpdated = true;
        }
        
        stock.setLastPriceUpdate(LocalDate.now());
        metadataUpdated = true;
        
        if (metadataUpdated) {
            stockRepository.save(stock);
            log.info("  → Updated stock metadata (ipoDate={}, lastPriceUpdate={})", 
                stock.getIpoDate(), stock.getLastPriceUpdate());
        }
        
        return new ImportResult(symbol, entities.size(), startDate, endDate);
    }

    /**
     * Create placeholder stock entry if it doesn't exist.
     * Will be enriched with proper metadata later (sector, industry, etc.)
     */
    private Stock createStockPlaceholder(String symbol) {
        log.info("  → Creating new stock entry for {}", symbol);
        
        String stockName = symbol + " (Auto-imported)";
        Stock stock = Stock.builder()
            .symbol(symbol)
            .name(stockName)
            .assetType(AssetType.inferFromSymbol(symbol, stockName))
            .isActive(true)
            .build();
        
        return stockRepository.save(stock);
    }

    /**
     * Map provider HistoricalPrice to StockPrice JPA entity.
     */
    private StockPrice mapToEntity(Stock stock, StockDataProvider.HistoricalPrice data) {
        return StockPrice.builder()
            .stock(stock)
            .date(data.date())
            .open(data.open())
            .high(data.high())
            .low(data.low())
            .close(data.close())
            .volume(data.volume())
            .adjustedClose(data.adjustedClose())
            .build();
    }

    // ========== Result Records ==========

    /**
     * Result of importing a single stock.
     */
    public record ImportResult(
        String symbol,
        int recordsImported,
        LocalDate startDate,
        LocalDate endDate
    ) {}

    /**
     * Summary of entire import operation.
     */
    public record ImportSummary(
        List<ImportResult> successes,
        List<FailureInfo> failures
    ) {
        public ImportSummary() {
            this(new ArrayList<>(), new ArrayList<>());
        }
        
        void addResult(ImportResult result) {
            successes.add(result);
        }
        
        void addFailure(String symbol, String error) {
            failures.add(new FailureInfo(symbol, error));
        }
        
        public int successCount() {
            return successes.size();
        }
        
        public int failureCount() {
            return failures.size();
        }
        
        public int totalRecords() {
            return successes.stream()
                .mapToInt(ImportResult::recordsImported)
                .sum();
        }
    }

    /**
     * Information about a failed import.
     */
    public record FailureInfo(
        String symbol,
        String error
    ) {}
}
