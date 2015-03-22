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

import java.io.RandomAccessFile;
import java.io.IOException;
import java.io.EOFException;
import java.io.FileNotFoundException;

// fredt@users 20020221 - patch 513005 by sqlbob@users (RMP) - new method

/**
 *  This class provides methods for reading and writing data from a
 *  database file such as that used for storing a cached table.
 *
 * @version  1.7.0
 */
class DatabaseFile extends RandomAccessFile {

    protected byte in[];
    protected long pos;
    protected int  index;
    protected int  count;

    DatabaseFile(String name, String mode,
                 int inSize) throws FileNotFoundException, IOException {

        super(name, mode);

        in = new byte[inSize];
    }

    protected void realSeek(long newPos) throws IOException {
        super.seek(newPos);
    }

    public void seek(long newPos) throws IOException {

        super.seek(newPos);

        pos   = newPos;
        index = count = 0;
    }

    public void readSeek(long newPos) throws IOException {

        if (in == null) {
            seek(newPos);
        } else if (newPos != pos) {
            index += (int) (newPos - pos);

            if ((index < 0) || (index > count)) {
                seek(newPos);
            } else {
                pos = newPos;
            }
        }
    }

    public int read() throws IOException {

        if (in == null) {
            return (super.read());
        }

        if (index == count) {
            index = 0;
            count = super.read(in);

            if (count == -1) {
                count = 0;
            }
        }

        if (index == count) {
            return (-1);
        }

        pos++;

        return (in[index++] & 0xff);
    }

    public int read(byte[] b) throws IOException {

        int i = 0;
        int next;

        for (; i < b.length; i++) {
            next = read();

            if (next == -1) {
                return (-1);
            }

            b[i] = (byte) next;
        }

        return (i);
    }

    //-- readInt is final.
    public int readInteger() throws IOException {

        int ret = 0;
        int next;

        for (int i = 0; i < 4; i++) {
            next = read();

            if (next == -1) {
                throw (new EOFException());
            }

            ret <<= 8;
            ret += (next & 0xff);
        }

        return (ret);
    }

    public void write(byte[] b) throws IOException {

        index = count = 0;
        pos   += b.length;

        super.write(b);
    }

    //-- writeInt is final.
    public void writeInteger(int i) throws IOException {

        index = count = 0;
        pos   += 4;

        writeInt(i);
    }

    public void close() throws IOException {

        super.close();

        in = null;
    }
}
