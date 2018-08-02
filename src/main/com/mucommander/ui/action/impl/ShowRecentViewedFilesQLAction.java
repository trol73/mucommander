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

import com.mucommander.ui.action.*;
import com.mucommander.ui.main.MainFrame;
import com.mucommander.ui.main.QuickLists;

import javax.swing.KeyStroke;
import java.awt.event.KeyEvent;
import java.util.Map;

/**
 * This action shows RecentViewedFilesQL on the current active FileTable.
 *
 * @author Oleg Trifonov
 */
public class ShowRecentViewedFilesQLAction extends ShowQuickListAction {
    ShowRecentViewedFilesQLAction(MainFrame mainFrame, Map<String, Object> properties) {
        super(mainFrame, properties);
    }

    @Override
    public void performAction() {
        openQuickList(QuickLists.RECENT_VIEWED_FILES);
    }

    @Override
    public ActionDescriptor getDescriptor() {
        return new Descriptor();
    }


    public static final class Descriptor extends AbstractActionDescriptor {
        public static final String ACTION_ID = "ShowRecentViewedFilesQL";

        public String getId() { return ACTION_ID; }

        public ActionCategory getCategory() { return ActionCategory.NAVIGATION; }

        public KeyStroke getDefaultAltKeyStroke() { return null; }

        public KeyStroke getDefaultKeyStroke() { return KeyStroke.getKeyStroke(KeyEvent.VK_7, KeyEvent.ALT_DOWN_MASK); }

        public MuAction createAction(MainFrame mainFrame, Map<String,Object> properties) {
            return new ShowRecentViewedFilesQLAction(mainFrame, properties);
        }
    }
}
