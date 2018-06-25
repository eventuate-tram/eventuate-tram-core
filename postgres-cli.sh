#! /bin/bash -e

docker run $* \
   --name postgresterm --rm \
   -e POSTGRES_ENV_POSTGRES_USER=eventuate -e POSTGRES_ENV_POSTGRES_PASSWORD=eventuate -e POSTGRES_HOST=$DOCKER_HOST_IP \
   postgres:9.6.5 \
   sh -c 'export PGPASSWORD="$POSTGRES_ENV_POSTGRES_PASSWORD"; exec psql -h $POSTGRES_HOST -U "$POSTGRES_ENV_POSTGRES_USER" '
