#!/usr/bin/env bash
set -e
BASE=${BASE_URL:-http://localhost:8080}

curl -i -X POST "$BASE/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "username":"this_user_should_not_exist_999",
    "password":"pass1234"
  }'
echo ""