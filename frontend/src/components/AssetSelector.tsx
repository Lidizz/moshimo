import { useState, useEffect, useMemo } from 'react';
import type { Asset, AssetType } from '../types/api.types';
import { AssetTypeFilter } from './AssetTypeFilter';
import { SectorFilter } from './SectorFilter';
import './AssetSelector.css';

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
    case 'ETF': return 'asset-selector__badge--etf';
    case 'INDEX': return 'asset-selector__badge--index';
    default: return 'asset-selector__badge--stock';
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
    <div className="asset-selector" onClick={(e) => e.stopPropagation()}>
      <label className="asset-selector__label">Select Asset</label>
      
      <div className="asset-selector__input-wrapper">
        <button
          type="button"
          className="asset-selector__trigger"
          onClick={() => !disabled && setIsOpen(!isOpen)}
          disabled={disabled}
        >
          {selectedAsset ? (
            <div className="asset-selector__selected">
              <span className="asset-selector__symbol">{selectedAsset.symbol}</span>
              <span className="asset-selector__name">{selectedAsset.name}</span>
            </div>
          ) : (
            <span className="asset-selector__placeholder">Choose an asset...</span>
          )}
          <span className="asset-selector__arrow">{isOpen ? '▲' : '▼'}</span>
        </button>

        {isOpen && (
          <div className="asset-selector__dropdown">
            {/* Filter Controls */}
            <div className="asset-selector__filters">
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
                  className="asset-selector__clear-filters"
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
              className="asset-selector__search"
              placeholder="Search by symbol, name, or sector..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              autoFocus
            />
            
            <div className="asset-selector__list">
              {filteredAssets.length > 0 ? (
                filteredAssets.map((asset) => (
                  <button
                    key={asset.id}
                    type="button"
                    className={`asset-selector__option ${
                      asset.symbol === selectedSymbol ? 'asset-selector__option--selected' : ''
                    }`}
                    onClick={() => handleSelect(asset.symbol)}
                  >
                    <div className="asset-selector__option-header">
                      <span className="asset-selector__option-symbol">{asset.symbol}</span>
                      <span className={`asset-selector__badge ${getAssetTypeBadgeClass(asset.assetType)}`}>
                        {asset.assetType}
                      </span>
                      <span className="asset-selector__option-exchange">{asset.exchange}</span>
                    </div>
                    <div className="asset-selector__option-name">{asset.name}</div>
                    {asset.sector && (
                      <div className="asset-selector__option-sector">{asset.sector}</div>
                    )}
                  </button>
                ))
              ) : (
                <div className="asset-selector__empty">
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