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
import java.lang.reflect.Method;
import java.util.Hashtable;

/**
 * Provides services to evaluate Java methods in the context of
 * SQL function and stored procedure calls.
 *
 * @version 1.7.0
 */

// fredt@users 20020912 - patch 1.7.1 - shortcut treatment of identity() call
// fredt@users 20020912 - patch 1.7.1 - cache java.lang.reflect.Method objects
class Function {

    private Session          cSession;
    private String           sFunction;
    private Method           mMethod;
    private int              iReturnType;
    private int              iArgCount;
    private int              iArgType[];
    private boolean          bArgNullable[];
    private Object           oArg[];
    private Expression       eArg[];
    private boolean          bConnection;
    private boolean          isIdentityFunction;
    private static Hashtable methodCache = new Hashtable();

    /**
     * Constructs a new Function object with the given function call name
     * and using the specified Session context. <p>
     *
     * The call name is the fully qualified name of a Java method, as
     * opposed to the method's canonical signature.  That is, the name
     * is of the form "package.class.method."  This implies that Java
     * methods with the same fully qualified name but different signatures
     * cannot be used properly as HSQLDB SQL functions or stored procedures.
     * For instance, it is impossible to call both System.getProperty(String)
     * and System.getProperty(String,String) under this arrangement, because
     * the HSQLDB Function object is unable to differentiate between the two;
     * it simply chooses the first method matching the FQN in the array of
     * methods obtained from calling getMethods() on an instance of the
     * Class indicated in the FQN, hiding all other methods with the same
     * FQN. <p>
     *
     * The function FQN must match at least one Java method FQN in the
     * specified class or construction cannot procede and a SQLException is
     * thrown. <p>
     *
     * The Session paramter is the connected context in which this
     * Function object will evaluate.  If it is determined that the
     * connected user does not have the right to evaluate this Function,
     * construction cannot proceed and a SQLException is thrown.
     *
     *
     * @param function the fully qualified name of a Java method
     * @param session the connected context in which this Function object will
     *                evaluate
     * @throws SQLException if the specified function FQN corresponds to no
     *                      Java method or the session user at the time of
     *                      construction does not have the right to evaluate
     *                      this Function.
     */
    Function(String function, Session session) throws SQLException {

        cSession  = session;
        sFunction = function;

        if (function.equals("org.hsqldb.Library.identity")) {
            isIdentityFunction = true;
        }

        int i = function.lastIndexOf('.');

        Trace.check(i != -1, Trace.UNEXPECTED_TOKEN, function);

        String classname = function.substring(0, i);

        session.check("CLASS \"" + classname + "\"", UserManager.ALL);

        mMethod = (Method) methodCache.get(function);

        if (mMethod == null) {
            String methodname    = function.substring(i + 1);
            Class  classinstance = null;

            try {
                classinstance = Class.forName(classname);
            } catch (Exception e) {
                throw Trace.error(Trace.ERROR_IN_FUNCTION,
                                  classname + " " + e);
            }

            Method method[] = classinstance.getMethods();

            for (i = 0; i < method.length; i++) {
                Method m = method[i];

                if (m.getName().equals(methodname)) {
                    Trace.check(mMethod == null, Trace.UNKNOWN_FUNCTION,
                                methodname);

                    mMethod = m;
                }
            }

            Trace.check(mMethod != null, Trace.UNKNOWN_FUNCTION, methodname);
            methodCache.put(function, mMethod);
        }

        Class returnclass = mMethod.getReturnType();

        iReturnType = Column.getTypeNr(returnclass.getName());

        Class arg[] = mMethod.getParameterTypes();

        iArgCount    = arg.length;
        iArgType     = new int[iArgCount];
        bArgNullable = new boolean[iArgCount];

        for (i = 0; i < arg.length; i++) {
            Class  a    = arg[i];
            String type = a.getName();

            if ((i == 0) && type.equals("java.sql.Connection")) {

                // only the first parameter can be a Connection
                bConnection = true;
            } else {

// fredt@users - byte[] is now supported directly as "[B"
//                if (type.equals("[B")) {
//                    type = "byte[]";
//                }
                iArgType[i]     = Column.getTypeNr(type);
                bArgNullable[i] = !a.isPrimitive();
            }
        }

        eArg = new Expression[iArgCount];
        oArg = new Object[iArgCount];
    }

    /**
     * Retrieves the value this Function evaluates to, given the current
     * state of this object's {@link #resolve(TableFilter) resolved}
     * TableFilter, if any, and any mapping of expressions to this
     * Function's parameter list that has been performed via
     * {link #setArgument(int,Expression) setArgument}.
     *
     *
     * @return the value resulting from evaluating this Function
     * @throws SQLException if an invocation exception is encountered when
     * calling the Java
     * method underlying this object
     */
    Object getValue() throws SQLException {

        int i = 0;

        if (isIdentityFunction) {
            return new Integer(cSession.getLastIdentity());
        }

        if (bConnection) {
            oArg[i] = cSession.getInternalConnection();

            i++;
        }

        for (; i < iArgCount; i++) {
            Expression e = eArg[i];
            Object     o = null;

            if (e != null) {

                // no argument: null
                o = e.getValue(iArgType[i]);
            }

            if ((o == null) &&!bArgNullable[i]) {

                // null argument for primitive datatype: don't call
                return null;
            }

            oArg[i] = o;
        }

        try {
            return mMethod.invoke(null, oArg);
        } catch (Exception e) {
            String s = sFunction + ": " + e.toString();

            throw Trace.getError(Trace.FUNCTION_NOT_SUPPORTED, s);
        }
    }

    /**
     * Retrieves the number of parameters that must be supplied to evaluate
     * this Function object from SQL.  <p>
     *
     * This value may be different than the number of parameters of the
     * underlying Java method.  This is because HSQLDB automatically detects
     * if the first parameter is of type java.sql.Connection, and supplies a
     * live Connection object constructed from the evaluating session context
     * if so.
     *
     *
     * @return the number of arguments this Function takes, as known to the
     * calling SQL context
     */
    int getArgCount() {
        return iArgCount - (bConnection ? 1
                                        : 0);
    }

    /**
     * Resolves the arguments supplied to this Function object against the
     * specified TableFilter.
     *
     *
     * @param f the TableFilter against which to resolve this Function
     * object's arguments
     * @throws SQLException if there is a problem resolving an argument
     * against the specified TableFilter
     */
    void resolve(TableFilter f) throws SQLException {

        for (int i = 0; i < iArgCount; i++) {
            if (eArg[i] != null) {
                eArg[i].resolve(f);
            }
        }
    }

    /**
     * Checks each of this object's arguments for resolution, throwing a
     * SQLException if any arguments have not yet been resolved.
     *
     *
     * @throws SQLException if any arguments have not yet been resolved
     */
    void checkResolved() throws SQLException {

        for (int i = 0; i < iArgCount; i++) {
            if (eArg[i] != null) {
                eArg[i].checkResolved();
            }
        }
    }

    /**
     * Retrieves the java.sql.Types type of the argument at the specified
     * offset in this Function object's paramter list
     *
     *
     * @param i the offset of the desired argument in this Function object's
     * paramter list
     * @return the specified argument's java.sql.Types type
     */
    int getArgType(int i) {
        return iArgType[i];
    }

    /**
     * Retrieves the java.sql.Types type of this Function
     * object's return type
     *
     *
     * @return this Function object's java.sql.Types return type
     */
    int getReturnType() {
        return iReturnType;
    }

    /**
     * Binds the specified expression to the specified argument in this
     * Function object's paramter list.
     *
     *
     * @param i the position of the agument to bind to
     * @param e the expression to bind
     */
    void setArgument(int i, Expression e) {

        if (bConnection) {
            i++;
        }

        eArg[i] = e;
    }
}
