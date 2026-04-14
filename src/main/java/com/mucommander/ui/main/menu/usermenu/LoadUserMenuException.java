/*
 * This file is part of trolCommander, http://www.trolsoft.ru/en/trolcommander
 * Copyright (C) 2014-2026 Oleg Trifonov
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
package com.mucommander.ui.main.menu.usermenu;

import lombok.Getter;

public class LoadUserMenuException extends Exception {
    @Getter
    private final int line;

    @Getter
    private final int column;

    public LoadUserMenuException(String message) {
        super(message);
        line = parseLine(message);
        column = parseColumn(message);
    }

    private static int parseLine(String message) {
        return parseInt(message, ", line ");
    }

    private static int parseColumn(String message) {
        return parseInt(message, ", column ");
    }

    private static int parseInt(String message, String attrName) {
        int indexStart = message.indexOf(attrName);
        if (indexStart < 0) {
            return -1;
        }
        indexStart += attrName.length();
        int indexEnd1 = message.indexOf(',', indexStart);
        int indexEnd2 = message.indexOf(':', indexStart);
        int indexEnd = indexEnd1 > 0 && indexEnd1 < indexEnd2 ? indexEnd1 : indexEnd2;
        if (indexEnd < 0) {
            indexEnd = message.length() - 1;
        }
        try {
            return Integer.parseInt(message.substring(indexStart, indexEnd));
        } catch (Exception e) {
            return -1;
        }

    }
}