#!/bin/bash

set -e

if [ -z "$DOCKER_COMPOSE" ]; then
    echo setting DOCKER_COMPOSE
    export DOCKER_COMPOSE="docker-compose -f docker-compose-mysql-binlog.yml -f docker-compose-cdc-mysql-binlog.yml"
else
    echo using existing DOCKER_COMPOSE = $DOCKER_COMPOSE
fi

export GRADLE_OPTIONS="-P excludeCdcLibs=true"

./gradlew $GRADLE_OPTIONS $* :eventuate-tram-cdc-mysql-service:clean :eventuate-tram-cdc-mysql-service:assemble

. ./set-env-mysql-binlog.sh

$DOCKER_COMPOSE down -v

$DOCKER_COMPOSE build
$DOCKER_COMPOSE up -d mysql

./wait-for-mysql.sh

$DOCKER_COMPOSE up -d

./wait-for-services.sh $DOCKER_HOST_IP 8099

./gradlew $GRADLE_OPTIONS -D:eventuate-tram-mysql-kafka-integration-test:test.single=TramIntegrationTest :eventuate-tram-mysql-kafka-integration-test:cleanTest :eventuate-tram-mysql-kafka-integration-test:test

$DOCKER_COMPOSE stop
$DOCKER_COMPOSE rm --force -v
