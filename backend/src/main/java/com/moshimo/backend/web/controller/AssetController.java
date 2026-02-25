package com.moshimo.backend.web.controller;

import com.moshimo.backend.application.dto.response.PriceDataDTO;
import com.moshimo.backend.application.dto.response.AssetDTO;
import com.moshimo.backend.domain.model.AssetType;
import com.moshimo.backend.domain.service.AssetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Asset Controller - REST API endpoints for asset data.
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
@RequestMapping("/api/assets")
@RequiredArgsConstructor
@Slf4j
public class AssetController {

    private final AssetService assetService;

    /**
     * Get all active assets with optional filtering.
     * 
     * GET /api/assets
     * GET /api/assets?type=ETF
     * GET /api/assets?sector=Technology
     * GET /api/assets?type=STOCK&sector=Technology
     * 
     * @param type optional asset type filter (STOCK, ETF, INDEX, CRYPTO)
     * @param sector optional sector filter
     * @return filtered list of assets
     */
    @GetMapping
    public ResponseEntity<List<AssetDTO>> getAssets(
            @RequestParam(required = false) AssetType type,
            @RequestParam(required = false) String sector) {
        
        log.info("GET /api/assets - type: {}, sector: {}", type, sector);
        List<AssetDTO> assets = assetService.getAssets(type, sector);
        log.info("Returning {} assets", assets.size());
        return ResponseEntity.ok(assets);
    }

    /**
     * Get available sectors.
     * 
     * GET /api/assets/sectors
     * 
     * @return list of distinct sector names
     */
    @GetMapping("/sectors")
    public ResponseEntity<List<String>> getSectors() {
        log.info("GET /api/assets/sectors");
        List<String> sectors = assetService.getAvailableSectors();
        return ResponseEntity.ok(sectors);
    }

    /**
     * Get asset by symbol.
     * 
     * GET /api/assets/{symbol}
     * 
     * Example: GET /api/assets/AAPL
     * 
     * @param symbol ticker symbol
     * @return asset details
     */
    @GetMapping("/{symbol}")
    public ResponseEntity<AssetDTO> getAssetBySymbol(@PathVariable String symbol) {
        log.info("GET /api/assets/{} - Fetching asset details", symbol);
        AssetDTO asset = assetService.getAssetBySymbol(symbol);
        return ResponseEntity.ok(asset);
    }

    /**
     * Get historical prices for an asset.
     * 
     * GET /api/assets/{symbol}/prices?from=2020-01-01&to=2024-12-31
     * 
     * @param symbol ticker
     * @param from start date (inclusive)
     * @param to end date (inclusive)
     * @return list of price data points
     */
    @GetMapping("/{symbol}/prices")
    public ResponseEntity<List<PriceDataDTO>> getPriceHistory(
            @PathVariable String symbol,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        
        log.info("GET /api/assets/{}/prices?from={}&to={}", symbol, from, to);
        List<PriceDataDTO> prices = assetService.getPriceHistory(symbol, from, to);
        log.info("Returning {} price records", prices.size());
        return ResponseEntity.ok(prices);
    }
}