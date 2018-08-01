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
********
Upload file request to parse ALS uploaded file. This is multipart content.

curl -F file=@"/local/content/cchecker/RAVE-ALS-10057-VS.xlsx" http://localhost:8080/gateway/parseservice?owner=owner1
curl -F file=@"/local/content/cchecker/RAVE-ALS-10057-VS.xlsx" -F owner="owner1" http://localhost:8080/gateway/parseservice

Error response on a wrong file format:
curl -F file=@"/local/content/cchecker/data.txt" -F owner="owner1" http://localhost:8080/gateway/parseservice
********
Service "/gateway/checkservice" is a sample implementation as of now.
For a manual test take a look into your directory /local/contents/cchecker.
Find any file created by parseservice with UID-like name on /local/content/cchecker.
The file is retrieved from DB storage table. 
This is one example we have in DEV DB: 855B4B76-A462-4B73-8727-3194517C6DE3.
 
use an existed UID as a cookie for testing this service. ALS parser data with this ID shall be save in DB before this call.
curl -v --cookie "_cchecker=855B4B76-A462-4B73-8727-3194517C6DE3" -X POST \
-H "Content-Type: application/json" --data "@/local/content/cchecker/formnamelist.json" http://localhost:8080/gateway/checkservice
"formnamelist.json" file contains a json array of string. Each String is a form name.
Example: ["Enrollment","Other Form 1"]
Other request parameters which are all "false" by default:
checkUOM=true/false
checkCRF=true/false
displayExceptions=true/false
********
********
Test services
********
If cchecker-service-parser microservice is running we can call a test service parsefileservice:
http://localhost:8080/gateway/parsefileservice?filepath=/local/content/cchecker/RAVE-ALS-10057-VS.xlsx
To call parser using curl and using prepared ALS file:
curl http://localhost:8080/gateway/parsefileservice?filepath=/local/content/cchecker/RAVE-ALS-10057-VS.xlsx
********
To upload a file to the server, select an existed file as /local/content/source/data.txt, and run "curl" command.
Go to a different terminal, and run:
curl -F file=@"/local/content/source/data.txt" http://localhost:8080/uploadfileservice


********
We added "testreportservice" for testing Congruence Checker Report representation. It sends generated test data based on input file.
The supporting class is "CCCReport".
curl http://localhost:8080/gateway/testreportservice?owner=owner1&filepath=/local/content/cchecker/RAVE-ALS-10057-VS.xlsx
********