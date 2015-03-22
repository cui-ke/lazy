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
 * ParameterizableInsert.java
 *
 * Created on June 8, 2002, 2:37 AM
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
final class ParameterizableInsert extends AbstractParameterizableStatement {

    static final int                    UNKNOWN = 0;
    static final int                    VALUES  = 1;
    static final int                    SELECT  = 2;
    private Table                       table;
    private Vector                      columns;
    private Vector                      values;
    private ParameterizableSelect       select;
    private int                         insertType;
    private Hashtable                   hValueListMap    = null;
    private Hashtable                   hSelectResultMap = null;
    private ParameterizableExpression[] aExpressionMap   = null;

    /** Creates a new instance of ParameterizableInsert */
    private ParameterizableInsert(Database db, Table t) throws SQLException {

        super(db);

        Trace.doAssert(t != null, "table is null");

        if (t.isView()) {
            throw Trace.error(Trace.NOT_A_TABLE, t.getName().name);
        }

        table      = t;
        columns    = null;
        values     = null;
        select     = null;
        insertType = UNKNOWN;
    }

    ParameterizableInsert(Database db, Table t, Vector cols,
                          ParameterizableSelect sel) throws SQLException {

        this(db, t);

        // may be null
        columns = cols;
        values  = null;

        Trace.doAssert(sel != null, "select is null");

        select = sel;

        makeSelectResultMap();

        insertType = SELECT;
    }

    ParameterizableInsert(Database db, Table t, Vector cols,
                          Vector vals) throws SQLException {

        this(db, t);

        columns = cols;

        Trace.doAssert(vals != null, "values vector is null");

        values = vals;

        makeValueListMap();

        insertType = VALUES;
    }

    private ParameterizableExpression valueAt(int i) {
        return (ParameterizableExpression) values.elementAt(i);
    }

    private String columnAt(int i) {
        return (String) columns.elementAt(i);
    }

    protected Result execImpl(Session session) throws SQLException {

        session.checkReadWrite();
        session.check(table.getName().name, UserManager.INSERT);

        switch (insertType) {

            case VALUES :

                //System.out.println("INSERT VALUES");
                return execInsertValuesImpl2(session);

            case SELECT :

                //System.out.println("INSERT SELECT");
                return execInsertSelectImpl(session);

            default :
                throw Trace.error(Trace.FUNCTION_NOT_SUPPORTED,
                                  "unknown insert type");
        }
    }

    private ParameterizableExpression[] makeExpressionMap()
    throws SQLException {

        String                      columnName;
        int                         columnCount;
        ParameterizableExpression[] expressions;
        Hashtable                   columnMap = makeValueListMap();

        columnCount = table.getColumnCount();
        expressions = new ParameterizableExpression[columnCount];

        for (int i = 0; i < columnCount; i++) {
            columnName = table.getColumn(i).columnName.name;
            expressions[i] =
                (ParameterizableExpression) columnMap.get(columnName);
        }

        return expressions;
    }

    private ParameterizableExpression[] getExpressionMap()
    throws SQLException {

        if (aExpressionMap == null) {
            aExpressionMap = makeExpressionMap();
        }

        return aExpressionMap;
    }

    private Result execInsertValuesImpl(Session session) throws SQLException {

        ParameterizableExpression expression;
        Hashtable                 columnMap;
        Column                    column;
        String                    columnName;
        String                    columnDefault;
        int                       columnType;
        int                       columnSize;
        int                       columnCount;
        Object                    value;
        Object[]                  row;
        String                    sesKey;
        boolean                   enforceSize;

        columnMap   = makeValueListMap();
        row         = table.getNewRow();
        columnCount = table.getColumnCount();
        sesKey      = "sql.enforce_size";
        enforceSize = database.getProperties().isPropertyTrue(sesKey);

        for (int i = 0; i < columnCount; i++) {
            column     = table.getColumn(i);
            columnType = column.getType();
            columnName = column.columnName.name;
            expression =
                (ParameterizableExpression) columnMap.get(columnName);

            if (expression == null) {
                columnDefault = column.getDefaultString();
                value         = (columnDefault == null) ? null
                                                        : Column
                                                        .convertObject(
                                                            columnDefault,
                                                                columnType);
            } else {
                value = expression.getValue(columnType);
            }

            if (enforceSize) {
                columnSize = column.getSize();
                value = ParameterizableStatementHelper.enforceSize(value,
                        columnType, columnSize, true);
            }

            //System.out.println("Setting column " + i + " to " + String.valueOf(value));
            row[i] = value;
        }

        table.insert(row, session);

        Result r = new Result();

        r.iUpdateCount = 1;

        return r;
    }

    private Result execInsertValuesImpl2(Session session)
    throws SQLException {

        ParameterizableExpression   expression;
        ParameterizableExpression[] expressions;
        Column                      column;
        String                      columnDefault;
        int                         columnType;
        int                         columnSize;
        int                         columnCount;
        Object                      value;
        Object[]                    row;
        String                      sesKey;
        boolean                     enforceSize;

        expressions = getExpressionMap();
        row         = table.getNewRow();
        columnCount = table.getColumnCount();
        sesKey      = "sql.enforce_size";
        enforceSize = database.getProperties().isPropertyTrue(sesKey);

        for (int i = 0; i < columnCount; i++) {
            column     = table.getColumn(i);
            columnType = column.getType();
            expression = expressions[i];

            if (expression == null) {
                columnDefault = column.getDefaultString();
                value         = (columnDefault == null) ? null
                                                        : Column
                                                        .convertObject(
                                                            columnDefault,
                                                                columnType);
            } else {
                value = expression.getValue(columnType);
            }

            if (enforceSize) {
                columnSize = column.getSize();
                value = ParameterizableStatementHelper.enforceSize(value,
                        columnType, columnSize, true);
            }

            //System.out.println("Setting column " + i + " to " + String.valueOf(value));
            row[i] = value;
        }

        table.insert(row, session);

        Result r = new Result();

        r.iUpdateCount = 1;

        return r;
    }

    private int insertResult(Result result, Hashtable columnMap,
                             Session session) throws SQLException {

        Record  record;
        Object  row[];
        int     tableColumnCount;
        Column  column;
        int     columnType;
        int     columnSize;
        String  columnDefault;
        Integer mappedColumn;
        int     updateCount = 0;
        Object  value;
        String  sesKey;
        boolean enforceSize;

        sesKey           = "sql.enforce_size";
        enforceSize      = database.getProperties().isPropertyTrue(sesKey);
        record           = result.rRoot;
        tableColumnCount = table.getColumnCount();

        while (record != null) {
            row = table.getNewRow();

            for (int i = 0; i < tableColumnCount; i++) {
                column       = table.getColumn(i);
                columnType   = column.getType();
                mappedColumn = (Integer) columnMap.get(new Integer(i));

                if (mappedColumn == null) {
                    columnDefault = column.getDefaultString();
                    value = Column.convertObject(columnDefault, columnType);
                } else {
                    value = record.data[mappedColumn.intValue()];
                    value = Column.convertObject(value, columnType);
                }

                if (enforceSize) {
                    columnSize = column.getSize();
                    value = ParameterizableStatementHelper.enforceSize(value,
                            columnType, columnSize, true);
                }

                row[i] = value;
            }

            table.insert(row, session);

            updateCount++;

            record = record.next;
        }

        return updateCount;
    }

    private Result execInsertSelectImpl(Session session) throws SQLException {

        Result    result;
        Hashtable columnMap;
        int       updateCount;

        columnMap = makeSelectResultMap();
        result    = select.getResult(session.getMaxRows());

        session.beginNestedTransaction();

        try {
            updateCount = insertResult(result, columnMap, session);

            session.endNestedTransaction(false);
        } catch (SQLException e) {

            // insert failed (violation of primary key)
            session.endNestedTransaction(true);

            throw e;
        }

        Result r = new Result();

        r.iUpdateCount = updateCount;

        return r;
    }

    private Hashtable makeValueListMap() throws SQLException {

        if (hValueListMap == null) {
            hValueListMap = (columns == null) ? makeFullValueListMap()
                                              : makePartialValueListMap();
        }

        return hValueListMap;
    }

    private Hashtable makeFullValueListMap() throws SQLException {

        Hashtable map;
        int       columnCount;

        columnCount = table.getColumnCount();

        Trace.doAssert((values.size() == columnCount),
                       "value list size differs from table column count");

        map = new Hashtable();

        for (int i = 0; i < columnCount; i++) {
            map.put(table.getColumn(i).columnName.name, valueAt(i));
        }

        return map;
    }

    private Hashtable makePartialValueListMap() throws SQLException {

        int       columnListSize;
        String    columnName;
        int       valueListSize;
        int       tableColumnCount;
        Hashtable map;

        columnListSize   = columns.size();
        valueListSize    = values.size();
        tableColumnCount = table.getColumnCount();

        Trace.doAssert((columnListSize <= tableColumnCount),
                       "more column list entries than table columns");
        Trace.doAssert((valueListSize == columnListSize),
                       "value list size differs from column list size");

        map = new Hashtable();

        for (int i = 0; i < columnListSize; i++) {
            columnName = columnAt(i);

            // Dup check:  (...coli_name, ..., coli_name,...) forms not allowed
            // do this check first, since anything already in map has already 
            // passed the table.getColumnNr(columnName) check
            Trace.check(map.get(columnName) == null,
                        Trace.COLUMN_ALREADY_EXISTS,
                        "duplicate entry in column list");

            // check if column list entry corresponds to existing table column 
            // throw if not
            table.getColumnNr(columnName);
            map.put(columnName, valueAt(i));
        }

        return map;
    }

    private Hashtable makeSelectResultMap() throws SQLException {

        if (hSelectResultMap == null) {
            hSelectResultMap = (columns == null) ? makeFullSelectResultMap()
                                                 : makePartialSelectResultMap();
        }

        return hSelectResultMap;
    }

    private Hashtable makeFullSelectResultMap() throws SQLException {

        Hashtable map;
        int       tableColumnCount;
        Integer   columnNumber;
        int       resultColumnCount;

        resultColumnCount = select.iResultLen;
        tableColumnCount  = table.getColumnCount();

        Trace.check(resultColumnCount == tableColumnCount,
                    Trace.COLUMN_COUNT_DOES_NOT_MATCH,
                    "result column count differes from table column count");

        map = new Hashtable();

        for (int i = 0; i < tableColumnCount; i++) {
            columnNumber = new Integer(i);

            map.put(columnNumber, columnNumber);
        }

        return map;
    }

    private Hashtable makePartialSelectResultMap() throws SQLException {

        Hashtable map;
        Hashtable dupMap;
        int       columnListSize;
        String    columnName;
        int       columnNumber;
        int       resultColumnCount;

        resultColumnCount = select.iResultLen;
        columnListSize    = columns.size();

        Trace.check(resultColumnCount == columnListSize,
                    Trace.COLUMN_COUNT_DOES_NOT_MATCH,
                    "result column count differs from column list size");

        map    = new Hashtable();
        dupMap = new Hashtable();

        for (int i = 0; i < columnListSize; i++) {
            columnName = columnAt(i);

            // Dup check:  (...coli_name, ..., coli_name,...) forms not allowed
            // do this check first, since anything already in map has already 
            // passed the table.getColumnNr(columnName) check
            Trace.check(dupMap.get(columnName) == null,
                        Trace.COLUMN_ALREADY_EXISTS,
                        "duplicate entry in column list");
            dupMap.put(columnName, columnName);

            // -check if column list entry corresponds to existing table column 
            // -throw if not
            columnNumber = table.getColumnNr(columnName);

            map.put(new Integer(i), new Integer(columnNumber));
        }

        return map;
    }
}
