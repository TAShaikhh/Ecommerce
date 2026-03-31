#!/usr/bin/env bash
set -euo pipefail

curl -fsS http://localhost:8080/health | cat
echo ""
curl -fsS http://localhost:8080/ping/iam | cat
echo ""
curl -fsS http://localhost:8081/db-health | cat
echo ""
