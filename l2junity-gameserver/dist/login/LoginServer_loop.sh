#!/bin/bash

err=1
until [ $err == 0 ]; 
do
	java -Xms128m -Xmx256m -jar l2jlogin.jar > /dev/null 2>&1
	err=$?
	sleep 10;
done
