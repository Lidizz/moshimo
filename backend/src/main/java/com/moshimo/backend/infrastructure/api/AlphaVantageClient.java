package com.moshimo.backend.infrastructure.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Alpha Vantage Client - Backup provider for monthly updates.
 * 
 * Learning Notes:
 * - REST API with JSON response parsing
 * - API key authentication
 * - Handling rate limits (25 calls/day free)
 * - Full historical data available
 * 
 * API Documentation: https://www.alphavantage.co/documentation/
 * Free Tier: 25 requests/day, 5 requests/minute
 */
@Service
@Slf4j
public class AlphaVantageClient implements StockDataProvider {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String apiKey;
    
    private static final String BASE_URL = "https://www.alphavantage.co/query";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public AlphaVantageClient(
            @Value("${stock.data.alpha-vantage.api-key:}") String apiKey,
            RestTemplate restTemplate,
            ObjectMapper objectMapper) {
        this.apiKey = apiKey;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public List<HistoricalPrice> getHistoricalPrices(String symbol, LocalDate from, LocalDate to) 
            throws StockDataException {
        
        if (apiKey == null || apiKey.isBlank()) {
            throw new StockDataException("Alpha Vantage API key not configured");
        }

        log.debug("Fetching data from Alpha Vantage for {} from {} to {}", symbol, from, to);

        try {
            // Alpha Vantage returns full history, we filter locally
            String url = UriComponentsBuilder.fromUriString(BASE_URL)
                .queryParam("function", "TIME_SERIES_DAILY_ADJUSTED")
                .queryParam("symbol", symbol)
                .queryParam("outputsize", "full") // Get all available data
                .queryParam("apikey", apiKey)
                .toUriString();

            String response = restTemplate.getForObject(url, String.class);
            
            if (response == null) {
                throw new StockDataException("Empty response from Alpha Vantage");
            }

            List<HistoricalPrice> prices = parseResponse(response, symbol, from, to);
            
            log.info("Successfully fetched {} records from Alpha Vantage for {}", prices.size(), symbol);
            return prices;

        } catch (Exception e) {
            log.error("Failed to fetch from Alpha Vantage for {}: {}", symbol, e.getMessage());
            throw new StockDataException("Alpha Vantage API error for " + symbol, e);
        }
    }

    @Override
    public boolean isHealthy() {
        try {
            if (apiKey == null || apiKey.isBlank()) {
                return false;
            }
            
            // Quick health check with SPY
            LocalDate to = LocalDate.now();
            LocalDate from = to.minusDays(5);
            List<HistoricalPrice> prices = getHistoricalPrices("SPY", from, to);
            return !prices.isEmpty();
        } catch (Exception e) {
            log.warn("Alpha Vantage health check failed: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public String getProviderName() {
        return "Alpha Vantage";
    }

    @Override
    public LocalDate getEarliestAvailableDate(String symbol) {
        // Alpha Vantage doesn't have a dedicated earliest_timestamp endpoint
        // Fallback to 1980 (when most stock data began digitally)
        log.debug("Alpha Vantage does not support earliest date lookup for {}, using fallback", symbol);
        return LocalDate.of(1980, 1, 1);
    }

    /**
     * Parse Alpha Vantage JSON response into HistoricalPrice list.
     * 
     * Example response structure:
     * {
     *   "Meta Data": {...},
     *   "Time Series (Daily)": {
     *     "2024-01-15": {
     *       "1. open": "150.25",
     *       "2. high": "152.50",
     *       "3. low": "149.75",
     *       "4. close": "151.80",
     *       "5. adjusted close": "151.80",
     *       "6. volume": "12345678"
     *     }
     *   }
     * }
     */
    private List<HistoricalPrice> parseResponse(String response, String symbol, LocalDate from, LocalDate to) 
            throws Exception {
        
        JsonNode root = objectMapper.readTree(response);
        
        // Check for errors
        if (root.has("Error Message")) {
            throw new StockDataException("Alpha Vantage error: " + root.get("Error Message").asText());
        }
        
        if (root.has("Note")) {
            // Rate limit message
            throw new StockDataException("Alpha Vantage rate limit: " + root.get("Note").asText());
        }

        JsonNode timeSeries = root.get("Time Series (Daily)");
        if (timeSeries == null) {
            log.warn("No time series data returned from Alpha Vantage for {}", symbol);
            return new ArrayList<>();
        }

        List<HistoricalPrice> prices = new ArrayList<>();
        
        Iterator<Map.Entry<String, JsonNode>> fields = timeSeries.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> entry = fields.next();
            
            try {
                LocalDate date = LocalDate.parse(entry.getKey(), DATE_FORMATTER);
                
                // Filter by date range
                if (date.isBefore(from) || date.isAfter(to)) {
                    continue;
                }
                
                JsonNode dayData = entry.getValue();
                
                prices.add(new HistoricalPrice(
                    date,
                    new BigDecimal(dayData.get("1. open").asText()),
                    new BigDecimal(dayData.get("2. high").asText()),
                    new BigDecimal(dayData.get("3. low").asText()),
                    new BigDecimal(dayData.get("4. close").asText()),
                    new BigDecimal(dayData.get("5. adjusted close").asText()),
                    Long.parseLong(dayData.get("6. volume").asText())
                ));
            } catch (Exception e) {
                log.warn("Failed to parse data point for {}: {}", symbol, e.getMessage());
            }
        }

        // Sort by date ascending
        prices.sort((a, b) -> a.date().compareTo(b.date()));

        return prices;
    }
}
