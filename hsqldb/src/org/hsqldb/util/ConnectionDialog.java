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


package org.hsqldb.util;

import java.awt.*;
import java.awt.event.*;
import java.sql.*;

// sqlbob@users 20020325 - patch 1.7.0 - enhancements
// sqlbob@users 20020407 - patch 1.7.0 - reengineering

/**
 * Opens a connection to a database
 *
 * @version 1.7.0
 */
public class ConnectionDialog extends Dialog
implements ActionListener, ItemListener {

    private Connection mConnection;
    private TextField  mDriver, mURL, mUser, mPassword;
    private Label      mError;
    private String     connTypes[][];

    /**
     * Method declaration
     *
     *
     * @param driver
     * @param url
     * @param user
     * @param password
     *
     * @return
     *
     * @throws Exception
     */
    public static Connection createConnection(String driver, String url,
            String user, String password) throws Exception {

        Class.forName(driver).newInstance();

        return DriverManager.getConnection(url, user, password);
    }

    /**
     * Constructor declaration
     *
     *
     * @param owner
     * @param title
     */
    ConnectionDialog(Frame owner, String title) {
        super(owner, title, true);
    }

    /**
     * Method declaration
     *
     */
    private void create() {

        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();

        setLayout(new BorderLayout());

        Panel p = new Panel(new BorderLayout());
        Panel pLabel;
        Panel pText;
        Panel pButton;

        // (ulrivo): full size on screen with less than 640 width
        if (d.width >= 640) {
            pLabel  = new Panel(new GridLayout(6, 1, 10, 10));
            pText   = new Panel(new GridLayout(6, 1, 10, 10));
            pButton = new Panel(new GridLayout(1, 2, 10, 10));
        } else {
            pLabel  = new Panel(new GridLayout(6, 1));
            pText   = new Panel(new GridLayout(6, 1));
            pButton = new Panel(new GridLayout(1, 2));
        }

        p.add("West", pLabel);
        p.add("Center", pText);
        p.add("South", pButton);
        p.add("North", createLabel(""));
        p.add("East", createLabel(""));
        p.setBackground(SystemColor.control);
        pText.setBackground(SystemColor.control);
        pLabel.setBackground(SystemColor.control);
        pButton.setBackground(SystemColor.control);
        pLabel.add(createLabel("Type:"));

        Choice types = new Choice();

        connTypes = ConnectionDialogCommon.getTypes();

        for (int i = 0; i < connTypes.length; i++) {
            types.add(connTypes[i][0]);
        }

        types.addItemListener(this);
        pText.add(types);
        pLabel.add(createLabel("Driver:"));

        mDriver = new TextField(connTypes[0][1]);

        pText.add(mDriver);
        pLabel.add(createLabel("URL:"));

        mURL = new TextField(connTypes[0][2]);

        mURL.addActionListener(this);
        pText.add(mURL);
        pLabel.add(createLabel("User:"));

        mUser = new TextField("sa");

        mUser.addActionListener(this);
        pText.add(mUser);
        pLabel.add(createLabel("Password:"));

        mPassword = new TextField("");

        mPassword.addActionListener(this);
        mPassword.setEchoChar('*');
        pText.add(mPassword);

        Button b;

        b = new Button("Ok");

        b.setActionCommand("ConnectOk");
        b.addActionListener(this);
        pButton.add(b);

        b = new Button("Cancel");

        b.setActionCommand("ConnectCancel");
        b.addActionListener(this);
        pButton.add(b);
        add("East", createLabel(""));
        add("West", createLabel(""));

        mError = new Label("");

        Panel pMessage = createBorderPanel(mError);

        add("South", pMessage);
        add("North", createLabel(""));
        add("Center", p);
        doLayout();
        pack();

        Dimension size = getSize();

        // (ulrivo): full size on screen with less than 640 width
        if (d.width >= 640) {
            setLocation((d.width - size.width) / 2,
                        (d.height - size.height) / 2);
        } else {
            setLocation(0, 0);
            setSize(d);
        }

        show();
    }

    /**
     * Method declaration
     *
     *
     * @param owner
     * @param title
     *
     * @return
     */
    public static Connection createConnection(Frame owner, String title) {

        ConnectionDialog dialog = new ConnectionDialog(owner, title);

        dialog.create();

        return dialog.mConnection;
    }

    /**
     * Method declaration
     *
     *
     * @param s
     *
     * @return
     */
    private static Label createLabel(String s) {

        Label l = new Label(s);

        l.setBackground(SystemColor.control);

        return l;
    }

    /**
     * Method declaration
     *
     *
     * @param center
     *
     * @return
     */
    private static Panel createBorderPanel(Component center) {

        Panel p = new Panel();

        p.setBackground(SystemColor.control);
        p.setLayout(new BorderLayout());
        p.add("Center", center);
        p.add("North", createLabel(""));
        p.add("South", createLabel(""));
        p.add("East", createLabel(""));
        p.add("West", createLabel(""));
        p.setBackground(SystemColor.control);

        return p;
    }

    /**
     * Method declaration
     *
     *
     * @param ev
     */
    public void actionPerformed(ActionEvent ev) {

        String s = ev.getActionCommand();

        if (s.equals("ConnectOk") || (ev.getSource() instanceof TextField)) {
            try {
                mConnection = createConnection(mDriver.getText(),
                                               mURL.getText(),
                                               mUser.getText(),
                                               mPassword.getText());

                dispose();
            } catch (Exception e) {
                e.printStackTrace();
                mError.setText(e.toString());
            }
        } else if (s.equals("ConnectCancel")) {
            dispose();
        }
    }

    /**
     * Method declaration
     *
     *
     * @param e
     */
    public void itemStateChanged(ItemEvent e) {

        String s = (String) e.getItem();

        for (int i = 0; i < connTypes.length; i++) {
            if (s.equals(connTypes[i][0])) {
                mDriver.setText(connTypes[i][1]);
                mURL.setText(connTypes[i][2]);
            }
        }
    }
}
