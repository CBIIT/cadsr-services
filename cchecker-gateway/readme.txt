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

If cchecker-service-parser microservice is running we can call a test service parsefileservice:
To call parser using curl and using prepared ALS file:
curl http://localhost:8080/gateway/parsefileservice?filepath=/local/content/cchecker/RAVE-ALS-10057-VS.xlsx

To upload a file to the server, select an existed rather small file as /local/content/source/data.txt, and run "curl" command.
Go to a different terminal, and run:
curl -F file=@"/local/content/source/data.txt" http://localhost:8080/uploadfileservice

http://localhost:8080/gateway/parsefileservice?filepath=/local/content/cchecker/RAVE-ALS-10057-VS.xlsx

curl -F file=@"/local/content/cchecker/RAVE-ALS-10057-VS.xlsx" http://localhost:8080/gateway/parseservice?owner=owner1
