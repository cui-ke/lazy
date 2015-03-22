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
import javax.swing.*;
import javax.swing.border.*;

// sqlbob@users 20020325 - patch 1.7.0 - enhancements
// sqlbob@users 20020407 - patch 1.7.0 - reengineering

/**
 * Opens a connection to a database
 *
 * @version 1.7.0
 */
public class ConnectionDialogSwing extends JDialog
implements ActionListener, ItemListener {

    private Connection     mConnection;
    private JTextField     mDriver, mURL, mUser, mError;
    private JPasswordField mPassword;
    private String         connTypes[][];

    public static Connection createConnection(String driver, String url,
            String user, String password) throws Exception {

        Class.forName(driver).newInstance();

        return DriverManager.getConnection(url, user, password);
    }

    ConnectionDialogSwing(JFrame owner, String title) {
        super(owner, title, true);
    }

    private void create() {

        CommonSwing.setDefaultColor();

        JButton b;
        Box     main     = Box.createHorizontalBox();
        Box     labels   = Box.createVerticalBox();
        Box     controls = Box.createVerticalBox();
        Box     buttons  = Box.createHorizontalBox();
        Box     whole    = Box.createVerticalBox();
        Box     status   = Box.createHorizontalBox();

        main.add(Box.createHorizontalGlue());
        main.add(labels);
        main.add(Box.createHorizontalStrut(10));
        main.add(Box.createHorizontalGlue());
        main.add(controls);
        main.add(Box.createHorizontalGlue());
        whole.add(Box.createVerticalGlue());
        whole.add(Box.createVerticalStrut(10));
        whole.add(main);
        whole.add(Box.createVerticalGlue());
        whole.add(Box.createVerticalStrut(10));
        whole.add(buttons);
        whole.add(Box.createVerticalGlue());
        whole.add(Box.createVerticalStrut(10));
        whole.add(status);
        whole.add(Box.createVerticalStrut(10));
        whole.add(Box.createVerticalGlue());
        labels.add(Box.createVerticalGlue());
        labels.add(createLabel("Type:"));
        labels.add(Box.createVerticalGlue());
        labels.add(createLabel("Driver"));
        labels.add(Box.createVerticalGlue());
        labels.add(createLabel("URL"));
        labels.add(Box.createVerticalGlue());
        labels.add(createLabel("User:"));
        labels.add(Box.createVerticalGlue());
        labels.add(createLabel("Password:"));
        labels.add(Box.createVerticalGlue());
        labels.add(Box.createVerticalStrut(10));

        // Now the 2nd column which is the controls box:
        controls.add(Box.createVerticalGlue());

        JComboBox types = new JComboBox();

        connTypes = ConnectionDialogCommon.getTypes();

        for (int i = 0; i < connTypes.length; i++) {
            types.addItem(connTypes[i][0]);
        }

        types.addItemListener(this);
        controls.add(types);
        controls.add(Box.createVerticalGlue());

        mDriver = new JTextField(connTypes[0][1]);

        mDriver.addActionListener(this);
        controls.add(mDriver);

        mURL = new JTextField(connTypes[0][2]);

        mURL.addActionListener(this);
        controls.add(mURL);
        controls.add(Box.createVerticalGlue());

        mUser = new JTextField("sa");

        mUser.addActionListener(this);
        controls.add(mUser);
        controls.add(Box.createVerticalGlue());

        mPassword = new JPasswordField("");

        mPassword.addActionListener(this);
        controls.add(mPassword);
        controls.add(Box.createVerticalGlue());
        controls.add(Box.createVerticalStrut(10));

        // The button bar
        buttons.add(Box.createHorizontalGlue());
        buttons.add(Box.createHorizontalStrut(10));

        b = new JButton("Ok");

        b.setActionCommand("ConnectOk");
        b.addActionListener(this);
        buttons.add(b);
        getRootPane().setDefaultButton(b);
        buttons.add(Box.createHorizontalGlue());
        buttons.add(Box.createHorizontalStrut(20));

        b = new JButton("Cancel");

        b.setActionCommand("ConnectCancel");
        b.addActionListener(this);
        buttons.add(b);
        buttons.add(Box.createHorizontalGlue());
        buttons.add(Box.createHorizontalStrut(10));

        // Now the status line
        mError = new JTextField("");

        mError.setEditable(false);
        status.add(Box.createHorizontalGlue());
        status.add(mError);
        status.add(Box.createHorizontalGlue());

        JPanel jp = new JPanel();

        jp.setBorder(new EmptyBorder(10, 10, 10, 10));
        jp.add("Center", whole);
        getContentPane().add("Center", jp);
        doLayout();
        pack();

        Dimension d    = Toolkit.getDefaultToolkit().getScreenSize();
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

    public static Connection createConnection(JFrame owner, String title) {

        ConnectionDialogSwing dialog = new ConnectionDialogSwing(owner,
            title);

        dialog.create();

        return dialog.mConnection;
    }

    private static JLabel createLabel(String s) {

        JLabel l = new JLabel(s);

        return l;
    }

    public void actionPerformed(ActionEvent ev) {

        String s = ev.getActionCommand();

        if (s.equals("ConnectOk") || (ev.getSource() instanceof JTextField)) {
            try {
                mConnection =
                    createConnection(mDriver.getText(), mURL.getText(),
                                     mUser.getText(),
                                     new String(mPassword.getPassword()));

                dispose();
            } catch (Exception e) {
                e.printStackTrace();
                mError.setText(e.toString());
            }
        } else if (s.equals("ConnectCancel")) {
            dispose();
        }
    }

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
