package com.moshimo.backend.application.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Simulation Response - Investment calculation results.
 * 
 * Learning: Using @Builder for complex response construction.
 */
@Builder
public record SimulationResponse(
    @JsonProperty("totalInvested")
    BigDecimal totalInvested,
    
    @JsonProperty("currentValue")
    BigDecimal currentValue,
    
    @JsonProperty("absoluteGain")
    BigDecimal absoluteGain,
    
    @JsonProperty("percentReturn")
    BigDecimal percentReturn,
    
    @JsonProperty("cagr")
    BigDecimal cagr,
    
    @JsonProperty("timeline")
    List<TimelinePoint> timeline,
    
    @JsonProperty("holdings")
    List<HoldingInfo> holdings,
    
    @JsonProperty("benchmarkTimeline")
    List<TimelinePoint> benchmarkTimeline,
    
    @JsonProperty("holdingsTimelines")
    Map<String, List<TimelinePoint>> holdingsTimelines
) {
    /**
     * Timeline point - Portfolio value at a specific date.
     */
    public record TimelinePoint(
        @JsonProperty("date")
        LocalDate date,
        
        @JsonProperty("value")
        BigDecimal value
    ) {}
    
    /**
     * Holding information - Individual stock performance.
     */
    public record HoldingInfo(
        @JsonProperty("symbol")
        String symbol,
        
        @JsonProperty("name")
        String name,
        
        @JsonProperty("invested")
        BigDecimal invested,
        
        @JsonProperty("currentValue")
        BigDecimal currentValue,
        
        @JsonProperty("shares")
        BigDecimal shares,
        
        @JsonProperty("purchasePrice")
        BigDecimal purchasePrice,
        
        @JsonProperty("currentPrice")
        BigDecimal currentPrice,
        
        @JsonProperty("absoluteGain")
        BigDecimal absoluteGain,
        
        @JsonProperty("percentReturn")
        BigDecimal percentReturn
    ) {}
}