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

import java.sql.SQLException;
import java.sql.Types;
import java.util.Hashtable;
import java.util.Vector;
import java.util.Enumeration;

/**
 * Script generation.
 *
 * @version 1.7.0
 */
class DatabaseScript {

    /**
     * Method declaration
     *
     *
     * @param bDrop
     * @param bInsert
     * @param bCached
     * @param session
     *
     * @return
     *
     * @throws SQLException
     */
    static Result getScript(Database dDatabase, boolean bDrop,
                            boolean bInsert, boolean bCached,
                            Session session) throws SQLException {

        Vector tTable          = dDatabase.getTables();
        Vector forwardFK       = new Vector();
        Vector forwardFKSource = new Vector();

        session.checkAdmin();

        Result r = new Result(1);

        r.colType[0] = Types.VARCHAR;
        r.sTable[0]  = "SYSTEM_SCRIPT";
        r.sLabel[0]  = "COMMAND";
        r.sName[0]   = "COMMAND";

        StringBuffer a;

        for (int i = 0, tSize = tTable.size(); i < tSize; i++) {
            Table t = (Table) tTable.elementAt(i);

// fredt@users 20020221 - patch 513005 by sqlbob@users (RMP)
            if (t.isTemp() || t.isView()) {
                continue;
            }

            if (bDrop) {
                addRow(r, "DROP TABLE " + t.getName().statementName);
            }

            a = new StringBuffer(128);

            getTableDDL(dDatabase, t, i, forwardFK, forwardFKSource, a);
            addRow(r, a.toString());

            for (int j = 1; j < t.getIndexCount(); j++) {
                Index index = t.getIndex(j);

                if (HsqlName.isReservedName(index.getName().name)) {

                    // the following are autocreated with the table
                    // indexes for primary keys
                    // indexes for unique constraints
                    // own table indexes for foreign keys
                    continue;
                }

                a = new StringBuffer(64);

                a.append("CREATE ");

                if (index.isUnique()) {
                    a.append("UNIQUE ");
                }

                a.append("INDEX ");
                a.append(index.getName().statementName);
                a.append(" ON ");
                a.append(t.getName().statementName);

                int col[] = index.getColumns();
                int len   = index.getVisibleColumns();

                getColumnList(t, col, len, a);
                addRow(r, a.toString());
            }

            if (t.isText() && t.isDataReadOnly()) {
                a = new StringBuffer("SET TABLE ");

                a.append(t.getName().statementName);
                a.append(" READONLY TRUE");
                addRow(r, a.toString());
            }

// sqlbob@users 04/27/2002 Added data source support
            String dataSource = getDataSource(t);

            if (dataSource != null) {
                addRow(r, dataSource);
            }

            if (bCached && t.isCached()) {
                if (i >= forwardFKSource.size()
                        || forwardFKSource.get(i) == null) {
                    addRow(r, getIndexRootsDDL(t));
                }
            }

            // trigger script
            int numTrigs = TriggerDef.numTrigs();

            for (int tv = 0; tv < numTrigs; tv++) {
                Vector trigVec = t.vTrigs[tv];
                int    trCount = trigVec.size();

                for (int k = 0; k < trCount; k++) {
                    a = ((TriggerDef) trigVec.elementAt(k)).toBuf();

                    addRow(r, a.toString());
                }
            }
        }

        for (int i = 0, tSize = forwardFK.size(); i < tSize; i++) {
            Constraint c = (Constraint) forwardFK.elementAt(i);

            a = new StringBuffer(128);

            a.append("ALTER TABLE ");
            a.append(c.getRef().getName().statementName);
            a.append(" ADD ");
            getFKStatement(c, a);
            addRow(r, a.toString());
        }

        for (int i = 0, tSize = forwardFKSource.size(); i < tSize; i++) {
            if (forwardFKSource.elementAt(i) != null) {
                addRow(r, getIndexRootsDDL((Table) tTable.elementAt(i)));
            }
        }

        Vector uv = dDatabase.getUserManager().getUsers();

        for (int i = 0, vSize = uv.size(); i < vSize; i++) {
            User u = (User) uv.elementAt(i);

            // todo: this is not a nice implementation
            if (u == null) {
                continue;
            }

            String name = u.getName();

            if (!name.equals("PUBLIC")) {
                a = new StringBuffer(128);

                a.append("CREATE USER ");
                a.append(name);
                a.append(" PASSWORD ");
                a.append('"');
                a.append(u.getPassword());
                a.append('"');

                if (u.isAdmin()) {
                    a.append(" ADMIN");
                }

                addRow(r, a.toString());
            }

            Hashtable rights = u.getRights();

            if (rights == null) {
                continue;
            }

            Enumeration e = rights.keys();

            while (e.hasMoreElements()) {
                String object = (String) e.nextElement();
                int    right  = ((Integer) (rights.get(object))).intValue();

                if (right == 0) {
                    continue;
                }

                a = new StringBuffer(64);

                a.append("GRANT ");
                a.append(UserManager.getRight(right));
                a.append(" ON ");
                a.append(object);
                a.append(" TO ");
                a.append(u.getName());
                addRow(r, a.toString());
            }
        }

        if (dDatabase.isIgnoreCase()) {
            addRow(r, "SET IGNORECASE TRUE");
        }

        Hashtable   h = dDatabase.getAlias();
        Enumeration e = h.keys();

        while (e.hasMoreElements()) {
            String       alias  = (String) e.nextElement();
            String       java   = (String) h.get(alias);
            StringBuffer buffer = new StringBuffer(64);

            buffer.append("CREATE ALIAS ");
            buffer.append(alias);
            buffer.append(" FOR \"");
            buffer.append(java);
            buffer.append('"');
            addRow(r, buffer.toString());
        }

// fredt@users 20020420 - patch523880 by leptipre@users - VIEW support
        for (int i = 0, tSize = tTable.size(); i < tSize; i++) {
            Table t = (Table) tTable.elementAt(i);

            if (t.isView()) {
                View v = (View) tTable.elementAt(i);

                if (bDrop) {
                    addRow(r, "DROP VIEW " + v.getName().name);
                }

                a = new StringBuffer(128);

                a.append("CREATE ");
                a.append("VIEW ");
                a.append(v.getName().statementName);
                a.append(" AS ");
                a.append(v.getStatement());
                addRow(r, a.toString());
            }
        }

        for (int i = 0, tSize = tTable.size(); i < tSize; i++) {
            Table t = (Table) tTable.elementAt(i);

            if (bInsert == false || t.isTemp() || t.isView()
                    || (t.isCached &&!bCached)
                    || (t.isText() && t.isDataReadOnly())) {
                continue;
            }

            Index   primary   = t.getPrimaryIndex();
            Node    x         = primary.first();
            boolean integrity = true;

            if (x != null) {
                integrity = false;

                // fredt@users - comment - is this necessary?
                // tables are in order and no rows break FK constraints
                // addRow(r, "SET REFERENTIAL_INTEGRITY FALSE");
            }

            while (x != null) {
                addRow(r, t.getInsertStatement(x.getData()));

                x = primary.next(x);
            }

/*
            if (!integrity) {
                addRow(r, "SET REFERENTIAL_INTEGRITY TRUE");
            }
*/

// fredt@users 20020221 - patch 513005 by sqlbob@users (RMP)
            if (t.isDataReadOnly()) {
                a = new StringBuffer("SET TABLE ");

                a.append(t.getName().statementName);
                a.append(" READONLY TRUE");
                addRow(r, a.toString());
            }
        }

        return r;
    }

    static String getIndexRootsDDL(Table t) throws SQLException {

        StringBuffer a = new StringBuffer(128);

        a.append("SET TABLE ");
        a.append(t.getName().statementName);
        a.append(" INDEX '");
        a.append(t.getIndexRoots());
        a.append('\'');

        return a.toString();
    }

    static void getTableDDL(Database dDatabase, Table t, int i,
                            Vector forwardFK, Vector forwardFKSource,
                            StringBuffer a) throws SQLException {

        a.append("CREATE ");

// fredt@users 20020221 - patch 513005 by sqlbob@users (RMP)
// required for Text Table support
        if (t.isText()) {
            a.append("TEXT ");
        } else if (t.isCached()) {
            a.append("CACHED ");
        }

        a.append("TABLE ");
        a.append(t.getName().statementName);
        a.append('(');

        int   columns = t.getColumnCount();
        Index pki     = t.getIndex(0);
        int   pk[]    = pki.getColumns();

        for (int j = 0; j < columns; j++) {
            Column column  = t.getColumn(j);
            String colname = column.columnName.statementName;

            a.append(colname);
            a.append(' ');

// fredt@users 20020130 - patch 491987 by jimbag@users
            String sType = Column.getTypeString(column.getType());

            a.append(sType);

            // append the size and scale if > 0
            if (column.getSize() > 0) {
                a.append('(');
                a.append(column.getSize());

                if (column.getScale() > 0) {
                    a.append(',');
                    a.append(column.getScale());
                }

                a.append(')');
            }

// fredt@users 20020218 - patch 1.7.0 by fredt - DEFAULT keyword
            if (column.getDefaultString() != null) {
                a.append(" DEFAULT ");
                a.append(Column.createSQLString(column.getDefaultString()));
            }

            if (!column.isNullable()) {
                a.append(" NOT NULL");
            }

            if (j == t.getIdentityColumn()) {
                a.append(" IDENTITY");
            }

            if ((pk.length == 1) && (j == pk[0])) {
                a.append(" PRIMARY KEY");
            }

            if (j < columns - 1) {
                a.append(',');
            }
        }

        if (pk.length > 1) {
            a.append(",CONSTRAINT ");
            a.append(pki.getName().statementName);
            a.append(" PRIMARY KEY");
            getColumnList(t, pk, pk.length, a);
        }

        Vector v = t.getConstraints();

        for (int j = 0, vSize = v.size(); j < vSize; j++) {
            Constraint c = (Constraint) v.elementAt(j);

            if (c.getType() == Constraint.UNIQUE) {
                a.append(",CONSTRAINT ");
                a.append(c.getName().statementName);
                a.append(" UNIQUE");

                int col[] = c.getMainColumns();

                getColumnList(c.getMain(), col, col.length, a);
            } else if (c.getType() == Constraint.FOREIGN_KEY) {

                // forward referencing FK
                Table maintable      = c.getMain();
                int   maintableindex = dDatabase.getTableIndex(maintable);

                if (maintableindex > i) {
                    if (i >= forwardFKSource.size()) {
                        forwardFKSource.setSize(i + 1);
                    }

                    forwardFKSource.setElementAt(c, i);
                    forwardFK.addElement(c);
                } else {
                    a.append(',');
                    getFKStatement(c, a);
                }
            }
        }

        a.append(')');
    }

    /**
     * Method declaration
     *
     *
     * @param t
     *
     * @return
     */
    static String getDataSource(Table t) throws SQLException {

        String dataSource = t.getDataSource();

        if (dataSource == null) {
            return null;
        }

        boolean      isDesc = t.isDescDataSource();
        StringBuffer a      = new StringBuffer(128);

        a.append("SET TABLE ");
        a.append(t.getName().statementName);
        a.append(" SOURCE \"");
        a.append(dataSource);
        a.append('"');

        if (isDesc) {
            a.append(" DESC");
        }

        return a.toString();
    }

    /**
     * Method declaration
     *
     *
     * @param t
     * @param col
     * @param len
     * @param a
     *
     * @return
     */
    private static void getColumnList(Table t, int col[], int len,
                                      StringBuffer a) {

        a.append('(');

        for (int i = 0; i < len; i++) {
            a.append(t.getColumn(col[i]).columnName.statementName);

            if (i < len - 1) {
                a.append(',');
            }
        }

        a.append(')');
    }

    /**
     * Method declaration
     *
     *
     * @param c
     * @param a
     *
     * @return
     */
    private static void getFKStatement(Constraint c, StringBuffer a) {

        a.append("CONSTRAINT ");
        a.append(c.getName().statementName);
        a.append(" FOREIGN KEY");

        int col[] = c.getRefColumns();

        getColumnList(c.getRef(), col, col.length, a);
        a.append(" REFERENCES ");
        a.append(c.getMain().getName().statementName);

        col = c.getMainColumns();

        getColumnList(c.getMain(), col, col.length, a);

        if (c.isCascade()) {
            a.append(" ON DELETE CASCADE");
        }
    }

    /**
     * Method declaration
     *
     *
     * @param r
     * @param sql
     */
    private static void addRow(Result r, String sql) {

        String s[] = new String[1];

        s[0] = sql;

        r.add(s);
    }
}
