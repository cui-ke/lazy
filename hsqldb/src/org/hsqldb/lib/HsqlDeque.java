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


package org.hsqldb.lib;

import java.util.Enumeration;
import java.util.NoSuchElementException;

// fredt@users 20020130 - patch 1.7.0 by fredt - new class

/**
 * jdk 1.1 compatible minimal implementation of a list object suitable for
 * stack, queue and deque usage patterns backed by an Object[].
 * The memory footprint of the HsqlDeque doubles when it gets full
 * but does not shrink when it gets empty.
 *
 * @author fredt@users
 * @version 1.7.0
 */
public class HsqlDeque {

    private Object[] list;
    private int      firstindex = 0;                   // index of first list element
    private int      endindex   = 0;                   // index of last list element + 1

    // can grow to fill list
    // if usedsize == 0 then firstindex == endindex
    private int       usedsize                 = 0;    // current number of elements
    private final int DEFAULT_INITIAL_CAPACITY = 10;

    public HsqlDeque() {
        list = new Object[DEFAULT_INITIAL_CAPACITY];
    }

    public int size() {
        return usedsize;
    }

    public Object getFirst() throws NoSuchElementException {

        if (usedsize == 0) {
            throw new NoSuchElementException();
        }

        return list[firstindex];
    }

    public Object getLast() throws NoSuchElementException {

        if (usedsize == 0) {
            throw new NoSuchElementException();
        }

        return list[endindex - 1];
    }

    public Object get(int i) throws IndexOutOfBoundsException {

        int index = getInternalIndex(i);

        return list[index];
    }

    public Object set(int i, Object o) throws IndexOutOfBoundsException {

        int    index  = getInternalIndex(i);
        Object result = list[index];

        list[index] = o;

        return result;
    }

    public Object removeFirst() throws NoSuchElementException {

        if (usedsize == 0) {
            throw new NoSuchElementException();
        }

        Object o = list[firstindex];

        list[firstindex] = null;

        firstindex++;
        usedsize--;

        if (usedsize == 0) {
            firstindex = endindex = 0;
        } else if (firstindex == list.length) {
            firstindex = 0;
        }

        return o;
    }

    public Object removeLast() throws NoSuchElementException {

        if (usedsize == 0) {
            throw new NoSuchElementException();
        }

        endindex--;

        Object o = list[endindex];

        list[endindex] = null;

        usedsize--;

        if (usedsize == 0) {
            firstindex = endindex = 0;
        } else if (endindex == 0) {
            endindex = list.length;
        }

        return o;
    }

/*
    public Object remove(int i){
        return get(i);
    }

    public void add(int i, Object o) {

    }
*/
    public boolean add(Object o) {

        resetCapacity();

        if (endindex == list.length) {
            endindex = 0;
        }

        list[endindex] = o;

        usedsize++;
        endindex++;

        return true;
    }

    public boolean addLast(Object o) {
        return add(o);
    }

    public boolean addFirst(Object o) {

        resetCapacity();

        firstindex--;

        if (firstindex < 0) {
            firstindex = list.length - 1;

            if (endindex == 0) {
                endindex = list.length;
            }
        }

        list[firstindex] = o;

        usedsize++;

        return true;
    }

    public void clear() {

        firstindex = endindex = usedsize = 0;

        for (int i = 0; i < list.length; i++) {
            list[i] = null;
        }
    }

    public boolean isEmpty() {
        return usedsize == 0;
    }

    private int getInternalIndex(int i) throws IndexOutOfBoundsException {

        if (i < 0 || i >= usedsize) {
            throw new IndexOutOfBoundsException();
        }

        int index = firstindex + i;

        if (index >= list.length) {
            index -= list.length;
        }

        return index;
    }

// based on code by dnordahl@users
    public Enumeration elements() {

        Enumeration enum = new Enumeration() {

            private int currentIndex = 0;

            public Object nextElement() {

                if (!hasMoreElements()) {
                    throw new NoSuchElementException("Enumeration complete");
                }

                return get(currentIndex++);
            }

            public boolean hasMoreElements() {
                return usedsize > currentIndex;
            }
        };

        return enum;
    }

    private void resetCapacity() {

        if (usedsize < list.length) {
            return;
        }

        // essential to at least double the capacity for the loop to work
        Object[] newList = new Object[list.length * 2];

        for (int i = 0; i < list.length; i++) {
            newList[i] = list[i];
        }

        list    = newList;
        newList = null;

        if (endindex <= firstindex) {
            int tail = firstindex + usedsize - endindex;

            for (int i = 0; i < endindex; i++) {
                list[tail + i] = list[i];
                list[i]        = null;
            }

            endindex = firstindex + usedsize;
        }
    }
/*
    public static void main(String[] args) {

        HsqlDeque d = new HsqlDeque();

        for (int i = 0; i < 9; i++) {
            d.add(new Integer(i));
        }

        d.removeFirst();
        d.removeFirst();
        d.add(new Integer(9));
        d.add(new Integer(10));

        for (int i = 0; i < d.size(); i++) {
            System.out.println(d.get(i));
        }

        System.out.println();
        d.add(new Integer(11));
        d.add(new Integer(12));

        for (int i = 0; i < d.size(); i++) {
            System.out.println(d.get(i));
        }

        d.addFirst(new Integer(1));
        d.addFirst(new Integer(0));
        d.addFirst(new Integer(-1));
        d.addFirst(new Integer(-2));

        for (int i = 0; i < d.size(); i++) {
            System.out.println(d.get(i));
        }

        System.out.println();
        d.removeFirst();
        d.removeFirst();
        d.removeFirst();

        for (int i = 0; i < d.size(); i++) {
            System.out.println(d.get(i));
        }

        System.out.println();

        Enumeration en = d.elements();

        for (; en.hasMoreElements(); ) {
            System.out.println(en.nextElement());
        }
    }
*/
}
