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

import java.io.IOException;
import java.io.FileNotFoundException;

/**
 *  Cache class declaration <P>
 *
 *  The cache class implements the handling of reversed text table caches.
 *
 * @author sqlbob@users
 * @version  1.7.0
 */
class ReverseDatabaseFile extends org.hsqldb.DatabaseFile {

    private int  size;
    private long max;

    ReverseDatabaseFile(String name, String mode,
                        int inSize)
                        throws FileNotFoundException, IOException {

        super(name, mode, inSize);

        max  = length();
        pos  = max;
        size = inSize;
    }

    public void readSeek(long newPos) throws IOException {

        newPos = max - newPos;

        if ((newPos < 0) || (in == null)) {
            seek(newPos);
        } else if (newPos != pos) {
            index -= (int) (pos - newPos);
            pos   = newPos;

            if ((index < 0) || (index > count)) {
                count = 0;
                index = 0;
            }
        }
    }

    public int read() throws IOException {

        if (in == null) {
            return (super.read());
        }

        if (index == 0) {
            long newPos = pos - size;

            if (newPos < 0) {
                realSeek(0);

                count = (int) pos;

                readFully(in, 0, count);
            } else {
                realSeek(newPos);
                readFully(in);

                count = size;
            }

            index = count;
        }

        if (index == 0) {
            return (-1);
        }

        pos--;

        return (in[--index] & 0xff);
    }

    public int readInteger() throws IOException {

        seek(pos);

        return (readInt());
    }

    public long getFilePointer() throws IOException {
        return (max - pos);
    }
}
