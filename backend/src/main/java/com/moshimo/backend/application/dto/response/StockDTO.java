package com.moshimo.backend.application.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.moshimo.backend.domain.model.AssetType;

import java.time.LocalDate;

/**
 * Stock Data Transfer Object - Represents stock information for API responses.
 * 
 * Learning: Using record for immutable response DTOs (Java 25 feature).
 */
public record StockDTO(
    @JsonProperty("id")
    Long id,
    
    @JsonProperty("symbol")
    String symbol,
    
    @JsonProperty("name")
    String name,
    
    @JsonProperty("assetType")
    AssetType assetType,
    
    @JsonProperty("sector")
    String sector,
    
    @JsonProperty("industry")
    String industry,
    
    @JsonProperty("exchange")
    String exchange,
    
    @JsonProperty("ipoDate")
    LocalDate ipoDate,
    
    @JsonProperty("isActive")
    Boolean isActive
) {}