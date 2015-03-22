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

// fred@users 20020522 - patch 1.7.0 - aggregate functions with DISTINCT
// rougier@users 20020522 - patch 552830 - COUNT(DISTINCT)

/**
 * Class declaration
 *
 *
 * @version 1.7.0
 */
class Select {

    boolean          isPreProcess;
    boolean          isDistinctSelect;
    private boolean  isDistinctAggregate;
    private boolean  isAggregated;
    private boolean  isGrouped;
    private Object[] aggregateRow;
    private int      aggregateCount;
    TableFilter      tFilter[];
    Expression       eCondition;         // null means no condition
    Expression       havingCondition;    // null means none
    Expression       eColumn[];          // 'result', 'group' and 'order' columns
    int              iResultLen;         // number of columns that are 'result'
    int              iGroupLen;          // number of columns that are 'group'
    int              iOrderLen;          // number of columns that are 'order'
    Select           sUnion;             // null means no union select
    HsqlName         sIntoTable;         // null means not select..into

// fredt@users 20020221 - patch 513005 by sqlbob@users (RMP)
// type and logging attributes of sIntotable
    int intoType = Table.MEMORY_TABLE;

//    boolean          intoTemp;
    boolean          isIntoTableQuoted;
    int              iUnionType;
    static final int UNION     = 1,
                     UNIONALL  = 2,
                     INTERSECT = 3,
                     EXCEPT    = 4;

// fredt@users 20010701 - patch 1.6.1 by hybris
// basic implementation of LIMIT n m
    int limitStart = 0;                  // set only by the LIMIT keyword
    int limitCount = 0;                  // set only by the LIMIT keyword

    /**
     * Set to preprocess mode
     *
     */
    void setPreProcess() {
        isPreProcess = true;
    }

    /**
     * Method declaration
     *
     *
     * @throws SQLException
     */
    void resolve() throws SQLException {

        int len = tFilter.length;

        for (int i = 0; i < len; i++) {
            resolve(tFilter[i], true);
        }
    }

    /**
     * Method declaration
     *
     *
     * @param f
     * @param ownfilter
     *
     * @throws SQLException
     */
    void resolve(TableFilter f, boolean ownfilter) throws SQLException {

        if (eCondition != null) {

            // first set the table filter in the condition
            eCondition.resolve(f);

            if (f != null && ownfilter) {

                // the table filter tries to get as many conditions as
                // possible but only if it belongs to this query
                f.setCondition(eCondition);
            }
        }

        int len = eColumn.length;

        for (int i = 0; i < len; i++) {
            eColumn[i].resolve(f);
        }
    }

    /**
     * Method declaration
     *
     *
     * @throws SQLException
     */
    void checkResolved() throws SQLException {

        if (eCondition != null) {
            eCondition.checkResolved();
        }

        int len = eColumn.length;

        for (int i = 0; i < len; i++) {
            eColumn[i].checkResolved();
        }
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

        resolve();

        Result r    = getResult(2);    // 2 records are (already) too much
        int    size = r.getSize();
        int    len  = r.getColumnCount();

        Trace.check(size == 1 && len == 1, Trace.SINGLE_VALUE_EXPECTED);

        Object o = r.rRoot.data[0];

        if (r.colType[0] == type) {
            return o;
        }

        return Column.convertObject(o, type);
    }

    /**
     * maxrow may be 0 to indicate no limit on the number of rows, or -1
     * to indicate 0 size result (used for pre-processing the selects in
     * view statements. positive values limit the size of the result set.
     *
     *
     * @param maxrows
     *
     * @return
     *
     * @throws SQLException
     */

// fredt@users 20020130 - patch 471710 by fredt - LIMIT rewritten
// for SELECT LIMIT n m DISTINCT
// fredt@users 20020804 - patch 580347 by dkkopp - view speedup
    Result getResult(int maxrows) throws SQLException {

        resolve();
        checkResolved();

        if (sUnion != null && sUnion.iResultLen != iResultLen) {
            throw Trace.error(Trace.COLUMN_COUNT_DOES_NOT_MATCH);
        }

        if (iGroupLen > 0) {    // has been set in Parser
            isGrouped = true;
        }

        int    len = eColumn.length;
        Result r   = new Result(len);

        for (int i = 0; i < len; i++) {
            Expression e = eColumn[i];

            r.colType[i]  = e.getDataType();
            r.colSize[i]  = e.getColumnSize();
            r.colScale[i] = e.getColumnScale();

            if (e.isAggregate()) {
                isAggregated = true;

                if (!isGrouped && e.isDistinctAggregate()) {
                    isDistinctAggregate = true;
                }
            }
        }

//        Object aggregateRow[] = null;
        if (isAggregated) {
            aggregateRow = new Object[len];
        }

// fredt@users 20020130 - patch 471710 by fredt - LIMIT rewritten
// for SELECT LIMIT n m DISTINCT
// find cases where the result does not have to be fully built and
// set issimplemaxrows and adjust maxrows with LIMIT params
// chnages made to apply LIMIT only to the containing SELECT
// so they can be used as part of UNION and other set operations
        if (maxrows == 0) {
            maxrows = limitCount;
        } else if (limitCount == 0) {
            limitCount = maxrows;
        } else {
            maxrows = limitCount = (maxrows > limitCount) ? limitCount
                                                          : maxrows;
        }

        boolean issimplemaxrows = false;

        if (maxrows != 0 && isDistinctSelect == false
                && isDistinctAggregate == false && isGrouped == false
                && sUnion == null && iOrderLen == 0) {
            issimplemaxrows = true;
        }

        int limitcount = issimplemaxrows ? limitStart + maxrows
                                         : Integer.MAX_VALUE;

        buildResult(r, limitcount);

        if (isAggregated &&!isGrouped &&!isDistinctAggregate) {
            addAggregateRow(r, aggregateRow, len, aggregateCount);
        } else if (isGrouped) {
            groupResult(r);
        } else if (isDistinctAggregate) {
            r.removeDuplicates();
            buildDistinctAggregates(r);

            for (int i = 0; i < len; i++) {
                Expression e = eColumn[i];

                e.setDistinctAggregate(false);

                r.colType[i]  = e.getDataType();
                r.colSize[i]  = e.getColumnSize();
                r.colScale[i] = e.getColumnScale();
            }
        }

        // the result is maybe bigger (due to group and order by)
        // but don't tell this anybody else
        if (isDistinctSelect) {
            int fullColumnCount = r.getColumnCount();

            r.setColumnCount(iResultLen);
            r.removeDuplicates();
            r.setColumnCount(fullColumnCount);
        }

        if (iOrderLen != 0) {
            int order[] = new int[iOrderLen];
            int way[]   = new int[iOrderLen];

// fredt@users 20020230 - patch 495938 by johnhobs@users - GROUP BY order
            for (int i = iResultLen + (isGrouped ? iGroupLen
                                                 : 0), j = 0; j < iOrderLen;
                    i++, j++) {
                order[j] = i;
                way[j]   = eColumn[i].isDescending() ? -1
                                                     : 1;
            }

            r.sortResult(order, way);
        }

        // fredt - now there is no need for the sort and group columns
        r.setColumnCount(iResultLen);

        for (int i = 0; i < iResultLen; i++) {
            Expression e = eColumn[i];

            r.sLabel[i]        = e.getAlias();
            r.isLabelQuoted[i] = e.isAliasQuoted();
            r.sTable[i]        = e.getTableName();
            r.sName[i]         = e.getColumnName();
        }

// fredt@users 20020130 - patch 471710 - LIMIT rewritten
        r.trimResult(limitStart, limitCount);

        if (sUnion != null) {
            Result x = sUnion.getResult(0);

            if (iUnionType == UNION) {
                r.append(x);
                r.removeDuplicates();
            } else if (iUnionType == UNIONALL) {
                r.append(x);
            } else if (iUnionType == INTERSECT) {
                r.removeDifferent(x);
            } else if (iUnionType == EXCEPT) {
                r.removeSecond(x);
            }
        }

        return r;
    }

    /**
     * Method declaration
     *
     *
     * @param row
     * @param n
     * @param len
     *
     * @throws SQLException
     */
    private void updateAggregateRow(Object row[], Object n[],
                                    int len) throws SQLException {

        for (int i = 0; i < len; i++) {
            int type = eColumn[i].getDataType();

            switch (eColumn[i].getType()) {

                case Expression.DIST_COUNT :
                    Integer increment = (n[i] == null) ? Expression.INTEGER_0
                                                       : Expression.INTEGER_1;

                    row[i] = Column.sum(row[i], increment, Types.INTEGER);
                    break;

                case Expression.COUNT :
                case Expression.AVG :
                case Expression.SUM :
                    row[i] = Column.sum(row[i], n[i], type);
                    break;

                case Expression.MIN :
                    row[i] = Column.min(row[i], n[i], type);
                    break;

                case Expression.MAX :
                    row[i] = Column.max(row[i], n[i], type);
                    break;

                default :
                    row[i] = n[i];
                    break;
            }
        }
    }

    /**
     * Method declaration
     *
     *
     * @param x
     * @param row
     * @param len
     * @param count
     *
     * @throws SQLException
     */
    private void addAggregateRow(Result x, Object row[], int len,
                                 int count) throws SQLException {

        for (int i = 0; i < len; i++) {
            int t = eColumn[i].getType();

            if (t == Expression.AVG) {
                row[i] = Column.avg(row[i], eColumn[i].getDataType(), count);
            } else if (t == Expression.COUNT) {

                // this fixes the problem with count(*) on a empty table
                if (row[i] == null) {
                    row[i] = Expression.INTEGER_0;
                }
            }
        }

        x.add(row);
    }

    private void buildResult(Result r, int limitcount) throws SQLException {

        int     len     = eColumn.length;
        int     count   = 0;
        int     filter  = tFilter.length;
        boolean first[] = new boolean[filter];
        int     level   = 0;
        boolean addtoaggregate = isAggregated &&!isGrouped
                                 &&!isDistinctAggregate;

        while (level >= 0 &&!isPreProcess) {
            TableFilter t = tFilter[level];
            boolean     found;

            if (!first[level]) {
                found        = t.findFirst();
                first[level] = found;
            } else {
                found        = t.next();
                first[level] = found;
            }

            if (!found) {
                level--;

                continue;
            }

            if (level < filter - 1) {
                level++;

                continue;
            }

            // apply condition
            if (eCondition == null || eCondition.test()) {
                Object row[] = new Object[len];

                for (int i = 0; i < len; i++) {
                    row[i] = eColumn[i].getValue();
                }

                count++;

// fredt@users 20010701 - patch for bug 416144 416146 430615 by fredt
                if (addtoaggregate) {
                    updateAggregateRow(aggregateRow, row, len);
                } else {
                    r.add(row);

                    if (count >= limitcount) {
                        break;
                    }
                }
            }
        }

        if (addtoaggregate) {
            aggregateCount = count;
        }
    }

    private void groupResult(Result r) throws SQLException {

        int len     = eColumn.length;
        int count   = 0;
        int order[] = new int[iGroupLen];
        int way[]   = new int[iGroupLen];

        for (int i = iResultLen, j = 0; j < iGroupLen; i++, j++) {
            order[j] = i;
            way[j]   = 1;
        }

        r.sortResult(order, way);

        Record n = r.rRoot;
        Result x = new Result(len);

        do {
            Object row[] = new Object[len];

            count = 0;

            boolean newgroup = false;

            while (n != null && newgroup == false) {
                count++;

// fredt@users 20020215 - patch 476650 by johnhobs@users - GROUP BY aggregates
                for (int i = iResultLen; i < iResultLen + iGroupLen; i++) {
                    if (n.next == null) {
                        newgroup = true;
                    } else if (Column.compare(
                            n.data[i], n.next.data[i], r.colType[i]) != 0) {

                        // can't use .equals because 'null' is also one group
                        newgroup = true;
                    }
                }

                updateAggregateRow(row, n.data, len);

                n = n.next;
            }

// fredt@users 20020320 - patch 476650 by fredt - empty GROUP BY
            if (isAggregated || count > 0) {
                addAggregateRow(x, row, len, count);
            }
        } while (n != null);

        r.setRows(x);
    }

    private void buildDistinctAggregates(Result r) throws SQLException {

        int    len   = eColumn.length;
        int    count = 0;
        Record n     = r.rRoot;
        Result x     = new Result(len);
        Object row[] = new Object[len];

        count = 0;

        while (n != null) {
            count++;

            updateAggregateRow(row, n.data, len);

            n = n.next;
        }

        if (isAggregated || count > 0) {
            addAggregateRow(x, row, len, count);
        }

        r.setRows(x);
    }
}
