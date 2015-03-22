@echo HSQLDB build file for jdk 1.2.x and 1.3.x
@echo *** we recommend the use of the ANT build.xml instead of this method
@echo for all jdk's include the path to jdk1.x.x\bin in your system path statement
cd ..\
md classes
del /s classes\*.class
cd src
mkdir ..\temp
copy org\hsqldb\jdbcDataSource*.java ..\temp\
del org\hsqldb\jdbcDataSource*.java
javac -O -nowarn -d ../classes -classpath %classpath%;../classes;../lib/servlet.jar;. ./*.java org/hsqldb/*.java org/hsqldb/lib/*.java org/hsqldb/util/*.java
copy ..\temp\jdbcDataSource*.java org\hsqldb
del ..\temp\jdbcDataSource*.java
rmdir ..\temp
cd ..\classes
copy ..\src\org\hsqldb\util\hsqldb.gif org\hsqldb\util
jar -cf ../lib/hsqldb.jar *.class org/hsqldb/*.class org/hsqldb/lib/*.class org/hsqldb/util/*.class  org/hsqldb/util/*.gif
cd ..\build
pause
