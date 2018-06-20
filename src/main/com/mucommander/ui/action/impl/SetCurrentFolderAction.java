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

import com.mucommander.ui.action.AbstractActionDescriptor;
import com.mucommander.ui.action.ActionCategory;
import com.mucommander.ui.action.MuAction;
import com.mucommander.ui.event.ActivePanelListener;
import com.mucommander.ui.main.FolderPanel;
import com.mucommander.ui.main.MainFrame;

import javax.swing.KeyStroke;
import java.util.Map;

public abstract class SetCurrentFolderAction extends MuAction implements ActivePanelListener {

    protected enum Type {LEFT_TO_RIGHT, RIGHT_TO_LEFT}

    private final Type type;

    SetCurrentFolderAction(MainFrame mainFrame, Type type, Map<String, Object> properties) {
        super(mainFrame, properties);
        this.type = type;
        mainFrame.addActivePanelListener(this);
        toggleEnabledState();
    }

    protected Type getType() {
        return type;
    }

    private void toggleEnabledState() {
        final FolderPanel panel;
        if (getType() == Type.LEFT_TO_RIGHT) {
            panel = mainFrame.getRightPanel();
        } else {
            panel = mainFrame.getLeftPanel();
        }
        setEnabled(!panel.getTabs().getCurrentTab().isLocked());
    }

    @Override
    public void activePanelChanged(FolderPanel folderPanel) {
        toggleEnabledState();
    }

    protected static abstract class Descriptor extends AbstractActionDescriptor {

        @Override
        public ActionCategory getCategory() {
            return ActionCategory.VIEW;
        }

        @Override
        public KeyStroke getDefaultAltKeyStroke() {
            return null;
        }

        @Override
        public KeyStroke getDefaultKeyStroke() {
            return null;
        }

    }

}
