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
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.mucommander.commons;

import com.mucommander.commons.file.DummyFile;
import com.mucommander.commons.file.FileURL;
import com.mucommander.commons.runtime.OsFamily;

/**
 *
 *  This class is a child of <code>DummyFile</code> with overwritten {@link #toString()}} method that return a "nice" value
 *  for local files (without 'file://localhost/')
 *
 *  @author Oleg Trifonov
 */
public class DummyDecoratedFile extends DummyFile {

    private static final String LOCAL_FILE_PREFIX;

    static {
        String prefix = "file://" + FileURL.LOCALHOST;
        LOCAL_FILE_PREFIX = OsFamily.WINDOWS.isCurrent() ? prefix + '/' : prefix;
    }

    public DummyDecoratedFile(FileURL url) {
        super(url);
    }

    @Override
    public String toString() {
        String result = super.toString();
        if (result.startsWith(LOCAL_FILE_PREFIX)) {
            return result.substring(LOCAL_FILE_PREFIX.length());
        }
        return result;
    }
}
