package com.moshimo.backend.domain.service;

import com.moshimo.backend.application.dto.request.Timeframe;
import com.moshimo.backend.application.dto.response.SimulationResponse;
import com.moshimo.backend.domain.model.Asset;
import com.moshimo.backend.domain.model.AssetPrice;
import com.moshimo.backend.domain.model.AssetType;
import com.moshimo.backend.domain.repository.AssetPriceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for BenchmarkService.
 *
 * Covers:
 * - Real SPY data path (happy path)
 * - Empty SPY data → CAGR fallback
 * - SPY fetch exception → CAGR fallback
 * - CAGR fallback value correctness
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("BenchmarkService Tests")
class BenchmarkServiceTest {

    @Mock
    private AssetService assetService;

    @Mock
    private AssetPriceRepository assetPriceRepository;

    @Mock
    private TimelineAggregator timelineAggregator;

    @InjectMocks
    private BenchmarkService benchmarkService;

    private Asset spyAsset;

    @BeforeEach
    void setUp() {
        spyAsset = Asset.builder()
                .id(99L)
                .symbol("SPY")
                .name("SPDR S&P 500 ETF Trust")
                .assetType(AssetType.ETF)
                .build();
    }

    // ── Real SPY data path ──────────────────────────────────────────────

    @Test
    @DisplayName("SPY data available → portfolio values scaled to SPY growth")
    void calculateBenchmarkTimeline_withSpyData_returnsScaledTimeline() {
        // Arrange
        LocalDate start = LocalDate.of(2023, 1, 3);
        LocalDate end = LocalDate.of(2023, 1, 5);
        BigDecimal invested = new BigDecimal("10000.00");

        AssetPrice day1 = price(start, "380.00");
        AssetPrice day2 = price(LocalDate.of(2023, 1, 4), "390.00");
        AssetPrice day3 = price(end, "400.00");

        when(assetService.getAssetEntityBySymbol("SPY")).thenReturn(spyAsset);
        when(assetPriceRepository.findByAssetIdAndDateBetween(eq(99L), eq(start), eq(end)))
                .thenReturn(List.of(day1, day2, day3));

        // TimelineAggregator: pass-through for simplicity
        when(timelineAggregator.aggregateTimeline(anyList(), eq(Timeframe.ALL)))
                .thenAnswer(inv -> inv.getArgument(0));

        // Act
        List<SimulationResponse.TimelinePoint> result =
                benchmarkService.calculateBenchmarkTimeline(start, end, invested, Timeframe.ALL);

        // Assert
        assertEquals(3, result.size());

        // Day 1: shares = 10000 / 380 = 26.31578947; value = 26.31578947 * 380 = 10000.00
        assertEquals(0, new BigDecimal("10000.00").compareTo(result.get(0).value()));

        // Day 3: shares * 400 = 26.31578947 * 400 ≈ 10526.32
        BigDecimal expectedEnd = invested.divide(new BigDecimal("380.00"), 8, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("400.00"))
                .setScale(2, RoundingMode.HALF_UP);
        assertEquals(0, expectedEnd.compareTo(result.get(2).value()));
    }

    @Test
    @DisplayName("SPY data available → timeline is aggregated through TimelineAggregator")
    void calculateBenchmarkTimeline_withSpyData_delegatesToAggregator() {
        // Arrange
        LocalDate start = LocalDate.of(2023, 1, 3);
        LocalDate end = LocalDate.of(2023, 6, 30);
        BigDecimal invested = new BigDecimal("5000.00");

        AssetPrice singlePrice = price(start, "400.00");

        when(assetService.getAssetEntityBySymbol("SPY")).thenReturn(spyAsset);
        when(assetPriceRepository.findByAssetIdAndDateBetween(eq(99L), eq(start), eq(end)))
                .thenReturn(List.of(singlePrice));
        when(timelineAggregator.aggregateTimeline(anyList(), eq(Timeframe.ONE_MONTH)))
                .thenReturn(List.of(new SimulationResponse.TimelinePoint(start, invested)));

        // Act
        List<SimulationResponse.TimelinePoint> result =
                benchmarkService.calculateBenchmarkTimeline(start, end, invested, Timeframe.ONE_MONTH);

        // Assert
        verify(timelineAggregator).aggregateTimeline(anyList(), eq(Timeframe.ONE_MONTH));
        assertEquals(1, result.size());
    }

    // ── Empty SPY data → CAGR fallback ──────────────────────────────────

    @Test
    @DisplayName("SPY data empty → falls back to 10% CAGR calculation")
    void calculateBenchmarkTimeline_emptySpyData_fallsBackToCAGR() {
        // Arrange
        LocalDate start = LocalDate.of(2023, 1, 1);
        LocalDate end = LocalDate.of(2024, 1, 1); // exactly 1 year
        BigDecimal invested = new BigDecimal("10000.00");

        when(assetService.getAssetEntityBySymbol("SPY")).thenReturn(spyAsset);
        when(assetPriceRepository.findByAssetIdAndDateBetween(eq(99L), eq(start), eq(end)))
                .thenReturn(Collections.emptyList());
        when(timelineAggregator.aggregateTimeline(anyList(), eq(Timeframe.ALL)))
                .thenAnswer(inv -> inv.getArgument(0));

        // Act
        List<SimulationResponse.TimelinePoint> result =
                benchmarkService.calculateBenchmarkTimeline(start, end, invested, Timeframe.ALL);

        // Assert — CAGR fallback generates daily points from start to end
        assertFalse(result.isEmpty());
        // First point is the invested amount
        assertEquals(0, invested.compareTo(result.get(0).value()));
        // Last point should be ~10% higher (1 year at 10% CAGR)
        SimulationResponse.TimelinePoint last = result.get(result.size() - 1);
        // 10000 * 1.10^1 = 11000.00
        assertTrue(last.value().compareTo(new BigDecimal("10900.00")) > 0);
        assertTrue(last.value().compareTo(new BigDecimal("11100.00")) < 0);
    }

    // ── SPY fetch exception → CAGR fallback ─────────────────────────────

    @Test
    @DisplayName("SPY fetch throws exception → fallback used, no exception propagates")
    void calculateBenchmarkTimeline_spyThrowsException_fallsBackToCAGR() {
        // Arrange
        LocalDate start = LocalDate.of(2023, 6, 1);
        LocalDate end = LocalDate.of(2023, 6, 30);
        BigDecimal invested = new BigDecimal("5000.00");

        when(assetService.getAssetEntityBySymbol("SPY"))
                .thenThrow(new RuntimeException("SPY not found in database"));
        when(timelineAggregator.aggregateTimeline(anyList(), eq(Timeframe.ALL)))
                .thenAnswer(inv -> inv.getArgument(0));

        // Act — should NOT throw
        List<SimulationResponse.TimelinePoint> result =
                benchmarkService.calculateBenchmarkTimeline(start, end, invested, Timeframe.ALL);

        // Assert — got a CAGR fallback timeline instead of an exception
        assertFalse(result.isEmpty());
        assertEquals(start, result.get(0).date());
        assertEquals(0, invested.compareTo(result.get(0).value()));
    }

    @Test
    @DisplayName("Repository exception mid-flow → CAGR fallback")
    void calculateBenchmarkTimeline_repositoryThrows_fallsBackToCAGR() {
        // Arrange
        LocalDate start = LocalDate.of(2023, 1, 1);
        LocalDate end = LocalDate.of(2023, 12, 31);
        BigDecimal invested = new BigDecimal("8000.00");

        when(assetService.getAssetEntityBySymbol("SPY")).thenReturn(spyAsset);
        when(assetPriceRepository.findByAssetIdAndDateBetween(eq(99L), any(), any()))
                .thenThrow(new RuntimeException("DB connection lost"));
        when(timelineAggregator.aggregateTimeline(anyList(), eq(Timeframe.ALL)))
                .thenAnswer(inv -> inv.getArgument(0));

        // Act
        List<SimulationResponse.TimelinePoint> result =
                benchmarkService.calculateBenchmarkTimeline(start, end, invested, Timeframe.ALL);

        // Assert
        assertFalse(result.isEmpty());
        assertEquals(0, invested.compareTo(result.get(0).value()));
    }

    // ── CAGR math correctness ───────────────────────────────────────────

    @Test
    @DisplayName("CAGR fallback - 2 year period grows ≈ 21%")
    void calculateBenchmarkTimeline_cagrFallback_twoYearsGrows21Percent() {
        // Arrange — 2 full years
        LocalDate start = LocalDate.of(2022, 1, 1);
        LocalDate end = LocalDate.of(2024, 1, 1); // 731 days
        BigDecimal invested = new BigDecimal("10000.00");

        when(assetService.getAssetEntityBySymbol("SPY"))
                .thenThrow(new RuntimeException("no SPY"));
        when(timelineAggregator.aggregateTimeline(anyList(), eq(Timeframe.ALL)))
                .thenAnswer(inv -> inv.getArgument(0));

        // Act
        List<SimulationResponse.TimelinePoint> result =
                benchmarkService.calculateBenchmarkTimeline(start, end, invested, Timeframe.ALL);

        // Assert — 10000 * 1.10^2 = 12100
        SimulationResponse.TimelinePoint last = result.get(result.size() - 1);
        assertTrue(last.value().compareTo(new BigDecimal("12000.00")) > 0,
                "Expected > 12000, got " + last.value());
        assertTrue(last.value().compareTo(new BigDecimal("12200.00")) < 0,
                "Expected < 12200, got " + last.value());
    }

    // ── Helper ──────────────────────────────────────────────────────────

    private AssetPrice price(LocalDate date, String close) {
        return AssetPrice.builder()
                .asset(spyAsset)
                .date(date)
                .close(new BigDecimal(close))
                .adjustedClose(new BigDecimal(close))
                .build();
    }
}
