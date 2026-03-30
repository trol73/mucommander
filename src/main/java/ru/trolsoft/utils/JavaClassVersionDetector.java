/*
 * This file is part of trolCommander, http://www.trolsoft.ru/en/soft/trolcommander
 * Copyright (C) 2013-2026 Oleg Trifonov
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

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Oleg Trifonov
 * Created on 19/01/17.
 */
public class JavaClassVersionDetector {

    private int major;
    private int minor;
    private final Version version;


    public enum Version {
        WRONG_FORMAT(-1, -1, "wrong"),
        VER_1_0(45, 0, "1.0"),
        VER_1_1(45, 3, "1.1"),
        VER_1_2(46, 0, "1.2"),
        VER_1_3(47, 0, "1.3"),
        VER_1_4(48, 0, "1.4"),
        VER_1_5(49, 0, "1.5"),
        VER_1_6(50, 0, "1.6"),
        VER_1_7(51, 0, "1.7"),
        VER_1_8(52, 0, "1.8"),
        VER_9(53, 0, "9"),
        VER_10(54, 0, "10"),
        VER_11(55, 0, "11 (LTS)"),
        VER_12(56, 0, "12"),
        VER_13(57, 0, "13"),
        VER_14(58, 0, "14"),
        VER_15(59, 0, "15"),
        VER_16(60, 0, "16"),
        VER_17(61, 0, "17 (LTS)"),
        VER_18(62, 0, "18"),
        VER_19(63, 0, "19"),
        VER_20(64, 0, "20"),
        VER_21(65, 0, "21 (LTS)"),
        VER_22(66, 0, "22"),
        VER_23(67, 0, "23"),
        VER_24(68, 0, "24"),
        VER_25(69, 0, "25 (LTS)"),
        VER_26(70, 0, "26"),

        UNKNOWN(-1, -1, "unknown");

        private final int major;
        private final int minor;
        public final String name;

        Version(int major, int minor, String name) {
            this.major = major;
            this.minor = minor;
            this.name = name;
        }
    }


    public JavaClassVersionDetector(InputStream is) throws IOException {
        version = process(is);
    }

    private Version process(InputStream is) throws IOException {
        int b1 = is.read();
        int b2 = is.read();
        int b3 = is.read();
        int b4 = is.read();
        if (b1 != 0xCA && b2 != 0xFE && b3 != 0xBA && b4 != 0xBE) {
            return Version.WRONG_FORMAT;
        }
        minor = (is.read() << 8) + is.read();
        major = (is.read() << 8) + is.read();

        for (Version ver : Version.values()) {
            if (ver.minor == minor && ver.major == major) {
                return ver;
            }
        }
        return Version.UNKNOWN;
    }

    public int getMajor() {
        return major;
    }

    public int getMinor() {
        return minor;
    }

    public Version getVersion() {
        return version;
    }

}
