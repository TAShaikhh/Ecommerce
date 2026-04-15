# Individual Use Case Testing

This document provides a manual API walkthrough so each use case can be tested individually for Deliverable 3 submission review.

## 1. Prerequisites

- Java 17 or higher
- Maven wrapper support via `mvnw.cmd`
- `curl`
- `bash` plus `python` or `python3` if you want to run the existing `.sh` helper scripts

## 2. Clean Old Local Data

Run this from the project root in PowerShell before a fresh end-to-end test pass:

```powershell
Remove-Item -Force iam\data\*.db,catalogue\data\*.db,auction\data\*.db,payment\data\*.db -ErrorAction SilentlyContinue
```

## 3. Build All Services

From the project root in `cmd.exe`:

```cmd
for %s in (iam catalogue auction payment gateway) do (cd %s && mvnw.cmd clean package -DskipTests && cd ..)
```

From PowerShell:

```powershell
$services = 'iam','catalogue','auction','payment','gateway'
foreach ($s in $services) {
  Push-Location $s
  try {
    .\mvnw.cmd clean package -DskipTests
    if ($LASTEXITCODE -ne 0) { throw "Build failed in $s" }
  } finally {
    Pop-Location
  }
}
```

## 4. Start All Services

From the project root:

```cmd
.\run_all.bat
```

Expected ports:

- Gateway: `http://localhost:8080`
- IAM: `http://localhost:8081`
- Catalogue: `http://localhost:8082`
- Auction: `http://localhost:8083`
- Payment: `http://localhost:8084`

## 5. Verify Services Are Running

Run these checks before testing any use case:

```bash
curl "http://localhost:8080/health"
curl "http://localhost:8080/ping/iam"
curl "http://localhost:8080/ping/catalogue"
curl "http://localhost:8080/ping/auction"
curl "http://localhost:8080/ping/payment"
```

## 6. Tokens and IDs You Will Reuse

During testing, keep track of:

- `SELLER_TOKEN` from `POST /auth/login`
- `BUYER_TOKEN` from `POST /auth/login`
- `ITEM_ID` from `POST /items`
- `PAYMENT_ID` from `POST /pay`

Protected endpoints require:

```text
Authorization: Bearer <token>
```

## 7. Use Case Tests

All requests below go through the gateway at `http://localhost:8080`.

### UC1: User Registration and Login

#### Seller signup

```bash
curl -X POST "http://localhost:8080/auth/signup" \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"seller1\",\"password\":\"pass1234\",\"firstName\":\"Alice\",\"lastName\":\"Smith\",\"address\":\"123 Seller St\"}"
```

#### Buyer signup

```bash
curl -X POST "http://localhost:8080/auth/signup" \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"buyer1\",\"password\":\"pass1234\",\"firstName\":\"Bob\",\"lastName\":\"Jones\",\"address\":\"456 Buyer Ave\"}"
```

#### Seller login

```bash
curl -X POST "http://localhost:8080/auth/login" \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"seller1\",\"password\":\"pass1234\"}"
```

Save the returned seller `token` as `SELLER_TOKEN`.

#### Buyer login

```bash
curl -X POST "http://localhost:8080/auth/login" \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"buyer1\",\"password\":\"pass1234\"}"
```

Save the returned buyer `token` as `BUYER_TOKEN`.

#### Check current session

```bash
curl "http://localhost:8080/auth/me" \
  -H "Authorization: Bearer <SELLER_TOKEN>"
```

#### Reset password

```bash
curl -X POST "http://localhost:8080/auth/reset-password" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <SELLER_TOKEN>" \
  -d "{\"username\":\"seller1\",\"currentPassword\":\"pass1234\",\"newPassword\":\"newpass123\"}"
```

#### Logout

```bash
curl -X POST "http://localhost:8080/auth/logout" \
  -H "Authorization: Bearer <SELLER_TOKEN>"
```

### UC7: Create Item and Auction

Prerequisite:

- seller must be logged in
- valid `SELLER_TOKEN`

```bash
curl -X POST "http://localhost:8080/items" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <SELLER_TOKEN>" \
  -d '{
    "title":"Gaming Laptop",
    "description":"RTX 4090 laptop",
    "condition":"NEW",
    "keywords":"laptop gaming",
    "shippingCost":15.0,
    "expeditedShippingCost":10.0,
    "shippingDays":5,
    "startingPrice":500.0,
    "auctionDurationSeconds":15
  }'
```

Save the returned `itemId` as `ITEM_ID`.

### UC2: Browse, Search, View, and Select Catalogue Item

#### Browse all items

```bash
curl "http://localhost:8080/catalogue"
```

#### Search by keyword

```bash
curl "http://localhost:8080/catalogue?keyword=laptop"
```

#### View one item

```bash
curl "http://localhost:8080/catalogue/items/<ITEM_ID>"
```

#### Select item for bidding

Prerequisite:

- buyer must be logged in
- valid `BUYER_TOKEN`
- item must still be `ACTIVE`

```bash
curl -X POST "http://localhost:8080/catalogue/select/<ITEM_ID>" \
  -H "Authorization: Bearer <BUYER_TOKEN>"
```

### UC3: Place Bid and View Bid History

Prerequisite:

- buyer must be logged in
- valid `BUYER_TOKEN`
- item must already be selected via `POST /catalogue/select/<ITEM_ID>`

#### Place a bid

```bash
curl -X POST "http://localhost:8080/bid" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <BUYER_TOKEN>" \
  -d "{\"itemId\":<ITEM_ID>,\"amount\":550}"
```

#### View bid history

```bash
curl "http://localhost:8080/bid/history/<ITEM_ID>"
```

### UC4: View Auction Result

Prerequisite:

- wait until the auction duration has expired

If you used `auctionDurationSeconds: 15`, wait at least 15 to 20 seconds before checking:

```bash
curl "http://localhost:8080/auction-result/<ITEM_ID>" \
  -H "Authorization: Bearer <BUYER_TOKEN>"
```

### UC5: View Payment Page and Submit Payment

Prerequisite:

- auction must be `ENDED`
- result must be `SOLD`
- caller must be the winning bidder

#### View payment page

```bash
curl "http://localhost:8080/payment-page/<ITEM_ID>" \
  -H "Authorization: Bearer <BUYER_TOKEN>"
```

#### Submit payment

```bash
curl -X POST "http://localhost:8080/pay" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <BUYER_TOKEN>" \
  -d "{
    \"itemId\":<ITEM_ID>,
    \"expedited\":true,
    \"cardName\":\"Bob Jones\",
    \"cardNumber\":\"4111111111111111\",
    \"expiryDate\":\"12/28\",
    \"securityCode\":\"123\"
  }"
```

Save the returned `paymentId` as `PAYMENT_ID`.

### UC6: View Receipt

Prerequisite:

- payment must already exist
- caller must be the owner of that payment

```bash
curl "http://localhost:8080/receipt/<PAYMENT_ID>" \
  -H "Authorization: Bearer <BUYER_TOKEN>"
```

## 8. Recommended Order

If you want to test everything manually in the right order:

1. Clean old DB files
2. Build all services
3. Run `run_all.bat`
4. Verify health endpoints
5. Run UC1 seller signup/login
6. Run UC1 buyer signup/login
7. Run UC7 create item
8. Run UC2 browse and select item
9. Run UC3 place bid
10. Wait for auction end
11. Run UC4 auction result
12. Run UC5 payment page and payment
13. Run UC6 receipt

## 9. Notes

- `POST /bid` expects the bid `amount` to be an integer.
- `POST /items` requires `auctionDurationSeconds` to be at least `10`.
- `POST /pay` requires `itemId`; `expedited` is optional and defaults to `false`.
- `POST /catalogue/select/<ITEM_ID>` must happen before bidding with that buyer token.
- If a port is already in use, stop the existing Java processes and restart the services.
