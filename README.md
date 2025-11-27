# 🚀 JGitkins Server (POC)
**Lightweight Git Server + Minimal Pipeline Runner (POC stage)**

JGitkins Server is an experimental Proof-of-Concept (POC) that combines:
- A lightweight **Git server** powered by JGit
- A minimal [jgitkins runner (pipeline runner)](https://github.com/jgitkins/jgitkins-runner) using Jenkinsfile Runner (Docker-based)

> ⚠️ Important  
> This project is in an **early POC phase**.  
> Only core functionality is partially implemented.  
> Many features described below are **planned but not yet implemented**.

---

## ✨ Overview

JGitkins Server aims to provide:
- A fully Java-based Git repository server
- A simple CI execution engine that runs Jenkinsfiles using Docker (from [jgitkins-runner](https://github.com/jgitkins/jgitkins-runner))
- A clean, modular architecture based on Ports & Adapters
- A foundation for future internal DevOps automation

This POC validates basic feasibility of:
1. Hosting Git repositories with JGit
2. Creating workspaces from Git commits
3. Running pipelines inside disposable Docker containers
4. Returning execution results

---

## ✅ Currently Implemented (POC Completed)
These features **already work** in the current POC:

### ✔ 1. Minimal Git Server (JGit)
- Bare repository creation
- Clone / fetch / push (HTTP smart protocol)
- Branch listing
- Commit checkout into a temporary workspace

### ✔ 2. Basic JGitkins Runner
- Runs a Jenkinsfile using a Docker container
- Uses `jenkins/jenkinsfile-runner` image
- Binds workspace → `/workspace` in container
