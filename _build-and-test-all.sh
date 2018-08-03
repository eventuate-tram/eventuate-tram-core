#! /bin/bash

set -e

. ./set-env-${DATABASE}-${MODE}.sh

docker-compose -f docker-compose-${DATABASE}-${MODE}.yml down -v

docker-compose -f docker-compose-${DATABASE}-${MODE}.yml up --build -d

./wait-for-${DATABASE}.sh

./gradlew -x eventuate-tram-rabbitmq-integration-tests:test -x eventuate-tram-activemq-integration-tests:test -x eventuate-tram-e2e-tests-in-memory:test -x eventuate-tram-e2e-tests-jdbc-rabbitmq:test -x eventuate-tram-e2e-tests-jdbc-activemq:test -x eventuate-tram-e2e-tests-jdbc-kafka:test build

docker-compose -f docker-compose-${DATABASE}-${MODE}.yml down -v
