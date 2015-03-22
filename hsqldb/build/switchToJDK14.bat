cd ..\
md classes
del /s classes\*.class
cd build
cd ..\src\org\hsqldb\util
javac -d ..\..\..\..\classes CodeSwitcher.java
cd ..\..\..\..\build
java -classpath %classpath%;../classes org.hsqldb.util.CodeSwitcher ../src/org/hsqldb/jdbcConnection.java +JDBC3
java -classpath %classpath%;../classes org.hsqldb.util.CodeSwitcher ../src/org/hsqldb/jdbcDatabaseMetaData.java +JDBC3
java -classpath %classpath%;../classes org.hsqldb.util.CodeSwitcher ../src/org/hsqldb/jdbcPreparedStatement.java +JDBC3
java -classpath %classpath%;../classes org.hsqldb.util.CodeSwitcher ../src/org/hsqldb/jdbcResultSet.java +JDBC3
java -classpath %classpath%;../classes org.hsqldb.util.CodeSwitcher ../src/org/hsqldb/jdbcStatement.java +JDBC3
