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

import com.mucommander.commons.file.util.FileSet;
import com.mucommander.ui.action.AbstractActionDescriptor;
import com.mucommander.ui.action.ActionCategory;
import com.mucommander.ui.action.ActionDescriptor;
import com.mucommander.ui.action.InvokesDialog;
import com.mucommander.ui.action.MuAction;
import com.mucommander.ui.dialog.file.PropertiesDialog;
import com.mucommander.ui.main.MainFrame;

import javax.swing.KeyStroke;
import java.awt.event.KeyEvent;
import java.util.Map;

/**
 * This action pops up the file Properties dialog.
 *
 * @author Maxence Bernard
 */
@InvokesDialog
public class ShowFilePropertiesAction extends SelectedFilesAction {

    ShowFilePropertiesAction(MainFrame mainFrame, Map<String, Object> properties) {
        super(mainFrame, properties);
    }

    @Override
    public void performAction(FileSet files) {
        new PropertiesDialog(mainFrame, files).showDialog();
    }

    @Override
    public ActionDescriptor getDescriptor() {
        return new Descriptor();
    }

    public static final class Descriptor extends AbstractActionDescriptor {
        public static final String ACTION_ID = "ShowFileProperties";

        @Override
        public String getId() {
            return ACTION_ID;
        }

        @Override
        public ActionCategory getCategory() {
            return ActionCategory.FILES;
        }

        @Override
        public KeyStroke getDefaultAltKeyStroke() {
            return null;
        }

        @Override
        public KeyStroke getDefaultKeyStroke() {
            return KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, KeyEvent.ALT_DOWN_MASK);
        }

        @Override
        public MuAction createAction(MainFrame mainFrame, Map<String, Object> properties) {
            return new ShowFilePropertiesAction(mainFrame, properties);
        }
    }

}
