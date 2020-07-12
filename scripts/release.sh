#!/bin/sh

SCRIPT_PATH="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/$(basename "${BASH_SOURCE[0]}")"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Go to project root
cd ${SCRIPT_DIR}
cd ..
echo "Working directory: $(pwd)"

# Determine versions
CUR_VERSION=$(cat VERSION)
NEW_VERSION=${1}
echo "Replacing ${CUR_VERSION} with ${NEW_VERSION}"

# Replace all versions
sed -i "s/${CUR_VERSION^^}/${NEW_VERSION^^}/g" service/fling/pom.xml
sed -i "s/${CUR_VERSION,,}/${NEW_VERSION,,}/g" service/fling/src/main/resources/*.yml
sed -i "s/${CUR_VERSION,,}/${NEW_VERSION,,}/g" .drone.yml
sed -i "s/${CUR_VERSION^^}/${NEW_VERSION^^}/g" .drone.yml
sed -i "s/${CUR_VERSION,,}/${NEW_VERSION,,}/g" web/fling/package.json
sed -i "s/${CUR_VERSION}/${NEW_VERSION}/g" VERSION
