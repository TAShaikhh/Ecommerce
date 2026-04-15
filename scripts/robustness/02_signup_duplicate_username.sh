#!/usr/bin/env bash
set -e
BASE=${BASE_URL:-http://localhost:8080}

# First signup (should succeed)
curl -s -X POST "$BASE/auth/signup" \
  -H "Content-Type: application/json" \
  -d '{
    "username":"dupeuser",
    "password":"pass1234",
    "firstName":"Dupe",
    "lastName":"User",
    "address":"123 Test St"
  }' > /dev/null

# Second signup (should fail)
curl -i -X POST "$BASE/auth/signup" \
  -H "Content-Type: application/json" \
  -d '{
    "username":"dupeuser",
    "password":"pass1234",
    "firstName":"Dupe",
    "lastName":"User",
    "address":"123 Test St"
  }'
echo ""