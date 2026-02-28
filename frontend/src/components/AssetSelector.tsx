import { useState, useEffect, useMemo } from 'react';
import type { Asset, AssetType } from '../types/api.types';
import { AssetTypeFilter } from './AssetTypeFilter';
import { SectorFilter } from './SectorFilter';
import styles from './AssetSelector.module.css';

interface AssetSelectorProps {
  assets: Asset[];
  selectedSymbol: string;
  onSelect: (symbol: string) => void;
  disabled?: boolean;
}

/**
 * Get badge class based on asset type.
 * 
 * Learning Notes:
 * - STOCK: Gray (default, most common)
 * - ETF: Blue (bundled investments)
 * - INDEX: Purple (market benchmarks)
 */
function getAssetTypeBadgeClass(assetType: AssetType): string {
  switch (assetType) {
    case 'ETF': return styles.assetSelectorBadgeEtf;
    case 'INDEX': return styles.assetSelectorBadgeIndex;
    default: return styles.assetSelectorBadgeStock;
  }
}

/**
 * Asset Selector Component - Searchable dropdown for asset selection.
 * 
 * Learning Notes:
 * - useMemo: Optimizes filtering performance (only recalculates when search changes)
 * - Controlled component pattern: parent manages state
 * - Mobile-first: Touch-friendly, responsive design
 * - Asset type filters: Users can filter by STOCK, ETF, INDEX, or CRYPTO
 * - Sector filters: Users can filter by business sector (Technology, Healthcare, etc.)
 */
export function AssetSelector({ assets, selectedSymbol, onSelect, disabled = false }: AssetSelectorProps) {
  const [searchTerm, setSearchTerm] = useState('');
  const [isOpen, setIsOpen] = useState(false);
  const [selectedAssetType, setSelectedAssetType] = useState<AssetType | null>(null);
  const [selectedSector, setSelectedSector] = useState<string | null>(null);

  // Filter assets based on search term, asset type, and sector
  const filteredAssets = useMemo(() => {
    let result = assets;
    
    // Filter by asset type
    if (selectedAssetType) {
      result = result.filter((asset) => asset.assetType === selectedAssetType);
    }
    
    // Filter by sector
    if (selectedSector) {
      result = result.filter((asset) => asset.sector === selectedSector);
    }
    
    // Filter by search term
    if (searchTerm) {
      const term = searchTerm.toLowerCase();
      result = result.filter(
        (asset) =>
          asset.symbol.toLowerCase().includes(term) ||
          asset.name.toLowerCase().includes(term) ||
          (asset.sector && asset.sector.toLowerCase().includes(term))
      );
    }
    
    return result;
  }, [assets, searchTerm, selectedAssetType, selectedSector]);

  // Calculate active filter count for the filter badge
  const activeFilterCount = (selectedAssetType ? 1 : 0) + (selectedSector ? 1 : 0);

  // Get selected asset details
  const selectedAsset = assets.find((s) => s.symbol === selectedSymbol);

  // Close dropdown when clicking outside
  useEffect(() => {
    const handleClickOutside = () => setIsOpen(false);
    if (isOpen) {
      document.addEventListener('click', handleClickOutside);
      return () => document.removeEventListener('click', handleClickOutside);
    }
  }, [isOpen]);

  const handleSelect = (symbol: string) => {
    onSelect(symbol);
    setIsOpen(false);
    setSearchTerm('');
  };

  return (
    <div className={styles.assetSelector} onClick={(e) => e.stopPropagation()}>
      <label className={styles.assetSelectorLabel}>Select Asset</label>
      
      <div className={styles.assetSelectorInputWrapper}>
        <button
          type="button"
          className={styles.assetSelectorTrigger}
          onClick={() => !disabled && setIsOpen(!isOpen)}
          disabled={disabled}
        >
          {selectedAsset ? (
            <span className={styles.assetSelectorSymbol}>{selectedAsset.symbol}</span>
          ) : (
            <span className={styles.assetSelectorPlaceholder}>Choose an asset...</span>
          )}
          <span className={styles.assetSelectorArrow}>{isOpen ? '▲' : '▼'}</span>
        </button>

        {/* Company name below trigger — keeps trigger compact */}
        {selectedAsset && (
          <span className={styles.assetSelectorCompanyLabel}>{selectedAsset.name}</span>
        )}

        {isOpen && (
          <div className={styles.assetSelectorDropdown}>
            {/* Filter Controls */}
            <div className={styles.assetSelectorFilters}>
              <AssetTypeFilter
                selectedType={selectedAssetType}
                onTypeChange={setSelectedAssetType}
              />
              <SectorFilter
                selectedSector={selectedSector}
                onSectorChange={setSelectedSector}
              />
              {activeFilterCount > 0 && (
                <button
                  type="button"
                  className={styles.assetSelectorClearFilters}
                  onClick={() => {
                    setSelectedAssetType(null);
                    setSelectedSector(null);
                  }}
                >
                  Clear filters ({activeFilterCount})
                </button>
              )}
            </div>
            
            <input
              type="text"
              className={styles.assetSelectorSearch}
              placeholder="Search by symbol, name, or sector..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              autoFocus
            />
            
            <div className={styles.assetSelectorList}>
              {filteredAssets.length > 0 ? (
                filteredAssets.map((asset) => (
                  <button
                    key={asset.id}
                    type="button"
                    className={`${styles.assetSelectorOption} ${
                      asset.symbol === selectedSymbol ? styles.assetSelectorOptionSelected : ''
                    }`}
                    onClick={() => handleSelect(asset.symbol)}
                  >
                    <div className={styles.assetSelectorOptionHeader}>
                      <span className={styles.assetSelectorOptionSymbol}>{asset.symbol}</span>
                      <span className={`${styles.assetSelectorBadge} ${getAssetTypeBadgeClass(asset.assetType)}`}>
                        {asset.assetType}
                      </span>
                      <span className={styles.assetSelectorOptionExchange}>{asset.exchange}</span>
                    </div>
                    <div className={styles.assetSelectorOptionName}>{asset.name}</div>
                    {asset.sector && (
                      <div className={styles.assetSelectorOptionSector}>{asset.sector}</div>
                    )}
                  </button>
                ))
              ) : (
                <div className={styles.assetSelectorEmpty}>
                  {activeFilterCount > 0 
                    ? 'No assets match your filters' 
                    : 'No assets found'}
                </div>
              )}
            </div>
          </div>
        )}
      </div>
    </div>
  );
}