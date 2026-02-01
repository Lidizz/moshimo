# Moshimo - Investment "What If" Simulator

> What if you had invested? Find out with real market data.

[![CI Build](https://github.com/Lidizz/moshimo/actions/workflows/ci.yml/badge.svg)](https://github.com/Lidizz/moshimo/actions/workflows/ci.yml)

---

## Overview

**Moshimo** (もしも - Japanese for "What if") is an educational investment simulator that helps users visualize how past investments would have grown. Simple enough for a 10-year-old, powerful enough for professionals.

### Key Features

- 📈 **Historical Simulation** - See how investments would have performed over 20+ years
- 💼 **Multi-Asset Portfolios** - Combine stocks, ETFs, and indexes
- 📊 **Interactive Charts** - TradingView-powered visualization with split views
- 🏷️ **Smart Filtering** - Filter by asset type (Stocks/ETFs/Indexes) and sector
- 🌓 **Dark/Light Mode** - Easy on the eyes
- 📱 **Responsive Design** - Works on desktop and mobile
- ⚡ **PWA Support** - Install as an app

---

## Screenshots

<!-- TODO: Add screenshots -->
*Coming soon*

---

## Tech Stack

| Layer | Technology |
|-------|------------|
| **Backend** | Java 25, Spring Boot 4.0, PostgreSQL 15 |
| **Frontend** | React 19, TypeScript, Vite |
| **Charts** | TradingView Lightweight Charts |
| **Data** | TwelveData API |
| **CI/CD** | GitHub Actions |
| **Container** | Docker Compose |

---

## Getting Started

### Prerequisites

- Java 25+
- Node.js 20+
- Docker & Docker Compose
- TwelveData API key (free tier available)

### Quick Start

```bash
# Clone repository
git clone https://github.com/Lidizz/moshimo.git
cd moshimo

# Start database
docker-compose up -d

# Backend
cd backend
cp .env.example .env  # Add your API keys
./mvnw spring-boot:run

# Frontend (new terminal)
cd frontend
npm install
npm run dev
```

Open http://localhost:5173

---

## Project Structure

```
moshimo/
├── backend/          # Java Spring Boot API
├── frontend/         # React TypeScript UI
├── scripts/          # Utility scripts
├── .github/          # CI/CD workflows
└── docker-compose.yml
```

---

## Roadmap

See PROJECT_BRIEF_v3.md for detailed roadmap.

### Upcoming Features
- [ ] Backtesting strategies (DCA, SMA Crossover)
- [ ] Advanced metrics (Sharpe Ratio, Max Drawdown)
- [ ] Crypto support
- [ ] User accounts & saved portfolios
- [ ] Mobile app

---

## Contributing

Contributions are welcome via Pull Request. See CONTRIBUTING.md for guidelines.

**Note:** This project uses a proprietary license. By contributing, you agree that your contributions become part of this project under the same license terms.

---

## License

**Proprietary License** : All Rights Reserved

This source code is provided for **viewing and educational purposes only**.

- ❌ No commercial use
- ❌ No redistribution
- ❌ No derivative works
- ✅ View source code
- ✅ Fork for personal learning (non-commercial)

See LICENSE for full terms.

---

## Author

**Lidor Shachar** : [GitHub](https://github.com/Lidizz)

---

## Acknowledgments

- [TwelveData](https://twelvedata.com/) for market data API
- [TradingView](https://www.tradingview.com/) for Lightweight Charts library
- The open source community for inspiration

---

*Built with ❤️ for financial literacy*
