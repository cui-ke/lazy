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


package org.hsqldb.util;

import java.util.Vector;

// sqlbob@users 20020407 - patch 1.7.0 - reengineering

/**
 * Common code in the Swing and AWT versions of ConnectionDialog
 * @version 1.7.0
 */
class ConnectionDialogCommon {

    private static String       connTypes[][];
    private final static String sJDBCTypes[][] = {
        {
            "HSQL Database Engine In-Memory", "org.hsqldb.jdbcDriver",
            "jdbc:hsqldb:."
        }, {
            "HSQL Database Engine Standalone", "org.hsqldb.jdbcDriver",
            "jdbc:hsqldb:\u00ABdatabase?\u00BB"
        }, {
            "HSQL Database Engine Server", "org.hsqldb.jdbcDriver",
            "jdbc:hsqldb:hsql://localhost"
        }, {
            "HSQL Database Engine WebServer", "org.hsqldb.jdbcDriver",
            "jdbc:hsqldb:http://\u00ABhost?\u00BB"
        }, {
            "JDBC-ODBC Bridge from Sun", "sun.jdbc.odbc.JdbcOdbcDriver",
            "jdbc:odbc:\u00ABdatabase?\u00BB"
        }, {
            "Cloudscape RMI", "RmiJdbc.RJDriver",
            "jdbc:rmi://\u00ABhost?\u00BB:1099/jdbc:cloudscape:"
            + "\u00ABdatabase?\u00BB;create=true"
        }, {
            "IBM DB2", "COM.ibm.db2.jdbc.app.DB2Driver",
            "jdbc:db2:\u00ABdatabase?\u00BB"
        }, {
            "IBM DB2 (thin)", "COM.ibm.db2.jdbc.net.DB2Driver",
            "jdbc:db2://\u00ABhost?\u00BB:6789/\u00ABdatabase?\u00BB"
        }, {
            "Informix", "com.informix.jdbc.IfxDriver",
            "jdbc:informix-sqli://\u00ABhost?\u00BB:1533/\u00ABdatabase?\u00BB:"
            + "INFORMIXSERVER=\u00ABserver?\u00BB"
        }, {
            "InstantDb", "jdbc.idbDriver",
            "jdbc:idb:\u00ABdatabase?\u00BB.prp"
        }, {
            "MM.MySQL", "org.gjt.mm.mysql.Driver",
            "jdbc:mysql://\u00ABhost?\u00BB/\u00ABdatabase?\u00BB"
        }, {
            "Oracle", "oracle.jdbc.driver.OracleDriver",
            "jdbc:oracle:oci8:@\u00ABdatabase?\u00BB"
        }, {
            "Oracle (thin)", "oracle.jdbc.driver.OracleDriver",
            "jdbc:oracle:thin:@\u00ABhost?\u00BB:1521:\u00ABdatabase?\u00BB"
        }, {
            "PointBase", "com.pointbase.jdbc.jdbcUniversalDriver",
            "jdbc:pointbase://\u00ABhost?\u00BB/\u00ABdatabase?\u00BB"
        }, {
            "PostgreSQL", "org.postgresql.Driver",
            "jdbc:postgresql://\u00ABhost?\u00BB/\u00ABdatabase?\u00BB"
        }, {
            "PostgreSQL v6.5", "postgresql.Driver",
            "jdbc:postgresql://\u00ABhost?\u00BB/\u00ABdatabase?\u00BB"
        }
    };

    static String[][] getTypes() {

        if (connTypes == null) {

            // Pluggable connection types:
            Vector plugTypes = new Vector();

            try {
                plugTypes = (Vector) Class.forName(
                    System.getProperty(
                        "org.hsqldb.util.ConnectionTypeClass")).newInstance();
            } catch (Exception e) {
                ;
            }

            connTypes =
                new String[(plugTypes.size() / 3) + sJDBCTypes.length][3];

            int i = 0;

            for (int j = 0; j < plugTypes.size(); i++) {
                connTypes[i]    = new String[3];
                connTypes[i][0] = plugTypes.elementAt(j++).toString();
                connTypes[i][1] = plugTypes.elementAt(j++).toString();
                connTypes[i][2] = plugTypes.elementAt(j++).toString();
            }

            for (int j = 0; j < sJDBCTypes.length; i++, j++) {
                connTypes[i]    = new String[3];
                connTypes[i][0] = sJDBCTypes[j][0];
                connTypes[i][1] = sJDBCTypes[j][1];
                connTypes[i][2] = sJDBCTypes[j][2];
            }
        }

        return (connTypes);
    }

    private ConnectionDialogCommon() {}
}
