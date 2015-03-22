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
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

// fredt@users 20020320 - patch 1.7.0 - JDBC 2 support and error trapping
// JDBC 2 methods can now be called from jdk 1.1.x - see javadoc comments
// boucherb@users 20020509 - added "throws SQLException" to all methods where
// it was missing here but specified in the java.sql.DatabaseMetaData interface,
// updated generic documentation to JDK 1.4, and added JDBC3 methods and docs
// boucherb@users and fredt@users 20020409/20020505 extensive review and update
// of docs and behaviour to comply with previous and latest java.sql specification

/**
 * Comprehensive information about the database as a whole.
 * <P>
 * This interface is implemented by driver vendors to let users know the capabilities
 * of a Database Management System (DBMS) in combination with
 * the driver based on JDBC<sup><font size=-2>TM</font></sup> technology
 * ("JDBC driver") that is used with it.  Different relational DBMSs often support
 * different features, implement features in different ways, and use different
 * data types.  In addition, a driver may implement a feature on top of what the
 * DBMS offers.  Information returned by methods in this interface applies
 * to the capabilities of a particular driver and a particular DBMS working
 * together. Note that as used in this documentation, the term "database" is
 * used generically to refer to both the driver and DBMS.
 * <P>
 * A user for this interface is commonly a tool that needs to discover how to
 * deal with the underlying DBMS.  This is especially true for applications
 * that are intended to be used with more than one DBMS. For example, a tool might use the method
 * <code>getTypeInfo</code> to find out what data types can be used in a
 * <code>CREATE TABLE</code> statement.  Or a user might call the method
 * <code>supportsCorrelatedSubqueries</code> to see if it is possible to use
 * a correlated subquery or <code>supportsBatchUpdates</code> to see if it is
 * possible to use batch updates.
 * <P>
 * Some <code>DatabaseMetaData</code> methods return lists of information
 * in the form of <code>ResultSet</code> objects.
 * Regular <code>ResultSet</code> methods, such as
 * <code>getString</code> and <code>getInt</code>, can be used
 * to retrieve the data from these <code>ResultSet</code> objects.  If
 * a given form of metadata is not available, the <code>ResultSet</code>
 * getter methods throw an <code>SQLException</code>.
 * <P>
 * Some <code>DatabaseMetaData</code> methods take arguments that are
 * String patterns.  These arguments all have names such as fooPattern.
 * Within a pattern String, "%" means match any substring of 0 or more
 * characters, and "_" means match any one character. Only metadata
 * entries matching the search pattern are returned. If a search pattern
 * argument is set to <code>null</code>, that argument's criterion will
 * be dropped from the search.
 * <P>
 * A method that gets information about a feature that the driver does not
 * support will throw an <code>SQLException</code>.
 * In the case of methods that return a <code>ResultSet</code>
 * object, either a <code>ResultSet</code> object (which may be empty) is
 * returned or an <code>SQLException</code> is thrown.<p>
 *
 * <!-- start release-specific documentation -->
 * <span class="ReleaseSpecificDocumentation">
 * <b>HSQLDB-Specific Information:</b> <p>
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
 * dbcResultSet.FETCH_FORWARD<br>
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
 */
public class jdbcDatabaseMetaData implements java.sql.DatabaseMetaData {

    /**
     * The connection this object uses to retrieve instance-specific metadata.
     */
    private jdbcConnection cConnection;

    //----------------------------------------------------------------------
    // First, a variety of minor information about the target database.

    /**
     * Retrieves whether the current user can call all the procedures
     * returned by the method <code>getProcedures</code>.  <p>
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * Up to and including 1.7.1, HSQLDB does not return any rows from
     * <code>getProcedures</code>.  However,
     * <code>allProceduresAreCallable</code> always returns <code>true</code>.
     * This is simply meant to indicate that all users can call all stored
     * procedures made available by default in a newly created HSQLDB
     * database.
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean allProceduresAreCallable() throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        return true;
    }

    /**
     * Retrieves whether the current user can use all the tables returned
     * by the method <code>getTables</code> in a <code>SELECT</code>
     * statement.  <p>
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * Up to and including 1.7.1, HSQLDB will, by default, throw an exception
     * for any non-admin user calling <code>getTables</code>, while any
     * admin user can <CODE>SELECT</CODE> from any table.  As such, this
     * method always returns <code>true</code>.  However, if an admin user
     * grants <CODE>SELECT</CODE> access to <CODE>SYSTEM_TABLES</CODE> to
     * a non-admin user, then it is possible for that user to be denied
     * <CODE>SELECT</CODE> access to tables listed when he/she calls
     * <code>getTables</code>.  This behaviour will be corrected in 1.7.2. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean allTablesAreSelectable() throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        return true;
    }

    /**
     * Retrieves the URL for this DBMS.
     *
     *
     * @return the URL for this DBMS or <code>null</code> if it cannot be
     *         generated
     * @exception SQLException if a database access error occurs
     */
    public String getURL() throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        // CHECKME:
        // is this always correct?
        // boucherb@users 20020426
        return "jdbc:hsqldb:" + cConnection.getName();
    }

    /**
     * Retrieves the user name as known to this database.
     *
     *
     * @return the database user name
     * @exception SQLException if a database access error occurs
     */
    public String getUserName() throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        ResultSet r = executeSelect("SYSTEM_CONNECTIONINFO", "KEY='USER'");

        r.next();

        return r.getString(2);
    }

    /**
     * Retrieves whether this database is in read-only mode. <p>
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * Up to and including 1.7.1, this is a synonym for
     * {@link jdbcConnection#isReadOnly()} and does not report on
     * the global read-only state of the database.  This behaviour
     * will be corrected in 1.7.2.
     * </span>
     * <!-- end release-specific documentation -->
     *
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean isReadOnly() throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        // FIXME:
        // This is wrong.  This should determine if the
        // whole database is in read-only mode, not whether the
        // connection to the database is in read only mode.
        // That is why there is Connection.isReadOnly() and
        // DatabaseMetaData.isReadOnly().
        // boucherb@users 20020426
        // fredt@users - sure, let's get the correct value for 1.7.1
        return cConnection.isReadOnly();
    }

    /**
     * Retrieves whether <code>NULL</code> values are sorted high.
     * Sorted high means that <code>NULL</code> values
     * sort higher than any other value in a domain.  In an ascending order,
     * if this method returns <code>true</code>,  <code>NULL</code> values
     * will appear at the end. By contrast, the method
     * <code>nullsAreSortedAtEnd</code> indicates whether <code>NULL</code> values
     * are sorted at the end regardless of sort order. <p>
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * HSQLDB sorts null low; this method always returns <code>false</code>.
     * </span>
     * <!-- end release-specific documentation -->
     *
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean nullsAreSortedHigh() throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        return false;
    }

    /**
     * Retrieves whether <code>NULL</code> values are sorted low.
     * Sorted low means that <code>NULL</code> values
     * sort lower than any other value in a domain.  In an ascending order,
     * if this method returns <code>true</code>,  <code>NULL</code> values
     * will appear at the beginning. By contrast, the method
     * <code>nullsAreSortedAtStart</code> indicates whether <code>NULL</code> values
     * are sorted at the beginning regardless of sort order. <p>
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * HSQLDB sorts null low; this method always returns <code>true</code>.
     * </span>
     * <!-- end release-specific documentation -->
     *
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean nullsAreSortedLow() throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        return true;
    }

    /**
     * Retrieves whether <code>NULL</code> values are sorted at the start regardless
     * of sort order. <p>
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * HSQLDB sorts null low; this method always returns <code>false</code>.
     * </span>
     * <!-- end release-specific documentation -->
     *
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean nullsAreSortedAtStart() throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        return false;
    }

    /**
     * Retrieves whether <code>NULL</code> values are sorted at the end regardless of
     * sort order. <p>
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * HSQLDB sorts null low; this method always returns <code>false</code>.
     * </span>
     * <!-- end release-specific documentation -->
     *
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean nullsAreSortedAtEnd() throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        return false;
    }

    /**
     * Retrieves the name of this database product.
     *
     * @return database product name
     * @exception SQLException if a database access error occurs
     */
    public String getDatabaseProductName() throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        return jdbcDriver.PRODUCT;
    }

    /**
     * Retrieves the version number of this database product.
     *
     * @return database version number
     * @exception SQLException if a database access error occurs
     */
    public String getDatabaseProductVersion() throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        return jdbcDriver.VERSION;
    }

    /**
     * Retrieves the name of this JDBC driver.
     *
     * @return JDBC driver name
     * @exception SQLException if a database access error occurs
     */
    public String getDriverName() throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        return jdbcDriver.PRODUCT + " Driver";
    }

    /**
     * Retrieves the version number of this JDBC driver as a <code>String</code>.
     *
     * @return JDBC driver version
     * @exception SQLException if a database access error occurs
     */
    public String getDriverVersion() throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        return jdbcDriver.VERSION;
    }

    /**
     * Retrieves this JDBC driver's major version number.
     *
     * @return JDBC driver major version
     */
    public int getDriverMajorVersion() {

        if (Trace.TRACE) {
            Trace.trace();
        }

        return jdbcDriver.MAJOR;
    }

    /**
     * Retrieves this JDBC driver's minor version number.
     *
     * @return JDBC driver minor version number
     */
    public int getDriverMinorVersion() {

        if (Trace.TRACE) {
            Trace.trace();
        }

        return jdbcDriver.MINOR;
    }

    /**
     * Retrieves whether this database stores tables in a local file. <p>
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * Up to and including HSQLDB 1.7.1, do not rely on this information.
     * This will be resolved and explained more clearly in 1.7.2.
     * </span>
     * <!-- end release-specific documentation -->
     *
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean usesLocalFiles() throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        // SEE: FIXME at jdbcConnection.usesLocalFiles()
        // boucherb@users 20020426
        return cConnection.usesLocalFiles();
    }

    /**
     * Retrieves whether this database uses a file for each table. <p>
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * Up to and including 1.7.1, HSQLDB never uses a file for each table.
     * This method always returns <code>false</code>.
     * </span>
     * <!-- end release-specific documentation -->
     *
     *
     * @return <code>true</code> if this database uses a local file for each table;
     *      <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean usesLocalFilePerTable() throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        return false;
    }

    /**
     * Retrieves whether this database treats mixed case unquoted SQL identifiers as
     * case sensitive and as a result stores them in mixed case. <p>
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * HSQLDB treats unquoted identifiers as case insensitive and stores
     * them in upper case. It treats quoted identifiers as case sensitive and
     * stores them verbatim; this method always returns <code>false</code>.
     * </span>
     * <!-- end release-specific documentation -->
     *
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean supportsMixedCaseIdentifiers() throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        return false;
    }

    /**
     * Retrieves whether this database treats mixed case unquoted SQL identifiers as
     * case insensitive and stores them in upper case. <p>
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * HSQLDB treats unquoted identifiers as case insensitive and stores
     * them in upper case. It treats quoted identifiers as case sensitive and
     * stores them verbatim; this method always returns <code>true</code>.
     * </span>
     * <!-- end release-specific documentation -->
     *
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean storesUpperCaseIdentifiers() throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        return true;
    }

    /**
     * Retrieves whether this database treats mixed case unquoted SQL identifiers as
     * case insensitive and stores them in lower case. <p>
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * HSQLDB treats unquoted identifiers as case insensitive and stores
     * them in upper case. It treats quoted identifiers as case sensitive and
     * stores them verbatim; this method always returns <code>false</code>.
     * </span>
     * <!-- end release-specific documentation -->
     *
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean storesLowerCaseIdentifiers() throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        return false;
    }

    /**
     * Retrieves whether this database treats mixed case unquoted SQL identifiers as
     * case insensitive and stores them in mixed case. <p>
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * HSQLDB treats unquoted identifiers as case insensitive and stores
     * them in upper case. It treats quoted identifiers as case sensitive and
     * stores them verbatim; this method always returns <code>false</code>.
     * </span>
     * <!-- end release-specific documentation -->
     *
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean storesMixedCaseIdentifiers() throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        return false;
    }

    /**
     * Retrieves whether this database treats mixed case quoted SQL identifiers as
     * case sensitive and as a result stores them in mixed case. <p>
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * HSQLDB treats unquoted identifiers as case insensitive and stores
     * them in upper case. It treats quoted identifiers as case sensitive and
     * stores them verbatim; this method always returns <code>true</code>.
     * </span>
     * <!-- end release-specific documentation -->
     *
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean supportsMixedCaseQuotedIdentifiers() throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        return true;

        // InterBase (iscdrv32.dll) returns false
    }

    /**
     * Retrieves whether this database treats mixed case quoted SQL identifiers as
     * case insensitive and stores them in upper case. <p>
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * HSQLDB treats unquoted identifiers as case insensitive and stores
     * them in upper case. It treats quoted identifiers as case sensitive and
     * stores them verbatim; this method always returns <code>false</code>.
     * </span>
     * <!-- end release-specific documentation -->
     *
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean storesUpperCaseQuotedIdentifiers() throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        return false;
    }

    /**
     * Retrieves whether this database treats mixed case quoted SQL identifiers as
     * case insensitive and stores them in lower case. <p>
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * HSQLDB treats unquoted identifiers as case insensitive and stores
     * them in upper case. It treats quoted identifiers as case sensitive and
     * stores them verbatim; this method always returns <code>false</code>e.
     * </span>
     * <!-- end release-specific documentation -->
     *
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean storesLowerCaseQuotedIdentifiers() throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        return false;
    }

    /**
     * Retrieves whether this database treats mixed case quoted SQL identifiers as
     * case insensitive and stores them in mixed case. <p>
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * HSQLDB treats unquoted identifiers as case insensitive and stores
     * them in upper case. It treats quoted identifiers as case sensitive and
     * stores them verbatim; this method always returns <code>false</code>.
     * </span>
     * <!-- end release-specific documentation -->
     *
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean storesMixedCaseQuotedIdentifiers() throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        return false;

        // No: as case sensitive.
    }

    /**
     * Retrieves the string used to quote SQL identifiers.
     * This method returns a space " " if identifier quoting is not supported. <p>
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * HSQLDB uses the standard SQL identifer quote character
     * (the double qoute character); this method always returns <b>"</b>.
     * </span>
     * <!-- end release-specific documentation -->
     *
     *
     * @return the quoting string or a space if quoting is not supported
     * @exception SQLException if a database access error occurs
     */
    public String getIdentifierQuoteString() throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        return "\"";

        // InterBase (iscdrv32.dll) returns ""
    }

//fredt@users 20020429 - JavaDoc comment - in 1.7.1 there are keywords such
// as TEMP, TEXT, CACHED that are not SQL 92 keywords

    /**
     * Retrieves a comma-separated list of all of this database's SQL keywords
     * that are NOT also SQL92 keywords. <p>
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * The list returned contains HSQLDB keywords that are not in the list
     * of reserved words. Some of these are in the list of potential reserved
     * words that are not SQL92 keywords, but are reported in the
     * standard as possible future SQL keywords.
     * </span>
     * <!-- end release-specific documentation -->
     *
     *
     * @return the list of this database's keywords that are not also
     *       SQL92 keywords
     * @exception SQLException if a database access error occurs
     */
    public String getSQLKeywords() throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        return "BEFORE,BIGINT,BINARY,CACHED,DATETIME,"
               + "LIMIT,LONGVARBINARY,LONGVARCHAR,OBJECT,OTHER,SAVEPOINT,"
               + "TEMP,TEXT,TRIGGER,TINYINT,VARBINARY,VARCHAR_IGNORECASE";
    }

    /**
     * Retrieves a comma-separated list of math functions available with
     * this database.  These are the Open /Open CLI math function names used in
     * the JDBC function escape clause.
     * @return the list of math functions supported by this database
     * @exception SQLException if a database access error occurs
     */
    public String getNumericFunctions() throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        return getList(Library.sNumeric);
    }

    /**
     * Retrieves a comma-separated list of string functions available with
     * this database.  These are the  Open Group CLI string function names used
     * in the JDBC function escape clause.
     * @return the list of string functions supported by this database
     * @exception SQLException if a database access error occurs
     */
    public String getStringFunctions() throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        return getList(Library.sString);
    }

    /**
     * Retrieves a comma-separated list of system functions available with
     * this database.  These are the  Open Group CLI system function names used
     * in the JDBC function escape clause.
     * @return a list of system functions supported by this database
     * @exception SQLException if a database access error occurs
     */
    public String getSystemFunctions() throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        return getList(Library.sSystem);
    }

    /**
     * Retrieves a comma-separated list of the time and date functions available
     * with this database.
     * @return the list of time and date functions supported by this database
     * @exception SQLException if a database access error occurs
     */
    public String getTimeDateFunctions() throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        return getList(Library.sTimeDate);
    }

    /**
     * Retrieves the string that can be used to escape wildcard characters.
     * This is the string that can be used to escape '_' or '%' in
     * the catalog search parameters that are a pattern (and therefore use one
     * of the wildcard characters).
     *
     * <P>The '_' character represents any single character;
     * the '%' character represents any sequence of zero or
     * more characters. <p>
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * Up to and including 1.7.1, HSQLDB uses the "\" character to escape
     * wildcard characters.
     * </span>
     * <!-- end release-specific documentation -->
     *
     *
     * @return the string used to escape wildcard characters
     * @exception SQLException if a database access error occurs
     */
    public String getSearchStringEscape() throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        return "\\";
    }

    /**
     * Retrieves all the "extra" characters that can be used in unquoted
     * identifier names (those beyond a-z, A-Z, 0-9 and _). <p>
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * Up to and including 1.7.1, HSQLDB does not support using any
     * "extra" characters in unquoted identifier names; this method always
     * returns the empty String.
     * </span>
     * <!-- end release-specific documentation -->
     *
     *
     * @return the string containing the extra characters
     * @exception SQLException if a database access error occurs
     */
    public String getExtraNameCharacters() throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        return "";
    }

    //--------------------------------------------------------------------
    // Functions describing which features are supported.

    /**
     * Retrieves whether this database supports <code>ALTER TABLE</code>
     * with add column. <p>
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * From 1.7.0, HSQLDB support this type of
     * <code>ALTER TABLE</code> statement; this method always
     * returns <code>true</code>.
     * </span>
     * <!-- end release-specific documentation -->
     *
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean supportsAlterTableWithAddColumn() throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        return true;
    }

    /**
     * Retrieves whether this database supports <code>ALTER TABLE</code>
     * with drop column. <p>
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * From 1.7.0, HSQLDB support this type of
     * <code>ALTER TABLE</code> statement; this method always
     * returns <code>true</code>.
     * </span>
     * <!-- end release-specific documentation -->
     *
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean supportsAlterTableWithDropColumn() throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        return true;
    }

    /**
     * Retrieves whether this database supports column aliasing.
     *
     * <P>If so, the SQL AS clause can be used to provide names for
     * computed columns or to provide alias names for columns as
     * required. <p>
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * HSQLDB supports column aliasing; this method always
     * returns <code>true</code>.
     * </span>
     * <!-- end release-specific documentation -->
     *
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean supportsColumnAliasing() throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        return true;

        // InterBase (iscdrv32.dll) returns false
    }

    /**
     * Retrieves whether this database supports concatenations between
     * <code>NULL</code> and non-<code>NULL</code> values being
     * <code>NULL</code>. <p>
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * HSQLDB supports this; this method always
     * returns <code>true</code>.
     * </span>
     * <!-- end release-specific documentation -->
     *
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean nullPlusNonNullIsNull() throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        return true;

        // Access (odbcjt32.dll) returns false
    }

    /**
     * Retrieves whether this database supports the <code>CONVERT</code>
     * function between SQL types. <p>
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * HSQLDB supports conversions; this method always
     * returns <code>true</code>.
     * </span>
     * <!-- end release-specific documentation -->
     *
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean supportsConvert() throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        return true;
    }

// fredt@users - JD comment - I think this is unsupported at the moment
// because SQL92 says conversion is implementation dependent, so if
// conversion from DOUBLE to INTEGER were possbible we would return the
// whole number part, but we currently throw. I'm not so sure about
// conversions between string and numeric where it is logically possible
// only if the string represents a numeric value

    /**
     * Retrieves whether this database supports the <code>CONVERT</code>
     * for two given SQL types. <p>
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * HSQLDB supports conversion though String intermediates, so everything
     * should be possible, short of number format errors (all Java objects
     * have a toString method); this method always returns <code>true</code>.
     * </span>
     * <!-- end release-specific documentation -->
     *
     *
     * @param fromType the type to convert from; one of the type codes from
     *       the class <code>java.sql.Types</code>
     * @param toType the type to convert to; one of the type codes from
     *       the class <code>java.sql.Types</code>
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     * @see java.sql.Types
     */
    public boolean supportsConvert(int fromType,
                                   int toType) throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        return true;
    }

    /**
     * Retrieves whether this database supports table correlation names. <p>
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * HSQLDB supports table correlation names; this method always
     * returns <code>true</code>.
     * </span>
     * <!-- end release-specific documentation -->
     *
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean supportsTableCorrelationNames() throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        return true;
    }

    /**
     * Retrieves whether, when table correlation names are supported, they
     * are restricted to being different from the names of the tables. <p>
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * HSQLDB requires that table correlation names are different from the
     * names of the tables; this method always returns <code>true</code>.
     * </span>
     * <!-- end release-specific documentation -->
     *
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean supportsDifferentTableCorrelationNames()
    throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        return true;
    }

    /**
     * Retrieves whether this database supports expressions in
     * <code>ORDER BY</code> lists. <p>
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * HSQLDB supports expressions in <code>ORDER BY</code> lists; this
     * method always returns <code>true</code>.
     * </span>
     * <!-- end release-specific documentation -->
     *
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean supportsExpressionsInOrderBy() throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        return true;
    }

    /**
     * Retrieves whether this database supports using a column that is
     * not in the <code>SELECT</code> statement in an
     * <code>ORDER BY</code> clause. <p>
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * HSQLDB supports using a column that is not in the <code>SELECT</code>
     * statement in an <code>ORDER BY</code> clause; this method always
     * returns <code>true</code>.
     * </span>
     * <!-- end release-specific documentation -->
     *
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean supportsOrderByUnrelated() throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        return true;
    }

    /**
     * Retrieves whether this database supports some form of
     * <code>GROUP BY</code> clause. <p>
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * HSQLDB supports using the <code>GROUP BY</code> clause; this method
     * always returns <code>true</code>.
     * </span>
     * <!-- end release-specific documentation -->
     *
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean supportsGroupBy() throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        return true;
    }

    /**
     * Retrieves whether this database supports using a column that is
     * not in the <code>SELECT</code> statement in a
     * <code>GROUP BY</code> clause. <p>
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * HSQLDB supports using a column that is
     * not in the <code>SELECT</code> statement in a
     * <code>GROUP BY</code> clause; this method
     * always returns <code>true</code>.
     * </span>
     * <!-- end release-specific documentation -->
     *
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean supportsGroupByUnrelated() throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        return true;
    }

    /**
     * Retrieves whether this database supports using columns not included in
     * the <code>SELECT</code> statement in a <code>GROUP BY</code> clause
     * provided that all of the columns in the <code>SELECT</code> statement
     * are included in the <code>GROUP BY</code> clause. <p>
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * HSQLDB supports using columns not included in
     * the <code>SELECT</code> statement in a <code>GROUP BY</code> clause
     * provided that all of the columns in the <code>SELECT</code> statement
     * are included in the <code>GROUP BY</code> clause; this method
     * always returns <code>true</code>.
     * </span>
     * <!-- end release-specific documentation -->
     *
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean supportsGroupByBeyondSelect() throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        return true;
    }

    /**
     * Retrieves whether this database supports specifying a
     * <code>LIKE</code> escape clause. <p>
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * HSQLDB supports specifying a
     * <code>LIKE</code> escape clause; this method
     * always returns <code>true</code>.
     * </span>
     * <!-- end release-specific documentation -->
     *
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean supportsLikeEscapeClause() throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        return true;
    }

    /**
     * Retrieves whether this database supports getting multiple
     * <code>ResultSet</code> objects from a single call to the
     * method <code>execute</code>. <p>
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * Up to and including 1.7.1, HSQLDB does not support getting multiple
     * <code>ResultSet</code> objects from a single call to the
     * method <code>execute</code>; this method
     * always returns <code>false</code>. <p>
     *
     * This behaviour <i>may</i> change in a future release.
     * </span>
     * <!-- end release-specific documentation -->
     *
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean supportsMultipleResultSets() throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        return false;
    }

    /**
     * Retrieves whether this database allows having multiple
     * transactions open at once (on different connections). <p>
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * HSQLDB allows having multiple
     * transactions open at once (on different connections); this method
     * always returns <code>true</code>.
     * </span>
     * <!-- end release-specific documentation -->
     *
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean supportsMultipleTransactions() throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        return true;
    }

    /**
     * Retrieves whether columns in this database may be defined as
     * non-nullable. <p>
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * HSQLDB supports the specification of non-nullable columns; this method
     * always returns <code>true</code>.
     * </span>
     * <!-- end release-specific documentation -->
     *
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean supportsNonNullableColumns() throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        return true;
    }

    /**
     * Retrieves whether this database supports the ODBC Minimum SQL grammar. <p>
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * up to and including 1.7.1, HSQLDB does not support the ODBC
     * Minimum SQL grammar; this method
     * always returns <code>false</code>.
     * </span>
     * <!-- end release-specific documentation -->
     *
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean supportsMinimumSQLGrammar() throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        return false;
    }

    /**
     * Retrieves whether this database supports the ODBC Core SQL grammar. <p>
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * up to and including 1.7.1, HSQLDB does not support the ODBC
     * Core SQL grammar; this method
     * always returns <code>false</code>.
     * </span>
     * <!-- end release-specific documentation -->
     *
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean supportsCoreSQLGrammar() throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        return false;
    }

    /**
     * Retrieves whether this database supports the ODBC Extended SQL grammar. <p>
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * up to and including 1.7.1, HSQLDB does not support the ODBC
     * Extended SQL grammar; this method
     * always returns <code>false</code>.
     * </span>
     * <!-- end release-specific documentation -->
     *
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean supportsExtendedSQLGrammar() throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        return false;
    }

    /**
     * Retrieves whether this database supports the ANSI92 entry level SQL
     * grammar. <p>
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * up to and including 1.7.1, HSQLDB does not support the ANSI92 entry
     * level SQL grammar; this method
     * always returns <code>false</code>.
     * </span>
     * <!-- end release-specific documentation -->
     *
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean supportsANSI92EntryLevelSQL() throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        return false;
    }

    /**
     * Retrieves whether this database supports the ANSI92 intermediate SQL
     * grammar supported. <p>
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * up to and including 1.7.1, HSQLDB does not support the ANSI92
     * intermediate SQL grammar; this method always returns
     * <code>false</code>.
     * </span>
     * <!-- end release-specific documentation -->
     *
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean supportsANSI92IntermediateSQL() throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        return false;
    }

    /**
     * Retrieves whether this database supports the ANSI92 full SQL
     * grammar supported. <p>
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * up to and including 1.7.1, HSQLDB does not support the ANSI92
     * full SQL grammar; this method always returns
     * <code>false</code>.
     * </span>
     * <!-- end release-specific documentation -->
     *
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean supportsANSI92FullSQL() throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        return false;
    }

    /**
     * Retrieves whether this database supports the SQL Integrity
     * Enhancement Facility. <p>
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * up to and including 1.7.1, HSQLDB does not support the SQL
     * Integrity Enhancement Facility; this method always returns
     * <code>false</code>.
     * </span>
     * <!-- end release-specific documentation -->
     *
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean supportsIntegrityEnhancementFacility()
    throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        return false;
    }

    /**
     * Retrieves whether this database supports some form of outer join. <p>
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * HSQLDB supports outer joins; this method always returns
     * <code>true</code>.
     * </span>
     * <!-- end release-specific documentation -->
     *
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean supportsOuterJoins() throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        return true;
    }

    /**
     * Retrieves whether this database supports full nested outer joins. <p>
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * Up to and including 1.7.1, HSQLDB  does not support full nested outer
     * joins; this method always returns <code>false</code>. <p>
     *
     * This behaviour may change in a future release.
     * </span>
     * <!-- end release-specific documentation -->
     *
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean supportsFullOuterJoins() throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        return false;
    }

    /**
     * Retrieves whether this database provides limited support for outer
     * joins.  (This will be <code>true</code> if the method
     * <code>supportsFullOuterJoins</code> returns <code>true</code>). <p>
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * Up to and including 1.7.1, HSQLDB support the LEFT OUTER join syntax;
     * this method always returns <code>true</code>.
     * </span>
     * <!-- end release-specific documentation -->
     *
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean supportsLimitedOuterJoins() throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        return true;
    }

    /**
     * Retrieves the database vendor's preferred term for "schema". <p>
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * Up to and including 1.7.1, HSQLDB does not support schemas;
     * this method always returns the empty String.
     * </span>
     * <!-- end release-specific documentation -->
     *
     *
     * @return the vendor term for "schema"
     * @exception SQLException if a database access error occurs
     */
    public String getSchemaTerm() throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        return "";
    }

    /**
     * Retrieves the database vendor's preferred term for "procedure". <p>
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * Up to and including 1.7.1, HSQLDB does not support declaration of
     * procedures in SQL; this method always returns the empty String.
     * </span>
     * <!-- end release-specific documentation -->
     *
     *
     * @return the vendor term for "procedure"
     * @exception SQLException if a database access error occurs
     */
    public String getProcedureTerm() throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        return "";
    }

    /**
     * Retrieves the database vendor's preferred term for "catalog". <p>
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * Up to and including 1.7.1, HSQLDB does not support catalogs;
     * this method always returns the empty String.
     * </span>
     * <!-- end release-specific documentation -->
     *
     *
     * @return the vendor term for "catalog"
     * @exception SQLException if a database access error occurs
     */
    public String getCatalogTerm() throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        return "";
    }

    /**
     * Retrieves whether a catalog appears at the start of a fully qualified
     * table name.  If not, the catalog appears at the end. <p>
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * Up to and including 1.7.1, HSQLDB does not support catalogs;
     * this method always returns <code>false</code>.
     * </span>
     * <!-- end release-specific documentation -->
     *
     *
     * @return <code>true</code> if the catalog name appears at the beginning
     *        of a fully qualified table name; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean isCatalogAtStart() throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        return false;
    }

    /**
     * Retrieves the <code>String</code> that this database uses as the
     * separator between a catalog and table name. <p>
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * Up to and including 1.7.1, HSQLDB does not support catalogs;
     * this method always returns the empty String.
     * </span>
     * <!-- end release-specific documentation -->
     *
     *
     * @return the separator string
     * @exception SQLException if a database access error occurs
     */
    public String getCatalogSeparator() throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        return "";
    }

    /**
     * Retrieves whether a schema name can be used in a data
     * manipulation statement. <p>
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * Up to and including 1.7.1, HSQLDB does not support schemas;
     * this method always returns <code>false</code>.
     * </span>
     * <!-- end release-specific documentation -->
     *
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean supportsSchemasInDataManipulation() throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        return false;
    }

    /**
     * Retrieves whether a schema name can be used in a procedure call
     * statement. <p>
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * Up to and including 1.7.1, HSQLDB does not support schemas;
     * this method always returns <code>false</code>.
     * </span>
     * <!-- end release-specific documentation -->
     *
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean supportsSchemasInProcedureCalls() throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        return false;
    }

    /**
     * Retrieves whether a schema name can be used in a table
     * definition statement. <p>
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * Up to and including 1.7.1, HSQLDB does not support schemas;
     * this method always returns <code>false</code>.
     * </span>
     * <!-- end release-specific documentation -->
     *
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean supportsSchemasInTableDefinitions() throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        return false;
    }

    /**
     * Retrieves whether a schema name can be used in an index
     * definition statement. <p>
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * Up to and including 1.7.1, HSQLDB does not support schemas;
     * this method always returns <code>false</code>.
     * </span>
     * <!-- end release-specific documentation -->
     *
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean supportsSchemasInIndexDefinitions() throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        return false;
    }

    /**
     * Retrieves whether a schema name can be used in a privilege
     * definition statement. <p>
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * Up to and including 1.7.1, HSQLDB does not support schemas;
     * this method always returns <code>false</code>.
     * </span>
     * <!-- end release-specific documentation -->
     *
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean supportsSchemasInPrivilegeDefinitions()
    throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        return false;
    }

    /**
     * Retrieves whether a catalog name can be used in a data
     * manipulation statement. <p>
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * Up to and including 1.7.1, HSQLDB does not support catalogs;
     * this method always returns <code>false</code>.
     * </span>
     * <!-- end release-specific documentation -->
     *
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean supportsCatalogsInDataManipulation() throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        return false;
    }

    /**
     * Retrieves whether a catalog name can be used in a
     * procedure call statement. <p>
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * Up to and including 1.7.1, HSQLDB does not support catalogs;
     * this method always returns <code>false</code>.
     * </span>
     * <!-- end release-specific documentation -->
     *
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean supportsCatalogsInProcedureCalls() throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        return false;
    }

    /**
     * Retrieves whether a catalog name can be used in a
     * table definition statement. <p>
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * Up to and including 1.7.1, HSQLDB does not support catalogs;
     * this method always returns <code>false</code>.
     * </span>
     * <!-- end release-specific documentation -->
     *
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean supportsCatalogsInTableDefinitions() throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        return false;
    }

    /**
     * Retrieves whether a catalog name can be used in an
     * index definition statement. <p>
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * Up to and including 1.7.1, HSQLDB does not support catalogs;
     * this method always returns <code>false</code>.
     * </span>
     * <!-- end release-specific documentation -->
     *
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean supportsCatalogsInIndexDefinitions() throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        return false;
    }

    /**
     * Retrieves whether a catalog name can be used in a
     * privilege definition statement.  <p>
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * Up to and including 1.7.1, HSQLDB does not support catalogs;
     * this method always returns <code>false</code>.
     * </span>
     * <!-- end release-specific documentation -->
     *
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean supportsCatalogsInPrivilegeDefinitions()
    throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        return false;
    }

    /**
     * Retrieves whether this database supports positioned <code>DELETE</code>
     * statements. <p>
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * Up to and including 1.7.1, HSQLDB does not support updateable
     * result sets; this method always returns <code>false</code>.
     * </span>
     * <!-- end release-specific documentation -->
     *
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean supportsPositionedDelete() throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        return false;
    }

    /**
     * Retrieves whether this database supports positioned <code>UPDATE</code>
     * statements. <p>
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * Up to and including 1.7.1, HSQLDB does not support updateable
     * result sets; this method always returns <code>false</code>.
     * </span>
     * <!-- end release-specific documentation -->
     *
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean supportsPositionedUpdate() throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        return false;
    }

    /**
     * Retrieves whether this database supports <code>SELECT FOR UPDATE</code>
     * statements. <p>
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * Up to and including 1.7.1, HSQLDB does not support explicit locking;
     * this method always returns <code>false</code>.
     * </span>
     * <!-- end release-specific documentation -->
     *
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean supportsSelectForUpdate() throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        return false;
    }

    /**
     * Retrieves whether this database supports stored procedure calls
     * that use the stored procedure escape syntax. <p>
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * Up to and including 1.7.1, HSQLDB supports calling public static
     * Java methods in the context of SQL Stored Procedures; this method
     * always returns <code>true</code>.
     * </span>
     * <!-- end release-specific documentation -->
     *
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     * @see jdbcPreparedStatement
     * @see jdbcConnection#prepareCall
     */
    public boolean supportsStoredProcedures() throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        return true;
    }

    /**
     * Retrieves whether this database supports subqueries in comparison
     * expressions. <p>
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * HSQLDB has always supported subqueries in comparison expressions;
     * this method always returns <code>true</code>.
     * </span>
     * <!-- end release-specific documentation -->
     *
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean supportsSubqueriesInComparisons() throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        return true;
    }

    /**
     * Retrieves whether this database supports subqueries in
     * <code>EXISTS</code> expressions. <p>
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * HSQLDB has always supported subqueries in <code>EXISTS</code>
     * expressions; this method always returns <code>true</code>.
     * </span>
     * <!-- end release-specific documentation -->
     *
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean supportsSubqueriesInExists() throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        return true;
    }

    /**
     * Retrieves whether this database supports subqueries in
     * <code>IN</code> statements. <p>
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * HSQLDB has always supported subqueries in <code>IN</code>
     * statements; this method always returns <code>true</code>.
     * </span>
     * <!-- end release-specific documentation -->
     *
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean supportsSubqueriesInIns() throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        return true;
    }

    /**
     * Retrieves whether this database supports subqueries in quantified
     * expressions. <p>
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * HSQLDB has always supported subqueries in quantified
     * expressions; this method always returns <code>true</code>.
     * </span>
     * <!-- end release-specific documentation -->
     *
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean supportsSubqueriesInQuantifieds() throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();

            // todo: check if this is correct
        }

        return true;
    }

    /**
     * Retrieves whether this database supports correlated subqueries. <p>
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * HSQLDB has always supported correlated subqueries;
     * this method always returns <code>true</code>.
     * </span>
     * <!-- end release-specific documentation -->
     *
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean supportsCorrelatedSubqueries() throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        return true;
    }

    /**
     * Retrieves whether this database supports SQL <code>UNION</code>. <p>
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * HSQLDB supports SQL <code>UNION</code>;
     * this method always returns <code>true</code>.
     * </span>
     * <!-- end release-specific documentation -->
     *
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean supportsUnion() throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        return true;
    }

    /**
     * Retrieves whether this database supports SQL <code>UNION ALL</code>. <p>
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * HSQLDB supports SQL <code>UNION ALL</code>;
     * this method always returns <code>true</code>.
     * </span>
     * <!-- end release-specific documentation -->
     *
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean supportsUnionAll() throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        return true;
    }

    /**
     * Retrieves whether this database supports keeping cursors open
     * across commits. <p>
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * Up to and including 1.7.1, HSQLDB does not support keeping
     * cursors open across commits;
     * this method always returns <code>false</code>.
     * </span>
     * <!-- end release-specific documentation -->
     *
     *
     * @return <code>true</code> if cursors always remain open;
     *      <code>false</code> if they might not remain open
     * @exception SQLException if a database access error occurs
     */
    public boolean supportsOpenCursorsAcrossCommit() throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        return false;
    }

    /**
     * Retrieves whether this database supports keeping cursors open
     * across rollbacks. <p>
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * Up to and including 1.7.1, HSQLDB does not support keeping
     * cursors open across rollbacks;
     * this method always returns <code>false</code>.
     * </span>
     * <!-- end release-specific documentation -->
     *
     *
     * @return <code>true</code> if cursors always remain open;
     *      <code>false</code> if they might not remain open
     * @exception SQLException if a database access error occurs
     */
    public boolean supportsOpenCursorsAcrossRollback() throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        return false;
    }

    /**
     * Retrieves whether this database supports keeping statements open
     * across commits. <p>
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * HSQLDB supports keeping statements open
     * across commits;
     * this method always returns <code>true</code>.
     * </span>
     * <!-- end release-specific documentation -->
     *
     *
     * @return <code>true</code> if statements always remain open;
     *      <code>false</code> if they might not remain open
     * @exception SQLException if a database access error occurs
     */
    public boolean supportsOpenStatementsAcrossCommit() throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        return true;
    }

    /**
     * Retrieves whether this database supports keeping statements open
     * across rollbacks. <p>
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * HSQLDB supports keeping statements open
     * across commits;
     * this method always returns <code>true</code>.
     * </span>
     * <!-- end release-specific documentation -->
     *
     *
     * @return <code>true</code> if statements always remain open;
     *      <code>false</code> if they might not remain open
     * @exception SQLException if a database access error occurs
     */
    public boolean supportsOpenStatementsAcrossRollback()
    throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        return true;
    }

    //----------------------------------------------------------------------
    // The following group of methods exposes various limitations
    // based on the target database with the current driver.
    // Unless otherwise specified, a result of zero means there is no
    // limit, or the limit is not known.

    /**
     * Retrieves the maximum number of hex characters this database allows in an
     * inline binary literal. <p>
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * HSQLDB does not impose a "known" limit.  The hard limit is the maximum
     * length of a java.lang.String (java.lang.Integer.MAX_VALUE);
     * this method always returns <code>0</code>.
     * </span>
     * <!-- end release-specific documentation -->
     *
     *
     * @return max the maximum length (in hex characters) for a binary literal;
     *     a result of zero means that there is no limit or the limit
     *     is not known
     * @exception SQLException if a database access error occurs
     */
    public int getMaxBinaryLiteralLength() throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        // hard limit is Integer.MAX_VALUE
        return 0;
    }

    /**
     * Retrieves the maximum number of characters this database allows
     * for a character literal. <p>
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * HSQLDB does not impose a "known" limit.  The hard limit is the maximum
     * length of a java.lang.String (java.lang.Integer.MAX_VALUE);
     * this method always returns <code>0</code>.
     * </span>
     * <!-- end release-specific documentation -->
     *
     *
     * @return the maximum number of characters allowed for a character literal;
     *     a result of zero means that there is no limit or the limit is
     *     not known
     * @exception SQLException if a database access error occurs
     */
    public int getMaxCharLiteralLength() throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        // hard limit is Integer.MAX_VALUE
        return 0;
    }

    /**
     * Retrieves the maximum number of characters this database allows
     * for a column name. <p>
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * HSQLDB does not impose a "known" limit.  The hard limit is the maximum
     * length of a java.lang.String (java.lang.Integer.MAX_VALUE);
     * this method always returns <code>0</code>.
     * </span>
     * <!-- end release-specific documentation -->
     *
     *
     * @return the maximum number of characters allowed for a column name;
     *     a result of zero means that there is no limit or the limit
     *     is not known
     * @exception SQLException if a database access error occurs
     */
    public int getMaxColumnNameLength() throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        // hard limit is Integer.MAX_VALUE
        return 0;
    }

    /**
     * Retrieves the maximum number of columns this database allows in a
     * <code>GROUP BY</code> clause. <p>
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * HSQLDB does not impose a "known" limit.  The hard limit is the maximum
     * length of a Java array (java.lang.Integer.MAX_VALUE);
     * this method always returns <code>0</code>.
     * </span>
     * <!-- end release-specific documentation -->
     *
     *
     * @return the maximum number of columns allowed;
     *     a result of zero means that there is no limit or the limit
     *     is not known
     * @exception SQLException if a database access error occurs
     */
    public int getMaxColumnsInGroupBy() throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        // hard limit is Integer.MAX_VALUE
        return 0;
    }

    /**
     * Retrieves the maximum number of columns this database allows in
     * an index. <p>
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * HSQLDB does not impose a "known" limit.  The hard limit is the maximum
     * length of a Java array (java.lang.Integer.MAX_VALUE);
     * this method always returns <code>0</code>.
     * </span>
     * <!-- end release-specific documentation -->
     *
     *
     * @return the maximum number of columns allowed;
     *     a result of zero means that there is no limit or the limit
     *     is not known
     * @exception SQLException if a database access error occurs
     */
    public int getMaxColumnsInIndex() throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        // hard limit is Integer.MAX_VALUE
        return 0;
    }

    /**
     * Retrieves the maximum number of columns this database allows in an
     * <code>ORDER BY</code> clause. <p>
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * HSQLDB does not impose a "known" limit.  The hard limit is the maximum
     * length of a Java array (java.lang.Integer.MAX_VALUE);
     * this method always returns <code>0</code>.
     * </span>
     * <!-- end release-specific documentation -->
     *
     *
     * @return the maximum number of columns allowed;
     *     a result of zero means that there is no limit or the limit
     *     is not known
     * @exception SQLException if a database access error occurs
     */
    public int getMaxColumnsInOrderBy() throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        // hard limit is Integer.MAX_VALUE
        return 0;
    }

    /**
     * Retrieves the maximum number of columns this database allows in a
     * <code>SELECT</code> list. <p>
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * HSQLDB does not impose a "known" limit.  The hard limit is the maximum
     * length of a Java array (java.lang.Integer.MAX_VALUE);
     * this method always returns <code>0</code>.
     * </span>
     * <!-- end release-specific documentation -->
     *
     *
     * @return the maximum number of columns allowed;
     *     a result of zero means that there is no limit or the limit
     *     is not known
     * @exception SQLException if a database access error occurs
     */
    public int getMaxColumnsInSelect() throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        // hard limit is Integer.MAX_VALUE
        return 0;
    }

    /**
     * Retrieves the maximum number of columns this database allows in
     * a table. <p>
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * HSQLDB does not impose a "known" limit.  The hard limit is the maximum
     * length of a Java array (java.lang.Integer.MAX_VALUE);
     * this method always returns <code>0</code>.
     * </span>
     * <!-- end release-specific documentation -->
     *
     *
     * @return the maximum number of columns allowed;
     *     a result of zero means that there is no limit or the limit
     *     is not known
     * @exception SQLException if a database access error occurs
     */
    public int getMaxColumnsInTable() throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        // hard limit is Integer.MAX_VALUE
        return 0;
    }

    /**
     * Retrieves the maximum number of concurrent connections to this
     * database that are possible. <p>
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * HSQLDB does not impose a "known" limit.  The hard limit is the maximum
     * length of a Java array (java.lang.Integer.MAX_VALUE);
     * this method always returns <code>0</code>.
     * </span>
     * <!-- end release-specific documentation -->
     *
     *
     * @return the maximum number of active connections possible at one time;
     *     a result of zero means that there is no limit or the limit
     *     is not known
     * @exception SQLException if a database access error occurs
     */
    public int getMaxConnections() throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        // hard limit is (probably) Integer.MAX_VALUE
        return 0;
    }

    /**
     * Retrieves the maximum number of characters that this database allows in a
     * cursor name. <p>
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * HSQLDB does not impose a "known" limit.  The hard limit is the maximum
     * length of a java.lang.String (java.lang.Integer.MAX_VALUE);
     * this method always returns <code>0</code>.
     * </span>
     * <!-- end release-specific documentation -->
     *
     *
     * @return the maximum number of characters allowed in a cursor name;
     *     a result of zero means that there is no limit or the limit
     *     is not known
     * @exception SQLException if a database access error occurs
     */
    public int getMaxCursorNameLength() throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        // N/A => 0
        return 0;
    }

    /**
     * Retrieves the maximum number of bytes this database allows for an
     * index, including all of the parts of the index. <p>
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * HSQLDB does not impose a "known" limit;
     * this method always returns <code>0</code>.
     * </span>
     * <!-- end release-specific documentation -->
     *
     *
     * @return the maximum number of bytes allowed; this limit includes the
     *     composite of all the constituent parts of the index;
     *     a result of zero means that there is no limit or the limit
     *     is not known
     * @exception SQLException if a database access error occurs
     */
    public int getMaxIndexLength() throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        // N/A => 0?  This may change as cache implementation changes?
        return 0;
    }

    /**
     * Retrieves the maximum number of characters that this database allows in a
     * schema name. <p>
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * Up to and including 1.7.1, HSQLDB does not support schema names;
     * this method always returns <code>0</code>.
     * </span>
     * <!-- end release-specific documentation -->
     *
     *
     * @return the maximum number of characters allowed in a schema name;
     *    a result of zero means that there is no limit or the limit
     *    is not known
     * @exception SQLException if a database access error occurs
     */
    public int getMaxSchemaNameLength() throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        // N/A => 0
        return 0;
    }

    /**
     * Retrieves the maximum number of characters that this database allows in a
     * procedure name. <p>
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * HSQLDB does not impose a "known" limit.  The hard limit is the maximum
     * length of a java.lang.String (java.lang.Integer.MAX_VALUE);
     * this method always returns <code>0</code>.
     * </span>
     * <!-- end release-specific documentation -->
     *
     *
     * @return the maximum number of characters allowed in a procedure name;
     *     a result of zero means that there is no limit or the limit
     *     is not known
     * @exception SQLException if a database access error occurs
     */
    public int getMaxProcedureNameLength() throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        // hard limit is Integer.MAX_VALUE
        return 0;
    }

    /**
     * Retrieves the maximum number of characters that this database allows in a
     * catalog name. <p>
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * Up to and including 1.7.1, HSQLDB does not support catalogs;
     * this method always returns <code>0</code>.
     * </span>
     * <!-- end release-specific documentation -->
     *
     *
     * @return the maximum number of characters allowed in a catalog name;
     *     a result of zero means that there is no limit or the limit
     *     is not known
     * @exception SQLException if a database access error occurs
     */
    public int getMaxCatalogNameLength() throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        // N/A => 0
        return 0;
    }

    /**
     * Retrieves the maximum number of bytes this database allows in
     * a single row. <p>
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * Up to and including 1.7.1, HSQLDB does not impose a "known" limit;
     * this method always returns <code>0</code>.
     * </span>
     * <!-- end release-specific documentation -->
     * @return the maximum number of bytes allowed for a row; a result of
     *        zero means that there is no limit or the limit is not known
     * @exception SQLException if a database access error occurs
     */
    public int getMaxRowSize() throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        return 0;
    }

    /**
     * Retrieves whether the return value for the method
     * <code>getMaxRowSize</code> includes the SQL data types
     * <code>LONGVARCHAR</code> and <code>LONGVARBINARY</code>. <p>
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * Up to and including 1.7.1, {@link #getMaxRowSize} always returns
     * 0, indicating that the maximum row size is unknown or has no limit.
     * This applies to the above types as well, so this method always returns
     * <code>true</code>.
     * </span>
     * <!-- end release-specific documentation -->
     *
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean doesMaxRowSizeIncludeBlobs() throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        // CHECKME: is this correct?  We return "unknown" (0) for getMaxRowSize
        // So, what does it mean to say that "unknown" does not include
        // LONGVARCHAR and LONGVARBINARY?
        // boucherb@users 20020427
        // fredt@users - changed
        return true;
    }

    /**
     * Retrieves the maximum number of characters this database allows in
     * an SQL statement. <p>
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * HSQLDB does not impose a "known" limit.  The hard limit is the maximum
     * length of a java.lang.String (java.lang.Integer.MAX_VALUE);
     * this method always returns <code>0</code>.
     * </span>
     * <!-- end release-specific documentation -->
     *
     *
     * @return the maximum number of characters allowed for an SQL statement;
     *     a result of zero means that there is no limit or the limit
     *     is not known
     * @exception SQLException if a database access error occurs
     */
    public int getMaxStatementLength() throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        return 0;
    }

    /**
     * Retrieves the maximum number of active statements to this database
     * that can be open at the same time. <p>
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * HSQLDB does not impose a "known" limit;
     * this method always returns <code>0</code>.
     * </span>
     * <!-- end release-specific documentation -->
     *
     *
     * @return the maximum number of statements that can be open at one time;
     *     a result of zero means that there is no limit or the limit
     *     is not known
     * @exception SQLException if a database access error occurs
     */
    public int getMaxStatements() throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        return 0;
    }

    /**
     * Retrieves the maximum number of characters this database allows in
     * a table name. <p>
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * HSQLDB does not impose a "known" limit.  The hard limit is the maximum
     * length of a java.lang.String (java.lang.Integer.MAX_VALUE);
     * this method always returns <code>0</code>.
     * </span>
     * <!-- end release-specific documentation -->
     *
     *
     * @return the maximum number of characters allowed for a table name;
     *     a result of zero means that there is no limit or the limit
     *     is not known
     * @exception SQLException if a database access error occurs
     */
    public int getMaxTableNameLength() throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        // hard limit is Integer.MAX_VALUE
        return 0;
    }

    /**
     * Retrieves the maximum number of tables this database allows in a
     * <code>SELECT</code> statement. <p>
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * HSQLDB does not impose a "known" limit.  The hard limit is the maximum
     * length of a Java array (java.lang.Integer.MAX_VALUE);
     * this method always returns <code>0</code>.
     * </span>
     * <!-- end release-specific documentation -->
     *
     *
     * @return the maximum number of tables allowed in a <code>SELECT</code>
     *        statement; a result of zero means that there is no limit or
     *        the limit is not known
     * @exception SQLException if a database access error occurs
     */
    public int getMaxTablesInSelect() throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        // - soft limit is >>> than will ever be seen in any real stmnt
        // - exists a fixed (non statement dependent) hard limit?  No.
        // - depends totally on number of table idents that can fit in
        // Integer.MAX_VALUE characters, minus the rest of the stmnt
        return 0;
    }

    /**
     * Retrieves the maximum number of characters this database allows in
     * a user name. <p>
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * HSQLDB does not impose a "known" limit.  The hard limit is the maximum
     * length of a java.lang.String (java.lang.Integer.MAX_VALUE);
     * this method always returns <code>0</code>.
     * </span>
     * <!-- end release-specific documentation -->
     *
     *
     * @return the maximum number of characters allowed for a user name;
     *     a result of zero means that there is no limit or the limit
     *     is not known
     * @exception SQLException if a database access error occurs
     */
    public int getMaxUserNameLength() throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        // hard limit is Integer.MAX_VALUE
        return 0;
    }

    //----------------------------------------------------------------------

    /**
     * Retrieves this database's default transaction isolation level.  The
     * possible values are defined in <code>java.sql.Connection</code>. <p>
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * </span>
     * <!-- end release-specific documentation -->
     *
     *
     * @return the default isolation level
     * @exception SQLException if a database access error occurs
     * @see jdbcConnection
     */
    public int getDefaultTransactionIsolation() throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        return Connection.TRANSACTION_READ_UNCOMMITTED;
    }

    /**
     * Retrieves whether this database supports transactions. If not, invoking the
     * method <code>commit</code> is a noop, and the isolation level is
     * <code>TRANSACTION_NONE</code>. <p>
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * HSQLDB supports transaction;
     * this method always returns <code>true</code>.
     * </span>
     * <!-- end release-specific documentation -->
     *
     *
     * @return <code>true</code> if transactions are supported;
     *        <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean supportsTransactions() throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        return true;
    }

    /**
     * Retrieves whether this database supports the given transaction
     * isolation level. <p>
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * HSQLDB supports only <code>TRANSACTION_READ_UNCOMMITED</code>.
     * </span>
     * <!-- end release-specific documentation -->
     *
     *
     * @param level one of the transaction isolation levels defined in
     *         <code>java.sql.Connection</code>
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     * @see jdbcConnection
     */
    public boolean supportsTransactionIsolationLevel(int level)
    throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        return level == Connection.TRANSACTION_READ_UNCOMMITTED;
    }

    /**
     * Retrieves whether this database supports both data definition and
     * data manipulation statements within a transaction. <p>
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * HSQLDB does not support a mix of both data definition and
     * data manipulation statements within a transaction.  DDL commits the
     * current transaction before proceding;
     * this method always returns <code>false</code>.
     * </span>
     * <!-- end release-specific documentation -->
     *
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean supportsDataDefinitionAndDataManipulationTransactions()
    throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        return false;
    }

    /**
     * Retrieves whether this database supports only data manipulation
     * statements within a transaction. <p>
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * HSQLDB supports only data manipulation
     * statements within a transaction.  DDL commits the
     * current transaction before proceeding, while DML does not;
     * this method always returns <code>true</code>.
     * </span>
     * <!-- end release-specific documentation -->
     *
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean supportsDataManipulationTransactionsOnly()
    throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        return true;
    }

    /**
     * Retrieves whether a data definition statement within a transaction forces
     * the transaction to commit. <p>
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * In HSQLDB,   data definition statement within a transaction forces
     * the transaction to commit;
     * this method always returns <code>true</code>.
     * </span>
     * <!-- end release-specific documentation -->
     *
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean dataDefinitionCausesTransactionCommit()
    throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        return true;
    }

    /**
     * Retrieves whether this database ignores a data definition statement
     * within a transaction. <p>
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * In HSQLDB, a data definition statement
     * is not ignored within a transaction.  Rather, a data
     * definition statement within a transaction forces
     * the transaction to commit;
     * this method always returns <code>false</code>.
     * </span>
     * <!-- end release-specific documentation -->
     *
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean dataDefinitionIgnoredInTransactions() throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        return false;
    }

    /**
     * Retrieves a description of the stored procedures available in the given
     * catalog.
     * <P>
     * Only procedure descriptions matching the schema and
     * procedure name criteria are returned.  They are ordered by
     * <code>PROCEDURE_SCHEM</code> and <code>PROCEDURE_NAME</code>.
     *
     * <P>Each procedure description has the the following columns:
     * <OL>
     *  <LI><B>PROCEDURE_CAT</B> String => procedure catalog (may be <code>null</code>)
     *  <LI><B>PROCEDURE_SCHEM</B> String => procedure schema (may be <code>null</code>)
     *  <LI><B>PROCEDURE_NAME</B> String => procedure name
     * <LI> reserved for future use
     * <LI> reserved for future use
     * <LI> reserved for future use
     *  <LI><B>REMARKS</B> String => explanatory comment on the procedure
     *  <LI><B>PROCEDURE_TYPE</B> short => kind of procedure:
     *    <UL>
     *    <LI> procedureResultUnknown - May return a result
     *    <LI> procedureNoResult - Does not return a result
     *    <LI> procedureReturnsResult - Returns a result
     *    </UL>
     * </OL> <p>
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * Up to and including 1.7.1, HSQLDB produces an empty result, despite
     * the fact that stored procedures are available.  Also, the three
     * "reserved for future use" columns in  the result are labeled
     * NUM_INPUT_PARAMS, NUM_OUTPUT_PARAMS, NUM_RESULT_SETS in anticipation
     * of future improvements (scheduled for 1.7.2).
     * </span>
     * <!-- end release-specific documentation -->
     *
     *
     * @param catalog a catalog name; must match the catalog name as it
     *      is stored in the database; "" retrieves those without a catalog;
     *      <code>null</code> means that the catalog name should not be used to narrow
     *      the search
     * @param schemaPattern a schema name pattern; must match the schema name
     *      as it is stored in the database; "" retrieves those without a schema;
     *      <code>null</code> means that the schema name should not be used to narrow
     *      the search
     * @param procedureNamePattern a procedure name pattern; must match the
     *      procedure name as it is stored in the database
     * @return <code>ResultSet</code> - each row is a procedure description
     * @exception SQLException if a database access error occurs
     * @see #getSearchStringEscape
     */
    public ResultSet getProcedures(String catalog, String schemaPattern,
                                   String procedureNamePattern)
                                   throws SQLException {

        // currently no records for any pattern
        return executeSelect("SYSTEM_PROCEDURES", null);
    }

    /**
     * Retrieves a description of the given catalog's stored procedure parameter
     * and result columns.
     *
     * <P>Only descriptions matching the schema, procedure and
     * parameter name criteria are returned.  They are ordered by
     * PROCEDURE_SCHEM and PROCEDURE_NAME. Within this, the return value,
     * if any, is first. Next are the parameter descriptions in call
     * order. The column descriptions follow in column number order.
     *
     * <P>Each row in the <code>ResultSet</code> is a parameter description or
     * column description with the following fields:
     * <OL>
     *  <LI><B>PROCEDURE_CAT</B> String => procedure catalog (may be <code>null</code>)
     *  <LI><B>PROCEDURE_SCHEM</B> String => procedure schema (may be <code>null</code>)
     *  <LI><B>PROCEDURE_NAME</B> String => procedure name
     *  <LI><B>COLUMN_NAME</B> String => column/parameter name
     *  <LI><B>COLUMN_TYPE</B> Short => kind of column/parameter:
     *     <UL>
     *     <LI> procedureColumnUnknown - nobody knows
     *     <LI> procedureColumnIn - IN parameter
     *     <LI> procedureColumnInOut - INOUT parameter
     *     <LI> procedureColumnOut - OUT parameter
     *     <LI> procedureColumnReturn - procedure return value
     *     <LI> procedureColumnResult - result column in <code>ResultSet</code>
     *     </UL>
     * <LI><B>DATA_TYPE</B> short => SQL type from java.sql.Types
     *  <LI><B>TYPE_NAME</B> String => SQL type name, for a UDT type the
     * type name is fully qualified
     *  <LI><B>PRECISION</B> int => precision
     *  <LI><B>LENGTH</B> int => length in bytes of data
     *  <LI><B>SCALE</B> short => scale
     *  <LI><B>RADIX</B> short => radix
     *  <LI><B>NULLABLE</B> short => can it contain NULL.
     *     <UL>
     *     <LI> procedureNoNulls - does not allow NULL values
     *     <LI> procedureNullable - allows NULL values
     *     <LI> procedureNullableUnknown - nullability unknown
     *     </UL>
     *  <LI><B>REMARKS</B> String => comment describing parameter/column
     * </OL>
     *
     * <P><B>Note:</B> Some databases may not return the column
     * descriptions for a procedure. Additional columns beyond
     * REMARKS can be defined by the database. <p>
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * Up to and including 1.7.1, HSQLDB produces an empty result, despite
     * the fact that stored procedures are available.
     * </span>
     * <!-- end release-specific documentation -->
     *
     *
     * @param catalog a catalog name; must match the catalog name as it
     *       is stored in the database; "" retrieves those without a catalog;
     *       <code>null</code> means that the catalog name should not be used to narrow
     *       the search
     * @param schemaPattern a schema name pattern; must match the schema name
     *       as it is stored in the database; "" retrieves those without a schema;
     *       <code>null</code> means that the schema name should not be used to narrow
     *       the search
     * @param procedureNamePattern a procedure name pattern; must match the
     *       procedure name as it is stored in the database
     * @param columnNamePattern a column name pattern; must match the column name
     *       as it is stored in the database
     * @return <code>ResultSet</code> - each row describes a stored procedure parameter or
     *     column
     * @exception SQLException if a database access error occurs
     * @see #getSearchStringEscape
     */
    public ResultSet getProcedureColumns(String catalog,
                                         String schemaPattern,
                                         String procedureNamePattern,
                                         String columnNamePattern)
                                         throws SQLException {

        // currently no records for any pattern
        return executeSelect("SYSTEM_PROCEDURECOLUMNS", null);
    }

    /**
     * Retrieves a description of the tables available in the given catalog.
     * Only table descriptions matching the catalog, schema, table
     * name and type criteria are returned.  They are ordered by
     * TABLE_TYPE, TABLE_SCHEM and TABLE_NAME.
     * <P>
     * Each table description has the following columns:
     * <OL>
     *  <LI><B>TABLE_CAT</B> String => table catalog (may be <code>null</code>)
     *  <LI><B>TABLE_SCHEM</B> String => table schema (may be <code>null</code>)
     *  <LI><B>TABLE_NAME</B> String => table name
     *  <LI><B>TABLE_TYPE</B> String => table type.  Typical types are "TABLE",
     *                  "VIEW", "SYSTEM TABLE", "GLOBAL TEMPORARY",
     *                  "LOCAL TEMPORARY", "ALIAS", "SYNONYM".
     *  <LI><B>REMARKS</B> String => explanatory comment on the table
     * <LI><B>TYPE_CAT</B> String => the types catalog (may be <code>null</code>)
     * <LI><B>TYPE_SCHEM</B> String => the types schema (may be <code>null</code>)
     * <LI><B>TYPE_NAME</B> String => type name (may be <code>null</code>)
     * <LI><B>SELF_REFERENCING_COL_NAME</B> String => name of the designated
     *                 "identifier" column of a typed table (may be <code>null</code>)
     *  <LI><B>REF_GENERATION</B> String => specifies how values in
     *                 SELF_REFERENCING_COL_NAME are created. Values are
     *                 "SYSTEM", "USER", "DERIVED". (may be <code>null</code>)
     * </OL>
     *
     * <P><B>Note:</B> Some databases may not return information for
     * all tables. <p>
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * Starting with 1.7.0, HSQLDB returns extra information on TEXT tables
     * in the REMARKS column.<p>
     *
     * Starting with 1.7.0, HSQLDB includes the new JDBC3 columns TYPE_CAT,
     * TYPE_SCHEM, TYPE_NAME and SELF_REFERENCING_COL_NAME in anticipation
     * of JDBC3 compliant tools.  However, these columns are never filled in,
     * since HSQLDB does not yet support the related features.
     * </span>
     * <!-- end release-specific documentation -->
     *
     *
     * @param catalog a catalog name; must match the catalog name as it
     *       is stored in the database; "" retrieves those without a catalog;
     *       <code>null</code> means that the catalog name should not be used to narrow
     *       the search
     * @param schemaPattern a schema name pattern; must match the schema name
     *       as it is stored in the database; "" retrieves those without a schema;
     *       <code>null</code> means that the schema name should not be used to narrow
     *       the search
     * @param tableNamePattern a table name pattern; must match the
     *       table name as it is stored in the database
     * @param types a list of table types to include; <code>null</code> returns all types
     * @return <code>ResultSet</code> - each row is a table description
     * @exception SQLException if a database access error occurs
     * @see #getSearchStringEscape
     */
    public ResultSet getTables(String catalog, String schemaPattern,
                               String tableNamePattern,
                               String types[]) throws SQLException {

        String table = "SYSTEM_TABLES";
        String where = null;

        if (tableNamePattern != null) {
            where = "TABLE_NAME LIKE '" + tableNamePattern + "'";
        }

        if (types != null) {
            if (where == null) {
                where = "";
            } else {
                where += " AND ";
            }

            where += " TABLE_TYPE IN (";
            where += getList(types, true);
            where += ')';
        }

        return executeSelect(table, where);
    }

    /**
     * Retrieves the schema names available in this database.  The results
     * are ordered by schema name.
     *
     * <P>The schema column is:
     * <OL>
     * <LI><B>TABLE_SCHEM</B> String => schema name
     * <LI><B>TABLE_CATALOG</B> String => catalog name (may be <code>null</code>)
     * </OL> <p>
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * Starting with 1.7.0, HSQLDB includes the new JDBC3 column
     * TABLE_CATALOG in anticipation of JDBC3 compliant tools.
     * However, HSQLDB does not yet support schemas and catalogs, so
     * this method always returns an empty result.
     * </span>
     * <!-- end release-specific documentation -->
     *
     *
     * @return a <code>ResultSet</code> object in which each row is a
     *        schema decription
     * @exception SQLException if a database access error occurs
     */
    public ResultSet getSchemas() throws SQLException {
        return executeSelect("SYSTEM_SCHEMAS", null);
    }

    /**
     * Retrieves the catalog names available in this database.  The results
     * are ordered by catalog name.
     *
     * <P>The catalog column is:
     * <OL>
     *    <LI><B>TABLE_CAT</B> String => catalog name
     * </OL> <p>
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * HSQLDB does not yet support catalogs, so
     * this method always returns an empty result.
     * </span>
     * <!-- end release-specific documentation -->
     *
     *
     * @return a <code>ResultSet</code> object in which each row has a
     *        single <code>String</code> column that is a catalog name
     * @exception SQLException if a database access error occurs
     */
    public ResultSet getCatalogs() throws SQLException {
        return executeSelect("SYSTEM_CATALOGS", null);
    }

    /**
     * Retrieves the table types available in this database.  The results
     * are ordered by table type.
     *
     * <P>The table type is:
     * <OL>
     *  <LI><B>TABLE_TYPE</B> String => table type.  Typical types are "TABLE",
     *                  "VIEW", "SYSTEM TABLE", "GLOBAL TEMPORARY",
     *                  "LOCAL TEMPORARY", "ALIAS", "SYNONYM".
     * </OL> <p>
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * From 1.7.1, HSQLDB reports: "TABLE", "VIEW" and "GLOBAL TEMPORARY" types.
     * </span>
     * <!-- end release-specific documentation -->
     *
     *
     * @return a <code>ResultSet</code> object in which each row has a
     *        single <code>String</code> column that is a table type
     * @exception SQLException if a database access error occurs
     */
    public ResultSet getTableTypes() throws SQLException {
        return executeSelect("SYSTEM_TABLETYPES", null);
    }

    /**
     * Retrieves a description of table columns available in
     * the specified catalog.
     *
     * <P>Only column descriptions matching the catalog, schema, table
     * and column name criteria are returned.  They are ordered by
     * <code>TABLE_SCHEM</code>, <code>TABLE_NAME</code>, and
     * <code>ORDINAL_POSITION</code>.
     *
     * <P>Each column description has the following columns:
     * <OL>
     *  <LI><B>TABLE_CAT</B> String => table catalog (may be <code>null</code>)
     *  <LI><B>TABLE_SCHEM</B> String => table schema (may be <code>null</code>)
     *  <LI><B>TABLE_NAME</B> String => table name
     *  <LI><B>COLUMN_NAME</B> String => column name
     *  <LI><B>DATA_TYPE</B> short => SQL type from java.sql.Types
     *  <LI><B>TYPE_NAME</B> String => Data source dependent type name,
     * for a UDT the type name is fully qualified
     *  <LI><B>COLUMN_SIZE</B> int => column size.  For char or date
     *      types this is the maximum number of characters, for numeric or
     *      decimal types this is precision.
     *  <LI><B>BUFFER_LENGTH</B> is not used.
     *  <LI><B>DECIMAL_DIGITS</B> int => the number of fractional digits
     *  <LI><B>NUM_PREC_RADIX</B> int => Radix (typically either 10 or 2)
     *  <LI><B>NULLABLE</B> int => is NULL allowed.
     *    <UL>
     *    <LI> columnNoNulls - might not allow <code>NULL</code> values
     *    <LI> columnNullable - definitely allows <code>NULL</code> values
     *    <LI> columnNullableUnknown - nullability unknown
     *    </UL>
     *  <LI><B>REMARKS</B> String => comment describing column (may be <code>null</code>)
     *  <LI><B>COLUMN_DEF</B> String => default value (may be <code>null</code>)
     *  <LI><B>SQL_DATA_TYPE</B> int => unused
     *  <LI><B>SQL_DATETIME_SUB</B> int => unused
     *  <LI><B>CHAR_OCTET_LENGTH</B> int => for char types the
     *     maximum number of bytes in the column
     *  <LI><B>ORDINAL_POSITION</B> int => index of column in table
     *    (starting at 1)
     *  <LI><B>IS_NULLABLE</B> String => "NO" means column definitely
     *    does not allow NULL values; "YES" means the column might
     *    allow NULL values.  An empty string means nobody knows.
     * <LI><B>SCOPE_CATLOG</B> String => catalog of table that is the scope
     *    of a reference attribute (<code>null</code> if DATA_TYPE isn't REF)
     * <LI><B>SCOPE_SCHEMA</B> String => schema of table that is the scope
     *    of a reference attribute (<code>null</code> if the DATA_TYPE isn't REF)
     * <LI><B>SCOPE_TABLE</B> String => table name that this the scope
     *    of a reference attribure (<code>null</code> if the DATA_TYPE isn't REF)
     * <LI><B>SOURCE_DATA_TYPE</B> short => source type of a distinct type or user-generated
     *    Ref type, SQL type from java.sql.Types (<code>null</code> if DATA_TYPE
     *    isn't DISTINCT or user-generated REF)
     * </OL> <p>
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * Starting with 1.7.0, HSQLDB includes the new JDBC3 columns SCOPE_CATLOG,
     * SCOPE_SCHEMA, SCOPE_TABLE and SOURCE_DATA_TYPE in anticipation
     * of JDBC3 compliant tools.  However, these columns are never filled in,
     * since HSQLDB does not yet support the related features.
     * </span>
     * <!-- end release-specific documentation -->
     *
     *
     * @param catalog a catalog name; must match the catalog name as it
     *      is stored in the database; "" retrieves those without a catalog;
     *      <code>null</code> means that the catalog name should not be used to narrow
     *      the search
     * @param schemaPattern a schema name pattern; must match the schema name
     *      as it is stored in the database; "" retrieves those without a schema;
     *      <code>null</code> means that the schema name should not be used to narrow
     *      the search
     * @param tableNamePattern a table name pattern; must match the
     *      table name as it is stored in the database
     * @param columnNamePattern a column name pattern; must match the column
     *      name as it is stored in the database
     * @return <code>ResultSet</code> - each row is a column description
     * @exception SQLException if a database access error occurs
     * @see #getSearchStringEscape
     */
    public ResultSet getColumns(String catalog, String schemaPattern,
                                String tableNamePattern,
                                String columnNamePattern)
                                throws SQLException {

        String table = "SYSTEM_COLUMNS";
        String where = null;

        if (tableNamePattern != null) {
            where = "TABLE_NAME LIKE '" + tableNamePattern + "'";
        }

        if (columnNamePattern != null) {
            where = addWhere(where,
                             "COLUMN_NAME LIKE '" + columnNamePattern + "'");
        }

        return executeSelect(table, where);
    }

    /**
     * Retrieves a description of the access rights for a table's columns.
     *
     * <P>Only privileges matching the column name criteria are
     * returned.  They are ordered by COLUMN_NAME and PRIVILEGE.
     *
     * <P>Each privilige description has the following columns:
     * <OL>
     *  <LI><B>TABLE_CAT</B> String => table catalog (may be <code>null</code>)
     *  <LI><B>TABLE_SCHEM</B> String => table schema (may be <code>null</code>)
     *  <LI><B>TABLE_NAME</B> String => table name
     *  <LI><B>COLUMN_NAME</B> String => column name
     *  <LI><B>GRANTOR</B> => grantor of access (may be <code>null</code>)
     *  <LI><B>GRANTEE</B> String => grantee of access
     *  <LI><B>PRIVILEGE</B> String => name of access (SELECT,
     *     INSERT, UPDATE, REFRENCES, ...)
     *  <LI><B>IS_GRANTABLE</B> String => "YES" if grantee is permitted
     *     to grant to others; "NO" if not; <code>null</code> if unknown
     * </OL> <p>
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * Up to and including 1.7.1, HSQLDB produces an empty result, despite
     * the fact that it is possible to specify DML privileges.  However,
     * column-level privileges are not supported.  So, if column privileges
     * were reported, they would be the privileges inherited from each
     * column's table.
     * </span>
     * <!-- end release-specific documentation -->
     *
     *
     * @param catalog a catalog name; must match the catalog name as it
     *       is stored in the database; "" retrieves those without a catalog;
     *       <code>null</code> means that the catalog name should not be used to narrow
     *       the search
     * @param schema a schema name; must match the schema name as it is
     *       stored in the database; "" retrieves those without a schema;
     *       <code>null</code> means that the schema name should not be used to narrow
     *       the search
     * @param table a table name; must match the table name as it is
     *       stored in the database
     * @param columnNamePattern a column name pattern; must match the column
     *       name as it is stored in the database
     * @return <code>ResultSet</code> - each row is a column privilege description
     * @exception SQLException if a database access error occurs
     * @see #getSearchStringEscape
     */
    public ResultSet getColumnPrivileges(String catalog, String schema,
                                         String table,
                                         String columnNamePattern)
                                         throws SQLException {

        String systemTable = "SYSTEM_COLUMNPRIVILEGES";
        String where       = null;

        // if (tableP != null) {
        if (table != null) {

// fredt@users 20020225 - comment - slight error
// LIKE mishandles an underscore when it is not a pattern character
            where = "TABLE_NAME LIKE '" + table + "'";
        }

        if (columnNamePattern != null) {
            where = addWhere(where,
                             "COLUMN_NAME LIKE '" + columnNamePattern + "'");
        }

        return executeSelect(systemTable, where);
    }

    /**
     * Retrieves a description of the access rights for each table available
     * in a catalog. Note that a table privilege applies to one or
     * more columns in the table. It would be wrong to assume that
     * this privilege applies to all columns (this may be true for
     * some systems but is not true for all.)
     *
     * <P>Only privileges matching the schema and table name
     * criteria are returned.  They are ordered by TABLE_SCHEM,
     * TABLE_NAME, and PRIVILEGE.
     *
     * <P>Each privilige description has the following columns:
     * <OL>
     *  <LI><B>TABLE_CAT</B> String => table catalog (may be <code>null</code>)
     *  <LI><B>TABLE_SCHEM</B> String => table schema (may be <code>null</code>)
     *  <LI><B>TABLE_NAME</B> String => table name
     *  <LI><B>GRANTOR</B> => grantor of access (may be <code>null</code>)
     *  <LI><B>GRANTEE</B> String => grantee of access
     *  <LI><B>PRIVILEGE</B> String => name of access (SELECT,
     *     INSERT, UPDATE, REFRENCES, ...)
     *  <LI><B>IS_GRANTABLE</B> String => "YES" if grantee is permitted
     *     to grant to others; "NO" if not; <code>null</code> if unknown
     * </OL> <p>
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * Up to and including 1.7.1, HSQLDB produces an incomplete and possibly
     * incorrect result: for each table, it lists the user "sa" as the
     * grantor, rather than the grantee, and lists IS_GRANTABLE as YES for
     * each row.  It does not list rights for any other users.  Since the
     * "sa" user can be dropped from the database and recreated as a non-admin
     * user, this result is not only incomplete, it is wrong.
     * </span>
     * <!-- end release-specific documentation -->
     *
     *
     * @param catalog a catalog name; must match the catalog name as it
     *       is stored in the database; "" retrieves those without a catalog;
     *       <code>null</code> means that the catalog name should not be used to narrow
     *       the search
     * @param schemaPattern a schema name pattern; must match the schema name
     *       as it is stored in the database; "" retrieves those without a schema;
     *       <code>null</code> means that the schema name should not be used to narrow
     *       the search
     * @param tableNamePattern a table name pattern; must match the
     *       table name as it is stored in the database
     * @return <code>ResultSet</code> - each row is a table privilege description
     * @exception SQLException if a database access error occurs
     * @see #getSearchStringEscape
     */
    public ResultSet getTablePrivileges(String catalog, String schemaPattern,
                                        String tableNamePattern)
                                        throws SQLException {

        // FIXME:
        // Our SYSTEM_TABLEPRIVILEGES table really sucks.
        // even without the full metadata patch, we might as well
        // simply return an empty result as use the current implementation;
        // Fred: just say the word and I'll quickly rework my current metadata
        // code to fit the current constraints (e.g. system tables are admin
        // access only by default) and provide at least a better than completely
        // useless result.
        // boucherb@users 20020426
        // fredt@users - if you can do it now, QUICKLY, fine,
        // otherwise let's just keep it as it is until 1.7.1
        String table = "SYSTEM_TABLEPRIVILEGES";
        String where = null;

        if (tableNamePattern != null) {
            where = "TABLE_NAME LIKE '" + tableNamePattern + "'";
        }

        return executeSelect(table, where);
    }

    /**
     * Retrieves a description of a table's optimal set of columns that
     * uniquely identifies a row. They are ordered by SCOPE.
     *
     * <P>Each column description has the following columns:
     * <OL>
     *  <LI><B>SCOPE</B> short => actual scope of result
     *     <UL>
     *     <LI> bestRowTemporary - very temporary, while using row
     *     <LI> bestRowTransaction - valid for remainder of current transaction
     *     <LI> bestRowSession - valid for remainder of current session
     *     </UL>
     *  <LI><B>COLUMN_NAME</B> String => column name
     *  <LI><B>DATA_TYPE</B> short => SQL data type from java.sql.Types
     *  <LI><B>TYPE_NAME</B> String => Data source dependent type name,
     * for a UDT the type name is fully qualified
     *  <LI><B>COLUMN_SIZE</B> int => precision
     *  <LI><B>BUFFER_LENGTH</B> int => not used
     *  <LI><B>DECIMAL_DIGITS</B> short  => scale
     *  <LI><B>PSEUDO_COLUMN</B> short => is this a pseudo column
     *     like an Oracle ROWID
     *     <UL>
     *     <LI> bestRowUnknown - may or may not be pseudo column
     *     <LI> bestRowNotPseudo - is NOT a pseudo column
     *     <LI> bestRowPseudo - is a pseudo column
     *     </UL>
     * </OL> <p>
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * HSQLDB 1.7.1 returns the columns for a user-defined primary key or
     * unique index if one exists. Otherwise it returns an empty result.<br>
     * <code>scope</code> and <code>nullable</code> parameters are not
     * taken into account.<p>
     * If the name of a column is defined in the database without double
     * quotes, an all-uppercase name must be specified when calling this
     * method. Otherwise, the name must be specified in the exact case of
     * the column definition in the database.
     * </span>
     * <!-- end release-specific documentation -->
     *
     *
     * @param catalog a catalog name; must match the catalog name as it
     *       is stored in the database; "" retrieves those without a catalog;
     *       <code>null</code> means that the catalog name should not be used to narrow
     *       the search
     * @param schema a schema name; must match the schema name
     *       as it is stored in the database; "" retrieves those without a schema;
     *       <code>null</code> means that the schema name should not be used to narrow
     *       the search
     * @param table a table name; must match the table name as it is stored
     *       in the database
     * @param scope the scope of interest; use same values as SCOPE
     * @param nullable include columns that are nullable.
     * @return <code>ResultSet</code> - each row is a column description
     * @exception SQLException if a database access error occurs
     */
    public ResultSet getBestRowIdentifier(String catalog, String schema,
                                          String table, int scope,
                                          boolean nullable)
                                          throws SQLException {

        String where = null;

        if (table != null) {
            where = "TABLE_NAME = '" + table + "'";
        }

        return executeSelect("SYSTEM_BESTROWIDENTIFIER", where);
    }

    /**
     * Retrieves a description of a table's columns that are automatically
     * updated when any value in a row is updated.  They are
     * unordered.
     *
     * <P>Each column description has the following columns:
     * <OL>
     *  <LI><B>SCOPE</B> short => is not used
     *  <LI><B>COLUMN_NAME</B> String => column name
     *  <LI><B>DATA_TYPE</B> short => SQL data type from <code>java.sql.Types</code>
     *  <LI><B>TYPE_NAME</B> String => Data source-dependent type name
     *  <LI><B>COLUMN_SIZE</B> int => precision
     *  <LI><B>BUFFER_LENGTH</B> int => length of column value in bytes
     *  <LI><B>DECIMAL_DIGITS</B> short  => scale
     *  <LI><B>PSEUDO_COLUMN</B> short => whether this is pseudo column
     *     like an Oracle ROWID
     *     <UL>
     *     <LI> versionColumnUnknown - may or may not be pseudo column
     *     <LI> versionColumnNotPseudo - is NOT a pseudo column
     *     <LI> versionColumnPseudo - is a pseudo column
     *     </UL>
     * </OL> <p>
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * Up to and including 1.7.1, HSQLDB produces an empty result as no
     * columns are automatically updated when any value in a row changes.
     * </span>
     * <!-- end release-specific documentation -->
     *
     *
     * @param catalog a catalog name; must match the catalog name as it
     *       is stored in the database; "" retrieves those without a catalog;
     *       <code>null</code> means that the catalog name should not be used to narrow
     *       the search
     * @param schema a schema name; must match the schema name
     *       as it is stored in the database; "" retrieves those without a schema;
     *       <code>null</code> means that the schema name should not be used to narrow
     *       the search
     * @param table a table name; must match the table name as it is stored
     *       in the database
     * @return a <code>ResultSet</code> object in which each row is a
     *        column description
     * @exception SQLException if a database access error occurs
     */
    public ResultSet getVersionColumns(String catalog, String schema,
                                       String table) throws SQLException {
        return executeSelect("SYSTEM_VERSIONCOLUMNS", null);
    }

    /**
     * Retrieves a description of the given table's primary key columns.  They
     * are ordered by COLUMN_NAME.
     *
     * <P>Each primary key column description has the following columns:
     *  <OL>
     *  <LI><B>TABLE_CAT</B> String => table catalog (may be <code>null</code>)
     *  <LI><B>TABLE_SCHEM</B> String => table schema (may be <code>null</code>)
     *  <LI><B>TABLE_NAME</B> String => table name
     *  <LI><B>COLUMN_NAME</B> String => column name
     *  <LI><B>KEY_SEQ</B> short => sequence number within primary key
     *  <LI><B>PK_NAME</B> String => primary key name (may be <code>null</code>)
     *  </OL> <p>
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * If the name of a column is defined in the database without double
     * quotes, an all-uppercase name must be specified when calling this
     * method. Otherwise, the name must be specified in the exact case of
     * the column definition in the database.
     * inclomplete result.
     * </span>
     * <!-- end release-specific documentation -->
     *
     *
     * @param catalog a catalog name; must match the catalog name as it
     *        is stored in the database; "" retrieves those without a catalog;
     *        <code>null</code> means that the catalog name should not be used to narrow
     *        the search
     * @param schema a schema name; must match the schema name
     *        as it is stored in the database; "" retrieves those without a schema;
     *        <code>null</code> means that the schema name should not be used to narrow
     *        the search
     * @param table a table name; must match the table name as it is stored
     *        in the database
     * @return <code>ResultSet</code> - each row is a primary key column description
     * @exception SQLException if a database access error occurs
     */

// fredt@users 20020226 - comment - changed query to exact name
    public ResultSet getPrimaryKeys(String catalog, String schema,
                                    String table) throws SQLException {

        String systemTable = "SYSTEM_PRIMARYKEYS";
        String where       = null;

        if (table != null) {
            where = "TABLE_NAME = '" + table + "'";
        }

        return executeSelect(systemTable, where);
    }

    /**
     * Retrieves a description of the primary key columns that are
     * referenced by a table's foreign key columns (the primary keys
     * imported by a table).  They are ordered by PKTABLE_CAT,
     * PKTABLE_SCHEM, PKTABLE_NAME, and KEY_SEQ.
     *
     * <P>Each primary key column description has the following columns:
     * <OL>
     *  <LI><B>PKTABLE_CAT</B> String => primary key table catalog
     *     being imported (may be <code>null</code>)
     *  <LI><B>PKTABLE_SCHEM</B> String => primary key table schema
     *     being imported (may be <code>null</code>)
     *  <LI><B>PKTABLE_NAME</B> String => primary key table name
     *     being imported
     *  <LI><B>PKCOLUMN_NAME</B> String => primary key column name
     *     being imported
     *  <LI><B>FKTABLE_CAT</B> String => foreign key table catalog (may be <code>null</code>)
     *  <LI><B>FKTABLE_SCHEM</B> String => foreign key table schema (may be <code>null</code>)
     *  <LI><B>FKTABLE_NAME</B> String => foreign key table name
     *  <LI><B>FKCOLUMN_NAME</B> String => foreign key column name
     *  <LI><B>KEY_SEQ</B> short => sequence number within a foreign key
     *  <LI><B>UPDATE_RULE</B> short => What happens to a
     *      foreign key when the primary key is updated:
     *     <UL>
     *     <LI> importedNoAction - do not allow update of primary
     *              key if it has been imported
     *     <LI> importedKeyCascade - change imported key to agree
     *              with primary key update
     *     <LI> importedKeySetNull - change imported key to <code>NULL</code>
     *              if its primary key has been updated
     *     <LI> importedKeySetDefault - change imported key to default values
     *              if its primary key has been updated
     *     <LI> importedKeyRestrict - same as importedKeyNoAction
     *                                (for ODBC 2.x compatibility)
     *     </UL>
     *  <LI><B>DELETE_RULE</B> short => What happens to
     *     the foreign key when primary is deleted.
     *     <UL>
     *     <LI> importedKeyNoAction - do not allow delete of primary
     *              key if it has been imported
     *     <LI> importedKeyCascade - delete rows that import a deleted key
     *     <LI> importedKeySetNull - change imported key to NULL if
     *              its primary key has been deleted
     *     <LI> importedKeyRestrict - same as importedKeyNoAction
     *                                (for ODBC 2.x compatibility)
     *     <LI> importedKeySetDefault - change imported key to default if
     *              its primary key has been deleted
     *     </UL>
     *  <LI><B>FK_NAME</B> String => foreign key name (may be <code>null</code>)
     *  <LI><B>PK_NAME</B> String => primary key name (may be <code>null</code>)
     *  <LI><B>DEFERRABILITY</B> short => can the evaluation of foreign key
     *     constraints be deferred until commit
     *     <UL>
     *     <LI> importedKeyInitiallyDeferred - see SQL92 for definition
     *     <LI> importedKeyInitiallyImmediate - see SQL92 for definition
     *     <LI> importedKeyNotDeferrable - see SQL92 for definition
     *     </UL>
     * </OL> <p>
     *
     * <!-- start release-specific documentation -->
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * If the name of a column is defined in the database without double
     * quotes, an all-uppercase name must be specified when calling this
     * method. Otherwise, the name must be specified in the exact case of
     * the column definition in the database.
     * inclomplete result.
     * <!-- end release-specific documentation -->
     *
     *
     * @param catalog a catalog name; must match the catalog name as it
     *       is stored in the database; "" retrieves those without a catalog;
     *       <code>null</code> means that the catalog name should not be used to narrow
     *       the search
     * @param schema a schema name; must match the schema name
     *       as it is stored in the database; "" retrieves those without a schema;
     *       <code>null</code> means that the schema name should not be used to narrow
     *       the search
     * @param table a table name; must match the table name as it is stored
     *       in the database
     * @return <code>ResultSet</code> - each row is a primary key column description
     * @exception SQLException if a database access error occurs
     * @see #getExportedKeys
     */
    public ResultSet getImportedKeys(String catalog, String schema,
                                     String table) throws SQLException {

        return executeSelect("SYSTEM_CROSSREFERENCE",
                             "FKTABLE_NAME = '" + table
                             + "' ORDER BY PKTABLE_NAME, KEY_SEQ");
    }

    /**
     * Retrieves a description of the foreign key columns that reference the
     * given table's primary key columns (the foreign keys exported by a
     * table).  They are ordered by FKTABLE_CAT, FKTABLE_SCHEM,
     * FKTABLE_NAME, and KEY_SEQ.
     *
     * <P>Each foreign key column description has the following columns:
     * <OL>
     *  <LI><B>PKTABLE_CAT</B> String => primary key table catalog (may be <code>null</code>)
     *  <LI><B>PKTABLE_SCHEM</B> String => primary key table schema (may be <code>null</code>)
     *  <LI><B>PKTABLE_NAME</B> String => primary key table name
     *  <LI><B>PKCOLUMN_NAME</B> String => primary key column name
     *  <LI><B>FKTABLE_CAT</B> String => foreign key table catalog (may be <code>null</code>)
     *    being exported (may be <code>null</code>)
     *  <LI><B>FKTABLE_SCHEM</B> String => foreign key table schema (may be <code>null</code>)
     *    being exported (may be <code>null</code>)
     *  <LI><B>FKTABLE_NAME</B> String => foreign key table name
     *    being exported
     *  <LI><B>FKCOLUMN_NAME</B> String => foreign key column name
     *    being exported
     *  <LI><B>KEY_SEQ</B> short => sequence number within foreign key
     *  <LI><B>UPDATE_RULE</B> short => What happens to
     *     foreign key when primary is updated:
     *    <UL>
     *    <LI> importedNoAction - do not allow update of primary
     *             key if it has been imported
     *    <LI> importedKeyCascade - change imported key to agree
     *             with primary key update
     *    <LI> importedKeySetNull - change imported key to <code>NULL</code> if
     *             its primary key has been updated
     *    <LI> importedKeySetDefault - change imported key to default values
     *             if its primary key has been updated
     *    <LI> importedKeyRestrict - same as importedKeyNoAction
     *                               (for ODBC 2.x compatibility)
     *    </UL>
     *  <LI><B>DELETE_RULE</B> short => What happens to
     *    the foreign key when primary is deleted.
     *    <UL>
     *    <LI> importedKeyNoAction - do not allow delete of primary
     *             key if it has been imported
     *    <LI> importedKeyCascade - delete rows that import a deleted key
     *    <LI> importedKeySetNull - change imported key to <code>NULL</code> if
     *             its primary key has been deleted
     *    <LI> importedKeyRestrict - same as importedKeyNoAction
     *                               (for ODBC 2.x compatibility)
     *    <LI> importedKeySetDefault - change imported key to default if
     *             its primary key has been deleted
     *    </UL>
     *  <LI><B>FK_NAME</B> String => foreign key name (may be <code>null</code>)
     *  <LI><B>PK_NAME</B> String => primary key name (may be <code>null</code>)
     *  <LI><B>DEFERRABILITY</B> short => can the evaluation of foreign key
     *    constraints be deferred until commit
     *    <UL>
     *    <LI> importedKeyInitiallyDeferred - see SQL92 for definition
     *    <LI> importedKeyInitiallyImmediate - see SQL92 for definition
     *    <LI> importedKeyNotDeferrable - see SQL92 for definition
     *    </UL>
     * </OL> <p>
     *
     * <!-- start release-specific documentation -->
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * If the name of a column is defined in the database without double
     * quotes, an all-uppercase name must be specified when calling this
     * method. Otherwise, the name must be specified in the exact case of
     * the column definition in the database.
     * inclomplete result.
     * <!-- end release-specific documentation -->
     *
     *
     * @param catalog a catalog name; must match the catalog name as it
     *      is stored in this database; "" retrieves those without a catalog;
     *      <code>null</code> means that the catalog name should not be used to narrow
     *      the search
     * @param schema a schema name; must match the schema name
     *      as it is stored in the database; "" retrieves those without a schema;
     *      <code>null</code> means that the schema name should not be used to narrow
     *      the search
     * @param table a table name; must match the table name as it is stored
     *      in this database
     * @return a <code>ResultSet</code> object in which each row is a
     *       foreign key column description
     * @exception SQLException if a database access error occurs
     * @see #getImportedKeys
     */
    public ResultSet getExportedKeys(String catalog, String schema,
                                     String table) throws SQLException {

        return executeSelect("SYSTEM_CROSSREFERENCE",
                             "PKTABLE_NAME = '" + table
                             + "' ORDER BY FKTABLE_NAME, KEY_SEQ");
    }

    /**
     * Retrieves a description of the foreign key columns in the given foreign key
     * table that reference the primary key columns of the given primary key
     * table (describe how one table imports another's key). This
     * should normally return a single foreign key/primary key pair because
     * most tables import a foreign key from a table only once.  They
     * are ordered by FKTABLE_CAT, FKTABLE_SCHEM, FKTABLE_NAME, and
     * KEY_SEQ.
     *
     * <P>Each foreign key column description has the following columns:
     * <OL>
     *  <LI><B>PKTABLE_CAT</B> String => primary key table catalog (may be <code>null</code>)
     *  <LI><B>PKTABLE_SCHEM</B> String => primary key table schema (may be <code>null</code>)
     *  <LI><B>PKTABLE_NAME</B> String => primary key table name
     *  <LI><B>PKCOLUMN_NAME</B> String => primary key column name
     *  <LI><B>FKTABLE_CAT</B> String => foreign key table catalog (may be <code>null</code>)
     *    being exported (may be <code>null</code>)
     *  <LI><B>FKTABLE_SCHEM</B> String => foreign key table schema (may be <code>null</code>)
     *    being exported (may be <code>null</code>)
     *  <LI><B>FKTABLE_NAME</B> String => foreign key table name
     *    being exported
     *  <LI><B>FKCOLUMN_NAME</B> String => foreign key column name
     *    being exported
     *  <LI><B>KEY_SEQ</B> short => sequence number within foreign key
     *  <LI><B>UPDATE_RULE</B> short => What happens to
     *     foreign key when primary is updated:
     *    <UL>
     *    <LI> importedNoAction - do not allow update of primary
     *             key if it has been imported
     *    <LI> importedKeyCascade - change imported key to agree
     *             with primary key update
     *    <LI> importedKeySetNull - change imported key to <code>NULL</code> if
     *             its primary key has been updated
     *    <LI> importedKeySetDefault - change imported key to default values
     *             if its primary key has been updated
     *    <LI> importedKeyRestrict - same as importedKeyNoAction
     *                               (for ODBC 2.x compatibility)
     *    </UL>
     *  <LI><B>DELETE_RULE</B> short => What happens to
     *    the foreign key when primary is deleted.
     *    <UL>
     *    <LI> importedKeyNoAction - do not allow delete of primary
     *             key if it has been imported
     *    <LI> importedKeyCascade - delete rows that import a deleted key
     *    <LI> importedKeySetNull - change imported key to <code>NULL</code> if
     *             its primary key has been deleted
     *    <LI> importedKeyRestrict - same as importedKeyNoAction
     *                               (for ODBC 2.x compatibility)
     *    <LI> importedKeySetDefault - change imported key to default if
     *             its primary key has been deleted
     *    </UL>
     *  <LI><B>FK_NAME</B> String => foreign key name (may be <code>null</code>)
     *  <LI><B>PK_NAME</B> String => primary key name (may be <code>null</code>)
     *  <LI><B>DEFERRABILITY</B> short => can the evaluation of foreign key
     *    constraints be deferred until commit
     *    <UL>
     *    <LI> importedKeyInitiallyDeferred - see SQL92 for definition
     *    <LI> importedKeyInitiallyImmediate - see SQL92 for definition
     *    <LI> importedKeyNotDeferrable - see SQL92 for definition
     *    </UL>
     * </OL> <p>
     *
     * <!-- start release-specific documentation -->
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * If the name of a column is defined in the database without double
     * quotes, an all-uppercase name must be specified when calling this
     * method. Otherwise, the name must be specified in the exact case of
     * the column definition in the database.
     * inclomplete result.
     * </span>
     * <!-- end release-specific documentation -->
     *
     *
     * @param primaryCatalog a catalog name; must match the catalog name
     * as it is stored in the database; "" retrieves those without a
     * catalog; <code>null</code> means drop catalog name from the selection criteria
     * @param primarySchema a schema name; must match the schema name as
     * it is stored in the database; "" retrieves those without a schema;
     * <code>null</code> means drop schema name from the selection criteria
     * @param primaryTable the name of the table that exports the key; must match
     * the table name as it is stored in the database
     * @param foreignCatalog a catalog name; must match the catalog name as
     * it is stored in the database; "" retrieves those without a
     * catalog; <code>null</code> means drop catalog name from the selection criteria
     * @param foreignSchema a schema name; must match the schema name as it
     * is stored in the database; "" retrieves those without a schema;
     * <code>null</code> means drop schema name from the selection criteria
     * @param foreignTable the name of the table that imports the key; must match
     * the table name as it is stored in the database
     * @return <code>ResultSet</code> - each row is a foreign key column description
     * @exception SQLException if a database access error occurs
     * @see #getImportedKeys
     */
    public ResultSet getCrossReference(String primaryCatalog,
                                       String primarySchema,
                                       String primaryTable,
                                       String foreignCatalog,
                                       String foreignSchema,
                                       String foreignTable)
                                       throws SQLException {

        return executeSelect("SYSTEM_CROSSREFERENCE",
                             "PKTABLE_NAME = '" + primaryTable
                             + "' AND FKTABLE_NAME = '" + foreignTable
                             + "' ORDER BY FKTABLE_NAME, KEY_SEQ");
    }

    /**
     * Retrieves a description of all the standard SQL types supported by
     * this database. They are ordered by DATA_TYPE and then by how
     * closely the data type maps to the corresponding JDBC SQL type.
     *
     * <P>Each type description has the following columns:
     * <OL>
     *  <LI><B>TYPE_NAME</B> String => Type name
     *  <LI><B>DATA_TYPE</B> short => SQL data type from java.sql.Types
     *  <LI><B>PRECISION</B> int => maximum precision
     *  <LI><B>LITERAL_PREFIX</B> String => prefix used to quote a literal
     *     (may be <code>null</code>)
     *  <LI><B>LITERAL_SUFFIX</B> String => suffix used to quote a literal
     * (may be <code>null</code>)
     *  <LI><B>CREATE_PARAMS</B> String => parameters used in creating
     *     the type (may be <code>null</code>)
     *  <LI><B>NULLABLE</B> short => can you use NULL for this type.
     *     <UL>
     *     <LI> typeNoNulls - does not allow NULL values
     *     <LI> typeNullable - allows NULL values
     *     <LI> typeNullableUnknown - nullability unknown
     *     </UL>
     *  <LI><B>CASE_SENSITIVE</B> boolean=> is it case sensitive.
     *  <LI><B>SEARCHABLE</B> short => can you use "WHERE" based on this type:
     *     <UL>
     *     <LI> typePredNone - No support
     *     <LI> typePredChar - Only supported with WHERE .. LIKE
     *     <LI> typePredBasic - Supported except for WHERE .. LIKE
     *     <LI> typeSearchable - Supported for all WHERE ..
     *     </UL>
     *  <LI><B>UNSIGNED_ATTRIBUTE</B> boolean => is it unsigned.
     *  <LI><B>FIXED_PREC_SCALE</B> boolean => can it be a money value.
     *  <LI><B>AUTO_INCREMENT</B> boolean => can it be used for an
     *     auto-increment value.
     *  <LI><B>LOCAL_TYPE_NAME</B> String => localized version of type name
     *     (may be <code>null</code>)
     *  <LI><B>MINIMUM_SCALE</B> short => minimum scale supported
     *  <LI><B>MAXIMUM_SCALE</B> short => maximum scale supported
     *  <LI><B>SQL_DATA_TYPE</B> int => unused
     *  <LI><B>SQL_DATETIME_SUB</B> int => unused
     *  <LI><B>NUM_PREC_RADIX</B> int => usually 2 or 10
     * </OL> <p>
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * Up to and including 1.7.1, HSQLDB produces a usable but partially
     * inclomplete result.
     * </span>
     * <!-- end release-specific documentation -->
     *
     *
     * @return a <code>ResultSet</code> object in which each row is an SQL
     *        type description
     * @exception SQLException if a database access error occurs
     */
    public ResultSet getTypeInfo() throws SQLException {
        return executeSelect("SYSTEM_TYPEINFO", null);
    }

    /**
     * Retrieves a description of the given table's indices and statistics. They are
     * ordered by NON_UNIQUE, TYPE, INDEX_NAME, and ORDINAL_POSITION.
     *
     * <P>Each index column description has the following columns:
     * <OL>
     *  <LI><B>TABLE_CAT</B> String => table catalog (may be <code>null</code>)
     *  <LI><B>TABLE_SCHEM</B> String => table schema (may be <code>null</code>)
     *  <LI><B>TABLE_NAME</B> String => table name
     *  <LI><B>NON_UNIQUE</B> boolean => Can index values be non-unique.
     *     false when TYPE is tableIndexStatistic
     *  <LI><B>INDEX_QUALIFIER</B> String => index catalog (may be <code>null</code>);
     *     <code>null</code> when TYPE is tableIndexStatistic
     *  <LI><B>INDEX_NAME</B> String => index name; <code>null</code> when TYPE is
     *     tableIndexStatistic
     *  <LI><B>TYPE</B> short => index type:
     *     <UL>
     *     <LI> tableIndexStatistic - this identifies table statistics that are
     *          returned in conjuction with a table's index descriptions
     *     <LI> tableIndexClustered - this is a clustered index
     *     <LI> tableIndexHashed - this is a hashed index
     *     <LI> tableIndexOther - this is some other style of index
     *     </UL>
     *  <LI><B>ORDINAL_POSITION</B> short => column sequence number
     *     within index; zero when TYPE is tableIndexStatistic
     *  <LI><B>COLUMN_NAME</B> String => column name; <code>null</code> when TYPE is
     *     tableIndexStatistic
     *  <LI><B>ASC_OR_DESC</B> String => column sort sequence, "A" => ascending,
     *     "D" => descending, may be <code>null</code> if sort sequence is not supported;
     *     <code>null</code> when TYPE is tableIndexStatistic
     *  <LI><B>CARDINALITY</B> int => When TYPE is tableIndexStatistic, then
     *     this is the number of rows in the table; otherwise, it is the
     *     number of unique values in the index.
     *  <LI><B>PAGES</B> int => When TYPE is  tableIndexStatisic then
     *     this is the number of pages used for the table, otherwise it
     *     is the number of pages used for the current index.
     *  <LI><B>FILTER_CONDITION</B> String => Filter condition, if any.
     *     (may be <code>null</code>)
     * </OL> <p>
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * Up to and including 1.7.1, HSQLDB produces a usable but partially
     * inclomplete result.  Cardinality is never listed, and the approximate
     * parameter is always ignored.  No statistics rows are generated.<p>
     *
     * If the name of a column is defined in the database without double
     * quotes, an all-uppercase name must be specified when calling this
     * method. Otherwise, the name must be specified in the exact case of
     * the column definition in the database.
     * inclomplete result.
     * </span>
     * <!-- end release-specific documentation -->
     *
     *
     * @param catalog a catalog name; must match the catalog name as it
     *       is stored in this database; "" retrieves those without a catalog;
     *       <code>null</code> means that the catalog name should not be used to narrow
     *       the search
     * @param schema a schema name; must match the schema name
     *       as it is stored in this database; "" retrieves those without a schema;
     *       <code>null</code> means that the schema name should not be used to narrow
     *       the search
     * @param table a table name; must match the table name as it is stored
     *       in this database
     * @param unique when true, return only indices for unique values;
     *    when false, return indices regardless of whether unique or not
     * @param approximate when true, result is allowed to reflect approximate
     *    or out of data values; when false, results are requested to be
     *    accurate
     * @return <code>ResultSet</code> - each row is an index column description
     * @exception SQLException if a database access error occurs
     */

// fredt@users 20020526 - comment - changed to exact table name
    public ResultSet getIndexInfo(String catalog, String schema,
                                  String table, boolean unique,
                                  boolean approximate) throws SQLException {

        String systemTable = "SYSTEM_INDEXINFO";
        String where       = null;

        if (table != null) {
            where = "TABLE_NAME = '" + table + "'";

            if (unique) {
                where += " and NON_UNIQUE != 'true'";
            }
        }

        return executeSelect(systemTable, where);
    }

    //--------------------------JDBC 2.0-----------------------------

    /**
     * Retrieves whether this database supports the given result set type. <p>
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * </span>
     * <!-- end release-specific documentation -->
     *
     *
     * @param type defined in <code>java.sql.ResultSet</code>
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     * @see jdbcConnection
     * @since  JDK 1.2 (JDK 1.1.x developers: read the new overview
     *      for jdbcDatabaseMetaData)
     */
    public boolean supportsResultSetType(int type) throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        return (type == jdbcResultSet.TYPE_FORWARD_ONLY
                || type == jdbcResultSet.TYPE_SCROLL_INSENSITIVE);
    }

    /**
     * Retrieves whether this database supports the given concurrency type
     * in combination with the given result set type. <p>
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * </span>
     * <!-- end release-specific documentation -->
     *
     *
     * @param type defined in <code>java.sql.ResultSet</code>
     * @param concurrency type defined in <code>java.sql.ResultSet</code>
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     * @see jdbcConnection
     * @since  JDK 1.2 (JDK 1.1.x developers: read the new overview
     *      for jdbcDatabaseMetaData)
     */
    public boolean supportsResultSetConcurrency(int type,
            int concurrency) throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        return supportsResultSetType(type)
               && concurrency == jdbcResultSet.CONCUR_READ_ONLY;
    }

    /**
     *
     * Retrieves whether for the given type of <code>ResultSet</code> object,
     * the result set's own updates are visible. <p>
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * Up to and including 1.7.1, HSQLDB does not support updateable
     * result sets; this method always returns <code>false</code>.
     * </span>
     * <!-- end release-specific documentation -->
     *
     *
     * @param type the <code>ResultSet</code> type; one of
     *       <code>ResultSet.TYPE_FORWARD_ONLY</code>,
     *       <code>ResultSet.TYPE_SCROLL_INSENSITIVE</code>, or
     *       <code>ResultSet.TYPE_SCROLL_SENSITIVE</code>
     * @return <code>true</code> if updates are visible for the given result set type;
     *       <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview
     *     for jdbcDatabaseMetaData)
     */
    public boolean ownUpdatesAreVisible(int type) throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        return false;
    }

    /**
     * Retrieves whether a result set's own deletes are visible. <p>
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * Up to and including 1.7.1, HSQLDB does not support updateable
     * result sets; this method always returns <code>false</code>.
     * </span>
     * <!-- end release-specific documentation -->
     *
     *
     * @param type the <code>ResultSet</code> type; one of
     *       <code>ResultSet.TYPE_FORWARD_ONLY</code>,
     *       <code>ResultSet.TYPE_SCROLL_INSENSITIVE</code>, or
     *       <code>ResultSet.TYPE_SCROLL_SENSITIVE</code>
     * @return <code>true</code> if deletes are visible for the given result set type;
     *       <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview
     *     for jdbcDatabaseMetaData)
     */
    public boolean ownDeletesAreVisible(int type) throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        return false;
    }

    /**
     * Retrieves whether a result set's own inserts are visible. <p>
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * Up to and including 1.7.1, HSQLDB does not support updateable
     * result sets; this method always returns <code>false</code>.
     * </span>
     * <!-- end release-specific documentation -->
     *
     *
     * @param type the <code>ResultSet</code> type; one of
     *       <code>ResultSet.TYPE_FORWARD_ONLY</code>,
     *       <code>ResultSet.TYPE_SCROLL_INSENSITIVE</code>, or
     *       <code>ResultSet.TYPE_SCROLL_SENSITIVE</code>
     * @return <code>true</code> if inserts are visible for the given result set type;
     *       <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview
     *     for jdbcDatabaseMetaData)
     */
    public boolean ownInsertsAreVisible(int type) throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        return false;
    }

    /**
     * Retrieves whether updates made by others are visible. <p>
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * Up to and including 1.7.1, HSQLDB does not support updateable
     * result sets; this method always returns <code>false</code>.
     * </span>
     * <!-- end release-specific documentation -->
     *
     *
     * @param type the <code>ResultSet</code> type; one of
     *       <code>ResultSet.TYPE_FORWARD_ONLY</code>,
     *       <code>ResultSet.TYPE_SCROLL_INSENSITIVE</code>, or
     *       <code>ResultSet.TYPE_SCROLL_SENSITIVE</code>
     * @return <code>true</code> if updates made by others
     *       are visible for the given result set type;
     *       <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview
     *     for jdbcDatabaseMetaData)
     */
    public boolean othersUpdatesAreVisible(int type) throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        return false;
    }

    /**
     * Retrieves whether deletes made by others are visible. <p>
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * Up to and including 1.7.1, HSQLDB does not support updateable
     * result sets; this method always returns <code>false</code>.
     * </span>
     * <!-- end release-specific documentation -->
     *
     *
     * @param type the <code>ResultSet</code> type; one of
     *       <code>ResultSet.TYPE_FORWARD_ONLY</code>,
     *       <code>ResultSet.TYPE_SCROLL_INSENSITIVE</code>, or
     *       <code>ResultSet.TYPE_SCROLL_SENSITIVE</code>
     * @return <code>true</code> if deletes made by others
     *       are visible for the given result set type;
     *       <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview
     *     for jdbcDatabaseMetaData)
     */
    public boolean othersDeletesAreVisible(int type) throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        return false;
    }

    /**
     * Retrieves whether inserts made by others are visible. <p>
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * Up to and including 1.7.1, HSQLDB does not support updateable
     * result sets; this method always returns <code>false</code>.
     * </span>
     * <!-- end release-specific documentation -->
     *
     *
     * @param type the <code>ResultSet</code> type; one of
     *       <code>ResultSet.TYPE_FORWARD_ONLY</code>,
     *       <code>ResultSet.TYPE_SCROLL_INSENSITIVE</code>, or
     *       <code>ResultSet.TYPE_SCROLL_SENSITIVE</code>
     * @return <code>true</code> if inserts made by others
     *        are visible for the given result set type;
     *        <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview
     *     for jdbcDatabaseMetaData)
     */
    public boolean othersInsertsAreVisible(int type) throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        return false;
    }

    /**
     * Retrieves whether or not a visible row update can be detected by
     * calling the method <code>ResultSet.rowUpdated</code>. <p>
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * Up to and including 1.7.1, HSQLDB does not support updateable
     * result sets; this method always returns <code>false</code>.
     * </span>
     * <!-- end release-specific documentation -->
     *
     *
     * @param type the <code>ResultSet</code> type; one of
     *       <code>ResultSet.TYPE_FORWARD_ONLY</code>,
     *       <code>ResultSet.TYPE_SCROLL_INSENSITIVE</code>, or
     *       <code>ResultSet.TYPE_SCROLL_SENSITIVE</code>
     * @return <code>true</code> if changes are detected by the result set type;
     *        <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview
     *     for jdbcDatabaseMetaData)
     */
    public boolean updatesAreDetected(int type) throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        return false;
    }

    /**
     * Retrieves whether or not a visible row delete can be detected by
     * calling the method <code>ResultSet.rowDeleted</code>.  If the method
     * <code>deletesAreDetected</code> returns <code>false</code>, it means that
     * deleted rows are removed from the result set. <p>
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * Up to and including 1.7.1, HSQLDB does not support updateable
     * result sets; this method always returns <code>false</code>.
     * </span>
     * <!-- end release-specific documentation -->
     *
     *
     * @param type the <code>ResultSet</code> type; one of
     *       <code>ResultSet.TYPE_FORWARD_ONLY</code>,
     *       <code>ResultSet.TYPE_SCROLL_INSENSITIVE</code>, or
     *       <code>ResultSet.TYPE_SCROLL_SENSITIVE</code>
     * @return <code>true</code> if deletes are detected by the given result set type;
     *        <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview
     *     for jdbcDatabaseMetaData)
     */
    public boolean deletesAreDetected(int type) throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        return false;
    }

    /**
     * Retrieves whether or not a visible row insert can be detected
     * by calling the method <code>ResultSet.rowInserted</code>. <p>
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * Up to and including 1.7.1, HSQLDB does not support updateable
     * result sets; this method always returns <code>false</code>.
     * </span>
     * <!-- end release-specific documentation -->
     *
     *
     * @param type the <code>ResultSet</code> type; one of
     *       <code>ResultSet.TYPE_FORWARD_ONLY</code>,
     *       <code>ResultSet.TYPE_SCROLL_INSENSITIVE</code>, or
     *       <code>ResultSet.TYPE_SCROLL_SENSITIVE</code>
     * @return <code>true</code> if changes are detected by the specified result
     *        set type; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview
     *     for jdbcDatabaseMetaData)
     */
    public boolean insertsAreDetected(int type) throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        return false;
    }

    /**
     * Retrieves whether this database supports batch updates. <p>
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * Up to and including 1.7.1, HSQLDB does not support batch updates;
     * this method always returns <code>false</code>.
     * </span>
     * <!-- end release-specific documentation -->
     *
     *
     * @return <code>true</code> if this database supports batch upcates;
     *        <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview
     *     for jdbcDatabaseMetaData)
     */
    public boolean supportsBatchUpdates() throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        return false;
    }

    /**
     * Retrieves a description of the user-defined types (UDTs) defined
     * in a particular schema.  Schema-specific UDTs may have type
     * <code>JAVA_OBJECT</code>, <code>STRUCT</code>,
     * or <code>DISTINCT</code>.
     *
     * <P>Only types matching the catalog, schema, type name and type
     * criteria are returned.  They are ordered by DATA_TYPE, TYPE_SCHEM
     * and TYPE_NAME.  The type name parameter may be a fully-qualified
     * name.  In this case, the catalog and schemaPattern parameters are
     * ignored.
     *
     * <P>Each type description has the following columns:
     * <OL>
     *  <LI><B>TYPE_CAT</B> String => the type's catalog (may be <code>null</code>)
     *  <LI><B>TYPE_SCHEM</B> String => type's schema (may be <code>null</code>)
     *  <LI><B>TYPE_NAME</B> String => type name
     * <LI><B>CLASS_NAME</B> String => Java class name
     *  <LI><B>DATA_TYPE</B> String => type value defined in java.sql.Types.
     *    One of JAVA_OBJECT, STRUCT, or DISTINCT
     *  <LI><B>REMARKS</B> String => explanatory comment on the type
     * <LI><B>BASE_TYPE</B> short => type code of the source type of a
     *    DISTINCT type or the type that implements the user-generated
     *    reference type of the SELF_REFERENCING_COLUMN of a structured
     *    type as defined in java.sql.Types (<code>null</code> if DATA_TYPE is not
     *    DISTINCT or not STRUCT with REFERENCE_GENERATION = USER_DEFINED)
     * </OL>
     *
     * <P><B>Note:</B> If the driver does not support UDTs, an empty
     * result set is returned. <p>
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * Up to and including 1.7.1, HSQLDB produces an empty result.
     * </span>
     * <!-- end release-specific documentation -->
     *
     *
     * @param catalog a catalog name; must match the catalog name as it
     *       is stored in the database; "" retrieves those without a catalog;
     *       <code>null</code> means that the catalog name should not be used to narrow
     *       the search
     * @param schemaPattern a schema pattern name; must match the schema name
     *       as it is stored in the database; "" retrieves those without a schema;
     *       <code>null</code> means that the schema name should not be used to narrow
     *       the search
     * @param typeNamePattern a type name pattern; must match the type name
     *       as it is stored in the database; may be a fully qualified name
     * @param types a list of user-defined types (JAVA_OBJECT,
     *       STRUCT, or DISTINCT) to include; <code>null</code> returns all types
     * @return <code>ResultSet</code> object in which each row describes a UDT
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview
     *     for jdbcDatabaseMetaData)
     */
    public ResultSet getUDTs(String catalog, String schemaPattern,
                             String typeNamePattern,
                             int[] types) throws SQLException {
        return executeSelect("SYSTEM_UDTS", null);
    }

    /**
     * Retrieves the connection that produced this metadata object. <p>
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * </span>
     * <!-- end release-specific documentation -->
     *
     *
     * @return the connection that produced this metadata object
     * @exception SQLException if a database access error occurs
     * @since  JDK 1.2 (JDK 1.1.x developers: read the new overview
     *      for jdbcDatabaseMetaData)
     */
    public Connection getConnection() throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        return cConnection;
    }

// boucherb@users 20020426 - javadocs for all JDBC 3 methods
// boucherb@users 20020426 - todos
    // ------------------- JDBC 3.0 -------------------------

    /**
     * Retrieves whether this database supports savepoints. <p>
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * Beginning with 1.7.1, this SQL feature is supported. Always returns
     * false, as savepoint access is not through JDBC.<p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     *
     * @return <code>true</code> if savepoints are supported;
     *        <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     * @since JDK 1.4, HSQLDB 1.7
     */
//#ifdef JDBC3
/*
    public boolean supportsSavepoints() throws SQLException {

        // TODO: fredt@users, I will incorporate the patch but this one should
        // simply return true regardless because an SQL feature is
        // concerned not a JDBC one
        // we need to decide if the new HSQLDB savepoint feature will be
        // supported directly in JDBC and modify this accordingly, based
        // on build variables.  The patch is available on Patch 546431.
        // boucherb@users 20020426
        // return true; // if built with jdbcSavepoint, else false?.
        // fredt - until we use the savepoint JDBC patch return false as
        // JDBC specs say so (contrary to the impression given in JavaDoc)
        return false;
    }


*/

//#endif JDBC3

    /**
     * Retrieves whether this database supports named parameters to callable
     * statements. <p>
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * 1.7.1 always returns false. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     *
     * @return <code>true</code> if named parameters are supported;
     *        <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     * @since JDK 1.4, HSQLDB 1.7
     */
//#ifdef JDBC3
/*
    public boolean supportsNamedParameters() throws SQLException {

        // TODO: fredt@users - sure
        // we should probably return false here for now, instead of throwing
        // boucherb@users 20020426
        // return false;
        return false;
    }


*/

//#endif JDBC3

    /**
     * Retrieves whether it is possible to have multiple <code>ResultSet</code>
     * objects returned from a <code>CallableStatement</code> object
     * simultaneously. <p>
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * 1.7.1 always returns false. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     *
     * @return <code>true</code> if a <code>CallableStatement</code> object
     *        can return multiple <code>ResultSet</code> objects
     *        simultaneously; <code>false</code> otherwise
     * @exception SQLException if a datanase access error occurs
     * @since JDK 1.4, HSQLDB 1.7
     */
//#ifdef JDBC3
/*
    public boolean supportsMultipleOpenResults() throws SQLException {

        // TODO: fredt@users  - agreed on both
        // we could support true quite easily
        // we should probably return false here for now, instead of throwing
        // boucherb@users 20020426
        // return false;
        return false;
    }


*/

//#endif JDBC3

    /**
     * Retrieves whether auto-generated keys can be retrieved after
     * a statement has been executed. <p>
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * 1.7.1 always returns false. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     *
     * @return <code>true</code> if auto-generated keys can be retrieved
     *        after a statement has executed; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     * @since JDK 1.4, HSQLDB 1.7
     */
//#ifdef JDBC3
/*
    public boolean supportsGetGeneratedKeys() throws SQLException {

        // TODO: fredt@users - agreed
        // we should probably return false here for now, instead of throwing
        // boucherb@users 20020426
        return false;
    }


*/

//#endif JDBC3

    /**
     * Retrieves a description of the user-defined type (UDT) hierarchies defined in a
     * particular schema in this database. Only the immediate super type
     * sub type relationship is modeled.
     * <P>
     * Only supertype information for UDTs matching the catalog,
     * schema, and type name is returned. The type name parameter
     * may be a fully-qualified name. When the UDT name supplied is a
     * fully-qualified name, the catalog and schemaPattern parameters are
     * ignored.
     * <P>
     * If a UDT does not have a direct super type, it is not listed here.
     * A row of the <code>ResultSet</code> object returned by this method
     * describes the designated UDT and a direct supertype. A row has the following
     * columns:
     * <OL>
     * <LI><B>TYPE_CAT</B> String => the UDT's catalog (may be <code>null</code>)
     * <LI><B>TYPE_SCHEM</B> String => UDT's schema (may be <code>null</code>)
     * <LI><B>TYPE_NAME</B> String => type name of the UDT
     * <LI><B>SUPERTYPE_CAT</B> String => the direct super type's catalog
     *                          (may be <code>null</code>)
     * <LI><B>SUPERTYPE_SCHEM</B> String => the direct super type's schema
     *                            (may be <code>null</code>)
     * <LI><B>SUPERTYPE_NAME</B> String => the direct super type's name
     * </OL>
     *
     * <P><B>Note:</B> If the driver does not support type hierarchies, an
     * empty result set is returned. <p>
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * Up to and including 1.7.1, this JDBC feature is not supported. <p>
     *
     * Calling this method throws a SQLException stating that the function
     * is not supported.
     * </span>
     * <!-- end release-specific documentation -->
     *
     *
     * @param catalog a catalog name; "" retrieves those without a catalog;
     *       <code>null</code> means drop catalog name from the selection criteria
     * @param schemaPattern a schema name pattern; "" retrieves those
     *       without a schema
     * @param typeNamePattern a UDT name pattern; may be a fully-qualified
     *       name
     * @return a <code>ResultSet</code> object in which a row gives information
     *        about the designated UDT
     * @throws SQLException if a database access error occurs
     * @since JDK 1.4, HSQLDB 1.7
     */
//#ifdef JDBC3
/*
    public ResultSet getSuperTypes(String catalog, String schemaPattern,
                                   String typeNamePattern)
                                       throws SQLException {

        // TODO: fredt@users - agreed, easy to implement :)
        // we could easily provide an empty table in DatabaseInformation or
        // even build an empty result of the correct type here, instead of
        // throwing. This is preferable for JDBC3 enabled reporting tools.
        // boucherb@users 20020426
        // return cConnection.execute("SELECT * FROM SYSTEM_SUPERTYPES");
        throw Trace.error(Trace.FUNCTION_NOT_SUPPORTED, "JDBC3");
    }


*/

//#endif JDBC3

    /**
     * Retrieves a description of the table hierarchies defined in a particular
     * schema in this database.
     *
     * <P>Only supertable information for tables matching the catalog, schema
     * and table name are returned. The table name parameter may be a fully-
     * qualified name, in which case, the catalog and schemaPattern parameters
     * are ignored. If a table does not have a super table, it is not listed here.
     * Supertables have to be defined in the same catalog and schema as the
     * sub tables. Therefore, the type description does not need to include
     * this information for the supertable.
     *
     * <P>Each type description has the following columns:
     * <OL>
     * <LI><B>TABLE_CAT</B> String => the type's catalog (may be <code>null</code>)
     * <LI><B>TABLE_SCHEM</B> String => type's schema (may be <code>null</code>)
     * <LI><B>TABLE_NAME</B> String => type name
     * <LI><B>SUPERTABLE_NAME</B> String => the direct super type's name
     * </OL>
     *
     * <P><B>Note:</B> If the driver does not support type hierarchies, an
     * empty result set is returned. <p>
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * Up to and including 1.7.1, this JDBC feature is not supported. <p>
     *
     * Calling this method throws a SQLException stating that the function
     * is not supported.
     * </span>
     * <!-- end release-specific documentation -->
     *
     *
     * @param catalog a catalog name; "" retrieves those without a catalog;
     *       <code>null</code> means drop catalog name from the selection criteria
     * @param schemaPattern a schema name pattern; "" retrieves those
     *       without a schema
     * @param tableNamePattern a table name pattern; may be a fully-qualified
     *       name
     * @return a <code>ResultSet</code> object in which each row is a type description
     * @throws SQLException if a database access error occurs
     * @since JDK 1.4, HSQLDB 1.7
     */
//#ifdef JDBC3
/*
    public ResultSet getSuperTables(String catalog, String schemaPattern,
                                    String tableNamePattern)
                                        throws SQLException {

        // TODO: fredt@users - as the method above
        // we could easily provide an empty table in DatabaseInformation or
        // even build an empty result of the correct type here, instead of
        // throwing.  This is preferable for JDBC3 enabled reporting tools.
        // boucherb@users 20020426
        // return cConnection.execute("SELECT * FROM SYSTEM_SUPERTABLES");
        throw Trace.error(Trace.FUNCTION_NOT_SUPPORTED, "JDBC3");
    }


*/

//#endif JDBC3

    /**
     * Retrieves a description of the given attribute of the given type
     * for a user-defined type (UDT) that is available in the given schema
     * and catalog.
     * <P>
     * Descriptions are returned only for attributes of UDTs matching the
     * catalog, schema, type, and attribute name criteria. They are ordered by
     * TYPE_SCHEM, TYPE_NAME and ORDINAL_POSITION. This description
     * does not contain inherited attributes.
     * <P>
     * The <code>ResultSet</code> object that is returned has the following
     * columns:
     * <OL>
     * <LI><B>TYPE_CAT</B> String => type catalog (may be <code>null</code>)
     *  <LI><B>TYPE_SCHEM</B> String => type schema (may be <code>null</code>)
     *  <LI><B>TYPE_NAME</B> String => type name
     *  <LI><B>ATTR_NAME</B> String => attribute name
     *  <LI><B>DATA_TYPE</B> short => attribute type SQL type from java.sql.Types
     *  <LI><B>ATTR_TYPE_NAME</B> String => Data source dependent type name.
     * For a UDT, the type name is fully qualified. For a REF, the type name is
     * fully qualified and represents the target type of the reference type.
     *  <LI><B>ATTR_SIZE</B> int => column size.  For char or date
     *      types this is the maximum number of characters; for numeric or
     *      decimal types this is precision.
     *  <LI><B>DECIMAL_DIGITS</B> int => the number of fractional digits
     *  <LI><B>NUM_PREC_RADIX</B> int => Radix (typically either 10 or 2)
     *  <LI><B>NULLABLE</B> int => whether NULL is allowed
     *     <UL>
     *     <LI> attributeNoNulls - might not allow NULL values
     *     <LI> attributeNullable - definitely allows NULL values
     *     <LI> attributeNullableUnknown - nullability unknown
     *     </UL>
     *  <LI><B>REMARKS</B> String => comment describing column (may be <code>null</code>)
     *  <LI><B>ATTR_DEF</B> String => default value (may be <code>null</code>)
     *  <LI><B>SQL_DATA_TYPE</B> int => unused
     *  <LI><B>SQL_DATETIME_SUB</B> int => unused
     *  <LI><B>CHAR_OCTET_LENGTH</B> int => for char types the
     *      maximum number of bytes in the column
     *  <LI><B>ORDINAL_POSITION</B> int => index of column in table
     *     (starting at 1)
     *  <LI><B>IS_NULLABLE</B> String => "NO" means column definitely
     *     does not allow NULL values; "YES" means the column might
     *     allow NULL values.  An empty string means unknown.
     * <LI><B>SCOPE_CATALOG</B> String => catalog of table that is the
     *     scope of a reference attribute (<code>null</code> if DATA_TYPE isn't REF)
     * <LI><B>SCOPE_SCHEMA</B> String => schema of table that is the
     *     scope of a reference attribute (<code>null</code> if DATA_TYPE isn't REF)
     * <LI><B>SCOPE_TABLE</B> String => table name that is the scope of a
     *     reference attribute (<code>null</code> if the DATA_TYPE isn't REF)
     * <LI><B>SOURCE_DATA_TYPE</B> short => source type of a distinct type or user-generated
     *     Ref type,SQL type from java.sql.Types (<code>null</code> if DATA_TYPE
     *     isn't DISTINCT or user-generated REF)
     * </OL> <p>
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * Up to and including 1.7.1, this JDBC feature is not supported. <p>
     *
     * Calling this method throws a SQLException stating that the function
     * is not supported.
     * </span>
     * <!-- end release-specific documentation -->
     *
     *
     * @param catalog a catalog name; must match the catalog name as it
     *       is stored in the database; "" retrieves those without a catalog;
     *       <code>null</code> means that the catalog name should not be used to narrow
     *       the search
     * @param schemaPattern a schema name pattern; must match the schema name
     *       as it is stored in the database; "" retrieves those without a schema;
     *       <code>null</code> means that the schema name should not be used to narrow
     *       the search
     * @param typeNamePattern a type name pattern; must match the
     *       type name as it is stored in the database
     * @param attributeNamePattern an attribute name pattern; must match the attribute
     *       name as it is declared in the database
     * @return a <code>ResultSet</code> object in which each row is an
     *        attribute description
     * @exception SQLException if a database access error occurs
     * @since JDK 1.4, HSQLDB 1.7
     */
//#ifdef JDBC3
/*
    public ResultSet getAttributes(String catalog, String schemaPattern,
                                   String typeNamePattern,
                                   String attributeNamePattern)
                                       throws SQLException {

        // TODO: fredt@users ditto
        // we could easily provide an empty table in DatabaseInformation or
        // even build an empty result of the correct type here, instead of
        // throwing. This is preferable for JDBC3 enabled reporting tools.
        // boucherb@users 20020426
        // return cConnection.execute("SELECT * FROM SYSTEM_ATTRIBUTES");
        throw Trace.error(Trace.FUNCTION_NOT_SUPPORTED, "JDBC3");
    }


*/

//#endif JDBC3

    /**
     * Retrieves whether this database supports the given result
     * set holdability. <p>
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * 1.7.1 always returns false. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     *
     * @param holdability one of the following constants:
     *         <code>ResultSet.HOLD_CURSORS_OVER_COMMIT</code> or
     *         <code>ResultSet.CLOSE_CURSORS_AT_COMMIT</code>
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     * @see jdbcConnection
     * @since JDK 1.4, HSQLDB 1.7
     */
//#ifdef JDBC3
/*
    public boolean supportsResultSetHoldability(int holdability)
            throws SQLException {

        // TODO: fredt@users - sure
        // we should probably return false here
        // for now, instead of throwing.
        // boucherb@users 20020426
        // return false;
        return false;
    }


*/

//#endif JDBC3

    /**
     * Retrieves the default holdability of this <code>ResultSet</code>
     * object. <p>
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * Up to and including 1.7.1, this JDBC feature is not supported. <p>
     *
     * Calling this method throws a SQLException stating that the function
     * is not supported.
     * </span>
     * <!-- end release-specific documentation -->
     *
     *
     * @return the default holdability; either
     *        <code>ResultSet.HOLD_CURSORS_OVER_COMMIT</code> or
     *        <code>ResultSet.CLOSE_CURSORS_AT_COMMIT</code>
     * @exception SQLException if a database access error occurs
     * @since JDK 1.4, HSQLDB 1.7
     */
//#ifdef JDBC3
/*
    public int getResultSetHoldability() throws SQLException {

        // TODO: fredt@users - if we don't support holdability how can we
        // say which type we support? Let's leave it for now.
        // we might return ResultSet.CLOSE_CURSORS_AT_COMMIT here
        // , instead of throwing.  Must check what we actually do.
        // boucherb@users 20020426
        // return ResultSet.CLOSE_CURSORS_AT_COMMIT; // ???
        throw Trace.error(Trace.FUNCTION_NOT_SUPPORTED, "JDBC3");
    }


*/

//#endif JDBC3

    /**
     * Retrieves the major version number of the underlying database. <p>
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * Up to and including 1.7.1, this JDBC feature is not supported. <p>
     *
     * Calling this method throws a SQLException stating that the function
     * is not supported.
     * </span>
     * <!-- end release-specific documentation -->
     *
     *
     * @return the underlying database's major version
     * @exception SQLException if a database access error occurs
     * @since JDK 1.4, HSQLDB 1.7
     */
//#ifdef JDBC3
/*
    public int getDatabaseMajorVersion() throws SQLException {

        // TODO: fredt@users - why not return the DB version we have?
        // ANSWER: boucherb@users because our drivers will be able to talk to
        // different database versions after DIXXX merge, so we should not
        // hard code a value here, but rather call a DIJdbcLibrary method to
        // query the (possibly remote) engine.
        // implement in 1.7.1 when DIXXX classes are merged
        // boucherb@users 20020426
        throw Trace.error(Trace.FUNCTION_NOT_SUPPORTED, "JDBC3");
    }


*/

//#endif JDBC3

    /**
     * Retrieves the minor version number of the underlying database. <p>
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * Up to and including 1.7.1, this JDBC feature is not supported. <p>
     *
     * Calling this method throws a SQLException stating that the function
     * is not supported.
     * </span>
     * <!-- end release-specific documentation -->
     *
     *
     * @return underlying database's minor version
     * @exception SQLException if a database access error occurs
     * @since JDK 1.4, HSQLDB 1.7
     */
//#ifdef JDBC3
/*
    public int getDatabaseMinorVersion() throws SQLException {

        // TODO: fredt@users - ditto
        // implement in 1.7.1 when DIXXX classes are merged
        // boucherb@users 20020426
        throw Trace.error(Trace.FUNCTION_NOT_SUPPORTED, "JDBC3");
    }


*/

//#endif JDBC3

    /**
     * Retrieves the major JDBC version number for this
     * driver. <p>
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * Up to and including 1.7.1, this JDBC feature is not supported. <p>
     *
     * Calling this method throws a SQLException stating that the function
     * is not supported.
     * </span>
     * <!-- end release-specific documentation -->
     *
     *
     * @return JDBC version major number
     * @exception SQLException if a database access error occurs
     * @since JDK 1.4, HSQLDB 1.7
     */
//#ifdef JDBC3
/*
    public int getJDBCMajorVersion() throws SQLException {

        // TODO: fredt@users - I think they mean 1, 2 or 3 and are planning
        // to have 3.1 etc. in the future.
        // What does this mean?  The major version of JDBC this driver supports?
        // boucherb@users 20020426
        throw Trace.error(Trace.FUNCTION_NOT_SUPPORTED, "JDBC3");
    }


*/

//#endif JDBC3

    /**
     * Retrieves the minor JDBC version number for this
     * driver. <p>
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * Up to and including 1.7.1, this JDBC feature is not supported. <p>
     *
     * Calling this method throws a SQLException stating that the function
     * is not supported.
     * </span>
     * <!-- end release-specific documentation -->
     *
     *
     * @return JDBC version minor number
     * @exception SQLException if a database access error occurs
     * @since JDK 1.4, HSQLDB 1.7
     */
//#ifdef JDBC3
/*
    public int getJDBCMinorVersion() throws SQLException {

        // TODO: fredt@users - see above
        // What does this mean?  The minor version of JDBC this driver supports?
        // boucherb@users 20020426
        throw Trace.error(Trace.FUNCTION_NOT_SUPPORTED, "JDBC3");
    }


*/

//#endif JDBC3

    /**
     * Indicates whether the SQLSTATEs returned by <code>SQLException.getSQLState</code>
     * is X/Open (now known as Open Group) SQL CLI or SQL99. <p>
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * Up to and including 1.7.1, this JDBC feature is not supported. <p>
     *
     * Calling this method throws a SQLException stating that the function
     * is not supported.
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @return the type of SQLSTATEs, one of:
     *       sqlStateXOpen or
     *       sqlStateSQL99
     * @throws SQLException if a database access error occurs
     * @since JDK 1.4, HSQLDB 1.7
     */
//#ifdef JDBC3
/*
    public int getSQLStateType() throws SQLException {

        // TODO: fredt@users we don't really. Need to review the codes.
        // Which do we support, if any? Probably X/OPEN, if any. Must check.
        // boucherb@users 20020426
        throw Trace.error(Trace.FUNCTION_NOT_SUPPORTED, "JDBC3");
    }


*/

//#endif JDBC3

    /**
     * Indicates whether updates made to a LOB are made on a copy or directly
     * to the LOB. <p>
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * Up to and including 1.7.1, HSQLDB updates the LOB directly. This
     * method return false.<p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @return <code>true</code> if updates are made to a copy of the LOB;
     *        <code>false</code> if updates are made directly to the LOB
     * @throws SQLException if a database access error occurs
     * @since JDK 1.4, HSQLDB 1.7
     */
//#ifdef JDBC3
/*
    public boolean locatorsUpdateCopy() throws SQLException {

        // TODO: fredt@users - agreed
        // we should probably return false here for now, instead of throwing;
        // we update LONGBINARY and LONGVARCHAR directly.
        // boucherb@users 20020426
        // return false;
        return false;
    }


*/

//#endif JDBC3

    /**
     * Retrieves whether this database supports statement pooling. <p>
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * Up to and including 1.7.1, HSQLDB does not support statement pooling.
     * This method returns false. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     * @return <code>true</code> is so;
     * <code>false</code> otherwise
     * @throws SQLException if a database access error occurs
     * @since JDK 1.4, HSQLDB 1.7
     */
//#ifdef JDBC3
/*
    public boolean supportsStatementPooling() throws SQLException {

        // TODO:
        // we should probably return false here
        // for now, instead of throwing.
        // boucherb@users 20020426
        // return false;
        return false;
    }


*/

//#endif JDBC3
    //----------------------- Internal Implementation --------------------------

    /**
     * Constructs a new jdbcDatabaseMetaData object using the
     * specified connection.  This contructor is used by
     * <code>jdbcConnection</code> when producing a DatabaseMetaData object
     * from a call to {@link jdbcConnection#getMetaData() getMetaData}
     *
     * @param  c the connection this object will use to retrieve
     *         instance-specific metadata
     */
    jdbcDatabaseMetaData(jdbcConnection c) {
        cConnection = c;
    }

    /**
     * Executes a select statement using the specified table and where clause.
     *
     * @param table the table to select from
     * @param where the where clause to use in the selection
     * @return the result of executing the selection
     * @throws SQLException where a database access error occurs
     */
    private ResultSet executeSelect(String table,
                                    String where) throws SQLException {

        if (where != null) {
            table += " WHERE " + where;
        }

        return cConnection.execute("SELECT * FROM " + table);
    }

    /**
     * Builds a CSV list from the specified array of array of String
     * objects. <p>
     *
     * This method is used to build the lists returned by the getXXXFunctions
     * methods.
     *
     * @param s the array of array of String objects from which to build a list
     * @return a CSV list
     */
    private static String getList(String[][] s) {

        StringBuffer b = new StringBuffer(s.length * 16);

        for (int i = 0; i < s.length; i++) {
            b.append(s[i][0]);
            b.append(',');

            // fredt@users 20020225 - patch 1.7.0 - don't return Java function names
            //          b.append(s[i][1]);
            //          b.append(',');
        }

        b.setLength(b.length() - 1);

        return b.toString();
    }

    /**
     * Builds a CSV list from the specified array of String
     * objects. <p>
     *
     * @param s the array of array of String objects from which to build a list
     * @return a CSV list
     */
    private static String getList(String[] s, boolean quote) {

        StringBuffer b = new StringBuffer(s.length * 16);

        for (int i = 0; i < s.length; i++) {
            if (quote) {
                b.append('\'');
            }

            b.append(s[i]);

            if (quote) {
                b.append('\'');
            }

            b.append(',');

            // fredt@users 20020225 - patch 1.7.0 - don't return Java function names
            //          b.append(s[i][1]);
            //          b.append(',');
        }

        b.setLength(b.length() - 1);

        return b.toString();
    }

    /**
     * Catenates the specified "where" clause with the specified String s,
     * inserting an " AND " conjunction between them.
     *
     * @param where the "where" clause to append
     * @param s the string to append
     * @return where + " AND " + s
     */
    private String addWhere(String where, String s) {

        if (where == null) {
            return s;
        }

        return where + " AND " + s;
    }
}
