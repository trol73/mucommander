/*
 * Copyright 2000-2014 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bulenkov.iconloader.util;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author Konstantin Bulenkov
 */
public class StringUtil {
    public static List<String> split(String s, String separator) {
        return split(s, separator, true);
    }

    public static List<String> split(String s, String separator,
                                     boolean excludeSeparator) {
        return split(s, separator, excludeSeparator, true);
    }

    public static List<String> split(String s, String separator,
                                     boolean excludeSeparator, boolean excludeEmptyStrings) {
        if (separator.isEmpty()) {
            return Collections.singletonList(s);
        }
        List<String> result = new ArrayList<String>();
        int pos = 0;
        while (true) {
            int index = s.indexOf(separator, pos);
            if (index == -1) break;
            final int nextPos = index + separator.length();
            String token = s.substring(pos, excludeSeparator ? index : nextPos);
            if (!token.isEmpty() || !excludeEmptyStrings) {
                result.add(token);
            }
            pos = nextPos;
        }
        if (pos < s.length() || (!excludeEmptyStrings && pos == s.length())) {
            result.add(s.substring(pos, s.length()));
        }
        return result;
    }

    public static int indexOfIgnoreCase(String where, String what, int fromIndex) {
        int targetCount = what.length();
        int sourceCount = where.length();

        if (fromIndex >= sourceCount) {
            return targetCount == 0 ? sourceCount : -1;
        }

        if (fromIndex < 0) {
            fromIndex = 0;
        }

        if (targetCount == 0) {
            return fromIndex;
        }

        char first = what.charAt(0);
        int max = sourceCount - targetCount;

        for (int i = fromIndex; i <= max; i++) {
      /* Look for first character. */
            if (!charsEqualIgnoreCase(where.charAt(i), first)) {
                while (++i <= max && !charsEqualIgnoreCase(where.charAt(i), first)) ;
            }

      /* Found first character, now look at the rest of v2 */
            if (i <= max) {
                int j = i + 1;
                int end = j + targetCount - 1;
                for (int k = 1; j < end && charsEqualIgnoreCase(where.charAt(j), what.charAt(k)); j++, k++) ;

                if (j == end) {
          /* Found whole string. */
                    return i;
                }
            }
        }

        return -1;
    }


    public static int indexOfIgnoreCase(String where, char what, int fromIndex) {
        int sourceCount = where.length();

        if (fromIndex >= sourceCount) {
            return -1;
        }

        if (fromIndex < 0) {
            fromIndex = 0;
        }

        for (int i = fromIndex; i < sourceCount; i++) {
            if (charsEqualIgnoreCase(where.charAt(i), what)) {
                return i;
            }
        }

        return -1;
    }

    public static boolean containsIgnoreCase(String where, String what) {
        return indexOfIgnoreCase(where, what, 0) >= 0;
    }

    public static boolean charsEqualIgnoreCase(char a, char b) {
        return a == b || toUpperCase(a) == toUpperCase(b) || toLowerCase(a) == toLowerCase(b);
    }

    public static char toUpperCase(char a) {
        if (a < 'a') {
            return a;
        }
        if (a <= 'z') {
            return (char) (a + ('A' - 'a'));
        }
        return Character.toUpperCase(a);
    }

    public static char toLowerCase(char a) {
        if (a < 'A' || a >= 'a' && a <= 'z') {
            return a;
        }

        if (a <= 'Z') {
            return (char) (a + ('a' - 'A'));
        }

        return Character.toLowerCase(a);
    }

    public static int compareVersionNumbers(String v1, String v2) {
        if (v1 == null && v2 == null) {
            return 0;
        }
        if (v1 == null) {
            return -1;
        }
        if (v2 == null) {
            return 1;
        }

        String[] part1 = v1.split("[\\.\\_\\-]");
        String[] part2 = v2.split("[\\.\\_\\-]");

        int idx = 0;
        for (; idx < part1.length && idx < part2.length; idx++) {
            String p1 = part1[idx];
            String p2 = part2[idx];

            int cmp;
            if (p1.matches("\\d+") && p2.matches("\\d+")) {
                cmp = new Integer(p1).compareTo(new Integer(p2));
            } else {
                cmp = part1[idx].compareTo(part2[idx]);
            }
            if (cmp != 0) return cmp;
        }

        if (part1.length == part2.length) {
            return 0;
        } else {
            boolean left = part1.length > idx;
            String[] parts = left ? part1 : part2;

            for (; idx < parts.length; idx++) {
                String p = parts[idx];
                int cmp;
                if (p.matches("\\d+")) {
                    cmp = new Integer(p).compareTo(0);
                } else {
                    cmp = 1;
                }
                if (cmp != 0) return left ? cmp : -cmp;
            }
            return 0;
        }
    }

    public static boolean startsWithChar(CharSequence s, char prefix) {
        return s != null && s.length() != 0 && s.charAt(0) == prefix;
    }

    public static boolean endsWithChar(CharSequence s, char suffix) {
        return s != null && s.length() != 0 && s.charAt(s.length() - 1) == suffix;
    }


    public static String stripQuotesAroundValue(String text) {
        if (startsWithChar(text, '\"') || startsWithChar(text, '\'')) text = text.substring(1);
        if (endsWithChar(text, '\"') || endsWithChar(text, '\'')) text = text.substring(0, text.length() - 1);
        return text;
    }

    /**
     * Equivalent to string.startsWith(prefixes[0] + prefixes[1] + ...) but avoids creating an object for concatenation.
     */
    public static boolean startsWithConcatenation(String string, String... prefixes) {
        int offset = 0;
        for (String prefix : prefixes) {
            int prefixLen = prefix.length();
            if (!string.regionMatches(offset, prefix, 0, prefixLen)) {
                return false;
            }
            offset += prefixLen;
        }
        return true;
    }

    public static String getFileExtension(String fileName) {
        int index = fileName.lastIndexOf('.');
        if (index < 0) return "";
        return fileName.substring(index + 1);
    }

    public static String getFileNameWithoutExtension(String name) {
        int i = name.lastIndexOf('.');
        if (i != -1) {
            name = name.substring(0, i);
        }
        return name;

    }

    @NotNull
    public static String join(@NotNull Collection<String> strings, @NotNull String separator) {
        if (strings.size() <= 1) {
            return notNullize(getFirstItem(strings));
        }
        StringBuilder result = new StringBuilder();
        join(strings, separator, result);
        return result.toString();
    }

    public static String join(@NotNull Iterable<?> items, @NotNull @NonNls String separator) {
        StringBuilder result = new StringBuilder();
        for (Object item : items) {
            result.append(item).append(separator);
        }
        if (result.length() > 0) {
            result.setLength(result.length() - separator.length());
        }
        return result.toString();
    }


    @NotNull
    public static String notNullize(@Nullable final String s) {
        return notNullize(s, "");
    }

    @NotNull
    public static String notNullize(@Nullable final String s, @NotNull String defaultValue) {
        return s == null ? defaultValue : s;
    }

    public static void join(@NotNull Collection<String> strings, @NotNull String separator, @NotNull StringBuilder result) {
        boolean isFirst = true;
        for (String string : strings) {
            if (string != null) {
                if (isFirst) {
                    isFirst = false;
                } else {
                    result.append(separator);
                }
                result.append(string);
            }
        }
    }

    @Nullable
    public static <T> T getFirstItem(@Nullable Collection<T> items) {
        return items == null || items.isEmpty() ? null : items.iterator().next();
    }

}
