#!/bin/sh
java -classpath $HSQL_HOME/lib/hsqldb.jar org.hsqldb.util.ScriptTool   -url jdbc:hsqldb:hsql: -database //localhost -script $1
