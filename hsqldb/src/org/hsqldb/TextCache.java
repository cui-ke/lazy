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
import java.io.File;
import java.sql.SQLException;

/**
 * Handles operations on a DatabaseFile object and uses signle
 * TextDdatbaseRowInput and TextDatabaseRowOutput objects to read and write
 * rows of data to the file in text table format.
 *
 * @author sqlbob@users (RMP)
 * @version 1.7.0
 */
class TextCache extends org.hsqldb.Cache {

    public static final String NL = System.getProperty("line.separator");
    private String                     fs;
    private String                     vs;
    private String                     lvs;
    private DatabaseRowOutputInterface out;
    protected boolean                  readOnly;
    protected TextDatabaseRowInput     in;
    protected boolean                  ignoreFirst;
    protected String                   ignoredFirst = NL;

    private class TextSource {

        private String source;

        TextSource(String s) {
            source = s;
        }

        String getAttr(String id, String ret) {

            id = ";" + id + "=";

            int len = id.length();
            int start;
            int end;

            if ((start = source.indexOf(id)) != -1) {
                start += len;

                if ((end = source.indexOf(";", start)) == -1) {
                    end = source.length();
                }

                ret = source.substring(start, end);
                source = source.substring(0, start - len)
                         + source.substring(end);
            }

            return (ret);
        }
    }

    /**
     *  TextCache constructor declaration <P>
     *
     *  The cache constructor sets up the initial parameters of the cache
     *  object, setting the name used for the file, etc.
     *
     * @param  name              of database file
     * @param  propPrefix        prefix for relevant properties
     * @param  props             Description of the Parameter
     * @exception  SQLException  Description of the Exception
     */
    TextCache(String name, String propPrefix,
              HsqlDatabaseProperties props) throws SQLException {

        super("", props);

        TextSource textSource = new TextSource(name);

        //-- Get separators:
        fs = translateSep(textSource.getAttr("fs", null));
        vs = textSource.getAttr("vs", fs);

        if (vs != fs) {
            vs = translateSep(vs);
        }

        String lvs = textSource.getAttr("lvs", fs);

        if (lvs != fs) {
            lvs = translateSep(lvs);
        }

        if (fs == null) {
            fs = translateSep(props.getProperty(propPrefix + "fs"), true);

            if (fs == null) {
                fs = ",";
            }
        }

        if (vs == null) {
            vs = props.getProperty(propPrefix + "vs", fs);

            if (vs != fs) {
                vs = translateSep(vs, true);
            }
        }

        if (lvs == null) {
            lvs = props.getProperty(propPrefix + "lvs", fs);

            if (lvs != fs) {
                lvs = translateSep(lvs, true);
            }
        }

        //-- Get boolean settings:
        String skipFirst = textSource.getAttr("ignore_first", null);

        if (skipFirst == null) {
            skipFirst = props.getProperty(propPrefix + "ignore_first",
                                          "false");
        }

        ignoreFirst = skipFirst.equals("true");

        String quoted = textSource.getAttr("quoted", null);

        if (quoted == null) {
            quoted = props.getProperty(propPrefix + "quoted", "true");
        }

        String emptyIsNull = textSource.getAttr("empty_is_null", null);

        if (emptyIsNull == null) {
            emptyIsNull = props.getProperty(propPrefix + "empty_is_null",
                                            "true");
        }

        // Get file name
        sName = textSource.source;

        if (sName.endsWith(";")) {
            sName = sName.substring(0, sName.length() - 1);
        }

        try {
            if (quoted.equals("true")) {
                in = new QuotedTextDatabaseRowInput(
                    fs, vs, lvs, emptyIsNull.equals("true"));
                out = new QuotedTextDatabaseRowOutput(fs, vs, lvs);
            } else {
                in = new TextDatabaseRowInput(fs, vs, lvs,
                                              emptyIsNull.equals("true"));
                out = new TextDatabaseRowOutput(fs, vs, lvs);
            }
        } catch (IOException e) {
            throw (Trace.error(Trace.FILE_IO_ERROR,
                               "invalid separator(s):" + e));
        }
    }

    private String translateSep(String sep) {
        return (translateSep(sep, false));
    }

    private String translateSep(String sep, boolean isProperty) {

        if (sep == null) {
            return (null);
        }

        int next = 0;

        if ((next = sep.indexOf('\\')) != -1) {
            int          start      = 0;
            char         sepArray[] = sep.toCharArray();
            char         ch         = 0;
            int          len        = sep.length();
            StringBuffer realSep    = new StringBuffer(len);

            do {
                realSep.append(sepArray, start, next - start);

                start = ++next;

                if (next >= len) {
                    realSep.append('\\');

                    break;
                }

                if (!isProperty) {
                    ch = sepArray[next];
                }

                if (ch == 'n') {
                    realSep.append('\n');

                    start++;
                } else if (ch == 'r') {
                    realSep.append('\r');

                    start++;
                } else if (ch == 't') {
                    realSep.append('\t');

                    start++;
                } else if (ch == '\\') {
                    realSep.append('\\');

                    start++;
                } else if (ch == 'u') {
                    start++;

                    realSep.append(
                        (char) Integer.parseInt(
                            sep.substring(start, start + 4), 16));

                    start += 4;
                } else if (sep.startsWith("semi", next)) {
                    realSep.append(';');

                    start += 4;
                } else if (sep.startsWith("space", next)) {
                    realSep.append(' ');

                    start += 5;
                } else if (sep.startsWith("quote", next)) {
                    realSep.append('\"');

                    start += 5;
                } else if (sep.startsWith("apos", next)) {
                    realSep.append('\'');

                    start += 4;
                } else {
                    realSep.append('\\');
                    realSep.append(sepArray[next]);

                    start++;
                }
            } while ((next = sep.indexOf('\\', start)) != -1);

            realSep.append(sepArray, start, len - start);

            sep = realSep.toString();
        }

        return (sep);
    }

    /**
     *  open method declaration <P>
     *
     *  The open method creates or opens a database file.
     *
     * @param  readonly       Description of the Parameter
     * @param  ignore1st      Description of the Parameter
     * @throws  SQLException
     */
    void open(boolean readonly) throws SQLException {

        try {
            rFile    = new DatabaseFile(sName, (readonly) ? "r"
                                                          : "rw", 4096);
            iFreePos = (int) rFile.length();

            if ((iFreePos == 0) && ignoreFirst) {
                rFile.write(ignoredFirst.getBytes());

                iFreePos = ignoredFirst.length();
            }
        } catch (Exception e) {
            throw Trace.error(Trace.FILE_IO_ERROR,
                              "error " + e + " opening " + sName);
        }

        readOnly = readonly;
    }

    void reopen() throws SQLException {
        open(readOnly);
        in.reset();
    }

    /**
     *  flush method declaration <P>
     *
     *  The flush method saves all cached data to the file, saves the free
     *  position and closes the file.
     *
     * @throws  SQLException
     */
    void flush() throws SQLException {

        if (rFile == null) {
            return;
        }

        try {
            rFile.seek(0);
            saveAll();

            boolean empty = (rFile.length() <= NL.length());

            rFile.close();

            rFile = null;

            if (empty &&!readOnly) {
                new File(sName).delete();
            }
        } catch (Exception e) {
            throw Trace.error(Trace.FILE_IO_ERROR,
                              "error " + e + " closing " + sName);
        }
    }

    void purge() throws SQLException {

        if (rFile == null) {
            return;
        }

        try {
            if (readOnly) {
                flush();
            } else {
                rFile.close();

                rFile = null;

                new File(sName).delete();
            }
        } catch (Exception e) {
            throw Trace.error(Trace.FILE_IO_ERROR,
                              "error " + e + " purging " + sName);
        }
    }

    void free(Row r, int pos, int length) throws SQLException {

        //-- Change to blank line:
        StringBuffer blank = new StringBuffer(length);

        length -= NL.length();

        for (int i = 0; i < length; i++) {
            blank.append(' ');
        }

        blank.append(NL);

        try {
            rFile.seek(pos);
            rFile.writeBytes(blank.toString());
        } catch (IOException e) {
            throw (Trace.error(Trace.FILE_IO_ERROR, e + ""));
        }

        remove(r);
    }

    protected int getStorageSize(Row r) throws SQLException {

        int size;

        try {
            out.writeData(r.getData(), r.getTable());

            size = out.toByteArray().length;
        } catch (IOException e) {
            throw (Trace.error(Trace.FILE_IO_ERROR, e + ""));
        }

        return (size);
    }

    protected Row makeRow(int pos, Table t) throws SQLException {

        Row r = null;

        try {
            StringBuffer buffer   = new StringBuffer(80);
            boolean      blank    = true;
            boolean      complete = false;

            try {
                char c;
                int  next;

                rFile.readSeek(pos);

                //-- The following should work for DOS, MAC, and Unix line
                //-- separators regardless of host OS.
                while (true) {
                    next = rFile.read();

                    if (next == -1) {
                        break;
                    }

                    c = (char) (next & 0xff);

                    //-- Ensure line is complete.
                    if (c == '\n') {
                        buffer.append('\n');

                        //-- Store first line.
                        if (ignoreFirst && pos == 0) {
                            ignoredFirst = buffer.toString();
                            blank        = true;
                        }

                        //-- Ignore blanks
                        if (!blank) {
                            complete = true;

                            break;
                        } else {
                            pos += buffer.length();

                            buffer.setLength(0);

                            blank = true;

                            in.skippedLine();

                            continue;
                        }
                    }

                    if (c == '\r') {

                        //-- Check for newline
                        try {
                            next = rFile.read();

                            if (next == -1) {
                                break;
                            }

                            c = (char) (next & 0xff);

                            if (c == '\n') {
                                buffer.append('\n');
                            }
                        } catch (Exception e2) {
                            ;
                        }

                        buffer.append('\n');

                        //-- Store first line.
                        if (ignoreFirst && pos == 0) {
                            ignoredFirst = buffer.toString();
                            blank        = true;
                        }

                        //-- Ignore blanks.
                        if (!blank) {
                            complete = true;

                            break;
                        } else {
                            pos += buffer.length();

                            buffer.setLength(0);

                            blank = true;

                            in.skippedLine();

                            continue;
                        }
                    }

                    if (c != ' ') {
                        blank = false;
                    }

                    buffer.append(c);
                }
            } catch (Exception e) {
                complete = false;
            }

            if (complete) {
                in.setSource(buffer.toString(), pos);

                r = new Row(t, in);
            }
        } catch (Exception e) {
            e.printStackTrace();

            throw Trace.error(Trace.FILE_IO_ERROR, "reading: " + e);
        }

        return (r);
    }

    protected void saveRow(Row r) throws IOException, SQLException {

        rFile.seek(r.iPos);
        r.write(out);
        rFile.write(out.toByteArray());
    }
}
