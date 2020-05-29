This is caDSR Congruence checker XML ALS Parser microservice.
This is a Maven application running RESTful services using Spring boot with embedded Tomcat.
Parser service port number is 4813.

The service expects that a file to parse is uploaded in a directory listed in configuration file.
This application configuration file is:
src/main/resources/boot.properties

Spring boot application configuration conventional file name:
application.properties

Maven "package" copies all dependencies libraries to "target/lib" directory.
Change pom.xml if this is not desirable.

Log file as of now:
log/cchecker-xml-parser.log

The RESTful controller:
"XmlAlsParserController"

"CCheckerXmlAlsParserService" controller class is the main application class. It also works as a controller for the root application page "/".

One can run Spring Boot application from Eclipse using a "CCheckerXmlAlsParserService" class, using right mouse button menu "Run as" -> "Spring Boot App".
This run menu is shown if using STS IDE, or if Eclipse "STS plugin" is installed.

To run boot from a command line:
1. Go to project pom.xml directory

2. ALS Parser port is 4813.
Maven command is "spring-boot:run":

>mvn spring-boot:run

You shall see the output:
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::       (v1.5.13.RELEASE)

and you shall not see any exception.
The service will be running

3. go to a browser with a URL
http://localhost:4813/ 

4. It shall show "index" page, implemented by "CCheckerXmlAlsParserService" controller class. One see a text:
CCheckerXmlAlsParserService is running!

5. An ALS file shall be saved in the directory from configuration.
Project configured directory: /local/content/cchecker/
File example:
RAVE-ALS-10057-VS.xlsx

6. To parse a file using a RESTful service select an existed ALS file saved on /local/content/cchecker, and run "curl" command.

curl -d "filepath=RAVE-ALS-10057-VS.xlsx" http://localhost:4813/rest/xmlalsparserservice

7. To create the application jar file use:
mvn clean package
or to skip tests
mvn clean package -DskipTests=true

8.To run the application using jar file from the main project directory:
java -jar target/cchecker-service-xml-als-parser-2.0.0.jar