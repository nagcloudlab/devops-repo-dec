# UPI Transfer Service - Spring Boot REST API

A complete Spring Boot REST API for UPI payment transfers with a comprehensive **Test Pyramid** implementation.

## 🏗️ Project Structure

```
transfer-service/
├── src/main/java/com/upi/
│   ├── TransferServiceApplication.java    # Spring Boot entry point
│   ├── controller/
│   │   └── TransferController.java        # REST endpoints
│   ├── service/
│   │   ├── TransferService.java           # Core business logic
│   │   ├── VpaValidatorService.java       # VPA validation
│   │   └── ChargeCalculatorService.java   # Fee calculations
│   ├── repository/
│   │   └── TransactionRepository.java     # JPA repository
│   ├── entity/
│   │   └── Transaction.java               # JPA entity
│   ├── dto/
│   │   ├── TransferRequest.java           # Request DTO
│   │   ├── TransferResponse.java          # Response DTO
│   │   ├── ValidationRequest.java         # VPA validation request
│   │   ├── ValidationResponse.java        # VPA validation response
│   │   └── ApiError.java                  # Error response
│   └── exception/
│       ├── PaymentException.java          # Custom exception
│       └── GlobalExceptionHandler.java    # Exception handler
├── src/main/resources/
│   └── application.yml                    # Configuration
├── src/test/java/com/upi/
│   ├── unit/                              # 70% - Unit tests
│   │   ├── service/
│   │   │   ├── TransferServiceTest.java
│   │   │   ├── VpaValidatorServiceTest.java
│   │   │   └── ChargeCalculatorServiceTest.java
│   │   ├── smoke/
│   │   │   └── SmokeTest.java             # @Tag("smoke")
│   │   └── contract/
│   │       └── ContractTest.java          # @Tag("contract")
│   ├── integration/                       # 20% - Integration tests
│   │   └── TransferServiceIntegrationTest.java
│   ├── api/                               # 8% - API tests
│   │   └── TransferControllerApiTest.java
│   ├── e2e/                               # 2% - E2E tests
│   │   └── UserJourneyE2ETest.java
│   └── architecture/
│       └── ArchitectureTest.java          # ArchUnit tests
├── pom.xml                                # Maven configuration
├── TEST-PYRAMID.md                        # Test pyramid documentation
└── README.md                              # This file
```

---

## 🚀 Quick Start

### Prerequisites
- Java 17+
- Maven 3.8+

### Run the Application
```bash
mvn spring-boot:run
```

### Access Points
- **API:** http://localhost:8080/api/v1
- **Swagger UI:** http://localhost:8080/swagger-ui.html
- **Health:** http://localhost:8080/actuator/health
- **H2 Console:** http://localhost:8080/h2-console

---

## 📡 API Endpoints

### Transfer Operations

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/transfer` | Process fund transfer |
| GET | `/api/v1/transfer/{ref}` | Get transaction status |
| GET | `/api/v1/transfer/history/{vpa}` | Get transaction history |

### Validation Operations

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/validate/vpa` | Validate VPA (full) |
| GET | `/api/v1/validate/vpa/{vpa}` | Quick VPA validation |

### Health

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/health` | Service health check |

---

## 📋 API Examples

### Process Transfer
```bash
curl -X POST http://localhost:8080/api/v1/transfer \
  -H "Content-Type: application/json" \
  -d '{
    "payerVpa": "user@sbi",
    "payeeVpa": "merchant@hdfc",
    "amount": 1000.00,
    "transactionType": "P2P",
    "remarks": "Payment for groceries"
  }'
```

**Response:**
```json
{
  "transactionRef": "TXN2024120712000001",
  "status": "SUCCESS",
  "message": "Transfer completed successfully",
  "timestamp": "2024-12-07T12:00:00",
  "amount": 1000.00,
  "charges": 0.00,
  "totalAmount": 1000.00,
  "payerVpa": "user@sbi",
  "payeeVpa": "merchant@hdfc",
  "bankRrn": "123456789012"
}
```

### Validate VPA
```bash
curl http://localhost:8080/api/v1/validate/vpa/user@sbi
```

**Response:**
```json
{
  "vpa": "user@sbi",
  "valid": true,
  "username": "user",
  "bankHandle": "sbi",
  "errors": [],
  "warnings": []
}
```

---

## 🔺 Test Pyramid

```
                    ╱╲
                   ╱  ╲
                  ╱ E2E╲           ← 2%  (10 tests)
                 ╱──────╲
                ╱   API  ╲         ← 8%  (25 tests)
               ╱──────────╲
              ╱ Integration╲       ← 20% (30 tests)
             ╱──────────────╲
            ╱   UNIT TESTS   ╲     ← 70% (100+ tests)
           ╱──────────────────╲
```

### Run Tests

```bash
# All tests
mvn clean verify

# Unit tests only (fastest)
mvn test -Punit-tests

# Integration tests
mvn verify -Pintegration-tests

# API tests
mvn verify -Papi-tests

# E2E tests
mvn verify -Pe2e-tests

# Smoke tests (critical path)
mvn test -Dgroups=smoke

# Contract tests
mvn test -Dgroups=contract
```

See [TEST-PYRAMID.md](TEST-PYRAMID.md) for detailed documentation.

---

## 🏦 Business Logic

### Transaction Types & Fees

| Type | Description | Fee |
|------|-------------|-----|
| P2P | Person to Person | Free |
| P2M | Person to Merchant | 0.30% |
| BILL | Bill Payment | 0.50% |

### GST
- 18% on applicable fees

### Transaction Limits
- Minimum: ₹1
- Maximum: ₹1,00,000 per transaction
- Daily: ₹2,00,000

### Supported Bank Handles
`sbi`, `hdfc`, `icici`, `axis`, `pnb`, `paytm`, `ybl`, `gpay`, `phonepe`, and 30+ more

---

## 🛠️ Technology Stack

| Component | Technology |
|-----------|------------|
| Framework | Spring Boot 3.2 |
| Language | Java 17 |
| Database | H2 (In-Memory) |
| ORM | Spring Data JPA |
| Validation | Jakarta Bean Validation |
| Documentation | SpringDoc OpenAPI |
| Testing | JUnit 5, Mockito, RestAssured |
| Architecture Tests | ArchUnit |
| Code Coverage | JaCoCo |
| Reports | Allure |

---

## 📊 Quality Gates

| Metric | Threshold |
|--------|-----------|
| Line Coverage | 80% |
| Branch Coverage | 80% |
| Smoke Tests | 100% pass |
| Unit Tests | 100% pass |

---

## 📁 Key Files

| File | Description |
|------|-------------|
| `pom.xml` | Maven dependencies & plugins |
| `application.yml` | Spring Boot configuration |
| `TEST-PYRAMID.md` | Test strategy documentation |
| `TransferService.java` | Core business logic |
| `TransferController.java` | REST API endpoints |

---

## 🎯 For Training

This project demonstrates:

1. **Spring Boot REST API** - Complete CRUD operations
2. **Test Pyramid** - Proper test distribution
3. **Clean Architecture** - Layered design
4. **Validation** - Request validation with annotations
5. **Exception Handling** - Global exception handler
6. **Documentation** - Swagger/OpenAPI
7. **Architecture Testing** - ArchUnit rules
8. **CI/CD Ready** - Maven profiles for different test suites

---

## 📚 Related Documentation

- [Test Pyramid Guide](TEST-PYRAMID.md)
- [Swagger UI](http://localhost:8080/swagger-ui.html) (when running)
- [Spring Boot Docs](https://docs.spring.io/spring-boot/docs/current/reference/html/)
