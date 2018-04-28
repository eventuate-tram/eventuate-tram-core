#! /bin/bash

set -e

export DATABASE=mysql
export MODE=binlog

./_build-and-test-all.sh
