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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Types;

/**
 *  Class for reading the data for a database row in text table format.
 *
 * @author sqlbob@users (RMP)
 * @version 1.7.0
 */
class TextDatabaseRowInput extends org.hsqldb.DatabaseRowInput
implements org.hsqldb.DatabaseRowInputInterface {

    // text table specific
    private String    fieldSep;
    private String    varSep;
    private String    longvarSep;
    private int       fieldSepLen;
    private int       varSepLen;
    private int       longvarSepLen;
    private boolean   fieldSepEnd;
    private boolean   varSepEnd;
    private boolean   longvarSepEnd;
    private int       textLen;
    protected String  text;
    protected int     line;
    protected int     field;
    protected int     next = 0;
    protected boolean emptyIsNull;

    /**
     * fredt@users - comment - in future may use a custom subclasse of
     * InputStream to read the data.
     *
     * @author sqlbob@users (RMP)
     */
    public TextDatabaseRowInput(String fieldSep, String varSep,
                                String longvarSep,
                                boolean emptyIsNull) throws IOException {

        super(new ByteArrayInputStream(new byte[0]));

        makeSystemId = true;

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

        this.emptyIsNull = emptyIsNull;
        this.fieldSep    = fieldSep;
        this.varSep      = varSep;
        this.longvarSep  = longvarSep;
        fieldSepLen      = fieldSep.length();
        varSepLen        = varSep.length();
        longvarSepLen    = longvarSep.length();
    }

    public void setSource(String text, int pos) {

        this.text = text.substring(0, text.indexOf('\n'));
        textLen   = this.text.length();
        this.pos  = pos;
        size      = text.length();
        nextPos   = pos + size;
        next      = 0;

        line++;

        field = 0;
    }

    protected String getField(String sep, int sepLen,
                              boolean isEnd) throws IOException {

        String s = (emptyIsNull) ? null
                                 : "";

        try {
            int start = next;

            field++;

            if (isEnd) {
                if ((next >= textLen) && (sepLen > 0)) {
                    throw (new Exception("No end sep."));
                } else if (text.endsWith(sep)) {
                    next = textLen - sepLen;
                } else {
                    throw (new Exception("No end sep."));
                }
            } else {
                next = text.indexOf(sep, start);

                if (next == -1) {
                    next = textLen;
                }
            }

            s    = text.substring(start, next);
            next += sepLen;

            if (emptyIsNull && s.equals("")) {
                s = null;
            }
        } catch (Exception e) {
            throw (new IOException("line " + line + ", field " + field + " ("
                                   + e.getMessage() + ")"));
        }

        return (s);
    }

    public String readString() throws IOException {
        return (getField(fieldSep, fieldSepLen, fieldSepEnd));
    }

    private String readVarString() throws IOException {
        return (getField(varSep, varSepLen, varSepEnd));
    }

    private String readLongVarString() throws IOException {
        return (getField(longvarSep, longvarSepLen, longvarSepEnd));
    }

    private byte[] readByteArray(String field)
    throws IOException, SQLException {

        char         from[] = field.toCharArray();
        String       hex;
        StringBuffer to = new StringBuffer();

        for (int i = 0; i < from.length; i++) {
            hex = Integer.toHexString(from[i]);

            if (hex.length() == 1) {
                to.append("0");
            }

            to.append(hex);
        }

        return ByteArray.hexToByteArray(to.toString());
    }

    public int readIntData() throws IOException {

        String s = readString();

        if (s == null) {
            return (0);
        }

        return (Integer.parseInt(s));
    }

    public int readType() throws IOException {
        return 0;
    }

    protected boolean checkNull() {

        // Return null on each column read instead.
        return false;
    }

    protected String readChar(int type) throws IOException {

        switch (type) {

            case Types.CHAR :
                return readString();

            case Types.VARCHAR :
            case Column.VARCHAR_IGNORECASE :
                return readVarString();

            case Types.LONGVARCHAR :
            default :
                return readLongVarString();
        }
    }

    protected Integer readSmallint() throws IOException, SQLException {

        String s = readString();

        if (s == null) {
            return (null);
        }

        return Integer.valueOf(s);
    }

    protected Integer readInteger() throws IOException, SQLException {

        String s = readString();

        if (s == null) {
            return (null);
        }

        return Integer.valueOf(s);
    }

    protected Long readBigint() throws IOException, SQLException {

        String s = readString();

        if (s == null) {
            return (null);
        }

        return Long.valueOf(s);
    }

    protected Double readReal(int type) throws IOException, SQLException {

        String s = readString();

        if (s == null) {
            return (null);
        }

        return Double.valueOf(s);
    }

    protected java.math.BigDecimal readDecimal()
    throws IOException, SQLException {

        String s = readString();

        if (s == null) {
            return (null);
        }

        return new java.math.BigDecimal(s);
    }

    protected java.sql.Time readTime() throws IOException, SQLException {

        String s = readString();

        if (s == null) {
            return (null);
        }

        return java.sql.Time.valueOf(s);
    }

    protected java.sql.Date readDate() throws IOException, SQLException {

        String s = readString();

        if (s == null) {
            return (null);
        }

        return java.sql.Date.valueOf(s);
    }

    protected java.sql.Timestamp readTimestamp()
    throws IOException, SQLException {

        String s = readString();

        if (s == null) {
            return (null);
        }

        return java.sql.Timestamp.valueOf(s);
    }

    protected Boolean readBit() throws IOException, SQLException {

        String s = readString();

        if (s == null) {
            return (null);
        }

        return Boolean.valueOf(s);
    }

    protected Object readOther() throws IOException, SQLException {

        byte[] o;
        String s = readLongVarString();

        if (s == null) {
            return (null);
        }

        o = readByteArray(s);

        return ByteArray.deserialize(o);
    }

    protected byte[] readBinary(int type) throws IOException, SQLException {

        String s;

        switch (type) {

            case Types.BINARY :
                s = readString();

                if (s == null) {
                    return (null);
                }

                return readByteArray(s);

            case Types.VARBINARY :
                s = readVarString();

                if (s == null) {
                    return (null);
                }

                return readByteArray(s);

            case Types.LONGVARBINARY :
            default :
                s = readLongVarString();

                if (s == null) {
                    return (null);
                }

                return readByteArray(s);
        }
    }

    public void setNextPos(int pos) {
        nextPos = pos;
    }

    public void skippedLine() {
        line++;
    }

    public void reset() {

        text    = "";
        textLen = 0;
        pos     = 0;
        size    = 0;
        nextPos = 0;
        next    = 0;
        field   = 0;
        line    = 0;
    }
}
