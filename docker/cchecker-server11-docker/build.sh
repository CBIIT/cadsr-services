#/usr/bin !sh
docker build -t cchecker-gateway .
docker kill cchecker-gateway
docker rm cchecker-gateway

if [[ $db_driver && $db_url && $db_user && $db_credential ]]
then
	docker build -t cchecker-gateway .
	docker kill cchecker-gateway
	docker rm cchecker-gateway
	docker run -e "db_driver=$db_driver" -e "db_user=$db_user" -e "db_credential=$db_credential" -e "db_url=$db_url" --name cadsr-services -p 8080:8080  cadsr-services
else
	echo 'environment variables must be set. Either set them or run the docker run command manually. docker run -e "jdbc_driver=some_driver" -e "db_user=some_user" -e "db_credential=some_password" -e "db_url=some_db_url" --name cadsr-services -p 8080:8080  cadsr-services'
fi
