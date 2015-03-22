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


package org.hsqldb.test;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Test cases for HSQL subselects.
 *
 * @author David Moles Apr 30, 2002
 */

// fredt@users - modified to remove dependecy on DBUnit
public class TestSubselect extends TestCase {

    //------------------------------------------------------------
    // Class variables
    //------------------------------------------------------------
    private static final String databaseDriver   = "org.hsqldb.jdbcDriver";
    private static final String databaseURL = "jdbc:hsqldb:../test/subselect";
    private static final String databaseUser     = "sa";
    private static final String databasePassword = "";
    public static final String  datasetPath = "../test/dataset-subselect.xml";

    //------------------------------------------------------------
    // Instance variables
    //------------------------------------------------------------
    private Connection jdbcConnection;

    //------------------------------------------------------------
    // Constructors
    //------------------------------------------------------------

    /**
     * Constructs a new SubselectTest.
     */
    public TestSubselect(String s) {
        super(s);
    }

    //------------------------------------------------------------
    // Class methods
    //------------------------------------------------------------
    protected static Connection getJDBCConnection() throws SQLException {
        return DriverManager.getConnection(databaseURL, databaseUser,
                                           databasePassword);
    }

    protected void setUp() throws Exception {

        super.setUp();
        Class.forName(databaseDriver);

        jdbcConnection = getJDBCConnection();
    }

    protected void tearDown() throws Exception {

        super.tearDown();
        jdbcConnection.close();

        jdbcConnection = null;
    }

    //------------------------------------------------------------
    // Helper methods
    //------------------------------------------------------------
    private static void compareResults(String sql, String[] expected,
                                       Connection jdbcConnection)
                                       throws SQLException {

        Statement statement = jdbcConnection.createStatement();
        ResultSet results   = statement.executeQuery(sql);
        int       rowCount  = 0;

        while (results.next()) {
            assertTrue("Statement <" + sql + "> returned too many rows.",
                       (rowCount < expected.length));
            assertEquals("Statement <" + sql + "> returned wrong value.",
                         expected[rowCount], results.getString(1));

            rowCount++;
        }

        assertEquals("Statement <" + sql
                     + "> returned wrong number of rows.", expected.length,
                         rowCount);
    }

    //------------------------------------------------------------
    // Test methods
    //------------------------------------------------------------

    /**
     * This test is basically a sanity check of the data set.
     */
    public void testSimpleJoin() throws SQLException {

        String sql =
            "select trees.id, trees.name, sizes.val, fruits.name, colors.val"
            + " from trees, sizes, fruits, colors"
            + " where trees.size_id = sizes.id"
            + " and trees.fruit_id = fruits.id"
            + " and fruits.color_id = colors.id" + " order by 1";
        int      expectedRows  = 5;
        String[] expectedTrees = new String[] {
            "small golden delicious tree", "large macintosh tree",
            "large red delicious tree", "small red delicious tree",
            "medium granny smith tree"
        };
        String[] expectedSizes  = new String[] {
            "small", "large", "large", "small", "medium"
        };
        String[] expectedFruits = new String[] {
            "golden delicious", "macintosh", "red delicious", "red delicious",
            "granny smith"
        };
        String[]  expectedColors = new String[] {
            "green", "red", "red", "red", "green"
        };
        Statement statement      = jdbcConnection.createStatement();
        ResultSet results        = statement.executeQuery(sql);
        String[]  trees          = new String[expectedRows];
        String[]  fruits         = new String[expectedRows];
        String[]  sizes          = new String[expectedRows];
        String[]  colors         = new String[expectedRows];
        int       rowCount       = 0;

        while (results.next()) {
            assertTrue("Statement <" + sql + "> returned too many rows.",
                       (rowCount <= expectedRows));
            assertEquals("Statement <" + sql
                         + "> returned rows in wrong order.", (1 + rowCount),
                             results.getInt(1));
            assertEquals("Statement <" + sql + "> returned wrong value.",
                         expectedTrees[rowCount], results.getString(2));
            assertEquals("Statement <" + sql + "> returned wrong value.",
                         expectedSizes[rowCount], results.getString(3));
            assertEquals("Statement <" + sql + "> returned wrong value.",
                         expectedFruits[rowCount], results.getString(4));
            assertEquals("Statement <" + sql + "> returned wrong value.",
                         expectedColors[rowCount], results.getString(5));

            rowCount++;
        }

        assertEquals("Statement <" + sql
                     + "> returned wrong number of rows.", expectedRows,
                         rowCount);
    }

    /**
     * Inner select with where clause in outer select having column with same name as where clause in inner select
     */
    public void testWhereClausesColliding() throws SQLException {

        String sql =
            "select name from fruits where id in (select fruit_id from trees where id < 3) order by name";
        String[] expected = new String[] {
            "golden delicious", "macintosh"
        };

        compareResults(sql, expected, jdbcConnection);
    }

    /**
     * As above, with table aliases.
     */
    public void testWhereClausesCollidingWithAliases() throws SQLException {

        String sql =
            "select a.name from fruits a where a.id in (select b.fruit_id from trees b where b.id < 3) order by name";
        String[] expected = new String[] {
            "golden delicious", "macintosh"
        };

        compareResults(sql, expected, jdbcConnection);
    }

    /**
     * Inner select with two tables having columns with the same name, one of which is referred to in the
     * subselect, the other of which is not used in the query (both FRUITS and TREES have NAME column,
     * but we're only selecting FRUITS.NAME and we're not referring to TREES.NAME at all).
     */
    public void testHiddenCollision() throws SQLException {

        String sql =
            "select name from fruits where id in (select fruit_id from trees) order by name";
        String[] expected = new String[] {
            "golden delicious", "granny smith", "macintosh", "red delicious"
        };

        compareResults(sql, expected, jdbcConnection);
    }

    /**
     * As above, with table aliases.
     */
    public void testHiddenCollisionWithAliases() throws SQLException {

        String sql =
            "select a.name from fruits a where a.id in (select b.fruit_id from trees b) order by a.name";
        String[] expected = new String[] {
            "golden delicious", "granny smith", "macintosh", "red delicious"
        };

        compareResults(sql, expected, jdbcConnection);
    }

    /**
     * Inner select with where clause in outer select having column with same name as select clause in inner select
     */
    public void testWhereSelectColliding() throws SQLException {

        // Yes, this is a nonsensical query
        String sql =
            "select val from colors where id in (select id from trees where fruit_id = 3) order by val";
        String[] expected = new String[] {
            "indigo", "orange"
        };

        compareResults(sql, expected, jdbcConnection);
    }

    /**
     * As above, with aliases.
     */
    public void testWhereSelectCollidingWithAliases() throws SQLException {

        // Yes, this is a nonsensical query
        String sql =
            "select a.val from colors a where a.id in (select b.id from trees b where b.fruit_id = 3) order by a.val";
        String[] expected = new String[] {
            "indigo", "orange"
        };

        compareResults(sql, expected, jdbcConnection);
    }

    /**
     * Inner select involving same table
     */
    public void testSameTable() throws SQLException {

        String sql =
            "select name from trees where id in (select id from trees where fruit_id = 3) order by name";
        String[] expected = new String[] {
            "large red delicious tree", "small red delicious tree"
        };

        compareResults(sql, expected, jdbcConnection);
    }

    /**
     * As above with aliases.
     */
    public void testSameTableWithAliases() throws SQLException {

        String sql =
            "select a.name from trees a where a.id in (select b.id from trees b where b.fruit_id = 3) order by a.name";
        String[] expected = new String[] {
            "large red delicious tree", "small red delicious tree"
        };

        compareResults(sql, expected, jdbcConnection);
    }

    /**
     * Inner select involving same table as one of two joined tables in outer select
     */
    public void testSameTableWithJoin() throws SQLException {

        String sql =
            "select sizes.val from trees, sizes where sizes.id = trees.size_id and trees.id in (select id from trees where fruit_id = 3) order by sizes.val";
        String[] expected = new String[] {
            "large", "small"
        };

        compareResults(sql, expected, jdbcConnection);
    }

    /**
     * Tests two subselects, anded.
     */
    public void testAndedSubselects() throws SQLException {

        String sql =
            "select name from trees where size_id in (select id from sizes where val = 'large') and fruit_id in (select id from fruits where color_id = 1) order by name";
        String[] expected = new String[] {
            "large macintosh tree", "large red delicious tree"
        };

        compareResults(sql, expected, jdbcConnection);
    }

    /**
     * Test nested subselects.
     */
    public void testNestedSubselects() throws SQLException {

        String sql =
            "select name from trees where fruit_id in (select id from fruits where color_id in (select id from colors where val = 'red')) order by name";
        String[] expected = new String[] {
            "large macintosh tree", "large red delicious tree",
            "small red delicious tree"
        };

        compareResults(sql, expected, jdbcConnection);
    }

    /**
     * Inner select with "not in" in outer select where clause.
     */
    public void testNotIn() throws SQLException {

        String sql =
            "select name from fruits where id not in (select fruit_id from trees) order by name";
        String[] expected = new String[]{ "tangerine" };

        compareResults(sql, expected, jdbcConnection);
    }

    /**
     * Inner select with "not in" in outer select where clause and same table in inner select where clause.
     */
    public void testNotInSameTableAndColumn() throws SQLException {

        String sql =
            "select name from fruits where id not in (select id from fruits where color_id > 1 ) order by name";
        String[] expected = new String[] {
            "macintosh", "red delicious"
        };

        compareResults(sql, expected, jdbcConnection);
    }

    /**
     * Inner select reusing alias names from outer select, but using them for different tables
     */
    public void testAliasScope() throws SQLException {

        String sql =
            "select a.val, b.name from sizes a, trees b where a.id = b.size_id and b.id in (select a.id from trees a, fruits b where a.fruit_id = b.id and b.name='red delicious') order by a.val";
        String expectedSizes[] = new String[] {
            "large", "small"
        };
        String expectedTrees[] = new String[] {
            "large red delicious tree", "small red delicious tree"
        };

        assertEquals(
            "Programmer error: expected arrays should be of equal length.",
            expectedSizes.length, expectedTrees.length);

        Statement statement = jdbcConnection.createStatement();
        ResultSet results   = statement.executeQuery(sql);
        int       rowCount  = 0;

        while (results.next()) {
            assertTrue("Statement <" + sql + "> returned too many rows.",
                       (rowCount < expectedSizes.length));
            assertEquals("Statement <" + sql + "> returned wrong value.",
                         expectedSizes[rowCount], results.getString(1));
            assertEquals("Statement <" + sql + "> returned wrong value.",
                         expectedTrees[rowCount], results.getString(2));

            rowCount++;
        }

        assertEquals(
            "Statement <" + sql + "> returned wrong number of rows.",
            expectedSizes.length, rowCount);
    }

    //------------------------------------------------------------
    // Main program
    //------------------------------------------------------------
    public static void main(String[] args) throws IOException {
        junit.swingui.TestRunner.run(TestSubselect.class);
    }
}
