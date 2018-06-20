/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2012 Maxence Bernard
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

package com.mucommander.ui.autocomplete.completers.services;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Vector;

/**
 * A <code>PrefixFilter</code> matches strings that start with certain example.
 *
 * @author Arik Hadas
 */

public class CollectionFilter<E> {

    private Collection<String> example;

    /**
     * Hidden constructor
     *
     * @param example example to filter items
     */
    private CollectionFilter(E example) {
        this.example = example != null ? parseExample(example.toString()) : null;

    }

    private static Collection<String> parseExample(String example) {
        final String[] exampleTokens = StringUtils.splitByCharacterTypeCamelCase(example);
        final List<String> ret = new ArrayList<>(exampleTokens.length);
        for (String exampleToken : exampleTokens) {
            final int exampleTokenLength = exampleToken.length();
            if (exampleTokenLength > 1) {
                ret.add(exampleToken.toLowerCase());
            } else if (exampleTokenLength == 1) {
                final String lowerCaseExampleToken = exampleToken.toLowerCase();
                final int type = Character.getType(lowerCaseExampleToken.charAt(0));
                if (type == Character.LOWERCASE_LETTER || type == Character.DECIMAL_DIGIT_NUMBER) {
                    ret.add(lowerCaseExampleToken);
                }
            }
        }
        return ret;

    }

    /**
     * @param prefix - The example to filter.
     * @return A filter of the given example.
     */
    public static <E> CollectionFilter<E> createFilter(E prefix) {
        return new CollectionFilter<>(prefix);
    }

    /**
     * @param input - Some item.
     * @return <code>true</code> if the given input was accepted by this filter, <code>false</code> otherwise.
     */
    private boolean accept(E input) {
        if (example == null || input == null) {
            return true;
        }
        final String lowerCaseInput = input.toString().toLowerCase();
        if (example.size() == 1) {
            final String example_ = example.iterator().next();
            return lowerCaseInput.contains(example_);
        } else {
            return example.stream().allMatch(lowerCaseInput::contains);
        }
    }

    /**
     * Convenient method that filters out items according to example.
     *
     * @param items - Array of items.
     * @return Vector of items which correspond to a given example.
     */
    public Vector<E> filter(E[] items) {
        final Vector<E> result = new Vector<>();
        Arrays.stream(items).filter(this::accept).forEach(result::add);
        return result;
    }

    /**
     * Convenient method that filters out items according to filter's example.
     *
     * @param items - Vector of items.
     * @return Vector of items which correspond to a given example.
     */
    @SuppressWarnings("unchecked")
    public Vector<E> filter(Vector<E> items) {
        return filter((E[]) items.toArray());
    }

}
