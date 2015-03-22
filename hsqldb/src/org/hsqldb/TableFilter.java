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

/**
 * Class declaration
 *
 *
 * @version 1.7.0
 */
class TableFilter {

    private Table      tTable;
    private String     sAlias;
    private Index      iIndex;
    private Node       nCurrent;
    private Object     oEmptyData[];
    private Expression eStart, eEnd;
    private Expression eAnd;
    private boolean    bOuterJoin;

    // this is public to improve performance
    Object oCurrentData[];

    // Object[] getCurrent() {
    // return oCurrentData;
    // }

    /**
     * Constructor declaration
     *
     *
     * @param t
     * @param alias
     * @param outerjoin
     */
    TableFilter(Table t, String alias, boolean outerjoin) {

        tTable     = t;
        iIndex     = null;
        sAlias     = (alias != null) ? alias
                                     : t.getName().name;
        bOuterJoin = outerjoin;
        oEmptyData = tTable.getNewRow();
    }

    /**
     * Method declaration
     *
     *
     * @return
     */
    String getName() {
        return sAlias;
    }

    /**
     * Method declaration
     *
     *
     * @return
     */
    Table getTable() {
        return tTable;
    }

    /**
     * Method declaration
     *
     *
     * @param e
     *
     * @throws SQLException
     */
    void setCondition(Expression e) throws SQLException {

        int        type = e.getType();
        Expression e1   = e.getArg();
        Expression e2   = e.getArg2();

        if (type == Expression.AND) {
            setCondition(e1);
            setCondition(e2);

            return;
        }

        int candidate;

        switch (type) {

            case Expression.NOT_EQUAL :
            case Expression.LIKE :    // todo: maybe use index
            case Expression.IN :
                candidate = 0;
                break;

            case Expression.EQUAL :
                candidate = 1;
                break;

            case Expression.BIGGER :
            case Expression.BIGGER_EQUAL :
                candidate = 2;
                break;

            case Expression.SMALLER :
            case Expression.SMALLER_EQUAL :
                candidate = 3;
                break;

            default :

                // not a condition so forget it
                return;
        }

        if (e1.getFilter() == this) {    // ok include this
        } else if ((e2.getFilter() == this) && (candidate != 0)) {

            // swap and try again to allow index usage
            e.swapCondition();
            setCondition(e);

            return;
        } else {

            // unrelated: don't include
            return;
        }

        Trace.doAssert(e1.getFilter() == this, "setCondition");

        if (!e2.isResolved()) {
            return;
        }

        if (candidate == 0) {
            addAndCondition(e);

            return;
        }

        int   i     = e1.getColumnNr();
        Index index = tTable.getIndexForColumn(i);

        if ((index == null) || ((iIndex != index) && (iIndex != null))) {

            // no index or already another index is used
            addAndCondition(e);

            return;
        }

// fredt@users 20020221 - patch 513005 by sqlbob@users (RMP)
// fredt - comment - this is for text tables only
        if (tTable.isText()) {
            Index primary = tTable.getPrimaryIndex();

            if (index != primary) {
                Node readAll = primary.getRoot();

                while (readAll != null) {
                    readAll = readAll.getRight();
                }
            }
        }

        iIndex = index;

        if (candidate == 1) {

            // candidate for both start & end
            if ((eStart != null) || (eEnd != null)) {
                addAndCondition(e);

                return;
            }

            eStart = new Expression(e);
            eEnd   = eStart;
        } else if (candidate == 2) {

            // candidate for start
            if (eStart != null) {
                addAndCondition(e);

                return;
            }

            eStart = new Expression(e);
        } else if (candidate == 3) {

            // candidate for end
            if (eEnd != null) {
                addAndCondition(e);

                return;
            }

            eEnd = new Expression(e);
        }

        e.setTrue();
    }

    /**
     * Method declaration
     *
     *
     * @return
     *
     * @throws SQLException
     */
    boolean findFirst() throws SQLException {

        if (iIndex == null) {
            iIndex = tTable.getPrimaryIndex();
        }

        if (eStart == null) {
            nCurrent = iIndex.first();
        } else {
            int    type = eStart.getArg().getDataType();
            Object o    = eStart.getArg2().getValue(type);

            nCurrent = iIndex.findFirst(o, eStart.getType());
        }

        while (nCurrent != null) {
            oCurrentData = nCurrent.getData();

            if (!test(eEnd)) {
                break;
            }

            if (test(eAnd)) {
                return true;
            }

            nCurrent = iIndex.next(nCurrent);
        }

        oCurrentData = oEmptyData;

        if (bOuterJoin) {
            return true;
        }

        return false;
    }

    /**
     * Method declaration
     *
     *
     * @return
     *
     * @throws SQLException
     */
    boolean next() throws SQLException {

        if (bOuterJoin && (nCurrent == null)) {
            return false;
        }

        nCurrent = iIndex.next(nCurrent);

        while (nCurrent != null) {
            oCurrentData = nCurrent.getData();

            if (!test(eEnd)) {
                break;
            }

            if (test(eAnd)) {
                return true;
            }

            nCurrent = iIndex.next(nCurrent);
        }

        oCurrentData = oEmptyData;

        return false;
    }

    /**
     * Method declaration
     *
     *
     * @param e
     */
    private void addAndCondition(Expression e) {

        Expression e2 = new Expression(e);

        if (eAnd == null) {
            eAnd = e2;
        } else {
            Expression and = new Expression(Expression.AND, eAnd, e2);

            eAnd = and;
        }

        e.setTrue();
    }

    /**
     * Method declaration
     *
     *
     * @param e
     *
     * @return
     *
     * @throws SQLException
     */
    private boolean test(Expression e) throws SQLException {

        if (e == null) {
            return true;
        }

        return e.test();
    }
}
