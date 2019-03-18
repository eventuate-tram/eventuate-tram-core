#! /bin/bash

set -e

. ./set-env-mysql-binlog.sh

docker-compose -f docker-compose-${BROKER}.yml down -v

docker-compose -f docker-compose-${BROKER}.yml up --build -d

export SPRING_PROFILES_ACTIVE=${PROFILE}

./wait-for-mysql.sh

if [ -d "eventuate-tram-${BROKER}-integration-tests" ]; then
./gradlew $GRADLE_OPTIONS :eventuate-tram-${BROKER}-integration-tests:cleanTest :eventuate-tram-${BROKER}-integration-tests:test $GRADLE_TASK_OPTIONS
fi

./gradlew $GRADLE_OPTIONS :eventuate-tram-consumer-${BROKER}:cleanTest :eventuate-tram-consumer-${BROKER}:test $GRADLE_TASK_OPTIONS

docker-compose -f docker-compose-${BROKER}.yml down -v