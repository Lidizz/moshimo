# PROJECT BRIEF v3 - Moshimo Vision & Roadmap

> **Version:** 3.0  
> **Created:** February 1, 2026  
> **Status:** Active  
> **Previous Versions:** [v1](PROJECT_BRIEF.md) (inception), [v2](PROJECT_BRIEF_v2.md) (changelog)

---

## Executive Summary

**Moshimo** is an educational investment simulator that answers the question: *"What if I had invested?"*

Our vision: Build the most intuitive investment learning tool — simple enough for a 10-year-old, powerful enough for professionals.

---

## Current State (v1 Complete)

### ✅ What's Built

| Component | Status | Details |
|-----------|--------|---------|
| **Backend** | 85% | Java 25, Spring Boot 4.0, PostgreSQL, TwelveData integration |
| **Frontend** | 80% | React 19, TypeScript, TradingView charts, dark/light theme |
| **Data Pipeline** | 95% | Multi-provider architecture, monthly auto-updates |
| **Asset Categories** | ✅ | Stocks, ETFs, Indexes with sector filtering |
| **CI/CD** | ✅ | GitHub Actions (build on push/PR) |
| **Repository** | ✅ | https://github.com/Lidizz/moshimo |

### Core Features Live
- Multi-stock portfolio simulation
- Historical price data (20+ years)
- CAGR, returns, gain/loss calculations
- Combined & split chart views
- Timeline aggregation (1D/1W/1M/1Y/ALL)
- Asset type filtering (Stocks/ETFs/Indexes)
- Sector filtering
- PWA support
- Responsive design

---

## Vision: Where We're Going

### 🎯 Mission Statement

> Make investment education accessible to everyone — from curious kids to seasoned professionals — through interactive historical simulation and smart backtesting tools.

### Target Users

| User Type | Age | Needs | Features |
|-----------|-----|-------|----------|
| **Kids & Beginners** | 10-18 | Learn basics, see compound growth | Simple UI, educational tooltips, gamification |
| **Young Adults** | 18-35 | Explore "what if" scenarios | Full simulation, comparisons, insights |
| **Professionals** | 25-55 | Backtest strategies, analyze metrics | Advanced indicators, exports, API access |
| **Parents/Teachers** | 30-60 | Teach financial literacy | Shareable portfolios, lesson mode |

---

## Phase 2: Enhanced Experience

### 2.1 Expanded Asset Coverage

| Asset Type | Current | Target | Priority |
|------------|---------|--------|----------|
| Stocks | ~21 | 100+ | High |
| ETFs | ~5 | 50+ | High |
| Indexes | ~3 | 10+ | Medium |
| Crypto | 0 | 20+ | Phase 3 |

**Implementation:**
- Batch import via TwelveData API
- Auto-categorization by type and sector
- Smart search with fuzzy matching
- Popular/trending assets section

### 2.2 Frontend Architecture Improvements

| Feature | Description | Priority |
|---------|-------------|----------|
| React Router | Multi-page navigation (Home, Simulator, Learn, About) | High |
| Custom Hooks | Extract reusable logic (useStockData, useSimulation) | High |
| Error Boundaries | Graceful error handling | Medium |
| Skeleton Loading | Better perceived performance | Medium |

### 2.3 Testing Foundation

| Type | Target Coverage | Priority |
|------|-----------------|----------|
| Backend Unit Tests | 60% on services | High |
| Backend Integration | Repository + Controller tests | Medium |
| Frontend Unit Tests | Utility functions, hooks | High |
| E2E Tests | Critical user flows | Phase 3 |

---

## Phase 3: Pro Features & Backtesting

### 3.1 Backtesting Strategies

Pre-calculated strategies users can apply without coding knowledge:

| Strategy | Description | Complexity |
|----------|-------------|------------|
| **Buy & Hold** | Baseline comparison | Simple |
| **Dollar Cost Averaging (DCA)** | Fixed periodic investments | Simple |
| **SMA Crossover** | 50-day vs 200-day moving average signals | Intermediate |
| **RSI Momentum** | Relative Strength Index overbought/oversold | Intermediate |
| **Rebalancing** | Periodic portfolio rebalancing | Intermediate |
| **Value Averaging** | Adjust contributions to hit targets | Advanced |

**User Experience:**
- Select strategy from dropdown
- See historical performance comparison
- Educational explanation of each strategy
- "What would have happened" visualization

### 3.2 Advanced Metrics

| Metric | Description | User Level |
|--------|-------------|------------|
| Sharpe Ratio | Risk-adjusted returns | Pro |
| Max Drawdown | Largest peak-to-trough decline | Pro |
| Volatility | Standard deviation of returns | Pro |
| Beta | Correlation to market benchmark | Pro |
| Alpha | Excess return vs benchmark | Pro |
| Sortino Ratio | Downside risk-adjusted returns | Pro |

### 3.3 Crypto Support

| Asset | Symbol | Priority |
|-------|--------|----------|
| Bitcoin | BTC | High |
| Ethereum | ETH | High |
| Solana | SOL | Medium |
| Cardano | ADA | Medium |
| Top 20 by market cap | Various | Low |

**Considerations:**
- Different data provider may be needed (CoinGecko, CryptoCompare)
- 24/7 trading vs market hours
- Higher volatility warnings
- Regulatory disclaimers

---

## Phase 4: Social & Educational

### 4.1 Shareable Portfolios
- Generate unique URL for portfolio
- Embed chart on external sites
- Social media preview cards
- QR codes for mobile sharing

### 4.2 Educational Content
- Interactive tooltips explaining terms
- "Learn" section with articles
- Video tutorials integration
- Guided first-time experience

### 4.3 Gamification (Kid-Friendly)
- Achievement badges
- "Investment challenges"
- Leaderboards (fictional money)
- Progress tracking

---

## Technical Roadmap

### Architecture Principles

1. **Simplicity First** — Kid-friendly doesn't mean dumbed down
2. **Progressive Disclosure** — Show basics, reveal complexity on demand
3. **Performance** — Fast charts, snappy interactions
4. **Accessibility** — WCAG 2.1 AA compliance
5. **Mobile-First** — Touch-friendly, responsive

### Tech Stack Evolution

| Component | Current | Future Consideration |
|-----------|---------|----------------------|
| Backend | Java 25, Spring Boot 4.0 | Stay current with LTS |
| Frontend | React 19, Vite | Consider React Server Components |
| Database | PostgreSQL 15 | TimescaleDB for time-series optimization |
| Charts | TradingView Lightweight | Keep (excellent performance) |
| Cache | None | Redis for API response caching |
| Search | Basic SQL | Elasticsearch for fuzzy search |
| Auth | None | OAuth2 (Google, GitHub) for saved portfolios |

### API Design

Future public API for developers:

```
  GET /api/v2/assets?type=STOCK&sector=Technology
  GET /api/v2/assets/{symbol}/prices?from=2020-01-01&to=2025-01-01
  POST /api/v2/simulate
  GET /api/v2/backtest/strategies
  POST /api/v2/backtest/run
```

---

## Development Process

### Workflow
- **Branching:** Feature branches → PR → main
- **CI/CD:** GitHub Actions (build, test, deploy)
- **Tracking:** GitHub Projects (Kanban board)
- **Releases:** Semantic versioning (v1.0.0, v1.1.0, etc.)

### Sprint Cadence
- 1-2 week sprints
- Tickets with acceptance criteria
- Demo at end of sprint
- Retrospective for learnings

### Quality Standards
- All PRs require CI passing
- Code comments explain "why"
- Learning-focused documentation
- Test coverage targets per phase

---

## Success Metrics

### User Engagement (Future)
- Simulations run per day
- Average session duration
- Return visitor rate
- Assets explored per session

### Technical Health
- CI build success rate > 95%
- Test coverage > 50% (Phase 2), > 70% (Phase 3)
- Page load time < 2 seconds
- API response time < 500ms

### Learning Goals (Personal)
- Master Java design patterns
- Deep understanding of financial calculations
- Full-stack production deployment
- Open source community engagement

---

## Backlog Overview

### Immediate (Next Sprint)
- [ ] GitHub Projects Kanban setup
- [ ] Basic unit tests (backend + frontend)
- [ ] React Router + page structure
- [ ] Expand asset database (50+ stocks, 30+ ETFs)

### Short-Term (1-2 months)
- [ ] DCA simulation mode
- [ ] S&P 500 benchmark comparison (complete)
- [ ] Educational tooltips
- [ ] Performance optimizations

### Medium-Term (3-6 months)
- [ ] Backtesting strategies (SMA, RSI)
- [ ] Advanced metrics (Sharpe, Drawdown)
- [ ] User accounts + saved portfolios
- [ ] Crypto support

### Long-Term (6-12 months)
- [ ] Mobile app (React Native or PWA enhancement)
- [ ] Public API
- [ ] Community features
- [ ] Monetization exploration

---

## Appendix

### Version History

| Version | Date | Focus |
|---------|------|-------|
| v1 | Inception - Jan 2026 | Original vision, MVP definition |
| v2 | Jan 31, 2026 | Changelog format, Java 25 upgrade |
| v3 | Feb 1, 2026 | Expanded vision, roadmap, phases |

### References
- [Original Brief (v1)](PROJECT_BRIEF.md)
- [Changelog (v2)](PROJECT_BRIEF_v2.md)
- [Repository](https://github.com/Lidizz/moshimo)
- [Design System](frontend/DESIGN_SYSTEM.md)

---

## Guiding Principles

> 🎯 **Simple for kids, powerful for pros**  
> 📚 **Every feature is a learning opportunity**  
> 🚀 **Ship early, iterate often**  
> ✨ **Make finance feel approachable, not intimidating**

---

*Last updated: February 1, 2026*
