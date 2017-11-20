#! /bin/bash

done=false

host=$1
shift
ports=$*

while [[ "$done" = false ]]; do
	for port in $ports; do
		curl -q http://${host}:${port}/health >& /dev/null
		if [[ "$?" -eq "0" ]]; then
			done=true
		else
			done=false
			break
		fi
	done
	if [[ "$done" = true ]]; then
		echo connected
		break;
  fi
	echo -n .
	sleep 1
done
