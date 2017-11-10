#! /bin/bash

set -e

. ./set-env-postgres-custom-db.sh

docker-compose -f docker-compose-postgres-custom-db.yml down -v

docker-compose -f docker-compose-postgres-custom-db.yml up --build -d

./wait-for-postgres.sh

./gradlew build

docker-compose -f docker-compose-postgres-custom-db.yml down -v
