package com.moshimo.backend.domain.service;

import com.moshimo.backend.application.dto.request.InvestmentItemRequest;
import com.moshimo.backend.application.dto.request.SimulationRequest;
import com.moshimo.backend.application.dto.request.Timeframe;
import com.moshimo.backend.application.dto.response.SimulationResponse;
import com.moshimo.backend.domain.model.Asset;
import com.moshimo.backend.domain.model.AssetPrice;
import com.moshimo.backend.domain.repository.AssetPriceRepository;
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
 * Core business logic for "What If" investment simulations.
 *
 * Collaborators:
 * - TimelineAggregator: daily → weekly/monthly/yearly/smart downsampling
 * - BenchmarkService:   SPY benchmark calculation (with @Cacheable)
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class InvestmentSimulationService {

    private final AssetService assetService;
    private final AssetPriceRepository assetPriceRepository;
    private final TimelineAggregator timelineAggregator;
    private final BenchmarkService benchmarkService;

    /**
     * Run a full simulation: process each investment, build portfolio timeline,
     * calculate metrics (CAGR, returns, gains), and attach benchmark data.
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

    /** Process a single investment: resolve purchase/current prices and compute shares & value. */
    private InvestmentHolding processInvestment(InvestmentItemRequest item, LocalDate endDate) {
        Asset asset = assetService.getAssetEntityBySymbol(item.getSymbol());
        
        // Get purchase price (falls back to next available trading day for holidays)
        AssetPrice purchasePrice = assetPriceRepository
            .findByAssetIdAndDate(asset.getId(), item.getPurchaseDate())
            .or(() -> assetPriceRepository.findNextAvailableDate(asset.getId(), item.getPurchaseDate()))
            .orElseThrow(() -> new IllegalArgumentException(
                "No price data available for " + item.getSymbol() + " on or after " + item.getPurchaseDate()
            ));

        BigDecimal priceOnPurchase = purchasePrice.getAdjustedClose() != null 
            ? purchasePrice.getAdjustedClose() 
            : purchasePrice.getClose();
        BigDecimal shares = item.getAmountUsd().divide(priceOnPurchase, 8, RoundingMode.HALF_UP);

        AssetPrice currentPrice = assetPriceRepository
            .findByAssetIdAndDate(asset.getId(), endDate)
            .orElseGet(() -> assetPriceRepository.findLatestByAssetId(asset.getId())
                    .orElseThrow(() -> new IllegalArgumentException(
                        "No price data available for " + item.getSymbol()
                    )));

        BigDecimal priceOnEnd = currentPrice.getAdjustedClose() != null 
            ? currentPrice.getAdjustedClose() 
            : currentPrice.getClose();
        BigDecimal currentValue = shares.multiply(priceOnEnd).setScale(2, RoundingMode.HALF_UP);

        return new InvestmentHolding(
            asset,
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
     * Batch-fetches all prices in one query, then aggregates in memory.
     * O(D × H) where D = trading days, H = holdings. Returns unsampled data
     * for the caller (TimelineAggregator) to downsample.
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
        
        // Batch-fetch all prices for all assets in one query
        List<Long> assetIds = holdings.stream()
            .map(h -> h.asset.getId())
            .distinct()
            .toList();
        
        List<AssetPrice> allPrices = assetPriceRepository
            .findByAssetIdsAndDateBetween(assetIds, startDate, endDate);
        
        // Group prices by date for O(1) lookup: Map<Date, Map<AssetId, Price>>
        Map<LocalDate, Map<Long, AssetPrice>> pricesByDate = allPrices.stream()
            .collect(Collectors.groupingBy(
                AssetPrice::getDate,
                Collectors.toMap(
                    ap -> ap.getAsset().getId(),
                    Function.identity()
                )
            ));
        
        // Build timeline day-by-day
        List<SimulationResponse.TimelinePoint> timeline = new ArrayList<>();
        LocalDate currentDate = startDate;
        
        while (!currentDate.isAfter(endDate)) {
            Map<Long, AssetPrice> pricesOnDate = pricesByDate.get(currentDate);
            
            if (pricesOnDate != null && !pricesOnDate.isEmpty()) {
                BigDecimal portfolioValue = BigDecimal.ZERO;
                
                for (InvestmentHolding holding : holdings) {
                    if (!holding.purchaseDate.isAfter(currentDate)) {
                        AssetPrice price = pricesOnDate.get(holding.asset.getId());
                        
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
        
        return timeline;
    }

    /**
     * Build individual timelines for each holding (symbol → aggregated timeline).
     * Enables per-asset performance comparison on the frontend.
     */
    private Map<String, List<SimulationResponse.TimelinePoint>> buildHoldingsTimelines(
            List<InvestmentHolding> holdings, LocalDate endDate, Timeframe timeframe) {
        
        Map<String, List<SimulationResponse.TimelinePoint>> result = new LinkedHashMap<>();
        
        for (InvestmentHolding holding : holdings) {
            List<AssetPrice> prices = assetPriceRepository
                .findByAssetIdAndDateBetween(holding.asset.getId(), holding.purchaseDate, endDate)
                .stream()
                .sorted((a, b) -> a.getDate().compareTo(b.getDate()))
                .collect(Collectors.toList());
            
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
            
            List<SimulationResponse.TimelinePoint> aggregated =
                    timelineAggregator.aggregateTimeline(dailyTimeline, timeframe);

            result.put(holding.asset.getSymbol(), aggregated);
        }
        
        return result;
    }

    /** Calculate percentage return: ((currentValue - invested) / invested) × 100. */
    private BigDecimal calculatePercentReturn(BigDecimal invested, BigDecimal currentValue) {
        if (invested.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return currentValue.subtract(invested)
            .divide(invested, 4, RoundingMode.HALF_UP)
            .multiply(BigDecimal.valueOf(100))
            .setScale(2, RoundingMode.HALF_UP);
    }

    /** Calculate CAGR: (endValue / beginValue)^(1/years) - 1. */
    private BigDecimal calculateCAGR(BigDecimal invested, BigDecimal currentValue, 
                                     LocalDate startDate, LocalDate endDate) {
        if (invested.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        long days = ChronoUnit.DAYS.between(startDate, endDate);
        double years = days / 365.25;

        if (years < 0.01) {
            return BigDecimal.ZERO;
        }

        double ratio = currentValue.doubleValue() / invested.doubleValue();
        double cagr = (Math.pow(ratio, 1.0 / years) - 1.0) * 100.0;

        return BigDecimal.valueOf(cagr).setScale(2, RoundingMode.HALF_UP);
    }

    /** Get earliest investment date. */
    private LocalDate getEarliestDate(List<InvestmentItemRequest> investments) {
        return investments.stream()
            .map(InvestmentItemRequest::getPurchaseDate)
            .min(LocalDate::compareTo)
            .orElse(LocalDate.now());
    }

    /** Convert internal holding to response DTO. */
    private SimulationResponse.HoldingInfo toHoldingInfo(InvestmentHolding holding) {
        BigDecimal absoluteGain = holding.currentValue.subtract(holding.invested);
        BigDecimal percentReturn = calculatePercentReturn(holding.invested, holding.currentValue);

        return new SimulationResponse.HoldingInfo(
                holding.asset.getSymbol(),
                holding.asset.getName(),
                holding.invested,
                holding.currentValue,
                holding.shares,
                holding.purchasePrice,
                holding.currentPrice,
                absoluteGain,
                percentReturn
        );
    }

    private record InvestmentHolding(
            Asset asset,
            LocalDate purchaseDate,
            LocalDate endDate,
            BigDecimal invested,
            BigDecimal shares,
            BigDecimal purchasePrice,
            BigDecimal currentPrice,
            BigDecimal currentValue
    ) {}
}