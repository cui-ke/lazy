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
import java.sql.DatabaseMetaData;
import java.util.Hashtable;
import java.util.Vector;

// fredt@users 20020130 - patch 491987 by jimbag@users
// applied to different parts
// fredt@users 20020215 - patch 1.7.0 by fredt - quoted identifiers
// applied to different parts to support the sql standard for
// naming of columns and tables (use of quoted identifiers as names)
// written for universal support of quoted names including the quote character
// and speed improvements to avoid repetitive string comparisons
// thanks to suggestions by boucherb@users
// instigated by patch 489864 by jytou@users
// fredt@users 20020218 - patch 1.7.0 by fredt - DEFAULT keyword
// support for default values for table columns
// fredt@users 20020225 - patch 489777 by fredt
// restructuring for error trapping
// fredt@users 20020225 - patch 1.7.0 - named constraints
// fredt@users 20020225 - patch 1.7.0 - multi-column primary keys
// fredt@users 20020523 - patch 1.7.0 - JDBC reporting of forgin keys
// fredt@users 20020526 - patch 1.7.0 - JDBC reporting of best row identifier

/**
 * Provides information about the database.
 *
 *
 * @version 1.7.0
 */
class DatabaseInformation {

    private Database             dDatabase;
    private UserManager          aAccess;
    private Vector               tTable;
    private static final Integer INTEGER_0 = new Integer(0);

    /**
     * Constructor declaration
     *
     *
     * @param db
     * @param tables
     * @param access
     */
    DatabaseInformation(Database db, Vector tables, UserManager access) {

        dDatabase = db;
        tTable    = tables;
        aAccess   = access;
    }

    // some drivers use the following titles:
    // static final String META_SCHEM="OWNER";
    // static final String META_CAT="QUALIFIER";
    // static final String META_COLUMN_SIZE="PRECISION";
    // static final String META_BUFFER_LENGTH="LENGTH";
    // static final String META_DECIMAL_DIGITS="SCALE";
    // static final String META_NUM_PREC_RADIX="RADIX";
    // static final String META_FIXED_PREC_SCALE="MONEY";
    // static final String META_ORDINAL_POSITION="SEQ_IN_INDEX";
    // static final String META_ASC_OR_DESC="COLLATION";
    static final String      META_SCHEM            = "SCHEM";
    static final String      META_CAT              = "CAT";
    static final String      META_COLUMN_SIZE      = "COLUMN_SIZE";
    static final String      META_BUFFER_LENGTH    = "BUFFER_LENGTH";
    static final String      META_DECIMAL_DIGITS   = "DECIMAL_DIGITS";
    static final String      META_NUM_PREC_RADIX   = "NUM_PREC_RADIX";
    static final String      META_FIXED_PREC_SCALE = "FIXED_PREC_SCALE";
    static final String      META_ORDINAL_POSITION = "ORDINAL_POSITION";
    static final String      META_ASC_OR_DESC      = "ASC_OR_DESC";
    private static Hashtable sysTableNames;
    private static final int SYSTEM_PROCEDURES        = 1;
    private static final int SYSTEM_PROCEDURECOLUMNS  = 2;
    private static final int SYSTEM_TABLES            = 3;
    private static final int SYSTEM_SCHEMAS           = 4;
    private static final int SYSTEM_CATALOGS          = 5;
    private static final int SYSTEM_TABLETYPES        = 6;
    private static final int SYSTEM_COLUMNS           = 7;
    private static final int SYSTEM_COLUMNPRIVILEGES  = 8;
    private static final int SYSTEM_TABLEPRIVILEGES   = 9;
    private static final int SYSTEM_BESTROWIDENTIFIER = 10;
    private static final int SYSTEM_VERSIONCOLUMNS    = 11;
    private static final int SYSTEM_PRIMARYKEYS       = 12;
    private static final int SYSTEM_IMPORTEDKEYS      = 13;
    private static final int SYSTEM_EXPORTEDKEYS      = 14;
    private static final int SYSTEM_CROSSREFERENCE    = 15;
    private static final int SYSTEM_TYPEINFO          = 16;
    private static final int SYSTEM_INDEXINFO         = 17;
    private static final int SYSTEM_UDTS              = 18;
    private static final int SYSTEM_CONNECTIONINFO    = 19;
    private static final int SYSTEM_USERS             = 20;

    // supported table types
    private static final String[] tableTypes = new String[] {
        "TABLE", "VIEW", "GLOBAL TEMPORARY"
    };

    static {
        sysTableNames = new Hashtable();

        String sysNames[] = {
            "SYSTEM_PROCEDURES", "SYSTEM_PROCEDURECOLUMNS", "SYSTEM_TABLES",
            "SYSTEM_SCHEMAS", "SYSTEM_CATALOGS", "SYSTEM_TABLETYPES",
            "SYSTEM_COLUMNS", "SYSTEM_COLUMNPRIVILEGES",
            "SYSTEM_TABLEPRIVILEGES", "SYSTEM_BESTROWIDENTIFIER",
            "SYSTEM_VERSIONCOLUMNS", "SYSTEM_PRIMARYKEYS",
            "SYSTEM_IMPORTEDKEYS", "SYSTEM_EXPORTEDKEYS",
            "SYSTEM_CROSSREFERENCE", "SYSTEM_TYPEINFO", "SYSTEM_INDEXINFO",
            "SYSTEM_UDTS", "SYSTEM_CONNECTIONINFO", "SYSTEM_USERS"
        };

        for (int i = 0; i < sysNames.length; i++) {
            sysTableNames.put(sysNames[i], new Integer(i + 1));
        }
    }

    static boolean isSystemTable(String name) {
        return sysTableNames.get(name) == null ? false
                                               : true;
    }

    /**
     * Method declaration
     *
     *
     * @param name
     * @param session
     *
     * @return
     *
     * @throws SQLException
     */
    Table getSystemTable(String tablename,
                         Session session) throws SQLException {

        HsqlName name    = new HsqlName(tablename, false);
        Integer  tableId = (Integer) sysTableNames.get(tablename);

        if (tableId == null) {
            return null;
        }

        int   tableIdValue = tableId.intValue();
        Table t            = createTable(name);

        switch (tableIdValue) {

            case SYSTEM_PROCEDURES : {
                t.addColumn("PROCEDURE_" + META_CAT, Types.VARCHAR);
                t.addColumn("PROCEDURE_" + META_SCHEM, Types.VARCHAR);
                t.addColumn("PROCEDURE_NAME", Types.VARCHAR);
                t.addColumn("NUM_INPUT_PARAMS", Types.INTEGER);
                t.addColumn("NUM_OUTPUT_PARAMS", Types.INTEGER);
                t.addColumn("NUM_RESULT_SETS", Types.INTEGER);
                t.addColumn("REMARKS", Types.VARCHAR);
                t.addColumn("PROCEDURE_TYPE", Types.SMALLINT);
                t.createPrimaryKey();

                return t;
            }
            case SYSTEM_PROCEDURECOLUMNS : {
                t.addColumn("PROCEDURE_" + META_CAT, Types.VARCHAR);
                t.addColumn("PROCEDURE_" + META_SCHEM, Types.VARCHAR);
                t.addColumn("PROCEDURE_NAME", Types.VARCHAR);
                t.addColumn("COLUMN_NAME", Types.VARCHAR);
                t.addColumn("COLUMN_TYPE", Types.SMALLINT);
                t.addColumn("DATA_TYPE", Types.SMALLINT);
                t.addColumn("TYPE_NAME", Types.VARCHAR);
                t.addColumn("PRECISION", Types.INTEGER);
                t.addColumn("LENGTH", Types.INTEGER);
                t.addColumn("SCALE", Types.SMALLINT);
                t.addColumn("RADIX", Types.SMALLINT);
                t.addColumn("NULLABLE", Types.SMALLINT);
                t.addColumn("REMARKS", Types.VARCHAR);
                t.createPrimaryKey();

                return t;
            }
            case SYSTEM_TABLES : {
                t.addColumn("TABLE_" + META_CAT, Types.VARCHAR);
                t.addColumn("TABLE_" + META_SCHEM, Types.VARCHAR);
                t.addColumn("TABLE_NAME", Types.VARCHAR);
                t.addColumn("TABLE_TYPE", Types.VARCHAR);
                t.addColumn("REMARKS", Types.VARCHAR);

// boucherb@users 20020415 added for JDBC 3 clients
                t.addColumn("TYPE_" + META_CAT, Types.VARCHAR);
                t.addColumn("TYPE_" + META_SCHEM, Types.VARCHAR);
                t.addColumn("TYPE_NAME", Types.VARCHAR);
                t.addColumn("SELF_REFERENCING_COL_NAME", Types.VARCHAR);
                t.addColumn("REF_GENERATION", Types.VARCHAR);
                t.createPrimaryKey();

                for (int i = 0, tSize = tTable.size(); i < tSize; i++) {
                    Table  table = (Table) tTable.elementAt(i);
                    Object o[]   = t.getNewRow();

                    o[0] = o[1] = "";
                    o[2] = table.getName().name;

                    switch (table.tableType) {

                        case Table.VIEW :
                            o[3] = "VIEW";
                            break;

                        case Table.TEMP_TABLE :
                        case Table.TEMP_TEXT_TABLE :
                            if (dDatabase.findUserTable(
                                    table.getName().name, session) == null) {
                                continue;
                            }

                            o[3] = "GLOBAL TEMPORARY";
                            break;

                        default :
                            o[3] = "TABLE";
                    }

                    // sqlbob@users Set remarks to readonly status.
                    if (table.isDataReadOnly()) {
                        o[4] = "ReadOnlyData=true";
                    }

                    // sqlbob@users Add data source to remarks
                    String dataSource = table.getDataSource();

                    if (dataSource != null) {
                        if (o[4] == null) {
                            o[4] = "";
                        } else {
                            o[4] = o[4] + "; ";
                        }

                        o[4] = o[4] + "DataSource=\"" + dataSource + "\"";

                        if (table.isDescDataSource()) {
                            o[4] = o[4] + " DESC";
                        }
                    }

                    t.insert(o, null);
                }

                return t;
            }
            case SYSTEM_SCHEMAS : {
                t.addColumn("TABLE_" + META_SCHEM, Types.VARCHAR);

// boucherb@users 20020415 added for JDBC 3 clients
                t.addColumn("TABLE_CATALOG", Types.VARCHAR);
                t.createPrimaryKey();

                return t;
            }
            case SYSTEM_CATALOGS : {
                t.addColumn("TABLE_" + META_CAT, Types.VARCHAR);
                t.createPrimaryKey();

                return t;
            }
            case SYSTEM_TABLETYPES : {
                t.addColumn("TABLE_TYPE", Types.VARCHAR);
                t.createPrimaryKey();

                for (int i = 0; i < tableTypes.length; i++) {
                    Object o[] = t.getNewRow();

                    o[0] = tableTypes[i];

                    t.insert(o, null);
                }

                return t;
            }
            case SYSTEM_COLUMNS : {
                t.addColumn("TABLE_" + META_CAT, Types.VARCHAR);
                t.addColumn("TABLE_" + META_SCHEM, Types.VARCHAR);
                t.addColumn("TABLE_NAME", Types.VARCHAR);
                t.addColumn("COLUMN_NAME", Types.VARCHAR);
                t.addColumn("DATA_TYPE", Types.SMALLINT);
                t.addColumn("TYPE_NAME", Types.VARCHAR);
                t.addColumn(META_COLUMN_SIZE, Types.INTEGER);
                t.addColumn(META_BUFFER_LENGTH, Types.INTEGER);
                t.addColumn(META_DECIMAL_DIGITS, Types.INTEGER);
                t.addColumn(META_NUM_PREC_RADIX, Types.INTEGER);
                t.addColumn("NULLABLE", Types.INTEGER);
                t.addColumn("REMARKS", Types.VARCHAR);
                t.addColumn("COLUMN_DEF", Types.VARCHAR);

// fredt@users 20020407 - patch 1.7.0 by sqlbob@users - fixed incorrect type
                t.addColumn("SQL_DATA_TYPE", Types.INTEGER);
                t.addColumn("SQL_DATETIME_SUB", Types.INTEGER);
                t.addColumn("CHAR_OCTET_LENGTH", Types.INTEGER);

// fredt@users 20020407 - patch 1.7.0 - fixed incorrect type
                t.addColumn("ORDINAL_POSITION", Types.INTEGER);
                t.addColumn("IS_NULLABLE", Types.VARCHAR);

// boucherb@users 20020415 added for JDBC 3 clients
// fredt - spelling of SCOPE_CATLOG is according to JDBC specs
                t.addColumn("SCOPE_CATLOG", Types.VARCHAR);
                t.addColumn("SCOPE_SCHEMA", Types.VARCHAR);
                t.addColumn("SCOPE_TABLE", Types.VARCHAR);
                t.addColumn("SOURCE_DATA_TYPE", Types.VARCHAR);
                t.addColumn("SCOPE_CATLOG ", Types.SMALLINT);
                t.createPrimaryKey();

                for (int i = 0, tSize = tTable.size(); i < tSize; i++) {
                    Table table       = (Table) tTable.elementAt(i);
                    int   columnCount = table.getColumnCount();

                    if (table.tableType == Table.TEMP_TABLE
                            || table.tableType == Table.TEMP_TEXT_TABLE) {
                        if (dDatabase.findUserTable(
                                table.getName().name, session) == null) {
                            continue;
                        }
                    }

                    for (int j = 0; j < columnCount; j++) {
                        Column column = table.getColumn(j);
                        Object o[]    = t.getNewRow();

                        o[0] = o[1] = "";
                        o[2] = table.getName().name;
                        o[3] = column.columnName.name;
                        o[4] = new Integer(column.getType());
                        o[5] = Column.getTypeString(column.getType());

// fredt@users 20020130 - patch 491987 by jimbag@users
                        o[6] = new Integer(column.getSize());
                        o[8] = new Integer(column.getScale());
                        o[9] = new Integer(10);

                        int nullable;

                        if (column.isNullable()) {
                            nullable = DatabaseMetaData.columnNullable;
                            o[17]    = new String("YES");
                        } else {
                            nullable = DatabaseMetaData.columnNoNulls;
                            o[17]    = new String("NO");
                        }

                        o[10] = new Integer(nullable);

                        if (table.getIdentityColumn() == j) {
                            o[11] = "IDENTITY";
                        }

                        o[12] = column.getDefaultString();

                        // ordinal position
                        o[16] = new Integer(j + 1);

                        t.insert(o, null);
                    }
                }

                return t;
            }
            case SYSTEM_COLUMNPRIVILEGES : {
                t.addColumn("TABLE_" + META_CAT, Types.VARCHAR);
                t.addColumn("TABLE_" + META_SCHEM, Types.VARCHAR);
                t.addColumn("TABLE_NAME", Types.VARCHAR);
                t.addColumn("COLUMN_NAME", Types.VARCHAR);
                t.addColumn("GRANTOR", Types.VARCHAR);
                t.addColumn("GRANTEE", Types.VARCHAR);
                t.addColumn("PRIVILEGE", Types.VARCHAR);
                t.addColumn("IS_GRANTABLE", Types.VARCHAR);
                t.createPrimaryKey();

                /*
                 * // todo: get correct info
                 * for(int i=0;i<tTable.size();i++) {
                 * table=(Table)tTable.elementAt(i);
                 * int columns=table.getColumnCount();
                 * for(int j=0;j<columns;j++) {
                 * Object o[]=t.getNewRow();
                 * o[2]=table.getName();
                 * o[3]=table.getColumnName(j);
                 * o[4]="sa";
                 * o[6]="FULL";
                 * o[7]="NO";
                 * t.insert(o,null);
                 * }
                 * }
                 */
                return t;
            }
            case SYSTEM_TABLEPRIVILEGES : {
                t.addColumn("TABLE_" + META_CAT, Types.VARCHAR);
                t.addColumn("TABLE_" + META_SCHEM, Types.VARCHAR);
                t.addColumn("TABLE_NAME", Types.VARCHAR);
                t.addColumn("GRANTOR", Types.VARCHAR);
                t.addColumn("GRANTEE", Types.VARCHAR);
                t.addColumn("PRIVILEGE", Types.VARCHAR);
                t.addColumn("IS_GRANTABLE", Types.VARCHAR);
                t.createPrimaryKey();

                for (int i = 0, tSize = tTable.size(); i < tSize; i++) {
                    Table table = (Table) tTable.elementAt(i);

                    if (table.tableType == Table.TEMP_TABLE
                            || table.tableType == Table.TEMP_TEXT_TABLE) {
                        if (dDatabase.findUserTable(
                                table.getName().name, session) == null) {
                            continue;
                        }
                    }

                    Object o[] = t.getNewRow();

                    o[0] = o[1] = "";
                    o[2] = table.getName().name;
                    o[3] = "sa";
                    o[5] = "FULL";

                    t.insert(o, null);
                }

                return t;
            }
            case SYSTEM_VERSIONCOLUMNS :

            // return an empty table for SYSTEM_VERSIONCOLUMNS
            case SYSTEM_BESTROWIDENTIFIER :
                t.addColumn("SCOPE", Types.SMALLINT);
                t.addColumn("COLUMN_NAME", Types.VARCHAR);
                t.addColumn("DATA_TYPE", Types.SMALLINT);
                t.addColumn("TYPE_NAME", Types.VARCHAR);
                t.addColumn(META_COLUMN_SIZE, Types.INTEGER);
                t.addColumn(META_BUFFER_LENGTH, Types.INTEGER);
                t.addColumn(META_DECIMAL_DIGITS, Types.SMALLINT);
                t.addColumn("PSEUDO_COLUMN", Types.SMALLINT);
                t.addColumn("TABLE_NAME", Types.VARCHAR);
                t.createPrimaryKey();

                if (tableIdValue == SYSTEM_VERSIONCOLUMNS) {
                    return t;
                }
            {
                for (int i = 0, tSize = tTable.size(); i < tSize; i++) {
                    Table table = (Table) tTable.elementAt(i);

                    if (table.tableType == Table.VIEW) {
                        continue;
                    }

                    if (table.tableType == Table.TEMP_TABLE
                            || table.tableType == Table.TEMP_TEXT_TABLE) {
                        if (dDatabase.findUserTable(
                                table.getName().name, session) == null) {
                            continue;
                        }
                    }

                    Index index = null;
                    int[] cols  = null;

                    // fredt - don't report primary key on hidden column
                    for (int j = 0; j < table.getIndexCount(); j++) {
                        index = table.getIndex(j);

                        if (index.isUnique()) {
                            cols = index.getColumns();

                            if (cols[0] == table.getColumnCount()) {
                                cols = null;
                            } else {
                                break;
                            }
                        }
                    }

                    if (cols == null) {
                        continue;
                    }

                    int len = cols.length;

                    for (int j = 0; j < len; j++) {
                        Column column = table.getColumn(cols[j]);
                        Object o[]    = t.getNewRow();

                        o[0] = new Short(
                            (short) DatabaseMetaData.bestRowTemporary);
                        o[1] = column.columnName.name;
                        o[2] = new Short((short) column.getType());
                        o[3] = Column.getTypeString(column.getType());
                        o[4] = new Integer(column.getSize());
                        o[6] = new Integer(column.getScale());
                        o[7] = new Short(
                            (short) DatabaseMetaData.bestRowNotPseudo);
                        o[8] = table.getName().name;

                        t.insert(o, null);
                    }
                }
            }

                return t;

            case SYSTEM_PRIMARYKEYS : {
                t.addColumn("TABLE_" + META_CAT, Types.VARCHAR);
                t.addColumn("TABLE_" + META_SCHEM, Types.VARCHAR);
                t.addColumn("TABLE_NAME", Types.VARCHAR);
                t.addColumn("COLUMN_NAME", Types.VARCHAR);
                t.addColumn("KEY_SEQ", Types.SMALLINT);
                t.addColumn("PK_NAME", Types.VARCHAR);
                t.createPrimaryKey();

                for (int i = 0, tSize = tTable.size(); i < tSize; i++) {
                    Table table = (Table) tTable.elementAt(i);

                    if (table.tableType == Table.VIEW) {
                        continue;
                    }

                    if (table.tableType == Table.TEMP_TABLE
                            || table.tableType == Table.TEMP_TEXT_TABLE) {
                        if (dDatabase.findUserTable(
                                table.getName().name, session) == null) {
                            continue;
                        }
                    }

                    Index index  = table.getIndex(0);
                    int   cols[] = index.getColumns();

                    // fredt - don't report primary key on hidden column
                    if (cols[0] == table.getColumnCount()) {
                        continue;
                    }

                    int len = cols.length;

                    for (int j = 0; j < len; j++) {
                        Object o[] = t.getNewRow();

                        o[0] = o[1] = "";
                        o[2] = table.getName().name;
                        o[3] = table.getColumn(cols[j]).columnName.name;
                        o[4] = new Integer((j + 1));
                        o[5] = index.getName().name;

                        t.insert(o, null);
                    }
                }

                return t;
            }
            case SYSTEM_IMPORTEDKEYS :
            case SYSTEM_EXPORTEDKEYS :
            case SYSTEM_CROSSREFERENCE :
                return getCrossReference(name, session);

            case SYSTEM_TYPEINFO : {
                t.addColumn("TYPE_NAME", Types.VARCHAR);
                t.addColumn("DATA_TYPE", Types.SMALLINT);
                t.addColumn("PRECISION", Types.INTEGER);
                t.addColumn("LITERAL_PREFIX", Types.VARCHAR);
                t.addColumn("LITERAL_SUFFIX", Types.VARCHAR);
                t.addColumn("CREATE_PARAMS", Types.VARCHAR);
                t.addColumn("NULLABLE", Types.SMALLINT);
                t.addColumn("CASE_SENSITIVE", Types.BIT);
                t.addColumn("SEARCHABLE", Types.SMALLINT);
                t.addColumn("UNSIGNED_ATTRIBUTE", Types.BIT);
                t.addColumn(META_FIXED_PREC_SCALE, Types.BIT);
                t.addColumn("AUTO_INCREMENT", Types.BIT);
                t.addColumn("LOCAL_TYPE_NAME", Types.VARCHAR);
                t.addColumn("MINIMUM_SCALE", Types.SMALLINT);
                t.addColumn("MAXIMUM_SCALE", Types.SMALLINT);
                t.addColumn("SQL_DATE_TYPE", Types.INTEGER);
                t.addColumn("SQL_DATETIME_SUB", Types.INTEGER);
                t.addColumn("NUM_PREC_RADIX", Types.INTEGER);
                t.createPrimaryKey();

                for (int h = 0; h < Column.typesArray.length; h++) {
                    for (int i = 0; i < Column.typesArray[h].length; i++) {
                        Object o[]  = t.getNewRow();
                        int    type = Column.typesArray[h][i];

                        o[0] = Column.getTypeString(type);
                        o[1] = new Integer(type);
                        o[2] = INTEGER_0;             // precision
                        o[6] = new Integer(DatabaseMetaData.typeNullable);
                        o[7] = new Boolean(true);     // case sensitive
                        o[8] = new Integer(DatabaseMetaData.typeSearchable);
                        o[9] = new Boolean(false);    // unsigned
                        o[10] = new Boolean(type == Types.NUMERIC
                                            || type == Types.DECIMAL);
                        o[11] = new Boolean(type == Types.INTEGER);
                        o[12] = o[0];
                        o[13] = INTEGER_0;
                        o[14] = INTEGER_0;            // maximum scale
                        o[17] = new Integer(10);

                        t.insert(o, null);
                    }
                }

                return t;
            }
            case SYSTEM_INDEXINFO : {
                t.addColumn("TABLE_" + META_CAT, Types.VARCHAR);
                t.addColumn("TABLE_" + META_SCHEM, Types.VARCHAR);
                t.addColumn("TABLE_NAME", Types.VARCHAR);
                t.addColumn("NON_UNIQUE", Types.BIT);
                t.addColumn("INDEX_QUALIFIER", Types.VARCHAR);
                t.addColumn("INDEX_NAME", Types.VARCHAR);
                t.addColumn("TYPE", Types.SMALLINT);
                t.addColumn(META_ORDINAL_POSITION, Types.SMALLINT);
                t.addColumn("COLUMN_NAME", Types.VARCHAR);
                t.addColumn(META_ASC_OR_DESC, Types.VARCHAR);
                t.addColumn("CARDINALITY", Types.INTEGER);
                t.addColumn("PAGES", Types.INTEGER);
                t.addColumn("FILTER_CONDITION", Types.VARCHAR);
                t.createPrimaryKey();

                for (int i = 0, tSize = tTable.size(); i < tSize; i++) {
                    Table table = (Table) tTable.elementAt(i);

                    for (int j = 0; j < table.getIndexCount(); j++) {
                        Index index  = table.getIndex(j);
                        int   cols[] = index.getColumns();
                        int   len    = index.getVisibleColumns();

                        if (len == 0) {
                            continue;
                        }

                        for (int k = 0; k < len; k++) {
                            Object o[] = t.getNewRow();

                            o[0] = o[1] = "";
                            o[2] = table.getName().name;
                            o[3] = new Boolean(!index.isUnique());
                            o[5] = index.getName().name;
                            o[6] = new Integer(
                                DatabaseMetaData.tableIndexOther);
                            o[7] = new Integer(k + 1);
                            o[8] = table.getColumn(cols[k]).columnName.name;
                            o[9] = "A";

                            t.insert(o, null);
                        }
                    }
                }

                return t;
            }
            case SYSTEM_UDTS : {
                t.addColumn("TYPE_" + META_CAT, Types.VARCHAR);
                t.addColumn("TYPE_" + META_SCHEM, Types.VARCHAR);
                t.addColumn("TYPE_NAME", Types.VARCHAR);
                t.addColumn("CLASS_NAME", Types.BIT);
                t.addColumn("DATA_TYPE", Types.VARCHAR);
                t.addColumn("REMARKS", Types.VARCHAR);

// boucherb@users 20020415 added for JDBC 3 clients
                t.addColumn("BASE_TYPE ", Types.SMALLINT);
                t.createPrimaryKey();

                return t;
            }
            case SYSTEM_CONNECTIONINFO : {
                t.addColumn("KEY", Types.VARCHAR);
                t.addColumn("VALUE", Types.VARCHAR);
                t.createPrimaryKey();

                Object o[] = t.getNewRow();

                o[0] = "USER";
                o[1] = session.getUsername();

                t.insert(o, null);

                o    = t.getNewRow();
                o[0] = "READONLY";
                o[1] = session.isReadOnly() ? "TRUE"
                                            : "FALSE";

                t.insert(o, null);

                o    = t.getNewRow();
                o[0] = "MAXROWS";
                o[1] = String.valueOf(session.getMaxRows());

                t.insert(o, null);

                o    = t.getNewRow();
                o[0] = "DATABASE";
                o[1] = session.getDatabase().getName();

                t.insert(o, null);

                o    = t.getNewRow();
                o[0] = "IDENTITY";
                o[1] = String.valueOf(session.getLastIdentity());

                t.insert(o, null);

                return t;
            }
            case SYSTEM_USERS : {
                t.addColumn("USER", Types.VARCHAR);
                t.addColumn("ADMIN", Types.BIT);
                t.createPrimaryKey();

                Vector v = aAccess.getUsers();

                for (int i = 0, vSize = v.size(); i < vSize; i++) {
                    User u = (User) v.elementAt(i);

                    // todo: this is not a nice implementation
                    if (u == null) {
                        continue;
                    }

                    String user = u.getName();

                    if (!user.equals("PUBLIC")) {
                        Object o[] = t.getNewRow();

                        o[0] = user;
                        o[1] = new Boolean(u.isAdmin());

                        t.insert(o, null);
                    }
                }

                return t;
            }
            default :
                return null;
        }
    }

    static final Short importedKeyNoActionShort =
        new Short((short) DatabaseMetaData.importedKeyNoAction);
    static final Short importedKeyCascadeShort =
        new Short((short) DatabaseMetaData.importedKeyCascade);
    static final Short importedKeyNotDeferrableShort =
        new Short((short) DatabaseMetaData.importedKeyNotDeferrable);

    Table getCrossReference(HsqlName name,
                            Session session) throws SQLException {

        Table t = createTable(name);

        t.addColumn("PKTABLE_" + META_CAT, Types.VARCHAR);
        t.addColumn("PKTABLE_" + META_SCHEM, Types.VARCHAR);
        t.addColumn("PKTABLE_NAME", Types.VARCHAR);
        t.addColumn("PKCOLUMN_NAME", Types.VARCHAR);
        t.addColumn("FKTABLE_" + META_CAT, Types.VARCHAR);
        t.addColumn("FKTABLE_" + META_SCHEM, Types.VARCHAR);
        t.addColumn("FKTABLE_NAME", Types.VARCHAR);
        t.addColumn("FKCOLUMN_NAME", Types.VARCHAR);
        t.addColumn("KEY_SEQ", Types.SMALLINT);
        t.addColumn("UPDATE_RULE", Types.SMALLINT);
        t.addColumn("DELETE_RULE", Types.SMALLINT);
        t.addColumn("FK_NAME", Types.VARCHAR);
        t.addColumn("PK_NAME", Types.VARCHAR);
        t.addColumn("DEFERRABILITY", Types.SMALLINT);
        t.createPrimaryKey();

        for (int i = 0, tSize = tTable.size(); i < tSize; i++) {
            Table      table     = (Table) tTable.elementAt(i);
            Vector     constVect = table.getConstraints();
            Constraint constraint;

            for (int j = 0; j < constVect.size(); j++) {
                constraint = (Constraint) constVect.elementAt(j);

                if (constraint.getType() != Constraint.FOREIGN_KEY) {
                    continue;
                }

                String mainTableName = constraint.getMain().tableName.name;
                String refTableName  = constraint.getRef().tableName.name;

                if (dDatabase.findUserTable(mainTableName) == null
                        || dDatabase.findUserTable(refTableName) == null) {
                    continue;
                }

                int pkcols[] = constraint.getMainColumns();
                int fkcols[] = constraint.getRefColumns();
                int len      = pkcols.length;

                for (int k = 0; k < len; k++) {
                    Object o[] = t.getNewRow();

                    o[0] = o[1] = "";
                    o[2] = mainTableName;
                    o[3] = constraint.getMain().getColumn(
                        pkcols[k]).columnName.name;
                    o[4] = o[5] = "";
                    o[6] = refTableName;
                    o[7] = constraint.getRef().getColumn(
                        fkcols[k]).columnName.name;
                    o[8]  = new Short((short) (k + 1));
                    o[9]  = importedKeyNoActionShort;
                    o[10] = constraint.isCascade() ? importedKeyCascadeShort
                                                   : importedKeyNoActionShort;
                    o[11] = constraint.getFkName();
                    o[12] = constraint.getPkName();
                    o[13] = importedKeyNotDeferrableShort;

                    t.insert(o, null);
                }
            }
        }

        return t;
    }

    /**
     * Method declaration
     *
     *
     * @param name
     *
     * @return
     */
    private Table createTable(HsqlName name) throws SQLException {
        return new Table(dDatabase, name, Table.SYSTEM_TABLE, null);
    }
}
