#! /bin/bash

set -e

. ./set-env-postgres-wal.sh

docker-compose -f docker-compose-postgres-wal.yml down -v

docker-compose -f docker-compose-postgres-wal.yml up --build -d

./wait-for-postgres.sh

./gradlew build

docker-compose -f docker-compose-postgres-wal.yml down -v
