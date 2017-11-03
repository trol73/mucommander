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

package com.mucommander.ui.dialog.pref.general;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import com.mucommander.bonjour.BonjourDirectory;
import com.mucommander.conf.MuConfigurations;
import com.mucommander.conf.MuPreferences;
import com.mucommander.conf.MuPreferencesAPI;
import com.mucommander.desktop.DesktopManager;
import com.mucommander.utils.text.Translator;
import com.mucommander.ui.action.impl.TerminalAction;
import com.mucommander.ui.dialog.DialogOwner;
import com.mucommander.ui.dialog.pref.PreferencesDialog;
import com.mucommander.ui.dialog.pref.PreferencesPanel;
import com.mucommander.ui.dialog.pref.component.PrefCheckBox;
import com.mucommander.ui.dialog.pref.component.PrefEncodingSelectBox;
import com.mucommander.ui.dialog.pref.component.PrefFilePathField;
import com.mucommander.ui.dialog.pref.component.PrefRadioButton;
import com.mucommander.ui.dialog.pref.component.PrefTextField;
import com.mucommander.ui.layout.XAlignedComponentPanel;
import com.mucommander.ui.layout.YBoxPanel;
import com.mucommander.ui.notifier.AbstractNotifier;

import static com.mucommander.conf.MuPreference.*;


/**
 * 'Misc' preferences panel.
 *
 * @author Maxence Bernard
 */
class MiscPanel extends PreferencesPanel implements ItemListener {

    /** Custom shell command text field */
    private PrefTextField customShellField;
	
    /** 'Use custom shell' radio button */
    private PrefRadioButton useCustomShellRadioButton;

    /** 'Check for updates on startup' checkbox */
    private PrefCheckBox checkForUpdatesCheckBox;

    /** 'Show confirmation dialog on quit' checkbox */
    private PrefCheckBox quitConfirmationCheckBox;
    
    /** 'Show splash screen' checkbox */
    private PrefCheckBox showSplashScreenCheckBox;

    /** 'Enable system notifications' checkbox */
    private PrefCheckBox systemNotificationsCheckBox;

    /** 'Enable Bonjour services discovery' checkbox */
    private PrefCheckBox bonjourDiscoveryCheckBox;

    /** Shell encoding auto-detect checkbox */
    private PrefCheckBox shellEncodingAutoDetectCheckbox;

    /** Shell encoding select box. */
    private PrefEncodingSelectBox shellEncodingSelectBox;

    /** Custom external terminal command text field */
    private PrefTextField customExternalTerminalField;

    /** 'Use custom external terminal' radio button */
    private PrefRadioButton useCustomExternalTerminalRadioButton;

    /** Custom builtin terminal command text field */
    private PrefTextField customTerminalShellField;

    /** 'Use custom builtin terminal shell' radio button */
    private PrefRadioButton useCustomTerminalShellRadioButton;


    private JPanel createShellEncodingPanel(PreferencesDialog parent) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEADING));

        shellEncodingAutoDetectCheckbox = new PrefCheckBox(Translator.get("prefs_dialog.auto_detect_shell_encoding"),
                checkBox -> checkBox.isSelected() != getVariable(AUTODETECT_SHELL_ENCODING, MuPreferences.DEFAULT_AUTODETECT_SHELL_ENCODING));

        boolean autoDetect = getVariable(AUTODETECT_SHELL_ENCODING, MuPreferences.DEFAULT_AUTODETECT_SHELL_ENCODING);
        shellEncodingAutoDetectCheckbox.setSelected(autoDetect);
        shellEncodingAutoDetectCheckbox.addItemListener(this);

        panel.add(shellEncodingAutoDetectCheckbox);

        shellEncodingSelectBox = new PrefEncodingSelectBox(new DialogOwner(parent), getVariable(SHELL_ENCODING)) {
            public boolean hasChanged() {
                return !getVariable(SHELL_ENCODING).equals(getSelectedEncoding());
            }
        };
        shellEncodingSelectBox.setEnabled(!autoDetect); 

        panel.add(shellEncodingSelectBox);

        return panel;
    }


    MiscPanel(PreferencesDialog parent) {
        super(parent, Translator.get("prefs_dialog.misc_tab"));

        setLayout(new BorderLayout());

        YBoxPanel northPanel = new YBoxPanel();

        JRadioButton useDefaultShellRadioButton = new JRadioButton(Translator.get("prefs_dialog.default_shell") + ':');
        useCustomShellRadioButton = new PrefRadioButton(Translator.get("prefs_dialog.custom_shell") + ':') {
			public boolean hasChanged() {
				return isSelected() != getVariable(USE_CUSTOM_SHELL, MuPreferences.DEFAULT_USE_CUSTOM_SHELL);
			}
        };

        // Use system default or custom shell ?
        if (getVariable(USE_CUSTOM_SHELL, MuPreferences.DEFAULT_USE_CUSTOM_SHELL)) {
            useCustomShellRadioButton.setSelected(true);
        } else {
            useDefaultShellRadioButton.setSelected(true);
        }

        useCustomShellRadioButton.addItemListener(this);

        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(useDefaultShellRadioButton);
        buttonGroup.add(useCustomShellRadioButton);

        // Shell panel
        XAlignedComponentPanel shellPanel = new XAlignedComponentPanel();
        shellPanel.setLabelLeftAligned(true);
        shellPanel.setBorder(BorderFactory.createTitledBorder(Translator.get("prefs_dialog.shell")));

        // create a path field with auto-completion capabilities
        customShellField = new PrefFilePathField(getVariable(CUSTOM_SHELL, "")) {
			public boolean hasChanged() {
				return isEnabled() && !getText().equals(getVariable(CUSTOM_SHELL));
			}
        };
        customShellField.setEnabled(useCustomShellRadioButton.isSelected());

        shellPanel.addRow(useDefaultShellRadioButton, new JLabel(DesktopManager.getDefaultShell()), 5);
        shellPanel.addRow(useCustomShellRadioButton, customShellField, 10);
        shellPanel.addRow(Translator.get("prefs_dialog.shell_encoding"), createShellEncodingPanel(parent), 5);

        northPanel.add(shellPanel, 5);

        northPanel.addSpace(10);


        // External Terminal panel
        XAlignedComponentPanel externalTerminalPanel = new XAlignedComponentPanel();
        externalTerminalPanel.setLabelLeftAligned(true);
        externalTerminalPanel.setBorder(BorderFactory.createTitledBorder(Translator.get("prefs_dialog.external_terminal")));

        JRadioButton useDefaultExternalTerminalRadioButton = new JRadioButton(Translator.get("prefs_dialog.default_terminal") + ':');
        useCustomExternalTerminalRadioButton = new PrefRadioButton(Translator.get("prefs_dialog.custom_terminal") + ':') {
            public boolean hasChanged() {
                return isSelected() != getVariable(USE_CUSTOM_EXTERNAL_TERMINAL, MuPreferences.DEFAULT_USE_CUSTOM_EXTERNAL_TERMINAL);
            }
        };

        // create a path field with auto-completion capabilities
        customExternalTerminalField = new PrefFilePathField(getVariable(CUSTOM_EXTERNAL_TERMINAL, "")) {
            public boolean hasChanged() {
                return isEnabled() && !getText().equals(getVariable(CUSTOM_EXTERNAL_TERMINAL));
            }
        };

        externalTerminalPanel.addRow(useDefaultExternalTerminalRadioButton, new JLabel(TerminalAction.getDefaultTerminalCommand()), 5);
        externalTerminalPanel.addRow(useCustomExternalTerminalRadioButton, customExternalTerminalField, 10);

        northPanel.add(externalTerminalPanel, 5);
        northPanel.addSpace(10);

        // Use system default or custom external terminal ?
        if (getVariable(USE_CUSTOM_EXTERNAL_TERMINAL, MuPreferences.DEFAULT_USE_CUSTOM_EXTERNAL_TERMINAL))
            useCustomExternalTerminalRadioButton.setSelected(true);
        else
            useDefaultExternalTerminalRadioButton.setSelected(true);
        customExternalTerminalField.setEnabled(useCustomExternalTerminalRadioButton.isSelected());

        useCustomExternalTerminalRadioButton.addItemListener(this);

        buttonGroup = new ButtonGroup();
        buttonGroup.add(useDefaultExternalTerminalRadioButton);
        buttonGroup.add(useCustomExternalTerminalRadioButton);


        // Builtin Terminal panel
        XAlignedComponentPanel terminalPanel = new XAlignedComponentPanel();
        terminalPanel.setLabelLeftAligned(true);
        terminalPanel.setBorder(BorderFactory.createTitledBorder(Translator.get("prefs_dialog.builtin_terminal")));

        JRadioButton useDefaultTerminalShellRadioButton = new JRadioButton(Translator.get("prefs_dialog.default_shell") + ':');
        useCustomTerminalShellRadioButton = new PrefRadioButton(Translator.get("prefs_dialog.custom_shell") + ':') {
            public boolean hasChanged() {
                return isSelected() != getVariable(TERMINAL_USE_CUSTOM_SHELL, MuPreferences.DEFAULT_TERMINAL_USE_CUSTOM_SHELL);
            }
        };

        // create a path field with auto-completion capabilities
        customTerminalShellField = new PrefFilePathField(getVariable(TERMINAL_SHELL, "")) {
            public boolean hasChanged() {
                return isEnabled() && !getText().equals(getVariable(TERMINAL_SHELL));
            }
        };

        terminalPanel.addRow(useDefaultTerminalShellRadioButton, new JLabel(DesktopManager.getDefaultTerminalShellCommand()), 5);
        terminalPanel.addRow(useCustomTerminalShellRadioButton, customTerminalShellField, 10);

        northPanel.add(terminalPanel, 5);
        northPanel.addSpace(10);

        // Use system default or custom builtin terminal ?
        if (getVariable(TERMINAL_USE_CUSTOM_SHELL, MuPreferences.DEFAULT_TERMINAL_USE_CUSTOM_SHELL))
            useCustomTerminalShellRadioButton.setSelected(true);
        else
            useDefaultTerminalShellRadioButton.setSelected(true);
        customTerminalShellField.setEnabled(useCustomTerminalShellRadioButton.isSelected());

        useCustomTerminalShellRadioButton.addItemListener(this);

        buttonGroup = new ButtonGroup();
        buttonGroup.add(useCustomTerminalShellRadioButton);
        buttonGroup.add(useDefaultTerminalShellRadioButton);


        // 'Show splash screen' option
        showSplashScreenCheckBox = new PrefCheckBox(Translator.get("prefs_dialog.show_splash_screen"),
                checkBox -> checkBox.isSelected() != getVariable(SHOW_SPLASH_SCREEN, MuPreferences.DEFAULT_SHOW_SPLASH_SCREEN));
        showSplashScreenCheckBox.setSelected(getVariable(SHOW_SPLASH_SCREEN, MuPreferences.DEFAULT_SHOW_SPLASH_SCREEN));
        northPanel.add(showSplashScreenCheckBox);

        // 'Check for updates on startup' option
        checkForUpdatesCheckBox = new PrefCheckBox(Translator.get("prefs_dialog.check_for_updates_on_startup"),
                checkBox -> checkBox.isSelected() != getVariable(CHECK_FOR_UPDATE, MuPreferences.DEFAULT_CHECK_FOR_UPDATE));
        checkForUpdatesCheckBox.setSelected(getVariable(CHECK_FOR_UPDATE, MuPreferences.DEFAULT_CHECK_FOR_UPDATE));
        northPanel.add(checkForUpdatesCheckBox);

        // 'Show confirmation dialog on quit' option
        quitConfirmationCheckBox = new PrefCheckBox(Translator.get("prefs_dialog.confirm_on_quit"),
                checkBox -> checkBox.isSelected() != getVariable(CONFIRM_ON_QUIT, MuPreferences.DEFAULT_CONFIRM_ON_QUIT));
        quitConfirmationCheckBox.setSelected(getVariable(CONFIRM_ON_QUIT, MuPreferences.DEFAULT_CONFIRM_ON_QUIT));
        northPanel.add(quitConfirmationCheckBox);

        // 'Enable system notifications' option, displayed only if current platform supports system notifications
        if (AbstractNotifier.isAvailable()) {
            systemNotificationsCheckBox = new PrefCheckBox(Translator.get("prefs_dialog.enable_system_notifications")+" ("+AbstractNotifier.getNotifier().getPrettyName()+")",
                    checkBox -> checkBox.isSelected() != getVariable(ENABLE_SYSTEM_NOTIFICATIONS, MuPreferences.DEFAULT_ENABLE_SYSTEM_NOTIFICATIONS));
            systemNotificationsCheckBox.setSelected(getVariable(ENABLE_SYSTEM_NOTIFICATIONS,
                                                                                     MuPreferences.DEFAULT_ENABLE_SYSTEM_NOTIFICATIONS));
            northPanel.add(systemNotificationsCheckBox);
        }

        // 'Enable Bonjour services discovery' option
        bonjourDiscoveryCheckBox = new PrefCheckBox(Translator.get("prefs_dialog.enable_bonjour_discovery"),
                checkBox -> checkBox.isSelected() != getVariable(ENABLE_BONJOUR_DISCOVERY, MuPreferences.DEFAULT_ENABLE_BONJOUR_DISCOVERY));

        bonjourDiscoveryCheckBox.setSelected(getVariable(ENABLE_BONJOUR_DISCOVERY, MuPreferences.DEFAULT_ENABLE_BONJOUR_DISCOVERY));
        northPanel.add(bonjourDiscoveryCheckBox);

        add(northPanel, BorderLayout.NORTH);
        
        customShellField.addDialogListener(parent);
    	useCustomShellRadioButton.addDialogListener(parent);
        useCustomExternalTerminalRadioButton.addDialogListener(parent);
        customExternalTerminalField.addDialogListener(parent);
        useCustomTerminalShellRadioButton.addDialogListener(parent);
        customTerminalShellField.addDialogListener(parent);
    	checkForUpdatesCheckBox.addDialogListener(parent);
    	quitConfirmationCheckBox.addDialogListener(parent);
        showSplashScreenCheckBox.addDialogListener(parent);
        bonjourDiscoveryCheckBox.addDialogListener(parent);
        shellEncodingAutoDetectCheckbox.addDialogListener(parent);
        shellEncodingSelectBox.addDialogListener(parent);
        if (systemNotificationsCheckBox != null) {
            systemNotificationsCheckBox.addDialogListener(parent);
        }
    }


    /////////////////////////////////
    // ItemListener implementation //
    /////////////////////////////////

    public void itemStateChanged(ItemEvent e) {
        Object source = e.getSource();
        if (source == useCustomShellRadioButton) {
            customShellField.setEnabled(useCustomShellRadioButton.isSelected());
        } else if (source == shellEncodingAutoDetectCheckbox) {
            shellEncodingSelectBox.setEnabled(!shellEncodingAutoDetectCheckbox.isSelected());
        } else if (source == useCustomExternalTerminalRadioButton) {
            customExternalTerminalField.setEnabled(useCustomExternalTerminalRadioButton.isSelected());
        } else if (source == useCustomTerminalShellRadioButton) {
            customTerminalShellField.setEnabled(useCustomTerminalShellRadioButton.isSelected());
        }
    }


    //////////////////////////////
    // PrefPanel implementation //
    //////////////////////////////

    @Override
    protected void commit() {
        MuPreferencesAPI pref = MuConfigurations.getPreferences();
    	pref.setVariable(CHECK_FOR_UPDATE, checkForUpdatesCheckBox.isSelected());

        // Saves the shell data.
    	pref.setVariable(USE_CUSTOM_SHELL, useCustomShellRadioButton.isSelected());
        pref.setVariable(CUSTOM_SHELL, customShellField.getText());

        // Saves the shell encoding data.
        boolean isAutoDetect = shellEncodingAutoDetectCheckbox.isSelected();
        pref.setVariable(AUTODETECT_SHELL_ENCODING, isAutoDetect);
        if (!isAutoDetect) {
            pref.setVariable(SHELL_ENCODING, shellEncodingSelectBox.getSelectedEncoding());
        }
        pref.setVariable(USE_CUSTOM_EXTERNAL_TERMINAL, useCustomExternalTerminalRadioButton.isSelected());
        pref.setVariable(CUSTOM_EXTERNAL_TERMINAL, customExternalTerminalField.getText());

        pref.setVariable(TERMINAL_USE_CUSTOM_SHELL, useCustomTerminalShellRadioButton.isSelected());
        pref.setVariable(TERMINAL_SHELL, customTerminalShellField.getText());

        pref.setVariable(CONFIRM_ON_QUIT, quitConfirmationCheckBox.isSelected());
        pref.setVariable(SHOW_SPLASH_SCREEN, showSplashScreenCheckBox.isSelected());

        boolean enabled;
        if (systemNotificationsCheckBox != null) {
            enabled = systemNotificationsCheckBox.isSelected();
            pref.setVariable(ENABLE_SYSTEM_NOTIFICATIONS, enabled);
            AbstractNotifier.getNotifier().setEnabled(enabled);
        }

        enabled = bonjourDiscoveryCheckBox.isSelected();
        pref.setVariable(ENABLE_BONJOUR_DISCOVERY, enabled);
        BonjourDirectory.setActive(enabled);
    }
}
