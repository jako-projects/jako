#!/usr/bin/env bash
# Print the Maven version to publish.
# Usage: resolve-version.sh <dev|rc|final>
# Exit 2: this commit was already published as a dev build; caller should skip.
set -euo pipefail

cd "$(cd "$(dirname "$0")" && pwd)/../.."

mode="${1:?usage: resolve-version.sh <dev|rc|final>}"

base="$(grep '^VERSION_NAME=' gradle.properties | head -n 1 | cut -d'=' -f2 | tr -d '\r')"
if ! printf '%s' "$base" | grep -Eq '^[0-9]+\.[0-9]+\.[0-9]+$'; then
  echo "VERSION_NAME must be a plain X.Y.Z release version, got: ${base}" >&2
  exit 1
fi

final_tag="v${base}"
if git rev-parse -q --verify "refs/tags/${final_tag}" >/dev/null; then
  echo "Final version ${base} is already tagged as ${final_tag}." >&2
  if [ "$mode" = "dev" ]; then
    echo "Bump VERSION_NAME on main before publishing more dev builds." >&2
  fi
  exit 1
fi

next_seq() {
  prefix="$1"
  max=0
  tag=""
  num=""
  for tag in $(git tag -l "${prefix}*"); do
    num="${tag#"$prefix"}"
    if printf '%s' "$num" | grep -Eq '^[0-9]+$' && [ "$num" -gt "$max" ]; then
      max="$num"
    fi
  done
  echo $((max + 1))
}

case "$mode" in
  dev)
    existing="$(git tag --points-at HEAD | grep -E "^v${base}-dev-[0-9]+$" || true)"
    if [ -n "$existing" ]; then
      echo "HEAD is already published (${existing}); skipping." >&2
      exit 2
    fi
    echo "${base}-dev-$(next_seq "v${base}-dev-")"
    ;;
  rc)
    echo "${base}-rc-$(next_seq "v${base}-rc-")"
    ;;
  final)
    echo "${base}"
    ;;
  *)
    echo "Unknown mode: ${mode} (expected dev, rc, or final)" >&2
    exit 1
    ;;
esac
