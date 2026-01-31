package com.moshimo.backend.application.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Investment Item Request - Single stock investment details.
 */
@Data
public class InvestmentItemRequest {
    
    @NotBlank(message = "Symbol is required")
    @Pattern(regexp = "^[A-Z.]{1,10}$", message = "Invalid symbol format")
    @JsonProperty("symbol")
    private String symbol;
    
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be at least $0.01")
    @DecimalMax(value = "1000000.00", message = "Amount cannot exceed $1,000,000")
    @JsonProperty("amountUsd")
    private BigDecimal amountUsd;
    
    @NotNull(message = "Purchase date is required")
    @PastOrPresent(message = "Purchase date cannot be in the future")
    @JsonProperty("purchaseDate")
    private LocalDate purchaseDate;
}