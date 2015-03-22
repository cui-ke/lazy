import java.sql.*;

class ResetCache{
    
    public static String ResetNodesDependencies(){
        String log="";
        try {
            String sqlModify="delete from lazy_nodedep";
            QueryResult Q1 = DBServices.execSQL(sqlModify, false);
            
            if (Q1.valid) {log+= "<b>ResetNodesDependencies:" + Q1.nbUpdated + " rows deleted</b><p/>\n";}
            else {log+="<b>ResetNodesDependencies:  error in delete </b><p/>\n"; return log;}
            
            String sqlRequest="select projectid, name, upper(collection) from lazy_nodes";
            QueryResult Q = DBServices.execSQL(sqlRequest, true);
            
            if (Q.valid) {
                int nbRes=0;
                while (Q.result.next()) {
                    nbRes++;
                    String project=Q.result.getString(1);
                    String name=Q.result.getString(2);
                    String collection=Q.result.getString(3);
                    if (collection.indexOf(",")!=-1){
                        while (collection.indexOf(",")!=-1){
                            int ix=collection.indexOf(",");
                            String table=collection.substring(0,ix);
                            collection=collection.substring(ix+1);
                            int ispace=table.indexOf(" ");
                            if (ispace!=-1) table=table.substring(0,ispace);
                            addDependency(project,name,table,log);
                        }
                    }
                    addDependency(project,name,collection,log); // for the last (or the only one!)
                }
                log+="<b>ResetNodesDependencies:  OK END </b><p/>\n";
                Q.result.close();
            }
            else{log+="<b>ResetNodesDependencies:  error in selec </b><p/>\n"; return log;}
        } // try
        catch (SQLException e) {
            log+="<b>ResetNodesDependencies:  SQLError: " + e.getMessage()+"</b><p/>\n"; return log;}
        return log;
    }
    
    static String addDependency(String project, String name, String table, String log){
        if (ns.verboseClearCache) System.out.println(project+","+name+","+table);
        String sqlModify="insert into lazy_nodedep values("+
        "'"+project+"',"+
        "'"+name+"',"+
        "'"+table+"')";
        QueryResult Q2 = DBServices.execSQL(sqlModify, false);
        if (Q2.valid) {}
        else {log+="<b>ResetNodesDependencies:  error in insert "
        +"<hr/><h3>Database access error</h3>"
        + "<p>during:<b>insert</b> execution</p>"
        + "<p>SQL text <b>"
        + Q2.sql
        + "</b></p>"
        + "<p>SQLError: "
        + Q2.msg
        + "</p>";}
        return log;
    }
    
    
}