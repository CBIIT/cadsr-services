#!/bin/bash
TAG=`date +%F`-`git log -1 --pretty=%h`

echo "Building ui image"
docker build --no-cache ./cchecker-ui-docker -t temp_ui
echo "tagging ui image to date/time-hash tag and latest tag"
docker tag temp_ui:latest ncidockerhub.nci.nih.gov/cadsrdocker/cchecker-ui:$TAG
docker tag temp_ui:latest ncidockerhub.nci.nih.gov/cadsrdocker/cchecker-ui:latest

echo "Building server image"
docker build --no-cache ./cchecker-server-docker -t temp_server
echo "tagging server image to date/time-hash tag and latest tag"
docker tag temp_server:latest ncidockerhub.nci.nih.gov/cadsrdocker/cchecker-server:$TAG
docker tag temp_server:latest ncidockerhub.nci.nih.gov/cadsrdocker/cchecker-server:latest

echo "pushing images to repository"
docker push ncidockerhub.nci.nih.gov/cadsrdocker/cchecker-ui:latest
docker push ncidockerhub.nci.nih.gov/cadsrdocker/cchecker-ui:$TAG
docker push ncidockerhub.nci.nih.gov/cadsrdocker/cchecker-server:$TAG
docker push ncidockerhub.nci.nih.gov/cadsrdocker/cchecker-server:latest

echo "removing unused images from system"
docker rmi temp_ui:latest
docker rmi ncidockerhub.nci.nih.gov/cadsrdocker/cchecker-ui:$TAG
docker rmi temp_server:latest
docker rmi ncidockerhub.nci.nih.gov/cadsrdocker/cchecker-server:$TAG
