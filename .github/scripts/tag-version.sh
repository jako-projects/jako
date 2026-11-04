#!/usr/bin/env bash
# Tag the current commit after a successful publish and push the tag.
# Usage: tag-version.sh <version>
set -euo pipefail

cd "$(cd "$(dirname "$0")" && pwd)/../.."

version="${1:?usage: tag-version.sh <version>}"
tag="v${version}"

if git rev-parse -q --verify "refs/tags/${tag}" >/dev/null; then
  echo "Tag ${tag} already exists." >&2
  exit 1
fi

git config user.name "github-actions[bot]"
git config user.email "41898282+github-actions[bot]@users.noreply.github.com"
git tag -a "${tag}" -m "Publish ${version}"
# The workflow token does not start the tag-triggered Publish workflow.
git push origin "refs/tags/${tag}"
echo "Pushed ${tag}"
