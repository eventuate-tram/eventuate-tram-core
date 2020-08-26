#! /bin/bash

set -e

export DATABASE=postgres
export MODE=wal
export BROKER=rabbitmq
export COORDINATOR=zookeeper
export MICRONAUT_ENVIRONMENTS=postgres
export SPRING_PROFILES_ACTIVE=PostgresWal,RabbitMQ,postgres

./_build-and-test-all.sh
