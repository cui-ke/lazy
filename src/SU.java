

import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;

/***********
*
* This class contains a set of string replacement functions to process all the 
* parameter and variable replacements in node generation.
*
*/
class SU { // strings utilities
    
    static final String hrefPrefix = "<a href=\"ns?";
    
    static final int NOQUOTE = 0;
    static final int SQL_QUOTES = 1;
    static final int SPARQL_QUOTES = 2;
    
    
	/**
	*  For each hypertext link to a Lazy node, replace the u parameter values by "safe" values
	*  i.e. values that are corretly transported by URLs.
	* Replace all occurences of <a href="ns?__PARAMETERS__">
	*  by
	*  <a href="ns?__encoded__(__PARAMETERS__)">
	*
	* i.e. replace all href parameters with a safe encoding
	*/
    static String replaceHrefs(String s) {
        int ix = 0, ixf;
        
        while ((ix = s.indexOf(hrefPrefix, ix)) > -1) {
            ixf = s.indexOf("\">", ix);
            String uParameterPart = s.substring(ix, ixf);
            //System.out.println("first ix:"+ix+" ixf:"+ixf+" s:"+s);
            s =
            s.substring(0, ix)
            + getParameters(uParameterPart)
            + s.substring(ixf, s.length());
            ix = ixf;
            
        }
        return s;
    }
    
	/**
	* Replace &amp;u=__X1__&amp;u=__X2__;...
	* by
	* &amp;u=__encode__(__X1__)&amp;u=__encode__(__X2__);...
	*/
    static String getParameters(String s) {
        int ix = 0, ixf = 0;
		
        if (s.indexOf("&amp;u=", 0) == -1) return s;
		
		/* Strange coding ? To change */
		
        while ((ix = s.indexOf("&amp;u=", ix)) > -1) {
            ix = ix + "&amp;u=".length();
            if ((ixf = s.indexOf("&amp;u=", ix)) < 0)
                break;
            //System.out.println("in ix:"+ix+" ixf:"+ixf+" s:"+s);
            String uParameterPart = s.substring(ix, ixf);
            s = s.substring(0, ix) + URLUTF8Encoder.encode(uParameterPart) + s.substring(ixf, s.length());
            ix = ixf;
            
        }
        //System.out.println("out ix:"+ix+" ixf:"+ixf+" s:"+s);
		if (ix < s.length())
        	s = s.substring(0, ix) + URLUTF8Encoder.encode(s.substring(ix, s.length()));
        
        return s;
    }
    
    /************************************
     *
     *  DOUBLE QUOTE
     */
    static String doubleQuotes(String s) {
        int ix = 0;
        while ((ix = s.indexOf("'", ix)) > -1) {
            s = s.substring(0, ix) + "''" + s.substring(ix + 1, s.length());
            ix += 2;
        }
        return s;
        
    }
    
    /************************************
     *
     * Adapt values to use them in SQL statements
	 *
	 * - double the quotes
	 *
	 * - for XML projects : replace "&" by "&amp;", "<" by "&lt;"
	 *   ? REALLY A GOOD IDEA ? TODO
	 *
     */
    static String cleanValue(String encodeType, String s) {
        int ix = 0;
        if (encodeType.equals("XML")){
            s = s.replace("&","&amp;");
            s = s.replace("<","&lt;");
        }
        s = s.replace("'","''");        
        return s;
        
    }
    
    
    
    
    /************************************
     *
     *  REPLACE PARAMETERS
	 *
	 * replace every occurence of "<<??param-__NUM__//??>> by
	 * '__the value of paramenter number NUM with quotes doubled__'
	 *
	 * TODO: replace numeric or unquoted parameters (unquoted)
	 * marked as <<??param-__NUM__//uq//??>>
	 *
     */
    
    static String replaceParameters(String s, String[] params, int withQuotes) {
        int nbparam = 0;
        String paramVal = "";
        
        int ix = 0, ixo = 0, ixf, ixx;
        boolean isOutput = false;
        if (params != null)
            nbparam = params.length;
        
        while ((ix = s.indexOf(ns.paramPrefix, ix)) > -1 | (ixo = s.indexOf(ns.outParamPrefix, ix)) > -1) { // not || !
            if (ix == -1 || (ixo > -1 && ixo < ix)) { // the first placeholder found in an output placeholder
                ix = ixo; 
                isOutput = true;
                ixx = ix + ns.outParamPrefix.length();  // end of prefix index
            }
            else {
                isOutput = false;
                ixx = ix + ns.paramPrefix.length(); // end of prefix index
            }
            // compute the int value of the param. no.
            int ipara = 0;
            ixf = s.indexOf(ns.paramSuffix, ix);
            // System.out.println("replacing "+s.substring(ix, ixf)+" at "+ix+"--"+ixf);
            while (ixx < ixf) { 
                ipara = ipara * 10 + (s.charAt(ixx) - '0');
                ixx++;
            }
            if (ipara < nbparam) {
               paramVal = isOutput ? outputForm(params[ipara]) : params[ipara] ;
               if (withQuotes == SQL_QUOTES) 
                   paramVal =  "'" + doubleQuotes(paramVal) + "'";
               else if (withQuotes == SPARQL_QUOTES)
                   paramVal =  "'" + paramVal.replace("'","\\'") + "'";
               
            } 
            else
                paramVal = "";
            
            s = s.substring(0, ix) + paramVal + s.substring(ixf + 6, s.length());
        }
        return s;
    }
    
    /*
     * Create a human readable representation of an RDF node defined by its string representation
     */
    static String outputForm(String node) {
        String out = "";
        if (node.matches("(^\".*\"$)|(^\'.*\'$)")) out = node.substring(1, node.length()-1);
        else if (node.matches("^\".*\"\\^\\^[^\"]*$")) out = node.replaceAll("^\"(.*)\"\\^\\^[^\"]*$","$1");
        else out = node.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;");
        return out;
    }
    

    
    /************************************
	 *
	 * Replace variable markers by the variable values. 
	 *
	 * There are three styles of variable markers
	 * old form = [[__VARIABLE_NAME__]]
	 * new form = <<[??var-__VARIABLE_NAME__??]>>
	 * sparql form = <begin-var-__VARIABLE_NAME__-end-var>
	 *
	 * The different types of variables are
	 *   ?NAME language dependent variable, defined either for this project or shared by all projects (SHARE)
	 *   !NAME old style : do nothing, it's for parameters; new style : same as session attribute
	 *   NAME : session attribute
     */
    static String replaceSystemParameters(String s,HttpSession session, Hashtable text, String projectid, int doubleTheQuotes) {
        
		//String varPrefix = "<<[??var-"; 
		//String varSuffix = "??]>>";
		
        int ix = 0, ixf, ixx, ixff;
		int iy = 0;
		int iz = 0;
        String replacevalue="";
		
		boolean newForm = false;
		boolean sparqlForm = false;
        
        while ((ix = s.indexOf(ns.SystParamPrefix, ix)) > -1 
             | (iy = s.indexOf(ns.varPrefix, ix)) > -1 
             | (iz = s.indexOf(ns.varPrefixSparql, ix)) > -1) {
			if (ns.verboseReplace) System.out.println("ix "+ix+" iy "+iy+" iz="+iz);
            int ipara = 0;
			newForm = (ix == -1) || (iy > -1 && iy < ix) || (iz > -1 && iz < ix); // GF
			if (newForm) {
			    sparqlForm = (iy == -1 || (iz > -1 && iz < iy));
			    if (sparqlForm) {
			        ix = iz; ixf = s.indexOf(ns.varSuffixSparql, ix); ixx = ix + ns.varPrefixSparql.length(); 
			        ixff = ixf+ns.varSuffixSparql.length(); 
			        if (ns.verboseReplace) System.out.println("sparql form "+ix+"--"+ixff);
			    }
			    else { 
			        ix = iy; ixf = s.indexOf(ns.varSuffix, ix); ixx = ix + ns.varPrefix.length(); 
				    ixff = ixf+ns.varSuffix.length(); 
				    if (ns.verboseReplace) System.out.println("new form "+ix+"--"+ixff);
				}
			} // GF
			else // old form
				{ixf = s.indexOf(ns.SystParamSuffix, ix); ixx = ix + "[[".length(); ixff = ixf+"]]".length(); }
				
            String var=s.substring(ixx,ixf);
            System.out.println("\n----- replaceSystemParameters - var = "+var+" double = "+doubleTheQuotes);
            
            if (ns.verboseReplace) System.out.println("replacing "+s.substring(ix, ixf)+" at "+ix+"--"+ixf+" var:"+var);
            if (!var.substring(0,1).equals("?")){  // not a project variable
                if (!var.substring(0,1).equals("!")){  // not a request variable
                    replacevalue=(String)(session.getAttribute(var));
                }
                else {
					if (newForm) replacevalue = (String) (session.getAttribute(var));
					else replacevalue = ns.SystParamPrefix+var+ns.SystParamSuffix; // a request variable dont modify now
                }
            }
            else { String keytxt=projectid+","+((String)(session.getAttribute("LANG")))+","+var.substring(1, var.length());
	            if (ns.verboseReplace) System.out.println("look for "+keytxt);
	            String value=(String)text.get(keytxt);
				if (value==null) { // try with the string description
					keytxt=projectid+","+"*"+","+var.substring(1, var.length());
					value=(String)text.get(keytxt);
				}
	            if (value==null) {// try SHARE
	                keytxt="SHARE"+","+((String)(session.getAttribute("STYLE")))+","+var.substring(1, var.length());
	                if (ns.verboseReplace) System.out.println("look for "+keytxt);
	                value=(String)text.get(keytxt);
	                if (value==null) {
	                    value="no definition for: "+var;}
	            }
	            replacevalue=value;
            }
			// DEL if (newForm) replacevalue = "'"+replacevalue+"'"; // because it's not in an quoted string

            if (replacevalue == null) replacevalue = "";
            if (doubleTheQuotes == SQL_QUOTES) replacevalue = doubleQuotes(replacevalue);
            else if (doubleTheQuotes == SPARQL_QUOTES) replacevalue = "'"+replacevalue.replace("'","\\'")+"'";
            s = s.substring(0, ix)
            +  replacevalue
            + s.substring(ixff, s.length());
            ix=ixx;
            if (ns.verboseReplace) System.out.println("new s : "+s);
        }
        return s;
    }
    
	/**
	* Replace variable markers in node parameters by their actual values
	*
	* => restriction: a node paramter must not contain the string <<[??ivar-
	*
	*/
    static void replaceQueryParameters(HttpSession session, String[] u) {
	
		String varPrefix = "<<[??ivar-"; 
		String varSuffix = "??]>>";
        
        if (u==null) return;
        for (int i=0 ;i<u.length ;i++){
            int ix = 0, ixf, ixx;
            String replacevalue="";
            String s=u[i];
			// Old style : should be dropped
            while ((ix = s.indexOf(ns.SystParamPrefix, ix)) > -1) {
                int ipara = 0;
                ixf = s.indexOf(ns.SystParamSuffix, ix);
                ixx = ix + 2; // length of "[["
                String var=s.substring(ixx,ixf);
                replacevalue=(String) (session.getAttribute(s.substring(ixx,ixf)));
                if (ns.verboseReplace) System.out.println("P replacing "+s.substring(ix, ixf)+" at "+ix+"--"+ixf+" var:"+var+" ->:"+replacevalue);
                s = s.substring(0, ix)
                + replacevalue
                + s.substring(ixf + 2, s.length()); // 2 length ]]
                ix=ixx;
            }
			ix = 0;
			while ((ix = s.indexOf(varPrefix, ix)) > -1) {
                int ipara = 0;
                ixf = s.indexOf(varSuffix, ix);
                ixx = ix + varPrefix.length();
                String var="!"+s.substring(ixx,ixf);
                replacevalue=(String) (session.getAttribute(var));
                if (ns.verboseReplace) System.out.println("P NEW replacing "+s.substring(ix, ixf)+" at "+ix+"--"+ixf+" var:"+var+" ->:"+replacevalue);
                s = s.substring(0, ix)
                + replacevalue
                + s.substring(ixf + varSuffix.length(), s.length());
                ix=ixx;
            }
            u[i]= s;
        }
    }
    

    
    static String concatParameters(String[] params) {
        int nbparam = 0;
        String s = "";
        
        int ix = 0, ixf, ixx;
        
        if (params != null) {
            nbparam = params.length;
            for (int i = 0; i < nbparam; i++)
                s += "|" + params[i];
        }
        
        return s;
    }
    
}
