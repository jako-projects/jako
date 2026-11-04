#!/usr/bin/env bash
# Print the Maven version encoded in a publish tag.
# Usage: validate-tag-version.sh <tag>
# The tag must be vX.Y.Z, vX.Y.Z-rc-N, or vX.Y.Z-dev-N, and X.Y.Z must match VERSION_NAME.
set -euo pipefail

cd "$(cd "$(dirname "$0")" && pwd)/../.."

tag="${1:?usage: validate-tag-version.sh <tag>}"
tag="${tag#refs/tags/}"
version="${tag#v}"

if ! printf '%s' "$version" | grep -Eq '^[0-9]+\.[0-9]+\.[0-9]+(-dev-[0-9]+|-rc-[0-9]+)?$'; then
  echo "Tag ${tag} is not a publishable version." >&2
  echo "Expected vX.Y.Z, vX.Y.Z-rc-N, or vX.Y.Z-dev-N." >&2
  exit 1
fi

base="$(grep '^VERSION_NAME=' gradle.properties | head -n 1 | cut -d'=' -f2 | tr -d '\r')"
case "$version" in
  "$base"|"$base"-rc-[0-9]*|"$base"-dev-[0-9]*) ;;
  *)
    echo "Tag ${tag} does not match VERSION_NAME=${base}." >&2
    exit 1
    ;;
esac

printf '%s\n' "$version"
