#!/bin/bash

set -e

. ./_set-env.sh

if [ -z "$DOCKER_COMPOSE" ]; then
    echo setting DOCKER_COMPOSE
    export DOCKER_COMPOSE="docker-compose -f docker-compose-unified.yml -f docker-compose-cdc-unified.yml"
else
    echo using existing DOCKER_COMPOSE = $DOCKER_COMPOSE
fi

export GRADLE_OPTIONS="-P excludeCdcLibs=true"

./gradlew $GRADLE_OPTIONS $* :eventuate-tram-cdc-mysql-service:clean :eventuate-tram-cdc-mysql-service:assemble


$DOCKER_COMPOSE stop
$DOCKER_COMPOSE rm --force -v

$DOCKER_COMPOSE build
$DOCKER_COMPOSE up -d mysqlbinlogpipeline postgrespollingpipeline postgreswalpipeline

./wait-for-mysql.sh
./wait-for-postgres.sh
export POSTGRES_PORT=5433
./wait-for-postgres.sh

$DOCKER_COMPOSE up -d
./wait-for-services.sh $DOCKER_HOST_IP 8099

echo TESTING KAFKA MYSQL BINLOG

. ./set-env-mysql-binlog.sh

./gradlew $GRADLE_OPTIONS :eventuate-tram-eventuate-local-tests:cleanTest :eventuate-tram-eventuate-local-tests:test
./gradlew $GRADLE_OPTIONS :eventuate-tram-e2e-tests-jdbc-kafka:cleanTest :eventuate-tram-e2e-tests-jdbc-kafka:test

echo TESTING KAFKA POSTGRES POLLING
. ./set-env-postgres-polling.sh

./gradlew $GRADLE_OPTIONS :eventuate-tram-eventuate-local-tests:cleanTest :eventuate-tram-eventuate-local-tests:test
./gradlew $GRADLE_OPTIONS :eventuate-tram-e2e-tests-jdbc-kafka:cleanTest :eventuate-tram-e2e-tests-jdbc-kafka:test

echo TESTING KAFKA POSTGRES WAL

. ./set-env-postgres-wal.sh

export SPRING_DATASOURCE_URL=jdbc:postgresql://${DOCKER_HOST_IP}:5433/eventuate

./gradlew $GRADLE_OPTIONS :eventuate-tram-eventuate-local-tests:cleanTest :eventuate-tram-eventuate-local-tests:test
./gradlew $GRADLE_OPTIONS :eventuate-tram-e2e-tests-jdbc-kafka:cleanTest :eventuate-tram-e2e-tests-jdbc-kafka:test

$DOCKER_COMPOSE stop
$DOCKER_COMPOSE rm --force -v

