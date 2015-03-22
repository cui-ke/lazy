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

// peterhudson@users 20020130 - patch 478657 by peterhudson - triggers support
// fredt@users 20020130 - patch 1.7.0 by fredt
// added new class as jdk 1.1 does not allow use of LinkedList
import org.hsqldb.lib.HsqlDeque;

/**
 *  TriggerDef class declaration Definition and execution of triggers
 *  Development of the trigger implementation sponsored by Logicscope
 *  Realisations Ltd
 *
 * @author  Logicscope Realisations Ltd
 * @version  1.7.0 (1.0.0.3) Revision History: 1.0.0.1 First release in hsqldb 1.61
 *      1.0.0.2 'nowait' support to prevent deadlock 1.0.0.3 multiple row
 *      queue for each trigger
 */
class TriggerDef extends Thread {

    /**
     *  member variables
     */
    static final int NUM_TRIGGER_OPS = 3;    // ie ins,del,upd
    static final int NUM_TRIGS       = NUM_TRIGGER_OPS * 2 * 2;

    // indexes into the triggers list
    static final int INSERT_AFTER      = 0;
    static final int DELETE_AFTER      = 1;
    static final int UPDATE_AFTER      = 2;
    static final int INSERT_BEFORE     = INSERT_AFTER + NUM_TRIGGER_OPS;
    static final int DELETE_BEFORE     = DELETE_AFTER + NUM_TRIGGER_OPS;
    static final int UPDATE_BEFORE     = UPDATE_AFTER + NUM_TRIGGER_OPS;
    static final int INSERT_AFTER_ROW  = INSERT_AFTER + 2 * NUM_TRIGGER_OPS;
    static final int DELETE_AFTER_ROW  = DELETE_AFTER + 2 * NUM_TRIGGER_OPS;
    static final int UPDATE_AFTER_ROW  = UPDATE_AFTER + 2 * NUM_TRIGGER_OPS;
    static final int INSERT_BEFORE_ROW = INSERT_BEFORE + 2 * NUM_TRIGGER_OPS;
    static final int DELETE_BEFORE_ROW = DELETE_BEFORE + 2 * NUM_TRIGGER_OPS;
    static final int UPDATE_BEFORE_ROW = UPDATE_BEFORE + 2 * NUM_TRIGGER_OPS;

    // other variables
    String  name;
    String  when;
    String  operation;
    boolean forEachRow;
    boolean nowait;                          // block or overwrite if queue full
    int     maxRowsQueued;                   // max size of queue of pending triggers

    public static int getDefaultQueueSize() {
        return defaultQueueSize;
    }

    protected static int defaultQueueSize = 1024;
    Table                table;
    Trigger              trig;
    String               fire;
    int                  vectorIndx;     // index into Vector[]

    //protected boolean busy;               // firing trigger in progress
    protected HsqlDeque pendingQueue;    // row triggers pending
    protected int       rowsQueued;      // rows in pendingQueue
    protected boolean   valid;           // parsing valid

    /**
     *  Constructor declaration create an object from the components of an
     *  SQL CREATE TRIGGER statement
     *
     * @param  sName
     * @param  sWhen
     * @param  sOper
     * @param  bForEach
     * @param  pTab
     * @param  pTrig
     * @param  sFire
     * @param  bNowait Description of the Parameter
     * @param  nQueueSize Description of the Parameter
     */
    public TriggerDef(String sName, String sWhen, String sOper,
                      boolean bForEach, Table pTab, Trigger pTrig,
                      String sFire, boolean bNowait, int nQueueSize) {

        name          = sName.toUpperCase();
        when          = sWhen.toUpperCase();
        operation     = sOper.toUpperCase();
        forEachRow    = bForEach;
        nowait        = bNowait;
        maxRowsQueued = nQueueSize;
        table         = pTab;
        trig          = pTrig;
        fire          = sFire;
        vectorIndx    = SqlToIndex();

        //busy = false;
        rowsQueued   = 0;
        pendingQueue = new HsqlDeque();

        if (vectorIndx < 0) {
            valid = false;
        } else {
            valid = true;
        }
    }

    /**
     *  Method declaration
     *
     * @return
     */
    public StringBuffer toBuf() {

        StringBuffer a = new StringBuffer(256);

        a.append("CREATE TRIGGER ");
        a.append(name);
        a.append(" ");
        a.append(when);
        a.append(" ");
        a.append(operation);
        a.append(" ON ");
        a.append(table.getName().statementName);

        if (forEachRow) {
            a.append(" FOR EACH ROW ");
        }

        if (nowait) {
            a.append(" NOWAIT ");
        }

        if (maxRowsQueued != getDefaultQueueSize()) {
            a.append(" QUEUE ");
            a.append(maxRowsQueued);    // no need for trailing space
        }

        a.append(" CALL ");
        a.append(fire);

        return a;
    }

    /**
     *  SqlToIndex method declaration <P>
     *
     *  Given the SQL creating the trigger, say what the index to the
     *  Vector[] is
     *
     * @return  index to the Vector[]
     */
    public int SqlToIndex() {

        int indx;

        if (operation.equals("INSERT")) {
            indx = INSERT_AFTER;
        } else if (operation.equals("DELETE")) {
            indx = DELETE_AFTER;
        } else if (operation.equals("UPDATE")) {
            indx = UPDATE_AFTER;
        } else {
            indx = -1;
        }

        if (when.equals("BEFORE")) {
            indx += NUM_TRIGGER_OPS;    // number of operations
        } else if (!when.equals("AFTER")) {
            indx = -1;
        }

        if (forEachRow) {
            indx += 2 * NUM_TRIGGER_OPS;
        }

        return indx;
    }

    /**
     *  run method declaration <P>
     *
     *  the trigger JSP is run in its own thread here. Its job is simply to
     *  wait until it is told by the main thread that it should fire the
     *  trigger.
     */
    public void run() {

        boolean keepGoing = true;

        while (keepGoing) {
            Object trigRow[] = pop();

            trig.fire(name, table.getName().name, trigRow);
        }
    }

    /**
     *  pop method declaration <P>
     *
     *  The consumer (trigger) thread waits for an event to be queued <P>
     *
     *  <B>Note: </B> This push/pop pairing assumes a single producer thread
     *  and a single consumer thread _only_.
     *
     * @return  Description of the Return Value
     */
    synchronized Object[] pop() {

        if (rowsQueued == 0) {
            try {
                wait();    // this releases the lock monitor
            } catch (InterruptedException e) {

                /* ignore and resume */
            }
        }

        rowsQueued--;

        notify();    // notify push's wait

        return (Object[]) pendingQueue.removeFirst();
    }

    /**
     *  push method declaration <P>
     *
     *  The main thread tells the trigger thread to fire by this call
     *
     * @param  row Description of the Parameter
     */
    synchronized void push(Object row[]) {

        if (rowsQueued >= maxRowsQueued) {
            if (nowait) {
                pendingQueue.removeLast();    // overwrite last
            } else {
                try {
                    wait();
                } catch (InterruptedException e) {

                    /* ignore and resume */
                }

                rowsQueued++;
            }
        } else {
            rowsQueued++;
        }

        pendingQueue.add(row);
        notify();    // notify pop's wait
    }

    /**
     *  Method declaration
     *
     * @return
     */
    public static int numTrigs() {
        return NUM_TRIGS;
    }

    /**
     *  Method declaration
     *
     * @return
     */
    public boolean isBusy() {
        return rowsQueued != 0;
    }

    /**
     *  Method declaration
     *
     * @return
     */
    public boolean isValid() {
        return valid;
    }
}
