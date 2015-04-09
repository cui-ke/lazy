/**
 * Created by Lin ZHANG on 21/03/14.
 */

import java.io.*;
import java.util.*;
import java.sql.*;
import java.util.regex.Pattern;

import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.sparql.syntax.*;
import com.hp.hpl.jena.sparql.lang.*;

class CompilerSparql {

    boolean debug = false;

    boolean traceBuf = true;
    boolean traceSystem = false;
    boolean traceComp = true;

    static final String compilerVersion = "4.3";

    static String url = "jdbc:oracle:thin:@cuisund:1521:cui";
    static String user = "lazy";
    static String pwd = "lazy";
    static String driver = "oracle.jdbc.driver.OracleDriver";
    static Statement stmt;
    static Connection con;
    static boolean directToDB;

    static String concatPre = ""; //gf "";
    static String concatSep = ""; //gf ", "; // ||
    static String concatPost = "";

    static String concatPreAmp = "";
    static String concatSepAmp = "&";
    static String concatPostAmp = "";

    PrintWriter out;

    String servlet = "ns";
    String originaltext = "";

    boolean fromFile;

    // for error reporting and processing

    int errInNodeDef = 0;
    int tokno = 0;
    int firstline = 1; // line no. of the first line of the current node
    
    int additionalLines = 0;  // when the tokenizer is set to recognize \n and \r as
                              // tokens, its does not count lines
                              // so we must correct the line count

    StreamTokenizer sym;
    BufferedReader txt; // to find node text
    int currenttxtline = 0;

    String laststring;

    int TT_WORD = StreamTokenizer.TT_WORD;
    int TT_NUMBER = StreamTokenizer.TT_NUMBER;
    int TT_EOF = StreamTokenizer.TT_EOF;


    boolean immediate, copyon;

    boolean todb = false;

    String nodeid, imm_node, projectid, projectConnectionID;

    StringBuffer viewtext, copybuf;

    String displaybuf, prebuf, postbuf, selectwherebuf, orderbuf, frombuf,
            plaintxtbuf, statusbuf, errorbuf, groupbuf, limitbuf, nodetypebuf,prefixbuf;


    List actionList;

    boolean attrOnly; /*
                     * to handle fields or parameters composed of a single
                     * attribute if the same attr. appears twice it must be
                     * aliased
                     */
    int nbAttrOnly = 0;
    Set attrOnlySet;
    String theAttr;
    int cachesize = -1;

    String itemsType;
    boolean itemsFound;

    String[] paranames = new String[100];
    int nbparams = 0;

    int attrCount = 0;
    int preAttrCount, dispAttrCount, postAttrCount; // number of database
    // attributes found in pre,
    // items, and post parts
    
    int complexExpressionCount; // no. of first level expressions in a node, used for naming aliasses
    boolean isComplexExpression; // needs some computation
    boolean hasVar; // true if a first level expression contains  a variable
    StringBuffer complexExpressionsInContent;
    
    boolean expressionInParam; // this expression appears as a node parameter in a link

    /*
     * look for s in the parameter list, return param index if found and
     * nbparams if not
     */

    int paraIndex(String s) {
        int x = 0;
        while (x < nbparams && !s.equals(paranames[x]))
            x++;
        return x;
    }

    CompilerSparql(Reader rdr, Reader plaintxt, PrintWriter p) {

        txt = new BufferedReader(plaintxt);
        fromFile = true;
        init(rdr, p);
    }

    CompilerSparql(String source, PrintWriter p) {

        txt = new BufferedReader(new StringReader(source));
        originaltext = source;
        fromFile = false;
        init(new StringReader(source), p);
    }

    void init(Reader rdr, PrintWriter p) {
        out = p;

        sym = new StreamTokenizer(rdr);

        sym.ordinaryChars('0','9');
        sym.ordinaryChars('-', '-');
        sym.ordinaryChars('.', '.');
        sym.ordinaryChars('/', '/');// Specifies that all characters c in the

        sym.wordChars('_', '_');
        sym.wordChars('0', '9');

        sym.slashSlashComments(true);
        sym.slashStarComments(true);
        
        additionalLines = 0; 

        next();
    }

    static void global_init() {

        ResourceBundle rb = ResourceBundle.getBundle("LazyCompiler");
        String stodb = rb.getString("todatabase");
        directToDB = stodb != null ? stodb.equals("on") : false;

        if (directToDB) {

            url = rb.getString("database.url");
            user = rb.getString("database.user");
            pwd = rb.getString("database.password");
            driver = rb.getString("database.driver");

            // adapt to different concatenation syntaxes
            String dbc = rb.getString("database.concatenation");
            if (dbc.equals("amp")) {
                concatPre = concatPreAmp;
                concatSep = concatSepAmp = "&";
                concatPost = concatPostAmp = "";
            }

            try {
                Class.forName(driver);
                con = DriverManager.getConnection(url, user, pwd);
                stmt = con.createStatement();
                System.out.println("Lazy node compiler " + compilerVersion
                        + "  (-->" + url + "/" + user + ")");
            } catch (Exception e) {
                System.out.println("*** Unable to connect to database " + url);
                System.out.println(e.toString());
                return;
            }
        }

    }

    static void ns_init(String wdriver, String wurl, String wuser, String wpwd) {

        directToDB = true;

        if (directToDB) {
            url = wurl;
            user = wuser;
            pwd = wpwd;
            driver = wdriver;
            try {
                Class.forName(driver);
                con = DriverManager.getConnection(url, user, pwd);
                stmt = con.createStatement();
                System.out.println("Lazy node compiler " + compilerVersion
                        + "  (-->" + url + "/" + user + ")");
            } catch (Exception e) {
                System.out.println("*** Unable to connect to database " + url);
                System.out.println(e.toString());
                return;
            }
        }

    }

    /*****
     *
     * Return the string made of lines 'first' to 'last' of the 'txt' Reader.
     * 'currenttxtline' holds the no. of the current line in 'txt'.
     *
     */
    String getNodeText(int first, int last) {
        try {
            int skip = first - currenttxtline - 1;
            for (int i = 0; i < skip; i++)
                txt.readLine();// skip line
            int get = last - first + 1;
            String s = "";
            for (int i = 0; i < get; i++)
                s += txt.readLine() + "\n";
            currenttxtline = last;
            return s;
        } catch (Exception e) {
            System.out.println("*** Unable to read lines from plaintext ");
            System.out.println(e.toString());
            return "error in file! (two nodes on the same line .... ?";
        }

    }

    static void closeDB() {
        try {
            if (con != null)
                con.close();
            // System.out.println("DB connection closed");
        } catch (java.sql.SQLException e) {
            System.out.println("*** DB connection close failed");
            System.out.println(e.toString());
        }

    }

    void next() {
        try {
            sym.nextToken();
            if (traceComp) 
               if (sym.ttype == TT_WORD) System.out.print(" "+sym.sval);
               else System.out.print(" "+String.valueOf((char)sym.ttype));
            tokno++;
        } catch (IOException e) {
            System.err.println("IO error");
        }

    }

    boolean term(String s) {
        return sym.ttype == TT_WORD && sym.sval.toUpperCase().equals(s.toUpperCase());
    }
    
    boolean isIntNumber() {
        if (sym.sval == null) return false;
        for (int i=0; i<sym.sval.length(); i++) { 
           char c = sym.sval.charAt(i);
           if ( c < '0' || c > '9') return false; }
        return true;
    }
    boolean startsWithDigit() {
        if (sym.sval == null) return false;
        char c = sym.sval.charAt(0); 
        return ( c >= '0' && c <= '9');

    }

    boolean isIdentifier() {
        if (sym.ttype == TT_WORD)
            if (sym.sval.equals("active") || sym.sval.equals("and")
                    || sym.sval.equals("by") || sym.sval.equals("cachesize")
                    || sym.sval.equals("define") || sym.sval.equals("delete")
                    || sym.sval.equals("desc") || sym.sval.equals("distinct")
                    || sym.sval.equals("end") || sym.sval.equals("exists")
                    || sym.sval.equals("expand") || sym.sval.equals("forall")
                    || sym.sval.equals("from") || sym.sval.equals("group")
                    || sym.sval.equals("href") || sym.sval.equals("in")
                    || sym.sval.equals("include") || sym.sval.equals("insert")
                    || sym.sval.equals("is") || sym.sval.equals("items")
                    || sym.sval.equals("like") || sym.sval.equals("node")
                    || sym.sval.equals("not") || sym.sval.equals("null")
                    || sym.sval.equals("on") || sym.sval.equals("open")
                    || sym.sval.equals("or") || sym.sval.equals("order")
                    || sym.sval.equals("post") || sym.sval.equals("pre")
                    || sym.sval.equals("project")
                    || sym.sval.equals("selected") || sym.sval.equals("set")
                    || sym.sval.equals("update"))
                return false;
            else
                return true;
        else
            return false;
    }

    // //////////////////////////// Parser ///////////////////////////////

    /*
     * startrule = "define" [project] { node_def } "end".
     */
    void startrule() {
        if (term("define")) // this define is used in batch mode - Page10
        {
            next();

            if (term("project"))
                project();
            else {
                projectid = "vers30";
                projectConnectionID = "DICTLAZY";
                System.out.println("add a project id after define !!! ");
            }

            System.out.println("project: " + projectid + " connectionId: "
                    + projectConnectionID);

            while (term("node"))
            {
                errInNodeDef = 0;
                errorbuf = "";
                node_def();
            }

            if (term("end")) {
                next();
            } else {
                err("'end' missing");
                invalideLastNode();
            }
            out.println("commit;");

        } 
        else
            err("'define' missing");
        if (fromFile)
            closeDB();
    }

    /*
     * project = "project" project_identifier [ '[' nodetype =
     * (xml|html|purexml|purehtml) ']' ]
     */

    void project() {
        next();
        projectid = sym.sval; // save_projectid
        /* Read project connectionid from database */
        projectConnectionID = "DICTLAZY"; // Nice name isn't it ?
        String getConnectionIDReq = "select dbconnection from lazy_projects where projectid='"
                + projectid + "'";
        try {
            java.sql.ResultSet r = stmt.executeQuery(getConnectionIDReq); // java.sql.statement
            if (r.next()) {
                projectConnectionID = r.getString("dbconnection");
            }
            else {
                // If the project does not exist yet, create it with reasonable
                // default values
                System.out.println("Project " + projectid
                        + " not defined in Lazy dictionary (lazy_projects)");
                String createProject = "insert into lazy_projects(projectid, nodetype, dbconnection, xslurl, cssurl, bkgndurl) "
                        + " values ('"
                        + projectid
                        + "','html','DICTLAZY','xsl/lazy.xsl','css/lazy.css','bckgnd/neutral.jpg')";
                stmt.executeUpdate(createProject);
                System.out
                        .println(" ... Created with nodetpye=html, dbconnection=DICTLAZY, cssurl=css/lazy.css bkgndurl=bckgnd/neutral.jpg");
                String grantAccess = "insert into lazy_rolenode(roleid, nodeid, typeid, display) "
                        + "values ('PUBLIC', '"
                        + projectid
                        + ".*', 'LAZY', 'NO')";
                stmt.executeUpdate(grantAccess);
                System.out
                        .println(" ... Access granted to role PUBLIC on all nodes");
                r.close();
            }
        } catch (SQLException se) {
            System.out
                    .println("Unable to access Lazy dictionary (lazy_projects)"
                            + " using connection DICTLAZY as default");
        }
        next();
        // '[' { projectattribute = value } ']'
        if (sym.ttype == '[') {
            next();
            if (term("nodetype")) {
                next();
                if (sym.ttype == '=')
                    next();
                else
                    err("'=' missing after project attribute");

                if (term("html") || term("purehtml") || term("xml")
                        || term("purexml")) {
                    // Update the project definition
                    String updateProj = "update lazy_projects set nodetype = '"
                            + sym.sval + "' where projectid='" + projectid
                            + "'";
                    try {
                        stmt.executeUpdate(updateProj);
                    } catch (SQLException se) {
                        System.out
                                .println("Unable to  update a project attribute : "
                                        + updateProj);
                    }
                    next();
                } else
                    err("value of project attribute 'nodetype' must be html or purehtml or xml or purexml");
            } else
                err("project attribute must be 'nodetype' (in this version)");
            if (sym.ttype == ']')
                next();
            else
                err("']' missing");
        }


   }




    /*
     * node_def = "node" node_identifier parameters [cachesize=number]
     * new_content from_part on_open
     */

    void node_def() {
        // "node"
        nodetypebuf = "SPARQL";
        firstline = sym.lineno() + additionalLines;
        errorbuf = "";
        actionList = new ArrayList();

        next();
        if (sym.ttype == TT_WORD) {
            nodeid = sym.sval; // %save_nodeid
            System.out.println("node " + sym.sval);

            next();
            parameters();
            // defaultvalue();

            cachesize = -1;
            if (term("cachesize")) {
                next();
                if (sym.ttype == '=') {
                    next();
                    if (isIntNumber()) { 
                        try {cachesize = Integer.parseInt(sym.sval);}
                        catch (NumberFormatException e) {cachesize = 0;}
                        next();
                    } else
                        err("missing number after =");
                } else
                    err("missing '=' after cachesize");
            }

            attrCount = 0;
            attrOnlySet = new TreeSet();

            new_content();

            if (probablyStillInContent()) { // try to reach the from part or the
                // next node
                err("from part expected here, or missing ',' between fields");
                while (probablyStillInContent())
                    next();
            }

            from_part();
            //group_part();
            on_open();

            //displaybuf = defineHtmlContent(originaltext);
//             orderbuf = definePrefix(originaltext);



            if (errInNodeDef == 0)
                statusbuf = "VALID";
            else {
                statusbuf = "INVALID";
                prebuf = "<hr><blockquote>node " + projectid + ".<b>"
                        + nodeid + "</b>"
                        + " has compilation errors</blockquote><hr>";
                displaybuf = "";
                postbuf = "";
                frombuf = "";
                selectwherebuf = "select (1 as $lazy__x) where {rdf:type rdf:type rdf:Property.} limit 1";
                orderbuf = "NODEF";
                groupbuf = "NODEF";
            }

            // try to reach the beginning of the next node's definition
            // -- this is to make sure that we copy all the input text, even if
            // there are errors
            //

            while (!term("node") && !term("end") && sym.ttype != TT_EOF) {
                next();
                if (term("display")) {
                    next();
                    if (term("node"))
                        next();
                } // skip these @!!#! "display node"
            }

            // if (fromFile)
            int lastline = sym.lineno() - 1 + additionalLines;
            plaintxtbuf = doubleQuotes(getNodeText(firstline, lastline));
            
            // check the syntax
            // String squery = selectwherebuf+ " \n" + groupbuf+ " \n"+ orderbuf+ " \n" + limitbuf
            storeCompiledNode();

        } else
            err("<identifier> missing after 'node'");
    }


    /*
     * copy the first {} content
     */
    String defineHtmlContent(String s)
    {
        String result = "";
        int first = s.indexOf("{");
        int last = s.indexOf("}");
        result = s.substring(first+1,last);
        return  result;
    }



    /*
     *
     * parameters = [ "[" parameter { "," parameter } "]" ]
     */
    void parameters() {
        nbparams = 0;
        if (sym.ttype == '[') {
            next();
            parameter();
            while (sym.ttype == ',') {
                next();
                parameter();
            }
            if (sym.ttype == ']') {
                next();
            } else
                err("',' or ']' expected in or after param list");
        }
    }

    /*
     * parameter = identifier
     */
    void parameter() {
        if (sym.ttype == TT_WORD) {
            paranames[nbparams] = sym.sval;
            nbparams++;
            next();
        }
    }

    /*
     * Return false if we really reached the end of the content part i.e. if we
     * are at the beginning of the from part, at the beginning of a node
     * declaration at the end of declarations or at the end of the file. Return
     * true if we are probably still in the content part (and there was a ','
     * missing).
     */
    boolean probablyStillInContent() {
        return !(term("from") || term("node") || term("end")
                || term("selected") || term("ordered") || sym.ttype == TT_EOF);
    }

    /*
     * new_content = field { "," field }
     */
    void new_content() {
        itemsFound = false;
        complexExpressionsInContent = new StringBuffer();
        complexExpressionCount = 0;
        attrCount = 0;
        startcopy();
        echo(concatPre);
        field();
        while (sym.ttype == ',') {
            next();
            field();
        }
        echo(concatPost);
        stopcopy();

        if (itemsFound) {
            postbuf = copybuf.toString();
            postAttrCount = attrCount;
        } else {
            prebuf = copybuf.toString();
            preAttrCount = attrCount;
            displaybuf = "";
            postbuf = "";
        }
    }

    /*
     * field = ["expand"] "href" link '(' field_list ')' | "include" link |
     * content** | "active" "href" active_link '(' field_list ')'** |
     * set_attribute** | on_action
     */

    void field() {

        if (term("href") || term("expand")) {

            link();
            if (sym.ttype == '(') {
                next();
                field();
                while (sym.ttype == ',') {
                    next();
                    field();
                }
                if (sym.ttype == ')')
                    next();
                else
                    err("',' or ')' expected in or after field list");
            } else
                err("missing '('  after href ");
            echo(concatSep + "</a>");
        } else if (term("include"))
            link();
        else if (term("active")) {
            echo(concatSep + "<form action=\"" + servlet
                    + "\" method=\"post\">");
            active_link();
            if (sym.ttype == '(') {
                next();
                field();
                while (sym.ttype == ',') {
                    next();
                    field();
                }
                if (sym.ttype == ')')
                    next();
                else
                    err("',' or ')' expected in or after field list (active link)");
            } else
                err("missing '('  after href ");
            echo(concatSep + "</form>");
        } else if (term("set"))
            set_attribute();
        else if (term("on"))
            on_action();

        else
            content();
    }

    /*
     * link = ( "include" | "expand" | "href" [ "in" frameident]) nodeident [
     * "[" simple_expression_OR_INCLUDE_list "]" ] [target]
     *
     * SHOULD BE CALLED node_reference
     *
     * an "href" link of the form "href nodeName[expr_1, expr_2, ...]" will
     * produce the following HTML code in a node instance:
     *
     * <a href="servletName?a=nodeName&u=$expr_$&u=$expr_2$&u=...">
     *
     * an "expand href" will produce
     *
     * <a
     * href="servletName?eip=ZYX0000XYZ&a=nodeName&u=$expr_$&u=$expr_2$&u=...">
     *
     * an "include" will produce
     *
     * <<??include a=nodeName&u=u=$expr_$&u=$expr_2$&u=...//??>>
     *
     * where $param_i$ is the result of compiling the expression param_i
     */

    void link() {
        boolean isInclude = false;
        if (term("href") || term("include") || term("expand")) {
            if (term("include")) {
                isInclude = true;
                echo(concatSep + ns.includePrefix );
                next();
            } else {
                if (term("expand")) {
                    echo(concatSep + "<a href=\"" + servlet
                            + "?eip=ZYX0000XYZ&amp;a="); // xml jg
                    next();
                    next();
                } // skip href !!
                else {// href
                    next();
                    if (term("in")) {
                        next();
                        if (sym.ttype == TT_WORD) {
                            {
                                echo(concatSep + "<a target=\"" + sym.sval
                                        + "\" href=\"" + servlet + "?a=");
                                next();
                            }
                        } else
                            err("illegal frame identifier");
                    } else {
                        echo(concatSep + "<a href=\"" + servlet + "?a=");
                    }
                }
            }
            if (sym.ttype == TT_WORD) {
                {
                    String nodeident = sym.sval;
                    next();
                    if (nodeident.indexOf(".") == -1) { // implicit call to a
                        // node of this project
                        nodeident = projectid + "." + nodeident;
                    }
                    echo(nodeident + "");
                } // xml jg //gf suppress &amp;u=
            } else
                err("illegal node identifier");
            if (sym.ttype == '[') {
                next();
                echo(concatSep + "&amp;u=");
                if (term("include"))
                    link();
                else {
                    echo(concatSep);
                    // if !isInclude echo(startIRIEncoding)                    
                    first_level_simple_expression_in_Param();
                    // if !isInclude echo(stopIRIEncoding)
                }
                while (sym.ttype == ',') {
                    next();
                    echo(concatSep + "&amp;u=");
                    if (term("include"))
                        link();
                    else {
                        echo(concatSep);
                        // if !isInclude echo ...
                        first_level_simple_expression_in_Param();
                        // if !isInclude echo ...
                    }// xml jg
                }
                if (sym.ttype != ']')
                    err("missing ',' in or ']' after node parameter list");
                // else
                next();
            }
            if (isInclude)
                echo(concatSep + "//??>>");
            else
                echo(concatSep + "\">");
        }
    }

    /*
     * active_link ::= "active" "href" nodeident [ "[" ( "!" input_var_param |
     * include | simple_expression ) "]" ]
     *
     * compiled to
     *
     * '<input type="hidden" name="a" value=" [[ nodeident ]] ">'
     *
     * '<input type="hidden" name="u"
     * value="', '<<[??ivar-"+input_variable+"??]>>', '">' or '<input
     * type="hidden" name="u" value="', [[ simple_expression ]], '">' or '<input
     * type="hidden" name="u" value="', [[ include ]], '">' ...
     */

    void active_link() {
        // current symbol is "active"
        next();
        if (term("href"))
            next();
        else
            err("missing href after active");

        if (sym.ttype == TT_WORD) {
            String nodeident = sym.sval;
            next();
            if (nodeident.indexOf(".") == -1) { // implicit call to a node of
                // this project
                nodeident = projectid + "." + nodeident;
            }
            echo(concatSep + "<input type=\"hidden\" name=\"a\" value=\""
                    + nodeident + "\"/>"); // xml jg
        } else
            err("illegal node identifier");
        if (sym.ttype == '[') {
            next();
            echo(concatSep + "<input type=\"hidden\" name=\"u\" value=\"");
            if (sym.ttype == '!') {
                echo(concatSep);
                inputVarParam();
            } else if (term("include"))
                link();
            else {
                echo(concatSep);
                // echo uri encode
                first_level_simple_expression_in_Param();
            }
            echo(concatSep + "\"/>"); // xml jg

            while (sym.ttype == ',') {
                next();
                echo(concatSep
                        + "<input type=\"hidden\" name=\"u\" value=\"");
                if (sym.ttype == '!') {
                    echo(concatSep);
                    inputVarParam();
                } else if (term("include"))
                    link();
                else {
                    echo(concatSep);
                    // echo uri encode
                    first_level_simple_expression_in_Param();
                }
                echo(concatSep + "\"/>"); // xml jg
            }
            if (sym.ttype != ']')
                err("missing ',' in or ']' after node param list (active_link)");
            else
                next();
        }

    }

    /*
     * inputVarParam = "!" identifier
     */
    void inputVarParam() {
        next();
        if (sym.ttype == TT_WORD) {
            echo("<<[??ivar-" + sym.sval + "??]>>");
            next();
        } else
            err("variable name expected after !");
    }

    /*
     * set_attribute = "set" attrident "=" (input_field | simple_expr)
     * set_attribute = "set" "parameter" "=" (input_field | simple_expr) // do
     * display action jg set_attribute = "set" "parameter_encoding" "="
     * (input_field | simple_expr) // to modify encoding jg
     */
    void set_attribute() {
        next();
        if (sym.ttype == TT_WORD) {
            String parameterName = ""; // jg
            String fieldName = "av";
            parameterName = sym.sval; // jg
            if (parameterName.equals("parameter")) {
                fieldName = "u";
            } // for a parameter U=... jg
            if (parameterName.equals("parameter_encoding")) {
                fieldName = "u_enc";
                parameterName = "parameter";
            } // for a parameter_encoding u_enc=... jg
            next();

            if (sym.ttype == '(') {
                next(); // it's a function like encoded(pwd)
                if (sym.ttype == TT_WORD) {
                    parameterName += "|" + sym.sval;
                    next();
                    if (sym.ttype == ')')
                        next();
                    else
                        err("missing ')' ");
                } else
                    err("illegal attribute identifier");
            }

            if (sym.ttype == '=')
                next();
            else
                err("missing '=' ");

            if (term("textfield") || term("textarea") || term("select")
                    || term("free")) {
                if (!(parameterName.equals("parameter"))) {
                    echo(concatSep
                            + "<input type=\"hidden\" name=\"an\" value=\"<<$$"
                            + parameterName + "$$>>\"/>");
                } // xml jg not a display
                input_field(fieldName);// jg select
            } else {
                if (!parameterName.equals("parameter")) { // a update hidden
                    echo(concatSep
                            + "<input type=\"hidden\" name=\"hn\" value=\"<<$$"
                            + parameterName + "$$>>\"/>");
                    echo(concatSep
                            + "<input type=\"hidden\" name=\"hv\" value=\"<<$$"
                            + concatSep); // jg
                    first_level_simple_expression();
                    echo(concatSep + "$$>>\"/>"); // xml jg
                } else { // a display or parameter_encoding
                    echo(concatSep + "<input type=\"hidden\" name=\""
                            + fieldName + "\" value=\"" + concatSep); // jg
                    first_level_simple_expression();
                    echo(concatSep + "\"/>"); // xml jg
                }
            }
        } else
            err("illegal attribute identifier");
    }

    /*
     * input_field = "textfield" "(" simple_expr [ "," simplexpr ] ")" |
     * "textarea" "(" simple_expr "," simple_expr [ "," simplexpr ] ")" |
     * "free    " "(" <element> "(" field ")" ")" | "select" "(" simple_expr ","
     * simple_expr [ "," simplexpr ] | include ... ")"
     */
    void input_field(String fieldName) { // jg
        if (term("textfield")) {
            next();
            if (sym.ttype == '(')
                next();
            else
                err("missing '('");
            echo(concatSep + "<input type=\"text\" name=\"" + fieldName
                    + "\" size=\"" + concatSep); // jg
            first_level_simple_expression();
            if (sym.ttype == ',') { // initial value of the field
                echo(concatSep + "\" value=\"" + concatSep);
                next();
                first_level_simple_expression();
            }
            echo(concatSep + "\"/>"); // xml jg
            if (sym.ttype == ')')
                next();
            else
                err("missing ')'");
        } else if (term("textarea")) {
            next();
            if (sym.ttype == '(')
                next();
            else
                err("missing '(' after 'textarea'");
            echo(concatSep + "<textarea name=\"" + fieldName + "\" rows=\""
                    + concatSep); // jg
            // nb. of columns
            first_level_simple_expression();
            echo(concatSep + "\" cols=\"" + concatSep);
            // skip ','
            next();
            // nb of rows
            first_level_simple_expression();
            echo(concatSep + "\">");
            // text (not required)
            if (sym.ttype == ',') {
                next();
                echo(concatSep);
                first_level_simple_expression();
            }
            echo(concatSep + "</textarea>");
            if (sym.ttype == ')')
                next();
            else
                err("missing ')'");
        } else if (term("free")) {
            next();
            if (sym.ttype == '(')
                next();
            else
                err("missing '(' after 'free'");
            field();
            if (sym.ttype == ')')
                next();
            else
                err("missing ')'");
        }
        if (term("select")) { // select field JG
            next();
            if (sym.ttype == '(')
                next();
            else
                err("missing '('");
            echo(concatSep + "<select name=\"" + fieldName + "\" >"); // jg
            if (term("include")) {
                link(); // include the option list
            } else {
                echo(concatSep + "<option>" + concatSep);
                first_level_simple_expression();
                echo(concatSep + "</option>");
                while (sym.ttype == ',') {
                    next();
                    echo(concatSep + "<option>" + concatSep);
                    first_level_simple_expression();
                    echo(concatSep + "</option>");
                }
            }
            echo(concatSep + "</select>"); // xml jg
            if (sym.ttype == ')')
                next();
            else
                err("missing ')'");
        }
    }

    /*
     *
     * on_action = "on" String-expr "do" db_action (tableident) [ "[" keyvalue
     * {"," keyvalue} "]" ] on_action = "on" String-expr "do" "display" "node"
     * // jg
     */
    void on_action() {
        next(); // skip "on"
        echo(concatSep + "<input type=\"submit\" name=\"bidon\" value=\""
                + concatSep);
        first_level_simple_expression();
        echo(concatSep + "\"/>"); // xml jg

        if (term("do"))
            next();
        else
            err("missing 'do'");
        String dbaction = "";

        if (term("insert"))
            dbaction = "new";
        else if (term("delete"))
            dbaction = "del";
        else if (term("update"))
            dbaction = "upd";
        else if (term("display") || term("navigate"))
            dbaction = "dis";
        else
            err("missing insert or delete or update or display or navigate after 'do'");

        // if (!dbaction.equals("dis")){ // not "do display" jg
        echo(concatSep + "<input type=\"hidden\" name=\"act\" value=\"<<$$"
                + dbaction + "$$>>\"/>"
                + "<input  type=\"hidden\" name=\"con\" value=\"<<$$"
                + projectConnectionID + "$$>>\"/>");
        // } //xml jg [[?!A_DB]]

        next();

        if (dbaction.equals("dis")) {
            if (term("node"))
                next(); // for backward compatiblity allow "node" after
            // "display"
        } else { // true db action
            if (sym.ttype == TT_WORD) {
                echo(concatSep
                        + "<input type=\"hidden\" name=\"tbl\" value=\"<<$$"
                        + sym.sval + "$$>>\"/>"); // xml jg
                next();
            }

            else
                err("missing table identifier in db operation");

            if (sym.ttype == '[') {
                do {
                    next();
                    if (sym.ttype == TT_WORD) {
                        keyvalue();
                    } else {
                        err("key attribute identifier expected");
                    }

                } while (sym.ttype == ',');

                if (sym.ttype == ']')
                    next();
                else
                    err("',' or ']' expected in or after key attribute list");
            }
        }
    }

    /*
     * keyvalue = keyattrident [ "=" first_level_simple_expression ]
     */
    void keyvalue() {
        String attrName = sym.sval;
        echo(concatSep + "<input type=\"hidden\" name=\"kn\" value=\"<<$$"
                + attrName + "$$>>\"/>");// xml jg
        next();
        if (sym.ttype == '=') {
            next();
            echo(concatSep
                    + "<input type=\"hidden\" name=\"kv\" value=\"<<$$"
                    + concatSep);
            first_level_simple_expression();
            echo(concatSep + "$$>>\"/>");// xml jg
        } else {
            echo(concatSep
                    + "<input type=\"hidden\" name=\"kv\" value=\"<<$$"
                    + concatSep + attrName);
            //
            // Still this tricky thing to avoid ambiguities in the 'order by'
            // clause
            // when the same column is selected twice
            //
            if (!attrOnlySet.add(attrName)) {
                nbAttrOnly++;
                echo(" as ZZZZ_00" + nbAttrOnly);
            }
            echo(concatSep + "$$>>\"/>");// xml jg
        }
    }

    /*
     * When there is a comma missing between fields, try to continue analysis
     * until we reach a ')' or a '}' or a keyword like 'node', 'from', etc.
     */
    boolean probablyStillInPar() {
        return probablyStillInContent() && sym.ttype != ')' && sym.ttype != '}';
    }

    /*
     * content = element_type [ "(" [ field { , field } ] ")" ] |
     * first_level_simple_expression | "{" items_content "}".
     */

    void content() {
        String elem_name = "";

        if (sym.ttype == '<') {
            elem_name = element_type();

//            if(verboseSparql)
//                System.out.println("elem_name == "+ elem_name + " , next == "+ sym.sval);

            if (sym.ttype == '(') {
                next();
                if (sym.ttype != ')') {
                    echo(concatSep + ">"); // ends tag
                    field();

                    while (sym.ttype == ',') {
                        next();
                        field();
                    }

                    if (sym.ttype == ')') {
                        next();
                    } else
                        err("missing ',' in or ')' after subfield list");
                    echo(concatSep + "</" + elem_name + ">");
                } else { // empty list
                    /* HTML specific ! */
                    if (elem_name.equals("hr") || elem_name.equals("HR")
                            || elem_name.equals("br") || elem_name.equals("BR"))
                        echo(concatSep + ">");
                    else
                        echo(concatSep + "/>");
                    next();
                }
            } else
                // tag alone
                echo(concatSep + ">");
        } else if (sym.ttype == '{')
                items_content();
        else {
            echo(concatSep);
            first_level_simple_expression();
        }
    }

    boolean probablyStillInItems() {
        return probablyStillInContent() && sym.ttype != '}';
    }


    /*
     * items_content = field {"," field }
     */
    void items_content() {
        if (!itemsFound) {
            itemsFound = true;

            // end pre-part
            echo(concatPost);
            stopcopy();
            prebuf = copybuf.toString();
            preAttrCount = attrCount;

            // start item-part
            attrCount = 0;
            attrOnlySet = new TreeSet();
            startcopy();
            echo(concatPre);
            next();

            field();
            /***
             * TEMP REMOVE while (sym.ttype == ',' || probablyStillInItems()) {
             * if (sym.ttype == ',') next(); else
             * err("missing ',' between fields in {} (probably)"); field(); }
             ***/
            while (sym.ttype == ',') {
                next();
                field();
            }

            if (sym.ttype == '}')
                next();
            else
                err("',' or '}' expected in or after field list");

            echo(concatPost);
            displaybuf = copybuf.toString();
            dispAttrCount = attrCount;

            // start post-part
            attrCount = 0;
            attrOnlySet = new TreeSet();
            startcopy();
            echo(concatPre);
        } else
            err("'{ ... }' found twice in the same node definition");
    }

    /*
     * element_type = "<" elem_type_ident { elem_type_ident "="
     * simple_expression } ">"
     */
    String element_type() {
        String eIdent = "";
        next();
        if (sym.ttype == TT_WORD) {
            eIdent = elem_type_ident();
            echo(concatSep + "<" + eIdent + "");

            while (sym.ttype == TT_WORD) {
                String attrIdent = elem_type_ident();
                echo(concatSep + " " + attrIdent + "=\"" + concatSep);

                if (sym.ttype == '=')
                    next();
                else
                    err("missing '=' after element attribute name");
                first_level_simple_expression();
                while (sym.ttype == ',') {
                    next();
                    echo(concatSep);
                    first_level_simple_expression();
                    // System.out.print("--" + sym.ttype + "--");

                }
                echo(concatSep + "\"");
            }
            // echo("concatSept+">");
            // we don't know yet if the tag ends with > or /> (empty tag)
            if (sym.ttype == '>')
                next();
            else
                err("missing '>' after style function name");
        } else
            err("missing  element type identifier after '<'");
        return eIdent;
    }

    /*
     * elem_type_ident = word { (':' | '-') word }
     */
    String elem_type_ident() {
        // PREcondition: sym.ttype == TT_WORD
        //
        // BUG: white spaces within the identifier are accepted before and
        // after : and - .
        //
        String id = sym.sval;
        next();
        while (sym.ttype == ':' || sym.ttype == '-') {
            id += (char) sym.ttype;
            next();
            if (sym.ttype == TT_WORD) {
                id += sym.sval;
                next();
            }
        }
        return id;
    }

    /*
     * A simple expression that appears as a node parameter in a link
     */
    void first_level_simple_expression_in_Param() {
        expressionInParam = true;
        first_level_simple_expression_gen();
    }
    
    /*
     * A simple expression that appears at the first level in an element, but
     * not as a node parameter in a link
     */
    void first_level_simple_expression() {
        expressionInParam = false;
        first_level_simple_expression_gen();
    }
        
    /*
     *  A simple expression that appears at the first level in an element 
     */
    void first_level_simple_expression_gen() {
    
        attrOnly = true;
        isComplexExpression = false;
        hasVar = false;
        begincopyPush();
        simple_expression();

        if (isComplexExpression) {  // the expression will go into the select instrution
            complexExpressionCount++;
            complexExpressionsInContent.append("(");
            //complexExpressionsInContent.append("str(");
            complexExpressionsInContent.append(copybuf);
            //complexExpressionsInContent.append(") ");
            complexExpressionsInContent.append(" as ?lazy__"+complexExpressionCount+") ");
            endcopyPush();
            if (expressionInParam)
                // if in a node parameter, put the full value of the variables (internal form)
                copybuf.append(ns.sparqlVarPrefix + "lazy__"+complexExpressionCount + ns.varSuffix);
            else
                // ohterwise put the output value (external forme)
                copybuf.append(ns.sparqlOutVarPrefix+"lazy__"+complexExpressionCount+ns.varSuffix); 
        } else { // the expression is a variable, a parameter or a string
            String simpleExprVal = copybuf.toString(); 
            endcopyPush();
            
            // we want the output form of the parameter if it is alone at the first level
            if (! expressionInParam) simpleExprVal = simpleExprVal.replace(ns.paramPrefix,ns.outParamPrefix);
            
            if (hasVar) { // the expression is a variable only, must appear in the 'select'
                          // so put it into complexexpressionincontent
                complexExpressionsInContent.append(" "+simpleExprVal+" ");
                if (! expressionInParam) 
                    // the var placeholder must be replaced by the output form of the value
                    simpleExprVal = simpleExprVal.replace(ns.sparqlVarPrefix,ns.sparqlOutVarPrefix);
            }
            
            // if it's a string, remove the quotes because it won't go into the query        
            if (simpleExprVal.charAt(0) == '"') simpleExprVal = simpleExprVal.substring(1,simpleExprVal.length()-1);
            
            copybuf.append(simpleExprVal);
        }
            
    }

    /*
     * simple_expression = [+|-] term { +|- term }
     */
    void simple_expression() {
        if (sym.ttype == '+')
            next();
        else if (sym.ttype == '-') {
            attrOnly = false;
            isComplexExpression = true;
            echo("-");
            next();
        }
        term();
        while (sym.ttype == '+' || sym.ttype == '-') {
            isComplexExpression = true;
            attrOnly = false;
            isComplexExpression = true;
            echo("" + (char) sym.ttype);
            next();
            term();
        }
    }

    /*
     * term = factor {*|/ factor }
     */

    void term() {
        factor();
        while (sym.ttype == '*' || sym.ttype == '/') {
            attrOnly = false;
            isComplexExpression = true;
            echo("" + (char) sym.ttype);
            next();
            factor();
        }
    }

    /*
     *
     * factor = stringconstant |numberconstant |function_identifier "("
     * simple_exression { "," simple_exression } ")" | [collection_ident '.']
     * attribute_ident | parameter_ident |'(' expression ')' | '[[' ['!' | '?']
     * variable_ident ']]'
     */
    void factor() {
        if (sym.ttype == '"') {
            quoted_string();
            attrOnly = false;
        } else if (startsWithDigit()) {
            number_literal();
            attrOnly = false;
        } else if (sym.ttype == '(') {
            attrOnly = false;
            isComplexExpression = true;
            echo("(");
            next();
            expression();
            if (sym.ttype == ')') {
                echo(")");
                next();
            } else
                err("')' missing");
        } else if (term("not")) {
            echo(" not ");
            isComplexExpression = true;
            next();
            condition();
        } else if( sym.ttype == '?' || sym.ttype == '$') { // used only in sparql, query values
            // echo("?");
            //gf ((
            attrOnly = false;
            hasVar = true;
            next();
            if (sym.ttype == TT_WORD) {
                echo(ns.sparqlVarPrefix + sym.sval + ns.varSuffix);
                next();
            } else
                err("variable name expected after ?");
            //gf ))
            
        } else if (sym.ttype == TT_WORD) {
            String ident1 = sym.sval;
            next();
            if (sym.ttype == '(' || sym.ttype == ':') { // function call
                attrOnly = false;
                isComplexExpression = true;
                echo(ident1);
                if(sym.ttype == ':'){ // this is a namespace:funcname form
                    echo(":");
                    next();
                    if(sym.ttype == TT_WORD) {
                        echo(sym.sval);
                        next();
                    }
                    else err("identifier missing after namespace: ");
                }
                if (sym.ttype != '(') err("( missing after funtion name");
                echo("(");
                next();
                simple_expression_list();
                if (sym.ttype == ')') {
                    echo(")");
                    next();
                } else
                    err("')' missing at end of param. list");
            } else{ // attribute or parameter
                int pi = paraIndex(ident1);
                if (pi < nbparams) {
                    echo(ns.paramPrefix + pi + ns.paramSuffix);
                    attrOnly = false;
                } else {
                    echo(ident1);
                    attrCount++;
                    theAttr = ident1;
                }

            }
        } else if (sym.ttype == '[') { // global or input variable
            attrOnly = false;
            String varCat = ""; // inputVar = false;
            next();
            // if (sym.ttype == '[') {
            // next();
            if (sym.ttype == '!') { // input variable
                varCat = "!"; // inputVar = true;
                next();
            } else if (sym.ttype == '?') {
                varCat = "?";
                next();
            }
            if (sym.ttype == TT_WORD) {
                echo(ns.varPrefix + varCat + sym.sval + ns.varSuffix);
                // else echo("<<[??var-"+sym.sval+"??]>>");
                next();
            } else
                err("variable name expected after [");
            // if (sym.ttype == ']') next();
            if (sym.ttype == ']')
                next();
            else
                err("] expected after [variable");
            // }
            // else err("] expected after [");
        }
        /***
         * TEMP REMOVE else if (sym.ttype != '}' && sym.ttype != ')' &&
         * sym.ttype != ']') {
         * err("Invalid symbol "+sym.ttype+" encountered (skipped)"); next(); }
         ***/
        else
            err("string or number or function or variable expected (factor)");
    }

    /*
     * expression = condition
     */
    void expression() {
        condition();
    }

    void quoted_string() {
        if (sym.ttype == '"') {
            laststring = sym.sval;
                // SQL echo("" + quadrupleQuotes(sym.sval) + "");
            echo("\"" + doubleQuotes(sym.sval.replace("\"","\\\"")) + "\""); // SPARQL
            next();
        } else
            err("illegal string");
    }

    void number_literal() {
        if (startsWithDigit()) {
            if (isIntNumber())
               echo(sym.sval);
            else
                // TODO: other formats not handled
                echo("0");
            next();
        } else
            err("illegal number literal");
    }

    void identifier() {
        if (sym.ttype == TT_WORD) {
            echo(sym.sval);
            next();
        } else
            err("illegal identifier");
    }

    void simple_expression_list() {
        simple_expression();
        while (sym.ttype == ',') {
            echo(",");
            next();
            simple_expression();
        }
    }

    /*
     * from_part = "from" collection_id [alias_id] { ',' collection_id
     * [alias_id] } [select_part();] [order_part]
     */

    void from_part() {

        if (term("from")) {
            startcopy();
            next();
            if (term("named")) {
                echo("named "); next();
                iri();
            } 
            else if (sym.ttype=='<' 
                    || (sym.ttype==TT_WORD && !term("select") && !term("distinct") && !term("where"))
                    || sym.ttype==':' ) iri();
            frombuf = copybuf.toString();
            stopcopy();

            select_part(); // JG           
            group_part(); // jg
            order_part(); // jg
            limit_part(); // gf
            // System.out.println("after order:"+sym.sval);
            if (sym.ttype != TT_EOF && !term("end"))
                err("Unexpected symbol");
            
            if (errInNodeDef == 0) {
            
                // check the SPARQL syntax of this part

				String query = Node.getProjectPrefix(projectid);
				query += ("\n" + selectwherebuf.replace(ns.paramPrefix,"?PARAM_").replace(ns.paramSuffix," "));
				if (!groupbuf.equals("NODEF")) query += ("\n" + groupbuf.replace(ns.paramPrefix,"?PARAM_").replace(ns.paramSuffix," "));
				if (!orderbuf.equals("NODEF")) query += ("\n" + orderbuf.replace(ns.paramPrefix,"?PARAM_").replace(ns.paramSuffix," "));
				if (!limitbuf.equals("NODEF")) query += ("\n" + limitbuf.replace(ns.paramPrefix,"1").replace(ns.paramSuffix," "));
		
				try {
					Query e = QueryFactory.create(query);
					System.out.println("--------"+e);
				} catch (com.hp.hpl.jena.query.QueryParseException e) {
					System.out.println(e.getMessage());
					String errMsg = e.getMessage();
					int ln = e.getLine();
					int col = e.getColumn();
					String[] qlines = query.split("\\n");
					
					if (ln > 0) {
 					    String errLine = qlines[ln-1];
					    err("Error in from part: "+errLine.substring(0, col-1)
					        +" <span style='color: red;'> &nbsp;&nbsp;==&gt;</span>  "
					        +errLine.substring(col-1,errLine.length()));
				    } else {
				        err("Error in from part: "+errMsg);
				    }
			    }
            }
            
       } else {
			frombuf = "";
			String cis = complexExpressionsInContent.toString();
			// replace the sparql variable placeholders by variable identifiers because this 
			// goes into the sparql select string.
			cis = cis.replaceAll("<<\\[\\?\\?sparql-var-([0-9A-Za-z_]*)\\?\\?\\]>>","?$1");
			selectwherebuf = "select (1 as $lazy__x) "+cis+" where {rdf:type rdf:type rdf:Property.} limit 1"; 
			orderbuf = "NODEF";
			groupbuf = "NODEF";
			limitbuf = "NODEF";
        } // FROM PART IS OPTIONAL JG

    }


    /* distinct_part = "distinct" //after from_part */

    void distinct() {
        next();
        displaybuf = "distinct " + displaybuf;
    }

    void group_part() {
        System.out.println("\n---- begin group_part");
        if (term("group")) {
            next();
            if (term("by")) {
               startcopy();
               echo("group by ");
               next();

               spacesAreTokens();
               while(sym.ttype != TT_EOF && !term("end") && !term("order") && !term("limit") && !term("offset")){
                   copyTokensReplacingParamAndGlobalVars();
               } 
               spacesNotTokens();

               groupbuf = copybuf.toString();
            } else
                err("'by' missing after 'group'");
        } else {
            groupbuf = "NODEF";
        } // GROUP PART IS OPTIONAL JG
       System.out.println("\n---- end group_part");


    }

/**** From the SPARQL syntax *****/
    
    void Var() {
       if(sym.ttype == '$') echo("$"); else echo("?");
       next();
       if (sym.ttype == TT_WORD) {echo(sym.sval+" "); next();}
       else err("Var: missing identifier"); 
    }
    
    void Expression() { ConditionalOrExpression(); }
    
    void ConditionalOrExpression() {    
       ConditionalAndExpression();
       while (sym.ttype == '|') {
         next();
         if (sym.ttype == '|') {
           next(); 
           ConditionalAndExpression();
         }
         else err("ConditionalOrExpression: missing | after |");
       }
    }
    
    void ConditionalAndExpression() {   
       ValueLogical();
       while (sym.ttype == '&') {
         next();
         if (sym.ttype == '&') {
           next(); 
           ValueLogical();
         }
         else err("ConditionalAndExpression: missing & after &");
       }
    }
    
    void ValueLogical() {RelationalExpression(); }
    
    void RelationalExpression() {
        NumericExpression();
        if (sym.ttype == '=' || sym.ttype == '<' || sym.ttype == '>' || term("in") || term("not")) {
           boolean isComp = (sym.ttype == '<' || sym.ttype == '>');
           boolean isNot = (term("not"));
           echo(sym.sval);
           next();
           if  ((isComp && sym.ttype == '=') || (isNot && term("in"))) {echo(sym.sval); next();} 
           NumericExpression();
        }
    }
    void NumericExpression() {      AdditiveExpression(); }
    void AdditiveExpression() {     
       MultiplicativeExpression();
       while (sym.ttype== '+' || sym.ttype == '-'){
          echo(sym.sval);
          next();
          MultiplicativeExpression();
       }
       /* NOT SUPPORTED
        * | ( NumericLiteralPositive | NumericLiteralNegative ) ( ( '*' UnaryExpression ) | ( '/' UnaryExpression ) )* )*
        */
     }
             
     void  MultiplicativeExpression() {
        UnaryExpression();
        while (sym.ttype== '*' || sym.ttype == '/'){
          echo(sym.sval);
          next();
          UnaryExpression();
        }
      }
      
    void UnaryExpression() {
        if (sym.ttype == '!' || sym.ttype == '+' || sym.ttype == '-'){
           echo(sym.sval);
           next();
        }
        PrimaryExpression();
    }
    

    void PrimaryExpression() { }
/**** 
        '(' BrackettedExpression | 
        if (sym.sval in "concat|....") BuiltInCall 
        if '<' or ttword iriOrFunction 
        if '"' RDFLiteral 
        if ttnumber or + or -NumericLiteral
        if "true" "false"   BooleanLiteral
        if ? or $ Var

     void   BrackettedExpression      ::=   '(' Expression ')'   
     
     void iriOrFunction   ::=   iri ArgList?   
     
     void   RDFLiteral    ::=   String ( LANGTAG | ( '^^' iri ) )? 
     
     void iri     ::=   IRIREF | PrefixedName
     
     void IRIREF      ::=   '<' ([^<>"{}|^`\]-[#x00-#x20])* '>'
****/    
     void iri() {
        if (sym.ttype == '<') IRIREF();
        else PrefixedName();
     } 
     
     void beginDetectWhiteChars() {
        sym.ordinaryChar(' ');
        sym.ordinaryChar('\n');
        sym.ordinaryChar('\r');
        sym.ordinaryChar('\t');
        //sym.ordinaryChar('_');
        //sym.ordinaryChars('0', '9');
        //sym.ordinaryChars('a', 'z');
        //sym.ordinaryChars('A', 'Z');
        sym.slashStarComments(false);
        sym.slashSlashComments(false);
     }
     
     void endDetectWhiteChars() {
        sym.slashStarComments(true);
        sym.slashSlashComments(true);
        sym.whitespaceChars (' ',' ');
        sym.whitespaceChars('\n','\n');
        sym.whitespaceChars('\r','\r');
        sym.whitespaceChars('\t','\t');
        //sym.wordChars('_', '_');
        //sym.wordChars('0', '9');
        //sym.wordChars('a', 'z');
        //sym.wordChars('A', 'Z');
     }
     
     void IRIREF() {
        if (sym.ttype != '<') err("IRIREF must begin with '<'");
        
        beginDetectWhiteChars();
        
        echo("<");
        next();
        while(sym.ttype != '>') {
           if (sym.ttype == ' ' || sym.ttype == TT_EOF) { 
              err("missing '>' at end of IRI");
              break;
           }
           // TODO and other forbidden chars
           if(sym.ttype == TT_WORD) echo(sym.sval);
           else echo(String.valueOf((char)sym.ttype));
           next();
        }
        if (sym.ttype == '>') {echo(">"); }
        
        endDetectWhiteChars();
        next();
     }

     void PrefixedName() { // ::= identifier? ':' identifier?
         int lastSymInPrefix = sym.ttype;
         if (sym.ttype == TT_WORD) { 
             echo(sym.sval); next(); // prefix
             while (sym.ttype == '-' || sym.ttype == '.' || sym.ttype == TT_WORD) {
                  if (sym.ttype == TT_WORD) { echo(sym.sval);  } 
                  else { echo(""+sym.ttype); }
                  lastSymInPrefix = sym.ttype;
                  next();
             }
         }
         if (lastSymInPrefix == '.') err("'.' at end of prefix");
         if (sym.ttype == ':') {echo(":"); next(); }
         else err("missing ':' in prefixed name");
         if (sym.ttype == TT_WORD) { echo(sym.sval); next(); } // name
     }
     

    /*
     * select_part = "select"  "distinct? "where"? "{" GroupeGraphPattern  "}" SolutionModifier.
     * SYNTAX CURRENTLY NOT CHECKED
     */
     
    void variable() {
    }
    
    void spacesAreTokens() {
         sym.ordinaryChar(' ');sym.ordinaryChar('\n');sym.ordinaryChar('\r');sym.ordinaryChar('\t');
         sym.slashStarComments(false);
         sym.slashSlashComments(false);
    }
    
    void spacesNotTokens() {
         sym.slashStarComments(true);
         sym.slashSlashComments(true);
         sym.whitespaceChars (' ',' '); sym.whitespaceChars('\n','\n'); sym.whitespaceChars('\r','\r'); 
         sym.whitespaceChars('\t','\t');
    }
    
    boolean isWhiteSpace() {
         return (sym.ttype == ' ' || sym.ttype == '\n' || sym.ttype == '\r' || sym.ttype == '\t' ) ;
    } 

    void select_part() {
    
        startcopy();        
        if (term("select")) next();
        echo("select ");
        if (term("distinct")) {echo("distinct "); next();}

        if(term("where")){ next(); }

        if(sym.ttype =='{'){
                
                echo(" where {");
                next();
                int depth = 0; // inclusion depth of {} parenthesis
                
                spacesAreTokens();
                while(sym.ttype != TT_EOF && !term("end") && !(sym.ttype=='}' && depth == 0)){

                   if (sym.ttype == '{') {
                       depth++; 
                   }
                   else if (sym.ttype == '}') {
                       depth--;
                   }   
                   copyTokensReplacingParamAndGlobalVars();

                } // end while
                spacesNotTokens();
                
                if (sym.ttype == '}') { 
                    echo("}"); 
                    next(); 
                }
                else err("missing } at end of selector");
                
                
                stopcopy();
                String cis = complexExpressionsInContent.toString();
                /*
                 * The complex expression contains placeholders for variables (<<?? .... ??>>)
                 * Replace them with the actual variable name because the expression goes
                 * directly into the sparql query.
                 */
                cis = cis.replaceAll("<<\\[\\?\\?sparql-var-([0-9A-Za-z_]*)\\?\\?\\]>>","?$1");
                if (cis.equals("")) cis = "(1 as $lazy__x) ";  // at least one (dummy) variable needed
                // insert into select-where
                if (! frombuf.equals("")) cis = cis + " from " + frombuf;
                selectwherebuf = copybuf.toString().replaceAll("(^select (distinct)?)", "$1 "+cis+" ") ;
                //selectwherebuf = copybuf.toString().replaceAll("($select (distinct)?)","\1 "+cisWithSparqlVariables+" ") ;
        } // end if {
        else // if '{'
                err("'{' missing after 'from graph'"+copybuf);
            
    }
    
    void copyTokensReplacingParamAndGlobalVars() {


        if (sym.ttype == '[') { // global or input variable ... or blank node
            String varCat = ""; // inputVar = false;
            next();
            if (sym.ttype == '!' || sym.ttype == '?' || sym.ttype == TT_WORD) {
                if (sym.ttype == '!') { // input variable
                   varCat = "!"; // inputVar = true;
                   next();
                } else if (sym.ttype == '?') {
                   varCat = "?";
                   next();
                }
                if (sym.ttype == TT_WORD) {
                    // global or input variables or session attributes are strings
                    echo("\"" + ns.varPrefix + varCat + sym.sval + ns.varSuffix +"\"");
                    next();
                } else
                  err("variable name expected after [? or [!");
                if (sym.ttype == ']')
                   next();
                else
                   err("] expected after [variable");
           } else {
               {echo("["); next();}
           }
        } // end if [
        else if (sym.ttype == '?'||sym.ttype == '$') Var();
        else if(sym.ttype == TT_WORD){
            int pi = paraIndex(sym.sval);
            if (pi < nbparams) { // Parameter
                echo(ns.paramPrefix  + pi + ns.paramSuffix);
                next();
            }
            else { // Reserved word or number or Prefixed identifier
//                PrefixedName();
                echo(sym.sval);
                next();                         
//                 if(sym.ttype == ':') {
//                     echo(":");
//                     next();
//                     if(sym.ttype == TT_WORD) {
//                         echo(sym.sval + " "); next();
//                     }
//                     else {
//                         err(" \":\" should be followed by a word or number ");
//                     }
//                }
            }
            // echo(" ");
        } // end if ttword
        else if (sym.ttype == '\"') {
            echo("\""+doubleQuotes(sym.sval)+"\"");      
            next();
        }   
        else if (sym.ttype == '<') { // IRI
//                 sym.ordinaryChar(' ');sym.ordinaryChar('\n');sym.ordinaryChar('\r');sym.ordinaryChar('\t');
//                 sym.slashStarComments(false);
//                 sym.slashSlashComments(false);
                echo("<");
                next();

                while(sym.ttype != '>' && sym.ttype != ' ')
                {
                    if(sym.ttype == TT_WORD) echo(sym.sval);
                    else 
                        if(sym.ttype == TT_EOF) {
                            err(" '>' missing to close iri");
                            break;
                        }
                        else echo(String.valueOf((char)sym.ttype));
                        next();
                }
                echo(""+(char)(sym.ttype));

//                 sym.slashStarComments(true);
//                 sym.slashSlashComments(true);
//                 sym.whitespaceChars (' ',' '); sym.whitespaceChars('\n','\n'); sym.whitespaceChars('\r','\r');
//                 sym.whitespaceChars('\t','\t');
                next();
        } // end if <   
        else { // any other symbol
           echo(String.valueOf((char)sym.ttype));
//           if (sym.ttype == '\n' || sym.ttype == '\r') additionalLines++ ; // count the line
           if (sym.ttype == sym.TT_EOL) additionalLines++ ; // count the line
           next();
        }
               
    }
    

    
    
    // GroupClause    ::=   'GROUP' 'BY' GroupCondition+
    void future_group_part() {
       if (term("group")) {
         next();
         if (term("by")) {
            GroupCondition();
         } else err("by");
       }
       else err("group");
    }
    
    // GroupCondition     ::=   BuiltInCall | FunctionCall | '(' Expression ( 'AS' Var )? ')' | Var 
    void GroupCondition() {
       if (isBuiltIn()) BuiltInCall();
       else if (sym.ttype == '(') {
          echo("("); next();
          Expression();
          if (term("as")) { echo(" as "); next(); Var(); }
          if (sym.ttype == ')') {echo(")"); next();} else err(")"); 
       }
       else if (sym.ttype == '?' || sym.ttype == '$') Var();
       else FuncntionCall();
    }
    
    void BuiltInCall() {}; // TODO
    
    void FuncntionCall() {}; // TODO

    /*
     * condition = ["not"] logical_term { "or" logical_term }
     */
    void condition() {
        if (term("not")) {
            echo(" not ");
            next();
        }
        logical_term();
        while (term("or")) {
            echo(" or ");
            next();
            logical_term();
        }
    }

    /*
     * logical_term = logical_factor { "and" logical_factor }
     */
    void logical_term() {
        logical_factor();
        while (term("and")) {
            echo(" and ");
            next();
            logical_factor();
        }
    }

    /*
     * logical_factor = exists_expression | forall_expression |
     * simple_expression ( comparision_op simple_expression | "is" ["not"]
     * "null" ).
     */
    void logical_factor() {
        if (term("exists"))
            exists_expression();
        else if (term("forall"))
            forall_expression();
        else {
            simple_expression();
            if (term("is")) {
                echo(" is ");
                next();
                if (term("not")) {
                    echo(" not ");
                    next();
                }
                if (term("null")) {
                    echo(" null ");
                    next();
                } else
                    err("'null' expected");
            } else {
                if (term("like") || sym.ttype == '<' || sym.ttype == '>'
                        || sym.ttype == '!' || sym.ttype == '=') {
                    comparison_op();
                    simple_expression();
                }

            }
        }
    }

    /*
     * comparison_op = "like" | "<" | "<=" | ">" | ">=" | "=" | "<>"
     */
    void comparison_op() {
        if (term("like")) {
            echo(" like ");
            next();
        } else if (sym.ttype == '<') {
            echo("<");
            next();
            if (sym.ttype == '=') {
                echo("=");
                next();
            }
            if (sym.ttype == '>') {
                echo(">");
                next();
            }
        } else if (sym.ttype == '>') {
            echo(">");
            next();
            if (sym.ttype == '=') {
                echo("=");
                next();
            }
        } else if (sym.ttype == '!') {
            next();
            if (sym.ttype == '=') {
                echo("<>");
                next();
            }
        } else if (sym.ttype == '=') {
            echo("=");
            next();
        }
    }

    /**
     * exists_expression = "exists" "(" collection_id [alias_id] { ','
     * collection_id [alias_id] } ":" condition ")"
     */

    void exists_expression() {
        if (term("exists")) {
            echo("exists(select * from ");
            next();
            if (sym.ttype == '(') {
                next();
                identifier(); // collection name
                if (sym.ttype == TT_WORD) {
                    echo(" ");
                    identifier();
                } // alias

                while (sym.ttype == ',') {
                    echo(",");
                    next();
                    identifier(); // collection name
                    if (sym.ttype == TT_WORD) {
                        echo(" ");
                        identifier();
                    }// alias
                }
            } else
                err("missing '(' after 'exists'");
        }
        if (sym.ttype == ':') {
            echo(" where ");
            next();
            condition();
        }
        if (sym.ttype == ')')
            next();
        else
            err("missing ')' after 'exists( ...'");
        echo(")");
    }

    /**
     * forall_expression = "forall" "(" collection_id [alias_id] { ','
     * collection_id [alias_id] } ":" condition "=>" condition ")"
     */

    void forall_expression() {
        if (term("forall")) {
            echo(" not exists(select * from ");
            next();
            if (sym.ttype == '(') {
                next();
                identifier(); // collection name
                if (sym.ttype == TT_WORD) {
                    echo(" ");
                    identifier();
                } // alias

                while (sym.ttype == ',') {
                    echo(",");
                    next();
                    identifier(); // collection name
                    if (sym.ttype == TT_WORD) {
                        echo(" ");
                        identifier();
                    }// alias
                }
            } else
                err("missing '(' after 'forall'");
        }
        if (sym.ttype == ':') {
            echo(" where ");
            next();
            condition();
            if (sym.ttype == '=') {
                next();
                if (sym.ttype == '>') {
                    next();
                    echo(" and not(");
                    condition();
                    echo(")");
                }
            }
        }

        if (sym.ttype == ')')
            next();
        else
            err("missing ')' after 'forall( ...'");
        echo(")");
    }

    // order_part = "order" "by" simple_expression ["desc"] {','
    // simple_expression ["desc"]}.

    void order_part() {
        if (term("order")) {
            startcopy();
            next();
            if (term("by")) {
               echo("order by ");
               next();
               spacesAreTokens();
               while(sym.ttype != TT_EOF && !term("end") && !term("limit") && !term("offset")){
                   copyTokensReplacingParamAndGlobalVars();
               } // end while
               spacesNotTokens();
               orderbuf = copybuf.toString();
            } else
                err("'by' missing after 'order'");
        } else
            orderbuf = "NODEF"; // order by is optional jg
    }
    
    void limit_part() {
        if (term("limit")||term("offset")) {
            startcopy();
            echo(sym.sval+" ");
            next();
            
            spacesAreTokens();
            while(sym.ttype != TT_EOF && !term("end")){
                   copyTokensReplacingParamAndGlobalVars();
            } // end while
            spacesNotTokens();
            
            limitbuf = copybuf.toString();
        } else
            limitbuf = "NODEF"; // order by is optional jg
    }


    static String builtins = ":LANG:LANGMATCHES:DATATYPE:BOUND:IRI:URI:BNODE:RAND:ABS:CEIL:FLOOR:ROUND:CONCAT:STRLEN:UCASE:LCASE:ENCODE_FOR_URI:CONTAINS:STRSTARTS:STRENDS:STRBEFORE:STRAFTER:YEAR:MONTH:DAY:HOURS:MINUTES:SECONDS:TIMEZONE:TZ:NOW:UUID::TRUUID:MD5:SHA1:SHA256:SHA384:SHA512:COALESCE:IF:STRLANG:STRDT:sameTerm:isIRI:isURI:isBLANK:isLITERAL:isNUMERIC:COUNT:SUM:MIN:MAX:AVG:SAMPLE:GROUP_CONCAT:SUBSTR:REPLACE:REGEX:EXISTS:NOT:";
    static String aggregates = ":COUNT:SUM:MIN:MAX:AVG:SAMPLE:GROUP_CONCAT:";
        
    
    boolean isBuiltIn() {
      if (sym.ttype==TT_WORD && builtins.indexOf(":"+sym.sval.toUpperCase()+":") > -1) return true;
      else return false;
    }

    /**
     * on_open = "on" "open" "{" action_list "}"
     */

    void on_open() {

        if (term("on")) {
            next();
            if (term("open")) {
                next();
                if (sym.ttype == '{') {
                    next();
                    action_list();
                    if (sym.ttype == '}')
                        next();
                    else
                        err("'}' missing after db action list");
                }
            }
        }
    }

    /**
     * action_list = basic_action {"," basic_action }
     */
    void action_list() {
        basic_action();
        while (sym.ttype == ',') {
            next();
            basic_action();
        }
    }

    /**
     * basic_action = insert_action | delete_action | update_action
     */
    void basic_action() {
        if (term("insert")) {
            insert_action();
        }
        if (term("delete")) {
            delete_action();
        }
        if (term("update")) {
            update_action();
        }

    }

    /**
     * delete_action = "delete" table_identifier [ "(" condition ")" ]
     */
    void delete_action() {
        next(); // skip "delete"
        startcopy();
        echo("delete from ");
        identifier(); // table identifier

        if (sym.ttype == '(') {
            next();
            echo(" where ");
            condition();
            if (sym.ttype == ')')
                next();
            else
                err("')' missing after delete table(condition");
        }
        actionList.add(copybuf.toString());
    }

    /*
     * update_action = "update" table_identifier "(" condition ")" "set" "["
     * attribute ":" simple_expression {"," attribute ":" simple_expression} "]"
     */
    void update_action() {
        StringBuffer cond = new StringBuffer();
        next();
        startcopy();
        StringBuffer instruction = copybuf;
        echo("update ");
        identifier(); // table identifier
        if (sym.ttype == '(') {
            next();
            copybuf = cond;
            condition();
            if (sym.ttype == ')')
                next();
            else
                err("')' missing after update table(condition");
            copybuf = instruction;
        }
        if (term("set"))
            next();
        else
            err("'set' missing after update table(condition)");
        if (sym.ttype == '[')
            next();
        else
            err("'[' missing after update table(condition) set ");

        echo(" set ");

        identifier(); // attribute
        if (sym.ttype == ':') {
            next();
        } else
            err("':' missing in update table_name(...) set [attr:expression, ...]");
        echo("=");

        simple_expression();

        while (sym.ttype == ',') {
            echo(",");
            next();
            identifier();
            if (sym.ttype == ':') {
                next();
            } else
                err("':' missing in update table_name(...) set [attr:expression, ...]");
            echo("=");
            simple_expression();
        }

        if (sym.ttype == ']') {
            next();
        } else
            err("']' missing after update table_name(...) set [attr:expression, ...");

        if (cond.length() > 0) {
            echo(" where ");
            copybuf.append(cond);
        }

        actionList.add(copybuf.toString());

    }

    /**
     * insert_action = "insert" table_identifier "[" attr_identifier ":"
     * expression "," ... "]"
     */
    void insert_action() {
        StringBuffer values = new StringBuffer();
        values.append("(");
        next();
        startcopy();
        StringBuffer instruction = copybuf;
        echo("insert into ");
        identifier(); // table identifier

        if (sym.ttype == '[')
            next();
        else
            err("'[' missing after insert table_name");

        echo("(");
        identifier(); // attribute
        if (sym.ttype == ':') {
            next();
        } else
            err("':' missing in insert table_name[...]");
        copybuf = values; // collect expressions in this StringBuffer
        simple_expression();

        while (sym.ttype == ',') {
            next();
            echo(",");
            copybuf = instruction;
            echo(",");
            identifier();
            if (sym.ttype == ':') {
                next();
            } else
                err("':' missing in insert table_name[...]");
            copybuf = values; // collect expressions in this StringBuffer
            simple_expression();
        }

        echo(")");
        copybuf = instruction;
        echo(") values ");
        if (sym.ttype == ']')
            next();
        else
            err("']' missing after insert table_name[...]");

        copybuf.append(values);

        actionList.add(copybuf.toString());

    }

    // /////////////////////// Actions //////////////////////////

    void err(String e) {
        String token = "";
        if (sym.ttype == TT_WORD)
            token += sym.sval;
        else if (sym.ttype == TT_NUMBER) {
            if ((int) sym.nval == sym.nval)
                token += (int) sym.nval;
            else
                token += sym.nval;
        } else
            token += (char) sym.ttype;

        System.err.println("***** Line " + sym.lineno() + ", token ['" + token
                + "'] : " + e);
        errorbuf += "Line " + (sym.lineno() - firstline + 1) + ", token '"
                + token + "' : " + e + "<br>";
        errInNodeDef++;
    }

    void echo(String s) {
        // System.out.print(s);
        // if (copyon)
        copybuf.append(s);
        if (debug)
            System.out.print(s);

    }
    
    ArrayList<StringBuffer> copybufStack = new ArrayList<StringBuffer>();
    ArrayList<Boolean> copyonStack = new ArrayList<Boolean>();

    void startcopy() {
        copyon = true;
        copybuf = new StringBuffer();
    }
    
    void begincopyPush() {
        copyonStack.add(copyon);
        copybufStack.add(copybuf);
        copyon = true;
        copybuf = new StringBuffer();
    }
    
    void endcopyPush() {
        int sz = copybufStack.size();
        
        if (copybufStack != null && sz > 0) {
            copybuf = copybufStack.get(sz-1);
            copyon = copyonStack.get(sz-1);
            copybufStack.remove(sz-1);
            copyonStack.remove(sz-1);
        }
        else {
            copybuf = new StringBuffer();
            copyon = false;
        }    
    }

    void stopcopy() {
        copyon = false;
    }

    void orderforview() {
        orderbuf = copybuf.toString();
    }

    void selectforview() {
        selectwherebuf = copybuf.toString();
    }

    void fromforview() {
        frombuf = copybuf.toString();
    }

    void displayforview() {
        displaybuf = copybuf.toString();
    }

    void storeCompiledNode() {
        if (directToDB) {
            try {
                // GF0902 if (fromFile){
                stmt.executeUpdate("delete from lazy_nodes where projectid='"
                        + projectid + "' and name='" + nodeid + "'");
                String insertRequest = "insert into lazy_nodes(projectid,name, nbparam, pre, "
                        + "items, post, collection, selector, "
                        + "groupby, ordering, limit, plaintxt,status,"
                        + "error, cachesize, nodetype)" + " values(" + "'"
                        + projectid+ "','"+ nodeid+ "', "+ nbparams+ ",'"+ prebuf+ "',"
                        + "'"+ displaybuf+ "','"+ postbuf+ "','"+ frombuf+ "','"+ selectwherebuf+ "',"
                        + "'"+ groupbuf+ "','"+ orderbuf+ "','"+ limitbuf+ "','"+ plaintxtbuf+ "','"+ statusbuf+ "',"
                        + "'"+ doubleQuotes(errorbuf) + "'," + cachesize + ",'"+ nodetypebuf+ "'"
                        + ")";

                //System.out.println("insert quest == "+ insertRequest);
                int m = stmt.executeUpdate(insertRequest);

                /*
                 * Update action table
                 */
                stmt.executeUpdate("delete from lazy_actions where projectid='"
                        + projectid + "' and nodename='" + nodeid + "'");
                // if (! actionList.isEmpty()) {
                int seqno = 0;
                for (Iterator i = actionList.iterator(); i.hasNext();) {
                    String act = (String) i.next();

                    String upla = "insert into lazy_actions(projectid,nodename,seqno,operation) "
                            + "values('"
                            + projectid
                            + "','"
                            + nodeid
                            + "',"
                            + seqno + ",'" + act + "')";

                    System.out.println(upla); // DBG

                    stmt.executeUpdate(upla);

                    seqno++;
                }
            } catch (SQLException se) {
                System.out.println("*** SQL ERR, node: " + nodeid + " "
                        + se.getMessage());
            }
        }
        if (fromFile) {
            out.println();
            out.println("delete from lazy_nodes where name='" + nodeid + "';");
            out.println();
            out.println("insert into lazy_nodes(name, nbparam, pre, items, post, collection, selector, ordering)");
            out.println("values(");
            out.println("'" + nodeid + "',");
            out.println(" " + nbparams + ",");
            out.println("'" + prebuf + "',");
            out.println("'" + displaybuf + "',");
            out.println("'" + postbuf + "',");
            out.println("'" + frombuf + "',");
            out.println("'" + selectwherebuf + "',");
            out.println("'" + orderbuf + "'");
            out.println(");");
        }
    }

    void invalideLastNode() {
        if (directToDB) {
            try {
                String updateRequest = "update  lazy_nodes" + " set "
                        + "status='INVALID'," + "error='"
                        + doubleQuotes(errorbuf) + "'" + " where "
                        + "projectid='" + projectid + "'" + " and name='"
                        + nodeid + "'";
                // System.out.println(updateRequest);
                int m = stmt.executeUpdate(updateRequest);
                // System.out.println("node: "+nodeid /*
                // +" "+m+" row updated in table lazy_nodes" */);
            } catch (SQLException se) {
                System.out.println("*** SQL ERR, node: " + nodeid + " "
                        + se.getMessage());
            }
        }
    }

    String quadrupleQuotes(String s) {
        return s.replace("'","''''");
        /*
        int ix = 0, ixf, ixx;
        while ((ix = s.indexOf("'", ix)) > -1) {
            s = s.substring(0, ix) + "''''" + s.substring(ix + 1, s.length());
            ix += 4;
        }
        return s;
        */
    }

    String doubleQuotes(String s) {
        return s.replace("'","''");
        /*        int ix = 0, ixf, ixx;
        while ((ix = s.indexOf("'", ix)) > -1) {
            s = s.substring(0, ix) + "''" + s.substring(ix + 1, s.length());
            ix += 2;
        }
        return s;
        */

    }

    public static void main(String args[]){}


}

