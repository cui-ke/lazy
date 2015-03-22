@echo off
rem Start the Lazy node server with hsqldb
rem
rem JDBC_DRIVER is the jdbc driver library
rem JAVA_HOME has been set to the home dir of the Java installation
rem TOMCAT_HOME has been set to the home dir of the Tomcat server
rem
set CLASSPATH=.;%JDBC_DRIVER%
cd %TOMCAT_HOME%\bin
call "%TOMCAT_HOME%\bin\startup"
