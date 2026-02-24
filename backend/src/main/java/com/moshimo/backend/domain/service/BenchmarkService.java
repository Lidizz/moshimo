package com.moshimo.backend.domain.service;

import com.moshimo.backend.application.dto.request.Timeframe;
import com.moshimo.backend.application.dto.response.SimulationResponse;
import com.moshimo.backend.domain.model.Stock;
import com.moshimo.backend.domain.model.StockPrice;
import com.moshimo.backend.domain.repository.StockPriceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Calculates S&P 500 benchmark timelines for portfolio comparison.
 *
 * Extracted from InvestmentSimulationService so that @Cacheable works correctly.
 * Spring AOP proxies cannot intercept private methods — the annotated method must
 * be public on a separate Spring-managed bean.
 *
 * Dependency graph (no cycles):
 *   BenchmarkService → StockPriceRepository
 *   BenchmarkService → StockService
 *   BenchmarkService → TimelineAggregator
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class BenchmarkService {

    private final StockService stockService;
    private final StockPriceRepository stockPriceRepository;
    private final TimelineAggregator timelineAggregator;

    /**
     * Calculate S&P 500 benchmark timeline for portfolio comparison.
     *
     * Strategy:
     * 1. Try to use real SPY historical data from stock_prices table.
     * 2. Fall back to 10% annual CAGR assumption if SPY data is unavailable.
     *
     * Caching: Results are cached by date range + timeframe because SPY data is
     * identical for all users with the same parameters.
     *
     * @param startDate     earliest investment date
     * @param endDate       simulation end date
     * @param totalInvested total amount invested
     * @param timeframe     aggregation timeframe
     * @return aggregated benchmark timeline showing SPY performance
     */
    @Cacheable(value = "spyBenchmark", key = "#startDate + '-' + #endDate + '-' + #timeframe.code")
    public List<SimulationResponse.TimelinePoint> calculateBenchmarkTimeline(
            LocalDate startDate, LocalDate endDate, BigDecimal totalInvested, Timeframe timeframe) {

        log.info("Calculating S&P 500 benchmark: {} to {}, amount: {}, timeframe: {}",
                startDate, endDate, totalInvested, timeframe.getCode());

        try {
            Stock spy = stockService.getStockEntityBySymbol("SPY");

            List<StockPrice> spyPrices = stockPriceRepository
                    .findByStockIdAndDateBetween(spy.getId(), startDate, endDate)
                    .stream()
                    .sorted((a, b) -> a.getDate().compareTo(b.getDate()))
                    .collect(Collectors.toList());

            if (spyPrices.isEmpty()) {
                log.warn("No SPY data found in range, falling back to 10% CAGR");
                return calculateBenchmarkWithCAGR(startDate, endDate, totalInvested, timeframe);
            }

            StockPrice startPrice = spyPrices.get(0);
            BigDecimal startSPYPrice = startPrice.getAdjustedClose() != null
                    ? startPrice.getAdjustedClose()
                    : startPrice.getClose();

            BigDecimal spyShares = totalInvested.divide(startSPYPrice, 8, RoundingMode.HALF_UP);

            List<SimulationResponse.TimelinePoint> dailyBenchmark = spyPrices.stream()
                    .map(price -> {
                        BigDecimal spyPrice = price.getAdjustedClose() != null
                                ? price.getAdjustedClose()
                                : price.getClose();
                        BigDecimal value = spyShares.multiply(spyPrice)
                                .setScale(2, RoundingMode.HALF_UP);
                        return new SimulationResponse.TimelinePoint(price.getDate(), value);
                    })
                    .collect(Collectors.toList());

            List<SimulationResponse.TimelinePoint> aggregated =
                    timelineAggregator.aggregateTimeline(dailyBenchmark, timeframe);

            log.info("SPY benchmark: {} daily points → {} aggregated points",
                    dailyBenchmark.size(), aggregated.size());

            return aggregated;

        } catch (Exception e) {
            log.warn("Failed to calculate SPY benchmark ({}), using 10% CAGR fallback", e.getMessage());
            return calculateBenchmarkWithCAGR(startDate, endDate, totalInvested, timeframe);
        }
    }

    /**
     * Fallback benchmark using a 10% annual CAGR assumption.
     *
     * Generates a smooth growth curve approximating S&P 500 historical average.
     *
     * @param startDate     earliest investment date
     * @param endDate       simulation end date
     * @param totalInvested total amount invested
     * @param timeframe     aggregation timeframe
     * @return aggregated benchmark timeline
     */
    private List<SimulationResponse.TimelinePoint> calculateBenchmarkWithCAGR(
            LocalDate startDate, LocalDate endDate, BigDecimal totalInvested, Timeframe timeframe) {

        List<SimulationResponse.TimelinePoint> dailyBenchmark = new ArrayList<>();
        long daysBetween = ChronoUnit.DAYS.between(startDate, endDate);

        for (long i = 0; i <= daysBetween; i++) {
            LocalDate date = startDate.plusDays(i);
            double yearsElapsed = i / 365.25;
            double growthFactor = Math.pow(1.10, yearsElapsed);
            BigDecimal value = totalInvested.multiply(BigDecimal.valueOf(growthFactor))
                    .setScale(2, RoundingMode.HALF_UP);
            dailyBenchmark.add(new SimulationResponse.TimelinePoint(date, value));
        }

        return timelineAggregator.aggregateTimeline(dailyBenchmark, timeframe);
    }
}
