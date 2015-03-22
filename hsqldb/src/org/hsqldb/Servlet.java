/* Copyrights and Licenses
 *
 * This product includes Hypersonic SQL.
 * Originally developed by Thomas Mueller and the Hypersonic SQL Group. 
 *
 * Copyright (c) 1995-2000 by the Hypersonic SQL Group. All rights reserved. 
 * Redistribution and use in source and binary forms, with or without modification, are permitted
 * provided that the following conditions are met: 
 *     -  Redistributions of source code must retain the above copyright notice, this list of conditions
 *         and the following disclaimer. 
 *     -  Redistributions in binary form must reproduce the above copyright notice, this list of
 *         conditions and the following disclaimer in the documentation and/or other materials
 *         provided with the distribution. 
 *     -  All advertising materials mentioning features or use of this software must display the
 *        following acknowledgment: "This product includes Hypersonic SQL." 
 *     -  Products derived from this software may not be called "Hypersonic SQL" nor may
 *        "Hypersonic SQL" appear in their names without prior written permission of the
 *         Hypersonic SQL Group. 
 *     -  Redistributions of any form whatsoever must retain the following acknowledgment: "This
 *          product includes Hypersonic SQL." 
 * This software is provided "as is" and any expressed or implied warranties, including, but
 * not limited to, the implied warranties of merchantability and fitness for a particular purpose are
 * disclaimed. In no event shall the Hypersonic SQL Group or its contributors be liable for any
 * direct, indirect, incidental, special, exemplary, or consequential damages (including, but
 * not limited to, procurement of substitute goods or services; loss of use, data, or profits;
 * or business interruption). However caused any on any theory of liability, whether in contract,
 * strict liability, or tort (including negligence or otherwise) arising in any way out of the use of this
 * software, even if advised of the possibility of such damage. 
 * This software consists of voluntary contributions made by many individuals on behalf of the
 * Hypersonic SQL Group.
 *
 *
 * For work added by the HSQL Development Group:
 *
 * Copyright (c) 2001-2002, The HSQL Development Group
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer, including earlier
 * license statements (above) and comply with all above license conditions.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution, including earlier
 * license statements (above) and comply with all above license conditions.
 *
 * Neither the name of the HSQL Development Group nor the names of its
 * contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL HSQL DEVELOPMENT GROUP, HSQLDB.ORG, 
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, 
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, 
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */


package org.hsqldb;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.util.*;
import java.sql.SQLException;

// fredt@users 20020130 - patch 475586 by wreissen@users
// fredt@users 20020328 - patch 1.7.0 by fredt - error trapping

/**
 * <font color="#009900">
 * Servlet acts as a interface between the applet and the database for the
 * the client / server mode of HSQL Database Engine. It is not required if
 * the included HSQL Database Engine WebServer is used, but if another
 * HTTP server is used. The HTTP Server must support the Servlet API.
 * <br>
 * This class should not be used directly by the application. It will be
 * called by the HTTP Server. The applet / application should use the
 * jdbc* classes.
 * <br>
 * The database name is taken from the servlet engine (extranal webserver)
 * property hsqldb.server.database (fredt@users)
 * <br>
 * </font>
 * @version 1.7.0
 */
public class Servlet extends javax.servlet.http.HttpServlet {

    private String   sError;
    private Database dDatabase;
    private String   sDatabase;

    /**
     * Method declaration
     *
     *
     * @param database
     */
    public void init(ServletConfig config) {

        try {
            super.init(config);
        } catch (ServletException exp) {
            log(exp.getMessage());
        }

        sDatabase = getInitParameter("hsqldb.server.database");

        if (sDatabase == null) {
            sDatabase = ".";
        }

        log("Database filename = " + sDatabase);

        try {
            dDatabase = new Database(sDatabase);
        } catch (SQLException e) {
            sError = e.getMessage();

            log(sError);
        }

        log("Initialization completed.");
    }

    private static long lModified = 0;

    /**
     * Method declaration
     *
     *
     * @param req
     *
     * @return
     */
    protected long getLastModified(HttpServletRequest req) {

        // this is made so that the cache of the http server is not used
        // maybe there is some other way
        return lModified++;
    }

    /**
     * Method declaration
     *
     *
     * @param request
     * @param response
     *
     * @throws IOException
     * @throws ServletException
     */
    public void doGet(HttpServletRequest request,
                      HttpServletResponse response)
                      throws IOException, ServletException {

        String query = request.getQueryString();

        if ((query == null) || (query.length() == 0)) {
            response.setContentType("text/html");

// fredt@users 20020130 - patch 1.7.0 by fredt
// to avoid caching on the browser
            response.setHeader("Pragma", "no-cache");

            PrintWriter out = response.getWriter();

            out.println(
                "<html><head><title>HSQL Database Engine Servlet</title>");
            out.println("</head><body><h1>HSQL Database Engine Servlet</h1>");
            out.println("The servlet is running.<P>");

            if (dDatabase != null) {
                out.println("The database is also running.<P>");
                out.println("Database name: " + sDatabase + "<P>");
                out.println("Queries processed: " + iQueries + "<P>");
            } else {
                out.println("<h2>The database is not running!</h2>");
                out.println("The error message is:<P>");
                out.println(sError);
            }

            out.println("</body></html>");
        }
    }

    /**
     * Method declaration
     *
     *
     * @param request
     * @param response
     *
     * @throws IOException
     * @throws ServletException
     */
    public void doPost(HttpServletRequest request,
                       HttpServletResponse response)
                       throws IOException, ServletException {

        ServletInputStream input = request.getInputStream();
        int                len   = request.getContentLength();
        byte               b[]   = new byte[len];

        input.read(b, 0, len);

        String s = new String(b);
        int    p = s.indexOf('+');
        int    q = s.indexOf('+', p + 1);

        if ((p == -1) || (q == -1)) {
            doGet(request, response);
        }

        String user     = s.substring(0, p);
        String password = s.substring(p + 1, q);

        s = s.substring(q + 1);

        try {
            user     = StringConverter.hexStringToUnicode(user);
            password = StringConverter.hexStringToUnicode(password);
            s        = StringConverter.hexStringToUnicode(s);
        } catch (SQLException e) {
            throw new ServletException();
        }

        response.setContentType("application/octet-stream");

        ServletOutputStream out      = response.getOutputStream();
        byte                result[] = dDatabase.execute(user, password, s);

        response.setContentLength(result.length);
        out.write(result);
        out.flush();
        out.close();

        iQueries++;

        // System.out.print("Queries processed: "+iQueries+"  \n");
    }

    static private int iQueries;
}
