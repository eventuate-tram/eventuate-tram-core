#! /bin/bash

set -e

export DATABASE=mysql
export MODE=binlog
export BROKER=kafka
export COORDINATOR=zookeeper

./_build-and-test-all.sh
