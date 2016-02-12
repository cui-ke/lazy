#!/bin/sh
java -ms8m -classpath $HSQL_HOME/lib/hsqldb.jar org.hsqldb.Server -database $LAZY_HOME/lazydict/test $*
