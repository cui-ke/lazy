/* Lazy hypertext view system
Copyright (C) 2002-2003,  ISI Research Group, CUI, University of Geneva

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
You can also find the GNU GPL at http://www.gnu.org/copyleft/gpl.html

You can contact the ISI research group at http://cui.unige.ch/isi
 */

import java.io.*;
import java.text.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSetFormatter;

import java.sql.*;
import java.net.*;
import java.lang.reflect.*;
// import java.security.*;
// import javax.crypto.*;
// import java.security.*;

/***************************************************
 *
 *    ns.java
 *
 * HISTORY
 *   Eearly versions of the Lazy node server have been written in
 *    - Applescript (ran with the Webstar server) (J. Guyot)
 *    - PL/SQL (ran in the Oracle Webserver) (JG, M. Bonjour)
 *   
 * v2.5 - first version written in Java (a servlet with a JDBC connection) (G. Falquet)
 * v2.6 - introduced 'expand-in-place' (GF)
 * v3.0 - active hrefs (database update) (GF)
 * v4.0 - node instance caching, security and encryption, node types, XML nodes, session variables, 
 *		  correct concurrency handling, multiple connections, code restructuring (JG)
 * v4.1 - new login policy, simplified security scheme, project description without special attributes (A_XSL), etc.
 *      - uses the Java 1.4 encryption scheme
 * v4.2 - includes in node parameters
 *      - simpler signatures for external nodes (Java methods with String parameters)
 *      - session variables are variables (not strings)
 * v4.3 - new type of database actions ("on open") executed before instantiating a node
 *		- correct node-database dependency management for cache management
 *
 ***************************************************/

/* 04.04.2014 (Lin ZHANG)
    void compilerProject() invoke either Compiler or CompilerSparql
 */

/* 11.07.2003 (GF)
 when reading secure_rolenode, put table names in uppercase
 */

 /* 25.04.2003 (GF)
 New variable replacement scheme (variables which are not in strings)
 */

/* 18.04.2003 (GF)
 Includes in node parameter lists
 */

/* 17.04.2003 (GF)
 New signatures for external nodes
 */

/* 07.02.2003 (GF)
 New encryption scheme, uses Java 1.4 only
 */

/* 17.07.2002 (GF)
 New login scheme (Lazy login)
 - by default (at session creation) the user is logged as name = PUBLIC, datagroup = default datagroup for public (PUBLIC)
 - when a node is not accessible, the user is asked for a new user name / password / data group

 - the SECURE.login node is a pseudo-node that is intended for changing the current name/datagroup
 this node always causes the login form to be displayed, then the user is lead to another node specified in the parameter
 i.e.  href SECURE.login["myNode", p1, p2, ...]
 asks for a new user/datagroup and then jumps to myNode[p1, p2, É] 
 This can be done in a separate window

 */

/* 18.07.2002 (GF)
 Node types
 - The type of a node is defined as follows
 1. get the node type from the node definition (Node.getNodeType), 
 in fact the type is defined by the node's project (Project.getNodeType)
 2. if there is a request parameter tn=T, set the node type of this node and of subsequent nodes to tn's value
 3. use &tn=fromdef to reset types as in the node definitions
 */

/* --.06.2002
 Extended authorization scheme
 project.* syntax to grant access to all nodes of a project
 user * role to give a role to user in every datagroup
 */

// 5.11.2001 implement selected and order optional JG

// 5.11.2001 optimize pre and post do nothing if there are empty JG

/*

 To do list

 =====> BUG -- IN CACHE MANAGMENET see Node.Actualize

 - reintroduce zoommax, ....


 - clean processing of session variables: [[X]]  instead of "[[X]]", requires a compiler change

 - rename variables according to english syntax

 LATER

 - add 'on open node' actions => much simpler handling of update parameters (no need to "store" them in hidden input fields"


 RESTRICTIONS

 The text of a node schema must not contain any of these strings :
 <<??include 
 <!--#EXPANDED
 <!--#END EXPANDED
 <<??param

 */

/**
 * 
 * The servlet that processes node requests
 * 
 * @author Jacques Guyot
 * @author Gilles Falquet
 * @version 4.1 Summer 2001
 * 
 */
public class ns extends HttpServlet {

	public static boolean verbose = false;
	public static boolean verboseCharCoding = false;
	public static boolean verboseCache = false;
	public static boolean verboseClearCache = false;
	public static boolean verboseModify = false;
	public static boolean verboseReplace = true;
	public static boolean verboseTiming = false;
	public static boolean verboseConnect = false;
	public static boolean verboseCrypto = false;
	public static boolean verbosePreActions = true;
    public static boolean verboseSparql = true;

	public static boolean cacheDefinition = true;
	public static boolean cacheNodeInstance = true;

	public static boolean DependentNodeClear = true;

	public static boolean computeSaveTime = true;

	String nsVersion = "4.3.0";

	// URL for a connection to an ORACLE database
	String url = "jdbc:oracle:thin:@cuisund:1521:cui";
	String dictionaryURL = "jdbc:db-type:driver-type:@db-host:db-port:db-instance";
	String user = "lazy";
	String pwd = "lazy";
	String driver = "oracle.jdbc.driver.OracleDriver";
	/** Server name for the xsl and css files */
	// String fileURL = "http://127.0.0.0:8080"; // xsl, css file server
	String hostAdress = "127.0.0.0"; // host Adress

	// constant Serveur LAZY
	/**
	 * Number of node instances kept - should be the same as the browser length
	 * of the browser 'back' menu
	 */
	public static int LENGTH_HISTORY = 20;
	public static int NODE_HISTORY = 1000;
	public static int NODE_REQ_HISTORY = 200;
	public static String SERVER_DEF_TYPE = "xml";
	public static boolean encryptOFF = false;
	public static String encryptKEY = "12121212";
	public static String UnicodeCollationSQL92 = ""; // other
	// static String UnicodeCollationSQL92 = "N"; // unicode

	// Markers

	public static final String includePrefix = "<<??include a=";
	public static final String includeSuffix = "//??>>";
	public static final String SystParamPrefix = "[["; // used in node definition
	public static final String SystParamSuffix = "]]";
	public static final String varPrefix = "<<[??var-";
	public static final String varSuffix = "??]>>";
	public static final String varPrefixSparql = "<begin-var-";
	public static final String varSuffixSparql = "-end-var>";
	public static final String sparqlVarPrefix = "<<[??sparql-var-";
	public static final String sparqlOutVarPrefix = "<<[??sparql-out-var-";
	public static final String paramPrefix = "<<??param-";
	public static final String paramSuffix = "//??>>";
	public static final String outParamPrefix = "<<??out-param-";
	
	public static final String eipPrefix = "<a href=\"ns?eip=ZYX"; // WARNING --
															// depends on "ns"
	public static final String expandedBegin = "<!--#EXPANDED-:";
	public static final String endComment = "-->";
	public static final String expandedEnd = "<!--#END EXPANDED-:";
	public static final String cipPrefix = "<a href=\"ns?cip=ZYX"; // WARNING --
															// depends on "ns"
	public static final String cipTextHTML = "<img src=\"icon/close.gif\" border=\"0\"/>";
	public static final String cipTextXML = "<CloseIt/>";
	public static final String focusTextHTML = "";
	public static final String focusTextXML = "<focus>expanded</focus>";
	public static final String msgNotInHistoryCache = "<h3>The context of this link was to old to be reloaded!</h3>";

	/* semantic */// static IdxStructure id; // only for indexing document

	int reqID = 0;

	ServletContext scontext;

	/************************************
	 * 
	 * Initializes the first database connection (dictionary) by reading
	 * parameters in the Lazy.properties file
	 */
	public void init(ServletConfig config) throws ServletException {

		scontext = config.getServletContext();

		// Obtain parameters from a ressource bundle (file Lazy.propertier)

		ResourceBundle rb = ResourceBundle.getBundle("Lazy");
		url = rb.getString("database.url");
		dictionaryURL = rb.getString("database.url"); // ** this line should
														// replace the previous
														// one
		user = rb.getString("database.user");
		pwd = rb.getString("database.password");
		driver = rb.getString("database.driver");
		// fileURL = rb.getString("fileserver.url"); **GF 0902
		hostAdress = rb.getString("host.adress");
		encryptKEY = rb.getString("encrypt.key");
		if (rb.getString("encrypt.off").equals("off"))
			encryptOFF = true;
		LENGTH_HISTORY = Integer.parseInt(rb.getString("cache.length_history"));
		NODE_HISTORY = Integer.parseInt(rb.getString("cache.node_history"));
		NODE_REQ_HISTORY = Integer.parseInt(rb
				.getString("cache.node_req_history"));
		SERVER_DEF_TYPE = rb.getString("host.server_def_type");
		UnicodeCollationSQL92 = rb.getString("host.unicodecollation");

		System.out.println("Initializing with :\n url=" + url + "\n user="
				+ user
				+ "\n driver="
				+ driver
				// + "\n fileserver="+ fileURL **GF 0902
				+ "\n hostAdress=" + hostAdress + "\n LENGTH_HISTORY="
				+ LENGTH_HISTORY + "\n NODE_HISTORY=" + NODE_HISTORY
				+ "\n LENGTH_HISTORY=" + NODE_REQ_HISTORY
				+ "\n SERVER_DEF_TYPE=" + SERVER_DEF_TYPE);

		// init secure engine
		Secure.init(encryptKEY /* +"12121212" */, encryptOFF); // only half key
																// is in the
																// properties /*
																// no more
																// GF0902 */

		// make a connection with the data base
		DBServices.init(url, user, Secure.decryptpwd(pwd), driver);

		// recompute nodedependencies
		// if (DependentNodeClear)
		// System.out.println(Node.ResetNodeDependencies());

	}

	/************************************
	 * 
	 * Closes the data base connections
	 */
	public void destroy() {
		DBServices.finish();

	}

	/**
	 * Processes node requests
	 * <p>
	 * the HTTP request parameters are as follows:
	 * </p>
	 * <ul>
	 * <li>a : node name</li>
	 * <li>u : node parameter value (repeated)</li>
	 * <li>eip : ZYXnnnXYZppp : expand in place request for page ppp at location
	 * nnn</li>
	 * <li>cip : ZYX ??? : close a previously expanded node</li>
	 * <li>act : new | upd | del : action to perform before node generation</li>
	 * <li>tbl : table to update
	 * <li>an : attribute name (multiple), for insert (new), update, and delete
	 * <li>av : attribute value (multiple)
	 * <li>kn : key attribute name (multiple), for update and delete
	 * <li>kv : key attribute value (multiple)
	 * <li>tn : type of node (http/xml)
	 * <li>hn : encrypted attribute name
	 * <li>hv : encrypted attribute value
	 * <li>direct : =yes if this request contains a login/pwd
	 * <li>con : database connection to use for database actions
	 * <li>u_enc
	 * <li>login : user name
	 * <li>password : password
	 * </ul>
	 * 
	 */
	public void doGetSparql(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {

		TreeMapCache contentsHistory;
		String nodeType;
		String identified = "";
		String userID;
		String grpID;
		String firstURL;
		String clientAdress;
		String firstconnection = "NO";

		long starttime = System.currentTimeMillis();

		reqID++; // request unique ID

		boolean isEIP = false; // is this request an "expand in place"
		boolean isCIP = false; // is this request a "close expanded in place"
		String eipNum = "";
		String anchorNum = "0";

		HttpSession session = request.getSession();
		// if (ns.verboseConnect)
		System.out.println("\n" + new java.util.Date(starttime)
				+ ": GET request for session " + session.getId()
				+ "\n client: " + request.getRemoteAddr() + " query: "
				+ request.getQueryString());
		
		System.out.println("******* This is the original doGet in ns *******");

		String[] login = request.getParameterValues("login");

		//
		// NEW SESSION
		//
		// - Create session history
		// - Set username to PUBLIC
		//

		if (session.getAttribute("contentsHistory") == null) { // first
																// connection!

			firstconnection = "YES";
			clientAdress = request.getRemoteAddr();
			System.out
					.println("Create contentsHistory for this session (client):"
							+ clientAdress);
			contentsHistory = new TreeMapCache(LENGTH_HISTORY, "REQUEST"
					+ session.getId());
			session.setAttribute("contentsHistory", contentsHistory);
			// nodeType = "xml"; // default
			session.setAttribute("nodeType", "fromdef");
			grpID = "PUBLIC"; // default group of data
			session.setAttribute("GRP", grpID);
			userID = "PUBLIC";
			session.setAttribute("USER", userID);
			session.setAttribute("clientAdress", clientAdress);

			boolean LOCAL_HOST_WITHOUT_LOGIN = false;

			if (clientAdress.equals(hostAdress) && LOCAL_HOST_WITHOUT_LOGIN) {
				firstconnection = "NO";
				userID = "LOCAL_RUN_ON_HOST";
				session.setAttribute("USER", userID);
				if (ns.verboseConnect)
					System.out.println(userID);
			} else {
				System.out.println("--- Default or direct identification");
				SecureLogin.checkUser(url, session, request, response, "",
						false);
				String direct = (String) (session.getAttribute("DIRECT"));
				System.out.println("connection type: direct = " + direct);
			}

		} // end new session
		else {
			if (login != null) {
				// just received a login submission form (generated by a
				// previous security exception)
				firstURL = (String) (session.getAttribute("firstURL")); // recover
																		// the
																		// previously
																		// requested
																		// URL
				SecureLogin.checkUser(url, session, request, response,
						firstURL, true);
				return;
				// the requested URL will be received in a subsequent request
			}
		}
		userID = (String) (session.getAttribute("USER"));
		grpID = (String) (session.getAttribute("GRP"));
		contentsHistory = (TreeMapCache) (session
				.getAttribute("contentsHistory"));

		// Parameter 'a' is the full node name (PROJECT.NODENAME)

		String[] as = request.getParameterValues("a"); // **GF0702
		String a = "UNSPECIFIED.NODE";
		if (as != null) {
			a = as[0];
			nodeType = Node.getNodeType(a);
		} else
			nodeType = "html";
		//
		// look for new default value tn="text/html", "image/svg" or "text/xml"
		//
		if (request.getParameterValues("tn") != null) {
			String urlval = request.getParameterValues("tn")[0];
			session.setAttribute("nodeType", urlval);
		}

		// node type defined during the session superceeds the node type defined
		// in the node/project dictionary
		String st = (String) (session.getAttribute("nodeType"));
		if (!st.equals("fromdef") && !st.equals(""))
			nodeType = st;

		if (nodeType.equals("html"))
			response.setContentType("text/html; charset=utf-8");
		if (nodeType.equals("purehtml"))
			response.setContentType("text/html");
		if (nodeType.equals("xml"))
			response.setContentType("text/xml; charset=utf-8");
		if (nodeType.equals("purexml"))
			response.setContentType("text/xml");
		if (nodeType.equals("svg"))
			response.setContentType("image/svg");

		PrintWriter out = response.getWriter();

		//
		// Perform database update action
		//

		String actionMsg = "";

		String[] act = request.getParameterValues("act");
		if (act != null) {
			actionMsg = dbAction(request, session, nodeType) + "<hr/>";
			if (!DependentNodeClear)
				Node.clearAllNodes();
			// clear cache if update - must be optimized !!!!!!!!!!!!!!!!
		}

		// Request parameters 'u' contain the node parameters

		String[] u = request.getParameterValues("u");

		// Expand in place request

		if (request.getParameterValues("eip") != null) {
			String eipPar = request.getParameterValues("eip")[0];
			isEIP = true;
			eipNum = eipPar.substring(3, eipPar.indexOf("XYZ"));
			anchorNum = eipPar.substring(eipPar.indexOf("XYZ") + 3,
					eipPar.length());

			if (ns.verbose)
				System.out.println("Expand in place anchorNum=" + anchorNum
						+ " anchorNumber=" + eipNum);

		}

		// Close in place request

		if (request.getParameterValues("cip") != null) {
			String eipPar = request.getParameterValues("cip")[0];
			if (ns.verbose)
				System.out.println("Close eipPar=" + eipPar);
			isCIP = true;
			eipNum = eipPar.substring(3, eipPar.indexOf("XYZ"));
			anchorNum = eipPar.substring(eipPar.indexOf("XYZ") + 3,
					eipPar.length());

			if (ns.verbose)
				System.out.println("Close expand in place anchorNum="
						+ anchorNum + " anchorNumber=" + eipNum);

		}

		// build a string representation of the node parameters

		String ul = "";
		if (u != null) {
			for (int i = 0; i < u.length; i++) {
				if (i > 0)
					ul += ",";
				ul += u[i];
			}
		}

		// outPre(a, ul, out, nodeType); // moved just before printing the node
		// content

		String newContent = "";

		if (isCIP) {
			// close expanded node
			Object oContent = contentsHistory.get(anchorNum);
			if (oContent == null)
				newContent = msgNotInHistoryCache;
			else
				newContent = closeInPlace((String) oContent, eipNum);
		} else {

			// generate the node content

			if (ns.verboseTiming)
				System.out.println("before query time: "
						+ (System.currentTimeMillis() - starttime) + " msec\n");
			try {
				newContent += query(a, u, 0, request, session);
				// += Purpose: add action error message in front of page
				// content.
				if (ns.verboseTiming)
					System.out.println("after query time : "
							+ (System.currentTimeMillis() - starttime)
							+ " msec\n");
			} catch (LazySecurityException le) {
				SecureLogin.askForPWD(response, "Restricted Access");
				if (a.equals("ADMIN.login")) {
					// One tricky thing !!
					// The first parameter becomes the node name
					// ASSUMPTION: the URL has the form
					// ....?a=ADMIN.login&u=param1&u=...
					String qs = request.getQueryString();
					String newqs = "a"
							+ qs.substring("a=ADMIN.login&u".length(),
									qs.length());
					firstURL = "/" + request.getContextPath().substring(1)
							+ request.getServletPath() + "?" + newqs; /*
																	 * removed
																	 * fileURL
																	 * **gf
																	 */
				} else
					firstURL = "/" + request.getContextPath().substring(1)
							+ request.getServletPath() + "?"
							+ request.getQueryString(); /* removed fileURL **gf */
				session.setAttribute("firstURL", firstURL);
			}

		}

		// if expand-in-place : place it into the appropriate page
		String prevContent = "";
		if (isEIP) {
			Object oContent = contentsHistory.get(anchorNum);
			if (oContent == null)
				prevContent = msgNotInHistoryCache + newContent;
			else
				prevContent = expandInPlace((String) oContent, eipNum,
						newContent, nodeType);
		} else
			prevContent = newContent;

		boolean isContainsEIPorCIP = false;
		// does this result contain "close or expanded in place ?

		if (prevContent.indexOf(eipPrefix, 0) != -1) { // contains eip
			prevContent = replaceEIP(prevContent);
			isContainsEIPorCIP = true;
		}

		if (prevContent.indexOf(cipPrefix, 0) != -1) { // contains cip
			prevContent = replaceCIP(prevContent);
			isContainsEIPorCIP = true;
		}

		if (isContainsEIPorCIP) { // archive in cache
			contentsHistory.put("" + reqID, prevContent, 0);
		}

		if (ns.verboseTiming)
			System.out.println("compute time: "
					+ (System.currentTimeMillis() - starttime) + " msec\n");

		outPre(a, ul, out, nodeType);
		out.println(actionMsg + SU.replaceHrefs(prevContent));
		outPost(a, ul, out, nodeType);

		System.out.println("Exit GET for session =" + session.getId());

		if (ns.verboseTiming)
			System.out.println("total (compute+transmit) time: "
					+ (System.currentTimeMillis() - starttime) + " msec\n");
		if (ns.verboseTiming)
			System.out.println(TreeMapCache.getGlobalStatistic());
	}

	public void doGet(HttpServletRequest request,
			HttpServletResponse response) throws IOException, ServletException {

//		System.out.println("Enter doGet ************************************************");

		TreeMapCache contentsHistory;
		String nodeType;
		String identified = "";
		String userID;
		String grpID;
		String firstURL;
		String clientAdress;
		String firstconnection = "NO";

		long starttime = System.currentTimeMillis();

		reqID++; // request unique ID

		boolean isEIP = false; // is this request an "expand in place"
		boolean isCIP = false; // is this request a "close expanded in place"
		String eipNum = "";
		String anchorNum = "0";

		HttpSession session = request.getSession();
		// if (ns.verboseConnect)
		System.out.println("\n" + new java.util.Date(starttime)
				+ ": GET request for session " + session.getId()
				+ "\n client: " + request.getRemoteAddr() + " query: "
				+ request.getQueryString());

		
		String[] login = request.getParameterValues("login");

		//
		// NEW SESSION
		//
		// - Create session history
		// - Set username to PUBLIC
		//

		
		if (session.getAttribute("contentsHistory") == null) { // first
																// connection!

//			System.out.println("Enter doGet ************************************************2");
			
			firstconnection = "YES";
			clientAdress = request.getRemoteAddr();
			System.out
					.println("Create contentsHistory for this session (client):"
							+ clientAdress);
			contentsHistory = new TreeMapCache(LENGTH_HISTORY, "REQUEST"
					+ session.getId());
			session.setAttribute("contentsHistory", contentsHistory);
			// nodeType = "xml"; // default
			session.setAttribute("nodeType", "fromdef");
			grpID = "PUBLIC"; // default group of data
			session.setAttribute("GRP", grpID);
			userID = "PUBLIC";
			session.setAttribute("USER", userID);
			session.setAttribute("clientAdress", clientAdress);

			boolean LOCAL_HOST_WITHOUT_LOGIN = false;

			if (clientAdress.equals(hostAdress) && LOCAL_HOST_WITHOUT_LOGIN) {
				firstconnection = "NO";
				userID = "LOCAL_RUN_ON_HOST";
				session.setAttribute("USER", userID);
				if (ns.verboseConnect)
					System.out.println(userID);
			} else {
				System.out.println("--- Default or direct identification");
				SecureLogin.checkUser(url, session, request, response, "",
						false);
				String direct = (String) (session.getAttribute("DIRECT"));
				System.out.println("connection type: direct = " + direct);
			}

		} // end new session
		else {
			
//			System.out.println("Enter doGet ************************************************3");
			
			if (login != null) {
				// just received a login submission form (generated by a
				// previous security exception)
				firstURL = (String) (session.getAttribute("firstURL")); // recover
																		// the
																		// previously
																		// requested
																		// URL
				SecureLogin.checkUser(url, session, request, response,
						firstURL, true);
				return;
				// the requested URL will be received in a subsequent request
			}
		}
		
		userID = (String) (session.getAttribute("USER"));
		grpID = (String) (session.getAttribute("GRP"));
		contentsHistory = (TreeMapCache) (session
				.getAttribute("contentsHistory"));

		// Parameter 'a' is the full node name (PROJECT.NODENAME)

		String[] as = request.getParameterValues("a"); // **GF0702
		String a = "UNSPECIFIED.NODE";
		if (as != null) {
			a = as[0];
			nodeType = Node.getNodeType(a);
		} else
			nodeType = "html";
		//
		// look for new default value tn="text/html", "image/svg" or "text/xml"
		//
		if (request.getParameterValues("tn") != null) {
			String urlval = request.getParameterValues("tn")[0];
			session.setAttribute("nodeType", urlval);
		}

		// node type defined during the session superceeds the node type defined
		// in the node/project dictionary
		String st = (String) (session.getAttribute("nodeType"));
		if (!st.equals("fromdef") && !st.equals(""))
			nodeType = st;

		if (nodeType.equals("html"))
			response.setContentType("text/html; charset=utf-8");
		if (nodeType.equals("purehtml"))
			response.setContentType("text/html");
		if (nodeType.equals("xml"))
			response.setContentType("text/xml; charset=utf-8");
		if (nodeType.equals("purexml"))
			response.setContentType("text/xml");
		if (nodeType.equals("svg"))
			response.setContentType("image/svg");

		PrintWriter out = response.getWriter();

		//
		// Perform database update action
		//

		String actionMsg = "";

		String[] act = request.getParameterValues("act");
		if (act != null) {
			actionMsg = dbAction(request, session, nodeType) + "<hr/>";
			if (!DependentNodeClear)
				Node.clearAllNodes();
			// clear cache if update - must be optimized !!!!!!!!!!!!!!!!
		}

		// Request parameters 'u' contain the node parameters

		String[] u = request.getParameterValues("u");

		// Expand in place request

		if (request.getParameterValues("eip") != null) {
			String eipPar = request.getParameterValues("eip")[0];
			isEIP = true;
			eipNum = eipPar.substring(3, eipPar.indexOf("XYZ"));
			anchorNum = eipPar.substring(eipPar.indexOf("XYZ") + 3,
					eipPar.length());

			if (ns.verbose)
				System.out.println("Expand in place anchorNum=" + anchorNum
						+ " anchorNumber=" + eipNum);

		}

		// Close in place request

		if (request.getParameterValues("cip") != null) {
			String eipPar = request.getParameterValues("cip")[0];
			if (ns.verbose)
				System.out.println("Close eipPar=" + eipPar);
			isCIP = true;
			eipNum = eipPar.substring(3, eipPar.indexOf("XYZ"));
			anchorNum = eipPar.substring(eipPar.indexOf("XYZ") + 3,
					eipPar.length());

			if (ns.verbose)
				System.out.println("Close expand in place anchorNum="
						+ anchorNum + " anchorNumber=" + eipNum);

		}

		// build a string representation of the node parameters

		String ul = "";
		if (u != null) {
			for (int i = 0; i < u.length; i++) {
				if (i > 0)
					ul += ",";
				ul += u[i];
			}
		}

		// outPre(a, ul, out, nodeType); // moved just before printing the node
		// content

		String newContent = "";
		
		System.out.println("Enter doGet ************************************************4");
		System.out.println("aaaaaaaaaaaaa == " + a );
        System.out.println("uuuuuuuuuuuuu == " + ul);

		if (isCIP) 
		{
//			System.out.println("Enter doGet **************is close expanded node?************4.2");
			
			// close expanded node
			Object oContent = contentsHistory.get(anchorNum);
			if (oContent == null)
				newContent = msgNotInHistoryCache;
			else
				newContent = closeInPlace((String) oContent, eipNum);
		} else 
		{

			// generate the node content

//			System.out.println("Enter doGet *******************generate the node content******************5");
			
			if (ns.verboseTiming)
				System.out.println("before query time: "
						+ (System.currentTimeMillis() - starttime) + " msec\n");
			try {
//				System.out.println("aaaaaaaaaaaaaaaaaaa == " + a );

				//System.out.println("Enter doGet *************regular work flow*****************7");
				newContent += query(a, u, 0, request, session);
				
				// += Purpose: add action error message in front of page
				// content.
				if (ns.verboseTiming)
					System.out.println("after query time : "
							+ (System.currentTimeMillis() - starttime)
							+ " msec\n");
			} catch (LazySecurityException le) {
				SecureLogin.askForPWD(response, "Restricted Access");
				if (a.equals("ADMIN.login")) {
					// One tricky thing !!
					// The first parameter becomes the node name
					// ASSUMPTION: the URL has the form
					// ....?a=ADMIN.login&u=param1&u=...
					String qs = request.getQueryString();
					String newqs = "a"
							+ qs.substring("a=ADMIN.login&u".length(),
									qs.length());
					firstURL = "/" + request.getContextPath().substring(1)
							+ request.getServletPath() + "?" + newqs; /*
																	 * removed
																	 * fileURL
																	 * **gf
																	 */
				} else
					firstURL = "/" + request.getContextPath().substring(1)
							+ request.getServletPath() + "?"
							+ request.getQueryString(); /* removed fileURL **gf */
				session.setAttribute("firstURL", firstURL);
			}

		}

		// if expand-in-place : place it into the appropriate page
		String prevContent = "";
		if (isEIP) {
			Object oContent = contentsHistory.get(anchorNum);
			if (oContent == null)
				prevContent = msgNotInHistoryCache + newContent;
			else
				prevContent = expandInPlace((String) oContent, eipNum,
						newContent, nodeType);
		} else
			prevContent = newContent;

		boolean isContainsEIPorCIP = false;
		// does this result contain "close or expanded in place ?

		if (prevContent.indexOf(eipPrefix, 0) != -1) { // contains eip
			prevContent = replaceEIP(prevContent);
			isContainsEIPorCIP = true;
		}

		if (prevContent.indexOf(cipPrefix, 0) != -1) { // contains cip
			prevContent = replaceCIP(prevContent);
			isContainsEIPorCIP = true;
		}

		if (isContainsEIPorCIP) { // archive in cache
			contentsHistory.put("" + reqID, prevContent, 0);
		}

		if (ns.verboseTiming)
			System.out.println("compute time: "
					+ (System.currentTimeMillis() - starttime) + " msec\n");

		outPre(a, ul, out, nodeType);
		out.println(actionMsg + SU.replaceHrefs(prevContent));
		outPost(a, ul, out, nodeType);

		System.out.println("Exit GET for session =" + session.getId());

		if (ns.verboseTiming)
			System.out.println("total (compute+transmit) time: "
					+ (System.currentTimeMillis() - starttime) + " msec\n");
		if (ns.verboseTiming)
			System.out.println(TreeMapCache.getGlobalStatistic());

	}

	void querySparql() {
		
//		System.out.println("Enter querySparql >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
		
		String queryServiceEndpoint = "http://lod.openlinksw.com/sparql";
		String prefixInfo = 
				"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
				"PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
				"PREFIX gr: <http://purl.org/goodrelations/v1#> \n";
		
		// create a new query
		//String queryString = "SELECT * WHERE {?s a ?o} limit 100";
		String queryString = prefixInfo +
				//"SELECT ?xx ?ab ?bp ?b ?c bif:ceiling ( xsd:int ( ?cv ) *100 ) /100.00 WHERE \n" +
				"SELECT ?xx ?ab ?bp ?b ?c ?cv WHERE \n" +
				"	  { \n" +
				"	    ?xx a gr:BusinessEntity .\n" +
				"	    ?xx gr:offers ?ab .\n" +
				"	    ?ab rdf:type gr:Offering .\n" +
				"	    ?ab gr:hasBusinessFunction ?bp .\n" +
				"	    ?ab gr:includesObject ?b .\n" +
				"	    ?b rdf:type gr:TypeAndQuantityNode .\n" +
				"	    ?b gr:typeOfGood ?c .\n" +
				"	    ?c rdf:type gr:ProductOrServicesSomeInstancesPlaceholder .\n" +
				"	    ?ab gr:hasPriceSpecification ?p .\n" +
				"	    ?p gr:hasCurrencyValue ?cv .\n" +
				"	    FILTER ( ?cv > 1000 ) \n" +
				"	  }\n" +
				" LIMIT 10 \n"; 
		
		// execute the query
		Query query = QueryFactory.create(queryString);
		QueryExecution x = QueryExecutionFactory.sparqlService(queryServiceEndpoint, queryString);
		
		com.hp.hpl.jena.query.ResultSet results = x.execSelect();
//		System.out.println("querySparql finished  >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
//		ResultSetFormatter.out(System.out, (com.hp.hpl.jena.query.ResultSet) results);

	}

	/**
	 * Outputs the content header for xml, html, ... , depending on the node
	 * type.
	 * 
	 * @param a
	 *            The node name
	 * @param ul
	 *            A string representation of the node parameters
	 * @param nodeType
	 *            Node type
	 */
	void outPre(String a, String ul, PrintWriter out, String nodeType) {
		if (nodeType.equals("html")) {
			out.println("<HTML>");
			out.println("<HEAD>");
			// out.println("<LINK rel=stylesheet href=\"lazy-basic.css\" type=\"text/css\">");
			out.println("<LINK rel=\"stylesheet\" href=\""
					+ Node.getCSSFileName(a) + "\" type=\"text/css\">");
			out.println("<TITLE>Node: " + a + "[" + ul + "]" + "</TITLE>");
			// out.println("<META HTTP-EQUIV=\"expires\" CONTENT=\"0\">"); // no
			// caching in browsers
			out.println("<META NAME=\"generator\" content=\"Lazy node server v"
					+ nsVersion + "\">");
			out.println("<META NAME=\"description\" content=\"node " + a + "[" /*
																				 * #
																				 * #
																				 * #
																				 * +
																				 * ul
																				 * #
																				 * #
																				 * #
																				 */
					+ "]\">");
/* EXPERIMENTAL, should be replaced by a configuration file ... */					
			out.println("<script type=\"text/x-mathjax-config\">/*<![CDATA[*/MathJax.Hub.Config({ tex2jax: {inlineMath: [ [\"$\",\"$\"], [\"\\\\(\",\"\\\\)\"] ], displayMath: [ [\"$$\",\"$$\"], [\"\\\\[\",\"\\\\]\"] ], processEscapes: true} }); MathJax.Hub.Config({ TeX: { equationNumbers: { autoNumber: \"AMS\" } }}); /*!]]>*/</script>");
			out.println("<script type=\"text/javascript\" charset=\"utf-8\" src=\"http://cdn.mathjax.org/mathjax/latest/MathJax.js?config=TeX-AMS_HTML\"></script>");
					
					
			out.println("</HEAD>");
			out.println("<BODY>");
		}

		if (nodeType.equals("xml")) {
			// out.println("<?xml version='1.0'  encoding='ISO-8859-1' ?>");
			// out.println("<?xml version='1.0'  encoding='UTF-8' ?>");
			out.println("<?xml version='1.0' ?>"); // utf-16 is the default for
													// explorer

			out.println("<?xml-stylesheet type='text/xsl' href='"
					+ /* fileURL +"xsl/"+ */Node.getXSLFileName(a) + "'?>");
			out.println("<lazy>");
			out.println("<lazycss>"
					+ /* fileURL +"css/"+ */Node.getCSSFileName(a) + "</lazycss>");
			out.println("<lazybody>"
					+ /* fileURL +"bckgnd/"+ */Node.getBCKGNDFileName(a)
					+ "</lazybody>");
			out.println("<title>" + a + "[" + ul + "]" + "</title>");
		}

		if (nodeType.equals("purexml")) {
			// out.println("<?xml version='1.0'  encoding='ISO-8859-1' ?>");
			out.println("<?xml version='1.0'  encoding='UTF-8' ?>");
		}
		if (nodeType.equals("purehtml")) {
		}
		if (nodeType.equals("svg")) {
		}

	}

	/**
	 * Outputs the end of the node content (xml, html, ...)
	 * 
	 */
	void outPost(String a, String ul, PrintWriter out, String nodeType) {
		if (nodeType.equals("html")) {
			// out.println("<P>&nbsp;</P><FONT SIZE=\"-2\"><I>node " + a + /*"["
			// + ul + "]" + */ "  -- Lazy node HTML server v" + nsVersion + " ("
			// + user + ")</I></FONT>");
			out.println("</BODY> </HTML>");
		}
		if (nodeType.equals("xml")) {
			out.println("</lazy>");
		}
		if (nodeType.equals("purexml")) {
		}
		if (nodeType.equals("purehtml")) {
		}
		if (nodeType.equals("svg")) {
		}

	}

	/**
	 * Converts URL parameters that have been encoded with the
	 * x-www-form-urlencoded scheme.
	 */
	public static String[] convertParams(String[] p) { //
		if (p == null)
			return null;
		// System.err.println("nb param:"+p.length);
		String[] okp = new String[p.length];
		byte[] bw;
		for (int i = 0; i < p.length; i++) {
			if (verboseCharCoding)
				System.out.print("convertParams / param:" + p[i] + "->");
			okp[i] = URLUTF8Encoder.unescape(p[i]);
			if (verboseCharCoding)
				System.out.println(okp[i]);
		}
		return okp;

	}

	/**
	 * Shows the numeric values of the bytes that make up a string
	 */
	public static String byteprint(String w) {
		String s = "->";
		try {
			byte[] bw = w.getBytes();
			for (int i = 0; i < java.lang.reflect.Array.getLength(bw); i++) {
				s += String.valueOf((int) (bw[i])) + ",";
			}
		} catch (Exception e) {
			System.err.println("Conversion error - byteprint");
		}
		return s;
	}

	/**
	 * Checks the user's access rights, then evaluates the content of the
	 * requested node instance.
	 * <p>
	 * - nodes starting with __ correspond to Java procedures
	 * <p>
	 * - nodes with other specific names correspond to specific actions.
	 */
	String query(String node, String[] params, int level,
			HttpServletRequest request, HttpSession session)
			throws LazySecurityException {

		params = convertParams(params);
		SU.replaceQueryParameters(session, params);

		String userId = (String) session.getAttribute("USER");
		String GrpId = (String) session.getAttribute("GRP");
		if (node.equals("ADMIN.login"))
			throw new LazySecurityException(
					"ADMIN.login is totally inaccessible");

		Node N = Node.getNodeDefinition(node);

		if (!SecureLogin.checkAccess(node, GrpId, userId, "LAZY", session)) {
			// maybe the node does not exist
			if (!N.valid)
				return N.msg;
			// it exists but this user does not have sufficient access rights
			System.out.println("--- Access check ***:" + node + "/" + GrpId
					+ "/" + userId);
			throw new LazySecurityException("No access right on " + node + "/"
					+ GrpId + "/" + userId);
			// return "<H1>NO PRIVILEGES ON NODE (" + node+ ")</H1>";
		}
		if (node.startsWith("__")) { // invoke external methods
			String className = Node.getProjectName(node.substring(2));
			String methodName = Node.getNodeName(node.substring(2));
			return callExternalService(className, methodName, params);
		}
		// Special action nodes //
		if (node.equals("NODE.getDynamicInfo")) {
			return Node.getDynamicInfo(params);
		} // get dynamic info on node
		if (node.equals("SECURE.grpid_modify")) {
			modifyGRPID(request, session);
		} // just modify the GRPID
		if (node.equals("ADMIN.lazy_admin_clearAllNodes")) {
			Node.clearAllNodes();
			Project.clearAll();
		} // clear node cache and project cache
		if (node.equals("NODE.lazy_compileProject")) {
			compileProject(request, session, params);
		} // clear node cache

		if (N.preActions != null) // node has pre-actions
			execPreActions(N, params, session);

		// return SU.replaceHrefs(queryDB(node, params, level, request,
		// session)); // jg 6.1.2004 : replace href parameters after all
		// processing
		return queryDB(node, params, level, request, session);
	}

	/**
	 * Execute all the pre actions of a node
	 * 
	 */
	public static void execPreActions(Node n, String[] params,
			HttpSession session) {
		String conID = Project.getDBConnection(n.projectid);
		if (conID == null) {
			conID = "DICTLAZY";
		}
		for (int ia = 0; ia < n.preActions.length; ia++) {
			String sqlRequest = n.preActions[ia];
			sqlRequest = SU.replaceParameters(sqlRequest, params, SU.SQL_QUOTES);
			sqlRequest = SU.replaceSystemParameters(sqlRequest, session,
					Node.Text, n.projectid, SU.SQL_QUOTES);
			if (ns.verbosePreActions)
				System.out.println("execPreActions " + ia + ") " + sqlRequest);
			QueryResult Q = DBServices.execSQLonDB(sqlRequest, conID, false);

			// Determine the target table of this action
			StringTokenizer st = new StringTokenizer(sqlRequest, " (,)");
			String command = st.nextToken();
			if (command.equals("insert") || command.equals("delete"))
				st.nextToken();
			String tableName = st.nextToken();
			if (ns.verbosePreActions)
				System.out.println("execPreActions: clearDependentNodes("
						+ tableName + ")");
			if (DependentNodeClear)
				Node.clearDependentNodes(tableName);

		}
		if (!DependentNodeClear)
			Node.clearAllNodes(); // TODO: optimize this
	}

	public static String callExternalService(String className, String method,
			String[] params) {
		boolean traceExt = false;
		String[] aStringArray = {};
		if (traceExt)
			System.out.println("callExternalService: " + className + "."
					+ method);
		String result = "";
		try {
			Class c = Class.forName(className);

			Class[] parameterTypesStrings = new Class[params == null ? 0
					: params.length];
			for (int i = 0; i < parameterTypesStrings.length; i++)
				parameterTypesStrings[i] = result.getClass(); // String
			Class[] parameterTypesStringArray = new Class[] { aStringArray
					.getClass() };
			Class[] parameterTypes = new Class[] { Object.class };
			Method callMethod;
			Object[] arguments = params == null ? new String[] {} : params;
			if (traceExt)
				System.out.println("before getmethod");
			try {
				callMethod = c.getMethod(method, parameterTypesStrings);
			} catch (NoSuchMethodException e1) {
				if (params != null)
					arguments = new Object[] { params };
				else
					arguments = new Object[] { aStringArray };
				try {
					callMethod = c.getMethod(method, parameterTypesStringArray);
				} catch (NoSuchMethodException e) {
					callMethod = c.getMethod(method, parameterTypes);
				}
			}
			if (traceExt)
				System.out.println("before invokemethod");
			result = (String) callMethod.invoke(null, arguments);
		} catch (NoSuchMethodException e) {
			result += "<h3>*** External method invocation error: method <em>"
					+ className + "." + method + "</em> not found</h3>";
			if (traceExt)
				System.out.println(e);
		} catch (IllegalAccessException e) {
			System.out.println(e);
		} catch (InvocationTargetException e) {
			System.out.println(e);
		} catch (ClassNotFoundException e) {
			if (traceExt)
				System.out.println(e);
			result += "<h3>*** External method invocation error: class <em>"
					+ className + "</em> not found</h3>";
		}
		return result;
	}

	/**
	 * Change the data group of this session (deprecated)
	 */
	void modifyGRPID(HttpServletRequest request, HttpSession session) {
		String[] u = request.getParameterValues("u");
		session.setAttribute("GRP", u[0]);
	}

	/**
	 * Calls the compiler to (re)compile a node
	 * 
	 * @param u
	 *            u[0] is the projet name and u[1] the node name
	 */
	void compileProject_OLD(HttpServletRequest request, HttpSession session,
			String u[]) {
		String projectid = u[0];
		String name = u[1];
		String sqlRequest = "select plaintxt from nodes where " + "projectid='"
				+ projectid + "' and name like '" + name + "'";
		QueryResult Q = DBServices.execSQL(sqlRequest, true);
		if (Q.valid) {
			try {
				int nbRes = 0;
				Compiler.ns_init(driver, url, user, Secure.decryptpwd(pwd));
				while (Q.result.next()) {
					nbRes++;
					String k = "define project " + projectid + " "
							+ Q.result.getString(1) + " end";
					PrintWriter xout = new PrintWriter(new FileOutputStream(
							"out.sql"));
					Compiler a = new Compiler(k, xout);
					a.startrule();
				}
				System.out.print("compile node from BD: " + nbRes);
				Node.clearAllNodes();
				Q.result.close();
				Compiler.closeDB();
			} // try
			catch (Exception e) {
				System.out
						.println("ns: during lazy_compileProject() SQLError: "
								+ e.getMessage());
			}
		} else
			System.out.println("ns: during lazy_compileProject() SQLError: "
					+ Q.msg);

	}

	void compileProject(HttpServletRequest request, HttpSession session,
			String u[]) {



		boolean trace = false;
		String projectid = u[0];
		String text = u[1];
		try {
            if(projectid.startsWith("SPARQL")){
            // for project id started with 'DB', currently only 'DB' and 'DBpedia', temporarily implementation

                if(verboseSparql)
                    System.out.println("ns.compileProject() ********************** compile sparql project******* ");

                CompilerSparql.ns_init(driver, url, user, Secure.decryptpwd(pwd));
                String k = "define project " + projectid + " \n" + text + " \n end";

                if(verboseSparql)
                    System.out.println("define project string >>>>>>>> \n "+k);

                if (trace)
                    System.out.println("+++Compile: " + k);
                PrintWriter xout = new PrintWriter(new FileOutputStream("out.sql"));
                CompilerSparql a = new CompilerSparql(k, xout);
                System.out.println("compiler string ===== "+k);
                a.startrule();
                if (trace)
                    System.out.print("End of compilation\n");
                Node.clearAllNodes();
                CompilerSparql.closeDB(); // ??

            }else {

                if(verboseSparql)
                    System.out.println("ns.compileProject() ********************** compile db project******* ");

                Compiler.ns_init(driver, url, user, Secure.decryptpwd(pwd));
                String k = "define project " + projectid + " \n" + text + " \n end";

                if(verboseSparql)
                    System.out.println("define project string >>>>>>>> "+k);

                if (trace)
                    System.out.println("+++Compile: " + k);
                PrintWriter xout = new PrintWriter(new FileOutputStream("out.sql"));
                Compiler a = new Compiler(k, xout);

                a.startrule();
                if (trace)
                    System.out.print("End of compilation\n");
                Node.clearAllNodes();
                Compiler.closeDB(); // ??

            }
		} // try
		catch (Exception e) {
			System.out.println("ns: during lazy_compileProject() " + e
					+ e.getMessage());
			e.printStackTrace();
		}

	}

	/**
	 * Computes a node instance by querying the database
	 */
	String queryDB(String node, String[] params, int level,
			HttpServletRequest request, HttpSession session) {
		ResultSet resDef;
		StringBuffer resb = new StringBuffer();

		if (level > 50)
			return ""; // Just to avoid infinite inclusions

		Node N = Node.getNodeDefinition(node);

		if (!N.valid) {
			return N.msg;
		} // error in loading definition

		StringBuffer content = N.Actualize(params, session);
		// get a instance of this node with these parameters

		/* Expand the included nodes */

		if (N.NOinclude)
			return (new String(content)); // THIS NODE cannot generate include
											// ...
		else
			return expandIncludes(new String(content), request, session);
	}

	/**
	 * Find all the inclusion marks and replace them with the node content to be
	 * included
	 * <p>
	 * Inclusion marks are : &lt;&lt;??include
	 * a=<b>NODE_NAME</b>&amp;amp;u=<b>NODE_PARAM_1
	 * </b>&amp;amp;u=<b>NODE_PARAM_2</b>&amp;amp;u=...??&gt;&gt;
	 * <p>
	 * 
	 * 2003-04-19: Change the replacement order to support includes within
	 * include parameters
	 */
	String expandIncludes(String s, HttpServletRequest request,
			HttpSession session) {
		boolean trace = false;
		int ix = 0;
		int iscan = 0;
		StringBuffer resb = new StringBuffer();

		while ((ix = s.indexOf(includePrefix, iscan)) > -1) {

			/*
			 * Find the end of the inclusion, which may contain includes in
			 * parameters
			 */

			int expandEnd = s.indexOf(includeSuffix, ix);
			int iy = s.indexOf(includePrefix, ix + 1);
			while (iy > -1 && iy < expandEnd) {// found one more inclusion
				if (trace)
					System.out.println("INC/INC --- include in PARAMS");
				expandEnd = s.indexOf(includeSuffix, expandEnd + 1); // must
																		// find
				iy = s.indexOf(includePrefix, iy + 1);
			}

			String ss = s.substring(ix, expandEnd + includeSuffix.length());
			if (trace)
				System.out.println("* Expand --- ss= " + ss);

			/*
			 * Now do all the expansions in ss, starting with the innermost ones
			 */
			int xend = expandEnd;
			while ((xend = ss.indexOf(includeSuffix)) > -1) {

				// Find an include without includes in its parameters
				int xstart = ss.lastIndexOf(includePrefix, xend);

				int nodeNameStart = xstart + includePrefix.length(); // !!
																		// length
																		// of
																		// include
																		// markup

				int nodeNameEnd = ss.indexOf("&amp;u=", xstart); // xml jg

				if (nodeNameEnd == -1 || nodeNameEnd > xend)
					nodeNameEnd = xend; // gf 0 parameter //xend for expandEnd
				String nodeName = ss.substring(nodeNameStart, nodeNameEnd);

				String[] params = {};

				if (nodeNameEnd < xend) {
					String upar = ss.substring(nodeNameEnd, xend); // xml jg
					upar = SU.getParameters(upar);
					// upar = upar.substring(5);
					int ixx = 0;
					// Replace every '&amp' by '&'
					while ((ixx = upar.indexOf("&amp;", ixx)) > -1) {
						ixx = ixx + 1; // keep &
						upar = upar.substring(0, ixx)
								+ upar.substring(ixx + 4, upar.length());
					}
					if (trace)
						System.out.print("DBG-INC upar='" + upar + "'");
					if (upar.equals("u=")) {
						params = new String[] { "" };
						// upar += "0";
					} else {
						java.util.Hashtable hparams = HttpUtils
								.parseQueryString(upar);
						params = (String[]) (hparams.get("u"));
					}
					if (trace)
						for (int i = 0; i < params.length; i++)
							System.out.print("[" + params[i] + "]");
				}
				if (ns.verbose)
					System.out.println("expanding " + nodeName + " "
							+ ss.substring(nodeNameEnd, xend));

				String expanded = "";
				try {
					expanded = query(nodeName, params, 1, request, session);
					if (trace)
						if (expanded.length() < 30)
							System.out.println("DBG-INC expanded='" + expanded
									+ "'");
				} catch (LazySecurityException le) { /*
													 * do nothing --
													 * unauthorized nodes are
													 * simply not included
													 */
				}

				ss = ss.substring(0, xstart) + expanded
						+ ss.substring(xend + includeSuffix.length());
			} // while

			resb.append(s.substring(iscan, ix));
			// resb.append(expanded);
			resb.append(ss);
			iscan = expandEnd + includeSuffix.length();

		} // while
		resb.append(s.substring(iscan, s.length()));
		return new String(resb);
	}

	/************************************
	 * 
	 * Replaces an expand-in-place mark by the referred node content.
	 * 
	 * <pre>
	 * 
	 *  Replace:     
	 *     <a href="ns?eip=ZYX_SEQ_NO_XYZ_REQ_ID_&amp;a=....>The anchor text</a> After
	 * 
	 *      where _EQ_NO<_ = seq. no. (a number) of this expand link with the node, 
	 *            _REQ_ID_ = request id of the page to expand
	 * 
	 *  By:  
	 *     <a href="ns?cip=ZYXXYZ_REQ_ID_&amp;a=....><!--#EXPANDED-_CURR_REQ_-->
	 *        _cipText_<!--The anchor text--> </a> The expanded nodes 
	 *        <!--#END EXPANDED-:_CURR_REQ_>-->
	 * 
	 *      where _CURR_REQ_ = current request id (reqid of this page)
	 *          _cipText_ is the visual element for closing the expansion
	 * 
	 *      <a href="ns?cip=ZYXXYZ_REQ_ID_ will subsequently be replaced by
	 *      <a href="ns?cip=ZYX_CIP_SEQ_XYZ<b>REQ_ID</b>  in replaceCIP
	 *      where _CIP_SEQ_ is a seq. no. for cips in this page
	 *
	 *     PRECONDITION: the node parameters must not contain the sequence "> this sequence is used
	 3                   to detect the end of the <a> element.
	 * 
	 *     WARNING  : this use of reqID is UNSAFE because it could have been incremented by a concurrent request
	 *     after we started processing this request.
	 * 
	 *     WARNING -- Commenting out the anchor does not work properly if there are tags in the anchor
	 * </pre>
	 */

	String expandInPlace(String s, String eipNum, String exp, String nodeType) {
		int ix = 0;

		int ifocus = s.indexOf("<focus>expanded</focus>");
		if (ifocus != -1) { // exist so delete old anchor
			s = s.substring(0, ifocus) + s.substring(ifocus + 23, s.length());
		}

		String cipText = ""; // xml jg
		String focusText = ""; // xml jg
		if (nodeType.equals("html") || nodeType.equals("purehtml")) {
			cipText = cipTextHTML; // xml jg
			focusText = focusTextHTML;
		}
		if (nodeType.equals("xml") || nodeType.equals("purexml")) {
			cipText = cipTextXML; // xml jg
			focusText = focusTextXML;
		}

		if ((ix = s.indexOf(eipPrefix + eipNum, 0)) > -1) {
			int beginAnchor = s.indexOf("\">", ix) + 2;             //gf 2015-04-09
			int endAnchor = s.indexOf("</a>", beginAnchor);

			return s.substring(0, ix)
					+ focusText
					+ cipPrefix
					+ s.substring(ix + (eipPrefix + eipNum).length(),
							beginAnchor) // <a href="ns?cip=ZYXXYZ<<p>>&a=...>
					+ expandedBegin + reqID + "-->" // <!--#EXPANDED-:<<reqID>>-->
					+ cipText // e.g. <img src="icon/close.gif"/>
					+ "<!--" + s.substring(beginAnchor, endAnchor) + "-->" // comment
																			// out
																			// the
																			// expand
																			// anchor
					+ "</a>" + exp + expandedEnd + reqID + "-->" // <!--#END
																	// EXPANDED-:<<reqID>>-->
					+ s.substring(endAnchor + "</a>".length()); // rest of the
																// page
		} else
			return s;
	}

	/************************************
	 * 
	 * Closes (shrink) an expanded node
	 * 
	 * @param s
	 *            the node content
	 * @param eipNum
	 *            the seq. no. of the expanded node to close
	 */

	String closeInPlace(String s, String eipNum) {
		int a = s.indexOf(cipPrefix + eipNum, 0);
		String res = "--ERROR--NOTHING TO CLOSE--";
		if (a > -1) {
			res = s.substring(0, a) + eipPrefix;
			int beginExpBegin = s.indexOf(expandedBegin, a); // <!--#EXPANDED-:
			res += s.substring(a + cipPrefix.length(), beginExpBegin);
			int beginReqNum = beginExpBegin + expandedBegin.length();
			int endReqNum = s.indexOf("-->", beginReqNum);
			String reqNum = s.substring(beginReqNum, endReqNum);
			int beginAnchor = s.indexOf("<!--", endReqNum) + "<!--".length();
			int endAnchor = s.indexOf("--></a>", beginAnchor);
			res += s.substring(beginAnchor, endAnchor) + "</a>";
			int beginEndExpand = s.indexOf(expandedEnd + reqNum, endAnchor);
			int endEndExpand = s.indexOf("-->", beginEndExpand)
					+ "-->".length();
			res += s.substring(endEndExpand);
		}
		return res;
	}

	/************************************
	 * 
	 * REPLACE EIP // optimised version JG
	 * 
	 * add an expansion sequence number and request number to every expansion
	 * link in this content
	 * 
	 * <<eipPrefix>> is replaced by <<eipPrefix>><<expand seq. no.>>"XYZ"<<this
	 * request id>> i.e. <a href="ns?eip=ZYX&..."> becomes <a
	 * href="ns?eip=ZYX<<expand seq. no.>>XYZ<<this request id>>&...">
	 */
	String replaceEIP(String s) {
		int ix = 0, ixf = 0, ixx;
		int eipnum = 0;
		StringBuffer bs = new StringBuffer("");

		if (s.indexOf(eipPrefix, ix) != -1) { // subsitute
			while ((ix = s.indexOf(eipPrefix, ix)) > -1) {
				bs.append(s.substring(ixf, ix) + eipPrefix + eipnum + "XYZ"
						+ reqID);
				ixf = s.indexOf("&", ix);
				// s = s.substring(0, ix) + eipPrefix + eipnum + "XYZ" + reqID +
				// s.substring(ixf); // fron not optimise version
				eipnum++;
				ix = ix + 10;
			} // while
			bs.append(s.substring(ixf));
			return bs.toString();
		} else
			return s;
	}

	/************************************
	 * 
	 * REPLACE CIP
	 * 
	 * see replaceEIP
	 */
	String replaceCIP(String s) {
		/* System.out.println("start replaceCIP"); */
		int ix = 0, ixf = 0, ixx;
		int eipnum = 0;
		StringBuffer bs = new StringBuffer("");

		if (s.indexOf(cipPrefix, ix) != -1) { // subsitute
			while ((ix = s.indexOf(cipPrefix, ix)) > -1) {
				/* System.out.println("replaceEIP - subs"); */
				bs.append(s.substring(ixf, ix) + cipPrefix + eipnum + "XYZ"
						+ reqID);
				ixf = s.indexOf("&", ix);
				// s = s.substring(0, ix) + cipPrefix + eipnum + "XYZ" + reqID +
				// s.substring(ixf); // fron not optimise version
				eipnum++;
				ix = ix + 10;
			} // while
			bs.append(s.substring(ixf));
			return bs.toString();
		} else
			return s;
	}

	/*
	 * Database actions or "display"
	 */
	String dbAction(HttpServletRequest request, HttpSession session,
			String nodeType) {
		String[] act = request.getParameterValues("act");
		act = Secure.decryptParam(act);
		String res = "";

		if (act != null) {
			String[] tbl = request.getParameterValues("tbl");
			tbl = Secure.decryptParam(tbl);
			String[] an = request.getParameterValues("an");
			an = Secure.decryptParam(an);
			String[] av = convertParams(request.getParameterValues("av"));
			String[] hn = request.getParameterValues("hn");
			hn = Secure.decryptParam(hn);
			String[] hv = convertParams(request.getParameterValues("hv"));
			hv = Secure.decryptParam(hv);
			String[] kn = request.getParameterValues("kn");
			kn = Secure.decryptParam(kn);
			String[] kv = convertParams(request.getParameterValues("kv"));
			kv = Secure.decryptParam(kv);
			String[] conid = request.getParameterValues("con");
			conid = Secure.decryptParam(conid);// db to update
			String[] u_enc = request.getParameterValues("u_enc");// CDATA

			String encodeType = "HTML";
			if (nodeType.equals("xml") || nodeType.equals("purexml"))
				encodeType = "XML";
			if (u_enc != null)
				encodeType = u_enc[0]; // XML is the standard but in CDATA field
										// no encoding is necessary

			if (!act[0].equals("dis")) {
				String userId = (String) session.getAttribute("USER");
				String GrpId = (String) session.getAttribute("GRP");

				if (!SecureLogin.checkAccess(tbl[0].toUpperCase(), GrpId,
						userId, "TABLE", session)) {
					// no privileges on DB
					System.out.println("*** access ERROR ***:" + tbl[0] + "/"
							+ GrpId + "/" + userId);
					return "<H1>NO PRIVILEGE ON TABLE (" + tbl[0] + ")</H1>";
				}
			}

			encodeAndStore(session, encodeType, hn, hv);
			encodeAndStore(session, encodeType, kn, kv);
			encodeAndStore(session, encodeType, an, av);

			if (an != null) {
				applyFunction(an, av);
			} // apply function on parameter value

			if (act[0].equals("new"))
				res = insertIntoDB(encodeType, tbl, an, av, hn, hv, conid[0]);
			if (act[0].equals("upd"))
				res = updateDB(encodeType, tbl, an, av, hn, hv, kn, kv,
						conid[0]);
			if (act[0].equals("del"))
				res = deletefromDB(encodeType, tbl, kn, kv, conid[0]);
			if (act[0].equals("dis"))
				return "";

			if (DependentNodeClear)
				Node.clearDependentNodes(tbl[0]);
		}
		return res;

	}

	/*
	 * Applies the function encoded into the attribute name e.g. PRE
	 * an[i]="encoded|attr", av[i]="v" POST an[i]="attr",
	 * av[i]=LazyFunction.encoded("v")
	 */
	static void applyFunction(String[] an, String[] av) { // an is not null
		int ix = 0;
		for (int i = 0; i < an.length; i++) {
			ix = 0;
			if ((ix = an[i].indexOf("|", ix)) > 0) { // there is a function
				String fName = an[i].substring(0, ix);
				String fAttr = an[i].substring(ix + 1, an[i].length());
				an[i] = fAttr;
				if (fName.equals("encoded")) {
					av[i] = LazyFunction.encoded(av[i]);
				} else
					an[i] += " ERROR on function name: " + fName;
			}
		}
	}

	/**
	 * Stores attribute-value pairs as session attributes (values are "cleaned")
	 * ? necessary ? TODO
	 */
	static void encodeAndStore(HttpSession session, String encodeType,
			String[] xn, String[] xv) {
		if (xn == null)
			return;
		if (verboseCache)
			System.out.println("bbbb");
		if (xn.length != xv.length) {
			System.out.println("ERROR - encodeAndStore - wrong parameters");
			return;
		}
		if (verboseCache)
			System.out.println("ccccc");
		for (int i = 0; i < xn.length; i++) {
			xv[i] = SU.cleanValue(encodeType, xv[i]); // double the quote, if
														// xml proj. replace &
														// by &amp; , < by &lt;
														// ' by ''
			session.setAttribute((String) ("!" + xn[i]), xv[i]); // to be
																	// referenced
																	// as
																	// [!attrname]
																	// in nodes
			if (verboseCharCoding)
				System.out.println("add : PARAM !" + xn[i] + "->" + xv[i]);
		}
	}

	/************************************
	 * 
	 * INSERT INTO DB
	 */
	String insertIntoDB(String encodeType, String[] intoTable,
			String[] attrNames, String[] attrValues, String[] attrNamesHidden,
			String[] attrValuesHidden, String conid) {

		String insertRequest = "";

		if (intoTable.length == 0 || attrNames.length == 0
				|| attrValues.length != attrNames.length)
			return "<p>ERROR - wrong parameters in insertIntoDB</p>";

		if (verboseModify)
			System.out.println("Preparing INSERT...");

		insertRequest = "insert into " + intoTable[0] + " (" + attrNames[0];
		for (int i = 1; i < attrNames.length; i++)
			insertRequest += "," + attrNames[i];
		if (attrNamesHidden != null) {
			for (int i = 0; i < attrNamesHidden.length; i++)
				insertRequest += "," + attrNamesHidden[i];
		}
		insertRequest += ") VALUES (" + UnicodeCollationSQL92 + "'"
				+ attrValues[0] + "'";
		for (int i = 1; i < attrValues.length; i++)
			insertRequest += "," + UnicodeCollationSQL92 + "'" + attrValues[i]
					+ "'";
		if (attrValuesHidden != null) {
			for (int i = 0; i < attrValuesHidden.length; i++)
				insertRequest += "," + UnicodeCollationSQL92 + "'"
						+ attrValuesHidden[i] + "'";
		}
		insertRequest += ")";

		if (verboseModify)
			System.out.println(insertRequest);

		QueryResult Q = DBServices.execSQLonDB(insertRequest, conid, false);

		if (Q.valid) {
			return "<b>" + Q.nbUpdated + " rows inserted</b>";
		} else { // sql error
			return "<hr/><h3>Database access error</h3>"
					+ "<p>during:<b>insert</b> execution</p>"
					// + "<p>SQL text <b>"
					// + Q.sql
					// + "</b></p>"
					+ "<p>SQLError: " + Q.msg + "</p>";
		}

	}

	/************************************
	 * 
	 * UPDATE DB
	 */
	String updateDB(String encodeType, String[] updateTable,
			String[] attrNames, String[] attrValues, String[] attrNamesHidden,
			String[] attrValuesHidden, String[] keyNames, String[] keyValues,
			String conid) {

		String updateRequest = "";

		/*
		 * accept attrName==null !!!!!!!!!!!!!!!!!!!!!!!!!!!! if
		 * (updateTable.length == 0 || attrNames.length == 0 ||
		 * attrValues.length != attrNames.length || keyNames.length == 0 ||
		 * keyValues.length != keyNames.length) return
		 * "<p>ERROR - wrong parameters in updateDB</p>";
		 */

		if (verboseModify)
			System.out.println("Preparing UPDATE...");

		updateRequest = "update  " + updateTable[0] + " set ";
		if (attrNames != null) { // exist visible attribute
			updateRequest += attrNames[0] + "=" + UnicodeCollationSQL92 + "'"
					+ attrValues[0] + "'";
			if (ns.verbose)
				System.out.println("UPDATE:" + updateRequest);

			for (int i = 1; i < attrNames.length; i++)
				updateRequest += "," + attrNames[i] + "="
						+ UnicodeCollationSQL92 + "'" + attrValues[i] + "'";
		}
		if (attrNamesHidden != null) {
			for (int i = 0; i < attrNamesHidden.length; i++) {
				if ((attrNames != null) || i != 0)
					updateRequest += ",";
				updateRequest += attrNamesHidden[i] + "="
						+ UnicodeCollationSQL92 + "'" + attrValuesHidden[i]
						+ "'";
			}
		}

		updateRequest += " where " + keyNames[0] + "=" + UnicodeCollationSQL92
				+ "'" + keyValues[0] + "'";

		for (int i = 1; i < keyNames.length; i++)
			updateRequest += " and " + keyNames[i] + "="
					+ UnicodeCollationSQL92 + "'" + keyValues[i] + "'";

		if (verboseModify)
			System.out.println(updateRequest);

		QueryResult Q = DBServices.execSQLonDB(updateRequest, conid, false);

		if (Q.valid) {
			return "<b>" + Q.nbUpdated + " rows updated</b>";
		} else { // sql error
			return "<hr/><h3>Database access error</h3>"
					+ "<p>during:<b>update</b> execution</p>"
					// + "<p>SQL text <b>"+ Q.sql+ "</b></p>"
					+ "<p>SQLError: " + Q.msg + "</p>";
		}

	}

	/************************************
	 * 
	 * DELETE FROM DB
	 */
	String deletefromDB(String encodeType, String[] delTable,
			String[] keyNames, String[] keyValues, String conid) {

		String deleteRequest = "";

		if (delTable.length == 0 || keyNames.length == 0
				|| keyValues.length != keyNames.length)
			return "<p>ERROR - wrong parameters in deleteFromDB</p>";

		if (verboseModify)
			System.out.println("Preparing DELETE...");

		deleteRequest = "delete from  " + delTable[0] + " where " + keyNames[0]
				+ "=" + UnicodeCollationSQL92 + "'" + keyValues[0] + "'";

		for (int i = 1; i < keyNames.length; i++)
			deleteRequest += " and " + keyNames[i] + "="
					+ UnicodeCollationSQL92 + "'" + keyValues[i] + "'";

		if (verboseModify)
			System.out.println(deleteRequest);

		QueryResult Q = DBServices.execSQLonDB(deleteRequest, conid, false);

		if (Q.valid) {
			return "<b>" + Q.nbUpdated + " rows deleted</b>";
		} else { // sql error
			return "<hr/><h3>Database access error</h3>"
					+ "<p>during:<b>delete</b> execution</p>"
					// + "<p>SQL text <b>"+ Q.sql+ "</b></p>"
					+ "<p>SQLError: " + Q.msg + "</p>";
		}

	}

	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {

		doGet(request, response);
	}

}

/**
 * Raised when an unauthorized access occurs
 * 
 */
class LazySecurityException extends Exception {
	LazySecurityException(String msg) {
		super(msg);
	}
}
