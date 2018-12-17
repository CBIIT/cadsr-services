docker configuration for cchecker-gateway
=====

## Requirements ##
* Docker installed and running

## Installation ##
1. git clone https://github.com/CBIIT/cadsr-services
2. cd cadsr-services/docker/cchecker-server-docker
3. run ./build.sh
4. When tomcat starts and window output stops tomcat is started

## To start docker container once installed ##
* This only needs to be done if the container is already installed and has previously been stopped

1. docker start -a cchecker-gateway

## To stop docker container once installed ##
1. docker stop cchecker-gateway
