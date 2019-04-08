#! /bin/bash -e

docker run -it --name rediscli --rm -e DOCKER_HOST_IP=${DOCKER_HOST_IP?} redis:5.0.3 bash -c 'redis-cli -h $DOCKER_HOST_IP'
