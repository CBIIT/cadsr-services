This is a feasibility project for caDSR ALS Congruence Checker Validator microservice.
It uses Spring boot with an embedded Tomcat server.

>mvn package
or
>mvn clean package
creates 
target/alsvalidator-0.0.1-SNAPSHOT.jar

********
Upload file request to parse ALS uploaded file. This is multipart content.

curl -F file=@"/local/content/cchecker/RAVE-ALS-10057-VS.xlsx" http://localhost:8080/gateway/parseservice?owner=owner1
curl -F file=@"/local/content/cchecker/RAVE-ALS-10057-VS.xlsx" -F owner="owner1" http://localhost:8080/gateway/parseservice

Error response on a wrong file format:
curl -F file=@"/local/content/cchecker/data.txt" -F owner="owner1" http://localhost:8080/gateway/parseservice
********
Service "/rest/validateservice" is a feasibility implementation.
For a manual test take a look into your directory /local/contents/cchecker.
Find any file created by parseservice with UID-like name on /local/content/cchecker.
The file is retrieved from DB storage table. 
This is one example we have in DEV DB: 0BCAEE78-9916-4ADA-B7CD-CE5854AFDD82.
 
use an existed UID as a request parameter for testing this service. 
ALS parser data with this ID shall be save in DB before this call.
curl -X POST -H "Content-Type: application/json" \
--data "@/local/content/cchecker/formnamelist.json" \
http://localhost:4805/rest/validateservice?_cchecker=0BCAEE78-9916-4ADA-B7CD-CE5854AFDD82

"formnamelist.json" file contains a json array of string. Each String is a form name.
Example: ["Enrollment","ELIGIBILITY_CHECKLIST"]
["HISTOLOGY_AND_DISEASE","PATIENT_ELIGIBILITY"]
["Enrollment","HISTOLOGY_AND_DISEASE","ADMINISTRATIVE_ENROLLMENT","ELIGIBILITY_CHECKLIST","MOLECULAR_MARKER"]
Other request parameters are all "false" by default:
checkUOM=true/false
checkCRF=true/false
displayExceptions=true/false
