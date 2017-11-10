#! /bin/bash

set -e

. ./set-env-custom-db.sh

docker-compose -f docker-compose-custom-db.yml down -v

docker-compose -f docker-compose-custom-db.yml up --build -d

./wait-for-mysql.sh

./gradlew build

docker-compose -f docker-compose-custom-db.yml down -v
