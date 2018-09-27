#! /bin/bash

set -e

./gradlew $* testClasses

. ./set-env-mysql-binlog.sh

docker-compose -f docker-compose-mysql-binlog.yml down -v

docker-compose -f docker-compose-mysql-binlog.yml up --build -d

./wait-for-mysql.sh

./gradlew $* build

docker-compose -f docker-compose-mysql-binlog.yml down -v
