cd ..\
md classes
del /s classes\*.class
cd build
cd ..\src\org\hsqldb\util
javac -d ..\..\..\..\classes CodeSwitcher.java
cd ..\..\..\..\build
java -classpath %classpath%;../classes org.hsqldb.util.CodeSwitcher ../src/org/hsqldb/jdbcSystem.java -JAVA2
java -classpath %classpath%;../classes org.hsqldb.util.CodeSwitcher ../src/org/hsqldb/jdbcResultSet.java -JAVA2
java -classpath %classpath%;../classes org.hsqldb.util.CodeSwitcher ../src/org/hsqldb/HsqlProperties.java -JAVA2
java -classpath %classpath%;../classes org.hsqldb.util.CodeSwitcher ../src/org/hsqldb/BinaryServerRowOutput.java -JAVA2
java -classpath %classpath%;../classes org.hsqldb.util.CodeSwitcher ../src/org/hsqldb/util/jdbcSystem.java -JAVA2
java -classpath %classpath%;../classes org.hsqldb.util.CodeSwitcher ../src/org/hsqldb/util/TransferDb.java -JAVA2
java -classpath %classpath%;../classes org.hsqldb.util.CodeSwitcher ../src/org/hsqldb/util/TransferSQLText.java -JAVA2
java -classpath %classpath%;../classes org.hsqldb.util.CodeSwitcher ../src/org/hsqldb/util/TransferHelper.java -JAVA2
java -classpath %classpath%;../classes org.hsqldb.util.CodeSwitcher ../src/org/hsqldb/util/TransferTable.java -JAVA2
