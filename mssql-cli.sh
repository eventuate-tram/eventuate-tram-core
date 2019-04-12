#! /bin/bash -e

docker run $* \
   --name mssqlterm --rm \
   -e MSSQL_HOST=$DOCKER_HOST_IP \
   mcr.microsoft.com/mssql/server:2017-latest  \
   sh -c 'exec /opt/mssql-tools/bin/sqlcmd -S "$MSSQL_HOST" -U SA -P "Eventuate123!"'
