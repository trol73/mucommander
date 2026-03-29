/*
 * This file is part of trolCommander, http://www.trolsoft.ru/en/soft/trolcommander
 * Copyright (C) 2013-2018 Oleg Trifonov
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

import java.util.LinkedList;

public class StringStream {
    private final LinkedList<String> lines = new LinkedList<>();
    private StringBuffer lastIncomplete;

    public void add(String s) {
        String[] array = s.split("(?<=\n)");
        for (int i = 0; i < array.length; i++) {
            String line = array[i];
            if (i == array.length-1 && !line.endsWith("\n")) {
                getLastIncomplete().append(line);
            } else if (i == 0 && hasRemains()) {
                lines.add(getRemains() + line);
            } else {
                lines.add(line);
            }
        }
    }

    public boolean hasCompleted() {
        return !lines.isEmpty();
    }

    public String getNext() {
        return lines.pollFirst();
    }

    public String getRemains() {
        if (lastIncomplete != null) {
            String result = lastIncomplete.toString();
            lastIncomplete.delete(0, lastIncomplete.length());
            return result;
        } else {
            return "";
        }
    }

    public boolean hasRemains() {
        return lastIncomplete != null && lastIncomplete.length() > 0;
    }

    private StringBuffer getLastIncomplete() {
        if (lastIncomplete == null) {
            lastIncomplete = new StringBuffer();
        }
        return lastIncomplete;
    }

    public void clear() {
        lines.clear();
        lastIncomplete = null;
    }

}
