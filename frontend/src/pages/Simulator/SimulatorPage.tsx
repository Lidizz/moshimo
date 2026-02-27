import { InvestmentBuilder } from '../../components/InvestmentBuilder';
import { SimulationResults } from '../../components/SimulationResults';
import { TimeframeSelector } from '../../components/TimeframeSelector';
import { LoadingSpinner } from '../../components/LoadingSpinner';
import { Toast } from '../../components/Toast';
import { PWAPrompt } from '../../components/PWAPrompt';
import { ErrorBoundary } from '../../components/ErrorBoundary';
import { useHealthCheck } from '../../hooks/useHealthCheck';
import { useSimulation } from '../../hooks/useSimulation';
import './SimulatorPage.css';

function SimulatorPage() {
  const { health, assets, loading, error } = useHealthCheck();
  const {
    isSimulating,
    simulationResults,
    simulationError,
    timeframe,
    lastRequest,
    toast,
    clearToast,
    handleSimulate,
    handleTimeframeChange,
  } = useSimulation();

  if (loading) {
    return (
      <div className="simulator-loading">
        <div className="spinner"></div>
        <h1>Loading Simulator...</h1>
        <p>Connecting to backend and fetching asset data</p>
      </div>
    );
  }

  if (error) {
    return (
      <div className="simulator-error">
        <h1>❌ Error</h1>
        <p>{error}</p>
        <button onClick={() => window.location.reload()}>Retry</button>
      </div>
    );
  }

  return (
    <div className="simulator-page">
      {/* PWA Install Prompt */}
      <PWAPrompt />

      {/* Toast notifications */}
      {toast && (
        <Toast 
          message={toast.message} 
          type={toast.type} 
          onClose={clearToast}
        />
      )}

      {/* Investment Builder */}
      <InvestmentBuilder 
        assets={assets}
        onSimulate={handleSimulate}
        isSimulating={isSimulating}
      />

      {/* Simulation Error */}
      {simulationError && (
        <div className="simulation-error">
          <strong>Simulation Error:</strong> {simulationError}
        </div>
      )}

      {/* Loading Spinner */}
      {isSimulating && !simulationResults && (
        <LoadingSpinner message="Calculating portfolio performance..." />
      )}

      {/* Timeframe Selector - only show when we have results */}
      {simulationResults && (
        <TimeframeSelector
          selectedTimeframe={timeframe}
          onTimeframeChange={handleTimeframeChange}
          disabled={isSimulating}
        />
      )}

      {/* Results */}
      {simulationResults && lastRequest && (
        <ErrorBoundary fallbackMessage="Could not render simulation results. Try adjusting your inputs.">
          <div id="results">
            <SimulationResults 
              results={simulationResults} 
              investments={lastRequest.investments}
            />
          </div>
        </ErrorBoundary>
      )}

      {/* Debug Info (can be hidden in production) */}
      {health && (
        <details className="simulator-debug" style={{ marginTop: '3rem' }}>
          <summary>Backend Status</summary>
          <div style={{ background: 'var(--card-bg)', padding: '1rem', borderRadius: '8px', marginTop: '0.5rem', border: '1px solid var(--border-color)' }}>
            <p><strong>Status:</strong> {health.status}</p>
            <p><strong>Database:</strong> {health.database.connected ? '✅ Connected' : '❌ Disconnected'}</p>
            <p><strong>Version:</strong> {health.database.version}</p>
            <p><strong>Total Assets:</strong> {health.database.totalAssets}</p>
            <p><strong>Total Prices:</strong> {health.database.totalPriceRecords}</p>
            <p><strong>Environment:</strong> {health.application.environment}</p>
          </div>
        </details>
      )}
    </div>
  );
}

export default SimulatorPage;
