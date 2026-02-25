package com.moshimo.backend.api;

import com.moshimo.backend.infrastructure.importer.CsvAssetDataImporter;
import com.moshimo.backend.infrastructure.scheduler.AssetPriceUpdateScheduler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Slf4j
@Profile("dev")
public class AdminController {
    
    private final CsvAssetDataImporter csvImporter;
    private final AssetPriceUpdateScheduler updateScheduler;
    
    /**
     * Import asset data from a CSV file.
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
            CsvAssetDataImporter.ImportSummary summary = csvImporter.importFromCsv(filePath);
            
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
     * Manually trigger the monthly asset price update process.
     * Useful for testing or running updates outside the scheduled time.
     * 
     * Example request:
     * POST /api/admin/update-prices
     * 
     * @return ResponseEntity with success/failure message
     */
    @PostMapping("/update-prices")
    public ResponseEntity<Map<String, Object>> updatePrices() {
        log.info("Manual asset price update triggered via API");
        
        try {
            updateScheduler.triggerUpdateNow();
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Asset price update completed. Check logs for details."
            ));
            
        } catch (Exception e) {
            log.error("Failed to update asset prices", e);
            return ResponseEntity.status(500)
                .body(Map.of(
                    "success", false,
                    "message", "Update failed: " + e.getMessage()
                ));
        }
    }
}
