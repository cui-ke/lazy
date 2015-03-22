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


package org.hsqldb.util;

import java.sql.Statement;
import java.sql.SQLException;
import java.util.Random;
import java.io.*;

/**
 * Common code in Swing and AWT versions of DatabaseManager
 * @version 1.7.0
 */

// sqlbob@users 20020401 - patch 1.7.0 by sqlbob (RMP) - enhancements
// sqlbob@users 20020407 - patch 1.7.0 - reengineering
class DatabaseManagerCommon {

    private static Random rRandom      = new Random(100);
    static String         selectHelp[] = {
        "SELECT * FROM ",
        "SELECT [LIMIT n m] [DISTINCT] \n"
        + "{ selectExpression | table.* | * } [, ... ] \n"
        + "[INTO [CACHED|TEMP|TEXT] newTable] \n" + "FROM tableList \n"
        + "[WHERE Expression] \n"
        + "[ORDER BY selectExpression [{ASC | DESC}] [, ...] ] \n"
        + "[GROUP BY Expression [, ...] ] \n"
        + "[UNION [ALL] selectStatement]"
    };
    static String insertHelp[] = {
        "INSERT INTO ",
        "INSERT INTO table [ (column [,...] ) ] \n"
        + "{ VALUES(Expression [,...]) | SelectStatement }"
    };
    static String updateHelp[] = {
        "UPDATE ",
        "UPDATE table SET column = Expression [, ...] \n"
        + "[WHERE Expression]"
    };
    static String deleteHelp[]      = {
        "DELETE FROM ", "DELETE FROM table [WHERE Expression]"
    };
    static String createTableHelp[] = {
        "CREATE TABLE ",
        "CREATE [TEMP] [CACHED|MEMORY|TEXT] TABLE name \n"
        + "( columnDefinition [, ...] ) \n\n" + "columnDefinition: \n"
        + "column DataType [ [NOT] NULL] [PRIMARY KEY] \n" + "DataType: \n"
        + "{ INTEGER | DOUBLE | VARCHAR | DATE | TIME |... }"
    };
    static String dropTableHelp[]   = {
        "DROP TABLE ", "DROP TABLE table"
    };
    static String createIndexHelp[] = {
        "CREATE INDEX ",
        "CREATE [UNIQUE] INDEX index ON \n" + "table (column [, ...])"
    };
    static String dropIndexHelp[]  = {
        "DROP INDEX ", "DROP INDEX table.index"
    };
    static String checkpointHelp[] = {
        "CHECKPOINT", "(HSQLDB SQL only)"
    };
    static String scriptHelp[]     = {
        "SCRIPT", "SCRIPT ['file']\n\n" + "(HSQLDB SQL only)"
    };
    static String shutdownHelp[]   = {
        "SHUTDOWN", "SHUTDOWN [COMPACT|IMMEDIATELY]\n\n" + "(HSQLDB SQL only)"
    };
    static String setHelp[]        = {
        "SET ",
        "AUTOCOMMIT { TRUE | FALSE }\n" + "IGNORECASE { TRUE | FALSE }\n"
        + "LOGSIZE size\n" + "MAXROWS maxrows\n" + "PASSWORD password\n"
        + "READONLY { TRUE | FALSE }\n"
        + "REFERENTIAL_INTEGRITY { TRUE | FALSE }\n"
        + "TABLE table READONLY { TRUE | FALSE }\n"
        + "TABLE table SOURCE \"file\" [DESC]\n"
        + "WRITE_DELAY { TRUE | FALSE }\n\n" + "(HSQLDB SQL only)"
    };
    static String testHelp[] = {
        "-->>>TEST<<<-- ;\n" + "--#1000;\n" + "DROP TABLE Test ;\n"
        + "CREATE TABLE Test(\n" + "  Id INTEGER PRIMARY KEY,\n"
        + "  FirstName VARCHAR(20),\n" + "  Name VARCHAR(50),\n"
        + "  ZIP INTEGER) ;\n" + "INSERT INTO Test \n"
        + "  VALUES(#,'Julia','Peterson-Clancy',#) ;\n"
        + "UPDATE Test SET Name='Hans' WHERE Id=# ;\n"
        + "SELECT * FROM Test WHERE Id=# ;\n"
        + "DELETE FROM Test WHERE Id=# ;\n" + "DROP TABLE Test",
        "This test script is parsed by the DatabaseManager\n"
        + "It may be changed manually. Rules:\n"
        + "- it must start with -->>>TEST<<<--.\n"
        + "- each line must end with ';' (no spaces after)\n"
        + "- lines starting with -- are comments\n"
        + "- lines starting with --#<count> means set new count\n"
    };
    static String testDataSql[] = {
        "SELECT * FROM Product", "SELECT * FROM Document",
        "SELECT * FROM Position",
        "SELECT * FROM Address a INNER JOIN Document d ON a.ID=d.AddressID",
        "SELECT * FROM Document d INNER JOIN Position p ON d.ID=p.DocumentID",
        "SELECT * FROM Address WHERE Street LIKE '1%' ORDER BY Lastname"
    };

    /**
     * Method declaration
     *
     *
     * @param s
     *
     * @return
     */
    static String random(String s[]) {
        return s[random(s.length)];
    }

    /**
     * Method declaration
     *
     *
     * @param i
     *
     * @return
     */
    static int random(int i) {

        i = rRandom.nextInt() % i;

        return i < 0 ? -i
                     : i;
    }

    /**
     * Method declaration
     *
     */
    static void createTestTables(Statement sStatement) {

        String demo[] = {
            "DROP TABLE Address", "DROP TABLE Product", "DROP TABLE Document",
            "DROP TABLE Position",
            "CREATE TABLE Address(ID INTEGER PRIMARY KEY,FirstName VARCHAR(255),"
            + "LastName VARCHAR(255),Street VARCHAR(255),City VARCHAR(255))",
            "CREATE TABLE Product(ID INTEGER PRIMARY KEY,Name VARCHAR(255),"
            + "Cost DECIMAL)",
            "CREATE TABLE Document(ID INTEGER PRIMARY KEY,AddressID INTEGER,"
            + "Total DECIMAL)",
            "CREATE TABLE Position(DocumentID INTEGER,Position INTEGER,"
            + "ProductID INTEGER,Quantity INTEGER,Price DECIMAL,"
            + "PRIMARY KEY(DocumentID,Position))"
        };

        for (int i = 0; i < demo.length; i++) {

            // drop table may fail
            try {
                sStatement.execute(demo[i]);
            } catch (SQLException e) {}
        }
    }

    /**
     * Method declaration
     *
     */
    static String createTestData(Statement sStatement) throws SQLException {

        String name[] = {
            "White", "Karsen", "Smith", "Ringer", "May", "King", "Fuller",
            "Miller", "Ott", "Sommer", "Schneider", "Steel", "Peterson",
            "Heiniger", "Clancy"
        };
        String firstname[] = {
            "Mary", "James", "Anne", "George", "Sylvia", "Robert", "Janet",
            "Michael", "Andrew", "Bill", "Susanne", "Laura", "Bob", "Julia",
            "John"
        };
        String street[] = {
            "Upland Pl.", "College Av.", "- 20th Ave.", "Seventh Av."
        };
        String city[]   = {
            "New York", "Dallas", "Boston", "Chicago", "Seattle",
            "San Francisco", "Berne", "Oslo", "Paris", "Lyon", "Palo Alto",
            "Olten"
        };
        String product[] = {
            "Iron", "Ice Tea", "Clock", "Chair", "Telephone", "Shoe"
        };
        int    max       = 50;

        for (int i = 0; i < max; i++) {
            sStatement.execute("INSERT INTO Address VALUES(" + i + ",'"
                               + random(firstname) + "','" + random(name)
                               + "','" + random(554) + " " + random(street)
                               + "','" + random(city) + "')");
            sStatement.execute("INSERT INTO Product VALUES(" + i + ",'"
                               + random(product) + " " + random(product)
                               + "'," + (20 + 2 * random(120)) + ")");
            sStatement.execute("INSERT INTO Document VALUES(" + i + ","
                               + random(max) + ",0.0)");

            for (int j = random(20) + 2; j >= 0; j--) {
                sStatement.execute("INSERT INTO Position VALUES(" + i + ","
                                   + j + "," + random(max) + ","
                                   + (1 + random(24)) + ",1.5)");
            }
        }

        sStatement.execute("UPDATE Product SET Cost=ROUND(Cost*.1,2)");
        sStatement.execute(
            "UPDATE Position SET Price=Price*"
            + "SELECT Cost FROM Product prod WHERE ProductID=prod.ID");
        sStatement.execute(
            "UPDATE Document SET Total=SELECT SUM(Price*"
            + "Quantity) FROM Position WHERE DocumentID=Document.ID");

        return ("SELECT * FROM Address");
    }

    /**
     * Method declaration
     *
     *
     * @param file
     *
     * @return
     */
    static String readFile(String file) {

        try {
            FileReader   read     = new FileReader(file);
            char         buffer[] = new char[1024];
            StringBuffer b        = new StringBuffer();

            while (true) {
                int i = read.read(buffer, 0, 1024);

                if (i == -1) {
                    break;
                }

                b.append(buffer, 0, i);
            }

            read.close();

            return b.toString();
        } catch (IOException e) {
            return e.getMessage();
        }
    }

    /**
     * Method declaration
     *
     *
     * @param file
     * @param text
     */
    static void writeFile(String file, String text) {

        try {
            FileWriter write = new FileWriter(file);

            write.write(text.toCharArray());
            write.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Method declaration
     *
     *
     * @param sql
     * @param max
     *
     * @return
     *
     * @throws SQLException
     */
    static long testStatement(Statement sStatement, String sql,
                              int max) throws SQLException {

        long start = System.currentTimeMillis();

        if (sql.indexOf('#') == -1) {
            max = 1;
        }

        for (int i = 0; i < max; i++) {
            String s = sql;

            while (true) {
                int j = s.indexOf("#r#");

                if (j == -1) {
                    break;
                }

                s = s.substring(0, j) + ((int) (Math.random() * i))
                    + s.substring(j + 3);
            }

            while (true) {
                int j = s.indexOf('#');

                if (j == -1) {
                    break;
                }

                s = s.substring(0, j) + i + s.substring(j + 1);
            }

            sStatement.execute(s);
        }

        return (System.currentTimeMillis() - start);
    }

    private DatabaseManagerCommon() {}
}
