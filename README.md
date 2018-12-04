# Forewords 
This project is at this moment a WORK IN PROGRESS v0.0.1. As the project goes, progress is shared via Git commits.

I used technology documentations, blogs, articles, stashoverflow, google, etc. 
I will make sure to give proper credits to the authors.
If I unintentionally missed anyone or made a mistake, please let me know through the 
[Github issues](https://github.com/itudoben/kafka_pc_vital_info/issues) and I will correct it asap. 

# Introduction
The goal of this project is to provide:
 - an installation only on macOS for the time being.
 - a working template for setting up ZooKeeper and Kafka with multiples nodes using Docker and Kubernetes.
 - consumer to process incoming data and store the results in Hive. 
 - one command/script to set this all up.

# TODO
 - processing of selected Kafka Streams with a Machine Learning model for classification or prediction.
 - available for *nix platforms.
 
# Requirements
- A Personal Computer running macOS version 10.13.6.
- Docker Desktop for macOS version 2.0.0.0-mac81 (29211). 

For getting vital information i.e. cpu, disk, etc, we used Ruby [iStats](https://rubygems.org/gems/iStats/versions/1.2.0). 
After installing the Gem, here is how to get the CPU temperature in Celsius.

```bash
$ istats cpu temp | cut -c 25-29
48.75
```

The fans speed in RPM:
```bash
$ istats fan speed | cut -c 25-28
2158
1995
```

The memory used from the /proc FS:
```bash
$ cat /proc/meminfo | grep MemFree | cut -c 18-27
2411212 kB
```

There are other metrics available and we will integrate them as we go.

# Architecture
Vital information of a simple docker container will stream through Kafka and will be stored in Hive.

# Docker
From within a docker container one can access the outside services via the Domain Name host.docker.internal 
as per the documentation.
For instance, let's suppose a web server is running on the host on port 8080, from within the container 
one can send a request like this:
```bash
curl http://host.docker.internal:8080
```

Kafka and ZooKeeper containers can be set up with this container 
[kafka/zookeeper docker container](https://github.com/christiangda/docker-kafka) to run zk and k together.

The following command lines from the 
[article on setting up Kafka and ZooKeeper on Docker](https://link.medium.com/wKgcaLFgnS) are used with slight modifications.
The IPs have been modified to get those set up by Docker machine. 

## ZooKeeper
```bash
docker run -d \
--name zookeeper \
-p 2181:2181 \
jplock/zookeeper
```

# Kafka docker
```bash
docker run -d \
--name kafka \
-p 7203:7203 \
-p 9092:9092 \
-e KAFKA_ADVERTISED_HOST_NAME=$(docker inspect zookeeper -f '{{.NetworkSettings.Gateway}}') \
-e ZOOKEEPER_IP=$(docker inspect zookeeper -f '{{.NetworkSettings.IPAddress}}') \
ches/kafka
```

```bash
docker run -d \
--name kafka2 \
-p 7203:7203 \
-p 9092:9092 \
-e KAFKA_ADVERTISED_HOST_NAME=$(docker inspect zookeeper -f '{{.NetworkSettings.Gateway}}') \
-e ZOOKEEPER_IP=$(docker inspect zookeeper -f '{{.NetworkSettings.IPAddress}}') \
ches/kafka
```

# Create a topic
```bash
docker run \
--rm ches/kafka kafka-topics.sh \
--zookeeper $(docker inspect zookeeper -f '{{.NetworkSettings.IPAddress}}'):2181 \
--create \
--topic toto \
--replication-factor 1 \
--partitions 1
```

# List the topics
```bash
docker run \
--rm ches/kafka kafka-topics.sh \
--zookeeper $(docker inspect zookeeper -f '{{.NetworkSettings.IPAddress}}'):2181 \
--list
```

# Producer on topic toto
```bash
docker run --rm --interactive \
ches/kafka kafka-console-producer.sh \
--broker-list $(docker inspect kafka -f '{{.NetworkSettings.IPAddress}}'):9092 \
--topic toto
```

## Send to topic 
```bash
docker run --rm \
-v $(pwd)/src/bash:/app/bin \
ches/kafka \
/app/bin/send_info.sh $(docker inspect kafka -f '{{.NetworkSettings.IPAddress}}') titi
```

# Consumer on topic toto
```bash
docker run --rm \
ches/kafka kafka-console-consumer.sh \
--bootstrap-server $(docker inspect kafka -f '{{.NetworkSettings.IPAddress}}'):9092 \
--from-beginning \
--topic titi
```

# Maven
First let's create our Java producer that will stream vital information from the docker container to a Kafka topic.

```bash
mvn archetype:generate -DgroupId=com.jh.kafka.vitalinfo \
    -DartifactId=vitalinfo \
    -DarchetypeArtifactId=maven-archetype-quickstart \
    -DinteractiveMode=false
```

# References
- https://github.com/christiangda/docker-kafka
- https://medium.com/@itseranga/kafka-and-zookeeper-with-docker-65cff2c2c34f
- http://kafka.apache.org/documentation.html#quickstart
- https://rubygems.org/gems/iStats/versions/1.2.0
- 
  