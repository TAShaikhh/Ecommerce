#!/usr/bin/env bash
set -e
BASE=${BASE_URL:-http://localhost:8080}

curl -i -X POST "$BASE/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "username":"nathan",
    "password":"wrongpass"
  }'
echo ""