/* Copyright (c) 2001-2002, The HSQL Development Group
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
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
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Types;
import java.sql.SQLException;

/**
 *  Provides methods for writing the data for a row to a
 *  byte array. The new format of data consists of mainly binary values
 *  and is not compatible with v.1.6.x databases.
 *
 * @version  1.7.0
 */
class BinaryServerRowOutput extends org.hsqldb.DatabaseRowOutput {

    private ByteArrayOutputStream byteOut = (ByteArrayOutputStream) out;

    /**
     *  Constructor used for a Result row
     *
     * @exception  IOException when an IO error is encountered
     */
    public BinaryServerRowOutput() throws IOException {
        super(new ByteArrayOutputStream());
    }

    /**
     *  Constructor used for persistent storage of a Table row
     *
     * @param  size no of bytes of storage used
     * @exception  IOException when an IO error is encountered
     */
    public BinaryServerRowOutput(int size) throws IOException {
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
        writeShort(type);
    }

    public void writeString(String s) throws IOException {

        byte[] bytes = s.getBytes("utf-8");

        writeInt(bytes.length);
        write(bytes);
    }

    public byte[] toByteArray() throws IOException {

        byte ret[] = byteOut.toByteArray();

        byteOut.reset();

        return (ret);
    }

// fredt@users - comment - methods used for writing each SQL type
    protected void writeFieldType(int type) throws IOException {
        writeByte(1);
    }

    protected void writeNull(int type) throws IOException {
        writeByte(0);
    }

    protected void writeChar(String s, int t) throws IOException {
        writeString(s);
    }

    protected void writeSmallint(Number o) throws IOException, SQLException {
        writeShort(o.intValue());
    }

    protected void writeInteger(Number o) throws IOException, SQLException {
        writeInt(o.intValue());
    }

    protected void writeBigint(Number o) throws IOException, SQLException {
        writeLong(o.longValue());
    }

    protected void writeReal(Double o,
                             int type) throws IOException, SQLException {
        writeLong(Double.doubleToLongBits((o.doubleValue())));
    }

    protected void writeDecimal(BigDecimal o)
    throws IOException, SQLException {

        int scale = o.scale();

//#ifdef JAVA2
        BigInteger bigint = o.unscaledValue();

//#else
/*
        BigInteger bigint = o.movePointRight(scale).toBigInteger();

*/

//#endif JAVA2
        byte[] bytearr = bigint.toByteArray();

        writeByteArray(bytearr);
        writeInt(scale);
    }

    protected void writeBit(Boolean o) throws IOException, SQLException {
        writeBoolean(o.booleanValue());
    }

    protected void writeDate(java.sql.Date o)
    throws IOException, SQLException {
        writeLong(o.getTime());
    }

    protected void writeTime(java.sql.Time o)
    throws IOException, SQLException {
        writeLong(o.getTime());
    }

    protected void writeTimestamp(java.sql.Timestamp o)
    throws IOException, SQLException {
        writeLong(o.getTime());
        writeInt(o.getNanos());
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

            s += 1;    // type

            if (o != null) {
                switch (type[i]) {

                    case Types.CHAR :
                    case Types.VARCHAR :
                    case Column.VARCHAR_IGNORECASE :
                    case Types.LONGVARCHAR :
                        s += getUTFSize((String) o);
                        break;

                    case Types.TINYINT :
                    case Types.SMALLINT :
                        s += 2;
                        break;

                    case Types.INTEGER :
                        s += 4;
                        break;

                    case Types.BIGINT :
                    case Types.REAL :
                    case Types.FLOAT :
                    case Types.DOUBLE :
                        s += 8;
                        break;

                    case Types.NUMERIC :
                    case Types.DECIMAL :
                        s += 8;

//#ifdef JAVA2
                        s += ((BigDecimal) o).unscaledValue().toByteArray()
                            .length;

//#else
/*
                        BigDecimal bigdecimal = (BigDecimal) o;
                        int        scale      = bigdecimal.scale();
                        BigInteger bigint     =
                            bigdecimal.movePointRight(scale).toBigInteger();

                        s += bigint.toByteArray().length;

*/

//#endif JAVA2
                        break;

                    case Types.BIT :
                        s += 1;
                        break;

                    case Types.DATE :
                    case Types.TIME :
                        s += 8;
                        break;

                    case Types.TIMESTAMP :
                        s += 12;
                        break;

                    case Types.BINARY :
                    case Types.VARBINARY :
                    case Types.LONGVARBINARY :
                        s += 4;
                        s += ((byte[]) o).length;
                        break;

                    case Types.OTHER :
                        s += 4;
                        s += ByteArray.serialize(o).length;
                        break;

                    default :
                        throw Trace.error(Trace.FUNCTION_NOT_SUPPORTED,
                                          type[i]);
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
    private static int getUTFSize(String s) {

        // a bit bigger is not really a problem, but never smaller!
        int len = (s == null) ? 0
                              : s.length();
        int l   = 4;    // length

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
