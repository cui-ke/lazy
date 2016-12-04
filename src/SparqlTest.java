import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.update.*;

import java.util.*;
import java.sql.*;

import org.openrdf.repository.*;

class SparqlTest {
    

    public static void execSparql(String q, String endPoint)
    {
        System.out.println("sparql query == " + q);
        //String remoteAddress = "http://wifo5-04.informatik.uni-mannheim.de/drugbank/sparql";
        //QueryExecution qe = QueryExecutionFactory.sparqlService(endPoint, q);
        
        UpdateRequest ur = new UpdateRequest();
        ur.add(q);
        UpdateProcessor u =	UpdateExecutionFactory.createRemote(ur, endPoint);
        u.execute();
        
        // com.hp.hpl.jena.query.ResultSet result = qe.execSelect();
        
    }
    
    public static void main(String[] args) {
       execSparql(args[1], args[0]);
       System.out.println("done");
    }

/*
    public void temporalsendRIG(RepositoryConnection con) {
        ArrayList<String> userInputArrayList = new ArrayList<String>();
        populateArrayList(userInputArrayList);
        try {
            con = makeConnection(RemoteServer_url, Repository_id);
            String updateQuery = buildUpdateQuery(userInputArrayList);
            System.out.println(updateQuery);
            Update update = con.prepareUpdate(QueryLanguage.SPARQL,
                        updateQuery);
                        update.execute(); 


        } catch (MalformedQueryException e) {
            e.printStackTrace();
        }catch (RepositoryException e) {
            e.printStackTrace();
        }catch(UpdateExecutionException e){
            e.printStackTrace();
        }

    }
*/
    
    public String buildUpdateQuery(ArrayList<String> arr){
    String updateQuery = 
        "PREFIX owl:<http://www.w3.org/2002/07/owl#> \n" + 
        "PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
        "DELETE \n" +
        "{ \n" +
             "?s ?p ?o.\n" +

            "}\n" +
            "INSERT \n" +
            "{\n" +
            "?s \n" +
                    "<http://localhost:9090/reservationService/onto/Reservation/bookerFirstName>" + "\"" + arr.get(0) + "\" ; \n"  +
                    "<http://localhost:9090/reservationService/onto/Reservation/cityOfInterest>" + "\"" + arr.get(1) + "\" ; \n" +
                    "<http://localhost:9090/reservationService/onto/Reservation/distanceToCityOfInterest>"  + "\"" + arr.get(2) + "\" ;\n" +
                    "<http://localhost:9090/reservationService/onto/Reservation/maximumShift>" + "\"" + arr.get(3) + "\" ; \n"  +
                    "<http://localhost:9090/reservationService/onto/Reservation/requiredAmountOfDays>" + "\"" + arr.get(4) + "\" ; \n" +
                    "<http://localhost:9090/reservationService/onto/Reservation/requiredBedrooms>" + "\"" + arr.get(5) + "\" ; \n" +
                    "<http://localhost:9090/reservationService/onto/Reservation/requiredDistanceToLake>"  + "\"" + arr.get(6) + "\" ; \n" +
                    "<http://localhost:9090/reservationService/onto/Reservation/requiredOccupacy>"  + "\"" + arr.get(7) + "\" ; \n" +
                    "<http://localhost:9090/reservationService/onto/Reservation/startingBookingDay>" + "\"" + arr.get(8) + "\" . \n" +

            "} \n" +
            "WHERE \n" +
            "{ \n" +
                " ?s rdf:type <http://sswapmeet.sswap.info/sswap/Subject>. \n" +
                " ?s ?p ?o. \n" +
                " FILTER NOT EXISTS  { ?s rdf:type  ?o  } \n" +
                 "FILTER NOT EXISTS  { ?s  <http://sswapmeet.sswap.info/sswap/mapsTo>  ?o  } \n" +
            "} \n"
             ;


            return updateQuery;
}
 
}