#! /bin/bash

set -e

export PROFILE=ActiveMQ
export BROKER=activemq

./_build-and-test-all-message-broker.sh
