#!/usr/bin/env bash

function usage() {
    echo "usage: ${0} <project directory>"
}

if [ -z "$1" ]; then
    usage
    exit 1
fi

pushd $1 2>&1 > /dev/null
echo "Changed directory to $1"
echo

# Compile the app
mvn -f ./vitalinfo/pom.xml \
    -DMAVEN_OPTS="-Xmx1g -XX:MaxPermSize=512m" \
    -Dmaven.test.skip=true \
    -DoutputDirectory=target/libs \
    clean dependency:copy-dependencies package

# Copy the Maven settings.xml file for the image.
cp /Users/hujol/.m2/settings-local.xml ./settings-local.xml

# Build the docker image
docker build -f ./src/docker/Dockerfile_vi_app -t localhost:8083/jhujol/viapp:0.0.1 .

# Remove the Maven settings.xml.
rm ./settings-local.xml

# Publish the image in the local Docker hub
docker push localhost:8083/jhujol/viapp:0.0.1

popd 2>&1 > /dev/null
echo "Back to $(pwd)"
