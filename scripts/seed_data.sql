-- =============================================================================
-- PrimeBid Forward Auction System - Seed Data
-- Run this after services are started to populate sample data
-- =============================================================================

-- IAM Service (port 8081) - Run against iam/data/iam.db
-- Note: Passwords are BCrypt hashes of "password123"
-- You can also use the API to create users:
--   curl -X POST http://localhost:8080/auth/signup -H "Content-Type: application/json" \
--     -d '{"username":"alice","password":"password123","firstName":"Alice","lastName":"Smith","address":"123 Main St"}'

-- Catalogue Service (port 8082) - Sample items via API:
-- curl -X POST http://localhost:8082/items -H "Content-Type: application/json" \
--   -d '{"ownerUsername":"alice","title":"Vintage Watch","description":"1960s Omega Seamaster","condition":"USED","keywords":"watch omega vintage","shippingCost":12.0,"expeditedShippingCost":8.0,"shippingDays":5}'

-- curl -X POST http://localhost:8082/items -H "Content-Type: application/json" \
--   -d '{"ownerUsername":"alice","title":"Gaming Console","description":"PS5 with controller","condition":"NEW","keywords":"ps5 gaming console sony","shippingCost":20.0,"expeditedShippingCost":15.0,"shippingDays":7}'

-- curl -X POST http://localhost:8082/items -H "Content-Type: application/json" \
--   -d '{"ownerUsername":"alice","title":"Acoustic Guitar","description":"Martin D-28 acoustic guitar","condition":"USED","keywords":"guitar martin acoustic music","shippingCost":25.0,"expeditedShippingCost":12.0,"shippingDays":10}'

-- Recommended: Use the full_flow.sh script to create users, items, and auctions via the gateway
-- This ensures all services are properly orchestrated and sessions are created
