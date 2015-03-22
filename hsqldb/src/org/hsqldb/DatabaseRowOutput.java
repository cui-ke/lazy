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

import java.io.OutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Types;

/**
 * Base class for writing the data for a database row in different formats.
 * Defines the methods that are independent of storage format and declares
 * the format-dependent methods that subclasses should define.
 *
 * @author sqlbob@users (RMP)
 * @author fredt@users
 * @version 1.7.0
 */
abstract class DatabaseRowOutput extends DataOutputStream
implements org.hsqldb.DatabaseRowOutputInterface {

    // the last column in a table is a SYSTEM_ID that should not be written to file
    protected boolean skipSystemId = false;

    public DatabaseRowOutput(OutputStream out) throws IOException {
        super(out);
    }

// fredt@users - comment - methods for writing column type, name and data size
    public abstract void writePos(int pos) throws IOException;

    public abstract void writeSize(int size) throws IOException;

    public abstract void writeType(int type) throws IOException;

    public abstract void writeIntData(int i) throws IOException;

    public abstract void writeString(String s) throws IOException;

// fredt@users - comment - methods used for writing each SQL type
    protected abstract void writeFieldType(int type) throws IOException;

    protected abstract void writeNull(int type) throws IOException;

    protected abstract void writeChar(String s, int t) throws IOException;

    protected abstract void writeSmallint(Number o)
    throws IOException, SQLException;

    protected abstract void writeInteger(Number o)
    throws IOException, SQLException;

    protected abstract void writeBigint(Number o)
    throws IOException, SQLException;

    protected abstract void writeReal(Double o,
                                      int type)
                                      throws IOException, SQLException;

    protected abstract void writeDecimal(java.math.BigDecimal o)
    throws IOException, SQLException;

    protected abstract void writeBit(Boolean o)
    throws IOException, SQLException;

    protected abstract void writeDate(java.sql.Date o)
    throws IOException, SQLException;

    protected abstract void writeTime(java.sql.Time o)
    throws IOException, SQLException;

    protected abstract void writeTimestamp(java.sql.Timestamp o)
    throws IOException, SQLException;

    protected abstract void writeOther(Object o)
    throws IOException, SQLException;

    protected abstract void writeBinary(byte[] o,
                                        int t)
                                        throws IOException, SQLException;

    /**
     *  This method is called to write data for a table
     *
     * @param  data
     * @param  t
     * @throws  IOException
     */
    public void writeData(Object data[],
                          Table t) throws IOException, SQLException {

        int[] types = t.getColumnTypes();
        int   l     = types.length;

        if (skipSystemId) {
            l--;
        }

        writeData(l, types, data);
    }

    /**
     *  This method is called to write data for a Result
     *
     * @param  l
     * @param  type
     * @param  data
     * @throws  IOException
     */
    public void writeData(int l, int types[],
                          Object data[]) throws IOException, SQLException {

        for (int i = 0; i < l; i++) {
            Object o = data[i];
            int    t = types[i];

            if (o == null) {
                writeNull(t);

                continue;
            }

            writeFieldType(t);

            switch (t) {

                case Types.CHAR :
                case Types.VARCHAR :
                case Column.VARCHAR_IGNORECASE :
                case Types.LONGVARCHAR :
                    writeChar((String) o, t);
                    break;

                case Types.TINYINT :
                case Types.SMALLINT :
                    writeSmallint((Number) o);
                    break;

                case Types.INTEGER :
                    writeInteger((Number) o);
                    break;

                case Types.BIGINT :
                    writeBigint((Number) o);
                    break;

                case Types.REAL :
                case Types.FLOAT :
                case Types.DOUBLE :
                    writeReal((Double) o, t);
                    break;

                case Types.NUMERIC :
                case Types.DECIMAL :
                    writeDecimal((BigDecimal) o);
                    break;

                case Types.BIT :
                    writeBit((Boolean) o);
                    break;

                case Types.DATE :
                    writeDate((java.sql.Date) o);
                    break;

                case Types.TIME :
                    writeTime((java.sql.Time) o);
                    break;

                case Types.TIMESTAMP :
                    writeTimestamp((java.sql.Timestamp) o);
                    break;

                case Types.OTHER :
                    writeOther(o);
                    break;

                case Types.BINARY :
                case Types.VARBINARY :
                case Types.LONGVARBINARY :
                    writeBinary((byte[]) o, t);
                    break;

                default :
                    throw Trace.error(Trace.FUNCTION_NOT_SUPPORTED, t);
            }
        }
    }
}
