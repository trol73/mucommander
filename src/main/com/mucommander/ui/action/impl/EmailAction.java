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

import com.mucommander.commons.file.FileOperation;
import com.mucommander.commons.file.filter.FileOperationFilter;
import com.mucommander.commons.file.util.FileSet;
import com.mucommander.commons.runtime.OsFamily;
import com.mucommander.job.SendMailJob;
import com.mucommander.utils.text.Translator;
import com.mucommander.ui.action.*;
import com.mucommander.ui.dialog.InformationDialog;
import com.mucommander.ui.dialog.file.EmailFilesDialog;
import com.mucommander.ui.dialog.pref.general.GeneralPreferencesDialog;
import com.mucommander.ui.main.MainFrame;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.util.Map;

/**
 * This action pops up the 'Email files' dialog that allows to email the currently marked files as attachment.
 *
 * @author Maxence Bernard
 */
@InvokesDialog
public class EmailAction extends SelectedFilesAction {

    private EmailAction(MainFrame mainFrame, Map<String, Object> properties) {
        super(mainFrame, properties);

        setSelectedFileFilter(new FileOperationFilter(FileOperation.READ_FILE));
    }

    @Override
    public void performAction(FileSet files) {
        // Notifies the user that mail preferences are not set and brings the preferences dialog
        if (!SendMailJob.mailPreferencesSet()) {
            InformationDialog.showErrorDialog(mainFrame, Translator.get("email_dialog.prefs_not_set"), Translator.get("email_dialog.prefs_not_set_title"));
            SwingUtilities.invokeLater(() -> {
                GeneralPreferencesDialog preferencesDialog = GeneralPreferencesDialog.getDialog();
                preferencesDialog.setActiveTab(GeneralPreferencesDialog.MAIL_TAB);
                preferencesDialog.showDialog();
            });
            return;
        }
        new EmailFilesDialog(mainFrame, files).showDialog();
    }

	@Override
	public ActionDescriptor getDescriptor() {
		return new Descriptor();
	}


    public static final class Descriptor extends AbstractActionDescriptor {
    	public static final String ACTION_ID = "Email";

		public String getId() {
		    return ACTION_ID;
		}

		public ActionCategory getCategory() {
		    return ActionCategory.FILES;
		}

		public KeyStroke getDefaultAltKeyStroke() {
		    return null;
		}

		public KeyStroke getDefaultKeyStroke() {
            return KeyStroke.getKeyStroke(KeyEvent.VK_S, CTRL_OR_META_DOWN_MASK);
        }

        public MuAction createAction(MainFrame mainFrame, Map<String,Object> properties) {
            return new EmailAction(mainFrame, properties);
        }
    }
}