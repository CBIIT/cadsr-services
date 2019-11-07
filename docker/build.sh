#!/bin/bash

echo "Building ui image"
docker build --no-cache ./cchecker-ui-docker -t ncidockerhub.nci.nih.gov/cadsrdocker/cchecker-ui:$TAG
docker tag ncidockerhub.nci.nih.gov/cadsrdocker/cchecker-ui:$TAG ncidockerhub.nci.nih.gov/cadsrdocker/cchecker-ui:latest

echo "Building server image"
docker build --no-cache ./cchecker-server-docker -t ncidockerhub.nci.nih.gov/cadsrdocker/cchecker-server:$TAG
docker tag ncidockerhub.nci.nih.gov/cadsrdocker/cchecker-server:$TAG ncidockerhub.nci.nih.gov/cadsrdocker/cchecker-server:latest

echo "pushing images to repository"
docker push ncidockerhub.nci.nih.gov/cadsrdocker/cchecker-ui:latest
docker push ncidockerhub.nci.nih.gov/cadsrdocker/cchecker-ui:$TAG
docker push ncidockerhub.nci.nih.gov/cadsrdocker/cchecker-server:$TAG
docker push ncidockerhub.nci.nih.gov/cadsrdocker/cchecker-server:latest

docker image prune -f
