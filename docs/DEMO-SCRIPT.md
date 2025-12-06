# 🎬 Demo Script for Trainer
## 4-Hour CI/CD Follow-up Session

---

## 📋 Pre-Session Checklist

### Environment Setup (Do 1 hour before session)
- [ ] Fork/clone repo to your GitHub account
- [ ] Ensure Java 17 installed locally
- [ ] Ensure Maven installed locally
- [ ] GitHub Actions enabled on repo
- [ ] AWS credentials configured (if showing real deployment)
- [ ] VS Code / IntelliJ open with project loaded
- [ ] Browser tabs ready: GitHub repo, Actions tab

### Materials Ready
- [ ] This demo script open
- [ ] Troubleshooting guide open
- [ ] Zoom/Teams screen share ready

---

## ⏱️ Session Timeline

| Time | Duration | Activity |
|------|----------|----------|
| 0:00 | 15 min | Project walkthrough |
| 0:15 | 30 min | Pipeline architecture explanation |
| 0:45 | 30 min | **DEMO 1:** Happy path - push & watch |
| 1:15 | 15 min | Break |
| 1:30 | 30 min | **DEMO 2:** Quality gate failure |
| 2:00 | 30 min | **DEMO 3:** Test reports walkthrough |
| 2:30 | 30 min | **DEMO 4:** Flaky tests |
| 3:00 | 30 min | Troubleshooting common errors |
| 3:30 | 30 min | Q&A and wrap-up |

---

## 🎯 Demo 1: Happy Path (45 min)

### 1.1 Show Project Structure (5 min)

**Say:** "Let me show you how a typical CI/CD project is structured."

```
Open VS Code, expand folders:
```

**Talk through:**
```
upi-cicd-demo/
├── .github/workflows/     ← "Pipeline definitions live here"
│   └── ci-pipeline.yml    ← "This is our main pipeline"
├── src/
│   ├── main/java/...      ← "Application code"
│   └── test/java/...      ← "All our tests"
├── pom.xml                ← "Build configuration + quality gates"
└── Dockerfile             ← "Container definition for deployment"
```

---

### 1.2 Explain Pipeline Stages (10 min)

**Open:** `.github/workflows/ci-pipeline.yml`

**Say:** "Let me walk you through each stage..."

**Scroll to triggers section:**
```yaml
on:
  push:
    branches: [main, develop]
  pull_request:
    branches: [main]
  workflow_dispatch:  ← "Manual trigger - let me show you this"
```

**Point out:**
- "Push triggers on every commit"
- "PR triggers for code review"
- "Manual trigger for on-demand runs"

**Scroll to jobs section:**
```yaml
jobs:
  build:        ← "Stage 1: Compile the code"
  unit-tests:   ← "Stage 2: Fast tests first - this is SHIFT-LEFT"
  integration-tests:  ← "Stage 3: Slower tests after"
  quality-gate: ← "Stage 4: Coverage check - BLOCKS if below 80%"
  build-image:  ← "Stage 5: Create Docker container"
  deploy:       ← "Stage 6: Push to AWS"
```

**Draw on whiteboard/screen:**
```
BUILD → UNIT TESTS → INTEGRATION → QUALITY GATE → DOCKER → AWS
         ↑                              ↑
    (Fast feedback)              (Blocks bad code)
```

---

### 1.3 Trigger Pipeline - Happy Path (15 min)

**Say:** "Now let's see this in action..."

**Step 1: Make a small change**
```bash
# Open UPITransferService.java
# Change line 20 comment or add a harmless change
# Or simply update README.md
```

**Step 2: Commit and push**
```bash
git add .
git commit -m "Demo: trigger pipeline"
git push
```

**Step 3: Watch pipeline**
```
Go to GitHub → Actions tab
Click on the running workflow
```

**Talk through as it runs:**
- "See BUILD starting... compiling code..."
- "Now UNIT TESTS... these are our shift-left tests, running first"
- "Watch the time - unit tests should complete in ~2 minutes"
- "Now INTEGRATION TESTS... these take longer"
- "QUALITY GATE checking coverage..."
- "All green! ✅ Pipeline passed"

**Show artifacts:**
- Click on workflow run
- Scroll to Artifacts section
- "These are our test reports - we'll look at these in detail later"

---

### 1.4 Show Quality Gates in pom.xml (5 min)

**Open:** `pom.xml`

**Scroll to JaCoCo section:**
```xml
<minimum>${jacoco.minimum.coverage}</minimum>  <!-- 80% -->
```

**Say:** "This is our quality gate. If coverage drops below 80%, the pipeline fails and deployment is blocked."

---

## 🔴 Demo 2: Quality Gate Failure (30 min)

### 2.1 Break a Test Intentionally

**Say:** "Now let me show you what happens when code doesn't meet quality standards..."

**Step 1: Comment out tests to reduce coverage**
```java
// Open UPITransferServiceTest.java
// Comment out entire VPAValidationTests nested class
/*
@Nested
@DisplayName("VPA Validation Tests")
class VPAValidationTests {
    ... comment all tests ...
}
*/
```

**Step 2: Push the change**
```bash
git add .
git commit -m "Demo: break quality gate"
git push
```

**Step 3: Watch pipeline fail**
```
Go to GitHub → Actions tab
Watch unit-tests pass (fewer tests)
Watch quality-gate FAIL ❌
```

**Show the error:**
```
[ERROR] Rule violated for bundle upi-payment-service: 
lines covered ratio is 0.65, but expected minimum is 0.80
```

**Key Teaching Point:**
> "See how the pipeline BLOCKED deployment? This is the quality gate in action. 
> Bad code never reaches production."

**Step 4: Revert the change**
```bash
git revert HEAD
git push
```

---

## 📊 Demo 3: Test Reports (30 min)

### 3.1 Download and Show Allure Report

**Step 1: Download artifact**
```
Go to successful workflow run
Download 'allure-report' artifact
Unzip locally
Open index.html in browser
```

**Walk through Allure Report:**
- **Overview:** "Total tests, pass rate, duration"
- **Suites:** "Organized by test class"
- **Graphs:** "Trend over time"
- **Categories:** "Failed tests grouped by error type"

**Say:** "This report is auto-generated. Your TMs can access this after every pipeline run."

---

### 3.2 Show JaCoCo Coverage Report

**Step 1: Download JaCoCo artifact**
```
Download 'unit-test-results' artifact
Open target/site/jacoco-ut/index.html
```

**Walk through:**
- **Package view:** Coverage per package
- **Class view:** Click on UPITransferService
- **Line view:** "Green = covered, Red = not covered"
- **Branch view:** "Yellow = partial branch coverage"

**Key Teaching Point:**
> "When coverage drops, look at red lines here. These tell you exactly what to test."

---

## 🎲 Demo 4: Flaky Tests (30 min)

### 4.1 Show Flaky Test Examples

**Open:** `FlakyTestExamples.java`

**Say:** "Let me show you what flaky tests look like and how to fix them..."

**Walk through each example:**

**Example 1: Time-dependent**
```java
// Show the FLAKY version
assertEquals(beforeCall.getSecond(), response.getTimestamp().getSecond());
// "This fails if the call crosses a second boundary"

// Show the FIXED version
assertTrue(actualTime.isAfter(startTime.minusSeconds(1)));
// "This allows a tolerance range"
```

**Example 2: Random data**
```java
// Show FLAKY version
Random random = new Random(); // No seed!

// Show FIXED version
Random random = new Random(12345); // Fixed seed!
// "Same seed = same 'random' values every time"
```

**Example 3: Shared state**
```java
// Show the problem
private static int sharedCounter = 0; // Shared across tests!

// Show the fix
int localCounter = 0; // Each test has its own
```

---

### 4.2 Run Flaky Tests (Optional Live Demo)

**Enable flaky tests:**
```java
// Remove @Disabled from one flaky test
@Test
// @Disabled("Intentionally flaky")
void flakyTimestampTest() {
```

**Run multiple times:**
```bash
mvn test -Dtest="FlakyTestExamples" -pl .

# Run 3 times, show different results
```

**Key Teaching Point:**
> "Flaky tests destroy trust in the pipeline. 
> When tests randomly fail, teams start ignoring failures. 
> That's when real bugs slip through."

---

## 🔧 Demo 5: Troubleshooting (30 min)

### 5.1 Walk Through Common Errors

**Open:** `docs/TROUBLESHOOTING.md`

**Cover top issues from their assignments:**

**Error 1: Tests Not Found**
```
[INFO] No tests to run.
```
**Explain:** "Usually wrong naming. Tests must end with Test.java"

**Error 2: Coverage Below Threshold**
```
lines covered ratio is 0.72, but expected minimum is 0.80
```
**Explain:** "Add more tests! Focus on red lines in JaCoCo report"

**Error 3: Docker Build Failed**
```
failed to compute cache key: not found
```
**Explain:** "JAR file missing. Build must run before Docker build"

---

### 5.2 Interactive Exercise

**Give them a scenario:**
> "Your pipeline failed with this error. What would you check?"

Show error:
```
org.junit.jupiter.api.extension.ExtensionConfigurationException: 
Test timed out after 10 seconds
```

**Let them discuss, then reveal:**
- "Check if test is waiting for external resource"
- "Increase timeout or mock the dependency"
- "Look for `Thread.sleep()` in test code"

---

## ❓ Q&A Session (30 min)

### Common Questions to Prepare For:

**Q: "How do we handle long-running E2E tests?"**
> Run them in parallel, use test tags to run subsets, consider running nightly instead of every commit.

**Q: "What if we need to deploy urgently but tests are failing?"**
> Show the `skip_tests` input in workflow_dispatch. Emphasize this is EMERGENCY ONLY.

**Q: "How do we add a new test stage?"**
> Show how to add a new job in the YAML, explain the `needs` dependency.

**Q: "Can we see test trends over time?"**
> Show Allure history feature, explain how to configure trend storage.

---

## 🎁 Wrap-up (10 min)

### Key Takeaways
1. **Shift-Left:** Unit tests run first, catch bugs early
2. **Quality Gates:** Automatic enforcement of standards
3. **Visibility:** Reports show exactly what's tested
4. **Flaky Tests:** Identify and fix immediately
5. **Troubleshooting:** Logs are your friend

### Share Resources
- "I'll share this repo with you"
- "Troubleshooting guide is in /docs folder"
- "Flaky test examples can be your reference"

### Next Steps for Them
- "Try adding a new test to the suite"
- "Practice reading the JaCoCo report"
- "Review the troubleshooting guide"

---

## 📝 Post-Session Notes

After session, note:
- [ ] Questions that came up (add to FAQ)
- [ ] Topics that need more time
- [ ] Additional errors they mentioned
- [ ] Follow-up items promised

---

*Good luck with the session! 🚀*
