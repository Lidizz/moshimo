# Final PROJECT_BRIEF.md - Ready for Production

Here's your **final, flexible, learning-focused version**. I've made key changes to emphasize experimentation, best practices, and Java mastery:

---

```markdown
# PROJECT OVERVIEW: Moshimo - Investment "What If" Simulator

> **Development Philosophy**: This is a learning journey. Nothing is final until we've experimented with alternatives. We prioritize clean architecture, best practices, and deep understanding of data structures and design patterns over quick wins.

---

## PURPOSE

Educational web/mobile app teaching financial literacy through historical investment simulation.
Users experience the power of long-term investing by seeing "what if I had invested $X in Y stock 
on [past date]?" with real historical market data.

**Core Philosophy**: Make investing concepts tangible and emotional - help parents teach kids, 
help individuals understand missed opportunities AND future potential.

---

## DEVELOPMENT PHASES

### Phase 1: LOCAL DEVELOPMENT (Current Focus)
- Run entire stack locally (zero hosting costs)
- Docker Compose for PostgreSQL + Spring Boot + React
- Localhost-only development and testing
- **Experimentation mode**: Try different approaches, libraries, patterns
- Focus: Build, iterate, learn, validate concept

### Phase 2: WEB DEPLOYMENT
- Deploy to free hosting tiers (options TBD after research)
- Custom domain + SSL
- Production-ready monitoring and analytics

### Phase 3: MOBILE APPS
- React Native or Progressive Web App (PWA) - decision pending
- Submit to Google Play Store and Apple App Store
- Native mobile experience with offline capabilities

**Current Priority**: Build locally, experiment freely, learn deeply, optimize later.

---

## TECH STACK

> ⚠️ **FLEXIBILITY NOTICE**: All technology choices below are initial selections open to change based on experimentation and learning. We will try alternatives before committing.

### Backend
- **Java 25 LTS** (leveraging modern features: virtual threads, records, pattern matching)
- **Spring Boot 3.3+** (REST API, scheduled jobs, security)
- **PostgreSQL 15+** (relational data, ACID compliance)
- **Docker** for containerized local development
- **Build Tool**: Maven (consider Gradle if preferred later)

**Learning Goals**:
- Master Java Collections Framework (List, Set, Map, Queue implementations)
- Deep dive into Stream API and functional programming patterns
- Understand concurrency (CompletableFuture, virtual threads)
- Practice design patterns: Strategy, Factory, Builder, Observer, Repository
- Learn Spring Boot internals: IoC, DI, AOP, transaction management

### Frontend
- **React 18+** with TypeScript (strict mode)
- **Build Tool**: Vite (fast development experience)
- **UI Framework**: TBD - experiment with options
- **Charts Library**: **EXPERIMENTAL** - try multiple before deciding:
  - TradingView Lightweight Charts (financial focus)
  - Recharts (React-native, declarative)
  - Chart.js with react-chartjs-2 (simple, flexible)
  - Victory (animation-focused)
  - D3.js (maximum control, steep learning curve)
- **State Management**: Start with React Context, evaluate Redux/Zustand if needed
- **Styling**: Start with Tailwind CSS, consider alternatives (CSS Modules, styled-components)

**Learning Goals**:
- TypeScript advanced types (generics, utility types, conditional types)
- React performance optimization (memo, useMemo, useCallback, code splitting)
- Custom hooks patterns and composition
- Responsive design principles and mobile-first approach

### Data Sources (Flexible - Evaluate Options)
- **Candidates to Test**:
  1. Yahoo Finance (unofficial, free, unlimited) - **START HERE**
  2. Alpha Vantage (500 calls/day free)
  3. Twelve Data (800 calls/day)
  4. Polygon.io (5 calls/min free)
  5. IEX Cloud (50k msgs/month)
  
- **Evaluation Criteria**: Reliability, data quality, rate limits, ease of integration
- **Strategy**: Build abstraction layer so we can swap providers easily

### Local Development Environment
```bash
# Docker Compose services:
- PostgreSQL 15 (port 5432)
- Spring Boot (port 8080)
- React dev server (port 5173)
- Optional: pgAdmin, Redis (for caching experiments)
```

### Future Deployment (Post-Local Validation)
**Options to Research**:
- Frontend: Vercel, Netlify, Cloudflare Pages
- Backend: Render, Railway, Fly.io, AWS EC2 free tier
- Database: Supabase, Neon, AWS RDS free tier, ElephantSQL

---

## PROJECT STRUCTURE

```
moshimo/
├── README.md
├── PROJECT_BRIEF.md (this file)
├── docker-compose.yml
├── .gitignore
├── docs/
│   ├── architecture/
│   │   ├── system-design.md
│   │   ├── database-design.md
│   │   └── api-contracts.md
│   ├── learning/
│   │   ├── java-patterns-used.md
│   │   ├── data-structures-guide.md
│   │   └── algorithm-decisions.md
│   └── decisions/
│       └── adr-001-tech-stack.md (Architecture Decision Records)
├── backend/
│   ├── pom.xml (or build.gradle)
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/moshimo/
│   │   │   │   ├── MoshimoApplication.java
│   │   │   │   ├── config/
│   │   │   │   │   ├── DatabaseConfig.java
│   │   │   │   │   ├── SecurityConfig.java
│   │   │   │   │   └── SchedulingConfig.java
│   │   │   │   ├── domain/
│   │   │   │   │   ├── model/           # Entities
│   │   │   │   │   │   ├── Stock.java
│   │   │   │   │   │   ├── StockPrice.java
│   │   │   │   │   │   ├── Portfolio.java
│   │   │   │   │   │   └── PortfolioItem.java
│   │   │   │   │   ├── repository/      # Data access
│   │   │   │   │   │   ├── StockRepository.java
│   │   │   │   │   │   ├── StockPriceRepository.java
│   │   │   │   │   │   └── PortfolioRepository.java
│   │   │   │   │   └── service/         # Business logic
│   │   │   │   │       ├── StockService.java
│   │   │   │   │       ├── StockDataService.java
│   │   │   │   │       ├── PortfolioSimulationService.java
│   │   │   │   │       └── CalculationService.java
│   │   │   │   ├── application/
│   │   │   │   │   ├── dto/             # Data Transfer Objects
│   │   │   │   │   │   ├── request/
│   │   │   │   │   │   │   ├── SimulationRequest.java
│   │   │   │   │   │   │   └── InvestmentItem.java
│   │   │   │   │   │   └── response/
│   │   │   │   │   │       ├── SimulationResponse.java
│   │   │   │   │   │       ├── StockDTO.java
│   │   │   │   │   │       └── PriceDataDTO.java
│   │   │   │   │   └── mapper/          # DTO ↔ Entity mapping
│   │   │   │   │       └── StockMapper.java
│   │   │   │   ├── infrastructure/
│   │   │   │   │   ├── api/             # External API clients
│   │   │   │   │   │   ├── StockDataProvider.java (interface)
│   │   │   │   │   │   ├── YahooFinanceClient.java
│   │   │   │   │   │   └── AlphaVantageClient.java
│   │   │   │   │   ├── cache/           # Caching strategy
│   │   │   │   │   │   └── PriceCache.java
│   │   │   │   │   └── scheduler/       # Background jobs
│   │   │   │   │       └── DailyPriceUpdateJob.java
│   │   │   │   ├── web/
│   │   │   │   │   ├── controller/      # REST endpoints
│   │   │   │   │   │   ├── StockController.java
│   │   │   │   │   │   ├── PortfolioController.java
│   │   │   │   │   │   └── HealthController.java
│   │   │   │   │   ├── exception/       # Global exception handling
│   │   │   │   │   │   ├── GlobalExceptionHandler.java
│   │   │   │   │   │   └── custom/
│   │   │   │   │   │       ├── StockNotFoundException.java
│   │   │   │   │   │       └── InvalidDateRangeException.java
│   │   │   │   │   └── validation/      # Custom validators
│   │   │   │   │       └── PastDateValidator.java
│   │   │   │   └── util/
│   │   │   │       ├── DateUtils.java
│   │   │   │       ├── FinancialCalculator.java
│   │   │   │       └── Constants.java
│   │   │   └── resources/
│   │   │       ├── application.yml
│   │   │       ├── application-dev.yml
│   │   │       ├── application-prod.yml
│   │   │       └── db/
│   │   │           └── migration/       # Flyway migrations
│   │   │               ├── V1__create_stocks_table.sql
│   │   │               ├── V2__create_stock_prices_table.sql
│   │   │               └── V3__create_portfolio_tables.sql
│   │   └── test/
│   │       └── java/com/moshimo/
│   │           ├── unit/                # Unit tests (fast, isolated)
│   │           │   ├── service/
│   │           │   └── util/
│   │           ├── integration/         # Integration tests (DB, API)
│   │           │   ├── repository/
│   │           │   └── controller/
│   │           └── performance/         # Load/stress tests
│   │               └── SimulationPerformanceTest.java
│   └── README.md
├── frontend/
│   ├── package.json
│   ├── tsconfig.json
│   ├── vite.config.ts
│   ├── index.html
│   ├── public/
│   │   ├── favicon.ico
│   │   └── assets/
│   ├── src/
│   │   ├── main.tsx
│   │   ├── App.tsx
│   │   ├── pages/
│   │   │   ├── Home/
│   │   │   │   ├── HomePage.tsx
│   │   │   │   ├── HeroSection.tsx
│   │   │   │   └── DemoSimulation.tsx
│   │   │   ├── Simulator/
│   │   │   │   ├── SimulatorPage.tsx
│   │   │   │   └── SimulatorPage.module.css (or .scss)
│   │   │   ├── About/
│   │   │   │   └── AboutPage.tsx
│   │   │   └── HowItWorks/
│   │   │       └── HowItWorksPage.tsx
│   │   ├── components/
│   │   │   ├── common/
│   │   │   │   ├── Button/
│   │   │   │   │   ├── Button.tsx
│   │   │   │   │   ├── Button.test.tsx
│   │   │   │   │   └── Button.module.css
│   │   │   │   ├── Input/
│   │   │   │   ├── DatePicker/
│   │   │   │   ├── Select/
│   │   │   │   └── LoadingSpinner/
│   │   │   ├── layout/
│   │   │   │   ├── Header/
│   │   │   │   │   └── Header.tsx
│   │   │   │   ├── Footer/
│   │   │   │   └── MobileNav/
│   │   │   ├── StockSelector/
│   │   │   │   ├── StockSelector.tsx
│   │   │   │   ├── StockCard.tsx
│   │   │   │   ├── StockSearchInput.tsx
│   │   │   │   └── StockSelector.test.tsx
│   │   │   ├── InvestmentBuilder/
│   │   │   │   ├── InvestmentForm.tsx
│   │   │   │   ├── InvestmentList.tsx
│   │   │   │   ├── InvestmentItem.tsx
│   │   │   │   └── InvestmentBuilder.test.tsx
│   │   │   ├── Charts/
│   │   │   │   ├── PortfolioChart.tsx           # Main chart component
│   │   │   │   ├── ChartControls.tsx            # Zoom, timeframe, split view
│   │   │   │   ├── adapters/                    # Chart library adapters (swap easily)
│   │   │   │   │   ├── LightweightChartsAdapter.tsx
│   │   │   │   │   ├── RechartsAdapter.tsx
│   │   │   │   │   └── ChartJsAdapter.tsx
│   │   │   │   └── Charts.test.tsx
│   │   │   ├── Results/
│   │   │   │   ├── MetricsPanel.tsx
│   │   │   │   ├── MetricCard.tsx
│   │   │   │   ├── BreakdownTable.tsx
│   │   │   │   ├── Insights.tsx
│   │   │   │   └── ComparisonChart.tsx          # vs S&P 500
│   │   │   └── Education/
│   │   │       ├── Tooltip.tsx
│   │   │       └── InfoModal.tsx
│   │   ├── hooks/
│   │   │   ├── useStockData.ts                  # Fetch stocks list
│   │   │   ├── useStockPrices.ts                # Fetch price history
│   │   │   ├── usePortfolioSimulation.ts        # Calculate returns
│   │   │   ├── useDebounce.ts                   # Debounce inputs
│   │   │   ├── useLocalStorage.ts               # Persist selections
│   │   │   └── useMediaQuery.ts                 # Responsive helpers
│   │   ├── services/
│   │   │   ├── api/
│   │   │   │   ├── apiClient.ts                 # Axios/Fetch config
│   │   │   │   ├── stockApi.ts                  # Stock endpoints
│   │   │   │   └── portfolioApi.ts              # Portfolio endpoints
│   │   │   ├── calculator/
│   │   │   │   ├── portfolioCalculator.ts       # Returns, CAGR, etc.
│   │   │   │   └── financialMetrics.ts          # Sharpe, volatility
│   │   │   └── formatters/
│   │   │       ├── currencyFormatter.ts
│   │   │       ├── dateFormatter.ts
│   │   │       └── percentageFormatter.ts
│   │   ├── context/
│   │   │   ├── PortfolioContext.tsx             # Global portfolio state
│   │   │   └── ThemeContext.tsx                 # Dark/light mode (future)
│   │   ├── types/
│   │   │   ├── stock.types.ts
│   │   │   ├── portfolio.types.ts
│   │   │   ├── chart.types.ts
│   │   │   └── api.types.ts
│   │   ├── utils/
│   │   │   ├── dateHelpers.ts
│   │   │   ├── validation.ts
│   │   │   ├── arrayHelpers.ts
│   │   │   └── constants.ts
│   │   ├── styles/
│   │   │   ├── global.css
│   │   │   ├── variables.css                    # CSS custom properties
│   │   │   └── themes/
│   │   │       ├── light.css
│   │   │       └── dark.css
│   │   └── assets/
│   │       ├── images/
│   │       └── icons/
│   └── README.md
├── scripts/
│   ├── seed-database.sh                         # Initial data load
│   ├── backup-database.sh
│   └── generate-test-data.py                    # Mock data for testing
└── .github/
    └── workflows/
        ├── backend-tests.yml
        └── frontend-tests.yml
```

---

## CORE FEATURES (MVP)

### Mode 1: Historical Retrospective (PRIMARY FEATURE)

1. **Stock Selector**: Multi-select dropdown
   - Start with 20 popular stocks: SPY, QQQ, GOOGL, AAPL, MSFT, NVDA, TSLA, AMZN, META, BRK.B, JPM, V, WMT, JNJ, PG, XOM, UNH, MA, HD, CVX
   - Display: Symbol, company name, sector/category
   - Search/filter functionality
   - **Java Learning**: Use `HashMap` for O(1) lookups, `TreeSet` for sorted display
   
2. **Investment Builder**:
   - Amount input (USD, default suggestions: $100, $500, $1000, $5000)
   - Date picker (any past date to today, default: 10 years ago)
   - Multi-stock portfolio builder (add/remove investments)
   - **Java Learning**: `ArrayList` vs `LinkedList` trade-offs, `PriorityQueue` for date sorting

3. **Visualization**:
   - Combined portfolio growth chart (total value over time)
   - Split view toggle: individual stock performance lines
   - Interactive tooltips: hover for exact values
   - Key metrics: Total invested, Current value, Gain/Loss, % return, CAGR
   - **Java Learning**: Stream API for data transformation, `Optional` for null safety

4. **Results Breakdown**:
   - Per-stock performance table
   - Best/worst performer highlights
   - Comparison against S&P 500 benchmark
   - Educational insights
   - **Java Learning**: Comparator chains, custom sorting algorithms

### Mode 2: Future Projection (PHASE 2 - Post-MVP)
- "What if I invest $X/month for Y years?"
- Monte Carlo simulation
- Clear disclaimers about projections

---

## DATA REQUIREMENTS & ALGORITHMS

### Historical Stock Data
- **Granularity**: Daily OHLCV (Open, High, Low, Close, Volume)
- **Timeframe**: Minimum 20 years per stock (~5,000 trading days)
- **Storage**: ~100,000 records for 20 stocks = ~50MB
- **Update**: Daily batch job after market close (4:30 PM ET)

**Algorithm Considerations**:
- **Binary Search**: Fast date lookups in sorted price arrays
- **Interpolation**: Handle missing data (holidays, stock splits)
- **Time Complexity**: O(log n) for date queries, O(n) for range scans
- **Space Complexity**: O(n) for in-memory caching, balance memory vs DB hits

### API Integration Strategy
```
Phase 1: Bulk Historical Load
- Fetch 20 years × 20 stocks = 100k API calls (one-time)
- Rate limiting: Batch requests, exponential backoff
- Data validation: Check for outliers, missing dates
- Concurrency: Use Java virtual threads for parallel fetching

Phase 2: Daily Updates
- Scheduled job: 5 PM ET daily
- Fetch only new trading day data (20 stocks = 20 calls)
- Idempotency: Safe to run multiple times
- Monitoring: Alert on failed updates

Phase 3: Real-time (Future)
- WebSocket connection for live prices
- Event-driven architecture
```

**Java Learning Goals**:
- CompletableFuture for async operations
- ExecutorService with virtual threads
- Thread-safe collections (ConcurrentHashMap)
- Lock-free algorithms where possible

---

## DATABASE SCHEMA

### Core Tables

```sql
-- Stocks Master Table
CREATE TABLE stocks (
    id BIGSERIAL PRIMARY KEY,
    symbol VARCHAR(10) UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL,
    sector VARCHAR(100),
    industry VARCHAR(100),
    exchange VARCHAR(50),
    ipo_date DATE,                              -- For validation (no dates before IPO)
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Historical Price Data (Largest Table - Query Optimization Critical)
CREATE TABLE stock_prices (
    id BIGSERIAL PRIMARY KEY,
    stock_id BIGINT NOT NULL REFERENCES stocks(id) ON DELETE CASCADE,
    date DATE NOT NULL,
    open NUMERIC(12, 4) NOT NULL,
    high NUMERIC(12, 4) NOT NULL,
    low NUMERIC(12, 4) NOT NULL,
    close NUMERIC(12, 4) NOT NULL,              -- Adjusted for splits/dividends
    volume BIGINT,
    adjusted_close NUMERIC(12, 4),              -- For accurate returns calculation
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(stock_id, date)
);

-- Performance Indexes (CRITICAL - Profile with EXPLAIN ANALYZE)
CREATE INDEX idx_stock_prices_stock_date ON stock_prices(stock_id, date DESC);
CREATE INDEX idx_stock_prices_date ON stock_prices(date);
CREATE INDEX idx_stocks_symbol ON stocks(symbol);
CREATE INDEX idx_stocks_sector ON stocks(sector);         -- For future sector analysis

-- User Portfolios (Phase 2)
CREATE TABLE portfolios (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT,                             -- NULL for anonymous sessions
    name VARCHAR(255) DEFAULT 'My Portfolio',
    description TEXT,
    is_public BOOLEAN DEFAULT false,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Portfolio Items (Many-to-Many: Portfolio ↔ Stocks)
CREATE TABLE portfolio_items (
    id BIGSERIAL PRIMARY KEY,
    portfolio_id BIGINT NOT NULL REFERENCES portfolios(id) ON DELETE CASCADE,
    stock_id BIGINT NOT NULL REFERENCES stocks(id),
    amount_usd NUMERIC(12, 2) NOT NULL CHECK (amount_usd > 0),
    purchase_date DATE NOT NULL,
    shares NUMERIC(20, 8),                      -- Calculated: amount_usd / price_on_date
    purchase_price NUMERIC(12, 4),              -- Snapshot of price on purchase_date
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT valid_purchase_date CHECK (purchase_date <= CURRENT_DATE)
);

CREATE INDEX idx_portfolio_items_portfolio ON portfolio_items(portfolio_id);
CREATE INDEX idx_portfolio_items_stock ON portfolio_items(stock_id);

-- Audit Table (Optional - Track data changes)
CREATE TABLE price_updates_log (
    id BIGSERIAL PRIMARY KEY,
    update_date DATE NOT NULL,
    stocks_updated INTEGER,
    success BOOLEAN,
    error_message TEXT,
    execution_time_ms INTEGER,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

**Database Learning Goals**:
- Normalization (1NF, 2NF, 3NF) vs denormalization trade-offs
- Index strategies: B-Tree vs Hash vs GiST
- Query optimization with EXPLAIN ANALYZE
- Transaction isolation levels
- Connection pooling (HikariCP configuration)

---

## API ENDPOINTS

### Stock Data Endpoints

```http
GET /api/stocks
    Description: List all available stocks
    Query Params: 
        - sector (optional): Filter by sector
        - active (optional): true/false, default: true
    Response: [
        {
            "id": 1,
            "symbol": "AAPL",
            "name": "Apple Inc.",
            "sector": "Technology",
            "exchange": "NASDAQ",
            "ipoDate": "1980-12-12"
        }
    ]
    Java Pattern: Repository Pattern, DTO Mapping

GET /api/stocks/{symbol}
    Description: Get stock details by symbol
    Path Param: symbol (e.g., "AAPL")
    Response: { /* Stock object */ }
    Error: 404 if not found
    Java Pattern: Optional<T>, Custom exception handling

GET /api/stocks/{symbol}/prices?from=2015-01-01&to=2024-12-31
    Description: Historical price data for date range
    Path Param: symbol
    Query Params:
        - from (required): Start date (YYYY-MM-DD)
        - to (required): End date (YYYY-MM-DD)
        - interval (optional): daily/weekly/monthly, default: daily
    Response: [
        {
            "date": "2015-01-01",
            "open": 109.33,
            "high": 111.44,
            "low": 107.35,
            "close": 110.38,
            "volume": 53204626,
            "adjustedClose": 110.38
        }
    ]
    Validation: from < to, both <= today, date after IPO
    Java Pattern: Stream API, Date validation, Pagination (future)

GET /api/stocks/search?query=apple&limit=10
    Description: Search stocks by name or symbol (autocomplete)
    Query Params:
        - query (required): Search term
        - limit (optional): Max results, default: 10
    Response: [ /* Array of matching stocks */ ]
    Java Pattern: Full-text search, Levenshtein distance (fuzzy matching)
```

### Portfolio Simulation Endpoints

```http
POST /api/portfolio/simulate
    Description: Calculate "what if" investment scenario
    Request Body: {
        "investments": [
            {
                "symbol": "AAPL",
                "amountUsd": 1000,
                "purchaseDate": "2015-01-01"
            },
            {
                "symbol": "GOOGL",
                "amountUsd": 2000,
                "purchaseDate": "2016-06-15"
            }
        ],
        "endDate": "2024-12-31"  // Optional, default: today
    }
    
    Response: {
        "totalInvested": 3000.00,
        "currentValue": 15420.50,
        "absoluteGain": 12420.50,
        "percentReturn": 414.02,
        "cagr": 18.5,                           // Compound Annual Growth Rate
        "timeline": [
            { "date": "2015-01-01", "value": 1000.00 },
            { "date": "2015-01-02", "value": 1005.32 },
            // ... daily values (can be large, consider pagination/sampling)
        ],
        "holdings": [
            {
                "symbol": "AAPL",
                "invested": 1000.00,
                "currentValue": 8500.00,
                "shares": 9.06,
                "purchasePrice": 110.38,
                "currentPrice": 938.12,
                "absoluteGain": 7500.00,
                "percentReturn": 750.00
            }
        ],
        "benchmarkComparison": {
            "benchmark": "SPY",                 // S&P 500 ETF
            "portfolioReturn": 414.02,
            "benchmarkReturn": 220.15,
            "outperformance": 193.87
        }
    }
    
    Validation:
        - Max 10 investments per request (performance limit)
        - All symbols must exist
        - All dates must be valid (after IPO, before today)
        - Amounts > 0 and < 1,000,000
    
    Java Learning:
        - Stream API for complex calculations
        - BigDecimal for precise financial math
        - Builder pattern for response construction
        - Parallel streams for performance (be careful!)

POST /api/portfolio/save
    Description: Save portfolio for sharing (Phase 2)
    Request: { /* Simulation data */ }
    Response: {
        "portfolioId": 123,
        "shareUrl": "/portfolio/123"
    }

GET /api/portfolio/{id}
    Description: Retrieve saved portfolio (Phase 2)
    Path Param: id
    Response: { /* Full portfolio details */ }
```

### System & Health Endpoints

```http
GET /api/health
    Description: Application health check
    Response: {
        "status": "UP",
        "components": {
            "database": "UP",
            "diskSpace": "UP"
        },
        "lastDataUpdate": "2024-12-31T21:00:00Z",
        "version": "1.0.0"
    }
    Java: Spring Boot Actuator

GET /api/stats
    Description: System statistics
    Response: {
        "totalStocks": 20,
        "totalPriceRecords": 105432,
        "oldestData": "2004-01-02",
        "latestData": "2024-12-31",
        "databaseSize": "52.3 MB"
    }

GET /api/admin/refresh-data
    Description: Manually trigger data refresh (admin only, Phase 2)
    Authorization: Required
    Response: { "status": "started", "jobId": "abc123" }
```

---

## KEY TECHNICAL CONSIDERATIONS

### Performance Optimization

**Backend (Java)**:
- **Collections Choice**:
  - `ArrayList` for sequential access, random access
  - `LinkedList` for frequent insertions/deletions
  - `HashMap` for O(1) lookups
  - `TreeMap` for sorted keys (date ranges)
  - `ConcurrentHashMap` for thread-safe caching
  
- **Algorithm Complexity**:
  - Price lookups: O(log n) with binary search
  - Portfolio calculations: O(n × m) where n = stocks, m = days
  - Optimize: Cache intermediate results, use parallel streams judiciously

- **Memory Management**:
  - Lazy loading: Don't load all prices upfront
  - Pagination: Limit timeline data points (e.g., sample weekly for 20-year range)
  - Connection pooling: HikariCP default settings usually sufficient

- **Caching Strategy**:
  - L1: In-memory cache (Caffeine or Spring Cache)
  - L2: Redis (future, for distributed systems)
  - TTL: Historical data = indefinite, latest data = 24 hours

**Frontend (React)**:
- **Component Optimization**:
  - `React.memo` for expensive components
  - `useMemo` for calculations (CAGR, returns)
  - `useCallback` for stable function references
  - Code splitting: Lazy load chart libraries

- **Data Handling**:
  - Debounce user inputs (300ms)
  - Virtualize large lists (react-window)
  - Paginate timeline data (sample 500 points max for charts)

- **Bundle Size**:
  - Tree-shaking: Import only what you need
  - Dynamic imports for routes
  - Compress assets (gzip/brotli)
  - Target: < 500KB initial bundle

### Mobile-First Design

- **Touch Targets**: Minimum 44×44px (iOS), 48×48px (Android)
- **Gestures**: Pinch-to-zoom on charts, swipe navigation
- **Responsive Breakpoints**:

  ```css
  /* Mobile-first approach */
  /* Base styles for mobile (< 640px) */
  
  @media (min-width: 640px) { /* Tablet */ }
  @media (min-width: 1024px) { /* Desktop */ }
  @media (min-width: 1280px) { /* Large desktop */ }
  ```
- **Performance**: Lazy load images, reduce animations on low-end devices
- **Testing**: Chrome DevTools mobile emulation, real device testing

### Error Handling Philosophy

**Backend**:
```java
// Global exception handling with @ControllerAdvice
@ControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(StockNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleStockNotFound(StockNotFoundException ex) {
        return ResponseEntity.status(404).body(
            new ErrorResponse("STOCK_NOT_FOUND", ex.getMessage())
        );
    }
    
    @ExceptionHandler(InvalidDateRangeException.class)
    public ResponseEntity<ErrorResponse> handleInvalidDateRange(InvalidDateRangeException ex) {
        return ResponseEntity.status(400).body(
            new ErrorResponse("INVALID_DATE_RANGE", ex.getMessage())
        );
    }
    
    // Generic fallback
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericError(Exception ex) {
        // Log full stack trace
        logger.error("Unexpected error", ex);
        // Return safe message to client
        return ResponseEntity.status(500).body(
            new ErrorResponse("INTERNAL_ERROR", "Something went wrong. Please try again.")
        );
    }
}
```

**Frontend**:
```typescript
// Error boundary component
class ErrorBoundary extends React.Component {
    // Catch React render errors
}

// API error handling
try {
    const data = await fetchStockPrices(symbol, from, to);
} catch (error) {
    if (error.status === 429) {
        // Rate limit: Show friendly message, retry after delay
        showToast("We're receiving too many requests. Please wait a moment.");
    } else if (error.status === 404) {
        // Not found: Suggest alternatives
        showToast(`Stock ${symbol} not found. Did you mean ${suggestions}?`);
    } else if (error.status >= 500) {
        // Server error: Retry with exponential backoff
        retryWithBackoff(() => fetchStockPrices(symbol, from, to));
    } else {
        // Generic: Fallback UI
        showErrorState("Unable to load data. Please try again.");
    }
}
```

### Accessibility (WCAG 2.1 AA)

- **Keyboard Navigation**: All interactive elements via Tab/Enter/Space
- **Screen Readers**: 
  - Semantic HTML (`<nav>`, `<main>`, `<article>`)
  - ARIA labels for dynamic content
  - Announce chart data changes
- **Color Contrast**: 4.5:1 minimum for text
- **Focus Management**: Visible outlines, focus trapping in modals
- **Alternative Content**: Describe chart trends in text

### Data Integrity & Validation

**Backend Validation**:
```java
@PostMapping("/simulate")
public ResponseEntity<SimulationResponse> simulate(
    @Valid @RequestBody SimulationRequest request
) {
    // Spring validation with @Valid
}

// Custom validators
public class SimulationRequest {
    @NotEmpty(message = "At least one investment required")
    @Size(max = 10, message = "Maximum 10 investments per simulation")
    private List<InvestmentItem> investments;
}

public class InvestmentItem {
    @NotBlank(message = "Symbol required")
    @Pattern(regexp = "^[A-Z]{1,5}$", message = "Invalid symbol format")
    private String symbol;
    
    @Positive(message = "Amount must be positive")
    @Max(value = 1_000_000, message = "Amount exceeds maximum")
    private BigDecimal amountUsd;
    
    @PastOrPresent(message = "Purchase date cannot be in future")
    private LocalDate purchaseDate;
}

// Business logic validation
public void validatePurchaseDate(String symbol, LocalDate purchaseDate) {
    Stock stock = stockRepository.findBySymbol(symbol)
        .orElseThrow(() -> new StockNotFoundException(symbol));
    
    if (purchaseDate.isBefore(stock.getIpoDate())) {
        throw new InvalidDateRangeException(
            "Cannot invest before IPO date: " + stock.getIpoDate()
        );
    }
}
```

**Frontend Validation**:
```typescript
// Form validation with Zod or Yup
const investmentSchema = z.object({
    symbol: z.string().min(1, "Symbol required").max(5),
    amountUsd: z.number().positive().max(1_000_000),
    purchaseDate: z.date().max(new Date(), "Future dates not allowed")
});

// Real-time validation feedback
const [errors, setErrors] = useState({});
const validateField = (field, value) => {
    try {
        investmentSchema.pick({ [field]: true }).parse({ [field]: value });
        setErrors(prev => ({ ...prev, [field]: null }));
    } catch (e) {
        setErrors(prev => ({ ...prev, [field]: e.message }));
    }
};
```

---

## JAVA LEARNING OBJECTIVES

### Data Structures to Master

1. **Lists**:
   - `ArrayList`: Dynamic array, O(1) random access, O(n) insertion/deletion
   - `LinkedList`: Doubly-linked list, O(1) insertion/deletion at ends, O(n) access
   - **Use Case**: Price data (ArrayList for sequential processing)

2. **Sets**:
   - `HashSet`: O(1) add/contains, no order
   - `TreeSet`: O(log n) add/contains, sorted order
   - `LinkedHashSet`: Insertion order preserved
   - **Use Case**: Unique stock symbols, sorted sector lists

3. **Maps**:
   - `HashMap`: O(1) get/put, no order
   - `TreeMap`: O(log n) get/put, sorted by keys
   - `LinkedHashMap`: Insertion order preserved
   - `ConcurrentHashMap`: Thread-safe, lock-free reads
   - **Use Case**: Symbol → Stock lookup, Date → Price mapping

4. **Queues**:
   - `PriorityQueue`: Min-heap, O(log n) insertion/removal
   - `Deque` (ArrayDeque): Double-ended queue
   - **Use Case**: Task scheduling, recent searches

5. **Specialized**:
   - `EnumSet`/`EnumMap`: For enum keys (memory efficient)
   - `BitSet`: Compact boolean arrays
   - **Use Case**: Stock categories, feature flags

### Design Patterns to Implement

1. **Creational**:
   - **Singleton**: Database connection pool
   - **Factory**: Stock data provider (Yahoo vs Alpha Vantage)
   - **Builder**: Complex DTO construction
   - **Prototype**: Clone portfolio configurations

2. **Structural**:
   - **Adapter**: Wrap different API clients with common interface
   - **Facade**: Simplify complex subsystem (financial calculations)
   - **Decorator**: Add caching to repository methods
   - **Proxy**: Lazy-load expensive stock data

3. **Behavioral**:
   - **Strategy**: Different calculation algorithms (simple vs time-weighted returns)
   - **Observer**: Notify UI of data updates (WebSocket future)
   - **Template Method**: Define skeleton of data fetching, subclasses fill details
   - **Command**: Queue API requests for rate limiting

4. **Architectural**:
   - **Repository**: Data access abstraction
   - **Service Layer**: Business logic separation
   - **DTO**: Data transfer between layers
   - **MVC**: Model-View-Controller (Spring Boot default)

### Algorithms to Practice

1. **Searching**:
   - **Binary Search**: Find price on specific date in sorted array
   - **Interpolation Search**: Optimize for evenly distributed dates
   - **Complexity**: O(log n) vs O(log log n)

2. **Sorting**:
   - **TimSort** (Java default): Hybrid merge/insertion sort
   - **Custom Comparators**: Sort stocks by performance, sector, etc.
   - **Complexity**: O(n log n)

3. **Financial Algorithms**:
   - **CAGR Calculation**: `(endValue / beginValue)^(1/years) - 1`
   - **Returns Calculation**: `(currentValue - invested) / invested × 100`
   - **Time-Weighted Return**: Handle multiple cash flows
   - **Sharpe Ratio**: (Return - RiskFreeRate) / StandardDeviation

4. **Date Algorithms**:
   - **Trading Days Calculation**: Skip weekends, holidays
   - **Date Interpolation**: Fill missing data points
   - **Timezone Handling**: Market close times

5. **Optimization**:
   - **Caching**: Memoization of expensive calculations
   - **Batch Processing**: Bulk API requests
   - **Parallel Processing**: Multi-threaded data fetching (virtual threads)

### Code Quality Practices

- **SOLID Principles**:
  - **S**ingle Responsibility: Each class has one job
  - **O**pen/Closed: Open for extension, closed for modification
  - **L**iskov Substitution: Subtypes must be substitutable
  - **I**nterface Segregation: Many specific interfaces > one general
  - **D**ependency Inversion: Depend on abstractions, not concretions

- **Clean Code**:
  - Meaningful names (avoid `data`, `temp`, `obj`)
  - Small functions (< 20 lines ideally)
  - Avoid deep nesting (max 2-3 levels)
  - DRY: Don't Repeat Yourself
  - Comments explain "why", not "what"

- **Testing**:
  - Unit tests: Test business logic in isolation
  - Integration tests: Test with real database
  - Test coverage: Aim for 80%+ on service layer
  - Test-Driven Development (TDD): Write test first, then code

---

## EXPERIMENTATION FRAMEWORK

> **Philosophy**: We don't know what works best until we try it. Build abstractions that allow swapping implementations easily.

### Chart Library Experimentation

Create an adapter pattern to swap chart libraries:

```typescript
// Abstract interface
interface ChartAdapter {
    initialize(container: HTMLElement, data: ChartData): void;
    update(data: ChartData): void;
    destroy(): void;
    setSplitView(enabled: boolean): void;
}

// Implementations
class LightweightChartsAdapter implements ChartAdapter { /* ... */ }
class RechartsAdapter implements ChartAdapter { /* ... */ }
class ChartJsAdapter implements ChartAdapter { /* ... */ }

// Usage (easy to swap)
const chartAdapter = new LightweightChartsAdapter();  // Change this line only
chartAdapter.initialize(containerRef.current, portfolioData);
```

### API Provider Experimentation

```java
// Interface
public interface StockDataProvider {
    List<StockPrice> fetchHistoricalPrices(String symbol, LocalDate from, LocalDate to);
    StockPrice fetchLatestPrice(String symbol);
}

// Implementations
@Service
@Profile("yahoo")
public class YahooFinanceProvider implements StockDataProvider { /* ... */ }

@Service
@Profile("alphavantage")
public class AlphaVantageProvider implements StockDataProvider { /* ... */ }

// Easy switching via application.yml:
# spring.profiles.active: yahoo
```

### UI Framework Experimentation (Future)

If we want to try alternatives to React:
- Keep business logic in separate TypeScript modules
- UI layer is just a thin wrapper
- Can port to Svelte, Vue, Solid.js with minimal rewrite

### Database Experimentation (Future)

- Start with PostgreSQL
- If needed, add Redis for caching (non-critical path)
- If scaling issues, consider TimescaleDB (PostgreSQL extension for time-series)
- Abstract repositories allow swapping implementations

---

## DOCUMENTATION STRATEGY

### Code Documentation

**Backend (JavaDoc)**:
```java
/**
 * Calculates the Compound Annual Growth Rate (CAGR) for an investment.
 * 
 * Formula: CAGR = (EndValue / BeginValue)^(1 / Years) - 1
 * 
 * @param beginValue initial investment amount (must be > 0)
 * @param endValue final investment value (must be > 0)
 * @param years investment period in years (must be > 0)
 * @return CAGR as a percentage (e.g., 15.5 for 15.5%)
 * @throws IllegalArgumentException if any parameter is <= 0
 * 
 * @see <a href="https://www.investopedia.com/terms/c/cagr.asp">CAGR Definition</a>
 */
public double calculateCAGR(double beginValue, double endValue, double years) {
    // Implementation
}
```

**Frontend (TSDoc)**:
```typescript
/**
 * Custom hook to fetch and cache stock price data.
 * 
 * Handles loading states, error handling, and automatic retry with exponential backoff.
 * Caches results in memory to avoid redundant API calls.
 * 
 * @param symbol - Stock ticker symbol (e.g., "AAPL")
 * @param fromDate - Start date for price range
 * @param toDate - End date for price range
 * 
 * @returns Object containing:
 *   - data: Array of price data points
 *   - isLoading: Boolean indicating fetch status
 *   - error: Error object if fetch failed
 *   - refetch: Function to manually trigger re-fetch
 * 
 * @example
 * ```tsx
 * const { data, isLoading, error } = useStockPrices("AAPL", "2020-01-01", "2024-12-31");
 * ```
 */
export function useStockPrices(symbol: string, fromDate: string, toDate: string) {
    // Implementation
}
```

### Architecture Decision Records (ADRs)

Document major decisions in `docs/decisions/`:

```markdown
# ADR-001: Use PostgreSQL over MongoDB

Date: 2025-01-01
Status: Accepted

## Context
Need to choose primary database for storing stock prices and portfolios.

## Decision
Use PostgreSQL over MongoDB.

## Rationale
- Stock prices are structured, relational data (perfect for SQL)
- ACID compliance critical for financial data
- Native support for date ranges, indexes
- Better query performance for time-series data
- Easier to learn SQL than NoSQL for beginners

## Consequences
Positive:
- Strong consistency guarantees
- Powerful querying with SQL
- Excellent tooling (pgAdmin, DBeaver)

Negative:
- Slightly more complex schema design vs document store
- Vertical scaling limits (but sufficient for our scale)

## Alternatives Considered
- MongoDB: Too flexible, no strict schema
- MySQL: Similar to PostgreSQL, but weaker JSON support
- TimescaleDB: Overkill for MVP, consider for Phase 2
```

### Learning Journal

Keep `docs/learning/` directory:
- `java-patterns-used.md`: List every pattern implemented with examples
- `data-structures-guide.md`: When to use ArrayList vs LinkedList, etc.
- `algorithm-decisions.md`: Why we chose binary search over linear, etc.
- `performance-tuning.md`: Benchmarks, optimizations applied

---

## TESTING STRATEGY

### Backend Testing

**Unit Tests** (Fast, isolated):
```java
@ExtendWith(MockitoExtension.class)
class PortfolioCalculatorTest {
    
    @Test
    void calculateCAGR_validInputs_returnsCorrectValue() {
        // Given
        double beginValue = 1000.0;
        double endValue = 2000.0;
        double years = 5.0;
        
        // When
        double cagr = calculator.calculateCAGR(beginValue, endValue, years);
        
        // Then
        assertEquals(14.87, cagr, 0.01);  // Allow 0.01 delta for floating point
    }
    
    @Test
    void calculateCAGR_zeroBeginValue_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> 
            calculator.calculateCAGR(0, 2000, 5)
        );
    }
}
```

**Integration Tests** (Slower, test with real DB):
```java
@SpringBootTest
@AutoConfigureTestDatabase(replace = Replace.NONE)  // Use real DB
@Testcontainers  // Spin up PostgreSQL container
class StockRepositoryIntegrationTest {
    
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");
    
    @Autowired
    private StockRepository repository;
    
    @Test
    void findBySymbol_existingStock_returnsStock() {
        // Given
        Stock apple = new Stock("AAPL", "Apple Inc.", "Technology");
        repository.save(apple);
        
        // When
        Optional<Stock> result = repository.findBySymbol("AAPL");
        
        // Then
        assertTrue(result.isPresent());
        assertEquals("Apple Inc.", result.get().getName());
    }
}
```

**API Tests** (Test endpoints):
```java
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class StockControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    void getStocks_returnsStockList() throws Exception {
        mockMvc.perform(get("/api/stocks"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$[0].symbol").exists());
    }
}
```

### Frontend Testing

**Unit Tests** (Utilities, hooks):
```typescript
import { renderHook } from '@testing-library/react';
import { useStockPrices } from './useStockPrices';

test('useStockPrices returns loading state initially', () => {
    const { result } = renderHook(() => useStockPrices('AAPL', '2020-01-01', '2024-12-31'));
    expect(result.current.isLoading).toBe(true);
});
```

**Component Tests** (React Testing Library):
```typescript
import { render, screen, fireEvent } from '@testing-library/react';
import StockSelector from './StockSelector';

test('selects stock when clicked', () => {
    const onSelect = jest.fn();
    render(<StockSelector stocks={mockStocks} onSelect={onSelect} />);
    
    fireEvent.click(screen.getByText('Apple Inc.'));
    
    expect(onSelect).toHaveBeenCalledWith(mockStocks[0]);
});
```

### Manual Testing Checklist

Before each release:
- [ ] Portfolio with 1 stock works
- [ ] Portfolio with 10 stocks works
- [ ] Split view toggle works smoothly
- [ ] Chart zooming/panning feels responsive
- [ ] Mobile responsive on real iPhone/Android
- [ ] Error messages are user-friendly
- [ ] Performance with 20-year range < 3 seconds
- [ ] Keyboard navigation works
- [ ] Screen reader announces chart changes

---

## LOCAL DEVELOPMENT SETUP

### Prerequisites

```bash
# Install Java 25
sdk install java 25-tem

# Install Node.js 20
nvm install 20
nvm use 20

# Install Docker Desktop (or Docker Engine + Docker Compose)
# https://docs.docker.com/get-docker/

# Verify installations
java --version    # Should show 25.x
node --version    # Should show 20.x
docker --version  # Should show 20.x+
```
```

### Initial Setup

```bash
# 1. Clone repository
git clone https://github.com/yourusername/moshimo.git
cd moshimo

# 2. Start PostgreSQL with Docker
docker-compose up -d

# Wait for DB to be ready (check with docker-compose logs -f postgres)

# 3. Backend setup
cd backend
./mvnw clean install       # Download dependencies, run tests
./mvnw spring-boot:run     # Start server on http://localhost:8080

# 4. Frontend setup (new terminal)
cd frontend
npm install                # Download dependencies
npm run dev                # Start dev server on http://localhost:5173

# 5. Seed initial data (optional)
cd scripts
./seed-database.sh         # Populate with 20 stocks + sample prices
```

### Docker Compose Configuration

```yaml
# docker-compose.yml
version: '3.8'

services:
  postgres:
    image: postgres:15-alpine
    container_name: moshimo_postgres
    environment:
      POSTGRES_DB: moshimo_dev
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: dev_password_123
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
      - ./scripts/init.sql:/docker-entrypoint-initdb.d/init.sql  # Run on first start
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U postgres"]
      interval: 10s
      timeout: 5s
      retries: 5

  # Optional: pgAdmin for database management
  pgadmin:
    image: dpage/pgadmin4:latest
    container_name: moshimo_pgadmin
    environment:
      PGADMIN_DEFAULT_EMAIL: admin@moshimo.local
      PGADMIN_DEFAULT_PASSWORD: admin
    ports:
      - "5050:80"
    depends_on:
      - postgres

volumes:
  postgres_data:
    driver: local
```

### Development Workflow

```bash
# Start everything
docker-compose up -d && cd backend && ./mvnw spring-boot:run &
cd frontend && npm run dev

# Run tests
cd backend && ./mvnw test           # Backend unit tests
cd frontend && npm run test         # Frontend unit tests

# Database management
docker exec -it moshimo_postgres psql -U postgres -d moshimo_dev
# Or use pgAdmin at http://localhost:5050

# Stop everything
docker-compose down               # Keeps data
docker-compose down -v            # Deletes data (fresh start)
```

---

## GIT WORKFLOW

```
main                → Production-ready code (protected branch)
  └── develop       → Integration branch (default branch)
      ├── feature/stock-selector      → New features
      ├── feature/portfolio-chart
      ├── bugfix/date-validation      → Bug fixes
      └── refactor/calculation-logic  → Code improvements
```

### Branch Naming Convention
- `feature/` - New features
- `bugfix/` - Bug fixes
- `refactor/` - Code improvements (no new features)
- `hotfix/` - Urgent production fixes
- `experiment/` - Try alternative approaches (chart libraries, etc.)

### Commit Message Format
```
type(scope): subject

body (optional)

footer (optional)
```

Examples:
```
feat(backend): add stock price calculation service

Implements CAGR and return percentage calculations
using BigDecimal for precision.

Closes #42

fix(frontend): prevent negative investment amounts

Added validation to reject amounts <= 0.

test(backend): add unit tests for date utilities

chore: update dependencies to latest versions
```

---

## MONITORING & ANALYTICS (Post-Deployment)

### Application Monitoring
- **Spring Boot Actuator**: `/actuator/health`, `/actuator/metrics`
- **Error Tracking**: Sentry, Rollbar, or similar
- **Performance**: Response times, slow queries (pg_stat_statements)
- **Logs**: Centralized logging (ELK stack or cloud provider logs)

### User Analytics
- **Privacy-Focused**: Plausible or Simple Analytics (GDPR-compliant)
- **Track Events**:
  - Page views
  - Simulations run
  - Stocks selected
  - Chart interactions
  - Error occurrences
- **Metrics**:
  - DAU (Daily Active Users)
  - Simulations per user
  - Most popular stocks/date ranges
  - Average session duration

### Business Metrics (Future)
- Conversion rates (free → paid features)
- Retention rates (7-day, 30-day)
- Feature adoption
- A/B test results

---

## COPILOT INSTRUCTIONS SUMMARY

When generating code for this project:

1. **Architecture**: Follow clean architecture, service layer pattern, DTOs
2. **Error Handling**: Try-catch blocks, custom exceptions, user-friendly messages
3. **Testing**: Write unit tests for all business logic
4. **Comments**: Explain "why" and business rules, not obvious "what"
5. **Modern Java**: Use Java 25 features (records, pattern matching, virtual threads)
6. **TypeScript**: Strict mode, explicit types, avoid `any`
7. **Mobile-First**: Touch-friendly, responsive design
8. **Long-Term**: Maintainable, extensible, well-documented code
9. **Security**: Validate inputs, sanitize outputs, prevent injection
10. **Performance**: Efficient queries, minimize API calls, cache strategically
11. **Learning**: Add comments explaining data structure choices, algorithm complexity
12. **Flexibility**: Use abstractions (interfaces, adapters) to allow experimentation

---

## NEXT STEPS

### Immediate Tasks (Week 1)
1. ✅ Finalize project structure
2. ⬜ Set up Git repository
3. ⬜ Create Docker Compose configuration
4. ⬜ Initialize Spring Boot project with dependencies
5. ⬜ Initialize React + TypeScript project with Vite
6. ⬜ Create database schema (Flyway migrations)
7. ⬜ Implement Stock entity + repository
8. ⬜ Build first API endpoint (GET /api/stocks)
9. ⬜ Create basic frontend layout

### Short-Term (Week 2-4)
- Implement stock data fetching service (Yahoo Finance)
- Build portfolio simulation logic
- Create chart component (try 2-3 libraries)
- Add frontend form validation
- Write comprehensive tests

### Medium-Term (Month 2-3)
- Polish UI/UX
- Add educational content
- Performance optimization
- User testing with friends/family
- Prepare for deployment

---

## CURRENT TASK

**START WITH**: Set up the initial Spring Boot project structure with PostgreSQL configuration and Docker Compose

**Next Prompts for Copilot**:
1. "Create pom.xml with all necessary dependencies (Spring Boot, PostgreSQL, Lombok, validation)"
2. "Generate docker-compose.yml for PostgreSQL 15"
3. "Create application.yml with database configuration"
4. "Generate Stock entity with JPA annotations"
5. "Create StockRepository interface extending JpaRepository"
6. "Build Flyway migration V1__create_stocks_table.sql"

---

**REMEMBER**: 
- ✅ Build locally first - zero hosting costs
- ✅ Experiment freely - nothing is final
- ✅ Learn deeply - understand every data structure and pattern
- ✅ Test thoroughly - aim for 80%+ coverage on business logic
- ✅ Document decisions - future you will thank you
- ✅ Have fun - this is a learning journey!

---

**Let's build something amazing! 🚀**
```
