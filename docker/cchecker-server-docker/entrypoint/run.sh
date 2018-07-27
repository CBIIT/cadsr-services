#~/bin/sh

# pull up to date code #
cd cadsr-services
git pull 

# buld and run the cchecker parser #
cd cchecker-service-parser
mvn package
java -jar target/cchecker-service-parser-0.0.1-SNAPSHOT.jar &

# build and run the cchecker db service #
cd ../cchecker-service-db
mvn clean package
java -jar target/cchecker-service-db-0.0.1-SNAPSHOT.jar &

# build and run the cchecker gateway #
cd ../cchecker-gateway
mvn package
cp target/cchecker-gateway-0.0.1-SNAPSHOT.war ./gateway.war
mv gateway.war /usr/local/apache-tomcat/webapps
cd /usr/local/apache-tomcat
./bin/catalina.sh run
