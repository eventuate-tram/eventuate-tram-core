#! /bin/bash -e

set -o pipefail

SCRIPTS="
./build-and-test-all-mysql-binlog.sh
./build-and-test-all-postgres-polling.sh
./build-and-test-all-mssql-polling.sh
./build-and-test-all-postgres-wal.sh
"


date > build-and-test-everything.log

for script in $SCRIPTS ; do
   echo '****************************************** Running' $script
   date >> build-and-test-everything.log
   echo '****************************************** Running' $script >> build-and-test-everything.log
   $script | tee -a build-and-test-everything.log
done

echo 'Finished successfully!!!'
