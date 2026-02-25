package com.moshimo.backend.infrastructure.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Twelve Data API Client - Fetches historical stock prices with adjusted data.
 * 
 * API Documentation: https://twelvedata.com/docs
 * Rate Limits: 8 requests/min, 800 requests/day (Basic tier)
 * 
 * Features:
 * - Dynamic earliest date lookup via /earliest_timestamp
 * - Adjusted prices (splits + dividends) via adjust=all parameter
 * - Pagination for >5000 records
 * - Rate limiting (8 seconds between requests)
 * - Graceful error handling (429 retry, 404 skip)
 * 
 * Design Pattern: Adapter Pattern (external API → internal domain model)
 */
@Service
@Primary  // Use this as default provider
@Slf4j
public class TwelveDataClient implements MarketDataProvider {

    private final String apiKey;
    private final String baseUrl;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE;
    
    // Rate limiting: 8 requests per minute - batch strategy
    // Make 8 requests as fast as possible, then wait for next minute
    private long currentMinuteStart = 0;
    private int requestsInCurrentMinute = 0;
    private static final int MAX_REQUESTS_PER_MINUTE = 8;
    private static final int MAX_RETRIES = 3;
    private static final LocalDate FALLBACK_START_DATE = LocalDate.of(1980, 1, 1);

    public TwelveDataClient(
            @Value("${stock.data.twelve-data.api-key:}") String apiKey,
            @Value("${stock.data.twelve-data.base-url:https://api.twelvedata.com}") String baseUrl,
            ObjectMapper objectMapper) {
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .build();
        
        log.info("TwelveDataClient initialized with base URL: {}", baseUrl);
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("Twelve Data API key not configured - client will fail on requests");
        }
    }

    @Override
    public LocalDate getEarliestAvailableDate(String symbol) {
        return getEarliestAvailableDate(symbol, 0);
    }

    private LocalDate getEarliestAvailableDate(String symbol, int retryCount) {
        log.debug("Fetching earliest available date for {}", symbol);
        
        try {
            enforceRateLimit();
            
            // Build URL for /earliest_timestamp endpoint
            String url = String.format(
                "%s/earliest_timestamp?symbol=%s&interval=1day&apikey=%s",
                baseUrl,
                symbol,
                apiKey
            );
            
            // Make HTTP request
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();
            
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            // Handle HTTP errors
            if (response.statusCode() == 429) {
                if (retryCount >= MAX_RETRIES) {
                    log.error("Rate limit retry limit ({}) exceeded for earliest date on {}, using fallback",
                            MAX_RETRIES, symbol);
                    return FALLBACK_START_DATE;
                }
                log.warn("Rate limit hit for earliest date lookup on {} (attempt {}/{}), waiting 60s...",
                        symbol, retryCount + 1, MAX_RETRIES);
                Thread.sleep(60000);
                return getEarliestAvailableDate(symbol, retryCount + 1);
            }
            
            if (response.statusCode() != 200) {
                log.warn("HTTP {} for earliest date on {}: {}", response.statusCode(), symbol, response.body());
                return FALLBACK_START_DATE;
            }
            
            // Parse JSON response
            JsonNode root = objectMapper.readTree(response.body());
            
            // Check for API errors
            if (root.has("status") && "error".equals(root.get("status").asText())) {
                String message = root.has("message") ? root.get("message").asText() : "Unknown error";
                log.warn("Twelve Data error for {}: {}", symbol, message);
                return FALLBACK_START_DATE;
            }
            
            // Extract datetime field
            if (root.has("datetime")) {
                String dateString = root.get("datetime").asText();
                LocalDate earliest = LocalDate.parse(dateString, dateFormatter);
                log.info("Earliest date for {}: {}", symbol, earliest);
                return earliest;
            }
            
            log.warn("No datetime field in earliest_timestamp response for {}", symbol);
            return FALLBACK_START_DATE;
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Interrupted while fetching earliest date for {}", symbol);
            return FALLBACK_START_DATE;
        } catch (Exception e) {
            log.error("Error fetching earliest date for {}: {}", symbol, e.getMessage(), e);
            return FALLBACK_START_DATE;
        }
    }

    @Override
    public List<HistoricalPrice> getHistoricalPrices(String symbol, LocalDate from, LocalDate to) 
            throws MarketDataException {
        
        if (apiKey == null || apiKey.isBlank()) {
            throw new MarketDataException("Twelve Data API key not configured");
        }
        
        log.info("Fetching historical data for {} from {} to {}", symbol, from, to);
        
        // Use LinkedHashMap to preserve order while deduplicating by date
        Map<LocalDate, HistoricalPrice> dataByDate = new LinkedHashMap<>();
        
        // Twelve Data returns max 5000 records per call
        // When requesting large date ranges, API returns MOST RECENT records
        // Solution: Request smaller chunks (15 years ≈ 3750 trading days < 5000)
        LocalDate currentStart = from;
        final int MAX_RECORDS_PER_CALL = 5000;
        final int YEARS_PER_CHUNK = 15; // Safe chunk size to stay under 5000 records
        int chunkCount = 0;
        
        while (!currentStart.isAfter(to)) {
            chunkCount++;
            
            // Calculate chunk end date (15 years forward, or final end date)
            LocalDate chunkEnd = currentStart.plusYears(YEARS_PER_CHUNK);
            if (chunkEnd.isAfter(to)) {
                chunkEnd = to;
            }
            
            log.info("  → Fetching chunk {}: {} to {}", chunkCount, currentStart, chunkEnd);
            
            List<HistoricalPrice> chunkData = fetchChunk(symbol, currentStart, chunkEnd);
            
            if (chunkData.isEmpty()) {
                log.warn("  → Chunk {} returned no data, stopping pagination", chunkCount);
                break;
            }
            
            // Add to map (automatically deduplicates by date)
            int duplicates = 0;
            for (HistoricalPrice price : chunkData) {
                if (dataByDate.putIfAbsent(price.date(), price) != null) {
                    duplicates++;
                }
            }
            
            if (duplicates > 0) {
                log.warn("  → Chunk {} had {} duplicate dates (skipped)", chunkCount, duplicates);
            }
            
            log.info("  → Chunk {} complete: {} records ({} unique, total: {})", 
                chunkCount, chunkData.size(), chunkData.size() - duplicates, dataByDate.size());
            
            // Move to next chunk: start from day after the chunk end date
            // This ensures we request sequential 15-year windows
            currentStart = chunkEnd.plusDays(1);
            
            // Safety check: if we've reached or passed the target, we're done
            if (!currentStart.isBefore(to)) {
                log.info("  → Reached target end date, pagination complete");
                break;
            }
            
            // Continue to next chunk (rate limiting is handled in fetchChunk)
        }
        
        List<HistoricalPrice> allData = new ArrayList<>(dataByDate.values());
        
        log.info("✓ Fetched {} total unique records for {} in {} chunks ({} to {})", 
            allData.size(), symbol, chunkCount, from, to);
        
        return allData;
    }

    private List<HistoricalPrice> fetchChunk(String symbol, LocalDate startDate, LocalDate endDate)
            throws MarketDataException {
        return fetchChunk(symbol, startDate, endDate, 0);
    }

    private List<HistoricalPrice> fetchChunk(String symbol, LocalDate startDate, LocalDate endDate, int retryCount)
            throws MarketDataException {
        try {
            // Rate limiting
            enforceRateLimit();
            
            // Build URL with adjust=all for split/dividend adjustment
            // Use smaller date ranges (15-year chunks) to get chronological data
            String url = String.format(
                "%s/time_series?symbol=%s&interval=1day&start_date=%s&end_date=%s&outputsize=5000&adjust=all&apikey=%s",
                baseUrl,
                symbol,
                startDate.format(dateFormatter),
                endDate.format(dateFormatter),
                apiKey
            );
            
            // Make HTTP request
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();
            
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            // Handle HTTP errors
            if (response.statusCode() == 429) {
                if (retryCount >= MAX_RETRIES) {
                    throw new MarketDataException(
                            "Rate limit retry limit (" + MAX_RETRIES + ") exceeded for " + symbol);
                }
                log.warn("Rate limit hit for {} (attempt {}/{}), waiting 60 seconds...",
                        symbol, retryCount + 1, MAX_RETRIES);
                Thread.sleep(60000);
                return fetchChunk(symbol, startDate, endDate, retryCount + 1);
            }
            
            if (response.statusCode() == 404) {
                log.warn("Symbol {} not found (404)", symbol);
                return Collections.emptyList();
            }
            
            if (response.statusCode() != 200) {
                log.error("HTTP {} for {}: {}", response.statusCode(), symbol, response.body());
                throw new MarketDataException("HTTP " + response.statusCode() + " for " + symbol);
            }
            
            // Parse JSON response
            return parseTimeSeriesResponse(response.body(), symbol);
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new MarketDataException("Interrupted while fetching data for " + symbol, e);
        } catch (MarketDataException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error fetching data for {}: {}", symbol, e.getMessage(), e);
            throw new MarketDataException("Failed to fetch data for " + symbol, e);
        }
    }

    /**
     * Smart rate limiting: Batch 8 requests per minute, then wait.
     * 
     * Strategy:
     * 1. Make up to 8 requests as fast as possible (API calls are quick ~1-2s each)
     * 2. When we hit 8 requests in current minute, wait until next minute starts
     * 3. Reset counter and continue
     * 
     * This is MUCH faster than spacing requests 7-8 seconds apart.
     * Example: 8 requests take ~15 seconds, then wait 45s = 1 minute per 8 requests
     * vs old: 8 requests * 8s = 64 seconds per 8 requests
     */
    private void enforceRateLimit() throws InterruptedException {
        long now = System.currentTimeMillis();
        long currentMinute = now / 60000; // Minute boundary (0, 1, 2, ...)
        
        // Reset counter if we've moved to a new minute
        if (currentMinute != currentMinuteStart) {
            currentMinuteStart = currentMinute;
            requestsInCurrentMinute = 0;
            log.info("✓ Rate limit: New minute started, counter reset");
        }
        
        // Check if we've hit the per-minute limit
        if (requestsInCurrentMinute >= MAX_REQUESTS_PER_MINUTE) {
            long nextMinuteStart = (currentMinuteStart + 1) * 60000;
            long waitTime = nextMinuteStart - now + 500; // +500ms buffer
            log.warn("⏸ Rate limit: Hit {}/{} requests, waiting {}s for next minute...", 
                requestsInCurrentMinute, MAX_REQUESTS_PER_MINUTE, waitTime / 1000);
            Thread.sleep(waitTime);
            
            // Reset for new minute
            currentMinuteStart = System.currentTimeMillis() / 60000;
            requestsInCurrentMinute = 0;
            log.info("✓ Rate limit: New minute started, resuming...");
        }
        
        // Increment counter (no delay between requests!)
        requestsInCurrentMinute++;
        log.debug("⚡ Request {}/{} in current minute", 
            requestsInCurrentMinute, MAX_REQUESTS_PER_MINUTE);
    }

    /**
     * Parse Twelve Data time_series JSON response.
     * 
     * Response structure:
     * {
     *   "meta": {"symbol": "MSFT", ...},
     *   "values": [
     *     {
     *       "datetime": "2024-01-15",
     *       "open": "150.25",
     *       "high": "152.50",
     *       "low": "149.75",
     *       "close": "151.80",  // Already adjusted when adjust=all
     *       "volume": "12345678"
     *     }
     *   ],
     *   "status": "ok"
     * }
     */
    private List<HistoricalPrice> parseTimeSeriesResponse(String responseBody, String symbol) 
            throws MarketDataException {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            
            // Check for API errors
            if (root.has("status") && "error".equals(root.get("status").asText())) {
                String message = root.has("message") ? root.get("message").asText() : "Unknown error";
                throw new MarketDataException("Twelve Data error for " + symbol + ": " + message);
            }
            
            // Get values array
            JsonNode values = root.get("values");
            if (values == null || !values.isArray() || values.isEmpty()) {
                log.warn("No data values in response for {}", symbol);
                return Collections.emptyList();
            }
            
            // Parse each data point
            List<HistoricalPrice> prices = new ArrayList<>();
            
            for (JsonNode value : values) {
                try {
                    LocalDate date = LocalDate.parse(value.get("datetime").asText(), dateFormatter);
                    
                    // When adjust=all is used, all OHLC values are adjusted for splits/dividends
                    BigDecimal close = new BigDecimal(value.get("close").asText());
                    
                    prices.add(new HistoricalPrice(
                        date,
                        new BigDecimal(value.get("open").asText()),
                        new BigDecimal(value.get("high").asText()),
                        new BigDecimal(value.get("low").asText()),
                        close,
                        close,  // adjustedClose = close when adjust=all
                        value.has("volume") ? Long.parseLong(value.get("volume").asText()) : 0L
                    ));
                } catch (Exception e) {
                    log.warn("Failed to parse data point for {}: {}", symbol, e.getMessage());
                    // Skip this data point and continue
                }
            }
            
            return prices;
            
        } catch (MarketDataException e) {
            throw e;
        } catch (Exception e) {
            throw new MarketDataException("Failed to parse response for " + symbol, e);
        }
    }

    /**
     * Fetch company profile from Twelve Data /profile endpoint.
     * Returns name, exchange, sector, industry, and type.
     * Counts as 1 API credit.
     */
    @Override
    public Optional<AssetProfile> getAssetProfile(String symbol) {
        if (apiKey == null || apiKey.isBlank()) {
            return Optional.empty();
        }

        try {
            enforceRateLimit();

            String url = String.format(
                "%s/profile?symbol=%s&apikey=%s",
                baseUrl, symbol, apiKey);

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                log.warn("HTTP {} fetching profile for {}", response.statusCode(), symbol);
                return Optional.empty();
            }

            JsonNode root = objectMapper.readTree(response.body());

            if (root.has("status") && "error".equals(root.get("status").asText())) {
                log.warn("Profile API error for {}: {}", symbol,
                    root.has("message") ? root.get("message").asText() : "unknown");
                return Optional.empty();
            }

            String name = root.has("name") ? root.get("name").asText() : null;
            String exchange = root.has("exchange") ? root.get("exchange").asText() : null;
            String sector = root.has("sector") ? root.get("sector").asText() : null;
            String industry = root.has("industry") ? root.get("industry").asText() : null;
            String type = root.has("type") ? root.get("type").asText() : null;

            log.info("Profile for {}: name={}, exchange={}, sector={}, industry={}, type={}",
                symbol, name, exchange, sector, industry, type);

            return Optional.of(new AssetProfile(name, exchange, sector, industry, type));

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Interrupted fetching profile for {}", symbol);
            return Optional.empty();
        } catch (Exception e) {
            log.error("Error fetching profile for {}: {}", symbol, e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public boolean isHealthy() {
        try {
            if (apiKey == null || apiKey.isBlank()) {
                return false;
            }
            
            // Quick health check with SPY (last 5 days)
            LocalDate to = LocalDate.now();
            LocalDate from = to.minusDays(5);
            List<HistoricalPrice> prices = getHistoricalPrices("SPY", from, to);
            
            return !prices.isEmpty();
            
        } catch (Exception e) {
            log.warn("Twelve Data health check failed: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public String getProviderName() {
        return "Twelve Data";
    }
}
