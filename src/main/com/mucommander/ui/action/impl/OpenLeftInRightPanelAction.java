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

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.ui.action.AbstractActionDescriptor;
import com.mucommander.ui.action.ActionCategory;
import com.mucommander.ui.action.ActionDescriptor;
import com.mucommander.ui.action.MuAction;
import com.mucommander.ui.main.FolderPanel;
import com.mucommander.ui.main.MainFrame;
import com.mucommander.ui.main.table.FileTable;
import com.mucommander.ui.main.tabs.FileTableTabs;

/**
 * Opens browsable files in the right panel.
 *
 * <p>
 * If the left panel is the active panel and the selected file is browsable as defined by
 * {@link com.mucommander.commons.file.AbstractFile#isBrowsable()} then the selected file will be opened.
 * </p>
 *
 * <p>
 * Otherwise the left panel's location will be opened.
 * </p>
 *
 * <p>
 * If the right panel is locked then a new tab in the right panel will be opened first.
 * </p>
 *
 * @author Martin Kortkamp
 */
public class OpenLeftInRightPanelAction extends FileAction {
    /**
     * Creates a new <code>OpenLeftInRightPanelAction</code> with the specified parameters.
     * @param mainFrame  frame to which the action is attached.
     * @param properties action's properties.
     */
    OpenLeftInRightPanelAction(MainFrame mainFrame, Map<String, Object> properties) {
        super(mainFrame, properties);
    }

    /**
     * This action is always enabled.
     */
    @Override
    protected boolean getFileTableCondition(FileTable fileTable) {

        return true;
    }

    /**
     * Opens the currently selected file or the current location in right folder panel.
     */
    @Override
    public void performAction() {
        AbstractFile destFile = getDestinationFile();

        if (destFile.isBrowsable()) {

            FileTableTabs destTabs = getDestPanel().getTabs();

            if (destTabs.getCurrentTab().isLocked()) {
                destTabs.add(destFile);
            } else {
                getDestPanel().tryChangeCurrentFolder(destFile);
            }
        }
    }

    private AbstractFile getDestinationFile() {
        AbstractFile destFile = null;

        FolderPanel srcPanel = getSrcPanel();

        if (mainFrame.getActivePanel() == srcPanel) {
            AbstractFile selectedFile = srcPanel.getFileTable().getSelectedFile();
            if (selectedFile != null && selectedFile.isBrowsable()) {
                destFile = selectedFile;
            }
        }

        if (destFile == null) {
            destFile = srcPanel.getCurrentFolder();
        }
        return destFile;
    }

    protected FolderPanel getDestPanel() {
        return mainFrame.getRightPanel();
    }

    protected FolderPanel getSrcPanel() {
        return mainFrame.getLeftPanel();
    }

    @Override
    public ActionDescriptor getDescriptor() {
        return new Descriptor();
    }


    public static final class Descriptor extends AbstractActionDescriptor {
        public static final String ACTION_ID = "OpenLeftInRightPanel";

        public String getId() { return ACTION_ID; }

        public ActionCategory getCategory() { return ActionCategory.NAVIGATION; }

        public KeyStroke getDefaultAltKeyStroke() { return null; }

        public KeyStroke getDefaultKeyStroke() { return null; }

        public MuAction createAction(MainFrame mainFrame, Map<String,Object> properties) {
            return new OpenLeftInRightPanelAction(mainFrame, properties);
        }
    }
}