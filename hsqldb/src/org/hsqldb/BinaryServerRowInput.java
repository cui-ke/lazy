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

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.SQLException;

/**
 *  Provides methods for reading the data for a row from a
 *  byte array. The format of data is that used for storage of cached
 *  tables by v.1.6.x databases, apart from strings.
 *
 * @version  1.7.0
 */
class BinaryServerRowInput extends org.hsqldb.DatabaseRowInput
implements org.hsqldb.DatabaseRowInputInterface {

    public BinaryServerRowInput(byte bin[]) throws IOException {
        super(bin, NO_POS);
    }

    public BinaryServerRowInput(byte bin[], int pos) throws IOException {
        super(bin, pos);
    }

    byte[] readByteArray() throws IOException {

        byte[] b = new byte[readInt()];

        readFully(b);

        return b;
    }

    public int readType() throws IOException {
        return readShort();
    }

    //public int getPos() throws IOException {
    //    return pos;
    //}
    public int readIntData() throws IOException {
        return readInt();
    }

    public String readString() throws IOException {

        byte[] bytes = new byte[readInt()];

        readFully(bytes);

        return (new String(bytes, "utf-8"));
    }

    protected boolean checkNull() throws IOException {

        int b = readByte();

        return b == 0 ? true
                      : false;
    }

    protected String readChar(int type) throws IOException {
        return readString();
    }

    protected Integer readSmallint() throws IOException, SQLException {
        return new Integer(readShort());
    }

    protected Integer readInteger() throws IOException, SQLException {
        return new Integer(readInt());
    }

    protected Long readBigint() throws IOException, SQLException {
        return new Long(readLong());
    }

    protected Double readReal(int type) throws IOException, SQLException {
        return new Double(Double.longBitsToDouble(readLong()));
    }

    protected BigDecimal readDecimal() throws IOException, SQLException {

        byte[]     bytes  = readByteArray();
        int        scale  = readInt();
        BigInteger bigint = new BigInteger(bytes);

        return new BigDecimal(bigint, scale);
    }

    protected Boolean readBit() throws IOException, SQLException {
        return new Boolean(readBoolean());
    }

    protected java.sql.Time readTime() throws IOException, SQLException {
        return new java.sql.Time(readLong());
    }

    protected java.sql.Date readDate() throws IOException, SQLException {
        return new java.sql.Date(readLong());
    }

    protected java.sql.Timestamp readTimestamp()
    throws IOException, SQLException {
        return org.hsqldb.lib.HsqlDateTime.timestampValue(readLong(),
                readInt());
    }

    protected Object readOther() throws IOException, SQLException {

// fredt@users 20020328 -  patch 482109 by fredt - OBJECT handling
// objects are / were stored as serialized byte[]
// now they are deserialized before retrieval
        byte[] o = readByteArray();

        return ByteArray.deserialize(o);
    }

    protected byte[] readBinary(int type) throws IOException, SQLException {
        return readByteArray();
    }
}
