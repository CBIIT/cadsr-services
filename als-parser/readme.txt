
																ALS Parser


ALS Parser is a Maven-built JAVA application. It requires a standard Maven set up on the computer.

ALS Parser will parse a RAVE ALS Excel file provided by the customer to validate the CDEs in it against the 
caDSR database to producce a congruence check report. 

The main class is gov.nih.nci.cadsr.parser.AlsParser, which consists of the parsing logic.
The data objects that will be used to store the input and the output for this program 
will be under gov.nih.nci.cadsr.data

The input (test) ALS file should be present in src/main/resources and is controlled by
'INPUT_XLSX_FILE_PATH' in AlsParser.java


To compile/build the application: 
Run the following command from the project root folder [where POM.xml resides]

mvn clean compile

To create the application jar file use the following command

mvn clean package


Run the JAVA application
1. Eclipse IDE
Run the main class - AlsParser as a JAVA application

2. To run as a JAR executable, use the following command
java -jar target/alsparser-0.0.1-SNAPSHOT.jar