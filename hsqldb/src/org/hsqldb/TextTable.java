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


package org.hsqldb;

import java.sql.SQLException;

// tony_lai@users 20020820 - patch 595099 by tlai@users use user define PK name

/**
 *  Class declaration
 * @author sqlbob@users (RMP)
 * @version    1.7.0
 */
class TextTable extends org.hsqldb.Table {

    private String  readRoots  = "";
    private String  emptyRoots = "";
    private String  dataSource = "";
    private boolean isReversed = false;

    /**
     *  Constructor declaration
     *
     * @param  db
     * @param  isTemp is a temp text table
     * @param  name
     * @exception  SQLException  Description of the Exception
     */
    TextTable(Database db, HsqlName name, int type,
              Session session) throws SQLException {
        super(db, name, type, session);
    }

    private void openCache(String source, boolean isDesc,
                           boolean isRdOnly) throws SQLException {

        // Close old cache:
        if (!dataSource.equals("")) {
            dDatabase.logger.closeTextCache(tableName.name);
        }

        cCache = null;

        int count = getIndexCount();

        for (int i = 0; i < count; i++) {
            getIndex(i).setRoot(null);
        }

        // Open new cache:
        if ((source != null) &&!source.equals("")) {
            try {
                cCache = dDatabase.logger.openTextCache(tableName.name,
                        source, isRdOnly, isDesc);

                int    freePos = cCache.getFreePos();
                String roots   = readRoots;

                if (freePos <= TextCache.NL.length()) {
                    roots = emptyRoots;
                }

                roots += freePos;

                super.setIndexRoots(roots);
            } catch (SQLException e) {
                if (!dataSource.equals(source) || (isDesc != isReversed)
                        || (isRdOnly != isReadOnly)) {

                    // Restore old cache.
                    openCache(dataSource, isReversed, isReadOnly);
                } else {
                    if (cCache != null) {
                        cCache.shutdown();
                    }

                    dataSource = "";
                    isReversed = false;
                }

                throw (e);
            }
        }

        if (source == null) {
            source = "";
        }

        dataSource = source;
        isReversed = (isDesc &&!source.equals(""));
    }

    boolean equals(String other, Session c) {

        boolean isEqual = super.equals(other, c);

        if (isEqual && isReversed) {
            try {
                openCache(dataSource, isReversed, isReadOnly);
            } catch (SQLException e) {
                return false;
            }
        }

        return (isEqual);
    }

    boolean equals(String other) {

        boolean isEqual = super.equals(other);

        if (isEqual && isReversed) {
            try {
                openCache(dataSource, isReversed, isReadOnly);
            } catch (SQLException e) {
                return false;
            }
        }

        return (isEqual);
    }

    protected void setDataSource(String source, boolean isDesc,
                                 Session s) throws SQLException {

        if (isTemp) {
            Trace.check(s.getId() == ownerSession.getId(),
                        Trace.ACCESS_IS_DENIED);
        } else {
            s.checkAdmin();
        }

        //-- Open if descending, direction changed, or file changed.
        if (isDesc || (isDesc != isReversed) ||!dataSource.equals(source)) {
            openCache(source, isDesc, isReadOnly);
        }

        if (isReversed) {
            isReadOnly = true;
        }
    }

    protected String getDataSource() throws SQLException {
        return (dataSource);
    }

    protected boolean isDescDataSource() throws SQLException {
        return (isReversed);
    }

    void setDataReadOnly(boolean value) throws SQLException {

        if (isReversed && value == true) {
            throw Trace.error(Trace.DATA_IS_READONLY);
        }

        openCache(dataSource, isReversed, value);

        isReadOnly = value;
    }

    /**
     *  Method declaration
     *
     * @return
     * @throws  SQLException
     */
    String getIndexRoots() throws SQLException {

        Trace.doAssert(isCached, "Table.getIndexRootData");

        return (readRoots + "0");
    }

    /**
     *  Method declaration
     *
     * @param  s
     * @throws  SQLException
     */
    void setIndexRoots(String s) throws SQLException {

        Trace.check(isCached, Trace.TABLE_NOT_FOUND);

        // Ignore
    }

    boolean isIndexCached() {
        return false;
    }

    protected Table duplicate() throws SQLException {
        return (new TextTable(dDatabase, tableName, tableType, ownerSession));
    }

    void indexRow(Row r, boolean inserted) throws SQLException {

        if (inserted) {
            super.indexRow(r, true);
        } else {
            Node n       = r.getNextNode(null);
            Node primary = getPrimaryIndex().insertUncached(n);

            if (primary == n) {

                // Not already indexed.
                n = r.getNextNode(n);

                for (int i = 1; n != null; i++) {
                    getIndex(i).insertUncached(n);

                    n = r.getNextNode(n);
                }
            } else {
                r.setPrimaryNode(primary);
            }
        }
    }

    void checkUpdate(int col[], Result deleted,
                     Result inserted) throws SQLException {
        Trace.check(!dataSource.equals(""), Trace.UNKNOWN_DATA_SOURCE);
        super.checkUpdate(col, deleted, inserted);
    }

    void insert(Object row[], Session c) throws SQLException {
        Trace.check(!dataSource.equals(""), Trace.UNKNOWN_DATA_SOURCE);
        super.insert(row, c);
    }

    void delete(Object row[], Session c) throws SQLException {
        Trace.check(!dataSource.equals(""), Trace.UNKNOWN_DATA_SOURCE);
        super.delete(row, c);
    }

    /**
     *  Method declaration
     *
     * @param  column
     * @param  name
     * @param  unique
     * @return                Description of the Return Value
     * @throws  SQLException
     */
    Index createIndexPrivate(int column[], HsqlName name,
                             boolean unique) throws SQLException {

        readRoots  += "0 ";
        emptyRoots += "-1 ";

        return (super.createIndexPrivate(column, name, unique));
    }

// tony_lai@users 20020820 - patch 595099
    void createPrimaryKey(String pkName, int[] columns) throws SQLException {

        if ((columns == null)
                || ((columns.length == 1)
                    && getColumn(columns[0]).columnName.name.equals(
                        DEFAULT_PK))) {
            super.createPrimaryKey(null, columns);
        } else {
            throw (Trace.error(Trace.SECOND_PRIMARY_KEY));
        }
    }
}
