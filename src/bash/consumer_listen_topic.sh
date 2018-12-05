#!/bin/bash

###
# Must provide the broker ID as $1, the broker port as $2 and the topic name as $3

function usage() {
    echo "usage: ${0} <broker ID> <broker port> <topic name>"
}

if [ -z "$1" -o -z "$2" -o -z "$3" ]; then
    usage
    exit 1
fi

docker run --rm ches/kafka \
    kafka-console-consumer.sh --new-consumer \
    --bootstrap-server $(docker inspect kafka$1 -f '{{.NetworkSettings.IPAddress}}'):$2 \
    --topic $3

#    --from-beginning \
