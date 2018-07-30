This is caDSR Congruence DB microservice.
This is a Maven application to run RESTful services connected to DB using Spring boot.
This is a feasibility investigation project.
Some packages are taken from caDSR CDE Browser project for RestCDEController:
/cchecker-service-db/src/main/java/gov/nih/nci/cadsr/dao/model
cchecker-service-db/src/main/java/gov/nih/nci/cadsr/service
RestCDEController uses CdeDetails representation class of CDE Browser


This is a Maven application running RESTful services using Spring boot with embedded Tomcat.
Parser service port number is configured to be 4803.

The service expects that a file to parse is uploaded in a directory listed in configuration file.
This application configuration file is:
src/main/resources/boot.properties

Spring boot application configuration conventional file name:
application.properties

!!!
We expect the next four environment variables to be passed to the application:
db_driver, db_url, db_user=SBR, db_credential
!!!

Maven "package" copies all dependencies libraries to "target/lib" directory.
Change pom.xml if this is not desirable.

Log file as of now:
log/cchecker-db.log

The RESTful controller:
"CCheckerDbController"

"CCheckerDbService" controller class is the main application class. It also works as a controller for the root application page "/".

One can run Spring Boot application from Eclipse using a "CCheckerDbService" class, using right mouse button menu "Run as" -> "Spring Boot App".
This run menu is shown if using STS IDE, or if Eclipse "STS plugin" is installed.

To run boot from a command line:
1. Go to project pom.xml directory

2. Maven build
mvn -Ddb_driver=... -Ddb_url=<drivertype>:@<url>:<port>:<sid> -Ddb_user=... -Ddb_credential=... clean package 

If environment is set up just run mvn command
>mvn clean package


3. Service Port is 4803.

To run the application using jar file from the main project directory passing environment dynamically:
>java -Ddb_driver=... -Ddb_url=<drivertype>:@<url>:<port>:<sid> -Ddb_user=... -Ddb_credential=...  -jar target/cchecker-service-db-0.0.1-SNAPSHOT.jar

If environment is set up just run java command
>java -jar target/cchecker-db-parser-0.0.1-SNAPSHOT.jar

4. Index page of the running service application:
http://localhost:4803

5. Retrieve ALSData
curl -v http://localhost:4803/rest/retrievealsdata?_cchecker=855B4B76-A462-4B73-8727-3194517C6DE3
