# 🔧 CI/CD Pipeline Troubleshooting Guide
## Common Errors and Solutions

---

## 📋 Table of Contents

1. [Build Stage Failures](#1-build-stage-failures)
2. [Test Stage Failures](#2-test-stage-failures)
3. [Quality Gate Failures](#3-quality-gate-failures)
4. [Docker Build Failures](#4-docker-build-failures)
5. [AWS Deployment Failures](#5-aws-deployment-failures)
6. [Flaky Test Issues](#6-flaky-test-issues)
7. [Pipeline Configuration Errors](#7-pipeline-configuration-errors)

---

## 1. Build Stage Failures

### Error 1.1: Compilation Error
```
[ERROR] COMPILATION ERROR : 
[ERROR] /src/main/java/com/npci/upi/service/UPITransferService.java:[45,17] 
cannot find symbol
```

**Cause:** Missing import, typo in code, or incompatible Java version

**Solution:**
```bash
# Check Java version locally
java -version

# Ensure pom.xml has correct version
<maven.compiler.source>17</maven.compiler.source>
<maven.compiler.target>17</maven.compiler.target>

# Clean and rebuild
mvn clean compile
```

---

### Error 1.2: Dependency Resolution Failed
```
[ERROR] Failed to execute goal on project upi-payment-service: 
Could not resolve dependencies for project com.npci:upi-payment-service:jar:1.0.0
```

**Cause:** Network issue, corrupted cache, or invalid dependency version

**Solution:**
```bash
# Clear local Maven cache
rm -rf ~/.m2/repository

# Force update dependencies
mvn clean install -U

# Check if repository is accessible
curl -I https://repo.maven.apache.org/maven2/
```

---

### Error 1.3: Out of Memory During Build
```
[ERROR] Java heap space
[ERROR] GC overhead limit exceeded
```

**Cause:** Insufficient memory allocated to Maven

**Solution:**
```yaml
# In GitHub Actions workflow, add:
env:
  MAVEN_OPTS: "-Xmx1024m -XX:MaxPermSize=256m"

# Or in pom.xml:
<configuration>
  <argLine>-Xmx1024m</argLine>
</configuration>
```

---

## 2. Test Stage Failures

### Error 2.1: Tests Not Found
```
[INFO] No tests to run.
[INFO] 
[INFO] --- maven-surefire-plugin:3.2.5:test ---
[INFO] No tests were executed!
```

**Cause:** Incorrect test file naming or wrong include pattern

**Solution:**
```xml
<!-- Ensure test classes follow naming convention -->
<!-- Must end with: Test.java, Tests.java, or TestCase.java -->

<!-- Check Surefire configuration in pom.xml -->
<includes>
  <include>**/*Test.java</include>
  <include>**/Test*.java</include>
</includes>
```

---

### Error 2.2: Test Timeout
```
org.junit.jupiter.api.extension.ExtensionConfigurationException: 
Test timed out after 10 seconds
```

**Cause:** Test takes too long, possibly due to external dependency

**Solution:**
```java
// Increase timeout for specific test
@Test
@Timeout(value = 30, unit = TimeUnit.SECONDS)
void testSlowOperation() {
    // test code
}

// Or configure globally in pom.xml
<configuration>
  <forkedProcessTimeoutInSeconds>300</forkedProcessTimeoutInSeconds>
</configuration>
```

---

### Error 2.3: Test Database Connection Failed
```
org.springframework.jdbc.CannotGetJdbcConnectionException: 
Failed to obtain JDBC Connection
```

**Cause:** Database not available in CI environment

**Solution:**
```yaml
# Add database service in GitHub Actions
services:
  postgres:
    image: postgres:14
    env:
      POSTGRES_DB: testdb
      POSTGRES_USER: test
      POSTGRES_PASSWORD: test
    ports:
      - 5432:5432
    options: >-
      --health-cmd pg_isready
      --health-interval 10s
      --health-timeout 5s
      --health-retries 5
```

---

## 3. Quality Gate Failures

### Error 3.1: Coverage Below Threshold
```
[ERROR] Rule violated for bundle upi-payment-service: 
lines covered ratio is 0.72, but expected minimum is 0.80
```

**Cause:** Insufficient test coverage

**Solution:**
```bash
# Check which classes need more coverage
mvn jacoco:report
# Open target/site/jacoco/index.html

# Focus on uncovered lines in critical classes
# Add tests for:
# - Exception handling paths
# - Edge cases
# - Validation logic
```

**Quick Wins for Coverage:**
```java
// 1. Test null inputs
@Test
void testNullInput() {
    assertThrows(PaymentException.class, 
        () -> service.processTransfer(null));
}

// 2. Test boundary values
@Test
void testMinimumAmount() {
    // Test with exact minimum amount
}

// 3. Test exception paths
@Test
void testInvalidVPA() {
    // Test with invalid VPA format
}
```

---

### Error 3.2: Branch Coverage Low
```
[ERROR] branches covered ratio is 0.65, but expected minimum is 0.80
```

**Cause:** If/else branches not fully tested

**Solution:**
```java
// Original code with branches
public String getStatus(int code) {
    if (code == 0) return "SUCCESS";
    else if (code == 1) return "PENDING";
    else return "FAILED";
}

// Tests needed for 100% branch coverage:
@Test void testStatusSuccess() { assertEquals("SUCCESS", getStatus(0)); }
@Test void testStatusPending() { assertEquals("PENDING", getStatus(1)); }
@Test void testStatusFailed()  { assertEquals("FAILED", getStatus(99)); }
```

---

## 4. Docker Build Failures

### Error 4.1: Docker Build Failed
```
ERROR: failed to solve: failed to compute cache key: 
failed to calculate checksum of ref: not found
```

**Cause:** File referenced in Dockerfile doesn't exist

**Solution:**
```dockerfile
# Ensure build happens before Docker build
# Check that JAR file exists
RUN ls -la target/  # Debug step

# Verify COPY paths are correct
COPY target/*.jar app.jar
```

---

### Error 4.2: ECR Login Failed
```
Error: Cannot perform an interactive login from a non TTY device
```

**Cause:** AWS credentials not configured or expired

**Solution:**
```yaml
# Verify secrets are configured in GitHub
# Settings > Secrets > Actions
# Required secrets:
# - AWS_ACCESS_KEY_ID
# - AWS_SECRET_ACCESS_KEY

# Use official AWS action
- uses: aws-actions/configure-aws-credentials@v4
  with:
    aws-access-key-id: ${{ secrets.AWS_ACCESS_KEY_ID }}
    aws-secret-access-key: ${{ secrets.AWS_SECRET_ACCESS_KEY }}
    aws-region: ap-south-1
```

---

## 5. AWS Deployment Failures

### Error 5.1: ECS Service Update Failed
```
Service update could not be completed: 
Task failed to start. ResourceInitializationError
```

**Cause:** Container can't start (missing env vars, wrong port, health check failing)

**Solution:**
```bash
# Check CloudWatch logs
aws logs tail /ecs/upi-payment-service --follow

# Verify task definition
aws ecs describe-task-definition --task-definition upi-payment-service

# Check container health
aws ecs describe-services --cluster upi-cluster --services upi-payment-service
```

---

### Error 5.2: Health Check Failed
```
Task failed ELB health checks: 
Target Group health checks failed
```

**Cause:** Application not responding on health endpoint

**Solution:**
```json
// Adjust health check settings in task-definition.json
"healthCheck": {
  "command": ["CMD-SHELL", "curl -f http://localhost:8080/actuator/health || exit 1"],
  "interval": 30,
  "timeout": 10,
  "retries": 5,
  "startPeriod": 120  // Increase start period
}
```

---

## 6. Flaky Test Issues

### Pattern 6.1: Time-Based Flakiness
```
Expected: 2024-01-15T10:30:00
Actual:   2024-01-15T10:30:01
```

**Solution:**
```java
// ❌ Bad: Exact time matching
assertEquals(expectedTime, actualTime);

// ✅ Good: Time range
assertTrue(actualTime.isAfter(startTime.minusSeconds(1)));
assertTrue(actualTime.isBefore(endTime.plusSeconds(1)));
```

---

### Pattern 6.2: Order-Dependent Tests
```
Test passes when run alone, fails when run with suite
```

**Solution:**
```java
// ✅ Reset state in @BeforeEach
@BeforeEach
void setUp() {
    // Reset to clean state
    service = new UPITransferService();
    database.clear();
}

// ✅ Use @TestInstance for shared expensive resources
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
```

---

### Pattern 6.3: Async/Race Condition Flakiness
```
java.util.ConcurrentModificationException
```

**Solution:**
```java
// ✅ Use thread-safe collections
List<String> results = new CopyOnWriteArrayList<>();

// ✅ Use proper synchronization
CountDownLatch latch = new CountDownLatch(expectedCount);
assertTrue(latch.await(30, TimeUnit.SECONDS));

// ✅ Use Awaitility for async assertions
await().atMost(5, SECONDS).until(() -> service.isComplete());
```

---

## 7. Pipeline Configuration Errors

### Error 7.1: Invalid YAML Syntax
```
Invalid workflow file: .github/workflows/ci-pipeline.yml
mapping values are not allowed in this context
```

**Solution:**
```yaml
# ❌ Bad: Missing space after colon
environment:production

# ✅ Good: Space after colon
environment: production

# ❌ Bad: Inconsistent indentation
jobs:
  build:
   runs-on: ubuntu-latest  # 1 space
    steps:                  # 4 spaces - inconsistent!

# ✅ Good: Consistent 2-space indentation
jobs:
  build:
    runs-on: ubuntu-latest
    steps:
```

---

### Error 7.2: Secret Not Found
```
Error: Input required and not supplied: aws-access-key-id
```

**Solution:**
1. Go to GitHub Repository → Settings → Secrets → Actions
2. Add required secrets:
   - `AWS_ACCESS_KEY_ID`
   - `AWS_SECRET_ACCESS_KEY`
3. Verify secret names match workflow exactly (case-sensitive)

---

### Error 7.3: Artifact Upload/Download Failed
```
Error: Unable to find any artifacts for the associated workflow
```

**Solution:**
```yaml
# Ensure artifact names match exactly
- uses: actions/upload-artifact@v4
  with:
    name: test-results  # This name...
    path: target/surefire-reports/

- uses: actions/download-artifact@v4
  with:
    name: test-results  # ...must match this name
```

---

## 🚨 Emergency Procedures

### Pipeline Completely Blocked
```bash
# Option 1: Skip tests (emergency only!)
workflow_dispatch with skip_tests: true

# Option 2: Rollback to last working version
aws ecs update-service --cluster upi-cluster \
  --service upi-payment-service \
  --task-definition upi-payment-service:PREVIOUS_VERSION
```

### Quick Diagnostic Commands
```bash
# Check recent workflow runs
gh run list --limit 10

# View specific run logs
gh run view RUN_ID --log

# Re-run failed jobs
gh run rerun RUN_ID --failed
```

---

## 📞 Escalation Path

| Issue Type | First Contact | Escalate To |
|------------|---------------|-------------|
| Build Failures | Development Team | Tech Lead |
| Test Failures | QA Team | Test Manager |
| Coverage Issues | Development Team | Tech Lead |
| AWS Issues | DevOps Team | Cloud Architect |
| Security Issues | DevOps Team | Security Team |

---

*Document Version: 1.0 | Last Updated: 2024*
