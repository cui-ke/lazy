#!/bin/sh
cd $HSQL_HOME/demo
java  -ms8m -classpath $HSQL_HOME/lib/hsqldb.jar org.hsqldb.util.DatabaseManager $*
