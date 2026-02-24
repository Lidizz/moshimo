package com.moshimo.backend.api;

import com.moshimo.backend.infrastructure.seeder.TwelveDataSeederService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * API Import Controller - Admin endpoints for importing data from external APIs.
 * 
 * Handles imports from external data providers (Twelve Data, future Alpha Vantage, crypto).
 * Separated from AdminController which handles local operations (CSV import, price updates).
 * 
 * Endpoints:
 *   POST /api/admin/import/twelve-data - Import from Twelve Data API
 *   GET  /api/admin/import/status/{jobId} - Check import job status (future)
 * 
 * Security Note: Protected by @Profile("dev") — not available in production.
 */
@RestController
@RequestMapping("/api/admin/import")
@RequiredArgsConstructor
@Slf4j
@Profile("dev")
public class ApiImportController {

    private final TwelveDataSeederService twelveDataSeederService;

    /**
     * Import historical stock data from Twelve Data API.
     * 
     * Example request:
     * POST /api/admin/import/twelve-data
     * {
     *   "symbols": ["MSFT", "AAPL", "GOOGL"],
     *   "clearExisting": true
     * }
     * 
     * @param request Import parameters (symbols list + clear flag)
     * @return Import summary with successes, failures, and record counts
     */
    @PostMapping("/twelve-data")
    public ResponseEntity<?> importFromTwelveData(@RequestBody ImportRequest request) {
        log.info("========================================");
        log.info("Import request received via API");
        log.info("Symbols: {}", request.symbols());
        log.info("Clear existing: {}", request.clearExisting());
        log.info("========================================");
        
        try {
            // Validate request
            if (request.symbols() == null || request.symbols().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(new ErrorResponse("At least one symbol required"));
            }
            
            if (request.symbols().size() > 50) {
                return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Maximum 50 symbols per request (rate limit protection)"));
            }
            
            // Execute import
            TwelveDataSeederService.ImportSummary summary = 
                twelveDataSeederService.seedStocks(request.symbols(), request.clearExisting());
            
            // Return summary
            return ResponseEntity.ok(summary);
            
        } catch (Exception e) {
            log.error("Import failed with exception", e);
            return ResponseEntity.internalServerError()
                .body(new ErrorResponse("Import failed: " + e.getMessage()));
        }
    }

    /**
     * Get import status (future enhancement).
     * Could track long-running imports with job IDs.
     */
    @GetMapping("/status/{jobId}")
    public ResponseEntity<?> getImportStatus(@PathVariable String jobId) {
        // TODO: Implement job tracking for long-running imports
        return ResponseEntity.ok("Job tracking not yet implemented");
    }

    // ========== Request/Response Records ==========

    /**
     * Import request payload.
     * 
     * @param symbols List of stock tickers to import (e.g., ["MSFT", "AAPL"])
     * @param clearExisting If true, deletes all existing price data before import
     */
    public record ImportRequest(
        List<String> symbols,
        boolean clearExisting
    ) {
        public ImportRequest {
            if (symbols == null) {
                throw new IllegalArgumentException("Symbols list cannot be null");
            }
            
            // Normalize symbols to uppercase
            symbols = symbols.stream()
                .map(String::toUpperCase)
                .distinct()
                .toList();
        }
    }

    /**
     * Error response payload.
     */
    public record ErrorResponse(String error) {}
}
