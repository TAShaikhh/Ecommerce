#!/usr/bin/env bash
set -e
BASE=${BASE_URL:-http://localhost:8080}

curl -s -X POST "$BASE/auth/signup" \
  -H "Content-Type: application/json" \
  -d '{
    "username":"nathan",
    "password":"pass1234",
    "firstName":"Nathan",
    "lastName":"B",
    "address":"123 Test St, Toronto"
  }' | cat
echo ""