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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.sql.SQLException;

/**
 * Manages a .properties file for a database.
 *
 * @version 1.7.0
 */
class HsqlDatabaseProperties extends org.hsqldb.HsqlProperties {

    private FileInputStream propsFileStream;    // kept open until closed

    private HsqlDatabaseProperties() {
        super();
    }

    public HsqlDatabaseProperties(String name) {

        super(name);

        // returns the underlying column name with getColumnName(int c)
        // false value returns getColumnLabel(int c)
        setProperty("jdbc.get_column_name", true);

        // month 1-12 instead of 0-11
        setProperty("sql.month", true);

        // char trimming and padding to size and varchar trimming to size
        setProperty("sql.enforce_size", false);

        // char and varchar sorting in charset of the current jvm Locale
        setProperty("sql.compare_in_locale", false);

        // if true, requires a pre-existing unique index for foreign key
        // referenced column and returns an error if index does not exist
        // 1.61 creates a non-unique index if no index exists
        setProperty("sql.strict_fk", false);

        // has no effect if sql_strict_fk is true, otherwise if true,
        // creates a unique index for foreign keys instead of non-unique
        setProperty("sql.strong_fk", true);

        // the earliest version that can open this database
        // this is set to 1.7.0 when the db is written to
        setProperty("hsqldb.compatible_version", "1.6.0");

        // the version that created this database
        // once created, this won't change if db is used with a future version
        setProperty("hsqldb.original_version", "1.7.1");

        // data format of the cache file
        // this is set to 1.7.0 when new cache is created
        setProperty("hsqldb.cache_version", "1.6.0");

        // garbage collect per Record or Cache Row objects created
        // the default, "0" means no garbage collection is forced by
        // hsqldb (the Java Runtime will do it's own garbage collection
        // in any case). Based on tests by meissnersd@users
        /*
            Setting this value can be useful when HSQLDB is used as an
            in-process part of an application. The minimum practical
            amount is probably "10000" and the maximum "1000000"

            In some versions of Java, such as 1.3.1_02 on windows,
            when the application runs out of memory it runs the gc AND
            requests more memory from the OS. Setting this property
            forces the DB to live inside its memory budget but the
            maximum amount of memory can still be set with the
            java -Xmx argument to provide the memory needed by other
            parts of the app to do graphics and networking.

            Of course there is a speed penalty for setting the value
            too low and doing garbage collection too often.
        */

        //setProperty("hsqldb.gc_interval", "0");
        // number of rows from CACHED tables kept constantly in memory
        // the number of rows in 2 to the power of cache_scale value.
        // reduce the default 15 (32K rows) if memory is limited and rows
        // are large.
        // values between 8-16 are allowed
        setProperty("hsqldb.cache_scale", "15");

        // maximum size of .script file in megabytes
        setProperty("hsqldb.log_size", "200");
        setProperty("readonly", false);
        setProperty("modified", "no");

        // the property "version" is also set to the current version
    }

    public void close() throws SQLException {

        try {
            if (propsFileStream != null) {
                if (Trace.TRACE) {
                    Trace.trace();
                }

                propsFileStream.close();

                propsFileStream = null;
            }
        } catch (Exception e) {
            throw Trace.error(Trace.FILE_IO_ERROR,
                              fileName + ".properties " + e);
        }
    }

    public void load() throws SQLException {

        close();

        if (Trace.TRACE) {
            Trace.trace();
        }

        try {
            File f = new File(fileName + ".properties");

            // the file is closed only when the database is closed
            propsFileStream = new FileInputStream(f);

            stringProps.load(propsFileStream);
        } catch (Exception e) {
            throw Trace.error(Trace.FILE_IO_ERROR,
                              fileName + ".properties " + e);
        }
    }

    /**
     *  Method declaration
     *
     * @throws  SQLException
     */
    public void save() throws SQLException {

        close();

        if (Trace.TRACE) {
            Trace.trace();
        }

        try {
            super.save();

            // after saving, open the file again
            load();
        } catch (Exception e) {
            throw Trace.error(Trace.FILE_IO_ERROR,
                              fileName + ".properties " + e);
        }
    }

    /**
     *  check by trying to delete the properties file this will not work if
     *  some application has the file open this is why the properties file
     *  is kept open when running ;-) todo: check if this works in all
     *  operating systems
     *
     * @return true if file is open
     * @exception  java.sql.SQLException
     */
    protected boolean isFileOpen() throws java.sql.SQLException {

        close();

        if (Trace.TRACE) {
            Trace.trace();
        }

        if ((new File(fileName + ".properties")).delete() == false) {
            return true;
        }

        // the file was deleted, so recreate it now
        save();

        return false;
    }
}
