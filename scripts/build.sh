#!/usr/bin/env bash
# Сборка NevaVis. Использование: bash scripts/build.sh [--setup-only]
set -euo pipefail
cd "$(dirname "$0")/.."

if [ ! -f gradle/wrapper/gradle-wrapper.jar ]; then
  echo ">> Gradle wrapper не найден, создаю (Gradle 8.12)..."
  if ! command -v gradle >/dev/null 2>&1; then
    echo ">> gradle не установлен, ставлю через SDKMAN..."
    source "$HOME/.sdkman/bin/sdkman-init.sh"
    sdk install gradle 8.12 </dev/null
  fi
  gradle wrapper --gradle-version 8.12
fi

if [ "${1:-}" = "--setup-only" ]; then
  echo ">> Готово. Запустите: bash scripts/build.sh"
  exit 0
fi

chmod +x gradlew
./gradlew clean build --no-daemon
echo
echo ">> Результат:"
ls -la build/libs/
