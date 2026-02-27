import { useEffect, useState } from 'react';
import { healthApi } from '../../services/api/healthApi';
import { assetApi } from '../../services/api/assetApi';
import { portfolioApi } from '../../services/api/portfolioApi';
import { InvestmentBuilder } from '../../components/InvestmentBuilder';
import { SimulationResults } from '../../components/SimulationResults';
import { TimeframeSelector } from '../../components/TimeframeSelector';
import { LoadingSpinner } from '../../components/LoadingSpinner';
import { Toast } from '../../components/Toast';
import { PWAPrompt } from '../../components/PWAPrompt';
import { ErrorBoundary } from '../../components/ErrorBoundary';
import type { HealthResponse, Asset, SimulationRequest, SimulationResponse } from '../../types/api.types';
import './SimulatorPage.css';

function SimulatorPage() {
  const [health, setHealth] = useState<HealthResponse | null>(null);
  const [assets, setAssets] = useState<Asset[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  
  // Simulation state
  const [isSimulating, setIsSimulating] = useState(false);
  const [simulationResults, setSimulationResults] = useState<SimulationResponse | null>(null);
  const [simulationError, setSimulationError] = useState<string | null>(null);
  
  // Timeframe state for chart sampling
  const [timeframe, setTimeframe] = useState<string>('ALL');
  
  // Store original request for re-simulation when timeframe changes
  const [lastRequest, setLastRequest] = useState<SimulationRequest | null>(null);
  
  // Toast notification
  const [toast, setToast] = useState<{ message: string; type: 'success' | 'error' } | null>(null);

  useEffect(() => {
    const fetchData = async () => {
      try {
        setLoading(true);
        setError(null);

        // Fetch health status and assets in parallel
        const [healthData, assetsData] = await Promise.all([
          healthApi.checkHealth(),
          assetApi.getAllAssets(),
        ]);

        setHealth(healthData);
        setAssets(assetsData);
      } catch (err: any) {
        setError(err.message || 'Failed to fetch data');
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, []);

  const handleSimulate = async (request: SimulationRequest) => {
    try {
      setIsSimulating(true);
      setSimulationError(null);

      // Store request for timeframe changes
      setLastRequest(request);

      const results = await portfolioApi.simulate(request, timeframe);

      setSimulationResults(results);
      
      // Show success toast
      setToast({ 
        message: '🎉 Simulation complete! Check out your results below.', 
        type: 'success' 
      });

      // Scroll to results
      setTimeout(() => {
        document.getElementById('results')?.scrollIntoView({ 
          behavior: 'smooth', 
          block: 'start' 
        });
      }, 100);

    } catch (err: any) {
      setSimulationError(
        err.response?.data?.message || 
        err.message || 
        'Simulation failed. Please check your inputs and try again.'
      );
      setSimulationResults(null);
    } finally {
      setIsSimulating(false);
    }
  };

  /**
   * Handle timeframe changes - re-run simulation with new sampling.
   * This teaches students how the same data looks different at different scales.
   */
  const handleTimeframeChange = async (newTimeframe: string) => {
    setTimeframe(newTimeframe);
    
    // Re-run simulation with new timeframe if we have a previous request
    if (lastRequest) {
      try {
        setIsSimulating(true);
        setSimulationError(null);
        
        const results = await portfolioApi.simulate(lastRequest, newTimeframe);
        setSimulationResults(results);
      } catch (err: any) {
        setSimulationError(
          err.response?.data?.message || 
          err.message || 
          'Failed to update timeframe'
        );
      } finally {
        setIsSimulating(false);
      }
    }
  };

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
          onClose={() => setToast(null)}
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
