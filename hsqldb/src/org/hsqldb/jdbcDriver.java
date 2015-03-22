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

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.util.Properties;

// fredt@users 20020320 - patch 1.7.0 - JDBC 2 support and error trapping
// JDBC 2 methods can now be called from jdk 1.1.x - see javadoc comments

/**
 *  Each JDBC driver must supply a class that implements the Driver
 *  interface. <p>
 *
 *  The Java SQL framework allows for multiple database drivers. <p>
 *
 *  The DriverManager will try to load as many drivers as it can find and
 *  then for any given connection request, it will ask each driver in turn
 *  to try to connect to the target URL. <p>
 *
 *  <font color="#009900"> The application developer will normally not need
 *  to call any function of the Driver directly. All required calls are made
 *  by the DriverManager. <p>
 *
 *  When the HSQL Database Engine Driver class is loaded, it creates an
 *  instance of itself and register it with the DriverManager. This means
 *  that a user can load and register the HSQL Database Engine driver by
 *  calling <pre>
 * <code>Class.forName("org.hsqldb.jdbcDriver")</code> </pre> For more
 *  information about how to connect to a HSQL Database Engine database,
 *  please see jdbcConnection. </font><p>
 *
 * <font color="#009900"> As of version 1.7.0 all JDBC 2 methods can be
 *  called with jdk 1.1.x. Some of these method calls require int values
 *  that are defined in JDBC 2 version of ResultSet. These values are
 *  defined in the jdbcResultSet class when it is compiled with jdk 1.1.x.
 *  When using the JDBC 2 methods that require those values as parameters or
 *  return one of those values, refer to them as follows: (The code will not
 *  be compatible with other JDBC 2 driver, which require ResultSet to be
 *  used instead of jdbcResultSet) (fredt@users)</font> <p>
 * <font color="#009900">
 *  jdbcResultSet.FETCH_FORWARD<br>
 *  jdbcResultSet.TYPE_FORWARD_ONLY<br>
 *  jdbcResultSet TYPE_SCROLL_INSENSITIVE<br>
 *  jdbcResultSet.CONCUR_READ_ONLY</font><p>
 *
 *
 * @see  jdbcConnection
 */
// fredt@users 20011220 - patch 1.7.0 by fredt
// new version numbering scheme
public class jdbcDriver implements Driver {

    static final String sStartURL = "jdbc:hsqldb:";
    static final int    MAJOR     = 1,
                        MINOR     = 7,
                        REVISION  = 1;
    static final String VERSION   = "1.7.1";
    static final String PRODUCT   = "HSQL Database Engine";

    /**
     *  Attempts to make a database connection to the given URL. The driver
     *  returns "null" if it realizes it is the wrong kind of driver to
     *  connect to the given URL. This will be common, as when the JDBC
     *  driver manager is asked to connect to a given URL it passes the URL
     *  to each loaded driver in turn. <p>
     *
     *  The driver raises a SQLException if it is the right driver to
     *  connect to the given URL, but has trouble connecting to the
     *  database. <p>
     *
     *  The java.util.Properties argument can be used to passed arbitrary
     *  string tag/value pairs as connection arguments. <p>
     *
     *  <font color="#009900"> For HSQL Database Engine, at least "user" and
     *  "password" properties must be included in the Properties. </font>
     *  <p>
     *
     *
     *
     * @param  url the URL of the database to which to connect
     * @param  info a list of arbitrary string tag/value pairs as connection
     *      arguments. Normally at least a "user" and "password" property
     *      should be included.
     * @return  a <code>Connection</code> object that represents a
     *      connection to the URL
     * @exception  SQLException if a database access error occurs
     */
    public Connection connect(String url,
                              Properties info) throws SQLException {

        if (!acceptsURL(url)) {
            return null;
        }

        String s = url.substring(sStartURL.length());

        return new jdbcConnection(s, info);
    }

    /**
     *  Returns true if the driver thinks that it can open a connection to
     *  the given URL. Typically drivers will return true if they understand
     *  the subprotocol specified in the URL and false if they don't.
     *
     * @param  url the URL of the database
     * @return  true if this driver can connect to the given URL
     */

    // fredt@users - patch 1.70 - allow mixedcase url's when called externally
    public boolean acceptsURL(String url) {

        if (Trace.TRACE) {
            Trace.trace(url);
        }

        return url.toLowerCase().startsWith(sStartURL);
    }

    /**
     *  Gets information about the possible properties for this driver. <p>
     *
     *  The getPropertyInfo method is intended to allow a generic GUI tool
     *  to discover what properties it should prompt a human for in order to
     *  get enough information to connect to a database. Note that depending
     *  on the values the human has supplied so far, additional values may
     *  become necessary, so it may be necessary to iterate though several
     *  calls to getPropertyInfo.
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * HSQLDB 1.7.1 uses the values submitted in info to set the value for
     * each DriverPropertyInfo object returned. It does not use the default
     * value that it would use for the property if the value is null.
     *
     * </span> <!-- end release-specific documentation -->
     *
     * @param  url the URL of the database to which to connect
     * @param  info a proposed list of tag/value pairs that will be sent on
     *      connect open
     * @return  an array of DriverPropertyInfo objects describing possible
     *      properties. This array may be an empty array if no properties
     *      are required.
     */
    public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) {

        if (Trace.TRACE) {
            Trace.trace();
        }

        String[]           choices = new String[] {
            "true", "false"
        };
        DriverPropertyInfo pinfo[] = new DriverPropertyInfo[2];
        DriverPropertyInfo p;

        p          = new DriverPropertyInfo("user", null);
        p.value    = info.getProperty("user");
        p.required = true;
        pinfo[0]   = p;
        p          = new DriverPropertyInfo("password", null);
        p.value    = info.getProperty("password");
        p.required = true;
        pinfo[1]   = p;
        p          = new DriverPropertyInfo("strict_md", null);
        p.value    = info.getProperty("strict_md");
        p.required = false;
        p.choices  = choices;
        pinfo[1]   = p;
        p          = new DriverPropertyInfo("get_column_name", null);
        p.value    = info.getProperty("get_column_name");
        p.required = false;
        p.choices  = choices;
        pinfo[1]   = p;

        return pinfo;
    }

    /**
     *  Gets the driver's major version number.
     *
     * @return  this driver's major version number
     */
    public int getMajorVersion() {

        if (Trace.TRACE) {
            Trace.trace();
        }

        return MAJOR;
    }

    /**
     *  Gets the driver's minor version number.
     *
     * @return  this driver's minor version number
     */
    public int getMinorVersion() {

        if (Trace.TRACE) {
            Trace.trace();
        }

        return MINOR;
    }

    /**
     *  Reports whether this driver is a genuine JDBC COMPLIANT<sup><font
     *  size=-2>TM</font> </sup> driver. A driver may only report true here
     *  if it passes the JDBC compliance tests; otherwise it is required to
     *  return false. JDBC compliance requires full support for the JDBC API
     *  and full support for SQL 92 Entry Level. It is expected that JDBC
     *  compliant drivers will be available for all the major commercial
     *  databases. <p>
     *
     *  <font color="#009900"> HSQL Database Engine currently does not yet
     *  support all required SQL 92 Entry Level functionality and thus
     *  returns false. The features that are missing are currently 'HAVING'
     *  and views. It looks like other drivers return true but do not
     *  support all features. </font> <p>
     *
     *  This method is not intended to encourage the development of non-JDBC
     *  compliant drivers, but is a recognition of the fact that some
     *  vendors are interested in using the JDBC API and framework for
     *  lightweight databases that do not support full database
     *  functionality, or for special databases such as document information
     *  retrieval where a SQL implementation may not be feasible.
     *
     * @return  Description of the Return Value
     */
    public boolean jdbcCompliant() {

        if (Trace.TRACE) {
            Trace.trace();

            // todo: not all required features are implemented yet
        }

        return false;
    }

    static {
        try {
            DriverManager.registerDriver(new jdbcDriver());

            if (Trace.TRACE) {
                Trace.trace(PRODUCT + " " + VERSION);
            }
        } catch (Exception e) {
            if (Trace.TRACE) {
                Trace.trace(e.getMessage());
            }
        }
    }
}
