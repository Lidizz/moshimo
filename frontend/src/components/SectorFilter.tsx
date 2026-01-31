import { useEffect, useState } from 'react';
import { stockApi } from '../services/api/stockApi';
import './SectorFilter.css';

interface SectorFilterProps {
  selectedSector: string | null;
  onSectorChange: (sector: string | null) => void;
  disabled?: boolean;
}

/**
 * Sector Filter - Dropdown for filtering by sector.
 * 
 * Learning Notes:
 * - Fetches available sectors from the backend
 * - Null means "All Sectors" (no filter)
 * - Uses native select for best mobile UX
 */
export function SectorFilter({ 
  selectedSector, 
  onSectorChange, 
  disabled = false 
}: SectorFilterProps) {
  const [sectors, setSectors] = useState<string[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchSectors = async () => {
      try {
        setLoading(true);
        const data = await stockApi.getSectors();
        setSectors(data);
        setError(null);
      } catch (err) {
        console.error('Failed to fetch sectors:', err);
        setError('Failed to load sectors');
      } finally {
        setLoading(false);
      }
    };

    fetchSectors();
  }, []);

  const handleChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    const value = e.target.value;
    onSectorChange(value === '' ? null : value);
  };

  return (
    <div className="sector-filter">
      <label className="sector-filter__label" htmlFor="sector-select">
        <span className="sector-filter__icon" aria-hidden="true">🏢</span>
        Sector
      </label>
      <select
        id="sector-select"
        className="sector-filter__select"
        value={selectedSector ?? ''}
        onChange={handleChange}
        disabled={disabled || loading}
        aria-busy={loading}
      >
        <option value="">
          {loading ? 'Loading...' : error ? 'Error' : 'All Sectors'}
        </option>
        {sectors.map((sector) => (
          <option key={sector} value={sector}>
            {sector}
          </option>
        ))}
      </select>
    </div>
  );
}
