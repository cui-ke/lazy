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

import org.hsqldb.HsqlProperties;
import java.io.*;
import java.sql.*;

/**
 * Test large cached tables by setting up a cached table of 100000 records
 * or more and a much smaller memory table with about 1/100th rows used.
 * Populate both tables so that an indexed column of the cached table has a
 * foreign key reference to the main table.
 *
 * This database can be used to demonstrate efficient queries to retrieve
 * the data from the cached table.
 *
 * insert timings for 100000 rows, cache scale 12:
 * simple table, no extra index: 52 s
 * with index on lastname only: 56 s
 * with index on zip only: 211 s
 * foreign key, referential_integrity true: 216 s
 *
 * @author fredt@users
 */
public class TestCacheSize {

    protected String url      = "jdbc:hsqldb:";
    protected String filepath = "/hsql/test/testcachesize";
    String           user;
    String           password;
    Statement        sStatement;
    Connection       cConnection;

    protected void setUp() {

        user     = "sa";
        password = "";

        try {
            sStatement  = null;
            cConnection = null;

            HsqlProperties props      = new HsqlProperties(filepath);
            boolean        fileexists = props.checkFileExists();

            Class.forName("org.hsqldb.jdbcDriver");
            System.out.println("connect");
            System.out.println(
                new java.util.Date(System.currentTimeMillis()));

            cConnection = DriverManager.getConnection(url + filepath, user,
                    password);

            System.out.println("connected");
            System.out.println(
                new java.util.Date(System.currentTimeMillis()));

            if (fileexists == false) {
                sStatement = cConnection.createStatement();

                sStatement.execute("SHUTDOWN");
                cConnection.close();
                props.load();
                props.setProperty("hsqldb.cache_scale", "15");
                props.save();

                cConnection = DriverManager.getConnection(url + filepath,
                        user, password);
                sStatement = cConnection.createStatement();
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("TestSql.setUp() error: " + e.getMessage());
        }
    }

    /**
     * Fill up the cache
     *
     *
     */
    public void testFillUp() {

        int    bigrows   = 200000;
        int    smallrows = 0xfff;
        double value     = 0;
        String ddl1 = "DROP TABLE test IF EXISTS;"
                      + "DROP TABLE zip IF EXISTS;";
        String ddl2 = "CREATE TABLE zip( zip INT IDENTITY );";
        String ddl3 = "CREATE CACHED TABLE test( id INT IDENTITY,"
                      + " firstname VARCHAR, " + " lastname VARCHAR, "
                      + " zip INTEGER, " + " filler VARCHAR); ";

        // adding extra index will slow down inserts a bit
        String ddl4 = "CREATE INDEX idx1 ON TEST (lastname);";

        // adding this index will slow down  inserts a lot
        String ddl5 = "CREATE INDEX idx2 ON TEST (zip);";

        // referential integrity checks will slow down inserts a bit
        String ddl6 =
            "ALTER TABLE test add constraint c1 FOREIGN KEY (zip) REFERENCES zip(zip);";
        String ddl7 = "CREATE TEMP TABLE temptest( id INT,"
                      + " firstname VARCHAR, " + " lastname VARCHAR, "
                      + " zip INTEGER, " + " filler VARCHAR); ";
        String filler =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZABCDEFGHIJKLMNOPQRSTUVWXYZ"
            + "ABCDEFGHIJKLMNOPQRSTUVWXYZABCDEFGHIJKLMNOPQRSTUVWXYZ";

        try {
            cConnection = null;
            sStatement  = null;
            cConnection = DriverManager.getConnection(url + filepath, user,
                    password);
            sStatement = cConnection.createStatement();

            java.util.Random randomgen = new java.util.Random();

            sStatement.execute(ddl1);
            sStatement.execute(ddl2);
            sStatement.execute(ddl3);
            System.out.println("test table with no index");

//            sStatement.execute(ddl4);
//            System.out.println("create index on lastname");
            sStatement.execute(ddl5);
            System.out.println("create index on zip");

//            sStatement.execute(ddl6);
//            System.out.println("add foreign key");
//            sStatement.execute(ddl7);
//            System.out.println("temp table");
//            sStatement.execute("CREATE INDEX idx3 ON tempTEST (zip);");
            int i;

            for (i = 0; i <= smallrows; i++) {
                sStatement.execute("INSERT INTO zip VALUES(null);");
            }

//            sStatement.execute("SET REFERENTIAL_INTEGRITY false;");
            PreparedStatement ps = cConnection.prepareStatement(
                "INSERT INTO test (firstname,lastname,zip,filler) VALUES (?,?,?,?)");

            ps.setString(1, "Julia");
            ps.setString(2, "Clancy");

            long startTime = System.currentTimeMillis();

            for (i = 0; i < bigrows; i++) {
                ps.setInt(3, randomgen.nextInt() & smallrows);
                ps.setString(4, randomgen.nextLong() + filler);
                ps.execute();

                if (i != 0 && i % 10000 == 0) {
                    System.out.println(i);
                    System.out.println(
                        new java.util.Date(System.currentTimeMillis()));

//                    sStatement.execute("INSERT INTO test SELECT * FROM temptest;");
//                    sStatement.execute("DROP TABLE temptest;");
//                    sStatement.execute(ddl7);
                }
            }

//            sStatement.execute("INSERT INTO test SELECT * FROM temptest;");
//            sStatement.execute("DROP TABLE temptest;");
//            sStatement.execute(ddl7);
            long endTime = System.currentTimeMillis();

            System.out.println(i);
            System.out.println(new java.util.Date(endTime));
            System.out.println("Insert Time:" + (endTime - startTime));
            sStatement.execute("SHUTDOWN");
            System.out.println("shutdown");
            System.out.println(
                new java.util.Date(System.currentTimeMillis()));
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    protected void tearDown() {

        try {
            cConnection.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("TestSql.tearDown() error: " + e.getMessage());
        }
    }

    public static void main(String argv[]) {

        TestCacheSize test = new TestCacheSize();

        test.setUp();
        test.testFillUp();
        test.tearDown();
    }
}
