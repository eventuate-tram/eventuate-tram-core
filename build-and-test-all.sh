#! /bin/bash

set -e

. ./set-env.sh

docker-compose down -v

docker-compose up -d --build

./wait-for-mysql.sh

./gradlew build

docker-compose down -v
