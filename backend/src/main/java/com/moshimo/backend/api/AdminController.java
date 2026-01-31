package com.moshimo.backend.api;

import com.moshimo.backend.infrastructure.importer.CsvStockDataImporter;
import com.moshimo.backend.infrastructure.scheduler.StockPriceUpdateScheduler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminController {
    
    private final CsvStockDataImporter csvImporter;
    private final StockPriceUpdateScheduler updateScheduler;
    
    /**
     * Import stock data from a CSV file.
     * 
     * Expected CSV format:
     * Date,Open,High,Low,Close,Volume,Adj Close,Symbol,Name
     * 
     * Example request:
     * POST /api/admin/import-csv
     * {
     *   "filePath": "C:/data/stocks/sp500-1964-2024.csv"
     * }
     * 
     * @param request Map containing the filePath
     * @return ResponseEntity with success/failure message
     */
    @PostMapping("/import-csv")
    public ResponseEntity<Map<String, Object>> importFromCsv(@RequestBody Map<String, String> request) {
        String filePath = request.get("filePath");
        
        if (filePath == null || filePath.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                .body(Map.of(
                    "success", false,
                    "message", "File path is required"
                ));
        }
        
        log.info("Starting CSV import from: {}", filePath);
        
        try {
            CsvStockDataImporter.ImportSummary summary = csvImporter.importFromCsv(filePath);
            
            log.info("Import completed: {}", summary);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Import completed successfully",
                "importedPrices", summary.importedPrices,
                "skippedPrices", summary.skippedPrices,
                "failedLines", summary.failedLines
            ));
            
        } catch (Exception e) {
            log.error("Unexpected error during CSV import", e);
            return ResponseEntity.status(500)
                .body(Map.of(
                    "success", false,
                    "message", "Import failed: " + e.getMessage()
                ));
        }
    }
    
    /**
     * Health check endpoint to verify admin API is accessible.
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "service", "Admin API"
        ));
    }
    
    /**
     * Manually trigger the monthly stock price update process.
     * Useful for testing or running updates outside the scheduled time.
     * 
     * Example request:
     * POST /api/admin/update-prices
     * 
     * @return ResponseEntity with success/failure message
     */
    @PostMapping("/update-prices")
    public ResponseEntity<Map<String, Object>> updatePrices() {
        log.info("Manual stock price update triggered via API");
        
        try {
            updateScheduler.triggerUpdateNow();
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Stock price update completed. Check logs for details."
            ));
            
        } catch (Exception e) {
            log.error("Failed to update stock prices", e);
            return ResponseEntity.status(500)
                .body(Map.of(
                    "success", false,
                    "message", "Update failed: " + e.getMessage()
                ));
        }
    }
}
