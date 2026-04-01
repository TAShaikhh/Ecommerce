# PrimeBid

PrimeBid is a forward auction system built with a Next.js frontend, a Spring Boot gateway, and four supporting backend microservices for identity, catalogue, auction, and payment processing.

## Quick Start

### Option 1: Run with Docker (recommended)

Install:

- Docker Desktop

Before you start:

- make sure Docker Desktop is fully running
- make sure ports `3000`, `8080`, `8081`, `8082`, `8083`, and `8084` are free

Steps:

1. Clone the repository.
2. Create a root-level `.env` file:

```env
NEXT_PUBLIC_API_BASE_URL=http://localhost:8080
GEMINI_API_KEY=
```

3. Start the full system:

```bash
docker compose up -d --build
```

4. Wait for the containers to finish starting, then open:

```text
http://localhost:3000
```

5. Verify that the gateway is healthy:

```bash
http://localhost:8080/health
```

Expected result:

```json
{"status":"UP"}
```

To stop the stack:

```bash
docker compose down
```

To view logs:

```bash
docker compose logs -f
```

### Option 2: Run without Docker

Install:

- Java 17 or higher
- Node.js 20 or higher
- npm

Steps:

1. Create the same root-level `.env` file:

```env
NEXT_PUBLIC_API_BASE_URL=http://localhost:8080
GEMINI_API_KEY=
```

2. Build the backend services:

```bash
for service in iam catalogue auction payment gateway; do
  (cd "$service" && ./mvnw clean package -DskipTests)
done
```

3. Start the backend services:

macOS / Linux:

```bash
./run_all.sh
```

Windows:

```cmd
run_all.bat
```

4. In a separate terminal, start the frontend:

```bash
cd frontend
npm install
npm run dev
```

5. Open:

```text
http://localhost:3000
```

6. Verify the backend:

```bash
http://localhost:8080/health
```

Expected result:

```json
{"status":"UP"}
```

## Submission Contents

This repository is prepared for EECS 4413 Deliverable 3 and includes:

- the full PrimeBid source code
- the Deliverable 3 diagram set in [DELIVERABLE3](/Users/tas/Desktop/EcommerceProject/DELIVERABLE3)
- the Deliverable 3 frontend and AI testing document in [Deliverable 3 Frontend and AI Chatbot Test Cases.pdf](/Users/tas/Desktop/EcommerceProject/DELIVERABLE3/Deliverable%203%20Frontend%20and%20AI%20Chatbot%20Test%20Cases.pdf)
- the Deliverable 2 API use-case walkthroughs in [INDIVIDUAL_USE_CASE_TESTING.md](/Users/tas/Desktop/EcommerceProject/Deliverable2/INDIVIDUAL_USE_CASE_TESTING.md) and [INDIVIDUAL_USE_CASE_TESTING_MAC.md](/Users/tas/Desktop/EcommerceProject/Deliverable2/INDIVIDUAL_USE_CASE_TESTING_MAC.md)
- the Deliverable 2 diagram set in [Deliverable2Diagrams](/Users/tas/Desktop/EcommerceProject/Deliverable2/Deliverable2Diagrams)

Deliverable 3 frontend verification was performed manually. No automated frontend end-to-end test suite is included in this submission.

## System Architecture

- Frontend UI: `http://localhost:3000`
- Gateway: `http://localhost:8080`
- IAM Service: `http://localhost:8081`
- Catalogue Service: `http://localhost:8082`
- Auction Service: `http://localhost:8083`
- Payment Service: `http://localhost:8084`

The frontend communicates with the backend through the gateway on `http://localhost:8080`.

## Prerequisites

- Docker Desktop for the Docker workflow
- Java 17 or higher for the local backend workflow
- Node.js 20 or higher for the local frontend workflow
- npm for the local frontend workflow

## Environment File

Create a root-level `.env` file before launching the project.

Example:

```env
NEXT_PUBLIC_API_BASE_URL=http://localhost:8080
GEMINI_API_KEY=
```

Notes:

- `NEXT_PUBLIC_API_BASE_URL` should point to the gateway at `http://localhost:8080`
- `GEMINI_API_KEY` may be left blank if the AI feature is not being exercised with a live key

## Running with Docker

### Start the full system

```bash
docker compose up -d --build
```

If the images have already been built or pulled, you can use:

```bash
docker compose up -d
```

### Launch on another machine

To run PrimeBid on someone else's system using Docker:

1. Install Docker Desktop and make sure Docker is running.
2. Clone this repository.
3. Create a root-level `.env` file with:

```env
NEXT_PUBLIC_API_BASE_URL=http://localhost:8080
GEMINI_API_KEY=
```

4. From the repository root, start the full stack:

```bash
docker compose up -d --build
```

5. Open the application at:

```text
http://localhost:3000
```

6. Confirm the gateway is reachable at:

```bash
http://localhost:8080
```

The default Docker setup expects these local ports to be available on the host machine:

- `3000` for the frontend
- `8080` for the gateway
- `8081` for IAM
- `8082` for catalogue
- `8083` for auction
- `8084` for payment

If the user does not want to build locally, they can pull the published images referenced by `docker-compose.yml` and then run:

```bash
docker compose up -d
```

### Verify the services

```bash
curl http://localhost:8080/health
curl http://localhost:8081/actuator/health
curl http://localhost:8082/actuator/health
curl http://localhost:8083/actuator/health
curl http://localhost:8084/actuator/health
```

Open the frontend at:

```text
http://localhost:3000
```

### Stop the system

```bash
docker compose down
```

### Clean backend SQLite data and restart

```bash
docker compose down
rm -f iam/data/*.db catalogue/data/*.db auction/data/*.db payment/data/*.db
docker compose up -d --build
```

### Docker troubleshooting

- If `docker compose up` fails immediately, make sure Docker Desktop is running.
- If the frontend loads but some pages fail, restart the frontend container:

```bash
docker compose restart frontend
```

- If login or signup fails with downstream-service errors, verify that IAM is healthy:

```bash
curl http://localhost:8081/actuator/health
```

- If services start behaving inconsistently after multiple rebuilds, do a clean reset:

```bash
docker compose down
rm -f iam/data/*.db catalogue/data/*.db auction/data/*.db payment/data/*.db
docker compose up -d --build
```

- If a port is already in use, stop the conflicting local process or free one of these ports:
  - `3000`
  - `8080`
  - `8081`
  - `8082`
  - `8083`
  - `8084`

## Running Locally

### Launch the backend locally

```bash
for service in iam catalogue auction payment gateway; do
  (cd "$service" && ./mvnw clean package -DskipTests)
done
./run_all.sh
```

Windows:

```cmd
run_all.bat
```

### Launch the frontend locally

```bash
cd frontend
npm install
npm run dev
```

Then open:

```text
http://localhost:3000
```

### Local troubleshooting

- If a backend service does not start, check the corresponding file in `logs/`
- If `./mvnw` fails, verify Java 17 is installed and available in `PATH`
- If the frontend fails to start, verify Node.js 20+ is installed:

```bash
node -v
npm -v
```

- If the frontend cannot reach the backend, verify the gateway is running on:

```text
http://localhost:8080
```

- If local ports are already occupied, stop the conflicting process before starting PrimeBid

## Platform Notes

### macOS / Apple Silicon

This repository is currently configured for the Mac environment used during development and testing.

- Java runtime stages should stay on `eclipse-temurin:17-jre`
- backend Dockerfiles should not be changed to `eclipse-temurin:17-jre-alpine`
- the frontend Dockerfile should stay on `node:20-alpine`

These image choices are required for reliable Docker builds on Mac ARM processors.

### Windows

The Dockerfiles are presently set up for the Mac development environment. Windows users may need to adjust the Dockerfiles if their Docker build environment behaves differently.

Review the following before rebuilding on Windows:

- backend Java runtime image tags
- frontend Node image and version assumptions
- shell command differences between Unix shells and Windows shells

If Docker builds fail on Windows, the safest fallback is:

1. keep the `.env` file values the same
2. review the Dockerfile base images for platform compatibility
3. rebuild with `docker compose up -d --build`
4. if needed, switch to the published images already listed in `docker-compose.yml`

## Manual Testing Documents

- Windows/general API use-case walkthrough: [INDIVIDUAL_USE_CASE_TESTING.md](/Users/tas/Desktop/EcommerceProject/Deliverable2/INDIVIDUAL_USE_CASE_TESTING.md)
- macOS API use-case walkthrough: [INDIVIDUAL_USE_CASE_TESTING_MAC.md](/Users/tas/Desktop/EcommerceProject/Deliverable2/INDIVIDUAL_USE_CASE_TESTING_MAC.md)
- Deliverable 2 testing instructions: [TESTING_INSTRUCTIONS.md](/Users/tas/Desktop/EcommerceProject/Deliverable2/TESTING_INSTRUCTIONS.md)
- frontend and AI manual testing: [Deliverable 3 Frontend and AI Chatbot Test Cases.pdf](/Users/tas/Desktop/EcommerceProject/DELIVERABLE3/Deliverable%203%20Frontend%20and%20AI%20Chatbot%20Test%20Cases.pdf)

## Deliverable 3 Diagram Set

The final Deliverable 3 diagram set is stored in [DELIVERABLE3](/Users/tas/Desktop/EcommerceProject/DELIVERABLE3).
