#! /bin/bash

set -e

. ./set-env-postgres.sh

docker-compose -f docker-compose-postgres.yml down -v

docker-compose -f docker-compose-postgres.yml up -d

./wait-for-postgres.sh

./gradlew build

docker-compose -f docker-compose-postgres.yml down -v
