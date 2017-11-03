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

package com.mucommander.ui.dialog.pref.theme;

import com.mucommander.conf.MuSnapshot;
import com.mucommander.utils.text.Translator;
import com.mucommander.ui.dialog.InformationDialog;
import com.mucommander.ui.dialog.QuestionDialog;
import com.mucommander.ui.dialog.pref.PreferencesDialog;
import com.mucommander.ui.dialog.pref.component.PrefComponent;
import com.mucommander.ui.theme.Theme;
import com.mucommander.ui.theme.ThemeData;
import com.mucommander.ui.theme.ThemeManager;

import java.awt.*;

/**
 * Main dialog for the theme editor.
 * @author Nicolas Rinaudo
 */
public class ThemeEditorDialog extends PreferencesDialog {
    private ThemeData data;
    private Theme     theme;
	private Theme modifiedTheme;

    /**
     * Creates a new theme editor dialog.
     * @param parent parent of the dialog.
     * @param theme  theme to edit.
     */
    public ThemeEditorDialog(Dialog parent, Theme theme) {
        super(parent, createTitle(theme));
        initUI(theme);
    }

    /**
     * Creates a new theme editor dialog.
     * @param parent parent of the dialog.
     * @param theme  theme to edit.
     */
    public ThemeEditorDialog(Frame parent, Theme theme) {
        super(parent, createTitle(theme));
        initUI(theme);
    }

    private static String createTitle(Theme theme) {
        return Translator.get("theme_editor.title") + ": " + theme.getName();
    }

    private void initUI(Theme theme) {
        this.theme = theme;
        data = theme.cloneData();
        addPreferencesPanel(new FolderPanePanel(this, data), false);
        addPreferencesPanel(new LocationBarPanel(this, data));
        addPreferencesPanel(new StatusBarPanel(this, data));
        addPreferencesPanel(new ShellPanel(this, data));
        addPreferencesPanel(new FileEditorPanel(this, data));
        addPreferencesPanel(new QuickListPanel(this, data));

        // Sets the dialog's size.
        Dimension screenSize = MuSnapshot.getScreenSize();
        Dimension minimumSize = new Dimension(580, 300);
        Dimension maximumSize = new Dimension(screenSize);
        if (screenSize.getWidth() >= 1024 && screenSize.getHeight() > 700) {
            minimumSize.setSize(800, 480);
        }
        setMinimumSize(minimumSize);
        setMaximumSize(maximumSize);
    }

    /**
	 * Edits the theme specified at creation time and returns the actually modified theme. This is a new custom theme if
	 * a pedefined theme was specified or else the theme itself.
	 * 
	 * @return the actually modified theme it was modified by the user, null otherwise.
	 */
	public Theme editTheme() {
        showDialog();
		return modifiedTheme;
    }

    @Override
    public boolean checkCommit() {
        super.checkCommit();

		// If the theme has been modified and is a predefined theme, asks the user to confirm
		// whether it's ok to duplicate it.
        if (!theme.isIdentical(data) && !theme.canModify())
			if (new QuestionDialog(this, Translator.get("warning"),
			        Translator.get("theme_editor.theme_warning_predefined"),
                                  this, new String[]{Translator.get("yes"), Translator.get("no")}, new int[]{0,1}, 0).getActionValue() != 0)
                return false;
        return true;
    }

    @Override
    public void commit() {
        super.commit();
        if (theme.isIdentical(data)) {
            return;
        }

        try {
			// If the theme cannot be modified, create a new custom theme with the same name to save modifications.
            if (!theme.canModify()) {
				modifiedTheme = ThemeManager.duplicateTheme(theme);
			} else {
				modifiedTheme = theme;
            }
			modifiedTheme.importData(data);
			ThemeManager.writeTheme(modifiedTheme);
			theme = modifiedTheme;
        } catch (Exception exception) {
			try {
				InformationDialog.showErrorDialog(this, Translator.get("write_error"),
				        Translator.get("cannot_write_file",
				                ThemeManager.getFile(theme.getType(), theme.getName()).getAbsolutePath()));
				exception.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
        }
    }

    @Override
    public void componentChanged(PrefComponent component) {
		setCommitButtonsEnabled(!theme.isIdentical(data));
	}
}
