@echo off
rem Variable settings for the LAZY environment
rem
rem set TOMCAT_HOME, JAVA_HOME, and LAZY_HOME in the System settings or autoexec.bat or here
rem
rem set JAVA_HOME=<<your java installation directory 
rem set TOMCAT_HOME=<<location of the tomcat directory>>
rem set LAZY_HOME=<<location of the Lazy installation>>
rem
rem
set CATALINA_HOME=%TOMCAT_HOME%
set HSQL_HOME=%LAZY_HOME%\hsqldb
set JDBC_DRIVER=%HSQL_HOME%\lib\hsqldb.jar
set PATH=%PATH%;%LAZY_HOME%\bin;.
