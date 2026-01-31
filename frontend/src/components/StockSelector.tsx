import { useState, useEffect, useMemo } from 'react';
import type { Stock, AssetType } from '../types/api.types';
import { AssetTypeFilter } from './AssetTypeFilter';
import { SectorFilter } from './SectorFilter';
import './StockSelector.css';

interface StockSelectorProps {
  stocks: Stock[];
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
    case 'ETF': return 'stock-selector__badge--etf';
    case 'INDEX': return 'stock-selector__badge--index';
    default: return 'stock-selector__badge--stock';
  }
}

/**
 * Stock Selector Component - Searchable dropdown for stock selection.
 * 
 * Learning Notes:
 * - useMemo: Optimizes filtering performance (only recalculates when search changes)
 * - Controlled component pattern: parent manages state
 * - Mobile-first: Touch-friendly, responsive design
 * - Asset type filters: Users can filter by STOCK, ETF, or INDEX
 * - Sector filters: Users can filter by business sector (Technology, Healthcare, etc.)
 */
export function StockSelector({ stocks, selectedSymbol, onSelect, disabled = false }: StockSelectorProps) {
  const [searchTerm, setSearchTerm] = useState('');
  const [isOpen, setIsOpen] = useState(false);
  const [selectedAssetType, setSelectedAssetType] = useState<AssetType | null>(null);
  const [selectedSector, setSelectedSector] = useState<string | null>(null);

  // Filter stocks based on search term, asset type, and sector
  const filteredStocks = useMemo(() => {
    let result = stocks;
    
    // Filter by asset type
    if (selectedAssetType) {
      result = result.filter((stock) => stock.assetType === selectedAssetType);
    }
    
    // Filter by sector
    if (selectedSector) {
      result = result.filter((stock) => stock.sector === selectedSector);
    }
    
    // Filter by search term
    if (searchTerm) {
      const term = searchTerm.toLowerCase();
      result = result.filter(
        (stock) =>
          stock.symbol.toLowerCase().includes(term) ||
          stock.name.toLowerCase().includes(term) ||
          (stock.sector && stock.sector.toLowerCase().includes(term))
      );
    }
    
    return result;
  }, [stocks, searchTerm, selectedAssetType, selectedSector]);

  // Calculate active filter count for the filter badge
  const activeFilterCount = (selectedAssetType ? 1 : 0) + (selectedSector ? 1 : 0);

  // Get selected stock details
  const selectedStock = stocks.find((s) => s.symbol === selectedSymbol);

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
    <div className="stock-selector" onClick={(e) => e.stopPropagation()}>
      <label className="stock-selector__label">Select Stock</label>
      
      <div className="stock-selector__input-wrapper">
        <button
          type="button"
          className="stock-selector__trigger"
          onClick={() => !disabled && setIsOpen(!isOpen)}
          disabled={disabled}
        >
          {selectedStock ? (
            <div className="stock-selector__selected">
              <span className="stock-selector__symbol">{selectedStock.symbol}</span>
              <span className="stock-selector__name">{selectedStock.name}</span>
            </div>
          ) : (
            <span className="stock-selector__placeholder">Choose a stock...</span>
          )}
          <span className="stock-selector__arrow">{isOpen ? '▲' : '▼'}</span>
        </button>

        {isOpen && (
          <div className="stock-selector__dropdown">
            {/* Filter Controls */}
            <div className="stock-selector__filters">
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
                  className="stock-selector__clear-filters"
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
              className="stock-selector__search"
              placeholder="Search by symbol, name, or sector..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              autoFocus
            />
            
            <div className="stock-selector__list">
              {filteredStocks.length > 0 ? (
                filteredStocks.map((stock) => (
                  <button
                    key={stock.id}
                    type="button"
                    className={`stock-selector__option ${
                      stock.symbol === selectedSymbol ? 'stock-selector__option--selected' : ''
                    }`}
                    onClick={() => handleSelect(stock.symbol)}
                  >
                    <div className="stock-selector__option-header">
                      <span className="stock-selector__option-symbol">{stock.symbol}</span>
                      <span className={`stock-selector__badge ${getAssetTypeBadgeClass(stock.assetType)}`}>
                        {stock.assetType}
                      </span>
                      <span className="stock-selector__option-exchange">{stock.exchange}</span>
                    </div>
                    <div className="stock-selector__option-name">{stock.name}</div>
                    {stock.sector && (
                      <div className="stock-selector__option-sector">{stock.sector}</div>
                    )}
                  </button>
                ))
              ) : (
                <div className="stock-selector__empty">
                  {activeFilterCount > 0 
                    ? 'No stocks match your filters' 
                    : 'No stocks found'}
                </div>
              )}
            </div>
          </div>
        )}
      </div>
    </div>
  );
}