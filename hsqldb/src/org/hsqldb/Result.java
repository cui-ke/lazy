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

import java.io.IOException;
import java.sql.SQLException;

// fredt@users 20020130 - patch 1.7.0 by fredt
// to ensure consistency of r.rTail r.iSize in all operations
// methods for set operations moved here from Select.java
// fredt@users 20020130 - patch 1.7.0 by fredt
// rewrite of LIMIT n m to apply to each select statement separately
// tony_lai@users 20020820 - patch 595073 by tlai@users - duplicated exception msg

/**
 *  Class declaration
 *
 * @version    1.7.0
 */
class Result {

    private Record   rTail;
    private int      iSize;
    private int      iColumnCount;
    static final int UPDATECOUNT = 0;
    static final int ERROR       = 1;
    static final int DATA        = 2;
    int              iMode;
    String           sError;
    int              errorCode;
    int              iUpdateCount;
    Record           rRoot;
    String           sLabel[];
    String           sTable[];
    String           sName[];
    boolean          isLabelQuoted[];
    int              colType[];
    int              colSize[];
    int              colScale[];

    /**
     *  Constructor declaration
     */
    Result() {
        iMode        = UPDATECOUNT;
        iUpdateCount = 0;
    }

// fredt@users 20020221 - patch 513005 by sqlbob@users (RMP)

    /**
     *  Constructor declaration
     *
     * @param  error
     * @param  code   Description of the Parameter
     */
    Result(String error, int code) {

        iMode     = ERROR;
        sError    = error;
        errorCode = code;
    }

    /**
     *  Constructor declaration
     *
     * @param  columns
     */
    Result(int columns) {

        prepareData(columns);

        iColumnCount = columns;
    }

    /**
     *  Constructor declaration
     *
     * @param  b
     * @exception  SQLException  Description of the Exception
     */
    Result(byte b[]) throws SQLException {

        try {
            DatabaseRowInputInterface in = new BinaryServerRowInput(b);

            iMode = in.readIntData();

            if (iMode == ERROR) {

// tony_lai@users 20020820 - patch 595073
                int code = in.readIntData();

                throw Trace.getError(in.readString(), code);

//                throw Trace.getError(in.readIntData(), in.readString());
            } else if (iMode == UPDATECOUNT) {
                iUpdateCount = in.readIntData();
            } else if (iMode == DATA) {
                int l = in.readIntData();

                prepareData(l);

                iColumnCount = l;

                for (int i = 0; i < l; i++) {
                    colType[i] = in.readType();
                    sLabel[i]  = in.readString();
                    sTable[i]  = in.readString();
                    sName[i]   = in.readString();
                }

                while (in.available() != 0) {
                    add(in.readData(colType));
                }
            }
        } catch (IOException e) {
            throw Trace.error(Trace.TRANSFER_CORRUPTED);
        }
    }

    /**
     *  Method declaration
     *
     * @return
     */
    int getSize() {
        return iSize;
    }

    /**
     *  Method declaration
     *
     * @param  columns
     */
    void setColumnCount(int columns) {
        iColumnCount = columns;
    }

    /**
     *  Method declaration
     *
     * @return
     */
    int getColumnCount() {
        return iColumnCount;
    }

    /**
     *  Method declaration
     *
     * @param  a
     */
    void append(Result a) {

        if (rRoot == null) {
            rRoot = a.rRoot;
        } else {
            rTail.next = a.rRoot;
        }

        rTail = a.rTail;
        iSize += a.iSize;
    }

    /**
     *  Method declaration
     *
     * @param  a
     */
    void setRows(Result a) {

        rRoot = a.rRoot;
        rTail = a.rTail;
        iSize = a.iSize;
    }

    /**
     *  Method declaration
     *
     * @param  d
     */
    void add(Object d[]) {

        Record r = new Record();

        r.data = d;

        if (rRoot == null) {
            rRoot = r;
        } else {
            rTail.next = r;
        }

        rTail = r;

        iSize++;
    }

    /**
     *  Method declaration
     *
     * @param  limitstart  number of records to discard at the head
     * @param  limitcount  number of records to keep, all the rest if 0
     */

// fredt@users 20020130 - patch 1.7.0 by fredt
// rewritten and moved from Select.java
    void trimResult(int limitstart, int limitcount) {

        Record n = rRoot;

        if (n == null) {
            return;
        }

        if (limitstart >= iSize) {
            iSize = 0;
            rRoot = rTail = null;

            return;
        }

        iSize -= limitstart;

        for (int i = 0; i < limitstart; i++) {
            n = n.next;

            if (n == null) {

                // if iSize is consistent this block will never be reached
                iSize = 0;
                rRoot = rTail = n;

                return;
            }
        }

        rRoot = n;

        if (limitcount == 0 || limitcount >= iSize) {
            return;
        }

        for (int i = 1; i < limitcount; i++) {
            n = n.next;

            if (n == null) {

                // if iSize is consistent this block will never be reached
                return;
            }
        }

        n.next = null;
        rTail  = n;
    }

    /**
     *  Method declaration
     *
     * @throws  SQLException
     */

// fredt@users 20020130 - patch 1.7.0 by fredt
// to ensure consistency of r.rTail r.iSize in all set operations
    void removeDuplicates() throws SQLException {

        if (rRoot == null) {
            return;
        }

        int len     = getColumnCount();
        int order[] = new int[len];
        int way[]   = new int[len];

        for (int i = 0; i < len; i++) {
            order[i] = i;
            way[i]   = 1;
        }

        sortResult(order, way);

        Record n = rRoot;

        for (;;) {
            Record next = n.next;

            if (next == null) {
                break;
            }

            if (compareRecord(n.data, next.data, len) == 0) {
                n.next = next.next;

                iSize--;
            } else {
                n = next;
            }
        }

        rTail = n;

        Trace.doAssert(rTail.next == null,
                       "rTail not correct in Result.removeDuplicates iSise ="
                       + iSize);
    }

    /**
     *  Method declaration
     *
     * @param  minus
     * @throws  SQLException
     */
    void removeSecond(Result minus) throws SQLException {

        removeDuplicates();
        minus.removeDuplicates();

        int     len   = getColumnCount();
        Record  n     = rRoot;
        Record  last  = rRoot;
        boolean rootr = true;    // checking rootrecord
        Record  n2    = minus.rRoot;
        int     i     = 0;

        while (n != null && n2 != null) {
            i = compareRecord(n.data, n2.data, len);

            if (i == 0) {
                if (rootr) {
                    rRoot = last = n.next;
                } else {
                    last.next = n.next;
                }

                n = n.next;

                iSize--;
            } else if (i > 0) {    // r > minus
                n2 = n2.next;
            } else {               // r < minus
                last  = n;
                rootr = false;
                n     = n.next;
            }
        }

        for (; n != null; ) {
            last = n;
            n    = n.next;
        }

        rTail = last;

        Trace.doAssert(
            (rRoot == null && rTail == null) || rTail.next == null,
            "rTail not correct in Result.removeSecond iSise =" + iSize);
    }

    /**
     *  Method declaration
     *
     * @param  r2
     * @throws  SQLException
     */
    void removeDifferent(Result r2) throws SQLException {

        removeDuplicates();
        r2.removeDuplicates();

        int     len   = getColumnCount();
        Record  n     = rRoot;
        Record  last  = rRoot;
        boolean rootr = true;    // checking rootrecord
        Record  n2    = r2.rRoot;
        int     i     = 0;

        iSize = 0;

        while (n != null && n2 != null) {
            i = compareRecord(n.data, n2.data, len);

            if (i == 0) {             // same rows
                if (rootr) {
                    rRoot = n;        // make this the first record
                } else {
                    last.next = n;    // this is next record in resultset
                }

                rootr = false;
                last  = n;            // this is last record in resultset
                n     = n.next;
                n2    = n2.next;

                iSize++;
            } else if (i > 0) {       // r > r2
                n2 = n2.next;
            } else {                  // r < r2
                n = n.next;
            }
        }

        if (rootr) {             // if no lines in resultset
            rRoot = null;        // then return null
            last  = null;
        } else {
            last.next = null;    // else end resultset
        }

        rTail = last;

        Trace.doAssert(
            (rRoot == null && rTail == null) || rTail.next == null,
            "rTail not correct in Result.removeDifference iSise =" + iSize);
    }

    /**
     *  Method declaration
     *
     * @param  order
     * @param  way
     * @throws  SQLException
     */
    void sortResult(int order[], int way[]) throws SQLException {

        if (rRoot == null || rRoot.next == null) {
            return;
        }

        Record source0, source1;
        Record target[]     = new Record[2];
        Record targetlast[] = new Record[2];
        int    dest         = 0;
        Record n            = rRoot;

        while (n != null) {
            Record next = n.next;

            n.next       = target[dest];
            target[dest] = n;
            n            = next;
            dest         ^= 1;
        }

        for (int blocksize = 1; target[1] != null; blocksize <<= 1) {
            source0   = target[0];
            source1   = target[1];
            target[0] = target[1] = targetlast[0] = targetlast[1] = null;

            for (dest = 0; source0 != null; dest ^= 1) {
                int n0 = blocksize,
                    n1 = blocksize;

                while (true) {
                    if (n0 == 0 || source0 == null) {
                        if (n1 == 0 || source1 == null) {
                            break;
                        }

                        n       = source1;
                        source1 = source1.next;

                        n1--;
                    } else if (n1 == 0 || source1 == null) {
                        n       = source0;
                        source0 = source0.next;

                        n0--;
                    } else if (compareRecord(
                            source0.data, source1.data, order, way) > 0) {
                        n       = source1;
                        source1 = source1.next;

                        n1--;
                    } else {
                        n       = source0;
                        source0 = source0.next;

                        n0--;
                    }

                    if (target[dest] == null) {
                        target[dest] = n;
                    } else {
                        targetlast[dest].next = n;
                    }

                    targetlast[dest] = n;
                    n.next           = null;
                }
            }
        }

        rRoot = target[0];
        rTail = targetlast[0];

        Trace.doAssert(rTail.next == null,
                       "rTail not correct in Result.sortResult iSise ="
                       + iSize);
    }

    /**
     *  Method declaration
     *
     * @param  a
     * @param  b
     * @param  order
     * @param  way
     * @return
     * @throws  SQLException
     */
    private int compareRecord(Object a[], Object b[], int order[],
                              int way[]) throws SQLException {

        int i = Column.compare(a[order[0]], b[order[0]], colType[order[0]]);

        if (i == 0) {
            for (int j = 1; j < order.length; j++) {
                i = Column.compare(a[order[j]], b[order[j]],
                                   colType[order[j]]);

                if (i != 0) {
                    return i * way[j];
                }
            }
        }

        return i * way[0];
    }

    /**
     *  Method declaration
     *
     * @param  a
     * @param  b
     * @param  len
     * @return
     * @throws  SQLException
     */
    private int compareRecord(Object a[], Object b[],
                              int len) throws SQLException {

        for (int j = 0; j < len; j++) {
            int i = Column.compare(a[j], b[j], colType[j]);

            if (i != 0) {
                return i;
            }
        }

        return 0;
    }

// fredt@users 20020221 - patch 513005 by sqlbob@users (RMP)

    /**
     *  Method declaration
     *
     * @return
     * @throws  SQLException
     */
    byte[] getBytes() throws SQLException {

        try {
            DatabaseRowOutputInterface out = new BinaryServerRowOutput();

            out.writeIntData(iMode);

            if (iMode == UPDATECOUNT) {
                out.writeIntData(iUpdateCount);
            } else if (iMode == ERROR) {
                out.writeIntData(errorCode);
                out.writeString(sError);
            } else {
                int l = iColumnCount;

                out.writeIntData(l);

                Record n = rRoot;

                for (int i = 0; i < l; i++) {
                    out.writeType(colType[i]);
                    out.writeString(sLabel[i]);
                    out.writeString(sTable[i]);
                    out.writeString(sName[i]);
                }

                while (n != null) {
                    out.writeData(l, colType, n.data);

                    n = n.next;
                }
            }

            return out.toByteArray();
        } catch (IOException e) {
            throw Trace.error(Trace.TRANSFER_CORRUPTED);
        }
    }

    /**
     *  Method declaration
     *
     * @param  columns
     */
    private void prepareData(int columns) {

        iMode         = DATA;
        sLabel        = new String[columns];
        sTable        = new String[columns];
        sName         = new String[columns];
        isLabelQuoted = new boolean[columns];
        colType       = new int[columns];
        colSize       = new int[columns];
        colScale      = new int[columns];
    }
}
