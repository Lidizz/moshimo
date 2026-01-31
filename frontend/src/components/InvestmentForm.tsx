import { useState, useEffect } from 'react';
import { StockSelector } from './StockSelector';
import type { Stock, Investment } from '../types/api.types';
import './InvestmentForm.css';

interface InvestmentFormProps {
  investment: Investment;
  stocks: Stock[];
  onUpdate: (investment: Investment) => void;
  onRemove: () => void;
  canRemove: boolean;
  showValidation?: boolean;  // Only show errors when true (after simulate attempt)
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
  stocks, 
  onUpdate, 
  onRemove, 
  canRemove,
  showValidation = false
}: InvestmentFormProps) {
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [touched, setTouched] = useState<Record<string, boolean>>({});

  // Mark field as touched when user interacts
  const handleBlur = (field: string) => {
    setTouched(prev => ({ ...prev, [field]: true }));
  };

  // Show error only if touched or showValidation is true
  const shouldShowError = (field: string) => {
    return (touched[field] || showValidation) && errors[field];
  };

  // Validate investment data
  useEffect(() => {
    const newErrors: Record<string, string> = {};

    if (!investment.symbol) {
      newErrors.symbol = 'Stock is required';
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

      // Check if date is after stock IPO
      const stock = stocks.find(s => s.symbol === investment.symbol);
      if (stock && stock.ipoDate) {
        const ipoDate = new Date(stock.ipoDate);
        if (purchaseDate < ipoDate) {
          newErrors.purchaseDate = `Date must be after IPO (${stock.ipoDate})`;
        }
      }
    }

    setErrors(newErrors);
  }, [investment, stocks]);

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

  return (
    <div className={`investment-form ${!isValid ? 'investment-form--invalid' : ''}`}>
      <div className="investment-form__grid">
        {/* Stock Selector */}
        <div className="investment-form__field">
          <StockSelector
            stocks={stocks}
            selectedSymbol={investment.symbol}
            onSelect={(symbol) => {
              handleSymbolChange(symbol);
              setTouched(prev => ({ ...prev, symbol: true }));
            }}
          />
          <span className="investment-form__error-slot">
            {shouldShowError('symbol') && errors.symbol}
          </span>
        </div>

        {/* Amount Input */}
        <div className="investment-form__field">
          <label className="investment-form__label">Amount (USD)</label>
          <input
            type="number"
            className="investment-form__input"
            placeholder="1000"
            min="0.01"
            max="1000000"
            step="0.01"
            value={investment.amountUsd || ''}
            onChange={handleAmountChange}
            onBlur={() => handleBlur('amountUsd')}
          />
          <span className="investment-form__error-slot">
            {shouldShowError('amountUsd') && errors.amountUsd}
          </span>
        </div>

        {/* Date Picker */}
        <div className="investment-form__field">
          <label className="investment-form__label">Purchase Date</label>
          <input
            type="date"
            className="investment-form__input"
            max={new Date().toISOString().split('T')[0]}
            value={investment.purchaseDate}
            onChange={handleDateChange}
            onBlur={() => handleBlur('purchaseDate')}
          />
          <span className="investment-form__error-slot">
            {shouldShowError('purchaseDate') && errors.purchaseDate}
          </span>
        </div>

        {/* Remove Button */}
        <div className="investment-form__actions">
          {canRemove && (
            <button
              type="button"
              className="investment-form__remove"
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
        <div className="investment-form__valid-indicator">✓ Valid</div>
      )}
    </div>
  );
}