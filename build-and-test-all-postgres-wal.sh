#! /bin/bash

set -e

export DATABASE=postgres
export MODE=wal

./_build-and-test-all.sh
