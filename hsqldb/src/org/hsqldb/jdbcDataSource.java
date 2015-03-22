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

import java.io.Serializable;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.sql.Connection;
import java.util.Properties;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.Referenceable;
import javax.naming.StringRefAddr;
import javax.sql.DataSource;

// fredt@users 20020130 - patch 416437 by deforest@users - jdbc DataSource
public class jdbcDataSource
implements Serializable, Referenceable, DataSource {

    /**
     * Login timeout
     */
    private int loginTimeout = 0;

    /**
     * Log writer
     */
    private transient PrintWriter logWriter;

    /**
     * Default password to use for connections
     */
    private String password = "";

    /**
     * Default user to use for connections
     */
    private String user = "";

    /**
     * Signature
     */
    private static final String sStartURL = "jdbc:hsqldb:";

    /**
     * Database location
     */
    private String database = "";

    /**
     * Constructor
     */
    public jdbcDataSource() {}

    /**
     * Forward with current user/password
     */
    public Connection getConnection() throws java.sql.SQLException {
        return getConnection(user, password);
    }

    /**
     * getConnection method comment.
     */
    public Connection getConnection(String user,
                                    String password) throws SQLException {

        Properties props = new Properties();

        if (user != null) {
            props.put("user", user);
        }

        if (password != null) {
            props.put("password", password);
        }

        return new jdbcConnection(database, props);
    }

    /**
     * Return database
     */
    public String getDatabase() {
        return database;
    }

    /**
     * getLoginTimeout method comment.
     */
    public int getLoginTimeout() throws java.sql.SQLException {
        return loginTimeout;
    }

    /**
     * getLogWriter method comment.
     */
    public java.io.PrintWriter getLogWriter() throws java.sql.SQLException {
        return null;
    }

    /**
     * getReference method comment.
     */
    public Reference getReference() throws NamingException {

        String    cname = "org.hsqldb.jdbcDataSourceFactory";
        Reference ref   = new Reference(getClass().getName(), cname, null);

        ref.add(new StringRefAddr("database", getDatabase()));
        ref.add(new StringRefAddr("user", getUser()));
        ref.add(new StringRefAddr("password", password));

        return ref;
    }

    /**
     * @return user ID for the connection
     */
    public String getUser() {
        return user;
    }

    /**
     * Set database location
     */
    public void setDatabase(String database) {
        this.database = database;
    }

    /**
     * Not yet implemented
     */
    public void setLoginTimeout(int ignore) throws java.sql.SQLException {
        this.loginTimeout = ignore;
    }

    /**
     * setLogWriter method comment.
     */
    public void setLogWriter(PrintWriter logWriter)
    throws java.sql.SQLException {
        this.logWriter = logWriter;
    }

    /**
     * Sets the password to use for connecting to the database
     * @param password the password
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Sets the userid
     * @param user the user id
     */
    public void setUser(String user) {
        this.user = user;
    }
}
