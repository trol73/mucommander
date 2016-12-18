/*
 * This file is part of trolCommander, http://www.trolsoft.ru/en/soft/trolcommander
 * Copyright (C) 2014-2016 Oleg Trifonov
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
package com.mucommander.ui.action.impl;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.ui.action.*;
import com.mucommander.ui.main.MainFrame;
import com.mucommander.ui.main.table.FileTable;
import com.mucommander.ui.main.table.views.full.FileTableModel;

import javax.swing.KeyStroke;
import java.util.Map;

/**
 * @author Oleg Trifonov
 * Created on 24/03/15.
 */
public class ShowFoldersSizeAction extends ParentFolderAction {

    public ShowFoldersSizeAction(MainFrame mainFrame, Map<String, Object> properties) {
        super(mainFrame, properties);
    }

    @Override
    protected void toggleEnabledState() {

    }

    @Override
    public void performAction() {
        FileTable activeTable = mainFrame.getActiveTable();
        FileTableModel fileTableModel = (FileTableModel)activeTable.getModel();
        for (AbstractFile file : fileTableModel.getFiles()) {
            if (file.isDirectory()) {
                fileTableModel.startDirectorySizeCalculation(activeTable, file);
            }
        }
    }

    @Override
    public ActionDescriptor getDescriptor() {
        return new Descriptor();
    }


    public static final class Descriptor extends AbstractActionDescriptor {
        public static final String ACTION_ID = "ShowFoldersSize";

        public String getId() { return ACTION_ID; }

        public ActionCategory getCategory() { return ActionCategory.VIEW; }

        public KeyStroke getDefaultAltKeyStroke() { return null; }

        public KeyStroke getDefaultKeyStroke() {
            return null;
        }

        public MuAction createAction(MainFrame mainFrame, Map<String, Object> properties) {
            return new ShowFoldersSizeAction(mainFrame, properties);
        }
    }

}
