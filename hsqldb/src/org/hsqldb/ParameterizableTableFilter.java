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
 * @version 1.0.0.1
 */
class ParameterizableTableFilter {

    private Table                     tTable;
    private String                    sAlias;
    private Index                     iIndex;
    private Node                      nCurrent;
    private Object                    oEmptyData[];
    private ParameterizableExpression eStart, eEnd;
    private ParameterizableExpression eAnd;
    private boolean                   bOuterJoin;

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
    ParameterizableTableFilter(Table t, String alias, boolean outerjoin) {

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
    void setCondition(ParameterizableExpression e) throws SQLException {

        int                       type = e.getType();
        ParameterizableExpression e1   = e.getArg();
        ParameterizableExpression e2   = e.getArg2();

        if (type == ParameterizableExpression.AND) {
            Trace.printSystemOut("TableFilter " + this + " setCondition(" + e
                                 + ") got AND");
            setCondition(e1);
            setCondition(e2);

            return;
        }

        int candidate;

        switch (type) {

            case ParameterizableExpression.NOT_EQUAL :
            case ParameterizableExpression.LIKE :    // todo: maybe use index
            case ParameterizableExpression.IN :
                Trace.printSystemOut("TableFilter " + this + " setCondition("
                                     + e + ") got NOT EQUAL, LIKE, or IN");

                candidate = 0;
                break;

            case ParameterizableExpression.EQUAL :
                Trace.printSystemOut("TableFilter " + this + " setCondition("
                                     + e + ") got EQUAL");

                candidate = 1;
                break;

            case ParameterizableExpression.BIGGER :
            case ParameterizableExpression.BIGGER_EQUAL :
                Trace.printSystemOut("TableFilter " + this + " setCondition("
                                     + e + ") got BIGGER | BIGGER EQUAL");

                candidate = 2;
                break;

            case ParameterizableExpression.SMALLER :
            case ParameterizableExpression.SMALLER_EQUAL :
                Trace.printSystemOut("TableFilter " + this + " setCondition("
                                     + e + ") got SMALLER | SMALLER EQUAL");

                candidate = 3;
                break;

            default :
                Trace.printSystemOut("TableFilter " + this + " setCondition("
                                     + e + ") got non-conditinal expression");

                // not a condition so forget it
                return;
        }

        if (e1.getFilter() == this) {    // ok include this
            Trace.printSystemOut("TableFilter " + this
                                 + " is filter for expression " + e1);
            Trace.printSystemOut("" + e1 + " is of type: "
                                 + e1.typeNames[e1.getType()]);
        } else if ((e2.getFilter() == this) && (candidate != 0)) {
            Trace.printSystemOut(
                "TableFilter " + this + "  is filter for expression " + e2
                + " but expr type is not in (NOT EQUAL, LIKE, or IN)");

            // swap and try again to allow index usage
            e.swapCondition();
            setCondition(e);

            return;
        } else {
            Trace.printSystemOut("TableFilter " + this
                                 + " is not filter for expression " + e2);
            Trace.printSystemOut("" + e2 + " is type: "
                                 + e2.typeNames[e2.getType()]);

            // unrelated: don't include
            return;
        }

        Trace.doAssert(e1.getFilter() == this, "setCondition");

        if (!e2.isResolved()) {
            Trace.printSystemOut("" + e2
                                 + " is not resolved and is of type: "
                                 + e2.typeNames[e2.getType()]);

            return;
        }

        Trace.printSystemOut("" + e2 + " is not resolved and is of type: "
                             + e2.typeNames[e2.getType()]);

        if (candidate == 0) {
            addAndCondition(e);
            Trace.printSystemOut(
                "expr " + e
                + " was determined to be (NOT EQUAL, LIKE, or IN) and was added as And condition");

            return;
        }

        int   i     = e1.getColumnNr();
        Index index = tTable.getIndexForColumn(i);

        Trace.printSystemOut("Candidate number is " + candidate);

        if (index != null) {
            Trace.printSystemOut("Considering index " + index.getName().name);
        }

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

            eStart = new ParameterizableExpression(e);
            eEnd   = eStart;
        } else if (candidate == 2) {

            // candidate for start
            if (eStart != null) {
                addAndCondition(e);

                return;
            }

            eStart = new ParameterizableExpression(e);
        } else if (candidate == 3) {

            // candidate for end
            if (eEnd != null) {
                addAndCondition(e);

                return;
            }

            eEnd = new ParameterizableExpression(e);
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

        Trace.printSystemOut("TableFilter " + this + " findFirst: index ["
                             + iIndex.getName().name + "]");

        if (eStart == null) {
            Trace.printSystemOut("eStart == null");

            nCurrent = iIndex.first();
        } else {
            int    type = eStart.getArg().getDataType();
            Object o    = eStart.getArg2().getValue(type);

            Trace.printSystemOut("eStart.getValue() == " + o);

            nCurrent = iIndex.findFirst(o, eStart.getType());
        }

        Trace.printSystemOut("nCurrent == " + nCurrent);

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
    private void addAndCondition(ParameterizableExpression e) {

        ParameterizableExpression e2 = new ParameterizableExpression(e);

        if (eAnd == null) {
            eAnd = e2;
        } else {
            ParameterizableExpression and =
                new ParameterizableExpression(ParameterizableExpression.AND,
                                              eAnd, e2);

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
    private boolean test(ParameterizableExpression e) throws SQLException {
        return (e == null) ? true
                           : e.test();
    }
}
