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
import java.util.Hashtable;
import java.util.Vector;

// fredt@users 20020320 - doc 1.7.0 - update
// fredt@users 20020315 - patch 1.7.0 by fredt - switch for scripting
// fredt@users 20020130 - patch 476694 by velichko - transaction savepoints
// additions in different parts to support savepoint transactions
// fredt@users 20020910 - patch 1.7.1 by fredt - database readonly enforcement
// fredt@users 20020912 - patch 1.7.1 by fredt - permanent internal connection

/**
 *  Implementation of a user session with the database.
 *
 * @version  1.7.0
 */
class Session {

    private Database       dDatabase;
    private User           uUser;
    private Vector         tTransaction;
    private boolean        bAutoCommit;
    private boolean        bNestedTransaction;
    private boolean        bNestedOldAutoCommit;
    private int            iNestedOldTransIndex;
    private boolean        bReadOnly;
    private int            iMaxRows;
    private int            iLastIdentity;
    private boolean        bClosed;
    private int            iId;
    private Hashtable      hSavepoints;
    private boolean        script;
    private jdbcConnection intConnection;

    /**
     *  closes the session.
     *
     * @throws  SQLException
     */
    public void finalize() throws SQLException {
        disconnect();
    }

    /**
     *  Constructor declaration
     *
     * @param  c
     * @param  id
     */
    Session(Session c, int id) {
        this(c.dDatabase, c.uUser, true, c.bReadOnly, id);
    }

    /**
     *  Constructor declaration
     *
     * @param  db
     * @param  user
     * @param  autocommit
     * @param  readonly
     * @param  id
     */
    Session(Database db, User user, boolean autocommit, boolean readonly,
            int id) {

        iId          = id;
        dDatabase    = db;
        uUser        = user;
        tTransaction = new Vector();
        bAutoCommit  = autocommit;
        bReadOnly    = db.bReadOnly || readonly;
        hSavepoints  = new Hashtable();
    }

    /**
     *  Method declaration
     *
     * @return
     */
    int getId() {
        return iId;
    }

    /**
     *  Method declaration
     *
     * @throws  SQLException
     */
    void disconnect() throws SQLException {

        if (bClosed) {
            return;
        }

        rollback();

        dDatabase     = null;
        uUser         = null;
        tTransaction  = null;
        hSavepoints   = null;
        intConnection = null;
        bClosed       = true;
    }

    /**
     *  Method declaration
     *
     * @return
     */
    boolean isClosed() {
        return bClosed;
    }

    /**
     *  Method declaration
     *
     * @param  i
     */
    void setLastIdentity(int i) {
        iLastIdentity = i;
    }

    /**
     *  Method declaration
     *
     * @return
     */
    int getLastIdentity() {
        return iLastIdentity;
    }

    /**
     *  Method declaration
     *
     * @return
     */
    Database getDatabase() {
        return dDatabase;
    }

    /**
     *  Method declaration
     *
     * @return
     */
    String getUsername() {
        return uUser.getName();
    }

    /**
     *  Method declaration
     *
     * @param  user
     */
    void setUser(User user) {
        uUser = user;
    }

    /**
     *  Method declaration
     *
     * @throws  SQLException
     */
    void checkAdmin() throws SQLException {
        uUser.checkAdmin();
    }

    /**
     *  Method declaration
     *
     * @param  object
     * @param  right
     * @throws  SQLException
     */
    void check(String object, int right) throws SQLException {
        uUser.check(object, right);
    }

    /**
     *  Method declaration
     *
     * @throws  SQLException
     */
    void checkReadWrite() throws SQLException {
        Trace.check(!bReadOnly, Trace.DATABASE_IS_READONLY);
    }

    /**
     *  Method declaration
     *
     * @param  s
     */
    void setPassword(String s) {
        uUser.setPassword(s);
    }

    /**
     *  Method declaration
     *
     * @param  table
     * @param  row
     * @throws  SQLException
     */
    void addTransactionDelete(Table table, Object row[]) throws SQLException {

        if (!bAutoCommit) {
            Transaction t = new Transaction(true, table, row);

            tTransaction.addElement(t);
        }
    }

    /**
     *  Method declaration
     *
     * @param  table
     * @param  row
     * @throws  SQLException
     */
    void addTransactionInsert(Table table, Object row[]) throws SQLException {

        if (!bAutoCommit) {
            Transaction t = new Transaction(false, table, row);

            tTransaction.addElement(t);
        }
    }

    /**
     *  Method declaration
     *
     * @param  autocommit
     * @throws  SQLException
     */
    void setAutoCommit(boolean autocommit) throws SQLException {

        commit();

        bAutoCommit = autocommit;
    }

    /**
     *  Method declaration
     *
     * @throws  SQLException
     */
    void commit() throws SQLException {
        tTransaction.removeAllElements();
        hSavepoints.clear();
    }

    /**
     *  Method declaration
     *
     * @throws  SQLException
     */
    void rollback() throws SQLException {

        for (int i = tTransaction.size() - 1; i >= 0; i--) {
            Transaction t = (Transaction) tTransaction.elementAt(i);

            t.rollback();
        }

        tTransaction.removeAllElements();
        hSavepoints.clear();
    }

    /**
     *  Implements a transaction SAVEPOIINT. Application may do a partial
     *  rollback by calling rollbackToSavepoint()
     *
     * @param  name Name of savepoint
     * @throws  SQLException
     */
    void savepoint(String name) throws SQLException {
        hSavepoints.put(name, new Integer(tTransaction.size()));
    }

    /**
     *  Implements a partial transaction ROLLBACK.
     *
     * @param  name Name of savepoint that was marked before by savepoint()
     * call
     * @throws  SQLException
     */
    void rollbackToSavepoint(String name) throws SQLException {

        Integer idx = (Integer) hSavepoints.get(name);

        Trace.check(idx != null, Trace.SAVEPOINT_NOT_FOUND, name);

        for (int i = tTransaction.size() - 1; i >= idx.intValue(); i--) {
            Transaction t = (Transaction) tTransaction.elementAt(i);

            t.rollback();
            tTransaction.removeElementAt(i);
        }

        hSavepoints.remove(name);
    }

    /**
     *  Method declaration
     *
     * @throws  SQLException
     */
    void beginNestedTransaction() throws SQLException {

        Trace.doAssert(!bNestedTransaction, "beginNestedTransaction");

        bNestedOldAutoCommit = bAutoCommit;

        // now all transactions are logged
        bAutoCommit          = false;
        iNestedOldTransIndex = tTransaction.size();
        bNestedTransaction   = true;
    }

    /**
     *  Method declaration
     *
     * @param  rollback
     * @throws  SQLException
     */
    void endNestedTransaction(boolean rollback) throws SQLException {

        Trace.doAssert(bNestedTransaction, "endNestedTransaction");

        if (rollback) {
            for (int i = tTransaction.size() - 1; i >= iNestedOldTransIndex;
                    i--) {
                Transaction t = (Transaction) tTransaction.elementAt(i);

                t.rollback();
            }
        }

        bNestedTransaction = false;
        bAutoCommit        = bNestedOldAutoCommit;

        if (bAutoCommit == true) {
            tTransaction.setSize(iNestedOldTransIndex);
        }
    }

    /**
     *  Method declaration
     *
     * @param  readonly
     */
    void setReadOnly(boolean readonly) {
        bReadOnly = readonly;
    }

    /**
     *  Method declaration
     *
     * @return
     */
    boolean isReadOnly() {
        return bReadOnly || dDatabase.bReadOnly;;
    }

    /**
     *  Method declaration
     *
     * @param  max
     */
    void setMaxRows(int max) {
        iMaxRows = max;
    }

    /**
     *  Method declaration
     *
     * @return
     */
    int getMaxRows() {
        return iMaxRows;
    }

    /**
     *  Method declaration
     *
     * @return
     */
    boolean isNestedTransaction() {
        return bNestedTransaction;
    }

    /**
     *  Method declaration
     *
     * @return
     */
    boolean getAutoCommit() {
        return bAutoCommit;
    }

    /**
     *  A switch to set scripting on the basis of type of statement executed.
     *  A method in Database.jave sets this value to false before other
     *  methods are called to act on an SQL statement, which may set this to
     *  true. Afterwards the method reponsible for logging uses
     *  getScripting() to determine if logging is required for the executed
     *  statement. (fredt@users)
     *
     * @param  script The new scripting value
     */
    void setScripting(boolean script) {
        this.script = script;
    }

    /**
     * @return  scripting for the last statement.
     */
    boolean getScripting() {
        return script;
    }

    /**
     * @return  scripting for the last statement.
     */
    jdbcConnection getInternalConnection() throws SQLException {

        if (intConnection == null) {
            intConnection = new jdbcConnection(this);
        }

        return intConnection;
    }
}
