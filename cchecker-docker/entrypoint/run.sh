#~/bin/sh
cd cadsr-services
git pull
cd cchecker-service-parser
mvn package
java -jar target/cchecker-service-parser-0.0.1-SNAPSHOT.jar &
cd ../cchecker-gateway
mvn package
cp target/cchecker-gateway-0.0.1-SNAPSHOT.war ./gateway.war
mv gateway.war /usr/local/apache-tomcat/webapps
cd /usr/local/apache-tomcat
./bin/catalina.sh run
