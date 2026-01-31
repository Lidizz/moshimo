# PROJECT BRIEF - Version 2

> **Last Updated**: January 31, 2026

This document tracks major changes to the project brief and infrastructure.

---

## Change Log

### Version 2.0 - January 31, 2026

#### Java 25 LTS Upgrade

**Ticket**: #1 - Upgrade to Java 25 LTS  
**Type**: Tech Debt / Infrastructure  
**Priority**: High  

##### What Changed
- Upgraded backend from **Java 21 LTS** to **Java 25 LTS** (released September 2025)
- Updated `java.version` property in [backend/pom.xml](backend/pom.xml) from 21 to 25
- Updated code comments referencing Java version in:
  - `StockDataProvider.java` 
  - `HealthResponse.java`

##### Why
- Leverage latest language features and performance improvements
- Ensure long-term support (LTS releases supported for years)
- Position codebase for upcoming backtesting features that may benefit from newer APIs
- Java 25 includes improvements to virtual threads, pattern matching, and other modern features

##### Verification
All acceptance criteria passed:
- ✅ Backend compiles with `mvn clean compile`
- ✅ Backend starts successfully with `mvn spring-boot:run`
- ✅ `/api/health` returns `UP` status
- ✅ `/api/stocks` returns expected stock data (21 stocks)
- ✅ Spring Boot 4.0.1 compatible with Java 25
- ✅ No regression in existing functionality

##### Notes
- **Lombok Warning**: There's a deprecation warning from Lombok regarding `sun.misc.Unsafe::objectFieldOffset`. This is a known issue with Lombok on newer Java versions and does not affect functionality. Monitor for Lombok updates.
- **AlphaVantageClient**: Minor deprecation warning noted - uses deprecated API. Consider updating in future sprint.

##### Local Development Setup
To run the backend with Java 25, ensure `JAVA_HOME` points to Java 25:

```powershell
# Windows PowerShell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-25.0.2"
./mvnw spring-boot:run
```

```bash
# Unix/macOS with SDKMAN
sdk use java 25-tem
./mvnw spring-boot:run
```

---

## Current Tech Stack (v2)

| Component | Version | Notes |
|-----------|---------|-------|
| **Java** | 25 LTS (25.0.2) | Upgraded Jan 31, 2026 |
| **Spring Boot** | 4.0.1 | Compatible with Java 25 |
| **PostgreSQL** | 15-alpine | Via Docker |
| **Node.js** | 20.x | Frontend |
| **React** | 18+ | With TypeScript |
| **Vite** | Latest | Build tool |

---

## Previous Versions

### Version 1.0 - Initial Release
- Initial project setup with Java 21 LTS
- Spring Boot 4.0.1
- PostgreSQL 15 with Docker Compose
- React 18+ frontend with TypeScript and Vite
- Core simulation functionality implemented

---

## Reference

For the complete project brief, see [PROJECT_BRIEF.md](PROJECT_BRIEF.md).
