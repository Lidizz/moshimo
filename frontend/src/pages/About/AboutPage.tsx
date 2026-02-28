import { TrendingUp, Briefcase, BarChart3, Zap, Moon, Smartphone, AlertTriangle } from 'lucide-react';
import styles from './AboutPage.module.css';

export default function AboutPage() {
  return (
    <div className={styles.aboutPage}>
      <div className={styles.aboutHeader}>
        <h1>About Moshimo</h1>
        <p className={styles.aboutTagline}>
          もしも - "What if" in Japanese
        </p>
      </div>
      
      <section className={styles.aboutSection}>
        <h2>What is Moshimo?</h2>
        <p>
          Moshimo (もしも - Japanese for "What if") is an educational 
          investment simulator that helps users visualize how past 
          investments would have grown using real historical market data.
        </p>
        <p>
          Ever wondered "What if I had invested $1,000 in Apple back in 2010?" 
          or "How would my portfolio have performed during the 2008 financial crisis?"
          Moshimo answers these questions with data-driven simulations.
        </p>
      </section>
      
      <section className={styles.aboutSection}>
        <h2>How It Works</h2>
        <ol className={styles.aboutList}>
          <li>
            <strong>Select Assets:</strong> Choose from stocks, ETFs, or market indexes
          </li>
          <li>
            <strong>Set Parameters:</strong> Enter investment amounts, purchase dates, and quantities
          </li>
          <li>
            <strong>Run Simulation:</strong> See historical growth with real market data
          </li>
          <li>
            <strong>Analyze Results:</strong> View interactive charts and performance metrics
          </li>
        </ol>
      </section>
      
      <section className={styles.aboutSection}>
        <h2>Features</h2>
        <ul className={`${styles.aboutList} ${styles.featuresList}`}>
          <li><TrendingUp size={20} strokeWidth={2} style={{display: 'inline', verticalAlign: 'middle', marginRight: '0.5rem'}} /> Historical data spanning 20+ years</li>
          <li><Briefcase size={20} strokeWidth={2} style={{display: 'inline', verticalAlign: 'middle', marginRight: '0.5rem'}} /> Multi-asset portfolio support</li>
          <li><BarChart3 size={20} strokeWidth={2} style={{display: 'inline', verticalAlign: 'middle', marginRight: '0.5rem'}} /> TradingView-powered charts</li>
          <li><Zap size={20} strokeWidth={2} style={{display: 'inline', verticalAlign: 'middle', marginRight: '0.5rem'}} /> Real-time S&P 500 benchmarking</li>
          <li><Moon size={20} strokeWidth={2} style={{display: 'inline', verticalAlign: 'middle', marginRight: '0.5rem'}} /> Dark/Light theme support</li>
          <li><Smartphone size={20} strokeWidth={2} style={{display: 'inline', verticalAlign: 'middle', marginRight: '0.5rem'}} /> Progressive Web App (PWA) installable</li>
        </ul>
      </section>
      
      <section className={styles.aboutSection}>
        <h2>Tech Stack</h2>
        <div className={styles.techGrid}>
          <div className={styles.techItem}>
            <h3>Backend</h3>
            <ul>
              <li>Java 25 LTS</li>
              <li>Spring Boot 4.0</li>
              <li>PostgreSQL 15</li>
              <li>Flyway Migrations</li>
            </ul>
          </div>
          <div className={styles.techItem}>
            <h3>Frontend</h3>
            <ul>
              <li>React 19</li>
              <li>TypeScript</li>
              <li>Vite</li>
              <li>React Router v6</li>
            </ul>
          </div>
          <div className={styles.techItem}>
            <h3>Data</h3>
            <ul>
              <li>TwelveData API</li>
              <li>20,000+ stocks</li>
              <li>Historical EOD prices</li>
              <li>Real-time updates</li>
            </ul>
          </div>
        </div>
      </section>
      
      <section className={styles.aboutSection}>
        <h2>Open Source</h2>
        <p>
          Moshimo is open source and available on GitHub. Contributions, 
          bug reports, and feature requests are welcome!
        </p>
        <a 
          href="https://github.com/Lidizz/moshimo" 
          target="_blank" 
          rel="noopener noreferrer"
          className={styles.githubLink}
        >
          View on GitHub →
        </a>
      </section>
      
      <section className={`${styles.aboutSection} ${styles.disclaimer}`}>
        <h2><AlertTriangle size={24} strokeWidth={2} style={{display: 'inline', verticalAlign: 'middle', marginRight: '0.5rem'}} /> Disclaimer</h2>
        <p>
          <strong>This is an educational tool only.</strong> Past performance 
          does not guarantee future results. All simulations are based on 
          historical data and do not account for fees, taxes, dividends, or 
          other real-world factors. Always consult with a financial advisor 
          before making investment decisions.
        </p>
      </section>
    </div>
  );
}
