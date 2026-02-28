import type { AssetType } from '../types/api.types';
import styles from './AssetTypeFilter.module.css';

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
    <div className={styles.assetTypeFilter} role="group" aria-label="Filter by asset type">
      {types.map(({ value, label, icon }) => (
        <button
          key={label}
          type="button"
          className={`${styles.assetTypeFilterBtn} ${
            selectedType === value ? styles.assetTypeFilterBtnActive : ''
          }`}
          onClick={() => onTypeChange(value)}
          disabled={disabled}
          aria-pressed={selectedType === value}
        >
          <span className={styles.assetTypeFilterIcon} aria-hidden="true">{icon}</span>
          <span className={styles.assetTypeFilterLabel}>{label}</span>
        </button>
      ))}
    </div>
  );
}
