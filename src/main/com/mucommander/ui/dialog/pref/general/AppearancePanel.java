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

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.FileFactory;
import com.mucommander.commons.runtime.OsVersion;
import com.mucommander.conf.MuConfigurations;
import com.mucommander.conf.MuPreference;
import com.mucommander.conf.MuPreferences;
import com.mucommander.conf.MuPreferencesAPI;
import com.mucommander.extension.ClassFinder;
import com.mucommander.extension.ExtensionManager;
import com.mucommander.extension.LookAndFeelFilter;
import com.mucommander.job.FileCollisionChecker;
import com.mucommander.ui.dialog.InformationDialog;
import com.mucommander.ui.dialog.QuestionDialog;
import com.mucommander.ui.dialog.file.FileCollisionDialog;
import com.mucommander.ui.dialog.pref.PreferencesDialog;
import com.mucommander.ui.dialog.pref.PreferencesPanel;
import com.mucommander.ui.dialog.pref.component.PrefCheckBox;
import com.mucommander.ui.dialog.pref.component.PrefComboBox;
import com.mucommander.ui.dialog.pref.theme.ThemeEditorDialog;
import com.mucommander.ui.icon.FileIcons;
import com.mucommander.ui.icon.IconManager;
import com.mucommander.ui.icon.SpinningDial;
import com.mucommander.ui.layout.ProportionalGridPanel;
import com.mucommander.ui.layout.YBoxPanel;
import com.mucommander.ui.main.WindowManager;
import com.mucommander.ui.theme.Theme;
import com.mucommander.ui.theme.ThemeManager;
import com.mucommander.ui.widgets.render.BasicComboBoxRenderer;
import com.mucommander.utils.FileIconsCache;
import com.mucommander.utils.text.Translator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

/**
 * 'Appearance' preferences panel.
 * @author Maxence Bernard, Nicolas Rinaudo
 */
class AppearancePanel extends PreferencesPanel implements ActionListener, Runnable {
	private static final Logger LOGGER = LoggerFactory.getLogger(AppearancePanel.class);
	
    // - Look and feel fields ------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /** Combo box containing the list of available look&feels. */
    private PrefComboBox<String> lookAndFeelComboBox;
    /** All available look&feels. */
    private UIManager.LookAndFeelInfo lookAndFeels[];
    /** 'Use brushed metal look' checkbox */
    private PrefCheckBox              brushedMetalCheckBox;
    /** 'Use screen menu bar' checkbox */
    private PrefCheckBox              screenMenuBarCheckBox;
    /** Triggers look and feel importing. */
    private JButton                   importLookAndFeelButton;
    /** Triggers look and feel deletion. */
    private JButton                   deleteLookAndFeelButton;
    /** Used to notify the user that the system is working. */
    private SpinningDial              dial;
    /** File from which to import looks and feels. */
    private AbstractFile              lookAndFeelLibrary;



    // - Icon size fields ----------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /** Displays the list of available sizes for toolbar icons. */
    private PrefComboBox<String> toolbarIconsSizeComboBox;
    /** Displays the list of available sizes for command bar icons. */
    private PrefComboBox<String> commandBarIconsSizeComboBox;
    /** Displays the list of available sizes for file icons. */
    private PrefComboBox<String>fileIconsSizeComboBox;
    /** All icon sizes label. */
    private final static String ICON_SIZES[]                = {"100%", "125%", "150%", "175%", "200%", "300%"};
    /** All icon sizes scale factors. */
    private final static float  ICON_SCALE_FACTORS[]        = {1.0f, 1.25f, 1.5f, 1.75f, 2.0f, 3.0f};



    // - Icons ---------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /** Icon used to identify 'locked' themes. */
    private ImageIcon lockIcon;
    /** Transparent icon used to align non-locked themes with the others. */
    private ImageIcon transparentIcon;



    // - Theme fields --------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /** Lists all available themes. */
    private PrefComboBox<Theme> themeComboBox;
    /** Triggers the theme editor. */
    private JButton      editThemeButton;
    /** Triggers the theme duplication dialog. */
    private JButton      duplicateThemeButton;
    /** Triggers the theme import dialog. */
    private JButton      importThemeButton;
    /** Triggers the theme export dialog. */
    private JButton      exportThemeButton;
    /** Triggers the theme rename dialog. */
    private JButton      renameThemeButton;
    /** Triggers the theme delete dialog. */
    private JButton      deleteThemeButton;
    /** Used to display the currently selected theme's type. */
    private JLabel       typeLabel;
    /** Whether or not to ignore theme combobox related events. */
    private boolean      ignoreComboChanges;
    /** Last folder that was selected in import or export operations. */
    private AbstractFile lastSelectedFolder;

    // - Editor Theme fields --------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /** Lists all available editor themes. */
    private PrefComboBox<String> syntaxThemeComboBox;



    // - Misc. fields --------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /** System icon combobox. */
    private PrefComboBox<String> useSystemFileIconsComboBox;
    /** Identifier of 'yes' actions in question dialogs. */
    private final static int       YES_ACTION = 0;
    /** Identifier of 'no' actions in question dialogs. */
    private final static int       NO_ACTION = 1;
    /** All known custom look and feels. */
    private List<String> customLookAndFeels;



    // - Initialisation ---------------------------------------------------------
    // --------------------------------------------------------------------------
    /**
     * Creates a new appearance panel with the specified parent.
     * @param parent dialog in which this panel is placed.
     */
    AppearancePanel(PreferencesDialog parent) {
        super(parent, Translator.get("prefs_dialog.appearance_tab"));
        initUI();

        // Initialises the known custom look and feels
        initializeCustomLookAndFeels();
    }



    // - UI initialisation ------------------------------------------------------
    // --------------------------------------------------------------------------
    private void initUI() {
        YBoxPanel mainPanel;

        mainPanel = new YBoxPanel();

        // Look and feel.
        mainPanel.add(createLookAndFeelPanel());
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        // Themes.
        mainPanel.add(createThemesPanel());
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        // Syntax highlighting.
        mainPanel.add(createSyntaxHighlightThemePanel());
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        // System icons.
        mainPanel.add(createSystemIconsPanel());
        mainPanel.add(Box.createVerticalGlue());

        // Icon size.
        mainPanel.add(createIconSizePanel());
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        setLayout(new BorderLayout());
        add(mainPanel, BorderLayout.NORTH);
        
        lookAndFeelComboBox.addDialogListener(parent);
        themeComboBox.addDialogListener(parent);
        syntaxThemeComboBox.addDialogListener(parent);
        useSystemFileIconsComboBox.addDialogListener(parent);
        toolbarIconsSizeComboBox.addDialogListener(parent);
        commandBarIconsSizeComboBox.addDialogListener(parent);
        fileIconsSizeComboBox.addDialogListener(parent);
        if (brushedMetalCheckBox != null) {
        	brushedMetalCheckBox.addDialogListener(parent);
        }
        if (screenMenuBarCheckBox != null) {
            screenMenuBarCheckBox.addDialogListener(parent);
        }
    }

    /**
     * Populates the look&feel combo box with all available look&feels.
     */
    private void populateLookAndFeels() {
        lookAndFeelComboBox.removeAllItems();
        initializeAvailableLookAndFeels();

        // Populates the combo box.
        int currentIndex = -1;
        String currentName = UIManager.getLookAndFeel().getClass().getName();
        for (int i = 0; i < lookAndFeels.length; i++) {
            // Looks for the currently selected look&feel.
            if (lookAndFeels[i].getClassName().equals(currentName)) {
                currentIndex = i;
            }
            lookAndFeelComboBox.addItem(lookAndFeels[i].getName());
        }

        // Sets the initial selection.
        if (currentIndex < 0) {
            currentIndex = 0;
        }
        lookAndFeelComboBox.setSelectedIndex(currentIndex);
    }

    /**
     * Creates the look and feel panel.
     * @return the look and feel panel.
     */
    private JPanel createLookAndFeelPanel() {
        // Creates the panel.
        JPanel lnfPanel = new YBoxPanel();
        lnfPanel.setAlignmentX(LEFT_ALIGNMENT);
        lnfPanel.setBorder(BorderFactory.createTitledBorder(Translator.get("prefs_dialog.look_and_feel")));

        // Creates the look and feel combo box.
        lookAndFeelComboBox = new PrefComboBox<String>() {
			public boolean hasChanged() {
                String lnf = getVariable(MuPreference.LOOK_AND_FEEL);
				int selectedIndex = getSelectedIndex();
                return selectedIndex >= 0 && !lookAndFeels[selectedIndex].getClassName().equals(lnf);
                }
			};
        lookAndFeelComboBox.setRenderer(new BasicComboBoxRenderer<String>() {
                @Override
                public Component getListCellRendererComponent(JList<? extends String> list, String value, int index, boolean isSelected, boolean cellHasFocus) {
                    JLabel label = (JLabel)super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                    if (index < 0) {
                        return label;
                    }
                    // All look and feels that are not modifiable must be flagged with a lock icon.
                    label.setIcon(isLookAndFeelModifiable(lookAndFeels[index]) ? transparentIcon : lockIcon);
                    return label;
                }
            });

        // Populates the look and feel combo box.
        populateLookAndFeels();

        // Initialises buttons and event listening.
        importLookAndFeelButton = new JButton(Translator.get("prefs_dialog.import") + "...");
        deleteLookAndFeelButton = new JButton(Translator.get("delete"));
        importLookAndFeelButton.addActionListener(this);
        deleteLookAndFeelButton.addActionListener(this);
        resetLookAndFeelButtons();
        lookAndFeelComboBox.addActionListener(this);

        // Adds the look and feel list and the action buttons to the panel.
        JPanel flowPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        flowPanel.add(lookAndFeelComboBox);
        flowPanel.add(importLookAndFeelButton);
        flowPanel.add(deleteLookAndFeelButton);
        flowPanel.add(new JLabel(dial = new SpinningDial()));
        lnfPanel.add(flowPanel);

        // For Mac OS X only, creates the 'brushed metal' checkbox.
        // At the time of writing, the 'brushed metal' look causes the JVM to crash randomly under Leopard (10.5)
        // so we disable brushed metal on that OS version but leave it for earlier versions where it works fine.
        // See http://www.mucommander.com/forums/viewtopic.php?f=4&t=746 for more info about this issue.
        if (OsVersion.MAC_OS_X_10_4.isCurrentOrLower() || OsVersion.MAC_OS_X_10_13.isCurrentOrHigher()) {
            flowPanel = new YBoxPanel();
            // 'Use brushed metal look' option
            brushedMetalCheckBox = new PrefCheckBox(Translator.get("prefs_dialog.use_brushed_metal"),
                    checkBox -> !String.valueOf(checkBox.isSelected()).equals(getVariable(MuPreference.USE_BRUSHED_METAL)));
            brushedMetalCheckBox.setSelected(getVariable(MuPreference.USE_BRUSHED_METAL, MuPreferences.DEFAULT_USE_BRUSHED_METAL));
            flowPanel.add(brushedMetalCheckBox);

            screenMenuBarCheckBox = new PrefCheckBox(Translator.get("prefs_dialog.screen_menu_bar"),
                    checkBox -> !String.valueOf(checkBox.isSelected()).equals(getVariable(MuPreference.USE_SCREEN_MENU_BAR)));
            screenMenuBarCheckBox.setSelected(getVariable(MuPreference.USE_SCREEN_MENU_BAR, MuPreferences.DEFAULT_USE_SCREEN_MENU_BAR));
            flowPanel.add(screenMenuBarCheckBox);

            lnfPanel.add(flowPanel);
        }

        return lnfPanel;
    }

    /**
     * Creates the icon size panel.
     * @return the icon size panel.
     */
    private JPanel createIconSizePanel() {
        ProportionalGridPanel gridPanel = new ProportionalGridPanel(2);

        gridPanel.add(new JLabel(Translator.get("prefs_dialog.toolbar_icons")));
        gridPanel.add(toolbarIconsSizeComboBox = createIconSizeCombo(MuPreference.TOOLBAR_ICON_SCALE, MuPreferences.DEFAULT_TOOLBAR_ICON_SCALE));

        gridPanel.add(new JLabel(Translator.get("prefs_dialog.command_bar_icons")));
        gridPanel.add(commandBarIconsSizeComboBox = createIconSizeCombo(MuPreference.COMMAND_BAR_ICON_SCALE, MuPreferences.DEFAULT_COMMAND_BAR_ICON_SCALE));

        gridPanel.add(new JLabel(Translator.get("prefs_dialog.file_icons")));
        gridPanel.add(fileIconsSizeComboBox = createIconSizeCombo(MuPreference.TABLE_ICON_SCALE, MuPreferences.DEFAULT_TABLE_ICON_SCALE));

        JPanel flowPanel = new JPanel(new FlowLayout(FlowLayout.LEADING));
        flowPanel.setBorder(BorderFactory.createTitledBorder(Translator.get("prefs_dialog.icons_size")));
        flowPanel.add(gridPanel);

        return flowPanel;
    }

    /**
     * Creates the themes panel.
     * @return the themes panel.
     */
    private JPanel createThemesPanel() {
        JPanel gridPanel = new ProportionalGridPanel(4);

        // Creates the various panel's buttons.
        editThemeButton      = new JButton(Translator.get("edit") + "...");
        importThemeButton    = new JButton(Translator.get("prefs_dialog.import") + "...");
        exportThemeButton    = new JButton(Translator.get("prefs_dialog.export") + "...");
        renameThemeButton    = new JButton(Translator.get("rename"));
        deleteThemeButton    = new JButton(Translator.get("delete"));
        duplicateThemeButton = new JButton(Translator.get("duplicate"));
        editThemeButton.addActionListener(this);
        importThemeButton.addActionListener(this);
        exportThemeButton.addActionListener(this);
        renameThemeButton.addActionListener(this);
        deleteThemeButton.addActionListener(this);
        duplicateThemeButton.addActionListener(this);

        // Creates the panel's 'type label'.
        typeLabel = new JLabel("");

        // Creates the theme combo box.
        themeComboBox = new PrefComboBox<Theme>() {
			public boolean hasChanged() {
				return !ThemeManager.isCurrentTheme(getSelectedItem());
			}
        };
        themeComboBox.addActionListener(this);

        // Sets the combobox's renderer.
        lockIcon = IconManager.getIcon(IconManager.IconSet.PREFERENCES, "lock.png");
        transparentIcon = new ImageIcon(new BufferedImage(lockIcon.getIconWidth(), lockIcon.getIconHeight(), BufferedImage.TYPE_INT_ARGB));
        themeComboBox.setRenderer(new BasicComboBoxRenderer<Theme>() {
                @Override
                public Component getListCellRendererComponent(JList<? extends Theme> list, Theme theme, int index, boolean isSelected, boolean cellHasFocus) {
                    JLabel label = (JLabel)super.getListCellRendererComponent(list, theme, index, isSelected, cellHasFocus);
                    if(ThemeManager.isCurrentTheme(theme))
                        label.setText(theme.getName() +  " (" + Translator.get("theme.current") + ")");
                    else
                        label.setText(theme.getName());

                    label.setIcon(theme.getType() == Theme.Type.PREDEFINED ? lockIcon : transparentIcon);

                    return label;
                }
            });

        // Initialises the content of the combo box.
        populateThemes(ThemeManager.getCurrentTheme());

        gridPanel.add(themeComboBox);
        gridPanel.add(editThemeButton);
        gridPanel.add(importThemeButton);
        gridPanel.add(exportThemeButton);

        gridPanel.add(typeLabel);
        gridPanel.add(renameThemeButton);
        gridPanel.add(deleteThemeButton);
        gridPanel.add(duplicateThemeButton);

        JPanel flowPanel = new JPanel(new FlowLayout(FlowLayout.LEADING));
        flowPanel.setBorder(BorderFactory.createTitledBorder(Translator.get("prefs_dialog.themes")));
        flowPanel.add(gridPanel);

        return flowPanel;
    }

    private JPanel createSyntaxHighlightThemePanel() {
        JPanel gridPanel = new ProportionalGridPanel(1);

        syntaxThemeComboBox = new PrefComboBox<String>(ThemeManager.predefinedSyntaxThemeNames()) {
            @Override
            public boolean hasChanged() {
                String selectedTheme = getSelectedItem();
                return !ThemeManager.getCurrentSyntaxThemeName().equalsIgnoreCase(selectedTheme);
            }
        };
        syntaxThemeComboBox.addActionListener(this);
        syntaxThemeComboBox.setSelectedItem(ThemeManager.getCurrentSyntaxThemeName());
        gridPanel.add(syntaxThemeComboBox);

        JPanel flowPanel = new JPanel(new FlowLayout(FlowLayout.LEADING));
        flowPanel.setBorder(BorderFactory.createTitledBorder(Translator.get("prefs_dialog.syntax_themes")));
        flowPanel.add(gridPanel);

        return flowPanel;
    }

	private void populateThemes(Theme selectedTheme) {
        ignoreComboChanges = true;

        themeComboBox.removeAllItems();

		// Creates new theme instances for all but the currentTheme (current as the currently active one - not
		// the currently selected one!) so we might need to find the new instance of our previously selected theme.
        Iterator<Theme> themes = ThemeManager.availableThemes();
		Theme selectedThemeAvailableInstance = null;

        while (themes.hasNext()) {
			final Theme availableTheme = themes.next();
			themeComboBox.addItem(availableTheme);
			if (availableTheme.equals(selectedTheme)) {
				selectedThemeAvailableInstance = availableTheme;
			}
        }

        ignoreComboChanges = false;

		if (selectedThemeAvailableInstance != null) {
			themeComboBox.setSelectedItem(selectedThemeAvailableInstance);
		} else {
			LOGGER.warn("selected theme not available anymore");
			themeComboBox.setSelectedIndex(0);
		}
    }

    /**
     * Creates the system icons panel.
     * @return the system icons panel.
     */
    private JPanel createSystemIconsPanel() {
        // 'Use system file icons' combo box
        this.useSystemFileIconsComboBox = new PrefComboBox<String>() {
			public boolean hasChanged() {
				String systemIconsPolicy;
				switch (useSystemFileIconsComboBox.getSelectedIndex()) {
                    case 0:
                        systemIconsPolicy = FileIcons.USE_SYSTEM_ICONS_NEVER;
                        break;
                    case 1:
                        systemIconsPolicy = FileIcons.USE_SYSTEM_ICONS_APPLICATIONS;
                        break;
                    default:
                        systemIconsPolicy = FileIcons.USE_SYSTEM_ICONS_ALWAYS;
				}
				return !systemIconsPolicy.equals(getVariable(MuPreference.USE_SYSTEM_FILE_ICONS, systemIconsPolicy));
			}
        };
        useSystemFileIconsComboBox.addItem(Translator.get("prefs_dialog.use_system_file_icons.never"));
        useSystemFileIconsComboBox.addItem(Translator.get("prefs_dialog.use_system_file_icons.applications"));
        useSystemFileIconsComboBox.addItem(Translator.get("prefs_dialog.use_system_file_icons.always"));
        String systemIconsPolicy = FileIcons.getSystemIconsPolicy();

        useSystemFileIconsComboBox.setSelectedIndex(FileIcons.USE_SYSTEM_ICONS_ALWAYS.equals(systemIconsPolicy) ? 2 : FileIcons.USE_SYSTEM_ICONS_APPLICATIONS.equals(systemIconsPolicy) ? 1 : 0);
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(BorderFactory.createTitledBorder(Translator.get("prefs_dialog.use_system_file_icons")));
        panel.add(useSystemFileIconsComboBox);

        return panel;
    }

    /**
     * Creates a combo box that allows to choose a size for a certain type of icon. The returned combo box is filled
     * with allowed choices, and the current configuration value is selected.
     *
     * @param preference
     * @param defaultValue the default value for the icon scale factor if the configuration variable has no value
     * @return a combo box that allows to choose a size for a certain type of icon
     */
    private PrefComboBox<String> createIconSizeCombo(final MuPreference preference, float defaultValue) {
    	PrefComboBox<String> iconSizeCombo = new PrefComboBox<String>(ICON_SIZES) {
			public boolean hasChanged() {
				return !String.valueOf(ICON_SCALE_FACTORS[getSelectedIndex()]).equals(getVariable(preference));
			}
    	};
        float scaleFactor = getVariable(preference, defaultValue);
        int index = 0;
        for (int i = 0; i < ICON_SCALE_FACTORS.length; i++) {
            if (scaleFactor == ICON_SCALE_FACTORS[i]) {
                index = i;
                break;
            }
        }
        iconSizeCombo.setSelectedIndex(index);

        return iconSizeCombo;
    }


    ///////////////////////
    // PrefPanel methods //
    ///////////////////////
    @Override
    protected void commit() {
        final MuPreferencesAPI pref = MuConfigurations.getPreferences();

        // Look and Feel
        if (pref.setVariable(MuPreference.LOOK_AND_FEEL, lookAndFeels[lookAndFeelComboBox.getSelectedIndex()].getClassName())) {
            resetLookAndFeelButtons();
            SwingUtilities.updateComponentTreeUI(parent);
        }

        if (brushedMetalCheckBox != null) {
            pref.setVariable(MuPreference.USE_BRUSHED_METAL, brushedMetalCheckBox.isSelected());
        }
        if (screenMenuBarCheckBox != null) {
            pref.setVariable(MuPreference.USE_SCREEN_MENU_BAR, screenMenuBarCheckBox.isSelected());
        }

        // Set ToolBar's icon size
        float scaleFactor = ICON_SCALE_FACTORS[toolbarIconsSizeComboBox.getSelectedIndex()];
        pref.setVariable(MuPreference.TOOLBAR_ICON_SCALE, scaleFactor);

        // Set CommandBar's icon size
        scaleFactor = ICON_SCALE_FACTORS[commandBarIconsSizeComboBox.getSelectedIndex()];
        pref.setVariable(MuPreference.COMMAND_BAR_ICON_SCALE , scaleFactor);

        // Set file icon size
        scaleFactor = ICON_SCALE_FACTORS[fileIconsSizeComboBox.getSelectedIndex()];
        // Set scale factor in FileIcons first so that it has the new value when ConfigurationListener instances call it
        FileIcons.setScaleFactor(scaleFactor);
        FileIconsCache.getInstance().clear();
        pref.setVariable(MuPreference.TABLE_ICON_SCALE , scaleFactor);

        // Sets the current theme.
        if (!ThemeManager.isCurrentTheme(themeComboBox.getSelectedItem())) {
            ThemeManager.setCurrentTheme(themeComboBox.getSelectedItem());
            resetThemeButtons(themeComboBox.getSelectedItem());
            themeComboBox.repaint();
        }

        // Sets the current syntax theme.
        final String syntaxThemeName = syntaxThemeComboBox.getSelectedItem();
        if (!ThemeManager.getCurrentSyntaxThemeName().equalsIgnoreCase(syntaxThemeName)) {
            ThemeManager.setCurrentSyntaxTheme(syntaxThemeName);
            syntaxThemeComboBox.repaint();
        }

        // Set system icons policy
        int comboIndex = useSystemFileIconsComboBox.getSelectedIndex();
        String systemIconsPolicy = comboIndex == 0 ? FileIcons.USE_SYSTEM_ICONS_NEVER : comboIndex == 1 ? FileIcons.USE_SYSTEM_ICONS_APPLICATIONS : FileIcons.USE_SYSTEM_ICONS_ALWAYS;
        FileIcons.setSystemIconsPolicy(systemIconsPolicy);
        pref.setVariable(MuPreference.USE_SYSTEM_FILE_ICONS, systemIconsPolicy);
    }



    // - Look and feel actions --------------------------------------------------
    // --------------------------------------------------------------------------
    /**
     * Initialises the list of custom look&feels.
     */
    private void initializeCustomLookAndFeels() {
        customLookAndFeels = getListVariable(MuPreference.CUSTOM_LOOK_AND_FEELS, MuPreferences.CUSTOM_LOOK_AND_FEELS_SEPARATOR);
    }

    /**
     * Initialises the list of available look&feels.
     */
    private void initializeAvailableLookAndFeels() {
        // Loads all available look and feels.
        lookAndFeels = UIManager.getInstalledLookAndFeels();

        // Sorts them.
        Arrays.sort(lookAndFeels, new Comparator<UIManager.LookAndFeelInfo>() {
                public int compare(UIManager.LookAndFeelInfo a, UIManager.LookAndFeelInfo b) {return a.getName().compareTo(b.getName());}
                public boolean equals(Object a) {return false;}
            });
    }

    /**
     * Returns <code>true</code> if the specified class name is that of a custom look and feel.
     * @return <code>true</code> if the specified class name is that of a custom look and feel, <code>false</code> otherwise.
     */
    private boolean isCustomLookAndFeel(String className) {
        return customLookAndFeels != null && customLookAndFeels.contains(className);
    }

    /**
     * Returns <code>true</code> if the specified look and feel is modifiable.
     * <p>
     * To be modifiable, a look and feel must meet all of the following conditions:
     * <ul>
     *   <li>It must be a custom look and feel.</li>
     *   <li>It cannot be the application's current look and feel.</li>
     * </ul>
     *
     * @return <code>true</code> if the specified look and feel is modifiable, <code>false</code> otherwise.
     */
    private boolean isLookAndFeelModifiable(UIManager.LookAndFeelInfo laf) {
        return isCustomLookAndFeel(laf.getClassName()) && !laf.getClassName().equals(UIManager.getLookAndFeel().getClass().getName());
    }

    /**
     * Resets the enabled status of the various look and feel buttons depending on the current selection.
     */
    private void resetLookAndFeelButtons() {
        // If the dial is animated, we're currently loading look&feels and should ignore this call.
        if (dial == null || !dial.isAnimated()) {
            int selectedIndex = lookAndFeelComboBox.getSelectedIndex();
            if (selectedIndex >= 0) {
                deleteLookAndFeelButton.setEnabled(isLookAndFeelModifiable(lookAndFeels[selectedIndex]));
            }
        }
    }

    /**
     * Uninstalls the specified look and feel.
     * @param selection look and feel to uninstall.
     */
    private void uninstallLookAndFeel(UIManager.LookAndFeelInfo selection) {
        UIManager.LookAndFeelInfo[] buffer;      // New array of installed look and feels.
        int                         bufferIndex; // Current index in buffer.

        // Copies the content of lookAndFeels into buffer, skipping over the look and feel to uninstall.
        buffer      = new UIManager.LookAndFeelInfo[lookAndFeels.length - 1];
        bufferIndex = 0;
        for (UIManager.LookAndFeelInfo lookAndFeel : lookAndFeels) {
            if (!selection.getClassName().equals(lookAndFeel.getClassName())) {
                buffer[bufferIndex] = lookAndFeel;
                bufferIndex++;
            }
        }

        // Resets the list of installed look and feels.
        UIManager.setInstalledLookAndFeels(lookAndFeels = buffer);
    }

    /**
     * Deletes the specified look and feel from the list of custom look and feels.
     * @param selection currently selection look and feel.
     */
    private void deleteCustomLookAndFeel(UIManager.LookAndFeelInfo selection) {
        if (customLookAndFeels != null) {
            if (customLookAndFeels.remove(selection.getClassName())) {
                MuConfigurations.getPreferences().setVariable(MuPreference.CUSTOM_LOOK_AND_FEELS, customLookAndFeels, MuPreferences.CUSTOM_LOOK_AND_FEELS_SEPARATOR);
            }
        }
    }

    /**
     * Deletes the currently selected look and feel.
     * <p>
     * After receiving user confirmation, this method will:
     * <ul>
     *   <li>Remove the look and feel from the combobox.</li>
     *   <li>Remove the look and feel from <code>UIManager</code>'s list of installed look and feels.</li>
     *   <li>Remove the look and feel from the list of custom look and feels.</li>
     * </ul>
     */
    private void deleteSelectedLookAndFeel() {
        UIManager.LookAndFeelInfo selection; // Currently selected look and feel.

        selection = lookAndFeels[lookAndFeelComboBox.getSelectedIndex()];

        // Asks the user whether he's sure he wants to delete the selected look and feel.
        if (new QuestionDialog(parent, null, Translator.get("prefs_dialog.delete_look_and_feel", selection.getName()), parent,
                              new String[] {Translator.get("yes"), Translator.get("no")},
                              new int[]  {YES_ACTION, NO_ACTION},
                              0).getActionValue() != YES_ACTION)
            return;

        // Removes the selected look and feel from the combo box.
        lookAndFeelComboBox.removeItem(selection.getName());

        // Removes the selected look and feel from the list of installed look and feels.
        uninstallLookAndFeel(selection);

        // Removes the selected look and feel from the list of custom look and feels.
        deleteCustomLookAndFeel(selection);
    }

    /**
     * Updates the different look&feel related UI widgets depending on whether they are busy or not.
     * @param loading whether look&feels are loading.
     */
    private void setLookAndFeelsLoading(boolean loading) {
        // Starts / stops the loading animation.
        dial.setAnimated(loading);

        // Disables / enables the import button and the combo box.
        importLookAndFeelButton.setEnabled(!loading);
        deleteLookAndFeelButton.setEnabled(!loading);
        lookAndFeelComboBox.setEnabled(!loading);

        // A special case must be made for the delete button
        // as it might not need to be re-enabled.
        if (loading) {
            deleteLookAndFeelButton.setEnabled(false);
        } else {
            resetLookAndFeelButtons();
        }
    }

    /**
     * Tries to import the specified library in the extensions folder.
     * <p>
     * If there is already a file with the same name in the extensions folder,
     * this method will ask the user for confirmation before overwriting it.
     *
     * @param  library     library to import in the extensions folder.
     * @return             <code>true</code> if the library was imported, <code>false</code> if the user cancelled the operation.
     * @throws IOException if an I/O error occurred while importing the library
     */
    private boolean importLookAndFeelLibrary(AbstractFile library) throws IOException {
        // Tries to import the file, but if a version of it is already present in the extensions folder,
        // asks the user for confirmation.

        AbstractFile destFile = ExtensionManager.getExtensionsFile(library.getName());

        int collision = FileCollisionChecker.checkForCollision(library, destFile);
        if (collision != FileCollisionChecker.NO_COLLOSION) {
            // Do not offer the multiple files mode options such as 'skip' and 'apply to all'
            int action = new FileCollisionDialog(parent, parent, collision, library, destFile, false, false).getActionValue();

            // User chose to overwrite the file
            if (action == FileCollisionDialog.OVERWRITE_ACTION) {
                // Simply continue and file will be overwritten
            } else if (action==FileCollisionDialog.OVERWRITE_IF_OLDER_ACTION) {
                // Overwrite if the source is more recent than the destination
                if (library.getLastModifiedDate() <= destFile.getLastModifiedDate())
                    return false;
                // Simply continue and file will be overwritten
            } else {
                return false;       // User chose to cancel or closed the dialog
            }
        }

        return ExtensionManager.importLibrary(library, true);
    }

    public void run() {
        List<Class<?>> newLookAndFeels;

        setLookAndFeelsLoading(true);
        try {
            // Identifies all the look&feels contained by the new library and adds them to the list of custom
            // If no look&feel was found, notifies the user.
            if ((newLookAndFeels = new ClassFinder().find(lookAndFeelLibrary, new LookAndFeelFilter())).isEmpty())
                InformationDialog.showWarningDialog(this, Translator.get("prefs_dialog.no_look_and_feel"));
            else if (importLookAndFeelLibrary(lookAndFeelLibrary)) {
                String currentName;

                if (customLookAndFeels == null)
                    customLookAndFeels = new Vector<>();

                // Adds all new instances to the list of custom look&feels.
                for (Class<?> newLookAndFeel : newLookAndFeels) {
                    currentName = newLookAndFeel.getName();
                    if (!customLookAndFeels.contains(currentName)) {
                        customLookAndFeels.add(currentName);
                        try {
                            WindowManager.installLookAndFeel(currentName);
                        } catch (Throwable e) {
                            e.printStackTrace();
                        }
                    }
                }

                if (customLookAndFeels.isEmpty())
                    customLookAndFeels = null;
                else
                	MuConfigurations.getPreferences().setVariable(MuPreference.CUSTOM_LOOK_AND_FEELS, customLookAndFeels, MuPreferences.CUSTOM_LOOK_AND_FEELS_SEPARATOR);

                populateLookAndFeels();
            }
        } catch(Exception e) {
        	LOGGER.debug("Exception caught", e);
            InformationDialog.showErrorDialog(this);
        }
        setLookAndFeelsLoading(false);
    }

    private void importLookAndFeel() {
        // Initialises the file chooser.
        JFileChooser chooser = createFileChooser();
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.addChoosableFileFilter(new ExtensionFileFilter("jar", Translator.get("prefs_dialog.jar_file")));
        chooser.setDialogTitle(Translator.get("prefs_dialog.import_look_and_feel"));
        chooser.setDialogType(JFileChooser.OPEN_DIALOG);

        if (chooser.showDialog(parent, Translator.get("prefs_dialog.import")) == JFileChooser.APPROVE_OPTION) {
            AbstractFile file = FileFactory.getFile(chooser.getSelectedFile().getAbsolutePath());
            if (file == null) {
                return;
            }
            lastSelectedFolder = file.getParent();

            // Makes sure the file actually exists - JFileChooser apparently doesn't enforce that properly in all look&feels.
            if (!file.exists()) {
                InformationDialog.showErrorDialog(this, Translator.get("this_file_does_not_exist", file.getName()));
                return;
            }

            // Imports the JAR in a separate thread.
            lookAndFeelLibrary = file;
            new Thread(this).start();
        }
    }



    // - Theme actions ----------------------------------------------------------
    // --------------------------------------------------------------------------
    private void setTypeLabel(Theme theme) {
        String label;

        if (theme.getType() == Theme.Type.USER) {
            label = Translator.get("theme.custom");
        } else if(theme.getType() == Theme.Type.PREDEFINED) {
            label = Translator.get("theme.built_in");
        } else {
            label = Translator.get("theme.add_on");
        }

        typeLabel.setText(Translator.get("prefs_dialog.theme_type", label));
    }

    private void resetThemeButtons(Theme theme) {
        if (ignoreComboChanges) {
            return;
        }

        setTypeLabel(theme);

        if (theme.getType() != Theme.Type.CUSTOM) {
            renameThemeButton.setEnabled(false);
            deleteThemeButton.setEnabled(false);
        } else {
            renameThemeButton.setEnabled(true);
            deleteThemeButton.setEnabled(!ThemeManager.isCurrentTheme(theme));
        }
    }

    /**
     * Renames the specified theme.
     * @param theme theme to rename.
     */
    private void renameTheme(Theme theme) {
        ThemeNameDialog dialog = new ThemeNameDialog(parent, theme.getName());

        if (dialog.wasValidated()) {
            // If the rename operation was a success, makes sure the theme is located at its proper position.
            try {
                ThemeManager.renameCustomTheme(theme, dialog.getText());
                themeComboBox.removeItem(theme);
                insertTheme(theme);
            } catch(Exception e) {
                // Otherwise, notifies the user.
                InformationDialog.showErrorDialog(this, Translator.get("prefs_dialog.rename_failed", theme.getName()));
            }
        }
    }

    /**
     * Deletes the specified theme.
     * @param theme theme to delete.
     */
    private void deleteTheme(Theme theme) {
        // Asks the user whether he's sure he wants to delete the selected theme.
        if (new QuestionDialog(parent, null, Translator.get("prefs_dialog.delete_theme", theme.getName()), parent,
                              new String[] {Translator.get("yes"), Translator.get("no")},
                              new int[]  {YES_ACTION, NO_ACTION},
                              0).getActionValue() != YES_ACTION)
            return;

        // Deletes the selected theme and removes it from the list.
        try {
            ThemeManager.deleteCustomTheme(theme.getName());
            themeComboBox.removeItem(theme);
        } catch(Exception e) {
            InformationDialog.showErrorDialog(this);
        }
    }

    /**
     * Starts the theme editor on the specified theme.
     * @param theme to edit.
     */
    private void editTheme(Theme theme) {
        // If the edited theme was modified, we must re-populate the list.
        final Theme modifiedTheme = new ThemeEditorDialog(parent, theme).editTheme();
		if (modifiedTheme != null) {
			populateThemes(modifiedTheme);
			parent.setCommitButtonsEnabled(true);
		}
    }

    /**
     * Creates a file chooser initialised on the last selected folder.
     */
    private JFileChooser createFileChooser() {
        if (lastSelectedFolder == null) {
            return new JFileChooser();
        }
        return new JFileChooser((java.io.File)lastSelectedFolder.getUnderlyingFileObject());
    }

    private void insertTheme(Theme theme) {
        int i;

        int count = themeComboBox.getItemCount();
        for (i = 0; i < count; i++) {
            if((themeComboBox.getItemAt(i)).getName().compareTo(theme.getName()) >= 0) {
                themeComboBox.insertItemAt(theme, i);
                break;
            }
        }
        if (i == count) {
            themeComboBox.addItem(theme);
        }
        themeComboBox.setSelectedItem(theme);
    }

    /**
     * Imports a new theme in muCommander.
     */
    private void importTheme() {
        // Initialises the file chooser.
        JFileChooser chooser = createFileChooser();
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.addChoosableFileFilter(new ExtensionFileFilter("xml", Translator.get("prefs_dialog.xml_file")));
        chooser.setDialogTitle(Translator.get("prefs_dialog.import_theme"));
        chooser.setDialogType(JFileChooser.OPEN_DIALOG);

        if (chooser.showDialog(parent, Translator.get("prefs_dialog.import")) == JFileChooser.APPROVE_OPTION) {
            // Makes sure the file actually exists - JFileChooser apparently doesn't enforce that properly in all look&feels.
            AbstractFile file = FileFactory.getFile(chooser.getSelectedFile().getAbsolutePath());
            if (file == null) {
                return;
            }
            lastSelectedFolder = file.getParent();
            if (!file.exists()) {
                InformationDialog.showErrorDialog(this, Translator.get("this_file_does_not_exist", file.getName()));
                return;
            }

            // Imports the theme and makes sure it appears in the combobox.
            try {
                insertTheme(ThemeManager.importTheme((java.io.File)file.getUnderlyingFileObject()));
            } catch(Exception ex) { // Notifies the user that something went wrong.
                InformationDialog.showErrorDialog(this, Translator.get("prefs_dialog.error_in_import", file.getName()));
            }
        }
    }

    /**
     * Exports the specified theme.
     * @param theme theme to export.
     */
    private void exportTheme(Theme theme) {
        JFileChooser chooser = createFileChooser();
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.addChoosableFileFilter(new ExtensionFileFilter("xml", Translator.get("prefs_dialog.xml_file")));

        chooser.setDialogTitle(Translator.get("prefs_dialog.export_theme", theme.getName()));
        if (chooser.showDialog(parent, Translator.get("prefs_dialog.export")) == JFileChooser.APPROVE_OPTION) {

            AbstractFile file = FileFactory.getFile(chooser.getSelectedFile().getAbsolutePath());
            lastSelectedFolder = file.getParent();

            // Makes sure the file's extension is .xml.
            try {
                if(!"xml".equalsIgnoreCase(file.getExtension()))    // Note: getExtension() may return null if no extension
                    file = lastSelectedFolder.getDirectChild(file.getName()+".xml");

                int collision = FileCollisionChecker.checkForCollision(null, file);
                if (collision != FileCollisionChecker.NO_COLLOSION) {
                    // Do not offer the multiple files mode options such as 'skip' and 'apply to all'
                    int action = new FileCollisionDialog(parent, parent, collision, null, file, false, false).getActionValue();

                    // User chose to overwrite the file
                    if (action == FileCollisionDialog.OVERWRITE_ACTION) {
                        // Simply continue and file will be overwritten
                    } else { // User chose to cancel or closed the dialog
                        return;
                    }
                }

                // Exports the theme.
                ThemeManager.exportTheme(theme, (java.io.File)file.getUnderlyingFileObject());

                // If it was exported to the custom themes folder, reload the theme combobox to reflect the
                // changes.
                if (lastSelectedFolder.equals(ThemeManager.getCustomThemesFolder())) {
                    populateThemes(theme);
            }
            } catch(Exception exception) { // Notifies users of errors.
                InformationDialog.showErrorDialog(this, Translator.get("write_error"), Translator.get("cannot_write_file", file.getName()));
            }
        }
    }

    /**
     * Duplicates the specified theme.
     */
    private void duplicateTheme(Theme theme) {
        try {
            insertTheme(ThemeManager.duplicateTheme(theme));
        } catch(Exception e) {
            InformationDialog.showErrorDialog(this);
        }
    }




    // - Listener code ----------------------------------------------------------
    // --------------------------------------------------------------------------
    /**
     * Called when a button is pressed.
     */
    public void actionPerformed(ActionEvent e) {
        Theme theme = themeComboBox.getSelectedItem();

        // Theme combobox selection changed.
        if(e.getSource() == themeComboBox)
            resetThemeButtons(theme);

        // Look and feel combobox selection changed.
        else if(e.getSource() == lookAndFeelComboBox)
            resetLookAndFeelButtons();

        // Delete look and feel button has been pressed.
        else if(e.getSource() == deleteLookAndFeelButton)
            deleteSelectedLookAndFeel();

        // Import look and feel button has been pressed.
        else if(e.getSource() == importLookAndFeelButton)
            importLookAndFeel();

        // Rename button was pressed.
        else if(e.getSource() == renameThemeButton)
            renameTheme(theme);

        // Delete button was pressed.
        else if(e.getSource() == deleteThemeButton)
            deleteTheme(theme);

        // Edit button was pressed.
        else if(e.getSource() == editThemeButton)
            editTheme(theme);

        // Import button was pressed.
        else if(e.getSource() == importThemeButton)
            importTheme();

        // Export button was pressed.
        else if(e.getSource() == exportThemeButton)
            exportTheme(theme);

        // Export button was pressed.
        else if(e.getSource() == duplicateThemeButton)
            duplicateTheme(theme);
    }


    // - File IMAGE_FILTER ------------------------------------------------------------
    // --------------------------------------------------------------------------
    /**
     * Filter used to only display XML files in the JFileChooser.
     * @author Nicolas Rinaudo
     */
    private static class ExtensionFileFilter extends javax.swing.filechooser.FileFilter {
        /** Extension to match. */
        private String extension;
        /** Filter's description. */
        private String description;

        /**
         * Creates a new extension file IMAGE_FILTER that will match files with the specified extension.
         * @param extension extension to match.
         */
        ExtensionFileFilter(String extension, String description) {
            this.extension   = extension;
            this.description = description;
        }

        /**
         * Returns <code>true</code> if the specified file should be displayed in the chooser.
         */
        @Override
        public boolean accept(File file) {
            // Directories are always displayed.
            if (file.isDirectory()) {
                return true;
            }

            // If the file has an extension, and it matches .xml, return true.
            // Otherwise, return false.
            String ext = AbstractFile.getExtension(file.getName());
            return ext != null && extension.equalsIgnoreCase(ext);
        }

        @Override
        public String getDescription() {return description;}
    }
}
