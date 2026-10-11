#!/bin/bash
set -e
getProp(){
   grep "${1}" gradle.properties | cut -d'=' -f2 | sed 's/\r//'
}
publishVersion=$(getProp VERSION_NAME)
snapshotSuffix='SNAPSHOT'

chmod +x ./gradlew
if [[ "$publishVersion" != *"$snapshotSuffix"* ]]; then
  echo "publish and auto release artifacts of ${publishVersion}"
  ./gradlew publishAndReleaseToMavenCentral
else
  ./gradlew publishToMavenCentral
fi

