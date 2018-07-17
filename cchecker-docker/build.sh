#!/bin/sh
docker build -t cadsr-services .
docker kill cadsr-services
docker rm cadsr-services
docker run --name cadsr-services -p 8080:8080  cadsr-services
