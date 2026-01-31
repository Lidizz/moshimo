package com.moshimo.backend.infrastructure.scheduler;

import com.moshimo.backend.domain.model.Stock;
import com.moshimo.backend.domain.model.StockPrice;
import com.moshimo.backend.domain.repository.StockPriceRepository;
import com.moshimo.backend.domain.repository.StockRepository;
import com.moshimo.backend.infrastructure.api.StockDataProvider;
import com.moshimo.backend.infrastructure.api.StockDataProvider.HistoricalPrice;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Scheduled service to update stock prices monthly on the first day of each month.
 * Uses the configured provider with automatic fallback to backup providers.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StockPriceUpdateScheduler {
    
    private final StockRepository stockRepository;
    private final StockPriceRepository stockPriceRepository;
    private final StockDataProvider dataProvider;
    
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
            log.info("Scheduled stock price updates are disabled");
            return;
        }
        
        log.info("Starting monthly stock price update using provider: {}", dataProvider.getProviderName());
        
        List<Stock> allStocks = stockRepository.findAll();
        
        if (allStocks.isEmpty()) {
            log.warn("No stocks found in database. Skipping update.");
            return;
        }
        
        int successCount = 0;
        int failureCount = 0;
        int updatedPrices = 0;
        
        // Calculate date range for the previous month
        LocalDate endDate = LocalDate.now().minusDays(1); // Yesterday
        LocalDate startDate = endDate.minusMonths(1); // One month back
        
        for (Stock stock : allStocks) {
            try {
                log.debug("Fetching prices for {} from {} to {}", stock.getSymbol(), startDate, endDate);
                
                List<HistoricalPrice> prices = dataProvider.getHistoricalPrices(
                    stock.getSymbol(),
                    startDate,
                    endDate
                );
                
                if (prices.isEmpty()) {
                    log.warn("No prices returned for {}", stock.getSymbol());
                    failureCount++;
                    continue;
                }
                
                // Save new prices (skip duplicates)
                int savedCount = 0;
                for (HistoricalPrice price : prices) {
                    if (!stockPriceRepository.existsByStockIdAndDate(stock.getId(), price.date())) {
                        StockPrice stockPrice = new StockPrice();
                        stockPrice.setStock(stock);
                        stockPrice.setDate(price.date());
                        stockPrice.setOpen(price.open());
                        stockPrice.setHigh(price.high());
                        stockPrice.setLow(price.low());
                        stockPrice.setClose(price.close());
                        stockPrice.setVolume(price.volume());
                        stockPrice.setAdjustedClose(price.adjustedClose());
                        
                        stockPriceRepository.save(stockPrice);
                        savedCount++;
                    }
                }
                
                updatedPrices += savedCount;
                successCount++;
                
                log.debug("Updated {} with {} new price records", stock.getSymbol(), savedCount);
                
                // Rate limiting: small delay between stocks to avoid hitting API limits
                Thread.sleep(100);
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("Update interrupted for {}", stock.getSymbol(), e);
                break;
                
            } catch (Exception e) {
                log.error("Failed to update prices for {}: {}", stock.getSymbol(), e.getMessage());
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
        log.info("Manual stock price update triggered");
        updateMonthlyPrices();
    }
}
