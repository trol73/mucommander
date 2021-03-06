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

package com.mucommander.commons.file.filter;

import com.mucommander.commons.util.StringUtils;

/**
 * This filter matches files whose criterion values are equal to one of several specified extensions.
 *
 * <p>The extension(s) may be any string, but when used in the traditional sense of a file extension (e.g. zip extension)
 * the '.' character must be included in the specified extension (e.g. ".zip" must be used, not just "zip").
 *
 * @author Maxence Bernard, Nicolas Rinaudo
 */
public class AbstractExtensionFilter extends AbstractStringCriterionFilter {

    /** File extensions to match against criterion values */
    private final char[][] extensions;

    /**
     * Creates a new <code>AbstractExtensionFilter</code> using the specified generator and string, and operating in the
     * specified mode.
     *
     * @param generator generates criterion values for files as requested
     * @param extensions the extensions to compare criterion values against
     * @param caseSensitive if true, this filter will be case-sensitive
     * @param inverted if true, this filter will operate in inverted mode.
     */
    AbstractExtensionFilter(CriterionValueGenerator<String> generator, String[] extensions, boolean caseSensitive, boolean inverted) {
        super(generator, caseSensitive, inverted);

        this.extensions = new char[extensions.length][];
        for (int i = 0; i < extensions.length; i++) {
            this.extensions[i] = extensions[i].toCharArray();
        }
    }


    @Override
    public boolean accept(String value) {
        return isCaseSensitive() ? containsCaseSensitive(value) : containsIgnoreCase(value);
    }

    private boolean containsIgnoreCase(String value) {
        int len = value.length();
        for (char[] extension : extensions) {
            if (StringUtils.matchesIgnoreCase(value, extension, len)) {
                return true;
            }
        }
        return false;
    }

    private boolean containsCaseSensitive(String value) {
        int len = value.length();
        // If case isn't important, a simple String.endsWith is enough.
        for (char[] extension : extensions) {
            if (StringUtils.matches(value, extension, len)) {
                return true;
            }
        }
        return false;
    }
}
