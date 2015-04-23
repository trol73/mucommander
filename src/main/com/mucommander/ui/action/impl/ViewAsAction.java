/*
 * This file is part of trolCommander, http://www.trolsoft.ru/trolcommander
 * Copyright (C) 2013-2015 Oleg Trifonov
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
import com.mucommander.commons.file.util.FileSet;
import com.mucommander.ui.action.*;
import com.mucommander.ui.main.MainFrame;
import com.mucommander.ui.main.quicklist.ViewAsQL;

import javax.swing.ImageIcon;
import javax.swing.KeyStroke;
import java.awt.event.KeyEvent;
import java.util.Map;

/**
 *
 * @author Oleg Trifonov
 */
public class ViewAsAction extends SelectedFilesAction {
    // - Initialization ------------------------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    /**
     * Creates a new instance of <code>ViewAction</code>.
     * @param mainFrame  frame to which the action is attached.
     * @param properties action's properties.
     */
    public ViewAsAction(MainFrame mainFrame, Map<String, Object> properties) {
        super(mainFrame, properties);

        ImageIcon icon = getStandardIcon(ViewAction.class);
        if (icon != null) {
            setIcon(icon);
        }
    }


    @Override
    public ActionDescriptor getDescriptor() {
        return new Descriptor();
    }

    @Override
    public void performAction(FileSet files) {
        AbstractFile file = mainFrame.getActiveTable().getSelectedFile(false, true);

        // At this stage, no assumption should be made on the type of file that is allowed to be viewed/edited:
        // viewer/editor implementations will decide whether they allow a particular file or not.
        if (file == null || file.isDirectory()) {
            return;
        }
        new ViewAsQL(mainFrame, file).show();
    }


    // - Factory -------------------------------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    public static class Factory implements ActionFactory {

        public MuAction createAction(MainFrame mainFrame, Map<String,Object> properties) {
            return new ViewAsAction(mainFrame, properties);
        }
    }

    public static class Descriptor extends AbstractActionDescriptor {
        public static final String ACTION_ID = "ViewAs";

        public String getId() { return ACTION_ID; }

        public ActionCategory getCategory() { return ActionCategory.FILES; }

        public KeyStroke getDefaultAltKeyStroke() { return null; }

        public KeyStroke getDefaultKeyStroke() { return KeyStroke.getKeyStroke(KeyEvent.VK_F3, KeyEvent.SHIFT_DOWN_MASK); }
    }
}
