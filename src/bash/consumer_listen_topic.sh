#!/bin/bash

###
# Must provide the broker ID as $1, the broker port as $2 and the topic name as $3

docker run --rm ches/kafka \
    kafka-console-consumer.sh --bootstrap-server $(docker inspect kafka$1 -f '{{.NetworkSettings.IPAddress}}'):$2 \
    --from-beginning --topic $3