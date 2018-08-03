

#! /bin/bash

set -e

export PROFILE=RabbitMQ
export BROKER=rabbitmq

./_build-and-test-all-message-broker.sh
