#! /bin/bash

set -e

export PROFILE=Redis
export BROKER=redis

./_build-and-test-all-message-broker.sh
