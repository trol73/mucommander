/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2012 Maxence Bernard
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
import com.mucommander.commons.runtime.OsFamily;
import com.mucommander.conf.MuConfigurations;
import com.mucommander.conf.MuPreference;
import com.mucommander.conf.MuPreferences;
import com.mucommander.ui.action.AbstractActionDescriptor;
import com.mucommander.ui.action.ActionCategory;
import com.mucommander.ui.action.ActionDescriptor;
import com.mucommander.ui.action.MuAction;
import com.mucommander.ui.main.MainFrame;
import com.mucommander.ui.main.table.FileTable;
import com.mucommander.ui.main.table.views.BaseFileTableModel;

import javax.swing.KeyStroke;
import java.awt.event.KeyEvent;
import java.util.Map;

/**
 * This action marks all files in the current file table.
 *
 * @author Maxence Bernard
 */
public class MarkAllAction extends MuAction {
    private boolean mark;

    MarkAllAction(MainFrame mainFrame, Map<String, Object> properties, boolean mark) {
        super(mainFrame, properties);
        this.mark = mark;
    }

    MarkAllAction(MainFrame mainFrame, Map<String, Object> properties) {
        this(mainFrame, properties, true);
    }

    @Override
    public void performAction() {
        final boolean markFolders = MuConfigurations.getPreferences().getVariable(MuPreference.MARK_FOLDERS_WITH_FILES, MuPreferences.DEFAULT_MARK_FOLDERS_WITH_FILES);

        FileTable fileTable = mainFrame.getActiveTable();
        BaseFileTableModel tableModel = fileTable.getFileTableModel();

        int nbFiles = tableModel.getFilesCount();
        boolean go = markFolders;
        for (int i = tableModel.getFirstMarkableIndex(); i < nbFiles; i++) {
            if (!markFolders) {
                final AbstractFile file = tableModel.getFileAt(i);
                go = !file.isDirectory();
            }
            if (go) {
                tableModel.setFileMarked(i, mark);
            }
        }
        fileTable.repaint();

        // Notify registered listeners that currently marked files have changed on the FileTable
        fileTable.fireMarkedFilesChangedEvent();
    }

    @Override
    public ActionDescriptor getDescriptor() {
        return new Descriptor();
    }


    public static final class Descriptor extends AbstractActionDescriptor {

        public static final String ACTION_ID = "MarkAll";

        @Override
        public String getId() {
            return ACTION_ID;
        }

        @Override
        public ActionCategory getCategory() {
            return ActionCategory.SELECTION;
        }

        @Override
        public KeyStroke getDefaultAltKeyStroke() {
            return null;
        }

        @Override
        public KeyStroke getDefaultKeyStroke() {
            if (!OsFamily.MAC_OS_X.isCurrent()) {
                return KeyStroke.getKeyStroke(KeyEvent.VK_A, KeyEvent.CTRL_DOWN_MASK);
            } else {
                return KeyStroke.getKeyStroke(KeyEvent.VK_A, KeyEvent.META_DOWN_MASK);
            }
        }

        @Override
        public MuAction createAction(MainFrame mainFrame, Map<String, Object> properties) {
            return new MarkAllAction(mainFrame, properties);
        }

    }

}
