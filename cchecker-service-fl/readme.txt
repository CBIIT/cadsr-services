This is caDSR Congruence checker ALS Parser microservice.
This is a Maven application running RESTful services using Spring boot with embedded Tomcat.
Parser service port number is 4811.

The service expects that a file to parse is uploaded in a directory listed in configuration file.
This application configuration file is:
src/main/resources/boot.properties

Spring boot application configuration conventional file name:
application.properties

Maven "package" copies all dependencies libraries to "target/lib" directory.
Change pom.xml if this is not desirable.

Log file as of now:
log/cchecker-fl.log

The RESTful controller:
"AlsParserController"

"CCheckerLoadFormService" controller class is the main application class. It also works as a controller for the root application page "/".

One can run Spring Boot application from Eclipse using a "CCheckerLoadFormService" class, using right mouse button menu "Run as" -> "Spring Boot App".
This run menu is shown if using STS IDE, or if Eclipse "STS plugin" is installed.

Requires the next environment:
db_credential
db_driver
db_url
db_user

To run boot from a command line:
1. Go to project pom.xml directory

2. Port is 4811.
Maven command is "spring-boot:run":

>mvn spring-boot:run

You shall see the output:
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::       (v2.1.4.RELEASE)

and you shall not see any exception.
The service will be running.

It require CCheckerDbService (DB microservice) running.

3. go to a browser with a URL
http://localhost:4811/ 

4. It shall show "index" page, implemented by "CCheckerLoadFormService" controller class. One see a text:
CCheckerLoadFormService is running!

5.A parsed ALS Excel file is expected to be in DB (ALSData java class instance).

6. RESTFul API
6a. To run load form service a RESTful service call find an IDSEQ of a stored ALS created by 
CCheckerParserService, and run "curl" command POST request with _cchecker QUERY PARAMETER (IDSEQ value).
"formloadlist.json" data file contains all services parameters. Parameter selForms is required. It has selected form names list. Context name and form names are case sensitive.
Example:
{
	"contextName":"TEST",
	"selForms": ["Literal Laboratory","Comments"]
}

curl -X POST -H "Content-Type: application/json" --data "@/local/content/cchecker/formloadlist.json" \
http://localhost:4811/rest/loadforms?_cchecker=9AD561C1-0BF4-43FE-BC9D-09402D6824D6

6b. To run generate XML Service
curl -X POST -H "Content-Type: application/json" --data "@/local/content/cchecker/formloadlist31.json" http://localhost:4811/rest/formxml?_cchecker=1296201E-6DCA-4E4D-93F9-E0A377868A0F
FL XML file is saved on the server REPORT directory in a file:
FormLoader-<session>.xml
Example:

FormLoader-1296201E-6DCA-4E4D-93F9-E0A377868A0F.xml

7.The response
The response sends the headers: ...
It contains form name JSON array which looks similar to "selForms" in request data above.

8. To create the application jar file use:
>mvn clean package
>mvn package
or to skip tests
>mvn package -DskipTests=true

9.To run the application using jar file from the main project directory:
>java -jar target/cchecker-service-fl-0.0.1-SNAPSHOT.jar
or
>mvn spring-boot:run
