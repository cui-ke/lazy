/*
  Compile and run instructions
  
$CP = '.'

# ADD Jena lib
for j in ${JENA_LIB}/*.jar ; do
   CP=${CP}:$j
done

javac TestJenaUpdate.java -classpath ${CP} 

java  -classpath ${CP}  TestJenaUpdate

*/

import com.hp.hpl.jena.update.*;


import java.util.*;



class TestJenaUpdate {
    
    static String sesameServer = "http://kr.unige.ch:8080/openrdf-sesame/repositories/H/statements"; 
    
    public static void main(String[] args) {
      
      try {
         UpdateRequest urq = new UpdateRequest().add(
              "insert data {<http://a.com/bbb> <http://a.com/p00> <http://a.com/6666666666>}; insert data {<http://a.com/bbb> <http://a.com/p00> \"yoyoyoyoyoyoyoyoyo\"@fr }; " );
         System.out.println("new UpdateRequest().add done");
         String ep = sesameServer;
       
         UpdateProcessor u = 	UpdateExecutionFactory.createRemoteForm(urq, ep);
         System.out.println("UpdateExecutionFactory.createRemoteForm done");
         u.execute();
     } catch (com.hp.hpl.jena.query.QueryParseException e) {
         System.out.println("---err---  "+e.getMessage());
     }
      
      System.out.println("done");
    }


 
}