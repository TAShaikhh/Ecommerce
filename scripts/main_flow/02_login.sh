#!/usr/bin/env bash
set -e
BASE=${BASE_URL:-http://localhost:8080}

curl -s -X POST "$BASE/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "username":"nathan",
    "password":"pass1234"
  }' | cat
echo ""