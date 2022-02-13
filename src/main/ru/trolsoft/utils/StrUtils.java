/*
 * This file is part of trolCommander, http://www.trolsoft.ru/en/soft/trolcommander
 * Copyright (C) 2013-2020 Oleg Trifonov
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
package ru.trolsoft.utils;

import javax.sql.rowset.spi.SyncResolver;

/**
 * @author Oleg Trifonov
 * Created on 03/04/14.
 */
public class StrUtils {
    private static final char[] HEX_CHAR_ARRAY = "0123456789ABCDEF".toCharArray();
    private static final String[] STRING_OF_ZERO = {"", "0", "00", "000", "0000", "00000", "000000", "0000000", "00000000", "000000000", "0000000000"};

    private static final String[] HEX_BYTE_STRINGS = new String[256];
    private static final String[] BINARY_BYTE_STRINGS = new String[256];
    private static final String[] OCTAL_BYTE_STRINGS = new String[256];

    private static final char UTF_16_BE_MARKER = 0xFEFF;
    private static final char UTF_16_LE_MARKER = 0xFFFE;


    /**
     *
     * @param val
     * @return
     */
    public static String dwordToHexStr(long val) {
        String result = Long.toHexString(val);
        int len = result.length();
        if (len > 8) {
            return result;
        }
        return STRING_OF_ZERO[8-len] + result;
    }


    /**
     *
     * @param b
     * @return
     */
    public static String byteToHexStr(byte b) {
        int v = b & 0xFF;
        String result = HEX_BYTE_STRINGS[v];
        if (result == null) {
            result = Character.toString(HEX_CHAR_ARRAY[v >>> 4]) + HEX_CHAR_ARRAY[v & 0x0f];
            HEX_BYTE_STRINGS[v] = result;
        }
        return result;
    }


    /**
     *
     * @param bytes
     * @param offset
     * @param size
     * @return
     */
    public static String bytesToHexStr(byte[] bytes, int offset, int size) {
        char[] hexChars = new char[size * 2];
        for (int i = offset; i < offset + size; i++) {
            int v = bytes[i] & 0xFF;
            hexChars[i * 2] = HEX_CHAR_ARRAY[v >>> 4];
            hexChars[i * 2 + 1] = HEX_CHAR_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }


    /**
     *
     * @param b
     * @return
     */
    public static String byteToBinaryStr(byte b) {
        int v = b & 0xFF;
        return byteToBinaryStr(v);
    }

    /**
     *
     * @param v
     * @return
     */
    public static String byteToBinaryStr(int v) {
        String result = BINARY_BYTE_STRINGS[v];
        if (result == null) {
            result = Integer.toBinaryString(v);
            result = STRING_OF_ZERO[8 - result.length()] + result;
            BINARY_BYTE_STRINGS[v] = result;
        }
        return result;
    }


    /**
     *
     * @param b
     * @return
     */
    public static String byteToOctalStr(byte b) {
        int v = b & 0xFF;
        return byteToOctalStr(v);
    }

    /**
     *
     * @param v
     * @return
     */
    public static String byteToOctalStr(int v) {
        String result = OCTAL_BYTE_STRINGS[v];
        if (result == null) {
            result = Integer.toOctalString(v);
            result = STRING_OF_ZERO[3 - result.length()] + result;
            OCTAL_BYTE_STRINGS[v] = result;
        }
        return result;
    }


    /**
     *
     * @param bytes
     * @return
     */
    public static String bytesToHexString(byte[] bytes) {
        StringBuilder s = new StringBuilder();
        for (byte aByte : bytes) {
            s.append(StrUtils.byteToHexStr(aByte));
            s.append(' ');
        }
        return s.toString();
    }


    public static byte[] hexStringToBytes(String text) {
//        text = text.replace(" ", "");
//        if (text.length() % 2 == 1) {
//            text = text.substring(0, text.length() - 1) + "0" + text.charAt(text.length() - 1);
//        }
//        return DatatypeConverter.parseHexBinary(text);

        int len = text.length();
        int i = 0;
        char c1 = 0;
        char c2 = 0;
        int outPos = 0;
        byte[] array = new byte[(len+1)/2];
        while (i < len) {
            if (c2 != 0) {
                int b1 = parseNibble(c1);
                int b2 = parseNibble(c2);
                if (b1 < 0 || b2 < 0) {
                    return new byte[0];
                }
                array[outPos++] = (byte)((b1 << 4) + b2);
                c1 = 0;
                c2 = 0;
            }
            char nextNibble = text.charAt(i++);
            if (nextNibble == ' ' || nextNibble == '\t') {
                continue;
            }
            if (c1 == 0) {
                c1 = nextNibble;
            } else {
                c2 = nextNibble;
            }
        }
        if (c1 != 0) {
            int b1 = parseNibble(c1);
            if (b1 < 0) {
                return new byte[0];
            }
            array[outPos++] = (byte)b1;
        }
        if (array.length == outPos) {
            return array;
        } else {
            byte[] result = new byte[outPos];
            System.arraycopy(array, 0, result, 0, outPos);
            return result;
        }
    }

    private static int parseNibble(char c) {
        if (c >= '0' && c <= '9') {
            return c - '0';
        } else if (c >= 'A' && c <= 'F') {
            return c - 'A' + 10;
        } else if (c >= 'a' && c <= 'f') {
            return c - 'a' + 10;
        } else {
            return -1;
        }
    }

    public static boolean hasUtfMarker(String s) {
        if (s == null || s.isEmpty()) {
            return false;
        }
        char firstChar = s.charAt(0);
        return firstChar == UTF_16_BE_MARKER || firstChar == UTF_16_LE_MARKER;
    }

    public static String removeUtfMarker(String s) {
        if (s == null) {
            return null;
        }
        return hasUtfMarker(s) ? s.substring(1) : s;
    }


}
