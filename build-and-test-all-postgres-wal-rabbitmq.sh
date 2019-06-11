#! /bin/bash

set -e

export DATABASE=postgres
export MODE=wal
export BROKER=rabbitmq
export COORDINATOR=zookeeper

./_build-and-test-all.sh
