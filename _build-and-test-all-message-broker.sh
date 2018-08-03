#! /bin/bash

set -e

. ./set-env-mysql-binlog.sh

docker-compose -f docker-compose-${BROKER}.yml down -v

docker-compose -f docker-compose-${BROKER}.yml up --build -d

export SPRING_PROFILES_ACTIVE=${PROFILE}

./wait-for-mysql.sh

./gradlew $GRADLE_OPTIONS :eventuate-tram-${BROKER}-integration-test:cleanTest :eventuate-tram-${BROKER}-integration-test:test

docker-compose -f docker-compose-${BROKER}.yml down -v