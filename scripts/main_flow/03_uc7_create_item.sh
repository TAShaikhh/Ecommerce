#!/usr/bin/env bash
set -e
BASE=${BASE_URL:-http://localhost:8080}
TOKEN=${TOKEN:-}

if [ -z "$TOKEN" ]; then
  echo "TOKEN is required. Export TOKEN from /auth/login first."
  exit 1
fi

curl -s -X POST "$BASE/items" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "title":"iPhone 15 Pro",
    "description":"Good condition, 256GB",
    "condition":"USED",
    "keywords":"iphone phone apple",
    "shippingCost":12.0,
    "expeditedShippingCost":6.0,
    "shippingDays":5,
    "startingPrice":200,
    "auctionDurationSeconds":145
  }' | cat
echo ""
