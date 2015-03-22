HSQLDB can be built in any combination of three different sizes and
three JRE (Java Runtime Environment) versions.

The smallest jar size (hsqldbmain.jar) contains only the database
and JDBC support. The default size (hsqldb.jar) also contains the
utilities. The largest size (hsqldbtest.jar) includes some test
classes as well. You need the JUnit jar in the /lib directory in
order to build and run the test classes.

A Jar file for HSQLDB is provided in the .zip package. This jar
contains both the database and the utilities and has been built
with JDK 1.3.1.

If you want to run HSQLDB with JRE version 1.1.x you should rebuild
the jar.

The preferred method of rebuilding the jar is with Ant. After
installing Ant on your system use the following command from the
/build directory:

ant

The command displays a list of different options for building 
different sizes of the HSQLDB Jar. The default is built using:

ant jar

The Ant method always builds a Jar that is compatible with the
JDK that is used by Ant and specified in the JAVA_HOME environment
variable.

Before building the hsqldbtest.jar package, you should download the
junit.jar and put it in the /lib directory, alongside servlet.jar, 
which is included in the .zip packabe.

Batch Build

A set of MSDOS batch files is also provided. These produce only
the default jar size. The path and classpath variables for the JDK
should of course be set before running any of the batch files.

If you are compiling for JDK's other than 1.2.x or 1.3.x, you should
use the appropriate switchtoJDK11.bat or switchtoJDK14.bat to adapt
the source files to the target JDK before running the appropriate
buildJDK11.bat or buildJDK14.bat

JDK and JRE versions

The JDK used for building the jar should generally be the same series
as the target. This is because the java.sql package in each version
(1.1.x 1.2.x 1.3.x 1.4.x) has a different set of methods
in its public interfaces.

Javadoc can be built with Ant and batch files.

fredt@users
