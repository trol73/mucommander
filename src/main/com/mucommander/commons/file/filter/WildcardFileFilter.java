/*
 * This file is part of trolCommander, http://www.trolsoft.ru/soft/trolcommander
 * Copyright (C) 2013-2014 Oleg Trifonov
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.mucommander.commons.file.filter;

import java.io.File;
import org.apache.commons.io.IOCase;


/**
 * This filter matches files whose string criterion values correspond a specified wildcard mask (with '*' and/or '?' characters).
 *
 * @author Oleg Trifonov
 */
public class WildcardFileFilter extends AbstractStringCriterionFilter {
    private org.apache.commons.io.filefilter.AbstractFileFilter fileFilter;

    /**
     * Creates a new case-insensitive <code>WildcardFileFilter</code> operating in non-inverted mode.
     *
     * @param s the wildcard to match
     */
    public WildcardFileFilter(String s) {
        this(s, false, false);
    }

    /**
     * Creates a new <code>WildcardFileFilter</code> operating in non-inverted mode.
     *
     * @param s the wildcard to match
     * @param caseSensitive if true, this FilenameFilter will be case-sensitive
     */
    public WildcardFileFilter(String s, boolean caseSensitive) {
        this(s, caseSensitive, false);
    }

    /**
     * Creates a new <code>WildcardFileFilter</code> operating in the specified mode.
     *
     * @param s the wildcard to match
     * @param caseSensitive if true, this FilenameFilter will be case-sensitive
     * @param inverted if true, this filter will operate in inverted mode.
     */
    public WildcardFileFilter(String s, boolean caseSensitive, boolean inverted) {
        super(new FilenameGenerator(), caseSensitive, inverted);
        this.fileFilter = new org.apache.commons.io.filefilter.WildcardFileFilter(s, isCaseSensitive() ? IOCase.SENSITIVE : IOCase.INSENSITIVE);
    }


    @Override
    public boolean accept(String value) {
        return fileFilter.accept(new File(value));
    }
}
