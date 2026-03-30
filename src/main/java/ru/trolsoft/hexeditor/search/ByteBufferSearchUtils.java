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
package ru.trolsoft.hexeditor.search;

import ru.trolsoft.hexeditor.data.AbstractByteBuffer;

import java.io.IOException;

/**
 * Search in AbstractByteBuffer
 */
public class ByteBufferSearchUtils {

    /**
     * Returns the offset within the ByteBuffer of the first occurrence of the specified data, starting at the specified offset.
     *
     * @param data buffer for search
     * @param pattern the data to search for
     * @param fromOffset the offset from which to start the search
     * @return the offset of the first occurrence of the specified data, at the specified offset, or -1 if there is no such occurrence
     */
    public static long indexOf(AbstractByteBuffer data, byte[] pattern, long fromOffset) throws IOException {
        if (data == null || pattern == null || fromOffset < 0) {
            return -1;
        }
        long fileSize = data.getFileSize();
        if (fileSize <= 0 || pattern.length == 0 || pattern.length > fileSize) {
            return -1;
        }
        fromOffset = Math.min(fromOffset, fileSize - 1);

        int[] failure = computeFailure(pattern);
        AbstractByteBuffer.CacheStrategy cacheStrategy = data.getCacheStrategy();
        data.setCacheStrategy(AbstractByteBuffer.CacheStrategy.FORWARD);

        try {
            int j = 0;
            for (long i = fromOffset; i <= fileSize - 1; i++) {
                byte currentByte = data.getByte(i);
                while (j > 0 && pattern[j] != currentByte) {
                    j = failure[j - 1];
                }
                if (pattern[j] == currentByte) {
                    j++;
                }
                if (j == pattern.length) {
                    return i - pattern.length + 1;
                }
            }
            return -1;
        } finally {
            data.setCacheStrategy(cacheStrategy);
        }
    }

    /**
     * Searches for the first occurrence of the specified patterns in the buffer,
     * starting at the specified offset.
     *
     * @param data buffer for search
     * @param patterns array of patterns to search for
     * @param fromOffset the offset from which to start the search
     * @return the offset of the first occurrence of any pattern, or -1 if none found
     */
    public static long indexOf(AbstractByteBuffer data, byte[][] patterns, long fromOffset) throws IOException {
        if (data == null || patterns == null || fromOffset < 0) {
            return -1;
        }
        long fileSize = data.getFileSize();
        if (fileSize <= 0) {
            return -1;
        }
//        fromOffset = Math.min(fromOffset, fileSize - 1);
        long earliestMatch = -1;

        AbstractByteBuffer.CacheStrategy originalStrategy = data.getCacheStrategy();
        data.setCacheStrategy(AbstractByteBuffer.CacheStrategy.FORWARD);

        try {
            for (byte[] pattern : patterns) {
                if (pattern == null || pattern.length == 0 || pattern.length > fileSize) {
                    continue;
                }
                long match = indexOf(data, pattern, fromOffset);

                if (match != -1) {
                    if (earliestMatch == -1 || match < earliestMatch) {
                        earliestMatch = match;
                        // Оптимизация: если нашли в starting offset, раньше быть не может
                        if (earliestMatch == fromOffset) {
                            return earliestMatch;
                        }
                    }
                }
            }
        } finally {
            data.setCacheStrategy(originalStrategy);
        }

        return earliestMatch;
    }

    public static long indexOfBackward(AbstractByteBuffer data, byte[] pattern, long fromOffset) throws IOException {
        if (data == null || pattern == null || fromOffset < 0) {
            return -1;
        }
        long fileSize = data.getFileSize();
        if (fileSize <= 0 || pattern.length == 0 || pattern.length > fileSize) {
            return -1;
        }
        fromOffset = Math.min(fromOffset, fileSize - 1);
        byte[] patternInvert = new byte[pattern.length];
        for (int i = 0; i < pattern.length; i++) {
            patternInvert[i] = pattern[pattern.length-i-1];
        }
        int[] failure = computeFailure(patternInvert);
        AbstractByteBuffer.CacheStrategy cacheStrategy = data.getCacheStrategy();
        data.setCacheStrategy(AbstractByteBuffer.CacheStrategy.BACKWARD);
        try {
            int j = 0;
            for (long i = fromOffset; i >= 0; i--) {
                while (j > 0 && patternInvert[j] != data.getByte(i)) {
                    j = failure[j - 1];
                }
                if (patternInvert[j] == data.getByte(i)) {
                    j++;
                }
                if (j == pattern.length) {
                    return i;
                }
            }
            return -1;
        } finally {
            data.setCacheStrategy(cacheStrategy);
        }
    }



    /**
     * Knuth-Morris-Pratt Algorithm for Pattern Matching
     * Finds the first occurrence of the pattern in the text.
     */
    public static int indexOf(byte[] data, byte[] pattern) {
        if (data == null || pattern == null || pattern.length == 0) {
            return -1;
        }
        if (pattern.length > data.length) {
            return -1;
        }
        int[] failure = computeFailure(pattern);

        int j = 0;

        for (int i = 0; i < data.length; i++) {
            while (j > 0 && pattern[j] != data[i]) {
                j = failure[j - 1];
            }
            if (pattern[j] == data[i]) {
                j++;
            }
            if (j == pattern.length) {
                return i - pattern.length + 1;
            }
        }
        return -1;
    }

    /**
     * Computes the failure function using a bootstrapping process,
     * where the pattern is matched against itself.
     */
    private static int[] computeFailure(byte[] pattern) {
        int[] failure = new int[pattern.length];

        int j = 0;
        for (int i = 1; i < pattern.length; i++) {
            while (j > 0 && pattern[j] != pattern[i]) {
                j = failure[j - 1];
            }
            if (pattern[j] == pattern[i]) {
                j++;
            }
            failure[i] = j;
        }
        return failure;
    }


}
