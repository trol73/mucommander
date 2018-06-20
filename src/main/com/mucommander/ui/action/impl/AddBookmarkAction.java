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

import java.awt.event.KeyEvent;
import java.util.Map;

import javax.swing.KeyStroke;

import com.mucommander.commons.runtime.OsFamily;
import com.mucommander.ui.action.*;
import com.mucommander.ui.dialog.bookmark.AddBookmarkDialog;
import com.mucommander.ui.main.MainFrame;

/**
 * This action brings up the 'Add bookmark' dialog that allows to bookmark the current folder.
 *
 * @author Maxence Bernard
 */
@InvokesDialog
public class AddBookmarkAction extends MuAction {

    AddBookmarkAction(MainFrame mainFrame, Map<String, Object> properties) {
        super(mainFrame, properties);
    }

    @Override
    public void performAction() {
        new AddBookmarkDialog(mainFrame);
    }

	@Override
	public ActionDescriptor getDescriptor() {
		return new Descriptor();
	}



    public static final class Descriptor extends AbstractActionDescriptor {
    	public static final String ACTION_ID = "AddBookmark";

		public String getId() {	return ACTION_ID; }

		public ActionCategory getCategory() { return ActionCategory.NAVIGATION; }

		public KeyStroke getDefaultAltKeyStroke() { return null; }

		public KeyStroke getDefaultKeyStroke() {
            if (!OsFamily.MAC_OS_X.isCurrent()) {
                return KeyStroke.getKeyStroke(KeyEvent.VK_B, KeyEvent.CTRL_DOWN_MASK);
            } else {
                return KeyStroke.getKeyStroke(KeyEvent.VK_B, KeyEvent.META_DOWN_MASK);
            }
        }

        public MuAction createAction(MainFrame mainFrame, Map<String,Object> properties) {
            return new AddBookmarkAction(mainFrame, properties);
        }
    }


}
