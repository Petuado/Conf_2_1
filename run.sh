#!/usr/bin/env bash
set -euo pipefail

# --- определяем корень проекта (где лежит этот скрипт) ---
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# --- ищем исходники: сначала в app/, потом в корне ---
if [[ -d "$ROOT/app/src/main/kotlin" ]]; then
    SRC="$ROOT/app/src/main/kotlin"
elif [[ -d "$ROOT/src/main/kotlin" ]]; then
    SRC="$ROOT/src/main/kotlin"
else
    echo "ERROR: cannot find src/main/kotlin (tried app/ and root)" >&2
    exit 1
fi

OUT_DIR="$ROOT/build"
JAR="$OUT_DIR/app.jar"
mkdir -p "$OUT_DIR"

echo ">> Source dir : $SRC"
echo ">> Output jar : $JAR"

if ! command -v kotlinc >/dev/null 2>&1; then
    echo "ERROR: kotlinc not found in PATH." >&2
    echo "Install with: brew install kotlin" >&2
    exit 1
fi

echo ">> Compiling..."
kotlinc "$SRC" -include-runtime -d "$JAR"

CONFIG="${1:-$ROOT/config.csv}"
if [[ ! -f "$CONFIG" ]]; then
    echo "ERROR: config file not found: $CONFIG" >&2
    exit 1
fi

echo ">> Running with config: $CONFIG"
java -jar "$JAR" "$CONFIG"