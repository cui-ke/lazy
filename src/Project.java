import java.util.*;

/***********
  *  
  *  Project manager
  *
  *  Load project definitions on demand and keep them in a Map
  *
  */
  
class Project {
    String projectid, nodetype, dbconnection, xslurl, cssurl, bkgndurl;
	
	static TreeMap projects = new TreeMap();
	
	static Project get(String pname) {
	   Project p = (Project)projects.get(pname);
	   if (p == null) { loadProjDef(pname); p = (Project)projects.get(pname); }
	   return p;
	}
	
	static void clearProjDef(String id) {
	   projects.remove(id);
	}
	
	static void clearAll() {
		projects = new TreeMap();
	}
	
	static void loadProjDef(String id) {
	  String query = 
	  "select nodetype, dbconnection, xslurl, cssurl, bkgndurl" +
	  " from lazy_projects" +
	  " where projectid = '" + id + "'";
	   QueryResult Q = DBServices.execSQL(query, true);
       int nbRes=0;
       if (Q.valid) {
            try {
                while (Q.result.next()) {
				    Project p = new Project();
					p.projectid = id; p.nodetype = Q.result.getString("nodetype"); 
					p.dbconnection = Q.result.getString("dbconnection");
					p.xslurl = Q.result.getString("xslurl"); 
					p.cssurl = Q.result.getString("cssurl"); 
					p.bkgndurl = Q.result.getString("bkgndurl"); 
					projects.put(id, p);
                    nbRes++;
                 }
           } // try
            catch (Exception e) {
                System.out.println("ns: Project.loadProjDef SQLError: " + e.getMessage());
           }
		   if (nbRes == 0)  System.out.println("ns: Project.loadProjDef SQLError: not found : "+id);
		   
        }else System.out.println("ns: Project.loadProjDef (2) SQLError:  " + Q.msg);  
	}

   public static String getNodeType(String pname) {
		Project p = get(pname);
		if (p != null)  return p.nodetype; else  return "html";  // use HTML as a default when project was not found
    }
   
   public static String getXSLFileName(String pname){
		Project p = get(pname);
		if (p != null)  return p.xslurl; else  return "not-found.xsl";
    }

    public static String getCSSFileName(String pname){
		Project p = get(pname); 
		if (p != null)    return p.cssurl;   else  return "not-found.css";        
    }
	
    public static String getBCKGNDFileName(String pname){
		Project p = get(pname);
		if (p != null)   return p.bkgndurl; else  return "not-found.gif";
   }
   
   public static String getDBConnection(String pname) {
 		Project p = get(pname);
		if (p != null)   return p.dbconnection; else  return null; // "connection-not-found";
   }
  
    	
}