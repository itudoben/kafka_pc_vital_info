#!/bin/bash

###
# This program loops to simulate streaming of monitoring information to Kafka.
# $1 is the broker IP, $2 the port of the Kafka broker and $3 is the topic name.

for x in {1..40}; do
    vital_info=$(ifconfig | grep "inet addr" | head -1 | cut -c 21-31)"; "$(cat /proc/meminfo | grep MemFree | cut -c 18-27)"; "$(date)
    echo $vital_info | kafka-console-producer.sh --broker-list $1:$2 --topic $3; sleep 1
done
