package com.moshimo.backend.infrastructure.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import yahoofinance.YahooFinance;
import yahoofinance.histquotes.HistoricalQuote;
import yahoofinance.histquotes.Interval;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Yahoo Finance Client - Implementation of StockDataProvider.
 * 
 * Learning Notes:
 * - Adapter Pattern: Wraps Yahoo Finance API with our interface
 * - Translates: Yahoo's HistoricalQuote → Our HistoricalPrice DTO
 * - Isolation: All Yahoo-specific code contained here
 * - Easy to replace: Just implement another StockDataProvider
 * 
 * Known Limitations:
 * - Unofficial API (may break when Yahoo changes website)
 * - Rate limiting possible
 * - No official support
 */
@Service
@Slf4j
public class YahooFinanceClient implements StockDataProvider {

    @Override
    public List<HistoricalPrice> getHistoricalPrices(String symbol, LocalDate from, LocalDate to) 
            throws StockDataException {
        
        log.debug("Fetching historical data for {} from {} to {}", symbol, from, to);
        
        try {
            // Convert LocalDate to Calendar (Yahoo API requirement)
            Calendar calFrom = Calendar.getInstance();
            calFrom.setTime(java.sql.Date.valueOf(from));
            
            Calendar calTo = Calendar.getInstance();
            calTo.setTime(java.sql.Date.valueOf(to));

            // Fetch from Yahoo Finance
            yahoofinance.Stock yahooStock = YahooFinance.get(symbol);
            
            if (yahooStock == null) {
                throw new StockDataException("Stock not found: " + symbol);
            }

            List<HistoricalQuote> history = yahooStock.getHistory(calFrom, calTo, Interval.DAILY);

            if (history == null || history.isEmpty()) {
                log.warn("No historical data found for {}", symbol);
                throw new StockDataException("No historical data available for: " + symbol);
            }

            // Convert Yahoo's HistoricalQuote to our HistoricalPrice DTO
            List<HistoricalPrice> prices = history.stream()
                .map(this::convertToHistoricalPrice)
                .collect(Collectors.toList());

            log.info("Successfully fetched {} price records for {}", prices.size(), symbol);
            return prices;

        } catch (Exception e) {
            log.error("Failed to fetch data for {}: {}", symbol, e.getMessage());
            throw new StockDataException("Failed to fetch data for " + symbol, e);
        }
    }

    @Override
    public LocalDate getEarliestAvailableDate(String symbol) {
        // Yahoo Finance doesn't provide a dedicated endpoint for earliest available date
        // Fallback to 1980 (when most stock data began digitally)
        log.debug("Yahoo Finance does not support earliest date lookup for {}, using fallback", symbol);
        return LocalDate.of(1980, 1, 1);
    }

    @Override
    public boolean isHealthy() {
        try {
            // Quick health check: Try fetching SPY (always available)
            yahoofinance.Stock testStock = YahooFinance.get("SPY");
            return testStock != null && testStock.getQuote() != null;
        } catch (Exception e) {
            log.warn("Yahoo Finance health check failed: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public String getProviderName() {
        return "Yahoo Finance (Unofficial)";
    }

    /**
     * Convert Yahoo Finance HistoricalQuote to our HistoricalPrice DTO.
     * 
     * This is where all Yahoo-specific logic is isolated.
     */
    private HistoricalPrice convertToHistoricalPrice(HistoricalQuote quote) {
        LocalDate date = quote.getDate().toInstant()
            .atZone(ZoneId.systemDefault())
            .toLocalDate();

        return new HistoricalPrice(
            date,
            toBigDecimal(quote.getOpen()),
            toBigDecimal(quote.getHigh()),
            toBigDecimal(quote.getLow()),
            toBigDecimal(quote.getClose()),
            toBigDecimal(quote.getAdjClose()),
            quote.getVolume()
        );
    }

    /**
     * Safely convert Yahoo's BigDecimal to our BigDecimal.
     */
    private BigDecimal toBigDecimal(java.math.BigDecimal value) {
        return value != null ? BigDecimal.valueOf(value.doubleValue()) : BigDecimal.ZERO;
    }
}
