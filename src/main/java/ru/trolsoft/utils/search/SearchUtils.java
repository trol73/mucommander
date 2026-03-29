/*
 * This file is part of trolCommander, http://www.trolsoft.ru/en/soft/trolcommander
 * Copyright (C) 2013-2016 Oleg Trifonov
 *
 * trolCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * trolCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ru.trolsoft.utils.search;


/**
 * @author Oleg Trifonov
 * Created on 16/11/14.
 */
public class SearchUtils {


    public static long indexOf(SearchSourceStream source, SearchPattern pattern) throws SearchException {
        if (!source.hasNext() || pattern.length() == 0) {
            return -1;
        }
        int[] failure = computeFailure(pattern);

        int j = 0;
        long i = 0;
        while (source.hasNext()) {
            int b = source.next();
            i++;
            while (j > 0 && !pattern.checkByte(j, b)) {
                j = failure[j - 1];
            }
            if (pattern.checkByte(j, b)) {
                j++;
            }
            if (j == pattern.length()) {
                return i - pattern.length() + 1;
            }
        }
        source.close();
        return -1;
    }

    /**
     * Computes the failure function using a boot-strapping process,
     * where the pattern is matched against itself.
     */
    private static int[] computeFailure(SearchPattern pattern) {
        int[] failure = new int[pattern.length()];

        int j = 0;
        for (int i = 1; i < pattern.length(); i++) {
            while (j > 0 && !pattern.checkSelf(i, j)) {
                j = failure[j - 1];
            }
            if (pattern.checkSelf(i, j)) {
                j++;
            }
            failure[i] = j;
        }
        return failure;
    }
}
