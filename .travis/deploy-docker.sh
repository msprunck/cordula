#!/bin/sh

TAG=${TRAVIS_TAG:-latest}
COMMIT=${TRAVIS_COMMIT::8}
REPO=msprunck/cordula

docker login -u "$DOCKER_USER" -p "$DOCKER_PASS"
docker build -f docker/cordula/Dockerfile -t $REPO:$COMMIT .
docker tag $REPO:$COMMIT $REPO:$TAG
docker push $REPO
