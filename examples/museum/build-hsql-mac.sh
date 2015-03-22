echo "MUSEUM schema"
java -classpath $HSQL_HOME/lib/hsqldb.jar org.hsqldb.util.ScriptTool    -script $LAZY_HOME/examples/museum/mw-drop-tables.sql -url jdbc:hsqldb:hsql: -database //localhost

java -classpath $HSQL_HOME/lib/hsqldb.jar org.hsqldb.util.ScriptTool    -script $LAZY_HOME/examples/museum/mw-dbschema-hsql.sql -url jdbc:hsqldb:hsql: -database //localhost
echo "MUSEUM data"
#
# On MacOSX the script file must be encoded in MacRoman (by default)
java -classpath $HSQL_HOME/lib/hsqldb.jar org.hsqldb.util.ScriptTool    -script $LAZY_HOME/examples/museum/mw-data-mac.sql -url jdbc:hsqldb:hsql: -database //localhost


echo "Compiling museum nodes"
lc $LAZY_HOME/examples/museum/mw.lzy
#lc $LAZY_HOME/examples/museum/mw-exh-nodes.lzy
lc $LAZY_HOME/examples/museum/mw-updates-hsql.lzy
lc $LAZY_HOME/examples/museum/mw-contemp-for-hsql.lzy
