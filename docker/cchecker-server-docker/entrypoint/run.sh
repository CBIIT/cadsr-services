#~/bin/sh

# pull up to date code #
echo "entering cadsr-services directory"
cd cadsr-services
echo "pulling latest code from github"
git pull 

# buld and run the cchecker parser #
echo "entering cchecker-service-parser directory"
cd cchecker-service-parser
echo "building cchecker-service-parser"
mvn package
java -jar target/cchecker-service-parser-0.0.1-SNAPSHOT.jar &
echo "done building cchecker-service-parser"

# build and run the cchecker db service #
echo "entering cchecker-service-db directory"
cd ../cchecker-service-db
echo "building cchecjer-service-db"
mvn clean package
java -jar target/cchecker-service-db-0.0.1-SNAPSHOT.jar &
echo "done building cchecker-service-db" 

# build and run the cchecker gateway #
echo "entering cchecker-gateway directory"
cd ../cchecker-gateway
echo "building cchecker-gateway"
mvn package
cp target/cchecker-gateway-0.0.1-SNAPSHOT.war ./gateway.war
mv gateway.war /usr/local/apache-tomcat/webapps
echo "done building cchecker-gateway"

echo "entering apache-tomcat directory"
echo "starting tomcat"
cd /usr/local/apache-tomcat
./bin/catalina.sh run
echo "done starting tomcat"
