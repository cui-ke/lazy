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

import java.sql.*;
import java.util.*;

/**
 * Base class for conversion from a different databases
 *
 * @author Nicolas BAZIN
 * @version 1.7.0
 */
public class JDBCTypes {

    private Hashtable hStringJDBCtypes;
    private Hashtable hIntJDBCtypes;

    public JDBCTypes() {

        hStringJDBCtypes = new Hashtable();
        hIntJDBCtypes    = new Hashtable();

//#ifdef JAVA2
        hStringJDBCtypes.put(new Integer(java.sql.Types.ARRAY), "ARRAY");
        hStringJDBCtypes.put(new Integer(java.sql.Types.BLOB), "BLOB");
        hStringJDBCtypes.put(new Integer(java.sql.Types.CLOB), "CLOB");
        hStringJDBCtypes.put(new Integer(java.sql.Types.DISTINCT),
                             "DISTINCT");
        hStringJDBCtypes.put(new Integer(java.sql.Types.JAVA_OBJECT),
                             "JAVA_OBJECT");
        hStringJDBCtypes.put(new Integer(java.sql.Types.REF), "REF");
        hStringJDBCtypes.put(new Integer(java.sql.Types.STRUCT), "STRUCT");

//#endif JAVA2
        hStringJDBCtypes.put(new Integer(java.sql.Types.BIGINT), "BIGINT");
        hStringJDBCtypes.put(new Integer(java.sql.Types.BINARY), "BINARY");
        hStringJDBCtypes.put(new Integer(java.sql.Types.BIT), "BIT");
        hStringJDBCtypes.put(new Integer(java.sql.Types.CHAR), "CHAR");
        hStringJDBCtypes.put(new Integer(java.sql.Types.DATE), "DATE");
        hStringJDBCtypes.put(new Integer(java.sql.Types.DECIMAL), "DECIMAL");
        hStringJDBCtypes.put(new Integer(java.sql.Types.DOUBLE), "DOUBLE");
        hStringJDBCtypes.put(new Integer(java.sql.Types.FLOAT), "FLOAT");
        hStringJDBCtypes.put(new Integer(java.sql.Types.INTEGER), "INTEGER");
        hStringJDBCtypes.put(new Integer(java.sql.Types.LONGVARBINARY),
                             "LONGVARBINARY");
        hStringJDBCtypes.put(new Integer(java.sql.Types.LONGVARCHAR),
                             "LONGVARCHAR");
        hStringJDBCtypes.put(new Integer(java.sql.Types.NULL), "NULL");
        hStringJDBCtypes.put(new Integer(java.sql.Types.NUMERIC), "NUMERIC");
        hStringJDBCtypes.put(new Integer(java.sql.Types.OTHER), "OTHER");
        hStringJDBCtypes.put(new Integer(java.sql.Types.REAL), "REAL");
        hStringJDBCtypes.put(new Integer(java.sql.Types.SMALLINT),
                             "SMALLINT");
        hStringJDBCtypes.put(new Integer(java.sql.Types.TIME), "TIME");
        hStringJDBCtypes.put(new Integer(java.sql.Types.TIMESTAMP),
                             "TIMESTAMP");
        hStringJDBCtypes.put(new Integer(java.sql.Types.TINYINT), "TINYINT");
        hStringJDBCtypes.put(new Integer(java.sql.Types.VARBINARY),
                             "VARBINARY");
        hStringJDBCtypes.put(new Integer(java.sql.Types.VARCHAR), "VARCHAR");

//#ifdef JAVA2
        hIntJDBCtypes.put("ARRAY", new Integer(java.sql.Types.ARRAY));
        hIntJDBCtypes.put("BLOB", new Integer(java.sql.Types.BLOB));
        hIntJDBCtypes.put("CLOB", new Integer(java.sql.Types.CLOB));
        hIntJDBCtypes.put("DISTINCT", new Integer(java.sql.Types.DISTINCT));
        hIntJDBCtypes.put("JAVA_OBJECT",
                          new Integer(java.sql.Types.JAVA_OBJECT));
        hIntJDBCtypes.put("REF", new Integer(java.sql.Types.REF));
        hIntJDBCtypes.put("STRUCT", new Integer(java.sql.Types.STRUCT));

//#endif JAVA2
        hIntJDBCtypes.put("BIGINT", new Integer(java.sql.Types.BIGINT));
        hIntJDBCtypes.put("BINARY", new Integer(java.sql.Types.BINARY));
        hIntJDBCtypes.put("BIT", new Integer(java.sql.Types.BIT));
        hIntJDBCtypes.put("CHAR", new Integer(java.sql.Types.CHAR));
        hIntJDBCtypes.put("DATE", new Integer(java.sql.Types.DATE));
        hIntJDBCtypes.put("DECIMAL", new Integer(java.sql.Types.DECIMAL));
        hIntJDBCtypes.put("DOUBLE", new Integer(java.sql.Types.DOUBLE));
        hIntJDBCtypes.put("FLOAT", new Integer(java.sql.Types.FLOAT));
        hIntJDBCtypes.put("INTEGER", new Integer(java.sql.Types.INTEGER));
        hIntJDBCtypes.put("LONGVARBINARY",
                          new Integer(java.sql.Types.LONGVARBINARY));
        hIntJDBCtypes.put("LONGVARCHAR",
                          new Integer(java.sql.Types.LONGVARCHAR));
        hIntJDBCtypes.put("NULL", new Integer(java.sql.Types.NULL));
        hIntJDBCtypes.put("NUMERIC", new Integer(java.sql.Types.NUMERIC));
        hIntJDBCtypes.put("OTHER", new Integer(java.sql.Types.OTHER));
        hIntJDBCtypes.put("REAL", new Integer(java.sql.Types.REAL));
        hIntJDBCtypes.put("SMALLINT", new Integer(java.sql.Types.SMALLINT));
        hIntJDBCtypes.put("TIME", new Integer(java.sql.Types.TIME));
        hIntJDBCtypes.put("TIMESTAMP", new Integer(java.sql.Types.TIMESTAMP));
        hIntJDBCtypes.put("TINYINT", new Integer(java.sql.Types.TINYINT));
        hIntJDBCtypes.put("VARBINARY", new Integer(java.sql.Types.VARBINARY));
        hIntJDBCtypes.put("VARCHAR", new Integer(java.sql.Types.VARCHAR));
    }

    public Hashtable getHashtable() {
        return hStringJDBCtypes;
    }

    public String toString(int type) {
        return (String) hStringJDBCtypes.get(new Integer(type));
    }

    public int toInt(String type) throws Exception {

        Integer tempInteger = (Integer) hIntJDBCtypes.get(type);

        return tempInteger.intValue();
    }
}
