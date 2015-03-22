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

import java.sql.SQLException;
import java.sql.Types;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

/**
 *  Database is the root class for HSQL Database Engine database. <p>
 *
 *  Although it either directly or indirectly provides all or most of the
 *  services required for DBMS functionality, this class should not be used
 *  directly by an application. Instead, to achieve portability and
 *  generality, the jdbc* classes should be used.
 *
 * @version  1.7.0
 */

// fredt@users 20020130 - patch 476694 by velichko - transaction savepoints
// additions to different parts to support savepoint transactions
// fredt@users 20020215 - patch 1.7.0 by fredt - new HsqlProperties class
// support use of properties from database.properties file
// fredt@users 20020218 - patch 1.7.0 by fredt - DEFAULT keyword
// support for default values for table columns
// fredt@users 20020305 - patch 1.7.0 - restructuring
// some methods move to Table.java, some removed
// fredt@users 20020221 - patch 513005 by sqlbob@users (RMP) - restructuring
// fredt@users 20020221 - patch 513005 by sqlbob@users (RMP) - error trapping
// boucherb@users 20020130 - patch 1.7.0 - use lookup for speed
// idents listed in alpha-order for easy check of stats...
// fredt@users 20020420 - patch523880 by leptipre@users - VIEW support
// fredt@users 20020430 - patch 549741 by velichko - ALTER TABLE RENAME
// fredt@users 20020405 - patch 1.7.0 by fredt - other ALTER TABLE statements
// boucherb@users - added javadoc comments
// tony_lai@users 20020820 - patch 595099 by tlai@users - use user define PK name
// tony_lai@users 20020820 - patch 595073 by tlai@users - duplicated exception msg
// tony_lai@users 20020820 - patch 595156 by tlai@users - violation of Integrity constraint name
// tony_lai@users 20020820 - patch 1.7.1 - modification to shutdown compact process to save memory usage
// boucherb@users 20020828 - patch 1.7.1 - allow reconnect to local db that has shutdown
// fredt@users 20020912 - patch 1.7.1 by fredt - drop duplicate name triggers
// fredt@users 20020912 - patch 1.7.1 by fredt - log alter statements
class Database {

    private String                 sName;
    private UserManager            aAccess;
    private Vector                 tTable;
    private DatabaseInformation    dInfo;
    Logger                         logger;
    boolean                        bReadOnly;
    private boolean                bShutdown;
    private Hashtable              hAlias;
    private boolean                bIgnoreCase;
    private boolean                bReferentialIntegrity;
    private Vector                 cSession;
    private HsqlDatabaseProperties databaseProperties;
    private Session                sysSession;
    private static final int       CALL       = 1;
    private static final int       CHECKPOINT = 2;
    private static final int       COMMIT     = 3;
    private static final int       CONNECT    = 4;
    private static final int       CREATE     = 5;
    private static final int       DELETE     = 6;
    private static final int       DISCONNECT = 7;
    private static final int       DROP       = 8;
    private static final int       GRANT      = 9;
    private static final int       INSERT     = 10;
    private static final int       REVOKE     = 11;
    private static final int       ROLLBACK   = 12;
    private static final int       SAVEPOINT  = 13;
    private static final int       SCRIPT     = 14;
    private static final int       SELECT     = 15;
    private static final int       SET        = 16;
    private static final int       SHUTDOWN   = 17;
    private static final int       UPDATE     = 18;
    private static final int       SEMICOLON  = 19;
    private static final int       ALTER      = 20;
    private static final Hashtable hCommands  = new Hashtable(37);

    static {
        hCommands.put("ALTER", new Integer(ALTER));
        hCommands.put("CALL", new Integer(CALL));
        hCommands.put("CHECKPOINT", new Integer(CHECKPOINT));
        hCommands.put("COMMIT", new Integer(COMMIT));
        hCommands.put("CONNECT", new Integer(CONNECT));
        hCommands.put("CREATE", new Integer(CREATE));
        hCommands.put("DELETE", new Integer(DELETE));
        hCommands.put("DISCONNECT", new Integer(DISCONNECT));
        hCommands.put("DROP", new Integer(DROP));
        hCommands.put("GRANT", new Integer(GRANT));
        hCommands.put("INSERT", new Integer(INSERT));
        hCommands.put("REVOKE", new Integer(REVOKE));
        hCommands.put("ROLLBACK", new Integer(ROLLBACK));
        hCommands.put("SAVEPOINT", new Integer(SAVEPOINT));
        hCommands.put("SCRIPT", new Integer(SCRIPT));
        hCommands.put("SELECT", new Integer(SELECT));
        hCommands.put("SET", new Integer(SET));
        hCommands.put("SHUTDOWN", new Integer(SHUTDOWN));
        hCommands.put("UPDATE", new Integer(UPDATE));
        hCommands.put(";", new Integer(SEMICOLON));
    }

    /**
     *  Constructs a new Database object that mounts or creates the database
     *  files specified by the supplied name.
     *
     * @param  name the path to and common name shared by the database files
     *      this Database uses
     * @exception  SQLException if the specified path and common name
     *      combination is illegal or unavailable, or the database files the
     *      name resolves to are in use by another process
     */
    Database(String name) throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        sName = name;

        open();
    }

    /**
     * Opens the database.  The database can be opened by the constructor,
     * or reopened by the close(int closemode) method during a
     * "shutdown compact".
     * @see close(int closemode)
     */

    // tony_lai@users 20020820
    private void open() throws SQLException {

        tTable                = new Vector();
        aAccess               = new UserManager();
        cSession              = new Vector();
        hAlias                = new Hashtable();
        logger                = new Logger();
        bReferentialIntegrity = true;

        Library.register(hAlias);

        dInfo = new DatabaseInformation(this, tTable, aAccess);

        boolean newdatabase = false;

        sysSession = new Session(this, new User(null, null, true, null),
                                 true, false, 0);

        registerSession(sysSession);

        databaseProperties = new HsqlDatabaseProperties(sName);

        if (sName.equals(".")) {
            newdatabase = true;

            databaseProperties.setProperty("sql.strict_fk", true);
        } else {
            newdatabase = logger.openLog(this, sysSession, sName);
        }

        HsqlName.sysNumber = 0;

        Library.setSqlMonth(databaseProperties.isPropertyTrue("sql.month"));
        Parser.setEnforceSize(
            databaseProperties.isPropertyTrue("sql.enforce_size"));
        Column.setCompareInLocal(
            databaseProperties.isPropertyTrue("sql.compare_in_locale"));

        Record.gcFrequency =
            databaseProperties.getIntegerProperty("hsqldb.gc_interval", 0);

        if (newdatabase) {
            execute("CREATE USER SA PASSWORD \"\" ADMIN", sysSession);
        }

        aAccess.grant("PUBLIC", "CLASS \"java.lang.Math\"", UserManager.ALL);
        aAccess.grant("PUBLIC", "CLASS \"org.hsqldb.Library\"",
                      UserManager.ALL);
    }

    /**
     *  Retrieves this Database object's name, as know to this Database
     *  object.
     *
     * @return  this Database object's name
     */
    String getName() {
        return sName;
    }

    /**
     *  Retrieves this Database object's properties.
     *
     * @return  this Database object's properties object
     */
    HsqlDatabaseProperties getProperties() {
        return databaseProperties;
    }

    /**
     *  isShutdown attribute getter.
     *
     * @return  the value of this Database object's isShutdown attribute
     */
    boolean isShutdown() {
        return bShutdown;
    }

    /**
     *  Constructs a new Session that operates within (is connected to) the
     *  context of this Database object. <p>
     *
     *  If successful, the new Session object initially operates on behalf of
     *  the user specified by the supplied user name.
     *
     * @param  username the name of the initial user of this session. The user
     *      must already exist in this Database object.
     * @param  password the password of the specified user. This must match
     *      the password, as known to this Database object, of the specified
     *      user
     * @return  a new Session object that initially that initially operates on
     *      behalf of the specified user
     * @throws  SQLException if the specified user does not exist or a bad
     *      password is specified
     */
    synchronized Session connect(String username,
                                 String password) throws SQLException {

        User user = aAccess.getUser(username.toUpperCase(),
                                    password.toUpperCase());
        int size = cSession.size();
        int id   = size;

        for (int i = 0; i < size; i++) {
            if (cSession.elementAt(i) == null) {
                id = i;

                break;
            }
        }

        Session session = new Session(this, user, true, bReadOnly, id);

        logger.writeToLog(session,
                          "CONNECT USER " + username + " PASSWORD \""
                          + password + "\"");
        registerSession(session);

        return session;
    }

    /**
     *  Binds the specified Session object into this Database object's active
     *  session registry. This method is typically called from {@link
     *  #connect} as the final step, when a successful connection has been
     *  made.
     *
     * @param  session the Session object to register
     */
    void registerSession(Session session) {

        int size = cSession.size();
        int id   = session.getId();

        if (id >= size) {
            cSession.setSize(id + 1);
        }

        cSession.setElementAt(session, id);
    }

    /**
     *  A specialized SQL statement executor, tailored for use by {@link
     *  WebServerConnection}. Calling this method fully connects the specified
     *  user, executes the specifed statement, and then disconects.
     *
     * @param  user the name of the user for which to execute the specified
     *      statement. The user must already exist in this Database object.
     * @param  password the password of the specified user. This must match
     *      the password, as known to this Database object, of the specified
     *      user
     * @param  statement the SQL statement to execute
     * @return  the result of executing the specified statement, in a form
     *      already suitable for transmitting as part of an HTTP response.
     */
    byte[] execute(String user, String password, String statement) {

        Result r = null;

        try {
            Session session = connect(user, password);

            r = execute(statement, session);

            execute("DISCONNECT", session);

// fredt@users 20020221 - patch 513005 by sqlbob@users (RMP)
        } catch (SQLException e) {
            r = new Result(e.getMessage(), e.getErrorCode());
        } catch (Exception e) {
            r = new Result(e.getMessage(), Trace.GENERAL_ERROR);
        }

        try {
            return r.getBytes();
        } catch (Exception e) {
            return new byte[0];
        }
    }

    /**
     *  The main SQL statement executor. <p>
     *
     *  All requests to execute SQL statements against this Database object
     *  eventually go through this method.
     *
     * @param  statement the SQL statement to execute
     * @param  session an object representing a connected user and a
     *      collection of session state attributes
     * @return  the result of executing the specified statement, in a form
     *      suitable for either wrapping in a local ResultSet object or for
     *      transmitting to a remote client via the native HSQLDB protocol
     */
    synchronized Result execute(String statement, Session session) {

        if (Record.gcFrequency != 0
                && Record.memoryRecords > Record.gcFrequency) {
            System.gc();
            Trace.printSystemOut("gc at " + Record.memoryRecords);

            Record.memoryRecords = 0;
        }

        if (Trace.TRACE) {
            Trace.trace(statement);
        }

        Result rResult = null;

        try {
            Tokenizer c = new Tokenizer(statement);
            Parser    p = new Parser(this, c, session);

            logger.cleanUp();

            if (Trace.DOASSERT) {
                Trace.doAssert(!session.isNestedTransaction());
            }

            Trace.check(session != null, Trace.ACCESS_IS_DENIED);
            Trace.check(!bShutdown, Trace.DATABASE_IS_SHUTDOWN);

            while (true) {
                c.setPartMarker();

// fredt@users 20020221 - patch 513005 by sqlbob@users (RMP)
                session.setScripting(false);

                String sToken = c.getString();

                if (sToken.length() == 0) {
                    break;
                }

// boucherb@users 20020306 - patch 1.7.0 - use lookup for tokens
                Integer command = (Integer) hCommands.get(sToken);

                if (command == null) {
                    throw Trace.error(Trace.UNEXPECTED_TOKEN, sToken);
                }

                int cmd = command.intValue();

                switch (cmd) {

                    case SELECT :
                        rResult = p.processSelect();
                        break;

                    case INSERT :
                        rResult = p.processInsert();
                        break;

                    case UPDATE :
                        rResult = p.processUpdate();
                        break;

                    case DELETE :
                        rResult = p.processDelete();
                        break;

                    case CALL :
                        rResult = p.processCall();
                        break;

                    case SET :
                        rResult = processSet(c, session);
                        break;

                    case COMMIT :
                        rResult = processCommit(c, session);

                        session.setScripting(true);
                        break;

                    case ROLLBACK :
                        rResult = processRollback(c, session);

                        session.setScripting(true);
                        break;

                    case SAVEPOINT :
                        rResult = processSavepoint(c, session);

                        session.setScripting(true);
                        break;

                    case CREATE :
                        rResult = processCreate(c, session);
                        break;

                    case ALTER :
                        rResult = processAlter(c, session);
                        break;

                    case DROP :
                        rResult = processDrop(c, session);
                        break;

                    case GRANT :
                        rResult = processGrantOrRevoke(c, session, true);
                        break;

                    case REVOKE :
                        rResult = processGrantOrRevoke(c, session, false);
                        break;

                    case CONNECT :
                        rResult = processConnect(c, session);
                        break;

                    case DISCONNECT :
                        rResult = processDisconnect(session);
                        break;

                    case SCRIPT :
                        rResult = processScript(c, session);
                        break;

                    case SHUTDOWN :
                        rResult = processShutdown(c, session);
                        break;

                    case CHECKPOINT :
                        rResult = processCheckpoint(session);
                        break;

                    case SEMICOLON :
                        break;
                }

                if (session.getScripting()) {
                    logger.writeToLog(session, c.getLastPart());
                }
            }
        } catch (SQLException e) {

            // e.printStackTrace();
// fredt@users 20020221 - patch 513005 by sqlbob@users (RMP)
// tony_lai@users 20020820 - patch 595073
//            rResult = new Result(Trace.getMessage(e) + " in statement ["
            rResult = new Result(e.getMessage() + " in statement ["
                                 + statement + "]", e.getErrorCode());
        } catch (Exception e) {
            e.printStackTrace();

            String s = Trace.getMessage(Trace.GENERAL_ERROR) + " " + e;

            rResult = new Result(s + " in statement [" + statement + "]",
                                 Trace.GENERAL_ERROR);
        } catch (java.lang.OutOfMemoryError e) {
            e.printStackTrace();

            rResult = new Result("out of memory", Trace.GENERAL_ERROR);
        }

        return rResult == null ? new Result()
                               : rResult;
    }

    /**
     *  Puts this Database object in global read-only mode. That is, after
     *  this call, all existing and future sessions are limited to read-only
     *  transactions. Any following attempts to update the state of the
     *  database will result in throwing a SQLException.
     */
    void setReadOnly() {
        bReadOnly = true;
    }

    /**
     *  Retrieves a Vector containing references to all registered non-system
     *  tables and views. This includes all tables and views registered with
     *  this Database object via a call to {@link #linkTable linkTable}.
     *
     * @return  a Vector of all registered non-system tables and views
     */
    Vector getTables() {
        return tTable;
    }

    /**
     *  Retrieves the UserManager object for this Database.
     *
     * @return  UserManager object
     */
    UserManager getUserManager() {
        return aAccess;
    }

    /**
     *  isReferentialIntegrity attribute setter.
     *
     * @param  ref if true, this Database object enforces referential
     *      integrity, else not
     */
    void setReferentialIntegrity(boolean ref) {
        bReferentialIntegrity = ref;
    }

    /**
     *  isReferentialIntegrity attribute getter.
     *
     * @return  indicates whether this Database object is currently enforcing
     *      referential integrity
     */
    boolean isReferentialIntegrity() {
        return bReferentialIntegrity;
    }

    /**
     *  Retrieves a map from Java method-call name aliases to the
     *  fully-qualified names of the Java methods themsleves.
     *
     * @return  a map in the form of a Hashtable
     */
    Hashtable getAlias() {
        return hAlias;
    }

    /**
     *  Retieves a Java method's fully qualified name, given a String that is
     *  supposedly an alias for it. <p>
     *
     *  This is somewhat of a misnomer, since it is not an alias that is being
     *  retrieved, but rather what the supplied alias maps to. If the
     *  specified alias does not map to any registered Java method
     *  fully-qualified name, then the specified String itself is returned.
     *
     * @param  s a call name alias that supposedly maps to a registered Java
     *      method
     * @return  a Java method fully-qualified name, or null if no method is
     *      registered with the given alias
     */
    String getAlias(String s) {

        String alias = (String) hAlias.get(s);

        return (alias == null) ? s
                               : alias;
    }

// fredt@users 20020221 - patch 513005 by sqlbob@users (RMP)
// temp tables should be accessed by the owner and not scripted in the log

    /**
     *  Retrieves the specified user defined table or view visible within the
     *  context of the specified Session, or any system table of the given
     *  name. This excludes any temp tables created in different Sessions.
     *
     * @param  name of the table or view to retrieve
     * @param  session the Session within which to search for user tables
     * @return  the user table or view, or system table
     * @throws  SQLException if there is no such table or view
     */
    Table getTable(String name, Session session) throws SQLException {

        Table t = findUserTable(name, session);

        if (t == null) {
            t = dInfo.getSystemTable(name, session);
        }

        if (t == null) {
            throw Trace.error(Trace.TABLE_NOT_FOUND, name);
        }

        return t;
    }

    /**
     *  get a user
     *
     * @param  name
     * @param  session
     * @return
     * @throws  SQLException
     */
    Table getUserTable(String name, Session session) throws SQLException {

        Table t = findUserTable(name, session);

        if (t == null) {
            throw Trace.error(Trace.TABLE_NOT_FOUND, name);
        }

        return t;
    }

    Table getUserTable(String name) throws SQLException {

        Table t = findUserTable(name);

        if (t == null) {
            throw Trace.error(Trace.TABLE_NOT_FOUND, name);
        }

        return t;
    }

    Table findUserTable(String name) {

        for (int i = 0, tsize = tTable.size(); i < tsize; i++) {
            Table t = (Table) tTable.elementAt(i);

            if (t.equals(name)) {
                return t;
            }
        }

        return null;
    }

// fredt@users 20020221 - patch 513005 by sqlbob@users (RMP)
    Table findUserTable(String name, Session session) {

        for (int i = 0, tsize = tTable.size(); i < tsize; i++) {
            Table t = (Table) tTable.elementAt(i);

            if (t.equals(name, session)) {
                return t;
            }
        }

        return null;
    }

    /**
     *  Generates a SQL script containing all or part of the SQL statements
     *  required to recreate the current state of this Database object.
     *
     * @param  drop if true, include drop statements for each droppable
     *      database object
     * @param  insert if true, include the insert statements required to
     *      populate each of this Database object's memory tables to match
     *      their current state.
     * @param  cached if true, include the insert statement required to
     *      populate each of this Database object's CACHED tables to match
     *      their current state.
     * @param  session the Session in which to generate the requested SQL
     *      script
     * @return  A Result object consisting of one VARCHAR column with a row
     *      for each statement in the generated script
     * @throws  SQLException if the specified Session's currently connected
     *      User does not have the right to call this method or there is some
     *      problem generating the result
     */
    Result getScript(boolean drop, boolean insert, boolean cached,
                     Session session) throws SQLException {
        return DatabaseScript.getScript(this, drop, insert, cached, session);
    }

    /**
     *  Attempts to register the specified table or view with this Database
     *  object.
     *
     * @param  t the table of view to register
     * @throws  SQLException if there is a problem
     */
    void linkTable(Table t) throws SQLException {
        tTable.addElement(t);
    }

    /**
     *  isIgnoreCase attribute getter.
     *
     * @return  the value of this Database object's isIgnoreCase attribute
     */
    boolean isIgnoreCase() {
        return bIgnoreCase;
    }

    /**
     *  Responsible for parsing and executing the SCRIPT SQL statement
     *
     * @param  c the tokenized representation of the statement being processed
     * @param  session
     * @return
     * @throws  SQLException
     */
    private Result processScript(Tokenizer c,
                                 Session session) throws SQLException {

        String sToken = c.getString();

        if (c.wasValue()) {
            sToken = (String) c.getAsValue();

            Log.scriptToFile(this, sToken, true, session);

            return new Result();
        } else {
            c.back();

// fredt@users - patch 1.7.0 - no DROP TABLE statements with SCRIPT command
// try to script all but drop, insert; but no positions for cached tables
            return getScript(false, true, false, session);
        }
    }

    /**
     *  Responsible for handling the parse and execution of CREATE SQL
     *  statements.
     *
     * @param  c the tokenized representation of the statement being processed
     * @param  session
     * @return
     * @throws  SQLException
     */
    private Result processCreate(Tokenizer c,
                                 Session session) throws SQLException {

        session.checkReadWrite();
        session.checkAdmin();

        String sToken = c.getString();

// fredt@users 20020221 - patch 513005 by sqlbob@users (RMP)
        boolean isTemp = false;

        if (sToken.equals("TEMP")) {
            isTemp = true;
            sToken = c.getString();

            Trace.check(sToken.equals("TABLE") || sToken.equals("MEMORY")
                        || sToken.equals("TEXT"), Trace.UNEXPECTED_TOKEN,
                                                  sToken);
            session.setScripting(false);
        } else {
            session.checkReadWrite();
            session.checkAdmin();
            session.setScripting(true);
        }

// fredt@users 20020221 - patch 513005 by sqlbob@users (RMP)
        if (sToken.equals("TABLE")) {
            int tableType = isTemp ? Table.TEMP_TABLE
                                   : Table.MEMORY_TABLE;

            processCreateTable(c, session, tableType);
        } else if (sToken.equals("MEMORY")) {
            c.getThis("TABLE");

            int tableType = isTemp ? Table.TEMP_TABLE
                                   : Table.MEMORY_TABLE;

            processCreateTable(c, session, tableType);
        } else if (sToken.equals("CACHED")) {
            c.getThis("TABLE");
            processCreateTable(c, session, Table.CACHED_TABLE);
        } else if (sToken.equals("TEXT")) {
            c.getThis("TABLE");

            int tableType = isTemp ? Table.TEMP_TEXT_TABLE
                                   : Table.TEXT_TABLE;

            processCreateTable(c, session, tableType);
        } else if (sToken.equals("VIEW")) {
            processCreateView(c, session);
        } else if (sToken.equals("TRIGGER")) {
            processCreateTrigger(c, session);
        } else if (sToken.equals("USER")) {
            String u = c.getStringToken();

            c.getThis("PASSWORD");

            String  p = c.getStringToken();
            boolean admin;

            if (c.getString().equals("ADMIN")) {
                admin = true;
            } else {
                admin = false;
            }

            aAccess.createUser(u, p, admin);
        } else if (sToken.equals("ALIAS")) {
            String name = c.getString();

            sToken = c.getString();

            Trace.check(sToken.equals("FOR"), Trace.UNEXPECTED_TOKEN, sToken);

            sToken = c.getString();

// fredt@users 20010701 - patch 1.6.1 by fredt - open <1.60 db files
// convert org.hsql.Library aliases from versions < 1.60 to org.hsqldb
// fredt@users 20020221 - patch 513005 by sqlbob@users (RMP) - ABS function
            if (sToken.startsWith("org.hsql.Library.")) {
                sToken = "org.hsqldb.Library."
                         + sToken.substring("org.hsql.Library.".length());
            } else if (sToken.equals("java.lang.Math.abs")) {
                sToken = "org.hsqldb.Library.abs";
            }

            hAlias.put(name, sToken);
        } else {
            boolean unique = false;

            if (sToken.equals("UNIQUE")) {
                unique = true;
                sToken = c.getString();
            }

            if (!sToken.equals("INDEX")) {
                throw Trace.error(Trace.UNEXPECTED_TOKEN, sToken);
            }

            String  name         = c.getName();
            boolean isnamequoted = c.wasQuotedIdentifier();

            c.getThis("ON");

            Table t = getTable(c.getName(), session);

            addIndexOn(c, session, name, isnamequoted, t, unique);
        }

        return new Result();
    }

    /**
     *  Process a bracketed column list as used in the declaration of SQL
     *  CONSTRAINTS and return an array containing the indexes of the columns
     *  within the table.
     *
     * @param  c
     * @param  t table that contains the columns
     * @return
     * @throws  SQLException if a column is not found or is duplicated
     */
    private int[] processColumnList(Tokenizer c,
                                    Table t) throws SQLException {

        Vector    v = new Vector();
        Hashtable h = new Hashtable();

        c.getThis("(");

        while (true) {
            String colname = c.getName();

            v.addElement(colname);
            h.put(colname, colname);

            String sToken = c.getString();

            if (sToken.equals(")")) {
                break;
            }

            if (!sToken.equals(",")) {
                throw Trace.error(Trace.UNEXPECTED_TOKEN, sToken);
            }
        }

        int s = v.size();

        if (s != h.size()) {
            throw Trace.error(Trace.COLUMN_ALREADY_EXISTS,
                              "duplicate column in list");
        }

        int col[] = new int[s];

        for (int i = 0; i < s; i++) {
            col[i] = t.getColumnNr((String) v.elementAt(i));
        }

        return col;
    }

    /**
     *  Indexes defined in DDL scripts are handled by this method. If the
     *  name of an existing index begins with "SYS_", the name is changed to
     *  begin with "USER_". The name should be unique within the database.
     *  For compatibility with old database, non-unique names are modified
     *  and assigned a new name<br>
     *  (fredt@users)
     *
     * @param  c
     * @param  session
     * @param  name
     * @param  t
     * @param  unique
     * @param  namequoted The feature to be added to the IndexOn attribute
     * @throws  SQLException
     */
    private void addIndexOn(Tokenizer c, Session session, String name,
                            boolean namequoted, Table t,
                            boolean unique) throws SQLException {

        HsqlName indexname;
        int      col[] = processColumnList(c, t);

        if (HsqlName.isReservedName(name)) {
            indexname = HsqlName.makeAutoName("USER", name);
        } else {
            indexname = new HsqlName(name, namequoted);
        }

// fredt@users - to check further - this is confined only to old scripts
// rename duplicate indexes
/*
        if (findIndex(name) != null && session == sysSession
                && databaseProperties.getProperty("hsqldb.compatible_version")
                    .equals("1.6.0")) {
            indexname = HsqlName.makeAutoName("USER", name);
            name      = indexname.name;
        }
*/
        if (findIndex(name) != null) {
            throw Trace.error(Trace.INDEX_ALREADY_EXISTS);
        }

        session.commit();
        session.setScripting(!t.isTemp());

        TableWorks tw = new TableWorks(t);

        tw.createIndex(col, indexname, unique);
    }

    /**
     *  Finds an index with the given name in the whole database.
     *
     * @param  name Description of the Parameter
     * @return  Description of the Return Value
     */
    private Index findIndex(String name) {

        Table t = findTableForIndex(name);

        if (t == null) {
            return null;
        } else {
            return t.getIndex(name);
        }
    }

    /**
     *  Finds the table that has an index with the given name in the
     *  whole database.
     *
     * @param  name Description of the Parameter
     * @return  Description of the Return Value
     */
    private Table findTableForIndex(String name) {

        for (int i = 0, tsize = tTable.size(); i < tsize; i++) {
            Table t = (Table) tTable.elementAt(i);

            if (t.getIndex(name) != null) {
                return t;
            }
        }

        return null;
    }

    /**
     *  Retrieves the index of a table or view in the Vector that contains
     *  these objects for a Database.
     *
     * @param  table the Table object
     * @return  the index of the specified table or view, or -1 if not found
     */
    int getTableIndex(Table table) {

        for (int i = 0, tsize = tTable.size(); i < tsize; i++) {
            Table t = (Table) tTable.elementAt(i);

            if (t == table) {
                return i;
            }
        }

        return -1;
    }

    /**
     *  Responsible for handling the execution of CREATE TRIGGER SQL
     *  statements. <p>
     *
     *  typical sql is: CREATE TRIGGER tr1 AFTER INSERT ON tab1 CALL "pkg.cls"
     *
     * @param  c the tokenized representation of the statement being processed
     * @param  session
     * @throws  SQLException
     */
    private void processCreateTrigger(Tokenizer c,
                                      Session session) throws SQLException {

        Table   t;
        boolean bForEach   = false;
        boolean bNowait    = false;
        int     nQueueSize = TriggerDef.getDefaultQueueSize();
        String  sTrigName  = c.getName();
        String  sWhen      = c.getString();
        String  sOper      = c.getString();

        c.getThis("ON");

        String sTableName = c.getString();

        t = getTable(sTableName, session);

        if (t.isView()) {
            throw Trace.error(Trace.NOT_A_TABLE);
        }

// fredt@users 20020221 - patch 513005 by sqlbob@users (RMP)
        session.setScripting(!t.isTemp());

        // "FOR EACH ROW" or "CALL"
        String tok = c.getString();

        if (tok.equals("FOR")) {
            tok = c.getString();

            if (tok.equals("EACH")) {
                tok = c.getString();

                if (tok.equals("ROW")) {
                    bForEach = true;
                    tok      = c.getString();    // should be 'NOWAIT' or 'QUEUE' or 'CALL'
                } else {
                    throw Trace.error(Trace.UNEXPECTED_END_OF_COMMAND, tok);
                }
            } else {
                throw Trace.error(Trace.UNEXPECTED_END_OF_COMMAND, tok);
            }
        }

        if (tok.equals("NOWAIT")) {
            bNowait = true;
            tok     = c.getString();    // should be 'CALL' or 'QUEUE'
        }

        if (tok.equals("QUEUE")) {
            nQueueSize = Integer.parseInt(c.getString());
            tok        = c.getString();    // should be 'CALL'
        }

        if (!tok.equals("CALL")) {
            throw Trace.error(Trace.UNEXPECTED_END_OF_COMMAND, tok);
        }

        String     sClassName = c.getString();    // double quotes have been stripped
        TriggerDef td;
        Trigger    o;

        try {
            Class cl = Class.forName(sClassName);    // dynamically load class

            o = (Trigger) cl.newInstance();          // dynamically instantiate it
            td = new TriggerDef(sTrigName, sWhen, sOper, bForEach, t, o,
                                "\"" + sClassName + "\"", bNowait,
                                nQueueSize);

            if (td.isValid()) {
                t.addTrigger(td);
                td.start();                          // start the trigger thread
            } else {
                String msg = "Error in parsing trigger command ";

                throw Trace.error(Trace.UNEXPECTED_TOKEN, msg);
            }
        } catch (Exception e) {
            String msg = "Exception in loading trigger class "
                         + e.getMessage();

            throw Trace.error(Trace.UNKNOWN_FUNCTION, msg);
        }
    }

    /**
     *  Responsible for handling the creation of table columns during the
     *  process of executing CREATE TABLE statements.
     *
     * @param  c the tokenized representation of the statement being processed
     * @param  t target table
     * @return
     * @throws  SQLException
     */
    private Column processCreateColumn(Tokenizer c,
                                       Table t) throws SQLException {

        boolean identity     = false;
        boolean primarykey   = false;
        String  sToken       = c.getString();
        String  sColumn      = sToken;
        boolean isnamequoted = c.wasQuotedIdentifier();
        String  typestring   = c.getString();
        int     iType        = Column.getTypeNr(typestring);

        Trace.check(!sColumn.equals(Table.DEFAULT_PK),
                    Trace.COLUMN_ALREADY_EXISTS, sColumn);

        if (typestring.equals("IDENTITY")) {
            identity   = true;
            primarykey = true;
        }

        if (iType == Types.VARCHAR && bIgnoreCase) {
            iType = Column.VARCHAR_IGNORECASE;
        }

        sToken = c.getString();

        if (iType == Types.DOUBLE && sToken.equals("PRECISION")) {
            sToken = c.getString();
        }

// fredt@users 20020130 - patch 491987 by jimbag@users
        String sLen = "";

        if (sToken.equals("(")) {

            // read length
            do {
                sToken = c.getString();

                if (!sToken.equals(")")) {
                    sLen += sToken;
                }
            } while (!sToken.equals(")"));

            sToken = c.getString();
        }

        int iLen   = 0;
        int iScale = 0;

        // see if we have a scale specified
        int index;

        if ((index = sLen.indexOf(",")) != -1) {
            String sScale = sLen.substring(index + 1, sLen.length());

            sLen = sLen.substring(0, index);

            try {
                iScale = Integer.parseInt(sScale.trim());
            } catch (NumberFormatException ne) {
                throw Trace.error(Trace.UNEXPECTED_TOKEN, sLen);
            }
        }

        // convert the length
        if (sLen.trim().length() > 0) {
            try {
                iLen = Integer.parseInt(sLen.trim());
            } catch (NumberFormatException ne) {
                throw Trace.error(Trace.UNEXPECTED_TOKEN, sLen);
            }
        }

        String defaultvalue = null;

        if (sToken.equals("DEFAULT")) {
            String s = c.getString();

            if (c.wasValue() && iType != Types.BINARY
                    && iType != Types.OTHER) {
                Object sv = c.getAsValue();

                if (sv != null) {
                    defaultvalue = String.valueOf(sv);

                    try {
                        Column.convertObject(defaultvalue, iType);
                    } catch (Exception e) {
                        throw Trace.error(Trace.WRONG_DEFAULT_CLAUSE,
                                          defaultvalue);
                    }

                    String testdefault =
                        (String) Parser.enforceSize(defaultvalue, iType,
                                                    iLen, false);

                    if (defaultvalue.equals(testdefault) == false) {
                        throw Trace.error(Trace.WRONG_DEFAULT_CLAUSE,
                                          defaultvalue);
                    }
                }
            } else {
                throw Trace.error(Trace.WRONG_DEFAULT_CLAUSE, s);
            }

            sToken = c.getString();
        }

        boolean nullable = true;

        if (sToken.equals("NULL")) {
            sToken = c.getString();
        } else if (sToken.equals("NOT")) {
            c.getThis("NULL");

            nullable = false;
            sToken   = c.getString();
        }

        if (sToken.equals("IDENTITY")) {
            identity   = true;
            sToken     = c.getString();
            primarykey = true;
        }

        if (sToken.equals("PRIMARY")) {
            c.getThis("KEY");

            primarykey = true;
        } else {
            c.back();
        }

        return new Column(new HsqlName(sColumn, isnamequoted), nullable,
                          iType, iLen, iScale, identity, primarykey,
                          defaultvalue);
    }

// fredt@users 20020225 - patch 509002 by fredt
// temporary attributes for constraints used in processCreateTable()

    /**
     *  temporary attributes for constraints used in processCreateTable()
     */
    private class TempConstraint {

        HsqlName name;
        int[]    localCol;
        Table    expTable;
        int[]    expCol;
        int      type;
        boolean  cascade;

        TempConstraint(HsqlName name, int[] localCol, Table expTable,
                       int[] expCol, int type, boolean cascade) {

            this.name     = name;
            this.type     = type;
            this.localCol = localCol;
            this.expTable = expTable;
            this.expCol   = expCol;
            this.cascade  = cascade;
        }
    }

// fredt@users 20020225 - patch 509002 by fredt
// process constraints after parsing to include primary keys defined as
// constraints
// fredt@users 20020225 - patch 489777 by fredt
// better error trapping
// fredt@users 20020221 - patch 513005 by sqlbob@users (RMP)

    /**
     *  Responsible for handling the execution CREATE TABLE SQL statements.
     *
     * @param  c
     * @param  session
     * @param  type Description of the Parameter
     * @throws  SQLException
     */
    private void processCreateTable(Tokenizer c, Session session,
                                    int type) throws SQLException {

        Table   t;
        String  sToken       = c.getName();
        boolean isnamequoted = c.wasQuotedIdentifier();

        if (DatabaseInformation.isSystemTable(sToken)
                || findUserTable(sToken, session) != null) {
            throw Trace.error(Trace.TABLE_ALREADY_EXISTS, sToken);
        }

        if (type == Table.TEMP_TEXT_TABLE || type == Table.TEXT_TABLE) {
            t = new TextTable(this, new HsqlName(sToken, isnamequoted), type,
                              session);
        } else {
            t = new Table(this, new HsqlName(sToken, isnamequoted), type,
                          session);
        }

        c.getThis("(");

        int[]   primarykeycolumn = null;
        int     column           = 0;
        boolean constraint       = false;

        while (true) {
            sToken       = c.getString();
            isnamequoted = c.wasQuotedIdentifier();

// fredt@users 20020225 - comment
// we can check here for reserved words used with quotes as column names
            if (sToken.equals("CONSTRAINT") || sToken.equals("PRIMARY")
                    || sToken.equals("FOREIGN") || sToken.equals("UNIQUE")) {
                c.back();

                constraint = true;

                break;
            }

            c.back();

            Column newcolumn = processCreateColumn(c, t);

            t.addColumn(newcolumn);

            if (newcolumn.isPrimaryKey()) {
                Trace.check(primarykeycolumn == null,
                            Trace.SECOND_PRIMARY_KEY, "column " + column);

                primarykeycolumn = new int[]{ column };
            }

            sToken = c.getString();

            if (sToken.equals(")")) {
                break;
            }

            if (!sToken.equals(",")) {
                throw Trace.error(Trace.UNEXPECTED_TOKEN, sToken);
            }

            column++;
        }

        try {

// fredt@users 20020225 - comment
// HSQLDB relies on primary index to be the first one defined
// and needs original or system added primary key before any non-unique index
// is created
            Vector tempConstraints = new Vector();
            TempConstraint tempConst = new TempConstraint(null,
                primarykeycolumn, null, null, Constraint.MAIN, false);

// tony_lai@users 20020820 - patch 595099
            HsqlName pkName = null;

            tempConstraints.addElement(tempConst);

            if (constraint) {
                int i = 0;

                while (true) {
                    sToken = c.getString();

                    HsqlName cname = null;

                    i++;

                    if (sToken.equals("CONSTRAINT")) {
                        cname = new HsqlName(c.getName(),
                                             c.wasQuotedIdentifier());
                        sToken = c.getString();
                    }

                    if (sToken.equals("PRIMARY")) {
                        c.getThis("KEY");

// tony_lai@users 20020820 - patch 595099
                        pkName = cname;

                        int col[] = processColumnList(c, t);
                        TempConstraint mainConst =
                            (TempConstraint) tempConstraints.elementAt(0);

                        Trace.check(mainConst.localCol == null,
                                    Trace.SECOND_PRIMARY_KEY);

                        mainConst.localCol = col;
                    } else if (sToken.equals("UNIQUE")) {
                        int col[] = processColumnList(c, t);

                        if (cname == null) {
                            cname = HsqlName.makeAutoName("CT");
                        }

                        tempConst = new TempConstraint(cname, col, null,
                                                       null,
                                                       Constraint.UNIQUE,
                                                       false);

                        tempConstraints.addElement(tempConst);
                    } else if (sToken.equals("FOREIGN")) {
                        c.getThis("KEY");

                        tempConst = processCreateFK(c, session, t, cname);

                        if (tempConst.expCol == null) {
                            TempConstraint mainConst =
                                (TempConstraint) tempConstraints.elementAt(0);

                            tempConst.expCol = mainConst.localCol;

                            if (tempConst.expCol == null) {
                                throw Trace.error(Trace.INDEX_NOT_FOUND,
                                                  "table has no primary key");
                            }
                        }

                        t.checkColumnsMatch(tempConst.localCol,
                                            tempConst.expTable,
                                            tempConst.expCol);
                        tempConstraints.addElement(tempConst);
                    }

                    sToken = c.getString();

                    if (sToken.equals(")")) {
                        break;
                    }

                    if (!sToken.equals(",")) {
                        throw Trace.error(Trace.UNEXPECTED_TOKEN, sToken);
                    }
                }
            }

            session.commit();

// fredt@users 20020225 - patch 509002 by fredt
// it is essential to stay compatible with existing cached tables
// so we create all constraints and indexes (even duplicates) for cached
// tables
// CONSTRAINT PRIMARY KEY can appear in user scripts and new tables only so
// we can safely apply it correctly
// first apply any primary key constraint
// then set all the constriants
// also, duplicate indexes can be avoided if we choose to in the future but
// currently we have to accept them to stay compatible with existing cached
// tables that include them
            tempConst = (TempConstraint) tempConstraints.elementAt(0);

// tony_lai@users 20020820 - patch 595099
            t.createPrimaryKey(pkName, tempConst.localCol);

            boolean logDDL = false;

            for (int i = 1; i < tempConstraints.size(); i++) {
                tempConst = (TempConstraint) tempConstraints.elementAt(i);

                if (tempConst.type == Constraint.UNIQUE) {
                    TableWorks tw = new TableWorks(t);

                    tw.createUniqueConstraint(tempConst.localCol,
                                              tempConst.name);

                    t = tw.getTable();
                }

                if (tempConst.type == Constraint.FOREIGN_KEY) {
                    TableWorks tw = new TableWorks(t);

                    tw.createForeignKey(tempConst.localCol, tempConst.expCol,
                                        tempConst.name, tempConst.expTable,
                                        tempConst.cascade);

                    t = tw.getTable();
                }
            }

            linkTable(t);
        } catch (SQLException e) {

// fredt@users 20020225 - comment
// if a SQLException is thrown while creating table, any foreign key that has
// been created leaves it modification to the expTable in place
// need to undo those modifications. This should not happen in practice.
            removeExportedKeys(t);

            throw e;
        }
    }

    TempConstraint processCreateFK(Tokenizer c, Session session, Table t,
                                   HsqlName cname) throws SQLException {

        int localcol[] = processColumnList(c, t);

        c.getThis("REFERENCES");

        String expTableName = c.getString();
        Table  expTable;

// fredt@users 20020221 - patch 520213 by boucherb@users - self reference FK
// allows foreign keys that reference a column in the same table
        if (t.equals(expTableName)) {
            expTable = t;
        } else {
            expTable = getTable(expTableName, session);
        }

        int    expcol[] = null;
        String sToken   = c.getString();

        c.back();

// fredt@users 20020503 - patch 1.7.0 by fredt -  FOREIGN KEY on table
        if (sToken.equals("(")) {
            expcol = processColumnList(c, expTable);
        } else {

            // the exp table must have a user defined primary key
            Index expIndex = expTable.getPrimaryIndex();

            if (expIndex != null) {
                expcol = expIndex.getColumns();

                if (expcol[0] == expTable.getColumnCount()) {
                    throw Trace.error(Trace.INDEX_NOT_FOUND,
                                      expTableName + " has no primary key");
                }
            }

            // with CREATE TABLE, (expIndex == null) when self referencing FK
            // is declared in CREATE TABLE
            // null will be returned for expCol and will be checked
            // in caller method
            // with ALTER TABLE, (expIndex == null) when table has no PK
        }

        sToken = c.getString();

// fredt@users 20020305 - patch 1.7.0 - cascading deletes
        boolean cascade = false;

        if (sToken.equals("ON")) {
            c.getThis("DELETE");
            c.getThis("CASCADE");

            cascade = true;
        } else {
            c.back();
        }

        if (cname == null) {
            cname = HsqlName.makeAutoName("FK");
        }

        return new TempConstraint(cname, localcol, expTable, expcol,
                                  Constraint.FOREIGN_KEY, cascade);
    }

// fredt@users 20020420 - patch523880 by leptipre@users - VIEW support

    /**
     *  Responsible for handling the execution CREATE VIEW SQL statements.
     *
     * @param  session
     * @param  c
     * @throws  SQLException
     */
    private void processCreateView(Tokenizer c,
                                   Session session) throws SQLException {

        View   v;
        String sToken      = c.getName();
        int    logposition = c.getPartMarker();

        if (this.findUserTable(sToken, session) != null) {
            throw Trace.error(Trace.VIEW_ALREADY_EXISTS, sToken);
        }

        v = new View(this, new HsqlName(sToken, c.wasQuotedIdentifier()));

        c.getThis("AS");
        c.setPartMarker();
        c.getThis("SELECT");

        Result rResult;
        Parser p       = new Parser(this, c, session);
        int    maxRows = session.getMaxRows();

        try {
            Select select = p.parseSelect();

            if (select.sIntoTable != null) {
                throw (Trace.error(Trace.TABLE_NOT_FOUND));
            }

            select.setPreProcess();

            rResult = select.getResult(1);
        } catch (SQLException e) {
            throw e;
        }

        v.setStatement(c.getLastPart());
        v.addColumns(rResult);
        session.commit();
        tTable.addElement(v);
        c.setPartMarker(logposition);
    }

    private void processRenameTable(Tokenizer c, Session session,
                                    String tablename) throws SQLException {

        String  newname  = c.getName();
        boolean isquoted = c.wasQuotedIdentifier();
        Table   t        = findUserTable(tablename);

        // this ensures temp table belongs to this session
        if (t == null ||!t.equals(tablename, session)) {
            Trace.error(Trace.TABLE_NOT_FOUND, tablename);
        }

        Table ttemp = findUserTable(newname);

        if (ttemp != null && ttemp.equals(ttemp.getName().name, session)) {
            throw Trace.error(Trace.TABLE_ALREADY_EXISTS, tablename);
        }

        session.commit();
        session.setScripting(!t.isTemp());
        t.setName(newname, isquoted);
    }

    /**
     * 'RENAME' declaration.
     * ALTER TABLE <name> RENAME TO <newname>
     * ALTER INDEX <name> RENAME TO <newname>
     *
     * ALTER TABLE <name> ADD CONSTRAINT <constname> FOREIGN KEY (<col>, ...)
     * REFERENCE <other table> (<col>, ...) [ON DELETE CASCADE]
     *
     * ALTER TABLE <name> ADD CONSTRAINT <constname> UNIQUE (<col>, ...)
     *
     * @param  c
     * @param  session
     * @return  Result
     * @throws  SQLException
     */
    private Result processAlter(Tokenizer c,
                                Session session) throws SQLException {

        session.checkReadWrite();
        session.checkAdmin();
        session.setScripting(true);

        String sToken = c.getString();

        if (sToken.equals("TABLE")) {
            processAlterTable(c, session);
        } else if (sToken.equals("INDEX")) {
            processAlterIndex(c, session);
        } else {
            throw Trace.error(Trace.UNEXPECTED_TOKEN, sToken);
        }

        return new Result();
    }

    private void processAlterTable(Tokenizer c,
                                   Session session) throws SQLException {

        String     tablename = c.getString();
        Table      t         = getUserTable(tablename, session);
        TableWorks tw        = new TableWorks(t);
        String     sToken    = c.getString();

        session.setScripting(!t.isTemp());

        if (sToken.equals("RENAME")) {
            c.getThis("TO");
            processRenameTable(c, session, tablename);

            return;
        } else if (sToken.equals("ADD")) {
            sToken = c.getString();

            if (sToken.equals("CONSTRAINT")) {
                HsqlName cname = new HsqlName(c.getName(),
                                              c.wasQuotedIdentifier());

                sToken = c.getString();

                if (sToken.equals("FOREIGN")) {
                    c.getThis("KEY");

                    TempConstraint tc = processCreateFK(c, session, t, cname);

                    t.checkColumnsMatch(tc.localCol, tc.expTable, tc.expCol);
                    session.commit();
                    tw.createForeignKey(tc.localCol, tc.expCol, tc.name,
                                        tc.expTable, tc.cascade);

                    return;
                } else if (sToken.equals("UNIQUE")) {
                    int col[] = processColumnList(c, t);

                    session.commit();
                    tw.createUniqueConstraint(col, cname);

                    return;
                } else {
                    throw Trace.error(Trace.UNEXPECTED_TOKEN, sToken);
                }
            } else if (sToken.equals("COLUMN")) {
                int    colindex = t.getColumnCount();
                Column column   = processCreateColumn(c, t);

                sToken = c.getString();

                if (sToken.equals("BEFORE")) {
                    sToken   = c.getName();
                    colindex = t.getColumnNr(sToken);
                } else {
                    c.back();
                }

                if (column.isIdentity() || column.isPrimaryKey()
                        || (!t.isEmpty() && column.isNullable() == false
                            && column.getDefaultString() == null)) {
                    throw Trace.error(Trace.BAD_ADD_COLUMN_DEFINITION);
                }

                session.commit();
                tw.addOrDropColumn(column, colindex, 1);

                return;
            }
        } else if (sToken.equals("DROP")) {
            sToken = c.getString();

            if (sToken.equals("CONSTRAINT")) {
                String cname = c.getName();

                session.commit();
                tw.dropConstraint(cname);

                return;
            } else if (sToken.equals("COLUMN")) {
                sToken = c.getName();

                int colindex = t.getColumnNr(sToken);

                session.commit();
                tw.addOrDropColumn(null, colindex, -1);

                return;
            } else {
                throw Trace.error(Trace.UNEXPECTED_TOKEN, sToken);
            }
        } else {
            throw Trace.error(Trace.UNEXPECTED_TOKEN, sToken);
        }
    }

    private void processAlterIndex(Tokenizer c,
                                   Session session) throws SQLException {

        String indexname = c.getName();

        c.getThis("RENAME");
        c.getThis("TO");

        String  newname  = c.getName();
        boolean isQuoted = c.wasQuotedIdentifier();
        Table   t        = findTableForIndex(indexname);

        if (t == null ||!t.equals(t.getName().name, session)) {
            throw Trace.error(Trace.INDEX_NOT_FOUND, indexname);
        }

        Table ttemp = findTableForIndex(newname);

        if (ttemp != null && ttemp.equals(ttemp.getName().name, session)) {
            throw Trace.error(Trace.INDEX_ALREADY_EXISTS, indexname);
        }

        if (HsqlName.isReservedName(indexname)) {
            throw Trace.error(Trace.SYSTEM_INDEX, indexname);
        }

        if (HsqlName.isReservedName(newname)) {
            throw Trace.error(Trace.BAD_INDEX_CONSTRAINT_NAME, indexname);
        }

        session.setScripting(!t.isTemp());
        session.commit();
        t.getIndex(indexname).setName(newname, isQuoted);
    }

// fredt@users 20020221 - patch 1.7.0 chnaged IF EXISTS syntax
// new syntax DROP TABLE tablename IF EXISTS
// fredt@users 20020221 - patch 513005 by sqlbob@users (RMP)

    /**
     *  Method declaration
     *
     * @param  c
     * @param  session
     * @return
     * @throws  SQLException
     */
    private Result processDrop(Tokenizer c,
                               Session session) throws SQLException {

        session.checkReadWrite();
        session.checkAdmin();
        session.setScripting(true);

        String sToken = c.getString();

        if (sToken.equals("TABLE") || sToken.equals("VIEW")) {
            boolean isview    = sToken.equals("VIEW");
            String  tablename = c.getString();
            boolean dropmode  = false;

            sToken = c.getString();

            if (sToken.equals("IF")) {
                c.getThis("EXISTS");

                dropmode = true;
            } else {
                c.back();

                Table t = getTable(tablename, session);

                session.setScripting(!t.isTemp());
            }

            dropTable(tablename, dropmode, isview, session);
            session.commit();
        } else if (sToken.equals("USER")) {
            aAccess.dropUser(c.getStringToken());
        } else if (sToken.equals("TRIGGER")) {
            dropTrigger(c.getString(), session);
        } else if (sToken.equals("INDEX")) {
            String indexname = c.getName();
            Table  t         = findTableForIndex(indexname);

            if (t == null ||!t.equals(t.getName().name, session)) {
                throw Trace.error(Trace.INDEX_NOT_FOUND, indexname);
            }

            t.checkDropIndex(indexname, null);

// fredt@users 20020405 - patch 1.7.0 by fredt - drop index bug
// see Table.moveDefinition();
            session.commit();
            session.setScripting(!t.isTemp());

            TableWorks tw = new TableWorks(t);

            tw.dropIndex(indexname);
        } else {
            throw Trace.error(Trace.UNEXPECTED_TOKEN, sToken);
        }

        return new Result();
    }

// fredt@users 20020221 - patch 513005 by sqlbob@users (RMP)

    /**
     *  Responsible for handling the execution of GRANT and REVOKE SQL
     *  statements.
     *
     * @param  c
     * @param  session
     * @param  grant
     * @return  Description of the Return Value
     * @throws  SQLException
     */
    private Result processGrantOrRevoke(Tokenizer c, Session session,
                                        boolean grant) throws SQLException {

        session.checkReadWrite();
        session.checkAdmin();

// fredt@users 20020221 - patch 513005 by sqlbob@users (RMP)
        session.setScripting(true);

        int    right = 0;
        String sToken;

        do {
            String sRight = c.getString();

            right  |= UserManager.getRight(sRight);
            sToken = c.getString();
        } while (sToken.equals(","));

        if (!sToken.equals("ON")) {
            throw Trace.error(Trace.UNEXPECTED_TOKEN, sToken);
        }

        String table = c.getString();

        if (table.equals("CLASS")) {

            // object is saved as 'CLASS "java.lang.Math"'
            // tables like 'CLASS "xy"' should not be created
            table += " \"" + c.getString() + "\"";
        } else {

// fredt@users 20020221 - patch 513005 by sqlbob@users (RMP)
// to make sure the table exists
            Table t = getTable(table, session);

            session.setScripting(!t.isTemp());
        }

        c.getThis("TO");

        String user = c.getStringToken();
        String command;

        if (grant) {
            aAccess.grant(user, table, right);

            command = "GRANT";
        } else {
            aAccess.revoke(user, table, right);

            command = "REVOKE";
        }

        return new Result();
    }

    /**
     *  Responsible for handling the execution CONNECT SQL statements
     *
     * @param  c
     * @param  session
     * @return
     * @throws  SQLException
     */
    private Result processConnect(Tokenizer c,
                                  Session session) throws SQLException {

        c.getThis("USER");

        String username = c.getStringToken();

        c.getThis("PASSWORD");

        String password = c.getStringToken();
        User   user     = aAccess.getUser(username, password);

        session.commit();
        session.setUser(user);

        return new Result();
    }

    /**
     *  Responsible for handling the execution DISCONNECT SQL statements
     *
     * @param  session
     * @return
     * @throws  SQLException
     */
    private Result processDisconnect(Session session) throws SQLException {

        if (!session.isClosed()) {
            session.disconnect();
            cSession.setElementAt(null, session.getId());
        }

        dropTempTables(session);

        return new Result();
    }

// fredt@users 20020221 - patch 513005 by sqlbob@users (RMP)

    /**
     *  Responsible for handling the execution SET SQL statements
     *
     * @param  c
     * @param  session
     * @return
     * @throws  SQLException
     */
    private Result processSet(Tokenizer c,
                              Session session) throws SQLException {

// fredt@users 20020221 - patch 513005 by sqlbob@users (RMP)
        session.setScripting(true);

        String sToken = c.getString();

        if (sToken.equals("PASSWORD")) {
            session.checkReadWrite();
            session.setPassword(c.getStringToken());
        } else if (sToken.equals("READONLY")) {
            session.commit();
            session.setReadOnly(processTrueOrFalse(c));
        } else if (sToken.equals("LOGSIZE")) {
            session.checkAdmin();

            int i = Integer.parseInt(c.getString());

            logger.setLogSize(i);
        } else if (sToken.equals("IGNORECASE")) {
            session.checkAdmin();

            bIgnoreCase = processTrueOrFalse(c);
        } else if (sToken.equals("MAXROWS")) {
            int i = Integer.parseInt(c.getString());

            session.setMaxRows(i);
        } else if (sToken.equals("AUTOCOMMIT")) {
            session.setAutoCommit(processTrueOrFalse(c));
        } else if (sToken.equals("TABLE")) {

// fredt@users 20020221 - patch 513005 by sqlbob@users (RMP)
// support for SET TABLE <table> READONLY [TRUE|FALSE]
// sqlbob@users 20020427 support for SET TABLE <table> SOURCE "file" [DESC]
            session.checkReadWrite();

            Table t = getTable(c.getString(), session);

            sToken = c.getString();

            session.setScripting(!t.isTemp());

            if (sToken.equals("SOURCE")) {
                if (!t.isTemp()) {
                    session.checkAdmin();
                }

                sToken = c.getString();

                if (!c.wasQuotedIdentifier()) {

                    //fredt - can replace with a better message
                    throw Trace.error(Trace.INVALID_ESCAPE);
                }

                boolean isDesc = false;

                if (c.getString().equals("DESC")) {
                    isDesc = true;
                } else {
                    c.back();
                }

                t.setDataSource(sToken, isDesc, session);
            } else if (sToken.equals("READONLY")) {
                session.checkAdmin();
                t.setDataReadOnly(processTrueOrFalse(c));
            } else if (sToken.equals("INDEX")) {
                session.checkAdmin();
                c.getString();
                t.setIndexRoots((String) c.getAsValue());
            } else {
                throw Trace.error(Trace.UNEXPECTED_TOKEN, sToken);
            }
        } else if (sToken.equals("REFERENTIAL_INTEGRITY")) {

            // fredt - no longer checking for misspelt form
            session.checkAdmin();

            bReferentialIntegrity = processTrueOrFalse(c);
        } else if (sToken.equals("WRITE_DELAY")) {
            session.checkAdmin();

            boolean delay = processTrueOrFalse(c);

            logger.setWriteDelay(delay);
        } else {
            throw Trace.error(Trace.UNEXPECTED_TOKEN, sToken);
        }

        return new Result();
    }

    /**
     *  Method declaration
     *
     * @param  c
     * @return
     * @throws  SQLException
     */
    private boolean processTrueOrFalse(Tokenizer c) throws SQLException {

        String sToken = c.getString();

        if (sToken.equals("TRUE")) {
            return true;
        } else if (sToken.equals("FALSE")) {
            return false;
        } else {
            throw Trace.error(Trace.UNEXPECTED_TOKEN, sToken);
        }
    }

    /**
     *  Responsible for handling the execution COMMIT SQL statements
     *
     * @param  c
     * @param  session
     * @return
     * @throws  SQLException
     */
    private Result processCommit(Tokenizer c,
                                 Session session) throws SQLException {

        String sToken = c.getString();

        if (!sToken.equals("WORK")) {
            c.back();
        }

        session.commit();

        return new Result();
    }

    /**
     *  Responsible for handling the execution ROLLBACK SQL statementsn
     *
     * @param  c
     * @param  session
     * @return
     * @throws  SQLException
     */
    private Result processRollback(Tokenizer c,
                                   Session session) throws SQLException {

        String sToken = c.getString();

        if (sToken.equals("TO")) {
            String sToken1 = c.getString();

            if (!sToken1.equals("SAVEPOINT")) {
                throw Trace.error(Trace.UNEXPECTED_TOKEN, sToken1);
            }

            sToken1 = c.getString();

            if (sToken1.length() == 0) {
                throw Trace.error(Trace.UNEXPECTED_TOKEN, sToken1);
            }

            session.rollbackToSavepoint(sToken1);

            return new Result();
        }

        if (!sToken.equals("WORK")) {
            c.back();
        }

        session.rollback();

        return new Result();
    }

    /**
     *  Responsible for handling the execution of SAVEPOINT SQL statements.
     *
     * @param  c Description of the Parameter
     * @param  session Description of the Parameter
     * @return  Description of the Return Value
     * @throws  SQLException
     */
    private Result processSavepoint(Tokenizer c,
                                    Session session) throws SQLException {

        String sToken = c.getString();

        if (sToken.length() == 0) {
            throw Trace.error(Trace.UNEXPECTED_TOKEN, sToken);
        }

        session.savepoint(sToken);

        return new Result();
    }

    /**
     *  Called by the garbage collector on this Databases object when garbage
     *  collection determines that there are no more references to it.
     */
    public void finalize() {

        try {
            close(-1);
        } catch (SQLException e) {    // it's too late now
        }
    }

    /**
     *  Method declaration
     *
     * @param  closemode Description of the Parameter
     * @throws  SQLException
     */
    private void close(int closemode) throws SQLException {

        logger.closeLog(closemode);

        // tony_lai@users 20020820
        // The database re-open and close has been moved from
        // Log#close(int closemode) for saving memory usage.
        // Doing so the instances of Log and other objects are no longer
        // referenced, and therefore can be garbage collected if necessary.
        if (closemode == 1) {
            open();
            logger.closeLog(0);
        }

        bShutdown = true;

        jdbcConnection.removeDatabase(this);
    }

    /**
     *  Responsible for handling the execution SHUTDOWN SQL statements
     *
     * @param  c
     * @param  session
     * @return
     * @throws  SQLException
     */
    private Result processShutdown(Tokenizer c,
                                   Session session) throws SQLException {

        if (!session.isClosed()) {
            session.checkAdmin();
        }

        int    closemode = 0;
        String token     = c.getString();

        if (token.equals("IMMEDIATELY")) {
            closemode = -1;
        } else if (token.equals("COMPACT")) {
            closemode = 1;
        } else {
            c.back();
        }

        // don't disconnect system user; need it to save database
        for (int i = 1, tsize = cSession.size(); i < tsize; i++) {
            Session d = (Session) cSession.elementAt(i);

            if (d != null) {
                d.disconnect();
            }
        }

        cSession.removeAllElements();
        close(closemode);
        processDisconnect(session);

        return new Result();
    }

    /**
     *  Responsible for handling the parse and execution of CHECKPOINT SQL
     *  statements.
     *
     * @param  session
     * @return
     * @throws  SQLException
     */
    private Result processCheckpoint(Session session) throws SQLException {

        session.checkAdmin();
        session.checkReadWrite();
        logger.checkpoint();

        return new Result();
    }

    /**
     * @param  ownerSession
     */
    private void dropTempTables(Session ownerSession) {

        for (int i = 0; i < tTable.size(); i++) {
            Table toDrop = (Table) tTable.elementAt(i);

            if (toDrop.isTemp() && toDrop.getOwnerSession() == ownerSession) {
                tTable.removeElementAt(i);
            }
        }
    }

// fredt@users 20020221 - patch 521078 by boucherb@users - DROP TABLE checks
// avoid dropping tables referenced by foreign keys - also bug 451245
// additions by fredt@users
// remove redundant constrains on tables referenced by the dropped table
// avoid dropping even with referential integrity off

    /**
     *  Drops the specified user-defined view or table from this Database
     *  object. <p>
     *
     *  The process of dropping a table or view includes:
     *  <OL>
     *    <LI> checking that the specified Session's currently connected User
     *    has the right to perform this operation and refusing to proceed if
     *    not by throwing.
     *    <LI> checking for referential constraints that conflict with this
     *    operation and refusing to proceed if they exist by throwing.</LI>
     *
     *    <LI> removing the specified Table from this Database object.
     *    <LI> removing any exported foreign keys Constraint objects held by
     *    any tables referenced by the table to be dropped. This is especially
     *    important so that the dropped Table ceases to be referenced,
     *    eventually allowing its full garbage collection.
     *    <LI>
     *  </OL>
     *  <p>
     *
     *
     *
     * @param  name of the table or view to drop
     * @param  ifExists if true and if the Table to drop does not exist, fail
     *      silently, else throw
     * @param  isView true if the name argument refers to a View
     * @param  session the connected context in which to perform this
     *      operation
     * @throws  SQLException if any of the checks listed above fail
     */
    void dropTable(String name, boolean ifExists, boolean isView,
                   Session session) throws SQLException {

        Table       toDrop            = null;
        int         dropIndex         = -1;
        int         refererIndex      = -1;
        Enumeration constraints       = null;
        Constraint  currentConstraint = null;
        Table       refTable          = null;
        boolean     isRef             = false;
        boolean     isSelfRef         = false;

        for (int i = 0; i < tTable.size(); i++) {
            toDrop = (Table) tTable.elementAt(i);

            if (toDrop.equals(name, session) && (isView == toDrop.isView())) {
                dropIndex = i;

                break;
            } else {
                toDrop = null;
            }
        }

        if (dropIndex == -1) {
            if (ifExists) {
                return;
            } else {
                throw Trace.error(isView ? Trace.VIEW_NOT_FOUND
                                         : Trace.TABLE_NOT_FOUND, name);
            }
        }

        constraints = toDrop.getConstraints().elements();

        while (constraints.hasMoreElements()) {
            currentConstraint = (Constraint) constraints.nextElement();

            if (currentConstraint.getType() != Constraint.MAIN) {
                continue;
            }

            refTable  = currentConstraint.getRef();
            isRef     = (refTable != null);
            isSelfRef = (isRef && toDrop.equals(refTable));

            if (isRef &&!isSelfRef) {

                // cover the case where the referencing table
                // may have already been dropped
                for (int k = 0; k < tTable.size(); k++) {
                    if (refTable.equals(tTable.elementAt(k))) {
                        refererIndex = k;

                        break;
                    }
                }

                if (refererIndex != -1) {

// tony_lai@users 20020820 - patch 595156
                    throw Trace.error(Trace.INTEGRITY_CONSTRAINT_VIOLATION,
                                      currentConstraint.getName().name
                                      + " table: " + refTable.getName().name);
                }
            }
        }

        if (toDrop.isText()) {
            toDrop.setDataSource("", false, session);
        }

        tTable.removeElementAt(dropIndex);
        removeExportedKeys(toDrop);
    }

    /**
     *  Removes any foreign key Constraint objects (exported keys) held by any
     *  tables referenced by the specified table. <p>
     *
     *  This method is called as the last step of a successful call to in
     *  order to ensure that the dropped Table ceases to be referenced when
     *  enforcing referential integrity.
     *
     * @param  toDrop The table to which other tables may be holding keys.
     *      This is typically a table that is in the process of being dropped.
     */
    void removeExportedKeys(Table toDrop) {

        for (int i = 0; i < tTable.size(); i++) {
            Vector constraintvector =
                ((Table) tTable.elementAt(i)).getConstraints();

            for (int j = constraintvector.size() - 1; j >= 0; j--) {
                Constraint currentConstraint =
                    (Constraint) constraintvector.elementAt(j);
                Table refTable = currentConstraint.getRef();

                if (toDrop == refTable) {
                    constraintvector.removeElementAt(j);
                }
            }
        }
    }

// fredt@users 20020221 - patch 513005 by sqlbob@users (RMP)

    /**
     *  Method declaration
     *
     * @param  name
     * @param  session
     * @throws  SQLException
     */
    private void dropTrigger(String name,
                             Session session) throws SQLException {

        boolean found = false;

        // look in each trigger list of each type of trigger for each table
        for (int i = 0, tsize = tTable.size(); i < tsize; i++) {
            Table t        = (Table) tTable.elementAt(i);
            int   numTrigs = TriggerDef.numTrigs();

            for (int tv = 0; tv < numTrigs; tv++) {
                Vector v = t.vTrigs[tv];

                for (int tr = v.size() - 1; tr >= 0; tr--) {
                    TriggerDef td = (TriggerDef) v.elementAt(tr);

                    if (td.name.equals(name)) {

// fredt@users 20020221 - patch 513005 by sqlbob@users (RMP)
                        session.setScripting(!td.table.isTemp());
                        v.removeElementAt(tr);

                        found = true;

                        if (Trace.TRACE) {
                            Trace.trace("Trigger dropped " + name);
                        }
                    }
                }
            }
        }

        Trace.check(found, Trace.TRIGGER_NOT_FOUND, name);
    }

    /**
     *  Transitional interface for log and cache management. In future, this
     *  will form the basis for the public interface of logging and cache
     *  classes.<p>
     *
     *  Implements a storage manager wrapper that provides a consitent, always
     *  available interface to storage management for the Database class,
     *  despite the fact not all Database objects actually use file storage.
     *  <p>
     *
     *  The Logger class makes it possible avoid the necessity to test for a
     *  null Log Database attribute again and again, in many different places,
     *  and generally avoids tight coupling between Database and Log, opening
     *  the doors for multiple logs/caches in the future. In this way, the
     *  Database class does not need to know the details of the Logging/Cache
     *  implementation, lowering its breakability factor and promoting
     *  long-term code flexibility.
     */
    class Logger {

        /**
         *  The Log object this Logger object wraps
         */
        private Log lLog;

        /**
         *  Opens the specified Database object's database files and starts up
         *  the logging process. <p>
         *
         *  If the specified Database object is a new database, its database
         *  files are first created.
         *
         * @param  db the Database
         * @param  sys the anonymous system Session context in which the
         *      specified Database object's logging process will operate
         * @param  name the path and common name of the database files
         * @return  true if the specified database files had to be created
         *      before being opened (i.e. a new database is created)
         * @throws  SQLException if there is a problem, such as the case when
         *      the specified files are in use by another process
         */
        boolean openLog(Database db, Session sys,
                        String name) throws SQLException {

            lLog = new Log(db, sys, name);

            boolean result = lLog.open();

            return result;
        }

// fredt@users 20020130 - patch 495484 by boucherb@users

        /**
         *  Shuts down the logging process using the specified mode. <p>
         *
         *
         *
         * @param  closemode The mode in which to shut down the logging
         *      process
         *      <OL>
         *        <LI> closemode -1 performs SHUTDOWN IMMEDIATELY, equivalent
         *        to  a poweroff or crash.
         *        <LI> closemode 0 performs a normal SHUTDOWN that
         *        checkpoints the database normally.
         *        <LI> closemode 1 performs a shutdown compact that scripts
         *        out the contents of any CACHED tables to the log then
         *        deletes the existing *.data file that contains the data
         *        for all CACHED table before the normal checkpoint process
         *        which in turn creates a new, compact *.data file.
         *      </OL>
         *
         * @throws  SQLException if there is a problem closing the Log and
         *        its dependent files.
         */
        void closeLog(int closemode) throws SQLException {

            if (lLog == null) {
                return;
            }

            lLog.stop();

            switch (closemode) {

                case -1 :
                    lLog.shutdown();
                    break;

                case 0 :
                    lLog.close(false);
                    break;

                case 1 :
                    lLog.close(true);
                    break;
            }

            lLog = null;
        }

        /**
         *  Determines if the logging process actually does anything. <p>
         *
         *  In-memory Database objects do not need to log anything. This
         *  method is essentially equivalent to testing whether this logger's
         *  database is an in-memory mode database.
         *
         * @return  true if this object encapsulates a non-null Log instance,
         *      else false
         */
        boolean hasLog() {
            return lLog != null;
        }

        /**
         *  Returns the Cache object or null if one doesn't exist.
         */
        Cache getCache() throws SQLException {

            if (lLog != null) {
                return lLog.getCache();
            } else {
                return null;
            }
        }

        /**
         *  Releases any cached data rows above the maximum set for any Cache
         *  objects existing within the context of this logger.
         *
         * @throws  SQLException if there is a problem releasing cahced data
         *      rows during the cleanup process
         */
        void cleanUp() throws SQLException {

            if (lLog != null && lLog.getCache() != null) {
                lLog.getCache().cleanUp();
            }
        }

        /**
         *  Records a Log entry representing a new connection action on the
         *  specified Session object.
         *
         * @param  session the Session object for which to record the log
         *      entry
         * @param  username the name of the User, as known to the database
         * @param  password the password of the user, as know to the database
         * @throws  SQLException if there is a problem recording the Log
         *      entry
         */
        void logConnectUser(Session session, String username,
                            String password) throws SQLException {

            if (lLog != null) {
                lLog.write(session,
                           "CONNECT USER " + username + " PASSWORD \""
                           + password + "\"");
            }
        }

        /**
         *  Records a Log entry for the specified SQL statement, on behalf of
         *  the specified Session object.
         *
         * @param  session the Session object for which to record the Log
         *      entry
         * @param  statement the SQL statement to Log
         * @throws  SQLException if there is a problem recording the entry
         */
        void writeToLog(Session session,
                        String statement) throws SQLException {

            if (lLog != null) {
                lLog.write(session, statement);
            }
        }

        /**
         *  Checkpoints the database. <p>
         *
         *  The most important effect of calling this method is to cause the
         *  log file to be rewritten in the most efficient form to
         *  reflect the current state of the database, i.e. only the DDL and
         *  insert DML required to recreate the database in its present state.
         *  Other house-keeping duties are performed w.r.t. other database
         *  files, in order to ensure as much as possible the ACID properites
         *  of the database.
         *
         * @throws  SQLException if there is a problem checkpointing the
         *      database
         */
        private void checkpoint() throws SQLException {

            if (lLog != null) {
                lLog.checkpoint();
            }
        }

        /**
         *  Sets the maximum size to which the log file can grow
         *  before being automatically checkpointed.
         *
         * @param  i The size, in MB
         */
        void setLogSize(int i) {

            if (lLog != null) {
                lLog.setLogSize(i);
            }
        }

        /**
         *  Sets the log write delay mode on or off. When write delay mode is
         *  switched on, the strategy is that executed commands are written to
         *  the log at most 1 second after they are executed. This may
         *  improve performance for applications that execute a large number
         *  of short running statements in a short period of time, but risks
         *  failing to log some possibly large number of statements in the
         *  event of a crash. When switched off, the strategy is that all SQL
         *  commands are written to the log immediately after they
         *  are executed, resulting in possibly slower execution but with the
         *  maximum risk being the loss of at most one statement in the event
         *  of a crash.
         *
         * @param  delay if true, used a delayed write strategy, else use an
         *      immediate write strategy
         */
        void setWriteDelay(boolean delay) {

            if (lLog != null) {
                lLog.setWriteDelay(delay);
            }
        }

        /**
         *  Opens the TextCache object if a Log object exists.
         */
        Cache openTextCache(String table, String source,
                            boolean readOnlyData,
                            boolean reversed) throws SQLException {
            return lLog.openTextCache(table, source, readOnlyData, reversed);
        }

        /**
         *  Closes the TextCache object if a Log object exists.
         */
        void closeTextCache(String name) throws SQLException {
            lLog.closeTextCache(name);
        }
    }
}
