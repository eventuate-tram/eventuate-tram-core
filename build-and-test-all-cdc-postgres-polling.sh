#!/bin/bash

set -e

export SPRING_PROFILES_ACTIVE=EventuatePolling

if [ -z "$DOCKER_COMPOSE" ]; then
    echo setting DOCKER_COMPOSE
    export DOCKER_COMPOSE="docker-compose -f docker-compose-postgres-polling.yml -f docker-compose-cdc-postgres-polling.yml"
else
    echo using existing DOCKER_COMPOSE = $DOCKER_COMPOSE
fi

export GRADLE_OPTIONS="-P excludeCdcLibs=true"

./gradlew $GRADLE_OPTIONS $* :eventuate-tram-cdc-mysql-service:clean :eventuate-tram-cdc-mysql-service:assemble

. ./set-env-postgres-polling.sh

$DOCKER_COMPOSE stop
$DOCKER_COMPOSE rm --force -v

$DOCKER_COMPOSE build
$DOCKER_COMPOSE up -d postgres

./wait-for-postgres.sh

$DOCKER_COMPOSE up -d

docker-compose -f docker-compose-postgres-polling.yml pause activemq

#./gradlew $GRADLE_OPTIONS :eventuate-tram-mysql-kafka-integration-test:cleanTest :eventuate-tram-mysql-kafka-integration-test:test
#./gradlew $GRADLE_OPTIONS :eventuate-tram-e2e-tests-in-memory:cleanTest :eventuate-tram-e2e-tests-in-memory:test
#./gradlew $GRADLE_OPTIONS :eventuate-tram-e2e-tests-jdbc-kafka:cleanTest :eventuate-tram-e2e-tests-jdbc-kafka:test

$DOCKER_COMPOSE stop cdcservice
$DOCKER_COMPOSE rm --force cdcservice
export ACTIVEMQ_URL=tcp://${DOCKER_HOST_IP}:61616
export SPRING_PROFILES_ACTIVE=EventuatePolling,ActiveMQ
$DOCKER_COMPOSE up -d cdcservice
$DOCKER_COMPOSE unpause activemq
$DOCKER_COMPOSE stop kafka

sleep 20

./gradlew $GRADLE_OPTIONS :eventuate-tram-e2e-tests-jdbc-activemq:cleanTest :eventuate-tram-e2e-tests-jdbc-activemq:test

$DOCKER_COMPOSE stop
$DOCKER_COMPOSE rm --force -v



