#!/bin/sh

# parallel-scp -h hosts.txt local remote


# This script deploy the latest built version of the NetExecutor to the seven SICS cloud machines.

JAR_SOURCE=$1
JAR_DEST=$2

echo test \&>/dev/null

if [ $# -ne 2 ]; then 
	echo "USAGE: command JAR_SRC JAR_DEST"
	exit 2
fi

for i in 1 2 3 4 5 6 7 
do
	echo -n "Deploying to cloud$i............."
	scp -rv "$JAR_SOURCE" "sics_gradient@cloud$i.sics.se:~/hieu/$JAR_DEST" 1>/dev/null 2>&1
	if [ $? -eq 0 ]; then
		echo 'DONE'
	else
		echo 'ERROR'
	fi
done                                                                                                              
