package com.moshimo.backend.web.controller;

import com.moshimo.backend.application.dto.response.PriceDataDTO;
import com.moshimo.backend.application.dto.response.StockDTO;
import com.moshimo.backend.domain.model.AssetType;
import com.moshimo.backend.domain.service.StockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Stock Controller - REST API endpoints for stock data.
 * 
 * Learning Notes:
 * - @RestController: Automatically serializes return values to JSON
 * - @RequestMapping: Base path for all endpoints
 * - @PathVariable: Extract value from URL path
 * - @RequestParam: Extract query parameters
 * - ResponseEntity<T>: Provides control over HTTP response (status, headers, body)
 * 
 * Design Pattern: Controller Pattern (MVC architecture)
 */
@RestController
@RequestMapping("/api/stocks")
@RequiredArgsConstructor
@Slf4j
public class StockController {

    private final StockService stockService;

    /**
     * Get all active stocks with optional filtering.
     * 
     * GET /api/stocks
     * GET /api/stocks?type=ETF
     * GET /api/stocks?sector=Technology
     * GET /api/stocks?type=STOCK&sector=Technology
     * 
     * @param type optional asset type filter (STOCK, ETF, INDEX)
     * @param sector optional sector filter
     * @return filtered list of stocks
     */
    @GetMapping
    public ResponseEntity<List<StockDTO>> getStocks(
            @RequestParam(required = false) AssetType type,
            @RequestParam(required = false) String sector) {
        
        log.info("GET /api/stocks - type: {}, sector: {}", type, sector);
        List<StockDTO> stocks = stockService.getStocks(type, sector);
        log.info("Returning {} stocks", stocks.size());
        return ResponseEntity.ok(stocks);
    }

    /**
     * Get available sectors.
     * 
     * GET /api/stocks/sectors
     * 
     * @return list of distinct sector names
     */
    @GetMapping("/sectors")
    public ResponseEntity<List<String>> getSectors() {
        log.info("GET /api/stocks/sectors");
        List<String> sectors = stockService.getAvailableSectors();
        return ResponseEntity.ok(sectors);
    }

    /**
     * Get stock by symbol.
     * 
     * GET /api/stocks/{symbol}
     * 
     * Example: GET /api/stocks/AAPL
     * 
     * @param symbol stock ticker symbol
     * @return stock details
     */
    @GetMapping("/{symbol}")
    public ResponseEntity<StockDTO> getStockBySymbol(@PathVariable String symbol) {
        log.info("GET /api/stocks/{} - Fetching stock details", symbol);
        StockDTO stock = stockService.getStockBySymbol(symbol);
        return ResponseEntity.ok(stock);
    }

    /**
     * Get historical prices for a stock.
     * 
     * GET /api/stocks/{symbol}/prices?from=2020-01-01&to=2024-12-31
     * 
     * @param symbol stock ticker
     * @param from start date (inclusive)
     * @param to end date (inclusive)
     * @return list of price data points
     */
    @GetMapping("/{symbol}/prices")
    public ResponseEntity<List<PriceDataDTO>> getPriceHistory(
            @PathVariable String symbol,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        
        log.info("GET /api/stocks/{}/prices?from={}&to={}", symbol, from, to);
        List<PriceDataDTO> prices = stockService.getPriceHistory(symbol, from, to);
        log.info("Returning {} price records", prices.size());
        return ResponseEntity.ok(prices);
    }
}