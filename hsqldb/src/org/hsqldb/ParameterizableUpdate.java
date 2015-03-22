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


/*
 * ParameterizableUpdate.java
 *
 * Created on June 9, 2002, 11:16 PM
 */
package org.hsqldb;

import java.sql.SQLException;
import java.sql.Types;
import java.util.Vector;
import java.util.Hashtable;

/**
 *
 * @author  Campbell Boucher-Burnet, Camco & Associates Consulting
 */
public class ParameterizableUpdate extends AbstractParameterizableStatement {

    Table                               table;
    Vector                              columnNames;
    Vector                              columnValues;
    ParameterizableExpression           condition;
    private ParameterizableExpression[] exp;
    private int[]                       col;
    private int[]                       type;
    private int[]                       csize;
    int                                 len;
    ParameterizableTableFilter          filter;

    /** Creates a new instance of ParameterizableUpdate */
    public ParameterizableUpdate(Database db, Table t, Vector colNames,
                                 Vector colValues,
                                 ParameterizableExpression cond)
                                 throws SQLException {

        super(db);

        table        = t;
        columnNames  = colNames;
        columnValues = colValues;
        condition    = cond;

        init();
    }

    private void init() throws SQLException {

        len = columnValues.size();
        exp = new ParameterizableExpression[len];

        columnValues.copyInto(exp);

        col   = new int[len];
        type  = new int[len];
        csize = new int[len];

        for (int i = len - 1; i >= 0; i--) {
            String columnName = (String) columnNames.elementAt(i);

            col[i] = table.getColumnNr(columnName);

            Column column = table.getColumn(col[i]);

            type[i]  = column.getType();
            csize[i] = column.getSize();
        }
    }

    private void resolve() throws SQLException {

        filter = new ParameterizableTableFilter(table, null, false);

        if (condition != null) {
            if (condition.getType() == condition.TRUE) {
                condition.resetTrue();
            }

            for (int i = len - 1; i >= 0; i--) {
                exp[i].resolve(filter);
            }

            condition.resolve(filter);
            filter.setCondition(condition);
        }
    }

    protected Result execImpl(Session session) throws SQLException {

        resolve();

        int ltypev;

        // do the update
        table.fireAll(TriggerDef.UPDATE_BEFORE);

        ParameterizableExpression[] lexp       = exp;
        int                         lcol[]     = col;
        int                         ltype[]    = type;
        int                         lcsize[]   = csize;
        ParameterizableExpression   lcondition = condition;
        ParameterizableTableFilter  lfilter    = filter;
        int                         count      = 0;

        if (lfilter.findFirst()) {
            Result del  = new Result();                // don't need column count and so on
            Result ins  = new Result();
            int    size = table.getColumnCount();
            boolean enforceSize =
                database.getProperties().isPropertyTrue("sql.enforce_size");

            do {
                if (lcondition == null || lcondition.test()) {

                    //System.out.println("Update condition" + lcondition + " is true, with value:" + lcondition.getValue() );
                    Object nd[] = lfilter.oCurrentData;

                    del.add(nd);

                    Object ni[] = new Object[size];    // we already have the size //table.getNewRow();

                    // fredt@users 20020130 - patch 1.7.0 by fredt
                    System.arraycopy(nd, 0, ni, 0, size);

                    /*
                    for ( int i = size -1; i >= 0; i--) {
                        ni[i] = nd[i];
                    }
                     */

                    // fredt@users 20020130 - patch 491987 by jimbag@users - made optional
                    if (enforceSize) {
                        for (int i = len - 1; i >= 0; i--) {
                            ltypev = ltype[i];
                            ni[lcol[i]] =
                                ParameterizableStatementHelper.enforceSize(
                                    lexp[i].getValue(ltypev), ltypev,
                                    lcsize[i], true);
                        }
                    } else {
                        for (int i = len - 1; i >= 0; i--) {
                            ni[lcol[i]] = lexp[i].getValue(ltype[i]);
                        }
                    }

                    ins.add(ni);
                }
            } while (lfilter.next());

            session.beginNestedTransaction();

            try {
                Record nd = del.rRoot;

                while (nd != null) {
                    Object[] data = nd.data;

                    table.fireAll(TriggerDef.UPDATE_BEFORE_ROW, data);
                    table.deleteNoCheck(data, session, true);

                    nd = nd.next;
                }

                Record ni = ins.rRoot;

                while (ni != null) {
                    Object[] data = ni.data;

                    table.insertNoCheck(data, session, true);

                    ni = ni.next;

                    count++;
                }

                table.checkUpdate(lcol, del, ins);

                ni = ins.rRoot;

                while (ni != null) {

                    // fire triggers now that update has been checked
                    table.fireAll(TriggerDef.UPDATE_AFTER_ROW, ni.data);

                    ni = ni.next;
                }

                session.endNestedTransaction(false);
            } catch (SQLException e) {

                // update failed (constraint violation)
                session.endNestedTransaction(true);

                throw e;
            }
        }

        table.fireAll(TriggerDef.UPDATE_AFTER);

        Result r = new Result();

        r.iUpdateCount = count;

        return r;
    }
}
