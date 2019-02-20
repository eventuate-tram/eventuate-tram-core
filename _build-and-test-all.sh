#! /bin/bash

set -e

. ./set-env-${DATABASE}-${MODE}.sh

./gradlew $* testClasses

docker-compose -f docker-compose-${DATABASE}-${MODE}.yml down -v

docker-compose -f docker-compose-${DATABASE}-${MODE}.yml up --build -d

./wait-for-${DATABASE}.sh

./gradlew -x eventuate-tram-consumer-redis:test -x eventuate-tram-eventuate-local-tests:test -x eventuate-tram-rabbitmq-integration-tests:test -x eventuate-tram-activemq-integration-tests:test -x eventuate-tram-e2e-tests-in-memory:test -x eventuate-tram-e2e-tests-jdbc-rabbitmq:test -x eventuate-tram-e2e-tests-jdbc-activemq:test -x eventuate-tram-e2e-tests-jdbc-kafka:test  -x eventuate-tram-e2e-tests-jdbc-redis:test build

docker-compose -f docker-compose-${DATABASE}-${MODE}.yml down -v
