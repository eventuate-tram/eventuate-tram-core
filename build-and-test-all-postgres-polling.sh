#! /bin/bash

set -e

. ./set-env-postgres-polling.sh

docker-compose -f docker-compose-postgres-polling.yml down -v

docker-compose -f docker-compose-postgres-polling.yml up --build -d

./wait-for-postgres.sh

docker-compose -f docker-compose-postgres-polling.yml pause activemq

./gradlew -x eventuate-tram-activemq-integration-tests:test -x eventuate-tram-e2e-tests-in-memory:test -x eventuate-tram-e2e-tests-jdbc-activemq:test -x eventuate-tram-e2e-tests-jdbc-kafka:test build

docker-compose -f docker-compose-postgres-polling.yml unpause activemq
docker-compose -f docker-compose-postgres-polling.yml stop kafka
export ACTIVEMQ_URL=tcp://${DOCKER_HOST_IP}:61616
export SPRING_PROFILES_ACTIVE=EventuatePolling,ActiveMQ

./gradlew $GRADLE_OPTIONS :eventuate-tram-activemq-integration-test:cleanTest :eventuate-tram-activemq-integration-test:test

docker-compose -f docker-compose-postgres-polling.yml down -v
