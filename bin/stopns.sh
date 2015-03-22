#!/bin/sh
# Stop the Lazy node server with hsqldb
#
# Assumes that
# JAVA_HOME has been set to the home dir of the Java installation
# TOMCAT_HOME has been set to the home dir of the Tomcat server
# JDBC_DRIVER is the JDBC driver used
#
# CLASSPATH=.:$LAZY_HOME/hsqldb/lib/hsqldb.jar
CLASSPATH=.:$JDBC_DRIVER
export CLASSPATH
cd $TOMCAT_HOME/bin
shutdown.sh
