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

/**
 * Class declaration
 *
 *
 * @version 1.7.0
 */
class Like {

    private char    cLike[];
    private int[]   iType;
    private int     iLen;
    private boolean bIgnoreCase;

    /**
     * Constructor declaration
     *
     *
     * @param s
     * @param escape
     * @param ignorecase
     */
    Like(String s, char escape, boolean ignorecase) {

        if (ignorecase) {
            s = s.toUpperCase();
        }

        normalize(s, true, escape);

        bIgnoreCase = ignorecase;
    }

    /**
     * Method declaration
     *
     *
     * @return
     */
    String getStartsWith() {

        StringBuffer s = new StringBuffer();
        int          i = 0;

        for (; (i < iLen) && (iType[i] == 0); i++) {
            s.append(cLike[i]);
        }

        if (i == 0) {
            return null;
        }

        return s.toString();
    }

    /**
     * Method declaration
     *
     *
     * @param o
     *
     * @return
     */
    boolean compare(Object o) {

        if (o == null) {
            return iLen == 0;
        }

        String s = o.toString();

        if (bIgnoreCase) {
            s = s.toUpperCase();
        }

        return compareAt(s, 0, 0, s.length());
    }

    /**
     * Method declaration
     *
     *
     * @param s
     * @param i
     * @param j
     * @param jLen
     *
     * @return
     */
    private boolean compareAt(String s, int i, int j, int jLen) {

        for (; i < iLen; i++) {
            switch (iType[i]) {

                case 0 :    // general character
                    if ((j >= jLen) || (cLike[i] != s.charAt(j++))) {
                        return false;
                    }
                    break;

                case 1 :    // underscore: do not test this character
                    if (j++ >= jLen) {
                        return false;
                    }
                    break;

                case 2 :    // percent: none or any character(s)
                    if (++i >= iLen) {
                        return true;
                    }

                    while (j < jLen) {
                        if ((cLike[i] == s.charAt(j))
                                && compareAt(s, i, j, jLen)) {
                            return true;
                        }

                        j++;
                    }

                    return false;
            }
        }

        if (j != jLen) {
            return false;
        }

        return true;
    }

    /**
     * Method declaration
     *
     *
     * @param s
     * @param b
     * @param e
     */
    private void normalize(String s, boolean b, char e) {

        iLen = 0;

        if (s == null) {
            return;
        }

        int l = s.length();

        cLike = new char[l];
        iType = new int[l];

        boolean bEscaping = false,
                bPercent  = false;

        for (int i = 0; i < l; i++) {
            char c = s.charAt(i);

            if (bEscaping == false) {
                if (b && (c == e)) {
                    bEscaping = true;

                    continue;
                } else if (c == '_') {
                    iType[iLen] = 1;
                } else if (c == '%') {
                    if (bPercent) {
                        continue;
                    }

                    bPercent    = true;
                    iType[iLen] = 2;
                } else {
                    bPercent = false;
                }
            } else {
                bPercent  = false;
                bEscaping = false;
            }

            cLike[iLen++] = c;
        }

        for (int i = 0; i < iLen - 1; i++) {
            if ((iType[i] == 2) && (iType[i + 1] == 1)) {
                iType[i]     = 1;
                iType[i + 1] = 2;
            }
        }
    }
}
