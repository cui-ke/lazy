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
import java.io.IOException;

// fredt@users 20020221 - patch 513005 by sqlbob@users (RMP)

/**
 *  Class declaration
 *
 * @version    1.7.0
 */
class Node {

    static final int NO_POS = Row.NO_POS;
    private int      iBalance;    // currently, -2 means 'deleted'
    int              iLeft   = NO_POS;
    private int      iRight  = NO_POS;
    private int      iParent = NO_POS;
    Node             nLeft;
    Node             nRight;
    private Node     nParent;
    private int      iId;         // id of index this table
    Node             nNext;       // node of next index (nNext==null || nNext.iId=iId+1)
    private Row      rData;
    private int      iData = NO_POS;
    private Table    tTable;

    /**
     *  Constructor declaration
     *
     * @param  r
     * @param  in
     * @param  id
     * @exception  IOException   Description of the Exception
     * @exception  SQLException  Description of the Exception
     */
    Node(Row r, DatabaseRowInputInterface in,
            int id) throws IOException, SQLException {

        iId      = id;
        tTable   = r.getTable();
        rData    = r;
        iData    = r.iPos;
        iBalance = in.readIntData();
        iLeft    = in.readIntData();

        if (iLeft <= 0) {
            iLeft = NO_POS;
        }

        iRight = in.readIntData();

        if (iRight <= 0) {
            iRight = NO_POS;
        }

        iParent = in.readIntData();

        if (iParent <= 0) {
            iParent = NO_POS;
        }

        if (Trace.DOASSERT) {
            Trace.doAssert(iBalance != -2);
        }
    }

    /**
     *  Constructor declaration
     *
     * @param  r
     * @param  id
     */
    Node(Row r, int id) {

        iId    = id;
        tTable = r.getTable();

        if ((r.iPos == Row.NO_POS) ||!tTable.isCached()) {
            rData = r;
        } else {
            iData = r.iPos;
        }
    }

    /**
     *  Method declaration
     */
    void delete() {

        iBalance = -2;
        nLeft    = nRight = nParent = null;
        iLeft    = iRight = iParent = 0;
        tTable   = null;
    }

    /**
     *  Method declaration
     *
     * @return
     */
    int getKey() {

        if (rData != null) {
            return (rData.iPos);
        }

        return (iData);
    }

    void setKey(int pos) {
        iData = pos;
        rData = null;
    }

    /**
     *  Method declaration
     *
     * @return
     * @exception  SQLException  Description of the Exception
     */
    Row getRow() throws SQLException {

        if (rData != null) {
            return (rData);
        }

        if (iData == NO_POS) {
            return (null);
        }

        return (tTable.getRow(iData));
    }

    private Node findNode(int pos, int id) throws SQLException {

        Node ret = null;
        Row  r   = tTable.getRow(pos);

        if (r != null) {
            ret = r.getNode(id);
        }

        return (ret);
    }

    /**
     *  Method declaration
     *
     * @return
     * @throws  SQLException
     */
    Node getLeft() throws SQLException {

        if (Trace.DOASSERT) {
            Trace.doAssert(iBalance != -2);
        }

        if (nLeft != null) {
            return (nLeft);
        }

        if (iLeft == NO_POS) {
            return (null);
        }

// rData.iLastAccess=Row.iCurrentAccess++;
        return (findNode(iLeft, iId));
    }

    /**
     *  Method declaration
     *
     * @param  n
     * @throws  SQLException
     */
    void setLeft(Node n) throws SQLException {

        if (Trace.DOASSERT) {
            Trace.doAssert(iBalance != -2);
        }

        if (tTable.isIndexCached()) {
            getRow().changed();
        }

        iLeft = NO_POS;
        nLeft = null;

        if (!tTable.isIndexCached()) {
            nLeft = n;
        } else if (n != null) {
            iLeft = n.getKey();
        }
    }

    /**
     *  Method declaration
     *
     * @return
     * @throws  SQLException
     */
    Node getRight() throws SQLException {

        if (Trace.DOASSERT) {
            Trace.doAssert(iBalance != -2);
        }

        if (nRight != null) {
            return (nRight);
        }

        if (iRight == NO_POS) {
            return (null);
        }

// rData.iLastAccess=Row.iCurrentAccess++;
        return (findNode(iRight, iId));
    }

    /**
     *  Method declaration
     *
     * @param  n
     * @throws  SQLException
     */
    void setRight(Node n) throws SQLException {

        if (Trace.DOASSERT) {
            Trace.doAssert(iBalance != -2);
        }

        if (tTable.isIndexCached()) {
            getRow().changed();
        }

        iRight = NO_POS;
        nRight = null;

        if (!tTable.isIndexCached()) {
            nRight = n;
        } else if (n != null) {
            iRight = n.getKey();
        }
    }

    /**
     *  Method declaration
     *
     * @param  i
     * @throws  SQLException
     */
    void setNextKey(int i) throws SQLException {

        if (Trace.DOASSERT) {
            Trace.doAssert(iBalance != -2);
        }

        if (tTable.isIndexCached()) {
            getRow().changed();
        }

        iRight = i;
        nRight = null;
    }

    /**
     *  Method declaration
     *
     * @return
     * @throws  SQLException
     */
    Node getParent() throws SQLException {

        if (Trace.DOASSERT) {
            Trace.doAssert(iBalance != -2);
        }

        if (nParent != null) {
            return (nParent);
        }

        if (iParent == NO_POS) {
            return (null);
        }

// rData.iLastAccess=Row.iCurrentAccess++;
        return (findNode(iParent, iId));
    }

    /** test used by Row.java */
    boolean isRoot() {
        return (iParent == Node.NO_POS && nParent == null);
    }

    /**
     *  Method declaration
     *
     * @param  n
     * @throws  SQLException
     */
    void setParent(Node n) throws SQLException {

        if (Trace.DOASSERT) {
            Trace.doAssert(iBalance != -2);
        }

        if (tTable.isIndexCached()) {
            getRow().changed();
        }

        iParent = NO_POS;
        nParent = null;

        if (!tTable.isIndexCached()) {
            nParent = n;
        } else if (n != null) {
            iParent = n.getKey();
        }
    }

    /**
     *  Method declaration
     *
     * @return
     * @throws  SQLException
     */
    int getBalance() throws SQLException {

        if (Trace.DOASSERT) {
            Trace.doAssert(iBalance != -2);

// rData.iLastAccess=Row.iCurrentAccess++;
        }

        return iBalance;
    }

    /**
     *  Method declaration
     *
     * @param  b
     * @throws  SQLException
     */
    void setBalance(int b) throws SQLException {

        if (Trace.DOASSERT) {
            Trace.doAssert(iBalance != -2);
        }

        if (iBalance != b) {
            if (tTable.isIndexCached()) {
                getRow().changed();
            }

            iBalance = b;
        }
    }

    /**
     *  Method declaration
     *
     * @return
     * @throws  SQLException
     */
    public Object[] getData() throws SQLException {

        if (Trace.DOASSERT) {
            Trace.doAssert(iBalance != -2);
        }

        return (getRow().getData());
    }

    /**
     *  Method declaration
     *
     * @param  n
     * @return
     * @throws  SQLException
     */
    boolean equals(Node n) throws SQLException {

        if (Trace.DOASSERT) {
            Trace.doAssert(iBalance != -2);

// rData.iLastAccess=Row.iCurrentAccess++;
            if (n != this) {
                Trace.doAssert((getKey() == NO_POS) || (n == null)
                               || (n.getKey() != getKey()));
            } else {
                Trace.doAssert(n.getKey() == getKey());
            }
        }

        return n == this;
    }

    /**
     *  Method declaration
     *
     * @param  out
     * @throws  IOException
     * @throws  SQLException
     */
    void write(DatabaseRowOutputInterface out)
    throws IOException, SQLException {

        if (Trace.DOASSERT) {
            Trace.doAssert(iBalance != -2);
        }

        out.writeIntData(iBalance);
        out.writeIntData((iLeft == NO_POS) ? 0
                                           : iLeft);
        out.writeIntData((iRight == NO_POS) ? 0
                                            : iRight);
        out.writeIntData((iParent == NO_POS) ? 0
                                             : iParent);
    }
}
