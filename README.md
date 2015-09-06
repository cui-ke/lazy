
# Lazy for the Semantic Web 

The Lazy documentation, including the installation instructions, is in
the doc directory

This distribution must be installed as a servelet in Appache Tomcat. The
lazy directory must be placed in the webapps directory of the Tomcat
server.

This distribution includes a version of the HSQL database engine
(http://hsqldb.sourceforge.net), read the licence in
hsqldb/hsqldb_lic.txt

It also includes the JENA library, read the licence at https://jena.apache.org/ .

Although Lazy is distributed with HSQLDB, which is a small and nice 100%
Java database engine, it has been tested with Oracle, Access, and
Postgres. It should work with any relational database system accessible
through a JDBC driver.

## Content of this distribution


`bin`        scripts to start and manage the Lazy system

`doc`        documentation, including the installation guide

`examples`   example hyperspaces

`hsqldb`     the hsqldb database engine (http://hsqldb.sourceforge.net/ )
`hsqldb/demo`	 contains the default lazy dictionary (node definitions, etc.) in
             the test database (test.data, test.script, test.properties)

`src`        the Lazy source code

`src/admin`  source code of the Lazy interactive development and environment (written
           in Lazy)

`WEB-INF`    all the servelet stuff
`WEB-INF/lib`  all the JENA JAR files

`css`        css files used by the lazy IDE


## Installation

- install a distribution of the Apache Tomcat servelet container

- define the `CATALINA_HOME` environment variable to point to the Tomcat main directory, 
  e.g. put the command
  
  `export CATALINA_HOME=yourTomcatDirectory`
  
  into your `~/.profile` file

- extract the content of the lazy distrinbution file into the `$CATALINA_HOME/webapps` 
  directory. It should create a directory called lazy

- open a terminal,change directory to `$CATALINA_HOME/webapps/lazy/bin` and execute 

    `source lazyenv.sh`    ### to define environment variables

    `./runServer.sh`       ### to start the Hsqldb database server 
                         ### chmod a+x runServer.sh if necessary
                         
- open another terminal,change directory to `$CATALINA_HOME/webapps/lazy/bin` and execute 

    `source lazyenv.sh`    ### to define environment variables
 
    `./startns.sh`         ### to start the Tomcat server
    
    (use the Lazy system ...)
    
   `./stopns.sh`           ### to stop the Tomcat server
    
    
- open the URL `localhost:8080/lazy` from a web browser

     
     
     

G. Falquet, 04.08.2015
