package com.moshimo.backend.infrastructure.seeder;

import com.moshimo.backend.domain.model.Stock;
import com.moshimo.backend.domain.model.StockPrice;
import com.moshimo.backend.domain.repository.StockPriceRepository;
import com.moshimo.backend.domain.repository.StockRepository;
import com.moshimo.backend.infrastructure.api.StockDataProvider;
import com.moshimo.backend.infrastructure.api.StockDataProvider.HistoricalPrice;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Stock Data Seeder Service - Fetches and stores historical price data.
 * 
 * Learning Notes:
 * - Adapter Pattern: Uses StockDataProvider interface (provider-agnostic)
 * - Dependency Injection: Provider can be swapped via configuration
 * - Batch processing: Insert prices in transactions
 * - Idempotency: Skip existing data
 * - Error handling: Continue on individual stock failures
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StockDataSeederService {

    private final StockRepository stockRepository;
    private final StockPriceRepository stockPriceRepository;
    private final StockDataProvider stockDataProvider; // ← Injected, easy to swap!

    /**
     * Seed historical price data for all active stocks.
     * 
     * @param yearsBack number of years of history to fetch (e.g., 5 for 2020-present)
     * @return summary of seeding operation
     */
    @Transactional
    public SeedingSummary seedHistoricalData(int yearsBack) {
        log.info("Starting historical data seeding for {} years back", yearsBack);
        
        SeedingSummary summary = new SeedingSummary();
        List<Stock> stocks = stockRepository.findByIsActiveTrue();
        
        log.info("Found {} active stocks to process", stocks.size());

        for (Stock stock : stocks) {
            try {
                // Check if stock already has data
                long existingCount = stockPriceRepository.countByStockId(stock.getId());
                if (existingCount > 0) {
                    log.info("Stock {} already has {} price records, skipping", 
                             stock.getSymbol(), existingCount);
                    summary.skipped++;
                    continue;
                }

                log.info("Fetching historical data for {}...", stock.getSymbol());
                
                // Calculate date range
                LocalDate from = LocalDate.now().minusYears(yearsBack);
                LocalDate to = LocalDate.now();

                // Fetch historical prices using the provider (adapter pattern!)
                List<HistoricalPrice> prices = stockDataProvider.getHistoricalPrices(
                    stock.getSymbol(), 
                    from, 
                    to
                );

                if (prices == null || prices.isEmpty()) {
                    log.warn("No historical data found for {}", stock.getSymbol());
                    summary.failed++;
                    continue;
                }

                // Convert and save price data
                int savedCount = 0;
                for (HistoricalPrice price : prices) {
                    try {
                        StockPrice stockPrice = convertToStockPrice(stock, price);
                        stockPriceRepository.save(stockPrice);
                        savedCount++;
                    } catch (Exception e) {
                        log.warn("Failed to save price for {} on {}: {}", 
                                 stock.getSymbol(), price.date(), e.getMessage());
                    }
                }

                log.info("Successfully saved {} price records for {}", savedCount, stock.getSymbol());
                summary.successful++;
                summary.totalRecords += savedCount;

                // Small delay to avoid rate limiting
                Thread.sleep(500);

            } catch (Exception e) {
                log.error("Failed to fetch data for {}: {}", stock.getSymbol(), e.getMessage());
                summary.failed++;
            }
        }

        log.info("Seeding complete - Success: {}, Failed: {}, Skipped: {}, Total records: {}", 
                 summary.successful, summary.failed, summary.skipped, summary.totalRecords);

        return summary;
    }

    /**
     * Seed data for specific symbols only.
     */
    @Transactional
    public SeedingSummary seedSpecificSymbols(List<String> symbols, int yearsBack) {
        log.info("Seeding data for specific symbols: {}", symbols);
        
        SeedingSummary summary = new SeedingSummary();

        for (String symbol : symbols) {
            try {
                Stock stock = stockRepository.findBySymbol(symbol)
                    .orElseThrow(() -> new IllegalArgumentException("Stock not found: " + symbol));

                // Check if already has data
                long existingCount = stockPriceRepository.countByStockId(stock.getId());
                if (existingCount > 0) {
                    log.info("Stock {} already has {} price records, skipping", 
                             symbol, existingCount);
                    summary.skipped++;
                    continue;
                }

                log.info("Fetching historical data for {}...", symbol);
                
                LocalDate from = LocalDate.now().minusYears(yearsBack);
                LocalDate to = LocalDate.now();

                List<HistoricalPrice> prices = stockDataProvider.getHistoricalPrices(
                    symbol, 
                    from, 
                    to
                );

                if (prices == null || prices.isEmpty()) {
                    log.warn("No historical data found for {}", symbol);
                    summary.failed++;
                    continue;
                }

                int savedCount = 0;
                for (HistoricalPrice price : prices) {
                    try {
                        StockPrice stockPrice = convertToStockPrice(stock, price);
                        stockPriceRepository.save(stockPrice);
                        savedCount++;
                    } catch (Exception e) {
                        log.warn("Failed to save price for {} on {}: {}", 
                                 symbol, price.date(), e.getMessage());
                    }
                }

                log.info("Successfully saved {} price records for {}", savedCount, symbol);
                summary.successful++;
                summary.totalRecords += savedCount;

                Thread.sleep(500);

            } catch (Exception e) {
                log.error("Failed to fetch data for {}: {}", symbol, e.getMessage());
                summary.failed++;
            }
        }

        log.info("Seeding complete - Success: {}, Failed: {}, Skipped: {}, Total records: {}", 
                 summary.successful, summary.failed, summary.skipped, summary.totalRecords);

        return summary;
    }

    /**
     * Convert HistoricalPrice DTO to StockPrice entity.
     * 
     * Provider-agnostic conversion using our own DTO.
     */
    private StockPrice convertToStockPrice(Stock stock, HistoricalPrice price) {
        return StockPrice.builder()
            .stock(stock)
            .date(price.date())
            .open(price.open())
            .high(price.high())
            .low(price.low())
            .close(price.close())
            .volume(price.volume())
            .adjustedClose(price.adjustedClose())
            .build();
    }

    /**
     * Summary of seeding operation.
     */
    public static class SeedingSummary {
        public int successful = 0;
        public int failed = 0;
        public int skipped = 0;
        public int totalRecords = 0;

        @Override
        public String toString() {
            return String.format(
                "Seeding Summary: %d successful, %d failed, %d skipped, %d total records",
                successful, failed, skipped, totalRecords
            );
        }
    }
}