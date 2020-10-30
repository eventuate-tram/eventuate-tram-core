#! /bin/bash

set -e

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

${docker}Down -P removeContainers=true
export USE_DB_ID=true
export EVENTUATE_OUTBOX_ID=1
unset TEST_CUSTOM_DB
unset EVENTUATE_DATABASE_SCHEMA
${docker}Up

./gradlew cleanTest build

${docker}Down  -P removeContainers=true
