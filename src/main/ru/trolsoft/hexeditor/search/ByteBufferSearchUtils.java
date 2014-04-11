/*
 * This file is part of muCommander, http://www.mucommander.com
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
        long fileSize = data.getFileSize();
        if (fileSize <= 0) {
            return -1;
        }
        int[] failure = computeFailure(pattern);
        AbstractByteBuffer.CacheStrategy cacheStrategy = data.getCacheStrategy();
        data.setCacheStrategy(AbstractByteBuffer.CacheStrategy.FORWARD);

        int j = 0;
        for (long i = fromOffset; i < fileSize; i++) {
            while (j > 0 && pattern[j] != data.getByte(i)) {
                j = failure[j - 1];
            }
            if (pattern[j] == data.getByte(i)) {
                j++;
            }
            if (j == pattern.length) {
                data.setCacheStrategy(cacheStrategy);
                return i - pattern.length + 1;
            }
        }
        data.setCacheStrategy(cacheStrategy);
        return -1;
    }


    public static long indexOfBackward(AbstractByteBuffer data, byte[] pattern, long fromOffset) throws IOException {
        byte[] patternInvert = new byte[pattern.length];
        for (int i = 0; i < pattern.length; i++) {
            patternInvert[i] = pattern[pattern.length-i-1];
        }
        int[] failure = computeFailure(patternInvert);
        AbstractByteBuffer.CacheStrategy cacheStrategy = data.getCacheStrategy();
        data.setCacheStrategy(AbstractByteBuffer.CacheStrategy.BACKWARD);

        int j = 0;
        for (long i = fromOffset; i >= 0; i--) {
            while (j > 0 && patternInvert[j] != data.getByte(i)) {
                j = failure[j - 1];
            }
            if (pattern[j] == data.getByte(i)) {
                j++;
            }
            if (j == pattern.length) {
                data.setCacheStrategy(cacheStrategy);
                return i - pattern.length + 1;
            }
        }
        data.setCacheStrategy(cacheStrategy);
        return -1;
    }



    /**
     * Knuth-Morris-Pratt Algorithm for Pattern Matching
     * Finds the first occurrence of the pattern in the text.
     */
    public static int indexOf(byte[] data, byte[] pattern) {
        int[] failure = computeFailure(pattern);

        int j = 0;
        if (data.length == 0) {
            return -1;
        }

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
     * Computes the failure function using a boot-strapping process,
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
