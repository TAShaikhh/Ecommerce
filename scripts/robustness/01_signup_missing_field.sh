#!/usr/bin/env bash
set -e
BASE=${BASE_URL:-http://localhost:8080}

curl -i -X POST "$BASE/auth/signup" \
  -H "Content-Type: application/json" \
  -d '{
    "username":"missingpass",
    "firstName":"A",
    "lastName":"B",
    "address":"Somewhere"
  }'
echo ""