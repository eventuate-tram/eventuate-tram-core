#! /bin/bash

set -e

. ./set-env-${DATABASE}-${MODE}-${BROKER}.sh

./gradlew $* testClasses

docker-compose -f docker-compose-${DATABASE}-${MODE}-${BROKER}.yml down -v --remove-orphans

docker-compose -f docker-compose-${DATABASE}-${MODE}-${BROKER}.yml up --build -d ${DATABASE} ${BROKER} ${COORDINATOR}

./wait-for-${DATABASE}.sh

docker-compose -f docker-compose-${DATABASE}-${MODE}-${BROKER}.yml up --build -d

./wait-for-services.sh $DOCKER_HOST_IP "actuator/health" 8099

./gradlew cleanTest build


if [[ "${DATABASE}" == "mysql" ]] && [[ "${BROKER}" == "kafka" ]]; then
    docker-compose -f docker-compose-${DATABASE}-${MODE}-${BROKER}.yml stop cdc-service
    docker-compose -f docker-compose-${DATABASE}-${MODE}-${BROKER}.yml rm -f cdc-service

    export EVENTUATE_DATABASE_SCHEMA=custom

    docker-compose -f docker-compose-${DATABASE}-${MODE}-${BROKER}.yml up --build -d cdc-service

    ./wait-for-services.sh $DOCKER_HOST_IP "actuator/health" 8099

    export TEST_CUSTOM_DB=true

    ./gradlew cleanTest :eventuate-tram-db-broker-integration-test:test --tests "io.eventuate.tram.broker.db.integrationtests.TramIntegrationCustomDBTest"
    ./gradlew cleanTest :eventuate-tram-commands-db-broker-integration-test:test --tests "io.eventuate.tram.commands.db.broker.integrationtests.TramCommandsDBBrokerIntegrationCustomDBTest"
fi

docker-compose -f docker-compose-${DATABASE}-${MODE}-${BROKER}.yml down -v --remove-orphans
