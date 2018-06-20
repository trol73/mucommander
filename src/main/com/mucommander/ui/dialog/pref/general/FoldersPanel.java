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

import com.mucommander.commons.runtime.OsFamily;
import com.mucommander.conf.MuConfigurations;
import com.mucommander.conf.MuPreference;
import com.mucommander.conf.MuPreferences;
import com.mucommander.conf.MuPreferencesAPI;
import com.mucommander.ui.dialog.pref.PreferencesDialog;
import com.mucommander.ui.dialog.pref.PreferencesPanel;
import com.mucommander.ui.dialog.pref.component.PrefCheckBox;
import com.mucommander.ui.dialog.pref.component.PrefComboBox;
import com.mucommander.ui.dialog.pref.component.PrefFilePathField;
import com.mucommander.ui.dialog.pref.component.PrefRadioButton;
import com.mucommander.ui.layout.SpringUtilities;
import com.mucommander.ui.layout.XBoxPanel;
import com.mucommander.ui.layout.YBoxPanel;
import com.mucommander.ui.main.WindowManager;
import com.mucommander.utils.text.Translator;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpringLayout;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;

/**
 * 'Folders' preferences panel.
 *
 * @author Maxence Bernard, Mariuz Jakubowski
 */
class FoldersPanel extends PreferencesPanel implements ItemListener, KeyListener, ActionListener {

    // Startup folders
    private PrefRadioButton lastFoldersRadioButton;
    private PrefRadioButton customFoldersRadioButton;

    private PrefFilePathFieldWithDefaultValue leftCustomFolderTextField;
    private JButton leftCustomFolderButton;

    private PrefFilePathFieldWithDefaultValue rightCustomFolderTextField;
    private JButton rightCustomFolderButton;

    private QuickSearchTimeoutCombobox comboQuickSearchTimeout;

    /**
     * Show hidden files?
     */
    private PrefCheckBox cbShowHiddenFiles;

    /**
     * Show Mac OS X .DS_Store?
     */
    private PrefCheckBox cbShowDSStoreFiles;

    /**
     * Show system folders ?
     */
    private PrefCheckBox cbShowSystemFolders;

    /**
     * Display compact file size ?
     */
    private PrefCheckBox cbCompactSize;

    /**
     * Follow symlinks when changing directory ?
     */
    private PrefCheckBox cbFollowSymlinks;

    /**
     * Always show single tab's header ?
     */
    private PrefCheckBox cbShowTabHeader;

    /**
     * Show quick search matches first in file panels
     */
    private PrefCheckBox cbShowQuickSearchMatchesFirst;

    /**
     * Calculate folder size on mark action
     */
    private PrefCheckBox cbCalculateFolderSizeOnMark;

    /**
     * Mark folders with files
     */
    private PrefCheckBox cbMarkFoldersWithFiles;

    /**
     * Mark files case sensitive filter
     */
    private PrefCheckBox cbMarkFilesCaseSensitiveFilter;

    /**
     * Block mark step size
     */
    private BlockMarkStepSizeCombobox comboBlockMarkStepSize;

    FoldersPanel(PreferencesDialog parent) {
        super(parent, Translator.get("prefs_dialog.folders_tab"));

        setLayout(new BorderLayout());

        // Startup folders panel
        YBoxPanel startupFolderPanel = new YBoxPanel();
        startupFolderPanel.setBorder(BorderFactory.createTitledBorder(Translator.get("prefs_dialog.startup_folders")));

        // Last folders or custom folders selections
        lastFoldersRadioButton = new PrefRadioButton(Translator.get("prefs_dialog.last_folder")) {
            public boolean hasChanged() {
                return !(isSelected() ?
                        MuPreferences.STARTUP_FOLDERS_LAST : MuPreferences.STARTUP_FOLDERS_CUSTOM).equals(
                        getVariable(MuPreference.STARTUP_FOLDERS));
            }
        };
        customFoldersRadioButton = new PrefRadioButton(Translator.get("prefs_dialog.custom_folder")) {
            public boolean hasChanged() {
                return !(isSelected() ?
                        MuPreferences.STARTUP_FOLDERS_CUSTOM : MuPreferences.STARTUP_FOLDERS_LAST).equals(
                        getVariable(MuPreference.STARTUP_FOLDERS));
            }
        };
        startupFolderPanel.add(lastFoldersRadioButton);
        startupFolderPanel.addSpace(5);
        startupFolderPanel.add(customFoldersRadioButton);

        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(lastFoldersRadioButton);
        buttonGroup.add(customFoldersRadioButton);

        customFoldersRadioButton.addItemListener(this);

        // Custom folders specification
        JLabel leftFolderLabel = new JLabel(Translator.get("prefs_dialog.left_folder"));
        leftFolderLabel.setAlignmentX(LEFT_ALIGNMENT);

        JLabel rightFolderLabel = new JLabel(Translator.get("prefs_dialog.right_folder"));
        rightFolderLabel.setAlignmentX(LEFT_ALIGNMENT);

        // Panel that contains the text field and button for specifying custom left folder
        XBoxPanel leftCustomFolderSpecifyingPanel = new XBoxPanel(5);
        leftCustomFolderSpecifyingPanel.setAlignmentX(LEFT_ALIGNMENT);

        // create a path field with auto-completion capabilities
        leftCustomFolderTextField = new PrefFilePathFieldWithDefaultValue(true);
        leftCustomFolderTextField.addKeyListener(this);
        leftCustomFolderSpecifyingPanel.add(leftCustomFolderTextField);

        leftCustomFolderButton = new JButton("...");
        leftCustomFolderButton.addActionListener(this);
        leftCustomFolderSpecifyingPanel.add(leftCustomFolderButton);

        // Panel that contains the text field and button for specifying custom right folder
        XBoxPanel rightCustomFolderSpecifyingPanel = new XBoxPanel(5);
        rightCustomFolderSpecifyingPanel.setAlignmentX(LEFT_ALIGNMENT);

        // create a path field with auto-completion capabilities
        rightCustomFolderTextField = new PrefFilePathFieldWithDefaultValue(false);
        rightCustomFolderTextField.addKeyListener(this);
        rightCustomFolderSpecifyingPanel.add(rightCustomFolderTextField);

        rightCustomFolderButton = new JButton("...");
        rightCustomFolderButton.addActionListener(this);
        rightCustomFolderSpecifyingPanel.add(rightCustomFolderButton);

        JPanel container = new JPanel(new SpringLayout());
        container.add(leftFolderLabel);
        container.add(leftCustomFolderSpecifyingPanel);
        container.add(rightFolderLabel);
        container.add(rightCustomFolderSpecifyingPanel);

        //Lay out the panel.
        SpringUtilities.makeCompactGrid(container,
                2, 2,       // rows, cols
                20, 6,      // initX, initY
                6, 6);      // xPad, yPad

        startupFolderPanel.add(container);

        if (getVariable(MuPreference.STARTUP_FOLDERS, "").equals(MuPreferences.STARTUP_FOLDERS_LAST)) {
            lastFoldersRadioButton.setSelected(true);
            setCustomFolderComponentsEnabled(false);
        } else {
            customFoldersRadioButton.setSelected(true);
        }

        // --------------------------------------------------------------------------------------------------------------

        YBoxPanel northPanel = new YBoxPanel();
        northPanel.add(startupFolderPanel);
        northPanel.addSpace(5);

        // ------- Quick search panel --------
        JPanel pnlQuickSearch = new JPanel(new SpringLayout());
        pnlQuickSearch.setBorder(BorderFactory.createTitledBorder(Translator.get("prefs_dialog.quick_search")));
        comboQuickSearchTimeout = new QuickSearchTimeoutCombobox();
        JLabel lblQuickSearchTimeout = new JLabel(Translator.get("prefs_dialog.quick_search_timeout"));
        lblQuickSearchTimeout.setAlignmentX(LEFT_ALIGNMENT);

        pnlQuickSearch.add(lblQuickSearchTimeout);
        pnlQuickSearch.add(comboQuickSearchTimeout);

        cbShowQuickSearchMatchesFirst = new PrefCheckBox(Translator.get("prefs_dialog.show_quick_search_matches_first"),
                checkBox -> checkBox.isSelected() != getVariable(MuPreference.SHOW_QUICK_SEARCH_MATCHES_FIRST, MuPreferences.DEFAULT_SHOW_QUICK_SEARCH_MATCHES_FIRST));
        cbShowQuickSearchMatchesFirst.setSelected(getVariable(MuPreference.SHOW_QUICK_SEARCH_MATCHES_FIRST, MuPreferences.DEFAULT_SHOW_QUICK_SEARCH_MATCHES_FIRST));
        pnlQuickSearch.add(cbShowQuickSearchMatchesFirst);
        pnlQuickSearch.add(Box.createHorizontalGlue());

        SpringUtilities.makeCompactGrid(pnlQuickSearch,
                2, 2,       // rows, cols
                6, 6,      // initX, initY
                6, 6);      // xPad, yPad

        northPanel.add(pnlQuickSearch);
        northPanel.addSpace(10);

        YBoxPanel pnlViewInt = new YBoxPanel();

        cbShowHiddenFiles = new PrefCheckBox(Translator.get("prefs_dialog.show_hidden_files"),
                checkBox -> checkBox.isSelected() != getVariable(MuPreference.SHOW_HIDDEN_FILES, MuPreferences.DEFAULT_SHOW_HIDDEN_FILES));
        cbShowHiddenFiles.setSelected(getVariable(MuPreference.SHOW_HIDDEN_FILES, MuPreferences.DEFAULT_SHOW_HIDDEN_FILES));
        pnlViewInt.add(cbShowHiddenFiles);

        // Mac OS X-only options
        if (OsFamily.MAC_OS_X.isCurrent()) {
            // Monitor cbShowHiddenFiles state to disable 'show .DS_Store files' option
            // when 'Show hidden files' is disabled, as .DS_Store files are hidden files
            cbShowHiddenFiles.addItemListener(this);

            cbShowDSStoreFiles = new PrefCheckBox(Translator.get("prefs_dialog.show_ds_store_files"),
                    checkBox -> checkBox.isSelected() != getVariable(MuPreference.SHOW_DS_STORE_FILES, MuPreferences.DEFAULT_SHOW_DS_STORE_FILES));

            cbShowDSStoreFiles.setSelected(getVariable(MuPreference.SHOW_DS_STORE_FILES, MuPreferences.DEFAULT_SHOW_DS_STORE_FILES));
            cbShowDSStoreFiles.setEnabled(cbShowHiddenFiles.isSelected());
            // Shift the check box to the right to indicate that it is a sub-option
            pnlViewInt.add(cbShowDSStoreFiles, 20);
        }

        if (OsFamily.MAC_OS_X.isCurrent() || OsFamily.WINDOWS.isCurrent()) {
            cbShowSystemFolders = new PrefCheckBox(Translator.get("prefs_dialog.show_system_folders"),
                    checkBox -> checkBox.isSelected() != getVariable(MuPreference.SHOW_SYSTEM_FOLDERS, MuPreferences.DEFAULT_SHOW_SYSTEM_FOLDERS));
            cbShowSystemFolders.setSelected(getVariable(MuPreference.SHOW_SYSTEM_FOLDERS, MuPreferences.DEFAULT_SHOW_SYSTEM_FOLDERS));
            pnlViewInt.add(cbShowSystemFolders);
        }

        cbCompactSize = new PrefCheckBox(Translator.get("prefs_dialog.compact_file_size"),
                checkBox -> checkBox.isSelected() != getVariable(MuPreference.DISPLAY_COMPACT_FILE_SIZE, MuPreferences.DEFAULT_DISPLAY_COMPACT_FILE_SIZE));

        cbCompactSize.setSelected(getVariable(MuPreference.DISPLAY_COMPACT_FILE_SIZE, MuPreferences.DEFAULT_DISPLAY_COMPACT_FILE_SIZE));
        pnlViewInt.add(cbCompactSize);

        cbShowTabHeader = new PrefCheckBox(Translator.get("prefs_dialog.show_tab_header"),
                checkBox -> checkBox.isSelected() != getVariable(MuPreference.SHOW_TAB_HEADER, MuPreferences.DEFAULT_SHOW_TAB_HEADER));
        cbShowTabHeader.setSelected(getVariable(MuPreference.SHOW_TAB_HEADER, MuPreferences.DEFAULT_SHOW_TAB_HEADER));
        pnlViewInt.add(cbShowTabHeader);

        JPanel pnlView = new JPanel(new FlowLayout(FlowLayout.LEADING));
        pnlView.setBorder(BorderFactory.createTitledBorder(Translator.get("prefs_dialog.view_options")));
        pnlView.add(pnlViewInt);

        northPanel.add(pnlView);
        northPanel.addSpace(10);

        cbFollowSymlinks = new PrefCheckBox(Translator.get("prefs_dialog.follow_symlinks_when_cd"),
                checkBox -> checkBox.isSelected() != getVariable(MuPreference.CD_FOLLOWS_SYMLINKS, MuPreferences.DEFAULT_CD_FOLLOWS_SYMLINKS));
        cbFollowSymlinks.setSelected(getVariable(MuPreference.CD_FOLLOWS_SYMLINKS, MuPreferences.DEFAULT_CD_FOLLOWS_SYMLINKS));
        northPanel.add(cbFollowSymlinks);
        northPanel.addSpace(10);

        JPanel pnlMarkFiles = new JPanel(new SpringLayout());
        pnlMarkFiles.setBorder(BorderFactory.createTitledBorder(Translator.get("prefs_dialog.mark_options")));

        cbCalculateFolderSizeOnMark = new PrefCheckBox(Translator.get("prefs_dialog.calculate_folder_size_on_mark"),
                checkBox -> checkBox.isSelected() != getVariable(MuPreference.CALCULATE_FOLDER_SIZE_ON_MARK, MuPreferences.DEFAULT_CALCULATE_FOLDER_SIZE_ON_MARK));
        cbCalculateFolderSizeOnMark.setSelected(getVariable(MuPreference.CALCULATE_FOLDER_SIZE_ON_MARK, MuPreferences.DEFAULT_CALCULATE_FOLDER_SIZE_ON_MARK));
        pnlMarkFiles.add(cbCalculateFolderSizeOnMark);
        pnlMarkFiles.add(Box.createHorizontalGlue());

        cbMarkFoldersWithFiles = new PrefCheckBox(Translator.get("prefs_dialog.mark_folders_with_files"),
                checkBox -> checkBox.isSelected() != getVariable(MuPreference.MARK_FOLDERS_WITH_FILES, MuPreferences.DEFAULT_MARK_FOLDERS_WITH_FILES));
        cbMarkFoldersWithFiles.setSelected(getVariable(MuPreference.MARK_FOLDERS_WITH_FILES, MuPreferences.DEFAULT_MARK_FOLDERS_WITH_FILES));
        pnlMarkFiles.add(cbMarkFoldersWithFiles);
        pnlMarkFiles.add(Box.createHorizontalGlue());

        cbMarkFilesCaseSensitiveFilter = new PrefCheckBox(Translator.get("prefs_dialog.mark_files_case_sensitive_filter"),
                checkBox -> checkBox.isSelected() != getVariable(MuPreference.MARK_FILES_CASE_SENSITIVE_FILTER, MuPreferences.DEFAULT_MARK_FILES_CASE_SENSITIVE_FILTER));
        cbMarkFilesCaseSensitiveFilter.setSelected(getVariable(MuPreference.MARK_FILES_CASE_SENSITIVE_FILTER, MuPreferences.DEFAULT_MARK_FILES_CASE_SENSITIVE_FILTER));
        pnlMarkFiles.add(cbMarkFilesCaseSensitiveFilter);
        pnlMarkFiles.add(Box.createHorizontalGlue());

        comboBlockMarkStepSize = new BlockMarkStepSizeCombobox();
        JLabel lblBlockMarkStepSize = new JLabel(Translator.get("prefs_dialog.block_mark_step_size"));
        lblBlockMarkStepSize.setAlignmentX(LEFT_ALIGNMENT);

        pnlMarkFiles.add(lblBlockMarkStepSize);
        pnlMarkFiles.add(comboBlockMarkStepSize);

        SpringUtilities.makeCompactGrid(pnlMarkFiles,
                4, 2,       // rows, cols
                6, 6,      // initX, initY
                6, 6);      // xPad, yPad

        northPanel.add(pnlMarkFiles);
        northPanel.addSpace(10);

        add(northPanel, BorderLayout.NORTH);

        lastFoldersRadioButton.addDialogListener(parent);
        customFoldersRadioButton.addDialogListener(parent);
        rightCustomFolderTextField.addDialogListener(parent);
        leftCustomFolderTextField.addDialogListener(parent);
        cbShowHiddenFiles.addDialogListener(parent);
        cbCompactSize.addDialogListener(parent);
        cbFollowSymlinks.addDialogListener(parent);
        cbShowTabHeader.addDialogListener(parent);
        cbShowQuickSearchMatchesFirst.addDialogListener(parent);
        cbCalculateFolderSizeOnMark.addDialogListener(parent);
        comboQuickSearchTimeout.addDialogListener(parent);
        if (OsFamily.MAC_OS_X.isCurrent()) {
            cbShowDSStoreFiles.addDialogListener(parent);
        }
        if (OsFamily.MAC_OS_X.isCurrent() || OsFamily.WINDOWS.isCurrent()) {
            cbShowSystemFolders.addDialogListener(parent);
        }
        cbMarkFoldersWithFiles.addDialogListener(parent);
        cbMarkFilesCaseSensitiveFilter.addDialogListener(parent);
        comboBlockMarkStepSize.addDialogListener(parent);
    }

    private void setCustomFolderComponentsEnabled(boolean enabled) {
        leftCustomFolderTextField.setEnabled(enabled);
        leftCustomFolderButton.setEnabled(enabled);
        rightCustomFolderTextField.setEnabled(enabled);
        rightCustomFolderButton.setEnabled(enabled);
    }


    /////////////////////////////////////
    // PreferencesPanel implementation //
    /////////////////////////////////////

    @Override
    protected void commit() {
        final MuPreferencesAPI pref = MuConfigurations.getPreferences();
        pref.setVariable(MuPreference.STARTUP_FOLDERS, lastFoldersRadioButton.isSelected() ? MuPreferences.STARTUP_FOLDERS_LAST : MuPreferences.STARTUP_FOLDERS_CUSTOM);

        pref.setVariable(MuPreference.LEFT_CUSTOM_FOLDER, leftCustomFolderTextField.getFilePath());

        pref.setVariable(MuPreference.RIGHT_CUSTOM_FOLDER, rightCustomFolderTextField.getFilePath());

        pref.setVariable(MuPreference.DISPLAY_COMPACT_FILE_SIZE, cbCompactSize.isSelected());

        pref.setVariable(MuPreference.CD_FOLLOWS_SYMLINKS, cbFollowSymlinks.isSelected());

        pref.setVariable(MuPreference.SHOW_TAB_HEADER, cbShowTabHeader.isSelected());

        pref.setVariable(MuPreference.SHOW_QUICK_SEARCH_MATCHES_FIRST, cbShowQuickSearchMatchesFirst.isSelected());

        pref.setVariable(MuPreference.CALCULATE_FOLDER_SIZE_ON_MARK, cbCalculateFolderSizeOnMark.isSelected());

        pref.setVariable(MuPreference.QUICK_SEARCH_TIMEOUT, comboQuickSearchTimeout.getValue());

        pref.setVariable(MuPreference.MARK_FOLDERS_WITH_FILES, cbMarkFoldersWithFiles.isSelected());

        pref.setVariable(MuPreference.MARK_FILES_CASE_SENSITIVE_FILTER, cbMarkFilesCaseSensitiveFilter.isSelected());

        pref.setVariable(MuPreference.BLOCK_MARK_STEP_SIZE, comboBlockMarkStepSize.getValue());

        // If one of the show/hide file filters have changed, refresh current folders of current MainFrame
        boolean refreshFolders = pref.setVariable(MuPreference.SHOW_HIDDEN_FILES, cbShowHiddenFiles.isSelected());

        if (OsFamily.MAC_OS_X.isCurrent()) {
            refreshFolders |= pref.setVariable(MuPreference.SHOW_DS_STORE_FILES, cbShowDSStoreFiles.isSelected());
        }

        if (OsFamily.MAC_OS_X.isCurrent() || OsFamily.WINDOWS.isCurrent()) {
            refreshFolders |= pref.setVariable(MuPreference.SHOW_SYSTEM_FOLDERS, cbShowSystemFolders.isSelected());
        }

        if (refreshFolders) {
            WindowManager.tryRefreshCurrentFolders();
        }
    }


    /////////////////////////////////
    // ItemListener implementation //
    /////////////////////////////////

    @Override
    public void itemStateChanged(ItemEvent event) {
        Object source = event.getSource();

        // Disable 'show .DS_Store files' option when 'Show hidden files' is disabled, as .DS_Store files are hidden files
        if (source == cbShowHiddenFiles) {
            cbShowDSStoreFiles.setEnabled(cbShowHiddenFiles.isSelected());
        } else if (source == customFoldersRadioButton) {
            setCustomFolderComponentsEnabled(customFoldersRadioButton.isSelected());
        }
    }


    ////////////////////////////////
    // KeyListener implementation //
    ////////////////////////////////

    /**
     * Catches key events to automatically select custom folder radio button if it was not already selected.
     */
    @Override
    public void keyTyped(KeyEvent e) {
        Object source = e.getSource();

        if (source == leftCustomFolderTextField || source == rightCustomFolderTextField) {
            if (!customFoldersRadioButton.isSelected()) {
                customFoldersRadioButton.setSelected(true);
            }
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {

    }

    @Override
    public void keyReleased(KeyEvent e) {

    }


    ///////////////////////////////////
    // ActionListener implementation //
    ///////////////////////////////////

    /**
     * Opens dialog for selecting starting folder.
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();

        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setDialogTitle(Translator.get("choose_folder"));
        chooser.setDialogType(JFileChooser.OPEN_DIALOG);
        if (chooser.showDialog(parent, Translator.get("choose")) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            if (source == leftCustomFolderButton) {
                leftCustomFolderTextField.setText(file.getPath());
                if (!customFoldersRadioButton.isSelected()) {
                    customFoldersRadioButton.setSelected(true);
                }
            } else if (source == rightCustomFolderButton) {
                rightCustomFolderTextField.setText(file.getPath());
                if (!customFoldersRadioButton.isSelected()) {
                    customFoldersRadioButton.setSelected(true);
                }
            }
        }
    }

    public class PrefFilePathFieldWithDefaultValue extends PrefFilePathField {

        private boolean isLeft;
        private final String HOME_FOLDER_PATH = System.getProperty("user.home");

        PrefFilePathFieldWithDefaultValue(boolean isLeft) {
            super(isLeft ? getVariable(MuPreference.LEFT_CUSTOM_FOLDER, "") : getVariable(MuPreference.RIGHT_CUSTOM_FOLDER, ""));
            this.isLeft = isLeft;
        }

        @Override
        public boolean hasChanged() {
            return isLeft ?
                    !getText().equals(getVariable(MuPreference.LEFT_CUSTOM_FOLDER)) :
                    !getText().equals(getVariable(MuPreference.RIGHT_CUSTOM_FOLDER));
        }

        String getFilePath() {
            String text = super.getText();

            return text.trim().isEmpty() ? HOME_FOLDER_PATH : text;
        }

    }

    private class QuickSearchTimeoutCombobox extends PrefComboBox<String> {

        QuickSearchTimeoutCombobox() {
            super();
            populate();
        }

        void populate() {
            addItem(Translator.get("prefs_dialog.quick_search_timeout_never"));
            int selectedIndex = 0;
            long prefVal = getPrefValue();
            int step = 1;
            for (int i = 1; i <= 60; i += step) {
                if (i == 5) {
                    step = 5;
                } else if (i == 30) {
                    step = 10;
                }
                addItem(i + " " + Translator.get("prefs_dialog.quick_search_timeout_sec"));
                if (i == prefVal / 1000) {
                    selectedIndex = getItemCount() - 1;
                }
            }
            setSelectedIndex(selectedIndex);
        }

        long getPrefValue() {
            return MuConfigurations.getPreferences().getVariable(MuPreference.QUICK_SEARCH_TIMEOUT, MuPreferences.DEFAULT_QUICK_SEARCH_TIMEOUT);
        }

        long getValue() {
            if (getSelectedIndex() == 0) {
                return 0;
            }
            String val = getSelectedItem();
            return Integer.parseInt(val.substring(0, val.indexOf(' '))) * 1000;
        }

        @Override
        public boolean hasChanged() {
            return getValue() != getPrefValue();
        }

    }

    private class BlockMarkStepSizeCombobox extends QuickSearchTimeoutCombobox {

        @Override
        void populate() {
            int selectedIndex = 0;
            long prefVal = getPrefValue();
            int step = 1;
            for (int i = 2; i <= 60; i += step) {
                if (i == 10) {
                    step = 5;
                } else if (i == 30) {
                    step = 10;
                }
                addItem(i + " " + Translator.get("prefs_dialog.block_mark_step_size_name"));
                if (i == prefVal) {
                    selectedIndex = getItemCount() - 1;
                }
            }
            setSelectedIndex(selectedIndex);
        }

        @Override
        long getValue() {
            return super.getValue() / 1000;
        }

        @Override
        long getPrefValue() {
            return MuConfigurations.getPreferences().getVariable(MuPreference.BLOCK_MARK_STEP_SIZE, MuPreferences.DEFAULT_BLOCK_MARK_STEP_SIZE);
        }

    }

}
