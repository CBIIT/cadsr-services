docker-compose configuration for cchecker
=====

## Requirements ##
* Docker installed and running

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
3. run startall
4. give container a minute or two to completely start angular ui and apache
5. visit http://localhost:8081 in a browser

## To start docker container once installed ##
* This only needs to be done if the container is already installed and has previously been stopped

1. docker start -a cchecker-ui

## To stop docker container once installed ##
1. docker stop cchecker-ui
