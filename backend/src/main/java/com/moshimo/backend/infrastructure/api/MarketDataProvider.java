package com.moshimo.backend.infrastructure.api;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Market Data Provider Interface - Adapter pattern for swappable data sources.
 * 
 * Learning Notes:
 * - Adapter Pattern: Abstracts external API details
 * - Easy to swap: Yahoo → Alpha Vantage → Twelve Data
 * - Single Responsibility: Each implementation handles one API
 * - Testability: Easy to mock for unit tests
 */
public interface MarketDataProvider {

    /**
     * Fetch historical price data for an asset symbol.
     * 
     * @param symbol asset ticker (e.g., "AAPL", "MSFT")
     * @param from start date (inclusive)
     * @param to end date (inclusive)
     * @return list of historical prices ordered by date ascending
     * @throws MarketDataException if API call fails or data unavailable
     */
    List<HistoricalPrice> getHistoricalPrices(String symbol, LocalDate from, LocalDate to) 
        throws MarketDataException;

    /**
     * Get the earliest available date for an asset symbol.
     * Useful for fetching maximum historical depth.
     * 
     * @param symbol asset ticker (e.g., "AAPL", "MSFT")
     * @return earliest available date, or fallback date (1980-01-01) if not available
     */
    LocalDate getEarliestAvailableDate(String symbol);

    /**
     * Fetch asset profile metadata (name, sector, industry, exchange).
     * Used to auto-enrich asset entities during import.
     *
     * @param symbol asset ticker (e.g., "AAPL")
     * @return profile data if available, empty if endpoint not supported or fails
     */
    default Optional<AssetProfile> getAssetProfile(String symbol) {
        return Optional.empty();
    }

    /**
     * Check if the provider is currently available and responding.
     * 
     * @return true if provider is healthy, false otherwise
     */
    boolean isHealthy();

    /**
     * Get the name of this provider (for logging/debugging).
     * 
     * @return provider name (e.g., "Yahoo Finance", "Alpha Vantage")
     */
    String getProviderName();

    /**
     * Historical Price DTO - Provider-agnostic price data.
     * 
     * Using Java record for immutability and clean syntax (Java 25 feature).
     */
    record HistoricalPrice(
        LocalDate date,
        BigDecimal open,
        BigDecimal high,
        BigDecimal low,
        BigDecimal close,
        BigDecimal adjustedClose,
        Long volume
    ) {
        /**
         * Validation constructor.
         */
        public HistoricalPrice {
            if (date == null) throw new IllegalArgumentException("Date cannot be null");
            if (close == null) throw new IllegalArgumentException("Close price cannot be null");
            if (volume != null && volume < 0) throw new IllegalArgumentException("Volume cannot be negative");
        }
    }

    /**
     * Asset profile metadata DTO.
     */
    record AssetProfile(
        String name,
        String exchange,
        String sector,
        String industry,
        String type  // e.g., "Common Stock", "ETF"
    ) {}

    /**
     * Custom exception for market data provider failures.
     */
    class MarketDataException extends Exception {
        public MarketDataException(String message) {
            super(message);
        }

        public MarketDataException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
