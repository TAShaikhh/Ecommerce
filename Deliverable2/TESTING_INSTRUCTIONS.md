# Testing Instructions

This guide explains how to run smoke checks, main-flow checks, robustness checks, and module test suites for Deliverable 2.

Before starting services, it is recommended to clear any old SQLite files from previous runs.

Windows PowerShell:
```powershell
Remove-Item -Force iam\data\*.db,catalogue\data\*.db,auction\data\*.db,payment\data\*.db -ErrorAction SilentlyContinue
```

Linux/macOS:
```bash
rm -f iam/data/*.db catalogue/data/*.db auction/data/*.db payment/data/*.db
```

Shell note:
- Use the PowerShell command blocks when you are in PowerShell.
- Use the Bash/macOS command blocks when you are in macOS Terminal, Linux, WSL, or Git Bash.

If `./mvnw` reports that `JAVA_HOME` is not set correctly on macOS, run this once in your terminal before using the Maven wrapper:

```bash
export JAVA_HOME=$(/usr/libexec/java_home)
```

## TA Quick Verification (PowerShell)

The following command sequence is what worked on our local Windows + PowerShell test environment for end-to-end verification.

If this quick path does not work in your environment, please use the full step-by-step sections below.

```powershell
# Reset local SQLite files so tests start from a clean state.
Remove-Item -Force iam\data\*.db,catalogue\data\*.db,auction\data\*.db,payment\data\*.db -ErrorAction SilentlyContinue

# Start all services (IAM, Catalogue, Auction, Payment, Gateway).
.\run_all.bat

# Verify core health endpoints quickly.
bash scripts/main_flow/00_smoke.sh

# Execute the end-to-end happy-path flow (UC1-UC7).
bash scripts/main_flow/full_flow.sh

# Run robustness checks for wrong inputs/corner cases.
bash scripts/robustness/test_robustness.sh

# Run IAM automated tests.
Push-Location iam; .\mvnw.cmd -q test; Pop-Location

# Run Catalogue automated tests.
Push-Location catalogue; .\mvnw.cmd -q test; Pop-Location

# Run Auction automated tests.
Push-Location auction; .\mvnw.cmd -q test; Pop-Location

# Run Payment automated tests.
Push-Location payment; .\mvnw.cmd -q test; Pop-Location

# Run Gateway automated tests.
Push-Location gateway; .\mvnw.cmd -q test; Pop-Location
```

## TA Quick Verification (macOS)

The following command sequence is the equivalent quick path for macOS Terminal or another POSIX shell.

```bash
# If ./mvnw complains about JAVA_HOME, initialize it once in this terminal:
export JAVA_HOME=$(/usr/libexec/java_home)

# Reset local SQLite files so tests start from a clean state.
rm -f iam/data/*.db catalogue/data/*.db auction/data/*.db payment/data/*.db

# Start all services (IAM, Catalogue, Auction, Payment, Gateway).
./run_all.sh

# In a second terminal, verify health, the main flow, and robustness checks.
bash scripts/main_flow/00_smoke.sh
bash scripts/main_flow/full_flow.sh
bash scripts/robustness/test_robustness.sh

# Run all automated test suites.
for service in iam catalogue auction payment gateway; do
  echo "=== Testing $service ==="
  (cd "$service" && ./mvnw -q test)
done
```

## API Walkthrough Shell Note

If you are manually following the API examples from `README.md`:
- Use the Bash / zsh examples for macOS Terminal, Linux shells, WSL, or Git Bash.
- Use the PowerShell examples for Windows PowerShell or PowerShell 7.
- Do not mix variable syntax between shells. For example, Bash uses `RUN_ID=$(date +%s)` while PowerShell uses `$RUN_ID = [int][double]::Parse((Get-Date -UFormat %s))`.

## 1. Prerequisites

- Java 17+ installed.
- Use Maven wrappers included in each service (`mvnw`/`mvnw.cmd`).
- `curl` available.
- For bash scripts (`*.sh`): `bash` + `python` or `python3`.

## 2. Start Services

### Optional (Recommended) Clean Data Reset

Use this before a full re-run to avoid stale local SQLite schema/data issues from older runs.

Windows PowerShell:
```powershell
Remove-Item -Force iam\data\*.db,catalogue\data\*.db,auction\data\*.db,payment\data\*.db -ErrorAction SilentlyContinue
```

Windows Command Prompt (`cmd.exe`):
```cmd
del /f /q iam\data\*.db catalogue\data\*.db auction\data\*.db payment\data\*.db
```

Linux/macOS:
```bash
rm -f iam/data/*.db catalogue/data/*.db auction/data/*.db payment/data/*.db
```

### Start Commands

### Windows
```cmd
run_all.bat
```

### Linux/macOS
```bash
./run_all.sh
```

Expected result:
- Gateway on `http://localhost:8080`
- IAM on `http://localhost:8081`
- Catalogue on `http://localhost:8082`
- Auction on `http://localhost:8083`
- Payment on `http://localhost:8084`

## 3. Smoke Checks

```bash
bash scripts/main_flow/00_smoke.sh
```

Expected result:
- Successful responses from gateway health/ping endpoints and IAM DB health endpoint.

## 4. Main Flow Use-Case Script (Happy Path)

```bash
bash scripts/main_flow/full_flow.sh
```

Expected result:
- End-to-end UC1-UC7 flow executes:
  - signup/login
  - create item + auction
  - browse/select/bid
  - auction result
  - payment and receipt

## 5. Robustness Script (Wrong Inputs / Corner Cases)

```bash
bash scripts/robustness/test_robustness.sh
```

Expected result:
- Script prints pass/fail lines for invalid input scenarios.
- Most checks should return expected HTTP error codes (400/403/404/409 as applicable).

## 6. Automated Module Tests

Run from project root:

### Windows Command Prompt (`cmd.exe`)
```cmd
cd iam && mvnw.cmd -q test && cd ..
cd catalogue && mvnw.cmd -q test && cd ..
cd auction && mvnw.cmd -q test && cd ..
cd payment && mvnw.cmd -q test && cd ..
cd gateway && mvnw.cmd -q test && cd ..
```

### Windows PowerShell
```powershell
$services = 'iam','catalogue','auction','payment','gateway'
foreach ($s in $services) {
  Write-Host "=== Testing $s ==="
  Push-Location $s
  try {
    .\mvnw.cmd -q test
    if ($LASTEXITCODE -ne 0) { throw "Tests failed in $s" }
  } finally {
    Pop-Location
  }
}
```

### Linux/macOS
```bash
# If needed on macOS:
export JAVA_HOME=$(/usr/libexec/java_home)

cd iam && ./mvnw -q test && cd ..
cd catalogue && ./mvnw -q test && cd ..
cd auction && ./mvnw -q test && cd ..
cd payment && ./mvnw -q test && cd ..
cd gateway && ./mvnw -q test && cd ..
```

Expected result:
- All modules complete with no test failures.

## 7. Optional Seed Data

If you need predefined records for manual checks:
- `scripts/seed_data.sql`

Apply it only if your local run requires pre-populated data.

## 8. Troubleshooting Notes

- If a port is already in use, stop existing Java processes and restart services.
- If `python` is unavailable in bash, install Python or use `python3`.
- If service startup fails, inspect logs in `logs/` (created by startup scripts).
