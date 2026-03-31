#!/usr/bin/env bash
set -euo pipefail

PORTS=(8080 8081 8082 8083 8084)
SERVICES=(iam catalogue auction payment gateway)
PIDS=()
CLEANED_UP=0

mkdir -p logs

# Ensure per-service SQLite directories exist.
for s in "${SERVICES[@]}"; do
  mkdir -p "$s/data"
done

ensure_java_home() {
  if [[ -n "${JAVA_HOME:-}" && -x "${JAVA_HOME}/bin/java" ]]; then
    return
  fi

  local java_bin
  java_bin=$(command -v java || true)
  if [[ -z "$java_bin" ]]; then
    echo "Java is required but was not found in PATH."
    exit 1
  fi

  local candidate
  candidate=$(cd "$(dirname "$java_bin")/.." && pwd -P)
  if [[ -x "${candidate}/bin/java" ]]; then
    export JAVA_HOME="$candidate"
    echo "JAVA_HOME was not set; using detected JAVA_HOME=$JAVA_HOME"
    return
  fi

  echo "JAVA_HOME is not set and could not be derived from java in PATH."
  exit 1
}

start_service () {
  local name=$1
  echo "Starting $name..."
  (cd "$name" && ./mvnw -q spring-boot:run) >"logs/$name.log" 2>&1 &
  PIDS+=("$!")
}

cleanup() {
  # prevent double cleanup (INT + EXIT)
  if [[ "$CLEANED_UP" -eq 1 ]]; then
    return
  fi
  CLEANED_UP=1

  echo ""
  echo "Stopping all services..."

  # 1) polite stop: SIGINT to the mvn processes (less noisy than kill -9)
  for pid in "${PIDS[@]}"; do
    kill -0 "$pid" 2>/dev/null && kill -INT "$pid" 2>/dev/null || true
  done

  # 2) wait a moment for graceful shutdown
  for _ in {1..20}; do
    local any=0
    for pid in "${PIDS[@]}"; do
      if kill -0 "$pid" 2>/dev/null; then any=1; fi
    done
    [[ "$any" -eq 0 ]] && break
    sleep 0.2
  done

  # 3) last resort: force kill anything still alive
  for pid in "${PIDS[@]}"; do
    kill -0 "$pid" 2>/dev/null && kill -KILL "$pid" 2>/dev/null || true
  done

  # 4) safety net: free ports if anything else is still listening
  for p in "${PORTS[@]}"; do
    lsof -ti tcp:$p | xargs -r kill -KILL 2>/dev/null || true
  done

  echo "All services stopped."
  echo "Logs are in ./logs (e.g., logs/gateway.log)"
}

trap cleanup INT TERM EXIT

ensure_java_home

for s in "${SERVICES[@]}"; do
  start_service "$s"
done

echo ""
echo "Ports:"
echo "  gateway   8080"
echo "  iam       8081"
echo "  catalogue 8082"
echo "  auction   8083"
echo "  payment   8084"
echo ""
echo "Try:"
echo "  curl http://localhost:8080/health"
echo "  curl http://localhost:8080/ping/iam"
echo ""
echo "To view logs: tail -f logs/gateway.log"
echo ""

wait
