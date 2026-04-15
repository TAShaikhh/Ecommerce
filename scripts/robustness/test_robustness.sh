#!/bin/bash
# =============================================================================
# PrimeBid Forward Auction System - Robustness Tests
# Tests edge cases and error handling for all use cases
# =============================================================================
set -euo pipefail

BASE=http://localhost:8080
PASS=0
FAIL=0
RUN_ID="$(date +%s)_$RANDOM"
ROBUST_USER="robustuser_${RUN_ID}"

if command -v python >/dev/null 2>&1; then
    PYTHON_BIN=python
elif command -v python3 >/dev/null 2>&1; then
    PYTHON_BIN=python3
else
    echo "python or python3 is required"
    exit 1
fi

check() {
    local desc="$1"
    local expected_code="$2"
    local actual_code="$3"
    if [ "$actual_code" -eq "$expected_code" ]; then
        echo "  PASS: $desc (HTTP $actual_code)"
        PASS=$((PASS+1))
    else
        echo "  FAIL: $desc (expected $expected_code, got $actual_code)"
        FAIL=$((FAIL+1))
    fi
}

echo "=============================================="
echo "  PrimeBid - Robustness Tests"
echo "=============================================="

# --- UC1 Robustness ---
echo ""
echo "--- UC1: Authentication Robustness ---"

# Signup with missing fields
CODE=$(curl -s -o /dev/null -w "%{http_code}" -X POST "$BASE/auth/signup" \
  -H "Content-Type: application/json" \
  -d '{"username":"","password":"pass","firstName":"A","lastName":"B","address":"Addr"}')
check "Signup with empty username" 400 "$CODE"

CODE=$(curl -s -o /dev/null -w "%{http_code}" -X POST "$BASE/auth/signup" \
  -H "Content-Type: application/json" \
  -d '{"username":"u1","firstName":"A","lastName":"B","address":"Addr"}')
check "Signup with missing password" 400 "$CODE"

# Signup success for subsequent tests
curl -s -o /dev/null -X POST "$BASE/auth/signup" \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"$ROBUST_USER\",\"password\":\"pass1234\",\"firstName\":\"R\",\"lastName\":\"U\",\"address\":\"Addr\"}"

# Duplicate username
CODE=$(curl -s -o /dev/null -w "%{http_code}" -X POST "$BASE/auth/signup" \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"$ROBUST_USER\",\"password\":\"pass1234\",\"firstName\":\"R\",\"lastName\":\"U\",\"address\":\"Addr\"}")
check "Signup with duplicate username" 400 "$CODE"

# Login with wrong password
CODE=$(curl -s -o /dev/null -w "%{http_code}" -X POST "$BASE/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"robustuser","password":"wrongpass"}')
check "Login with wrong password" 400 "$CODE"

# Login with unknown user
CODE=$(curl -s -o /dev/null -w "%{http_code}" -X POST "$BASE/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"ghostuser","password":"pass1234"}')
check "Login with unknown user" 400 "$CODE"

# Access /auth/me without token
CODE=$(curl -s -o /dev/null -w "%{http_code}" "$BASE/auth/me")
check "Access /auth/me without token" 403 "$CODE"

# Access /auth/me with invalid token
CODE=$(curl -s -o /dev/null -w "%{http_code}" "$BASE/auth/me" \
  -H "Authorization: Bearer invalidtoken123")
check "Access /auth/me with invalid token" 403 "$CODE"

# --- Login for remaining tests ---
LOGIN_RES=$(curl -s -X POST "$BASE/auth/login" \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"$ROBUST_USER\",\"password\":\"pass1234\"}")
TOKEN=$(echo "$LOGIN_RES" | "$PYTHON_BIN" -c "import sys,json; print(json.load(sys.stdin)['token'])")

# --- UC7 Robustness ---
echo ""
echo "--- UC7: Item Creation Robustness ---"

CODE=$(curl -s -o /dev/null -w "%{http_code}" -X POST "$BASE/items" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"description":"d","condition":"NEW","keywords":"k1","shippingCost":10,"expeditedShippingCost":0,"shippingDays":5,"startingPrice":10,"auctionDurationSeconds":30}')
check "Create item with missing title" 400 "$CODE"

CODE=$(curl -s -o /dev/null -w "%{http_code}" -X POST "$BASE/items" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"title":"T","description":"d","condition":"NEW","keywords":"k1","shippingCost":10,"expeditedShippingCost":0,"shippingDays":5,"startingPrice":10,"auctionDurationSeconds":5}')
check "Create item with duration too small" 400 "$CODE"

# --- UC2 Robustness ---
echo ""
echo "--- UC2: Browse/Search Robustness ---"

CODE=$(curl -s -o /dev/null -w "%{http_code}" "$BASE/catalogue?keyword=xyznonexistent")
check "Search with no results returns 200" 200 "$CODE"

CODE=$(curl -s -o /dev/null -w "%{http_code}" "$BASE/catalogue/items/99999")
check "Get nonexistent item returns error" 404 "$CODE"

# --- UC3 Robustness ---
echo ""
echo "--- UC3: Bidding Robustness ---"

# Create an item for bidding tests
ITEM_RES=$(curl -s -X POST "$BASE/items" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"title":"BidTest","description":"d","condition":"NEW","keywords":"bidtest item","shippingCost":10,"expeditedShippingCost":0,"shippingDays":5,"startingPrice":100,"auctionDurationSeconds":120}')
BID_ITEM_ID=$(echo "$ITEM_RES" | "$PYTHON_BIN" -c "import sys,json; print(json.load(sys.stdin)['itemId'])")

# Bid without selecting item
CODE=$(curl -s -o /dev/null -w "%{http_code}" -X POST "$BASE/bid" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d "{\"itemId\":$BID_ITEM_ID,\"amount\":150}")
check "Bid without selecting item first" 400 "$CODE"

# Select item
curl -s -o /dev/null -X POST "$BASE/catalogue/select/$BID_ITEM_ID" \
  -H "Authorization: Bearer $TOKEN"

# Bid lower than starting price
CODE=$(curl -s -o /dev/null -w "%{http_code}" -X POST "$BASE/bid" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d "{\"itemId\":$BID_ITEM_ID,\"amount\":50}")
check "Bid lower than starting price" 400 "$CODE"

# Valid bid
curl -s -o /dev/null -X POST "$BASE/bid" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d "{\"itemId\":$BID_ITEM_ID,\"amount\":150}"

# Bid equal to current
CODE=$(curl -s -o /dev/null -w "%{http_code}" -X POST "$BASE/bid" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d "{\"itemId\":$BID_ITEM_ID,\"amount\":150}")
check "Bid equal to current highest" 400 "$CODE"

# Bid lower than current
CODE=$(curl -s -o /dev/null -w "%{http_code}" -X POST "$BASE/bid" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d "{\"itemId\":$BID_ITEM_ID,\"amount\":100}")
check "Bid lower than current highest" 400 "$CODE"

# --- UC5 Robustness ---
echo ""
echo "--- UC5: Payment Robustness ---"

# Pay when auction is still active
CODE=$(curl -s -o /dev/null -w "%{http_code}" -X POST "$BASE/pay" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d "{\"itemId\":$BID_ITEM_ID,\"expedited\":false,\"cardName\":\"R U\",\"cardNumber\":\"4111111111111111\",\"expiryDate\":\"12/28\",\"securityCode\":\"123\"}")
check "Pay when auction still active" 409 "$CODE"

echo ""
echo "=============================================="
echo "  Results: $PASS passed, $FAIL failed"
echo "=============================================="
