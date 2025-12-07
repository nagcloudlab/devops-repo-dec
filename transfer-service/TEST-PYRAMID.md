# 🔺 Test Pyramid - UPI Transfer Service

## Overview

This project implements a comprehensive **Test Pyramid** strategy for the UPI Transfer Service REST API. The test pyramid ensures optimal test coverage with appropriate investment at each layer.

```
                    ╱╲
                   ╱  ╲
                  ╱ E2E╲           ← 2% (Few, Expensive, Slow)
                 ╱──────╲
                ╱   API  ╲         ← 8% (Integration with HTTP)
               ╱──────────╲
              ╱ Integration╲       ← 20% (Spring Context)
             ╱──────────────╲
            ╱   UNIT TESTS   ╲     ← 70% (Fast, Isolated, Many)
           ╱──────────────────╲
```

---

## 📊 Test Distribution

| Layer | Percentage | Test Count | Execution Time |
|-------|------------|------------|----------------|
| Unit Tests | 70% | ~100+ tests | < 5 seconds |
| Integration Tests | 20% | ~30 tests | < 30 seconds |
| API Tests | 8% | ~25 tests | < 20 seconds |
| E2E Tests | 2% | ~10 tests | < 60 seconds |

---

## 📁 Test Structure

```
src/test/java/com/upi/
├── unit/                          # UNIT TESTS (70%)
│   ├── service/
│   │   ├── TransferServiceTest.java
│   │   ├── VpaValidatorServiceTest.java
│   │   └── ChargeCalculatorServiceTest.java
│   ├── smoke/
│   │   └── SmokeTest.java         # @Tag("smoke")
│   └── contract/
│       └── ContractTest.java      # @Tag("contract")
│
├── integration/                   # INTEGRATION TESTS (20%)
│   └── TransferServiceIntegrationTest.java
│
├── api/                           # API TESTS (8%)
│   └── TransferControllerApiTest.java
│
├── e2e/                           # E2E TESTS (2%)
│   └── UserJourneyE2ETest.java
│
├── architecture/                  # ARCHITECTURE TESTS
│   └── ArchitectureTest.java
│
└── performance/                   # PERFORMANCE TESTS
    └── [Future: PerformanceTest.java]
```

---

## 🧪 Test Layers Explained

### 1. Unit Tests (70%) - `unit/`

**Purpose:** Test individual components in isolation with mocked dependencies.

**Characteristics:**
- ⚡ Fastest execution
- 🔌 No Spring context
- 🎭 All dependencies mocked
- ✅ Test business logic only

**Example:**
```java
@ExtendWith(MockitoExtension.class)
class TransferServiceTest {
    @Mock private TransactionRepository repository;
    @Mock private VpaValidatorService validator;
    @InjectMocks private TransferService service;
    
    @Test
    void validTransfer_shouldSucceed() {
        // Pure logic test with mocks
    }
}
```

**Run:** `mvn test -Punit-tests`

---

### 2. Integration Tests (20%) - `integration/`

**Purpose:** Test component integration with real dependencies.

**Characteristics:**
- 🔄 Full Spring context
- 💾 Real H2 database
- 🔗 Tests component wiring
- 📝 Transactional (rollback after each test)

**Example:**
```java
@SpringBootTest
@Transactional
class TransferServiceIntegrationTest {
    @Autowired private TransferService service;
    @Autowired private TransactionRepository repository;
    
    @Test
    void completeTransfer_shouldPersistToDatabase() {
        // Real database test
    }
}
```

**Run:** `mvn verify -Pintegration-tests`

---

### 3. API Tests (8%) - `api/`

**Purpose:** Test REST endpoints and HTTP behavior.

**Characteristics:**
- 🌐 HTTP layer testing
- 📋 Request/Response validation
- ❌ Error handling verification
- 📊 JSON schema validation

**Example:**
```java
@SpringBootTest
@AutoConfigureMockMvc
class TransferControllerApiTest {
    @Autowired private MockMvc mockMvc;
    
    @Test
    void postTransfer_shouldReturn201() throws Exception {
        mockMvc.perform(post("/api/v1/transfer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.transactionRef").exists());
    }
}
```

**Run:** `mvn verify -Papi-tests`

---

### 4. E2E Tests (2%) - `e2e/`

**Purpose:** Test complete user journeys across the system.

**Characteristics:**
- 👤 Simulates real user flows
- 🔄 Multi-step scenarios
- 💰 Most expensive to run
- 🎯 Critical path verification

**Example:**
```java
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class UserJourneyE2ETest {
    
    @Test
    @Order(1)
    void step1_validatePayerVpa() { }
    
    @Test
    @Order(2)
    void step2_validatePayeeVpa() { }
    
    @Test
    @Order(3)
    void step3_initiateTransfer() { }
    
    @Test
    @Order(4)
    void step4_checkTransactionStatus() { }
}
```

**Run:** `mvn verify -Pe2e-tests`

---

## 🏷️ Special Test Categories

### Smoke Tests (`@Tag("smoke")`)

Quick sanity checks for critical functionality. Run before every deployment.

```bash
mvn test -Psmoke-tests
# OR
mvn test -Dgroups=smoke
```

### Contract Tests (`@Tag("contract")`)

Verify API contracts and backward compatibility.

```bash
mvn test -Pcontract-tests
# OR
mvn test -Dgroups=contract
```

### Architecture Tests

Enforce code structure using ArchUnit.

```bash
mvn test -Dtest=ArchitectureTest
```

---

## 🚀 Running Tests

### Run All Tests
```bash
mvn clean verify
```

### Run by Layer
```bash
# Unit tests only (fastest)
mvn test -Punit-tests

# Integration tests
mvn verify -Pintegration-tests

# API tests
mvn verify -Papi-tests

# E2E tests
mvn verify -Pe2e-tests
```

### Run by Tag
```bash
# Smoke tests
mvn test -Dgroups=smoke

# Contract tests
mvn test -Dgroups=contract
```

### Skip Tests
```bash
# Skip all tests
mvn package -DskipTests

# Skip only integration tests
mvn package -DskipITs
```

---

## 📈 Code Coverage

JaCoCo is configured to:
- Generate separate reports for unit and integration tests
- Merge reports for overall coverage
- Enforce minimum coverage thresholds

### Coverage Thresholds
- **Line Coverage:** 80%
- **Branch Coverage:** 80%

### View Reports
```bash
# Generate coverage report
mvn verify

# Reports located at:
# - target/site/jacoco-ut/index.html (Unit tests)
# - target/site/jacoco-it/index.html (Integration tests)
# - target/site/jacoco-merged/index.html (Combined)
```

---

## 📊 Allure Reports

Generate beautiful test reports with Allure.

```bash
# Run tests with Allure
mvn clean test

# Serve report
allure serve target/allure-results
```

---

## 🔄 CI/CD Pipeline Integration

### GitHub Actions Example

```yaml
jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - name: Smoke Tests
        run: mvn test -Psmoke-tests
        
      - name: Unit Tests
        run: mvn test -Punit-tests
        
      - name: Integration Tests
        run: mvn verify -Pintegration-tests
        
      - name: API Tests
        run: mvn verify -Papi-tests
```

### Pipeline Stages

```
┌─────────────┐   ┌──────────────┐   ┌─────────────┐   ┌─────────────┐
│   Smoke     │ → │    Unit      │ → │ Integration │ → │    E2E      │
│   Tests     │   │    Tests     │   │    Tests    │   │   Tests     │
│  (30 sec)   │   │  (2 min)     │   │  (5 min)    │   │  (10 min)   │
└─────────────┘   └──────────────┘   └─────────────┘   └─────────────┘
      ↓                  ↓                  ↓                  ↓
    FAIL FAST       QUALITY GATE      INTEGRATION        PRODUCTION
    if critical     if coverage        READY             READY
    paths fail      < 80%
```

---

## 📝 Test Naming Conventions

### Test Class Naming
| Type | Pattern | Example |
|------|---------|---------|
| Unit | `*Test.java` | `TransferServiceTest.java` |
| Integration | `*IntegrationTest.java` / `*IT.java` | `TransferServiceIntegrationTest.java` |
| API | `*ApiTest.java` | `TransferControllerApiTest.java` |
| E2E | `*E2ETest.java` | `UserJourneyE2ETest.java` |

### Test Method Naming
```java
// Pattern: methodName_scenario_expectedResult
@Test
void processTransfer_validRequest_shouldReturnSuccess() { }

@Test
void processTransfer_invalidVpa_shouldThrowException() { }

// Or with DisplayName
@Test
@DisplayName("✅ Valid P2P transfer should succeed")
void validP2PTransfer() { }
```

---

## 🎯 Best Practices

### DO ✅
- Follow the test pyramid proportions
- Use meaningful test names
- Test one concept per test
- Use parameterized tests for multiple scenarios
- Keep unit tests fast and isolated
- Use `@Nested` for logical grouping

### DON'T ❌
- Skip unit tests in favor of integration tests
- Write tests that depend on execution order
- Test implementation details
- Create flaky tests
- Ignore test failures

---

## 📚 Resources

- [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/)
- [Spring Boot Testing](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.testing)
- [ArchUnit User Guide](https://www.archunit.org/userguide/html/000_Index.html)
- [Test Pyramid - Martin Fowler](https://martinfowler.com/articles/practical-test-pyramid.html)
