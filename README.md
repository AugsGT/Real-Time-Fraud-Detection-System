# FraudShield — Real-Time Fraud Detection System

A production-grade fraud detection system built with **Java 17** and **Spring Boot 3.2**.  
It combines rule-based filtering with a pure-Java **Isolation Forest** ML model in a two-stage asynchronous pipeline.

---

## 🏗️ Architecture

```
Transaction Input (REST API)
        │
        ▼
 TransactionIngestionService
        │ (async, thread pool)
        ▼
 FraudDetectionPipeline
        ├── Stage 1: RuleEvaluationService
        │         ├── AmountThresholdRule
        │         ├── FrequencyRule
        │         ├── GeoVelocityRule (Haversine)
        │         └── ChannelRiskRule
        │
        └── Stage 2: AnomalyDetectionService
                  ├── FeatureExtractor (5 features)
                  └── IsolationForestModel (100 trees, pure Java)
                  
        ▼
 FraudAlert persisted → REST API → Dashboard
```

## 🚀 Quick Start

**Prerequisites**: Java 17+, Maven 3.8+

```bash
# 1. Clone / navigate to project
cd c:\Storage\java\fraud-detection

# 2. Build
mvn clean package -DskipTests

# 3. Run
mvn spring-boot:run

# 4. Open dashboard
start http://localhost:8080

# 5. Run Swagger UI
start http://localhost:8080/swagger-ui.html
```

## 📡 REST API

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/transactions` | POST | Ingest a transaction |
| `/api/transactions` | GET | List transactions |
| `/api/alerts` | GET | List fraud alerts |
| `/api/alerts/stats` | GET | Dashboard statistics |
| `/api/alerts/{id}/resolve` | PUT | Resolve an alert |
| `/api/rules` | GET/POST | List/create rules |
| `/api/rules/{id}/toggle` | PUT | Enable/disable rule |
| `/api/simulate` | POST | Inject bulk test data |
| `/api/simulate/scenario/{name}` | POST | Named fraud scenarios |

### Fraud Scenarios
- `card-testing` — 20 rapid micro-transactions
- `account-takeover` — sudden high-value online purchase after normal history
- `geo-velocity` — New York then Tokyo in 5 minutes (impossible travel)

## 🧠 Machine Learning

The **Isolation Forest** model trains at startup (`ApplicationReadyEvent`) using:
- Existing DB transactions + 256 synthetic normal baselines
- 100 isolation trees, max depth 10, subsample size 256
- Anomaly threshold: `0.65` (configurable in `application.properties`)

**Feature vector (5 dimensions)**:
1. Amount z-score (normalized vs. $200 mean, $500 std)
2. Time since last transaction (normalized to 24h)
3. Geo distance from last transaction (Haversine, normalized to 20,000 km)
4. Transaction velocity in last 1 hour (normalized to 20 tx/h)
5. Channel risk weight (ONLINE=0.8, MOBILE=0.5, ATM=0.4, POS=0.2)

## ⚙️ Dynamic Rules

Rules are stored in the database and evaluated fresh on every transaction — **no redeployment needed**.

| Rule Type | Default Params |
|-----------|---------------|
| `AMOUNT_THRESHOLD` | `{"maxAmount": 5000.0}` |
| `FREQUENCY` | `{"maxCount": 8, "windowMinutes": 30}` |
| `GEO_VELOCITY` | `{"maxSpeedKmH": 900.0}` |
| `CHANNEL_RISK` | `{"onlineThreshold": 2000.0}` |

## 🗄️ Database

H2 file database at `./data/frauddb` — persists across restarts.  
H2 Console: `http://localhost:8080/h2-console` (JDBC: `jdbc:h2:file:./data/frauddb`)

## 🧪 Testing

```bash
mvn test
```

Test coverage includes:
- `AmountThresholdRuleTest` — rule logic edge cases
- `IsolationForestModelTest` — model training, score ordering, bounds
- `FraudDetectionApplicationTests` — Spring context load

## 📁 Project Structure

```
src/main/java/com/frauddetection/
├── config/         AsyncConfig, DataInitializer, ModelTrainer
├── controller/     TransactionController, FraudAlertController,
│                   RuleController, SimulationController, GlobalExceptionHandler
├── dto/            Request/Response DTOs
├── exception/      ResourceNotFoundException
├── ml/             IsolationForestModel, FeatureExtractor
├── model/          Transaction, FraudAlert, FraudRule (JPA entities)
├── repository/     Spring Data JPA repositories
├── rules/          RuleEngine interface + 4 implementations
└── service/        TransactionIngestionService, RuleEvaluationService,
                    AnomalyDetectionService, FraudDetectionPipeline
```
