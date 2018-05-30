This is a Maven test application to run RESTful services using Spring boot to create a microservice.

The service uploads a file, and saves it to directory listed in configuration file.
Configuration file is:
src/main/resources/boot.properties

Maven build copies all dependencies libraries to "target/lib" directory.
Change pom.xml if this is not desirable.

The RESTful controller example is in the class "RestUploadController".
Add more similar controllers as required to the same package "gov.nih.nci.testspringboot"

"Example" controller class is also the main application class. It serves the root application page "/".

One can run Spring Boot application from Eclipse using this class, and Run as "Spring Boot App".
This requires using STS IDE, or Eclipse "STS plugin" to be installed.

To run from a command line:
1. Go to project pom.xml directory

2. Select a port to listen, below is 8040.
Maven command is "spring-boot:run":

mvn -Dserver.port=8040 spring-boot:run

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

7. DB Example:
curl http://localhost:8040/rest/cde/
Add DB information to the file:
application.properties
This is a conventional boot configuration file.