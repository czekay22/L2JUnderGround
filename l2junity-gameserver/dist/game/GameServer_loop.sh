#!/bin/bash

# exit codes of GameServer:
#  0 normal shutdown
#  2 reboot attempt

while :; do
	java -Xms1024m -Xmx1536m -jar l2jserver.jar > /dev/null 2>&1
	[ $? -ne 2 ] && break
	sleep 10
done
