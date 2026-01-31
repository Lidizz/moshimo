package com.moshimo.backend.domain.service;

import com.moshimo.backend.application.dto.response.PriceDataDTO;
import com.moshimo.backend.application.dto.response.StockDTO;
import com.moshimo.backend.domain.model.AssetType;
import com.moshimo.backend.domain.model.Stock;
import com.moshimo.backend.domain.model.StockPrice;
import com.moshimo.backend.domain.repository.StockPriceRepository;
import com.moshimo.backend.domain.repository.StockRepository;
import com.moshimo.backend.web.exception.StockNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Stock Service - Business logic for stock operations.
 * 
 * Learning Notes:
 * - @Service: Marks this as a service layer component
 * - @Transactional(readOnly = true): Optimizes read-only database operations
 * - Stream API: Functional programming for data transformation
 * - Method references: Entity::toDTO conversion
 * 
 * Design Pattern: Service Layer (separates business logic from controllers)
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class StockService {

    private final StockRepository stockRepository;
    private final StockPriceRepository stockPriceRepository;

    /**
     * Get all active stocks with optional filtering.
     * 
     * @param assetType optional filter by asset type
     * @param sector optional filter by sector
     * @return list of matching stock DTOs
     */
    public List<StockDTO> getStocks(AssetType assetType, String sector) {
        log.debug("Fetching stocks with filters - type: {}, sector: {}", assetType, sector);
        
        List<Stock> stocks;
        
        if (assetType != null && sector != null && !sector.isBlank()) {
            // Both filters
            stocks = stockRepository.findByAssetTypeAndSectorAndIsActiveTrue(assetType, sector);
        } else if (assetType != null) {
            // Type filter only
            stocks = stockRepository.findByAssetTypeAndIsActiveTrue(assetType);
        } else if (sector != null && !sector.isBlank()) {
            // Sector filter only
            stocks = stockRepository.findBySectorAndIsActiveTrue(sector);
        } else {
            // No filters
            stocks = stockRepository.findByIsActiveTrue();
        }
        
        return stocks.stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }

    /**
     * Get all active stocks (no filtering).
     * Kept for backward compatibility.
     * 
     * @return list of active stock DTOs
     */
    public List<StockDTO> getAllActiveStocks() {
        return getStocks(null, null);
    }

    /**
     * Get list of available sectors.
     * 
     * @return list of distinct sector names
     */
    public List<String> getAvailableSectors() {
        return stockRepository.findDistinctSectors();
    }

    /**
     * Get stock by symbol.
     * 
     * @param symbol stock ticker symbol
     * @return stock DTO
     * @throws StockNotFoundException if stock not found
     */
    public StockDTO getStockBySymbol(String symbol) {
        log.debug("Fetching stock: {}", symbol);
        Stock stock = stockRepository.findBySymbol(symbol.toUpperCase())
            .orElseThrow(() -> new StockNotFoundException("Stock not found: " + symbol));
        return toDTO(stock);
    }

    /**
     * Get stock entity by symbol (for internal use).
     * 
     * @param symbol stock ticker
     * @return Stock entity
     * @throws StockNotFoundException if not found
     */
    public Stock getStockEntityBySymbol(String symbol) {
        return stockRepository.findBySymbol(symbol.toUpperCase())
            .orElseThrow(() -> new StockNotFoundException("Stock not found: " + symbol));
    }

    /**
     * Get historical prices for a stock within date range.
     * 
     * @param symbol stock ticker
     * @param startDate start date (inclusive)
     * @param endDate end date (inclusive)
     * @return list of price data DTOs
     */
    public List<PriceDataDTO> getPriceHistory(String symbol, LocalDate startDate, LocalDate endDate) {
        log.debug("Fetching price history for {} from {} to {}", symbol, startDate, endDate);
        
        Stock stock = getStockEntityBySymbol(symbol);
        
        // Validate date range
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date must be before or equal to end date");
        }
        
        if (stock.getIpoDate() != null && startDate.isBefore(stock.getIpoDate())) {
            log.warn("Start date {} is before IPO date {} for {}", startDate, stock.getIpoDate(), symbol);
        }
        
        List<StockPrice> prices = stockPriceRepository.findByStockIdAndDateBetween(
            stock.getId(), startDate, endDate
        );
        
        log.info("Found {} price records for {} between {} and {}", 
                 prices.size(), symbol, startDate, endDate);
        
        return prices.stream()
            .map(this::toPriceDTO)
            .collect(Collectors.toList());
    }

    /**
     * Convert Stock entity to DTO.
     * 
     * Learning: Mapping layer separates domain model from API representation.
     */
    private StockDTO toDTO(Stock stock) {
        return new StockDTO(
            stock.getId(),
            stock.getSymbol(),
            stock.getName(),
            stock.getAssetType(),
            stock.getSector(),
            stock.getIndustry(),
            stock.getExchange(),
            stock.getIpoDate(),
            stock.getIsActive()
        );
    }

    /**
     * Convert StockPrice entity to DTO.
     */
    private PriceDataDTO toPriceDTO(StockPrice price) {
        return new PriceDataDTO(
            price.getDate(),
            price.getOpen(),
            price.getHigh(),
            price.getLow(),
            price.getClose(),
            price.getVolume(),
            price.getAdjustedClose()
        );
    }
}