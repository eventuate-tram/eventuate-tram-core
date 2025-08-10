#! /bin/bash

set -e


docker="./gradlew ${DATABASE}${MODE}${BROKER}Compose"


if [ -z "$TEST_DB_ID" ] ; then

./gradlew $* testClasses

  ${docker}Down -P removeContainers=true

  ${docker}Up

  ./gradlew build

  if [[ "${DATABASE}" == "mysql" ]] && [[ "${BROKER}" == "kafka" ]]; then
      ${docker}Down -P removeContainers=true
      ${docker}Up

      export EVENTUATE_DATABASE_SCHEMA=custom

      ${docker}Up

      export TEST_CUSTOM_DB=true

      ./gradlew :eventuate-tram-db-broker-integration-test:cleanTest :eventuate-tram-db-broker-integration-test:test --tests "io.eventuate.tram.broker.db.integrationtests.TramIntegrationCustomDBTest"
      ./gradlew :eventuate-tram-commands-db-broker-integration-test:cleanTest :eventuate-tram-commands-db-broker-integration-test:test --tests "io.eventuate.tram.commands.db.broker.integrationtests.TramCommandsDBBrokerIntegrationCustomDBTest"
  fi

else
  echo ==== TESTING DB_ID

  ./gradlew $* testClasses

    ${docker}Down -P removeContainers=true

  export USE_DB_ID=true
  export EVENTUATE_OUTBOX_ID=1
  unset TEST_CUSTOM_DB
  unset EVENTUATE_DATABASE_SCHEMA
  ${docker}Up

  ./gradlew build
fi

${docker}Down  -P removeContainers=true
