#! /bin/bash

set -e

export DATABASE=mysql
export MODE=binlog
export BROKER=redis

./_build-and-test-all.sh
