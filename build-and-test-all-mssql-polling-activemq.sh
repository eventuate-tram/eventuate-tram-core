#! /bin/bash

set -e

export DATABASE=mssql
export MODE=polling
export BROKER=activemq
export COORDINATOR=zookeeper
export MICRONAUT_ENVIRONMENTS=mssql
export SPRING_PROFILES_ACTIVE=EventuatePolling,ActiveMQ,mssql

./_build-and-test-all.sh
