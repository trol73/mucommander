/*
 * This file is part of trolCommander, http://www.trolsoft.ru/en/soft/trolcommander
 * Copyright (C) 2013-2020 Oleg Trifonov
 *
 * trolCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * trolCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.mucommander.ui.dialog.startup;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;

import javax.swing.*;

import com.mucommander.RuntimeConstants;
import com.mucommander.commons.runtime.OsFamily;
import com.mucommander.conf.TcConfigurations;
import com.mucommander.conf.TcPreference;
import com.mucommander.ui.combobox.TcComboBox;
import com.mucommander.ui.dialog.FocusDialog;
import com.mucommander.ui.layout.YBoxPanel;
import com.mucommander.ui.theme.Theme;
import com.mucommander.ui.theme.ThemeManager;

import static com.mucommander.conf.TcPreference.*;
import static com.mucommander.conf.TcPreference.LOOK_AND_FEEL;

/**
 * Dialog box allowing users to select misc. setup options for muCommander.
 * @author Nicolas Rinaudo
 */
public class InitialSetupDialog extends FocusDialog implements ActionListener {
    // - Instance fields -----------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /** All available look and feels. */
    private UIManager.LookAndFeelInfo[] lfInfo;
    /** Used to select a startup theme. */
    private TcComboBox<Theme> cbTheme;
    /** Used to select a look and feel. */
    private TcComboBox<String> cbLookAndFeel;
    private TcComboBox<String> cbEditorTheme;
    /** Used to validate the user's choice. */
    private JButton   okButton;


    /**
     * Creates the dialog's theme panel.
     * @return the dialog's theme panel.
     */
    private JPanel createThemesPanel() {
		JPanel themePanel = new YBoxPanel();
		themePanel.setAlignmentX(LEFT_ALIGNMENT);
        themePanel.setBorder(BorderFactory.createTitledBorder(i18n("prefs_dialog.themes")));

		// Adds the panel description.
		JPanel tempPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		tempPanel.add(new JLabel(i18n("setup.theme") + ':'));

		// Adds the theme combo box.
		cbTheme = createThemeComboBox();
		tempPanel.add(cbTheme);
		themePanel.add(tempPanel);

		// Adds the panel description.
		tempPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		tempPanel.add(new JLabel(i18n("prefs_dialog.syntax_themes") + ':'));

		cbEditorTheme = new TcComboBox<>(ThemeManager.predefinedSyntaxThemeNames());
		cbEditorTheme.addActionListener(this);

		tempPanel.add(cbEditorTheme);
		themePanel.add(tempPanel);

		return themePanel;
    }

	private TcComboBox<Theme> createThemeComboBox() {
		TcComboBox<Theme> themeComboBox = new TcComboBox<>();
		Iterator<Theme> themes = ThemeManager.availableThemes();

		int index = 0;				// Index of the currently analyzed theme.
		int selectedIndex = 0;		// Index of the current theme in the combo box.
		// Adds all themes to the combo box.
		while (themes.hasNext()) {
			Theme theme = themes.next();
			themeComboBox.addItem(theme);
			if (ThemeManager.isCurrentTheme(theme)) {
				selectedIndex = index;
			}
			index++;
		}
		// Selects the current theme.
		themeComboBox.setSelectedIndex(selectedIndex);
		themeComboBox.addActionListener(this);

		return themeComboBox;
	}

	/**
     * Creates the dialog's look and feel panel.
     * @return the dialog's look and feel panel.
     */
    private JPanel createLookAndFeelPanel() {
		// Initialises the theme panel.
		JPanel lfPanel = new YBoxPanel();
		lfPanel.setAlignmentX(LEFT_ALIGNMENT);
        lfPanel.setBorder(BorderFactory.createTitledBorder(i18n("prefs_dialog.look_and_feel")));

		// Adds the panel description.
		JPanel tempPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		tempPanel.add(new JLabel(i18n("setup.look_and_feel") + ':'));
		lfPanel.add(tempPanel);

		// Initialises the l&f combo box.
		cbLookAndFeel = new TcComboBox<>();
		lfInfo = UIManager.getInstalledLookAndFeels();
		String currentLf = UIManager.getLookAndFeel().getName();
		int selectedIndex = -1;
		// Goes through all available look&feels and selects the current one.
        for (int i = 0; i < lfInfo.length; i++) {
			String buffer = lfInfo[i].getName();

            // Tries to select current L&F
            if (currentLf.equals(buffer)) {
				selectedIndex = i;
			}// Under Mac OS X, Mac L&F is either reported as 'MacOS' or 'MacOS Adaptative'
            // so we need this test
            else if (selectedIndex == -1 && (currentLf.startsWith(buffer) || buffer.startsWith(currentLf))) {
				selectedIndex = i;
			}
            cbLookAndFeel.addItem(buffer);
        }

        // If no match, selects first one
        if (selectedIndex == -1) {
			selectedIndex = 0;
		}
        cbLookAndFeel.setSelectedIndex(selectedIndex);
		cbLookAndFeel.addActionListener(this);
		lfPanel.add(cbLookAndFeel);

		return lfPanel;
    }

    /**
     * Creates the dialog's main panel.
     * @return the dialog's main panel.
     */
    private JPanel createMainPanel() {
		YBoxPanel mainPanel = new YBoxPanel();
		mainPanel.add(new JLabel(i18n("setup.intro")));
		mainPanel.addSpace(10);
		mainPanel.add(createThemesPanel());
		mainPanel.addSpace(10);
		mainPanel.add(createLookAndFeelPanel());
		mainPanel.addSpace(10);

		JPanel okPanel = new JPanel();
		okPanel.setLayout(new FlowLayout(FlowLayout.TRAILING));
		okPanel.add(okButton = new JButton(i18n("ok")));
		okButton.addActionListener(this);

		mainPanel.add(okPanel);

		return mainPanel;
    }


	/**
     * Creates a new InitialSetupDialog.
     * @param owner dialog's owner.
     */
    public InitialSetupDialog(Frame owner) {
		super(owner, i18n("setup.title"), owner);
		initDefault();

		getContentPane().add(createMainPanel(), BorderLayout.CENTER);
		pack();
		setResizable(false);
        setInitialFocusComponent(cbTheme);
		setKeyboardDisposalEnabled(false);
        getRootPane().setDefaultButton(okButton);
    }


	@Override
    public void actionPerformed(ActionEvent e) {
    	Object src = e.getSource();
		if (src == cbTheme) {
			ThemeManager.setCurrentTheme((Theme) cbTheme.getSelectedItem());
		} else if (src == cbLookAndFeel) {
			setVariable(LOOK_AND_FEEL, lfInfo[cbLookAndFeel.getSelectedIndex()].getClassName());
		} else if (src == cbEditorTheme) {
			setVariable(SYNTAX_THEME_NAME, cbEditorTheme.getSelectedIndex());
		} else if (src == okButton) {
			ThemeManager.setCurrentTheme((Theme) cbTheme.getSelectedItem());
			setVariable(LOOK_AND_FEEL, lfInfo[cbLookAndFeel.getSelectedIndex()].getClassName());
			System.out.println(UIManager.get("MenuItem.margin"));
			UIManager.put("MenuItem.margin", new javax.swing.plaf.InsetsUIResource(3, 10, 3, 20));
			System.out.println(UIManager.get("MenuItem.margin"));
			dispose();
		}
    }

	private void initDefault() {
    	boolean isGnome = OsFamily.LINUX.isCurrent() && new com.mucommander.desktop.gnome.GuessedGnomeDesktopAdapter().isAvailable();
    	if (isGnome && RuntimeConstants.DISPLAY_4K) {
			setVariable(TOOLBAR_ICON_SCALE, 2.0f);
			setVariable(COMMAND_BAR_ICON_SCALE, 2.0f);
			setVariable(TABLE_ICON_SCALE, 3.0f);
		}
	}

	private static void setVariable(TcPreference preference, String value) {
		TcConfigurations.getPreferences().setVariable(preference, value);
	}

	private static void setVariable(TcPreference preference, int value) {
		TcConfigurations.getPreferences().setVariable(preference, value);
	}

	private static void setVariable(TcPreference preference, float value) {
		TcConfigurations.getPreferences().setVariable(preference, value);
	}
}
