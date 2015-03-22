import com.hp.hpl.jena.query.*;

import java.util.*;

import com.hp.hpl.jena.sparql.syntax.*;

import com.hp.hpl.jena.sparql.lang.*;

import com.hp.hpl.jena.query.*; 

public class tsp{

 public static void main(String[] a) {

   // Element e = ParserSPARQL11.parseElement(a[0]);
   
   try {
       Query e = QueryFactory.create(a[0]);
       System.out.println(e);
   } catch (com.hp.hpl.jena.query.QueryParseException e) {
       System.out.println(e.getMessage());
   }
   
 }
}
