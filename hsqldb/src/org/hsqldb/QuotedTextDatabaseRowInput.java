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
class QuotedTextDatabaseRowInput extends org.hsqldb.TextDatabaseRowInput {

    private static final int NORMAL_FIELD   = 0;
    private static final int NEED_END_QUOTE = 1;
    private static final int FOUND_QUOTE    = 2;
    char                     qtext[];

    public QuotedTextDatabaseRowInput(String fieldSep, String varSep,
                                      String longvarSep,
                                      boolean emptyIsNull)
                                      throws IOException {
        super(fieldSep, varSep, longvarSep, emptyIsNull);
    }

    public void setSource(String text, int pos) {

        super.setSource(text, pos);

        qtext = this.text.toCharArray();
    }

    protected String getField(String sep, int sepLen,
                              boolean isEnd) throws IOException {

        String s = (emptyIsNull) ? null
                                 : "";

        if (next >= qtext.length) {
            return (super.getField(sep, sepLen, isEnd));
        }

        try {
            field++;

            StringBuffer ret   = new StringBuffer();
            boolean      done  = false;
            int          state = NORMAL_FIELD;
            int          end   = -1;

            if (!isEnd) {
                end = text.indexOf(sep, next);
            }

            for (; next < qtext.length; next++) {
                switch (state) {

                    case NORMAL_FIELD :
                    default :
                        if (next == end) {
                            next += sepLen;
                            done = true;
                        } else if (qtext[next] == '\"') {

                            //-- Beginning of field
                            state = NEED_END_QUOTE;
                        } else {
                            ret.append(qtext[next]);
                        }
                        break;

                    case NEED_END_QUOTE :
                        if (qtext[next] == '\"') {
                            state = FOUND_QUOTE;
                        } else {
                            ret.append(qtext[next]);
                        }
                        break;

                    case FOUND_QUOTE :
                        if (qtext[next] == '\"') {

                            //-- Escaped quote
                            ret.append(qtext[next]);

                            state = NEED_END_QUOTE;
                        } else {

                            //-- End of field.
                            if (((next + 1) != qtext.length)
                                    && (text.indexOf(sep, next) != next)) {
                                throw (new Exception("No sep."));
                            }

                            next  += sepLen - 1;
                            state = NORMAL_FIELD;

                            if (!isEnd) {
                                next++;

                                done = true;
                            }
                        }
                        break;
                }

                if (done) {
                    break;
                }
            }

            s = ret.toString();

            if (emptyIsNull && s.equals("")) {
                s = null;
            }
        } catch (Exception e) {
            throw (new IOException("line " + line + ", field " + field + " ("
                                   + e.getMessage() + ")"));
        }

        return (s);
    }
}
