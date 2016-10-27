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
import com.mucommander.ui.action.AbstractActionDescriptor;
import com.mucommander.ui.action.ActionCategory;
import com.mucommander.ui.action.ActionDescriptor;
import com.mucommander.ui.action.MuAction;
import com.mucommander.ui.main.FolderPanel;
import com.mucommander.ui.main.MainFrame;
import com.mucommander.ui.main.table.FileTable;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.util.Map;

/**
 * @author Oleg Trifonov
 * Created on 27/10/16.
 */
public class LeftArrowAction extends MuAction {

    public LeftArrowAction(MainFrame mainFrame, Map<String, Object> properties) {
        super(mainFrame, properties);
    }

    @Override
    public void performAction() {
        FileTable table = mainFrame.getActiveTable();
        if (table == null) {
            return;
        }
        if (table.getSelectedFileIndex() == 0) {
            FolderPanel panel = mainFrame.getActivePanel();
            AbstractFile parent = panel.getCurrentFolder().getParent();
            if (parent != null) {
                panel.tryChangeCurrentFolder(parent, null, true);
            }
        } else {
            table.selectFile(0);
        }
    }

    @Override
    public ActionDescriptor getDescriptor() {
        return new Descriptor();
    }

    public static final class Descriptor extends AbstractActionDescriptor {
        public static final String ACTION_ID = "LeftArrowAction";

        public String getId() {
            return ACTION_ID;
        }

        public ActionCategory getCategory() {
            return ActionCategory.NAVIGATION;
        }

        public KeyStroke getDefaultKeyStroke() {
            return KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0);
        }

        public KeyStroke getDefaultAltKeyStroke() {
            return null;
        }

        public MuAction createAction(MainFrame mainFrame, Map<String,Object> properties) {
            return new LeftArrowAction(mainFrame, properties);
        }
    }
}
