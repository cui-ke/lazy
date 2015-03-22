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
import java.awt.image.*;
import java.sql.*;
import java.io.File;
import java.util.*;
import javax.swing.*;
import javax.swing.tree.*;

// sqlbob@users 20020401 - patch 1.7.0 by sqlbob (RMP) - enhancements
// sqlbob@users 20020401 - patch 537501 by ulrivo - commandline arguments
// sqlbob@users 20020407 - patch 1.7.0 - reengineering

/**
 * Manages a JDBC database
 *
 * @version 1.7.0
 */
public class DatabaseManagerSwing extends JApplet
implements ActionListener, WindowListener, KeyListener {

    final static String    NL         = System.getProperty("line.separator");
    static int             iMaxRecent = 24;
    Connection             cConn;
    DatabaseMetaData       dMeta;
    Statement              sStatement;
    JMenu                  mRecent;
    String                 sRecent[];
    int                    iRecent;
    JTextArea              txtCommand;
    JScrollPane            txtCommandScroll;
    JButton                butExecute;
    JTree                  tTree;
    JScrollPane            tScrollPane;
    DefaultTreeModel       treeModel;
    DefaultMutableTreeNode rootNode;
    JPanel                 pResult;
    long                   lTime;
    int                    iResult;        // 0: grid; 1: text
    GridSwing              gResult;
    JTable                 gResultTable;
    JScrollPane            gScrollPane;
    JTextArea              txtResult;
    JScrollPane            txtResultScroll;
    JSplitPane             nsSplitPane;    // Contains query over results
    JSplitPane             ewSplitPane;    // Contains tree beside nsSplitPane
    boolean                bHelp;
    JFrame                 fMain;

    // (ulrivo): variables set by arguments from the commandline
    static String defDriver   = "org.hsqldb.jdbcDriver";
    static String defURL      = "jdbc:hsqldb:.";
    static String defUser     = "sa";
    static String defPassword = "";
    static String defScript;
    static String defDirectory;

    public void init() {

        DatabaseManagerSwing m = new DatabaseManagerSwing();

        m.main();

        try {
            m.connect(ConnectionDialogSwing.createConnection(defDriver,
                    defURL, defUser, defPassword));
            m.insertTestData();
            m.refreshTree();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String arg[]) {

        System.getProperties().put("sun.java2d.noddraw", "true");

        // (ulrivo): read all arguments from the command line
        String  lowerArg;
        boolean autoConnect = false;

        for (int i = 0; i < arg.length; i++) {
            lowerArg = arg[i].toLowerCase();

            i++;

            if (i == arg.length) {
                showUsage();

                return;
            }

            if (lowerArg.equals("-driver")) {
                defDriver   = arg[i];
                autoConnect = true;
            } else if (lowerArg.equals("-url")) {
                defURL      = arg[i];
                autoConnect = true;
            } else if (lowerArg.equals("-user")) {
                defUser     = arg[i];
                autoConnect = true;
            } else if (lowerArg.equals("-password")) {
                defPassword = arg[i];
                autoConnect = true;
            } else if (lowerArg.equals("-dir")) {
                defDirectory = arg[i];
            } else if (lowerArg.equals("-script")) {
                defScript = arg[i];
            } else {
                showUsage();

                return;
            }
        }

        DatabaseManagerSwing m = new DatabaseManagerSwing();

        m.main();

        Connection c = null;

        try {
            if (autoConnect) {
                c = ConnectionDialogSwing.createConnection(defDriver, defURL,
                        defUser, defPassword);
            } else {
                c = ConnectionDialogSwing.createConnection(m.fMain,
                        "Connect");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (c == null) {
            return;
        }

        m.connect(c);
    }

    private void connect(Connection c) {

        if (c == null) {
            return;
        }

        if (cConn != null) {
            try {
                cConn.close();
            } catch (SQLException e) {}
        }

        cConn = c;

        try {
            dMeta      = cConn.getMetaData();
            sStatement = cConn.createStatement();

            refreshTree();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void showUsage() {

        System.out.println(
            "Usage: java DatabaseManagerSwing [-options]\n"
            + "where options include:\n"
            + "    -driver <classname>  jdbc driver class\n"
            + "    -url <name>          jdbc url\n"
            + "    -user <name>         username used for connection\n"
            + "    -password <password> password for this user\n"
            + "    -dir <path>          default directory\n"
            + "    -script <file>       reads from script file\n");
    }

    private void insertTestData() {

        try {
            DatabaseManagerCommon.createTestTables(sStatement);
            refreshTree();
            txtCommand.setText(
                DatabaseManagerCommon.createTestData(sStatement));
            refreshTree();

            for (int i = 0; i < DatabaseManagerCommon.testDataSql.length;
                    i++) {
                addToRecent(DatabaseManagerCommon.testDataSql[i]);
            }

            execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    void main() {

        CommonSwing.setDefaultColor();

        fMain = new JFrame("HSQL Database Manager");

        // (ulrivo): An actual icon.
        fMain.setIconImage(CommonSwing.getIcon());
        fMain.addWindowListener(this);

        JMenuBar bar = new JMenuBar();

        // used shortcuts: CERGTSIUDOLM
        String fitems[] = {
            "-Connect...", "--", "-Open Script...", "-Save Script...",
            "-Save Result...", "--", "-Exit"
        };

        addMenu(bar, "File", fitems);

        String vitems[] = {
            "RRefresh Tree", "--", "GResults in Grid", "TResults in Text"
        };

        addMenu(bar, "View", vitems);

        String sitems[] = {
            "SSELECT", "IINSERT", "UUPDATE", "DDELETE", "---",
            "-CREATE TABLE", "-DROP TABLE", "-CREATE INDEX", "-DROP INDEX",
            "--", "-CHECKPOINT", "-SCRIPT", "-SET", "-SHUTDOWN", "--",
            "-Test Script"
        };

        addMenu(bar, "Command", sitems);

        mRecent = new JMenu("Recent");

        bar.add(mRecent);

        String soptions[] = {
            "-AutoCommit on", "-AutoCommit off", "OCommit", "LRollback", "--",
            "-Disable MaxRows", "-Set MaxRows to 100", "--", "-Logging on",
            "-Logging off", "--", "-Insert test data"
        };

        addMenu(bar, "Options", soptions);

        /* NB - 26052002 Restore is not implemented yet in the transfer tool */
        String stools[] = {
            "-Dump", /*"-Restore",*/ "-Transfer"
        };

        addMenu(bar, "Tools", stools);
        fMain.setJMenuBar(bar);
        initGUI();

        sRecent = new String[iMaxRecent];

        Dimension d    = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension size = fMain.getSize();

        // (ulrivo): full size on screen with less than 640 width
        if (d.width >= 640) {
            fMain.setLocation((d.width - size.width) / 2,
                              (d.height - size.height) / 2);
        } else {
            fMain.setLocation(0, 0);
            fMain.setSize(d);
        }

        fMain.show();

        // (ulrivo): load query from command line
        if (defScript != null) {
            if (defDirectory != null) {
                defScript = defDirectory + File.separator + defScript;
            }

            txtCommand.setText(DatabaseManagerCommon.readFile(defScript));
        }

        txtCommand.requestFocus();
    }

    private void addMenu(JMenuBar b, String name, String items[]) {

        JMenu menu = new JMenu(name);

        addMenuItems(menu, items);
        b.add(menu);
    }

    private void addMenuItems(JMenu f, String m[]) {

        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();

        for (int i = 0; i < m.length; i++) {
            if (m[i].equals("--")) {
                f.addSeparator();
            } else if (m[i].equals("---")) {

                // (ulrivo): full size on screen with less than 640 width
                if (d.width >= 640) {
                    f.addSeparator();
                } else {
                    return;
                }
            } else {
                JMenuItem item = new JMenuItem(m[i].substring(1));
                char      c    = m[i].charAt(0);

                if (c != '-') {
                    item.setMnemonic(c);
                }

                item.addActionListener(this);
                f.add(item);
            }
        }
    }

    public void keyPressed(KeyEvent k) {}

    public void keyReleased(KeyEvent k) {}

    public void keyTyped(KeyEvent k) {

        if (k.getKeyChar() == '\n' && k.isControlDown()) {
            k.consume();
            execute();
        }
    }

    public void actionPerformed(ActionEvent ev) {

        String s = ev.getActionCommand();

        if (s == null) {
            if (ev.getSource() instanceof JMenuItem) {
                JMenuItem i;

                s = ((JMenuItem) ev.getSource()).getText();
            }
        }

        if (s.equals("Execute")) {
            execute();
        } else if (s.equals("Exit")) {
            windowClosing(null);
        } else if (s.equals("Transfer")) {
            Transfer.work(null);
        } else if (s.equals("Dump")) {
            Transfer.work(new String[]{ "-d" });

            /* NB - 26052002 Restore is not implemented yet in the transfer tool */
/*
        } else if (s.equals("Restore")) {
            TransferSwing.work(new String[]{ "-r" });
*/
        } else if (s.equals("Logging on")) {
            jdbcSystem.setLogToSystem(true);
        } else if (s.equals("Logging off")) {
            jdbcSystem.setLogToSystem(false);
        } else if (s.equals("Refresh Tree")) {
            refreshTree();
        } else if (s.startsWith("#")) {
            int i = Integer.parseInt(s.substring(1));

            txtCommand.setText(sRecent[i]);
        } else if (s.equals("Connect...")) {
            connect(ConnectionDialogSwing.createConnection(fMain, "Connect"));
            refreshTree();
        } else if (s.equals("Results in Grid")) {
            iResult = 0;

            pResult.removeAll();
            pResult.add(gScrollPane, BorderLayout.CENTER);
            pResult.doLayout();
            gResult.fireTableChanged(null);
            pResult.repaint();
        } else if (s.equals("Open Script...")) {
            JFileChooser f = new JFileChooser(".");

            f.setDialogTitle("Open Script...");

            // (ulrivo): set default directory if set from command line
            if (defDirectory != null) {
                f.setCurrentDirectory(new File(defDirectory));
            }

            int option = f.showOpenDialog(fMain);

            if (option == JFileChooser.APPROVE_OPTION) {
                File file = f.getSelectedFile();

                if (file != null) {
                    txtCommand.setText(
                        DatabaseManagerCommon.readFile(
                            file.getAbsolutePath()));
                }
            }
        } else if (s.equals("Save Script...")) {
            JFileChooser f = new JFileChooser(".");

            f.setDialogTitle("Save Script");

            // (ulrivo): set default directory if set from command line
            if (defDirectory != null) {
                f.setCurrentDirectory(new File(defDirectory));
            }

            int option = f.showSaveDialog(fMain);

            if (option == JFileChooser.APPROVE_OPTION) {
                File file = f.getSelectedFile();

                if (file != null) {
                    DatabaseManagerCommon.writeFile(file.getAbsolutePath(),
                                                    txtCommand.getText());
                }
            }
        } else if (s.equals("Save Result...")) {
            JFileChooser f = new JFileChooser(".");

            f.setDialogTitle("Save Result...");

            // (ulrivo): set default directory if set from command line
            if (defDirectory != null) {
                f.setCurrentDirectory(new File(defDirectory));
            }

            int option = f.showSaveDialog(fMain);

            if (option == JFileChooser.APPROVE_OPTION) {
                File file = f.getSelectedFile();

                if (file != null) {
                    showResultInText();
                    DatabaseManagerCommon.writeFile(file.getAbsolutePath(),
                                                    txtResult.getText());
                }
            }
        } else if (s.equals("Results in Text")) {
            iResult = 1;

            pResult.removeAll();
            pResult.add(txtResultScroll, BorderLayout.CENTER);
            pResult.doLayout();
            showResultInText();
            pResult.repaint();
        } else if (s.equals("AutoCommit on")) {
            try {
                cConn.setAutoCommit(true);
            } catch (SQLException e) {}
        } else if (s.equals("AutoCommit off")) {
            try {
                cConn.setAutoCommit(false);
            } catch (SQLException e) {}
        } else if (s.equals("Commit")) {
            try {
                cConn.commit();
            } catch (SQLException e) {}
        } else if (s.equals("Insert test data")) {
            insertTestData();
        } else if (s.equals("Rollback")) {
            try {
                cConn.rollback();
            } catch (SQLException e) {}
        } else if (s.equals("Disable MaxRows")) {
            try {
                sStatement.setMaxRows(0);
            } catch (SQLException e) {}
        } else if (s.equals("Set MaxRows to 100")) {
            try {
                sStatement.setMaxRows(100);
            } catch (SQLException e) {}
        } else if (s.equals("SELECT")) {
            showHelp(DatabaseManagerCommon.selectHelp);
        } else if (s.equals("INSERT")) {
            showHelp(DatabaseManagerCommon.insertHelp);
        } else if (s.equals("UPDATE")) {
            showHelp(DatabaseManagerCommon.updateHelp);
        } else if (s.equals("DELETE")) {
            showHelp(DatabaseManagerCommon.deleteHelp);
        } else if (s.equals("CREATE TABLE")) {
            showHelp(DatabaseManagerCommon.createTableHelp);
        } else if (s.equals("DROP TABLE")) {
            showHelp(DatabaseManagerCommon.dropTableHelp);
        } else if (s.equals("CREATE INDEX")) {
            showHelp(DatabaseManagerCommon.createIndexHelp);
        } else if (s.equals("DROP INDEX")) {
            showHelp(DatabaseManagerCommon.dropIndexHelp);
        } else if (s.equals("CHECKPOINT")) {
            showHelp(DatabaseManagerCommon.checkpointHelp);
        } else if (s.equals("SCRIPT")) {
            showHelp(DatabaseManagerCommon.scriptHelp);
        } else if (s.equals("SHUTDOWN")) {
            showHelp(DatabaseManagerCommon.shutdownHelp);
        } else if (s.equals("SET")) {
            showHelp(DatabaseManagerCommon.setHelp);
        } else if (s.equals("Test Script")) {
            showHelp(DatabaseManagerCommon.testHelp);
        }
    }

    private void showHelp(String help[]) {

        txtCommand.setText(help[0]);

        bHelp = true;

        pResult.removeAll();
        pResult.add(txtResultScroll, BorderLayout.CENTER);
        pResult.doLayout();
        txtResult.setText(help[1]);
        pResult.repaint();
        txtCommand.requestFocus();
        txtCommand.setCaretPosition(help[0].length());
    }

    public void windowActivated(WindowEvent e) {}

    public void windowDeactivated(WindowEvent e) {}

    public void windowClosed(WindowEvent e) {}

    public void windowDeiconified(WindowEvent e) {}

    public void windowIconified(WindowEvent e) {}

    public void windowOpened(WindowEvent e) {}

    public void windowClosing(WindowEvent ev) {

        try {
            cConn.close();
        } catch (Exception e) {}

        fMain.dispose();
        System.exit(0);
    }

    private void execute() {

        gResult.clear();

        String sCmd = txtCommand.getText();

        if (sCmd.startsWith("-->>>TEST<<<--")) {
            testPerformance();

            return;
        }

        String g[] = new String[1];

        try {
            lTime = System.currentTimeMillis();

            sStatement.execute(sCmd);

            int r = sStatement.getUpdateCount();

            if (r == -1) {
                formatResultSet(sStatement.getResultSet());
            } else {
                g[0] = "update count";

                gResult.setHead(g);

                g[0] = "" + r;

                gResult.addRow(g);
            }

            lTime = System.currentTimeMillis() - lTime;

            addToRecent(txtCommand.getText());
            gResult.fireTableChanged(null);
        } catch (SQLException e) {
            lTime = System.currentTimeMillis() - lTime;
            g[0]  = "SQL Error";

            gResult.setHead(g);

            String s = e.getMessage();

            s    += " / Error Code: " + e.getErrorCode();
            s    += " / State: " + e.getSQLState();
            g[0] = s;

            gResult.addRow(g);
            gResult.fireTableChanged(null);
        }

        updateResult();
        System.gc();
    }

    private void updateResult() {

        if (iResult == 0) {

            // in case 'help' has removed the grid
            if (bHelp) {
                pResult.removeAll();
                pResult.add(gScrollPane, BorderLayout.CENTER);
                pResult.doLayout();
                gResult.fireTableChanged(null);
                pResult.repaint();

                bHelp = false;
            }
        } else {
            showResultInText();
        }

        txtCommand.selectAll();
        txtCommand.requestFocus();
    }

    private void formatResultSet(ResultSet r) {

        if (r == null) {
            String g[] = new String[1];

            g[0] = "Result";

            gResult.setHead(g);

            g[0] = "(empty)";

            gResult.addRow(g);

            return;
        }

        try {
            ResultSetMetaData m   = r.getMetaData();
            int               col = m.getColumnCount();
            String            h[] = new String[col];

            for (int i = 1; i <= col; i++) {
                h[i - 1] = m.getColumnLabel(i);
            }

            gResult.setHead(h);

            while (r.next()) {
                for (int i = 1; i <= col; i++) {
                    h[i - 1] = r.getString(i);

                    if (r.wasNull()) {
                        h[i - 1] = "(null)";
                    }
                }

                gResult.addRow(h);
            }

            r.close();
        } catch (SQLException e) {}
    }

    private void testPerformance() {

        String       all   = txtCommand.getText();
        StringBuffer b     = new StringBuffer();
        long         total = 0;

        for (int i = 0; i < all.length(); i++) {
            char c = all.charAt(i);

            if (c != '\n') {
                b.append(c);
            }
        }

        all = b.toString();

        String g[] = new String[4];

        g[0] = "ms";
        g[1] = "count";
        g[2] = "sql";
        g[3] = "error";

        gResult.setHead(g);

        int max = 1;

        lTime = System.currentTimeMillis() - lTime;

        while (!all.equals("")) {
            int    i = all.indexOf(';');
            String sql;

            if (i != -1) {
                sql = all.substring(0, i);
                all = all.substring(i + 1);
            } else {
                sql = all;
                all = "";
            }

            if (sql.startsWith("--#")) {
                max = Integer.parseInt(sql.substring(3));

                continue;
            } else if (sql.startsWith("--")) {
                continue;
            }

            g[2] = sql;

            long l = 0;

            try {
                l = DatabaseManagerCommon.testStatement(sStatement, sql, max);
                total += l;
                g[0]  = "" + l;
                g[1]  = "" + max;
                g[3]  = "";
            } catch (SQLException e) {
                g[0] = g[1] = "n/a";
                g[3] = e.toString();
            }

            gResult.addRow(g);
            System.out.println(l + " ms : " + sql);
        }

        g[0] = "" + total;
        g[1] = "total";
        g[2] = "";

        gResult.addRow(g);

        lTime = System.currentTimeMillis() - lTime;

        updateResult();
    }

    /**
     * Method declaration
     *
     */
    private void showResultInText() {

        String col[]  = gResult.getHead();
        int    width  = col.length;
        int    size[] = new int[width];
        Vector data   = gResult.getData();
        String row[];
        int    height = data.size();

        for (int i = 0; i < width; i++) {
            size[i] = col[i].length();
        }

        for (int i = 0; i < height; i++) {
            row = (String[]) data.elementAt(i);

            for (int j = 0; j < width; j++) {
                int l = row[j].length();

                if (l > size[j]) {
                    size[j] = l;
                }
            }
        }

        StringBuffer b = new StringBuffer();

        for (int i = 0; i < width; i++) {
            b.append(col[i]);

            for (int l = col[i].length(); l <= size[i]; l++) {
                b.append(' ');
            }
        }

        b.append(NL);

        for (int i = 0; i < width; i++) {
            for (int l = 0; l < size[i]; l++) {
                b.append('-');
            }

            b.append(' ');
        }

        b.append(NL);

        for (int i = 0; i < height; i++) {
            row = (String[]) data.elementAt(i);

            for (int j = 0; j < width; j++) {
                b.append(row[j]);

                for (int l = row[j].length(); l <= size[j]; l++) {
                    b.append(' ');
                }
            }

            b.append(NL);
        }

        b.append(NL + height + " row(s) in " + lTime + " ms");
        txtResult.setText(b.toString());
    }

    private void addToRecent(String s) {

        for (int i = 0; i < iMaxRecent; i++) {
            if (s.equals(sRecent[i])) {
                return;
            }
        }

        if (sRecent[iRecent] != null) {
            mRecent.remove(iRecent);
        }

        sRecent[iRecent] = s;

        if (s.length() > 43) {
            s = s.substring(0, 40) + "...";
        }

        JMenuItem item = new JMenuItem(s);

        item.setActionCommand("#" + iRecent);
        item.addActionListener(this);
        mRecent.insert(item, iRecent);

        iRecent = (iRecent + 1) % iMaxRecent;
    }

    private void initGUI() {

        JPanel pCommand = new JPanel();

        pResult = new JPanel();
        nsSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, pCommand,
                                     pResult);

        pCommand.setLayout(new BorderLayout());
        pResult.setLayout(new BorderLayout());

        Font fFont = new Font("Dialog", Font.PLAIN, 12);

        txtCommand = new JTextArea(5, 40);

        txtCommand.setMargin(new Insets(5, 5, 5, 5));
        txtCommand.addKeyListener(this);

        txtCommandScroll = new JScrollPane(txtCommand);
        txtResult        = new JTextArea(20, 40);

        txtResult.setMargin(new Insets(5, 5, 5, 5));

        txtResultScroll = new JScrollPane(txtResult);

        txtCommand.setFont(fFont);
        txtResult.setFont(new Font("Courier", Font.PLAIN, 12));

        butExecute = new JButton("Execute");

        butExecute.addActionListener(this);
        pCommand.add(butExecute, BorderLayout.EAST);
        pCommand.add(txtCommandScroll, BorderLayout.CENTER);

        gResult      = new GridSwing();
        gResultTable = new JTable(gResult);
        gScrollPane  = new JScrollPane(gResultTable);

        //getContentPane().setLayout(new BorderLayout());
        pResult.add(gScrollPane, BorderLayout.CENTER);

        // Set up the tree
        rootNode    = new DefaultMutableTreeNode("Connection");
        treeModel   = new DefaultTreeModel(rootNode);
        tTree       = new JTree(treeModel);
        tScrollPane = new JScrollPane(tTree);

        tScrollPane.setPreferredSize(new Dimension(120, 400));
        tScrollPane.setMinimumSize(new Dimension(70, 100));
        txtCommandScroll.setPreferredSize(new Dimension(360, 100));
        txtCommandScroll.setMinimumSize(new Dimension(180, 100));
        gScrollPane.setPreferredSize(new Dimension(460, 300));

        ewSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                                     tScrollPane, nsSplitPane);

        fMain.getContentPane().add(ewSplitPane, BorderLayout.CENTER);
        doLayout();
        fMain.pack();
    }

    /* Simple tree node factory method - set's parent and user object.
     */
    private DefaultMutableTreeNode makeNode(Object userObject,
            MutableTreeNode parent) {

        DefaultMutableTreeNode node = new DefaultMutableTreeNode(userObject);

        if (parent != null) {
            treeModel.insertNodeInto(node, parent, parent.getChildCount());
        }

        return node;
    }

    /* Clear all existing nodes from the tree model and rebuild from scratch.
     */
    protected void refreshTree() {

        DefaultMutableTreeNode propertiesNode;
        DefaultMutableTreeNode leaf;

        // First clear the existing tree by simply enumerating
        // over the root node's children and removing them one by one.
        while (treeModel.getChildCount(rootNode) > 0) {
            DefaultMutableTreeNode child =
                (DefaultMutableTreeNode) treeModel.getChild(rootNode, 0);

            treeModel.removeNodeFromParent(child);
            child.removeAllChildren();
            child.removeFromParent();
        }

        treeModel.nodeStructureChanged(rootNode);
        treeModel.reload();
        tScrollPane.repaint();

        // Now rebuild the tree below its root
        try {

            // Start by naming the root node from its URL:
            rootNode.setUserObject(dMeta.getURL());

            // get metadata about user tables by building a vector of table names
            String    usertables[] = { "TABLE" };
            ResultSet result = dMeta.getTables(null, null, null, usertables);
            Vector    tables       = new Vector();

            // sqlbob@users Added remarks.
            Vector remarks = new Vector();

            while (result.next()) {
                tables.addElement(result.getString(3));
                remarks.addElement(result.getString(5));
            }

            result.close();

            // For each table, build a tree node with interesting info
            for (int i = 0; i < tables.size(); i++) {
                String                 name = (String) tables.elementAt(i);
                DefaultMutableTreeNode tableNode = makeNode(name, rootNode);
                ResultSet col = dMeta.getColumns(null, null, name, null);

                // sqlbob@users Added remarks.
                String remark = (String) remarks.elementAt(i);

                if ((remark != null) &&!remark.trim().equals("")) {
                    makeNode(remark, tableNode);
                }

                // With a child for each column containing pertinent attributes
                while (col.next()) {
                    String c = col.getString(4);
                    DefaultMutableTreeNode columnNode = makeNode(c,
                        tableNode);
                    String type = col.getString(6);

                    makeNode("Type: " + type, columnNode);

                    boolean nullable = col.getInt(11)
                                       != DatabaseMetaData.columnNoNulls;

                    makeNode("Nullable: " + nullable, columnNode);
                }

                col.close();

                DefaultMutableTreeNode indexesNode = makeNode("Indices",
                    tableNode);
                ResultSet ind = dMeta.getIndexInfo(null, null, name, false,
                                                   false);
                String oldiname = null;

                // A child node to contain each index - and its attributes
                while (ind.next()) {
                    DefaultMutableTreeNode indexNode = null;
                    boolean                nonunique = ind.getBoolean(4);
                    String                 iname     = ind.getString(6);

                    if ((oldiname == null ||!oldiname.equals(iname))) {
                        indexNode = makeNode(iname, indexesNode);

                        makeNode("Unique: " + !nonunique, indexNode);

                        oldiname = iname;
                    }

                    // And the ordered column list for index components
                    makeNode(ind.getString(9), indexNode);
                }

                ind.close();
            }

            // Finally - a little additional metadata on this connection
            propertiesNode = makeNode("Properties", rootNode);

            makeNode("User: " + dMeta.getUserName(), propertiesNode);
            makeNode("ReadOnly: " + cConn.isReadOnly(), propertiesNode);
            makeNode("AutoCommit: " + cConn.getAutoCommit(), propertiesNode);
            makeNode("Driver: " + dMeta.getDriverName(), propertiesNode);
            makeNode("Product: " + dMeta.getDatabaseProductName(),
                     propertiesNode);
            makeNode("Version: " + dMeta.getDatabaseProductVersion(),
                     propertiesNode);
        } catch (SQLException se) {
            propertiesNode = makeNode("Error getting metadata:", rootNode);

            makeNode(se.getMessage(), propertiesNode);
            makeNode(se.getSQLState(), propertiesNode);
        }

        treeModel.nodeStructureChanged(rootNode);
        treeModel.reload();
        tScrollPane.repaint();
    }
}
