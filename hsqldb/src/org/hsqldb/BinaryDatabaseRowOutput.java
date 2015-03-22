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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Types;

/**
 *  Provides methods for writing the data for a row to a byte array. The
 *  format of data is that used for storage of cached tables by v.1.6.x
 *  databases.
 *
 * @version  1.7.0
 */
class BinaryDatabaseRowOutput extends org.hsqldb.DatabaseRowOutput {

    private ByteArrayOutputStream byteOut = (ByteArrayOutputStream) out;

    /**
     *  Constructor used for a Result row
     *
     * @exception  IOException when an IO error is encountered
     */
    public BinaryDatabaseRowOutput() throws IOException {
        super(new ByteArrayOutputStream());
    }

    /**
     *  Constructor used for persistent storage of a Table row
     *
     * @param  size no of bytes of storage used
     * @exception  IOException when an IO error is encountered
     */
    public BinaryDatabaseRowOutput(int size) throws IOException {
        super(new ByteArrayOutputStream(size));
    }

// fredt@users - comment - methods for writing column type, name and data size
    public void writeIntData(int i) throws IOException {
        writeInt(i);
    }

    public void writePos(int pos) throws IOException {
        writeInt(pos);
    }

    public void writeSize(int size) throws IOException {
        writeInt(size);
    }

    public void writeType(int type) throws IOException {
        writeInt(type);
    }

    public void writeString(String s) throws IOException {
        writeUTF(s);
    }

    public byte[] toByteArray() throws IOException {

        byte ret[] = byteOut.toByteArray();

        byteOut.reset();

        return (ret);
    }

// fredt@users - comment - methods used for writing each SQL type
    protected void writeFieldType(int type) throws IOException {
        writeInt(type);
    }

    protected void writeNull(int type) throws IOException {
        writeType(Types.NULL);
    }

    protected void writeChar(String s, int t) throws IOException {
        writeUTF(s);
    }

    //fredt: REAL, TINYINT and SMALLINT are written in the old format
    // for compatibility
    protected void writeSmallint(Number o) throws IOException, SQLException {
        writeString(o.toString());
    }

    protected void writeInteger(Number o) throws IOException, SQLException {
        writeInt(o.intValue());
    }

    protected void writeBigint(Number o) throws IOException, SQLException {
        writeString(o.toString());
    }

    protected void writeReal(Double o,
                             int type) throws IOException, SQLException {

        if (type == Types.REAL) {
            writeString(o.toString());
        } else {

            // some JDKs have a problem with this:
            // out.writeDouble(((Double)o).doubleValue());
            writeLong(Double.doubleToLongBits(o.doubleValue()));
        }
    }

    protected void writeDecimal(java.math.BigDecimal o)
    throws IOException, SQLException {
        writeString(o.toString());
    }

    protected void writeBit(Boolean o) throws IOException, SQLException {
        writeString(o.toString());
    }

    protected void writeDate(java.sql.Date o)
    throws IOException, SQLException {
        writeString(o.toString());
    }

    protected void writeTime(java.sql.Time o)
    throws IOException, SQLException {
        writeString(o.toString());
    }

    protected void writeTimestamp(java.sql.Timestamp o)
    throws IOException, SQLException {
        writeString(o.toString());
    }

    protected void writeOther(Object o) throws IOException, SQLException {

        byte[] ba = ByteArray.serialize(o);

        writeByteArray(ba);
    }

    protected void writeBinary(byte[] o,
                               int t) throws IOException, SQLException {
        writeByteArray(o);
    }

// fredt@users - comment - helper and conversion methods
    protected void writeByteArray(byte b[]) throws IOException {

        writeString("**");    //new format flag
        writeInt(b.length);
        write(b, 0, b.length);
    }

    /**
     *  Calculate the size of byte array required to store a row.
     *
     * @param  row - a database row
     * @return  size of byte array
     * @exception  SQLException When data is inconsistent
     */
    public static int getSize(Row row) throws SQLException {

        Object data[] = row.getData();
        int    type[] = row.getTable().getColumnTypes();

        return getSize(data, data.length, type);
    }

    /**
     *  Calculate the size of byte array required to store a row.
     *
     * @param  data - the row data
     * @param  l - number of data[] elements to include in calculation
     * @param  type - array of java.sql.Types values
     * @return size of byte array
     * @exception  SQLException when data is inconsistent
     */
    private static int getSize(Object data[], int l,
                               int type[]) throws SQLException {

        int s = 0;

        for (int i = 0; i < l; i++) {
            Object o = data[i];

            s += 4;                               // type

            if (o != null) {
                switch (type[i]) {

                    case Types.INTEGER :
                        s += 4;
                        break;

                    case Types.FLOAT :
                    case Types.DOUBLE :
                        s += 8;
                        break;

                    case Types.BINARY :
                    case Types.VARBINARY :
                    case Types.LONGVARBINARY :
                        s += getUTFsize("**");    //new format flag
                        s += 4;
                        s += ((byte[]) o).length;
                        break;

                    case Types.OTHER :
                        s += getUTFsize("**");    //new format flag
                        s += 4;
                        s += ByteArray.serialize(o).length;
                    default :
                        s += getUTFsize(o.toString());
                }
            }
        }

        return s;
    }

    /**
     *  Calculate the size of byte array required to store a string in utf8.
     *
     * @param  s - string to convert
     * @return size of the utf8 string
     */
    private static int getUTFsize(String s) {

        // a bit bigger is not really a problem, but never smaller!
        int len = (s == null) ? 0
                              : s.length();
        int l   = 2;

        for (int i = 0; i < len; i++) {
            int c = s.charAt(i);

            if ((c >= 0x0001) && (c <= 0x007F)) {
                l++;
            } else if (c > 0x07FF) {
                l += 3;
            } else {
                l += 2;
            }
        }

        return l;
    }
}
