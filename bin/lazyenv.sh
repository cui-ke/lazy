# Environment varibles for the basic Lazy installation
#
# TOMCAT_HOME must have been set to your tomcat installation directory
# LAZY_HOME must have been set to your Lazy installation directory

export TOMCAT_HOME=$CATALINA_HOME
export LAZY_HOME=$TOMCAT_HOME/webapps/lazy
export HSQL_HOME=$LAZY_HOME/hsqldb
export JDBC_DRIVER=$HSQL_HOME/lib/hsqldb.jar

# set JENA path
export JENA_HOME=$LAZY_HOME/Jena/apache-jena-2.12.1
export JENA_LIB=$LAZY_HOME/WEB-INF/lib


export PATH=$PATH:$LAZY_HOME/bin:.
