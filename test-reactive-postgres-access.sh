#! /bin/bash

set -e

export DATABASE=postgres
export MODE=wal
export BROKER=kafka
export COORDINATOR=zookeeper
export SPRING_PROFILES_ACTIVE=PostgresWal,postgres

./_test-reactive-database-access.sh
