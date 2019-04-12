#! /bin/bash

set -e

export DATABASE=mssql
export MODE=polling

./_build-and-test-all.sh
