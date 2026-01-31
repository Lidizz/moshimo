import type { AssetType } from '../types/api.types';
import './AssetTypeFilter.css';

interface AssetTypeFilterProps {
  selectedType: AssetType | null;
  onTypeChange: (type: AssetType | null) => void;
  disabled?: boolean;
}

/**
 * Asset Type Filter - Toggle buttons for filtering by asset type.
 * 
 * Learning Notes:
 * - Null means "All" (no filter)
 * - Kid-friendly: Large touch targets, clear visual feedback
 * - Accessible: ARIA labels for screen readers
 */
export function AssetTypeFilter({ 
  selectedType, 
  onTypeChange, 
  disabled = false 
}: AssetTypeFilterProps) {
  const types: { value: AssetType | null; label: string; icon: string }[] = [
    { value: null, label: 'All', icon: '📊' },
    { value: 'STOCK', label: 'Stocks', icon: '📈' },
    { value: 'ETF', label: 'ETFs', icon: '📦' },
    { value: 'INDEX', label: 'Indexes', icon: '📉' },
  ];

  return (
    <div className="asset-type-filter" role="group" aria-label="Filter by asset type">
      {types.map(({ value, label, icon }) => (
        <button
          key={label}
          type="button"
          className={`asset-type-filter__btn ${
            selectedType === value ? 'asset-type-filter__btn--active' : ''
          }`}
          onClick={() => onTypeChange(value)}
          disabled={disabled}
          aria-pressed={selectedType === value}
        >
          <span className="asset-type-filter__icon" aria-hidden="true">{icon}</span>
          <span className="asset-type-filter__label">{label}</span>
        </button>
      ))}
    </div>
  );
}
