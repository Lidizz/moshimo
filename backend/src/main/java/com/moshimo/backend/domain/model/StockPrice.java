package com.moshimo.backend.domain.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * StockPrice Entity - Historical daily price data for stocks.
 * 
 * Learning Notes:
 * - BigDecimal: Essential for financial calculations (avoids floating-point precision errors)
 * - @ManyToOne: Each price record belongs to one stock (foreign key relationship)
 * - @JoinColumn: Specifies the foreign key column name
 * - Composite unique constraint: (stock_id, date) prevents duplicate data
 * - FetchType.LAZY: Stock is loaded only when accessed (performance optimization)
 * 
 * Design Pattern: Value Object (immutable price data)
 */
@Entity
@Table(name = "stock_prices",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_stock_price_date", columnNames = {"stock_id", "date"})
    },
    indexes = {
        @Index(name = "idx_stock_prices_stock_date", columnList = "stock_id, date DESC"),
        @Index(name = "idx_stock_prices_date", columnList = "date")
    }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockPrice {

    /**
     * Primary key - auto-generated.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Reference to the parent Stock entity.
     * ManyToOne: Many prices belong to one stock.
     * FetchType.LAZY: Stock data loaded only when explicitly accessed.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "stock_id", nullable = false, foreignKey = @ForeignKey(name = "fk_stock_price_stock"))
    @ToString.Exclude  // Prevent circular toString calls
    private Stock stock;

    /**
     * Trading date for this price data.
     * Combined with stock_id, must be unique (one price per stock per day).
     */
    @Column(nullable = false)
    private LocalDate date;

    /**
     * Opening price for the trading day.
     * BigDecimal(12,4) allows values up to 99,999,999.9999
     */
    @Column(nullable = false, precision = 12, scale = 4)
    private BigDecimal open;

    /**
     * Highest price during the trading day.
     */
    @Column(nullable = false, precision = 12, scale = 4)
    private BigDecimal high;

    /**
     * Lowest price during the trading day.
     */
    @Column(nullable = false, precision = 12, scale = 4)
    private BigDecimal low;

    /**
     * Closing price at end of trading day.
     */
    @Column(nullable = false, precision = 12, scale = 4)
    private BigDecimal close;

    /**
     * Number of shares traded during the day.
     * Can be null if volume data unavailable.
     */
    @Column
    private Long volume;

    /**
     * Closing price adjusted for stock splits and dividends.
     * CRITICAL: Use this for accurate return calculations!
     * 
     * Example: If stock had 2:1 split, historical prices are halved
     * so returns are calculated correctly.
     */
    @Column(name = "adjusted_close", precision = 12, scale = 4)
    private BigDecimal adjustedClose;

    /**
     * Timestamp when this price record was created.
     * Useful for tracking when data was imported.
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}