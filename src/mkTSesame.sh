#!/bin/sh
CP=.:$TOMCAT_HOME/lib/servlet.jar:$TOMCAT_HOME/lib/servlet-api.jar:\
$TOMCAT_HOME/webapps/lazy/WEB-INF/classes
# $JENA_LIB/\"*\".jar

# ADD Jena lib
for j in ${JENA_LIB}/*.jar ; do
   CP=${CP}:$j
done

# ADD Sesame lib
#for j in ${TOMACAT_HOME}/openrdf-sesame-4.0.1/lib/* ; do
#    CP=${CP}:$j
#done

# echo $CP

export CP

# echo $CP
# javac ns.java -classpath ${CP} -Xlint:deprecation -d $TOMCAT_HOME/webapps/lazy/WEB-INF/classes
javac TestSesame.java -classpath ${CP} 


