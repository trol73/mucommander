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
import com.mucommander.conf.MuConfigurations;
import com.mucommander.conf.MuPreference;
import com.mucommander.conf.MuPreferences;
import com.mucommander.ui.action.AbstractActionDescriptor;
import com.mucommander.ui.action.ActionCategory;
import com.mucommander.ui.action.ActionDescriptor;
import com.mucommander.ui.action.MuAction;
import com.mucommander.ui.main.MainFrame;
import com.mucommander.ui.main.table.FileTable;
import com.mucommander.ui.main.table.views.full.FileTableModel;

import javax.swing.KeyStroke;
import java.awt.event.KeyEvent;
import java.util.Map;

/**
 * Marks or unmarks the current selected file (current row) and advance current row to the next one,
 * with the following exceptions:
 * <ul>
 * <li>if quick search is active, this method does nothing
 * <li>if '..' file is selected, file is not marked but current row is still advanced to the next one
 * <li>if the {@link com.mucommander.ui.action.impl.MarkSelectedFileAction} key event is repeated and the last file has already
 * been marked/unmarked since the key was last released, the file is not marked in order to avoid
 * marked/unmarked flaps when the mark key is kept pressed.
 *
 * @author Maxence Bernard
 */
public class MarkSelectedFileAction extends MuAction {

    MarkSelectedFileAction(MainFrame mainFrame, Map<String, Object> properties) {
        super(mainFrame, properties);
    }


    @Override
    public void performAction() {
		if (MuConfigurations.getPreferences().getVariable(MuPreference.CALCULATE_FOLDER_SIZE_ON_MARK, MuPreferences.DEFAULT_CALCULATE_FOLDER_SIZE_ON_MARK))
		{
			calculateFolderSize();
		}
        mainFrame.getActiveTable().markSelectedFile();
    }

	private void calculateFolderSize() {
		FileTable table = mainFrame.getActiveTable();
		AbstractFile file = table.getSelectedFile();

		if (file != null && file.isDirectory()) {
			FileTableModel model = (FileTableModel) mainFrame.getActiveTable().getModel();
			model.startDirectorySizeCalculation(table, table.getSelectedFile());
		}
	}

	@Override
	public ActionDescriptor getDescriptor() {
		return new Descriptor();
	}


    public static final class Descriptor extends AbstractActionDescriptor {
    	public static final String ACTION_ID = "MarkSelectedFile";
    	
		public String getId() { return ACTION_ID; }

		public ActionCategory getCategory() { return ActionCategory.SELECTION; }

		public KeyStroke getDefaultAltKeyStroke() { return KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, 0); }

		public KeyStroke getDefaultKeyStroke() { return KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0); }

		public MuAction createAction(MainFrame mainFrame, Map<String,Object> properties) {
			return new MarkSelectedFileAction(mainFrame, properties);
		}
    }
}
