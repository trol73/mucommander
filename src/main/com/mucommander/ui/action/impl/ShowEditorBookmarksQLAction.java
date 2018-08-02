/*
 * This file is part of trolCommander, http://www.trolsoft.ru/en/trolcommander
 * Copyright (C) 2014-2018 Oleg Trifonov
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

import com.mucommander.ui.action.AbstractActionDescriptor;
import com.mucommander.ui.action.ActionCategory;
import com.mucommander.ui.action.ActionDescriptor;
import com.mucommander.ui.action.MuAction;
import com.mucommander.ui.main.MainFrame;
import com.mucommander.ui.main.QuickLists;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.util.Map;

public class ShowEditorBookmarksQLAction extends ShowQuickListAction {

    ShowEditorBookmarksQLAction(MainFrame mainFrame, Map<String, Object> properties) {
        super(mainFrame, properties);
    }

    @Override
    public void performAction() {
        openQuickList(QuickLists.EDITOR_BOOKMARKS);
    }

    @Override
    public ActionDescriptor getDescriptor() {
        return new ShowRecentEditedFilesQLAction.Descriptor();
    }


    public static final class Descriptor extends AbstractActionDescriptor {
        public static final String ACTION_ID = "ShowEditorBookmarksQL";

        public String getId() { return ACTION_ID; }

        public ActionCategory getCategory() { return ActionCategory.NAVIGATION; }

        public KeyStroke getDefaultAltKeyStroke() { return null; }

        public KeyStroke getDefaultKeyStroke() { return KeyStroke.getKeyStroke(KeyEvent.VK_9, KeyEvent.ALT_DOWN_MASK); }

        public MuAction createAction(MainFrame mainFrame, Map<String,Object> properties) {
            return new ShowEditorBookmarksQLAction(mainFrame, properties);
        }
    }

}
