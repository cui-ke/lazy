#!/bin/sh
CP=.:$TOMCAT_HOME/lib/servlet.jar::$TOMCAT_HOME/lib/servlet-api.jar:\
$TOMCAT_HOME/common/lib/servlet.jar:\
$TOMCAT_HOME/webapps/lazy/WEB-INF/classes
# ADD Jena lib
for j in ${JENA_LIB}/* ; do
    CP=${CP}:$j
done
# echo $CP
java  -classpath ${CP}  TestSesame "prefix owl: <http://a.com/a#> select ?c wher {?c a owl:Class.}"

