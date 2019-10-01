This is a feasibility project for caDSR ALS Congruence Checker Validator microservice.
It uses Spring boot with an embedded Tomcat server.

>mvn package
or
>mvn clean package
creates 
target/alsvalidator-0.0.1-SNAPSHOT.jar

To run the service using Maven:
>mvn spring-boot:run
To run using jar:
>java -jar target/alsvalidator-0.0.1-SNAPSHOT.jar

For a manual test take a look into your directory /local/contents/cchecker.
Find any file created by parseservice with UID-like name on /local/content/cchecker.
The file is retrieved from DB storage table. 
This is one example we have in DEV DB: 0BCAEE78-9916-4ADA-B7CD-CE5854AFDD82.
 
Use an existed UID as a request parameter for testing this service. 
ALS parser data with this ID shall be save in DB before this call.
curl -X POST -H "Content-Type: application/json" \
--data "@/local/content/cchecker/formnamelist.json" \
http://localhost:4805/rest/validateservice?_cchecker=0BCAEE78-9916-4ADA-B7CD-CE5854AFDD82


"formnamelist.json" file contains all parameters. Parameter selForms is required. It has selected form name list.
Example:
{
	"checkUom":false,
	"selForms": ["Literal Laboratory"]
}

Other request parameters are all "false" by default:
checkUOM=true/false
checkCRF=true/false
displayExceptions=true/false


To call feed current form number (deprecated):
http://localhost:4805/rest/validateservice/feedvalidateformnumber/8519163A-ECB2-40C4-B229-5C13E0F4279B
curl -X GET -i http://localhost:4805/rest/feedvalidateformnumber/8519163A-ECB2-40C4-B229-5C13E0F4279B

To call feed current form
curl -X GET -i -v http://localhost:4805/rest/feedvalidateform/8519163A-ECB2-40C4-B229-5C13E0F4279B
Response is JSON as
{"currFormName":"Specimen Transmittal","currFormNumber":67,"countValidatedQuestions":758}

To call cancel current form validation:
http://localhost:4805/rest/validateservice/cancelrequest/8519163A-ECB2-40C4-B229-5C13E0F4279B
curl -v -X GET -i http://localhost:4805/rest/cancelrequest/8519163A-ECB2-40C4-B229-5C13E0F4279B

Running Unit tests (Excludes integration tests):
>mvn clean package

	or
	
>mvn test	 


Running Integration tests:

>mvn integration-test



Running code coverage reports (Jacoco):

Running one of the below commands will create code coverage reports with respect to unit tests (no integration tests) under target/report/jacoco-ut.
Open the master report - index.html with links to reports by package.

>mvn clean package

	or
	
>mvn test	 

Running the below command will create code coverage reports for both unit & integration tests under target/report/jacoco-ut & target/report/jacoco-it.
Open the master report - index.html with links to reports by package.

>mvn clean verify

 
