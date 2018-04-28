#! /bin/bash

set -e

. ./set-env-${DATABASE}-${MODE}.sh

docker-compose -f docker-compose-${DATABASE}-${MODE}.yml down -v

docker-compose -f docker-compose-${DATABASE}-${MODE}.yml up --build -d

./wait-for-${DATABASE}.sh

docker-compose -f docker-compose-${DATABASE}-${MODE}.yml pause activemq

./gradlew -x eventuate-tram-activemq-integration-tests:test -x eventuate-tram-e2e-tests-in-memory:test -x eventuate-tram-e2e-tests-jdbc-activemq:test -x eventuate-tram-e2e-tests-jdbc-kafka:test build

docker-compose -f docker-compose-${DATABASE}-${MODE}.yml unpause activemq
docker-compose -f docker-compose-${DATABASE}-${MODE}.yml stop kafka

if [ -z "$SPRING_PROFILES_ACTIVE" ] ; then
  export SPRING_PROFILES_ACTIVE=ActiveMQ
else
  export SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE},ActiveMQ
fi

./gradlew $GRADLE_OPTIONS :eventuate-tram-activemq-integration-test:cleanTest :eventuate-tram-activemq-integration-test:test

docker-compose -f docker-compose-${DATABASE}-${MODE}.yml down -v
