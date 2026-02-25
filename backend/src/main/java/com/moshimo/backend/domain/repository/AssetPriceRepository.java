package com.moshimo.backend.domain.repository;

import com.moshimo.backend.domain.model.AssetPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository for AssetPrice entity - Optimized for time-series queries.
 * 
 * Learning Notes:
 * - Date range queries are the primary use case (investment simulations)
 * - Composite index (asset_id, date DESC) makes these queries O(log n)
 * - @Param: Named parameters in JPQL queries (more readable than ?1, ?2)
 * - JOIN FETCH: Eagerly loads Asset entity to avoid N+1 query problem
 * 
 * Performance Consideration:
 * This table will contain ~100,000 rows. Query optimization is critical.
 * Always use indexed columns in WHERE clauses.
 */
@Repository
public interface AssetPriceRepository extends JpaRepository<AssetPrice, Long> {

    /**
     * Find all price records for a specific asset within a date range.
     * Ordered by date ascending (earliest first) for chart display.
     * 
     * CRITICAL QUERY - Used for investment simulations.
     * Index used: idx_asset_price_asset_date
     * Time complexity: O(log n + m) where m = result set size
     * 
     * @param assetId the asset ID
     * @param startDate inclusive start date
     * @param endDate inclusive end date
     * @return list of prices in date order (empty if none found)
     */
    @Query("SELECT ap FROM AssetPrice ap " +
           "WHERE ap.asset.id = :assetId " +
           "AND ap.date BETWEEN :startDate AND :endDate " +
           "ORDER BY ap.date ASC")
    List<AssetPrice> findByAssetIdAndDateBetween(
        @Param("assetId") Long assetId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    /**
     * Find price for a specific asset on a specific date.
     * Used for exact date lookups (e.g., purchase price calculation).
     * 
     * @param assetId the asset ID
     * @param date the trading date
     * @return Optional containing the price if found
     */
    @Query("SELECT ap FROM AssetPrice ap " +
           "WHERE ap.asset.id = :assetId " +
           "AND ap.date = :date")
    Optional<AssetPrice> findByAssetIdAndDate(
        @Param("assetId") Long assetId,
        @Param("date") LocalDate date
    );

    /**
     * Find the most recent price for an asset (latest trading day).
     * 
     * @param assetId the asset ID
     * @return Optional containing the latest price
     */
    @Query("SELECT ap FROM AssetPrice ap " +
           "WHERE ap.asset.id = :assetId " +
           "ORDER BY ap.date DESC " +
           "LIMIT 1")
    Optional<AssetPrice> findLatestByAssetId(@Param("assetId") Long assetId);

    /**
     * Find the most recent price for an asset (by Asset entity).
     * Used during import to resume from last known date.
     * 
     * @param asset the Asset entity
     * @return Optional containing the latest price
     */
    @Query("SELECT ap FROM AssetPrice ap " +
           "WHERE ap.asset = :asset " +
           "ORDER BY ap.date DESC " +
           "LIMIT 1")
    Optional<AssetPrice> findTopByAssetOrderByDateDesc(@Param("asset") com.moshimo.backend.domain.model.Asset asset);

    /**
     * Count all price records for a specific asset.
     * Used to show how many records will be deleted.
     * 
     * @param asset the Asset entity
     * @return count of price records
     */
    long countByAsset(com.moshimo.backend.domain.model.Asset asset);

    /**
     * Delete all price records for a specific asset.
     * Used when clearExisting=true for specific symbols.
     * 
     * @param asset the Asset entity
     */
    @Modifying
    @Query("DELETE FROM AssetPrice ap WHERE ap.asset = :asset")
    void deleteByAsset(@Param("asset") com.moshimo.backend.domain.model.Asset asset);

    /**
     * Find all prices for a specific date across all assets.
     * Used for market-wide analysis.
     * 
     * @param date the trading date
     * @return list of prices for all assets on that date
     */
    @Query("SELECT ap FROM AssetPrice ap JOIN FETCH ap.asset " +
           "WHERE ap.date = :date")
    List<AssetPrice> findAllByDate(@Param("date") LocalDate date);

    /**
     * Check if price data exists for an asset on a specific date.
     * Used to avoid duplicate insertions.
     * 
     * @param assetId the asset ID
     * @param date the trading date
     * @return true if price exists, false otherwise
     */
    @Query("SELECT CASE WHEN COUNT(ap) > 0 THEN true ELSE false END " +
           "FROM AssetPrice ap " +
           "WHERE ap.asset.id = :assetId AND ap.date = :date")
    boolean existsByAssetIdAndDate(
        @Param("assetId") Long assetId,
        @Param("date") LocalDate date
    );

    /**
     * Count total price records for an asset (data coverage check).
     * 
     * @param assetId the asset ID
     * @return number of price records
     */
    @Query("SELECT COUNT(ap) FROM AssetPrice ap WHERE ap.asset.id = :assetId")
    long countByAssetId(@Param("assetId") Long assetId);

    /**
     * Find next available trading day on or after the specified date.
     * Used to handle weekends, holidays, and missing data.
     * 
     * @param assetId the asset ID
     * @param date the target date (or after)
     * @return Optional containing the next available price
     */
    @Query("SELECT ap FROM AssetPrice ap " +
           "WHERE ap.asset.id = :assetId " +
           "AND ap.date >= :date " +
           "ORDER BY ap.date ASC " +
           "LIMIT 1")
    Optional<AssetPrice> findNextAvailableDate(
        @Param("assetId") Long assetId,
        @Param("date") LocalDate date
    );

    /**
     * Batch-fetch prices for multiple assets within a date range.
     * CRITICAL OPTIMIZATION: Replaces N individual queries with single batch query.
     * 
     * Performance Impact:
     * - Old approach: 5 assets × 1000 days = 5,000 queries
     * - New approach: 1 query returning all 5,000 rows
     * - Speedup: ~100-1000x faster (network latency elimination)
     * 
     * Time Complexity: O(log n + m) where m = total matching rows
     * Index Used: idx_asset_price_asset_date (composite index)
     * 
     * Use Case: Multi-asset portfolio timeline generation
     * 
     * Learning Note:
     * This demonstrates the N+1 query problem solution. In financial applications,
     * batch queries are essential when working with time-series data across
     * multiple securities. Always batch when possible!
     * 
     * @param assetIds list of asset IDs to fetch prices for
     * @param startDate inclusive start date
     * @param endDate inclusive end date
     * @return all prices for specified assets in date range, ordered by date ASC, then asset ID ASC
     */
    @Query("SELECT ap FROM AssetPrice ap " +
           "WHERE ap.asset.id IN :assetIds " +
           "AND ap.date BETWEEN :startDate AND :endDate " +
           "ORDER BY ap.date ASC, ap.asset.id ASC")
    List<AssetPrice> findByAssetIdsAndDateBetween(
        @Param("assetIds") List<Long> assetIds,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );
}