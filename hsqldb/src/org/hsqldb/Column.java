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

import org.hsqldb.lib.HsqlDateTime;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.math.BigDecimal;
import java.util.Hashtable;
import java.text.Collator;

// fredt@users 20020320 - doc 1.7.0 - update
// fredt@users 20020401 - patch 442993 by fredt - arithmetic expressions
// to allow mixed type arithmetic expressions beginning with a narrower type
// changes applied to several lines of code and not marked separately
// consists of changes to arithmatic functions to allow promotion of
// java.lang.Number values and new functions to choose type for promotion
// fredt@users 20020401 - patch 455757 by galena@users (Michiel de Roo)
// interpretation of TINYINT as Byte instead of Short
// fredt@users 20020130 - patch 505356 by daniel_fiser@users
// use of the current locale for string comparison (instead of posix)
// turned off by default but can be applied accross the database by defining
// sql.compare_in_locale=true in database.properties file
// changes marked separately
// fredt@users 20020130 - patch 491987 by jimbag@users
// support for sql standard char and varchar. size is maintained as
// defined in the DDL and trimming and padding takes place accordingly
// modified by fredt - trimming and padding are turned off by default but
// can be applied accross the database by defining sql.enforce_size=true in
// database.properties file
// fredt@users 20020215 - patch 1.7.0 by fredt - quoted identifiers
// applied to different parts to support the sql standard for
// naming of columns and tables (use of quoted identifiers as names)
// fredt@users 20020328 - patch 1.7.0 by fredt - change REAL to Double
// fredt@users 20020402 - patch 1.7.0 by fredt - type conversions
// frequently used type conversions are done without creating temporary
// Strings to reduce execution time and garbage collection

/**
 *  Implementation of SQL table columns as defined in DDL statements with
 *  static methods to process their values.
 *
 * @version    1.7.0
 */
class Column {

    // non-standard type not in JDBC
    static final int VARCHAR_IGNORECASE = 100;

    // lookup for types
    private static Hashtable hTypes;

    // supported JDBC types - exclude NULL and VARCHAR_IGNORECASE
    static final int numericTypes[] = {
        Types.TINYINT, Types.SMALLINT, Types.INTEGER, Types.BIGINT,
        Types.NUMERIC, Types.DECIMAL, Types.FLOAT, Types.REAL, Types.DOUBLE
    };
    static final int otherTypes[] = {
        Types.BIT, Types.LONGVARBINARY, Types.VARBINARY, Types.BINARY,
        Types.LONGVARCHAR, Types.CHAR, Types.VARCHAR, Types.DATE, Types.TIME,
        Types.TIMESTAMP, Types.OTHER
    };
    static final int[][] typesArray = {
        Column.numericTypes, Column.otherTypes
    };

    // DDL name, size, scale, null, identity and default values
    // most variables are final but not declared so because of a bug in
    // JDK 1.1.8 compiler
    HsqlName        columnName;
    private int     colType;
    private int     colSize;
    private int     colScale;
    private boolean isNullable;
    private boolean isIdentity;
    private boolean isPrimaryKey;
    private String  defaultString;

    // helper values
    private static final BigDecimal BIGDECIMAL_0 = new BigDecimal("0");

    static {
        hTypes = new Hashtable();

        addTypes(Types.INTEGER, "INTEGER", "int", "java.lang.Integer");
        addTypes(Types.INTEGER, "INT", "IDENTITY", null);
        addTypes(Types.DOUBLE, "DOUBLE", "double", "java.lang.Double");
        addType(Types.FLOAT, "FLOAT");    // this is a Double
        addType(Types.REAL, "REAL");      // fredt - this is a Double as of 1.7.0
        addTypes(Types.VARCHAR, "VARCHAR", "java.lang.String", null);
        addTypes(Types.CHAR, "CHAR", "CHARACTER", null);
        addType(Types.LONGVARCHAR, "LONGVARCHAR");

        // for ignorecase data types, the 'original' type name is lost
        addType(VARCHAR_IGNORECASE, "VARCHAR_IGNORECASE");
        addTypes(Types.DATE, "DATE", "java.sql.Date", null);
        addTypes(Types.TIME, "TIME", "java.sql.Time", null);

        // DATETIME is for compatibility with MS SQL 7
        addTypes(Types.TIMESTAMP, "TIMESTAMP", "java.sql.Timestamp",
                 "DATETIME");
        addTypes(Types.DECIMAL, "DECIMAL", "java.math.BigDecimal", null);
        addType(Types.NUMERIC, "NUMERIC");
        addTypes(Types.BIT, "BIT", "java.lang.Boolean", "boolean");
        addTypes(Types.TINYINT, "TINYINT", "java.lang.Byte", "byte");
        addTypes(Types.SMALLINT, "SMALLINT", "java.lang.Short", "short");
        addTypes(Types.BIGINT, "BIGINT", "java.lang.Long", "long");
        addTypes(Types.BINARY, "BINARY", "B[", null);
        addType(Types.VARBINARY, "VARBINARY");
        addType(Types.LONGVARBINARY, "LONGVARBINARY");
        addTypes(Types.OTHER, "OTHER", "java.lang.Object", "OBJECT");

// boucherb@users 20020306 - handle calling void methods
        addTypes(Types.NULL, "NULL", "java.lang.Void", "void");
    }

    private static void addTypes(int type, String name, String n2,
                                 String n3) {

        addType(type, name);
        addType(type, n2);
        addType(type, n3);
    }

    private static void addType(int type, String name) {

        if (name != null) {
            hTypes.put(name, new Integer(type));
        }
    }

// fredt@users 20020130 - patch 491987 by jimbag@users

    /**
     *  Creates a column defined in DDL statement.
     *
     * @param  name
     * @param  nullable
     * @param  type
     * @param  identity
     * @param  namequoted  Description of the Parameter
     * @param  size        Description of the Parameter
     * @param  scale       Description of the Parameter
     * @param  defvalue    Description of the Parameter
     */
    Column(HsqlName name, boolean nullable, int type, int size, int scale,
            boolean identity, boolean primarykey, String defstring) {

        columnName    = name;
        isNullable    = nullable;
        colType       = type;
        colSize       = size;
        colScale      = scale;
        isIdentity    = identity;
        isPrimaryKey  = primarykey;
        defaultString = defstring;
    }

    /**
     *  Is this the identity column in the table.
     *
     * @return boolean
     */
    boolean isIdentity() {
        return isIdentity;
    }

    /**
     *  Is column nullable.
     *
     * @return boolean
     */
    boolean isNullable() {
        return isNullable;
    }

    /**
     *  Set nullable.
     *
     */
    void setNullable(boolean value) {
        isNullable = value;
    }

    /**
     *  Is this single column primary key of the table.
     *
     * @return boolean
     */
    boolean isPrimaryKey() {
        return isPrimaryKey;
    }

    /**
     *  Set primary key.
     *
     */
    void setPrimaryKey(boolean value) {
        isPrimaryKey = value;
    }

    /**
     *  The default value for the column.
     *
     * @return default value string as defined in DDL
     */
    String getDefaultString() {
        return defaultString;
    }

    /**
     *  Type of the column.
     *
     * @return java.sql.Types int value for the column
     */
    int getType() {
        return colType;
    }

    /**
     *  Size of the column in DDL (0 if not defined).
     *
     * @return DDL size of column
     */
    int getSize() {
        return colSize;
    }

    /**
     *  Scale of the column in DDL (0 if not defined).
     *
     * @return DDL scale of column
     */
    int getScale() {
        return colScale;
    }

    /**
     *
     * @param  SQL type string
     * @return java.sql.Types int value
     * @throws  SQLException
     */
    static int getTypeNr(String type) throws SQLException {

        Integer i = (Integer) hTypes.get(type);

        Trace.check(i != null, Trace.WRONG_DATA_TYPE, type);

        return i.intValue();
    }

    /**
     *
     * @param  type
     * @return SQL type string for a java.sql.Types int value
     * @throws  SQLException
     */
    static String getTypeString(int type) throws SQLException {

        switch (type) {

            case Types.NULL :
                return "NULL";

            case Types.INTEGER :
                return "INTEGER";

            case Types.DOUBLE :
                return "DOUBLE";

            case VARCHAR_IGNORECASE :
                return "VARCHAR_IGNORECASE";

            case Types.VARCHAR :
                return "VARCHAR";

            case Types.CHAR :
                return "CHAR";

            case Types.LONGVARCHAR :
                return "LONGVARCHAR";

            case Types.DATE :
                return "DATE";

            case Types.TIME :
                return "TIME";

            case Types.DECIMAL :
                return "DECIMAL";

            case Types.BIT :
                return "BIT";

            case Types.TINYINT :
                return "TINYINT";

            case Types.SMALLINT :
                return "SMALLINT";

            case Types.BIGINT :
                return "BIGINT";

            case Types.REAL :
                return "REAL";

            case Types.FLOAT :
                return "FLOAT";

            case Types.NUMERIC :
                return "NUMERIC";

            case Types.TIMESTAMP :
                return "TIMESTAMP";

            case Types.BINARY :
                return "BINARY";

            case Types.VARBINARY :
                return "VARBINARY";

            case Types.LONGVARBINARY :
                return "LONGVARBINARY";

            case Types.OTHER :
                return "OBJECT";

            default :
                throw Trace.error(Trace.WRONG_DATA_TYPE, type);
        }
    }

    /**
     *  Add two object of a given type
     *
     * @param  a
     * @param  b
     * @param  type
     * @return result
     * @throws  SQLException
     */
    static Object add(Object a, Object b, int type) throws SQLException {

        if (a == null || b == null) {
            return null;
        }

        switch (type) {

            case Types.NULL :
                return null;

            case Types.REAL :
            case Types.FLOAT :
            case Types.DOUBLE :
                double ad = ((Number) a).doubleValue();
                double bd = ((Number) b).doubleValue();

                return new Double(ad + bd);

            case Types.VARCHAR :
            case Types.CHAR :
            case Types.LONGVARCHAR :
            case VARCHAR_IGNORECASE :
                return (String) a + (String) b;

            case Types.NUMERIC :
            case Types.DECIMAL :
                BigDecimal abd = (BigDecimal) a;
                BigDecimal bbd = (BigDecimal) b;

                return abd.add(bbd);

            case Types.TINYINT :
            case Types.SMALLINT :
            case Types.INTEGER :
                int ai = ((Number) a).intValue();
                int bi = ((Number) b).intValue();

                return new Integer(ai + bi);

            case Types.BIGINT :
                long longa = ((Number) a).longValue();
                long longb = ((Number) b).longValue();

                return new Long(longa + longb);

            default :
                throw Trace.error(Trace.FUNCTION_NOT_SUPPORTED, type);
        }
    }

    /**
     *  Concat two objects by turning them into strings first.
     *
     * @param  a
     * @param  b
     * @return result
     * @throws  SQLException
     */
    static Object concat(Object a, Object b) throws SQLException {

        if (a == null) {
            return b;
        } else if (b == null) {
            return a;
        }

        return convertObject(a) + convertObject(b);
    }

    /**
     *  Negate a numeric object.
     *
     * @param  a
     * @param  type
     * @return result
     * @throws  SQLException
     */
    static Object negate(Object a, int type) throws SQLException {

        if (a == null) {
            return null;
        }

        switch (type) {

            case Types.NULL :
                return null;

            case Types.REAL :
            case Types.FLOAT :
            case Types.DOUBLE :
                return new Double(-((Number) a).doubleValue());

            case Types.NUMERIC :
            case Types.DECIMAL :
                return ((BigDecimal) a).negate();

            case Types.TINYINT :
            case Types.SMALLINT :
            case Types.INTEGER :
                return new Integer(-((Number) a).intValue());

            case Types.BIGINT :
                return new Long(-((Number) a).longValue());

            default :
                throw Trace.error(Trace.FUNCTION_NOT_SUPPORTED, type);
        }
    }

    /**
     *  Multiply two numeric objects.
     *
     * @param  a
     * @param  b
     * @param  type
     * @return result
     * @throws  SQLException
     */
    static Object multiply(Object a, Object b, int type) throws SQLException {

        if (a == null || b == null) {
            return null;
        }

        switch (type) {

            case Types.NULL :
                return null;

            case Types.REAL :
            case Types.FLOAT :
            case Types.DOUBLE :
                double ad = ((Number) a).doubleValue();
                double bd = ((Number) b).doubleValue();

                return new Double(ad * bd);

            case Types.NUMERIC :
            case Types.DECIMAL :
                BigDecimal abd = (BigDecimal) a;
                BigDecimal bbd = (BigDecimal) b;

                return abd.multiply(bbd);

            case Types.TINYINT :
            case Types.SMALLINT :
            case Types.INTEGER :
                int ai = ((Number) a).intValue();
                int bi = ((Number) b).intValue();

                return new Integer(ai * bi);

            case Types.BIGINT :
                long longa = ((Number) a).longValue();
                long longb = ((Number) b).longValue();

                return new Long(longa * longb);

            default :
                throw Trace.error(Trace.FUNCTION_NOT_SUPPORTED, type);
        }
    }

    /**
     *  Divide numeric object a by object b.
     *
     * @param  a
     * @param  b
     * @param  type
     * @return result
     * @throws  SQLException
     */
    static Object divide(Object a, Object b, int type) throws SQLException {

        if (a == null || b == null) {
            return null;
        }

        switch (type) {

            case Types.NULL :
                return null;

            case Types.REAL :
            case Types.FLOAT :
            case Types.DOUBLE :
                double ad = ((Number) a).doubleValue();
                double bd = ((Number) b).doubleValue();

                return (bd == 0) ? null
                                 : new Double(ad / bd);

            case Types.NUMERIC :
            case Types.DECIMAL :
                BigDecimal abd   = (BigDecimal) a;
                BigDecimal bbd   = (BigDecimal) b;
                int        scale = abd.scale() > bbd.scale() ? abd.scale()
                                                             : bbd.scale();

                return (bbd.signum() == 0) ? null
                                           : abd.divide(bbd, scale,
                                           BigDecimal.ROUND_HALF_DOWN);

            case Types.TINYINT :
            case Types.SMALLINT :
            case Types.INTEGER :
                int ai = ((Number) a).intValue();
                int bi = ((Number) b).intValue();

                Trace.check(bi != 0, Trace.DIVISION_BY_ZERO);

                return new Integer(ai / bi);

            case Types.BIGINT :
                long longa = ((Number) a).longValue();
                long longb = ((Number) b).longValue();

                return (longb == 0) ? null
                                    : new Long(longa / longb);

            default :
                throw Trace.error(Trace.FUNCTION_NOT_SUPPORTED, type);
        }
    }

    /**
     *  Subtract numeric object b from object a.
     *
     * @param  a
     * @param  b
     * @param  type
     * @return result
     * @throws  SQLException
     */
    static Object subtract(Object a, Object b, int type) throws SQLException {

        if (a == null || b == null) {
            return null;
        }

        switch (type) {

            case Types.NULL :
                return null;

            case Types.REAL :
            case Types.FLOAT :
            case Types.DOUBLE :
                double ad = ((Number) a).doubleValue();
                double bd = ((Number) b).doubleValue();

                return new Double(ad - bd);

            case Types.NUMERIC :
            case Types.DECIMAL :
                BigDecimal abd = (BigDecimal) a;
                BigDecimal bbd = (BigDecimal) b;

                return abd.subtract(bbd);

            case Types.TINYINT :
            case Types.SMALLINT :
            case Types.INTEGER :
                int ai = ((Number) a).intValue();
                int bi = ((Number) b).intValue();

                return new Integer(ai - bi);

            case Types.BIGINT :
                long longa = ((Number) a).longValue();
                long longb = ((Number) b).longValue();

                return new Long(longa - longb);

            default :
                throw Trace.error(Trace.FUNCTION_NOT_SUPPORTED, type);
        }
    }

    /**
     *  Add two numeric objects.
     *
     * @param  a
     * @param  b
     * @param  type
     * @return result
     * @throws  SQLException
     */
    static Object sum(Object a, Object b, int type) throws SQLException {

        if (a == null) {
            return b;
        }

        if (b == null) {
            return a;
        }

        switch (type) {

            case Types.NULL :
                return null;

            case Types.REAL :
            case Types.FLOAT :
            case Types.DOUBLE :
                return new Double(((Number) a).doubleValue()
                                  + ((Number) b).doubleValue());

            case Types.NUMERIC :
            case Types.DECIMAL :
                return ((BigDecimal) a).add((BigDecimal) b);

            case Types.TINYINT :
            case Types.SMALLINT :
            case Types.INTEGER :
                return new Integer(((Number) a).intValue()
                                   + ((Number) b).intValue());

            case Types.BIGINT :
                return new Long(((Number) a).longValue()
                                + ((Number) b).longValue());

            default :
                throw Trace.error(Trace.SUM_OF_NON_NUMERIC);
        }
    }

    /**
     *  Divide numeric object a by int count. Adding all of these values in
     *  a column of the result of a SELECT statement gives the average for
     *  that column.
     *
     * @param  a
     * @param  type
     * @param  count
     * @return result
     * @throws  SQLException
     */
    static Object avg(Object a, int type, int count) throws SQLException {

        if (a == null || count == 0) {
            return null;
        }

        switch (type) {

            case Types.NULL :
                return null;

            case Types.REAL :
            case Types.FLOAT :
            case Types.DOUBLE :
                return new Double(((Double) a).doubleValue() / count);

            case Types.NUMERIC :
            case Types.DECIMAL :
                return ((BigDecimal) a).divide(new BigDecimal(count),
                                               BigDecimal.ROUND_HALF_DOWN);

            case Types.TINYINT :
            case Types.SMALLINT :
            case Types.INTEGER :
                return new Integer(((Number) a).intValue() / count);

            case Types.BIGINT :
                return new Long(((Long) a).longValue() / count);

            default :
                throw Trace.error(Trace.SUM_OF_NON_NUMERIC);
        }
    }

    /**
     *  Return the smaller of two objects.
     *
     * @param  a
     * @param  b
     * @param  type
     * @return result
     * @throws  SQLException
     */
    static Object min(Object a, Object b, int type) throws SQLException {

        if (a == null) {
            return b;
        }

        if (b == null) {
            return a;
        }

        if (compare(a, b, type) < 0) {
            return a;
        }

        return b;
    }

    /**
     *  Return the larger of two objects.
     *
     * @param  a
     * @param  b
     * @param  type
     * @return result
     * @throws  SQLException
     */
    static Object max(Object a, Object b, int type) throws SQLException {

        if (a == null) {
            return b;
        }

        if (b == null) {
            return a;
        }

        if (compare(a, b, type) > 0) {
            return a;
        }

        return b;
    }

// fredt@users 20020130 - patch 505356 by daniel_fiser@users
// modified for performance and made optional
    private static Collator i18nCollator          = Collator.getInstance();
    private static boolean  sql_compare_in_locale = false;

    static void setCompareInLocal(boolean value) {
        sql_compare_in_locale = value;
    }

    /**
     *  Compare a with b and return int value as result.
     *
     * @param  a
     * @param  b
     * @param  type
     * @return result
     * @throws  SQLException
     */
    static int compare(Object a, Object b, int type) throws SQLException {

        int i = 0;

        if (a == b) {
            return 0;
        }

        // null handling: null==null and smaller any value
        // todo: implement standard SQL null handling
        // it is also used for grouping ('null' is one group)
        if (a == null) {
            if (b == null) {
                return 0;
            }

            return -1;
        }

        if (b == null) {
            return 1;
        }

        switch (type) {

            case Types.NULL :
                return 0;

            case Types.VARCHAR :
            case Types.LONGVARCHAR :
                if (sql_compare_in_locale) {
                    i = i18nCollator.compare((String) a, (String) b);
                } else {
                    i = ((String) a).compareTo((String) b);
                }
                break;

// fredt@users 20020130 - patch 418022 by deforest@users
// use of rtrim() to mimic SQL92 behaviour
            case Types.CHAR :
                if (sql_compare_in_locale) {
                    i = i18nCollator.compare(Library.rtrim((String) a),
                                             Library.rtrim((String) b));
                } else {
                    i = (Library.rtrim((String) a)).compareTo(
                        Library.rtrim((String) b));
                }
                break;

            case VARCHAR_IGNORECASE :
                if (sql_compare_in_locale) {
                    i = i18nCollator.compare(((String) a).toUpperCase(),
                                             ((String) b).toUpperCase());
                } else {
                    i = ((String) a).toUpperCase().compareTo(
                        ((String) b).toUpperCase());
                }
                break;

            case Types.TINYINT :
            case Types.SMALLINT :
            case Types.INTEGER :
                int ai = ((Number) a).intValue();
                int bi = ((Number) b).intValue();

                return (ai > bi) ? 1
                                 : (bi > ai ? -1
                                            : 0);

            case Types.BIGINT :
                long longa = ((Number) a).longValue();
                long longb = ((Number) b).longValue();

                return (longa > longb) ? 1
                                       : (longb > longa ? -1
                                                        : 0);

            case Types.REAL :
            case Types.FLOAT :
            case Types.DOUBLE :
                double ad = ((Number) a).doubleValue();
                double bd = ((Number) b).doubleValue();

                return (ad > bd) ? 1
                                 : (bd > ad ? -1
                                            : 0);

            case Types.NUMERIC :
            case Types.DECIMAL :
                i = ((BigDecimal) a).compareTo((BigDecimal) b);
                break;

            case Types.DATE :
                if (((java.sql.Date) a).after((java.sql.Date) b)) {
                    return 1;
                } else if (((java.sql.Date) a).before((java.sql.Date) b)) {
                    return -1;
                } else {
                    return 0;
                }
            case Types.TIME :
                if (((Time) a).after((Time) b)) {
                    return 1;
                } else if (((Time) a).before((Time) b)) {
                    return -1;
                } else {
                    return 0;
                }
            case Types.TIMESTAMP :
                if (((Timestamp) a).after((Timestamp) b)) {
                    return 1;
                } else if (((Timestamp) a).before((Timestamp) b)) {
                    return -1;
                } else {
                    return 0;
                }
            case Types.BIT :
                boolean boola = ((Boolean) a).booleanValue();
                boolean boolb = ((Boolean) b).booleanValue();

                return (boola == boolb) ? 0
                                        : (boolb ? -1
                                                 : 1);

            case Types.BINARY :
            case Types.VARBINARY :
            case Types.LONGVARBINARY :
                i = ByteArray.compareTo((byte[]) a, (byte[]) b);
                break;

            case Types.OTHER :
                return 0;

            default :
                throw Trace.error(Trace.FUNCTION_NOT_SUPPORTED, type);
        }

        return (i > 0) ? 1
                       : (i < 0 ? -1
                                : 0);
    }

    /**
     *  Return a java string representation of a java object.
     *
     * @param  o
     * @return result (null value for null object)
     */
    private static String convertObject(Object o) {

        if (o == null) {
            return null;
        }

        return o.toString();
    }

    /**
     *  Convert an object into a Java object that represents its SQL type.<p>
     *  All type conversion operations start with
     *  this method. If a direct conversion doesn't take place, the object
     *  is converted into a string first and an attempt is made to convert
     *  the string into the target type.<br>
     *
     *  One objective of this mehod is to ensure the Object can be converted
     *  to the given SQL type. For example, a number that has decimal points
     *  cannot be converted into an integral type, or a very large BIGINT
     *  value cannot be narrowed down to an INTEGER or SMALLINT.<br>
     *
     *  Integral types may be represented by either Integer or Long. This
     *  works because in the rest of the methods, the java.lang.Number
     *  interface is used to retrieve the values from the object.
     *
     * @param  o
     * @param  type
     * @return result
     * @throws  SQLException
     */
    static Object convertObject(Object o, int type) throws SQLException {

        try {
            if (o == null) {
                return null;
            }

            switch (type) {

                case Types.NULL :
                    return null;

                case Types.TINYINT :
                    if (o instanceof java.lang.String) {
                        o = new Integer((String) o);
                    }

                    if (o instanceof java.lang.Integer
                            || o instanceof java.lang.Long) {
                        int temp = ((Number) o).intValue();

                        if (Byte.MAX_VALUE < temp || temp < Byte.MIN_VALUE) {
                            throw new java.lang.NumberFormatException();
                        }

                        // fredt@users - no narrowing for Long values
                        return o;
                    }

                    // fredt@users - direct conversion for JDBC setObject()
                    if (o instanceof java.lang.Byte) {
                        return new Integer(((Number) o).intValue());
                    }
                    break;

                case Types.SMALLINT :
                    if (o instanceof java.lang.String) {
                        o = new Integer((String) o);
                    }

                    if (o instanceof java.lang.Integer
                            || o instanceof java.lang.Long) {
                        int temp = ((Number) o).intValue();

                        if (Short.MAX_VALUE < temp
                                || temp < Short.MIN_VALUE) {
                            throw new java.lang.NumberFormatException();
                        }

                        // fredt@users - no narrowing for Long values
                        return o;
                    }

                    // fredt@users - direct conversion for JDBC setObject()
                    if (o instanceof java.lang.Byte
                            || o instanceof java.lang.Short) {
                        return new Integer(((Number) o).intValue());
                    }
                    break;

                case Types.INTEGER :
                    if (o instanceof java.lang.String) {
                        return new Integer((String) o);
                    }

                    if (o instanceof java.lang.Integer) {
                        return o;
                    }

                    if (o instanceof java.lang.Long) {
                        long temp = ((Number) o).longValue();

                        if (Integer.MAX_VALUE < temp
                                || temp < Integer.MIN_VALUE) {
                            throw new java.lang.NumberFormatException();
                        }

                        // fredt@users - narroing is required for library function calls
                        return new Integer(((Number) o).intValue());
                    }
                    break;

                case Types.BIGINT :
                    if (o instanceof java.lang.Long) {
                        return o;
                    }

                    if (o instanceof java.lang.String) {
                        return new Long((String) o);
                    }

                    if (o instanceof java.lang.Integer) {
                        return new Long(((Integer) o).longValue());
                    }
                    break;

                case Types.REAL :
                case Types.FLOAT :
                case Types.DOUBLE :
                    if (o instanceof java.lang.Double) {
                        return o;
                    }

                    if (o instanceof java.lang.String) {
                        return new Double((String) o);
                    }

                    if (o instanceof java.lang.Number) {
                        return new Double(((Number) o).doubleValue());
                    }
                    break;

                case Types.NUMERIC :
                case Types.DECIMAL :
                    if (o instanceof java.math.BigDecimal) {
                        return o;
                    }
                    break;

                case Types.BIT :
                    if (o instanceof java.lang.Boolean) {
                        return o;
                    }

                    if (o instanceof java.lang.String) {
                        return new Boolean((String) o);
                    }

                    if (o instanceof Integer || o instanceof Long) {
                        boolean bit = ((Number) o).longValue() == 0L ? false
                                                                     : true;

                        return new Boolean(bit);
                    }

                    if (o instanceof java.lang.Double) {
                        boolean bit = ((Double) o).doubleValue() == 0.0
                                      ? false
                                      : true;

                        return new Boolean(bit);
                    }

                    if (o instanceof java.math.BigDecimal) {
                        boolean bit = ((BigDecimal) o).compareTo(BIGDECIMAL_0)
                                      == 0 ? false
                                           : true;

                        return new Boolean(bit);
                    }
                    break;

                case VARCHAR_IGNORECASE :
                case Types.VARCHAR :
                case Types.CHAR :
                case Types.LONGVARCHAR :
                    if (o instanceof java.lang.String) {
                        return o;
                    }

                    if (o instanceof byte[]) {
                        return ByteArray.toString((byte[]) o);
                    }
                    break;

                case Types.TIME :
                    if (o instanceof java.sql.Timestamp) {
                        return new Time(((Timestamp) o).getTime());
                    }

                    if (o instanceof java.sql.Date) {
                        return new Time(0);
                    }
                    break;

                case Types.DATE :
                    if (o instanceof java.sql.Timestamp) {
                        return new java.sql.Date(((Timestamp) o).getTime());
                    }
                    break;

                case Types.BINARY :
                case Types.VARBINARY :
                case Types.LONGVARBINARY :
                    if (o instanceof byte[]) {
                        return o;
                    }
                    break;

// fredt@users 20020328 -  patch 482109 by fredt - OBJECT handling
// currently String objects cannot be stored directly in OTHER columns
// all strings are treated as a hex representation of a serialized Object
// a new escape pattern needs to be established to differentiate between
// SQL strings that are normal strings and those that represent a hex version
// of the BINARY or OTHER data
                case Types.OTHER :
                    if (o instanceof String == false) {
                        return o;
                    }
                default :
            }

            return convertString(o.toString(), type);
        } catch (SQLException e) {
            throw e;
        } catch (Exception e) {
            throw Trace.error(Trace.WRONG_DATA_TYPE, e.getMessage());
        }
    }

    /**
     *  Return a java object based on a SQL string. This is called from
     *  convertObject(Object o, int type).
     *
     * @param  s
     * @param  type
     * @return
     * @throws  SQLException
     */
    private static Object convertString(String s,
                                        int type) throws SQLException {

        switch (type) {

            case Types.TINYINT :
            case Types.SMALLINT :

                // fredt - do maximumm / minimum checks on each type
                return convertObject(s, type);

            case Types.INTEGER :
                return new Integer(s);

            case Types.BIGINT :
                return new Long(s);

            case Types.REAL :
            case Types.FLOAT :
            case Types.DOUBLE :
                return new Double(s);

            case VARCHAR_IGNORECASE :
            case Types.VARCHAR :
            case Types.CHAR :
            case Types.LONGVARCHAR :
                return s;

            case Types.DATE :
                return HsqlDateTime.dateValue(s);

            case Types.TIME :
                return HsqlDateTime.timeValue(s);

            case Types.TIMESTAMP :
                return HsqlDateTime.timestampValue(s);

            case Types.NUMERIC :
            case Types.DECIMAL :
                return new BigDecimal(s.trim());

            case Types.BIT :
                return new Boolean(s);

            case Types.BINARY :
            case Types.VARBINARY :
            case Types.LONGVARBINARY :
                return ByteArray.hexToByteArray(s);

            case Types.OTHER :
                return ByteArray.deserialize(ByteArray.hexToByteArray(s));

            default :
                throw Trace.error(Trace.FUNCTION_NOT_SUPPORTED, type);
        }
    }

    /**
     *  Return an SQL representation of an object. Strings will be quoted
     *  with single quotes, other objects will represented as in a SQL
     *  statement.
     *
     * @param  o
     * @param  type
     * @return result
     * @throws  SQLException
     */
    static String createSQLString(Object o, int type) throws SQLException {

        if (o == null) {
            return "NULL";
        }

        switch (type) {

            case Types.NULL :
                return "NULL";

            case Types.DATE :
            case Types.TIME :
            case Types.TIMESTAMP :
                return StringConverter.toQuotedString(o.toString(), '\'',
                                                      false);

            case Types.BINARY :
            case Types.VARBINARY :
            case Types.LONGVARBINARY :
                return StringConverter.toQuotedString(
                    StringConverter.byteToHex((byte[]) o), '\'', false);

            case Types.OTHER :
                return StringConverter.toQuotedString(
                    ByteArray.serializeToString(o), '\'', false);

            case VARCHAR_IGNORECASE :
            case Types.VARCHAR :
            case Types.CHAR :
            case Types.LONGVARCHAR :
                return createSQLString((String) o);

            default :
                return o.toString();
        }
    }

    /**
     *  Turns a java string into a quoted SQL string
     *
     * @param  java string
     * @return quoted SQL string
     */
    static String createSQLString(String s) {
        return StringConverter.toQuotedString(s, '\'', true);
    }

// fredt@users 20020408 - patch 442993 by fredt - arithmetic expressions

    /**
     *  Arithmetic expressions terms are promoted to a type that can
     *  represent the resulting values and avoid incorrect results.<p>
     *  When the result or the expression is converted to the
     *  type of the target column for storage, an exception is thrown if the
     *  resulting value cannot be stored in the column<p>
     *  Returns a SQL type "wide" enough to represent the result of the
     *  expression.<br>
     *  A type is "wider" than the other if it can represent all its
     *  numeric values.<BR>
     *  Types narrower than INTEGER (int) are promoted to
     *  INTEGER. The order is as follows<p>
     *
     *  INTEGER, BIGINT, DOUBLE, DECIMAL<p>
     *
     *  TINYINT and SMALLINT in any combination return INTEGER<br>
     *  INTEGER and INTEGER return BIGINT<br>
     *  BIGINT and INTEGER return NUMERIC/DECIMAL<br>
     *  BIGINT and BIGINT return NUMERIC/DECIMAL<br>
     *  DOUBLE and INTEGER return DOUBLE<br>
     *  DOUBLE and BIGINT return DOUBLE<br>
     *  NUMERIC/DECIMAL and any type returns NUMERIC/DECIMAL<br>
     *
     * @author fredt@users
     * @param  type1  java.sql.Types value for the first numeric type
     * @param  type2  java.sql.Types value for the second numeric type
     * @return        either type1 or type2 on the basis of the above order
     */
    static int getCombinedNumberType(int type1, int type2, int expType) {

        int typeWidth1 = getNumTypeWidth(type1);
        int typeWidth2 = getNumTypeWidth(type2);

        if (typeWidth1 == 16 || typeWidth2 == 16) {
            return Types.DOUBLE;
        }

        if (expType != Expression.DIVIDE) {
            if (typeWidth1 + typeWidth2 <= 4) {
                return Types.INTEGER;
            }

            if (typeWidth1 + typeWidth2 <= 8) {
                return Types.BIGINT;
            }

            if (typeWidth1 + typeWidth2 <= 16) {
                return Types.NUMERIC;
            }
        }

        return (typeWidth1 > typeWidth2) ? type1
                                         : type2;
    }

    /**
     * @param  java.sql.Types int for a numeric type
     * @return relative width
     */
    private static int getNumTypeWidth(int type) {

        switch (type) {

            case Types.TINYINT :
                return 1;

            case Types.SMALLINT :
                return 2;

            case Types.INTEGER :
                return 4;

            case Types.BIGINT :
                return 8;

            case Types.REAL :
            case Types.FLOAT :
            case Types.DOUBLE :
                return 16;

            case Types.NUMERIC :
            case Types.DECIMAL :
                return 32;

            default :
                return 32;
        }
    }
}
