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
 * Name of an SQL object<p>
 *
 * Methods check user defined names and issue system generated names
 * for SQL objects.<p>
 *
 * This class does not deal with the type of the SQL object for which it
 * is used.<p>
 *
 * sysNumber is used to generate system generated names. It is
 * set to the largest integer encountered in names that use the
 * SYS_xxxxxxx_INTEGER format. As the DDL is processed before any ALTER
 * command, any new system generated name will have a larger integer suffix
 * than all the existing names.
 *
 * @author fredt@users
 * @version 1.7.0
 */
class HsqlName {

    String     name;
    boolean    isNameQuoted;
    String     statementName;
    static int sysNumber = 0;

    public HsqlName(String name, boolean isquoted) {
        rename(name, isquoted);
    }

    public HsqlName(String prefix, String name, boolean isquoted) {
        rename(prefix, name, isquoted);
    }

    public static HsqlName makeAutoName(String type) {

        StringBuffer sbname = new StringBuffer();

        sbname.append("SYS_");
        sbname.append(type);
        sbname.append('_');
        sbname.append(++sysNumber);

        return new HsqlName(sbname.toString(), false);
    }

    public static HsqlName makeAutoName(String type, String namepart) {

        StringBuffer sbname = new StringBuffer();

        sbname.append("SYS_");
        sbname.append(type);
        sbname.append('_');
        sbname.append(namepart);
        sbname.append('_');
        sbname.append(++sysNumber);

        return new HsqlName(sbname.toString(), false);
    }

    public void rename(String name, boolean isquoted) {

        this.name          = name;
        this.statementName = name;
        this.isNameQuoted  = isquoted;

        if (name == null) {
            return;
        }

        if (isNameQuoted) {
            statementName = StringConverter.toQuotedString(name, '"', true);
        }

        if (name.startsWith("SYS_")) {
            int index = name.lastIndexOf('_') + 1;

            try {
                int temp = Integer.parseInt(name.substring(index));

                if (temp > sysNumber) {
                    sysNumber = temp;
                }
            } catch (NumberFormatException e) {}
        }
    }

    public void rename(String prefix, String name, boolean isquoted) {

        StringBuffer sbname = new StringBuffer(prefix);

        sbname.append('_');
        sbname.append(name);
        rename(sbname.toString(), isquoted);
    }

    public boolean equals(HsqlName other) {
        return name.equals(other.name) && isNameQuoted == other.isNameQuoted;
    }

    /**
     * "SYS_IDX_" is used for auto-indexes on referring FK columns or
     * unique constraints.
     * "SYS_PK_" is for the primary key indexes.
     * "SYS_REF_" is for FK constraints in referenced tables
     *
     */
    public static boolean isReservedName(String name) {

        if (name.startsWith("SYS_IDX_") || name.startsWith("SYS_PK_")
                || name.startsWith("SYS_REF_")) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isReservedName() {
        return isReservedName(name);
    }
}
