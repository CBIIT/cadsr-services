#!/bin/bash

echo "Executing XML Form Download"

DATE=`date +%Y%m%d`
JAVA_HOME=/usr/java8
BASE_DIR=/local/content/fbdwlnxml/bin
CLASS_DIR=/local/content/fbdwlnxml/target/classes
LIB_DIR=/local/content/fbdwlnxml/target/lib

export JAVA_HOME BASE_DIR CLASS_DIR LIB_DIR
 
JAVA_PARMS='-Xms512m -Xmx512m'

export JAVA_PARMS

echo "Executing new job as `id`"
echo "Executing on `date`"

for x in $LIB_DIR/*.jar
do

CP=$CP:$x

done

export CP

echo $JAVA_HOME/bin/java -client $JAVA_PARMS -classpath $CLASS_DIR:$CP -Ddb.url=@ -Ddb.user= -Ddb.passwd= gov.nih.nci.ncicb.cadsr.common.bulkdownload.FormBulkDownloadXML 100 Theradex

$JAVA_HOME/bin/java -client $JAVA_PARMS -classpath $CLASS_DIR:$CP -Ddb.url= -Ddb.user= -Ddb.passwd= gov.nih.nci.ncicb.cadsr.common.bulkdownload.FormBulkDownloadXML 100 Theradex