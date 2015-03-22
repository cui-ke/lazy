import java.io.*;
import java.text.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.sql.*;
import java.net.*;
import java.lang.reflect.*;

class SecureLogin{
    
    public static void askForPWD(HttpServletResponse response, String comment)
    throws ServletException, IOException {
        response.setContentType("text/html");
		response.setHeader("expires","Wed, 26 Feb 1997 08:21:57 GMT");
        PrintWriter out = response.getWriter();
        
        out.println("<html>");
        out.println("<head>");
        out.println("<title>Authentification</title>");
		//out.println("<meta http-equiv=\"expires\" content=\"0\">");
        out.println("</head>");
        out.println("<body>");
        out.println("<center>");
        out.println("<h1>" + comment + "</h1>");
        out.println("<font class='OraFieldText'></font>");
        out.println("<table  border='0' cellspacing = '2' bgcolor='#88FF88' >");
        out.println("<form method='POST'  action = '/lazy/ns' color='#ffffff'>");
        out.println("<tr><td>");
        out.println("User :");
        out.println("<input type='text' name='login' size='20' value='PUBLIC'/>");
        out.println("</td></tr><tr><td>");
        out.println("Password:");
        out.println("<input type='password' name='password' size='20' value='x' /> ('x' for PUBLIC)");
        out.println("</td></tr><tr><td>");
        out.println("Data Group:");
        out.println("<input type='text' name='datagroup' size='20' /> (optional)");
        out.println("</td></tr><tr><td>");
        out.println("<Input type='submit'  value='Connect'/>");
        out.println("</td></tr></form>");
        out.println("</table>");
        out.println("</center>");
        out.println("</body>");
        out.println("</html>");
        out.close();
        
    }
	
    /**
	*   Check user name and password and load grants corresponding to the default datagroup
	*   - if no login and password parameters are given, use PUBLIC as default **GF0702
	*/
    public static void checkUser(String dbURL, HttpSession session,  HttpServletRequest request, HttpServletResponse response,
    											String firstURL, boolean automaticURL) throws ServletException, IOException {
   
        String user = "PUBLIC", pwd = "", direct = "YES";   // default values for a first connection without login/pwd parameters
		String datagroup = "DEFAULT";  // **GF0702
        if (request.getParameterValues("login") != null) {
		    // user = (request.getParameter("login")).toUpperCase();
		    user = request.getParameter("login");
            pwd = request.getParameter("password");
            direct = request.getParameter("direct");
			datagroup = request.getParameter("datagroup");
           if (direct==null) direct="NO";
		}
        session.setAttribute("DIRECT", direct);
        
        try {
            
            // load user parameters
			String chkusr = 	"select defaultGrpId,lang,style,admin"
            	+ " from lazy_users "
				+ " where userid = '"+user+ "' and (pwd='"+Secure.encryptpwd(pwd)+"' or userid = 'PUBLIC')";
			// System.out.println("Checking user with query : "+chkusr);
            QueryResult Q = DBServices.execSQL(chkusr,true);
            if (!Q.valid) {
                askForPWD(response, "Connection to database failed - no answer from db server");
                return;
            }    
            Q.result.next(); // if the password was wrong --> generates an exception that is caught below
            session.setAttribute("LANG",Q.result.getString("lang"));
            session.setAttribute("STYLE",Q.result.getString("style"));
            session.setAttribute("ADMIN",Q.result.getString("admin"));
			session.setAttribute("USER", user);
 			if  (datagroup == null || datagroup.equals("") || datagroup.equals("DEFAULT"))  // **GF0702
               session.setAttribute("GRP", Q.result.getString("defaultGrpId"));
			else
			   session.setAttribute("GRP", datagroup);
            
            // load user grants -- for all data sets (groups)
	    	// See checkAccess  for details
			String ldacc = "select distinct g.grpid, n.nodeid, n.typeid " + 
					"from lazy_grants g, lazy_rolenode n " + 
					"where g.userid = '"+user+ "' and (g.roleid=n.roleid)";
			// System.out.println("ldacc="+ldacc);
			// !! SHOULD DROP ALL THE grp|node|type SESSION ATTRIBUTES FIRST !!
            Q = DBServices.execSQL(ldacc, true);
            if (Q.valid) {
                int nbRes=0;
                while (Q.result.next()) {
                    nbRes++;
					String typeid = Q.result.getString(3);
					String tableOrNodeid = Q.result.getString(2);
					if (typeid.equals("TABLE")) tableOrNodeid = tableOrNodeid.toUpperCase();
                    String k=Q.result.getString(1)+"|"+tableOrNodeid+"|"+typeid;
                    System.out.println(k+" granted");
                    session.setAttribute(k,"ok");
                }
                System.out.println("No. of grants  loaded: "+nbRes);
                Q.result.close();
				System.out.println("DB connection OK for:" + user + "\n");
            }
			else 
			    System.out.println("Unable to load access rights for :" + user + "\n");
            
            
            String identified = "YES";
            session.setAttribute("identified", identified);
            String userID = user;
            session.setAttribute("USER", userID);
            
            if(direct.equals("NO")){
                response.setContentType("text/html");
                PrintWriter out = response.getWriter();
                
                if (automaticURL) out.println("<META HTTP-EQUIV=refresh CONTENT=\"0.1; URL=" + firstURL + "\">");
                out.println("<HTML>");
                out.println("<HEAD>");
                out.println("<TITLE>Authentification</TITLE>");
                out.println("</HEAD>");
                out.println("<BODY>");
                out.println("<H1>Connection OK (" + user + ")</H1>");
                out.println("<p>You should be lead automatically to the desired node</p>");
                out.println("<p>If not, please click <a href=\""+firstURL+"\">this link</a></p>");
                out.println("</BODY>");
                out.println("</HTML>");
                
                
                out.close();
            }
        }
        catch (Exception e) {
            System.out.println("*** Error (database connection or password) for user: " + user);
            askForPWD(response, "Connection error - Retry ");
        }
    }
    
    
    public static boolean checkAccess(String nodeID, String grpID,String userID,String typeID, HttpSession session) {
       /* 
		 *  Access control is based on two tables :
		 *
		 *    GRANTS(UserId, GrpId (data set), RoleId)
		 *    ROLENODE(RoleId, NodeId, TypeId, ...)
		 *
		 *    User U, currently in data group D may instantiate node N in project P if
         *
		 *       There is a role R such that
		 *              (GRANTS(U, D, R) or GRANTS(U, 'PUBLIC', R)) or GRANTS(U, '*', R))
		 *             and 
		 *             ( ROLENODE(R, P.N, 'LAZY') or ROLENODE(R, P.*, 'LAZY') 
		 *
		 *       Role PUBLIC should normally be granted to everyone for every data group, i.e.
		 *       ForAll U : GRANTS(U, '*', 'PUBLIC') or GRANTS(U, 'PUBLIC', 'PUBLIC')  (old style)
		 *
		 *          
		 *  To accelerate access checking, when a user logs in, his or her grants are loaded in memory and
		 *  kept in the form of attribute names
		 *
         *   A session attribute named D | N | T is created if there is a role R such that
		 *
		 *      GRANTS(U, D, R) and ROLENODE(R, N, T)
		 *   
         */
		// Has the user ADMIN rights ?
        if (session.getAttribute("ADMIN") != null && ((String)session.getAttribute("ADMIN")).equals("ADMIN")) {return true;} 
		
        String grants=grpID+"|"+nodeID+"|"+typeID;
        // System.out.println(ok+":"+grants);
		
	    // Explicit access right ?
        if (session.getAttribute(grants) != null) {return true;}
		
	    // Access to all nodes of this project ?
		String allNodesInProject = nodeID;
		if (typeID.equals("LAZY")) { // only for nodes, not for tables !
	    	allNodesInProject = Node.getProjectName(nodeID)+".*";
	    	if (session.getAttribute(grpID+"|"+allNodesInProject+"|"+typeID) != null) return true;
		} 
		
	    // Is this node available whatever the current data space ?
	    if (session.getAttribute("*|"+nodeID+"|"+typeID) != null) return true;	
	    if (session.getAttribute("*|"+allNodesInProject+"|"+typeID) != null) return true;	
	    if (session.getAttribute("PUBLIC|"+nodeID+"|"+typeID) != null) return true;	
	    if (session.getAttribute("PUBLIC|"+allNodesInProject+"|"+typeID) != null) return true;	
        // Is the request comming from this same host
        if (userID.equals("LOCAL_RUN_ON_HOST")) {return true;}  
        return false;
    }
}
