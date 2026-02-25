package com.moshimo.backend.domain.repository;

import com.moshimo.backend.domain.model.AssetType;
import com.moshimo.backend.domain.model.Asset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Asset entity - Data Access Layer.
 * 
 * Learning Notes:
 * - Extends JpaRepository<Entity, ID>: Provides CRUD operations out-of-the-box
 * - Method naming conventions: Spring Data JPA auto-generates queries
 *   Example: findBySymbol → SELECT * FROM asset WHERE symbol = ?
 * - Optional<T>: Java 8+ feature to handle potential null results safely
 * - @Query: Custom JPQL queries for complex operations
 * 
 * Design Pattern: Repository Pattern (abstracts data access logic)
 */
@Repository
public interface AssetRepository extends JpaRepository<Asset, Long> {

    /**
     * Find an asset by its ticker symbol.
     * 
     * Spring Data JPA auto-generates:
     * SELECT * FROM asset WHERE symbol = :symbol
     * 
     * @param symbol the ticker (e.g., "AAPL")
     * @return Optional containing the asset if found, empty otherwise
     * 
     * Learning: Optional prevents NullPointerException, forces explicit handling
     */
    Optional<Asset> findBySymbol(String symbol);

    /**
     * Find all active assets (is_active = true).
     * 
     * Auto-generated query:
     * SELECT * FROM asset WHERE is_active = true
     * 
     * @return list of active assets (empty list if none found)
     */
    List<Asset> findByIsActiveTrue();

    /**
     * Find all assets in a specific sector.
     * 
     * @param sector the sector name (e.g., "Technology")
     * @return list of assets in that sector
     */
    List<Asset> findBySector(String sector);

    /**
     * Check if a symbol already exists (for validation).
     * 
     * @param symbol the ticker to check
     * @return true if exists, false otherwise
     */
    boolean existsBySymbol(String symbol);

    /**
     * Find all active assets ordered by symbol alphabetically.
     * 
     * Custom query demonstrating JPQL syntax.
     * 
     * @return list of active assets sorted by symbol
     */
    @Query("SELECT a FROM Asset a WHERE a.isActive = true ORDER BY a.symbol ASC")
    List<Asset> findAllActiveOrderedBySymbol();

    /**
     * Find all active assets by asset type.
     * 
     * @param assetType the type of asset (STOCK, ETF, INDEX, CRYPTO)
     * @return list of assets matching the type
     */
    List<Asset> findByAssetTypeAndIsActiveTrue(AssetType assetType);

    /**
     * Find all active assets by sector.
     * 
     * @param sector the sector name
     * @return list of assets in that sector
     */
    List<Asset> findBySectorAndIsActiveTrue(String sector);

    /**
     * Find all active assets by asset type and sector.
     * 
     * @param assetType the type of asset
     * @param sector the sector name
     * @return list of assets matching both criteria
     */
    List<Asset> findByAssetTypeAndSectorAndIsActiveTrue(AssetType assetType, String sector);

    /**
     * Get distinct sectors for active assets.
     * 
     * @return list of unique sector names
     */
    @Query("SELECT DISTINCT a.sector FROM Asset a WHERE a.isActive = true AND a.sector IS NOT NULL ORDER BY a.sector")
    List<String> findDistinctSectors();
}