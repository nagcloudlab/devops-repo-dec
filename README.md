# 🚀 UPI Payment Service - CI/CD Demo

**Training Program: CI/CD Pipeline for Test Managers**

This repository demonstrates a complete CI/CD pipeline using GitHub Actions, with AWS ECS deployment. Designed for training Test Managers on pipeline execution, test reporting, quality gates, and troubleshooting.

---

## 📁 Project Structure

```
devops-repo-dec/
├── .github/
│   └── workflows/
│       └── ci-pipeline.yml      # Main CI/CD pipeline
├── transfer-service/
│   ├── pom.xml                  # Module POM (dependencies, plugins)
│   └── src/
│       ├── main/
│       │   ├── java/com/npci/upi/
│       │   │   ├── service/
│       │   │   │   └── UPITransferService.java
│       │   │   ├── model/
│       │   │   │   ├── TransferRequest.java
│       │   │   │   └── TransferResponse.java
│       │   │   └── exception/
│       │   │       └── PaymentException.java
│       │   └── resources/
│       └── test/
│           ├── java/com/npci/upi/
│           │   ├── unit/
│           │   │   └── UPITransferServiceTest.java
│           │   ├── integration/
│           │   │   └── UPITransferIntegrationTest.java
│           │   └── flaky/
│           │       └── FlakyTestExamples.java
│           └── resources/
├── infrastructure/
│   └── aws/
│       └── task-definition.json
├── docs/
│   ├── DEMO-SCRIPT.md
│   └── TROUBLESHOOTING.md
├── pom.xml                      # Parent POM (multi-module)
├── Dockerfile
└── README.md
```

---

## 🏃 Quick Start

### Prerequisites
- Java 17+
- Maven 3.8+
- Git
- GitHub account

### Local Setup
```bash
# Clone repository
git clone https://github.com/YOUR_USERNAME/upi-cicd-demo.git
cd upi-cicd-demo

# Run unit tests
mvn test

# Run all tests with coverage
mvn verify

# View coverage report
open target/site/jacoco-ut/index.html
```

---

## 🔄 Pipeline Stages

```
┌─────────┐    ┌─────────┐    ┌─────────┐    ┌─────────┐    ┌─────────┐    ┌─────────┐
│  BUILD  │───▶│  UNIT   │───▶│ INTEG.  │───▶│ QUALITY │───▶│ DOCKER  │───▶│ DEPLOY  │
│         │    │  TESTS  │    │  TESTS  │    │  GATE   │    │  BUILD  │    │  AWS    │
└─────────┘    └─────────┘    └─────────┘    └─────────┘    └─────────┘    └─────────┘
                   │                              │
              Fast Feedback              Coverage >= 80%
              (Shift-Left)               (Blocks if fails)
```

### Triggers
| Trigger | When | Purpose |
|---------|------|---------|
| `push` | Commit to main/develop | Continuous integration |
| `pull_request` | PR to main | Code review validation |
| `workflow_dispatch` | Manual | On-demand deployment |

---

## 🧪 Test Categories

### Unit Tests (`/unit/`)
- **Purpose:** Test individual components in isolation
- **Execution:** Every commit
- **Duration:** < 2 minutes
- **Coverage Target:** 80%+

### Integration Tests (`/integration/`)
- **Purpose:** Test components working together
- **Execution:** Every PR
- **Duration:** 5-10 minutes
- **Features:** Concurrent transactions, multi-bank scenarios

### Flaky Test Examples (`/flaky/`)
- **Purpose:** Training on test stability
- **Contains:** Common flakiness patterns with fixes
- **Usage:** Enable `@Disabled` tests to demonstrate

---

## 📊 Quality Gates

| Metric | Threshold | Action if Failed |
|--------|-----------|------------------|
| Line Coverage | ≥ 80% | Block deployment |
| Branch Coverage | ≥ 80% | Block deployment |
| Service Coverage | ≥ 90% | Block deployment |
| Unit Test Pass | 100% | Block deployment |
| Integration Test Pass | 100% | Block deployment |

---

## 🔧 Maven Profiles

```bash
# Run only unit tests (fast)
mvn test -P unit-tests

# Run only integration tests
mvn verify -P integration-tests

# Run all tests including flaky demos
mvn test -P all-tests

# Skip coverage check (emergency only!)
mvn verify -P skip-coverage-check
```

---

## 📈 Reports

### JaCoCo Coverage
```bash
mvn jacoco:report
open target/site/jacoco-ut/index.html
```

### Allure Test Report
```bash
mvn allure:report
open target/site/allure-report/index.html
```

---

## 🚨 Troubleshooting

See [docs/TROUBLESHOOTING.md](docs/TROUBLESHOOTING.md) for:
- Build failures
- Test failures
- Quality gate issues
- Docker/AWS deployment problems
- Flaky test patterns

---

## 🎓 Training Resources

| Document | Purpose |
|----------|---------|
| [DEMO-SCRIPT.md](docs/DEMO-SCRIPT.md) | Step-by-step trainer guide |
| [TROUBLESHOOTING.md](docs/TROUBLESHOOTING.md) | Common errors & fixes |
| [FlakyTestExamples.java](transfer-service/src/test/java/com/npci/upi/flaky/FlakyTestExamples.java) | Flaky test patterns |

---

## 🏗️ AWS Deployment

### Required Secrets (GitHub)
- `AWS_ACCESS_KEY_ID`
- `AWS_SECRET_ACCESS_KEY`

### AWS Resources
- **ECR:** Container registry
- **ECS:** Container orchestration (Fargate)
- **CloudWatch:** Logs and monitoring

### Manual Deploy
```bash
# Trigger deployment manually
gh workflow run ci-pipeline.yml -f environment=staging
```

---

## 📝 License

Internal use only - Training Program

---

## 👥 Contact

**Trainer:** Nag  
**Program:** Test Automation Excellence
