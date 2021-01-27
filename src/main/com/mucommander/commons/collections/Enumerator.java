/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2010 Maxence Bernard
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.mucommander.commons.collections;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Converts an <code>Enumeration</code> into an <code>Iterator</code>.
 * @author Nicolas Rinaudo
 */
public class Enumerator<T> implements Iterator<T> {

    /** Enumeration wrapped by this <code>Enumerator</code>. */
    private final Enumeration<T> enumeration;


    /**
     * Creates a new enumerator from the specified enumeration.
     * @param e enumeration that needs to be treated as an iterator.
     */
    public Enumerator(Enumeration<T> e) {
        enumeration = e;
    }



    /**
     * Returns <code>true</code> if the iterator has more elements.
     * (In other words, returns <code>true</code> if {@link #next() next} would return an element rather than throwing an exception.)
     * @return <code>true</code> if the iterator has more elements, <code>false</code> otherwise.
     */
    @Override
    public boolean hasNext() {
        return enumeration.hasMoreElements();
    }

    /**
     * Returns the next element in the iteration.
     * @return                        the next element in the iteration.
     * @throws NoSuchElementException if there is no next element in the iteration.
     */
    @Override
    public T next() throws NoSuchElementException {
        return enumeration.nextElement();
    }

    /**
     * Operation not supported.
     * @throws UnsupportedOperationException whenever this method is called.
     */
    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
}
