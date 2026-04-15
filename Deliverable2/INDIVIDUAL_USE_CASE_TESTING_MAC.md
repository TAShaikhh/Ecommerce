# Individual Use Case Testing for macOS

This document provides a manual macOS API walkthrough so each use case can be tested individually for Deliverable 3 submission review.

## 1. Prerequisites

- Java 17 or higher
- Terminal access to `bash` or `zsh`
- `curl`
- `python` or `python3`
- Maven wrapper support via `./mvnw`

If `./mvnw` reports that `JAVA_HOME` is not set correctly on macOS, run this once in your terminal:

```bash
export JAVA_HOME=$(/usr/libexec/java_home)
```

## 2. Clean Old Local Data

Run this from the project root before a fresh end-to-end test pass:

```bash
rm -f iam/data/*.db catalogue/data/*.db auction/data/*.db payment/data/*.db
```

## 3. Build All Services

From the project root:

```bash
for service in iam catalogue auction payment gateway; do
  cd "$service" && ./mvnw clean package -DskipTests && cd ..
done
```

## 4. Start All Services

From the project root:

```bash
./run_all.sh
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

### Optional Repeatable Variables

To avoid username collisions during repeated testing:

```bash
RUN_ID=$(date +%s)
SELLER_USER="seller_$RUN_ID"
BUYER_USER="buyer_$RUN_ID"
SELLER_PASS="pass1234"
BUYER_PASS="pass1234"
```

### UC1: User Registration and Login

#### Seller signup

```bash
curl -X POST "http://localhost:8080/auth/signup" \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"$SELLER_USER\",\"password\":\"$SELLER_PASS\",\"firstName\":\"Alice\",\"lastName\":\"Smith\",\"address\":\"123 Seller St\"}"
```

#### Buyer signup

```bash
curl -X POST "http://localhost:8080/auth/signup" \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"$BUYER_USER\",\"password\":\"$BUYER_PASS\",\"firstName\":\"Bob\",\"lastName\":\"Jones\",\"address\":\"456 Buyer Ave\"}"
```

#### Seller login

```bash
SELLER_LOGIN_RESPONSE=$(curl -s -X POST "http://localhost:8080/auth/login" \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"$SELLER_USER\",\"password\":\"$SELLER_PASS\"}")

echo "$SELLER_LOGIN_RESPONSE"
SELLER_TOKEN=$(echo "$SELLER_LOGIN_RESPONSE" | python3 -c "import sys, json; print(json.load(sys.stdin)['token'])")
echo "$SELLER_TOKEN"
```

If your Mac uses `python` instead of `python3`, replace `python3` with `python`.

#### Buyer login

```bash
BUYER_LOGIN_RESPONSE=$(curl -s -X POST "http://localhost:8080/auth/login" \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"$BUYER_USER\",\"password\":\"$BUYER_PASS\"}")

echo "$BUYER_LOGIN_RESPONSE"
BUYER_TOKEN=$(echo "$BUYER_LOGIN_RESPONSE" | python3 -c "import sys, json; print(json.load(sys.stdin)['token'])")
echo "$BUYER_TOKEN"
```

#### Check current session

```bash
curl "http://localhost:8080/auth/me" \
  -H "Authorization: Bearer $SELLER_TOKEN"
```

#### Reset password

```bash
curl -X POST "http://localhost:8080/auth/reset-password" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $SELLER_TOKEN" \
  -d "{\"username\":\"$SELLER_USER\",\"currentPassword\":\"$SELLER_PASS\",\"newPassword\":\"newpass123\"}"
```

#### Logout

```bash
curl -X POST "http://localhost:8080/auth/logout" \
  -H "Authorization: Bearer $SELLER_TOKEN"
```

### UC7: Create Item and Auction

Prerequisite:

- seller must be logged in
- valid `SELLER_TOKEN`

```bash
CREATE_ITEM_RESPONSE=$(curl -s -X POST "http://localhost:8080/items" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $SELLER_TOKEN" \
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
  }')

echo "$CREATE_ITEM_RESPONSE"
ITEM_ID=$(echo "$CREATE_ITEM_RESPONSE" | python3 -c "import sys, json; print(json.load(sys.stdin)['itemId'])")
echo "$ITEM_ID"
```

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
curl "http://localhost:8080/catalogue/items/$ITEM_ID"
```

#### Select item for bidding

Prerequisite:

- buyer must be logged in
- valid `BUYER_TOKEN`
- item must still be `ACTIVE`

```bash
curl -X POST "http://localhost:8080/catalogue/select/$ITEM_ID" \
  -H "Authorization: Bearer $BUYER_TOKEN"
```

### UC3: Place Bid and View Bid History

Prerequisite:

- buyer must be logged in
- valid `BUYER_TOKEN`
- item must already be selected via `POST /catalogue/select/$ITEM_ID`

#### Place a bid

```bash
curl -X POST "http://localhost:8080/bid" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $BUYER_TOKEN" \
  -d "{\"itemId\":$ITEM_ID,\"amount\":550}"
```

#### View bid history

```bash
curl "http://localhost:8080/bid/history/$ITEM_ID"
```

### UC4: View Auction Result

Prerequisite:

- wait until the auction duration has expired

If you used `auctionDurationSeconds: 15`, wait at least 15 to 20 seconds before checking:

```bash
sleep 20

curl "http://localhost:8080/auction-result/$ITEM_ID" \
  -H "Authorization: Bearer $BUYER_TOKEN"
```

### UC5: View Payment Page and Submit Payment

Prerequisite:

- auction must be `ENDED`
- result must be `SOLD`
- caller must be the winning bidder

#### View payment page

```bash
curl "http://localhost:8080/payment-page/$ITEM_ID" \
  -H "Authorization: Bearer $BUYER_TOKEN"
```

#### Submit payment

```bash
PAYMENT_RESPONSE=$(curl -s -X POST "http://localhost:8080/pay" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $BUYER_TOKEN" \
  -d "{
    \"itemId\":$ITEM_ID,
    \"expedited\":true,
    \"cardName\":\"Bob Jones\",
    \"cardNumber\":\"4111111111111111\",
    \"expiryDate\":\"12/28\",
    \"securityCode\":\"123\"
  }")

echo "$PAYMENT_RESPONSE"
PAYMENT_ID=$(echo "$PAYMENT_RESPONSE" | python3 -c "import sys, json; print(json.load(sys.stdin)['paymentId'])")
echo "$PAYMENT_ID"
```

### UC6: View Receipt

Prerequisite:

- payment must already exist
- caller must be the owner of that payment

```bash
curl "http://localhost:8080/receipt/$PAYMENT_ID" \
  -H "Authorization: Bearer $BUYER_TOKEN"
```

## 8. Recommended Order

If you want to test everything manually in the right order:

1. Clean old DB files
2. Set `JAVA_HOME` if needed
3. Build all services
4. Run `./run_all.sh`
5. Verify health endpoints
6. Run UC1 seller signup/login
7. Run UC1 buyer signup/login
8. Run UC7 create item
9. Run UC2 browse and select item
10. Run UC3 place bid
11. Wait for auction end
12. Run UC4 auction result
13. Run UC5 payment page and payment
14. Run UC6 receipt

## 9. Notes

- `POST /bid` expects the bid `amount` to be an integer.
- `POST /items` requires `auctionDurationSeconds` to be at least `10`.
- `POST /pay` requires `itemId`; `expedited` is optional and defaults to `false`.
- `POST /catalogue/select/$ITEM_ID` must happen before bidding with that buyer token.
- If a port is already in use, stop the existing Java processes and restart the services.
