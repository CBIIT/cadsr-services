This is a feasibility project for caDSR ALS Congruence Checker Validator microservice.
It uses Spring boot with an embedded Tomcat server.

>mvn package
or
>mvn clean package
creates 
target/alsvalidator-0.0.1-SNAPSHOT.jar

To run the service:
>mvn spring-boot:run

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


To call feed current form number:
http://localhost:4805/rest/validateservice/feedvalidateformnumber/8519163A-ECB2-40C4-B229-5C13E0F4279B
curl -X GET -i http://localhost:4805/rest/feedvalidateformnumber/8519163A-ECB2-40C4-B229-5C13E0F4279B
