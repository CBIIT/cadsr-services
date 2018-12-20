docker-compose configuration for cchecker
=====

## Requirements ##
* Docker installed and running
* Setup environment variables (cdetier, REST_API, configuration). Default (dev, http://127.0.0.1:8080, dev)
* Environment variable expected on the host box: $LANG with value "en_US.UTF-8"

## NOTE ##
1. If install fails to connect to the internet add a file daemon.json file in /etc/docker
2. Add the following entry into the file 
{
    "dns": ["x.x.x.x", "x.x.x.x"] 
}
3. replace x.x.x.x with the two nih naneservers. can be found be running cat /etc/resolve.conf on your main computer.
4. Save file
5. restart docker - on ubuntu run service docker restart

## Installation ##
1. git clone https://github.com/CBIIT/cadsr-services
2. cd cadsr-services/docker
3. run startall (optional tier docker-compose -f docker-compose-dev.yml, -f docker-compose-prod.yml)
4. give container a minute or two to completely start angular ui and apache
5. visit http://localhost:8081 in a browser

## To start docker container once installed ##
* This only needs to be done if the container is already installed and has previously been stopped

1. docker start -a cchecker-ui

## To stop docker container once installed ##
1. docker stop cchecker-ui

## To remove all images and containers in case of need to completely rebuild run the following ##
1. docker kill $(docker ps -q)
2. docker rm $(docker ps -a -q)
3. docker rmi $(docker images -q)

## To create a server container with OpenJDK 11, remove all containers and images and run with docker-compose11.yml.
docker-compose -f docker-compose11.yml up
