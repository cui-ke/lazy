import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.*;

import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.sql.*;

/*
  Revision history
  
  2004-09-01 
  
  getNodeDefinition
	 If the node contains variables other than GRP, USER, LANG, STYLE, set its cachesize to 0
	 This is a rough handling of variables, a more subtle caching policy would detect variable assignments
	 and remove only the concerned nodes from the cache.
  
*/

/**
* Node manager
* <p>
* - loads node definitions from the Lazy dictionary
* <p>
* - manages the definition and node (instance) cache
* <p>
* <h4>CURRENT LIMITATIONS<h4>
* <p>- does not take into account the view definitions when computing the table --> node dependencies
* <p>- should consider {db-connection}.{table} instead of {table}
*
*/
class Node {
    
    //static private Hashtable nodeDefCache=new Hashtable();
	
    static private TreeMapCache nodeDefCache = new TreeMapCache(ns.NODE_HISTORY, "NODE_DEF");
    
	static Hashtable Text = initText() ;
	
	static Map DBObjectDependencies = new TreeMap();  // for each DB object (table/view) keeps the set of dependant nodes
	


    boolean valid = true;
    boolean NOinclude = true;
    int nbparam;
    String projectid, nodeName;
    String pre, items, post; // content to display pre=heading, items=for each selected result, post=footer
    String collection; // tables or graphs to query
    String selector; // for SQL nodes: the WHERE part, 
                     // for SPARQL nodes: the SELECT .... WHERE parts
    String order, groupby, limit;
    String nodetype; // SPARQL (or NODEF)
	String[] preActions;
    String preSQL, itemsSQL, postSQL;
    String msg = "";
    private TreeMapCache Cache;
    private int cachesize=-1;
    
    Node() {
    }
    Node(String wnodeName) {
        nodeName = wnodeName;
    }
    
    public static String getNodeType(String nodename){
		return Project.getNodeType(getProjectName(nodename));
    }
    public static String getXSLFileName(String nodename){
		return Project.getXSLFileName(getProjectName(nodename));
    }
    public static String getCSSFileName(String nodename){
		return Project.getCSSFileName(getProjectName(nodename));      
    }
    public static String getBCKGNDFileName(String nodename){
		return Project.getBCKGNDFileName(getProjectName(nodename));  
    }
    
	/**
	* Load the localized string definitions (called text)
	*
	* TODO: remove dependencies on French
	*
	*/
    public static Hashtable initText(){
        Hashtable ht= new Hashtable();

		String sqlRequest="select projectid,lang, txtid,lib from lazy_txtlang union " +
                                     "select projectid, '*', txtid,lib from lazy_txt" ; 
									 
        QueryResult Q = DBServices.execSQL(sqlRequest, true);
        
        if (Q.valid) {
            
            try {
                int nbRes=0;
                while (Q.result.next()) {
                    nbRes++;
                    String k=Q.result.getString(1)+","+Q.result.getString(2)+","+Q.result.getString(3);// prj, lang, key
                    String v=Q.result.getString(4);
                    if (ns.verboseCache) System.out.println("load Text :"+k+"=>"+v);
                    ht.put(k,v);//
                }
                if (ns.verboseCache) System.out.print("load Text in hashtable nb: "+nbRes);
                Q.result.close();
            } // try
            catch (SQLException e) {
                System.out.println("Nodes: during initText() SQLError: " + e.getMessage());
            }
        }
        else System.out.println("Nodes: during initText() SQLError: " + Q.msg);
        return ht;
    }
    
    
    
    public static String getProjectName(String s){
        int ix = s.indexOf(".");
        if (ix==-1) {System.out.println("Node.getProjectName: error in node name " + s); return "***ERROR***";}
        return s.substring(0,ix);
    }
    
    
    public static String getNodeName(String s){
        int ix = s.indexOf(".");
        if (ix==-1) {System.out.println("Node.getNodeName error in node name " + s); return "***ERROR***";}
        return s.substring(ix+1,s.length());
    }
    
    public static String  getDynamicInfo(String[] params){
        if (params[2].equals("DEF")){
            return nodeDefCache.getXMLStatistic();
        }
        if (params[2].equals("GLOBAL")){
            return TreeMapCache.getXMLGlobalStatistic();
        }
        if (params[2].equals("PROJECT")){
            Node N = (Node) nodeDefCache.get(params[0]+"."+params[1]); // try to load from cache
            if (N != null) { // already in the cache
                return N.Cache.getXMLStatistic();
            }
            else return "<CELL>no statistic</CELL>"+
            "<CELL></CELL>"+
            "<CELL></CELL>"+
            "<CELL></CELL>"+
            "<CELL></CELL>"+
            "<CELL></CELL>"+
            "<CELL></CELL>";
        }
        return "<CELL>error in parameter</CELL>"+
        "<CELL></CELL>"+
        "<CELL></CELL>"+
        "<CELL></CELL>"+
        "<CELL></CELL>"+
        "<CELL></CELL>"+
        "<CELL></CELL>";
    }
    
	/**
	* Obtain the definition of a node identified by its projectname.nodename
	*/
    public static Node getNodeDefinition(String nodeName) {

        Node N = (Node) nodeDefCache.get(nodeName); // try to load from cache
        
        if (N != null) { // already in the cache
            if (ns.verboseCache)
                System.out.println(
                "Nodes: loading definition from CACHE for node: " + N.nodeName);
            return N;
        }
        
        // not in cache must be loaded from DB
        long starttime=System.currentTimeMillis();
        N = new Node(nodeName);
        
        if (ns.verboseCache)
            System.out.println("Nodes: loading definition from BD for node: " + N.nodeName);
        String projectpart=getProjectName(N.nodeName);
        String nodepart=getNodeName(N.nodeName);
        if (ns.verboseCache) 
        	System.out.println("Project: "+projectpart +" Node: " + nodepart);

        System.out.println("Project: "+projectpart +" Node: " + nodepart  + " full node name == "+N.nodeName);
        try {
            QueryResult Q =
            DBServices.execSQL(
            "select projectid,nbparam, pre, items, post, collection, selector, groupby, ordering, limit, cachesize,nodetype"
            + " from lazy_nodes where projectid = '"+ projectpart+"' and name = '"+ nodepart+ "'",
            true);

            if(!Q.valid)
            {
                N.valid = false;
                N.msg = "<hr/><h3>Database access error when loading node definition</h3>";
                N.msg += "<p>Unable to connect to the Lazy dictionary</p>";
                N.msg += "<p>Trying to re-connect ... "+
                        DBServices.reConnect("DICTLAZY")+DBServices.isOK("DICTLAZY")+"</p>";
                return N;
            }
            else if(!Q.result.next())
            {
                N.valid = false;
                N.msg = "<hr/><h3>Database access error when loading node definition</h3>";
                N.msg += ("<p>(the requested node:<b>"+ N.nodeName+ "</b> may not exist)</p>");
                return N;
            }

            N.projectid = Q.result.getString("projectid");
            N.nbparam = Q.result.getInt("nbparam");
            N.pre = Q.result.getString("pre");
            N.items = Q.result.getString("items");
            N.post = Q.result.getString("post");
            N.collection = Q.result.getString("collection");
            N.selector = Q.result.getString("selector");
            N.nodetype = Q.result.getString("nodetype");

            if (!(N.selector.equals("NODEF"))) {
                if(!N.nodetype.equals("SPARQL")) 
                    N.selector = " where " + N.selector; // selected by could be optional jg
            }else
                N.selector = "";
            
            N.groupby = Q.result.getString("groupby");
            if (!(N.groupby.equals("NODEF")) && !N.nodetype.equals("SPARQL"))
                N.groupby = " group by " + N.groupby; // group by could be optional jg
            else
                N.groupby = "";
            
            N.order = Q.result.getString("ordering");
            if (!(N.order.equals("NODEF")) && !N.nodetype.equals("SPARQL"))
                N.order = " order by " + N.order; // order by could be optional jg
            else
                N.order = "";
            
            N.limit = Q.result.getString("limit");
            if (N.limit.equals("NODEF")) N.limit = "";
            
            N.cachesize = Q.result.getInt("cachesize");
            if (N.cachesize==-1) N.cachesize=ns.NODE_REQ_HISTORY;
            
			/* If the node contains variables other than GRP, USER, LANG, STYLE, set its cachesize to 0
			* This is a rough handling of variables, a more subtle caching policy would detect variable assignments
			* and remove only the concerned nodes from the cache.
			*/
			String findIn = N.pre+N.items+N.post+N.selector+N.groupby+N.order;
			boolean foundVar = false;
			int x1 = findIn.indexOf(ns.varPrefix);
			while (!foundVar & x1 > -1) {
				int x2 = findIn.indexOf(ns.varSuffix, x1);
				String varName = findIn.substring(x1+ns.varPrefix.length(), x2);
				if (! (varName.equals("USER") || varName.equals("GRP") || varName.equals("LANG") || varName.equals("STYLE") ) )
					foundVar = true;
				else
					x1 = findIn.indexOf(ns.varPrefix, x2);
			}
			if (foundVar) {
				N.cachesize = 0;
				if (ns.verboseCache)
           			System.out.println("Nodes: ...... set cachesize to 0 because of [variables] ");
			}
			
            Q.result.close();
            // System.out.println("Nodes: Definition of node " + N.nodeName + " loaded");
            // build sql queries


            if (N.nodetype.equals("SPARQL")) // if this is a sparql node
            {  // temporary implementation, check if sparql node
                if (N.pre.equals("''"))
                    N.preSQL = "";
                else
                   // N.preSQL = N.pre + " && " + N.order.substring(10) + " && "  + N.selector;
                    
                    N.preSQL = N.selector;

                if (N.post.equals("''"))
                    N.postSQL = "";
                else
                    N.postSQL =  N.selector;

                 if (N.items.equals("''"))
                    N.itemsSQL = "";
                else
                    N.itemsSQL =  N.selector + " \n" + N.groupby + " \n" + N.order;

            }
            else // it is a SQL node
            {
                if (N.pre.equals("''"))
                    N.preSQL = "";
                else
                    N.preSQL = "select 1," + N.pre + " FROM " + N.collection + N.selector;

                if (N.post.equals("''"))
                    N.postSQL = "";
                else
                    N.postSQL = "select 1," + N.post + " FROM " + N.collection + N.selector;

                N.itemsSQL = "select " + N.items + " FROM " + N.collection + N.selector +N.groupby + N.order;
            }

            
            N.NOinclude =
            (N.pre.indexOf(ns.includePrefix) == -1)
            & (N.items.indexOf(ns.includePrefix) == -1)
            & (N.post.indexOf(ns.includePrefix) == -1);
            if (ns.verbose)
                System.out.println("Nodes: no include : " + N.NOinclude);
            N.Cache = new TreeMapCache(N.cachesize, "NODE_REQ_" + nodeName);
			
			/*
			 * Find DB table/view dependencies
			 */
			String collection = N.collection;
			
			while (! collection.equals("")) {
                String table = collection;
				int ix=collection.indexOf(",");				
				if (ix == -1) {collection="";}
                else {table=collection.substring(0,ix); collection=collection.substring(ix+1); }
                int ispace=table.lastIndexOf(" ");
                if (ispace!=-1) table=table.substring(0,ispace).trim();
				
				Set depNodes = (Set) DBObjectDependencies.get(table);
				if (depNodes == null) 
				{ 
					depNodes = new HashSet(); 
					DBObjectDependencies.put(table, depNodes); 
				}
				depNodes.add(nodeName);
				/* DBG */ System.out.println(
			  "getNodeDefinition:   added dependency "+table+" --> "+DBObjectDependencies.get(table));
				
            }
            
        }
        catch (SQLException e) {
            System.out.println("Nodes: SQLError: " + e.getMessage());
            N.valid = false;
            N.msg = e.getMessage();
        }
        
		// Load pre-actions
		
		try {
			if (ns.verbosePreActions) 
				System.out.println("Node.getNodeDefinition: loading pre-actions for "+nodeName);
            QueryResult qr =
            DBServices.execSQL(
            "select projectid, nodename, seqno, operation"
            + " from lazy_actions where projectid = '"+ projectpart+"' and nodename = '"+ nodepart+ "'"
			+ " order by seqno",
            true);
			    
			// temporary hack, waiting for a true "for each" in Java
			
			String [] tmpAct = new String[100]; // room for 100 actions
			int nbAct = 0;
			
            if (qr.valid) {
				while (qr.result.next()) {
					tmpAct[nbAct] = qr.result.getString("operation");
					if (nbAct < tmpAct.length - 1) nbAct++;
				}
					
            }
			if (nbAct > 0) {
				N.preActions = new String[nbAct];
				for (int i = 0; i < nbAct; i++) {
					N.preActions[i] = tmpAct[i];
					if (ns.verbosePreActions) System.out.println(" "+i+") "+N.preActions[i]);
				}
			}
		}	
		catch (SQLException e) {
                System.out.println("Node.getNodeDefinition: loading of pre-actions SQLError: " + e.getMessage());
        }
        

		
        if (ns.cacheDefinition)
            nodeDefCache.put(nodeName, N,(System.currentTimeMillis() - starttime)); // add to the cache
        return N;
    }
    
    public StringBuffer Actualize(String[] params,HttpSession session) { // get a instance of this node with this parameters
        
		/* THERE IS STILL A BUG HERE :
		     If the node definition contains a session variable [var] the key should include the session number
		*/
        String keyuser ="";
        String sqlall=preSQL+itemsSQL+postSQL; // examine all resquest
        if (sqlall.indexOf("[[USER]]")!=-1 || sqlall.indexOf(ns.varPrefix+"USER"+ns.varSuffix)!=-1)
            keyuser+=session.getAttribute("USER");
        else keyuser+="|";

        if (sqlall.indexOf("[[LANG]]")!=-1 || sqlall.indexOf(ns.varPrefix+"LANG"+ns.varSuffix)!=-1)
            keyuser+=session.getAttribute("LANG");
        else keyuser+="|";

        if (sqlall.indexOf("[[GRP]]")!=-1 || sqlall.indexOf(ns.varPrefix+"GRP"+ns.varSuffix)!=-1)
            keyuser+=session.getAttribute("GRP");
        else keyuser+="|";

        if (sqlall.indexOf("[[STYLE]]")!=-1 || sqlall.indexOf(ns.varPrefix+"STYLE"+ns.varSuffix)!=-1)
            keyuser+=session.getAttribute("STYLE");
        else keyuser+="|";
        
        
        String keyparam = keyuser+SU.concatParameters(params);
        StringBuffer content = (StringBuffer) Cache.get(keyparam);
        // try to load from cache
        
        if (content != null) { // already in the cache
            if (ns.verboseCache)
                System.out.println("Nodes: loading data from CACHE for node: "+ nodeName+ " with param: "+ keyparam);
            return Secure.transformPrivatePart(content);
        }
        
        long starttime=System.currentTimeMillis();
        
        content = new StringBuffer();

		String conID = Project.getDBConnection(projectid);
        if (ns.verboseConnect) System.out.println("For project "+projectid+" found connection: "+conID);
        if (conID==null) {conID="DICTLAZY";}
        
        //System.out.println("Node.Actualize. preSQL >>>>>>> " + preSQL);
        //System.out.println("Node.Actualize. itemsSQL >>>>>>> " + itemsSQL);
        //System.out.println("Node.Actualize. postSQL >>>>>>> " + postSQL);
        //System.out.println("Node.Actulize. session.toString >>>>>> "+session.toString());

        if (nodetype.equals("SPARQL")) {
            doQuerySQ(conID, nodeName, "PRE", preSQL, params, pre, limit, 1, content, session, projectid);
            doQuerySQ(conID, nodeName, "ITEMS", itemsSQL, params, items, limit, 10000, content, session, projectid);
            doQuerySQ(conID, nodeName, "POST", postSQL, params, post, limit, 1, content, session, projectid);

        } else { // SQL
            doQuery(conID, nodeName, "PRE", preSQL, params, 1, 2, content, session, projectid);
            doQuery(conID, nodeName, "ITEMS", itemsSQL, params, 10000, 1, content, session, projectid);
            doQuery(conID, nodeName, "POST", postSQL, params, 1, 2, content, session, projectid);
        }
        
        // System.out.println("Actualize.content >>>>>> ."+content.toString());
        
        if (ns.cacheNodeInstance)
            Cache.put(keyparam, content,(System.currentTimeMillis() - starttime)); // add to the cache
        
        
        return Secure.transformPrivatePart(content);
    }

    /*
     * delete ', ',',' and ''' from node.items for displaying sparql content
     */
    static String formatSparqlDisplayString(String src)
    {
        String result = src.replace(", ","");
        result = result.replace(",","");
        result = result.replace("'","");
        return result;
    }

	/**
	* This is the main function of the node server :
	*
	* query the database to get the (first level) content of a node
	*
	* TODO: change the variable substitution process for SPARQL queries
	*
	*/
    static void doQuery(String conID, String nodeName, String nodeExecute, String sqlRequest,
    					String[] params, int maxRes, int firstCol, StringBuffer content,
    					HttpSession session, String projectid) {
        
        if (!sqlRequest.equals("")) {
           // System.out.println("> sqlRequest='"+sqlRequest+"'");
            StringBuffer resb = new StringBuffer();

            sqlRequest = SU.replaceParameters(sqlRequest, params, SU.SQL_QUOTES);
            sqlRequest = SU.replaceSystemParameters(sqlRequest,session,Text,projectid, SU.SQL_QUOTES);

			QueryResult Q = DBServices.execSQLonDB(sqlRequest, conID, true);
			//System.out.print("> executed "); ////

			int count = 0;
			if (Q.valid) {
				try {
					ResultSetMetaData resultSchema = Q.result.getMetaData();
					int nbCol = resultSchema.getColumnCount();

					//System.out.println("> meta ");   ////
					int nbRes = 0;
					while (nbRes < maxRes && Q.result.next()) {

						//System.out.print("> row ");  ////
						nbRes++;
						resb.setLength(0); // clear resb
						for (int i = firstCol; i <= nbCol; i++) {
							String cs = Q.result.getString(i);
							//System.out.print("> col ");
							if (!Q.result.wasNull())
								resb.append(cs);
						}

						content.append(resb.toString());
					}
					//System.out.println("> end rows");
					Q.result.close();
				} // try
				catch (SQLException e) {
					System.out.println("Nodes: SQLError: " + e.getMessage());
					content = new StringBuffer();
					content.append("<hr/><h3>Database access error</h3>");
					content.append("<p>Error in node conversion:<b>" + nodeName + "</b></p>");
					content.append("<p>during:<b>" + nodeExecute + "</b> part</p>");
					content.append("<p>SQL text<b>" + sqlRequest + "</b></p>");
					content.append("<p>SQLError: " + e.getMessage() + "</p>");
				}

			} else { // sql error
				content.append("<hr/><h3>Database access error</h3>");
				content.append("<p>Error in node execution:<b>" + nodeName + "</b></p>");
				content.append("<p>during:<b>" + nodeExecute + "</b> part</p>");
				// content.append("<p>SQL text <b>" + sqlRequest + "</b></p>");
				content.append("<p>SQLError: " + Q.msg + "</p>");
			}
        } // if not empty
    }
	static void doQuerySQ(String conID, String nodeName, String nodeExecute, String sparqlRequest,
    					String[] params, String nodePart, String limitPart, int maxRes, StringBuffer content,
    					HttpSession session, String projectid) {
        
        if (!sparqlRequest.equals("")) {
           // System.out.println("> sqlRequest='"+sqlRequest+"'");
            StringBuffer resb = new StringBuffer();
            
            // no quotes for the parametrs in the sparql query
            sparqlRequest = SU.replaceParameters(sparqlRequest, params, SU.NOQUOTE);
            // quotes for the system parameters, session attributes, etc.
            sparqlRequest = SU.replaceSystemParameters(sparqlRequest,session,Text,projectid, SU.SPARQL_QUOTES);
            // no quotes in the limit part
            limitPart = SU.replaceParameters(limitPart, params, SU.NOQUOTE);
            // neither for the system parameters
            limitPart = SU.replaceSystemParameters(limitPart,session,Text,projectid, SU.NOQUOTE);
            // the limit and offset values must be sequences of digits
            // remove the quotes and type indicator for integers
            limitPart = limitPart.replaceAll("\"([0-9]+)\"(\\^\\^[^ ]*)?","$1");
            // relplace by 1 for any other value
            limitPart = limitPart.replaceAll("\"(.*)\"(\\^\\^[^ ]*)?","1");
           
            // replace without quoting for the part that is not executed
            nodePart = SU.replaceParameters(nodePart, params, SU.NOQUOTE);
            nodePart = SU.replaceSystemParameters(nodePart,session,Text,projectid, SU.NOQUOTE);

			String prefixString = getProjectPrefix(projectid);
			String queryString = prefixString +" \n" + sparqlRequest + " \n" + limitPart;

			com.hp.hpl.jena.query.ResultSet result = DBServices.execSparql(queryString, Project.getDBConnection(projectid));
			int nbRes = 0;
			while(nbRes<maxRes && result.hasNext()) 
			{
				QuerySolution row = result.next();
				Iterator<String> varIterator = row.varNames();
				String display = nodePart;
				while(varIterator.hasNext())
				{
					String var = varIterator.next();
					String replaceStr =  ns.sparqlVarPrefix  + var + ns.varSuffix;
                    String replaceOutStr = ns.sparqlOutVarPrefix  + var + ns.varSuffix;
                    
                    String replaceVal = "",  replaceOutVal = "";
					RDFNode val = row.get(var);
					if (val.isLiteral())  {
					    replaceVal = "\""+(val.asLiteral().getLexicalForm().replace("\"","\\\"")) + "\"";
                        if (val.asLiteral().getDatatypeURI() != null)              
                            replaceVal += "^^"+normalizeURI(val.asLiteral().getDatatypeURI(), prefixString) ;
                        replaceOutVal = val.asLiteral().getLexicalForm();
                    }
					else if (val.isResource()) {
					    if (val.isURIResource()) {
					        replaceVal = normalizeURI(val.asResource().toString(), prefixString); 
					        System.out.println("TEST -------- "+val.asResource().getNameSpace()+":"+val.asResource().getLocalName());
					        replaceOutVal = replaceVal;
					    }
					    else { // blank node (anonymous resource)
					        replaceVal = "_:" + val.asResource().getId().toString();
					        replaceOutVal = replaceVal;
					    }
					}
					//String replaceOutVal = beautifyDisplay(row.get(var).toString(),prefixString);
					display = display.replace(replaceStr, replaceVal); 
					replaceOutVal = replaceOutVal.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;");
					display = display.replace(replaceOutStr, replaceOutVal);

				}
				display = display.replaceAll("<<\\[\\?\\?sparql-(out-)?var-([0-9A-Za-z_]*)\\?\\?\\]>>",
											 " [?] <!-- UNBOUND VAR. $1 -->");
				content.append(display);
				nbRes++;
			} // while
		} // if
     }		



    /*
     * This method is used to cut off prefix string from content URI, for beautify purpose
     *
     * limitation: PREFIX must be declared in uppercase.
     */
    public static String beautifyDisplay(String source, String prefix)
    {
        String[] stringArray = prefix.split("PREFIX |prefix ");
        for(int i = 0; i < stringArray.length; i++)
        {
            String singlePre = stringArray[i].trim();
            int start = singlePre.indexOf('<');
            int end = singlePre.indexOf(">");
            if(start >= 0 && end >= 0) {
                String prefixName = singlePre.substring(0, start);
                String uri = singlePre.substring(start + 1, end);
                if (source.contains(uri)) {
                    String result = source.replace(uri, prefixName.trim());
                    return result;
                }
            }
        }
        return source;
    }
    /*
     * pre: source is a URI, prefixes is a string of PREIFX P_1 <U_1> PREIFX P_2 <U_2> ...
     * post: if the beignning of source matches a URI U_i in the prefix list
     *         return P_i:rest-of-source
     *       else return <source>
     */
    public static String normalizeURI(String source, String prefixes) {
    	String b = beautifyDisplay(source,prefixes);
		if (b.equals(source)) return  "<" + source + ">";
		else return b;
	}

    
    public static String getProjectPrefix(String projectid) {
       String keytxt=projectid+","+"*"+","+"prefix:";
	   String value=(String)Text.get(keytxt);
	   return value;
    }
	
	/** 
	*	remove all definitions from cache
	*/
	public static void clearAllNodes()
    {
        if (ns.verboseClearCache)
            System.out.println("Node: clear all node definitions");
        nodeDefCache.clear();
        Text=initText(); // must be optimizeD
	    Project.clearAll();
    }
    
	
	/**
	* Invalidate (remove) cached nodes that depend on the modified object (table)
	*/
    public static void OLDDependentNodes(String modifiedObject) { // remove cache dependent nodes
        modifiedObject=modifiedObject.toUpperCase();
        if (ns.verboseClearCache) System.out.println("clearDependentNodes: clear dependents nodes for: "+modifiedObject);
        try {
            String sqlRequest="select distinct PROJECTID, NODENAME from LAZY_NODE_DB_DEP where DBOBJECT = '"+
            modifiedObject+"'";
            QueryResult Q = DBServices.execSQL(sqlRequest, true);
            if (Q.valid) {
                int nbRes=0;
                while (Q.result.next()) {
                    nbRes++;
                    String project=Q.result.getString(1);
                    String name=Q.result.getString(2);
                    if (ns.verboseClearCache) System.out.println("clearDependentNodes: try to clear CACHE for node: " + project+"."+name);
                    Node N = (Node) nodeDefCache.get(project+"."+name); // try to load from cache
                    if (N != null) { // already in the cache
                        if (ns.verboseClearCache) {
                            if (ns.verboseClearCache) System.out.println("clearDependentNodes: clear CACHE for node: " + project+"."+name);
                            N.Cache.clear();
                        }
                    }
                }
                Q.result.close();
            }
            else System.out.println("clearDependentNodes:  error in select\n");
        } // try
        catch (SQLException e) {
            System.out.println("clearDependentNodes:  SQLError: " + e.getMessage()+"\n");}
        Text=initText(); // must be optimize
    }
	
	public static void clearDependentNodes(String modifiedObject) {
		/* DBG */ System.out.println("clearDependentNodes("+modifiedObject+")");
		Set nodesToClear = (Set) DBObjectDependencies.get(modifiedObject);
		/* DBG */ System.out.println("nodes to clear: "+nodesToClear);
		if (nodesToClear == null) return ;
		String nodeName = "";
		for (Iterator i = nodesToClear.iterator(); i.hasNext(); ) {
			nodeName = (String) i.next();
			/* DGB */ System.out.println("clear "+nodeName);
			Node N = (Node) nodeDefCache.get(nodeName); // try to load from cache
            if (N != null) { // already in the cache
            	if (ns.verboseClearCache) System.out.println("clearDependentNodes: clear CACHE for " + nodeName);
                N.Cache.clear();
			}
 		}
	}
    
}

