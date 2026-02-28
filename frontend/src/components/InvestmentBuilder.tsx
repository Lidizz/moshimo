import { useState, useMemo } from 'react';
import { InvestmentForm } from './InvestmentForm';
import { useSimulationContext } from '../hooks/useSimulationContext';
import type { Asset, Investment, SimulationRequest } from '../types/api.types';
import styles from './InvestmentBuilder.module.css';

interface InvestmentBuilderProps {
  assets: Asset[];
  onSimulate: (request: SimulationRequest) => void;
  isSimulating: boolean;
}

/**
 * Investment Builder - Manages portfolio of investments.
 * 
 * Learning Notes:
 * - Dynamic form arrays: Add/remove items
 * - Form validation: Only enable submit when all investments valid
 * - UUID generation: crypto.randomUUID() for unique keys
 * - Investments state lives in SimulationContext (survives navigation)
 */
export function InvestmentBuilder({ assets, onSimulate, isSimulating }: InvestmentBuilderProps) {
  const { investments, setInvestments } = useSimulationContext();
  const [showValidation, setShowValidation] = useState(false);

  // Create a new empty investment
  function createEmptyInvestment(): Investment {
    return {
      id: crypto.randomUUID(),
      symbol: '',
      amountUsd: 0,
      purchaseDate: '',
    };
  }

  // Add new investment row
  const handleAddInvestment = () => {
    if (investments.length < 10) {
      setInvestments([...investments, createEmptyInvestment()]);
    }
  };

  // Update an investment
  const handleUpdateInvestment = (index: number, updated: Investment) => {
    const newInvestments = [...investments];
    newInvestments[index] = updated;
    setInvestments(newInvestments);
  };

  // Remove an investment
  const handleRemoveInvestment = (index: number) => {
    if (investments.length > 1) {
      setInvestments(investments.filter((_, i) => i !== index));
    }
  };

  // Validate all investments
  const validInvestments = useMemo(() => {
    return investments.filter(
      (inv) =>
        inv.symbol &&
        inv.amountUsd > 0 &&
        inv.amountUsd <= 1000000 &&
        inv.purchaseDate &&
        new Date(inv.purchaseDate) <= new Date()
    );
  }, [investments]);

  const canSimulate = validInvestments.length > 0 && !isSimulating;

  // Handle simulation
  const handleSimulate = () => {
    // Always show validation when user clicks simulate
    setShowValidation(true);
    
    if (!canSimulate) return;

    const request: SimulationRequest = {
      investments: validInvestments.map((inv) => ({
        symbol: inv.symbol,
        amountUsd: inv.amountUsd,
        purchaseDate: inv.purchaseDate,
      })),
    };

    onSimulate(request);
  };

  return (
    <div className={styles.investmentBuilder}>
      <div className={styles.investmentBuilderHeader}>
        <h2 className={styles.investmentBuilderTitle}>Build Your Portfolio</h2>
        <p className={styles.investmentBuilderSubtitle}>
          Add up to 10 investments to simulate "what if" scenarios
        </p>
      </div>

      <div className={styles.investmentBuilderForms}>
        {investments.map((investment, index) => (
          <InvestmentForm
            key={investment.id}
            investment={investment}
            assets={assets}
            onUpdate={(updated) => handleUpdateInvestment(index, updated)}
            onRemove={() => handleRemoveInvestment(index)}
            canRemove={investments.length > 1}
            showValidation={showValidation}
          />
        ))}
      </div>

      <div className={styles.investmentBuilderActions}>
        <button
          type="button"
          className={styles.investmentBuilderAddBtn}
          onClick={handleAddInvestment}
          disabled={investments.length >= 10}
        >
          + Add Another Investment
        </button>

        <div className={styles.investmentBuilderSummary}>
          <span className={styles.investmentBuilderSummaryText}>
            {validInvestments.length} of {investments.length} investments valid
          </span>
          <button
            type="button"
            className={styles.investmentBuilderSimulateBtn}
            onClick={handleSimulate}
            disabled={!canSimulate}
          >
            {isSimulating ? 'Simulating...' : `Simulate ${validInvestments.length} Investment${validInvestments.length !== 1 ? 's' : ''}`}
          </button>
        </div>
      </div>
    </div>
  );
}