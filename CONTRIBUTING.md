# CONTRIBUTING.md

## 👋 Welcome to the JGitkins Runner Contribution Guide

Thank you for contributing to **JGitkins Runner**!  
This document outlines the collaboration workflow, development setup, git hooks, and task automation rules used across the project.

---

## 1. Contribution Workflow Overview

### ✔ Branch Strategy
- Follow the **feature branch** workflow:
  - `main` — stable branch
  - `feature/<name>` — feature development
  - `fix/<name>` — bug fixes
  - `docs/<name>` — documentation updates
- Submit Pull Requests into `main`.

### ✔ Commit Message Convention
Use conventional commit style:
```
feat: add new runner registration workflow
fix: correct docker mount path
docs: update README with task table
chore: refactor CLI integration
```

### ✔ Pull Request Rules
- Ensure the latest **README task table is updated automatically** (via git hooks).
- Include clear description, screenshots/logs if necessary.
- All tests must pass before merging.

---

## 2. Development Environment Setup

### ✔ Prerequisites
- Java 17+
- Docker installed
- Node.js 20+ (for task/README automation)
- Task Master AI (optional but recommended)

---

## 3. Shared Git Hook Setup (Required for Contributors)

We maintain a version-controlled git hooks directory: `.githooks/`.

To activate shared hooks on your machine:

```bash
./scripts/setup-dev.sh
```

This command:

1. Configures Git to use `.githooks` as the hooks directory  
2. Enables the shared `pre-commit` hook  
3. Ensures README task section updates before each commit

---

## 4. Pre-commit Hook Behavior

The `pre-commit` hook:

- Detects changes to `.taskmaster/tasks/tasks.json`
- Automatically regenerates the README task table
- Stages `README.md` so your commit always remains clean

No more forgetting to update documentation manually!

---

## 5. Task Management Workflow

We use **Task Master AI** and its `.taskmaster/tasks/tasks.json` as the source of truth.

- Do not manually edit the README task section
- All task updates must be done through Task Master AI
- README updates are automated through the pre-commit hook

---

## 6. Running Tests

```bash
./gradlew test
```

Use Testcontainers only when necessary.

---

## 7. Reporting Issues

Please include:

- Steps to reproduce
- Expected result
- Actual result
- Logs or screenshots
- Relevant task ID, if applicable

---

## 8. Thank you! :)

Your contributions help make JGitkins Runner better for everyone—thank you for being part of the project!
