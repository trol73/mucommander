/*
 * This file is part of trolCommander, http://www.trolsoft.ru/en/soft/trolcommander
 * Copyright (C) 2013-2016 Oleg Trifonov
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.mucommander.ui.action.impl;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.filter.MountedDriveFilter;
import com.mucommander.commons.file.util.FileSet;
import com.mucommander.commons.runtime.OsFamily;
import com.mucommander.ui.action.*;
import com.mucommander.ui.macosx.AppleScript;
import com.mucommander.ui.main.MainFrame;

import javax.swing.KeyStroke;
import java.util.Map;

/**
 *
 * Created on 26/01/16.
 * @author Oleg Trifonov
 */
public class EjectDriveAction extends SelectedFilesAction {

    public EjectDriveAction(MainFrame mainFrame, Map<String, Object> properties) {
        super(mainFrame, properties);
        setSelectedFileFilter(new MountedDriveFilter());
    }

    @Override
    public void performAction(FileSet files) {
        if (files.size() == 1 && eject(files.get(0))) {
            mainFrame.tryRefreshCurrentFolders();
        }
    }

    public static boolean eject(AbstractFile file) {
        if (OsFamily.getCurrent() == OsFamily.MAC_OS_X) {
            StringBuilder sb = new StringBuilder();
            return AppleScript.execute("tell application \"Finder\"\n" +
                    "   eject disk \"" + file.getName() + "\"\n" +
                    "end tell", sb);
        }
        return false;
    }

    @Override
    public ActionDescriptor getDescriptor() {
        return new Descriptor();
    }

    public static class Factory implements ActionFactory {

        public MuAction createAction(MainFrame mainFrame, Map<String, Object> properties) {
            return new EjectDriveAction(mainFrame, properties);
        }
    }

    public static class Descriptor extends AbstractActionDescriptor {
        public static final String ACTION_ID = "EjectDrive";

        public String getId() { return ACTION_ID; }

        public ActionCategory getCategory() { return ActionCategory.FILES; }

        public KeyStroke getDefaultAltKeyStroke() { return null; }

        public KeyStroke getDefaultKeyStroke() { return null; }
    }
}
