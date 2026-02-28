import { useState, useEffect } from 'react';
import { AssetSelector } from './AssetSelector';
import type { Asset, Investment } from '../types/api.types';
import styles from './InvestmentForm.module.css';

interface InvestmentFormProps {
  investment: Investment;
  assets: Asset[];
  onUpdate: (investment: Investment) => void;
  onRemove: () => void;
  canRemove: boolean;
  showValidation?: boolean;  // Only show errors when true (after simulate attempt)
  isNew?: boolean;  // Suppress validation on freshly-added rows
}

/**
 * Format an ISO date string (YYYY-MM-DD) to a readable label.
 * e.g. "1986-03-13" → "Mar 13, 1986"
 */
function formatIpoDate(dateStr: string): string {
  const d = new Date(dateStr + 'T00:00:00');
  return d.toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' });
}

/**
 * Individual Investment Form - One row in the investment builder.
 * 
 * Learning Notes:
 * - Controlled inputs: Parent manages state
 * - Validation: Real-time feedback on input errors
 * - Date input: HTML5 date picker (mobile-friendly)
 */
export function InvestmentForm({ 
  investment, 
  assets, 
  onUpdate, 
  onRemove, 
  canRemove,
  showValidation = false,
  isNew = false
}: InvestmentFormProps) {
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [touched, setTouched] = useState<Record<string, boolean>>({});

  // Mark field as touched when user interacts
  const handleBlur = (field: string) => {
    setTouched(prev => ({ ...prev, [field]: true }));
  };

  // Show error only if (touched OR showValidation) AND not a fresh "new" row
  const shouldShowError = (field: string) => {
    if (isNew) return false;
    return (touched[field] || showValidation) && errors[field];
  };

  // Validate investment data
  useEffect(() => {
    const newErrors: Record<string, string> = {};

    if (!investment.symbol) {
      newErrors.symbol = 'Asset is required';
    }

    if (investment.amountUsd <= 0) {
      newErrors.amountUsd = 'Amount must be greater than $0';
    }

    if (investment.amountUsd > 1000000) {
      newErrors.amountUsd = 'Amount cannot exceed $1,000,000';
    }

    if (!investment.purchaseDate) {
      newErrors.purchaseDate = 'Date is required';
    } else {
      const purchaseDate = new Date(investment.purchaseDate);
      const today = new Date();
      today.setHours(0, 0, 0, 0);

      if (purchaseDate > today) {
        newErrors.purchaseDate = 'Date cannot be in the future';
      }

      // Check if date is after asset IPO
      const asset = assets.find(s => s.symbol === investment.symbol);
      if (asset && asset.ipoDate) {
        const ipoDate = new Date(asset.ipoDate);
        if (purchaseDate < ipoDate) {
          newErrors.purchaseDate = `Date must be after IPO (${asset.ipoDate})`;
        }
      }
    }

    setErrors(newErrors);
  }, [investment, assets]);

  const handleSymbolChange = (symbol: string) => {
    onUpdate({ ...investment, symbol });
  };

  const handleAmountChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const value = parseFloat(e.target.value) || 0;
    onUpdate({ ...investment, amountUsd: value });
  };

  const handleDateChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    onUpdate({ ...investment, purchaseDate: e.target.value });
  };

  const isValid = Object.keys(errors).length === 0 && 
                  investment.symbol && 
                  investment.amountUsd > 0 && 
                  investment.purchaseDate;

  // Resolve selected asset for IPO date tooltip
  const selectedAsset = assets.find(a => a.symbol === investment.symbol);
  const ipoDate = selectedAsset?.ipoDate || undefined;

  return (
    <div className={`${styles.investmentForm} ${!isValid ? styles.investmentFormInvalid : ''}`}>
      <div className={styles.investmentFormGrid}>
        {/* Asset Selector */}
        <div className={styles.investmentFormField}>
          <AssetSelector
            assets={assets}
            selectedSymbol={investment.symbol}
            onSelect={(symbol) => {
              handleSymbolChange(symbol);
              setTouched(prev => ({ ...prev, symbol: true }));
            }}
          />
          <span className={styles.investmentFormErrorSlot}>
            {shouldShowError('symbol') && errors.symbol}
          </span>
        </div>

        {/* Amount Input */}
        <div className={styles.investmentFormField}>
          <label className={styles.investmentFormLabel}>Amount (USD)</label>
          <input
            type="number"
            className={styles.investmentFormInput}
            placeholder="1000"
            min="0.01"
            max="1000000"
            step="0.01"
            value={investment.amountUsd || ''}
            onChange={handleAmountChange}
            onBlur={() => handleBlur('amountUsd')}
          />
          <span className={styles.investmentFormErrorSlot}>
            {shouldShowError('amountUsd') && errors.amountUsd}
          </span>
        </div>

        {/* Date Picker */}
        <div className={styles.investmentFormField}>
          <label className={styles.investmentFormLabel}>
            Purchase Date
            {ipoDate && (
              <span
                className={styles.investmentFormIpoInfo}
                title={`${selectedAsset!.symbol} available from ${formatIpoDate(ipoDate)}`}
              >
                ℹ️
              </span>
            )}
          </label>
          <input
            type="date"
            className={styles.investmentFormInput}
            min={ipoDate || undefined}
            max={new Date().toISOString().split('T')[0]}
            value={investment.purchaseDate}
            onChange={handleDateChange}
            onBlur={() => handleBlur('purchaseDate')}
          />
          <span className={styles.investmentFormErrorSlot}>
            {shouldShowError('purchaseDate') && errors.purchaseDate}
          </span>
        </div>

        {/* Remove Button */}
        <div className={styles.investmentFormActions}>
          {canRemove && (
            <button
              type="button"
              className={styles.investmentFormRemove}
              onClick={onRemove}
              title="Remove investment"
            >
              ✕
            </button>
          )}
        </div>
      </div>

      {/* Validation Status Indicator */}
      {isValid && (
        <div className={styles.investmentFormValidIndicator}>✓ Valid</div>
      )}
    </div>
  );
}