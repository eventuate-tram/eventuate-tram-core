#! /bin/bash

set -e

export PROFILE=Redis
export BROKER=redis
export REDIS_SERVERS=${DOCKER_HOST_IP}:6379,${DOCKER_HOST_IP}:6380,${DOCKER_HOST_IP}:6381

./_build-and-test-all-message-broker.sh
