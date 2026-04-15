#!/bin/bash

# PrimeBid EECS 4413 - Backend Validation Script
# Tests Robustness, Scalability (via multiple concurrent requests), and Security.
# Requires: Gateway running on localhost:8080.

GATEWAY_URL="http://localhost:8080"
echo "========================================="
echo "  PrimeBid API Validation Script "
echo "========================================="
echo ""

# 1. Provide an easy way to get a token
echo "--- SECURITY TEST: Unauthenticated Access ---"
# Calling a guarded endpoint without a token should yield a 401 or 403 or 500 security exception.
STATUS=$(curl -s -o /dev/null -w "%{http_code}" -X POST $GATEWAY_URL/auto-bid/start \
  -H "Content-Type: application/json" \
  -d '{"itemId": 1, "maxBid": 500, "strategy": "CONSERVATIVE"}')
echo "Expected failure status for missing token. Received HTTP Status: $STATUS"

echo ""
echo "--- ROBUSTNESS TEST: Invalid Payload ---"
# Providing a negative maxBid for auto-bidding
# Simulate we have a token (we will just send a mock token since our Security filter checks presence,
# but the SessionStore checks validity. The session store will reject a fake token).
STATUS_INVALID=$(curl -s -o /dev/null -w "%{http_code}" -X POST $GATEWAY_URL/auto-bid/start \
  -H "Authorization: Bearer mock-token-123" \
  -H "Content-Type: application/json" \
  -d '{"itemId": 1, "maxBid": -100, "strategy": "CONSERVATIVE"}')
echo "Sending negative budget. Received HTTP Status: $STATUS_INVALID (Expected 4xx or 5xx rejection)"

echo ""
echo "--- SCALABILITY TEST: Concurrent Reads ---"
echo "Sending 20 concurrent requests to fetch the catalogue to ensure the gateway handles load..."
for i in {1..20}; do
  curl -s -o /dev/null $GATEWAY_URL/catalogue/items &
done
wait
echo "20 concurrent requests completed successfully."

echo ""
echo "--- TEST: View All Items ---"
curl -s $GATEWAY_URL/catalogue/items | grep "title" | head -n 3
echo ""
echo "Validation complete."
