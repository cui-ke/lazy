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

// fredt@users 20020320 - doc 1.7.0 - update

/**
 *  The collection (Vector) of User object instances within a specific
 *  database. Methods are provided for creating, modifying and deleting
 *  users, as well as manipulating their access rights to the database
 *  objects.
 *
 * @version  1.7.0
 * @see  User
 */
class UserManager {

    static final int SELECT = 1,
                     DELETE = 2,
                     INSERT = 4,
                     UPDATE = 8,
                     ALL    = 15;
    private Vector   uUser;
    private User     uPublic;

    /**
     *  Creates a new Vector to contain the User object instances, as well
     *  as creating an initial PUBLIC user, with no password.
     *
     * @throws  SQLException
     */
    UserManager() throws SQLException {
        uUser   = new Vector();
        uPublic = createUser("PUBLIC", null, false);
    }

    /**
     *  Returns int value for the string argument
     *
     * @param  right one of ALL SELECT UPDATE DELETE INSERT
     * @return  A static int representing the String right passed in.
     * @throws  SQLException
     */
    static int getRight(String right) throws SQLException {

        if (right.equals("ALL")) {
            return ALL;
        } else if (right.equals("SELECT")) {
            return SELECT;
        } else if (right.equals("UPDATE")) {
            return UPDATE;
        } else if (right.equals("DELETE")) {
            return DELETE;
        } else if (right.equals("INSERT")) {
            return INSERT;
        }

        throw Trace.error(Trace.UNEXPECTED_TOKEN, right);
    }

    /**
     * Returns comma separated list of String arguments based on int mask
     * @param  right Description of the Parameter
     * @return  A String representation of the right or rights associated
     *      with the argument.
     */
    static String getRight(int right) {

        if (right == ALL) {
            return "ALL";
        } else if (right == 0) {
            return null;
        }

        StringBuffer b = new StringBuffer();

        if ((right & SELECT) != 0) {
            b.append("SELECT,");
        }

        if ((right & UPDATE) != 0) {
            b.append("UPDATE,");
        }

        if ((right & DELETE) != 0) {
            b.append("DELETE,");
        }

        if ((right & INSERT) != 0) {
            b.append("INSERT,");
        }

        b.setLength(b.length() - 1);

        return b.toString();
    }

    /**
     *  This method is used to create a new user. The collection of users is
     *  first checked for a duplicate name, and an exception will be thrown
     *  if a user of the same name already exists.
     *
     * @param  name (User login)
     * @param  password (Plaintext password)
     * @param  admin (Is this a database admin user?)
     * @return  An instance of the newly created User object
     * @throws  SQLException
     */
    User createUser(String name, String password,
                    boolean admin) throws SQLException {

// fredt@users 20020130 - patch 497872 by Nitin Chauhan
// changes to loops for speed, also applied to similar loops below
        for (int i = 0, uSize = uUser.size(); i < uSize; i++) {
            User u = (User) uUser.elementAt(i);

            if ((u != null) && u.getName().equals(name)) {
                throw Trace.error(Trace.USER_ALREADY_EXISTS, name);
            }
        }

        User u = new User(name, password, admin, uPublic);

        uUser.addElement(u);

        return u;
    }

    /**
     *  This method is used to drop a user. Since we are using a vector to
     *  hold the User objects, we must iterate through the Vector looking
     *  for the name. The user object is currently set to null, and all
     *  access rights revoked. <P>
     *
     *  <B>Note:</B> An ACCESS_IS_DENIED exception will be thrown if an
     *  attempt is made to drop the PUBLIC user.
     *
     * @param  name of the user to be dropped
     * @throws  SQLException
     */
    void dropUser(String name) throws SQLException {

        Trace.check(!name.equals("PUBLIC"), Trace.ACCESS_IS_DENIED);

        for (int i = 0, uSize = uUser.size(); i < uSize; i++) {
            User u = (User) uUser.elementAt(i);

            if ((u != null) && u.getName().equals(name)) {

                // todo: find a better way. Problem: removeElementAt would not
                // work correctly while others are connected
                uUser.setElementAt(null, i);
                u.revokeAll();    // in case the user is referenced in another way

                return;
            }
        }

        throw Trace.error(Trace.USER_NOT_FOUND, name);
    }

    /**
     *  This method is used to return an instance of a particular User
     *  object, given the user name and password. <P>
     *
     *  <B>Note:</B> An ACCESS_IS_DENIED exception will be thrown if an
     *  attempt is made to get the PUBLIC user.
     *
     * @param  name Description of the Parameter
     * @param  password Description of the Parameter
     * @return  The requested User object
     * @throws  SQLException
     */
    User getUser(String name, String password) throws SQLException {

        Trace.check(!name.equals("PUBLIC"), Trace.ACCESS_IS_DENIED);

        if (name == null) {
            name = "";
        }

        if (password == null) {
            password = "";
        }

        User u = get(name);

        u.checkPassword(password);

        return u;
    }

    /**
     *  This method is used to access the entire Vector of User objects for
     *  this database.
     *
     * @return  The Vector of our User objects
     */
    Vector getUsers() {
        return uUser;
    }

    /**
     *  This method is used to grant a user rights to database objects.
     *
     * @param  name of the user
     * @param  object in the database
     * @param  right to grant to the user
     * @throws  SQLException
     */
    void grant(String name, String object, int right) throws SQLException {
        get(name).grant(object, right);
    }

    /**
     *  This method is used to revoke a user's rights to database objects.
     *
     * @param  name of the user
     * @param  object in the database
     * @param  right to grant to the user
     * @throws  SQLException
     */
    void revoke(String name, String object, int right) throws SQLException {
        get(name).revoke(object, right);
    }

    /**
     *  This private method is used to access the User objects in the
     *  collection and perform operations on them.
     *
     * @param  name Description of the Parameter
     * @return  Description of the Return Value
     * @exception  SQLException Description of the Exception
     */
    private User get(String name) throws SQLException {

        for (int i = 0, uSize = uUser.size(); i < uSize; i++) {
            User u = (User) uUser.elementAt(i);

            if ((u != null) && u.getName().equals(name)) {
                return u;
            }
        }

        throw Trace.error(Trace.USER_NOT_FOUND, name);
    }
}
