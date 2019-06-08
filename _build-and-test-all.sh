#! /bin/bash

set -e

. ./set-env-${DATABASE}-${MODE}.sh

./gradlew $* testClasses

docker-compose -f docker-compose-${DATABASE}-${MODE}.yml down -v --remove-orphans

docker-compose -f docker-compose-${DATABASE}-${MODE}.yml up --build -d

./wait-for-${DATABASE}.sh

./gradlew cleanTest build

docker-compose -f docker-compose-${DATABASE}-${MODE}.yml down -v --remove-orphans
