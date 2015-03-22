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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.SQLException;

// fredt@users 20020320 - doc 1.7.0 - update
// fredt@users 20020825 - patch 1.7.1 - converted to static methods only
// BINARY objest are now represented internally as byte[] and use the static
// methods in this class to compare or convert the byte[] objects

/**
 *  This class allows HSQLDB to store binary data as an array of bytes. It
 *  contains methods to create and access the data, perform comparisons,
 *  etc.
 *
 * @version  1.7.0
 */
class ByteArray {

    /**
     * Private constructor, no instance of this is available.
     *
     */
    private ByteArray() {}

    /**
     * Converts the specified hexadecimal digit <CODE>String</CODE>
     * to an equivalent array of bytes.
     *
     * @param hexString a <CODE>String</CODE> of hexadecimal digits
     * @throws SQLException if the specified string contains non-hexadecimal digits.
     * @return a byte array equivalent to the specified string of hexadecimal digits
     */
    static byte[] hexToByteArray(String hexString) throws SQLException {
        return StringConverter.hexToByte(hexString);
    }

    /**
     * Compares a <CODE>byte[]</CODE> with another specified
     * <CODE>byte[]</CODE> for order.  Returns a negative integer, zero,
     * or a positive integer as the first object is less than, equal to, or
     * greater than the specified second <CODE>byte[]</CODE>.<p>
     *
     * @param o1 the first byte[] to be compared
     * @param o2 the second byte[] to be compared
     * @return a negative integer, zero, or a positive integer as this object
     * is less than, equal to, or greater than the specified object.
     */
    static int compareTo(byte[] o1, byte[] o2) {

        int len  = o1.length;
        int lenb = o2.length;

        for (int i = 0; ; i++) {
            int a = 0;
            int b = 0;

            if (i < len) {
                a = ((int) o1[i]) & 0xff;
            } else if (i >= lenb) {
                return 0;
            }

            if (i < lenb) {
                b = ((int) o2[i]) & 0xff;
            }

            if (a > b) {
                return 1;
            }

            if (b > a) {
                return -1;
            }
        }
    }

    /**
     * Retrieves the serialized form of the specified <CODE>Object</CODE>
     * as an array of bytes.
     *
     * @param s the Object to serialize
     * @return  a static byte array representing the passed Object
     * @throws SQLException if a serialization failure occurs
     */
    static byte[] serialize(Object s) throws SQLException {

        ByteArrayOutputStream bo = new ByteArrayOutputStream();

        try {
            ObjectOutputStream os = new ObjectOutputStream(bo);

            os.writeObject(s);

            return bo.toByteArray();
        } catch (Exception e) {
            throw Trace.error(Trace.SERIALIZATION_FAILURE, e.getMessage());
        }
    }

    /**
     * Retrieves the serialized form of the specified <CODE>Object</CODE>
     * as an equivalent <CODE>String</CODE> of hexadecimal digits.
     *
     * @param s the Object to serialize
     * @return  A String representing the passed Object
     * @throws SQLException if a serialization failure occurs
     */
    static String serializeToString(Object s) throws SQLException {
        return StringConverter.byteToHex(serialize(s));
    }

    /**
     * Deserializes the specified byte array to an
     * <CODE>Object</CODE> instance.
     *
     * @return the Object resulting from deserializing the specified array of bytes
     * @param ba the byte array to deserialize to an Object
     * @throws SQLException if a serialization failure occurs
     */
    static Object deserialize(byte[] ba) throws SQLException {

        try {
            ByteArrayInputStream bi = new ByteArrayInputStream(ba);
            ObjectInputStream    is = new ObjectInputStream(bi);

            return is.readObject();
        } catch (Exception e) {
            throw Trace.error(Trace.SERIALIZATION_FAILURE, e.getMessage());
        }
    }

    /**
     * Converts an array of bytes to an equivalent
     * <CODE>String</CODE> of hexadecimal digits.
     *
     * @return  String representation of the byte[].
     */
    static public String toString(byte[] o) {
        return StringConverter.byteToHex(o);
    }
}
