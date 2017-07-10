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
 * This action compares the content of the 2 MainFrame's file tables and marks the files that are different.
 *
 * @author Maxence Bernard
 */
public class CompareFoldersAction extends MuAction {

    CompareFoldersAction(MainFrame mainFrame, Map<String, Object> properties) {
        super(mainFrame, properties);
    }

    @Override
    public void performAction() {
        FileTable leftTable = mainFrame.getLeftPanel().getFileTable();
        FileTable rightTable = mainFrame.getRightPanel().getFileTable();

        BaseFileTableModel leftTableModel = leftTable.getFileTableModel();
        BaseFileTableModel rightTableModel = rightTable.getFileTableModel();
        if (compare(leftTableModel, rightTableModel)) {
            leftTable.repaint();
        }
        if (compare(rightTableModel, leftTableModel)) {
            rightTable.repaint();
        }

        // Notify registered listeners that currently marked files have changed on the file tables
        leftTable.fireMarkedFilesChangedEvent();
        rightTable.fireMarkedFilesChangedEvent();
    }

    private boolean compare(BaseFileTableModel firstTableModel, BaseFileTableModel secondTableModel) {
        boolean result = false;
        int nbFilesFirst = firstTableModel.getFileCount();
        int nbFilesSecond = secondTableModel.getFileCount();

        for (int i = 0; i < nbFilesFirst; i++) {
            AbstractFile tempFile = firstTableModel.getFileAt(i);
            if (tempFile.isDirectory()) {
                continue;
            }

            String tempFileName = tempFile.getName();
            int fileIndex = -1;
            for (int j = 0; j < nbFilesSecond; j++) {
                if (secondTableModel.getFileAt(j).getName().equals(tempFileName)) {
                    fileIndex = j;
                    break;
                }
            }
            if (fileIndex < 0 || secondTableModel.getFileAt(fileIndex).getLastModifiedDate() < tempFile.getLastModifiedDate()) {
                firstTableModel.setFileMarked(tempFile, true);
                result = true;
            }
        }
        return result;
    }

	@Override
	public ActionDescriptor getDescriptor() {
		return new Descriptor();
	}


    public static final class Descriptor extends AbstractActionDescriptor {
    	public static final String ACTION_ID = "CompareFolders";
    	
		public String getId() { return ACTION_ID; }

		public ActionCategory getCategory() { return ActionCategory.SELECTION; }

		public KeyStroke getDefaultAltKeyStroke() { return null; }

		public KeyStroke getDefaultKeyStroke() {
//            if (OsFamily.getCurrent() != OsFamily.MAC_OS_X) {
                return KeyStroke.getKeyStroke(KeyEvent.VK_M, KeyEvent.CTRL_DOWN_MASK);
//            } else {
//                return KeyStroke.getKeyStroke(KeyEvent.VK_M, KeyEvent.META_DOWN_MASK);
//            }
        }

        public MuAction createAction(MainFrame mainFrame, Map<String,Object> properties) {
            return new CompareFoldersAction(mainFrame, properties);
        }

    }
}
