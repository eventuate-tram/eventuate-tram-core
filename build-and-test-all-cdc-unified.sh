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
$DOCKER_COMPOSE pause activemq
$DOCKER_COMPOSE pause rabbitmq

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

$DOCKER_COMPOSE stop cdcservice
$DOCKER_COMPOSE rm --force cdcservice

export SPRING_PROFILES_ACTIVE=ActiveMQ

$DOCKER_COMPOSE up -d cdcservice
$DOCKER_COMPOSE unpause activemq
./wait-for-services.sh $DOCKER_HOST_IP 8099
sleep 15

echo TESTING ACTIVEMQ MYSQL BINLOG

. ./set-env-mysql-binlog.sh
./gradlew $GRADLE_OPTIONS :eventuate-tram-e2e-tests-jdbc-activemq:cleanTest :eventuate-tram-e2e-tests-jdbc-activemq:test

echo TESTING ACTIVEMQ POSTGRES POLLING

. ./set-env-postgres-polling.sh
./gradlew $GRADLE_OPTIONS :eventuate-tram-e2e-tests-jdbc-activemq:cleanTest :eventuate-tram-e2e-tests-jdbc-activemq:test

echo TESTING POSTGRES WAL

. ./set-env-postgres-wal.sh
export SPRING_DATASOURCE_URL=jdbc:postgresql://${DOCKER_HOST_IP}:5433/eventuate
./gradlew $GRADLE_OPTIONS :eventuate-tram-e2e-tests-jdbc-activemq:cleanTest :eventuate-tram-e2e-tests-jdbc-activemq:test


$DOCKER_COMPOSE stop cdcservice
$DOCKER_COMPOSE rm --force cdcservice

export SPRING_PROFILES_ACTIVE=RabbitMQ

$DOCKER_COMPOSE up -d cdcservice
$DOCKER_COMPOSE unpause rabbitmq
$DOCKER_COMPOSE stop activemq
./wait-for-services.sh $DOCKER_HOST_IP 8099
sleep 15

echo TESTING RABBITMQ MYSQL BINLOG

. ./set-env-mysql-binlog.sh
./gradlew $GRADLE_OPTIONS :eventuate-tram-e2e-tests-jdbc-rabbitmq:cleanTest :eventuate-tram-e2e-tests-jdbc-rabbitmq:test

echo TESTING RABBITMQ POSTGRES POLLING

. ./set-env-postgres-polling.sh
./gradlew $GRADLE_OPTIONS :eventuate-tram-e2e-tests-jdbc-rabbitmq:cleanTest :eventuate-tram-e2e-tests-jdbc-rabbitmq:test

echo TESTING RABBITMQ POSTGRES WAL

. ./set-env-postgres-wal.sh
export SPRING_DATASOURCE_URL=jdbc:postgresql://${DOCKER_HOST_IP}:5433/eventuate
./gradlew $GRADLE_OPTIONS :eventuate-tram-e2e-tests-jdbc-rabbitmq:cleanTest :eventuate-tram-e2e-tests-jdbc-rabbitmq:test

$DOCKER_COMPOSE stop
$DOCKER_COMPOSE rm --force -v

