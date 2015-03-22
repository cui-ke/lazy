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

import org.hsqldb.lib.ArrayUtil;
import java.sql.SQLException;
import java.util.Hashtable;

// fredt@users 20020225 - patch 1.7.0 by boucherb@users - named constraints
// fredt@users 20020320 - doc 1.7.0 - update
// tony_lai@users 20020820 - patch 595156 by tlai@users -  violation of Integrity constraint name

/**
 *  Implementation of a table constraint with references to the indexes used
 *  by the constraint.
 *
 * @version    1.7.0
 */
class Constraint {

    static final int       FOREIGN_KEY = 0,
                           MAIN        = 1,
                           UNIQUE      = 2;
    private ConstraintCore core;
    private HsqlName       constName;
    private int            iType;

    /**
     *  Constructor declaration
     *
     * @param  name
     * @param  t
     * @param  index
     */
    Constraint(HsqlName name, Table t, Index index) {

        core       = new ConstraintCore();
        constName  = name;
        iType      = UNIQUE;
        core.tMain = t;
        core.iMain = index;
        /* fredt - in unique constraints column list for iColMain is that
           of iMain
        */
        core.iColMain = index.getColumns();
        core.iLen     = core.iColMain.length;
    }

    /**
     *  Constructor for main constraints (foreign key references in PK table)
     *
     * @param  name
     * @param  t
     * @param  index
     */
    Constraint(HsqlName name, Constraint fkconstraint) {

        constName = name;
        iType     = MAIN;
        core      = fkconstraint.core;
    }

    /**
     *  Constructor for foreign key constraints
     *
     * @param  pkname
     * @param  fkname
     * @param  main
     * @param  ref
     * @param  colmain
     * @param  colref
     * @param  imain
     * @param  iref
     * @exception  SQLException  Description of the Exception
     */
    Constraint(HsqlName pkname, HsqlName fkname, Table main, Table ref,
               int colmain[], int colref[], Index imain, Index iref,
               boolean cascade) throws SQLException {

        core        = new ConstraintCore();
        core.pkName = pkname;
        core.fkName = fkname;
        constName   = fkname;
        iType       = FOREIGN_KEY;
        core.tMain  = main;
        core.tRef   = ref;
        /* fredt - in FK constraints column lists for iColMain and iColRef have
           identical sets to visible columns of iMain and iRef respectively
           but the order of columns can be different and must be maintained
        */
        core.iColMain = colmain;
        core.iLen     = core.iColMain.length;
        core.iColRef  = colref;
        core.oColRef  = new Object[core.iColRef.length];
        core.iMain    = imain;
        core.iRef     = iref;
        core.bCascade = cascade;

        setTableRows();
    }

    private Constraint() {}

    private void setTableRows() throws SQLException {

        core.oMain = core.tMain.getNewRow();

        if (core.tRef != null) {
            core.oRef = core.tRef.getNewRow();
        }

        if (Trace.DOASSERT) {
            Trace.doAssert(core.iColMain.length == core.iColRef.length);
        }
    }

    HsqlName getName() {
        return constName;
    }

    /**
     * Changes constraint name.
     *
     * @param name
     * @param isquoted
     */
    private void setName(String name, boolean isquoted) {
        constName.rename(name, isquoted);
    }

    /**
     *  probably a misnomer, but DatabaseMetaData.getCrossReference specifies
     *  it this way (I suppose because most FKs are declared against the PK of
     *  another table)
     *
     *  @return name of the index refereneced by a foreign key
     */
    String getPkName() {
        return core.pkName == null ? null
                                   : core.pkName.name;
    }

    /**
     *  probably a misnomer, but DatabaseMetaData.getCrossReference specifies
     *  it this way (I suppose because most FKs are declared against the PK of
     *  another table)
     *
     *  @return name of the index for the referencing foreign key
     */
    String getFkName() {
        return core.fkName == null ? null
                                   : core.fkName.name;
    }

    /**
     *  Method declaration
     *
     * @return name of the index for the foreign key column (child)
     */
    int getType() {
        return iType;
    }

    /**
     *  Method declaration
     *
     * @return
     */
    Table getMain() {
        return core.tMain;
    }

    Index getMainIndex() {
        return core.iMain;
    }

    /**
     *  Method declaration
     *
     * @return
     */
    Table getRef() {
        return core.tRef;
    }

    Index getRefIndex() {
        return core.iRef;
    }

    /**
     *  Does (foreign key) constraint cascade on delete
     *
     * @return
     */
    boolean isCascade() {
        return core.bCascade;
    }

    /**
     *  Method declaration
     *
     * @return
     */
    int[] getMainColumns() {
        return core.iColMain;
    }

    /**
     *  Method declaration
     *
     * @return
     */
    int[] getRefColumns() {
        return core.iColRef;
    }

    /**
     *  See if an index is part this constraint and the constraint is set for
     *  a foreign key. Used for tests before dropping an index. (fredt@users)
     *
     * @return
     */
    boolean isIndexFK(Index index) {

        if (iType == FOREIGN_KEY || iType == MAIN) {
            if (core.iMain == index || core.iRef == index) {
                return true;
            }
        }

        return false;
    }

    /**
     *  See if an index is part this constraint and the constraint is set for
     *  a unique constraint. Used for tests before dropping an index.
     *  (fredt@users)
     *
     * @return
     */
    boolean isIndexUnique(Index index) {

        if (iType == UNIQUE && core.iMain == index) {
            return true;
        }

        return false;
    }

// fredt@users 20020225 - patch 1.7.0 by fredt - duplicate constraints

    /**
     * Compares this with another constraint column set. This implementation
     * only checks UNIQUE constraints.
     */
    boolean isEquivalent(int col[], int type) {

        if (type == iType && iType == UNIQUE && core.iLen == col.length) {
            if (ArrayUtil.haveEqualSets(core.iColMain, col, core.iLen)) {
                return true;
            }
        }

        return false;
    }

    /**
     *  Used to update constrains to reflect structural changes in a table.
     *
     * @param  oldt reference to the old version of the table
     * @param  newt referenct to the new version of the table
     * @param  colindex index at which table column is added or removed
     * @param  adjust -1, 0, +1 to indicate if column is added or removed
     * @throws  SQLException
     */
    void replaceTable(Table oldt, Table newt, int colindex,
                      int adjust) throws SQLException {

        if (oldt == core.tMain) {
            core.tMain = newt;

            setTableRows();

            core.iMain = core.tMain.getIndex(core.iMain.getName().name);
            core.iColMain = ArrayUtil.getAdjustedColumnArray(core.iColMain,
                    core.iLen, colindex, adjust);
        }

        if (oldt == core.tRef) {
            core.tRef = newt;

            setTableRows();

            if (core.iRef != null) {
                core.iRef = core.tRef.getIndex(core.iRef.getName().name);

                if (core.iRef != core.iMain) {
                    core.iColRef =
                        ArrayUtil.getAdjustedColumnArray(core.iColRef,
                                                         core.iLen, colindex,
                                                         adjust);
                }
            }
        }
    }

    /**
     *  Checks for foreign key violation when inserting a row in the child
     *  table.
     *
     * @param  row
     * @throws  SQLException
     */
    void checkInsert(Object row[]) throws SQLException {

        if ((iType == MAIN) || (iType == UNIQUE)) {

            // inserts in the main table are never a problem
            // unique constraints are checked by the unique index
            return;
        }

        // must be called synchronized because of oMain
        for (int i = 0; i < core.iLen; i++) {
            Object o = row[core.iColRef[i]];

            if (o == null) {

                // if one column is null then integrity is not checked
                return;
            }

            core.oMain[core.iColMain[i]] = o;
        }

        // a record must exist in the main table
        Trace.check(core.iMain.find(core.oMain) != null,
                    Trace.INTEGRITY_CONSTRAINT_VIOLATION,
                    core.fkName.name + " table: "
                    + core.tMain.getName().name);
    }

    /**
     *  Check if a row in the referenced (parent) table can be deleted. Used
     *  only for UPDATE table statements. Checks for DELETE FROM table
     *  statements are now handled by findFkRef() to support ON DELETE
     *  CASCADE.
     *
     * @param  row
     * @throws  SQLException
     */
    private void checkDelete(Object row[]) throws SQLException {

        // must be called synchronized because of oRef
        for (int i = 0; i < core.iLen; i++) {
            Object o = row[core.iColMain[i]];

            if (o == null) {

                // if one column is null then integrity is not checked
                return;
            }

            core.oRef[core.iColRef[i]] = o;
        }

        // there must be no record in the 'slave' table
        Node node = core.iRef.find(core.oRef);

        // tony_lai@users 20020820 - patch 595156
        Trace.check(node == null, Trace.INTEGRITY_CONSTRAINT_VIOLATION,
                    core.fkName.name + " table: " + core.tRef.getName().name);
    }

// fredt@users 20020225 - patch 1.7.0 - cascading deletes

    /**
     * New method to find any referencing node (containing the row) for a
     * foreign key (finds row in child table). If ON DELETE CASCADE is
     * supported by this constraint, then the method finds the first row
     * among the rows of the table ordered by the index and doesn't throw.
     * Without ON DELETE CASCADE, the method attempts to finds any row that
     * exists, in which case it throws an exception. If no row is found,
     * null is returned.
     * (fredt@users)
     *
     * @param  array of objects for a database row
     * @return Node object or null
     * @throws  SQLException
     */
    Node findFkRef(Object row[]) throws SQLException {

        // must be called synchronized because of oRef
        for (int i = 0; i < core.iLen; i++) {
            Object o = row[core.iColMain[i]];

            if (o == null) {

                // if one column is null then integrity is not checked
                return null;
            }

            core.oColRef[i] = o;
        }

        // there must be no record in the 'slave' table
        Node node = core.iRef.findSimple(core.oColRef, core.bCascade);

        // tony_lai@users 20020820 - patch 595156
        Trace.check((node == null) || core.bCascade,
                    Trace.INTEGRITY_CONSTRAINT_VIOLATION,
                    core.fkName.name + " table: " + core.tRef.getName().name);

        return node;
    }

    /**
     *  Checks if updating a set of columns in a table row breaks the
     *  referential integrity constraint.
     *
     * @param  col array of column indexes for columns to check
     * @param  deleted  rows to delete
     * @param  inserted rows to insert
     * @throws  SQLException
     */
    void checkUpdate(int col[], Result deleted,
                     Result inserted) throws SQLException {

        if (iType == UNIQUE) {

            // unique constraints are checked by the unique index
            return;
        }

        if (iType == MAIN) {
            if (!ArrayUtil.haveCommonElement(col, core.iColMain, core.iLen)) {
                return;
            }

            // check deleted records
            Record r = deleted.rRoot;

            while (r != null) {

                // if an identical record exists we don't have to test
                if (core.iMain.find(r.data) == null) {
                    checkDelete(r.data);
                }

                r = r.next;
            }
        } else if (iType == FOREIGN_KEY) {
            if (!ArrayUtil.haveCommonElement(col, core.iColMain, core.iLen)) {
                return;
            }

            // check inserted records
            Record r = inserted.rRoot;

            while (r != null) {
                checkInsert(r.data);

                r = r.next;
            }
        }
    }
}
