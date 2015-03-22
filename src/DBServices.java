import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.query.ResultSet;

import java.util.*;
import java.sql.*;

class DBServices {
    
    static private Hashtable connections;
    static private int nbreq = 0;
    // URL for a connection to an ORACLE database
    
    public static void init(String wurl, String wuser, String wpwd, String wdriver){
        connections=new Hashtable();
        ConnectionDB dictlazy=new ConnectionDB(wdriver,wurl,wuser,wpwd,"DICTLAZY: Bootstrap DB");
        connections.put("DICTLAZY",dictlazy);
        QueryResult Q=execSQL("select connectid,driver,url,userdb,pwddb from lazy_connects", true);
        if (Q.valid) {
            
            try {
                int nbRes=0;
                while (Q.result.next()) {
                    nbRes++;
                    String k=Q.result.getString(1);
                    String d=Q.result.getString(2);
                    String u=Q.result.getString(3);
                    String n=Q.result.getString(4);
                    String p=Secure.decryptpwd(Q.result.getString(5));
                    ConnectionDB con=new ConnectionDB(d,u,n,p,k);
                    connections.put(k,con);
                }
                Q.result.close();
            } // try
            catch (SQLException e) {
                System.out.println("DBServices: (Get) during initializition of DB connections SQLError: " + e.getMessage());
            }
        }
        else System.out.println("DBServices: (Query) during initializition of DB connections SQLError: " + Q.msg);
    }
	
    public static String isOK(String db) {
        QueryResult q = execSQLonDB("select count(*) from dual", db, true);
        if (q.valid) return "up"; else return "down";
    }

    public static String reInit() {		
          ConnectionDB dict=(ConnectionDB) connections.get("DICTLAZY");
          init(dict.url, dict.user, dict.pwd, dict.driver);
          return "DBServices re-initialized";
    }
                /*
		if (cdb == null) return "KO";
		if (cdb.con == null) return "KO";
		boolean r = true;
		try { r = ! cdb.con.isClosed(); }
		catch (SQLException e) { r = true; }
		return r ? "OK" : "KO" ;
		*/


    public static String reConnect(String db) {
        ConnectionDB cdb=(ConnectionDB) connections.get(db);
		if (cdb != null) {
		    cdb.reConnect();
		}
		return "";
	}
	
    public static void finish() {
        connections=null; // force finalize of connectionDB objects
    }
    
    public  static QueryResult execSQL(String sql, boolean select) {
        return execSQLonDB(sql,"DICTLAZY",select);
    }

    public static ResultSet execSparql(String q, String endPoint)
    {
        System.out.println("sparql query == " + q);
        //String remoteAddress = "http://wifo5-04.informatik.uni-mannheim.de/drugbank/sparql";
        QueryExecution qe = QueryExecutionFactory.sparqlService(endPoint, q);
        com.hp.hpl.jena.query.ResultSet result = qe.execSelect();
        return result;
    }

    public  static QueryResult execSQLonDB(String sql,String db, boolean select) {
        ConnectionDB CDB=(ConnectionDB) connections.get(db);
        ++nbreq;
        // increase opencursor in ORACLE !!!
        QueryResult Q = new QueryResult(sql);
        try {
            if ((nbreq % 100) == 0)
                if (ns.verbose) System.out.println("DBServices: nbreq" + nbreq);
            if (CDB.con == null) {
                System.out.println("DBServices: error in connection after" + nbreq);
                CDB.con = DriverManager.getConnection(CDB.url, CDB.user, CDB.pwd);
                Q.stmt = CDB.con.createStatement();
                Q.stmt.setEscapeProcessing(false);
            }
            else {
                Q.stmt = CDB.con.createStatement();
                Q.stmt.setEscapeProcessing(false);
            }
            if (ns.verbose)
                if (ns.verbose) System.out.println("DBServices: Executing sql ..." + sql);
            if (select)
                Q.result = Q.stmt.executeQuery(Q.sql);
            else {
                Q.nbUpdated = Q.stmt.executeUpdate(Q.sql);
            }
        }
        catch (SQLException e) {
            System.out.println("DBServices: SQLError: " + e.getMessage());
            System.out.println("DBServices: SQL: " + Q.sql);
            Q.valid = false;
            Q.msg = e.getMessage();
         /*  only if bugs !!
         try {
            con.close();
            System.out.println("DBServices: close DB OK");
         }
         catch (java.sql.SQLException e2) {
            System.out.println("DBServices: failed to close DB");
            System.out.println(e2.toString());
         }
         con = null;
          */
        }
        return Q;
    }
}