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

package com.mucommander.commons.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import ru.trolsoft.test.VariableSource;

import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests the {@link StringUtils} class.
 * @author Nicolas Rinaudo
 */
public class StringUtilsTest {

    /**
     * Tests the {@link StringUtils#endsWith(String, char[])} method.
     * @param a        string to compare.
     * @param b        char array to test.
     * @param expected expected return value of {@link StringUtils#endsWith(String, char[])}.
     */

    @ParameterizedTest
    @CsvSource(value = {
        "abc, c,   true",
        "abc, bc,  true",
        "abc, abc, true",
        "abc, '',    true",

        "abc, C,   false",
        "abc, BC,  false",
        "abc, ABC, false",

        "abc, d,   false",
        "abc, de,  false",
        "abc, def, false",
    })
    public void testEndsWith(String a, String b, boolean expected) {
        assertEquals(expected, StringUtils.endsWith(a, b.toCharArray()));
    }



    @ParameterizedTest
    @CsvSource({
        "abc, C, 3, true",
        "abc, B, 2, true",
        "abc, A, 1, true",
        "abc, '',  0, true",

        "abc, ABC, 3, true",
        "abc, AB,  2, true",
        "abc, A,   1, true",
        "abc, '',    0, true",

        "abc, abc, 2, false",

        "abc, 123, 3, false",
        "abc, 123, 2, false",
        "abc, 123, 1, false",
        "abc, 123, 0, false",
    })
    public void testMatchesIgnoreCaseCharArray(String a, String b, int pos, boolean expected) {
        assert StringUtils.matchesIgnoreCase(a, b.toCharArray(), pos) == expected;
    }

    @ParameterizedTest
    @CsvSource({
            "abc, C, 3, true",
            "abc, B, 2, true",
            "abc, A, 1, true",
            "abc, '',  0, true",

            "abc, ABC, 3, true",
            "abc, AB,  2, true",
            "abc, A,   1, true",
            "abc, '',  0, true",

            "abc, abc, 2, false",

            "abc, 123, 3, false",
            "abc, 123, 2, false",
            "abc, 123, 1, false",
            "abc, 123, 0, false",
    })
    public void testMatchesIgnoreCase(String a, String b, int pos, boolean expected) {
        assert StringUtils.matchesIgnoreCase(a, b, pos) == expected;
    }


    /**
     * Tests the {@link StringUtils#matches(String, char[], int)} method.
     * @param a        first string.
     * @param b        second string.
     * @param pos      position at which to start the comparison.
     * @param expected expected return value of {@link StringUtils#matches(String, char[], int)}
     */
    @ParameterizedTest
    @CsvSource({
        "abc, c, 3, true",
        "abc, b, 2, true",
        "abc, a, 1, true",
        "abc, '',  0, true",

        "abc, abc, 3, true",
        "abc, ab,  2, true",
        "abc, a,   1, true",
        "abc, '',    0, true",

        "abc, abc, 2, false",

        "abc, aBC, 3, false",
        "abc, ABC, 2, false",
        "abc, aBc, 1, false",
        "abc, ABc, 0, false",
    })
    public void testMatches(String a, String b, int pos, boolean expected) {
        assert StringUtils.matches(a, b.toCharArray(), pos) == expected;
    }


    /**
     * Tests the {@link StringUtils#parseIntDef(String, int)} method.
     * @param input    string to parse.
     * @param def      default value.
     * @param expected expected return value of {@link StringUtils#parseIntDef(String, int)}.
     */
    @ParameterizedTest
    @CsvSource({
            "0,      0, 0",
            "foobar, 0, 0",
            "1,      0, 1",
            "foobar, 1, 1",
            "2,      0, 2",
            "foobar, 2, 2",
            "3,      0, 3",
            "foobar, 3, 3",
            "4,      0, 4",
            "foobar, 4, 4",
            "5,      0, 5",
            "foobar, 5, 5",
            "6,      0, 6",
            "foobar, 6, 6",
            "7,      0, 7",
            "foobar, 7, 7",
            "8,      0, 8",
            "foobar, 8, 8",
            "9,      0, 9",
            "foobar, 9, 9",
    })
    public void testParseIntDef(String input, int def, int expected) {
        assert StringUtils.parseIntDef(input, def) == expected;
    }


    /**
     * Provides test cases for {@link #testEndsWithIgnoreCaseCharArray(String, String, boolean)} and {@link #testEndsWithIgnoreCase(String, String, boolean)}.
     */
    private static final Arguments[] endsWithIgnoreCaseStringsData = new Arguments[]{
            Arguments.of("this is a test", "a test", true),
            Arguments.of("this is a test", "a TeSt", true),
            Arguments.of("this is a test", "A TEST", true),

            Arguments.of("THIS IS A TEST", "a test", true),
            Arguments.of("THIS IS A TEST", "a TeSt", true),
            Arguments.of("THIS IS A TEST", "A TEST", true),

            Arguments.of("ThIs Is A TeSt", "a test", true),
            Arguments.of("ThIs Is A TeSt", "a TeSt", true),
            Arguments.of("ThIs Is A TeSt", "A TEST", true),

            Arguments.of("this is a test", "this is a test", true),
            Arguments.of("this is a test", "ThIs Is A TeSt", true),
            Arguments.of("this is a test", "THIS IS A TEST", true),

            Arguments.of("THIS IS A TEST", "this is a test", true),
            Arguments.of("THIS IS A TEST", "ThIs Is A TeSt", true),
            Arguments.of("THIS IS A TEST", "THIS IS A TEST", true),

            Arguments.of("ThIs Is A TeSt", "this is a test", true),
            Arguments.of("ThIs Is A TeSt", "ThIs Is A TeSt", true),
            Arguments.of("ThIs Is A TeSt", "THIS IS A TEST", true),

            Arguments.of("this is a test", "", true),

            Arguments.of("this is a test", "test a is this", false),
            Arguments.of("this is a test", "tEsT a Is ThIs", false),
            Arguments.of("this is a test", "TEST A IS THIS", false),

            Arguments.of("THIS IS A TEST", "test a is this", false),
            Arguments.of("THIS IS A TEST", "tEsT a Is ThIs", false),
            Arguments.of("THIS IS A TEST", "TEST A IS THIS", false),

            Arguments.of("ThIs Is A tEst", "test a is this", false),
            Arguments.of("ThIs Is A tEst", "tEsT a Is ThIs", false),
            Arguments.of("ThIs Is A tEst", "TEST A IS THIS", false)
    };


    public static final Stream<Arguments> endsWithIgnoreCaseStringsSource = Stream.of(endsWithIgnoreCaseStringsData);
    /**
     * Test the {@link StringUtils#endsWithIgnoreCase(String, String)} method.
     * @param a        first string to compare.
     * @param b        second string to compare.
     * @param expected expected return value of {@link StringUtils#endsWithIgnoreCase(String, String)}
     */
    @ParameterizedTest
    @VariableSource("endsWithIgnoreCaseStringsSource")
    public void testEndsWithIgnoreCase(String a, String b, boolean expected) {
        assert StringUtils.endsWithIgnoreCase(a, b) == expected;
    }

    public static final Stream<Arguments> endsWithIgnoreCaseStringsSource2 = Stream.of(endsWithIgnoreCaseStringsData);
    /**
     * Test the {@link StringUtils#endsWithIgnoreCase(String, char[])} method.
     * @param a        first string to compare.
     * @param b        second string to compare.
     * @param expected expected return value of {@link StringUtils#endsWithIgnoreCase(String, char[])}
     */
    @ParameterizedTest
    @VariableSource("endsWithIgnoreCaseStringsSource2")
    public void testEndsWithIgnoreCaseCharArray(String a, String b, boolean expected) {
        assert StringUtils.endsWithIgnoreCase(a, b.toCharArray()) == expected;
    }



    /**
     * Tests {@link StringUtils#startsWithIgnoreCase(String, String)}.
     * @param a        first string to compare.
     * @param b        second string to compare.
     * @param expected expected return value of {@link StringUtils#startsWithIgnoreCase(String, String)}.
     */
    @ParameterizedTest
    @CsvSource({
            "this is a test, this is, true",
            "this is a test, ThIs Is, true",
            "this is a test, THIS IS, true",

            "THIS IS A TEST, this is, true",
            "THIS IS A TEST, ThIs Is, true",
            "THIS IS A TEST, THIS IS, true",

            "ThIs Is a tEsT, this is, true",
            "ThIs Is a tEsT, ThIs Is, true",
            "ThIs Is a tEsT, THIS IS, true",

            "this is a test, this is a test, true",
            "this is a test, THIS IS A TEST, true",
            "this is a test, ThIs Is a tEsT, true",

            "THIS IS A TEST, this is a test, true",
            "THIS IS A TEST, THIS IS A TEST, true",
            "THIS IS A TEST, ThIs Is a tEsT, true",

            "ThIs Is a tEsT, this is a test, true",
            "ThIs Is a tEsT, THIS IS A TEST, true",
            "ThIs Is a tEsT, ThIs Is a tEsT, true",

            "ThIs Is a tEsT, '', true",
            "THIS IS A TEST, '', true",
            "ThIs Is a tEsT, '', true",

            "this is a test, test a is this, false",
            "this is a test, TEST A IS THIS, false",
            "this is a test, TeSt A iS tHiS, false",

            "THIS IS A TEST, test a is this, false",
            "THIS IS A TEST, TEST A IS THIS, false",
            "THIS IS A TEST, TeSt A iS tHiS, false",

            "ThIs Is A tEsT, test a is this, false",
            "ThIs Is A tEsT, TEST A IS THIS, false",
            "ThIs Is A tEsT, TeSt A iS tHiS, false",
    })
    public void testStartsWithIgnoreCase(String a, String b, boolean expected) {
        assert StringUtils.startsWithIgnoreCase(a, b) == expected;
    }



    /**
     * Provides test cases for {@link #testCaseSensitiveEquals(String, String, boolean)}.:
     */
    public static final Stream<Arguments> caseSensitiveEqualsSource = Stream.of(
    );


    /**
     * Tests the {@link StringUtils#equals(String, String, boolean)} method (case insensitive).
     * @param a        first string to compare.
     * @param b        second string to compare
     * @param expected expected return value of {@link StringUtils#equals(String, String, boolean)}
     */
    @ParameterizedTest
    @CsvSource({
            "a,  a,  true",
            "A,  A,  true",
            ",   ,   true",
            ",   a,  false",
            "a,  ,   false",
            "a,  A,  false",
            "A,  a,  false"
    })
    public void testCaseSensitiveEquals(String a, String b, boolean expected) {
        assert StringUtils.equals(a, b, true) == expected;
    }

    /**
     * Tests the {@link StringUtils#equals(String, String, boolean)} method (case sensitive).
     * @param a        first string to compare.
     * @param b        second string to compare
     * @param expected expected return value of {@link StringUtils#equals(String, String, boolean)}
     */
    @ParameterizedTest
    @CsvSource({
        "a,  a,  true",
        "A,  A,  true",
        "a,  A,  true",
        "A,  a,  true",
        ",   ,   true",
        ",   a,  false",
        "a,  ,   false",
        "a,  z,  false"
    })
    public void testCaseInsensitiveEquals(String a, String b, boolean expected) {
        assert StringUtils.equals(a, b, false) == expected;
    }




    /**
     * Tests the {@link StringUtils#capitalize(String)} method.
     * @param input    string to capitalize.
     * @param expected expected result of the capitalization.
     */
    @ParameterizedTest
    @CsvSource({
            "bob,         Bob",
            "BOB,         Bob",
            "bOB,         Bob",
            "boB,         Bob",
            "Bob,         Bob",
            "Bob Servant, Bob servant",
            "b,           B",
            ",            ''",
            ",            ''",
            "7,            7"
    })
    public void testCapitalize(String input, String expected) {
        assert expected.equals(StringUtils.capitalize(input));
    }



    /**
     * Provides test cases for {@link #testFlatten(String, String[], String)}.
     */
    public static final Stream<Arguments> flattenSource = Stream.of(
        Arguments.of("a b c", new String[] {"a", "b", "c"}, " "),
        Arguments.of("a*b*c", new String[] {"a", "b", "c"}, "*"),
        Arguments.of("a*c", new String[] {"a", "", "c"}, "*"),
        Arguments.of("a*c", new String[] {"a", null, "c"}, "*"),
        Arguments.of("b", new String[] {null, "b", null}, "*"),
        Arguments.of("", new String[] {null, null, null}, "*")
);

    /**
     * Tests {@link StringUtils#flatten(String[], String)}.
     * @param expected  expected returned value of {@link StringUtils#flatten(String[], String)}.
     * @param data      data to flatten.
     * @param separator separator to use when flattening.
     */
    @ParameterizedTest
    @VariableSource("flattenSource")
    public void testFlatten(String expected, String[] data, String separator) {
        assert expected.equals(StringUtils.flatten(data, separator));
        assert !separator.equals(" ") || expected.equals(StringUtils.flatten(data));
    }

    /**
     * Tests {@link StringUtils#flatten(String[], String)}
     */
    @Test
    public void testFlatten() {
        assert StringUtils.flatten(null, "*") == null;
        assert StringUtils.flatten(null) == null;
    }
}
