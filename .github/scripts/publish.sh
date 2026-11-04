#!/usr/bin/env bash
# Publish one immutable version to Maven Central.
# Usage: publish.sh <version>   version is X.Y.Z, X.Y.Z-dev-N, or X.Y.Z-rc-N
set -euo pipefail

cd "$(cd "$(dirname "$0")" && pwd)/../.."

publish_version="${1:?usage: publish.sh <version>}"

if printf '%s' "$publish_version" | grep -q 'SNAPSHOT'; then
  echo "Refusing to publish a SNAPSHOT version: ${publish_version}" >&2
  exit 1
fi

if ! printf '%s' "$publish_version" | grep -Eq '^[0-9]+\.[0-9]+\.[0-9]+(-dev-[0-9]+|-rc-[0-9]+)?$'; then
  echo "Unsupported version: ${publish_version}" >&2
  echo "Expected X.Y.Z, X.Y.Z-dev-N, or X.Y.Z-rc-N." >&2
  exit 1
fi

echo "Publishing ${publish_version}"
chmod +x ./gradlew
./gradlew publishAndReleaseToMavenCentral -PVERSION_NAME="${publish_version}" --stacktrace
