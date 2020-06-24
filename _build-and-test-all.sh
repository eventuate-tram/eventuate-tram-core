#! /bin/bash

set -e

. ./set-env-${DATABASE}-${MODE}-${BROKER}.sh

./gradlew $* testClasses

docker="./gradlew ${DATABASE}${MODE}${BROKER}Compose"

${docker}Down -P removeContainers=true
${docker}Up

./gradlew cleanTest build


if [[ "${DATABASE}" == "mysql" ]] && [[ "${BROKER}" == "kafka" ]]; then
    ${docker}Down -P removeContainers=true
    ${docker}Up

    export EVENTUATE_DATABASE_SCHEMA=custom

    ${docker}Up

    export TEST_CUSTOM_DB=true

    ./gradlew cleanTest :eventuate-tram-db-broker-integration-test:test --tests "io.eventuate.tram.broker.db.integrationtests.TramIntegrationCustomDBTest"
    ./gradlew cleanTest :eventuate-tram-commands-db-broker-integration-test:test --tests "io.eventuate.tram.commands.db.broker.integrationtests.TramCommandsDBBrokerIntegrationCustomDBTest"
fi

${docker}Down  -P removeContainers=true
