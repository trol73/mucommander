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

import java.util.Map;

import javax.swing.KeyStroke;

import com.mucommander.ui.action.AbstractActionDescriptor;
import com.mucommander.ui.action.ActionCategory;
import com.mucommander.ui.action.ActionDescriptor;
import com.mucommander.ui.action.ActionFactory;
import com.mucommander.ui.action.MuAction;
import com.mucommander.ui.main.FolderPanel;
import com.mucommander.ui.main.MainFrame;

/**
 * Opens browsable files in the left panel.
 *
 * <p>
 * If the right panel is the active panel and the selected file is browsable as defined by
 * {@link com.mucommander.commons.file.AbstractFile#isBrowsable()} then the selected file will be opened.
 * </p>
 *
 * <p>
 * Otherwise the right panel's location will be opened.
 * </p>
 *
 * <p>
 * If the left panel is locked then a new tab in the left panel will be opened first.
 * </p>
 *
 * @author Martin Kortkamp
 */
public class OpenRightInLeftPanelAction extends OpenLeftInRightPanelAction {
    /**
     * Creates a new <code>OpenRightInLeftPanelAction</code> with the specified parameters.
     * @param mainFrame  frame to which the action is attached.
     * @param properties action's properties.
     */
    public OpenRightInLeftPanelAction(MainFrame mainFrame, Map<String,Object> properties) {
        super(mainFrame, properties);
    }

    protected FolderPanel getDestPanel() {
        return mainFrame.getLeftPanel();
    }

    protected FolderPanel getSrcPanel() {
        return mainFrame.getRightPanel();
    }

    @Override
    public ActionDescriptor getDescriptor() {
        return new Descriptor();
    }

    public static class Factory implements ActionFactory {

        public MuAction createAction(MainFrame mainFrame, Map<String,Object> properties) {
            return new OpenRightInLeftPanelAction(mainFrame, properties);
        }
    }

    public static class Descriptor extends AbstractActionDescriptor {
        public static final String ACTION_ID = "OpenRightInLeftPanel";

        public String getId() { return ACTION_ID; }

        public ActionCategory getCategory() { return ActionCategory.NAVIGATION; }

        public KeyStroke getDefaultAltKeyStroke() { return null; }

        public KeyStroke getDefaultKeyStroke() { return null; }
    }
}