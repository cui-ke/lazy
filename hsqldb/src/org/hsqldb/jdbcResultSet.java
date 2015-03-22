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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.math.BigDecimal;
import java.sql.*;     // for Array, Blob, Clob, Ref
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.sql.SQLWarning;
import java.util.*;    // for Map
import java.util.Calendar;
import org.hsqldb.lib.AsciiStringInputStream;
import org.hsqldb.lib.StringInputStream;

// fredt@users 20020320 - patch 1.7.0 - JDBC 2 support and error trapping
// JDBC 2 methods can now be called from jdk 1.1.x - see javadoc comments
// SCROLL_INSENSITIVE and FORWARD_ONLY types for ResultSet are now supported
// fredt@users 20020315 - patch 497714 by lakuhns@users - scrollable ResultSet
// all absolute and relative positioning methods defined
// boucherb@users 20020409 - added "throws SQLException" to all methods where
// it was missing here but specified in the java.sql.ResultSet and
// java.sql.ResultSetMetaData interfaces, updated generic documentation to
// JDK 1.4, and added JDBC3 methods and docs
// boucherb@users and fredt@users 20020409/20020505 extensive review and update
// of docs and behaviour to comply with previous and latest java.sql specification
// tony_lai@users 20020820 - patch 595073 by tlai@users - duplicated exception msg

/**
 * Implements both the <CODE>java.sql.ResultSet</CODE> and
 * <CODE>java.sql.ResultSetMetaData</CODE> interfaces. <p>
 *
 * <span class="ReleaseSpecificDocumentation">
 * In short: <p>
 *
 * <UL>
 * <LI>A <code>ResultSet</code> object is essentially--but not limited to
 *    being--a table of data representing a database result set, which
 *    is usually generated by executing a statement that queries the
 *    database.</LI>
 * <LI>A <CODE>ResultSetMetaData</CODE> object is one that can be used to
 *    get information about the types and properties of the columns in a
 *    <code>ResultSet</code> object.</LI>
 * </UL>
 * <p>
 *
 * The following is composed of three sections:
 * <OL>
 * <LI>The generic overview for <CODE>ResultSet</CODE>.</LI>
 * <LI>The generic overview for <CODE>ResultSetMetaData</CODE>.</LI>
 * <LI>A discussion of some HSQLDB-specific concerns.</LI>
 * </OL>
 * </span> <p>
 * <!-- end Release-specific documentation -->
 *
 * <!-- start java.sql.ResultSet generaic documentation -->
 * <B>From <CODE>ResultSet</CODE>:</B><p>
 *
 * A table of data representing a database result set, which
 * is usually generated by executing a statement that queries the database.
 *
 * <P>A <code>ResultSet</code> object  maintains a cursor pointing
 * to its current row of data.  Initially the cursor is positioned
 * before the first row. The <code>next</code> method moves the
 * cursor to the next row, and because it returns <code>false</code>
 * when there are no more rows in the <code>ResultSet</code> object,
 * it can be used in a <code>while</code> loop to iterate through
 * the result set.
 * <P>
 * A default <code>ResultSet</code> object is not updatable and
 * has a cursor that moves forward only.  Thus, you can
 * iterate through it only once and only from the first row to the
 * last row. It is possible to
 * produce <code>ResultSet</code> objects that are scrollable and/or
 * updatable.  The following code fragment, in which <code>con</code>
 * is a valid <code>Connection</code> object, illustrates how to make
 * a result set that is scrollable and insensitive to updates by others,
 * and that is updatable. See <code>ResultSet</code> fields for other
 * options.
 * <PRE>
 *
 * Statement stmt = con.createStatement(
 *                            ResultSet.TYPE_SCROLL_INSENSITIVE,
 *                            ResultSet.CONCUR_UPDATABLE);
 * ResultSet rs = stmt.executeQuery("SELECT a, b FROM TABLE2");
 * // rs will be scrollable, will not show changes made by others,
 * // and will be updatable
 *
 * </PRE>
 * The <code>ResultSet</code> interface provides
 * <i>getter</i> methods (<code>getBoolean</code>, <code>getLong</code>,
 * and so on) for retrieving column values from the current row.
 * Values can be retrieved using either the index number of the
 * column or the name of the column.  In general, using the
 * column index will be more efficient.  Columns are numbered from 1.
 * For maximum portability, result set columns within each row should be
 * read in left-to-right order, and each column should be read only once.
 *
 * <P>For the getter methods, a JDBC driver attempts
 * to convert the underlying data to the Java type specified in the
 * getter method and returns a suitable Java value.  The JDBC specification
 * has a table showing the allowable mappings from SQL types to Java types
 * that can be used by the <code>ResultSet</code> getter methods.
 * <P>
 * <P>Column names used as input to getter methods are case
 * insensitive.  When a getter method is called  with
 * a column name and several columns have the same name,
 * the value of the first matching column will be returned.
 * The column name option is
 * designed to be used when column names are used in the SQL
 * query that generated the result set.
 * For columns that are NOT explicitly named in the query, it
 * is best to use column numbers. If column names are used, there is
 * no way for the programmer to guarantee that they actually refer to
 * the intended columns.
 * <P>
 * A set of updater methods were added to this interface
 * in the JDBC 2.0 API (Java<sup><font size=-2>TM</font></sup> 2 SDK,
 * Standard Edition, version 1.2). The comments regarding parameters
 * to the getter methods also apply to parameters to the
 * updater methods.
 * <P>
 * The updater methods may be used in two ways:
 * <ol>
 * <LI>to update a column value in the current row.  In a scrollable
 * <code>ResultSet</code> object, the cursor can be moved backwards
 * and forwards, to an absolute position, or to a position
 * relative to the current row.
 * The following code fragment updates the <code>NAME</code> column
 * in the fifth row of the <code>ResultSet</code> object
 * <code>rs</code> and then uses the method <code>updateRow</code>
 * to update the data source table from which <code>rs</code> was
 * derived.
 * <PRE>
 *
 * rs.absolute(5); // moves the cursor to the fifth row of rs
 * rs.updateString("NAME", "AINSWORTH"); // updates the
 * // <code>NAME</code> column of row 5 to be <code>AINSWORTH</code>
 * rs.updateRow(); // updates the row in the data source
 *
 * </PRE>
 * <LI>to insert column values into the insert row.  An updatable
 * <code>ResultSet</code> object has a special row associated with
 * it that serves as a staging area for building a row to be inserted.
 * The following code fragment moves the cursor to the insert row, builds
 * a three-column row, and inserts it into <code>rs</code> and into
 * the data source table using the method <code>insertRow</code>.
 * <PRE>
 *
 * rs.moveToInsertRow(); // moves cursor to the insert row
 * rs.updateString(1, "AINSWORTH"); // updates the
 * // first column of the insert row to be <code>AINSWORTH</code>
 * rs.updateInt(2,35); // updates the second column to be <code>35</code>
 * rs.updateBoolean(3, true); // updates the third row to <code>true</code>
 * rs.insertRow();
 * rs.moveToCurrentRow();
 *
 * </PRE>
 * </ol>
 * <P>A <code>ResultSet</code> object is automatically closed when the
 * <code>Statement</code> object that
 * generated it is closed, re-executed, or used
 * to retrieve the next result from a sequence of multiple results.
 *
 * <P>The number, types and properties of a <code>ResultSet</code>
 * object's columns are provided by the <code>ResulSetMetaData</code>
 * object returned by the <code>ResultSet.getMetaData</code> method. <p>
 * <!-- end java.sql.ResultSet generic documentation -->
 *
 * <!-- start java.sql.ResultSetMetaData generic documentation-->
 * <B>From <CODE>ResultSetMetaData</CODE>:</B><p>
 *
 * An object that can be used to get information about the types
 * and properties of the columns in a <code>ResultSet</code> object.
 * The following code fragment creates the <code>ResultSet</code>
 * object rs, creates the <code>ResultSetMetaData</code> object rsmd,
 * and uses rsmd
 * to find out how many columns rs has and whether the first column in rs
 * can be used in a <code>WHERE</code> clause.
 * <PRE>
 *
 * ResultSet rs = stmt.executeQuery("SELECT a, b, c FROM TABLE2");
 * ResultSetMetaData rsmd = rs.getMetaData();
 * int numberOfColumns = rsmd.getColumnCount();
 * boolean b = rsmd.isSearchable(1);
 *
 * </PRE>
 * <!-- end generic documentation -->
 *
 * <!-- start release-specific documentation -->
 * <span class="ReleaseSpecificDocumentation">
 * <B>HSQLDB-Specific Information:</B> <p>
 *
 * As stated above, <CODE>jdbcResultSet</CODE> implements both the
 * <CODE>ResultSet</CODE> and <CODE>ResultSetMetaData</CODE> interfaces.
 * However, to gain access to the interface methods of
 * <CODE>ResultSetMetaData</CODE> in a driver independent way, the
 * traditional call to the {@link #getMetaData getMetaData} method should
 * be used, rather than casting objects known to be of type
 * <CODE>jdbcResultSet</CODE> to type <CODE>ResultSetMetaData</CODE>. <p>
 *
 * A <code>ResultSet</code> object generated by HSQLDB is, as is standard
 * JDBC behavior, by default of <code>ResultSet.TYPE_FORWARD_ONLY</code>
 * and does not allow the use of absolute and relative positioning
 * methods.  However, starting with 1.7.0, if a statement is created
 * with:<p>
 *
 * <code class="JavaCodeExample">
 * Statement stmt createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
 *                                ResultSet.CONCUR_READ_ONLY);
 * </code> <p>
 *
 * then the <CODE>ResultSet</CODE> objects it produces support
 * using all of  the absolute and relative positioning methods of JDBC2
 * to set the position of the current row, for example:<p>
 *
 * <code class="JavaCodeExample">
 * rs.absolute(5);<br>
 * String fifthRowValue = rs.getString(1);<br>
 * rs.relative(4);<br>
 * String ninthRowValue = rs.getString(1);<br>
 * </code>
 * <p>
 *
 * Note: An HSQLDB <code>ResultSet</code> object persists, even after its
 * connection is closed.  This is regardless of the operational mode of
 * the {@link Database Database} from which it came.  That is, they
 * persist whether originating from a <CODE>Server</CODE>,
 * <CODE>WebServer</CODE> or in-process mode <CODE>Database.</CODE>
 * <p>
 *
 * Up to and including HSQLDB 1.7.0, there is no support for any of
 * the methods introduced in JDBC 2 relating to updateable result sets.
 * These methods include all updateXXX methods, as well as the
 * {@link #insertRow}, {@link #updateRow}, {@link #deleteRow},
 * {@link #moveToInsertRow} (and so on) methods.  A call to any such
 * unsupported method will simply result in throwing a
 * <CODE>SQLException</CODE> which states that the function is not
 * supported.  It is not anticipated that HSQLDB-native support for
 * updateable <CODE>ResultSet</CODE> objects will be introduced in the
 * HSQLDB 1.7.x series.  Such features <I>may</I> be part of the
 * HSQLDB 2.x series, but no decisions have been made at this point.<p>
 *
 * <b>JRE 1.1.x Notes:</b> <p>
 *
 * In general, JDBC 2 support requires Java 1.2 and above, and JDBC 3 requires
 * Java 1.4 and above. In HSQLDB, support for methods introduced in different
 * versions of JDBC depends on the JDK version used for compiling and building
 * HSQLDB.<p>
 *
 * Since 1.7.0, it is possible to build the product so that
 * all JDBC 2 methods can be called while executing under the version 1.1.x
 * <em>Java Runtime Environment</em><sup><font size="-2">TM</font></sup>.
 * However, some of these method calls require <code>int</code> values that
 * are defined only in the JDBC 2 or greater version of the
 * <a href="http://java.sun.com/j2se/1.4/docs/api/java/sql/ResultSet.html">
 * <CODE>ResultSet</CODE></a> interface.  For this reason, when the
 * product is compiled under JDK 1.1.x, these values are defined in
 * here, in this class. <p>
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
 * <b>ResultSetMetaData Implementation Notes:</b> <p>
 *
 * HSQLDB supports a subset of <code>ResultSetMetaData</code> interface.
 * The JDBC specification for <code>ResultSetMetaData</code> is in part very
 * vague. Several methods are exclusively for columns that are database
 * table columns. There is a complete lack of specification on how these
 * methods are supposed to distinguish between updatable and non-updatable
 * <code>ResultSet</code> objects or between columns that are database
 * table columns and those that are results of calculations or functions.
 * This causes potential incompatibility between interpretations of the
 * specifications in different JDBC drivers.<p>
 *
 * As such, <code>DatabaseMetadata</code> reporting will be enhanced
 * in future 1.7.x and greater versions, but enhancements to reporting
 * <code>ResultSetMetaData</code> have to be considered carefully as they
 * impose a performance penalty on all <code>ResultSet</code> objects
 * returned from HSQLDB, whether or not the <code>ResultSetMetaData</code>
 * methods are used.<p>
 *
 * (fredt@users) <br>
 * (boucherb@users)<p>
 *
 * </span>
 * @see jdbcStatement#executeQuery
 * @see jdbcStatement#getResultSet
 * @see <a href=
 * "http://java.sun.com/j2se/1.4/docs/api/java/sql/ResultSetMetaData.html">
 * <CODE>ResultSetMetaData</CODE></a>
 */
public class jdbcResultSet implements ResultSet, ResultSetMetaData {

// fredt@users 20020320 - patch 497714 by lakuhns@users - scrollable ResultSet
// variable values in different states
// Condition definitions
//                  bInit  iCurrentRow  nCurrent  nCurrent.next
//                  -----  -----------  --------  -------------
// beforeFirst      false       0         N/A          N/A
// first            true        1        !null    next or null
// last             true    last row #   !null        null
// afterLast        true        0        !null         N/A
    //------------------------ Private Attributes --------------------------
/*
 * Future Development Information for Developers and Contributors<p>
 * Providing a
 * full and robust implementation guaranteeing consistently accurate
 * results and behaviour depends upon introducing several new engine
 * features for which the internals of the product currently have no
 * infrastructure: <p>
 *
 * <OL>
 * <LI>a unique rowid for each row in the database which lasts the life
 *  of a row, independent of any updates made to that row</LI>
 * <LI>the ability to explicitly lock either the tables or the
 *  individual rows of an updateable result, for the duration that
 *  the result is open</LI>
 * <LI>the ability to choose between transactions supporting repeatable
 *  reads, committed reads, and uncommitted reads
 * <LI>the ability to map an updated result row's columns back to
 *  specific updateable objects on the database.<p>
 *
 *  <B>Note:</B> Typically, it is easy to do this mapping if all the
 *  rows of a result consist of columns from a single table.  And it
 *  is especially easy if the result's columns are a superset of the
 *  primary key columns of that table.  The ability to
 *  update a result consisting of any combintation of join, union,
 *  intersect, difference and grouping operations, however, is much more
 *  complex to implement and often impossible, especially under
 *  grouping and non-natural joins.  Also, it is not even guaranteed
 *  that the columns of a result map back to *any* updateable object
 *  on the database, for instance in the cases where the
 *  result's column values are general expressions or the result
 *  comes from a stored procedure where the data may not even come,
 *  directly or indirectly, from updateable database objects such as
 *  columns in table rows.
 * </OL>
 *
 * For developers working under a JDBC3 environment,
 * it is gently recommended to take a look at Sun's early access
 * <a href="http://developer.java.sun.com/developer/earlyAccess/crs/">
 * <CODE>RowSet</CODE></a> implementation, as this can be used to add
 * JDBC driver independent scrollablility and updateability.
 * However, as a driver independent implementation, it obviously cannot
 * guarantee to use the traditional table and/or row locking features
 * that many DBMS make available to ensure the success of all
 * valid updates against updateable results sets.  As such, performing
 * updates through Sun's early access <CODE>RowSet</CODE> implementation
 * may not always succeed, even when it is generally expected that they
 * should.  This is because the condition used to find the original row
 * on the database to update (which, for a driver independent
 * implementation, would have to be equality on all columns values of
 * the originally retrieved row) can become invalid if another
 * transaction modifies or deletes that row on the database at some
 * point between the time the row was last retrieved or refreshed in
 * the RowSet and the time the RowSet attempts to make its next
 * update to that row.  Also, any driver independent implementation
 * of RowSet is still dependent on each driver guaranteeing that its
 * <CODE>ResultSet</CODE> objects return completely accurate
 * <CODE>ResultSetMetaData</CODE> that fulfills all of the
 * JDBC <CODE>ResultSetMetaData</CODE> contracts under all circumstances.
 * However, up to and including 1.7.1, HSQLDB does not make such guarantees
 * under all conditions. See the discussion at {@link #getMetaData}.
 * (boucherb@users)<p>
*/

    /**
     * The internal representation.  Basically, a linked list of records,
     * each containing an Object[] payload representing the data for a row,
     * plus some metadata.
     */
    private Result rResult;

    /**
     * The record containing the data for the row, if any,
     * currently positioned on.
     */
    private Record nCurrent;

    /**
     * The offset of the row, if any, currently positioned on.
     */
    private int iCurrentRow;

    /**
     * If a result of updating the database, then this is the number of rows
     * updated.
     */
    private int iUpdateCount;

    /**
     * Is current row before the first row?
     */
    private boolean bInit;    // false if before first row

    /**
     * How many columns does this <code>ResultSet</code> have?
     */
    private int iColumnCount;

    /**
     * Did the last getXXX method encounter a null value? <p>
     *
     * This is important for methods that return primitive values, since
     * there is no other way to check for this condition in those cases.
     */
    private boolean bWasNull;

// fredt@users 20020222 - patch 489917 by jytou@users - made optional
// see setGetColumnName in package private internal implementation
// methods section

    /**
     * Does {@link #getColumnName(int) getColumnName} return the
     * column name (true) or label (false)?
     */
    private boolean getColumnName = true;

    /**
     * if false, various unsupported ResultSetMetaData methods return the
     * true/false values they used to return in version 1.61.
     * if true they throw an SQLException
     */
    private boolean strictMetaData = false;

    /**
     * Properties for the connectin
     *
     */
    private HsqlProperties connProperties;

    //------------------------ Package Attributes --------------------------

    /**
     * The Statement that generated this result.
     */
    Statement sqlStatement;

    /**
     * The direction of this result.
     */
    int rsType = TYPE_FORWARD_ONLY;

    /**
     * <!-- start generic documentation -->
     * Moves the cursor down one row from its current position.
     * A <code>ResultSet</code> cursor is initially positioned
     * before the first row; the first call to the method
     * <code>next</code> makes the first row the current row; the
     * second call makes the second row the current row, and so on.
     *
     * <P>If an input stream is open for the current row, a call
     * to the method <code>next</code> will
     * implicitly close it. A <code>ResultSet</code> object's
     * warning chain is cleared when a new row is read. <p>
     *
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * </span>
     * <!-- end release-specific documentation -->
     *
     *
     * @return <code>true</code> if the new current row is valid;
     * <code>false</code> if there are no more rows
     * @exception SQLException if a database access error occurs
     */
    public boolean next() throws SQLException {

        bWasNull = false;

        // Have an empty resultset so exit with false
        if (rResult == null) {
            return false;
        }

        if (rResult.rRoot == null) {
            return false;
        }

        if (!bInit) {

            // The resultset has not been traversed, so set the cursor
            // to the first row (1)
            nCurrent    = rResult.rRoot;
            bInit       = true;
            iCurrentRow = 1;
        } else {

            // The resultset has been traversed, if afterLast, retrun false
            if (nCurrent == null) {
                return false;
            }

            // On a valid row so go to next
            nCurrent = nCurrent.next;

            iCurrentRow++;
        }

        // finally test to see if we are in an afterLast situation
        if (nCurrent == null) {

            // Yes, set the current row to 0 and exit with false
            iCurrentRow = 0;

            return false;
        } else {

            // Not afterLast, so success
            return true;
        }
    }

    /**
     * <!-- start generic documentation -->
     * Releases this <code>ResultSet</code> object's database and
     * JDBC resources immediately instead of waiting for
     * this to happen when it is automatically closed.
     *
     * <P><B>Note:</B> A <code>ResultSet</code> object
     * is automatically closed by the
     * <code>Statement</code> object that generated it when
     * that <code>Statement</code> object is closed,
     * re-executed, or is used to retrieve the next result from a
     * sequence of multiple results. A <code>ResultSet</code> object
     * is also automatically closed when it is garbage collected. <p>
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
        iUpdateCount = -1;
        rResult      = null;
    }

    /**
     * <!-- start generic documentation -->
     * Reports whether
     * the last column read had a value of SQL <code>NULL</code>.
     * Note that you must first call one of the getter methods
     * on a column to try to read its value and then call
     * the method <code>wasNull</code> to see if the value read was
     * SQL <code>NULL</code>. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @return <code>true</code> if the last column value read was SQL
     *     <code>NULL</code> and <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean wasNull() throws SQLException {
        return bWasNull;
    }

    //======================================================================
    // Methods for accessing results by column index
    //======================================================================

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of the designated column in the current row
     * of this <code>ResultSet</code> object as
     * a <code>String</code> in the Java programming language. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @return the column value; if the value is SQL <code>NULL</code>, the
     * value returned is <code>null</code>
     * @exception SQLException if a database access error occurs
     */
    public String getString(int columnIndex) throws SQLException {

        checkAvailable();

        Object o;

        try {
            o = nCurrent.data[--columnIndex];
        } catch (ArrayIndexOutOfBoundsException e) {
            throw Trace.error(Trace.COLUMN_NOT_FOUND, ++columnIndex);
        }

        // use checknull because getColumnInType is not used
        checkNull(o);

        return o == null ? null
                         : o.toString();
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of the designated column in the current row
     * of this <code>ResultSet</code> object as
     * a <code>boolean</code> in the Java programming language. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @return the column value; if the value is SQL <code>NULL</code>, the
     * value returned is <code>false</code>
     * @exception SQLException if a database access error occurs
     */
    public boolean getBoolean(int columnIndex) throws SQLException {

        Object o = getColumnInType(columnIndex, Types.BIT);

        return o == null ? false
                         : ((Boolean) o).booleanValue();
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of the designated column in the current row
     * of this <code>ResultSet</code> object as
     * a <code>byte</code> in the Java programming language. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @return the column value; if the value is SQL <code>NULL</code>, the
     * value returned is <code>0</code>
     * @exception SQLException if a database access error occurs
     */
    public byte getByte(int columnIndex) throws SQLException {

        Object o = getColumnInType(columnIndex, Types.SMALLINT);

        return o == null ? 0
                         : ((Number) o).byteValue();
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of the designated column in the current row
     * of this <code>ResultSet</code> object as
     * a <code>short</code> in the Java programming language. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @return the column value; if the value is SQL <code>NULL</code>, the
     * value returned is <code>0</code>
     * @exception SQLException if a database access error occurs
     */
    public short getShort(int columnIndex) throws SQLException {

        Object o = getColumnInType(columnIndex, Types.SMALLINT);

        return o == null ? 0
                         : ((Number) o).shortValue();
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of the designated column in the current row
     * of this <code>ResultSet</code> object as
     * an <code>int</code> in the Java programming language. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @return the column value; if the value is SQL <code>NULL</code>, the
     * value returned is <code>0</code>
     * @exception SQLException if a database access error occurs
     */
    public int getInt(int columnIndex) throws SQLException {

        Object o = getColumnInType(columnIndex, Types.INTEGER);

        return o == null ? 0
                         : ((Number) o).intValue();
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of the designated column in the current row
     * of this <code>ResultSet</code> object as
     * a <code>long</code> in the Java programming language. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @return the column value; if the value is SQL <code>NULL</code>, the
     * value returned is <code>0</code>
     * @exception SQLException if a database access error occurs
     */
    public long getLong(int columnIndex) throws SQLException {

        Object o = getColumnInType(columnIndex, Types.BIGINT);

        return o == null ? 0
                         : ((Number) o).longValue();
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of the designated column in the current row
     * of this <code>ResultSet</code> object as
     * a <code>float</code> in the Java programming language. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @return the column value; if the value is SQL <code>NULL</code>, the
     * value returned is <code>0</code>
     * @exception SQLException if a database access error occurs
     */
    public float getFloat(int columnIndex) throws SQLException {

        Object o = getColumnInType(columnIndex, Types.REAL);

        return o == null ? (float) 0.0
                         : ((Number) o).floatValue();
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of the designated column in the current row
     * of this <code>ResultSet</code> object as
     * a <code>double</code> in the Java programming language. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @return the column value; if the value is SQL <code>NULL</code>, the
     * value returned is <code>0</code>
     * @exception SQLException if a database access error occurs
     */
    public double getDouble(int columnIndex) throws SQLException {

        Object o = getColumnInType(columnIndex, Types.DOUBLE);

        return o == null ? 0.0
                         : ((Number) o).doubleValue();
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of the designated column in the current row
     * of this <code>ResultSet</code> object as
     * a <code>java.sql.BigDecimal</code> in the Java programming language.<p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * Beginning with 1.7.0, HSQLDB converts the result and sets the scale
     * with BigDecimal.ROUND_HALF_DOWN<p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param scale the number of digits to the right of the decimal point
     * @return the column value; if the value is SQL <code>NULL</code>, the
     * value returned is <code>null</code>
     * @exception SQLException if a database access error occurs
     * @deprecated
     */
    public BigDecimal getBigDecimal(int columnIndex,
                                    int scale) throws SQLException {

        // boucherb@users 20020502 - added conversion
        BigDecimal bd = (BigDecimal) getColumnInType(columnIndex,
            Types.DECIMAL);

        if (scale < 0) {
            throw Trace.error(Trace.INVALID_JDBC_ARGUMENT);
        }

        if (bd != null) {
            bd.setScale(scale, BigDecimal.ROUND_HALF_DOWN);
        }

        return bd;
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of the designated column in the current row
     * of this <code>ResultSet</code> object as
     * a <code>byte</code> array in the Java programming language.
     * The bytes represent the raw values returned by the driver. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * Up to and including 1.7.1, HSQLDB returns correct values for columns
     * of type <CODE>BINARY</CODE>, <CODE>CHAR</CODE> and their variations.
     * For other types, it returns the <CODE>byte[]</CODE> for the
     * <CODE>String</CODE> representation of the value. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @return the column value; if the value is SQL <code>NULL</code>, the
     * value returned is <code>null</code>
     * @exception SQLException if a database access error occurs
     */
    public byte[] getBytes(int columnIndex) throws SQLException {

        Object x = getObject(columnIndex);

        if (x == null) {
            return null;
        }

        if (x instanceof byte[]) {
            return (byte[]) x;
        }

        if (x instanceof java.lang.String) {
            return ((String) x).getBytes();
        }

        x = getColumnInType(--columnIndex, Types.BINARY);

        return (byte[]) x;
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of the designated column in the current row
     * of this <code>ResultSet</code> object as a
     * <code>java.sql.Date</code> object in the Java programming language.<p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @return the column value; if the value is SQL <code>NULL</code>, the
     * value returned is <code>null</code>
     * @exception SQLException if a database access error occurs
     */
    public java.sql.Date getDate(int columnIndex) throws SQLException {
        return (java.sql.Date) getColumnInType(columnIndex, Types.DATE);
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of the designated column in the current row
     * of this <code>ResultSet</code> object as a <code>java.sql.Time</code>
     * object in the Java programming language. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @return the column value; if the value is SQL <code>NULL</code>, the
     * value returned is <code>null</code>
     * @exception SQLException if a database access error occurs
     */
    public java.sql.Time getTime(int columnIndex) throws SQLException {
        return (Time) getColumnInType(columnIndex, Types.TIME);
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of the designated column in the current row
     * of this <code>ResultSet</code> object as
     * a <code>java.sql.Timestamp</code> object in the Java programming
     * language. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @return the column value; if the value is SQL <code>NULL</code>, the
     * value returned is <code>null</code>
     * @exception SQLException if a database access error occurs
     */
    public java.sql.Timestamp getTimestamp(int columnIndex)
    throws SQLException {
        return (Timestamp) getColumnInType(columnIndex, Types.TIMESTAMP);
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of the designated column in the current row
     * of this <code>ResultSet</code> object as
     * a stream of ASCII characters. The value can then be read in chunks
     * from the stream. This method is particularly
     * suitable for retrieving large <char>LONGVARCHAR</char> values.
     * The JDBC driver will
     * do any necessary conversion from the database format into ASCII.
     *
     * <P><B>Note:</B> All the data in the returned stream must be
     * read prior to getting the value of any other column. The next
     * call to a getter method implicitly closes the stream.  Also, a
     * stream may return <code>0</code> when the method
     * <code>InputStream.available</code>
     * is called whether there is data available or not. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * The limitation noted above does not apply to HSQLDB.<p>
     *
     * Up to and including 1.6.1, getAsciiStream was identical to
     * getUnicodeStream and both simply returned a byte stream
     * constructed from the raw {@link #getBytes(int) getBytes}
     * representation.
     *
     * Starting with 1.7.0, this has been updated to comply with the
     * java.sql specification.
     *
     * When the column is of type CHAR and its variations, it requires no
     * conversion since it is represented internally already as
     * Java Strings. When the column is not of type CHAR and its variations,
     * the returned stream is based on a conversion to the
     * Java <CODE>String</CODE> representation of the value. In either case,
     * the obtained stream is always equivalent to a stream of the low order
     * bytes from the value's String representation. <p>
     *
     * HSQLDB SQL <CODE>CHAR</CODE> and its variations are all Unicode strings
     * internally, so the recommended alternatives to this method are
     * {@link #getString(int) getString},
     * {@link #getUnicodeStream(int) getUnicodeStream} (<b>deprecated</b>)
     * and new to 1.7.0: {@link #getCharacterStream(int) getCharacterStream}
     * (now prefered over the deprecated getUnicodeStream alternative). <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @return a Java input stream that delivers the database column value
     * as a stream of one-byte ASCII characters;
     * if the value is SQL <code>NULL</code>, the
     * value returned is <code>null</code>
     * @exception SQLException if a database access error occurs
     */
    public java.io.InputStream getAsciiStream(int columnIndex)
    throws SQLException {

        String s = getString(columnIndex);

        if (s == null) {
            return null;
        }

        return new AsciiStringInputStream(s);
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of the designated column in the current row
     * of this <code>ResultSet</code> object as
     * as a stream of two-byte Unicode characters. The first byte is
     * the high byte; the second byte is the low byte.
     *
     * The value can then be read in chunks from the
     * stream. This method is particularly
     * suitable for retrieving large <code>LONGVARCHAR</code>values.  The
     * JDBC driver will do any necessary conversion from the database
     * format into Unicode.
     *
     * <P><B>Note:</B> All the data in the returned stream must be
     * read prior to getting the value of any other column. The next
     * call to a getter method implicitly closes the stream.
     * Also, a stream may return <code>0</code> when the method
     * <code>InputStream.available</code>
     * is called, whether there is data available or not. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * The limitation noted above does not apply to HSQLDB.<p>
     *
     * Up to and including 1.6.1, getUnicodeStream (and getAsciiStream)
     * both simply returned a byte stream constructed from the
     * raw {@link #getBytes(int) getBytes} representation.
     *
     * Starting with 1.7.0, this has been corrected to comply with the
     * java.sql specification.
     *
     * When the column is of type CHAR and its variations, it requires no
     * conversion since it is represented internally already as
     * Java Strings. When the column is not of type CHAR and its variations,
     * the returned stream is based on a conversion to the
     * Java <CODE>String</CODE> representation of the value. In either case,
     * the obtained stream is always equivalent to a stream of
     * bytes from the value's String representation, with high-byte first.<p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @return a Java input stream that delivers the database column value
     *   as a stream of two-byte Unicode characters;
     *   if the value is SQL <code>NULL</code>, the value returned is
     *   <code>null</code>
     * @exception SQLException if a database access error occurs
     * @deprecated use <code>getCharacterStream</code> in place of
     *        <code>getUnicodeStream</code>
     */
    public java.io.InputStream getUnicodeStream(int columnIndex)
    throws SQLException {

        String s = getString(columnIndex);

        if (s == null) {
            return null;
        }

        return new StringInputStream(s);
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of the designated column in the current row
     * of this <code>ResultSet</code> object as a binary stream of
     * uninterpreted bytes. The value can then be read in chunks from the
     * stream. This method is particularly
     * suitable for retrieving large <code>LONGVARBINARY</code> values.
     *
     * <P><B>Note:</B> All the data in the returned stream must be
     * read prior to getting the value of any other column. The next
     * call to a getter method implicitly closes the stream.  Also, a
     * stream may return <code>0</code> when the method
     * <code>InputStream.available</code>
     * is called whether there is data available or not. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @return a Java input stream that delivers the database column value
     *     as a stream of uninterpreted bytes;
     *     if the value is SQL <code>NULL</code>, the value returned is
     *     <code>null</code>
     * @exception SQLException if a database access error occurs
     */

// fredt@users 20020215 - patch 485704 by boucherb@users
    public java.io.InputStream getBinaryStream(int columnIndex)
    throws SQLException {

        byte[] b = getBytes(columnIndex);

        return wasNull() ? null
                         : new ByteArrayInputStream(b);

        // or new ByteArrayInputStream(new byte[0]) : ...
    }

    //======================================================================
    // Methods for accessing results by column name
    //======================================================================

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of the designated column in the current row
     * of this <code>ResultSet</code> object as
     * a <code>String</code> in the Java programming language. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param columnName the SQL name of the column
     * @return the column value; if the value is SQL <code>NULL</code>, the
     * value returned is <code>null</code>
     * @exception SQLException if a database access error occurs
     */
    public String getString(String columnName) throws SQLException {
        return getString(findColumn(columnName));
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of the designated column in the current row
     * of this <code>ResultSet</code> object as
     * a <code>boolean</code> in the Java programming language. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param columnName the SQL name of the column
     * @return the column value; if the value is SQL <code>NULL</code>, the
     * value returned is <code>false</code>
     * @exception SQLException if a database access error occurs
     */
    public boolean getBoolean(String columnName) throws SQLException {
        return getBoolean(findColumn(columnName));
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of the designated column in the current row
     * of this <code>ResultSet</code> object as
     * a <code>byte</code> in the Java programming language. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param columnName the SQL name of the column
     * @return the column value; if the value is SQL <code>NULL</code>, the
     * value returned is <code>0</code>
     * @exception SQLException if a database access error occurs
     */
    public byte getByte(String columnName) throws SQLException {
        return getByte(findColumn(columnName));
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of the designated column in the current row
     * of this <code>ResultSet</code> object as
     * a <code>short</code> in the Java programming language. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param columnName the SQL name of the column
     * @return the column value; if the value is SQL <code>NULL</code>, the
     * value returned is <code>0</code>
     * @exception SQLException if a database access error occurs
     */
    public short getShort(String columnName) throws SQLException {
        return getShort(findColumn(columnName));
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of the designated column in the current row
     * of this <code>ResultSet</code> object as
     * an <code>int</code> in the Java programming language. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param columnName the SQL name of the column
     * @return the column value; if the value is SQL <code>NULL</code>, the
     * value returned is <code>0</code>
     * @exception SQLException if a database access error occurs
     */
    public int getInt(String columnName) throws SQLException {
        return getInt(findColumn(columnName));
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of the designated column in the current row
     * of this <code>ResultSet</code> object as
     * a <code>long</code> in the Java programming language. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param columnName the SQL name of the column
     * @return the column value; if the value is SQL <code>NULL</code>, the
     * value returned is <code>0</code>
     * @exception SQLException if a database access error occurs
     */
    public long getLong(String columnName) throws SQLException {
        return getLong(findColumn(columnName));
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of the designated column in the current row
     * of this <code>ResultSet</code> object as
     * a <code>float</code> in the Java programming language. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param columnName the SQL name of the column
     * @return the column value; if the value is SQL <code>NULL</code>, the
     * value returned is <code>0</code>
     * @exception SQLException if a database access error occurs
     */
    public float getFloat(String columnName) throws SQLException {
        return getFloat(findColumn(columnName));
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of the designated column in the current row
     * of this <code>ResultSet</code> object as
     * a <code>double</code> in the Java programming language. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param columnName the SQL name of the column
     * @return the column value; if the value is SQL <code>NULL</code>, the
     * value returned is <code>0</code>
     * @exception SQLException if a database access error occurs
     */
    public double getDouble(String columnName) throws SQLException {
        return getDouble(findColumn(columnName));
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of the designated column in the current row
     * of this <code>ResultSet</code> object as a
     * <code>java.math.BigDecimal</code> in the Java programming language.<p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * Up to and including 1.7.1, HSQLDB ignores the scale parameter. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param columnName the SQL name of the column
     * @param scale the number of digits to the right of the decimal point
     * @return the column value; if the value is SQL <code>NULL</code>, the
     * value returned is <code>null</code>
     * @exception SQLException if a database access error occurs
     * @deprecated
     */
    public BigDecimal getBigDecimal(String columnName,
                                    int scale) throws SQLException {
        return getBigDecimal(findColumn(columnName), scale);
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of the designated column in the current row
     * of this <code>ResultSet</code> object as
     * a <code>byte</code> array in the Java programming language.
     * The bytes represent the raw values returned by the driver. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param columnName the SQL name of the column
     * @return the column value; if the value is SQL <code>NULL</code>, the
     * value returned is <code>null</code>
     * @exception SQLException if a database access error occurs
     */
    public byte[] getBytes(String columnName) throws SQLException {
        return getBytes(findColumn(columnName));
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of the designated column in the current row
     * of this <code>ResultSet</code> object as a
     * <code>java.sql.Date</code> object in the Java programming language.<p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param columnName the SQL name of the column
     * @return the column value; if the value is SQL <code>NULL</code>, the
     * value returned is <code>null</code>
     * @exception SQLException if a database access error occurs
     */
    public java.sql.Date getDate(String columnName) throws SQLException {
        return getDate(findColumn(columnName));
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of the designated column in the current row
     * of this <code>ResultSet</code> object as a <code>java.sql.Time</code>
     * object in the Java programming language. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param columnName the SQL name of the column
     * @return the column value;
     * if the value is SQL <code>NULL</code>,
     * the value returned is <code>null</code>
     * @exception SQLException if a database access error occurs
     */
    public java.sql.Time getTime(String columnName) throws SQLException {
        return getTime(findColumn(columnName));
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of the designated column in the current row
     * of this <code>ResultSet</code> object as
     * a <code>java.sql.Timestamp</code> object. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param columnName the SQL name of the column
     * @return the column value; if the value is SQL <code>NULL</code>, the
     * value returned is <code>null</code>
     * @exception SQLException if a database access error occurs
     */
    public java.sql.Timestamp getTimestamp(String columnName)
    throws SQLException {
        return getTimestamp(findColumn(columnName));
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of the designated column in the current row
     * of this <code>ResultSet</code> object as a stream of
     * ASCII characters. The value can then be read in chunks from the
     * stream. This method is particularly
     * suitable for retrieving large <code>LONGVARCHAR</code> values.
     * The JDBC driver will
     * do any necessary conversion from the database format into ASCII.
     *
     * <P><B>Note:</B> All the data in the returned stream must be
     * read prior to getting the value of any other column. The next
     * call to a getter method implicitly closes the stream. Also, a
     * stream may return <code>0</code> when the method <code>available</code>
     * is called whether there is data available or not. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param columnName the SQL name of the column
     * @return a Java input stream that delivers the database column value
     * as a stream of one-byte ASCII characters.
     * If the value is SQL <code>NULL</code>,
     * the value returned is <code>null</code>.
     * @exception SQLException if a database access error occurs
     * @see #getAsciiStream(int)
     */
    public java.io.InputStream getAsciiStream(String columnName)
    throws SQLException {
        return getAsciiStream(findColumn(columnName));
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of the designated column in the current row
     * of this <code>ResultSet</code> object as a stream of two-byte
     * Unicode characters. The first byte is the high byte; the second
     * byte is the low byte.
     *
     * The value can then be read in chunks from the
     * stream. This method is particularly
     * suitable for retrieving large <code>LONGVARCHAR</code> values.
     * The JDBC technology-enabled driver will
     * do any necessary conversion from the database format into Unicode.
     *
     * <P><B>Note:</B> All the data in the returned stream must be
     * read prior to getting the value of any other column. The next
     * call to a getter method implicitly closes the stream.
     * Also, a stream may return <code>0</code> when the method
     * <code>InputStream.available</code> is called, whether there
     * is data available or not. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param columnName the SQL name of the column
     * @return a Java input stream that delivers the database column value
     *    as a stream of two-byte Unicode characters.
     *    If the value is SQL <code>NULL</code>, the value returned
     *    is <code>null</code>.
     * @exception SQLException if a database access error occurs
     * @deprecated use <code>getCharacterStream</code> instead
     * @see #getUnicodeStream(int)
     */
    public java.io.InputStream getUnicodeStream(String columnName)
    throws SQLException {
        return getUnicodeStream(findColumn(columnName));
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of the designated column in the current row
     * of this <code>ResultSet</code> object as a stream of uninterpreted
     * <code>byte</code>s.
     * The value can then be read in chunks from the
     * stream. This method is particularly
     * suitable for retrieving large <code>LONGVARBINARY</code>
     * values.
     *
     * <P><B>Note:</B> All the data in the returned stream must be
     * read prior to getting the value of any other column. The next
     * call to a getter method implicitly closes the stream. Also, a
     * stream may return <code>0</code> when the method <code>available</code>
     * is called whether there is data available or not. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param columnName the SQL name of the column
     * @return a Java input stream that delivers the database column value
     * as a stream of uninterpreted bytes;
     * if the value is SQL <code>NULL</code>, the result is <code>null</code>
     * @exception SQLException if a database access error occurs
     */
    public java.io.InputStream getBinaryStream(String columnName)
    throws SQLException {
        return getBinaryStream(findColumn(columnName));
    }

    //=====================================================================
    // Advanced features:
    //=====================================================================

    /**
     * <!-- start generic documentation -->
     * Retrieves the first warning reported by calls on this
     * <code>ResultSet</code> object.
     * Subsequent warnings on this <code>ResultSet</code> object
     * will be chained to the <code>SQLWarning</code> object that
     * this method returns.
     *
     * <P>The warning chain is automatically cleared each time a new
     * row is read.  This method may not be called on a <code>ResultSet</code>
     * object that has been closed; doing so will cause an
     * <code>SQLException</code> to be thrown.
     * <P>
     * <B>Note:</B> This warning chain only covers warnings caused
     * by <code>ResultSet</code> methods.  Any warning caused by
     * <code>Statement</code> methods
     * (such as reading OUT parameters) will be chained on the
     * <code>Statement</code> object. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * Up to and including 1.7.1, HSQLDB does not produce
     * <code>SQLWarning</code> objects. This method always returns
     * <code>null</code>. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @return the first <code>SQLWarning</code> object reported or
     *    <code>null</code> if there are none <p>
     *
     * Up to and including 1.7.1, HSQLDB always returns null. <p>
     * @exception SQLException if a database access error occurs or this
     *    method is called on a closed result set
     */
    public SQLWarning getWarnings() throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        return null;
    }

    /**
     * <!-- start generic documentation -->
     * Clears all warnings reported on this <code>ResultSet</code> object.
     * After this method is called, the method <code>getWarnings</code>
     * returns <code>null</code> until a new warning is
     * reported for this <code>ResultSet</code> object. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * Up to and including 1.7.1, HSQLDB does not produce
     * <CODE>SQLWarning</CODE> objects, so this method is
     * simply ignored. <p>
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
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves the name of the SQL cursor used by this
     * <code>ResultSet</code> object.
     *
     * <P>In SQL, a result table is retrieved through a cursor that is
     * named. The current row of a result set can be updated or deleted
     * using a positioned update/delete statement that references the
     * cursor name. To insure that the cursor has the proper isolation
     * level to support update, the cursor's <code>SELECT</code> statement
     * should be of the form <code>SELECT FOR UPDATE</code>. If
     * <code>FOR UPDATE</code> is omitted, the positioned updates may fail.
     *
     * <P>The JDBC API supports this SQL feature by providing the name of the
     * SQL cursor used by a <code>ResultSet</code> object.
     * The current row of a <code>ResultSet</code> object
     * is also the current row of this SQL cursor.
     *
     * <P><B>Note:</B> If positioned update is not supported, a
     * <code>SQLException</code> is thrown. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * HSQLDB 1.7.0 does not support this feature.  <p>
     *
     * Calling this method always throws a <CODE>SQLException</CODE>,
     * stating that the function is not supported. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @return the SQL name for this <code>ResultSet</code> object's cursor
     * @exception SQLException if a database access error occurs
     */
    public String getCursorName() throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        throw getNotSupported();
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves the  number, types and properties of
     * this <code>ResultSet</code> object's columns. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * <code>jdbcResultSet</code> implements both the
     * <code>ResultSet</code> and <code>ResultSetMetaData</code> interfaces.
     * However, to gain access to the interface methods of
     * <code>ResultSetMetaData</code> in a driver independent way, the
     * traditional call to this method should be used, rather than casting
     * objects known to be of type <code>jdbcResultSet</code> to type
     * <code>ResultSetMetaData</code>. <p>
     *
     * <B>Example:</B> <p>
     *
     * The following code fragment creates a <code>ResultSet</code> object rs,
     * creates a <code>ResultSetMetaData</code> object rsmd, and uses rsmd
     * to find out how many columns rs has and whether the first column
     * in rs can be used in a <code>WHERE</code> clause. <p>
     *
     * <code class="JavaCodeExample">
     * ResultSet rs = stmt.<b>executeQuery</b>
     * (<span class="JavaStringLiteral">
     * "SELECT a, b, c FROM TABLE2"</span>);<br>
     * ResultSetMetaData rsmd = rs.<b>getMetaData</b>();<br>
     * int numberOfColumns = rsmd.<b>getColumnCount</b>();<br>
     * boolean b = rsmd.<b>isSearchable</b>(1);<br>
     * </code> <p>
     *
     * <B>Warning:</B> <p>
     *
     * Up to and including 1.7.1, HSQLDB does not generate accurate
     * <CODE>ResultSetMetaData</CODE>.  Below are the points to consider: <p>
     *
     * <ol>
     * <li>{@link #isCurrency} <i>always</i> returns <CODE>false</CODE></li>
     * <li>{@link #isNullable} <i>always</i> returns
     *   <CODE>columnNullableUnknown</CODE></li>
     * <li>{@link #getColumnDisplaySize} returns zero for all valid column
     *    numbers</li>
     * <li>{@link #getSchemaName} <i>always</i> returns
     *  <span class="JavaStringLiteral">""</span></li>
     * <li>{@link #getPrecision} <i>always</i> returns zero</li>
     * <li>{@link #getScale} <i>always</i> returns zero</li>
     * <li>{@link #getCatalogName} <i>always</i> returns
     *  <span class="JavaStringLiteral">""</span></li>
     * </ol> <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @return the description of this <code>ResultSet</code> object's columns
     * @exception SQLException if a database access error occurs
     * @see #getCatalogName
     * @see #getColumnClassName
     * @see #getColumnCount
     * @see #getColumnDisplaySize
     * @see #getColumnLabel
     * @see #getColumnName
     * @see #getColumnType
     * @see #getColumnTypeName
     * @see #getPrecision
     * @see #getScale
     * @see #getSchemaName
     * @see #getTableName
     * @see #isAutoIncrement
     * @see #isCaseSensitive
     * @see #isCurrency
     * @see #isDefinitelyWritable
     * @see #isNullable
     * @see #isReadOnly
     * @see #isSearchable
     * @see #isSigned
     * @see #isWritable
     */
    public ResultSetMetaData getMetaData() throws SQLException {
        return this;
    }

    /**
     * <!-- start generic documentation -->
     * Gets the value of the designated column in the current row
     * of this <code>ResultSet</code> object as
     * an <code>Object</code> in the Java programming language.
     *
     * <p>This method will return the value of the given column as a
     * Java object.  The type of the Java object will be the default
     * Java object type corresponding to the column's SQL type,
     * following the mapping for built-in types specified in the JDBC
     * specification. If the value is an SQL <code>NULL</code>,
     * the driver returns a Java <code>null</code>.
     *
     * <p>This method may also be used to read datatabase-specific
     * abstract data types.
     *
     * In the JDBC 2.0 API, the behavior of method
     * <code>getObject</code> is extended to materialize
     * data of SQL user-defined types.  When a column contains
     * a structured or distinct value, the behavior of this method is as
     * if it were a call to: <code>getObject(columnIndex,
     * this.getStatement().getConnection().getTypeMap())</code>. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @return a <code>java.lang.Object</code> holding the column value
     * @exception SQLException if a database access error occurs
     */
    public Object getObject(int columnIndex) throws SQLException {

        checkAvailable();

        Object o;

        try {
            o = nCurrent.data[--columnIndex];
        } catch (ArrayIndexOutOfBoundsException e) {
            throw Trace.error(Trace.COLUMN_NOT_FOUND, ++columnIndex);
        }

        // use checknull because getColumnInType is not used
        checkNull(o);

// fredt@users 20020328 -  patch 482109 by fredt - OBJECT handling
// all objects are stored in Result as the original java object,
// except byte[] which is wrapped in ByteArray to allow comparison.
// Deserialization of OTHER is now handled in BinaryServerRowInput
// when reconstructing a Result from a bytestream.
        return o;
    }

    /**
     * <!-- start generic documentation -->
     * Gets the value of the designated column in the current row
     * of this <code>ResultSet</code> object as
     * an <code>Object</code> in the Java programming language.
     *
     * <p>This method will return the value of the given column as a
     * Java object.  The type of the Java object will be the default
     * Java object type corresponding to the column's SQL type,
     * following the mapping for built-in types specified in the JDBC
     * specification. If the value is an SQL <code>NULL</code>,
     * the driver returns a Java <code>null</code>.
     * <P>
     * This method may also be used to read datatabase-specific
     * abstract data types.
     * <P>
     * In the JDBC 2.0 API, the behavior of the method
     * <code>getObject</code> is extended to materialize
     * data of SQL user-defined types.  When a column contains
     * a structured or distinct value, the behavior of this method is as
     * if it were a call to: <code>getObject(columnIndex,
     * this.getStatement().getConnection().getTypeMap())</code>. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * </span>
     * <!-- end release-specific documentation -->
     * @param columnName the SQL name of the column
     * @return a <code>java.lang.Object</code> holding the column value
     * @exception SQLException if a database access error occurs
     */
    public Object getObject(String columnName) throws SQLException {
        return getObject(findColumn(columnName));
    }

    //----------------------------------------------------------------

    /**
     * <!-- start generic documentation -->
     * Maps the given <code>ResultSet</code> column name to its
     * <code>ResultSet</code> column index. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param columnName the name of the column
     * @return the column index of the given column name
     * @exception SQLException if the <code>ResultSet</code> object does not
     *    contain <code>columnName</code> or a database access error occurs
     */
    public int findColumn(String columnName) throws SQLException {

        if (Trace.TRACE) {
            Trace.trace(columnName);
        }

        for (int i = 0; i < iColumnCount; i++) {
            if (columnName.equalsIgnoreCase(rResult.sLabel[i])) {
                return i + 1;
            }
        }

        throw Trace.error(Trace.COLUMN_NOT_FOUND);
    }

    //--------------------------JDBC 2.0-----------------------------------
    //---------------------------------------------------------------------
    // Getters and Setters
    //---------------------------------------------------------------------

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of the designated column in the current row
     * of this <code>ResultSet</code> object as a
     * <code>java.io.Reader</code> object. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * This method is new to HSQLDB 1.7.0.  Previous versions did not
     * implement this, thowing a <CODE>SQLException</CODE> instead. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     * @return a <code>java.io.Reader</code> object that contains the column
     *   value; if the value is SQL <code>NULL</code>, the value returned
     *   is <code>null</code> in the Java programming language.
     * @param columnIndex the first column is 1, the second is 2, ...
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2
     */
    public java.io.Reader getCharacterStream(int columnIndex)
    throws SQLException {

        String s = getString(columnIndex);

        if (s == null) {
            return null;
        }

        return new StringReader(s);
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of the designated column in the current row
     * of this <code>ResultSet</code> object as a
     * <code>java.io.Reader</code> object. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * This method is new to HSQLDB 1.7.0.  Previous versions did not
     * implement this, thowing a <CODE>SQLException</CODE> instead. <p>
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param columnName the name of the column
     * @return a <code>java.io.Reader</code> object that contains the column
     * value; if the value is SQL <code>NULL</code>, the value returned is
     * <code>null</code> in the Java programming language
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2
     */
    public java.io.Reader getCharacterStream(String columnName)
    throws SQLException {
        return getCharacterStream(findColumn(columnName));
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of the designated column in the current row
     * of this <code>ResultSet</code> object as a
     * <code>java.math.BigDecimal</code> with full precision. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @return the column value (full precision);
     * if the value is SQL <code>NULL</code>, the value returned is
     * <code>null</code> in the Java programming language.
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     *    jdbcResultSet)
     */
    public BigDecimal getBigDecimal(int columnIndex)
    throws java.sql.SQLException {
        return (BigDecimal) getColumnInType(columnIndex, Types.DECIMAL);
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of the designated column in the current row
     * of this <code>ResultSet</code> object as a
     * <code>java.math.BigDecimal</code> with full precision. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param columnName the column name
     * @return the column value (full precision);
     * if the value is SQL <code>NULL</code>, the value returned is
     * <code>null</code> in the Java programming language.
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     *    jdbcResultSet)
     */
    public BigDecimal getBigDecimal(String columnName) throws SQLException {
        return getBigDecimal(findColumn(columnName));
    }

    //---------------------------------------------------------------------
    // Traversal/Positioning
    //---------------------------------------------------------------------

    /**
     * <!-- start generic documentation -->
     * Retrieves whether the cursor is before the first row in
     * this <code>ResultSet</code> object. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @return <code>true</code> if the cursor is before the first row;
     * <code>false</code> if the cursor is at any other position or the
     * result set contains no rows
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     *    jdbcResultSet)
     */
    public boolean isBeforeFirst() throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        // Start Old Code
        //      if (bInit == false) {
        //          return true;
        //      }
        //      return false;
        // End Old Code
        // Start New Cose
        // bInit indicates whether the resultset has not been traversed or not
        // true - it has ---- false it hasn't
        return !bInit;

        // End New Cose
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves whether the cursor is after the last row in
     * this <code>ResultSet</code> object. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @return <code>true</code> if the cursor is after the last row;
     * <code>false</code> if the cursor is at any other position or the
     * result set contains no rows
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     *    jdbcResultSet)
     */
    public boolean isAfterLast() throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        // At afterLast condition esists when resultset has been traversed and
        // the current row is null.  iCurrentRow should also be set to "0",
        // but no need to test
        if (!bInit) {
            return false;
        }

        return nCurrent == null;

        // why not just: return bInit ? (nCurrent == null) : false; ?
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves whether the cursor is on the first row of
     * this <code>ResultSet</code> object. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @return <code>true</code> if the cursor is on the first row;
     * <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     *    jdbcResultSet)
     */
    public boolean isFirst() throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        return iCurrentRow == 1;
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves whether the cursor is on the last row of
     * this <code>ResultSet</code> object.
     * Note: Calling the method <code>isLast</code> may be expensive
     * because the JDBC driver
     * might need to fetch ahead one row in order to determine
     * whether the current row is the last row in the result set. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * Up to and including HSQLDB 1.7.0, this method is not
     * terribly expensive, since the entire result is fetched
     * internally before this object is returned to a caller. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @return <code>true</code> if the cursor is on the last row;
     * <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     *  jdbcResultSet)
     */
    public boolean isLast() throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        // If the resultset has not been traversed, then exit with false
        if ((!bInit) || (nCurrent == null)) {
            return false;
        }

        // At the last row if the next row is null
        return nCurrent.next == null;
    }

    /**
     * <!-- start generic documentation -->
     * Moves the cursor to the front of
     * this <code>ResultSet</code> object, just before the
     * first row. This method has no effect if the result set contains
     * no rows.<p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @exception SQLException if a database access error
     * occurs or the result set type is <code>TYPE_FORWARD_ONLY</code>
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     *    jdbcResultSet)
     */
    public void beforeFirst() throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        if (this.getType() == TYPE_FORWARD_ONLY) {
            throw Trace.error(Trace.RESULTSET_FORWARD_ONLY);
        }

        // Set to beforeFirst status
        bInit       = false;
        nCurrent    = null;
        iCurrentRow = 0;
    }

    /**
     * <!-- start generic documentation -->
     * Moves the cursor to the end of
     * this <code>ResultSet</code> object, just after the last row. This
     * method has no effect if the result set contains no rows. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * </span>
     * <!-- end release-specific documentation -->
     * @exception SQLException if a database access error
     * occurs or the result set type is <code>TYPE_FORWARD_ONLY</code>
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     *    jdbcResultSet)
     */
    public void afterLast() throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        if (this.getType() == TYPE_FORWARD_ONLY) {
            throw Trace.error(Trace.RESULTSET_FORWARD_ONLY);
        }

        if (rResult != null) {
            if (rResult.rRoot != null) {

                // not and empty resultset, so set the afterLast status
                bInit       = true;
                iCurrentRow = 0;
                nCurrent    = null;
            }
        }
    }

    /**
     * <!-- start generic documentation -->
     * Moves the cursor to the first row in
     * this <code>ResultSet</code> object. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @return <code>true</code> if the cursor is on a valid row;
     * <code>false</code> if there are no rows in the result set
     * @exception SQLException if a database access error
     * occurs or the result set type is <code>TYPE_FORWARD_ONLY</code>
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     *    jdbcResultSet)
     */
    public boolean first() throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        if (this.getType() == TYPE_FORWARD_ONLY) {
            throw Trace.error(Trace.RESULTSET_FORWARD_ONLY);
        }

        if (rResult == null) {
            return false;
        }

        bInit = false;

        if (rResult.rRoot != null) {
            bInit       = true;
            nCurrent    = rResult.rRoot;
            iCurrentRow = 1;
        }

        return bInit;
    }

    /**
     * <!-- start generic documentation -->
     * Moves the cursor to the last row in
     * this <code>ResultSet</code> object. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @return <code>true</code> if the cursor is on a valid row;
     * <code>false</code> if there are no rows in the result set
     * @exception SQLException if a database access error
     * occurs or the result set type is <code>TYPE_FORWARD_ONLY</code>
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     *    jdbcResultSet)
     */
    public boolean last() throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        if (this.getType() == TYPE_FORWARD_ONLY) {
            throw Trace.error(Trace.RESULTSET_FORWARD_ONLY);
        }

        if (rResult == null) {
            return false;
        }

        if (rResult.rRoot == null) {
            return false;
        }

        // it resultset not traversed yet, set to first row
        if ((!bInit) | (nCurrent == null)) {
            first();
        }

        // go to the last row
        while (nCurrent.next != null) {
            iCurrentRow++;

            nCurrent = nCurrent.next;
        }

        return true;
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves the current row number.  The first row is number 1, the
     * second number 2, and so on. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @return the current row number; <code>0</code> if there is no current
     *    row
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     *    jdbcResultSet)
     */
    public int getRow() throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        return iCurrentRow;
    }

    /**
     * <!-- start generic documentation -->
     * Moves the cursor to the given row number in
     * this <code>ResultSet</code> object.
     *
     * <p>If the row number is positive, the cursor moves to
     * the given row number with respect to the
     * beginning of the result set.  The first row is row 1, the second
     * is row 2, and so on.
     *
     * <p>If the given row number is negative, the cursor moves to
     * an absolute row position with respect to
     * the end of the result set.  For example, calling the method
     * <code>absolute(-1)</code> positions the
     * cursor on the last row; calling the method <code>absolute(-2)</code>
     * moves the cursor to the next-to-last row, and so on.
     *
     * <p>An attempt to position the cursor beyond the first/last row in
     * the result set leaves the cursor before the first row or after
     * the last row.
     *
     * <p><B>Note:</B> Calling <code>absolute(1)</code> is the same
     * as calling <code>first()</code>. Calling <code>absolute(-1)</code>
     * is the same as calling <code>last()</code>. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param row the number of the row to which the cursor should move.
     *    A positive number indicates the row number counting from the
     *    beginning of the result set; a negative number indicates the
     *    row number counting from the end of the result set
     * @return <code>true</code> if the cursor is on the result set;
     * <code>false</code> otherwise
     * @exception SQLException if a database access error
     * occurs, or the result set type is <code>TYPE_FORWARD_ONLY</code>
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     *    jdbcResultSet)
     */
    public boolean absolute(int row) throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        if (this.getType() == TYPE_FORWARD_ONLY) {
            throw Trace.error(Trace.RESULTSET_FORWARD_ONLY);
        }

        if (rResult == null) {
            return false;
        }

        if ((rResult.rRoot == null) || (row == 0)) {

            // No rows in the resultset or tried to execute absolute(0)
            // which is not valid
            return false;
        }

        // A couple of special cases
        switch (row) {

            case 1 :
                return first();    // absolute(1) is same as first()

            case -1 :
                return last();     // absolute(-1) is same as last()
        }

        // If the row variable is negative, calculate the target
        // row from the end of the resultset.
        if (row < 0) {

            // we know there are rows in resultset, so get the last
            last();

            // calculate the target row
            row = iCurrentRow + row + 1;

            // Exit if the target row is before the beginning of the resultset
            if (row <= 0) {
                beforeFirst();

                return false;
            }
        }

        if ((row < iCurrentRow) || (iCurrentRow == 0)) {

            // Need to go back and start from the beginning of the resultset
            beforeFirst();
        }

        // go to the tagget row;
        while (row > iCurrentRow) {
            next();

            if (nCurrent == null) {
                break;
            }
        }

        return nCurrent != null;
    }

    /**
     * <!-- start generic documentation -->
     * Moves the cursor a relative number of rows, either positive or
     * negative. Attempting to move beyond the first/last row in the
     * result set positions the cursor before/after the
     * the first/last row. Calling <code>relative(0)</code> is valid, but does
     * not change the cursor position.
     *
     * <p>Note: Calling the method <code>relative(1)</code>
     * is identical to calling the method <code>next()</code> and
     * calling the method <code>relative(-1)</code> is identical
     * to calling the method <code>previous()</code>. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param rows an <code>int</code> specifying the number of rows to
     *    move from the current row; a positive number moves the cursor
     *    forward; a negative number moves the cursor backward
     * @return <code>true</code> if the cursor is on a row;
     *     <code>false</code> otherwise
     * @exception SQLException if a database access error occurs,
     *        there is no current row, or the result set type is
     *        <code>TYPE_FORWARD_ONLY</code>
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     *    jdbcResultSet)
     */
    public boolean relative(int rows) throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        if (this.getType() == TYPE_FORWARD_ONLY) {
            throw Trace.error(Trace.RESULTSET_FORWARD_ONLY);
        }

        if (rResult == null) {
            return false;
        }

        if ((rResult.rRoot == null) || ((bInit) && (iCurrentRow == 0))) {
            return false;
        }

        // We are on a valid row, so if the direction is from the last row,
        // calculate the target row
        if (rows < 0) {
            rows = iCurrentRow + rows;

            // set status to beforeFirst status
            beforeFirst();

            // Exit if the target row is before the beginning of the resultset
            if (rows <= 0) {
                return false;
            }
        }

        while (rows-- > 0) {
            next();

            if (nCurrent == null) {
                break;
            }
        }

        // if nCurrent is null, the postion will be afterLast
        return nCurrent != null;
    }

    /**
     * <!-- start generic documentation -->
     * Moves the cursor to the previous row in this
     * <code>ResultSet</code> object. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @return <code>true</code> if the cursor is on a valid row;
     * <code>false</code> if it is off the result set
     * @exception SQLException if a database access error
     * occurs or the result set type is <code>TYPE_FORWARD_ONLY</code>
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     *    jdbcResultSet)
     */
    public boolean previous() throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        if (this.getType() == TYPE_FORWARD_ONLY) {
            throw Trace.error(Trace.RESULTSET_FORWARD_ONLY);
        }

        if (rResult == null) {

            // No resultset
            return false;
        }

        if ((rResult.rRoot == null) || (iCurrentRow == 0)) {

            // Empty resultset or no valid row
            return false;
        }

        if ((bInit) && (nCurrent == null)) {

            // Special condition: in an afterlast condition so go to last
            // row in the resultset
            return last();
        }

        int targetRow = iCurrentRow - 1;

        if (targetRow == 0) {

            // Have gone to a beforeFirst status. Not sure if the
            // beforeFirst status should be set or not.
            // The spec is not very clear.
            beforeFirst();

            return false;
        }

        // Go to the target row.  We always have to start from the first row
        // since the resultset is a forward direction list only
        first();

        while (targetRow != iCurrentRow) {
            nCurrent = nCurrent.next;

            iCurrentRow++;
        }

        return nCurrent != null;
    }

    //---------------------------------------------------------------------
    // Properties
    //---------------------------------------------------------------------
// fredt@users - 20020902 - patch 1.7.1 - fetch size and direction
// We now interpret fetch size and direction as irrelevent to HSQLDB because
// the result set is built and returned as one whole data structure.
// Exceptions thrown are adjusted to mimimal and the javadoc updated.

    /**
     * <!-- start generic documentation -->
     * Gives a hint as to the direction in which the rows in this
     * <code>ResultSet</code> object will be processed.
     * The initial value is determined by the
     * <code>Statement</code> object
     * that produced this <code>ResultSet</code> object.
     * The fetch direction may be changed at any time. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * HSQLDB 1.7.0 builds and returns result sets as a whole, so this
     * method does nothing, apart from the case mandated by the JDBC standard
     * below where
     * an SQLException is thrown with result sets of TYPE_FORWARD_ONLY and
     * fetch directions other than FETCH_FORWARD.
     * <p>
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param direction an <code>int</code> specifying the suggested
     *  fetch direction; one of <code>ResultSet.FETCH_FORWARD</code>,
     *  <code>ResultSet.FETCH_REVERSE</code>, or
     *  <code>ResultSet.FETCH_UNKNOWN</code>
     * @exception SQLException if a database access error occurs or
     *  the result set type is <code>TYPE_FORWARD_ONLY</code> and the
     *  fetch direction is not <code>FETCH_FORWARD</code>
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     *  jdbcResultSet)
     * @see jdbcStatement#setFetchDirection
     * @see #getFetchDirection
     */
    public void setFetchDirection(int direction) throws SQLException {

        if (Trace.TRACE) {
            Trace.trace(direction);
        }

        if (rsType == TYPE_FORWARD_ONLY && direction != FETCH_FORWARD) {
            throw getNotSupported();
        }
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves the fetch direction for this
     * <code>ResultSet</code> object. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * HSQLDB 1.7.0 returns result sets as a whole, so the value returned
     * by this method has no real meaning. <p>
     *
     * Calling this method always returns <code>FETCH_FORWARD</code>. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @return the current fetch direction for this <code>ResultSet</code>
     *   object
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     *   jdbcResultSet)
     * @see #setFetchDirection
     */
    public int getFetchDirection() throws SQLException {
        return FETCH_FORWARD;
    }

    /**
     * <!-- start generic documentation -->
     * Gives the JDBC driver a hint as to the number of rows that should
     * be fetched from the database when more rows are needed for this
     * <code>ResultSet</code> object.
     * If the fetch size specified is zero, the JDBC driver
     * ignores the value and is free to make its own best guess as to what
     * the fetch size should be.  The default value is set by the
     * <code>Statement</code> object
     * that created the result set.  The fetch size may be changed at any
     * time. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     * This method does nothing in HSQLDB as the result set is
     * built and returned completely as a whole.
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param rows the number of rows to fetch
     * @exception SQLException if a database access error occurs or the
     * condition <code>0 <= rows <= this.getMaxRows()</code> is not satisfied
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     *   jdbcResultSet)
     * @see #getFetchSize
     * @see jdbcStatement#setFetchSize
     * @see jdbcStatement#getFetchSize
     */
    public void setFetchSize(int rows) throws SQLException {

        if (Trace.TRACE) {
            Trace.trace(rows);
        }
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves the fetch size for this
     * <code>ResultSet</code> object. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     * As HSQLDB builds and returns the whole result set as a whole, the
     * value returned (1) has no significance.
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @return the current fetch size for this <code>ResultSet</code> object
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     *   jdbcResultSet)
     * @see #setFetchSize
     * @see jdbcStatement#getFetchSize
     * @see jdbcStatement#setFetchSize
     */
    public int getFetchSize() throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        return 1;
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves the type of this <code>ResultSet</code> object.
     * The type is determined by the <code>Statement</code> object
     * that created the result set. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * Up to and including 1.7.1, HSQLDB does not support and thus
     * never returns <code>ResultSet.TYPE_SCROLL_SENSITIVE</code><p>
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @return <code>ResultSet.TYPE_FORWARD_ONLY</code>,
     *     <code>ResultSet.TYPE_SCROLL_INSENSITIVE</code>,
     *     or <code>ResultSet.TYPE_SCROLL_SENSITIVE</code> (not supported)
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     *    jdbcResultSet)
     */
    public int getType() throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        return rsType;
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves the concurrency mode of this <code>ResultSet</code> object.
     * The concurrency used is determined by the
     * <code>Statement</code> object that created the result set. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * Up to and including 1.7.1, HSQLDB supports only and thus always
     * returns <code>CONCUR_READ_ONLY</code>.<p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @return the concurrency type, either
     *    <code>ResultSet.CONCUR_READ_ONLY</code>
     *    or <code>ResultSet.CONCUR_UPDATABLE</code>
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     *   jdbcResultSet)
     */
    public int getConcurrency() throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        return CONCUR_READ_ONLY;
    }

    //---------------------------------------------------------------------
    // Updates
    //---------------------------------------------------------------------

    /**
     * <!-- start generic documentation -->
     * Retrieves whether the current row has been updated.  The value returned
     * depends on whether or not the result set can detect updates. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * Up to and including 1.7.1, HSQLDB does not support updateable
     * results. <p>
     *
     * This method always returns false. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @return <code>true</code> if both (1) the row has been visibly updated
     *    by the owner or another and (2) updates are detected
     * @exception SQLException if a database access error occurs
     * @see DatabaseMetaData#updatesAreDetected
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     *   jdbcResultSet)
     */
    public boolean rowUpdated() throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        return false;
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves whether the current row has had an insertion.
     * The value returned depends on whether or not this
     * <code>ResultSet</code> object can detect visible inserts. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * Up to and including 1.7.1, HSQLDB does not support updateable
     * results. <p>
     *
     * This method always returns false. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @return <code>true</code> if a row has had an insertion
     * and insertions are detected; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     * @see DatabaseMetaData#insertsAreDetected
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     *   jdbcResultSet)
     */
    public boolean rowInserted() throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        return false;
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves whether a row has been deleted.  A deleted row may leave
     * a visible "hole" in a result set.  This method can be used to
     * detect holes in a result set.  The value returned depends on whether
     * or not this <code>ResultSet</code> object can detect deletions. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * Up to and including 1.7.1, HSQLDB does not support updateable
     * results. <p>
     *
     * This method always returns false. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     * @return <code>true</code> if a row was deleted and deletions are
     *      detected; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     * @see DatabaseMetaData#deletesAreDetected
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     *   jdbcResultSet)
     */
    public boolean rowDeleted() throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        return false;
    }

    /**
     * <!-- start generic documentation -->
     * Gives a nullable column a null value.
     *
     * The updater methods are used to update column values in the
     * current row or the insert row.  The updater methods do not
     * update the underlying database; instead the <code>updateRow</code>
     * or <code>insertRow</code> methods are called to update the database.<p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * Up to and including 1.7.1, HSQLDB does not support updateable
     * results. <p>
     *
     * Calling this method always throws a SQLException, stating that
     * the function is not supported. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2
     */
    public void updateNull(int columnIndex) throws SQLException {

        if (Trace.TRACE) {
            Trace.trace(columnIndex);
        }

        throw getNotSupported();
    }

    /**
     * <!-- start generic documentation -->
     * Updates the designated column with a <code>boolean</code> value.
     * The updater methods are used to update column values in the
     * current row or the insert row.  The updater methods do not
     * update the underlying database; instead the <code>updateRow</code> or
     * <code>insertRow</code> methods are called to update the database. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * Up to and including 1.7.1, HSQLDB does not support updateable
     * results. <p>
     *
     * Calling this method always throws a SQLException, stating that
     * the function is not supported. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param x the new column value
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2
     */
    public void updateBoolean(int columnIndex,
                              boolean x) throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        throw getNotSupported();
    }

    /**
     * <!-- start generic documentation -->
     * Updates the designated column with a <code>byte</code> value.
     * The updater methods are used to update column values in the
     * current row or the insert row.  The updater methods do not
     * update the underlying database; instead the <code>updateRow</code> or
     * <code>insertRow</code> methods are called to update the database. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * Up to and including 1.7.1, HSQLDB does not support updateable
     * results. <p>
     *
     * Calling this method always throws a SQLException, stating that
     * the function is not supported. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param x the new column value
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     * jdbcResultSet)
     */
    public void updateByte(int columnIndex, byte x) throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        throw getNotSupported();
    }

    /**
     * <!-- start generic documentation -->
     * Updates the designated column with a <code>short</code> value.
     * The updater methods are used to update column values in the
     * current row or the insert row.  The updater methods do not
     * update the underlying database; instead the <code>updateRow</code> or
     * <code>insertRow</code> methods are called to update the database. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * Up to and including 1.7.1, HSQLDB does not support updateable
     * results. <p>
     *
     * Calling this method always throws a SQLException, stating that
     * the function is not supported. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param x the new column value
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     * jdbcResultSet)
     */
    public void updateShort(int columnIndex, short x) throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        throw getNotSupported();
    }

    /**
     * <!-- start generic documentation -->
     * Updates the designated column with an <code>int</code> value.
     * The updater methods are used to update column values in the
     * current row or the insert row.  The updater methods do not
     * update the underlying database; instead the <code>updateRow</code> or
     * <code>insertRow</code> methods are called to update the database. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * Up to and including 1.7.1, HSQLDB does not support updateable
     * results. <p>
     *
     * Calling this method always throws a SQLException, stating that
     * the function is not supported. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param x the new column value
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     * jdbcResultSet)
     */
    public void updateInt(int columnIndex, int x) throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        throw getNotSupported();
    }

    /**
     * <!-- start generic documentation -->
     * Updates the designated column with a <code>long</code> value.
     * The updater methods are used to update column values in the
     * current row or the insert row.  The updater methods do not
     * update the underlying database; instead the <code>updateRow</code> or
     * <code>insertRow</code> methods are called to update the database. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * Up to and including 1.7.1, HSQLDB does not support updateable
     * results. <p>
     *
     * Calling this method always throws a SQLException, stating that
     * the function is not supported. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param x the new column value
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     * jdbcResultSet)
     */
    public void updateLong(int columnIndex, long x) throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        throw getNotSupported();
    }

    /**
     * <!-- start generic documentation -->
     * Updates the designated column with a <code>float</code> value.
     * The updater methods are used to update column values in the
     * current row or the insert row.  The updater methods do not
     * update the underlying database; instead the <code>updateRow</code> or
     * <code>insertRow</code> methods are called to update the database. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * Up to and including 1.7.1, HSQLDB does not support updateable
     * results. <p>
     *
     * Calling this method always throws a SQLException, stating that
     * the function is not supported. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param x the new column value
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     * jdbcResultSet)
     */
    public void updateFloat(int columnIndex, float x) throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        throw getNotSupported();
    }

    /**
     * <!-- start generic documentation -->
     * Updates the designated column with a <code>double</code> value.
     * The updater methods are used to update column values in the
     * current row or the insert row.  The updater methods do not
     * update the underlying database; instead the <code>updateRow</code> or
     * <code>insertRow</code> methods are called to update the database. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * Up to and including 1.7.1, HSQLDB does not support updateable
     * results. <p>
     *
     * Calling this method always throws a SQLException, stating that
     * the function is not supported. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param x the new column value
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     * jdbcResultSet)
     */
    public void updateDouble(int columnIndex, double x) throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        throw getNotSupported();
    }

    /**
     * <!-- start generic documentation -->
     * Updates the designated column with a <code>java.math.BigDecimal</code>
     * value.
     * The updater methods are used to update column values in the
     * current row or the insert row.  The updater methods do not
     * update the underlying database; instead the <code>updateRow</code> or
     * <code>insertRow</code> methods are called to update the database. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * Up to and including 1.7.1, HSQLDB does not support updateable
     * results. <p>
     *
     * Calling this method always throws a SQLException, stating that
     * the function is not supported. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param x the new column value
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     * jdbcResultSet)
     */
    public void updateBigDecimal(int columnIndex,
                                 BigDecimal x) throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        throw getNotSupported();
    }

    /**
     * <!-- start generic documentation -->
     * Updates the designated column with a <code>String</code> value.
     * The updater methods are used to update column values in the
     * current row or the insert row.  The updater methods do not
     * update the underlying database; instead the <code>updateRow</code> or
     * <code>insertRow</code> methods are called to update the database. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * Up to and including 1.7.1, HSQLDB does not support updateable
     * results. <p>
     *
     * Calling this method always throws a SQLException, stating that
     * the function is not supported. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param x the new column value
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     * jdbcResultSet)
     */
    public void updateString(int columnIndex, String x) throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        throw getNotSupported();
    }

    /**
     * <!-- start generic documentation -->
     * Updates the designated column with a <code>byte</code> array value.
     * The updater methods are used to update column values in the
     * current row or the insert row.  The updater methods do not
     * update the underlying database; instead the <code>updateRow</code> or
     * <code>insertRow</code> methods are called to update the database. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * Up to and including 1.7.1, HSQLDB does not support updateable
     * results. <p>
     *
     * Calling this method always throws a SQLException, stating that
     * the function is not supported. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param x the new column value
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     * jdbcResultSet)
     */
    public void updateBytes(int columnIndex, byte x[]) throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        throw getNotSupported();
    }

    /**
     * <!-- start generic documentation -->
     * Updates the designated column with a <code>java.sql.Date</code> value.
     * The updater methods are used to update column values in the
     * current row or the insert row.  The updater methods do not
     * update the underlying database; instead the <code>updateRow</code> or
     * <code>insertRow</code> methods are called to update the database. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * Up to and including 1.7.1, HSQLDB does not support updateable
     * results. <p>
     *
     * Calling this method always throws a SQLException, stating that
     * the function is not supported. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param x the new column value
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     * jdbcResultSet)
     */
    public void updateDate(int columnIndex,
                           java.sql.Date x) throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        throw getNotSupported();
    }

    /**
     * <!-- start generic documentation -->
     * Updates the designated column with a <code>java.sql.Time</code> value.
     * The updater methods are used to update column values in the
     * current row or the insert row.  The updater methods do not
     * update the underlying database; instead the <code>updateRow</code> or
     * <code>insertRow</code> methods are called to update the database. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * Up to and including 1.7.1, HSQLDB does not support updateable
     * results. <p>
     *
     * Calling this method always throws a SQLException, stating that
     * the function is not supported. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param x the new column value
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     * jdbcResultSet)
     */
    public void updateTime(int columnIndex,
                           java.sql.Time x) throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        throw getNotSupported();
    }

    /**
     * <!-- start generic documentation -->
     * Updates the designated column with a <code>java.sql.Timestamp</code>
     * value.
     * The updater methods are used to update column values in the
     * current row or the insert row.  The updater methods do not
     * update the underlying database; instead the <code>updateRow</code> or
     * <code>insertRow</code> methods are called to update the database. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * Up to and including 1.7.1, HSQLDB does not support updateable
     * results. <p>
     *
     * Calling this method always throws a SQLException, stating that
     * the function is not supported. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param x the new column value
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     * jdbcResultSet)
     */
    public void updateTimestamp(int columnIndex,
                                java.sql.Timestamp x) throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        throw getNotSupported();
    }

    /**
     * <!-- start generic documentation -->
     * Updates the designated column with an ascii stream value.
     * The updater methods are used to update column values in the
     * current row or the insert row.  The updater methods do not
     * update the underlying database; instead the <code>updateRow</code> or
     * <code>insertRow</code> methods are called to update the database. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * Up to and including 1.7.1, HSQLDB does not support updateable
     * results. <p>
     *
     * Calling this method always throws a SQLException, stating that
     * the function is not supported. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param x the new column value
     * @param length the length of the stream
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     * jdbcResultSet)
     */
    public void updateAsciiStream(int columnIndex, java.io.InputStream x,
                                  int length) throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        throw getNotSupported();
    }

    /**
     * <!-- start generic documentation -->
     * Updates the designated column with a binary stream value.
     * The updater methods are used to update column values in the
     * current row or the insert row.  The updater methods do not
     * update the underlying database; instead the <code>updateRow</code> or
     * <code>insertRow</code> methods are called to update the database. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * Up to and including 1.7.1, HSQLDB does not support updateable
     * results. <p>
     *
     * Calling this method always throws a SQLException, stating that
     * the function is not supported. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param x the new column value
     * @param length the length of the stream
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     * jdbcResultSet)
     */
    public void updateBinaryStream(int columnIndex, java.io.InputStream x,
                                   int length) throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        throw getNotSupported();
    }

    /**
     * <!-- start generic documentation -->
     * Updates the designated column with a character stream value.
     * The updater methods are used to update column values in the
     * current row or the insert row.  The updater methods do not
     * update the underlying database; instead the <code>updateRow</code> or
     * <code>insertRow</code> methods are called to update the database. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * Up to and including 1.7.1, HSQLDB does not support updateable
     * results. <p>
     *
     * Calling this method always throws a SQLException, stating that
     * the function is not supported. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param x the new column value
     * @param length the length of the stream
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     *  jdbcResultSet)
     */
    public void updateCharacterStream(int columnIndex, java.io.Reader x,
                                      int length) throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        throw getNotSupported();
    }

    /**
     * <!-- start generic documentation -->
     * Updates the designated column with an <code>Object</code> value.
     * The updater methods are used to update column values in the
     * current row or the insert row.  The updater methods do not
     * update the underlying database; instead the <code>updateRow</code> or
     * <code>insertRow</code> methods are called to update the database. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * Up to and including 1.7.1, HSQLDB does not support updateable
     * results. <p>
     *
     * Calling this method always throws a SQLException, stating that
     * the function is not supported. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param x the new column value
     * @param scale for <code>java.sql.Types.DECIMA</code>
     * or <code>java.sql.Types.NUMERIC</code> types,
     * this is the number of digits after the decimal point.  For all other
     * types this value will be ignored.
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     *  jdbcResultSet)
     */
    public void updateObject(int columnIndex, Object x,
                             int scale) throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        throw getNotSupported();
    }

    /**
     * <!-- start generic documentation -->
     * Updates the designated column with an <code>Object</code> value.
     * The updater methods are used to update column values in the
     * current row or the insert row.  The updater methods do not
     * update the underlying database; instead the <code>updateRow</code> or
     * <code>insertRow</code> methods are called to update the database. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * Up to and including 1.7.1, HSQLDB does not support updateable
     * results. <p>
     *
     * Calling this method always throws a SQLException, stating that
     * the function is not supported. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param x the new column value
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     *  jdbcResultSet)
     */
    public void updateObject(int columnIndex, Object x) throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        throw getNotSupported();
    }

    /**
     * <!-- start generic documentation -->
     * Updates the designated column with a <code>null</code> value.
     * The updater methods are used to update column values in the
     * current row or the insert row.  The updater methods do not
     * update the underlying database; instead the <code>updateRow</code> or
     * <code>insertRow</code> methods are called to update the database. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * Up to and including 1.7.1, HSQLDB does not support updateable
     * results. <p>
     *
     * Calling this method always throws a SQLException, stating that
     * the function is not supported. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param columnName the name of the column
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     *  jdbcResultSet)
     */
    public void updateNull(String columnName) throws SQLException {
        updateNull(findColumn(columnName));
    }

    /**
     * <!-- start generic documentation -->
     * Updates the designated column with a <code>boolean</code> value.
     * The updater methods are used to update column values in the
     * current row or the insert row.  The updater methods do not
     * update the underlying database; instead the <code>updateRow</code> or
     * <code>insertRow</code> methods are called to update the database. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * Up to and including 1.7.1, HSQLDB does not support updateable
     * results. <p>
     *
     * Calling this method always throws a SQLException, stating that
     * the function is not supported. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param columnName the name of the column
     * @param x the new column value
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     *  jdbcResultSet)
     */
    public void updateBoolean(String columnName,
                              boolean x) throws SQLException {
        updateBoolean(findColumn(columnName), x);
    }

    /**
     * <!-- start generic documentation -->
     * Updates the designated column with a <code>byte</code> value.
     * The updater methods are used to update column values in the
     * current row or the insert row.  The updater methods do not
     * update the underlying database; instead the <code>updateRow</code> or
     * <code>insertRow</code> methods are called to update the database. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * Up to and including 1.7.1, HSQLDB does not support updateable
     * results. <p>
     *
     * Calling this method always throws a SQLException, stating that
     * the function is not supported. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param columnName the name of the column
     * @param x the new column value
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     *  jdbcResultSet)
     */
    public void updateByte(String columnName, byte x) throws SQLException {
        updateByte(findColumn(columnName), x);
    }

    /**
     * <!-- start generic documentation -->
     * Updates the designated column with a <code>short</code> value.
     * The updater methods are used to update column values in the
     * current row or the insert row.  The updater methods do not
     * update the underlying database; instead the <code>updateRow</code> or
     * <code>insertRow</code> methods are called to update the database. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * Up to and including 1.7.1, HSQLDB does not support updateable
     * results. <p>
     *
     * Calling this method always throws a SQLException, stating that
     * the function is not supported. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param columnName the name of the column
     * @param x the new column value
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     *  jdbcResultSet)
     */
    public void updateShort(String columnName, short x) throws SQLException {
        updateShort(findColumn(columnName), x);
    }

    /**
     * <!-- start generic documentation -->
     * Updates the designated column with an <code>int</code> value.
     * The updater methods are used to update column values in the
     * current row or the insert row.  The updater methods do not
     * update the underlying database; instead the <code>updateRow</code> or
     * <code>insertRow</code> methods are called to update the database. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * Up to and including 1.7.1, HSQLDB does not support updateable
     * results. <p>
     *
     * Calling this method always throws a SQLException, stating that
     * the function is not supported. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param columnName the name of the column
     * @param x the new column value
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     *  jdbcResultSet)
     */
    public void updateInt(String columnName, int x) throws SQLException {
        updateInt(findColumn(columnName), x);
    }

    /**
     * <!-- start generic documentation -->
     * Updates the designated column with a <code>long</code> value.
     * The updater methods are used to update column values in the
     * current row or the insert row.  The updater methods do not
     * update the underlying database; instead the <code>updateRow</code> or
     * <code>insertRow</code> methods are called to update the database. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * Up to and including 1.7.1, HSQLDB does not support updateable
     * results. <p>
     *
     * Calling this method always throws a SQLException, stating that
     * the function is not supported. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param columnName the name of the column
     * @param x the new column value
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     *   jdbcResultSet)
     */
    public void updateLong(String columnName, long x) throws SQLException {
        updateLong(findColumn(columnName), x);
    }

    /**
     * <!-- start generic documentation -->
     * Updates the designated column with a <code>float</code> value.
     * The updater methods are used to update column values in the
     * current row or the insert row.  The updater methods do not
     * update the underlying database; instead the <code>updateRow</code> or
     * <code>insertRow</code> methods are called to update the database. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * Up to and including 1.7.1, HSQLDB does not support updateable
     * results. <p>
     *
     * Calling this method always throws a SQLException, stating that
     * the function is not supported. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param columnName the name of the column
     * @param x the new column value
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     *   jdbcResultSet)
     */
    public void updateFloat(String columnName, float x) throws SQLException {
        updateFloat(findColumn(columnName), x);
    }

    /**
     * <!-- start generic documentation -->
     * Updates the designated column with a <code>double</code> value.
     * The updater methods are used to update column values in the
     * current row or the insert row.  The updater methods do not
     * update the underlying database; instead the <code>updateRow</code> or
     * <code>insertRow</code> methods are called to update the database. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * Up to and including 1.7.1, HSQLDB does not support updateable
     * results. <p>
     *
     * Calling this method always throws a SQLException, stating that
     * the function is not supported. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param columnName the name of the column
     * @param x the new column value
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     *   jdbcResultSet)
     */
    public void updateDouble(String columnName,
                             double x) throws SQLException {
        updateDouble(findColumn(columnName), x);
    }

    /**
     * <!-- start generic documentation -->
     * Updates the designated column with a <code>java.sql.BigDecimal</code>
     * value.
     * The updater methods are used to update column values in the
     * current row or the insert row.  The updater methods do not
     * update the underlying database; instead the <code>updateRow</code> or
     * <code>insertRow</code> methods are called to update the database. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * Up to and including 1.7.1, HSQLDB does not support updateable
     * results. <p>
     *
     * Calling this method always throws a SQLException, stating that
     * the function is not supported. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param columnName the name of the column
     * @param x the new column value
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     *   jdbcResultSet)
     */
    public void updateBigDecimal(String columnName,
                                 BigDecimal x) throws SQLException {
        updateBigDecimal(findColumn(columnName), x);
    }

    /**
     * <!-- start generic documentation -->
     * Updates the designated column with a <code>String</code> value.
     * The updater methods are used to update column values in the
     * current row or the insert row.  The updater methods do not
     * update the underlying database; instead the <code>updateRow</code> or
     * <code>insertRow</code> methods are called to update the database. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * Up to and including 1.7.1, HSQLDB does not support updateable
     * results. <p>
     *
     * Calling this method always throws a SQLException, stating that
     * the function is not supported. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param columnName the name of the column
     * @param x the new column value
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     *   jdbcResultSet)
     */
    public void updateString(String columnName,
                             String x) throws SQLException {
        updateString(findColumn(columnName), x);
    }

    /**
     * <!-- start generic documentation -->
     * Updates the designated column with a byte array value.
     *
     * The updater methods are used to update column values in the
     * current row or the insert row.  The updater methods do not
     * update the underlying database; instead the <code>updateRow</code> or
     * <code>insertRow</code> methods are called to update the database. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * Up to and including 1.7.1, HSQLDB does not support updateable
     * results. <p>
     *
     * Calling this method always throws a SQLException, stating that
     * the function is not supported. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param columnName the name of the column
     * @param x the new column value
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     *   jdbcResultSet)
     */
    public void updateBytes(String columnName, byte x[]) throws SQLException {
        updateBytes(findColumn(columnName), x);
    }

    /**
     * <!-- start generic documentation -->
     * Updates the designated column with a <code>java.sql.Date</code> value.
     * The updater methods are used to update column values in the
     * current row or the insert row.  The updater methods do not
     * update the underlying database; instead the <code>updateRow</code> or
     * <code>insertRow</code> methods are called to update the database. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * Up to and including 1.7.1, HSQLDB does not support updateable
     * results. <p>
     *
     * Calling this method always throws a SQLException, stating that
     * the function is not supported. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param columnName the name of the column
     * @param x the new column value
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     *   jdbcResultSet)
     */
    public void updateDate(String columnName,
                           java.sql.Date x) throws SQLException {
        updateDate(findColumn(columnName), x);
    }

    /**
     * <!-- start generic documentation -->
     * Updates the designated column with a <code>java.sql.Time</code> value.
     * The updater methods are used to update column values in the
     * current row or the insert row.  The updater methods do not
     * update the underlying database; instead the <code>updateRow</code> or
     * <code>insertRow</code> methods are called to update the database. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * Up to and including 1.7.1, HSQLDB does not support updateable
     * results. <p>
     *
     * Calling this method always throws a SQLException, stating that
     * the function is not supported. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param columnName the name of the column
     * @param x the new column value
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     *   jdbcResultSet)
     */
    public void updateTime(String columnName,
                           java.sql.Time x) throws SQLException {
        updateTime(findColumn(columnName), x);
    }

    /**
     * <!-- start generic documentation -->
     * Updates the designated column with a <code>java.sql.Timestamp</code>
     * value.
     * The updater methods are used to update column values in the
     * current row or the insert row.  The updater methods do not
     * update the underlying database; instead the <code>updateRow</code> or
     * <code>insertRow</code> methods are called to update the database. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * Up to and including 1.7.1, HSQLDB does not support updateable
     * results. <p>
     *
     * Calling this method always throws a SQLException, stating that
     * the function is not supported. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param columnName the name of the column
     * @param x the new column value
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     *   jdbcResultSet)
     */
    public void updateTimestamp(String columnName,
                                java.sql.Timestamp x) throws SQLException {
        updateTimestamp(findColumn(columnName), x);
    }

    /**
     * <!-- start generic documentation -->
     * Updates the designated column with an ascii stream value.
     * The updater methods are used to update column values in the
     * current row or the insert row.  The updater methods do not
     * update the underlying database; instead the <code>updateRow</code> or
     * <code>insertRow</code> methods are called to update the database. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * Up to and including 1.7.1, HSQLDB does not support updateable
     * results. <p>
     *
     * Calling this method always throws a SQLException, stating that
     * the function is not supported. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param columnName the name of the column
     * @param x the new column value
     * @param length the length of the stream
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     *   jdbcResultSet)
     */
    public void updateAsciiStream(String columnName, java.io.InputStream x,
                                  int length) throws SQLException {
        updateAsciiStream(findColumn(columnName), x, length);
    }

    /**
     * <!-- start generic documentation -->
     * Updates the designated column with a binary stream value.
     * The updater methods are used to update column values in the
     * current row or the insert row.  The updater methods do not
     * update the underlying database; instead the <code>updateRow</code> or
     * <code>insertRow</code> methods are called to update the database. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * Up to and including 1.7.1, HSQLDB does not support updateable
     * results. <p>
     *
     * Calling this method always throws a SQLException, stating that
     * the function is not supported. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param columnName the name of the column
     * @param x the new column value
     * @param length the length of the stream
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     *   jdbcResultSet)
     */
    public void updateBinaryStream(String columnName, java.io.InputStream x,
                                   int length) throws SQLException {
        updateBinaryStream(findColumn(columnName), x, length);
    }

    /**
     * <!-- start generic documentation -->
     * Updates the designated column with a character stream value.
     * The updater methods are used to update column values in the
     * current row or the insert row.  The updater methods do not
     * update the underlying database; instead the <code>updateRow</code> or
     * <code>insertRow</code> methods are called to update the database. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * Up to and including 1.7.1, HSQLDB does not support updateable
     * results. <p>
     *
     * Calling this method always throws a SQLException, stating that
     * the function is not supported. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param columnName the name of the column
     * @param reader the <code>java.io.Reader</code> object containing
     *   the new column value
     * @param length the length of the stream
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     *   jdbcResultSet)
     */
    public void updateCharacterStream(String columnName,
                                      java.io.Reader reader,
                                      int length) throws SQLException {
        updateCharacterStream(findColumn(columnName), reader, length);
    }

    /**
     * <!-- start generic documentation -->
     * Updates the designated column with an <code>Object</code> value.
     * The updater methods are used to update column values in the
     * current row or the insert row.  The updater methods do not
     * update the underlying database; instead the <code>updateRow</code> or
     * <code>insertRow</code> methods are called to update the database. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * Up to and including 1.7.1, HSQLDB does not support updateable
     * results. <p>
     *
     * Calling this method always throws a SQLException, stating that
     * the function is not supported. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param columnName the name of the column
     * @param x the new column value
     * @param scale for <code>java.sql.Types.DECIMAL</code>
     * or <code>java.sql.Types.NUMERIC</code> types,
     * this is the number of digits after the decimal point.  For all other
     * types this value will be ignored.
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     *   jdbcResultSet)
     */
    public void updateObject(String columnName, Object x,
                             int scale) throws SQLException {
        updateObject(findColumn(columnName), x, scale);
    }

    /**
     * <!-- start generic documentation -->
     * Updates the designated column with an <code>Object</code> value.
     * The updater methods are used to update column values in the
     * current row or the insert row.  The updater methods do not
     * update the underlying database; instead the <code>updateRow</code> or
     * <code>insertRow</code> methods are called to update the database. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * Up to and including 1.7.1, HSQLDB does not support updateable
     * results. <p>
     *
     * Calling this method always throws a SQLException, stating that
     * the function is not supported. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param columnName the name of the column
     * @param x the new column value
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     *   jdbcResultSet)
     */
    public void updateObject(String columnName,
                             Object x) throws SQLException {
        updateObject(findColumn(columnName), x);
    }

    /**
     * <!-- start generic documentation -->
     * Inserts the contents of the insert row into this
     * <code>ResultSet</code> object and into the database.
     * The cursor must be on the insert row when this method is called. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * Up to and including 1.7.1, HSQLDB does not support updateable
     * results. <p>
     *
     * Calling this method always throws a SQLException, stating that
     * the function is not supported. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @exception SQLException if a database access error occurs,
     * if this method is called when the cursor is not on the insert row,
     * or if not all of non-nullable columns in
     * the insert row have been given a value
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     *   jdbcResultSet)
     */
    public void insertRow() throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        throw getNotSupported();
    }

    /**
     * <!-- start generic documentation -->
     * Updates the underlying database with the new contents of the
     * current row of this <code>ResultSet</code> object.
     * This method cannot be called when the cursor is on the insert row. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * Up to and including 1.7.1, HSQLDB does not support updateable
     * results. <p>
     *
     * Calling this method always throws a SQLException, stating that
     * the function is not supported. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @exception SQLException if a database access error occurs or
     * if this method is called when the cursor is on the insert row
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     *   jdbcResultSet)
     */
    public void updateRow() throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        throw getNotSupported();
    }

    /**
     * <!-- start generic documentation -->
     * Deletes the current row from this <code>ResultSet</code> object
     * and from the underlying database.  This method cannot be called when
     * the cursor is on the insert row. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * Up to and including 1.7.1, HSQLDB does not support updateable
     * results. <p>
     *
     * Calling this method always throws a SQLException, stating that
     * the function is not supported. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @exception SQLException if a database access error occurs
     * or if this method is called when the cursor is on the insert row
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     *   jdbcResultSet)
     */
    public void deleteRow() throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        throw getNotSupported();
    }

    /**
     * <!-- start generic documentation -->
     * Refreshes the current row with its most recent value in
     * the database.  This method cannot be called when
     * the cursor is on the insert row.
     *
     * <P>The <code>refreshRow</code> method provides a way for an
     * application to
     * explicitly tell the JDBC driver to refetch a row(s) from the
     * database.  An application may want to call <code>refreshRow</code> when
     * caching or prefetching is being done by the JDBC driver to
     * fetch the latest value of a row from the database.  The JDBC driver
     * may actually refresh multiple rows at once if the fetch size is
     * greater than one.
     *
     * <P> All values are refetched subject to the transaction isolation
     * level and cursor sensitivity.  If <code>refreshRow</code> is called
     * after calling an updater method, but before calling
     * the method <code>updateRow</code>, then the
     * updates made to the row are lost.  Calling the method
     * <code>refreshRow</code> frequently will likely slow performance. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * Up to and including 1.7.1, HSQLDB does not support updateable
     * results. <p>
     *
     * Calling this method always throws a SQLException, stating that
     * the function is not supported. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @exception SQLException if a database access error
     * occurs or if this method is called when the cursor is on the insert row
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     *    jdbcResultSet)
     */
    public void refreshRow() throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        throw getNotSupported();
    }

    /**
     * <!-- start generic documentation -->
     * Cancels the updates made to the current row in this
     * <code>ResultSet</code> object.
     * This method may be called after calling an
     * updater method(s) and before calling
     * the method <code>updateRow</code> to roll back
     * the updates made to a row.  If no updates have been made or
     * <code>updateRow</code> has already been called, this method has no
     * effect. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * Up to and including 1.7.1, HSQLDB does not support updateable
     * results. <p>
     *
     * Calling this method always throws a SQLException, stating that
     * the function is not supported. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @exception SQLException if a database access error
     *       occurs or if this method is called when the cursor is
     *       on the insert row
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     *   jdbcResultSet)
     */
    public void cancelRowUpdates() throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        throw getNotSupported();
    }

    /**
     * <!-- start generic documentation -->
     * Moves the cursor to the insert row.  The current cursor position is
     * remembered while the cursor is positioned on the insert row.
     *
     * The insert row is a special row associated with an updatable
     * result set.  It is essentially a buffer where a new row may
     * be constructed by calling the updater methods prior to
     * inserting the row into the result set.
     *
     * Only the updater, getter,
     * and <code>insertRow</code> methods may be
     * called when the cursor is on the insert row.  All of the columns in
     * a result set must be given a value each time this method is
     * called before calling <code>insertRow</code>.
     * An updater method must be called before a
     * getter method can be called on a column value. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * Up to and including 1.7.1, HSQLDB does not support updateable
     * results. <p>
     *
     * Calling this method always throws a SQLException, stating that
     * the function is not supported. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @exception SQLException if a database access error occurs
     * or the result set is not updatable
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     *   jdbcResultSet)
     */
    public void moveToInsertRow() throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        throw getNotSupported();
    }

    /**
     * <!-- start generic documentation -->
     * Moves the cursor to the remembered cursor position, usually the
     * current row.  This method has no effect if the cursor is not on
     * the insert row. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * Up to and including 1.7.1, HSQLDB does not support
     * {@link #moveToInsertRow()} so the current row is never strayed from.
     * Consequentially, this request is simply ignored. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     * @exception SQLException if a database access error occurs
     * or the result set is not updatable
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     *  jdbcResultSet)
     */
    public void moveToCurrentRow() throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves the <code>Statement</code> object that produced this
     * <code>ResultSet</code> object.
     * If the result set was generated some other way, such as by a
     * <code>DatabaseMetaData</code> method, this method returns
     * <code>null</code>. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @return the <code>Statment</code> object that produced
     * this <code>ResultSet</code> object or <code>null</code>
     * if the result set was produced some other way
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     *    jdbcResultSet)
     */
    public Statement getStatement() throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        return sqlStatement;
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of the designated column in the current row
     * of this <code>ResultSet</code> object as an <code>Object</code>
     * in the Java programming language.
     * If the value is an SQL <code>NULL</code>,
     * the driver returns a Java <code>null</code>.
     * This method uses the given <code>Map</code> object
     * for the custom mapping of the
     * SQL structured or distinct type that is being retrieved. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * Up to and including 1.7.1, HSQLDB does not support this feature.  <p>
     *
     * Calling this method always throws a <CODE>SQLException</CODE>,
     * stating that the function is not supported. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param i the first column is 1, the second is 2, ...
     * @param map a <code>java.util.Map</code> object that contains the
     *  mapping from SQL type names to classes in the Java programming
     *  language
     * @return an <code>Object</code> in the Java programming language
     * representing the SQL value
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     * jdbcResultSet)
     */
    public Object getObject(int i, Map map) throws SQLException {

        // ADDED:
        // trace was missing.
        // boucherb@users 20020413
        if (Trace.TRACE) {
            Trace.trace();
        }

        throw getNotSupported();
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of the designated column in the current row
     * of this <code>ResultSet</code> object as a <code>Ref</code> object
     * in the Java programming language. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * Up to and including 1.7.1, HSQLDB does not support this feature.  <p>
     *
     * Calling this method always throws a <CODE>SQLException</CODE>,
     * stating that the function is not supported. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param i the first column is 1, the second is 2, ...
     * @return a <code>Ref</code> object representing an SQL <code>REF</code>
     *  value
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     * jdbcResultSet)
     */
    public Ref getRef(int i) throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        throw getNotSupported();
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of the designated column in the current row
     * of this <code>ResultSet</code> object as a <code>Blob</code> object
     * in the Java programming language. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * Up to and including 1.7.1, HSQLDB does not support this feature.  <p>
     *
     * Calling this method always throws a <CODE>SQLException</CODE>,
     * stating that the function is not supported. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param i the first column is 1, the second is 2, ...
     * @return a <code>Blob</code> object representing the SQL
     *  <code>BLOB</code> value in the specified column
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     * jdbcResultSet)
     */
    public Blob getBlob(int i) throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        throw getNotSupported();
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of the designated column in the current row
     * of this <code>ResultSet</code> object as a <code>Clob</code> object
     * in the Java programming language. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * Up to and including 1.7.1, HSQLDB does not support this feature.  <p>
     *
     * Calling this method always throws a <CODE>SQLException</CODE>,
     * stating that the function is not supported. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param i the first column is 1, the second is 2, ...
     * @return a <code>Clob</code> object representing the SQL
     *   <code>CLOB</code> value in the specified column
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     *  jdbcResultSet)
     */
    public Clob getClob(int i) throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        throw getNotSupported();
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of the designated column in the current row
     * of this <code>ResultSet</code> object as an <code>Array</code> object
     * in the Java programming language. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * Up to and including 1.7.1, HSQLDB does not support this feature.  <p>
     *
     * Calling this method always throws a <CODE>SQLException</CODE>,
     * stating that the function is not supported. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param i the first column is 1, the second is 2, ...
     * @return an <code>Array</code> object representing the SQL
     *   <code>ARRAY</code> value in the specified column
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     *  jdbcResultSet)
     */
    public Array getArray(int i) throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        throw getNotSupported();
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of the designated column in the current row
     * of this <code>ResultSet</code> object as an <code>Object</code>
     * in the Java programming language.
     * If the value is an SQL <code>NULL</code>,
     * the driver returns a Java <code>null</code>.
     * This method uses the specified <code>Map</code> object for
     * custom mapping if appropriate. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * Up to and including 1.7.1, HSQLDB does not support this feature.  <p>
     *
     * Calling this method always throws a <CODE>SQLException</CODE>,
     * stating that the function is not supported. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param colName the name of the column from which to retrieve the value
     * @param map a <code>java.util.Map</code> object that contains the
     *   mapping from SQL type names to classes in the Java programming
     *   language
     * @return an <code>Object</code> representing the SQL value in the
     *   specified column
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     *  jdbcResultSet)
     */
    public Object getObject(String colName, Map map) throws SQLException {

        // MODIFIED:
        // made this consistent with all other
        // column name oriented methods
        // boucherb@users 2002013
        return getObject(findColumn(colName), map);
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of the designated column in the current row
     * of this <code>ResultSet</code> object as a <code>Ref</code> object
     * in the Java programming language. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * Up to and including 1.7.1, HSQLDB does not support this feature.  <p>
     *
     * Calling this method always throws a <CODE>SQLException</CODE>,
     * stating that the function is not supported. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     * @param colName the column name
     * @return a <code>Ref</code> object representing the SQL <code>REF</code>
     *   value in the specified column
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     *  jdbcResultSet)
     */
    public Ref getRef(String colName) throws SQLException {
        return getRef(findColumn(colName));
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of the designated column in the current row
     * of this <code>ResultSet</code> object as a <code>Blob</code> object
     * in the Java programming language. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * Up to and including 1.7.1, HSQLDB does not support this feature.  <p>
     *
     * Calling this method always throws a <CODE>SQLException</CODE>,
     * stating that the function is not supported. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param colName the name of the column from which to retrieve the value
     * @return a <code>Blob</code> object representing the
     *   SQL <code>BLOB</code> value in the specified column
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     *  jdbcResultSet)
     */
    public Blob getBlob(String colName) throws SQLException {
        return getBlob(findColumn(colName));
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of the designated column in the current row
     * of this <code>ResultSet</code> object as a <code>Clob</code> object
     * in the Java programming language. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * Up to and including 1.7.1, HSQLDB does not support this feature.  <p>
     *
     * Calling this method always throws a <CODE>SQLException</CODE>,
     * stating that the function is not supported. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param colName the name of the column from which to retrieve the value
     * @return a <code>Clob</code> object representing the SQL
     *   <code>CLOB</code> value in the specified column
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     *  jdbcResultSet)
     */
    public Clob getClob(String colName) throws SQLException {
        return getClob(findColumn(colName));
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of the designated column in the current row
     * of this <code>ResultSet</code> object as an <code>Array</code> object
     * in the Java programming language. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * Up to and including 1.7.1, HSQLDB does not support this feature.  <p>
     *
     * Calling this method always throws a <CODE>SQLException</CODE>,
     * stating that the function is not supported. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param colName the name of the column from which to retrieve the value
     * @return an <code>Array</code> object representing the SQL
     *   <code>ARRAY</code> value in the specified column
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     *  jdbcResultSet)
     */
    public Array getArray(String colName) throws SQLException {
        return getArray(findColumn(colName));
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of the designated column in the current row
     * of this <code>ResultSet</code> object as a
     * <code>java.sql.Date</code> object
     * in the Java programming language.
     * This method uses the given calendar to construct an appropriate
     * millisecond value for the date if the underlying database does
     * not store timezone information. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * Up to and including 1.7.1, HSQLDB does not support this feature.  <p>
     *
     * Calling this method always throws a <CODE>SQLException</CODE>,
     * stating that the function is not supported. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param cal the <code>java.util.Calendar</code> object
     * to use in constructing the date
     * @return the column value as a <code>java.sql.Date</code> object;
     * if the value is SQL <code>NULL</code>, the value returned is
     * <code>null</code> in the Java programming language
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     *  jdbcResultSet)
     */

// fredt@users 20020320 - comment - to do
// use new code already in jdbcPreparedStatement
    public java.sql.Date getDate(int columnIndex,
                                 Calendar cal) throws SQLException {

        // ADDED:
        // to be consistent:  this was missing
        // boucherb@users 20020413
        // TODO: implement, based on new jdbcPreparedStatement code
        // and change documentation to reflect
        if (Trace.TRACE) {
            Trace.trace();
        }

        throw getNotSupported();
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of the designated column in the current row
     * of this <code>ResultSet</code> object as a <code>java.sql.Date</code>
     * object in the Java programming language.
     * This method uses the given calendar to construct an appropriate
     * millisecond
     * value for the date if the underlying database does not store
     * timezone information. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * Up to and including 1.7.1, HSQLDB does not support this feature.  <p>
     *
     * Calling this method always throws a <CODE>SQLException</CODE>,
     * stating that the function is not supported. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param columnName the SQL name of the column from which to retrieve the
     *   value
     * @param cal the <code>java.util.Calendar</code> object
     *   to use in constructing the date
     * @return the column value as a <code>java.sql.Date</code> object;
     *   if the value is SQL <code>NULL</code>,
     *   the value returned is <code>null</code> in the Java programming
     *   language
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     *  jdbcResultSet)
     */

// fredt@users 20020320 - comment - to do
// use new code already in jdbcPreparedStatement
    public java.sql.Date getDate(String columnName,
                                 Calendar cal) throws SQLException {

        // MODIFIED:
        // made this consistent with our other
        // column name oriented methods
        // boucherb@users 20020413
        return getDate(findColumn(columnName), cal);
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of the designated column in the current row
     * of this <code>ResultSet</code> object as a <code>java.sql.Time</code>
     * object in the Java programming language.
     * This method uses the given calendar to construct an appropriate
     * millisecond value for the time if the underlying database does not
     * store timezone information. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * Up to and including 1.7.1, HSQLDB does not support this feature.  <p>
     *
     * Calling this method always throws a <CODE>SQLException</CODE>,
     * stating that the function is not supported. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param cal the <code>java.util.Calendar</code> object
     *   to use in constructing the time
     * @return the column value as a <code>java.sql.Time</code> object;
     *   if the value is SQL <code>NULL</code>,
     *   the value returned is <code>null</code> in the Java programming
     *   language
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     *   jdbcResultSet)
     */

// fredt@users 20020320 - comment - to do
// use new code already in jdbcPreparedStatement
    public java.sql.Time getTime(int columnIndex,
                                 Calendar cal) throws SQLException {

        // ADDED:
        // trace to be consistent:  this was missing
        // boucherb@users 20020413
        // TODO: implement, based on new jdbcPreparedStatement code
        // and change documentation to reflect
        if (Trace.TRACE) {
            Trace.trace();
        }

        throw getNotSupported();
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of the designated column in the current row
     * of this <code>ResultSet</code> object as
     * a <code>java.sql.Time</code> object
     * in the Java programming language.
     * This method uses the given calendar to construct an appropriate
     * millisecond
     * value for the time if the underlying database does not store
     * timezone information. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * Up to and including 1.7.1, HSQLDB does not support this feature.  <p>
     *
     * Calling this method always throws a <CODE>SQLException</CODE>,
     * stating that the function is not supported. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param columnName the SQL name of the column
     * @param cal the <code>java.util.Calendar</code> object
     *   to use in constructing the time
     * @return the column value as a <code>java.sql.Time</code> object;
     *   if the value is SQL <code>NULL</code>,
     * the value returned is <code>null</code> in the Java programming
     *   language
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     *  jdbcResultSet)
     */

// fredt@users 20020320 - comment - to do
// use new code already in jdbcPreparedStatement
    public java.sql.Time getTime(String columnName,
                                 Calendar cal) throws SQLException {

        // MODIFIED:
        // to be consistent with our
        // other column name oriented methods
        // boucherb@users 20020413
        return getTime(findColumn(columnName), cal);
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of the designated column in the current row
     * of this <code>ResultSet</code> object as a
     * <code>java.sql.Timestamp</code> object in the Java programming
     * anguage.
     * This method uses the given calendar to construct an appropriate
     * millisecond value for the timestamp if the underlying database does
     * not store timezone information. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * Up to and including 1.7.1, HSQLDB does not support this feature.  <p>
     *
     * Calling this method always throws a <CODE>SQLException</CODE>,
     * stating that the function is not supported. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param cal the <code>java.util.Calendar</code> object
     * to use in constructing the timestamp
     * @return the column value as a <code>java.sql.Timestamp</code> object;
     *   if the value is SQL <code>NULL</code>,
     *   the value returned is <code>null</code> in the Java programming
     *   language
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     *  jdbcResultSet)
     */
    public java.sql.Timestamp getTimestamp(int columnIndex,
                                           Calendar cal) throws SQLException {

        // ADDED:
        // trace was missing.  not consistent with the other methdods
        // boucherb@users 20020413
        // TODO: implement, based on new jdbcPreparedStatement code
        // and change documentation to reflect
        if (Trace.TRACE) {
            Trace.trace();
        }

        throw getNotSupported();
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of the designated column in the current row
     * of this <code>ResultSet</code> object as a
     * <code>java.sql.Timestamp</code> object in the Java programming
     * language.
     * This method uses the given calendar to construct an appropriate
     * millisecond value for the timestamp if the underlying database does
     * not store timezone information. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * Up to and including 1.7.1, HSQLDB does not support this feature.  <p>
     *
     * Calling this method always throws a <CODE>SQLException</CODE>,
     * stating that the function is not supported. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param columnName the SQL name of the column
     * @param cal the <code>java.util.Calendar</code> object
     *   to use in constructing the date
     * @return the column value as a <code>java.sql.Timestamp</code> object;
     *   if the value is SQL <code>NULL</code>,
     *   the value returned is <code>null</code> in the Java programming
     *   language
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     *  jdbcResultSet)
     */
    public java.sql.Timestamp getTimestamp(String columnName,
                                           Calendar cal) throws SQLException {

        // MODIFIED:
        // made this consistent with our other column name oriented methods
        // boucherb@users 20020413
        return getTimestamp(findColumn(columnName), cal);
    }

    //-------------------------- JDBC 3.0 ----------------------------------------

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of the designated column in the current row
     * of this <code>ResultSet</code> object as a <code>java.net.URL</code>
     * object in the Java programming language. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * Up to and including 1.7.1, HSQLDB does not support this feature.  <p>
     *
     * Calling this method always throws a <CODE>SQLException</CODE>,
     * stating that the function is not supported. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param columnIndex the index of the column 1 is the first, 2
     *    is the second,...
     * @return the column value as a <code>java.net.URL</code> object;
     *    if the value is SQL <code>NULL</code>, the value returned
     *    is <code>null</code> in the Java programming language
     * @exception SQLException if a database access error occurs,
     *    or if a URL is malformed
     * @since JDK 1.4, HSQLDB 1.7.0
     */
//#ifdef JDBC3
/*
    public java.net.URL getURL(int columnIndex) throws SQLException {
        throw getNotSupportedJDBC3();
    }


*/

//#endif JDBC3

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of the designated column in the current row
     * of this <code>ResultSet</code> object as a <code>java.net.URL</code>
     * object in the Java programming language. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * Up to and including 1.7.1, HSQLDB does not support this feature.  <p>
     *
     * Calling this method always throws a <CODE>SQLException</CODE>,
     * stating that the function is not supported. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param columnName the SQL name of the column
     * @return the column value as a <code>java.net.URL</code> object;
     * if the value is SQL <code>NULL</code>, the value returned
     * is <code>null</code> in the Java programming language
     * @exception SQLException if a database access error occurs
     *       or if a URL is malformed
     * @since JDK 1.4, HSQLDB 1.7.0
     */
//#ifdef JDBC3
/*
    public java.net.URL getURL(String columnName) throws SQLException {
        throw getNotSupportedJDBC3();
    }


*/

//#endif JDBC3

    /**
     * <!-- start generic documentation -->
     * Updates the designated column with a <code>java.sql.Ref</code> value.
     * The updater methods are used to update column values in the
     * current row or the insert row.  The updater methods do not
     * update the underlying database; instead the <code>updateRow</code> or
     * <code>insertRow</code> methods are called to update the database. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * Up to and including 1.7.1, HSQLDB does not support updateable
     * results or this data type. <p>
     *
     * Calling this method always throws a SQLException, stating that
     * the function is not supported. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param x the new column value
     * @exception SQLException if a database access error occurs
     * @since JDK 1.4, HSQLDB 1.7.0
     */
//#ifdef JDBC3
/*
    public void updateRef(int columnIndex,
                          java.sql.Ref x) throws SQLException {
        throw getNotSupportedJDBC3();
    }


*/

//#endif JDBC3

    /**
     * <!-- start generic documentation -->
     * Updates the designated column with a <code>java.sql.Ref</code> value.
     * The updater methods are used to update column values in the
     * current row or the insert row.  The updater methods do not
     * update the underlying database; instead the <code>updateRow</code> or
     * <code>insertRow</code> methods are called to update the database. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * Up to and including 1.7.1, HSQLDB does not support updateable
     * results or this data type. <p>
     *
     * Calling this method always throws a SQLException, stating that
     * the function is not supported. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param columnName the name of the column
     * @param x the new column value
     * @exception SQLException if a database access error occurs
     * @since JDK 1.4, HSQLDB 1.7.0
     */
//#ifdef JDBC3
/*
    public void updateRef(String columnName,
                          java.sql.Ref x) throws SQLException {
        throw getNotSupportedJDBC3();
    }


*/

//#endif JDBC3

    /**
     * <!-- start generic documentation -->
     * Updates the designated column with a <code>java.sql.Blob</code> value.
     * The updater methods are used to update column values in the
     * current row or the insert row.  The updater methods do not
     * update the underlying database; instead the <code>updateRow</code> or
     * <code>insertRow</code> methods are called to update the database. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * Up to and including 1.7.1, HSQLDB does not support updateable
     * results or this data type. <p>
     *
     * Calling this method always throws a SQLException, stating that
     * the function is not supported. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param x the new column value
     * @exception SQLException if a database access error occurs
     * @since JDK 1.4, HSQLDB 1.7.0
     */
//#ifdef JDBC3
/*
    public void updateBlob(int columnIndex,
                           java.sql.Blob x) throws SQLException {
        throw getNotSupportedJDBC3();
    }


*/

//#endif JDBC3

    /**
     * <!-- start generic documentation -->
     * Updates the designated column with a <code>java.sql.Blob</code> value.
     * The updater methods are used to update column values in the
     * current row or the insert row.  The updater methods do not
     * update the underlying database; instead the <code>updateRow</code> or
     * <code>insertRow</code> methods are called to update the database. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * Up to and including 1.7.1, HSQLDB does not support updateable
     * results or this data type. <p>
     *
     * Calling this method always throws a SQLException, stating that
     * the function is not supported. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param columnName the name of the column
     * @param x the new column value
     * @exception SQLException if a database access error occurs
     * @since JDK 1.4, HSQLDB 1.7.0
     */
//#ifdef JDBC3
/*
    public void updateBlob(String columnName,
                           java.sql.Blob x) throws SQLException {
        throw getNotSupportedJDBC3();
    }


*/

//#endif JDBC3

    /**
     * <!-- start generic documentation -->
     * Updates the designated column with a <code>java.sql.Clob</code> value.
     * The updater methods are used to update column values in the
     * current row or the insert row.  The updater methods do not
     * update the underlying database; instead the <code>updateRow</code> or
     * <code>insertRow</code> methods are called to update the database. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * Up to and including 1.7.1, HSQLDB does not support updateable
     * results or this data type. <p>
     *
     * Calling this method always throws a SQLException, stating that
     * the function is not supported. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param x the new column value
     * @exception SQLException if a database access error occurs
     * @since JDK 1.4, HSQLDB 1.7.0
     */
//#ifdef JDBC3
/*
    public void updateClob(int columnIndex,
                           java.sql.Clob x) throws SQLException {
        throw getNotSupportedJDBC3();
    }


*/

//#endif JDBC3

    /**
     * <!-- start generic documentation -->
     * Updates the designated column with a <code>java.sql.Clob</code> value.
     * The updater methods are used to update column values in the
     * current row or the insert row.  The updater methods do not
     * update the underlying database; instead the <code>updateRow</code> or
     * <code>insertRow</code> methods are called to update the database. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * Up to and including 1.7.1, HSQLDB does not support updateable
     * results or this data type. <p>
     *
     * Calling this method always throws a SQLException, stating that
     * the function is not supported. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param columnName the name of the column
     * @param x the new column value
     * @exception SQLException if a database access error occurs
     * @since JDK 1.4, HSQLDB 1.7.0
     */
//#ifdef JDBC3
/*
    public void updateClob(String columnName,
                           java.sql.Clob x) throws SQLException {
        throw getNotSupportedJDBC3();
    }


*/

//#endif JDBC3

    /**
     * <!-- start generic documentation -->
     * Updates the designated column with a <code>java.sql.Array</code> value.
     * The updater methods are used to update column values in the
     * current row or the insert row.  The updater methods do not
     * update the underlying database; instead the <code>updateRow</code> or
     * <code>insertRow</code> methods are called to update the database. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * Up to and including 1.7.1, HSQLDB does not support updateable
     * results or this data type. <p>
     *
     * Calling this method always throws a SQLException, stating that
     * the function is not supported. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param x the new column value
     * @exception SQLException if a database access error occurs
     * @since JDK 1.4, HSQLDB 1.7.0
     */
//#ifdef JDBC3
/*
    public void updateArray(int columnIndex,
                            java.sql.Array x) throws SQLException {
        throw getNotSupportedJDBC3();
    }


*/

//#endif JDBC3

    /**
     * <!-- start generic documentation -->
     * Updates the designated column with a <code>java.sql.Array</code> value.
     * The updater methods are used to update column values in the
     * current row or the insert row.  The updater methods do not
     * update the underlying database; instead the <code>updateRow</code> or
     * <code>insertRow</code> methods are called to update the database. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * Up to and including 1.7.1, HSQLDB does not support updateable
     * results or this data type. <p>
     *
     * Calling this method always throws a SQLException, stating that
     * the function is not supported. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param columnName the name of the column
     * @param x the new column value
     * @exception SQLException if a database access error occurs
     * @since JDK 1.4, HSQLDB 1.7.0
     */
//#ifdef JDBC3
/*
    public void updateArray(String columnName,
                            java.sql.Array x) throws SQLException {
        throw getNotSupportedJDBC3();
    }


*/

//#endif JDBC3
//----------------  java.sql.ResultSetMetaData implementation --------------

    /**
     * <!-- start generic documentation -->
     * Returns the number of columns in this <code>ResultSet</code>
     * object. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * See discussion at: {@link #getMetaData} <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     * @return the number of columns
     * @exception SQLException if a database access error occurs
     */
    public int getColumnCount() throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        return iColumnCount;
    }

    /**
     * <!-- start generic documentation -->
     * Indicates whether the designated column is automatically numbered,
     * thus read-only. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * HSQLDB 1.7.0 does not support this feature.  <p>
     *
     * The return value from this method depends on whether the
     * <code>jdbc.strict_md</code>
     * connection property is specified as true. When this property is true:
     * Calling this method always throws a <CODE>SQLException</CODE>,
     * stating that the function is not supported. <p>
     *
     * See discussion at: {@link #getMetaData} <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param column the first column is 1, the second is 2, ...
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean isAutoIncrement(int column) throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        // FIXME:
        //
        // We should throw:  this returns false even for columns
        // that *are* autoincrement (identity) columns,
        // which is incorrect behaviour.
        //
        // I realize that it makes no diff. w.r.t. result set updatability,
        // since we do not support updateable results, but that's
        // not the only reason client code wants to know something
        // like this.
        //
        // The (shudder...) alternative is to fix the
        // Result class to provide this info and do a
        // massive sweep of the engine code to ensure
        // the info is set correctly everywhere when
        // generating Result objects.
        // boucherb@users 20025013
        // MISSING:
        // checkColumn(column); ?
        // boucherb@users 20020413
        if (strictMetaData) {
            throw getNotSupported();
        }

        return false;
    }

    /**
     * <!-- start generic documentation -->
     * Indicates whether a column's case matters. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * HSQLDB 1.7.0 does not support this feature.  <p>
     *
     * The return value from this method depends on whether the
     * <code>jdbc.strict_md</code>
     * connection property is specified as true. When this property is true:
     * Calling this method always throws a <CODE>SQLException</CODE>,
     * stating that the function is not supported. <p>
     *
     * See discussion at: {@link #getMetaData} <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param column the first column is 1, the second is 2, ...
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean isCaseSensitive(int column) throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        // MISSING:
        // checkColumn(column); ?
        // boucherb@users 20020413
        if (strictMetaData) {
            throw getNotSupported();
        }

        return true;
    }

    /**
     * <!-- start generic documentation -->
     * Indicates whether the designated column can be used in a where
     * clause. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * HSQLDB 1.7.0 does not support this feature.  <p>
     *
     * The return value from this method depends on whether the
     * <code>jdbc.strict_md</code>
     * connection property is specified as true. When this property is true:
     * Calling this method always throws a <CODE>SQLException</CODE>,
     * stating that the function is not supported. <p>
     *
     * See discussion at: {@link #getMetaData} <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param column the first column is 1, the second is 2, ...
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean isSearchable(int column) throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        // CHECKME:
        // is this absolutely always true?
        // javav object (other) columns , for instance,
        // are always equal if not null.
        // Does that qualify as searchable?
        // We need to go back and read the spec. for searchable.
        // boucherb@users 20020413
        // MISSING:
        // checkColumn(column); ?
        // boucherb@users 20020413
// fredt@users - OTHER can be used in a WHERE clause but we don't know if
// RS column is a DB column or a computed value
        if (strictMetaData) {
            throw getNotSupported();
        }

        return true;
    }

    /**
     * <!-- start generic documentation -->
     * Indicates whether the designated column is a cash value. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * Up to and including HSQLDB 1.7.0, this method always returns false.<p>
     *
     * See discussion at: {@link #getMetaData} <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param column the first column is 1, the second is 2, ...
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean isCurrency(int column) throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        checkColumn(column);

        // FIXME:
        // we should throw until this not a todo
        // isCurrency <==> DECIMAL?
        // Re-read the DatabaseMetadata.getTypeInfo spec.
        // boucherb@users 20020413
        // MISSING:
        // boucherb@users 20020413
// fredt@users - 20020413 - DECIMAL has variable scale so it is not currency
        return false;
    }

    /**
     * <!-- start generic documentation -->
     * Indicates the nullability of values in the designated column. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * The return value from this method depends on whether the
     * <code>jdbc.strict_md</code>
     * connection property is specified as true. When this property is true:
     * Always returns <code>columnNullableUnknown</code>. <p>
     *
     * See discussion at: {@link #getMetaData} <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param column the first column is 1, the second is 2, ...
     * @return the nullability status of the given column; one of
     *   <code>columnNoNulls</code>,
     *   <code>columnNullable</code> or <code>columnNullableUnknown</code>
     * @exception SQLException if a database access error occurs
     */
    public int isNullable(int column) throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        // FIXME:
        //
        // We should throw:  this returns columnNullable even for columns
        // that are NOT NULL constrained columns,
        // which is incorrect behaviour.
        //
        // I realize that it makes no diff. w.r.t. result set updatability,
        // since we do not support updateable results, but that's
        // not the only reason client code wants to know something
        // like this.
        //
        // The (shudder...) alternative is to fix the
        // Result class to provide this info and do a
        // massive sweep of the engine code to ensure
        // the info is set correctly everywhere when
        // generating Result objects.
        // boucherb@users 20025013
        // MISSING:
        // checkColumn(column); ?
        // boucherb@users 20020413
        if (strictMetaData) {
            return columnNullableUnknown;
        }

        return columnNullable;
    }

    /**
     * <!-- start generic documentation -->
     * Indicates whether values in the designated column are signed
     * numbers. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * HSQLDB 1.7.1 adds support for this feature.  <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param column the first column is 1, the second is 2, ...
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean isSigned(int column) throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        checkColumn(column);

        int type = rResult.colType[column - 1];

        for (int i = 0; i < Column.numericTypes.length; i++) {
            if (type == Column.numericTypes[i]) {
                return true;
            }
        }

        return false;
    }

    /**
     * <!-- start generic documentation -->
     * Indicates the designated column's normal maximum width in
     * characters. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * Up to and including HSQLDB 1.7.0, this method always returns
     * 0 (no limit/unknown).<p>
     *
     * See discussion at: {@link #getMetaData} <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param column the first column is 1, the second is 2, ...
     * @return the normal maximum number of characters allowed as the width
     *    of the designated column
     * @exception SQLException if a database access error occurs
     */
    public int getColumnDisplaySize(int column) throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        checkColumn(column);

        // Some program expect that this is the maximum allowed length
        // for this column, so it is dangerous to return the size required
        // to display all records
        return 0;
    }

    /**
     * <!-- start generic documentation -->
     * Gets the designated column's suggested title for use in printouts and
     * displays. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * In HSQLDB a <code>ResultSet</code> column label is determined in the
     * following order of precedence:<p>
     *
     * <OL>
     * <LI>The label (alias) specified in the generating query.</LI>
     * <LI>The name of the underlying column, if no label is specified.<br>
     *    This also applies to aggregate functions.</LI>
     * <LI>An empty <CODE>String</CODE>.</LI>
     * </OL> <p>
     *
     * See discussion at: {@link #getMetaData} <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param column the first column is 1, the second is 2, ...
     * @return the suggested column title
     * @exception SQLException if a database access error occurs
     */
    public String getColumnLabel(int column) throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        checkColumn(column);

        return rResult.sLabel[--column];
    }

    /**
     * <!-- start generic documentation -->
     * Get the designated column's name. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * In HSQLDB a ResultSet column name is determined in the following
     * order of prcedence:<p>
     *
     * <OL>
     * <LI>The name of the underlying columnm, if the ResultSet column
     *   represents a column in a table.</LI>
     * <LI>The label or alias specified in the generating query.</LI>
     * <LI>An empty <CODE>String</CODE>.</LI>
     * </OL> <p>
     *
     * If the <code>jdbc.get_column_name</code> property of the database
     * has been set to false, this method returns the same value as
     * {@link #getColumnLabel(int)}.<p>
     *
     * See discussion at: {@link #getMetaData} <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param column the first column is 1, the second is 2, ...
     * @return column name
     * @exception SQLException if a database access error occurs
     */
    public String getColumnName(int column) throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        checkColumn(column);

        if (getColumnName) {
            return rResult.sName[--column];
        } else {
            return rResult.sLabel[--column];
        }
    }

    /**
     * <!-- start generic documentation -->
     * Get the designated column's table's schema. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * Up to and including 1.7.1, HSQLDD does not support schema names. <p>
     *
     * This method always returns an empty <CODE>String</CODE>.<p>
     *
     * See discussion at: {@link #getMetaData} <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param column the first column is 1, the second is 2, ...
     * @return schema name or "" if not applicable
     * @exception SQLException if a database access error occurs
     */
    public String getSchemaName(int column) throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        checkColumn(column);

        return "";
    }

    /**
     * <!-- start generic documentation -->
     * Get the designated column's number of decimal digits. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * Up to and including HSQLDB 1.7.0, this method always returns
     * 0 (unknown/no limit).<p>
     *
     * See discussion at: {@link #getMetaData} <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     * @param column the first column is 1, the second is 2, ...
     * @return precision
     * @exception SQLException if a database access error occurs
     */
    public int getPrecision(int column) throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        checkColumn(column);

        return 0;
    }

    /**
     * <!-- start generic documentation -->
     * Gets the designated column's number of digits to right of the
     * decimal point. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * Up to and including HSQLDB 1.7.0, this method always returns
     * 0 (unknown).<p>
     *
     * See discussion at: {@link #getMetaData} <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param column the first column is 1, the second is 2, ...
     * @return scale
     * @exception SQLException if a database access error occurs
     */
    public int getScale(int column) throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        checkColumn(column);

        return 0;
    }

    /**
     * <!-- start generic documentation -->
     * Gets the designated column's table name. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * See discussion at: {@link #getMetaData} <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param column the first column is 1, the second is 2, ...
     * @return table name or "" if not applicable
     * @exception SQLException if a database access error occurs
     */
    public String getTableName(int column) throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        checkColumn(column);

        return rResult.sTable[--column];
    }

    /**
     * <!-- start generic documentation -->
     * Gets the designated column's table's catalog name. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * Up to and including 1.7.1, HSQLDD does not support catalogs. <p>
     *
     * This method always returns an empty <CODE>String</CODE>.<p>
     *
     * See discussion at: {@link #getMetaData} <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param column the first column is 1, the second is 2, ...
     * @return the name of the catalog for the table in which the given column
     *     appears or "" if not applicable
     * @exception SQLException if a database access error occurs
     */
    public String getCatalogName(int column) throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        checkColumn(column);

        return "";
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves the designated column's SQL type. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * This reports the SQL type of the column. HSQLDB can return Objects in
     * any Java integral type wider than <code>Integer</code> for an SQL
     * integral type.<p>
     *
     * See discussion at: {@link #getMetaData} <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     *
     * @param column the first column is 1, the second is 2, ...
     * @return SQL type from java.sql.Types
     * @exception SQLException if a database access error occurs
     * @see java.sql.Types
     */
    public int getColumnType(int column) throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        checkColumn(column);

        return rResult.colType[--column];
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves the designated column's database-specific type name. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * See above at: (@link #getColumnType)<p>
     *
     * See discussion at: {@link #getMetaData} <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param column the first column is 1, the second is 2, ...
     * @return type name used by the database. If the column type is
     * a user-defined type, then a fully-qualified type name is returned.
     * @exception SQLException if a database access error occurs
     */
    public String getColumnTypeName(int column) throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        checkColumn(column);

        return Column.getTypeString(rResult.colType[--column]);
    }

    /**
     * <!-- start generic documentation -->
     * Indicates whether the designated column is definitely not writable.<p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * HSQLDB 1.7.0 does not support this feature. <p>
     *
     * The return value from this method depends on whether the
     * <code>jdbc.strict_md</code>
     * connection property is specified as true. When this property is true:
     * Calling this method always throws a <CODE>SQLException</CODE>,
     * stating that the function is not supported. <p>
     *
     * See discussion at: {@link #getMetaData} <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param column the first column is 1, the second is 2, ...
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean isReadOnly(int column) throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        // FIXME:
        // we should throw until this is not a todo
        // isReadOnly <==>
        //     db readonly |
        //     connection readonly |
        //     table readonly |
        //     user not granted insert or update or delete on table |
        //     user not granted update on column (not yet supported)
        // boucherb@users 20020413
        // MISSING:
        // checkColumn(column); ?
        // boucherb@users 20020413
        // fredt@users - 20020413 - also if the RS column is a DB column
        if (strictMetaData) {
            throw getNotSupported();
        }

        return false;
    }

    /**
     * <!-- start generic documentation -->
     * Indicates whether it is possible for a write on the designated
     * column to succeed. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * HSQLDB 1.7.0 does not support this feature. <p>
     *
     * The return value from this method depends on whether the
     * <code>jdbc.strict_md</code>
     * connection property is specified as true. When this property is true:
     * Calling this method always throws a <CODE>SQLException</CODE>,
     * stating that the function is not supported. <p>
     *
     * See discussion at: {@link #getMetaData} <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param column the first column is 1, the second is 2, ...
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean isWritable(int column) throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        // FIXME:
        // we should throw until this is not a todo
        // we just don't know as it is: how can we say true?
        // isWritable <==>
        // !isReadOnly &
        // user *is* granted update on table (if column has non-null table)
        // boucherb@users 20020413
        // MISSING:
        // checkColumn(column); ?
        // boucherb@users 20020413
        // fredt@users - 20020413 - also if the RS column is a DB column
        if (strictMetaData) {
            throw getNotSupported();
        }

        return true;
    }

    /**
     * <!-- start generic documentation -->
     * Indicates whether a write on the designated column will definitely
     * succeed. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * HSQLDB 1.7.0 does not support this feature. <p>
     *
     * The return value from this method depends on whether the
     * <code>jdbc.strict_md</code>
     * connection property is specified as true. When this property is true:
     * Calling this method always throws a <CODE>SQLException</CODE>,
     * stating that the function is not supported. <p>
     *
     * See discussion at: {@link #getMetaData} <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param column the first column is 1, the second is 2, ...
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean isDefinitelyWritable(int column) throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        // FIXME:
        // we should throw until this is not a todo
        // we just don't know as it is: how can we say true?
        // isDefinitelyWritable <==>
        //     isWritable &
        //     ???
        // boucherb@users 20020413
        // MISSING:
        // checkColumn(column); ?
        // boucherb@users 20020413
        // fredt@users - 20020413 - also if the RS column is a DB column
        if (strictMetaData) {
            throw getNotSupported();
        }

        return true;
    }

    //--------------------------JDBC 2.0-----------------------------------

    /**
     * <!-- start generic documentation -->
     * Returns the fully-qualified name of the Java class whose instances
     * are manufactured if the method <code>ResultSet.getObject</code>
     * is called to retrieve a value
     * from the column.  <code>ResultSet.getObject</code> may return a
     * subclass of the class returned by this method. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * HSQLDB 1.7.0 does not support this feature. <p>
     *
     * Calling this method always throws a <CODE>SQLException</CODE>,
     * stating that the function is not supported. <p>
     *
     * See discussion at: {@link #getMetaData} <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param column the first column is 1, the second is 2, ...
     * @return the fully-qualified name of the class in the Java programming
     *   language that would be used by the method
     *   <code>ResultSet.getObject</code> to retrieve the value in the
     *   specified column. This is the class name used for custom mapping.
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     *  jdbcResultSet)
     */
    public String getColumnClassName(int column) throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        // fredt@users - 20020413
        // need a reverse mapping list between SQL and Java types used
        // in HSQLDB.
        throw getNotSupported();
    }

    //-------------------- Internal Implementation -------------------------
// Support for JDBC 2 from JRE 1.1.x
//#ifdef JAVA2
//#else
/*
    public static final int FETCH_FORWARD               = 1000;
    public static final int TYPE_FORWARD_ONLY           = 1003;
    public static final int TYPE_SCROLL_INSENSITIVE     = 1004;
    public static final int CONCUR_READ_ONLY            = 1007;



*/

//#endif
    //---------------------------- Private ---------------------------------

    /**
     * Convenience method for throwing FUNCTION_NOT_SUPPORTED
     *
     * @return a SQLException object whose message states that the function is
     * not supported
     */
    private SQLException getNotSupported() {
        return Trace.error(Trace.FUNCTION_NOT_SUPPORTED);
    }

    /**
     * Convenience method for throwing FUNCTION_NOT_SUPPORTED for JDBC 3
     * methods.
     *
     * @return a SQLException object whose message states that the function is
     * not supported and is a JDBC 3 method
     */
    private SQLException getNotSupportedJDBC3() {
        return Trace.error(Trace.FUNCTION_NOT_SUPPORTED, "JDBC3");
    }

    /**
     * Internal row data availability check.
     *
     * @throws  SQLException when no row data is available
     */
    private void checkAvailable() throws SQLException {

        if (rResult == null ||!bInit || nCurrent == null) {
            throw Trace.error(Trace.NO_DATA_IS_AVAILABLE);
        }
    }

    /**
     * Internal check for column index validity.
     *
     * @param columnIndex to check
     * @throws SQLException when this result has no such column
     */
    private void checkColumn(int columnIndex) throws SQLException {

        if (columnIndex < 1 || columnIndex > iColumnCount) {
            throw Trace.error(Trace.COLUMN_NOT_FOUND, columnIndex);
        }
    }

    /**
     * Internal wasNull tracker.
     *
     * @param  o the Object to track
     */
    private void checkNull(Object o) {

        if (o == null) {
            bWasNull = true;
        } else {
            bWasNull = false;
        }
    }

    /**
     * Internal value converter. <p>
     *
     * All trivially successful getXXX methods eventually go through this
     * method, converting if neccessary from the hsqldb-native representation
     * of a column's value to the requested representation.  <p>
     *
     * @return an Object of the requested type, representing the value of the
     *       specified column
     * @param columnIndex of the column value for which to perform the
     *                 conversion
     * @param type the target Java object type for the conversion
     * @throws SQLException when there is no data, the column index is
     *    invalid, or the conversion cannot be performed
     */
    private Object getColumnInType(int columnIndex,
                                   int type) throws SQLException {

        checkAvailable();

        int    t;
        Object o;

        try {
            t = rResult.colType[--columnIndex];
            o = nCurrent.data[columnIndex];
        } catch (ArrayIndexOutOfBoundsException e) {
            throw Trace.error(Trace.COLUMN_NOT_FOUND, ++columnIndex);
        }

        checkNull(o);

        // no conversion necessary
        if (type == t) {
            return o;
        }

        // try to convert
        try {
            return Column.convertObject(o, type);
        } catch (Exception e) {
            String s = "type: " + Column.getTypeString(t) + " (" + t
                       + ") expected: " + Column.getTypeString(type)
                       + " value: " + o.toString();

            throw Trace.error(Trace.WRONG_DATA_TYPE, s);
        }
    }

    //-------------------------- Package Private ---------------------------

    /**
     * Constructs a new <CODE>jdbcResultSet</CODE> object using the specified
     * <CODE>org.hsqldb.Result</CODE>.
     *
     * @param r the internal result form that the new <code>jdbcResultSet</code>
     * represents
     * @exception SQLException when the supplied Result is of type org.hsqldb.Result.ERROR
     */
    jdbcResultSet(Result r, HsqlProperties props) throws SQLException {

        connProperties = props;
        if (props != null){
            getColumnName  = props.isPropertyTrue("jdbc.get_column_name", true);
            strictMetaData = props.isPropertyTrue("jdbc.strict_md", false);
        }
        if (r.iMode == Result.UPDATECOUNT) {
            iUpdateCount = r.iUpdateCount;
        } else if (r.iMode == Result.ERROR) {

// fredt@users 20020221 - patch 513005 by sqlbob@users (RMP)
// tony_lai@users 20020820 - patch 595073
//            throw (Trace.getError(r.errorCode, r.sError));
            throw (Trace.getError(r.sError, r.errorCode));
        } else {
            iUpdateCount = -1;
            rResult      = r;
            iColumnCount = r.getColumnCount();
        }

        bWasNull = false;
    }

    /**
     * If executing my statement updated rows on the database, how many were
     * affected?
     *
     * @return the number of rows affected by executing my statement
     */
    int getUpdateCount() {
        return iUpdateCount;
    }

    /**
     * Does this Result contain actual row data? <p>
     *
     * Not all results have row data.  Some are ERROR results
     * (an execption occured while executing my statement), and
     * some are UPDATE results, in which case updates occured to rows
     * on the database, but no rows were actually returned.
     *
     * @return true if Result has row data, false if not.
     */
    boolean isResult() {
        return rResult == null ? false
                               : true;
    }
}
