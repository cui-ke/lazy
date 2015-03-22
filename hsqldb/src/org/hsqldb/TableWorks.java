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
import java.util.Vector;
import java.util.Hashtable;

// fredt@users 20020520 - patch 1.7.0 - ALTER TABLE support
// tony_lai@users 20020820 - patch 595172 by tlai@users - drop constraint fix

/**
 * The methods in this class perform alterations to the structure of an
 * existing table which may result in a new Table object
 *
 * @version 1.7.0
 */
class TableWorks {

    private Table table;

    TableWorks(Table table) {
        this.table = table;
    }

    Table getTable() {
        return table;
    }

// fredt@users 20020225 - patch 1.7.0 - require existing index for foreign key

    /**
     *  Creates a foreign key according to current sql.strict_fk or
     *  sql.strong_fk settings. Foreign keys are enforced via indexes on both
     *  the referencing (child) and referenced (parent) tables.
     *  <p>
     *  If sql.strict_fk is set (default for new databases) a pre-existing
     *  primary key or unique index is required on the referenced columns
     *  of the referenced table.
     *  <p>
     *  When there is no primary key or unique index, a new index is created
     *  automatically. If sql.strong_fk is set in the abasence of
     *  sql.strict_fk, this automatic index will be a unique index. Otherwise
     *  (for compatibility with existing data created with HSQLDB 1.61 or
     *  earlier) it will be an ordinary index.
     *
     *  The non-unique index on the referencing table is created if none
     *  exits. The existence of a PK or unique constraint is required for
     *  backward referencing foreign keys. This is becuase the index must
     *  always be created before the foreign key DDL is processed.
     *  Any index, including a user-defined one can be used for forward
     *  referencing FK's.
     *
     *  Foriegn keys on temp tables can reference other temp tables with the
     *  same rules above. When they reference other permanent tables, the
     *  sql.strict_fk is always applied. Foreign keys on permanent tables
     *  cannot reference temp tables.
     *
     *  Currently, duplicate foreign keys can be declared, but they do not
     *  create any additional indexes. (fred@users)
     *
     * @param  fkcol
     * @param  expcol
     * @param  fkname           foreign key name
     * @param  expTable
     * @param  cascade        foreign key enforces cascading deletes
     * @throws SQLException
     */
    void createForeignKey(int fkcol[], int expcol[], HsqlName fkname,
                          Table expTable,
                          boolean cascade) throws SQLException {

        if (table.getConstraint(fkname.name) != null) {
            throw Trace.error(Trace.CONSTRAINT_ALREADY_EXISTS);
        }

        int interval = table.dDatabase.getTableIndex(table)
                       - table.dDatabase.getTableIndex(expTable);
        Index exportindex = (interval == 0)
                            ? expTable.getConstraintIndexForColumns(expcol,
                                true)
                            : expTable.getIndexForColumns(expcol, true);
        boolean strict =
            table.dDatabase.getProperties().isPropertyTrue("sql.strict_fk");

        strict = strict || (!expTable.isTemp() && table.isTemp());

        if (expTable.isTemp() &&!table.isTemp()) {
            throw Trace.error(Trace.FOREIGN_KEY_NOT_ALLOWED,
                              "referenced table cannot be TEMPORARY");
        }

        if (strict && (exportindex == null ||!exportindex.isUnique())) {
            throw Trace.error(Trace.INDEX_NOT_FOUND,
                              "needs unique index on referenced columns of "
                              + expTable.getName().statementName);
        }

        Index fkindex = (interval >= 0)
                        ? table.getConstraintIndexForColumns(fkcol, false)
                        : table.getIndexForColumns(fkcol, false);

        if (fkindex == null) {
            HsqlName iname = HsqlName.makeAutoName("IDX");

            fkindex = createIndex(fkcol, iname, false);
        }

        boolean strong =
            table.dDatabase.getProperties().isPropertyTrue("sql.strong_fk");

        if (exportindex == null || (strong &&!exportindex.isUnique())) {
            HsqlName   iname = HsqlName.makeAutoName("FK");
            TableWorks tw    = new TableWorks(expTable);

            exportindex = tw.createIndex(expcol, iname, strong);
            expTable    = tw.getTable();
        }

        HsqlName pkname = HsqlName.makeAutoName("REF", fkname.name);
        Constraint c = new Constraint(pkname, fkname, expTable, table,
                                      expcol, fkcol, exportindex, fkindex,
                                      cascade);

        table.addConstraint(c);
        expTable.addConstraint(new Constraint(pkname, c));
    }

// fredt@users 20020315 - patch 1.7.0 - create index bug
// this method would break existing foreign keys as the table order in the DB
// was changed. Instead, we now link in place of the old table

    /**
     *  Because of the way indexes and column data are held in memory and
     *  on disk, it is necessary to recreate the table when an index is added
     *  to a non-empty table. (fredt@users)
     *
     * @param  col
     * @param  name
     * @param  unique
     * @return  new index
     * @throws  SQLException normally for lack of resources
     */
    Index createIndex(int col[], HsqlName name,
                      boolean unique) throws SQLException {

        if (table.isEmpty()) {
            return table.createIndexPrivate(col, name, unique);
        }

        Table tn = table.moveDefinition(null, null, table.getColumnCount(),
                                        0);
        Index newindex = tn.createIndexPrivate(col, name, unique);

        tn.moveData(table, table.getColumnCount(), 0);
        tn.updateConstraints(table, table.getColumnCount(), 0);

        int index = table.dDatabase.getTableIndex(table);

        table.dDatabase.getTables().setElementAt(tn, index);

        table = tn;

        return newindex;
    }

// fredt@users 20020225 - avoid duplicate constraints

    /**
     *  A unique constraint relies on a unique indexe on the table. It can
     *  cover a single column or multiple columns.
     *  <p>
     *  All unique constraint names are generated by Database.java as unique
     *  within the database. Duplicate constraints (more than one unique
     *  constriant on the same set of columns are still allowed but the
     *  names will be different. (fredt@users)
     *
     * @param  col
     * @param  name
     * @throws  SQLException
     */
    void createUniqueConstraint(int[] col,
                                HsqlName name) throws SQLException {

        Vector constraints = table.getConstraints();

        for (int i = 0; i < constraints.size(); i++) {
            Constraint c = (Constraint) constraints.elementAt(i);

            if (c.isEquivalent(col, Constraint.UNIQUE)
                    || c.getName().name.equals(name.name)) {
                throw Trace.error(Trace.CONSTRAINT_ALREADY_EXISTS);
            }
        }

        // create an autonamed index
        HsqlName   indexname     = HsqlName.makeAutoName("IDX");
        Index      index         = createIndex(col, indexname, true);
        Constraint newconstraint = new Constraint(name, table, index);

        table.addConstraint(newconstraint);
    }

// fredt@users 20020315 - patch 1.7.0 - drop index bug

    /**
     *  Because of the way indexes and column data are held in memory and
     *  on disk, it is necessary to recreate the table when an index is added
     *  to a non-empty table.<p>
     *  Originally, this method would break existing foreign keys as the
     *  table order in the DB was changed. The new table is now linked
     *  in place of the old table (fredt@users)
     *
     * @param  indexname
     * @throws  SQLException
     */
    void dropIndex(String indexname) throws SQLException {

        Table tn = table.moveDefinition(indexname, null,
                                        table.getColumnCount(), 0);

        tn.moveData(table, table.getColumnCount(), 0);
        tn.updateConstraints(table, table.getColumnCount(), 0);

        int i = table.dDatabase.getTableIndex(table);

        table.dDatabase.getTables().setElementAt(tn, i);

        table = tn;
    }

    /**
     *
     * @param  column
     * @param  colindex
     * @param  adjust +1 or -1
     * @throws  SQLException
     */
    void addOrDropColumn(Column column, int colindex,
                         int adjust) throws SQLException {

        Table tn = table.moveDefinition(null, column, colindex, adjust);

        tn.moveData(table, colindex, adjust);
        tn.updateConstraints(table, colindex, adjust);

        int i = table.dDatabase.getTableIndex(table);

        table.dDatabase.getTables().setElementAt(tn, i);

        table = tn;
    }

    /**
     *  Method declaration
     *
     */
    void dropConstraint(String name) throws SQLException {

        int        j    = table.getConstraintIndex(name);
        Constraint c    = table.getConstraint(name);
        Hashtable  cmap = new Hashtable();

        cmap.put(c, c);

        if (c == null) {
            throw Trace.error(Trace.CONSTRAINT_NOT_FOUND,
                              name + " in table: " + table.getName().name);
        }

        if (c.getType() == c.MAIN) {
            throw Trace.error(Trace.DROP_SYSTEM_CONSTRAINT);
        }

        if (c.getType() == c.FOREIGN_KEY) {
            Table mainTable = c.getMain();
            Constraint mainConstraint =
                mainTable.getConstraint(c.getPkName());

            cmap.put(mainConstraint, mainConstraint);

            int   k         = mainTable.getConstraintIndex(c.getPkName());
            Index mainIndex = mainConstraint.getMainIndex();

            // never drop user defined indexes
            // fredt - todo - use of auto main indexes for FK's will be
            // deprecated so that there is no need to drop an index on the
            // main (pk) table
            if (mainIndex.getName().isReservedName()) {
                boolean candrop = false;

                try {

                    // check if the index is used by other constraints otherwise drop
                    mainTable.checkDropIndex(mainIndex.getName().name, cmap);

                    candrop = true;

                    TableWorks tw = new TableWorks(mainTable);

                    tw.dropIndex(mainIndex.getName().name);

                    // update this.table if self referencing FK
                    if (mainTable == table) {
                        table = tw.getTable();
                    }
                } catch (SQLException e) {
                    if (candrop) {
                        throw e;
                    }
                }
            }

            // drop the reference index if automatic and unused elsewhere
            Index refIndex = c.getRefIndex();

            if (refIndex.getName().isReservedName()) {
                try {

                    // check if the index is used by other constraints otherwise drop
                    table.checkDropIndex(refIndex.getName().name, cmap);
                    dropIndex(refIndex.getName().name);
                } catch (SQLException e) {}
            }

            mainTable.vConstraint.removeElementAt(k);
            table.vConstraint.removeElementAt(j);
        } else if (c.getType() == c.UNIQUE) {

            // throw if the index for unique constraint is shared
            table.checkDropIndex(c.getMainIndex().getName().name, cmap);

            // all is well if dropIndex throws for lack of resources
            dropIndex(c.getMainIndex().getName().name);
            table.vConstraint.removeElementAt(j);
        }
    }
}
