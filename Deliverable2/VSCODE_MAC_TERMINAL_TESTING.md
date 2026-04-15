# VS Code Terminal Testing for Mac

This guide is for running and testing the project from the VS Code integrated terminal on a MacBook.

It assumes:

- you are using the VS Code terminal
- your shell is `zsh` or `bash`
- you want terminal commands only
- you want to test each use case manually through the gateway

## 1. Open the Project in VS Code

Open the folder:

```text
forward-auction-system-dev
```

Then open a new VS Code terminal.

Recommended shell:

- `zsh`
- `bash`

You can verify your shell with:

```bash
echo $SHELL
```

## 2. Prerequisites

Make sure these are available:

```bash
java -version
curl --version
python3 --version
```

If Maven wrapper complains about `JAVA_HOME`, run:

```bash
export JAVA_HOME=$(/usr/libexec/java_home)
```

## 3. Go to the Project Root

In the VS Code terminal, make sure you are at the project root:

```bash
pwd
ls
```

You should see folders like:

- `iam`
- `catalogue`
- `auction`
- `payment`
- `gateway`
- `scripts`

## 4. Clean Old Local Databases

Run:

```bash
rm -f iam/data/*.db catalogue/data/*.db auction/data/*.db payment/data/*.db
```

## 5. Build All Services

Run this from the project root:

```bash
for service in iam catalogue auction payment gateway; do
  cd "$service" && ./mvnw clean package -DskipTests && cd ..
done
```

## 6. Start All Services

Run:

```bash
./run_all.sh
```

Leave that terminal open.

Open a second VS Code terminal for testing requests.

## 7. Verify the Services Are Up

In the second terminal, run:

```bash
curl "http://localhost:8080/health"
curl "http://localhost:8080/ping/iam"
curl "http://localhost:8080/ping/catalogue"
curl "http://localhost:8080/ping/auction"
curl "http://localhost:8080/ping/payment"
```

## 8. Set Reusable Test Variables

Run this once in the second terminal:

```bash
RUN_ID=$(date +%s)
SELLER_USER="seller_$RUN_ID"
BUYER_USER="buyer_$RUN_ID"
SELLER_PASS="pass1234"
BUYER_PASS="pass1234"
```

## 9. UC1: User Registration and Login

### Seller signup

```bash
curl -X POST "http://localhost:8080/auth/signup" \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"$SELLER_USER\",\"password\":\"$SELLER_PASS\",\"firstName\":\"Alice\",\"lastName\":\"Smith\",\"address\":\"123 Seller St\"}"
```

### Buyer signup

```bash
curl -X POST "http://localhost:8080/auth/signup" \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"$BUYER_USER\",\"password\":\"$BUYER_PASS\",\"firstName\":\"Bob\",\"lastName\":\"Jones\",\"address\":\"456 Buyer Ave\"}"
```

### Seller login

```bash
SELLER_LOGIN_RESPONSE=$(curl -s -X POST "http://localhost:8080/auth/login" \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"$SELLER_USER\",\"password\":\"$SELLER_PASS\"}")

echo "$SELLER_LOGIN_RESPONSE"
SELLER_TOKEN=$(echo "$SELLER_LOGIN_RESPONSE" | python3 -c "import sys, json; print(json.load(sys.stdin)['token'])")
echo "$SELLER_TOKEN"
```

### Buyer login

```bash
BUYER_LOGIN_RESPONSE=$(curl -s -X POST "http://localhost:8080/auth/login" \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"$BUYER_USER\",\"password\":\"$BUYER_PASS\"}")

echo "$BUYER_LOGIN_RESPONSE"
BUYER_TOKEN=$(echo "$BUYER_LOGIN_RESPONSE" | python3 -c "import sys, json; print(json.load(sys.stdin)['token'])")
echo "$BUYER_TOKEN"
```

### Check current session

```bash
curl "http://localhost:8080/auth/me" \
  -H "Authorization: Bearer $SELLER_TOKEN"
```

### Reset password

```bash
curl -X POST "http://localhost:8080/auth/reset-password" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $SELLER_TOKEN" \
  -d "{\"username\":\"$SELLER_USER\",\"currentPassword\":\"$SELLER_PASS\",\"newPassword\":\"newpass123\"}"
```

### Logout

```bash
curl -X POST "http://localhost:8080/auth/logout" \
  -H "Authorization: Bearer $SELLER_TOKEN"
```

## 10. UC7: Create Item and Auction

Prerequisite:

- seller is logged in
- `SELLER_TOKEN` exists

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

## 11. UC2: Browse, Search, View, and Select

### Browse all items

```bash
curl "http://localhost:8080/catalogue"
```

### Search items

```bash
curl "http://localhost:8080/catalogue?keyword=laptop"
```

### View one item

```bash
curl "http://localhost:8080/catalogue/items/$ITEM_ID"
```

### Select item for bidding

```bash
curl -X POST "http://localhost:8080/catalogue/select/$ITEM_ID" \
  -H "Authorization: Bearer $BUYER_TOKEN"
```

## 12. UC3: Place Bid and View Bid History

Prerequisite:

- buyer is logged in
- item was selected first

### Place a bid

```bash
curl -X POST "http://localhost:8080/bid" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $BUYER_TOKEN" \
  -d "{\"itemId\":$ITEM_ID,\"amount\":550}"
```

### View bid history

```bash
curl "http://localhost:8080/bid/history/$ITEM_ID"
```

## 13. UC4: View Auction Result

Wait for the auction to expire:

```bash
sleep 20
```

Then check result:

```bash
curl "http://localhost:8080/auction-result/$ITEM_ID" \
  -H "Authorization: Bearer $BUYER_TOKEN"
```

## 14. UC5: Payment

### View payment page

```bash
curl "http://localhost:8080/payment-page/$ITEM_ID" \
  -H "Authorization: Bearer $BUYER_TOKEN"
```

### Submit payment

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

## 15. UC6: Receipt

```bash
curl "http://localhost:8080/receipt/$PAYMENT_ID" \
  -H "Authorization: Bearer $BUYER_TOKEN"
```

## 16. Existing Repo Scripts

If you want the existing scripted flows:

- `./run_all.sh`
- `bash scripts/main_flow/00_smoke.sh`
- `bash scripts/main_flow/full_flow.sh`
- `bash scripts/robustness/test_robustness.sh`
- `bash scripts/manual_curl_use_cases.sh`

## 17. Common Mistakes in VS Code Terminal on Mac

- Do not use PowerShell syntax in the Mac terminal.
- Use `\` for multi-line `curl` commands in `bash`/`zsh`.
- Do not use PowerShell backticks.
- If `python3` is missing, install Python or replace with `python` if available.
- If startup fails, check the terminal running `./run_all.sh` for service errors.

## 18. Recommended Flow

1. Open one VS Code terminal.
2. Clean old DB files.
3. Build all services.
4. Start services with `./run_all.sh`.
5. Open a second VS Code terminal.
6. Run health checks.
7. Run UC1 seller and buyer signup/login.
8. Run UC7 create item.
9. Run UC2 browse and select.
10. Run UC3 bid.
11. Wait for auction end.
12. Run UC4 result.
13. Run UC5 payment.
14. Run UC6 receipt.
