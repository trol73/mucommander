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
import com.mucommander.ui.main.FolderPanel;
import com.mucommander.ui.main.MainFrame;

import javax.swing.KeyStroke;
import java.awt.event.KeyEvent;
import java.util.Map;

/**
 * Opens the active panel's parent in the inactive panel.
 * <p>
 * This action is only enabled when the active panel has a parent,
 * and the selected tab in the other panel is not locked.
 *
 * @author Nicolas Rinaudo
 */
public class GoToParentInOtherPanelAction extends ParentFolderAction {
    /**
     * Creates a new <code>GoToParentInOtherPanelAction</code> with the specified parameters.
     * @param mainFrame  frame to which the action is attached.
     * @param properties action's properties.
     */
    private GoToParentInOtherPanelAction(MainFrame mainFrame, Map<String, Object> properties) {
        super(mainFrame, properties);
    }

    /**
     * Goes to <code>sourcePanel</code>'s parent in <code>destPanel</code>.
     * <p>
     * If <code>sourcePanel</code> doesn't have a parent, nothing will happen.
     *
     * @param  sourcePanel panel whose parent should be used.
     * @param  destPanel   panel in which to change the location.
     * @return             <code>true</code> if <code>sourcePanel</code> has a parent, <code>false</code> otherwise.
     */
    private boolean goToParent(FolderPanel sourcePanel, FolderPanel destPanel) {
        AbstractFile parent = sourcePanel.getCurrentFolder().getParent();

        if (parent != null) {
            destPanel.tryChangeCurrentFolder(parent, null, true);
            return true;
        }
        return false;
    }
    
    /**
     * Enables or disables this action based on the currently active folder's
     * has a parent and selected tab in the other panel is not locked,
     * this action will be enabled, if not it will be disabled.
     */
    @Override
    protected void toggleEnabledState() {
        AbstractFile currentFolder = mainFrame.getActivePanel().getCurrentFolder();
        setEnabled(!mainFrame.getInactivePanel().getTabs().getCurrentTab().isLocked() &&
        		    currentFolder != null && currentFolder.getParent() != null);
    }
    
    /**
     * Opens the active panel's parent in the inactive panel.
     */
    @Override
    public void performAction() {
    	goToParent(mainFrame.getActivePanel(), mainFrame.getInactivePanel());
    }

	@Override
	public ActionDescriptor getDescriptor() {
		return new Descriptor();
	}


    public static final class Descriptor extends AbstractActionDescriptor {
    	public static final String ACTION_ID = "GoToParentInOtherPanel";
    	
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
            return KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, CTRL_OR_META_DOWN_MASK);
        }

        public MuAction createAction(MainFrame mainFrame, Map<String,Object> properties) {
            return new GoToParentInOtherPanelAction(mainFrame, properties);
        }
    }
}
