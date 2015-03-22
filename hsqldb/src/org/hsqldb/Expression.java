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

// fredt@users 20020215 - patch 1.7.0 by fredt
// to preserve column size etc. when SELECT INTO TABLE is used

/**
 * Expression class declaration
 *
 *
 * @version    1.7.0
 */
class Expression {

    // leaf types
    static final int VALUE     = 1,
                     COLUMN    = 2,
                     QUERY     = 3,
                     TRUE      = 4,
                     VALUELIST = 5,
                     ASTERIX   = 6,
                     FUNCTION  = 7;

    // operations
    static final int NEGATE   = 9,
                     ADD      = 10,
                     SUBTRACT = 11,
                     MULTIPLY = 12,
                     DIVIDE   = 14,
                     CONCAT   = 15;

    // logical operations
    static final int NOT           = 20,
                     EQUAL         = 21,
                     BIGGER_EQUAL  = 22,
                     BIGGER        = 23,
                     SMALLER       = 24,
                     SMALLER_EQUAL = 25,
                     NOT_EQUAL     = 26,
                     LIKE          = 27,
                     AND           = 28,
                     OR            = 29,
                     IN            = 30,
                     EXISTS        = 31;

    // aggregate functions
    static final int COUNT      = 40,
                     SUM        = 41,
                     MIN        = 42,
                     MAX        = 43,
                     AVG        = 44,
                     DIST_COUNT = 45;

    // system functions
    static final int IFNULL   = 60,
                     CONVERT  = 61,
                     CASEWHEN = 62;

    // temporary used during paring
    static final int PLUS         = 100,
                     OPEN         = 101,
                     CLOSE        = 102,
                     SELECT       = 103,
                     COMMA        = 104,
                     STRINGCONCAT = 105,
                     BETWEEN      = 106,
                     CAST         = 107,
                     END          = 108;
    private int      iType;

    // nodes
    private Expression eArg, eArg2;

    // VALUE, VALUELIST
    private Object    oData;
    private Hashtable hList;
    private boolean   hListHasNull;
    private int       iDataType;

    // QUERY (correlated subquery)
    private Select sSelect;

    // FUNCTION
    private Function fFunction;

    // LIKE
    private char cLikeEscape;

    // COLUMN
    private String      sTable;
    private String      sColumn;
    private TableFilter tFilter;        // null if not yet resolved
    private int         iColumn;
    private boolean     columnQuoted;
    private int         iColumnSize;
    private int         iColumnScale;
    private String      sAlias;         // if it is a column of a select column list
    private boolean     aliasQuoted;
    private boolean     bDescending;    // if it is a column in a order by

// rougier@users 20020522 - patch 552830 - COUNT(DISTINCT)
    // {COUNT|SUM|MIN|MAX|AVG}(distinct ...)
    private boolean      isDistinctAggregate;
    static final Integer INTEGER_0 = new Integer(0);
    static final Integer INTEGER_1 = new Integer(1);

    /**
     * Constructor declaration
     *
     *
     * @param f
     */
    Expression(Function f) {
        iType     = FUNCTION;
        fFunction = f;
    }

    /**
     * Constructor declaration
     *
     *
     * @param e
     */
    Expression(Expression e) {

        iType       = e.iType;
        iDataType   = e.iDataType;
        eArg        = e.eArg;
        eArg2       = e.eArg2;
        cLikeEscape = e.cLikeEscape;
        sSelect     = e.sSelect;
        fFunction   = e.fFunction;
    }

    /**
     * Constructor declaration
     *
     *
     * @param s
     */
    Expression(Select s) {
        iType   = QUERY;
        sSelect = s;
    }

    /**
     * Constructor declaration
     *
     *
     * @param v
     */
    Expression(Vector v) {

        iType     = VALUELIST;
        iDataType = Types.VARCHAR;

        int len = v.size();

        hList = new Hashtable(len, 1);

        for (int i = 0; i < len; i++) {
            Object o = v.elementAt(i);

            if (o != null) {
                hList.put(o, this.INTEGER_1);
            } else {
                this.hListHasNull = true;
            }
        }
    }

    /**
     * Constructor declaration
     *
     *
     * @param type
     * @param e
     * @param e2
     */
    Expression(int type, Expression e, Expression e2) {

        iType = type;
        eArg  = e;
        eArg2 = e2;
    }

    /**
     * Constructor declaration
     *
     *
     * @param table
     * @param column
     */
    Expression(String table, String column) {

        sTable = table;

        if (column == null) {
            iType = ASTERIX;
        } else {
            iType   = COLUMN;
            sColumn = column;
        }
    }

    Expression(String table, String column, boolean isquoted) {

        sTable = table;

        if (column == null) {
            iType = ASTERIX;
        } else {
            iType        = COLUMN;
            sColumn      = column;
            columnQuoted = isquoted;
        }
    }

    /**
     * Constructor declaration
     *
     *
     * @param datatype
     * @param o
     */
    Expression(int datatype, Object o) {

        iType     = VALUE;
        iDataType = datatype;
        oData     = o;
    }

    /**
     * Method declaration
     *
     *
     * @param c
     */
    void setLikeEscape(char c) {
        cLikeEscape = c;
    }

    /**
     * Method declaration
     *
     *
     * @param type
     */
    void setDataType(int type) {
        iDataType = type;
    }

    /**
     * Method declaration
     *
     */
    void setTrue() {
        iType = TRUE;
    }

    /**
     * Method declaration
     *
     *
     * @return
     */
    boolean isAggregate() {
        return isAggregate(iType);
    }

    static boolean isAggregate(int type) {

        if ((type == COUNT) || (type == MAX) || (type == MIN)
                || (type == SUM) || (type == AVG) || (type == DIST_COUNT)) {
            return true;
        }

        // todo: recurse eArg and eArg2; maybe they are grouped.
        // grouping 'correctly' would be quite complex
        return false;
    }

    /**
     * Method declaration
     *
     */
    void setDescending() {
        bDescending = true;
    }

    /**
     * Method declaration
     *
     *
     * @return
     */
    boolean isDescending() {
        return bDescending;
    }

    /**
     * Method declaration
     *
     *
     * @param s
     */
    void setAlias(String s, boolean isquoted) {
        sAlias      = s;
        aliasQuoted = isquoted;
    }

    /**
     * Method declaration
     *
     *
     * @return
     */
    String getAlias() {

        if (sAlias != null) {
            return sAlias;
        }

        if (iType == VALUE) {
            return "";
        }

        if (iType == COLUMN) {
            return sColumn;
        }

// fredt@users 20020130 - patch 497872 by Nitin Chauhan - modified
// return column name for aggregates without alias
        if (isAggregate()) {
            return eArg.getColumnName();
        }

        return "";
    }

    /**
     * Method declaration
     *
     *
     * @return
     */
    boolean isAliasQuoted() {

        if (sAlias != null) {
            return aliasQuoted;
        }

        if (iType == COLUMN) {
            return columnQuoted;
        }

        if (isAggregate()) {
            return eArg.columnQuoted;
        }

        return false;
    }

    /**
     * Method declaration
     *
     *
     * @return
     */
    int getType() {
        return iType;
    }

    /**
     * Method declaration
     *
     *
     * @return
     */
    Expression getArg() {
        return eArg;
    }

    /**
     * Method declaration
     *
     *
     * @return
     */
    Expression getArg2() {
        return eArg2;
    }

    /**
     * Method declaration
     *
     *
     * @return
     */
    TableFilter getFilter() {
        return tFilter;
    }

    /**
     * Method declaration
     *
     *
     * @throws SQLException
     */
    void checkResolved() throws SQLException {

        Trace.check((iType != COLUMN) || (tFilter != null),
                    Trace.COLUMN_NOT_FOUND, sColumn);

        if (eArg != null) {
            eArg.checkResolved();
        }

        if (eArg2 != null) {
            eArg2.checkResolved();
        }

        if (sSelect != null) {
            sSelect.checkResolved();
        }

        if (fFunction != null) {
            fFunction.checkResolved();
        }
    }

    /**
     * Method declaration
     *
     *
     * @param f
     *
     * @throws SQLException
     */
    void resolve(TableFilter f) throws SQLException {

        if ((f != null) && (iType == COLUMN)) {
            if ((sTable == null) || f.getName().equals(sTable)) {
                int i = f.getTable().searchColumn(sColumn);

                if (i != -1) {

// fredt@users 20011110 - fix for 471711 - subselects
                    // todo: other error message: multiple tables are possible
                    Trace.check(
                        tFilter == null
                        || tFilter.getName().equals(
                            f.getName()), Trace.COLUMN_NOT_FOUND, sColumn);

                    tFilter      = f;
                    iColumn      = i;
                    sTable       = f.getName();
                    iDataType    = f.getTable().getColumn(i).getType();
                    iColumnSize  = f.getTable().getColumn(i).getSize();
                    iColumnScale = f.getTable().getColumn(i).getScale();
                }
            }
        }

        // currently sets only data type
        // todo: calculate fixed expressions if possible
        if (eArg != null) {
            eArg.resolve(f);
        }

        if (eArg2 != null) {
            eArg2.resolve(f);
        }

        if (sSelect != null) {
            sSelect.resolve(f, false);
            sSelect.resolve();
        }

        if (fFunction != null) {
            fFunction.resolve(f);
        }

        if (iDataType != 0) {
            return;
        }

        switch (iType) {

            case FUNCTION :
                iDataType = fFunction.getReturnType();
                break;

            case QUERY :
                iDataType = sSelect.eColumn[0].iDataType;
                break;

            case NEGATE :
                iDataType = eArg.iDataType;
                break;

            case ADD :
            case SUBTRACT :
            case MULTIPLY :
            case DIVIDE :

// fredt@users 20011010 - patch 442993 by fredt
                iDataType = Column.getCombinedNumberType(eArg.iDataType,
                        eArg2.iDataType, iType);
                break;

            case CONCAT :
                iDataType = Types.VARCHAR;
                break;

            case NOT :
            case EQUAL :
            case BIGGER_EQUAL :
            case BIGGER :
            case SMALLER :
            case SMALLER_EQUAL :
            case NOT_EQUAL :
            case LIKE :
            case AND :
            case OR :
            case IN :
            case EXISTS :
                iDataType = Types.BIT;
                break;

            case COUNT :
                iDataType = Types.INTEGER;
                break;

            case DIST_COUNT :
                if (eArg.iType == ASTERIX) {
                    iDataType = Types.INTEGER;
                }
            case MAX :
            case MIN :
            case SUM :
            case AVG :
                iDataType = eArg.iDataType;
                break;

            case CONVERT :

                // it is already set
                break;

            case IFNULL :
            case CASEWHEN :
                iDataType = eArg2.iDataType;
                break;
        }
    }

    /**
     * Method declaration
     *
     *
     * @return
     */
    boolean isResolved() {

        if (iType == VALUE) {
            return true;
        }

        if (iType == COLUMN) {
            return tFilter != null;
        }

        // todo: could recurse here, but never miss a 'false'!
        return false;
    }

    /**
     * Method declaration
     *
     *
     * @param i
     *
     * @return
     */
    static boolean isCompare(int i) {

        switch (i) {

            case EQUAL :
            case BIGGER_EQUAL :
            case BIGGER :
            case SMALLER :
            case SMALLER_EQUAL :
            case NOT_EQUAL :
                return true;
        }

        return false;
    }

    /**
     * Method declaration
     *
     *
     * @return
     */
    String getTableName() {

        if (iType == ASTERIX) {
            return sTable;
        }

        if (iType == COLUMN) {
            if (tFilter == null) {
                return sTable;
            } else {
                return tFilter.getTable().getName().name;
            }
        }

        // todo
        return "";
    }

    /**
     * Method declaration
     *
     *
     * @return
     */
    String getColumnName() {

        if (iType == COLUMN) {
            if (tFilter == null) {
                return sColumn;
            } else {
                return tFilter.getTable().getColumn(iColumn).columnName.name;
            }
        }

        return getAlias();
    }

    /**
     * Method declaration
     *
     *
     * @return
     */
    int getColumnNr() {
        return iColumn;
    }

    /**
     * Method declaration
     *
     *
     * @return
     */
    int getColumnSize() {
        return iColumnSize;
    }

    /**
     * Method declaration
     *
     *
     * @return
     */
    int getColumnScale() {
        return iColumnScale;
    }

    /**
     * Method declaration
     *
     * @return
     */
    boolean isDistinctAggregate() {
        return isDistinctAggregate;
    }

    /**
     * Method declaration
     *
     * @param type
     */
    void setDistinctAggregate(boolean type) {

        isDistinctAggregate = type;

        if (iType == COUNT || iType == DIST_COUNT) {
            iType     = type ? DIST_COUNT
                             : COUNT;
            iDataType = type ? iDataType
                             : Types.INTEGER;
        }
    }

    /**
     * Method declaration
     *
     *
     * @throws SQLException
     */
    void swapCondition() throws SQLException {

        int i = EQUAL;

        switch (iType) {

            case BIGGER_EQUAL :
                i = SMALLER_EQUAL;
                break;

            case SMALLER_EQUAL :
                i = BIGGER_EQUAL;
                break;

            case SMALLER :
                i = BIGGER;
                break;

            case BIGGER :
                i = SMALLER;
                break;

            case EQUAL :
                break;

            default :
                Trace.doAssert(false, "Expression.swapCondition");
        }

        iType = i;

        Expression e = eArg;

        eArg  = eArg2;
        eArg2 = e;
    }

    /**
     * Method declaration
     *
     *
     * @return
     */
    int getDataType() {
        return iDataType;
    }

    /**
     * Method declaration
     *
     *
     * @param type
     *
     * @return
     *
     * @throws SQLException
     */
    Object getValue(int type) throws SQLException {

        Object o = getValue();

        if ((o == null) || (iDataType == type)) {
            return o;
        }

        return Column.convertObject(o, type);
    }

    /**
     * Method declaration
     *
     *
     * @return
     *
     * @throws SQLException
     */
    Object getValue() throws SQLException {

        switch (iType) {

            case VALUE :
                return oData;

            case COLUMN :
                try {
                    return tFilter.oCurrentData[iColumn];
                } catch (NullPointerException e) {
                    throw Trace.error(Trace.COLUMN_NOT_FOUND, sColumn);
                }
            case FUNCTION :
                return fFunction.getValue();

            case QUERY :
                return sSelect.getValue(iDataType);

            case NEGATE :
                return Column.negate(eArg.getValue(iDataType), iDataType);

            case COUNT :

                // count(*): sum(1); count(col): sum(col<>null)
                if (eArg.iType == ASTERIX) {
                    return INTEGER_1;
                }

                if (eArg.getValue() == null) {
                    return INTEGER_0;
                } else {
                    return INTEGER_1;
                }
            case DIST_COUNT :
                if (eArg.iType == ASTERIX) {
                    return INTEGER_1;
                }
            case MAX :
            case MIN :
            case SUM :
            case AVG :
                return eArg.getValue();

            case EXISTS :
                return new Boolean(test());

            case CONVERT :
                return eArg.getValue(iDataType);

            case CASEWHEN :
                if (eArg.test()) {
                    return eArg2.eArg.getValue();
                } else {
                    return eArg2.eArg2.getValue();
                }
        }

        // todo: simplify this
        Object a = null,
               b = null;

        if (eArg != null) {
            a = eArg.getValue(iDataType);
        }

        if (eArg2 != null) {
            b = eArg2.getValue(iDataType);
        }

        switch (iType) {

            case ADD :
                return Column.add(a, b, iDataType);

            case SUBTRACT :
                return Column.subtract(a, b, iDataType);

            case MULTIPLY :
                return Column.multiply(a, b, iDataType);

            case DIVIDE :
                return Column.divide(a, b, iDataType);

            case CONCAT :
                return Column.concat(a, b);

            case IFNULL :
                return (a == null) ? b
                                   : a;

            default :

                // must be comparisation
                // todo: make sure it is
                return new Boolean(test());
        }
    }

    /**
     * Method declaration
     *
     *
     * @return
     *
     * @throws SQLException
     */
    boolean test() throws SQLException {

        switch (iType) {

            case TRUE :
                return true;

            case NOT :
                Trace.doAssert(eArg2 == null, "Expression.test");

                return !eArg.test();

            case AND :
                return eArg.test() && eArg2.test();

            case OR :
                return eArg.test() || eArg2.test();

            case LIKE :

                // todo: now for all tests a new 'like' object required!
                String s    = (String) eArg2.getValue(Types.VARCHAR);
                int    type = eArg.iDataType;
                Like l = new Like(s, cLikeEscape,
                                  type == Column.VARCHAR_IGNORECASE);
                String c = (String) eArg.getValue(Types.VARCHAR);

                return l.compare(c);

            case IN :
                return eArg2.testValueList(eArg.getValue(), eArg.iDataType);

            case EXISTS :
                Result r = eArg.sSelect.getResult(1);    // 1 is already enough

                return r.rRoot != null;
        }

        Trace.check(eArg != null, Trace.GENERAL_ERROR);

        Object o    = eArg.getValue();
        int    type = eArg.iDataType;

        Trace.check(eArg2 != null, Trace.GENERAL_ERROR);

        Object o2     = eArg2.getValue(type);
        int    result = Column.compare(o, o2, type);

        switch (iType) {

            case EQUAL :
                return result == 0;

            case BIGGER :
                return result > 0;

            case BIGGER_EQUAL :
                return result >= 0;

            case SMALLER_EQUAL :
                return result <= 0;

            case SMALLER :
                return result < 0;

            case NOT_EQUAL :
                return result != 0;
        }

        Trace.doAssert(false, "Expression.test2");

        return false;
    }

    /**
     * Method declaration
     *
     *
     * @param o
     * @param datatype
     *
     * @return
     *
     * @throws SQLException
     */
    private boolean testValueList(Object o,
                                  int datatype) throws SQLException {

        if (iType == VALUELIST) {
            if (datatype != iDataType) {
                o = Column.convertObject(o, iDataType);
            }

            if (o == null) {
                return hListHasNull;
            } else {
                return hList.containsKey(o);
            }
        } else if (iType == QUERY) {

            // todo: convert to valuelist before if everything is resolvable
            Result r    = sSelect.getResult(0);
            Record n    = r.rRoot;
            int    type = r.colType[0];

            if (datatype != type) {
                o = Column.convertObject(o, type);
            }

            while (n != null) {
                Object o2 = n.data[0];

                if ((o2 != null) && o2.equals(o)) {
                    return true;
                }

                n = n.next;
            }

            return false;
        }

        throw Trace.error(Trace.WRONG_DATA_TYPE);
    }
}
