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
$DOCKER_COMPOSE up -d ${DATABASE} zookeeper kafka

./wait-for-${DATABASE}.sh

$DOCKER_COMPOSE up -d cdcservice
./wait-for-services.sh $DOCKER_HOST_IP 8099

./gradlew $GRADLE_OPTIONS -D:eventuate-tram-mysql-kafka-integration-test:test.single=TramIntegrationTest :eventuate-tram-mysql-kafka-integration-test:test
./gradlew $GRADLE_OPTIONS :eventuate-tram-e2e-tests-in-memory:cleanTest :eventuate-tram-e2e-tests-in-memory:test
./gradlew $GRADLE_OPTIONS :eventuate-tram-e2e-tests-jdbc-kafka:cleanTest :eventuate-tram-e2e-tests-jdbc-kafka:test


if [[ "${DATABASE}" != "mssql" ]]; then

    $DOCKER_COMPOSE stop kafka

    if [[ "${DATABASE}" == "mysql" ]]; then
        $DOCKER_COMPOSE up -d activemq
        $DOCKER_COMPOSE stop cdcservice
        $DOCKER_COMPOSE rm --force cdcservice

        if [ -z "$SPRING_PROFILES_ACTIVE" ] ; then
          export SPRING_PROFILES_ACTIVE=ActiveMQ
        else
          export SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE},ActiveMQ
        fi

        $DOCKER_COMPOSE up -d cdcservice
        ./wait-for-services.sh $DOCKER_HOST_IP 8099

        ./gradlew $GRADLE_OPTIONS :eventuate-tram-e2e-tests-jdbc-activemq:cleanTest :eventuate-tram-e2e-tests-jdbc-activemq:test

        $DOCKER_COMPOSE stop activemq
        $DOCKER_COMPOSE up -d rabbitmq
        $DOCKER_COMPOSE stop cdcservice
        $DOCKER_COMPOSE rm --force cdcservice

        export SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE/ActiveMQ/RabbitMQ}

        $DOCKER_COMPOSE up -d cdcservice
        ./wait-for-services.sh $DOCKER_HOST_IP 8099

        ./gradlew $GRADLE_OPTIONS :eventuate-tram-e2e-tests-jdbc-rabbitmq:cleanTest :eventuate-tram-e2e-tests-jdbc-rabbitmq:test


        $DOCKER_COMPOSE stop rabbitmq
        export SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE/RabbitMQ/Redis}
    else
        if [ -z "$SPRING_PROFILES_ACTIVE" ] ; then
          export SPRING_PROFILES_ACTIVE=Redis
        else
          export SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE},Redis
        fi
    fi

    $DOCKER_COMPOSE stop zookeeper
    $DOCKER_COMPOSE up -d redis
    $DOCKER_COMPOSE stop cdcservice
    $DOCKER_COMPOSE rm --force cdcservice


    $DOCKER_COMPOSE up -d cdcservice
    ./wait-for-services.sh $DOCKER_HOST_IP 8099

    ./gradlew $GRADLE_OPTIONS :eventuate-tram-e2e-tests-jdbc-redis:cleanTest :eventuate-tram-e2e-tests-jdbc-redis:test

    $DOCKER_COMPOSE stop redis
    sleep 10
    $DOCKER_COMPOSE start redis

    ./gradlew $GRADLE_OPTIONS :eventuate-tram-e2e-tests-jdbc-redis:cleanTest :eventuate-tram-e2e-tests-jdbc-redis:test

fi

$DOCKER_COMPOSE stop
$DOCKER_COMPOSE rm --force -v

