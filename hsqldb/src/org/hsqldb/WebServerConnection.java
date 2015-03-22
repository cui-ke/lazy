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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.sql.SQLException;
import java.util.StringTokenizer;

/**
 *  Class declaration
 *
 * @version  1.7.0
 */
class WebServerConnection extends Thread {

    static final String      ENCODING = "8859_1";
    private Socket           mSocket;
    private WebServer        mServer;
    private static final int GET         = 1,
                             HEAD        = 2,
                             POST        = 3,
                             BAD_REQUEST = 400,
                             FORBIDDEN   = 403,
                             NOT_FOUND   = 404;

    /**
     *  Constructor declaration
     *
     * @param  socket
     * @param  server
     */
    WebServerConnection(Socket socket, WebServer server) {
        mServer = server;
        mSocket = socket;
    }

    /**
     *  Method declaration
     */
    public void run() {

        try {
            BufferedReader input = new BufferedReader(
                new InputStreamReader(mSocket.getInputStream(), ENCODING));
            String request;
            String name   = null;
            int    method = BAD_REQUEST;
            int    len    = -1;

            while (true) {
                request = input.readLine();

                if (request == null) {
                    break;
                }

                StringTokenizer tokenizer = new StringTokenizer(request, " ");

                if (!tokenizer.hasMoreTokens()) {
                    break;
                }

                String first = tokenizer.nextToken();

                if (first.equals("GET")) {
                    method = GET;
                    name   = tokenizer.nextToken();
                } else if (first.equals("HEAD")) {
                    method = HEAD;
                    name   = tokenizer.nextToken();
                } else if (first.equals("POST")) {
                    method = POST;
                    name   = tokenizer.nextToken();
                } else if (request.toUpperCase().startsWith(
                        "CONTENT-LENGTH:")) {
                    len = Integer.parseInt(tokenizer.nextToken());
                }
            }

            switch (method) {

                case BAD_REQUEST :
                    processError(BAD_REQUEST);
                    break;

                case GET :
                    processGet(name, true);
                    break;

                case HEAD :
                    processGet(name, false);
                    break;

                case POST :
                    processPost(input, name, len);
                    break;
            }

            input.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     *  Method declaration
     *
     * @param  name
     * @param  send
     */
    private void processGet(String name, boolean send) {

        try {
            if (name.endsWith("/")) {
                name = name + mServer.mDefaultFile;
            }

            if (name.indexOf("..") != -1) {
                processError(FORBIDDEN);

                return;
            }

            name = mServer.mRoot + name;

            if (mServer.mPathSeparatorChar != '/') {
                name = name.replace('/', mServer.mPathSeparatorChar);
            }

            String mime = null;
            int    i    = name.lastIndexOf(".");

            if (i != -1) {
                String ending = name.substring(i).toLowerCase();

                mime = mServer.serverProperties.getProperty(ending);
            }

            if (mime == null) {
                mime = "text/html";
            }

            BufferedInputStream file = null;
            String              header;

            try {
                file = new BufferedInputStream(
                    new FileInputStream(new File(name)));

                int len = file.available();

                header = getHead("HTTP/1.0 200 OK",
                                 "Content-Type: " + mime + "\n"
                                 + "Content-Length: " + len);
            } catch (IOException e) {
                processError(NOT_FOUND);

                return;
            }

            DataOutputStream output = new DataOutputStream(
                new BufferedOutputStream(mSocket.getOutputStream()));

            output.write(header.getBytes(ENCODING));

            if (send) {
                int b;

                while (true) {
                    b = file.read();

                    if (b == -1) {
                        break;
                    }

                    output.writeByte(b);
                }
            }

            output.flush();
            output.close();
        } catch (Exception e) {
            mServer.traceError("processGet: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     *  Method declaration
     *
     * @param  start
     * @param  end
     * @return
     */
    private String getHead(String start, String end) {
        return start + "\nAllow: GET, HEAD, POST\nMIME-Version: 1.0\n"
               + "Server: " + mServer.mServerName + "\n" + end + "\n\n";
    }

    /**
     *  Method declaration
     *
     * @param  input
     * @param  name
     * @param  len
     */
    private void processPost(BufferedReader input, String name,
                             int len) throws SQLException {

        if (len < 0) {
            processError(BAD_REQUEST);

            return;
        }

        char b[] = new char[len];

        try {
            input.read(b, 0, len);
        } catch (IOException e) {
            processError(BAD_REQUEST);

            return;
        }

        String s = new String(b);
        int    p = s.indexOf('+');
        int    q = s.indexOf('+', p + 1);

        if ((p == -1) || (q == -1)) {
            processError(BAD_REQUEST);

            return;
        }

        String user = s.substring(0, p);

        user = StringConverter.hexStringToUnicode(user);

        String password = s.substring(p + 1, q);

        password = StringConverter.hexStringToUnicode(password);
        s        = s.substring(q + 1);
        s        = StringConverter.hexStringToUnicode(s);

        processQuery(user, password, s);
    }

    /**
     *  Method declaration
     *
     * @param  code
     */
    private void processError(int code) {

        mServer.trace("processError " + code);

        String message = null;

        switch (code) {

            case BAD_REQUEST :
                message = getHead("HTTP/1.0 400 Bad Request", "")
                          + "<HTML><BODY><H1>Bad Request</H1>"
                          + "The server could not understand this request."
                          + "<P></BODY></HTML>";
                break;

            case NOT_FOUND :
                message =
                    getHead("HTTP/1.0 404 Not Found", "")
                    + "<HTML><BODY><H1>Not Found</H1>"
                    + "The server could not find this file.<P></BODY></HTML>";
                break;

            case FORBIDDEN :
                message = getHead("HTTP/1.0 403 Forbidden", "")
                          + "<HTML><BODY><H1>Forbidden</H1>"
                          + "Access is not allowed.<P></BODY></HTML>";
                break;
        }

        try {
            DataOutputStream output = new DataOutputStream(
                new BufferedOutputStream(mSocket.getOutputStream()));

            output.write(message.getBytes(ENCODING));
            output.flush();
            output.close();
        } catch (Exception e) {
            mServer.traceError("processError: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     *  Method declaration
     *
     * @param  user
     * @param  password
     * @param  statement
     */
    private void processQuery(String user, String password,
                              String statement) {

        try {
            mServer.trace(statement);

            byte result[] = mServer.mDatabase.execute(user, password,
                statement);
            int len = result.length;
            String header = getHead("HTTP/1.0 200 OK",
                                    "Content-Type: application/octet-stream\n"
                                    + "Content-Length: " + len);
            DataOutputStream output = new DataOutputStream(
                new BufferedOutputStream(mSocket.getOutputStream()));

            output.write(header.getBytes(ENCODING));
            output.write(result);
            output.flush();
            output.close();
        } catch (Exception e) {
            mServer.traceError("processQuery: " + e.getMessage());
            e.printStackTrace();
        }

        // System.out.print("Queries processed: "+(iQueries++)+"  \n");
        if (mServer.mDatabase.isShutdown()) {
            mServer.trace("The database is shutdown");
            System.exit(0);
        }
    }

    // static private int iQueries=0;
}
