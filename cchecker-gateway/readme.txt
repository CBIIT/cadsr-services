This is a feasibility project for caDSR Congruence Checker Gateway.
This is Maven project. It uses Spring boot to deploy to a standalone Tomcat server.

Embedded Tomcat is disabled.
One needs to download Tomcat v.8.0.x or above and unzip it in a directory.

Tomcat conf/server.xml shall have :
      <Context docBase="gateway" path="/gateway"/>
</Host>

>mvn package
creates 
target/cchecker-gateway-0.0.1-SNAPSHOT.war

Copy this war file as "gateway.war" into Tomcat "webapp" directory.
Start Tomcat.
The first page in a bowser:
http://localhost:8080/gateway
It is served by:
cchecker-gateway/src/main/resources/templates/welcome.html

GatewayBootWebApplication is a spring boot service to be deployed to a standalone Tomcat.
********parseservice**********
Upload file request to parse ALS uploaded file. This is multipart content.

curl -F file=@"/local/content/cchecker/RAVE-ALS-10057-VS.xlsx" http://localhost:8080/gateway/parseservice?owner=owner1
curl -F file=@"/local/content/cchecker/RAVE-ALS-10057-VS.xlsx" -F owner="owner1" http://localhost:8080/gateway/parseservice

Error response on a wrong file format:
curl -F file=@"/local/content/cchecker/data.txt" -F owner="owner1" http://localhost:8080/gateway/parseservice
********checkservice**********
Service "/gateway/checkservice" is a sample implementation as of now.
For a manual test take a look into your directory /local/contents/cchecker.
Find any file created by parseservice with UID-like name on /local/content/cchecker.
The file is retrieved from DB storage table which is stored for a short time only. 
This is one example we could have in DEV DB: 40A7A07A-CE45-4DEF-A1E3-3C78F67B2E37.
 
Use an existed UID as a cookie and as a URL path parameter for testing this service. 
ALS parser data with this ID shall be save in DB before this call.
curl -v --cookie "_cchecker=0BCAEE78-9916-4ADA-B7CD-CE5854AFDD82" -X POST \
-H "Content-Type: application/json" --data "@/local/content/cchecker/formnamelist.json" http://localhost:8080/gateway/checkservice?sessionid=40A7A07A-CE45-4DEF-A1E3-3C78F67B2E37

"formnamelist.json" file contains a json array of string. Each String is a form name.
Examples of Form names lists: 
["Enrollment"]
["HISTOLOGY AND DISEASE","PATIENT ELIGIBILITY"]

Other URL query request parameter which is "false" by default:
checkCRF=true/false
Example:
curl 'http://nciws-d1030-v.nci.nih.gov:8080/gateway/checkservice?checkCRF=true&sessionid=40A7A07A-CE45-4DEF-A1E3-3C78F67B2E37' \
-H 'Accept: application/json, text/plain, */*' -H 'Content-Type: application/json' -H 'Cookie: _cchecker=40A7A07A-CE45-4DEF-A1E3-3C78F67B2E37'\
--data '["US","Vital Signs"]'

Response HTTP Status code is 201 (Created) 
URI in Location header is to call 'retrievereporterror' service (below).

Location header:
http://localhost:8080/gateway/retrievereporterror/45635A0C-6B3D-4BFB-ADA4-FC28DC557B2E

********genexcelcheckreport***********
The next call shall open Save as dialog for a report previously generated.
Use an existed UID as a cookie and as a URL path parameter for testing this service. 
curl -v --cookie "_cchecker=0BCAEE78-9916-4ADA-B7CD-CE5854AFDD82" http://localhost:8080/gateway/genexcelcheckreport/40A7A07A-CE45-4DEF-A1E3-3C78F67B2E37

********retrievereporterror************
Retrieve an existed report
curl -v http://localhost:8080/gateway/retrievereporterror/235393B4-3676-4A79-871C-EE632D4E8279
returns CCCReport object
or 400 - wrong ID format
or 404 - not found
********retrieveexcelreporterror**********
Retrieve an existed Excel report generated earlier.
curl -v http://localhost:8080/gateway/retrieveexcelreporterror/A9D4DF89-7680-48F5-8E0E-7094567944D1
returns Save as Excel object
or 400 - wrong ID format
or 404 - not found
**********feedcheckstatus************
Feed Validation status (deprecated)
/feedcheckstatus/{sessionid}
Use an existed UID as a cookie and as a URL path parameter for testing this service.
Example:
curl -v --cookie "_cchecker=005BE648-0924-491B-AF22-C02AEF415FB8" http://localhost:8080/gateway/feedcheckstatus/A9D4DF89-7680-48F5-8E0E-7094567944D1
return SSE with current form number

/feedvalidatestatus/{sessionid}
curl -v --cookie "_cchecker=005BE648-0924-491B-AF22-C02AEF415FB8" http://localhost:8080/gateway/feedvalidatestatus/38015E6C-439A-45A9-9D78-0674202D9BEE
return SSE with JSON. Example:
{"currFormName":"Specimen Transmittal","currFormNumber":67,"countValidatedQuestions":758}
**********cancelvalidation************
Cancels previous Validation status
/cancelvalidation/{sessionid}
Use an existed UID as a cookie and as a URL path parameter for testing this service. The validation with this ID shall be running for full test.
Example:
curl -v --cookie "_cchecker=005BE648-0924-491B-AF22-C02AEF415FB8" http://localhost:8080/gateway/cancelvalidation/A9D4DF89-7680-48F5-8E0E-7094567944D1
return SSE with current form number

********ALS Forms to caDSR Forms*********
********retrievecontexts**********
GET request to retrieve a list of caDSR context names.
/retrievecontexts
http://localhost:8080/gateway/retrievecontexts
********loadformservice**********
curl -v --cookie "_cchecker=B0625028-88E7-44E0-80B8-D52D0ADC1131" -X POST \
-H "Content-Type: application/json" --data "@/local/content/cchecker/formloadlist.json" http://localhost:8080/gateway/loadformservice?sessionid=B0625028-88E7-44E0-80B8-D52D0ADC1131
This service expects a cookie which is ID of a session and earlier parsed ALS File.
Example of JSON request body:
{
	"contextName":"TEST",
	"selForms": ["Follow Up"]
}
Request body provides a valid caDSR Context name, and a JSON Array of Strings with ALS Form names.
This service requires cchecker-service-db and cchecker-service-fl running.
********Retrieve Context Names Service********
http://localhost:8080/gateway/retrievecontexts
********
********formxmlservice**********
curl -v --cookie "_cchecker=B0625028-88E7-44E0-80B8-D52D0ADC1131" -X POST \
-H "Content-Type: application/json" --data "@/local/content/cchecker/formloadlist.json" http://localhost:8080/gateway/formxmlservice?sessionid=B0625028-88E7-44E0-80B8-D52D0ADC1131
This service expects a cookie which is ID of a session and earlier parsed ALS File.
Example of JSON request body:
{
	"contextName":"TEST",
	"selForms": ["Follow Up"]
}
Request body provides a valid caDSR Context name, and a JSON Array of Strings with ALS Form names.
This service requires cchecker-service-db and cchecker-service-fl running.

Response HTTP Status code is 201 (Created) 
URI in Location header is to call 'retrieveformxml' service (below).
Response contains session ID.

Location header:
http://localhost:8080/gateway/retrieveformxml/45635A0C-6B3D-4BFB-ADA4-FC28DC557B2E

********
********retrieveformxml************
Retrieve an XML document generated by 'formxmlservice' .
curl -v http://localhost:8080/gateway/retrieveformxml/235393B4-3676-4A79-871C-EE632D4E8279
returns Save as XML object

or 400 - wrong ID format
or 404 - not found
Error response context is text/plain.

Response headers:
Content-Type: application/xml
Content-Disposition: attachment; filename=FormLoader-6533280A-14F6-44B9-90AB-011006533FF1.xml
********
********
Swagger 2 - Documentation
Swagger and Swagger-UI are added.
To try services by Swagger UI you have to have your browser cookie "_cchecker". The cookie is created by calling RESTful service "/parseservice".
http://localhost:8080/gateway/swagger-ui.html
http://localhost:8080/gateway/v2/api-docs
http://localhost:8080/gateway/swagger-resources
********
********
Test services
********
If cchecker-service-parser microservice is running we can call a test service parsefileservice:
http://localhost:8080/gateway/parsefileservice?filepath=/local/content/cchecker/RAVE-ALS-10057-VS.xlsx
To call parser using curl and using prepared ALS file:
curl http://localhost:8080/gateway/parsefileservice?filepath=/local/content/cchecker/RAVE-ALS-10057-VS.xlsx
********
