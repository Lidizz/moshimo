package com.moshimo.backend.application.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * Simulation Request - User's investment scenario input.
 * 
 * Learning: Bean Validation annotations for input validation.
 * Using @Data (not record) because we need mutable fields for deserialization.
 * 
 * New Feature: Timeframe parameter controls timeline sampling granularity:
 * - 1D: Daily points (every trading day)
 * - 1W: Weekly points (first trading day of each week)
 * - 1M: Monthly points (first trading day of each month)
 * - 1Y: Yearly points (first trading day of January)
 * - ALL: Smart sampling (~500 points max for performance)
 */
@Data
public class SimulationRequest {
    
    @NotEmpty(message = "At least one investment is required")
    @Size(max = 10, message = "Maximum 10 investments per simulation")
    @Valid  // Validate nested objects
    @JsonProperty("investments")
    private List<InvestmentItemRequest> investments;
    
    @JsonProperty("endDate")
    private LocalDate endDate;  // Optional - defaults to today
    
    /**
     * Timeline sampling granularity: 1D, 1W, 1M, 1Y, or ALL (default).
     * Controls how densely the chart data is sampled across the full period.
     */
    @JsonProperty("timeframe")
    private String timeframe;  // Optional - defaults to "ALL"
}