#! /bin/bash

(
echo "open localhost 5554"
sleep 1
echo "geo fix 0.0 0.0"
sleep 1
) | telnet

#echo "geo fix 2.2 48.8"