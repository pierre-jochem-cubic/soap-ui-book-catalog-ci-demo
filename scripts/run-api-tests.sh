#!/usr/bin/env bash
# Run the SoapUI suite locally, the same way CI does:
#   build jar -> start app -> wait for health -> testrunner -> stop app
#
# Usage:  SOAPUI_HOME=/path/to/SoapUI-5.9.1 ./scripts/run-api-tests.sh
set -euo pipefail

cd "$(dirname "$0")/.."

: "${SOAPUI_HOME:?Set SOAPUI_HOME to your SoapUI installation directory}"
APP_URL="${APP_URL:-http://localhost:8080}"
REPORT_DIR="reports/soapui"

./mvnw -B -q package -DskipTests

java -jar target/app.jar > app.log 2>&1 &
APP_PID=$!
trap 'kill "$APP_PID" 2>/dev/null || true' EXIT

echo "Waiting for $APP_URL/actuator/health ..."
timeout 90 bash -c "until curl -sf $APP_URL/actuator/health > /dev/null; do sleep 2; done" \
  || { echo "App did not start, see app.log"; exit 1; }

"$SOAPUI_HOME/bin/testrunner.sh" \
  -j -r \
  -f "$PWD/$REPORT_DIR" \
  -PbaseUrl="$APP_URL" \
  "$PWD/soapui/book-catalog-soapui-project.xml"

echo "JUnit reports written to $REPORT_DIR"
