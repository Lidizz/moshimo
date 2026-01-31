package com.moshimo.backend.application.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Price Data Transfer Object - Historical price information.
 * 
 * Learning: BigDecimal for financial precision in JSON responses.
 */
public record PriceDataDTO(
    @JsonProperty("date")
    LocalDate date,
    
    @JsonProperty("open")
    BigDecimal open,
    
    @JsonProperty("high")
    BigDecimal high,
    
    @JsonProperty("low")
    BigDecimal low,
    
    @JsonProperty("close")
    BigDecimal close,
    
    @JsonProperty("volume")
    Long volume,
    
    @JsonProperty("adjustedClose")
    BigDecimal adjustedClose
) {}