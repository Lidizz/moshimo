package com.moshimo.backend.domain.repository;

import com.moshimo.backend.domain.model.StockPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository for StockPrice entity - Optimized for time-series queries.
 * 
 * Learning Notes:
 * - Date range queries are the primary use case (investment simulations)
 * - Composite index (stock_id, date DESC) makes these queries O(log n)
 * - @Param: Named parameters in JPQL queries (more readable than ?1, ?2)
 * - JOIN FETCH: Eagerly loads Stock entity to avoid N+1 query problem
 * 
 * Performance Consideration:
 * This table will contain ~100,000 rows. Query optimization is critical.
 * Always use indexed columns in WHERE clauses.
 */
@Repository
public interface StockPriceRepository extends JpaRepository<StockPrice, Long> {

    /**
     * Find all price records for a specific stock within a date range.
     * Ordered by date ascending (earliest first) for chart display.
     * 
     * CRITICAL QUERY - Used for investment simulations.
     * Index used: idx_stock_prices_stock_date
     * Time complexity: O(log n + m) where m = result set size
     * 
     * @param stockId the stock ID
     * @param startDate inclusive start date
     * @param endDate inclusive end date
     * @return list of prices in date order (empty if none found)
     */
    @Query("SELECT sp FROM StockPrice sp " +
           "WHERE sp.stock.id = :stockId " +
           "AND sp.date BETWEEN :startDate AND :endDate " +
           "ORDER BY sp.date ASC")
    List<StockPrice> findByStockIdAndDateBetween(
        @Param("stockId") Long stockId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    /**
     * Find price for a specific stock on a specific date.
     * Used for exact date lookups (e.g., purchase price calculation).
     * 
     * @param stockId the stock ID
     * @param date the trading date
     * @return Optional containing the price if found
     */
    @Query("SELECT sp FROM StockPrice sp " +
           "WHERE sp.stock.id = :stockId " +
           "AND sp.date = :date")
    Optional<StockPrice> findByStockIdAndDate(
        @Param("stockId") Long stockId,
        @Param("date") LocalDate date
    );

    /**
     * Find the most recent price for a stock (latest trading day).
     * 
     * @param stockId the stock ID
     * @return Optional containing the latest price
     */
    @Query("SELECT sp FROM StockPrice sp " +
           "WHERE sp.stock.id = :stockId " +
           "ORDER BY sp.date DESC " +
           "LIMIT 1")
    Optional<StockPrice> findLatestByStockId(@Param("stockId") Long stockId);

    /**
     * Find the most recent price for a stock (by Stock entity).
     * Used during import to resume from last known date.
     * 
     * @param stock the Stock entity
     * @return Optional containing the latest price
     */
    @Query("SELECT sp FROM StockPrice sp " +
           "WHERE sp.stock = :stock " +
           "ORDER BY sp.date DESC " +
           "LIMIT 1")
    Optional<StockPrice> findTopByStockOrderByDateDesc(@Param("stock") com.moshimo.backend.domain.model.Stock stock);

    /**
     * Count all price records for a specific stock.
     * Used to show how many records will be deleted.
     * 
     * @param stock the Stock entity
     * @return count of price records
     */
    long countByStock(com.moshimo.backend.domain.model.Stock stock);

    /**
     * Delete all price records for a specific stock.
     * Used when clearExisting=true for specific symbols.
     * 
     * @param stock the Stock entity
     */
    @Modifying
    @Query("DELETE FROM StockPrice sp WHERE sp.stock = :stock")
    void deleteByStock(@Param("stock") com.moshimo.backend.domain.model.Stock stock);

    /**
     * Find all prices for a specific date across all stocks.
     * Used for market-wide analysis.
     * 
     * @param date the trading date
     * @return list of prices for all stocks on that date
     */
    @Query("SELECT sp FROM StockPrice sp JOIN FETCH sp.stock " +
           "WHERE sp.date = :date")
    List<StockPrice> findAllByDate(@Param("date") LocalDate date);

    /**
     * Check if price data exists for a stock on a specific date.
     * Used to avoid duplicate insertions.
     * 
     * @param stockId the stock ID
     * @param date the trading date
     * @return true if price exists, false otherwise
     */
    @Query("SELECT CASE WHEN COUNT(sp) > 0 THEN true ELSE false END " +
           "FROM StockPrice sp " +
           "WHERE sp.stock.id = :stockId AND sp.date = :date")
    boolean existsByStockIdAndDate(
        @Param("stockId") Long stockId,
        @Param("date") LocalDate date
    );

    /**
     * Count total price records for a stock (data coverage check).
     * 
     * @param stockId the stock ID
     * @return number of price records
     */
    @Query("SELECT COUNT(sp) FROM StockPrice sp WHERE sp.stock.id = :stockId")
    long countByStockId(@Param("stockId") Long stockId);

    /**
     * Find next available trading day on or after the specified date.
     * Used to handle weekends, holidays, and missing data.
     * 
     * @param stockId the stock ID
     * @param date the target date (or after)
     * @return Optional containing the next available price
     */
    @Query("SELECT sp FROM StockPrice sp " +
           "WHERE sp.stock.id = :stockId " +
           "AND sp.date >= :date " +
           "ORDER BY sp.date ASC " +
           "LIMIT 1")
    Optional<StockPrice> findNextAvailableDate(
        @Param("stockId") Long stockId,
        @Param("date") LocalDate date
    );

    /**
     * Batch-fetch prices for multiple stocks within a date range.
     * CRITICAL OPTIMIZATION: Replaces N individual queries with single batch query.
     * 
     * Performance Impact:
     * - Old approach: 5 stocks × 1000 days = 5,000 queries
     * - New approach: 1 query returning all 5,000 rows
     * - Speedup: ~100-1000x faster (network latency elimination)
     * 
     * Time Complexity: O(log n + m) where m = total matching rows
     * Index Used: idx_stock_prices_stock_date (composite index)
     * 
     * Use Case: Multi-stock portfolio timeline generation
     * 
     * Learning Note:
     * This demonstrates the N+1 query problem solution. In financial applications,
     * batch queries are essential when working with time-series data across
     * multiple securities. Always batch when possible!
     * 
     * @param stockIds list of stock IDs to fetch prices for
     * @param startDate inclusive start date
     * @param endDate inclusive end date
     * @return all prices for specified stocks in date range, ordered by date ASC, then stock ID ASC
     */
    @Query("SELECT sp FROM StockPrice sp " +
           "WHERE sp.stock.id IN :stockIds " +
           "AND sp.date BETWEEN :startDate AND :endDate " +
           "ORDER BY sp.date ASC, sp.stock.id ASC")
    List<StockPrice> findByStockIdsAndDateBetween(
        @Param("stockIds") List<Long> stockIds,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );
}