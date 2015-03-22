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

import java.io.IOException;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.LineNumberReader;
import java.io.Writer;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

//import java.util.zip.
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

// fredt@users 20020215 - patch 1.7.0 by fredt
// to move operations on the database.properties files to new
// class HsqlDatabaseProperties
// fredt@users 20020220 - patch 488200 by xclayl@users - throw exception
// throw addded to all methods relying on file io
// fredt@users 20020221 - patch 513005 by sqlbob@users (RMP)
// fredt@users 20020405 - patch 1.7.0 by fredt - no change in db location
// because important information about the database is now stored in the
// *.properties file, all database files should be in the same folder as the
// *.properties file
// tony_lai@users 20020820 - export hsqldb.log_size to .properties file
// tony_lai@users 20020820 - modification to shutdown compact process to save memory usage
// fredt@users 20020910 - patch 1.7.1 by Nitin Chauhan - code improvements

/**
 *  This class is responsible for most file handling. A HSQL database
 *  consists of a .properties file, a .script file (contains a SQL script),
 *  a .data file (contains data of cached tables) and a .backup file
 *  (contains the compressed .data file) <P>
 *
 *  This is an example of the .properties file. The version and the modified
 *  properties are automatically created by the database and should not be
 *  changed manually: <pre>
 * modified=no
 * version=1.43
 * </pre> The following lines are optional, this means they are not created
 *  automatically by the database, but they are interpreted if they exist in
 *  the .script file. They have to be created manually if required. If they
 *  don't exist the default is used. This are the defaults of the database
 *  'test': <pre>
 * readonly=false
 * </pre>
 *
 * @version 1.7.0
 */
class Log implements Runnable {

    // block size for copying data
    private static final int       COPY_BLOCK_SIZE = 1 << 16;
    private HsqlDatabaseProperties pProperties;
    private String                 sName;
    private Database               dDatabase;
    private Session                sysSession;
    private Writer                 wScript;
    private String                 sFileScript;
    private String                 sFileCache;
    private String                 sFileBackup;
    private boolean                bRestoring;
    private boolean                bReadOnly;
    private int                    iLogSize;
    private int                    iLogCount;
    private Thread                 tRunner;
    private volatile boolean       bNeedFlush;
    private volatile boolean       bWriteDelay;
    private int                    mLastId;
    private Cache                  cCache;

// boucherb@users - comment - FIXME
//  boolean                  stopped;

    /**
     *  Constructor declaration
     *
     * @param  db
     * @param  system
     * @param  name
     * @exception  SQLException  Description of the Exception
     */
    Log(Database db, Session system, String name) throws SQLException {

        dDatabase   = db;
        sysSession  = system;
        sName       = name;
        pProperties = db.getProperties();
        tRunner     = new Thread(this);

        // boucherb@users - FIXME:
        // standard VM behaviour is to shut down only after all
        // non-daemon threads exit.  Therefor, tRunner shuld be
        // daemon.  Consider the case of:
        /*
         public void main(String[] args) {
         ...
         try {
         // fails due to bad user/password...must then connect with valid combo
         // again to *really* shutdown database, or explicitly call System.exit(...)
         DriverManager.getConnection("jdbc:hsqldb:filespec,"user","password");
         ...
         } catch (...) {
         }
         ...
         }
         */

        // the VM will not exit, since tRunner is still running and
        // no shutdown is issued to close the database.
        //
        //  - setDaemon(false) may require flush in finalization
        // CB
        // tRunner.setDaemon(false);
        tRunner.start();
    }

    /**
     *  Method declaration
     */
    public void run() {

        // boucherb@users - FIXME
        // while (!stopped) {
        while (tRunner != null) {
            try {
                tRunner.sleep(1000);

                if (bNeedFlush) {
                    wScript.flush();

                    bNeedFlush = false;
                }

                // todo: try to do Cache.cleanUp() here, too
            } catch (Exception e) {

                // ignore exceptions; may be InterruptedException or IOException
            }
        }
    }

    /**
     *  Method declaration
     *
     * @param  delay
     */
    void setWriteDelay(boolean delay) {
        bWriteDelay = delay;
    }

    /**
     * When opening a database, the hsqldb.compatible_version property is
     * used to determine if this version of the engine is equal to or greater
     * than the earliest version of the engine capable of opening that
     * database.<p>
     *
     * @return
     * @throws  SQLException
     */
    boolean open() throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        if (!pProperties.checkFileExists()) {
            create();
            open();

            // this is a new database
            return true;
        }

        // todo: some parts are not necessary for ready-only access
        pProperties.load();

        sFileScript = sName + ".script";
        sFileCache  = sName + ".data";
        sFileBackup = sName + ".backup";

        // tony_lai@users 20020820
        // Allows the user to modify log size from the properties file.
        iLogSize = pProperties.getIntegerProperty("hsqldb.log_size",
                iLogSize);

        String version = pProperties.getProperty("hsqldb.compatible_version");

// fredt@users 20020428 - patch 1.7.0 by fredt
        int check = version.substring(0, 5).compareTo(jdbcDriver.VERSION);

        Trace.check(check <= 0, Trace.WRONG_DATABASE_FILE_VERSION);

        // save the current version
        pProperties.setProperty("hsqldb.version", jdbcDriver.VERSION);

        if (pProperties.isPropertyTrue("readonly")) {
            bReadOnly = true;

            dDatabase.setReadOnly();

            if (cCache != null) {
                cCache.open(true);
            }

            reopenAllTextCaches();
            runScript();

            return false;
        }

        boolean needbackup = false;
        String  state      = pProperties.getProperty("modified");

        if (state.equals("yes-new-files")) {
            renameNewToCurrent(sFileScript);
            renameNewToCurrent(sFileBackup);
        } else if (state.equals("yes")) {
            if (isAlreadyOpen()) {
                throw Trace.error(Trace.DATABASE_ALREADY_IN_USE);
            }

            // recovering after a crash (or forgot to close correctly)
            restoreBackup();

            needbackup = true;
        }

        pProperties.setProperty("modified", "yes");
        pProperties.save();

        if (cCache != null) {
            cCache.open(false);
        }

        reopenAllTextCaches();
        runScript();

        if (needbackup) {
            close(false);
            pProperties.setProperty("modified", "yes");
            pProperties.save();

            if (cCache != null) {
                cCache.open(false);
            }

            reopenAllTextCaches();
        }

        openScript();

        // this is an existing database
        return false;
    }

    Cache getCache() throws SQLException {

        if (cCache == null) {
            cCache = new Cache(sFileCache, pProperties);

            cCache.open(bReadOnly);
        }

        return (cCache);
    }

    /**
     *  Method declaration
     */
    void /* synchronized */ stop() {

        //boucherb@users - FIXME:
        /*
         if (!stopped)
         stopped = true;
         tRunner.interrupt();
         tRunner = null;
         }
         */
        tRunner = null;
    }

    /**
     *  Method declaration
     *
     * @param  compact
     * @throws  SQLException
     */
    void close(boolean compact) throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        if (bReadOnly) {
            return;
        }

        // no more scripting
        closeScript();

        // create '.script.new' (for this the cache may be still required)
        writeScript(compact);

        // flush the cache (important: after writing the script)
        if (cCache != null) {
            cCache.flush();
        }

        closeAllTextCaches(compact);

        // create '.backup.new' using the '.data'
        backup();

        // we have the new files
        pProperties.setProperty("modified", "yes-new-files");
        pProperties.save();

        // old files can be removed and new files renamed
        renameNewToCurrent(sFileScript);
        renameNewToCurrent(sFileBackup);

        // now its done completely
        pProperties.setProperty("modified", "no");
        pProperties.setProperty("version", jdbcDriver.VERSION);
        pProperties.setProperty("hsqldb.compatible_version", "1.7.0");
        pProperties.save();
        pProperties.close();

        if (compact) {

            // stop the runner thread of this process (just for security)
            stop();

            // delete the .data so then a new file is created
            (new File(sFileCache)).delete();
            (new File(sFileBackup)).delete();

            // tony_lai@users 20020820
            // The database re-open and close has been moved to
            // Database#close(int closemode) for saving memory usage.
        }
    }

    /**
     *  Method declaration
     *
     * @throws  SQLException
     */
    void checkpoint() throws SQLException {

        close(false);
        pProperties.setProperty("modified", "yes");
        pProperties.save();

        if (cCache != null) {
            cCache.open(false);
        }

        reopenAllTextCaches();
        openScript();
    }

    /**
     *  Method declaration
     *
     * @param  mb
     */
    void setLogSize(int mb) {

        iLogSize = mb;

        pProperties.setProperty("hsqldb.log_size", iLogSize);
    }

    /**
     *  Method declaration
     *
     * @param  c
     * @param  s
     * @throws  SQLException
     */
    void write(Session c, String s) throws SQLException {

        if (bRestoring || s == null || s.length() == 0) {
            return;
        }

        if (!bReadOnly) {
            int id = 0;

            if (c != null) {
                id = c.getId();
            }

            if (id != mLastId) {
                s       = "/*C" + id + "*/" + s;
                mLastId = id;
            }

            try {
                writeLine(wScript, s);

                if (bWriteDelay) {
                    bNeedFlush = true;
                } else {
                    wScript.flush();
                }
            } catch (IOException e) {
                throw Trace.error(Trace.FILE_IO_ERROR, sFileScript);
            }

            // fredt@users - todo - eliminate new File() calls
            if (iLogSize > 0 && iLogCount++ > 100) {
                iLogCount = 0;

                if ((new File(sFileScript)).length()
                        > iLogSize * 1024 * 1024) {
                    checkpoint();
                }
            }
        }
    }

    /**
     *  Method declaration
     *
     * @throws  SQLException
     */
    void shutdown() throws SQLException {

        tRunner = null;

        if (cCache != null) {
            cCache.shutdown();

            cCache = null;
        }

        shutdownAllTextCaches();
        closeScript();
        pProperties.close();
    }

    /**
     *  Method declaration
     *
     * @param  db
     * @param  file
     * @param  full
     * @param  session
     * @throws  SQLException
     */
    static void scriptToFile(Database db, String file, boolean full,
                             Session session) throws SQLException {

        if ((new File(file)).exists()) {

            // there must be no such file; overwriting not allowed for security
            throw Trace.error(Trace.FILE_IO_ERROR, file);
        }

        try {
            long time = 0;

            if (Trace.TRACE) {
                time = System.currentTimeMillis();
            }

            // only ddl commands; needs not so much memory
            Result r;

            if (full) {

                // no drop, no insert, and no positions for cached tables
                r = db.getScript(false, false, false, session);
            } else {

                // no drop, no insert, but positions for cached tables
                r = db.getScript(false, false, true, session);
            }

            Record     n = r.rRoot;
            FileWriter w = new FileWriter(file);

            while (n != null) {
                writeLine(w, (String) n.data[0]);

                n = n.next;
            }

            // inserts are done separetely to save memory
            Vector tables = db.getTables();

            for (int i = 0; i < tables.size(); i++) {
                Table t = (Table) tables.elementAt(i);

// cached tables have the index roots set in the ddl script
                if ((full ||!t.isCached()) &&!t.isTemp() &&!t.isView()
                        && (!t.isText() ||!t.isDataReadOnly())) {
                    Index primary = t.getPrimaryIndex();
                    Node  x       = primary.first();

                    while (x != null) {
                        writeLine(w, t.getInsertStatement(x.getData()));

                        x = primary.next(x);
                    }
                }

// fredt@users 20020221 - patch 513005 by sqlbob@users (RMP)
                if (t.isDataReadOnly() &&!t.isTemp() &&!t.isText()) {
                    StringBuffer a = new StringBuffer("SET TABLE ");

                    a.append(t.getName().statementName);
                    a.append(" READONLY TRUE");
                    writeLine(w, a.toString());
                }
            }

            w.close();

            if (Trace.TRACE) {
                Trace.trace(time - System.currentTimeMillis());
            }
        } catch (IOException e) {
            throw Trace.error(Trace.FILE_IO_ERROR, file + " " + e);
        }
    }

    /**
     *  Method declaration
     *
     * @param  file
     */
    private void renameNewToCurrent(String file) {

        // even if it crashes here, recovering is no problem
        File newFile = new File(file + ".new");

        if (newFile.exists()) {

            // if we have a new file
            // delete the old (maybe already deleted)
            File oldFile = new File(file);

            oldFile.delete();

            // rename the new to the current
            newFile.renameTo(oldFile);
        }
    }

    private void create() throws SQLException {

        if (Trace.TRACE) {
            Trace.trace(sName);
        }

        pProperties.setProperty("version", jdbcDriver.VERSION);
        pProperties.setProperty("sql.strict_fk", true);
        pProperties.save();
    }

    /**
     *  Method declaration
     *
     * @return
     * @throws  SQLException
     */
    private boolean isAlreadyOpen() throws SQLException {

        // reading the last modified, wait 3 seconds, read again.
        // if the same information was read the file was not changed
        // and is probably, except the other process is blocked
        if (Trace.TRACE) {
            Trace.trace();
        }

        File f  = new File(sName + ".lock");
        long l1 = f.lastModified();

        try {
            Thread.sleep(3000);
        } catch (Exception e) {}

        long l2 = f.lastModified();

        if (l1 != l2) {
            return true;
        }

        return pProperties.isFileOpen();
    }

    /**
     *  Method declaration
     *
     * @throws  SQLException
     */
    private void backup() throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();

            // if there is no cache file then backup is not necessary
        }

        if (!(new File(sFileCache)).exists()) {
            return;
        }

        try {
            long time = 0;

            if (Trace.TRACE) {
                time = System.currentTimeMillis();
            }

            // create a '.new' file; rename later
            DeflaterOutputStream f = new DeflaterOutputStream(
                new FileOutputStream(sFileBackup + ".new"),
                new Deflater(Deflater.BEST_SPEED), COPY_BLOCK_SIZE);
            byte            b[] = new byte[COPY_BLOCK_SIZE];
            FileInputStream in  = new FileInputStream(sFileCache);

            while (true) {
                int l = in.read(b, 0, COPY_BLOCK_SIZE);

                if (l == -1) {
                    break;
                }

                f.write(b, 0, l);
            }

            f.close();
            in.close();

            if (Trace.TRACE) {
                Trace.trace(time - System.currentTimeMillis());
            }
        } catch (Exception e) {
            throw Trace.error(Trace.FILE_IO_ERROR, sFileBackup);
        }
    }

    /**
     *  Method declaration
     *
     * @throws  SQLException
     */
    private void restoreBackup() throws SQLException {

        if (Trace.TRACE) {
            Trace.trace("not closed last time!");
        }

        if (!(new File(sFileBackup)).exists()) {

            // the backup don't exists because it was never made or is empty
            // the cache file must be deleted in this case
            (new File(sFileCache)).delete();

            return;
        }

        try {
            long time = 0;

            if (Trace.TRACE) {
                time = System.currentTimeMillis();
            }

            InflaterInputStream f =
                new InflaterInputStream(new FileInputStream(sFileBackup),
                                        new Inflater());
            FileOutputStream cache = new FileOutputStream(sFileCache);
            byte             b[]   = new byte[COPY_BLOCK_SIZE];

            while (true) {
                int l = f.read(b, 0, COPY_BLOCK_SIZE);

                if (l == -1) {
                    break;
                }

                cache.write(b, 0, l);
            }

            cache.close();
            f.close();

            if (Trace.TRACE) {
                Trace.trace(time - System.currentTimeMillis());
            }
        } catch (Exception e) {
            throw Trace.error(Trace.FILE_IO_ERROR, sFileBackup);
        }
    }

    /**
     *  Method declaration
     *
     * @throws  SQLException
     */
    private void openScript() throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        try {

            // todo: use a compressed stream
            wScript = new BufferedWriter(new FileWriter(sFileScript, true),
                                         4096);
        } catch (Exception e) {
            throw Trace.error(Trace.FILE_IO_ERROR, sFileScript);
        }
    }

    /**
     *  Method declaration
     *
     * @throws  SQLException
     */
    private void closeScript() throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        try {
            if (wScript != null) {
                wScript.close();

                wScript = null;
            }
        } catch (Exception e) {
            throw Trace.error(Trace.FILE_IO_ERROR, sFileScript);
        }
    }

    /**
     *  Method declaration
     *
     * @throws  SQLException
     */
    private void runScript() throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        if (!(new File(sFileScript)).exists()) {
            return;
        }

        bRestoring = true;

        dDatabase.setReferentialIntegrity(false);

        Vector session = new Vector();

        session.addElement(sysSession);

        Session current = sysSession;

        try {
            long time = 0;

            if (Trace.TRACE) {
                time = System.currentTimeMillis();
            }

            LineNumberReader r =
                new LineNumberReader(new FileReader(sFileScript));

            while (true) {
                String s = readLine(r);

                if (s == null) {
                    break;
                }

                if (s.startsWith("/*C")) {
                    int id = Integer.parseInt(s.substring(3, s.indexOf('*',
                        4)));

                    if (id >= session.size()) {
                        session.setSize(id + 1);
                    }

                    current = (Session) session.elementAt(id);

                    if (current == null) {
                        current = new Session(sysSession, id);

                        session.setElementAt(current, id);
                        dDatabase.registerSession(current);
                    }

                    s = s.substring(s.indexOf('/', 1) + 1);
                }

                if (s.length() != 0) {
                    Result result = dDatabase.execute(s, current);

                    if ((result != null) && (result.iMode == Result.ERROR)) {
                        throw (Trace.getError(result.errorCode,
                                              result.sError));
                    }
                }

                if (s.equals("DISCONNECT")) {
                    int id = current.getId();

                    current = new Session(sysSession, id);

                    session.setElementAt(current, id);
                }
            }

            r.close();

            for (int i = 0; i < session.size(); i++) {
                current = (Session) session.elementAt(i);

                if (current != null) {
                    current.rollback();
                }
            }

            if (Trace.TRACE) {
                Trace.trace(time - System.currentTimeMillis());
            }
        } catch (IOException e) {
            throw Trace.error(Trace.FILE_IO_ERROR, sFileScript + " " + e);
        }

        dDatabase.setReferentialIntegrity(true);

        bRestoring = false;
    }

    /**
     *  Method declaration
     *
     * @param  full
     * @throws  SQLException
     */
    private void writeScript(boolean full) throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();

            // create script in '.new' file
        }

        (new File(sFileScript + ".new")).delete();

        // script; but only positions of cached tables, not full
        scriptToFile(dDatabase, sFileScript + ".new", full, sysSession);
    }

    /**
     *  Method declaration
     *
     * @param  w
     * @param  s
     * @throws  IOException
     */

// fredt@users 20011120 - patch 450455 by kibu@users - optimised
    private static final String lineSep = System.getProperty("line.separator",
        "\n");

    private static int writeLine(Writer w, String s) throws IOException {

        String logLine =
            StringConverter.unicodeToAscii(s).append(lineSep).toString();

        w.write(logLine);

        return logLine.length();
    }

    /**
     *  Method declaration
     *
     * @param  r
     * @return
     * @throws  IOException
     */
    private static String readLine(LineNumberReader r) throws IOException {

        String s = r.readLine();

        return StringConverter.asciiToUnicode(s);
    }

    /**
     *  Method declaration
     *
     * @return
     */
    HsqlDatabaseProperties getProperties() {
        return pProperties;
    }

// fredt@users 20020221 - patch 513005 by sqlbob@users (RMP) - text tables
    private Hashtable textCacheList = new Hashtable();

    Cache openTextCache(String table, String source, boolean readOnlyData,
                        boolean reversed) throws SQLException {

        closeTextCache(table);

        if (pProperties.getProperty("textdb.allow_full_path",
                                    "false").equals("false")) {
            if (source.indexOf("..") != -1) {
                throw (Trace.error(Trace.ACCESS_IS_DENIED, source));
            }

            String path =
                new File(new File(sName).getAbsolutePath()).getParent();

            if (path != null) {
                source = path + File.separator + source;
            }
        }

        String    prefix = "textdb." + table.toLowerCase() + ".";
        TextCache c;

        if (reversed) {
            c = new ReverseTextCache(source, prefix, pProperties);
        } else {
            c = new TextCache(source, prefix, pProperties);
        }

        c.open(readOnlyData || bReadOnly);
        textCacheList.put(table, c);

        return (c);
    }

    void closeTextCache(String table) throws SQLException {

        TextCache c = (TextCache) textCacheList.remove(table);

        if (c != null) {
            c.flush();
        }
    }

    void closeAllTextCaches(boolean compact) throws SQLException {

        for (Enumeration e =
                textCacheList.elements(); e.hasMoreElements(); ) {
            if (compact) {
                ((TextCache) e.nextElement()).purge();
            } else {
                ((TextCache) e.nextElement()).flush();
            }
        }
    }

    void reopenAllTextCaches() throws SQLException {

        for (Enumeration e =
                textCacheList.elements(); e.hasMoreElements(); ) {
            ((TextCache) e.nextElement()).reopen();
        }
    }

    void shutdownAllTextCaches() throws SQLException {

        for (Enumeration e =
                textCacheList.elements(); e.hasMoreElements(); ) {
            ((TextCache) e.nextElement()).shutdown();
        }

        textCacheList = new Hashtable();
    }
}
