package com.moshimo.backend.domain.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Asset Entity - Represents a tradeable asset (stock, ETF, index, or crypto).
 * 
 * Learning Notes:
 * - @Entity: Marks this class as a JPA entity (maps to 'asset' table)
 * - @Table: Explicitly specifies table name and indexes
 * - @Data: Lombok generates getters, setters, toString, equals, hashCode
 * - @Builder: Lombok provides builder pattern for clean object construction
 * - @NoArgsConstructor/@AllArgsConstructor: Required by JPA (no-arg) and Builder pattern
 * - LocalDate: Java 8+ date type, maps to SQL DATE
 * - @CreationTimestamp/@UpdateTimestamp: Hibernate automatically sets timestamps
 */
@Entity
@Table(name = "asset", indexes = {
    @Index(name = "idx_asset_symbol", columnList = "symbol"),
    @Index(name = "idx_asset_sector", columnList = "sector"),
    @Index(name = "idx_asset_is_active", columnList = "is_active"),
    @Index(name = "idx_asset_asset_type", columnList = "asset_type")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Asset {

    /**
     * Primary key - auto-generated.
     * Maps to BIGSERIAL in PostgreSQL.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Ticker symbol (e.g., AAPL, SPY, BTC-USD).
     * Must be unique across all assets.
     */
    @Column(unique = true, nullable = false, length = 10)
    private String symbol;

    /**
     * Full name (e.g., "Apple Inc.", "SPDR S&P 500 ETF Trust").
     */
    @Column(nullable = false)
    private String name;

    /**
     * Type of asset: STOCK, ETF, INDEX, or CRYPTO.
     * Used for filtering and categorization in UI.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "asset_type", nullable = false, length = 20)
    @Builder.Default
    private AssetType assetType = AssetType.STOCK;

    /**
     * Business sector (e.g., "Technology", "Healthcare").
     * Used for filtering and categorization.
     */
    @Column(length = 100)
    private String sector;

    /**
     * Specific industry within sector (e.g., "Consumer Electronics").
     */
    @Column(length = 100)
    private String industry;

    /**
     * Exchange where traded (e.g., "NASDAQ", "NYSE").
     */
    @Column(length = 50)
    private String exchange;

    /**
     * Listing / inception date.
     * Prevents simulation attempts before this date.
     */
    @Column(name = "ipo_date")
    private LocalDate ipoDate;

    /**
     * Date when price data was last updated from external API.
     * Used to track data freshness and determine when updates are needed.
     */
    @Column(name = "last_price_update")
    private LocalDate lastPriceUpdate;

    /**
     * Whether this asset is currently active and tradeable.
     * Inactive assets don't appear in UI but historical data remains.
     */
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    /**
     * Timestamp when this record was created.
     * Automatically set by Hibernate on persist.
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Timestamp when this record was last updated.
     * Automatically updated by Hibernate on merge.
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}