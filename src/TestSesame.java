//import org.openrdf.repository.Repository;
//import org.openrdf.repository.*;
//import org.openrdf.repository.http.HTTPRepository;

//import org.openrdf.repository.sparql.*;
//import org.openrdf.repository.*;

import org.openrdf.query.*;
import org.openrdf.repository.*;
import org.openrdf.repository.sparql.*;
import org.openrdf.model.*;

import java.util.*;
/*
import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.sparql.syntax.*;
import com.hp.hpl.jena.sparql.lang.*;

import org.apache.jena.updata.*;
*/

import java.net.*;

public class TestSesame {

   //static UpdateExecutionFactory f ;

   static String sesameServer = "http://kr.unige.ch:8080/openrdf-sesame/";
   static String repositoryID = "H";
   static String hrep = "http://127.0.0.1:8080/openrdf-sesame/repositories/H/statements";
   static String dbp = "http://dbpedia.org/sparql";

   public static void main(String[] args) {
   
//      ClassLoader classLoader = TestSesame.class.getClassLoader();
//      URL resource = classLoader.getResource("org/apache/http/message/BasicLineFormatter.class");
//      System.out.println(resource);
      
      String updateQuery = "insert data {<http://a.com/bbb> <http://a.com/p00> <http://a.com/9211>}; insert data {<http://a.com/bbb> <http://a.com/p00> \"hophop\"@fr }; ";
      
      // Repository repo = new HTTPRepository(sesameServer, repositoryID); 
      Repository repo = new SPARQLRepository(sesameServer+"repositories/"+repositoryID,  sesameServer+"repositories/"+repositoryID+"/statements"); 
      // Repository repo = new SPARQLRepository("http://dbpedia.org/sparql");
   
      repo.initialize();
      
      RepositoryConnection rc = repo.getConnection();
      
      //if (repo.isWritable()) {
          Update update = rc.prepareUpdate(QueryLanguage.SPARQL,
                        updateQuery);
          update.execute(); 
      
          System.out.println("--- update executed");
      //} else {
      //    System.out.println("--- not writable");
      //}
      
      String queryString = "SELECT ?x ?y WHERE { ?x <http://a.com/p00>  ?y } ";
      TupleQuery tupleQuery = rc.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
      
      // TupleQueryResult result = tupleQuery.evaluate(); 
        try (TupleQueryResult result = tupleQuery.evaluate()) {
           List<String> names = result.getBindingNames();
           System.out.println(names);
           while (result.hasNext()) {  // iterate over the result
	           BindingSet bindingSet = result.next();
	           Value valueOfX = bindingSet.getValue("x");
	           Value valueOfY = bindingSet.getValue("y");
	           String yt = valueOfY instanceof IRI ? " IRI " : " ---" ;
	           yt = valueOfY instanceof Literal ? " Lit " : yt;

	       // do something interesting with the values here...
	       System.out.println(" -- "+valueOfX+ " "+valueOfY+" "+yt);
	       
          }
          System.out.println("-- select completed");
        }  
      repo.shutDown();
      System.out.println("-- shutdown completed");
                        
   }
}