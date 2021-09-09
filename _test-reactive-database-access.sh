#! /bin/bash

set -e

./gradlew $* testClasses

docker="./gradlew ${DATABASE}${MODE}${BROKER}Compose"

${docker}Down -P removeContainers=true
${docker}Up

./gradlew cleanTest :eventuate-tram-spring-reactive-producer-jdbc:test
./gradlew cleanTest :eventuate-tram-spring-reactive-integration-tests:test

${docker}Down  -P removeContainers=true
