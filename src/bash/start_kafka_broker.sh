#!/bin/bash

###
# Must provide the broker ID as $1 and the port as $2.

function usage() {
    echo "usage: ${0} <broker ID> <broker port>"
}

if [ -z "$1" -o -z "$2" ]; then
    usage
    exit 1
fi

docker run -d \
    --name kafka$1 \
    -p $2:$2 \
    -e KAFKA_BROKER_ID=$1 \
    -e KAFKA_ADVERTISED_HOST_NAME=$(docker inspect zookeeper -f '{{.NetworkSettings.Gateway}}') \
    -e KAFKA_ADVERTISED_PORT=$2 \
    -e KAFKA_PORT=$2 \
    -e ZOOKEEPER_IP=$(docker inspect zookeeper -f '{{.NetworkSettings.IPAddress}}') \
    localhost:8082/ches/kafka
