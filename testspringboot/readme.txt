This is a Maven test application to run RESTful services using Spring boot to create microservices.

The service uploads a file, and saves it to directory listed in configuration file.
This application configuration file is:
src/main/resources/boot.properties

Spring boot application configuration conventional file name:
application.properties
Add DB properties values to this file.
There is a port listened by Tomcat Spring embedded web server (8040). Default port would be 8080.

Maven "package" copies all dependencies libraries to "target/lib" directory.
Change pom.xml if this is not desirable.

The RESTful controller examples:
"RestUploadController"
"RestDataElementController"

Add more similar controllers as required to the same package "gov.nih.nci.testspringboot"using say "RestDataElementController" as a start point.

"Example" controller class is the main Spring boot application class. It also works as a controller for the root application page "/".

"EmbeddedTomcatDBConfig" creates application beans on the application start. It uses the file "application.properties" to create DB "DataSource" bean.
The bean set includes beans "dataSource" and "jdbcTemplate".
A class "DataElementRepository" is created as a bean, and it has autowired "jdbcTemplate" bean.
RESTful controller "RestDataElementController" has autowired DataElementRepository bean used to retrieve DB objects.
A class "DataElements" is a POJO, which is streamed by RESTful controller as a JSON.

One can run Spring Boot application from Eclipse using a "Example" class, using right mouse button menu "Run as" -> "Spring Boot App".
This run menu is shown if using STS IDE, or if Eclipse "STS plugin" is installed.

To run boot from a command line:
1. Go to project pom.xml directory

2. Select a port to listen, below is 8040.
Maven command is "spring-boot:run":

mvn spring-boot:run

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
http://localhost:8040/ 

4. It shall show "index" page, implemented by "Example" controller class. This class is also main application class.
Hello Spring Boot World!

5. To upload a file using a RESTful service select an existed rather small file as /local/content/source/data.txt, and run "curl" command.
Go to a different terminal, and run:
curl -F file=@"/local/content/source/data.txt" http://localhost:8040/rest/upload/

You can send an AJAX request from a web page.

6. The file shall be saved in the directory from configuration.
Default: /local/content/cchecker/
If the directory does not exist it will be created when possible.

7. DB RESTful service example:
curl http://localhost:8080/rest/cde/
DB information shall be added to the file "application.properties".
The order of fields in JSON is the same as the order of 'get' methods in class "DataElements".

8. To create the application jar file use:
mvn package
or to skip tests
mvn package --DskipTests=true
To run the application using jar file from the main project directory:
java -jar target/testspringboot-0.0.1-SNAPSHOT.jar