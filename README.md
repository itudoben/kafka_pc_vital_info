# Forewords 
This project is at this moment a WORK IN PROGRESS v0.0.1. As the project goes, progress is shared via Git commits.

I used technology documentations, blogs, articles, stashoverflow, google, etc. 
I will make sure to give proper credits to the authors in the [references](#references) section.
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

# Set up Kafka
## ZooKeeper
```bash
cd src/docker
docker-compose up -d zookeeper

or

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
-e KAFKA_BROKER_ID=1 \
-e KAFKA_ADVERTISED_HOST_NAME=$(docker inspect zookeeper -f '{{.NetworkSettings.Gateway}}') \
-e KAFKA_ADVERTISED_PORT=9092 \
-e KAFKA_PORT=9092 \
-e ZOOKEEPER_IP=$(docker inspect zookeeper -f '{{.NetworkSettings.IPAddress}}') \
ches/kafka
```

# Create a topic
```bash
docker run \
--rm ches/kafka kafka-topics.sh \
--zookeeper $(docker inspect zookeeper -f '{{.NetworkSettings.IPAddress}}'):2181 \
--create \
--topic titi \
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
/app/bin/send_info.sh $(docker inspect kafka2 -f '{{.NetworkSettings.IPAddress}}') 29092 titi
```

# Consumer on topic toto
```bash
docker run --rm \
ches/kafka kafka-console-consumer.sh \
--bootstrap-server $(docker inspect kafka -f '{{.NetworkSettings.IPAddress}}'):9092 \
--from-beginning \
--topic titi
```

# Get the Number of partition for the topic
```bash
docker run --rm \
    ches/kafka \
    kafka-topics.sh --describe --zookeeper $(docker inspect zookeeper -f '{{.NetworkSettings.IPAddress}}'):2181 \
    --topic titi | awk '{print $2}' | uniq -c |awk 'NR==2{print "count of partitions=" $1}'
```

# Maven
First let's create our Java producer that will stream information to a Kafka topic.

```bash
mvn archetype:generate -DgroupId=com.jh.kafka.vitalinfo \
    -DartifactId=vitalinfo \
    -DarchetypeArtifactId=maven-archetype-quickstart \
    -DinteractiveMode=false
```

# ZooKeeper CLI

Execute the ZK CLI from the running ZK container:
```bash
docker exec -ti $(docker inspect zookeeper -f "{{.Id}}") /opt/zookeeper/bin/zkCli.sh
```

Once in the ZK shell help lists all commands.
```
ls /brokers/ids # list all brokers
get /broker/ids/<ID> # get details about broker ID
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
Unseal Key (will be hidden): 
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
  