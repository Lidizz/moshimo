package com.moshimo.backend.domain.service;

import com.moshimo.backend.application.dto.response.AssetDTO;
import com.moshimo.backend.domain.model.AssetType;
import com.moshimo.backend.domain.model.Asset;
import com.moshimo.backend.domain.repository.AssetPriceRepository;
import com.moshimo.backend.domain.repository.AssetRepository;
import com.moshimo.backend.web.exception.AssetNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AssetService.
 * 
 * Tests cover:
 * - Asset type filtering (STOCK, ETF, INDEX)
 * - Sector filtering
 * - Combined filters
 * - Asset lookup by symbol
 * - Available sectors retrieval
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Asset Service Tests")
class AssetServiceTest {

    @Mock
    private AssetRepository assetRepository;

    @Mock
    private AssetPriceRepository assetPriceRepository;

    @InjectMocks
    private AssetService assetService;

    private Asset aaplStock;
    private Asset spyETF;
    private Asset spxIndex;

    @BeforeEach
    void setUp() {
        // Create AAPL stock
        aaplStock = Asset.builder()
                .id(1L)
                .symbol("AAPL")
                .name("Apple Inc.")
                .assetType(AssetType.STOCK)
                .sector("Technology")
                .isActive(true)
                .build();

        // Create SPY ETF
        spyETF = Asset.builder()
                .id(2L)
                .symbol("SPY")
                .name("SPDR S&P 500 ETF Trust")
                .assetType(AssetType.ETF)
                .sector(null)
                .isActive(true)
                .build();

        // Create SPX Index
        spxIndex = Asset.builder()
                .id(3L)
                .symbol("^SPX")
                .name("S&P 500 Index")
                .assetType(AssetType.INDEX)
                .sector(null)
                .isActive(true)
                .build();
    }

    @Test
    @DisplayName("Filter by asset type - STOCK returns only stocks")
    void testFilterByAssetType_stock_returnsOnlyStocks() {
        // Arrange
        when(assetRepository.findByAssetTypeAndIsActiveTrue(AssetType.STOCK))
            .thenReturn(List.of(aaplStock));

        // Act
        List<AssetDTO> result = assetService.getAssets(AssetType.STOCK, null);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("AAPL", result.get(0).symbol());
        assertEquals(AssetType.STOCK, result.get(0).assetType());
        verify(assetRepository).findByAssetTypeAndIsActiveTrue(AssetType.STOCK);
    }

    @Test
    @DisplayName("Filter by asset type - ETF returns only ETFs")
    void testFilterByAssetType_etf_returnsOnlyETFs() {
        // Arrange
        when(assetRepository.findByAssetTypeAndIsActiveTrue(AssetType.ETF))
            .thenReturn(List.of(spyETF));

        // Act
        List<AssetDTO> result = assetService.getAssets(AssetType.ETF, null);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("SPY", result.get(0).symbol());
        assertEquals(AssetType.ETF, result.get(0).assetType());
        verify(assetRepository).findByAssetTypeAndIsActiveTrue(AssetType.ETF);
    }

    @Test
    @DisplayName("Filter by asset type - INDEX returns only indexes")
    void testFilterByAssetType_index_returnsOnlyIndexes() {
        // Arrange
        when(assetRepository.findByAssetTypeAndIsActiveTrue(AssetType.INDEX))
            .thenReturn(List.of(spxIndex));

        // Act
        List<AssetDTO> result = assetService.getAssets(AssetType.INDEX, null);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("^SPX", result.get(0).symbol());
        assertEquals(AssetType.INDEX, result.get(0).assetType());
        verify(assetRepository).findByAssetTypeAndIsActiveTrue(AssetType.INDEX);
    }

    @Test
    @DisplayName("Filter by sector - Technology returns tech stocks")
    void testFilterBySector_technology_returnsTechStocks() {
        // Arrange
        when(assetRepository.findBySectorAndIsActiveTrue("Technology"))
            .thenReturn(List.of(aaplStock));

        // Act
        List<AssetDTO> result = assetService.getAssets(null, "Technology");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("AAPL", result.get(0).symbol());
        assertEquals("Technology", result.get(0).sector());
        verify(assetRepository).findBySectorAndIsActiveTrue("Technology");
    }

    @Test
    @DisplayName("Filter by type and sector - Combined filters work correctly")
    void testCombinedFilters_stockAndSector_returnsMatchingStocks() {
        // Arrange
        when(assetRepository.findByAssetTypeAndSectorAndIsActiveTrue(AssetType.STOCK, "Technology"))
            .thenReturn(List.of(aaplStock));

        // Act
        List<AssetDTO> result = assetService.getAssets(AssetType.STOCK, "Technology");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("AAPL", result.get(0).symbol());
        assertEquals(AssetType.STOCK, result.get(0).assetType());
        assertEquals("Technology", result.get(0).sector());
        verify(assetRepository).findByAssetTypeAndSectorAndIsActiveTrue(AssetType.STOCK, "Technology");
    }

    @Test
    @DisplayName("No filters - Returns all active assets")
    void testNoFilters_returnsAllActiveStocks() {
        // Arrange
        when(assetRepository.findByIsActiveTrue())
            .thenReturn(List.of(aaplStock, spyETF, spxIndex));

        // Act
        List<AssetDTO> result = assetService.getAssets(null, null);

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());
        verify(assetRepository).findByIsActiveTrue();
    }

    @Test
    @DisplayName("Get asset by symbol - Returns correct asset")
    void testGetStockBySymbol_validSymbol_returnsStock() {
        // Arrange
        when(assetRepository.findBySymbol("AAPL"))
            .thenReturn(Optional.of(aaplStock));

        // Act
        AssetDTO result = assetService.getAssetBySymbol("aapl"); // Test case insensitivity

        // Assert
        assertNotNull(result);
        assertEquals("AAPL", result.symbol());
        assertEquals("Apple Inc.", result.name());
        verify(assetRepository).findBySymbol("AAPL");
    }

    @Test
    @DisplayName("Get asset by symbol - Throws exception for invalid symbol")
    void testGetStockBySymbol_invalidSymbol_throwsException() {
        // Arrange
        when(assetRepository.findBySymbol("INVALID"))
            .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(AssetNotFoundException.class, () -> {
            assetService.getAssetBySymbol("invalid");
        });
        verify(assetRepository).findBySymbol("INVALID");
    }

    @Test
    @DisplayName("Get available sectors - Returns unique sector list")
    void testGetAvailableSectors_returnsUniqueSectors() {
        // Arrange
        when(assetRepository.findDistinctSectors())
            .thenReturn(List.of("Technology", "Healthcare", "Financial Services"));

        // Act
        List<String> result = assetService.getAvailableSectors();

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());
        assertTrue(result.contains("Technology"));
        assertTrue(result.contains("Healthcare"));
        assertTrue(result.contains("Financial Services"));
        verify(assetRepository).findDistinctSectors();
    }

    @Test
    @DisplayName("Empty sector filter - Treats as no filter")
    void testEmptySectorFilter_treatsAsNoFilter() {
        // Arrange
        when(assetRepository.findByAssetTypeAndIsActiveTrue(AssetType.STOCK))
            .thenReturn(List.of(aaplStock));

        // Act
        List<AssetDTO> result = assetService.getAssets(AssetType.STOCK, "");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        // Should use type-only filter, not combined filter
        verify(assetRepository).findByAssetTypeAndIsActiveTrue(AssetType.STOCK);
        verify(assetRepository, never()).findByAssetTypeAndSectorAndIsActiveTrue(any(), anyString());
    }

    @Test
    @DisplayName("Blank sector filter - Treats as no filter")
    void testBlankSectorFilter_treatsAsNoFilter() {
        // Arrange
        when(assetRepository.findByAssetTypeAndIsActiveTrue(AssetType.STOCK))
            .thenReturn(List.of(aaplStock));

        // Act
        List<AssetDTO> result = assetService.getAssets(AssetType.STOCK, "   ");

        // Assert
        assertNotNull(result);
        verify(assetRepository).findByAssetTypeAndIsActiveTrue(AssetType.STOCK);
        verify(assetRepository, never()).findByAssetTypeAndSectorAndIsActiveTrue(any(), anyString());
    }
}
