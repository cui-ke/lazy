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

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Observable;
import java.util.Observer;
import java.util.Vector;

/**
 * Server acts as a database server and is one way of using
 * the client-server mode of HSQL Database Engine. This server
 * can only process database queries.<p>
 * An applet or application will use only the JDBC classes to access
 * the database.<p>
 *
 * The Server can be configured with the file 'server.properties'.
 * This is an example of the file:
 * <pre>
 * server.port=9001<p>
 * server.database=test<p>
 * server.silent=true<p>
 * </pre>
 *
 *  If the server is embedded in an application server, such as when
 *  DataSource or HsqlServerFactory classes are used, it is necessary
 *  to avoid calling System.exit() when the HSQLDB is shutdown with
 *  an SQL command.<br>
 *  For this, the server.no_system_exit property can be
 *  set either on the command line or in server.properties file.
 *  This ensures that System.exit() is not called at the end.
 *  All that is left for the embedder application server is to release
 *  the "empty" Server object and create another one to reopen the
 *  database (fredt@users).

 * @version 1.7.0
 */

// fredt@users 20020215 - patch 1.7.0 by fredt
// method rorganised to use new HsqlServerProperties class
// fredt@users 20020424 - patch 1.7.0 by fredt - shutdown without exit
// see the comments in ServerConnection.java
public class Server implements Observer {

    // used to notify this
    static final Integer CONNECTION_CLOSED = new Integer(0);
    Vector               serverConnList    = new Vector(16);
    Database             mDatabase;
    HsqlServerProperties serverProperties;
    private ServerSocket socket;
    private Thread       thread;
    private boolean      traceMessages;
    private boolean      restartOnShutdown;
    private boolean      noSystemExit;

    /**
     * Method declaration
     *
     *
     * @param arg
     */
    public static void main(String arg[]) {

        if (arg.length > 0) {
            String p = arg[0];

            if ((p != null) && p.startsWith("-?")) {
                printHelp();

                return;
            }
        }

        HsqlProperties props  = HsqlProperties.argArrayToProps(arg, "server");
        Server         server = new Server();

        server.setProperties(props);
        server.run();
    }

    void setProperties(HsqlProperties props) {

        serverProperties = new HsqlServerProperties("server");

        serverProperties.load();
        serverProperties.addProperties(props);
        serverProperties.setPropertyIfNotExists("server.database", "test");
        serverProperties.setPropertyIfNotExists("server.port",
                String.valueOf(jdbcConnection.DEFAULT_HSQLDB_PORT));

        if (serverProperties.isPropertyTrue("server.trace")) {
            jdbcSystem.setLogToSystem(true);
        }

        traceMessages = !serverProperties.isPropertyTrue("server.silent",
                true);
        noSystemExit =
            serverProperties.isPropertyTrue("server.no_system_exit");

        // fredt - not used yet
        restartOnShutdown =
            serverProperties.isPropertyTrue("server.restart_on_shutdown");
    }

    void openDB() throws SQLException {

        String database = serverProperties.getProperty("server.database");

        mDatabase = new Database(database);
    }

    /**
     * Method declaration
     *
     *
     * @param arg
     */
    private void run() {

        try {
            int port = serverProperties.getIntegerProperty("server.port",
                jdbcConnection.DEFAULT_HSQLDB_PORT);
            String database = serverProperties.getProperty("server.database");

            Trace.printSystemOut("Opening database: " + database);
            printTraceMessages();
            openDB();

            socket = new ServerSocket(port);

            Trace.printSystemOut(
                new java.util.Date(System.currentTimeMillis())
                + " Listening for connections ...");
        } catch (Exception e) {
            traceError("Server.run/init: " + e);
            e.printStackTrace();

            return;
        }

        try {
            while (true) {
                Socket           s = socket.accept();
                ServerConnection c = new ServerConnection(s, this);

                thread = new Thread(c);

                thread.start();
            }
        } catch (IOException e) {
            if (mDatabase != null) {
                traceError("Server.run/loop: " + e.getMessage());
                e.printStackTrace();
            } else {
                trace("");
            }
        }
    }

    static void printHelp() {

        Trace.printSystemOut(
            "Usage: java Server [-options]\n" + "where options include:\n"
            + "    -port <nr>            port where the server is listening\n"
            + "    -database <name>      name of the database\n"
            + "    -silent <true/false>  false means display all queries\n"
            + "    -trace <true/false>   display JDBC trace messages\n"
            + "    -no_system_exit <true/false>  do not issue System.exit()\n"
            + "The command line arguments override the values in the server.properties file.");
        System.exit(0);
    }

    void printTraceMessages() {

        trace("server.port    ="
              + serverProperties.getProperty("server.port"));
        trace("server.database="
              + serverProperties.getProperty("server.database"));
        trace("server.silent  ="
              + serverProperties.getProperty("server.silent"));
        Trace.printSystemOut("HSQLDB server " + jdbcDriver.VERSION
                             + " is running");
        Trace.printSystemOut(
            "Use SHUTDOWN to close normally. Use [Ctrl]+[C] to abort abruptly");
    }

    /**
     * Method declaration
     *
     *
     * @param s
     */
    void trace(String s) {

        if (traceMessages) {
            Trace.printSystemOut(s);
        }
    }

    /**
     * Method declaration
     *
     *
     * @param s
     */
    void traceError(String s) {
        Trace.printSystemOut(s);
    }

    void closeAllServerConnections() {

        for (int i = serverConnList.size() - 1; i >= 0; i--) {
            ServerConnection sc =
                (ServerConnection) serverConnList.elementAt(i);

            sc.close();
        }

        //fredt@users - when we implement restart on shutdown add resize to 16
        serverConnList.removeAllElements();
    }

    public void update(Observable o, Object arg) {

//      Trace.doAssert(arg.equals(ServerConnection.CONNECTION_CLOSED));
        if (mDatabase == null) {
            return;
        }

        if (!mDatabase.isShutdown()) {
            return;
        }

        mDatabase = null;

        closeAllServerConnections();

        // fredt@users - this is used to exit the loop in this.run()
        try {
            socket.close();
        } catch (IOException e) {
            traceError("Exception when closing the main socket");
        }

        serverProperties = null;
        socket           = null;
        thread           = null;

        if (!noSystemExit) {
            Trace.printSystemOut(
                new java.util.Date(System.currentTimeMillis())
                + " SHUTDOWN : System.exit() is called next");
            System.exit(0);
        }

        Trace.printSystemOut(new java.util.Date(System.currentTimeMillis())
                             + " SHUTDOWN : System.exit() was not called");
    }
}
