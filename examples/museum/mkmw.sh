#!/bin/sh
javac MW.java \
-classpath .:$TOMCAT_HOME/webapps/lazy/WEB-INF/classes \
-d $TOMCAT_HOME/webapps/lazy/WEB-INF/classes
