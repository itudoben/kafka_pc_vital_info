#!/bin/bash

###
# This program loops to simulate streaming of monitoring information to Kafka.
# $1 is the broker IP and $2 is the topic name.

for x in {1..10}; do
    vital_info=$(cat /proc/meminfo | grep MemFree | cut -c 18-27)"; "$(date)
    echo $vital_info | kafka-console-producer.sh --broker-list $1:9092 --topic $2; sleep 1
done
