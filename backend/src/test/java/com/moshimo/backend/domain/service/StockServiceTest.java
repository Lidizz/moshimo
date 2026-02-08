package com.moshimo.backend.domain.service;

import com.moshimo.backend.application.dto.response.StockDTO;
import com.moshimo.backend.domain.model.AssetType;
import com.moshimo.backend.domain.model.Stock;
import com.moshimo.backend.domain.repository.StockPriceRepository;
import com.moshimo.backend.domain.repository.StockRepository;
import com.moshimo.backend.web.exception.StockNotFoundException;
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
 * Unit tests for StockService.
 * 
 * Tests cover:
 * - Asset type filtering (STOCK, ETF, INDEX)
 * - Sector filtering
 * - Combined filters
 * - Stock lookup by symbol
 * - Available sectors retrieval
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Stock Service Tests")
class StockServiceTest {

    @Mock
    private StockRepository stockRepository;

    @Mock
    private StockPriceRepository stockPriceRepository;

    @InjectMocks
    private StockService stockService;

    private Stock aaplStock;
    private Stock spyETF;
    private Stock spxIndex;

    @BeforeEach
    void setUp() {
        // Create AAPL stock
        aaplStock = Stock.builder()
                .id(1L)
                .symbol("AAPL")
                .name("Apple Inc.")
                .assetType(AssetType.STOCK)
                .sector("Technology")
                .isActive(true)
                .build();

        // Create SPY ETF
        spyETF = Stock.builder()
                .id(2L)
                .symbol("SPY")
                .name("SPDR S&P 500 ETF Trust")
                .assetType(AssetType.ETF)
                .sector(null)
                .isActive(true)
                .build();

        // Create SPX Index
        spxIndex = Stock.builder()
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
        when(stockRepository.findByAssetTypeAndIsActiveTrue(AssetType.STOCK))
            .thenReturn(List.of(aaplStock));

        // Act
        List<StockDTO> result = stockService.getStocks(AssetType.STOCK, null);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("AAPL", result.get(0).symbol());
        assertEquals(AssetType.STOCK, result.get(0).assetType());
        verify(stockRepository).findByAssetTypeAndIsActiveTrue(AssetType.STOCK);
    }

    @Test
    @DisplayName("Filter by asset type - ETF returns only ETFs")
    void testFilterByAssetType_etf_returnsOnlyETFs() {
        // Arrange
        when(stockRepository.findByAssetTypeAndIsActiveTrue(AssetType.ETF))
            .thenReturn(List.of(spyETF));

        // Act
        List<StockDTO> result = stockService.getStocks(AssetType.ETF, null);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("SPY", result.get(0).symbol());
        assertEquals(AssetType.ETF, result.get(0).assetType());
        verify(stockRepository).findByAssetTypeAndIsActiveTrue(AssetType.ETF);
    }

    @Test
    @DisplayName("Filter by asset type - INDEX returns only indexes")
    void testFilterByAssetType_index_returnsOnlyIndexes() {
        // Arrange
        when(stockRepository.findByAssetTypeAndIsActiveTrue(AssetType.INDEX))
            .thenReturn(List.of(spxIndex));

        // Act
        List<StockDTO> result = stockService.getStocks(AssetType.INDEX, null);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("^SPX", result.get(0).symbol());
        assertEquals(AssetType.INDEX, result.get(0).assetType());
        verify(stockRepository).findByAssetTypeAndIsActiveTrue(AssetType.INDEX);
    }

    @Test
    @DisplayName("Filter by sector - Technology returns tech stocks")
    void testFilterBySector_technology_returnsTechStocks() {
        // Arrange
        when(stockRepository.findBySectorAndIsActiveTrue("Technology"))
            .thenReturn(List.of(aaplStock));

        // Act
        List<StockDTO> result = stockService.getStocks(null, "Technology");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("AAPL", result.get(0).symbol());
        assertEquals("Technology", result.get(0).sector());
        verify(stockRepository).findBySectorAndIsActiveTrue("Technology");
    }

    @Test
    @DisplayName("Filter by type and sector - Combined filters work correctly")
    void testCombinedFilters_stockAndSector_returnsMatchingStocks() {
        // Arrange
        when(stockRepository.findByAssetTypeAndSectorAndIsActiveTrue(AssetType.STOCK, "Technology"))
            .thenReturn(List.of(aaplStock));

        // Act
        List<StockDTO> result = stockService.getStocks(AssetType.STOCK, "Technology");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("AAPL", result.get(0).symbol());
        assertEquals(AssetType.STOCK, result.get(0).assetType());
        assertEquals("Technology", result.get(0).sector());
        verify(stockRepository).findByAssetTypeAndSectorAndIsActiveTrue(AssetType.STOCK, "Technology");
    }

    @Test
    @DisplayName("No filters - Returns all active stocks")
    void testNoFilters_returnsAllActiveStocks() {
        // Arrange
        when(stockRepository.findByIsActiveTrue())
            .thenReturn(List.of(aaplStock, spyETF, spxIndex));

        // Act
        List<StockDTO> result = stockService.getStocks(null, null);

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());
        verify(stockRepository).findByIsActiveTrue();
    }

    @Test
    @DisplayName("Get stock by symbol - Returns correct stock")
    void testGetStockBySymbol_validSymbol_returnsStock() {
        // Arrange
        when(stockRepository.findBySymbol("AAPL"))
            .thenReturn(Optional.of(aaplStock));

        // Act
        StockDTO result = stockService.getStockBySymbol("aapl"); // Test case insensitivity

        // Assert
        assertNotNull(result);
        assertEquals("AAPL", result.symbol());
        assertEquals("Apple Inc.", result.name());
        verify(stockRepository).findBySymbol("AAPL");
    }

    @Test
    @DisplayName("Get stock by symbol - Throws exception for invalid symbol")
    void testGetStockBySymbol_invalidSymbol_throwsException() {
        // Arrange
        when(stockRepository.findBySymbol("INVALID"))
            .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(StockNotFoundException.class, () -> {
            stockService.getStockBySymbol("invalid");
        });
        verify(stockRepository).findBySymbol("INVALID");
    }

    @Test
    @DisplayName("Get available sectors - Returns unique sector list")
    void testGetAvailableSectors_returnsUniqueSectors() {
        // Arrange
        when(stockRepository.findDistinctSectors())
            .thenReturn(List.of("Technology", "Healthcare", "Financial Services"));

        // Act
        List<String> result = stockService.getAvailableSectors();

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());
        assertTrue(result.contains("Technology"));
        assertTrue(result.contains("Healthcare"));
        assertTrue(result.contains("Financial Services"));
        verify(stockRepository).findDistinctSectors();
    }

    @Test
    @DisplayName("Empty sector filter - Treats as no filter")
    void testEmptySectorFilter_treatsAsNoFilter() {
        // Arrange
        when(stockRepository.findByAssetTypeAndIsActiveTrue(AssetType.STOCK))
            .thenReturn(List.of(aaplStock));

        // Act
        List<StockDTO> result = stockService.getStocks(AssetType.STOCK, "");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        // Should use type-only filter, not combined filter
        verify(stockRepository).findByAssetTypeAndIsActiveTrue(AssetType.STOCK);
        verify(stockRepository, never()).findByAssetTypeAndSectorAndIsActiveTrue(any(), anyString());
    }

    @Test
    @DisplayName("Blank sector filter - Treats as no filter")
    void testBlankSectorFilter_treatsAsNoFilter() {
        // Arrange
        when(stockRepository.findByAssetTypeAndIsActiveTrue(AssetType.STOCK))
            .thenReturn(List.of(aaplStock));

        // Act
        List<StockDTO> result = stockService.getStocks(AssetType.STOCK, "   ");

        // Assert
        assertNotNull(result);
        verify(stockRepository).findByAssetTypeAndIsActiveTrue(AssetType.STOCK);
        verify(stockRepository, never()).findByAssetTypeAndSectorAndIsActiveTrue(any(), anyString());
    }
}
