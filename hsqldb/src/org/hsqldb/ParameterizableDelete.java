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


/*
 * Delete.java
 *
 * Created on January 26, 2002, 4:32 PM
 */
package org.hsqldb;

import java.sql.SQLException;

/**
 *
 * @author Campbell Boucher-Burnet, Camco & Associates Consulting
 * @version 1.0
 */
final class ParameterizableDelete extends AbstractParameterizableStatement {

    Table                      table;
    ParameterizableExpression  eCondition;
    ParameterizableTableFilter filter;

    ParameterizableDelete(Database db, Table t,
                          ParameterizableExpression condition)
                          throws SQLException {

        super(db);

        Trace.doAssert(t != null, "table is null");

        if (t.isView()) {
            throw Trace.error(Trace.NOT_A_TABLE, t.getName().name);
        }

        table      = t;
        eCondition = condition;
        /*
        if (eCondition != null) {
            eCondition.resolve(filter);
            filter.setCondition(eCondition);
        }
         */
    }

    protected Result execImpl(Session session) throws SQLException {

        // We shouldn't have to do this here once things are integrated properly.
        // TODO:  add some sort of change listener infrastructure and shared prepared statement manager
        // so that pstms are dropped or set invalid (need to be resolved again) when their underlying database 
        // objects are dropped/altered respectively
        Trace.check(table
                    == database.getTable(table.getName().name,
                                         session), Trace.TABLE_NOT_FOUND);
        session.checkReadWrite();
        session.check(table.getName().name, UserManager.DELETE);

// fredt@users 20020221 - patch 513005 by sqlbob@users (RMP)
        Trace.check(!table.isDataReadOnly(), Trace.DATA_IS_READONLY);
        table.fireAll(TriggerDef.DELETE_BEFORE);

        int count = 0;

        //believe it or not, constructing a new table filter on each exec actually significantly
        //improves performance, vs. doing one construction in this object's constructor
        // TODO: look at ParameterizableTableFilter to figure out why and correct, if possible
        filter = new ParameterizableTableFilter(table, null, false);

        // TODO:  fix setCondition so that condition expressions that contain unbound PARAMETER
        // subexpression still cause the appropriate indexes to be used/optimizations to occur.
        // this will allow us to avoid calling this code on each execution, possibly improving 
        // performance                
        if (eCondition != null) {
            eCondition.resolve(filter);
            filter.setCondition(eCondition);
        }

        // -questionably improves performance for deletes that will remove a number of rows at once
        // -seems to even improve the result over 1000 calls of single row deletes by about 10ms  
        // -eventually, this stuff can be fully optimized by decompiling to bytecode assembly and 
        // removing inefficiencies by hand or using BCEL or some other bytecode manipulation
        // software to do standard, well-known optimizations that javac -O does not do.        
        ParameterizableTableFilter lFilter   = filter;
        ParameterizableExpression  condition = eCondition;

        if (lFilter.findFirst()) {
            Result del = new Result();    // don't need column count and so on

            do {
                if (condition == null || condition.test()) {
                    del.add(lFilter.oCurrentData);
                }
            } while (lFilter.next());

            Record n = del.rRoot;

            while (n != null) {
                table.delete(n.data, session);

                count++;

                n = n.next;
            }
        }

        table.fireAll(TriggerDef.DELETE_AFTER);

        Result r = new Result();

        r.iUpdateCount = count;

        return r;
    }
}
