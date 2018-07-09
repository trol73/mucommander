/*
 * This file is part of trolCommander, http://www.trolsoft.ru/en/soft/trolcommander
 * Copyright (C) 2013-2016 Oleg Trifonov
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
package com.mucommander.tools;

import com.mucommander.commons.runtime.OsFamily;

/**
 * @author Oleg Trifonov
 * Created on 09/09/15.
 */
public class FileMergeTool extends ExternalTool {

    @Override
    public boolean isActive() {
        return OsFamily.MAC_OS_X.isCurrent();
    }

    @Override
    boolean detect() {
        String path = "/Applications/Xcode.app/Contents/Applications/FileMerge.app";
        if (checkFileExists(path)) {
            setFullPath(path);
            return true;
        }
        // opendiff <file1> <file2>
        return false;
    }
}
