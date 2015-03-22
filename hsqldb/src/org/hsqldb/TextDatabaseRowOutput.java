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
import java.io.ByteArrayOutputStream;
import java.sql.SQLException;
import java.sql.Types;

/**
 *  Class for writing the data for a database row in text table format.
 *
 * @author sqlbob@users (RMP)
 * @version 1.7.0
 */
class TextDatabaseRowOutput extends org.hsqldb.DatabaseRowOutput {

    protected String              fieldSep;
    protected String              varSep;
    protected String              longvarSep;
    private boolean               fieldSepEnd;
    private boolean               varSepEnd;
    private boolean               longvarSepEnd;
    private String                nextSep = "";
    private boolean               nextSepEnd;
    private ByteArrayOutputStream byteOut = (ByteArrayOutputStream) out;

    public TextDatabaseRowOutput(String fieldSep, String varSep,
                                 String longvarSep) throws IOException {

        super(new ByteArrayOutputStream());

        skipSystemId = true;

        //-- Newline indicates that field should match to end of line.
        if (fieldSep.endsWith("\n")) {
            fieldSepEnd = true;
            fieldSep    = fieldSep.substring(0, fieldSep.length() - 1);
        }

        if (varSep.endsWith("\n")) {
            varSepEnd = true;
            varSep    = varSep.substring(0, varSep.length() - 1);
        }

        if (longvarSep.endsWith("\n")) {
            longvarSepEnd = true;
            longvarSep    = longvarSep.substring(0, longvarSep.length() - 1);
        }

        this.fieldSep   = fieldSep;
        this.varSep     = varSep;
        this.longvarSep = longvarSep;
    }

    public void writePos(int pos) throws IOException {

        //-- Do nothing.
    }

    public void writeSize(int size) throws IOException {

        //-- Do nothing.
    }

    public void writeType(int type) throws IOException {

        //-- Do nothing.
    }

    private void writeNull() throws IOException {

        writeBytes(nextSep);

        nextSep    = fieldSep;
        nextSepEnd = fieldSepEnd;
    }

    private void writeVarNull() throws IOException {

        writeBytes(nextSep);

        nextSep    = varSep;
        nextSepEnd = varSepEnd;
    }

    private void writeLongVarNull() throws IOException {

        writeBytes(nextSep);

        nextSep    = longvarSep;
        nextSepEnd = longvarSepEnd;
    }

    public void writeString(String s) throws IOException {

        writeBytes(nextSep);
        writeBytes(s);

        nextSep    = fieldSep;
        nextSepEnd = fieldSepEnd;
    }

    protected void writeVarString(String s) throws IOException {

        writeBytes(nextSep);
        writeBytes(s);

        nextSep    = varSep;
        nextSepEnd = varSepEnd;
    }

    protected void writeLongVarString(String s) throws IOException {

        writeBytes(nextSep);
        writeBytes(s);

        nextSep    = longvarSep;
        nextSepEnd = longvarSepEnd;
    }

    protected void writeByteArray(byte b[]) throws IOException {

        writeBytes(nextSep);
        write(b, 0, b.length);

        nextSep    = fieldSep;
        nextSepEnd = fieldSepEnd;
    }

    protected void writeVarByteArray(byte b[]) throws IOException {

        writeBytes(nextSep);
        write(b, 0, b.length);

        nextSep    = varSep;
        nextSepEnd = varSepEnd;
    }

    protected void writeLongVarByteArray(byte b[]) throws IOException {

        writeBytes(nextSep);
        write(b, 0, b.length);

        nextSep    = longvarSep;
        nextSepEnd = longvarSepEnd;
    }

    public void writeIntData(int i) throws IOException {

        writeBytes(nextSep + i);

        nextSep    = fieldSep;
        nextSepEnd = fieldSepEnd;
    }

// fredt: write methods for SQL types
// fredt@users - comment - methods used for writing each SQL type
    protected void writeFieldType(int type) throws IOException {

        //-- Do nothing.
    }

    protected void writeNull(int type) throws IOException {

        switch (type) {

            case Types.VARCHAR :
            case Column.VARCHAR_IGNORECASE :
            case Types.VARBINARY :
                writeVarNull();
                break;

            case Types.LONGVARCHAR :
            case Types.LONGVARBINARY :
                writeLongVarNull();
                break;

            default :
                writeNull();
                break;
        }
    }

    protected void writeChar(String s, int t) throws IOException {

        switch (t) {

            case Types.CHAR :
                writeString(s);

                return;

            case Types.VARCHAR :
            case Column.VARCHAR_IGNORECASE :
                writeVarString(s);

                return;

            case Types.LONGVARCHAR :
            default :
                writeLongVarString(s);

                return;
        }
    }

    protected void writeSmallint(Number o) throws IOException, SQLException {
        writeString(o.toString());
    }

    protected void writeInteger(Number o) throws IOException, SQLException {
        writeString(o.toString());
    }

    protected void writeBigint(Number o) throws IOException, SQLException {
        writeString(o.toString());
    }

    protected void writeReal(Double o,
                             int type) throws IOException, SQLException {
        writeString(o.toString());
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

        switch (t) {

            case Types.BINARY :
            case Types.VARBINARY :
            case Types.LONGVARBINARY :
            default :
                writeByteArray(o);
        }
    }

//
    public byte[] toByteArray() throws IOException {

        if (nextSepEnd) {
            writeBytes(nextSep);
        }

        nextSep    = "";
        nextSepEnd = false;

        writeBytes(TextCache.NL);

        byte ret[] = byteOut.toByteArray();

        byteOut.reset();

        return (ret);
    }
}
