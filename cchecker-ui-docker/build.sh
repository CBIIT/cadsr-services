#!/bin/bash
if [ -z "$1" ] || [ -z "$2" ]; then
  errorMessage="Build requires a sitename & port: ./build.sh sitename 8080"
  echo $errorMessage
else
  echo "Building site $1"
  docker kill $1
  docker rm $1
  rm -rf $3
  docker build -t alpineangular .
  docker run --name $1 -p 8080:80 alpineangular
fi
