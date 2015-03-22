#!/usr/bin/csh
#  Environment varibles for the basic Lazy installation
#
# TOMCAT_HOME must have been set to your tomcat installation directory
# LAZY_HOME must have been set to your Lazy installation directory

setenv CATALINA_HOME $TOMCAT_HOME
setenv HSQL_HOME $LAZY_HOME/hsqldb
setenv JDBC_DRIVER $HSQL_HOME/lib/hsqldb.jar
setenv PATH ${PATH}:${LAZY_HOME}/bin:.

