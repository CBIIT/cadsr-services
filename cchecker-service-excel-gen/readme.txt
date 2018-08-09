This is caDSR Congruence checker ALS Parser microservice.
This is a Maven application running RESTful services using Spring boot with embedded Tomcat.
Parser service port number is 4801.

The service expects that a file to parse is uploaded in a directory listed in configuration file.
This application configuration file is:
src/main/resources/boot.properties

Spring boot application configuration conventional file name:
application.properties

Maven "package" copies all dependencies libraries to "target/lib" directory.
Change pom.xml if this is not desirable.

Log file as of now:
log/cchecker-excel-gen.log

The RESTful controller:
"AlsParserController"

"CCheckerExcelGenService" controller class is the main application class. It also works as a controller for the root application page "/".

One can run Spring Boot application from Eclipse using a "CCheckerExcelGenService" class, using right mouse button menu "Run as" -> "Spring Boot App".
This run menu is shown if using STS IDE, or if Eclipse "STS plugin" is installed.

To run boot from a command line:
1. Go to project pom.xml directory

2. Port is 4807.
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
The service will be running.

It require CCheckerDbService (DB microservice) running.

3. go to a browser with a URL
http://localhost:4807/ 

4. It shall show "index" page, implemented by "CCheckerExcelGenService" controller class. One see a text:
CCheckerExcelGenService is running!

5.The generated Excel report error file is saved in the directory from configuration with "Report_" prefix.
Project configured directory: /local/content/cchecker/
File example:
Report-0BCAEE78-9916-4ADA-B7CD-CE5854AFDD82.xlsx

6. To generate an Excel file using a RESTful service find an IDSEQ of an existed Report Error created by 
ALSValidatorService, and run "curl" command GET request with _cchecker QUERY PARAMETER (IDSEQ value).

curl -v http://localhost:4807/rest/generatereporterror?_cchecker=0BCAEE78-9916-4ADA-B7CD-CE5854AFDD82


7.The response is a stream of Excel file. The response sends the headers:
"Content-Type": "application/vnd.ms-excel"
"Content-Disposition": "attachment; filename=Report-MyFile.xlsx"
to open "Save as" dialog in the browser.

8. To create the application jar file use:
>mvn clean package
>mvn package
or to skip tests
>mvn package -DskipTests=true

9.To run the application using jar file from the main project directory:
>java -jar target/cchecker-parser-0.0.1-SNAPSHOT.jar

