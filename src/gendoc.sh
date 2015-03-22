#!/bin/sh
javadoc *.java \
-private -d javadoc \
-classpath .:$TOMCAT_HOME/lib/servlet.jar
