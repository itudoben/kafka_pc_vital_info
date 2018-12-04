# Abstract
This setup a kafka pipeline to process vital information from PC and monitor them.

# Requirements
This project uses Ruby [iStats](https://rubygems.org/gems/iStats/versions/1.2.0) to read vital information from CPU, drives, fan, etc.
After installing the Gem, here is how to get the CPU temperature in Celsius.
```bash
$ istats cpu temp | cut -c 25-29
48.75
```

The fans speed:
```bash
$ istats fan speed | cut -c 25-28
2158
1995
```
There are other metrics available and we will integrate them as we go.

# Architecture
Vital information will be streamed through a Kafka stream processor that will store them in Hive.
All the install will work in Docker files and VMs on VirtualBox.

# Docker
From within a docker container one can access the outside services via the Domain Name host.docker.internal.
Suppose a web server is running on the host on port 8080, from within the container one can send a request like this:

```bash
curl http://host.docker.internal:8080
```
