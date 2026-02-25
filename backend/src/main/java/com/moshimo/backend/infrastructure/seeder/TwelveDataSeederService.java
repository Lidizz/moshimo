package com.moshimo.backend.infrastructure.seeder;

import com.moshimo.backend.domain.model.AssetType;
import com.moshimo.backend.domain.model.Asset;
import com.moshimo.backend.domain.model.AssetPrice;
import com.moshimo.backend.domain.repository.AssetPriceRepository;
import com.moshimo.backend.domain.repository.AssetRepository;
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
 * Twelve Data Seeder Service - Bulk imports historical asset prices from Twelve Data API.
 * 
 * Features:
 * - Dynamic start date (fetches from earliest available via API)
 * - Batch saves for performance
 * - Asset metadata updates (ipoDate, lastPriceUpdate)
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
    private final AssetRepository assetRepository;
    private final AssetPriceRepository assetPriceRepository;

    /**
     * Seed database with historical data for multiple assets.
     * 
     * @param symbols List of asset tickers to import (e.g., ["MSFT", "AAPL"])
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
                Optional<Asset> asset = assetRepository.findBySymbol(symbol);
                if (asset.isPresent()) {
                    long deletedCount = assetPriceRepository.countByAsset(asset.get());
                    assetPriceRepository.deleteByAsset(asset.get());
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
     * Import historical data for a single asset.
     * 
     * Process:
     * 1. Get start date (earliest available OR last existing date + 1)
     * 2. Ensure asset exists in database
     * 3. Fetch historical prices from start → today
     * 4. Convert to JPA entities and batch save
     * 5. Update asset metadata (ipoDate, lastPriceUpdate)
     */
    private ImportResult importSingleStock(String symbol, LocalDate endDate, boolean clearExisting) {
        // Step 1: Determine start date
        LocalDate startDate;
        
        // Check if we already have data for this symbol
        Optional<Asset> existingAsset = assetRepository.findBySymbol(symbol);
        
        if (!clearExisting && existingAsset.isPresent()) {
            // Find the last date we have data for this asset
            Optional<LocalDate> lastDate = assetPriceRepository
                .findTopByAssetOrderByDateDesc(existingAsset.get())
                .map(AssetPrice::getDate);
            
            if (lastDate.isPresent()) {
                // Start from the day after the last existing record
                startDate = lastDate.get().plusDays(1);
                log.info("  → Resuming from last known date: {} (last record: {})", 
                    startDate, lastDate.get());
                
                // If we're already up to date, skip this asset
                if (!startDate.isBefore(endDate)) {
                    log.info("  → Asset already up to date, skipping");
                    return new ImportResult(symbol, 0, lastDate.get(), lastDate.get());
                }
            } else {
                // Asset exists but has no price data, get earliest from API
                startDate = stockDataProvider.getEarliestAvailableDate(symbol);
                log.info("  → No existing data, fetching from earliest: {}", startDate);
            }
        } else {
            // clearExisting=true or asset doesn't exist, get earliest from API
            startDate = stockDataProvider.getEarliestAvailableDate(symbol);
            log.info("  → Fetching from earliest available date: {}", startDate);
        }
        
        long totalDays = ChronoUnit.DAYS.between(startDate, endDate);
        
        log.info("  → Date range: {} to {} ({} days)", startDate, endDate, totalDays);
        
        // Step 2: Ensure asset exists in database
        Asset asset = existingAsset.orElseGet(() -> createAssetPlaceholder(symbol));
        
        log.info("  → Asset entity: {} (ID: {})", asset.getName(), asset.getId());
        
        // Step 2b: Auto-enrich metadata if missing (name, sector, industry, exchange)
        if (needsMetadataEnrichment(asset)) {
            enrichAssetMetadata(asset, symbol);
        }
        
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
        List<AssetPrice> entities = priceData.stream()
            .map(data -> mapToEntity(asset, data))
            .toList();
        
        // Step 5: Batch save (efficient bulk insert)
        log.info("  → Saving to database...");
        assetPriceRepository.saveAll(entities);
        
        // Step 6: Update asset metadata
        boolean metadataUpdated = false;
        
        if (asset.getIpoDate() == null || startDate.isBefore(asset.getIpoDate())) {
            asset.setIpoDate(startDate);
            metadataUpdated = true;
        }
        
        asset.setLastPriceUpdate(LocalDate.now());
        metadataUpdated = true;
        
        if (metadataUpdated) {
            assetRepository.save(asset);
            log.info("  → Updated asset metadata (ipoDate={}, lastPriceUpdate={})", 
                asset.getIpoDate(), asset.getLastPriceUpdate());
        }
        
        return new ImportResult(symbol, entities.size(), startDate, endDate);
    }

    /**
     * Create placeholder asset entry if it doesn't exist.
     * Will be enriched with proper metadata via /profile API call.
     */
    private Asset createAssetPlaceholder(String symbol) {
        log.info("  → Creating new asset entry for {}", symbol);
        
        String assetName = symbol + " (Auto-imported)";
        Asset asset = Asset.builder()
            .symbol(symbol)
            .name(assetName)
            .assetType(AssetType.inferFromSymbol(symbol, assetName))
            .isActive(true)
            .build();
        
        return assetRepository.save(asset);
    }

    /**
     * Check if asset metadata needs enrichment.
     * True if name contains "(Auto-imported)" or if sector/industry/exchange are missing.
     */
    private boolean needsMetadataEnrichment(Asset asset) {
        return asset.getName().contains("(Auto-imported)")
            || asset.getSector() == null
            || asset.getIndustry() == null
            || asset.getExchange() == null;
    }

    /**
     * Fetch asset profile from API and update entity metadata.
     * Uses Twelve Data /profile endpoint (1 API credit).
     */
    private void enrichAssetMetadata(Asset asset, String symbol) {
        log.info("  → Enriching metadata for {} via /profile API...", symbol);
        
        Optional<StockDataProvider.StockProfile> profile = stockDataProvider.getStockProfile(symbol);
        
        if (profile.isEmpty()) {
            log.warn("  → Could not fetch profile for {}, metadata unchanged", symbol);
            return;
        }
        
        StockDataProvider.StockProfile p = profile.get();
        
        if (p.name() != null && !p.name().isBlank()) {
            asset.setName(p.name());
        }
        if (p.exchange() != null && !p.exchange().isBlank()) {
            asset.setExchange(p.exchange());
        }
        if (p.sector() != null && !p.sector().isBlank()) {
            asset.setSector(p.sector());
        }
        if (p.industry() != null && !p.industry().isBlank()) {
            asset.setIndustry(p.industry());
        }
        
        // Infer asset type from profile type field
        if (p.type() != null) {
            String type = p.type().toLowerCase();
            if (type.contains("etf")) {
                asset.setAssetType(AssetType.ETF);
            } else if (type.contains("index")) {
                asset.setAssetType(AssetType.INDEX);
            } else {
                asset.setAssetType(AssetType.STOCK);
            }
        }
        
        assetRepository.save(asset);
        log.info("  → Enriched: name={}, sector={}, industry={}, exchange={}, type={}",
            asset.getName(), asset.getSector(), asset.getIndustry(), 
            asset.getExchange(), asset.getAssetType());
    }

    /**
     * Map provider HistoricalPrice to AssetPrice JPA entity.
     */
    private AssetPrice mapToEntity(Asset asset, StockDataProvider.HistoricalPrice data) {
        return AssetPrice.builder()
            .asset(asset)
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
