#!/bin/bash
# start.sh - starts everything Sora needs from a cold machine (macOS)
# Run from the repo root: ./start.sh

REPO_ROOT="$(cd "$(dirname "$0")" && pwd)"

# ---- Colors -----------------------------------------------------------------

CYAN='\033[0;36m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

step()  { echo "" ; echo -e "${CYAN}> $1${NC}"; }
ok()    { echo -e "${GREEN}  OK  $1${NC}"; }
warn()  { echo -e "${YELLOW}  ... $1${NC}"; }
die()   { echo -e "${RED}  ERR $1${NC}"; exit 1; }

# ---- 1. Find Java 21+ -------------------------------------------------------

step "Java"

JAVA_HOME_FOUND=""

# Try the current JAVA_HOME first
if [ -n "$JAVA_HOME" ] && [ -x "$JAVA_HOME/bin/java" ]; then
    version=$("$JAVA_HOME/bin/java" -version 2>&1 | grep -oE '"[0-9]+' | grep -oE '[0-9]+' | head -1)
    if [ "$version" -ge 21 ] 2>/dev/null; then
        JAVA_HOME_FOUND="$JAVA_HOME"
    fi
fi

# macOS java_home utility
if [ -z "$JAVA_HOME_FOUND" ] && [ -x /usr/libexec/java_home ]; then
    jh=$(/usr/libexec/java_home -v 21 2>/dev/null || true)
    if [ -n "$jh" ]; then JAVA_HOME_FOUND="$jh"; fi
fi

# IntelliJ bundled JBR (system or user Applications)
if [ -z "$JAVA_HOME_FOUND" ]; then
    for candidate in \
        "/Applications/IntelliJ IDEA.app/Contents/jbr/Contents/Home" \
        "$HOME/Applications/IntelliJ IDEA.app/Contents/jbr/Contents/Home"; do
        if [ -x "$candidate/bin/java" ]; then
            JAVA_HOME_FOUND="$candidate"
            break
        fi
    done
fi

if [ -z "$JAVA_HOME_FOUND" ]; then
    die "Could not find Java 21+. Install it (brew install --cask temurin@21) or set JAVA_HOME."
fi

export JAVA_HOME="$JAVA_HOME_FOUND"
ok "Java -> $JAVA_HOME"

# ---- 2. Docker Desktop ------------------------------------------------------

step "Docker Desktop"

if ! docker info &>/dev/null; then
    warn "Docker not running - starting Docker Desktop..."
    open -a Docker
    echo -n "  Waiting for Docker to be ready"
    elapsed=0
    while [ $elapsed -lt 120 ]; do
        sleep 3
        elapsed=$((elapsed + 3))
        echo -n "."
        if docker info &>/dev/null; then break; fi
    done
    echo ""
    docker info &>/dev/null || die "Docker did not start in time. Open Docker Desktop manually and re-run."
fi
ok "Docker is running"

# ---- 3. Postgres ------------------------------------------------------------

step "Postgres"
cd "$REPO_ROOT"
docker compose up -d
ok "sora-postgres is up (port 5432)"

# ---- 4. Ollama --------------------------------------------------------------

step "Ollama"

if ! pgrep -ix "ollama" &>/dev/null; then
    warn "Ollama not running - starting..."
    if [ -d "/Applications/Ollama.app" ]; then
        open -a Ollama
    elif command -v ollama &>/dev/null; then
        ollama serve &>/dev/null &
    else
        die "Ollama not found. Install from https://ollama.com then run: ollama pull llama3.2:3b-instruct-q8_0"
    fi
    sleep 3
    ok "Ollama started (http://localhost:11434)"
else
    ok "Ollama already running"
fi

# ---- 5. Backend -------------------------------------------------------------

step "Backend (Spring Boot - http://localhost:8080)"

osascript <<EOF
tell application "Terminal"
    do script "cd '$REPO_ROOT/backend' && export JAVA_HOME='$JAVA_HOME' && echo '--- SORA BACKEND ---' && ./mvnw spring-boot:run '-Dspring-boot.run.profiles=local'"
    activate
end tell
EOF

ok "Backend window opened"

# ---- 6. Frontend ------------------------------------------------------------

step "Frontend (Vue - http://localhost:5173)"

osascript <<EOF
tell application "Terminal"
    do script "cd '$REPO_ROOT/frontend' && echo '--- SORA FRONTEND ---' && npm run dev"
    activate
end tell
EOF

ok "Frontend window opened"

# ---- Done -------------------------------------------------------------------

echo ""
echo "=========================================="
echo "  Sora is starting up"
echo ""
echo "  Frontend  ->  http://localhost:5173"
echo "  Backend   ->  http://localhost:8080"
echo "  Postgres  ->  localhost:5432 (db: sora)"
echo "  Ollama    ->  http://localhost:11434"
echo ""
echo "  Backend takes ~30s. Watch for"
echo "  'Started BackendApplication' in its window."
echo "=========================================="
