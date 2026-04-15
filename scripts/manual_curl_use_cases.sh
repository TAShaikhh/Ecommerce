#!/usr/bin/env bash
set -euo pipefail

# Sequential curl walkthrough for UC1-UC7 through the gateway.
# Run after all services are up. Override BASE_URL if needed:
#   BASE_URL=http://localhost:8080 bash scripts/manual_curl_use_cases.sh

BASE_URL="${BASE_URL:-http://localhost:8080}"

if command -v python >/dev/null 2>&1; then
  PYTHON_BIN=python
elif command -v python3 >/dev/null 2>&1; then
  PYTHON_BIN=python3
else
  echo "python or python3 is required to extract ids/tokens from JSON responses"
  exit 1
fi

RUN_ID="$(date +%s)_$RANDOM"
SELLER_USER="seller_${RUN_ID}"
BUYER_USER="buyer_${RUN_ID}"
SELLER_PASS="pass1234"
BUYER_PASS="pass1234"

echo "== Smoke checks =="
curl -sS "$BASE_URL/health"
echo
curl -sS "$BASE_URL/ping/iam"
echo
curl -sS "$BASE_URL/ping/catalogue"
echo
curl -sS "$BASE_URL/ping/auction"
echo
curl -sS "$BASE_URL/ping/payment"
echo
echo

echo "== UC1: seller signup =="
curl -sS -X POST "$BASE_URL/auth/signup" \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"$SELLER_USER\",\"password\":\"$SELLER_PASS\",\"firstName\":\"Alice\",\"lastName\":\"Smith\",\"address\":\"123 Seller St\"}"
echo
echo

echo "== UC1: buyer signup =="
curl -sS -X POST "$BASE_URL/auth/signup" \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"$BUYER_USER\",\"password\":\"$BUYER_PASS\",\"firstName\":\"Bob\",\"lastName\":\"Jones\",\"address\":\"456 Buyer Ave\"}"
echo
echo

echo "== UC1: seller login =="
SELLER_LOGIN_RESPONSE="$(curl -sS -X POST "$BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"$SELLER_USER\",\"password\":\"$SELLER_PASS\"}")"
printf '%s\n' "$SELLER_LOGIN_RESPONSE"
SELLER_TOKEN="$(printf '%s' "$SELLER_LOGIN_RESPONSE" | "$PYTHON_BIN" -c "import sys, json; print(json.load(sys.stdin)['token'])")"
echo "SELLER_TOKEN=$SELLER_TOKEN"
echo

echo "== UC1: buyer login =="
BUYER_LOGIN_RESPONSE="$(curl -sS -X POST "$BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"$BUYER_USER\",\"password\":\"$BUYER_PASS\"}")"
printf '%s\n' "$BUYER_LOGIN_RESPONSE"
BUYER_TOKEN="$(printf '%s' "$BUYER_LOGIN_RESPONSE" | "$PYTHON_BIN" -c "import sys, json; print(json.load(sys.stdin)['token'])")"
echo "BUYER_TOKEN=$BUYER_TOKEN"
echo

echo "== UC1: auth/me =="
curl -sS "$BASE_URL/auth/me" \
  -H "Authorization: Bearer $SELLER_TOKEN"
echo
echo

echo "== UC1: reset password =="
curl -sS -X POST "$BASE_URL/auth/reset-password" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $SELLER_TOKEN" \
  -d "{\"username\":\"$SELLER_USER\",\"currentPassword\":\"$SELLER_PASS\",\"newPassword\":\"newpass123\"}"
echo
echo

echo "== UC7: create item and auction =="
CREATE_ITEM_RESPONSE="$(curl -sS -X POST "$BASE_URL/items" \
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
  }')"
printf '%s\n' "$CREATE_ITEM_RESPONSE"
ITEM_ID="$(printf '%s' "$CREATE_ITEM_RESPONSE" | "$PYTHON_BIN" -c "import sys, json; print(json.load(sys.stdin)['itemId'])")"
echo "ITEM_ID=$ITEM_ID"
echo

echo "== UC2: browse catalogue =="
curl -sS "$BASE_URL/catalogue"
echo
echo

echo "== UC2: search catalogue =="
curl -sS "$BASE_URL/catalogue?keyword=laptop"
echo
echo

echo "== UC2: item detail =="
curl -sS "$BASE_URL/catalogue/items/$ITEM_ID"
echo
echo

echo "== UC2: select item =="
curl -sS -X POST "$BASE_URL/catalogue/select/$ITEM_ID" \
  -H "Authorization: Bearer $BUYER_TOKEN"
echo
echo

echo "== UC3: place bid =="
curl -sS -X POST "$BASE_URL/bid" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $BUYER_TOKEN" \
  -d "{\"itemId\":$ITEM_ID,\"amount\":550}"
echo
echo

echo "== UC3: bid history =="
curl -sS "$BASE_URL/bid/history/$ITEM_ID"
echo
echo

echo "== UC4: wait for auction to end =="
sleep 20

echo "== UC4: auction result =="
curl -sS "$BASE_URL/auction-result/$ITEM_ID" \
  -H "Authorization: Bearer $BUYER_TOKEN"
echo
echo

echo "== UC5: payment page =="
curl -sS "$BASE_URL/payment-page/$ITEM_ID" \
  -H "Authorization: Bearer $BUYER_TOKEN"
echo
echo

echo "== UC5: submit payment =="
PAYMENT_RESPONSE="$(curl -sS -X POST "$BASE_URL/pay" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $BUYER_TOKEN" \
  -d "{
    \"itemId\":$ITEM_ID,
    \"expedited\":true,
    \"cardName\":\"Bob Jones\",
    \"cardNumber\":\"4111111111111111\",
    \"expiryDate\":\"12/28\",
    \"securityCode\":\"123\"
  }")"
printf '%s\n' "$PAYMENT_RESPONSE"
PAYMENT_ID="$(printf '%s' "$PAYMENT_RESPONSE" | "$PYTHON_BIN" -c "import sys, json; print(json.load(sys.stdin)['paymentId'])")"
echo "PAYMENT_ID=$PAYMENT_ID"
echo

echo "== UC6: receipt =="
curl -sS "$BASE_URL/receipt/$PAYMENT_ID" \
  -H "Authorization: Bearer $BUYER_TOKEN"
echo
echo

echo "== UC1: logout =="
curl -sS -X POST "$BASE_URL/auth/logout" \
  -H "Authorization: Bearer $SELLER_TOKEN"
echo
