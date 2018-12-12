# Forewords 
This project is a work in progress shared via Github.

I used technology documentations, blogs, articles, stashoverflow, google, etc. 
I will make sure to give proper credits to the authors in the [references](#references) section.

If I unintentionally missed anyone or made a mistake, please let me know through the 
[Github issues](https://github.com/itudoben/kafka_pc_vital_info/issues) and I will correct it asap. 

# Introduction
The goal of this project is to provide a simple framework for building a distributed streaming processing solution based
on Docker, Kubernetes, Hive and Kafka.

The requirements are: 
 - a template for setting up ZooKeeper and Kafka with multiples nodes using Docker and Kubernetes.
 - consumer to process incoming data and store the results in Hive. 
 - one command/script to set this all up for macOS.

# Future Improvements 
 - processing of selected Kafka Streams with a Machine Learning model for classification or prediction.
 - available for *nix platforms.
 
# Requirements
- A Personal Computer running macOS version 10.13.6.
- Docker Desktop for macOS version 2.0.0.0-mac81 (29211).
- Minikube and kubernetes installed.
- Maven 3.6 and Java 8.
- an IDE like IntelliJ IDEA, etc. 

# Technologies for Vital Info Collection
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
Vital information from a simple docker containerised app will stream through Kafka and will be stored in Hive. 

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

# Custom Docker Hub
```bash
docker login localhost:8082
# Provide local user/paswd Nexus credential of the Nexus repos. 
```

# Set up Kafka
## ZooKeeper
```bash
docker-compose -f src/docker/docker-compose.yml up -d zookeeper
```

or

```bash
docker run -d \
    --name zookeeper \
    -p 2181:2181 \
    jplock/zookeeper
```

Using the local Docker Hub
```bash
docker run -d \
    --name localhost:8082/zookeeper \
    -p 2181:2181 \
    jplock/zookeeper
```

# Kafka Broker
To start a broker use the ./bin/bash/start_kafka_broker.sh command. Running it with no parameters show the usage.
For instance:
```bash
for i in {2..4}; do 
    ./src/bash/start_kafka_broker.sh ${i} ${i}9092
done
```

# Create a topic
```bash
docker run \
    --rm localhost:8082/ches/kafka kafka-topics.sh \
    --zookeeper $(docker inspect zookeeper -f '{{.NetworkSettings.IPAddress}}'):2181 \
    --create \
    --topic memory \
    --replication-factor 3 \
    --partitions 2
```

# Alter a topic
```bash
docker run \
    -v /Users/hujol/Projects/kafka_streaming/kafka_pc_vital_info/src/bash/modification_topic_replica.json:/kafka/modification_topic_replica.json \
    --rm localhost:8082/ches/kafka \
    kafka-reassign-partitions.sh \
    --zookeeper $(docker inspect zookeeper -f '{{.NetworkSettings.IPAddress}}'):2181 \
    --reassignment-json-file /kafka/modification_topic_replica.json \
    --execute
```

# List the topics
```bash
docker run \
    --rm localhost:8082/ches/kafka \
    kafka-topics.sh \
    --zookeeper $(docker inspect zookeeper -f '{{.NetworkSettings.IPAddress}}'):2181 \
    --list
```

# Describe a topic (partitions, replicas, etc)
```bash
docker run \
    --rm localhost:8082/ches/kafka kafka-topics.sh \
    --zookeeper $(docker inspect zookeeper -f '{{.NetworkSettings.IPAddress}}'):2181 \
    --describe \
    --topic memory
```

# Producer on topic toto

Get the broker ID defined when first creating the Kafka broker with the port and use them here with the 
topic.

```bash
docker run --rm --interactive \
    localhost:8082/ches/kafka kafka-console-producer.sh \
    --broker-list $(docker inspect kafka -f '{{.NetworkSettings.IPAddress}}'):9092 \
    --topic toto
```

## Send to topic 
```bash
cd src/bash
./produce_into_topic.sh 1 9092 memory
```

# Consumer on topic toto
```bash
docker run --rm \
    localhost:8082/ches/kafka kafka-console-consumer.sh \
    --bootstrap-server $(docker inspect kafka -f '{{.NetworkSettings.IPAddress}}'):9092 \
    --from-beginning \
    --topic memory
```

# Get the Number of partition for the topic
```bash
docker run --rm \
    localhost:8082/ches/kafka \
    kafka-topics.sh --describe --zookeeper $(docker inspect zookeeper -f '{{.NetworkSettings.IPAddress}}'):2181 \
    --topic memory | awk '{print $2}' | uniq -c |awk 'NR==2{print "count of partitions=" $1}'
```

# Add partitions to existing topic
```bash
docker run --rm \
    localhost:8082/ches/kafka \
    kafka-topics.sh --alter --zookeeper $(docker inspect zookeeper -f '{{.NetworkSettings.IPAddress}}'):2181 \
    --topic memory --partitions 3
```

# Vital Info Collector App
## Maven
First let's create our Java producer that will stream information to a Kafka topic.

```bash
mvn archetype:generate -DgroupId=com.jh.kafka.vitalinfo \
    -DartifactId=vitalinfo \
    -DarchetypeArtifactId=maven-archetype-quickstart \
    -DinteractiveMode=false
```

java -cp ".:$(pwd)/vitalinfo/target/libs/:$(pwd)/vitalinfo/target/vitalinfo-0.0.1.jar" com.jh.kafka.vitalinfo.App

## run the app in docker
```bash
docker run -ti \
    -v /Users/hujol/.m2/repository:/root/.m2/repository \
    -v /Users/hujol/.m2/settings-local.xml:/root/.m2/settings.xml \
    -v /Users/hujol/Projects/kafka_streaming/kafka_pc_vital_info/vitalinfo:/app/kafkap \
    localhost:8082/maven:3.6-alpine \
    mvn -f /app/kafkap/pom.xml -DMAVEN_OPTS="-Xmx792m -XX:MaxPermSize=396m" -Dmaven.test.skip=true exec:exec@kafkap
```

## Dockerfile and Deployment
The first step is to remove any existing Docker image of the collector app.
Then call the `deploy_vi_app.sh` script that will build the Docker image and publish it on the local Docker Hub hosted
in Nexus. 
```bash
docker image rm -f localhost:8082/jhujol/viapp:0.0.1 && \
    ./src/bash/deploy_vi_app.sh /Users/hujol/Projects/kafka_streaming/kafka_pc_vital_info
```

To test the new image and look what's inside one can run the following command, note that the `--entrypoint` overrides
the default entrypoint to enable instead an interactive `bash` shell.
```bash
docker run --rm -ti --entrypoint /bin/bash localhost:8082/jhujol/viapp:0.0.1
```

To run the Vital Info Collector App, the following `bash` command is executed:
```bash
docker run --rm localhost:8082/jhujol/viapp:0.0.1
```

The first thing that will happen is that Maven will download all the needed dependencies from our local Nexus hub.
Then it will run the executable and here is the output of the results.

```bash
Downloaded from mac_os_group: http://172.17.0.1:8081/repository/mac_os_group/org/codehaus/plexus/plexus-utils/3.0.20/plexus-utils-3.0.20.jar (243 kB at 156 kB/s)
ARG: --somestuff=${c}
${c}
SLF4J: Failed to load class "org.slf4j.impl.StaticLoggerBinder".
SLF4J: Defaulting to no-operation (NOP) logger implementation
SLF4J: See http://www.slf4j.org/codes.html#StaticLoggerBinder for further details.
Sending record 0
Record sent with key 0 to partition 0 with offset 0
....
```

One can run a Kafka consumer using one of the broker like this:
```bash
./src/bash/consumer_listen_topic.sh 3 39092 memory
```

The output on the consumer looks like this:
```bash
2018-12-12T09:48:02.206 - 172.17.0.8 -  #0 - 92,672 KB
2018-12-12T09:48:04.870 - 172.17.0.8 -  #1 - 92,672 KB
2018-12-12T09:48:06.878 - 172.17.0.8 -  #2 - 92,672 KB
...
```

## Kubernetes and MiniKube
```bash
minikube start --cpus 2 --memory 4096
```

# ZooKeeper CLI

Execute the ZK CLI from the running ZK container:
```bash
docker exec -ti $(docker inspect zookeeper -f "{{.Id}}") /opt/zookeeper/bin/zkCli.sh
```

Once in the ZK shell help lists all commands.
```
ls /brokers/ids # list all brokers
get /brokers/ids/<ID> # get details about broker ID
```
   
# Vault

Start up Vault server with Consul:
```bash
$ cd ./src/docker
$ docker-compose -f docker-compose_vault.yml build && docker-compose -f docker-compose_vault.yml up -d 
```

## Check Vault Status

One can run a shell in the running container and initialize HashiCorp Vault:
```bash
$ docker exec -it vault.server /bin/sh
/ # export VAULT_ADDR=http://127.0.0.1:8200
or using the Gateway also:
/ # export VAULT_ADDR=http://172.27.0.1:8200           

/ # vault operator init
Unseal Key 1: 8iIWsSsW+G0AqCLBgcZFjdDOtMim/rU0+L3NgQfvdaDF
Unseal Key 2: lBB2s2mJ/k61qqQYRa0HacEqFLeERBfRizrFdH4Zvx6V
Unseal Key 3: 2k5mjT+fHHR5VKDyPVB3kP8wcql5Dly6FMYXe8fYVHzL
Unseal Key 4: 1Z/5Eh+bDRAW8JX5Q54uL4lNGXDAqhmAIv02lW+7OCt/
Unseal Key 5: SjpmJRD1iwH+pHAb41TxaQheDH4LZ1MYeK2g3SCd/BxO

Initial Root Token: s.3IK4bjKJ6HVq4cL5wa5yWA5z

Vault initialized with 5 key shares and a key threshold of 3. Please securely
distribute the key shares printed above. When the Vault is re-sealed,
restarted, or stopped, you must supply at least 3 of these keys to unseal it
before it can start servicing requests.

Vault does not store the generated master key. Without at least 3 key to
reconstruct the master key, Vault will remain permanently sealed!

It is possible to generate new unseal keys, provided you have a quorum of
existing unseal keys shares. See "vault operator rekey" for more information.
/ #  
```

From within the bash.test docker container one can test the vault:
```bash
$ docker exec -it bash.test /bin/sh
# export VAULT_ADDR=http://172.27.0.1:9200
# vault status
Key                Value
---                -----
Seal Type          shamir
Initialized        true
Sealed             true
Total Shares       5
Threshold          3
Unseal Progress    0/3
Unseal Nonce       n/a
Version            1.0.0
HA Enabled         true
```

Then unseal the vault using 3 of the unseal keys:
```bash
/ # vault operator unseal
Unseal Key \(will be hidden\): 
Key                Value
---                -----
Seal Type          shamir
Initialized        true
Sealed             true
Total Shares       5
Threshold          3
Unseal Progress    2/3
Unseal Nonce       271fe22c-f0c4-5702-639c-240f25721af1
Version            1.0.0
HA Enabled         true
/ # vault operator unseal
```

Write a secret, read, seal the vault and try to read the secret:
```bash
/ # vault write secret/toto email=toto@gmail.com value=pouet password=mypassword 
Success! Data written to: secret/toto
/ # vault read secret/toto 
Key                 Value
---                 -----
refresh_interval    768h
email               toto@gmail.com
password            mypassword
value               pouet
/ # vault seal
WARNING! The "vault seal" command is deprecated. Please use "vault operator
seal" instead. This command will be removed in Vault 1.1.

Success! Vault is sealed.
/ # vault read secret/toto 
Error reading secret/toto: Error making API request.

URL: GET http://127.0.0.1:8200/v1/secret/toto
Code: 503. Errors:

* Vault is sealed
/ # export VAULT_TOKEN=s.3IK4bjKJ6HVq4cL5wa5yWA5z
or 
/ # vault login s.3IK4bjKJ6HVq4cL5wa5yWA5z
Success! You are now authenticated. The token information displayed below
is already stored in the token helper. You do NOT need to run "vault login"
again. Future Vault requests will automatically use this token.

Key                  Value
---                  -----
token                s.3IK4bjKJ6HVq4cL5wa5yWA5z
token_accessor       8rKX0xs20aVIDaoaKAuCV34x
token_duration       ∞
token_renewable      false
token_policies       ["root"]
identity_policies    []
policies             ["root"]
# vault secrets list

/ # 
```

# Vault with GPG

One can initialize Vault - vault init - using GPG public keys

https://www.vaultproject.io/docs/concepts/pgp-gpg-keybase.html
```bash
gpg --gen-key

XXX is the email address specified when the key is generated.
gpg --list-keys shows all the keys.

gpg --export XXX | base64 > unseal_key1.asc
```

# References
- https://github.com/christiangda/docker-kafka
- https://medium.com/@itseranga/kafka-and-zookeeper-with-docker-65cff2c2c34f
- http://kafka.apache.org/documentation.html#quickstart
- https://rubygems.org/gems/iStats/versions/1.2.0
  