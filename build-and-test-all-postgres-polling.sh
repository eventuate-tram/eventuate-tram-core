#! /bin/bash

set -e

export DATABASE=postgres
export MODE=polling

./_build-and-test-all.sh