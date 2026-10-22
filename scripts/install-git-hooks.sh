#!/usr/bin/env bash
set -e

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
HOOKS_DIR="$REPO_ROOT/.git/hooks"

mkdir -p "$HOOKS_DIR"

cat << 'EOF' > "$HOOKS_DIR/pre-commit"
#!/usr/bin/env bash
set -e

echo "🔍 [pre-commit] Running jako JDK target parity & backward compatibility checks..."
if ! ./gradlew :jako-stdlib:jvmTest --tests "jako.test.parity.JdkSignatureParityTest" --quiet; then
    echo "❌ [pre-commit] Commit BLOCKED: API compatibility or JDK target parity check failed!" >&2
    echo "💡 Run './gradlew checkApiCompatibility' to see detailed failure diagnostics." >&2
    echo "   - If you added a new public API, ensure it is documented in API_CHECKLIST.md." >&2
    echo "   - If you intentionally modified the public API baseline, run './gradlew dumpApiBaseline'." >&2
    exit 1
fi
echo "✅ [pre-commit] API compatibility & JDK parity checks passed."
exit 0
EOF

chmod +x "$HOOKS_DIR/pre-commit"
echo "✅ Installed $HOOKS_DIR/pre-commit"
