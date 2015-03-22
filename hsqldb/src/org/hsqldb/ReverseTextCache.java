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
import java.sql.SQLException;

/**
 *  Cache class declaration <P>
 *
 *  The cache class implements the handling of reversed text table caches.
 *
 * @author sqlbob@users
 * @version  1.7.0
 * @see  Row
 * @see  CacheFree
 */
class ReverseTextCache extends org.hsqldb.TextCache {

    /**
     *  The cache constructor sets up the initial parameters of the cache
     *  object, setting the name used for the file, etc.
     *
     * @param  name              of database file
     * @param  cacheScale        (scale of memory cache)
     * @param  propPrefix        prefix for relevant properties
     * @param  props             Description of the Parameter
     * @exception  SQLException  Description of the Exception
     */
    ReverseTextCache(String name, String propPrefix,
                     HsqlDatabaseProperties props) throws SQLException {
        super(name, propPrefix, props);
    }

    /**
     *  The open method creates or opens a database file.
     *
     * @param  readonly Description of the Parameter
     * @param  ignore1st Description of the Parameter
     * @throws  SQLException
     */
    void open(boolean readonly) throws SQLException {

        try {
            if (!readonly) {
                throw (Trace.error(Trace.FILE_IO_ERROR,
                                   "File '" + sName + "' must be read-only"));
            }

            rFile    = new ReverseDatabaseFile(sName, "r", 4096);
            iFreePos = (int) rFile.length();
        } catch (Exception e) {
            throw Trace.error(Trace.FILE_IO_ERROR,
                              "error " + e + " opening " + sName);
        }

        readOnly = readonly;
    }

    /**
     *  The flush method saves all cached data to the file, saves the free
     *  position and closes the file.
     *
     * @throws  SQLException
     */
    void flush() throws SQLException {
        shutdown();
    }

    void purge() throws SQLException {
        shutdown();
    }

    protected Row makeRow(int pos, Table t) throws SQLException {

        Row r = null;

        try {
            StringBuffer buffer   = new StringBuffer(80);
            boolean      blank    = true;
            boolean      complete = false;
            int          nextPos  = 0;

            try {
                char rowSep = 0;
                int  next   = 0;

                rFile.readSeek(pos);

                //-- The following should work for DOS, MAC, and Unix line
                //-- separators regardless of host OS.
                //-- Skip incomplete last line.
                do {
                    next = rFile.read();

                    if (next == -1) {
                        throw (new Exception("break"));
                    }

                    rowSep = (char) (next & 0xff);
                } while ((rowSep != '\n') && (rowSep != '\r'));

                buffer.append('\n');

                char    c;
                boolean first = true;

                nextPos = (int) rFile.getFilePointer();

                while (true) {
                    next = rFile.read();

                    if (next == -1) {

                        //-- Ignore blanks and first line.
                        complete = !blank &&!ignoreFirst;

                        break;
                    }

                    nextPos++;

                    c = (char) (next & 0xff);

                    if (first && (rowSep == '\n') && (c == '\r')) {
                        c = '\n';
                    } else if ((c == rowSep) || (c == '\n')) {

                        //-- Ignore blank lines
                        if (!blank) {
                            nextPos--;

                            complete = true;

                            break;
                        } else {
                            pos += buffer.length();

                            buffer.setLength(0);
                            buffer.append(c);

                            first = blank = true;

                            in.skippedLine();

                            continue;
                        }
                    }

                    first = false;

                    if ((c != ' ') && (c != '\n')) {
                        blank = false;
                    }

                    buffer.append(c);
                }
            } catch (Exception e) {
                complete = false;
            }

            if (complete) {
                buffer.reverse();
                in.setSource(buffer.toString(), pos);
                in.setNextPos(nextPos);

                r = new Row(t, in);
            }
        } catch (IOException e) {
            e.printStackTrace();

            throw Trace.error(Trace.FILE_IO_ERROR, "reading: " + e);
        }

        return (r);
    }
}
