import { Download } from 'lucide-react';
import { PortfolioChart } from './PortfolioChart';
import { PortfolioHeader } from './PortfolioHeader';
import { AllocationChart } from './AllocationChart';
import { useCountUp } from '../hooks/useCountUp';
import { generatePortfolioCSV, downloadCSV, buildExportFilename } from '../utils/csvExport';
import type { SimulationResponse, InvestmentItem } from '../types/api.types';
import styles from './SimulationResults.module.css';

interface SimulationResultsProps {
  results: SimulationResponse;
  investments?: InvestmentItem[];
}

/**
 * Simulation Results Component - Displays portfolio performance metrics.
 * 
 * Learning Notes:
 * - Number formatting: Intl.NumberFormat for currency/percentage
 * - Conditional styling: Green for gains, red for losses
 * - Grid layout: Responsive card-based design
 */
export function SimulationResults({ results, investments }: SimulationResultsProps) {
  // Format currency
  const formatCurrency = (value: number) => {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD',
      minimumFractionDigits: 2,
      maximumFractionDigits: 2,
    }).format(value);
  };

  // Format percentage with commas for large values
  const formatPercent = (value: number) => {
    const sign = value >= 0 ? '+' : '';
    const formattedNumber = new Intl.NumberFormat('en-US', {
      minimumFractionDigits: 2,
      maximumFractionDigits: 2,
    }).format(Math.abs(value));
    return `${sign}${formattedNumber}%`;
  };

  // Format shares with 4 decimal places (industry standard for fractional shares)
  const formatShares = (value: number) => {
    return new Intl.NumberFormat('en-US', {
      minimumFractionDigits: 4,
      maximumFractionDigits: 4,
    }).format(value);
  };

  // Animate metric values from 0 to target on render
  const animatedTotalInvested = useCountUp(results.totalInvested);
  const animatedCurrentValue = useCountUp(results.currentValue);
  const animatedAbsoluteGain = useCountUp(results.absoluteGain);
  const animatedPercentReturn = useCountUp(results.percentReturn);
  const animatedCagr = useCountUp(results.cagr);

  const isProfit = results.absoluteGain >= 0;

  const handleExportCSV = () => {
    const csv = generatePortfolioCSV(results);
    downloadCSV(csv, buildExportFilename());
  };

  return (
    <div className={styles.simulationResults}>
      {/* Key Metrics Cards */}
      <div className={styles.simulationResultsMetrics} data-testid="metrics-grid">
        <div className={styles.metricCard}>
          <div className={styles.metricCardLabel}>Total Invested</div>
          <div className={styles.metricCardValue}>{formatCurrency(animatedTotalInvested)}</div>
        </div>

        <div className={styles.metricCard}>
          <div className={styles.metricCardLabel}>Current Value</div>
          <div className={`${styles.metricCardValue} ${styles.metricCardValueHighlight}`}>
            {formatCurrency(animatedCurrentValue)}
          </div>
        </div>

        <div className={`${styles.metricCard} ${isProfit ? styles.metricCardGain : styles.metricCardLoss}`}>
          <div className={styles.metricCardLabel}>Absolute Gain</div>
          <div className={styles.metricCardValue}>
            {formatCurrency(animatedAbsoluteGain)}
          </div>
        </div>

        <div className={`${styles.metricCard} ${isProfit ? styles.metricCardGain : styles.metricCardLoss}`}>
          <div className={styles.metricCardLabel}>Percent Return</div>
          <div className={styles.metricCardValue}>
            {formatPercent(animatedPercentReturn)}
          </div>
        </div>

        <div className={`${styles.metricCard} ${styles.metricCardCagr}`}>
          <div className={styles.metricCardLabel}>CAGR</div>
          <div className={styles.metricCardValue}>
            {formatPercent(animatedCagr)}
          </div>
          <div className={styles.metricCardHint}>Compound Annual Growth Rate</div>
        </div>
      </div>

      {/* Export Toolbar */}
      <div className={styles.resultsToolbar}>
        <button
          className={styles.exportButton}
          onClick={handleExportCSV}
          title="Export simulation results as CSV"
          data-testid="export-csv-button"
        >
          <Download size={16} />
          Export CSV
        </button>
      </div>

      {/* Mobile sticky summary — visible only on small screens */}
      <div className={styles.simulationResultsMobileSummary}>
        <div className={styles.mobileSummaryItem}>
          <span className={styles.mobileSummaryLabel}>Value</span>
          <span className={styles.mobileSummaryValue}>{formatCurrency(results.currentValue)}</span>
        </div>
        <div className={styles.mobileSummaryItem}>
          <span className={`${styles.mobileSummaryLabel} ${isProfit ? styles.textGreen : styles.textRed}`}>Return</span>
          <span className={`${styles.mobileSummaryValue} ${isProfit ? styles.textGreen : styles.textRed}`}>
            {formatPercent(results.percentReturn)}
          </span>
        </div>
      </div>

      {/* Portfolio Header - Shows date range */}
      {investments && investments.length > 0 && (
        <PortfolioHeader
          holdings={results.holdings}
          investments={investments}
          endDate={results.timeline[results.timeline.length - 1]?.date || new Date().toISOString().split('T')[0]}
        />
      )}

      {/* Portfolio Chart */}
      <PortfolioChart 
        timeline={results.timeline} 
        totalInvested={results.totalInvested}
        investments={investments}
        showBenchmark={true}
        benchmarkTimeline={results.benchmarkTimeline}
        holdings={results.holdings}
        holdingsTimelines={results.holdingsTimelines}
      />

      {/* Allocation Donut Chart */}
      {results.holdings.length > 1 && (
        <AllocationChart holdings={results.holdings} />
      )}

      {/* Holdings Table */}
      <div className={styles.holdingsTable} data-testid="holdings-table">
        <h3 className={styles.holdingsTableTitle}>Holdings Breakdown</h3>
        
        <div className={styles.holdingsTableContainer}>
          <table className={styles.holdingsTableTable}>
            <thead>
              <tr>
                <th>Asset</th>
                <th className={styles.textRight}>Invested</th>
                <th className={styles.textRight}>Shares</th>
                <th className={styles.textRight}>Purchase Price</th>
                <th className={styles.textRight}>Current Price</th>
                <th className={styles.textRight}>Current Value</th>
                <th className={styles.textRight}>Gain/Loss</th>
                <th className={styles.textRight}>Return %</th>
              </tr>
            </thead>
            <tbody>
              {results.holdings.map((holding) => {
                const isProfitable = holding.absoluteGain >= 0;
                return (
                  <tr key={holding.symbol}>
                    <td>
                      <div className={styles.holdingsTableAsset}>
                        <span className={styles.holdingsTableSymbol}>{holding.symbol}</span>
                        <span className={styles.holdingsTableName}>{holding.name}</span>
                      </div>
                    </td>
                    <td className={styles.textRight} data-label="Invested">{formatCurrency(holding.invested)}</td>
                    <td className={styles.textRight} data-label="Shares">{formatShares(holding.shares)}</td>
                    <td className={styles.textRight} data-label="Purchase Price">{formatCurrency(holding.purchasePrice)}</td>
                    <td className={styles.textRight} data-label="Current Price">{formatCurrency(holding.currentPrice)}</td>
                    <td className={`${styles.textRight} ${styles.fontSemibold}`} data-label="Current Value">{formatCurrency(holding.currentValue)}</td>
                    <td className={`${styles.textRight} ${styles.fontSemibold} ${isProfitable ? styles.textGreen : styles.textRed}`} data-label="Gain/Loss">
                      {formatCurrency(holding.absoluteGain)}
                    </td>
                    <td className={`${styles.textRight} ${styles.fontSemibold} ${isProfitable ? styles.textGreen : styles.textRed}`} data-label="Return %">
                      {formatPercent(holding.percentReturn)}
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      </div>

      {/* Data Freshness Disclaimer */}
      <div className={styles.dataDisclaimer}>
        <span className={styles.disclaimerIcon}>📅</span>
        <p className={styles.disclaimerText}>
          Historical price data updated monthly on the 1st. 
          Current data as of: <strong>{new Date().toLocaleDateString('en-US', { month: 'long', year: 'numeric' })}</strong>
        </p>
      </div>
    </div>
  );
}