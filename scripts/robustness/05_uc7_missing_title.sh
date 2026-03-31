#!/usr/bin/env bash
set -e
BASE=${BASE_URL:-http://localhost:8080}
TOKEN=${TOKEN:-}

if [ -z "$TOKEN" ]; then
  echo "TOKEN is required. Export TOKEN from /auth/login first."
  exit 1
fi

curl -i -X POST "$BASE/items" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "description":"d",
    "condition":"USED",
    "keywords":"test",
    "shippingCost":10,
    "expeditedShippingCost":0,
    "shippingDays":5,
    "startingPrice":10,
    "auctionDurationSeconds":120
  }'
echo ""
