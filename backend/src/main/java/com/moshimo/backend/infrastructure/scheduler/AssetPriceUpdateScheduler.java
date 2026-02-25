package com.moshimo.backend.infrastructure.scheduler;

import com.moshimo.backend.domain.model.Asset;
import com.moshimo.backend.domain.model.AssetPrice;
import com.moshimo.backend.domain.repository.AssetPriceRepository;
import com.moshimo.backend.domain.repository.AssetRepository;
import com.moshimo.backend.infrastructure.api.MarketDataProvider;
import com.moshimo.backend.infrastructure.api.MarketDataProvider.HistoricalPrice;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

/**
 * Scheduled service to update asset prices monthly on the first day of each month.
 * Uses the configured provider with automatic fallback to backup providers.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AssetPriceUpdateScheduler {
    
    private final AssetRepository assetRepository;
    private final AssetPriceRepository assetPriceRepository;
    private final MarketDataProvider dataProvider;
    
    @Value("${stock.data.update-enabled:true}")
    private boolean updateEnabled;
    
    /**
     * Runs on the 1st of every month at 2 AM.
     * Cron expression: second minute hour day-of-month month day-of-week
     * "0 0 2 1 * *" = At 02:00 on day 1 of every month
     */
    @Scheduled(cron = "${stock.data.update-cron:0 0 2 1 * *}")
    @Transactional
    public void updateMonthlyPrices() {
        if (!updateEnabled) {
            log.info("Scheduled asset price updates are disabled");
            return;
        }
        
        log.info("Starting monthly asset price update using provider: {}", dataProvider.getProviderName());
        
        List<Asset> allAssets = assetRepository.findAll();
        
        if (allAssets.isEmpty()) {
            log.warn("No assets found in database. Skipping update.");
            return;
        }
        
        int successCount = 0;
        int failureCount = 0;
        int updatedPrices = 0;
        
        // Calculate date range for the previous month
        LocalDate endDate = LocalDate.now().minusDays(1); // Yesterday
        LocalDate startDate = endDate.minusMonths(1); // One month back
        
        for (Asset asset : allAssets) {
            try {
                log.debug("Fetching prices for {} from {} to {}", asset.getSymbol(), startDate, endDate);
                
                List<HistoricalPrice> prices = dataProvider.getHistoricalPrices(
                    asset.getSymbol(),
                    startDate,
                    endDate
                );
                
                if (prices.isEmpty()) {
                    log.warn("No prices returned for {}", asset.getSymbol());
                    failureCount++;
                    continue;
                }
                
                // Batch-fetch existing dates to skip duplicates without per-record queries
                Set<LocalDate> existingDates = new HashSet<>(
                    assetPriceRepository.findDatesByAssetIdAndDateBetween(
                        asset.getId(), startDate, endDate));
                
                List<AssetPrice> newPrices = new ArrayList<>();
                for (HistoricalPrice price : prices) {
                    if (!existingDates.contains(price.date())) {
                        AssetPrice assetPrice = new AssetPrice();
                        assetPrice.setAsset(asset);
                        assetPrice.setDate(price.date());
                        assetPrice.setOpen(price.open());
                        assetPrice.setHigh(price.high());
                        assetPrice.setLow(price.low());
                        assetPrice.setClose(price.close());
                        assetPrice.setVolume(price.volume());
                        assetPrice.setAdjustedClose(price.adjustedClose());
                        newPrices.add(assetPrice);
                    }
                }
                
                if (!newPrices.isEmpty()) {
                    assetPriceRepository.saveAll(newPrices);
                }
                
                updatedPrices += newPrices.size();
                successCount++;
                
                log.debug("Updated {} with {} new price records", asset.getSymbol(), newPrices.size());
                
                // Rate limiting: small delay between stocks to avoid hitting API limits
                Thread.sleep(100);
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("Update interrupted for {}", asset.getSymbol(), e);
                break;
                
            } catch (Exception e) {
                log.error("Failed to update prices for {}: {}", asset.getSymbol(), e.getMessage());
                failureCount++;
            }
        }
        
        log.info("Monthly update completed. Success: {}, Failures: {}, Total new prices: {}",
                successCount, failureCount, updatedPrices);
    }
    
    /**
     * Manual trigger for testing purposes.
     * Can be called via an admin endpoint if needed.
     */
    public void triggerUpdateNow() {
        log.info("Manual asset price update triggered");
        updateMonthlyPrices();
    }
}
