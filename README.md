# PrimeBid - Forward Auction System

A microservice-based forward auctioning system built with Spring Boot 4.0.3, Java 17, and SQLite.

## Architecture

```
                      +------------------+
                      | Gateway (Facade) | :8080
                      +---------+--------+
                                |
          +---------------------+---------------------+---------------------+
          |                     |                     |                     |
   +------+-------+      +------+-------+      +------+-------+      +------+-------+
   | IAM Service  |      | Catalogue    |      | Auction      |      | Payment      |
   | :8081        |      | Service :8082|      | Service :8083|      | Service :8084|
   +--------------+      +--------------+      +--------------+      +--------------+
```

**5 Microservices:**
- **Gateway** (port 8080) - Facade pattern; single entry point for all client requests
- **IAM** (port 8081) - User authentication, registration, password reset
- **Catalogue** (port 8082) - Item management, search, status tracking
- **Auction** (port 8083) - Auction lifecycle, bidding with Strategy pattern validation, scheduled expiry with Observer pattern
- **Payment** (port 8084) - Payment processing, receipt generation

## Prerequisites

- **Java 17** (or higher)
- **Maven 3.8+** (optional if using the included Maven wrappers `mvnw`/`mvnw.cmd`)
- **curl** for endpoint/script checks
- **bash + python/python3** for `scripts/*.sh`

If `./mvnw` reports that `JAVA_HOME` is not set correctly on macOS, run:

```bash
export JAVA_HOME=$(/usr/libexec/java_home)
```

## Quick Start (Docker - Recommended)

For EECS 4413 Deliverable 3, the entire system (Frontend + 5 Backend Microservices) is containerized. 

1. Ensure **Docker Desktop** is running.
2. Build and start all services via Docker Compose:

```bash
docker-compose up -d --build
```

### Launch Backend and Frontend with Docker

If you want the exact launch flow separated by concern:

1. Start the full backend stack plus frontend:

```bash
docker compose up -d --build
```

2. Verify the backend is running:

```bash
curl http://localhost:8080/health
curl http://localhost:8081/actuator/health
curl http://localhost:8082/actuator/health
curl http://localhost:8083/actuator/health
curl http://localhost:8084/actuator/health
```

3. Open the frontend:

```text
http://localhost:3000
```

4. If you only need to restart the frontend container after UI changes:

```bash
docker compose build frontend
docker compose up -d frontend
```

5. If you only need to restart the backend services:

```bash
docker compose up -d iam catalogue auction payment gateway
```

### Apple Silicon (Mac ARM) Note

If you are building this project on a Mac with an ARM processor (M1, M2, M3, or newer), keep the Docker base images below as-is:

- The Java services should use `eclipse-temurin:17-jre` for their runtime stage.
- Do not switch them to `eclipse-temurin:17-jre-alpine`, because that tag does not provide the required Linux `arm64` manifest and Docker builds fail with `no match for platform in manifest`.
- The frontend should use `node:20-alpine`, because this project uses Next.js 16, which requires Node `>= 20.9.0`.

If someone updates the Dockerfiles later, preserving these image tags will keep the project buildable on Apple Silicon Macs.

### Windows Docker Note

The Dockerfiles in this repository are currently configured around the Mac setup used during development and testing.

If you are building on Windows, review the Dockerfiles before running a full build, especially:

- Java runtime image tags used in the backend service Dockerfiles
- frontend Node image/version assumptions
- shell command differences between macOS/Linux and Windows

In other words, the current Dockerfile setup should be treated as the Mac-first configuration. If a Windows build fails, update the Dockerfiles to match the platform/runtime requirements of your Windows Docker environment before rebuilding.

### Accessing the System
- **Frontend (UI)**: [http://localhost:3000](http://localhost:3000)
- **Gateway API**: `http://localhost:8080` (Internal microservices run on 8081-8084 and are hidden via Docker network, accessible only through the Gateway).

To view the backend logs:
```bash
docker-compose logs -f gateway
```

To stop the system:
```bash
docker-compose down
```

## Running the Validations (Postman & Scripts)
As per Deliverable 3 requirements, testing scripts are included:
1. **Postman**: Import `PrimeBid_Tests.postman_collection.json` into Postman to run API-level scenarios (Invalid budgets, missing auth headers, catalogue fetches).
2. **cURL Script**: Run `./test_cases.sh` to execute a shell-based robustness and concurrency test.

## Running Locally (Without Docker)

Before starting the services, it is recommended to clear any old SQLite files from previous runs.

Windows PowerShell:
```powershell
Remove-Item -Force iam\data\*.db,catalogue\data\*.db,auction\data\*.db,payment\data\*.db -ErrorAction SilentlyContinue
```

Linux/macOS:
```bash
rm -f iam/data/*.db catalogue/data/*.db auction/data/*.db payment/data/*.db
```

### Launch Backend Locally

The backend consists of `iam`, `catalogue`, `auction`, `payment`, and `gateway`.

1. Build all backend services:

```bash
for service in iam catalogue auction payment gateway; do
  (cd "$service" && ./mvnw clean package -DskipTests)
done
```

2. Start all backend services:

```bash
./run_all.sh
```

3. Verify the backend is available:

```bash
curl http://localhost:8080/health
curl http://localhost:8080/ping/iam
curl http://localhost:8080/ping/catalogue
curl http://localhost:8080/ping/auction
curl http://localhost:8080/ping/payment
```

Windows users can use:

```cmd
run_all.bat
```

### Launch Frontend Locally

If you want to run the frontend outside Docker while keeping the backend local or Dockerized:

1. Open a new terminal.
2. Go into the frontend app:

```bash
cd frontend
```

3. Install dependencies:

```bash
npm install
```

4. Start the Next.js development server:

```bash
npm run dev
```

5. Open:

```text
http://localhost:3000
```

The frontend talks to the gateway on `http://localhost:8080`, so make sure the backend is already running before opening the UI.

### Build & Run All Services Locally

**Linux/macOS:**
```bash
for service in iam catalogue auction payment gateway; do cd $service && ./mvnw clean package -DskipTests && cd ..; done
./run_all.sh
```

**Windows:**
```cmd
run_all.bat
```

### Verify Services Are Running
```bash
curl "http://localhost:8080/health"
|----|-------------|---------------------|
| UC1 | User Registration & Login | `POST /auth/signup`, `POST /auth/login`, `POST /auth/reset-password`, `GET /auth/me`, `POST /auth/logout` |
| UC2 | Browse/Search Catalogue | `GET /catalogue`, `GET /catalogue?keyword=X`, `GET /catalogue/items/{id}`, `POST /catalogue/select/{id}` |
| UC3 | Place Bids | `POST /bid`, `GET /bid/history/{itemId}` |
| UC4 | Auction End | `GET /auction-result/{itemId}` (automatic via scheduler) |
| UC5 | Payment | `GET /payment-page/{itemId}`, `POST /pay` |
| UC6 | Receipt | `GET /receipt/{paymentId}` |
| UC7 | Create Item & Auction | `POST /items` |

## API Reference

The API examples below are split by shell because Bash/zsh and PowerShell use different variable syntax.

### Authentication (UC1)

**Bash / zsh (macOS, Linux, Git Bash)**

```bash
# Repeatable demo values for the curl walkthrough below.
RUN_ID=$(date +%s)
SELLER_USER="seller_$RUN_ID"
BUYER_USER="buyer_$RUN_ID"
PYTHON_BIN=$(command -v python || command -v python3)

# Signup seller
curl -X POST "http://localhost:8080/auth/signup" \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"$SELLER_USER\",\"password\":\"pass1234\",\"firstName\":\"Alice\",\"lastName\":\"Smith\",\"address\":\"123 Main St\"}"

# Signup buyer
curl -X POST "http://localhost:8080/auth/signup" \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"$BUYER_USER\",\"password\":\"pass1234\",\"firstName\":\"Bob\",\"lastName\":\"Jones\",\"address\":\"456 Market St\"}"

# Login seller (returns a gateway session token)
SELLER_TOKEN=$(curl -s -X POST "http://localhost:8080/auth/login" \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"$SELLER_USER\",\"password\":\"pass1234\"}" | "$PYTHON_BIN" -c "import sys, json; print(json.load(sys.stdin)['token'])")

# Login buyer
BUYER_TOKEN=$(curl -s -X POST "http://localhost:8080/auth/login" \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"$BUYER_USER\",\"password\":\"pass1234\"}" | "$PYTHON_BIN" -c "import sys, json; print(json.load(sys.stdin)['token'])")

# Save the returned session token from /auth/login before calling protected endpoints.
TOKEN="$SELLER_TOKEN"

# Check seller session
curl "http://localhost:8080/auth/me" -H "Authorization: Bearer $SELLER_TOKEN"

# Reset seller password
curl -X POST "http://localhost:8080/auth/reset-password" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $SELLER_TOKEN" \
  -d "{\"username\":\"$SELLER_USER\",\"currentPassword\":\"pass1234\",\"newPassword\":\"newpass123\"}"
```

**PowerShell**

```powershell
$RUN_ID = [int][double]::Parse((Get-Date -UFormat %s))
$SELLER_USER = "seller_$RUN_ID"
$BUYER_USER = "buyer_$RUN_ID"
$PYTHON_BIN = if (Get-Command python -ErrorAction SilentlyContinue) { "python" } else { "python3" }

$signupSeller = @{
  username = $SELLER_USER
  password = "pass1234"
  firstName = "Alice"
  lastName = "Smith"
  address = "123 Main St"
} | ConvertTo-Json

Invoke-RestMethod -Method Post `
  -Uri "http://localhost:8080/auth/signup" `
  -ContentType "application/json" `
  -Body $signupSeller

$signupBuyer = @{
  username = $BUYER_USER
  password = "pass1234"
  firstName = "Bob"
  lastName = "Jones"
  address = "456 Market St"
} | ConvertTo-Json

Invoke-RestMethod -Method Post `
  -Uri "http://localhost:8080/auth/signup" `
  -ContentType "application/json" `
  -Body $signupBuyer

$sellerLogin = @{
  username = $SELLER_USER
  password = "pass1234"
} | ConvertTo-Json
$SELLER_TOKEN = (Invoke-RestMethod -Method Post `
  -Uri "http://localhost:8080/auth/login" `
  -ContentType "application/json" `
  -Body $sellerLogin).token

$buyerLogin = @{
  username = $BUYER_USER
  password = "pass1234"
} | ConvertTo-Json
$BUYER_TOKEN = (Invoke-RestMethod -Method Post `
  -Uri "http://localhost:8080/auth/login" `
  -ContentType "application/json" `
  -Body $buyerLogin).token

Invoke-RestMethod -Method Get `
  -Uri "http://localhost:8080/auth/me" `
  -Headers @{ Authorization = "Bearer $SELLER_TOKEN" }

$resetBody = @{
  username = $SELLER_USER
  currentPassword = "pass1234"
  newPassword = "newpass123"
} | ConvertTo-Json

Invoke-RestMethod -Method Post `
  -Uri "http://localhost:8080/auth/reset-password" `
  -ContentType "application/json" `
  -Headers @{ Authorization = "Bearer $SELLER_TOKEN" } `
  -Body $resetBody
```

### Create Item & Auction (UC7)

**Bash / zsh**

```bash
# Create item as the seller and capture the new item id.
ITEM_ID=$(curl -s -X POST "http://localhost:8080/items" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $SELLER_TOKEN" \
  -d '{
    "title":"Gaming Laptop",
    "description":"RTX 4090 laptop","condition":"NEW",
    "keywords":"laptop gaming","shippingCost":15.0,
    "expeditedShippingCost":10.0,"shippingDays":5,
    "startingPrice":500.0,"auctionDurationSeconds":15
  }' | "$PYTHON_BIN" -c "import sys, json; print(json.load(sys.stdin)['itemId'])")

echo "$ITEM_ID"
```

**PowerShell**

```powershell
$createItemBody = @{
  title = "Gaming Laptop"
  description = "RTX 4090 laptop"
  condition = "NEW"
  keywords = "laptop gaming"
  shippingCost = 15.0
  expeditedShippingCost = 10.0
  shippingDays = 5
  startingPrice = 500.0
  auctionDurationSeconds = 15
} | ConvertTo-Json

$ITEM_ID = (Invoke-RestMethod -Method Post `
  -Uri "http://localhost:8080/items" `
  -ContentType "application/json" `
  -Headers @{ Authorization = "Bearer $SELLER_TOKEN" } `
  -Body $createItemBody).itemId

$ITEM_ID
```

### Browse & Search (UC2)

**Bash / zsh**

```bash
# Search by keyword
curl "http://localhost:8080/catalogue?keyword=laptop"

# Get item detail with auction state
curl "http://localhost:8080/catalogue/items/$ITEM_ID"

# Select item for bidding (requires auth)
curl -X POST "http://localhost:8080/catalogue/select/$ITEM_ID" \
  -H "Authorization: Bearer $BUYER_TOKEN"
```

**PowerShell**

```powershell
Invoke-RestMethod -Method Get -Uri "http://localhost:8080/catalogue?keyword=laptop"

Invoke-RestMethod -Method Get -Uri "http://localhost:8080/catalogue/items/$ITEM_ID"

Invoke-RestMethod -Method Post `
  -Uri "http://localhost:8080/catalogue/select/$ITEM_ID" `
  -Headers @{ Authorization = "Bearer $BUYER_TOKEN" }
```

### Place Bids (UC3)

**Bash / zsh**

```bash
# Place bid (must select item first)
curl -X POST "http://localhost:8080/bid" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $BUYER_TOKEN" \
  -d "{\"itemId\":$ITEM_ID,\"amount\":550}"

# View bid history
curl "http://localhost:8080/bid/history/$ITEM_ID"
```

**PowerShell**

```powershell
$bidBody = @{
  itemId = $ITEM_ID
  amount = 550
} | ConvertTo-Json

Invoke-RestMethod -Method Post `
  -Uri "http://localhost:8080/bid" `
  -ContentType "application/json" `
  -Headers @{ Authorization = "Bearer $BUYER_TOKEN" } `
  -Body $bidBody

Invoke-RestMethod -Method Get -Uri "http://localhost:8080/bid/history/$ITEM_ID"
```

### Payment & Receipt (UC5, UC6)

**Bash / zsh**

```bash
# Wait for the 15-second auction to expire before checking result/payment.
sleep 20

# View auction result
curl "http://localhost:8080/auction-result/$ITEM_ID" -H "Authorization: Bearer $BUYER_TOKEN"

# View payment page (winner only)
curl "http://localhost:8080/payment-page/$ITEM_ID" -H "Authorization: Bearer $BUYER_TOKEN"

# Submit payment and capture the generated payment id
PAYMENT_ID=$(curl -s -X POST "http://localhost:8080/pay" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $BUYER_TOKEN" \
  -d "{\"itemId\":$ITEM_ID,\"expedited\":true,\"cardName\":\"Bob Jones\",\"cardNumber\":\"4111111111111111\",\"expiryDate\":\"12/28\",\"securityCode\":\"123\"}" | "$PYTHON_BIN" -c "import sys, json; print(json.load(sys.stdin)['paymentId'])")

echo "$PAYMENT_ID"

# View receipt
curl "http://localhost:8080/receipt/$PAYMENT_ID" -H "Authorization: Bearer $BUYER_TOKEN"
```

**PowerShell**

```powershell
Start-Sleep -Seconds 20

Invoke-RestMethod -Method Get `
  -Uri "http://localhost:8080/auction-result/$ITEM_ID" `
  -Headers @{ Authorization = "Bearer $BUYER_TOKEN" }

Invoke-RestMethod -Method Get `
  -Uri "http://localhost:8080/payment-page/$ITEM_ID" `
  -Headers @{ Authorization = "Bearer $BUYER_TOKEN" }

$payBody = @{
  itemId = $ITEM_ID
  expedited = $true
  cardName = "Bob Jones"
  cardNumber = "4111111111111111"
  expiryDate = "12/28"
  securityCode = "123"
} | ConvertTo-Json

$PAYMENT_ID = (Invoke-RestMethod -Method Post `
  -Uri "http://localhost:8080/pay" `
  -ContentType "application/json" `
  -Headers @{ Authorization = "Bearer $BUYER_TOKEN" } `
  -Body $payBody).paymentId

$PAYMENT_ID

Invoke-RestMethod -Method Get `
  -Uri "http://localhost:8080/receipt/$PAYMENT_ID" `
  -Headers @{ Authorization = "Bearer $BUYER_TOKEN" }
```

## Running Tests

```bash
# Run all tests for a specific service
cd iam && ./mvnw test && cd ..
cd catalogue && ./mvnw test && cd ..
cd auction && ./mvnw test && cd ..
cd payment && ./mvnw test && cd ..

# Or run all tests at once
for service in iam catalogue auction payment; do
  echo "Testing $service..."
  cd $service && ./mvnw test && cd ..
done
```

## Demo Scripts

```bash
# Full happy path (UC1 -> UC7)
bash scripts/main_flow/full_flow.sh

# Robustness tests (edge cases)
bash scripts/robustness/test_robustness.sh
```

## Deliverable 2 Artifacts

- AI acknowledgement: `AI_USAGE_ACKNOWLEDGEMENT.md`
- Test execution guide: `TESTING_INSTRUCTIONS.md`
- UML diagram exports (PNG): `docs/diagrams/*.png`
- Main flow script: `scripts/main_flow/full_flow.sh`
- Robustness script: `scripts/robustness/test_robustness.sh`
- Optional seed SQL: `scripts/seed_data.sql`

## How Graders Should Run and Verify

### TA Quick Verification (PowerShell)

This command sequence is what worked on our local Windows + PowerShell environment for end-to-end verification.

If this quick path does not work in your environment, follow the full instructions in `TESTING_INSTRUCTIONS.md`.

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

### TA Quick Verification (macOS)

This is the equivalent quick path for macOS Terminal or another POSIX shell.

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

### Full Cross-Platform Steps

1. (Recommended for repeat runs) reset local DB files:
```bash
rm -f iam/data/*.db catalogue/data/*.db auction/data/*.db payment/data/*.db
```
or on Windows Command Prompt (`cmd.exe`):
```cmd
del /f /q iam\data\*.db catalogue\data\*.db auction\data\*.db payment\data\*.db
```
or on Windows PowerShell:
```powershell
Remove-Item -Force iam\data\*.db,catalogue\data\*.db,auction\data\*.db,payment\data\*.db -ErrorAction SilentlyContinue
```

2. Start all services:
```bash
# If needed on macOS:
export JAVA_HOME=$(/usr/libexec/java_home)

./run_all.sh
```
or on Windows:
```cmd
run_all.bat
```

3. Run smoke checks:
```bash
bash scripts/main_flow/00_smoke.sh
```

4. Run main use-case flow:
```bash
bash scripts/main_flow/full_flow.sh
```

5. Run robustness checks:
```bash
bash scripts/robustness/test_robustness.sh
```

6. Run automated tests in each service:
```bash
# If needed on macOS:
export JAVA_HOME=$(/usr/libexec/java_home)

cd iam && ./mvnw -q test && cd ..
cd catalogue && ./mvnw -q test && cd ..
cd auction && ./mvnw -q test && cd ..
cd payment && ./mvnw -q test && cd ..
cd gateway && ./mvnw -q test && cd ..
```
Windows Command Prompt (`cmd.exe`):
```cmd
cd iam && mvnw.cmd -q test && cd ..
cd catalogue && mvnw.cmd -q test && cd ..
cd auction && mvnw.cmd -q test && cd ..
cd payment && mvnw.cmd -q test && cd ..
cd gateway && mvnw.cmd -q test && cd ..
```

Windows PowerShell:
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

## Design Patterns

| Pattern | Where | Purpose |
|---------|-------|---------|
| **Facade** | Gateway service | Single entry point orchestrating all backend services |
| **Repository** | All services | Data access abstraction with JdbcTemplate |
| **Strategy** | Auction service | `BidValidationStrategy` with `IntegerBidValidator` and `IncreasingBidValidator` |
| **Observer** | Auction service | `AuctionEndObserver` with `CatalogueNotifier` for auction expiry events |

**Considered but not used:**
- **Abstract Factory** - Considered for different auction types (forward, dutch, sealed) but only forward auctions are required
- **Singleton** - Considered for DB connection pooling but Spring IoC manages bean lifecycle as singletons by default

## Technology Stack

| Technology | Purpose |
|-----------|---------|
| Spring Boot 4.0.3 | Application framework |
| Java 17 | Programming language |
| SQLite | Lightweight embedded database |
| Spring JDBC (JdbcTemplate) | Data access |
| Spring Security Crypto | BCrypt password hashing |
| Spring Validation | Input validation |
| RestTemplate | Inter-service HTTP communication |
| JUnit 5 + MockMvc | Testing |

## HATEOAS

All gateway business-flow responses include `_links` with:
- `self` - Link to the current resource
- Next logical action links (e.g., after login -> catalogue, after bid -> bidHistory)
- Related resource links (e.g., item -> auction)

## Project Structure

```
forward-auction-system/
|-- gateway/          # Port 8080 - API Gateway (Facade)
|-- iam/              # Port 8081 - Identity & Access Management
|-- catalogue/        # Port 8082 - Item Catalogue
|-- auction/          # Port 8083 - Auction Management
|-- payment/          # Port 8084 - Payment Processing
|-- scripts/          # Curl demo and test scripts
|   |-- main_flow/    # Happy path scripts
|   `-- robustness/   # Edge case scripts
|-- docs/
|   `-- diagrams/     # Deliverable 2 UML diagram exports (PNG)
|-- TESTING_INSTRUCTIONS.md   # Detailed test execution guide
|-- AI_USAGE_ACKNOWLEDGEMENT.md  # AI usage statement for Deliverable 2
|-- run_all.sh        # Start all services (Linux/macOS)
|-- run_all.bat       # Start all services (Windows)
`-- README.md         # This file
```
