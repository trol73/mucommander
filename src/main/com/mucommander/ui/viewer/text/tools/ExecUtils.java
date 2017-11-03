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
package com.mucommander.ui.viewer.text.tools;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.impl.local.LocalFile;
import com.mucommander.process.AbstractProcess;
import com.mucommander.process.ProcessListener;
import com.mucommander.shell.Shell;

import java.io.IOException;

public class ExecUtils {


    public static ProcessParams getBuilderParams(AbstractFile file) {
        if (file == null || !file.exists() || !(file.getTopAncestor() instanceof LocalFile)) {
            return null;
        }
        AbstractFile folder = file.getParent();
        if (folder == null || !folder.exists()) {
            return null;
        }
        while (true) {
            if (folderContainsBuilder(folder, "Makefile")) {
                return new ProcessParams(folder, "make");
            } else if (folderContainsBuilder(folder, "make.builder")) {
                return new ProcessParams(folder, "builder");
            } else if (folderContainsBuilder(folder, "build.xml")) {
                return new ProcessParams(folder, "ant");
            }
            if (folder.isRoot()) {
                return null;
            }
            folder = folder.getParent();
        }
    }

    private static boolean folderContainsBuilder(AbstractFile folder, String fileName) {
        try {
            AbstractFile child = folder.getChild(fileName);
            return child != null && child.exists();
        } catch (IOException ignore) {}
        return false;
    }


}
