#~/bin/sh
if [ -d "/var/local/cadsr-services" ]; then
        echo "Skipping cadsr-services install"
else
  git clone https://github.com/CBIIT/cadsr-services  
  echo "Cloning the repository"
  ln -s /usr/local/apache-tomcat/log/cchecker-gateway.log /logs/cchecker-gateway.log
  ln -s /var/local/cadsr-services/alsvalidator/log/alsvalidator.log /logs/alsvalidator.log
  ln -s /var/local/cadsr-services/cchecker-service-db/log/cchecker-db.log /logs/cchecker-db.log
  ln -s /var/local/cadsr-services/cchecker-service-excel-gen/log/cchecker-excel-gen.log /logs/cchecker-excel-gen.log
  ln -s /var/local/cadsr-services/cchecker-service-parser/log/cchecker-parser.log /logs/cchecker-parser.log
  echo "creating symlinks for log files"
fi
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
echo "building cchecher-service-db"
mvn clean package
java -jar target/cchecker-service-db-0.0.1-SNAPSHOT.jar &
echo "done building cchecker-service-db" 

# build and run the alsvalidator service #
echo "entering alsvalidator directory"
cd ../alsvalidator
echo "building alsvalidator"
mvn clean package
java -jar target/alsvalidator-0.0.1-SNAPSHOT.jar &
echo "done building, alsvalidator service started"

# build and run the generate Excel service #
echo "entering cchecker-service-excel-gen directory"
cd ../cchecker-service-excel-gen
echo "building cchecker-service-excel-gen"
mvn clean package
java -jar target/cchecker-service-excel-gen-0.0.1-SNAPSHOT.jar &
echo "done building cchecker-service-excel-gen, cchecker-service-excel-gen service started"

# build and run the cchecker gateway #
echo "entering cchecker-gateway directory"
cd ../cchecker-gateway
echo "building cchecker-gateway"
mvn package
cp target/cchecker-gateway-0.0.1-SNAPSHOT.war ./gateway.war
mv gateway.war /usr/local/apache-tomcat/webapps
echo "done building cchecker-gateway"

echo "starting crond"
crond && tail -f /dev/null &
echo "done starting crond"

echo "entering apache-tomcat directory"
echo "starting tomcat"
cd /usr/local/apache-tomcat
./bin/catalina.sh run
echo "done starting tomcat"
