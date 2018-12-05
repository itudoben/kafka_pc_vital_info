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

docker run --rm \
    -v $(pwd)/.:/app/bin ches/kafka \
    /app/bin/send_info.sh $(docker inspect kafka$1 -f '{{.NetworkSettings.IPAddress}}') $2 $3
