# PrimeBid

PrimeBid is a full-stack forward-auction platform where sellers list items, buyers place competitive bids in real time, winners complete checkout, and receipts are generated through a microservices backend.

It includes:

- A modern Next.js frontend
- A Spring Boot gateway for API orchestration
- Dedicated IAM, catalogue, auction, and payment services
- AI-assisted auto-bidding with strategy recommendations
- Dockerized local deployment

## What This Project Does

PrimeBid supports an end-to-end auction workflow:

1. User signup and login
2. Item listing with auction creation
3. Catalogue browsing and keyword search
4. Manual bidding and bid history tracking
5. Automatic auction ending and winner determination
6. Winner-only payment flow
7. Receipt generation with shipping details

## Architecture Overview

Services and default ports:

- Frontend: http://localhost:3000
- Gateway: http://localhost:8080
- IAM Service: http://localhost:8081
- Catalogue Service: http://localhost:8082
- Auction Service: http://localhost:8083
- Payment Service: http://localhost:8084

The frontend communicates with the backend only through the gateway.

## Tech Stack

- Frontend: Next.js, React, TypeScript, Tailwind CSS, Zustand, Framer Motion
- Backend: Java 17, Spring Boot, Spring MVC, Spring JDBC
- Data: SQLite (one DB per service)
- AI: Gemini API (with rule-based fallback for auto-bid assistance)
- DevOps: Docker, Docker Compose
- Testing: JUnit, MockMvc, Postman collection, shell-based flow scripts

## Prerequisites

For Docker workflow:

- Docker Desktop

For local (non-Docker) workflow:

- Java 17+
- Node.js 20+
- npm

## Environment Configuration

Create a root-level .env file:

```env
NEXT_PUBLIC_API_BASE_URL=http://localhost:8080
GEMINI_API_KEY=
```

Notes:

- NEXT_PUBLIC_API_BASE_URL should point to the gateway URL.
- GEMINI_API_KEY is optional. If omitted, AI endpoints use fallback logic.

## Quick Start (Docker Recommended)

1. Clone the repository.
2. Create the .env file shown above.
3. Start the full stack:

```bash
docker compose up -d --build
```

4. Open the app:

```text
http://localhost:3000
```

5. Verify gateway health:

```bash
curl http://localhost:8080/health
```

Expected response:

```json
{"status":"UP"}
```

Stop the stack:

```bash
docker compose down
```

View logs:

```bash
docker compose logs -f
```

## Run Locally (Without Docker)

1. Create the same root-level .env file.

2. Build backend services:

```bash
for service in iam catalogue auction payment gateway; do
  (cd "$service" && ./mvnw clean package -DskipTests)
done
```

3. Start backend services:

macOS/Linux:

```bash
./run_all.sh
```

Windows:

```cmd
run_all.bat
```

4. In a separate terminal, run frontend:

```bash
cd frontend
npm install
npm run dev
```

5. Open:

```text
http://localhost:3000
```

## Health Checks

Use these checks after startup:

```bash
curl http://localhost:8080/health
curl http://localhost:8080/ping/iam
curl http://localhost:8080/ping/catalogue
curl http://localhost:8080/ping/auction
curl http://localhost:8080/ping/payment
```

## Testing

Main flow and robustness scripts:

```bash
bash scripts/main_flow/00_smoke.sh
bash scripts/main_flow/full_flow.sh
bash scripts/robustness/test_robustness.sh
```

Run backend test suites:

Windows PowerShell:

```powershell
$services = 'iam','catalogue','auction','payment','gateway'
foreach ($s in $services) {
  Push-Location $s
  try {
    .\mvnw.cmd -q test
    if ($LASTEXITCODE -ne 0) { throw "Tests failed in $s" }
  } finally {
    Pop-Location
  }
}
```

macOS/Linux:

```bash
for service in iam catalogue auction payment gateway; do
  (cd "$service" && ./mvnw -q test)
done
```

## Project Structure

- iam: authentication and user profile service
- catalogue: item listing and search service
- auction: bidding engine and auction lifecycle service
- payment: payment and receipt service
- gateway: API facade and orchestration layer
- frontend: Next.js client application
- scripts: smoke, main-flow, and robustness test scripts

## Troubleshooting

- Ensure ports 3000, 8080, 8081, 8082, 8083, and 8084 are free.
- If startup becomes inconsistent, clear local SQLite files and restart.

Linux/macOS:

```bash
rm -f iam/data/*.db catalogue/data/*.db auction/data/*.db payment/data/*.db
```

Windows PowerShell:

```powershell
Remove-Item -Force iam\data\*.db,catalogue\data\*.db,auction\data\*.db,payment\data\*.db -ErrorAction SilentlyContinue
```

- If frontend cannot connect, confirm gateway is reachable at http://localhost:8080/health.
- If Gemini API is not configured, auto-bid AI endpoints still work with fallback behavior.
