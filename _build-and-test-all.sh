#! /bin/bash

set -e

. ./set-env-${DATABASE}-${MODE}.sh

./gradlew $* testClasses

docker-compose -f docker-compose-${DATABASE}-${MODE}.yml down -v --remove-orphans

docker-compose -f docker-compose-${DATABASE}-${MODE}.yml up --build -d

./wait-for-${DATABASE}.sh

./gradlew -x eventuate-tram-redis-integration-tests:test -x eventuate-tram-consumer-redis:test -x eventuate-tram-rabbitmq-integration-tests:test -x eventuate-tram-activemq-integration-tests:test build

docker-compose -f docker-compose-${DATABASE}-${MODE}.yml down -v --remove-orphans
