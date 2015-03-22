#!/bin/sh
# Start the Lazy node server with hsqldb
#
# JDBC_DRIVER is the jdbc driver library
# JAVA_HOME has been set to the home dir of the Java installation
# TOMCAT_HOME has been set to the home dir of the Tomcat server
#
CLASSPATH=.:$JDBC_DRIVER

# export Jena lib
for j in ${JENA_LIB}/* ; do
  if [ "$CLASSPATH" != "" ]; then
    CLASSPATH=${CLASSPATH}:$j
  else
    CLASSPATH=$j
  fi
done

export CLASSPATH
echo "Start Tomacat with ${CLASSPATH}"
cd $TOMCAT_HOME/bin
startup.sh
