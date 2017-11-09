#! /bin/bash -e

docker run $* \
   --name postgresterm --link $(echo ${PWD##*/} | sed -e 's/-//g')_postgres_1:postgres --rm postgres:9.6.5 \
   sh -c 'export PGPASSWORD="$POSTGRES_ENV_POSTGRES_PASSWORD"; exec psql -h postgres -U "$POSTGRES_ENV_POSTGRES_USER" '
