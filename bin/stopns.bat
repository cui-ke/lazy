@echo off
rem Stop the Lazy node server with hsqldb
rem
rem Assumes that
rem JAVA_HOME has been set to the home dir of the Java installation
rem TOMCAT_HOME has been set to the home dir of the Tomcat server
rem JDBC_DRIVER is the JDBC driver used
rem
set CLASSPATH=.;%JDBC_DRIVER%
cd %TOMCAT_HOME%\bin
call %TOMCAT_HOME%\bin\shutdown
