This is a Maven project. It requires a standard Maven set up on the computer.
Main class: gov.nih.nci.ncicb.cadsr.common.bulkdownload.FormBulkDownloadXML
Spring framework configuration: application-context.xml

Run the below command for execution of the JAVA program.
mvn clean resources:resources compile -Ddb.url=@HOST:PORT:SID -Ddb.user=<> -Ddb.passwd=<> exec:exec