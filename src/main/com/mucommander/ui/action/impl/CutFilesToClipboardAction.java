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
import com.mucommander.ui.action.TcAction;
import com.mucommander.ui.dnd.ClipboardOperations;
import com.mucommander.ui.dnd.ClipboardSupport;
import com.mucommander.ui.main.MainFrame;

import javax.swing.KeyStroke;
import java.awt.event.KeyEvent;
import java.util.Map;

/**
 * This action cuts the selected / marked files to the system clipboard, allowing to paste
 * them to muCommander.
 *
 * @author Nicholai R. Svarre
 */
public class CutFilesToClipboardAction extends SelectedFilesAction {
    

    private CutFilesToClipboardAction(MainFrame mainFrame, Map<String, Object> properties) {
        super(mainFrame, properties);
    }

    @Override
    public void performAction(FileSet files) {
        ClipboardSupport.setClipboardFiles(files);
        ClipboardSupport.setOperation(ClipboardOperations.CUT);
    }


    public static final class Descriptor extends AbstractActionDescriptor {
    	public static final String ACTION_ID = "CutFilesToClipboard";

		public String getId() {
		    return ACTION_ID;
		}

		public ActionCategory getCategory() {
		    return ActionCategory.SELECTION;
		}

		public KeyStroke getDefaultAltKeyStroke() {
		    return KeyStroke.getKeyStroke(KeyEvent.VK_X, KeyEvent.META_DOWN_MASK);
		}

		public KeyStroke getDefaultKeyStroke() {
		    return KeyStroke.getKeyStroke(KeyEvent.VK_X, KeyEvent.CTRL_DOWN_MASK);
		}

        public TcAction createAction(MainFrame mainFrame, Map<String,Object> properties) {
            return new CutFilesToClipboardAction(mainFrame, properties);
        }
    }
    @Override
    public ActionDescriptor getDescriptor() {
        return new Descriptor();
    }
}