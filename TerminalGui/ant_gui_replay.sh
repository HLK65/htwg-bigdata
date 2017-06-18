#!/bin/bash
curl -X GET http://127.0.0.1:9100/startreplay
while true; do 
	clear
	curl -X GET http://127.0.0.1:9100/replay/$1
	sleep 1
	echo "----------------------------------------------------------"
done
