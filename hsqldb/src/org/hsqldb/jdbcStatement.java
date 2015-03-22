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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.SQLWarning;

// fredt@users 20020320 - patch 1.7.0 - JDBC 2 support and error trapping
// JDBC 2 methods can now be called from jdk 1.1.x - see javadoc comments
// SCROLL_INSENSITIVE and FORWARD_ONLY types for ResultSet are now supported
// boucherb@users 20020509 - added "throws SQLException" to all methods where
// it was missing here but specified in the java.sql.Statement interface,
// updated generic documentation to JDK 1.4, and added JDBC3 methods and docs
// boucherb@users and fredt@users 20020409/20020505 extensive review and update
// of docs and behaviour to comply with previous and latest java.sql specification

/**
 * <!-- start generic documentation -->
 * The object used for executing a static SQL statement
 * and returning the results it produces.
 * <P>
 * By default, only one <code>ResultSet</code> object per <code>Statement</code>
 * object can be open at the same time. Therefore, if the reading of one
 * <code>ResultSet</code> object is interleaved
 * with the reading of another, each must have been generated by
 * different <code>Statement</code> objects. All execution methods in the
 * <code>Statement</code> interface implicitly close a statment's current
 * <code>ResultSet</code> object if an open one exists.<p>
 * <!-- end generic documentation-->
 *
 * <!-- start release-specific documentation -->
 * <span class="ReleaseSpecificDocumentation">
 * <b>HSQLDB-Specific Information:</b><p>
 *
 * <b>JRE 1.1.x Notes:</b> <p>
 *
 * In general, JDBC 2 support requires Java 1.2 and above, and JDBC3 requires
 * Java 1.4 and above. In HSQLDB, support for methods introduced in different
 * versions of JDBC depends on the JDK version used for compiling and building
 * HSQLDB.<p>
 *
 * Since 1.7.0, it is possible to build the product so that
 * all JDBC 2 methods can be called while executing under the version 1.1.x
 * <em>Java Runtime Environment</em><sup><font size="-2">TM</font></sup>.
 * However, some of these method calls require <code>int</code> values that
 * are defined only in the JDBC 2 or greater version of
 * <a href="http://java.sun.com/j2se/1.4/docs/api/java/sql/ResultSet.html">
 * <CODE>ResultSet</CODE></a> interface.  For this reason, when the
 * product is compiled under JDK 1.1.x, these values are defined in
 * {@link jdbcResultSet jdbcResultSet}.<p>
 *
 * In a JRE 1.1.x environment, calling JDBC 2 methods that take or return the
 * JDBC2-only <CODE>ResultSet</CODE> values can be achieved by referring
 * to them in parameter specifications and return value comparisons,
 * respectively, as follows: <p>
 *
 * <CODE class="JavaCodeExample">
 * jdbcResultSet.FETCH_FORWARD<br>
 * jdbcResultSet.TYPE_FORWARD_ONLY<br>
 * jdbcResultSet.TYPE_SCROLL_INSENSITIVE<br>
 * jdbcResultSet.CONCUR_READ_ONLY<br>
 * </CODE> <p>
 *
 * However, please note that code written in such a manner will not be
 * compatible for use with other JDBC 2 drivers, since they expect and use
 * <code>ResultSet</code>, rather than <code>jdbcResultSet</code>.  Also
 * note, this feature is offered solely as a convenience to developers
 * who must work under JDK 1.1.x due to operating constraints, yet wish to
 * use some of the more advanced features available under the JDBC 2
 * specification.<p>
 *
 * (fredt@users)<br>
 * (boucherb@users)<p>
 *
 * </span>
 * <!-- end release-specific documentation -->
 *
 * @see jdbcConnection#createStatement
 * @see jdbcResultSet
 */
public class jdbcStatement implements java.sql.Statement {

    /**
     * Is escape processing enabled?
     */
    private boolean bEscapeProcessing = true;

    /**
     * The connection used to execute this statement.
     */
    private jdbcConnection cConnection;

    /**
     * The maximum number of rows to generate when executing this statement.
     */
    private int iMaxRows;

    /**
     * The result of executing this statement.
     */
    private jdbcResultSet rSet;

    /**
     * The result type obtained by executing this statement.
     */
    private int rsType = jdbcResultSet.TYPE_FORWARD_ONLY;

    /**
     * <!-- start generic documentation -->
     * Executes the given SQL statement, which returns a single
     * <code>ResultSet</code> object. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * </span>
     * <!-- end release-specific documentation -->
     *
     *
     * @param sql an SQL statement to be sent to the database, typically a
     *      static SQL <code>SELECT</code> statement
     * @return a <code>ResultSet</code> object that contains the data produced
     *       by the given query; never <code>null</code>
     * @exception SQLException if a database access error occurs or the given
     *          SQL statement produces anything other than a single
     *          <code>ResultSet</code> object
     */
    public ResultSet executeQuery(String sql) throws SQLException {

        checkClosed();
        fetchResult(sql);

        return rSet;
    }

    /**
     * <!-- start generic documentation -->
     * Executes the given SQL statement, which may be an <code>INSERT</code>,
     * <code>UPDATE</code>, or <code>DELETE</code> statement or an
     * SQL statement that returns nothing, such as an SQL DDL statement. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param sql an SQL <code>INSERT</code>, <code>UPDATE</code> or
     * <code>DELETE</code> statement or an SQL statement that returns nothing
     * @return either the row count for <code>INSERT</code>, <code>UPDATE</code>
     * or <code>DELETE</code> statements, or <code>0</code> for SQL statements
     * that return nothing
     * @exception SQLException if a database access error occurs or the given
     *          SQL statement produces a <code>ResultSet</code> object
     */
    public int executeUpdate(String sql) throws SQLException {

        checkClosed();
        fetchResult(sql);

        if (rSet == null) {
            return -1;
        }

        return rSet.getUpdateCount();
    }

    /**
     * <!-- start generic documentation -->
     * Releases this <code>Statement</code> object's database
     * and JDBC resources immediately instead of waiting for
     * this to happen when it is automatically closed.
     * It is generally good practice to release resources as soon as
     * you are finished with them to avoid tying up database
     * resources.
     * <P>
     * Calling the method <code>close</code> on a <code>Statement</code>
     * object that is already closed has no effect.
     * <P>
     * <B>Note:</B> A <code>Statement</code> object is automatically closed
     * when it is garbage collected. When a <code>Statement</code> object is
     * closed, its current <code>ResultSet</code> object, if one exists, is
     * also closed. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @exception SQLException if a database access error occurs
     */
    public void close() throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        closeOldResult();

        rSet = null;
    }

    //----------------------------------------------------------------------

    /**
     * <!-- start generic documentation -->
     * Retrieves the maximum number of bytes that can be
     * returned for character and binary column values in a <code>ResultSet</code>
     * object produced by this <code>Statement</code> object.
     * This limit applies only to <code>BINARY</code>,
     * <code>VARBINARY</code>, <code>LONGVARBINARY</code>, <code>CHAR</code>,
     * <code>VARCHAR</code>, and <code>LONGVARCHAR</code>
     * columns.  If the limit is exceeded, the excess data is silently
     * discarded. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</B> <p>
     *
     * Up to and including 1.7.1, HSQLDB always returns zero, meaning there
     * is no limit. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @return the current column size limit for columns storing character and
     *       binary values; zero means there is no limit
     * @exception SQLException if a database access error occurs
     * @see #setMaxFieldSize
     */
    public int getMaxFieldSize() throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        checkClosed();

        return 0;
    }

    /**
     * <!-- start generic documentation -->
     * Sets the limit for the maximum number of bytes in a <code>ResultSet</code>
     * column storing character or binary values to
     * the given number of bytes.  This limit applies
     * only to <code>BINARY</code>, <code>VARBINARY</code>,
     * <code>LONGVARBINARY</code>, <code>CHAR</code>, <code>VARCHAR</code>, and
     * <code>LONGVARCHAR</code> fields.  If the limit is exceeded, the excess data
     * is silently discarded. For maximum portability, use values
     * greater than 256. <p>
     * <!-- emd generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * Calls to this method are simply ignored; HSQLDB always stores the
     * full number of bytes when dealing with any of the field types
     * mentioned above. These types all have an absolute maximum element upper
     * bound determined by the Java array index limit
     * java.lang.Integer.MAX_VALUE.  For XXXBINARY types, this translates to
     * Integer.MAX_VALUE bytes.  For XXXCHAR types, this translates to
     * 2 * Integer.MAX_VALUE bytes (2 bytes / character)
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param max the new column size limit in bytes; zero means there is no limit
     * @exception SQLException if a database access error occurs
     *          or the condition max >= 0 is not satisfied
     * @see #getMaxFieldSize
     */
    public void setMaxFieldSize(int max) throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        checkClosed();
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves the maximum number of rows that a
     * <code>ResultSet</code> object produced by this
     * <code>Statement</code> object can contain.  If this limit is exceeded,
     * the excess rows are silently dropped. <p>
     * <!-- start generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @return the current maximum number of rows for a <code>ResultSet</code>
     *       object produced by this <code>Statement</code> object;
     *       zero means there is no limit
     * @exception SQLException if a database access error occurs
     * @see #setMaxRows
     */
    public int getMaxRows() throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        checkClosed();

        return iMaxRows;
    }

    /**
     * <!-- start generic documentation -->
     * Sets the limit for the maximum number of rows that any
     * <code>ResultSet</code> object can contain to the given number.
     * If the limit is exceeded, the excess
     * rows are silently dropped. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param max the new max rows limit; zero means there is no limit
     * @exception SQLException if a database access error occurs
     *          or the condition max >= 0 is not satisfied
     * @see #getMaxRows
     */
    public void setMaxRows(int max) throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        checkClosed();

        iMaxRows = max;
    }

    /**
     * <!-- start generic documentation -->
     * Sets escape processing on or off.
     * If escape scanning is on (the default), the driver will do
     * escape substitution before sending the SQL statement to the database.
     *
     * Note: Since prepared statements have usually been parsed prior
     * to making this call, disabling escape processing for
     * <code>PreparedStatements</code> objects will have no effect. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * Up to HSQLDB 1.6.1, disabling escape processing for
     * <code>PreparedStatements</code> objects <i>does</i> have an effect. <p>
     *
     * HSQLDB 1.7.0 follows the standard behaviour. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param enable <code>true</code> to enable escape processing;
     *     <code>false</code> to disable it
     * @exception SQLException if a database access error occurs
     */
    public void setEscapeProcessing(boolean enable) throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        checkClosed();

        // fredt - this is a no-brainer - we just override this method in
        // jdbcPreparedStatement.java with no action performed
        //
        // FIXME:TODO ??? (not sure which category this best fits in)
        //
        // In our current implementation, setEscapeProcessing false for a
        // jdbcXXXStatement *does* actually disable escape processing for
        // prepared/callable statements.  However, the javadocs mention that
        // prepared/callable statements are usually parsed prior to making a
        // setEscapeProcessing call, so disabling escape processing for
        // prepared/callable statements will have no effect.
        //
        // The big question is:
        //
        // "Why would we want to perform nativeSQL over and over again on
        // prepared/callable statements (or even regular statements,
        // if it is redundant)?"
        //
        // This is not efficient and makes the standard comments
        // innacurate against our current implementation.  It would be pretty
        // simple to set up something to preprocess escape processing
        // for prepared/callable statements, as well as something to
        // normalize, escape-process, and cache regular statmements (although
        // normalizing and caching is probably better left completely on the
        // engine side, rather than here in client-side code).
        //
        // boucherb@users 20020425
        bEscapeProcessing = enable;
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves the number of seconds the driver will
     * wait for a <code>Statement</code> object to execute. If the
     * limit is exceeded, a <code>SQLException</code> is thrown. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</B> <p>
     *
     * Up to and including 1.7.1, HSQLDB always returns zero, meaning there
     * is no limit. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @return the current query timeout limit in seconds; zero means there is
     *       no limit
     * @exception SQLException if a database access error occurs
     * @see #setQueryTimeout
     */
    public int getQueryTimeout() throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        checkClosed();

        return 0;
    }

    /**
     * <!-- start generic documentation -->
     * Sets the number of seconds the driver will wait for a
     * <code>Statement</code> object to execute to the given number of seconds.
     * If the limit is exceeded, an <code>SQLException</code> is thrown. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * Calls to this method are simply ignored; up to and including 1.7.1,
     * HSQLDB waits an unlimited amount of time for statement execution
     * requests to return. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param seconds the new query timeout limit in seconds; zero means
     *     there is no limit
     * @exception SQLException if a database access error occurs
     *         or the condition seconds >= 0 is not satisfied
     * @see #getQueryTimeout
     */
    public void setQueryTimeout(int seconds) throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        checkClosed();
    }

    /**
     * <!-- start generic documentation -->
     * Cancels this <code>Statement</code> object if both the DBMS and
     * driver support aborting an SQL statement.
     * This method can be used by one thread to cancel a statement that
     * is being executed by another thread. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * Up to and including HSQLDB 1.7.0, aborting a SQL statement is
     * <i>not</i> supported, and calls to this method are simply ignored. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @exception SQLException if a database access error occurs
     */
    public void cancel() throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        checkClosed();
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves the first warning reported by calls on this <code>Statement</code> object.
     * Subsequent <code>Statement</code> object warnings will be chained to this
     * <code>SQLWarning</code> object.
     *
     * <p>The warning chain is automatically cleared each time
     * a statement is (re)executed. This method may not be called on a closed
     * <code>Statement</code> object; doing so will cause an <code>SQLException</code>
     * to be thrown.
     *
     * <P><B>Note:</B> If you are processing a <code>ResultSet</code> object, any
     * warnings associated with reads on that <code>ResultSet</code> object
     * will be chained on it rather than on the <code>Statement</code>
     * object that produced it. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * Up to and including 1.7.1, HSQLDB never produces warnings and
     * always returns null.<p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @return the first <code>SQLWarning</code> object or <code>null</code>
     *       if there are no warnings
     * @exception SQLException if a database access error occurs or this
     *          method is called on a closed statement
     */
    public SQLWarning getWarnings() throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        checkClosed();

        return null;
    }

    /**
     * <!-- start generic documentation -->
     * Clears all the warnings reported on this <code>Statement</code>
     * object. After a call to this method,
     * the method <code>getWarnings</code> will return
     * <code>null</code> until a new warning is reported for this
     * <code>Statement</code> object. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * Up to and including HSQLDB 1.7.0, <CODE>SQLWarning</CODE> objects are
     * never produced, and calls to this method are simply ignored. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @exception SQLException if a database access error occurs
     */
    public void clearWarnings() throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        checkClosed();
    }

    /**
     * <!-- start generic documentation -->
     * Sets the SQL cursor name to the given <code>String</code>, which
     * will be used by subsequent <code>Statement</code> object
     * <code>execute</code> methods. This name can then be
     * used in SQL positioned update or delete statements to identify the
     * current row in the <code>ResultSet</code> object generated by this
     * statement.  If the database does not support positioned update/delete,
     * this method is a noop.  To insure that a cursor has the proper isolation
     * level to support updates, the cursor's <code>SELECT</code> statement
     * should have the form <code>SELECT FOR UPDATE</code>.  If
     * <code>FOR UPDATE</code> is not present, positioned updates may fail.
     *
     * <P><B>Note:</B> By definition, the execution of positioned updates and
     * deletes must be done by a different <code>Statement</code> object than
     * the one that generated the <code>ResultSet</code> object being used for
     * positioning. Also, cursor names must be unique within a connection. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * Up to and including 1.7.1, HSQLDB does not support named cursors,
     * updateable results or table locking via
     * <code>SELECT FOR UPDATE</code>, so calls to this method are
     * simply ignored. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param name the new cursor name, which must be unique within
     *           a connection
     * @exception SQLException if a database access error occurs
     */
    public void setCursorName(String name) throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        checkClosed();
    }

    //----------------------- Multiple Results --------------------------

    /**
     * <!-- start generic documentation -->
     * Executes the given SQL statement, which may return multiple results.
     * In some (uncommon) situations, a single SQL statement may return
     * multiple result sets and/or update counts.  Normally you can ignore
     * this unless you are (1) executing a stored procedure that you know may
     * return multiple results or (2) you are dynamically executing an
     * unknown SQL string.
     * <P>
     * The <code>execute</code> method executes an SQL statement and indicates the
     * form of the first result.  You must then use the methods
     * <code>getResultSet</code> or <code>getUpdateCount</code>
     * to retrieve the result, and <code>getMoreResults</code> to
     * move to any subsequent result(s). <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param sql any SQL statement
     * @return <code>true</code> if the first result is a <code>ResultSet</code>
     *       object; <code>false</code> if it is an update count or there are
     *       no results
     * @exception SQLException if a database access error occurs
     * @see #getResultSet
     * @see #getUpdateCount
     * @see #getMoreResults
     */
    public boolean execute(String sql) throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        checkClosed();
        fetchResult(sql);

        if (rSet == null) {
            return false;
        }

        return rSet.isResult();
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves the current result as a <code>ResultSet</code> object.
     * This method should be called only once per result. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * Calling this method multiple times returns multiple references
     * to the same result, if any.
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @return the current result as a <code>ResultSet</code> object or
     * <code>null</code> if the result is an update count or there are no more results
     * @exception SQLException if a database access error occurs
     * @see #execute
     */
    public ResultSet getResultSet() throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        checkClosed();

        if ((rSet != null) && rSet.isResult()) {
            return rSet;
        }

        return null;
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves the current result as an update count;
     * if the result is a <code>ResultSet</code> object or there are no more results, -1
     * is returned. This method should be called only once per result. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @return the current result as an update count; -1 if the current result is a
     * <code>ResultSet</code> object or there are no more results
     * @exception SQLException if a database access error occurs
     * @see #execute
     */
    public int getUpdateCount() throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        checkClosed();

        if (rSet == null) {
            return -1;
        }

        return rSet.getUpdateCount();
    }

    /**
     * <!-- start generic documentation -->
     * Moves to this <code>Statement</code> object's next result, returns
     * <code>true</code> if it is a <code>ResultSet</code> object, and
     * implicitly closes any current <code>ResultSet</code>
     * object(s) obtained with the method <code>getResultSet</code>.
     *
     * <P>There are no more results when the following is true:
     * <PRE>
     *    <code>(!getMoreResults() && (getUpdateCount() == -1)</code>
     * </PRE> <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * Up to and including 1.7.1, HSQLDB does not support multiple results. <p>
     *
     * Calling this method closes the current result (if any) and always
     * returns <code>false</code>.
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @return <code>true</code> if the next result is a <code>ResultSet</code>
     *       object; <code>false</code> if it is an update count or there are
     *       no more results
     * @exception SQLException if a database access error occurs
     * @see #execute
     */
    public boolean getMoreResults() throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        checkClosed();

        if (rSet != null) {
            rSet.close();

            rSet = null;
        }

        return false;
    }

    //--------------------------JDBC 2.0-----------------------------

    /**
     * <!-- start generic documentation -->
     * Gives the driver a hint as to the direction in which
     * rows will be processed in <code>ResultSet</code>
     * objects created using this <code>Statement</code> object.  The
     * default value is <code>ResultSet.FETCH_FORWARD</code>.
     * <P>
     * Note that this method sets the default fetch direction for
     * result sets generated by this <code>Statement</code> object.
     * Each result set has its own methods for getting and setting
     * its own fetch direction. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * Up to and including 1.7.1, HSQLDB supports only
     * <code>FETCH_FORWARD</code>. <p>
     *
     * Setting any other value will throw a <CODE>SQLException</CODE>,
     * stating the the function is not supported.
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param direction the initial direction for processing rows
     * @exception SQLException if a database access error occurs
     * or the given direction
     * is not one of <code>ResultSet.FETCH_FORWARD</code>,
     * <code>ResultSet.FETCH_REVERSE</code>, or
     * <code>ResultSet.FETCH_UNKNOWN</code> <p>
     *
     * HSQLDB throws for all values except <code>FETCH_FORWARD</code>
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview
     *    for jdbcStatement)
     * @see #getFetchDirection
     */
    public void setFetchDirection(int direction) throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        checkClosed();

        if (direction != jdbcResultSet.FETCH_FORWARD) {
            throw Trace.error(Trace.FUNCTION_NOT_SUPPORTED);
        }
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves the direction for fetching rows from
     * database tables that is the default for result sets
     * generated from this <code>Statement</code> object.
     * If this <code>Statement</code> object has not set
     * a fetch direction by calling the method <code>setFetchDirection</code>,
     * the return value is implementation-specific. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</B> <p>
     *
     * Up to and including 1.7.1, HSQLDB always returns FETCH_FORWARD. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @return the default fetch direction for result sets generated
     *        from this <code>Statement</code> object
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview
     *    for jdbcStatement)
     * @see #setFetchDirection
     */
    public int getFetchDirection() throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        checkClosed();

        return jdbcResultSet.FETCH_FORWARD;
    }

    /**
     * <!-- start generic documentation -->
     * Gives the JDBC driver a hint as to the number of rows that should
     * be fetched from the database when more rows are needed.  The number
     * of rows specified affects only result sets created using this
     * statement. If the value specified is zero, then the hint is ignored.
     * The default value is zero. <p>
     * <!-- start generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * Up to and including HSQLDB 1.7.0, calls to this method are simply
     * ignored; HSQLDB always fetches a result completely as part of e
     * xecuting its statement.
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param rows the number of rows to fetch
     * @exception SQLException if a database access error occurs, or the
     *     condition 0 <= <code>rows</code> <= <code>this.getMaxRows()</code>
     *     is not satisfied. <p>
     *
     *     HSQLDB never throws an exception, since calls to this method
     *     are always ignored.
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview
     *   for jdbcStatement)
     * @see #getFetchSize
     */
    public void setFetchSize(int rows) throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        checkClosed();
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves the number of result set rows that is the default
     * fetch size for <code>ResultSet</code> objects
     * generated from this <code>Statement</code> object.
     * If this <code>Statement</code> object has not set
     * a fetch size by calling the method <code>setFetchSize</code>,
     * the return value is implementation-specific. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information</b> <p>
     *
     * Up to and including 1.7.1, this method always returns 0.
     * That is, HSQLDB always decides the fetch size, that being all the
     * rows of a result. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @return the default fetch size for result sets generated
     *      from this <code>Statement</code> object
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview
     *  for jdbcStatement)
     * @see #setFetchSize
     */
    public int getFetchSize() throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        checkClosed();

        // FIXME: fredt - changed
        // setFetchSize suggests that the attribute is a hint and
        // that zero is the "magic number" that lets the driver decide for
        // itself what the best fetch is.  As such, we should return zero
        // here, since we always ignore setFetchSize and simply fetch the
        // result entirely.
        // boucherb@users 20020425
        return 0;
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves the result set concurrency for <code>ResultSet</code> objects
     * generated by this <code>Statement</code> object. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * Up to and including 1.7.1, HSQLDB supports only
     * <code>CONCUR_READ_ONLY</code> concurrency. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @return either <code>ResultSet.CONCUR_READ_ONLY</code> or
     * <code>ResultSet.CONCUR_UPDATABLE</code> (not supported)
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview
     *  for jdbcStatement)
     */
    public int getResultSetConcurrency() throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        checkClosed();

        return jdbcResultSet.CONCUR_READ_ONLY;
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves the result set type for <code>ResultSet</code> objects
     * generated by this <code>Statement</code> object. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * Up to 1.6.1, HSQLDB supported <code>TYPE_FORWARD_ONLY</code> -
     * <code>CONCUR_READ_ONLY</code> results only, so <code>ResultSet</code>
     * objects created using the returned <code>Statement</code>
     * object would <I>always</I> be type <code>TYPE_FORWARD_ONLY</code>
     * with <code>CONCUR_READ_ONLY</code> concurrency. <p>
     *
     * Starting with 1.7.0, HSQLDB also supports
     * <code>TYPE_SCROLL_INSENSITIVE</code> results.<p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @return one of <code>ResultSet.TYPE_FORWARD_ONLY</code>,
     * <code>ResultSet.TYPE_SCROLL_INSENSITIVE</code>, or
     * <code>ResultSet.TYPE_SCROLL_SENSITIVE</code> (not supported) <p>
     *
     * <b>Note:</b> Up to and including 1.7.1, HSQLDB never returns
     * <code>TYPE_SCROLL_SENSITIVE</code>
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview
     *   for jdbcStatement)
     */
    public int getResultSetType() throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        checkClosed();

        return rsType;
    }

    /**
     * <!-- start generic documentation -->
     * Adds the given SQL command to the current list of commmands for this
     * <code>Statement</code> object. The commands in this list can be
     * executed as a batch by calling the method <code>executeBatch</code>.
     * <P>
     * <B>NOTE:</B>  This method is optional. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * HSQLDB 1.7.0 does not support this feature. <p>
     *
     * Calling this method always throws a <CODE>SQLException</CODE>,
     * stating that the function is not supported. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param sql typically this is a static SQL <code>INSERT</code> or
     * <code>UPDATE</code> statement
     * @exception SQLException if a database access error occurs, or the
     * driver does not support batch updates
     * @see #executeBatch
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview
     *   for jdbcStatement)
     */
    public void addBatch(String sql) throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        throw Trace.error(Trace.FUNCTION_NOT_SUPPORTED);
    }

    /**
     * <!-- start generic documentation -->
     * Empties this <code>Statement</code> object's current list of
     * SQL commands.
     * <P>
     * <B>NOTE:</B>  This method is optional. <p>
     * <!-- start generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * HSQLDB 1.7.0 does not support this feature. <p>
     *
     * Calling this method always throws a <CODE>SQLException</CODE>,
     * stating that the function is not supported. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @exception SQLException if a database access error occurs or the
     * driver does not support batch updates
     * @see #addBatch
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview
     *   for jdbcStatement)
     */
    public void clearBatch() throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        throw Trace.error(Trace.FUNCTION_NOT_SUPPORTED);
    }

    /**
     * <!-- start generic documentation -->
     * Submits a batch of commands to the database for execution and
     * if all commands execute successfully, returns an array of update counts.
     * The <code>int</code> elements of the array that is returned are ordered
     * to correspond to the commands in the batch, which are ordered
     * according to the order in which they were added to the batch.
     * The elements in the array returned by the method <code>executeBatch</code>
     * may be one of the following:
     * <OL>
     * <LI>A number greater than or equal to zero -- indicates that the
     * command was processed successfully and is an update count giving the
     * number of rows in the database that were affected by the command's
     * execution
     * <LI>A value of <code>SUCCESS_NO_INFO</code> -- indicates that the command was
     * processed successfully but that the number of rows affected is
     * unknown
     * <P>
     * If one of the commands in a batch update fails to execute properly,
     * this method throws a <code>BatchUpdateException</code>, and a JDBC
     * driver may or may not continue to process the remaining commands in
     * the batch.  However, the driver's behavior must be consistent with a
     * particular DBMS, either always continuing to process commands or never
     * continuing to process commands.  If the driver continues processing
     * after a failure, the array returned by the method
     * <code>BatchUpdateException.getUpdateCounts</code>
     * will contain as many elements as there are commands in the batch, and
     * at least one of the elements will be the following:
     * <P>
     * <LI>A value of <code>EXECUTE_FAILED</code> -- indicates that the command failed
     * to execute successfully and occurs only if a driver continues to
     * process commands after a command fails
     * </OL>
     * <P>
     * A driver is not required to implement this method.
     * The possible implementations and return values have been modified in
     * the Java 2 SDK, Standard Edition, version 1.3 to
     * accommodate the option of continuing to proccess commands in a batch
     * update after a <code>BatchUpdateException</code> obejct has been thrown. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * HSQLDB 1.7.0 does not support this feature. <p>
     *
     * Calling this method always throws a <CODE>SQLException</CODE>,
     * stating that the function is not supported. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @return an array of update counts containing one element for each
     * command in the batch.  The elements of the array are ordered according
     * to the order in which commands were added to the batch.
     * @exception SQLException if a database access error occurs or the
     * driver does not support batch statements. Throws
     * {@link java.sql.BatchUpdateException}
     * (a subclass of <code>java.sql.SQLException</code>) if one of the commands
     * sent  to the database fails to execute properly or attempts to return a
     * result set.
     * @since JDK 1.3 (JDK 1.1.x developers: read the new overview
     *   for jdbcStatement)
     */
    public int[] executeBatch() throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        checkClosed();

        throw Trace.error(Trace.FUNCTION_NOT_SUPPORTED);
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves the <code>Connection</code> object
     * that produced this <code>Statement</code> object. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * </span>
     * <!-- end release-specific documentation -->
     * @return the connection that produced this statement
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview
     *    for jdbcStatement)
     */
    public Connection getConnection() throws SQLException {

        checkClosed();

        if (Trace.TRACE) {
            Trace.trace();
        }

        return cConnection;
    }

    //--------------------------JDBC 3.0-----------------------------

    /**
     * <!-- start generic documentation -->
     * Moves to this <code>Statement</code> object's next result, deals with
     * any current <code>ResultSet</code> object(s) according  to the instructions
     * specified by the given flag, and returns
     * <code>true</code> if the next result is a <code>ResultSet</code> object.
     *
     * <P>There are no more results when the following is true:
     * <PRE>
     *   <code>(!getMoreResults() && (getUpdateCount() == -1)</code>
     * </PRE> <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * HSQLDB 1.7.0 does not support this feature. <p>
     *
     * Calling this method always throws a <CODE>SQLException</CODE>,
     * stating that the function is not supported. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param current one of the following <code>Statement</code>
     *     constants indicating what should happen to current
     *     <code>ResultSet</code> objects obtained using the method
     *     <code>getResultSet</code:
     *     <code>CLOSE_CURRENT_RESULT</code>,
     *     <code>KEEP_CURRENT_RESULT</code>, or
     *     <code>CLOSE_ALL_RESULTS</code>
     * @return <code>true</code> if the next result is a <code>ResultSet</code>
     *      object; <code>false</code> if it is an update count or there are no
     *      more results
     * @exception SQLException if a database access error occurs
     * @since JDK 1.4, HSQLDB 1.7
     * @see #execute
     */
//#ifdef JDBC3
/*
    public boolean getMoreResults(int current) throws SQLException {
        throw Trace.error(Trace.FUNCTION_NOT_SUPPORTED, "JDBC3");
    }


*/

//#endif JDBC3

    /**
     * <!-- start generic documentation -->
     * Retrieves any auto-generated keys created as a result of executing this
     * <code>Statement</code> object. If this <code>Statement</code> object did
     * not generate any keys, an empty <code>ResultSet</code>
     * object is returned. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * HSQLDB 1.7.0 does not support this feature. <p>
     *
     * Calling this method always throws a <CODE>SQLException</CODE>,
     * stating that the function is not supported. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @return a <code>ResultSet</code> object containing the auto-generated key(s)
     *     generated by the execution of this <code>Statement</code> object
     * @exception SQLException if a database access error occurs
     * @since JDK 1.4, HSQLDB 1.7
     */
//#ifdef JDBC3
/*
    public ResultSet getGeneratedKeys() throws SQLException {
        throw Trace.error(Trace.FUNCTION_NOT_SUPPORTED, "JDBC3");
    }


*/

//#endif JDBC3

    /**
     * <!-- start generic documentation -->
     * Executes the given SQL statement and signals the driver with the
     * given flag about whether the
     * auto-generated keys produced by this <code>Statement</code> object
     * should be made available for retrieval. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * HSQLDB 1.7.0 does not support this feature. <p>
     *
     * Calling this method always throws a <CODE>SQLException</CODE>,
     * stating that the function is not supported. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param sql must be an SQL <code>INSERT</code>, <code>UPDATE</code> or
     *    <code>DELETE</code> statement or an SQL statement that
     *    returns nothing
     * @param autoGeneratedKeys a flag indicating whether auto-generated keys
     *    should be made available for retrieval;
     *     one of the following constants:
     *     <code>Statement.RETURN_GENERATED_KEYS</code>
     *     <code>Statement.NO_GENERATED_KEYS</code>
     * @return either the row count for <code>INSERT</code>, <code>UPDATE</code>
     *     or <code>DELETE</code> statements, or <code>0</code> for SQL
     *     statements that return nothing
     * @exception SQLException if a database access error occurs, the given
     *        SQL statement returns a <code>ResultSet</code> object, or
     *        the given constant is not one of those allowed
     * @since JDK 1.4, HSQLDB 1.7
     */
//#ifdef JDBC3
/*
    public int executeUpdate(String sql,
                             int autoGeneratedKeys) throws SQLException {
        throw Trace.error(Trace.FUNCTION_NOT_SUPPORTED, "JDBC3");
    }


*/

//#endif JDBC3

    /**
     * <!-- start generic documentation -->
     * Executes the given SQL statement and signals the driver that the
     * auto-generated keys indicated in the given array should be made available
     * for retrieval.  The driver will ignore the array if the SQL statement
     * is not an <code>INSERT</code> statement. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * HSQLDB 1.7.0 does not support this feature. <p>
     *
     * Calling this method always throws a <CODE>SQLException</CODE>,
     * stating that the function is not supported. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param sql an SQL <code>INSERT</code>, <code>UPDATE</code> or
     *    <code>DELETE</code> statement or an SQL statement that returns nothing,
     *    such as an SQL DDL statement
     * @param columnIndexes an array of column indexes indicating the columns
     *    that should be returned from the inserted row
     * @return either the row count for <code>INSERT</code>, <code>UPDATE</code>,
     *     or <code>DELETE</code> statements, or 0 for SQL statements
     *     that return nothing
     * @exception SQLException if a database access error occurs or the SQL
     *        statement returns a <code>ResultSet</code> object
     * @since JDK 1.4, HSQLDB 1.7
     */
//#ifdef JDBC3
/*
    public int executeUpdate(String sql,
                             int columnIndexes[]) throws SQLException {
        throw Trace.error(Trace.FUNCTION_NOT_SUPPORTED, "JDBC3");
    }


*/

//#endif JDBC3

    /**
     * <!-- start generic documentation -->
     * Executes the given SQL statement and signals the driver that the
     * auto-generated keys indicated in the given array should be made available
     * for retrieval.  The driver will ignore the array if the SQL statement
     * is not an <code>INSERT</code> statement. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * HSQLDB 1.7.0 does not support this feature. <p>
     *
     * Calling this method always throws a <CODE>SQLException</CODE>,
     * stating that the function is not supported. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param sql an SQL <code>INSERT</code>, <code>UPDATE</code> or
     *    <code>DELETE</code> statement or an SQL statement that returns nothing
     * @param columnNames an array of the names of the columns that should be
     *    returned from the inserted row
     * @return either the row count for <code>INSERT</code>, <code>UPDATE</code>,
     *     or <code>DELETE</code> statements, or 0 for SQL statements
     *     that return nothing
     * @exception SQLException if a database access error occurs
     * @since JDK 1.4, HSQLDB 1.7
     */
//#ifdef JDBC3
/*
    public int executeUpdate(String sql,
                             String columnNames[]) throws SQLException {
        throw Trace.error(Trace.FUNCTION_NOT_SUPPORTED, "JDBC3");
    }


*/

//#endif JDBC3

    /**
     * <!-- start generic documentation -->
     * Executes the given SQL statement, which may return multiple results,
     * and signals the driver that any
     * auto-generated keys should be made available
     * for retrieval.  The driver will ignore this signal if the SQL statement
     * is not an <code>INSERT</code> statement.
     * <P>
     * In some (uncommon) situations, a single SQL statement may return
     * multiple result sets and/or update counts.  Normally you can ignore
     * this unless you are (1) executing a stored procedure that you know may
     * return multiple results or (2) you are dynamically executing an
     * unknown SQL string.
     * <P>
     * The <code>execute</code> method executes an SQL statement and indicates the
     * form of the first result.  You must then use the methods
     * <code>getResultSet</code> or <code>getUpdateCount</code>
     * to retrieve the result, and <code>getMoreResults</code> to
     * move to any subsequent result(s). <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * HSQLDB 1.7.0 does not support this feature. <p>
     *
     * Calling this method always throws a <CODE>SQLException</CODE>,
     * stating that the function is not supported. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param sql any SQL statement
     * @param autoGeneratedKeys a constant indicating whether auto-generated
     *    keys should be made available for retrieval using the method
     *    <code>getGeneratedKeys</code>; one of the following constants:
     *    <code>Statement.RETURN_GENERATED_KEYS</code> or
     *      <code>Statement.NO_GENERATED_KEYS</code>
     * @return <code>true</code> if the first result is a <code>ResultSet</code>
     *     object; <code>false</code> if it is an update count or there are
     *     no results
     * @exception SQLException if a database access error occurs
     * @see #getResultSet
     * @see #getUpdateCount
     * @see #getMoreResults
     * @see #getGeneratedKeys
     * @since JDK 1.4, HSQLDB 1.7
     */
//#ifdef JDBC3
/*
    public boolean execute(String sql,
                           int autoGeneratedKeys) throws SQLException {
        throw Trace.error(Trace.FUNCTION_NOT_SUPPORTED, "JDBC3");
    }


*/

//#endif JDBC3

    /**
     * <!-- start generic documentation -->
     * Executes the given SQL statement, which may return multiple results,
     * and signals the driver that the
     * auto-generated keys indicated in the given array should be made available
     * for retrieval.  This array contains the indexes of the columns in the
     * target table that contain the auto-generated keys that should be made
     * available. The driver will ignore the array if the given SQL statement
     * is not an <code>INSERT</code> statement.
     * <P>
     * Under some (uncommon) situations, a single SQL statement may return
     * multiple result sets and/or update counts.  Normally you can ignore
     * this unless you are (1) executing a stored procedure that you know may
     * return multiple results or (2) you are dynamically executing an
     * unknown SQL string.
     * <P>
     * The <code>execute</code> method executes an SQL statement and indicates the
     * form of the first result.  You must then use the methods
     * <code>getResultSet</code> or <code>getUpdateCount</code>
     * to retrieve the result, and <code>getMoreResults</code> to
     * move to any subsequent result(s). <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * HSQLDB 1.7.0 does not support this feature. <p>
     *
     * Calling this method always throws a <CODE>SQLException</CODE>,
     * stating that the function is not supported. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param sql any SQL statement
     * @param columnIndexes an array of the indexes of the columns in the
     *    inserted row that should be  made available for retrieval by a
     *    call to the method <code>getGeneratedKeys</code>
     * @return <code>true</code> if the first result is a <code>ResultSet</code>
     *     object; <code>false</code> if it is an update count or there
     *     are no results
     * @exception SQLException if a database access error occurs
     * @see #getResultSet
     * @see #getUpdateCount
     * @see #getMoreResults
     * @since JDK 1.4, HSQLDB 1.7
     */
//#ifdef JDBC3
/*
    public boolean execute(String sql,
                           int columnIndexes[]) throws SQLException {
        throw Trace.error(Trace.FUNCTION_NOT_SUPPORTED, "JDBC3");
    }


*/

//#endif JDBC3

    /**
     * <!-- start generic documentation -->
     * Executes the given SQL statement, which may return multiple results,
     * and signals the driver that the
     * auto-generated keys indicated in the given array should be made available
     * for retrieval. This array contains the names of the columns in the
     * target table that contain the auto-generated keys that should be made
     * available. The driver will ignore the array if the given SQL statement
     * is not an <code>INSERT</code> statement.
     * <P>
     * In some (uncommon) situations, a single SQL statement may return
     * multiple result sets and/or update counts.  Normally you can ignore
     * this unless you are (1) executing a stored procedure that you know may
     * return multiple results or (2) you are dynamically executing an
     * unknown SQL string.
     * <P>
     * The <code>execute</code> method executes an SQL statement and indicates the
     * form of the first result.  You must then use the methods
     * <code>getResultSet</code> or <code>getUpdateCount</code>
     * to retrieve the result, and <code>getMoreResults</code> to
     * move to any subsequent result(s). <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * HSQLDB 1.7.0 does not support this feature. <p>
     *
     * Calling this method always throws a <CODE>SQLException</CODE>,
     * stating that the function is not supported. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param sql any SQL statement
     * @param columnNames an array of the names of the columns in the inserted
     *    row that should be made available for retrieval by a call to the
     *    method <code>getGeneratedKeys</code>
     * @return <code>true</code> if the next result is a <code>ResultSet</code>
     *     object; <code>false</code> if it is an update count or there
     *     are no more results
     * @exception SQLException if a database access error occurs
     * @see #getResultSet
     * @see #getUpdateCount
     * @see #getMoreResults
     * @see #getGeneratedKeys
     * @since JDK 1.4, HSQLDB 1.7
     */
//#ifdef JDBC3
/*
    public boolean execute(String sql,
                           String columnNames[]) throws SQLException {
        throw Trace.error(Trace.FUNCTION_NOT_SUPPORTED, "JDBC3");
    }


*/

//#endif JDBC3

    /**
     * <!-- start generic documentation -->
     * Retrieves the result set holdability for <code>ResultSet</code> objects
     * generated by this <code>Statement</code> object. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * HSQLDB 1.7.0 does not support this feature. <p>
     *
     * Calling this method always throws a <CODE>SQLException</CODE>,
     * stating that the function is not supported. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @return either <code>ResultSet.HOLD_CURSORS_OVER_COMMIT</code> or
     *      <code>ResultSet.CLOSE_CURSORS_AT_COMMIT</code>
     * @exception SQLException if a database access error occurs
     * @since JDK 1.4, HSQLDB 1.7
     */
//#ifdef JDBC3
/*
    public int getResultSetHoldability() throws SQLException {
        throw Trace.error(Trace.FUNCTION_NOT_SUPPORTED, "JDBC3");
    }


*/

//#endif JDBC3
    // -------------------- Internal Implementation ----------------------------

    /**
     * Constructs a new jdbcStatement using the specified connection.
     *
     * @param c the connection on which this statement will execute
     */
    jdbcStatement(jdbcConnection c) {

        // FIXME: fredt - no it is checkd in Connection.getStatement
        // uhh.. check for null?  Yes: package private does not mean
        // carte blanche.  We are only human too, so why not use
        // the standard best practices?  This must never be null,
        // so why let it pass here. Perhaps this constructor s
        // hould really throw a SQLException
        // boucherb@users 20020425
        cConnection = c;
        rsType      = jdbcResultSet.TYPE_FORWARD_ONLY;
    }

    /**
     * Constructs a new jdbcStatement with the specified connection and
     * result type.
     *
     * @param  c the connection on which this statement will execute
     * @param  type the kind of results this will return
     */
    jdbcStatement(jdbcConnection c, int type) {

        // FIXME:
        // better form is:
        //
        this(c);

        rsType = type;

        //
        // that way, code changes need not be duplicated in both constructors
        // boucherb@users 20020425
//                cConnection       = c;
        // FIXME: fredt - no it is checked and in Connection.getStatement()
        // check this is correct first.  Perhaps this constructor
        // should really throw a SQLException?
        // boucherb@users 20020425
    }

    /**
     * An internal check for closed connections.
     *
     * @throws SQLException when the connection is closed
     */
    void checkClosed() throws SQLException {
        cConnection.checkClosed();
    }

    /**
     * Closes the current result, if any.
     * @throws SQLException when a database access error occurs
     */
    private void closeOldResult() throws SQLException {

        // this is necessary to conform the JDBC standard
        if (rSet != null) {
            rSet.close();

            rSet = null;
        }
    }

    /**
     * The internal result producer. <p>
     *
     * All jdbcXXXStatement objects eventually go through this method to
     * get results. This method is reponsible for closing the current
     * result, if any, translating the SQL the statement represents (if
     * escape processing is enabled), limiting results to the maximum number
     * set with setMaxRows, and actually submitting the query
     * to the engine and retrieving the corresponding result.
     *
     * @param sql the SQL to be executed
     * @throws SQLException when a database access error occurs
     */
    private void fetchResult(String sql) throws SQLException {

        if (bEscapeProcessing) {
            sql = cConnection.nativeSQL(sql);
        }

        closeOldResult();

        if (iMaxRows == 0) {
            rSet = cConnection.execute(sql);
        } else {
            try {
                sql  = "SET MAXROWS " + iMaxRows + ";" + sql;
                rSet = cConnection.execute(sql);

                cConnection.execute("SET MAXROWS 0");
            } catch (SQLException e) {
                cConnection.execute("SET MAXROWS 0");

                throw e;
            }
        }

        if (rSet != null) {
            rSet.sqlStatement = this;
            rSet.rsType       = rsType;
        }
    }
}
