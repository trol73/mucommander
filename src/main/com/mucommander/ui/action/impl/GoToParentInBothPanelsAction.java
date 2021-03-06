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
import com.mucommander.ui.action.AbstractActionDescriptor;
import com.mucommander.ui.action.ActionCategory;
import com.mucommander.ui.action.ActionDescriptor;
import com.mucommander.ui.action.TcAction;
import com.mucommander.ui.main.MainFrame;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.util.Map;

/**
 * Changes the current directory to its parent and tries to do the same in the inactive panel.
 * <p>
 * When possible, this action will open the active panel's current folder's parent. Additionally,
 * if the inactive panel's current folder has a parent, it will open that one as well.
 * <p>
 * Note that this action's behavior is strictly equivalent to that of {@link GoToParentAction} in the
 * active panel. Differences will only occur in the inactive panel, and then again only when possible.
 * <p>
 * This action opens both files synchronously: it will wait for the active panel location change confirmation
 * before performing the inactive one.
 *
 * @author Nicolas Rinaudo
 */
public class GoToParentInBothPanelsAction extends ActiveTabAction {

    /**
     * Creates a new <code>GoToParentInBothPanelsAction</code> instance with the specified parameters.
     * @param mainFrame  frame to which the action is attached.
     * @param properties action's properties.
     */
    private GoToParentInBothPanelsAction(MainFrame mainFrame, Map<String, Object> properties) {
        super(mainFrame, properties);

        // Perform this action in a separate thread, to avoid locking the event thread
        setPerformActionInSeparateThread(true);
    }

    /**
     * Enables or disables this action based on the currently active folder's
     * has a parent and both tabs in the two panel are not locked,
     * this action will be enabled, if not it will be disabled.
     */
    @Override
    protected void toggleEnabledState() {
        AbstractFile currentFolder = mainFrame.getActivePanel().getCurrentFolder();
        setEnabled(!mainFrame.getActivePanel().getTabs().getCurrentTab().isLocked() &&
        		   !mainFrame.getInactivePanel().getTabs().getCurrentTab().isLocked() &&
        		    currentFolder != null && currentFolder.getParent() != null);
    }

    /**
     * Opens both the active and inactive folder panel's parent directories.
     */
    @Override
    public void performAction() {
        // If the current panel has a parent file, navigate to it.
        AbstractFile parent = mainFrame.getActivePanel().getCurrentFolder().getParent();
        if (parent != null) {
            Thread openThread = mainFrame.getActivePanel().tryChangeCurrentFolder(parent);

            // If the inactive panel has a parent file, wait for the current panel change to be complete and navigate to it.
            parent = mainFrame.getInactivePanel().getCurrentFolder().getParent();
            if (parent != null) {
                if (openThread != null) {
                    while (openThread.isAlive()) {
                        try {
                            openThread.join();
                        } catch(InterruptedException ignore) {}
                    }
                }
                mainFrame.getInactivePanel().tryChangeCurrentFolder(parent);
            }
        }
    }

	@Override
	public ActionDescriptor getDescriptor() {
		return new Descriptor();
	}


    public static final class Descriptor extends AbstractActionDescriptor {
    	public static final String ACTION_ID = "GoToParentInBothPanels";
    	
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
            return KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, KeyEvent.SHIFT_DOWN_MASK| CTRL_OR_META_DOWN_MASK);
        }

        public TcAction createAction(MainFrame mainFrame, Map<String,Object> properties) {
            return new GoToParentInBothPanelsAction(mainFrame, properties);
        }
    }
}
