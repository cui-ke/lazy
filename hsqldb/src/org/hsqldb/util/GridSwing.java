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

import java.util.Vector;
import javax.swing.table.*;

// sqlbob@users 20020401 - patch 1.7.0 by sqlbob (RMP) - enhancements

/** Simple table model to represent a grid of tuples.
 *
 * @version 1.7.0
 */
public class GridSwing extends AbstractTableModel {

    String[] headers;
    Vector   rows;

    /**
     * Default constructor.
     */
    public GridSwing() {

        super();

        headers = new String[0];    // initially empty
        rows    = new Vector();     // initially empty
    }

    /**
     * Get the name for the specified column.
     */
    public String getColumnName(int i) {
        return headers[i];
    }

    /**
     * Get the number of columns.
     */
    public int getColumnCount() {
        return headers.length;
    }

    /**
     * Get the number of rows currently in the table.
     */
    public int getRowCount() {
        return rows.size();
    }

    /**
     * Get the current column headings.
     */
    public String[] getHead() {
        return headers;
    }

    /**
     * Get the current table data.
     *  Each row is represented as a <code>String[]</code>
     *  with a single non-null value in the 0-relative
     *  column position.
     *  <p>The first row is at offset 0, the nth row at offset n etc.
     */
    public Vector getData() {
        return rows;
    }

    /**
     * Get the object at the specified cell location.
     */
    public Object getValueAt(int row, int col) {
        return ((String[]) rows.elementAt(row))[col];
    }

    /**
     * Set the name of the column headings.
     */
    public void setHead(String[] h) {

        headers = new String[h.length];

        // System.arraycopy(h, 0, headers, 0, h.length);
        for (int i = 0; i < h.length; i++) {
            headers[i] = h[i];
        }
    }

    /**
     * Append a tuple to the end of the table.
     */
    public void addRow(String[] r) {

        String[] row = new String[r.length];

        // System.arraycopy(r, 0, row, 0, r.length);
        for (int i = 0; i < r.length; i++) {
            row[i] = r[i];

            if (row[i] == null) {
                row[i] = "(null)";
            }
        }

        rows.addElement(row);
    }

    /**
     * Remove data from all cells in the table (without
     *  affecting the current headings).
     */
    public void clear() {
        rows.removeAllElements();
    }
}
