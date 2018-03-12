This is a Maven project. It requires a standard Maven set up on the computer.
Main class: gov.nih.nci.ncicb.cadsr.common.bulkdownload.FormBulkDownloadXML
Spring framework configuration application-context.xml

1.Compile and substitute parameters
mvn clean compile -Ddb.url=@URL:PORT:SID -Ddb.user=ADD-HERE -Ddb.passwd=ADD-HERE

2. Run the below command for execution of the JAVA program
mvn exec:exec