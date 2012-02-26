#!/bin/sh

# This script deploy the latest built version of the NetExecutor to the seven SICS cloud machines.

# parallel-ssh -h hosts.txt .....

SERVER_IP=$1
SERVER_PORT=$2

if [ $# -ne 2 ]; then
        echo "USAGE: command SERVER_IP SERVER_PORT"
        exit 2
fi

for i in 1 2 3 4 5 6 7 
do
	echo -n "Starting daemon on cloud$i............."
	ssh sics_gradient@cloud$i.sics.se "cd hieu; java -jar NetExecutor.jar $1 $2 &> log.txt &"
	if [ $? -eq 0 ]; then
		echo 'DONE'
	else
		echo 'ERROR'
	fi
done                                             

exit 0                                                                 
