package com.moshimo.backend.infrastructure.importer;

import com.moshimo.backend.domain.model.AssetType;
import com.moshimo.backend.domain.model.Stock;
import com.moshimo.backend.domain.model.StockPrice;
import com.moshimo.backend.domain.repository.StockPriceRepository;
import com.moshimo.backend.domain.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * CSV Stock Data Importer - Bulk import from Kaggle datasets.
 * 
 * Learning Notes:
 * - File I/O with BufferedReader for memory efficiency
 * - CSV parsing with proper error handling
 * - Batch processing for performance
 * - Transaction management for data integrity
 * - Idempotency: Skip existing stocks/prices
 * 
 * Expected CSV Format (Kaggle):
 * Date,Open,High,Low,Close,Volume,Adj Close,Symbol,Name
 * 2020-01-02,150.25,152.50,149.75,151.80,12345678,151.80,AAPL,Apple Inc.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CsvStockDataImporter {

    private final StockRepository stockRepository;
    private final StockPriceRepository stockPriceRepository;
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final int BATCH_SIZE = 1000;

    /**
     * Import stocks and historical prices from CSV file.
     * 
     * @param csvFilePath absolute path to CSV file
     * @return import summary
     */
    @Transactional
    public ImportSummary importFromCsv(String csvFilePath) {
        log.info("Starting CSV import from: {}", csvFilePath);
        
        // Extract symbol from filename (e.g., "AAPL.csv" -> "AAPL")
        String filename = csvFilePath.substring(csvFilePath.lastIndexOf('\\') + 1);
        filename = filename.substring(filename.lastIndexOf('/') + 1); // Handle both separators
        String symbolFromFilename = filename.replace(".csv", "").toUpperCase();
        log.info("Extracted symbol from filename: {}", symbolFromFilename);
        
        ImportSummary summary = new ImportSummary();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(csvFilePath))) {
            String line;
            String[] headers = null;
            int lineNumber = 0;
            List<StockPrice> batch = new ArrayList<>();

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                
                // Parse header row
                if (lineNumber == 1) {
                    headers = line.split(",");
                    continue;
                }

                try {
                    CsvRow row = parseCsvLine(line, headers);
                    
                    // Use symbol from filename if not in CSV
                    if (row.symbol == null || row.symbol.isEmpty()) {
                        row.symbol = symbolFromFilename;
                    }
                    
                    // Get or create stock
                    Stock stock = getOrCreateStock(row);
                    
                    // Check if price already exists
                    if (stockPriceRepository.existsByStockIdAndDate(stock.getId(), row.date)) {
                        summary.skippedPrices++;
                        continue;
                    }

                    // Create price record
                    StockPrice price = StockPrice.builder()
                        .stock(stock)
                        .date(row.date)
                        .open(row.open)
                        .high(row.high)
                        .low(row.low)
                        .close(row.close)
                        .volume(row.volume)
                        .adjustedClose(row.adjustedClose)
                        .build();

                    batch.add(price);
                    summary.importedPrices++;

                    // Batch insert for performance
                    if (batch.size() >= BATCH_SIZE) {
                        stockPriceRepository.saveAll(batch);
                        batch.clear();
                        log.info("Imported {} price records so far...", summary.importedPrices);
                    }

                } catch (Exception e) {
                    log.warn("Failed to parse line {}: {}", lineNumber, e.getMessage());
                    summary.failedLines++;
                }
            }

            // Save remaining batch
            if (!batch.isEmpty()) {
                stockPriceRepository.saveAll(batch);
            }

            log.info("CSV import complete: {}", summary);
            return summary;

        } catch (IOException e) {
            log.error("Failed to read CSV file: {}", e.getMessage());
            throw new RuntimeException("CSV import failed", e);
        }
    }

    /**
     * Get existing stock or create new one from CSV data.
     */
    private Stock getOrCreateStock(CsvRow row) {
        return stockRepository.findBySymbol(row.symbol)
            .orElseGet(() -> {
                String stockName = row.name != null ? row.name : row.symbol;
                Stock newStock = Stock.builder()
                    .symbol(row.symbol)
                    .name(stockName)
                    .assetType(AssetType.inferFromSymbol(row.symbol, stockName))
                    .isActive(true)
                    .build();
                
                Stock saved = stockRepository.save(newStock);
                log.info("Created new stock: {} ({}) [{}]", saved.getSymbol(), saved.getName(), saved.getAssetType());
                return saved;
            });
    }

    /**
     * Parse CSV line into CsvRow object.
     * 
     * Handles different CSV formats from Kaggle datasets.
     */
    private CsvRow parseCsvLine(String line, String[] headers) {
        String[] values = line.split(",");
        
        CsvRow row = new CsvRow();
        
        for (int i = 0; i < headers.length && i < values.length; i++) {
            String header = headers[i].trim().toLowerCase();
            String value = values[i].trim();
            
            try {
                switch (header) {
                    case "date" -> row.date = LocalDate.parse(value, DATE_FORMATTER);
                    case "open" -> row.open = new BigDecimal(value);
                    case "high" -> row.high = new BigDecimal(value);
                    case "low" -> row.low = new BigDecimal(value);
                    case "close" -> row.close = new BigDecimal(value);
                    case "volume" -> row.volume = Long.parseLong(value);
                    case "adj close", "adjusted close" -> row.adjustedClose = new BigDecimal(value);
                    case "symbol", "ticker" -> row.symbol = value.toUpperCase();
                    case "name", "company" -> row.name = value;
                }
            } catch (Exception e) {
                log.debug("Failed to parse field {}: {}", header, e.getMessage());
            }
        }
        
        // Fallback: use close as adjusted close if not provided
        if (row.adjustedClose == null && row.close != null) {
            row.adjustedClose = row.close;
        }
        
        return row;
    }

    /**
     * CSV row data structure.
     */
    private static class CsvRow {
        LocalDate date;
        BigDecimal open;
        BigDecimal high;
        BigDecimal low;
        BigDecimal close;
        BigDecimal adjustedClose;
        Long volume;
        String symbol;
        String name;
    }

    /**
     * Import summary.
     */
    public static class ImportSummary {
        public int importedPrices = 0;
        public int skippedPrices = 0;
        public int failedLines = 0;

        @Override
        public String toString() {
            return String.format(
                "Imported: %d, Skipped: %d, Failed: %d",
                importedPrices, skippedPrices, failedLines
            );
        }
    }
}
