#! /bin/bash

set -e

export DATABASE=mssql
export MODE=polling
export BROKER=activemq
export COORDINATOR=zookeeper

./_build-and-test-all.sh
