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
import com.mucommander.ui.action.*;
import com.mucommander.ui.main.FolderPanel;
import com.mucommander.ui.main.MainFrame;
import com.mucommander.ui.main.table.FileTable;

import javax.swing.KeyStroke;
import java.awt.event.KeyEvent;
import java.util.Map;

/**
 * Opens browsable files in the inactive panel.
 * <p>
 * This action is only enabled if the current selection is browsable as defined by
 * {@link com.mucommander.commons.file.AbstractFile#isBrowsable()}.
 *
 * @author Nicolas Rinaudo
 */
public class OpenInOtherPanelAction extends SelectedFileAction {
    /**
     * Creates a new <code>OpenInOtherPanelAction</code> with the specified parameters.
     * @param mainFrame  frame to which the action is attached.
     * @param properties action's properties.
     */
    private OpenInOtherPanelAction(MainFrame mainFrame, Map<String, Object> properties) {
        super(mainFrame, properties);
    }

    @Override
    public void activePanelChanged(FolderPanel folderPanel) {
        super.activePanelChanged(folderPanel);
        
        if (mainFrame.getInactivePanel().getTabs().getCurrentTab().isLocked())
        	setEnabled(false);
    }
    
    /**
     * This method is overridden to enable this action when the parent folder is selected.
     */
    @Override
    protected boolean getFileTableCondition(FileTable fileTable) {
        AbstractFile selectedFile = fileTable.getSelectedFile(true, true);

        return selectedFile!=null && selectedFile.isBrowsable();
    }

    /**
     * Opens the currently selected file in the inactive folder panel.
     */
    @Override
    public void performAction() {
        AbstractFile file = mainFrame.getActiveTable().getSelectedFile(true, true);

        // Retrieves the currently selected file, aborts if none (should not normally happen).
        if (file == null || !file.isBrowsable()) {
            return;
        }

        // Opens the currently selected file in the inactive panel.
        mainFrame.getInactivePanel().tryChangeCurrentFolder(file);
    }

	@Override
	public ActionDescriptor getDescriptor() {
		return new Descriptor();
	}


    public static final class Descriptor extends AbstractActionDescriptor {
    	public static final String ACTION_ID = "OpenInOtherPanel";
    	
		public String getId() {
		    return ACTION_ID;
		}

		public ActionCategory getCategory() {
		    return ActionCategory.NAVIGATION;
		}

		public KeyStroke getDefaultAltKeyStroke() {
		    return null;
		}

		public KeyStroke getDefaultKeyStroke() {
            return KeyStroke.getKeyStroke(KeyEvent.VK_O, CTRL_OR_META_DOWN_MASK);
        }

        public TcAction createAction(MainFrame mainFrame, Map<String,Object> properties) {
            return new OpenInOtherPanelAction(mainFrame, properties);
        }
    }
}
