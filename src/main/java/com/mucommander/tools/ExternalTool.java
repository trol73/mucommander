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

import java.io.File;

/**
 * Base class for external tools.
 *
 * @author Oleg Trifonov
 * Created on 09/09/15.
 */
public abstract class ExternalTool {

    /**
     * Path to application / file
     */
    private String fullPath;

    /**
     *
     * @return true if the tool is available on current OS
     */
    public boolean isActive() {
        return true;
    }

    /**
     * Try to find tools
     * @return true if tool was found
     */
    abstract boolean detect();


    /**
     *
     * @param path full file path
     * @return true if file exists
     */
    protected boolean checkFileExists(String path) {
        return new File(path).exists();
    }

    public String getFullPath() {
        return fullPath;
    }

    public void setFullPath(String fullPath) {
        this.fullPath = fullPath;
    }

}
