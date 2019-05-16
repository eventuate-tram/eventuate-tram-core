#! /bin/bash -e

BRANCH=$(git rev-parse --abbrev-ref HEAD)

if ! [[  $BRANCH =~ ^[0-9]+ ]] ; then
  echo Not release $BRANCH - no PUSH
  exit 0
elif [[  $BRANCH =~ RELEASE$ ]] ; then
  BINTRAY_REPO_TYPE=release
elif [[  $BRANCH =~ M[0-9]+$ ]] ; then
    BINTRAY_REPO_TYPE=milestone
elif [[  $BRANCH =~ RC[0-9]+$ ]] ; then
    BINTRAY_REPO_TYPE=rc
else
  echo cannot figure out bintray for this branch $BRANCH
  exit -1
fi

echo BINTRAY_REPO_TYPE=${BINTRAY_REPO_TYPE}

VERSION=$BRANCH

$PREFIX ./gradlew -P version=${VERSION} \
  -P bintrayRepoType=${BINTRAY_REPO_TYPE} \
  -P deployUrl=https://dl.bintray.com/eventuateio-oss/eventuate-maven-${BINTRAY_REPO_TYPE} \
  testClasses bintrayUpload
