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

/**
 *
 * @author sqlbob@users (RMP)
 * @version 1.7.0
 */
class QuotedTextDatabaseRowOutput extends org.hsqldb.TextDatabaseRowOutput {

    public QuotedTextDatabaseRowOutput(String fieldSep, String varSep,
                                       String longvarSep) throws IOException {
        super(fieldSep, varSep, longvarSep);
    }

    private String addQuotes(String s, String sep) {

        if ((s.indexOf('\"') != -1)
                || ((sep.length() > 0) && (s.indexOf(sep) != -1))) {
            int          len    = s.length();
            StringBuffer quoted = new StringBuffer(len + 3);    //-- at least 3.

            quoted.append('\"');

            char ch;

            for (int i = 0; i < len; i++) {
                ch = s.charAt(i);

                if (ch == '\"') {
                    quoted.append('\"');
                }

                quoted.append(ch);
            }

            quoted.append('\"');

            s = quoted.toString();
        }

        return (s);
    }

    public void writeString(String s) throws IOException {
        super.writeString(addQuotes(s, fieldSep));
    }

    protected void writeVarString(String s) throws IOException {
        super.writeVarString(addQuotes(s, varSep));
    }

    protected void writeLongVarString(String s) throws IOException {
        super.writeLongVarString(addQuotes(s, longvarSep));
    }

    private String addQuotes(byte b[], String sep) {

        StringBuffer quoted = new StringBuffer(b.length + 2);
        char         ch;

        //-- Always quote (just in case its needed for unprintable chars).
        quoted.append('\"');

        for (int i = 0; i < b.length; i++) {
            ch = (char) (b[i] & 0xff);

            if (ch == '\"') {
                quoted.append('\"');
            }

            quoted.append(ch);
        }

        quoted.append('\"');

        return (quoted.toString());
    }

    protected void writeByteArray(byte b[]) throws IOException {
        super.writeString(addQuotes(b, fieldSep));
    }

    protected void writeVarByteArray(byte b[]) throws IOException {
        super.writeVarString(addQuotes(b, varSep));
    }

    protected void writeLongVarByteArray(byte b[]) throws IOException {
        super.writeLongVarString(addQuotes(b, longvarSep));
    }
}
