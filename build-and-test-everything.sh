#! /bin/bash -e

set -o pipefail

SCRIPTS=" ./build-and-test-all.sh ./build-and-test-all-cdc.sh ./build-and-test-all-cdc-postgres.sh ./build-and-test-all-postgres.sh "


date > build-and-test-everything.log

for script in $SCRIPTS ; do
   echo '****************************************** Running' $script
   date >> build-and-test-everything.log
   echo '****************************************** Running' $script >> build-and-test-everything.log
   $script | tee -a build-and-test-everything.log
done

echo 'Finished successfully!!!'
