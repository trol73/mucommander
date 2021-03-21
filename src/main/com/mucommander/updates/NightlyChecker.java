/*
 * This file is part of trolCommander, http://www.trolsoft.ru/en/soft/trolcommander
 * Copyright (C) 2014-2021 Oleg Trifonov
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
package com.mucommander.updates;

import com.mucommander.RuntimeConstants;
import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.FileFactory;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

public class NightlyChecker {

    public static final String ROOT_URL = "http://trolsoft.ru/content/soft/trolcommander/";
    private static final String BUILD_CODE_URL = ROOT_URL + "nightly/buildcode";

    private static String getNightlyBuildCode() {
        AbstractFile file = FileFactory.getFile(BUILD_CODE_URL);
        if (file == null) {
            return null;
        }
        try (InputStream is = file.getInputStream(); DataInputStream reader = new DataInputStream(is)) {
            return reader.readUTF();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean hasUpdates() {
        String current = RuntimeConstants.BUILD_CODE;
        if (current == null) {
            return false;
        }
        String nightly = getNightlyBuildCode();
        if (nightly == null) {
            return false;
        }
        try {
            int currentCode = Integer.parseInt(current);
            int nightlyCode = Integer.parseInt(nightly);
            return nightlyCode > currentCode;
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return false;
        }
    }
}
