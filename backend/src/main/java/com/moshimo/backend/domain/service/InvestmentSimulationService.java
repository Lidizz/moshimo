package com.moshimo.backend.domain.service;

import com.moshimo.backend.application.dto.request.InvestmentItemRequest;
import com.moshimo.backend.application.dto.request.SimulationRequest;
import com.moshimo.backend.application.dto.request.Timeframe;
import com.moshimo.backend.application.dto.response.SimulationResponse;
import com.moshimo.backend.domain.model.Stock;
import com.moshimo.backend.domain.model.StockPrice;
import com.moshimo.backend.domain.repository.StockPriceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Investment Simulation Service - Core business logic for "What If" calculations.
 *
 * Learning Notes:
 * - BigDecimal arithmetic for financial precision
 * - Stream API for data transformation and aggregation
 * - Algorithm complexity: O(n × m) where n = investments, m = days
 * - CAGR formula: (endValue / beginValue)^(1/years) - 1
 *
 * Design Pattern: Service Layer, Strategy Pattern (calculation algorithms)
 *
 * Collaborators:
 * - TimelineAggregator: handles daily → weekly/monthly/yearly/smart downsampling
 * - BenchmarkService:   handles SPY benchmark calculation (with @Cacheable)
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class InvestmentSimulationService {

    private final StockService stockService;
    private final StockPriceRepository stockPriceRepository;
    private final TimelineAggregator timelineAggregator;
    private final BenchmarkService benchmarkService;

    /**
     * Calculate investment simulation results.
     * 
     * Algorithm:
     * 1. For each investment, calculate shares purchased (amount / price on purchase date)
     * 2. Build timeline: for each date, calculate total portfolio value
     * 3. Calculate metrics: CAGR, returns, gains
     * 
     * @param request simulation parameters
     * @return simulation results with timeline and holdings
     */
    public SimulationResponse simulate(SimulationRequest request) {
        LocalDate endDate = request.getEndDate() != null ? request.getEndDate() : LocalDate.now();
        
        // Parse timeframe (default to ALL if not specified)
        Timeframe timeframe = Timeframe.fromCode(request.getTimeframe());
        
        log.info("Starting simulation: {} investments, end date: {}, timeframe: {}", 
                 request.getInvestments().size(), endDate, timeframe.getCode());

        // Process each investment
        List<InvestmentHolding> holdings = new ArrayList<>();
        BigDecimal totalInvested = BigDecimal.ZERO;
        
        for (InvestmentItemRequest item : request.getInvestments()) {
            InvestmentHolding holding = processInvestment(item, endDate);
            holdings.add(holding);
            totalInvested = totalInvested.add(item.getAmountUsd());
        }

        // Build full daily timeline
        List<SimulationResponse.TimelinePoint> dailyTimeline = buildTimeline(holdings, endDate);
        
        // Apply timeframe aggregation
        List<SimulationResponse.TimelinePoint> timeline =
                timelineAggregator.aggregateTimeline(dailyTimeline, timeframe);
        
        log.info("Timeline: {} daily points → {} aggregated points ({})", 
                 dailyTimeline.size(), timeline.size(), timeframe.getCode());

        // Calculate current total value
        BigDecimal currentValue = holdings.stream()
            .map(h -> h.currentValue)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Calculate metrics
        BigDecimal absoluteGain = currentValue.subtract(totalInvested);
        BigDecimal percentReturn = calculatePercentReturn(totalInvested, currentValue);
        LocalDate earliestDate = getEarliestDate(request.getInvestments());
        BigDecimal cagr = calculateCAGR(totalInvested, currentValue, earliestDate, endDate);

        // Build holdings response
        List<SimulationResponse.HoldingInfo> holdingInfos = holdings.stream()
            .map(this::toHoldingInfo)
            .collect(Collectors.toList());

        // Calculate S&P 500 benchmark for comparison
        List<SimulationResponse.TimelinePoint> benchmarkTimeline =
                benchmarkService.calculateBenchmarkTimeline(earliestDate, endDate, totalInvested, timeframe);

        // Build individual holding timelines for per-stock visualization
        Map<String, List<SimulationResponse.TimelinePoint>> holdingsTimelines = 
            buildHoldingsTimelines(holdings, endDate, timeframe);

        log.info("Simulation complete - Invested: {}, Current: {}, Return: {}%", 
                 totalInvested, currentValue, percentReturn);

        return SimulationResponse.builder()
            .totalInvested(totalInvested)
            .currentValue(currentValue)
            .absoluteGain(absoluteGain)
            .percentReturn(percentReturn)
            .cagr(cagr)
            .timeline(timeline)
            .holdings(holdingInfos)
            .benchmarkTimeline(benchmarkTimeline)
            .holdingsTimelines(holdingsTimelines)
            .build();
    }

    /**
     * Process a single investment and calculate current value.
     */
    private InvestmentHolding processInvestment(InvestmentItemRequest item, LocalDate endDate) {
        Stock stock = stockService.getStockEntityBySymbol(item.getSymbol());
        
        // Get purchase price (handle holidays by finding next available trading day)
        StockPrice purchasePrice = stockPriceRepository
            .findByStockIdAndDate(stock.getId(), item.getPurchaseDate())
            .or(() -> {
                log.info("Exact date {} not available for {}, finding next trading day", 
                         item.getPurchaseDate(), item.getSymbol());
                return stockPriceRepository.findNextAvailableDate(stock.getId(), item.getPurchaseDate());
            })
            .orElseThrow(() -> new IllegalArgumentException(
                "No price data available for " + item.getSymbol() + " on or after " + item.getPurchaseDate()
            ));

        // Calculate shares purchased (use adjusted close for accuracy)
        BigDecimal priceOnPurchase = purchasePrice.getAdjustedClose() != null 
            ? purchasePrice.getAdjustedClose() 
            : purchasePrice.getClose();
        BigDecimal shares = item.getAmountUsd().divide(priceOnPurchase, 8, RoundingMode.HALF_UP);

        // Get current price (on end date)
        StockPrice currentPrice = stockPriceRepository
            .findByStockIdAndDate(stock.getId(), endDate)
            .orElseGet(() -> {
                log.warn("No price data for {} on {}, using latest available", 
                         item.getSymbol(), endDate);
                return stockPriceRepository.findLatestByStockId(stock.getId())
                    .orElseThrow(() -> new IllegalArgumentException(
                        "No price data available for " + item.getSymbol()
                    ));
            });

        BigDecimal priceOnEnd = currentPrice.getAdjustedClose() != null 
            ? currentPrice.getAdjustedClose() 
            : currentPrice.getClose();

        // Calculate current value
        BigDecimal currentValue = shares.multiply(priceOnEnd).setScale(2, RoundingMode.HALF_UP);

        return new InvestmentHolding(
            stock,
            item.getPurchaseDate(),
            endDate,
            item.getAmountUsd(),
            shares,
            priceOnPurchase,
            priceOnEnd,
            currentValue
        );
    }

    /**
     * Build complete portfolio value timeline with daily granularity.
     * 
     * NEW IMPLEMENTATION (replaces 2-point stub):
     * This method now generates a COMPLETE historical performance timeline,
     * showing actual portfolio value on every trading day from first purchase
     * to end date. This reveals real market volatility, crashes, and recoveries.
     * 
     * Algorithm:
     * 1. Determine date range (earliest purchase → end date)
     * 2. Batch-fetch ALL stock prices for entire period (1 efficient query!)
     * 3. For each trading day:
     *    - Calculate each holding's value on that day (shares × price)
     *    - Sum to get total portfolio value
     * 4. Return chronological list of daily values
     * 
     * Performance Optimization:
     * - OLD: N holdings × D days = N×D individual queries (SLOW!)
     * - NEW: 1 batch query fetching all data, then in-memory aggregation (FAST!)
     * 
     * Time Complexity: O(D × H) where D = trading days, H = holdings count
     * Space Complexity: O(D) for timeline list
     * Database Queries: 1 (batch query for all stocks)
     * 
     * @param holdings list of processed investments with purchase info
     * @param endDate simulation end date
     * @return complete daily timeline (unsampled, to be aggregated by caller)
     */
    private List<SimulationResponse.TimelinePoint> buildTimeline(
            List<InvestmentHolding> holdings, LocalDate endDate) {
        
        if (holdings.isEmpty()) {
            return List.of();
        }
        
        // Find overall date range
        LocalDate startDate = holdings.stream()
            .map(h -> h.purchaseDate)
            .min(LocalDate::compareTo)
            .orElse(endDate);
        
        long totalDays = ChronoUnit.DAYS.between(startDate, endDate);
        log.info("Building timeline from {} to {} ({} days)",
                 startDate, endDate, totalDays);
        
        // Batch-fetch all prices for all stocks in one query
        List<Long> stockIds = holdings.stream()
            .map(h -> h.stock.getId())
            .distinct()
            .toList();
        
        List<StockPrice> allPrices = stockPriceRepository
            .findByStockIdsAndDateBetween(stockIds, startDate, endDate);
        
        log.info("Fetched {} price records for {} stocks", allPrices.size(), stockIds.size());
        
        // Group prices by date for O(1) lookup: Map<Date, Map<StockId, Price>>
        Map<LocalDate, Map<Long, StockPrice>> pricesByDate = allPrices.stream()
            .collect(Collectors.groupingBy(
                StockPrice::getDate,
                Collectors.toMap(
                    sp -> sp.getStock().getId(),
                    Function.identity()
                )
            ));
        
        // Build timeline day-by-day
        List<SimulationResponse.TimelinePoint> timeline = new ArrayList<>();
        LocalDate currentDate = startDate;
        
        while (!currentDate.isAfter(endDate)) {
            Map<Long, StockPrice> pricesOnDate = pricesByDate.get(currentDate);
            
            if (pricesOnDate != null && !pricesOnDate.isEmpty()) {
                // Trading day - calculate portfolio value
                BigDecimal portfolioValue = BigDecimal.ZERO;
                
                for (InvestmentHolding holding : holdings) {
                    // Only count holdings purchased on or before this date
                    if (!holding.purchaseDate.isAfter(currentDate)) {
                        StockPrice price = pricesOnDate.get(holding.stock.getId());
                        
                        if (price != null) {
                            BigDecimal priceValue = price.getAdjustedClose() != null 
                                ? price.getAdjustedClose() 
                                : price.getClose();
                            
                            BigDecimal holdingValue = holding.shares.multiply(priceValue);
                            portfolioValue = portfolioValue.add(holdingValue);
                        }
                    }
                }
                
                timeline.add(new SimulationResponse.TimelinePoint(currentDate, portfolioValue));
            }
            
            currentDate = currentDate.plusDays(1);
        }
        
        log.info("Generated {} daily timeline points", timeline.size());
        return timeline;
    }

    /**
     * Build individual timelines for each holding.
     * 
     * This enables the frontend to display per-stock performance charts,
     * allowing users to compare how each investment performed over time.
     * 
     * Algorithm:
     * 1. For each holding, get all price data from purchase date to end date
     * 2. Calculate daily value (shares × price) for each trading day
     * 3. Apply same timeframe aggregation as main timeline
     * 
     * @param holdings list of processed investments
     * @param endDate simulation end date
     * @param timeframe aggregation timeframe
     * @return Map of symbol → aggregated timeline
     */
    private Map<String, List<SimulationResponse.TimelinePoint>> buildHoldingsTimelines(
            List<InvestmentHolding> holdings, LocalDate endDate, Timeframe timeframe) {
        
        Map<String, List<SimulationResponse.TimelinePoint>> result = new LinkedHashMap<>();
        
        for (InvestmentHolding holding : holdings) {
            // Get all prices for this stock from purchase date to end date
            List<StockPrice> prices = stockPriceRepository
                .findByStockIdAndDateBetween(holding.stock.getId(), holding.purchaseDate, endDate)
                .stream()
                .sorted((a, b) -> a.getDate().compareTo(b.getDate()))
                .collect(Collectors.toList());
            
            // Build daily timeline for this holding
            List<SimulationResponse.TimelinePoint> dailyTimeline = prices.stream()
                .map(price -> {
                    BigDecimal priceValue = price.getAdjustedClose() != null 
                        ? price.getAdjustedClose() 
                        : price.getClose();
                    BigDecimal value = holding.shares.multiply(priceValue)
                        .setScale(2, RoundingMode.HALF_UP);
                    return new SimulationResponse.TimelinePoint(price.getDate(), value);
                })
                .collect(Collectors.toList());
            
            // Apply same timeframe aggregation
            List<SimulationResponse.TimelinePoint> aggregated =
                    timelineAggregator.aggregateTimeline(dailyTimeline, timeframe);

            result.put(holding.stock.getSymbol(), aggregated);
            
            log.debug("Built timeline for {}: {} daily points → {} aggregated points", 
                     holding.stock.getSymbol(), dailyTimeline.size(), aggregated.size());
        }
        
        log.info("Built individual timelines for {} holdings", result.size());
        return result;
    }

    /**
     * Calculate percentage return.
     * Formula: ((currentValue - invested) / invested) × 100
     */
    private BigDecimal calculatePercentReturn(BigDecimal invested, BigDecimal currentValue) {
        if (invested.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return currentValue.subtract(invested)
            .divide(invested, 4, RoundingMode.HALF_UP)
            .multiply(BigDecimal.valueOf(100))
            .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Calculate Compound Annual Growth Rate (CAGR).
     * 
     * Formula: CAGR = (endValue / beginValue)^(1/years) - 1
     * 
     * Learning: Financial algorithm for annualized return rate.
     * Example: $1000 → $2000 over 5 years = 14.87% CAGR
     */
    private BigDecimal calculateCAGR(BigDecimal invested, BigDecimal currentValue, 
                                     LocalDate startDate, LocalDate endDate) {
        if (invested.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        // Calculate years (as decimal)
        long days = ChronoUnit.DAYS.between(startDate, endDate);
        double years = days / 365.25;  // Account for leap years

        if (years < 0.01) {  // Less than ~4 days
            return BigDecimal.ZERO;
        }

        // CAGR = (endValue / beginValue)^(1/years) - 1
        double ratio = currentValue.doubleValue() / invested.doubleValue();
        double cagr = (Math.pow(ratio, 1.0 / years) - 1.0) * 100.0;

        return BigDecimal.valueOf(cagr).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Get earliest investment date.
     */
    private LocalDate getEarliestDate(List<InvestmentItemRequest> investments) {
        return investments.stream()
            .map(InvestmentItemRequest::getPurchaseDate)
            .min(LocalDate::compareTo)
            .orElse(LocalDate.now());
    }

    /**
     * Convert internal holding to response DTO.
     */
    private SimulationResponse.HoldingInfo toHoldingInfo(InvestmentHolding holding) {
        BigDecimal absoluteGain = holding.currentValue.subtract(holding.invested);
        BigDecimal percentReturn = calculatePercentReturn(holding.invested, holding.currentValue);

        return new SimulationResponse.HoldingInfo(
                holding.stock.getSymbol(),
                holding.stock.getName(),
                holding.invested,
                holding.currentValue,
                holding.shares,
                holding.purchasePrice,
                holding.currentPrice,
                absoluteGain,
                percentReturn
        );
    }

    /**
     * Internal holding data structure.
     */
    private record InvestmentHolding(
            Stock stock,
            LocalDate purchaseDate,
            LocalDate endDate,
            BigDecimal invested,
            BigDecimal shares,
            BigDecimal purchasePrice,
            BigDecimal currentPrice,
            BigDecimal currentValue
    ) {}
}