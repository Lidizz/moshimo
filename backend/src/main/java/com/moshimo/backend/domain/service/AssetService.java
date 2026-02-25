package com.moshimo.backend.domain.service;

import com.moshimo.backend.application.dto.response.PriceDataDTO;
import com.moshimo.backend.application.dto.response.AssetDTO;
import com.moshimo.backend.domain.model.AssetType;
import com.moshimo.backend.domain.model.Asset;
import com.moshimo.backend.domain.model.AssetPrice;
import com.moshimo.backend.domain.repository.AssetPriceRepository;
import com.moshimo.backend.domain.repository.AssetRepository;
import com.moshimo.backend.web.exception.AssetNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Asset Service - Business logic for asset operations.
 * 
 * Learning Notes:
 * - @Service: Marks this as a service layer component
 * - @Transactional(readOnly = true): Optimizes read-only database operations
 * - Stream API: Functional programming for data transformation
 * 
 * Design Pattern: Service Layer (separates business logic from controllers)
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class AssetService {

    private final AssetRepository assetRepository;
    private final AssetPriceRepository assetPriceRepository;

    /**
     * Get all active assets with optional filtering.
     * 
     * @param assetType optional filter by asset type
     * @param sector optional filter by sector
     * @return list of matching asset DTOs
     */
    public List<AssetDTO> getAssets(AssetType assetType, String sector) {
        log.debug("Fetching assets with filters - type: {}, sector: {}", assetType, sector);
        
        List<Asset> assets;
        
        if (assetType != null && sector != null && !sector.isBlank()) {
            assets = assetRepository.findByAssetTypeAndSectorAndIsActiveTrue(assetType, sector);
        } else if (assetType != null) {
            assets = assetRepository.findByAssetTypeAndIsActiveTrue(assetType);
        } else if (sector != null && !sector.isBlank()) {
            assets = assetRepository.findBySectorAndIsActiveTrue(sector);
        } else {
            assets = assetRepository.findByIsActiveTrue();
        }
        
        return assets.stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }

    /**
     * Get all active assets (no filtering).
     * Kept for backward compatibility.
     * 
     * @return list of active asset DTOs
     */
    public List<AssetDTO> getAllActiveAssets() {
        return getAssets(null, null);
    }

    /**
     * Get list of available sectors.
     * 
     * @return list of distinct sector names
     */
    public List<String> getAvailableSectors() {
        return assetRepository.findDistinctSectors();
    }

    /**
     * Get asset by symbol.
     * 
     * @param symbol ticker symbol
     * @return asset DTO
     * @throws AssetNotFoundException if asset not found
     */
    public AssetDTO getAssetBySymbol(String symbol) {
        log.debug("Fetching asset: {}", symbol);
        Asset asset = assetRepository.findBySymbol(symbol.toUpperCase())
            .orElseThrow(() -> new AssetNotFoundException("Asset not found: " + symbol));
        return toDTO(asset);
    }

    /**
     * Get asset entity by symbol (for internal use).
     * 
     * @param symbol ticker
     * @return Asset entity
     * @throws AssetNotFoundException if not found
     */
    public Asset getAssetEntityBySymbol(String symbol) {
        return assetRepository.findBySymbol(symbol.toUpperCase())
            .orElseThrow(() -> new AssetNotFoundException("Asset not found: " + symbol));
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
        
        Asset asset = getAssetEntityBySymbol(symbol);
        
        // Validate date range
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date must be before or equal to end date");
        }
        
        if (asset.getIpoDate() != null && startDate.isBefore(asset.getIpoDate())) {
            log.warn("Start date {} is before listing date {} for {}", startDate, asset.getIpoDate(), symbol);
        }
        
        List<AssetPrice> prices = assetPriceRepository.findByAssetIdAndDateBetween(
            asset.getId(), startDate, endDate
        );
        
        log.info("Found {} price records for {} between {} and {}", 
                 prices.size(), symbol, startDate, endDate);
        
        return prices.stream()
            .map(this::toPriceDTO)
            .collect(Collectors.toList());
    }

    /**
     * Convert Asset entity to DTO.
     * 
     * Learning: Mapping layer separates domain model from API representation.
     */
    private AssetDTO toDTO(Asset asset) {
        return new AssetDTO(
            asset.getId(),
            asset.getSymbol(),
            asset.getName(),
            asset.getAssetType(),
            asset.getSector(),
            asset.getIndustry(),
            asset.getExchange(),
            asset.getIpoDate(),
            asset.getIsActive()
        );
    }

    /**
     * Convert AssetPrice entity to DTO.
     */
    private PriceDataDTO toPriceDTO(AssetPrice price) {
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