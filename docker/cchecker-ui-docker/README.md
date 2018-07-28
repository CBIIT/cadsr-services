docker configuration for cchecker-ui
=====

## Requirements ##
* Docker installed and running

## Installation ##
1. git clone https://github.com/CBIIT/cadsr-services
2. cd cadsr-services/docker/cchecker-ui-docker
3. run ./build.sh cchecker-ui 80
4. give container a minute or two to completely start angular ui and apache
5. visit http://localhost in a browser

## To start docker container once installed ##
* This only needs to be done if the container is already installed and has previously been stopped

1. docker start -a cchecker-ui

## To stop docker container once installed ##
1. docker stop cchecker-ui
