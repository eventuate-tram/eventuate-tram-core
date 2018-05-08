#!/bin/bash

set -e

if [ -z "$DOCKER_COMPOSE" ]; then
    echo setting DOCKER_COMPOSE
    export DOCKER_COMPOSE="docker-compose -f docker-compose-${DATABASE}-${MODE}.yml -f docker-compose-cdc-${DATABASE}-${MODE}.yml"
else
    echo using existing DOCKER_COMPOSE = $DOCKER_COMPOSE
fi

export GRADLE_OPTIONS="-P excludeCdcLibs=true"

./gradlew $GRADLE_OPTIONS $* :eventuate-tram-cdc-mysql-service:clean :eventuate-tram-cdc-mysql-service:assemble

. ./set-env-${DATABASE}-${MODE}.sh

$DOCKER_COMPOSE stop
$DOCKER_COMPOSE rm --force -v

$DOCKER_COMPOSE build
$DOCKER_COMPOSE up -d ${DATABASE}

./wait-for-${DATABASE}.sh

$DOCKER_COMPOSE up -d
./wait-for-services.sh $DOCKER_HOST_IP 8099
$DOCKER_COMPOSE pause activemq
$DOCKER_COMPOSE pause rabbitmq

./gradlew $GRADLE_OPTIONS :eventuate-tram-mysql-kafka-integration-test:cleanTest :eventuate-tram-mysql-kafka-integration-test:test
./gradlew $GRADLE_OPTIONS :eventuate-tram-e2e-tests-in-memory:cleanTest :eventuate-tram-e2e-tests-in-memory:test
./gradlew $GRADLE_OPTIONS :eventuate-tram-e2e-tests-jdbc-kafka:cleanTest :eventuate-tram-e2e-tests-jdbc-kafka:test



$DOCKER_COMPOSE stop cdcservice
$DOCKER_COMPOSE rm --force cdcservice

if [ -z "$SPRING_PROFILES_ACTIVE" ] ; then
  export SPRING_PROFILES_ACTIVE=ActiveMQ
else
  export SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE},ActiveMQ
fi

$DOCKER_COMPOSE up -d cdcservice
./wait-for-services.sh $DOCKER_HOST_IP 8099
$DOCKER_COMPOSE unpause activemq
$DOCKER_COMPOSE stop kafka

./gradlew $GRADLE_OPTIONS :eventuate-tram-e2e-tests-jdbc-activemq:cleanTest :eventuate-tram-e2e-tests-jdbc-activemq:test



$DOCKER_COMPOSE stop cdcservice
$DOCKER_COMPOSE rm --force cdcservice

export SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE/ActiveMQ/RabbitMQ}

$DOCKER_COMPOSE up -d cdcservice
./wait-for-services.sh $DOCKER_HOST_IP 8099
$DOCKER_COMPOSE unpause rabbitmq
$DOCKER_COMPOSE stop activemq

./gradlew $GRADLE_OPTIONS :eventuate-tram-e2e-tests-jdbc-rabbitmq:cleanTest :eventuate-tram-e2e-tests-jdbc-rabbitmq:test



$DOCKER_COMPOSE stop
$DOCKER_COMPOSE rm --force -v

