#! /bin/bash

set -e

. ./set-env-mysql-binlog.sh

docker-compose -f docker-compose-mariadb-binlog.yml down -v

docker-compose -f docker-compose-mariadb-binlog.yml up --build -d

./wait-for-mysql.sh

./gradlew $* build

docker-compose -f docker-compose-mariadb-binlog.yml down -v
