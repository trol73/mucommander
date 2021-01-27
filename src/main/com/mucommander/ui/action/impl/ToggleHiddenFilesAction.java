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

import com.mucommander.conf.TcConfigurations;
import com.mucommander.conf.TcPreference;
import com.mucommander.conf.TcPreferences;
import com.mucommander.ui.action.AbstractActionDescriptor;
import com.mucommander.ui.action.ActionCategory;
import com.mucommander.ui.action.ActionDescriptor;
import com.mucommander.ui.action.TcAction;
import com.mucommander.ui.main.MainFrame;
import com.mucommander.ui.main.WindowManager;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.util.Map;

/**
 * A simple action that toggles hidden files visibility on and off.
 * @author Nicolas Rinaudo
 */
public class ToggleHiddenFilesAction extends TcAction {
    // - Initialization ------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Creates a new <code>ToggleHiddenFilesAction</code>.
     */
    private ToggleHiddenFilesAction(MainFrame mainFrame, Map<String, Object> properties) {
        super(mainFrame, properties);
    }


    // - Action code ---------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Toggles hidden files display on and off and requests for all file tables to be repainted.
     */
    @Override
    public void performAction() {
        boolean show = TcConfigurations.getPreferences().getVariable(TcPreference.SHOW_HIDDEN_FILES, TcPreferences.DEFAULT_SHOW_HIDDEN_FILES);
    	TcConfigurations.getPreferences().setVariable(TcPreference.SHOW_HIDDEN_FILES, !show);
        WindowManager.tryRefreshCurrentFolders();
    }

	@Override
	public ActionDescriptor getDescriptor() {
		return new Descriptor();
	}


    public static final class Descriptor extends AbstractActionDescriptor {
    	public static final String ACTION_ID = "ToggleHiddenFiles";
    	
		public String getId() {
		    return ACTION_ID;
		}

		public ActionCategory getCategory() {
		    return ActionCategory.VIEW;
		}

		public KeyStroke getDefaultAltKeyStroke() {
		    return null;
		}

		public KeyStroke getDefaultKeyStroke() {
		    return KeyStroke.getKeyStroke(KeyEvent.VK_PERIOD, CTRL_OR_META_DOWN_MASK);
        }

        public TcAction createAction(MainFrame mainFrame, Map<String,Object> properties) {
            return new ToggleHiddenFilesAction(mainFrame, properties);
        }
    }
}
