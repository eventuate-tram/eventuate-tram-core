#! /bin/bash -e

BRANCH=$(git rev-parse --abbrev-ref HEAD)

if ! [[  $BRANCH =~ ^[0-9]+ ]] ; then
  echo Not release $BRANCH
  exit 0
fi

VERSION=$BRANCH

$PREFIX ./gradlew -P version=${VERSION} \
  -P deployUrl=https://dl.bintray.com/eventuateio-oss/eventuate-maven-release \
  testClasses bintrayUpload

DOCKER_REPO=eventuateio
DOCKER_COMPOSE_PREFIX=eventuatetramcore_

function tagAndPush() {
  LOCAL=$1
  REMOTE=$2
  $PREFIX docker tag ${DOCKER_COMPOSE_PREFIX?}$LOCAL $DOCKER_REPO/$REMOTE:$VERSION
  $PREFIX docker tag ${DOCKER_COMPOSE_PREFIX?}$LOCAL $DOCKER_REPO/$REMOTE:latest
  echo Pushing $DOCKER_REPO/$REMOTE:$VERSION
  $PREFIX docker push $DOCKER_REPO/$REMOTE:$VERSION
  $PREFIX docker push $DOCKER_REPO/$REMOTE:latest
}

$PREFIX docker login -u ${DOCKER_USER_ID?} -p ${DOCKER_PASSWORD?}

tagAndPush "cdcservice" "eventuate-tram-cdc-mysql-service"
tagAndPush "mysql" "eventuate-tram-mysql"
tagAndPush "postgres" "eventuate-tram-postgres"
