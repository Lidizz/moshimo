package com.moshimo.backend.domain.repository;

import com.moshimo.backend.domain.model.AssetType;
import com.moshimo.backend.domain.model.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Stock entity - Data Access Layer.
 * 
 * Learning Notes:
 * - Extends JpaRepository<Entity, ID>: Provides CRUD operations out-of-the-box
 * - Method naming conventions: Spring Data JPA auto-generates queries
 *   Example: findBySymbol → SELECT * FROM stocks WHERE symbol = ?
 * - Optional<T>: Java 8+ feature to handle potential null results safely
 * - @Query: Custom JPQL queries for complex operations
 * 
 * Design Pattern: Repository Pattern (abstracts data access logic)
 */
@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {

    /**
     * Find a stock by its ticker symbol.
     * 
     * Spring Data JPA auto-generates:
     * SELECT * FROM stocks WHERE symbol = :symbol
     * 
     * @param symbol the stock ticker (e.g., "AAPL")
     * @return Optional containing the stock if found, empty otherwise
     * 
     * Learning: Optional prevents NullPointerException, forces explicit handling
     */
    Optional<Stock> findBySymbol(String symbol);

    /**
     * Find all active stocks (is_active = true).
     * 
     * Auto-generated query:
     * SELECT * FROM stocks WHERE is_active = true
     * 
     * @return list of active stocks (empty list if none found)
     */
    List<Stock> findByIsActiveTrue();

    /**
     * Find all stocks in a specific sector.
     * 
     * @param sector the sector name (e.g., "Technology")
     * @return list of stocks in that sector
     */
    List<Stock> findBySector(String sector);

    /**
     * Check if a stock symbol already exists (for validation).
     * 
     * @param symbol the stock ticker to check
     * @return true if exists, false otherwise
     */
    boolean existsBySymbol(String symbol);

    /**
     * Find all active stocks ordered by symbol alphabetically.
     * 
     * Custom query demonstrating JPQL syntax.
     * 
     * @return list of active stocks sorted by symbol
     */
    @Query("SELECT s FROM Stock s WHERE s.isActive = true ORDER BY s.symbol ASC")
    List<Stock> findAllActiveOrderedBySymbol();

    /**
     * Find all active stocks by asset type.
     * 
     * @param assetType the type of asset (STOCK, ETF, INDEX)
     * @return list of stocks matching the type
     */
    List<Stock> findByAssetTypeAndIsActiveTrue(AssetType assetType);

    /**
     * Find all active stocks by sector.
     * 
     * @param sector the sector name
     * @return list of stocks in that sector
     */
    List<Stock> findBySectorAndIsActiveTrue(String sector);

    /**
     * Find all active stocks by asset type and sector.
     * 
     * @param assetType the type of asset
     * @param sector the sector name
     * @return list of stocks matching both criteria
     */
    List<Stock> findByAssetTypeAndSectorAndIsActiveTrue(AssetType assetType, String sector);

    /**
     * Get distinct sectors for active stocks.
     * 
     * @return list of unique sector names
     */
    @Query("SELECT DISTINCT s.sector FROM Stock s WHERE s.isActive = true AND s.sector IS NOT NULL ORDER BY s.sector")
    List<String> findDistinctSectors();
}