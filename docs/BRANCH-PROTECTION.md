# 🔒 Branch Protection Rules Setup

## Overview

Branch protection rules enforce quality gates at the repository level, ensuring no code merges to `main` without passing all checks.

---

## 📋 Recommended Settings for `main` Branch

### Step 1: Navigate to Branch Protection

1. Go to your GitHub repository
2. Click **Settings** → **Branches**
3. Under "Branch protection rules", click **Add rule**
4. Enter `main` as the branch name pattern

---

### Step 2: Configure Protection Rules

Enable these settings:

#### ✅ Require a pull request before merging
- [x] Require approvals: **1** (or more for production)
- [x] Dismiss stale pull request approvals when new commits are pushed
- [x] Require approval of the most recent reviewable push

#### ✅ Require status checks to pass before merging
- [x] Require branches to be up to date before merging
- **Required status checks:**
  - `🔨 Build`
  - `🧪 Unit Tests`
  - `🔗 Integration Tests`
  - `🚦 Quality Gate`

#### ✅ Require conversation resolution before merging
- Ensures all review comments are addressed

#### ✅ Do not allow bypassing the above settings
- Even admins must follow the rules

---

## 🖼️ Visual Guide

```
┌─────────────────────────────────────────────────────────────────┐
│                    BRANCH PROTECTION FLOW                        │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│   Developer                                                      │
│      │                                                           │
│      ▼                                                           │
│   ┌─────────────┐                                               │
│   │ Create PR   │                                               │
│   └─────────────┘                                               │
│      │                                                           │
│      ▼                                                           │
│   ┌─────────────────────────────────────────────────┐           │
│   │           CI/CD PIPELINE RUNS                    │           │
│   │  ┌─────┐  ┌─────┐  ┌─────┐  ┌─────────────┐    │           │
│   │  │Build│→ │Unit │→ │Integ│→ │Quality Gate │    │           │
│   │  │  ✓  │  │Tests│  │Tests│  │  Coverage   │    │           │
│   │  └─────┘  │  ✓  │  │  ✓  │  │    ≥80%     │    │           │
│   │           └─────┘  └─────┘  └─────────────┘    │           │
│   └─────────────────────────────────────────────────┘           │
│      │                                                           │
│      ▼                                                           │
│   ┌─────────────────────┐                                       │
│   │ All Checks Passed?  │                                       │
│   └─────────────────────┘                                       │
│      │           │                                               │
│     YES         NO                                               │
│      │           │                                               │
│      ▼           ▼                                               │
│   ┌───────┐  ┌───────────┐                                      │
│   │ Merge │  │  Blocked  │                                      │
│   │Enabled│  │ Fix Code  │                                      │
│   └───────┘  └───────────┘                                      │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

---

## 📸 Screenshots Reference

### Required Status Checks Selection

When configuring "Require status checks", search and add:

| Check Name | Description |
|------------|-------------|
| `build` | Compilation check |
| `unit-tests` | Unit test execution |
| `integration-tests` | Integration test execution |
| `quality-gate` | Coverage threshold check |

---

## 🎯 Why Branch Protection Matters

| Without Protection | With Protection |
|-------------------|-----------------|
| Anyone can push to main | Changes require PR |
| No code review | Mandatory review |
| Broken code can merge | Tests must pass |
| No quality standards | Coverage enforced |
| Manual enforcement | Automated enforcement |

---

## 🔧 Troubleshooting

### "Merge blocked: Required status check is expected"

**Cause:** Status check hasn't run yet or job name doesn't match

**Solution:**
1. Wait for pipeline to complete
2. Verify job names match exactly (case-sensitive)
3. Push a new commit to trigger checks

### "Cannot merge: Branch is out-of-date"

**Cause:** Main has new commits not in your branch

**Solution:**
```bash
git fetch origin
git rebase origin/main
git push --force-with-lease
```

---

## 📚 Additional Resources

- [GitHub Docs: Managing branch protection rules](https://docs.github.com/en/repositories/configuring-branches-and-merges-in-your-repository/managing-protected-branches/managing-a-branch-protection-rule)
- [GitHub Docs: Required status checks](https://docs.github.com/en/repositories/configuring-branches-and-merges-in-your-repository/managing-protected-branches/about-protected-branches#require-status-checks-before-merging)

---

*Document for CI/CD Training - Branch Protection Rules*
