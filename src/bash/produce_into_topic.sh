#!/bin/bash

###
# Must provide the broker ID as $1, the broker port as $2 and the topic name as $3

docker run --rm \
    -v $(pwd)/src/bash:/app/bin ches/kafka \
    /app/bin/send_info.sh $(docker inspect kafka$1 -f '{{.NetworkSettings.IPAddress}}') $2 $3
