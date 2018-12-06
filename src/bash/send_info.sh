#!/bin/bash

###
# This program loops to simulate streaming of monitoring information to Kafka.
# $1 is a comma separated list of brokers IP:port and $2 is the topic name.

function usage() {
    echo "usage: ${0} <brokers IP:port comma separated list> <topic name>"
}

if [ -z "$1" -o -z "$2" ]; then
    usage
    exit 1
fi

for i in {1..19}; do
    vital_info=$(date -u "+%Y.%m.%d.%H.%M.%S")" - "$(ifconfig | grep "inet addr" | head -1 | cut -c 21-31)" - # $i"" - "$(cat /proc/meminfo | grep MemFree | cut -c 18-27)
    echo $vital_info | kafka-console-producer.sh --broker-list $1 --topic $2; sleep 1
done
