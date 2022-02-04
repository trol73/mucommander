/*
 * This file is part of trolCommander, http://www.trolsoft.ru/en/soft/trolcommander
 * Copyright (C) 2013-2017 Oleg Trifonov
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
        VER_1_9(53, 0, "1.9"),
        VER_1_10(54, 0, "1.10"),
        VER_1_11(55, 0, "1.11"),
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
