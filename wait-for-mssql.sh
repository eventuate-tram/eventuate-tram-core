#! /bin/sh

until (echo select 1 from dual | ./mssql-cli.sh -i > /dev/null)
do
 echo sleeping for mssql
 sleep 5
done
