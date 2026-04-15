#!/bin/bash
# =============================================================================
# PrimeBid Forward Auction System - Full Happy Path Flow (UC1 -> UC7)
# =============================================================================
# Prerequisites: All 5 services running (use run_all.sh or run_all.bat)
# Gateway at http://localhost:8080
# =============================================================================
set -euo pipefail

BASE=http://localhost:8080

if command -v python >/dev/null 2>&1; then
  PYTHON_BIN=python
elif command -v python3 >/dev/null 2>&1; then
  PYTHON_BIN=python3
else
  echo "python or python3 is required"
  exit 1
fi

echo "=============================================="
echo "  PrimeBid - Full Happy Path Demo"
echo "=============================================="

RUN_ID="$(date +%s)_$RANDOM"
SELLER_USER="seller_${RUN_ID}"
BUYER_USER="buyer_${RUN_ID}"

# --- UC1: Signup Seller ---
echo ""
echo ">>> UC1: Signup Seller"
curl -s -X POST "$BASE/auth/signup" \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"$SELLER_USER\",\"password\":\"pass1234\",\"firstName\":\"Alice\",\"lastName\":\"Smith\",\"address\":\"123 Seller St\"}" | "$PYTHON_BIN" -m json.tool
echo ""

# --- UC1: Signup Buyer ---
echo ">>> UC1: Signup Buyer"
curl -s -X POST "$BASE/auth/signup" \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"$BUYER_USER\",\"password\":\"pass1234\",\"firstName\":\"Bob\",\"lastName\":\"Jones\",\"address\":\"456 Buyer Ave\"}" | "$PYTHON_BIN" -m json.tool
echo ""

# --- UC1: Login Seller ---
echo ">>> UC1: Login Seller"
SELLER_RES=$(curl -s -X POST "$BASE/auth/login" \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"$SELLER_USER\",\"password\":\"pass1234\"}")
echo "$SELLER_RES" | "$PYTHON_BIN" -m json.tool
SELLER_TOKEN=$(echo "$SELLER_RES" | "$PYTHON_BIN" -c "import sys,json; print(json.load(sys.stdin)['token'])")
echo "Seller token: $SELLER_TOKEN"
echo ""

# --- UC1: Login Buyer ---
echo ">>> UC1: Login Buyer"
BUYER_RES=$(curl -s -X POST "$BASE/auth/login" \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"$BUYER_USER\",\"password\":\"pass1234\"}")
echo "$BUYER_RES" | "$PYTHON_BIN" -m json.tool
BUYER_TOKEN=$(echo "$BUYER_RES" | "$PYTHON_BIN" -c "import sys,json; print(json.load(sys.stdin)['token'])")
echo "Buyer token: $BUYER_TOKEN"
echo ""

# --- UC7: Seller Creates Item ---
echo ">>> UC7: Seller Creates Item with Auction"
ITEM_RES=$(curl -s -X POST "$BASE/items" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $SELLER_TOKEN" \
  -d '{
    "title":"Gaming Laptop",
    "description":"High-end gaming laptop, RTX 4090",
    "condition":"NEW",
    "keywords":"laptop gaming rtx",
    "shippingCost":15.0,
    "expeditedShippingCost":10.0,
    "shippingDays":5,
	    "startingPrice":500.0,
	    "auctionDurationSeconds":30
	  }')
echo "$ITEM_RES" | "$PYTHON_BIN" -m json.tool
ITEM_ID=$(echo "$ITEM_RES" | "$PYTHON_BIN" -c "import sys,json; print(json.load(sys.stdin)['itemId'])")
echo "Item ID: $ITEM_ID"
echo ""

# --- UC2: Buyer Searches Catalogue ---
echo ">>> UC2: Buyer Searches Catalogue"
curl -s "$BASE/catalogue?keyword=laptop" \
  -H "Authorization: Bearer $BUYER_TOKEN" | "$PYTHON_BIN" -m json.tool
echo ""

# --- UC2: Buyer Views Item Detail ---
echo ">>> UC2: Buyer Views Item Detail"
curl -s "$BASE/catalogue/items/$ITEM_ID" \
  -H "Authorization: Bearer $BUYER_TOKEN" | "$PYTHON_BIN" -m json.tool
echo ""

# --- UC2: Buyer Selects Item ---
echo ">>> UC2: Buyer Selects Item"
curl -s -X POST "$BASE/catalogue/select/$ITEM_ID" \
  -H "Authorization: Bearer $BUYER_TOKEN" | "$PYTHON_BIN" -m json.tool
echo ""

# --- UC3: Buyer Places Bids ---
echo ">>> UC3: Buyer Places First Bid"
curl -s -X POST "$BASE/bid" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $BUYER_TOKEN" \
  -d "{\"itemId\":$ITEM_ID,\"amount\":550}" | "$PYTHON_BIN" -m json.tool
echo ""

echo ">>> UC3: Buyer Places Second Bid (higher)"
curl -s -X POST "$BASE/bid" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $BUYER_TOKEN" \
  -d "{\"itemId\":$ITEM_ID,\"amount\":600}" | "$PYTHON_BIN" -m json.tool
echo ""

echo ">>> UC3: View Bid History"
curl -s "$BASE/bid/history/$ITEM_ID" \
  -H "Authorization: Bearer $BUYER_TOKEN" | "$PYTHON_BIN" -m json.tool
echo ""

# --- UC4: Wait for Auction to End ---
echo ">>> UC4: Waiting 35 seconds for auction to expire..."
sleep 35

echo ">>> UC4: Check Auction Result"
curl -s "$BASE/auction-result/$ITEM_ID" \
  -H "Authorization: Bearer $BUYER_TOKEN" | "$PYTHON_BIN" -m json.tool
echo ""

# --- UC5: Buyer Loads Payment Page ---
echo ">>> UC5: Buyer Views Payment Page"
curl -s "$BASE/payment-page/$ITEM_ID" \
  -H "Authorization: Bearer $BUYER_TOKEN" | "$PYTHON_BIN" -m json.tool
echo ""

# --- UC5: Buyer Makes Payment ---
echo ">>> UC5: Buyer Submits Payment"
PAY_RES=$(curl -s -X POST "$BASE/pay" \
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
echo "$PAY_RES" | "$PYTHON_BIN" -m json.tool
PAYMENT_ID=$(echo "$PAY_RES" | "$PYTHON_BIN" -c "import sys,json; print(json.load(sys.stdin)['paymentId'])")
echo "Payment ID: $PAYMENT_ID"
echo ""

# --- UC6: Buyer Views Receipt ---
echo ">>> UC6: Buyer Views Receipt"
curl -s "$BASE/receipt/$PAYMENT_ID" \
  -H "Authorization: Bearer $BUYER_TOKEN" | "$PYTHON_BIN" -m json.tool
echo ""

echo "=============================================="
echo "  Full flow complete!"
echo "=============================================="
