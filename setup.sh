#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BACKEND="$ROOT/backend"
WEB="$ROOT/web-admin"

if command -v python3 >/dev/null 2>&1; then
    PYTHON=python3
elif command -v python >/dev/null 2>&1; then
    PYTHON=python
else
    printf '%s\n' "Python 3.10 or newer is required."
    exit 1
fi

if ! command -v node >/dev/null 2>&1 || ! command -v npm >/dev/null 2>&1; then
    printf '%s\n' "Node.js 20 or newer and npm are required."
    exit 1
fi

"$PYTHON" -c 'import sys; raise SystemExit(0 if sys.version_info >= (3, 10) else "Python 3.10 or newer is required.")'
printf 'Using Python: '
"$PYTHON" --version
node_major="$(node -p 'Number(process.versions.node.split(".")[0])')"
if [ "$node_major" -lt 20 ]; then
    printf '%s\n' "Node.js 20 or newer is required."
    exit 1
fi
printf 'Using Node.js: '
node --version

if [ ! -f "$BACKEND/.env" ]; then
    cp "$BACKEND/.env.example" "$BACKEND/.env"
fi
if [ ! -f "$WEB/.env" ]; then
    cp "$WEB/.env.example" "$WEB/.env"
fi

if [ ! -x "$BACKEND/.venv/bin/python" ]; then
    "$PYTHON" -m venv "$BACKEND/.venv"
fi

"$BACKEND/.venv/bin/python" -m pip install --upgrade pip
"$BACKEND/.venv/bin/python" -m pip install -r "$BACKEND/requirements.txt"

cd "$WEB"
npm ci

printf '\n%s\n' "Setup completed."
printf '%s\n' "Configure backend/.env and web-admin/.env before starting the applications."
printf '%s\n' "Create the PostgreSQL database and run backend/schemas/schema.sql."
printf '%s\n' "Start the API: cd backend && .venv/bin/python -m uvicorn main:app --reload --host 0.0.0.0 --port 8000"
printf '%s\n' "Start Web Admin: cd web-admin && npm run dev"
