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

import com.mucommander.text.Translator;
import com.mucommander.ui.chooser.FontChooser;
import com.mucommander.ui.dialog.pref.PreferencesDialog;
import com.mucommander.ui.layout.ProportionalGridPanel;
import com.mucommander.ui.layout.YBoxPanel;
import com.mucommander.ui.theme.ThemeData;

import javax.swing.*;
import java.awt.*;

/**
 * @author Nicolas Rinaudo, Maxence Bernard
 */
class FolderPanePanel extends ThemeEditorPanel {

    private FileGroupsPanel fileGroupsPanel;

    // - Initialisation ------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Creates a new file table editor.
     * @param parent dialog containing the panel.
     * @param themeData themeData being edited.
     */
    FolderPanePanel(PreferencesDialog parent, ThemeData themeData) {
        super(parent, Translator.get("theme_editor.folder_tab"), themeData);
        initUI();
    }



    // - UI initialisation ---------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Initialises the panel's UI.
     */
    private void initUI() {
        FontChooser fontChooser = createFontChooser(ThemeData.FILE_TABLE_FONT);

        JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);

        // Adds the general panel.
        tabbedPane.add(Translator.get("theme_editor.general"), createScrollPane(createGeneralPanel(fontChooser)));

        // Adds the active panel.
		FilePanel activeFilePanel = new FilePanel(parent, true, themeData, fontChooser);
		tabbedPane.add(activeFilePanel.getTitle(), createScrollPane(activeFilePanel));

        // Adds the inactive panel.
		FilePanel inactiveFilePanel = new FilePanel(parent, false, themeData, fontChooser);
		tabbedPane.add(inactiveFilePanel.getTitle(), createScrollPane(inactiveFilePanel));

		// Give the file panels the colors of the respective other one to let them copy from each other.
		activeFilePanel.setCopyColorButtonsSource(inactiveFilePanel);
		inactiveFilePanel.setCopyColorButtonsSource(activeFilePanel);

        // Adds the file groups panel.
        fileGroupsPanel = new FileGroupsPanel(parent, themeData);
        tabbedPane.add(fileGroupsPanel.getTitle(), createScrollPane(fileGroupsPanel));

        // Creates the layout.
        setLayout(new BorderLayout());
        add(tabbedPane, BorderLayout.NORTH);
    }

    /**
     * Creates the 'general' theme.
     */
    private JPanel createGeneralPanel(FontChooser chooser) {
        ProportionalGridPanel panel = new ProportionalGridPanel(4);

        // Initialises the quicksearch panel.
        addLabelRow(panel);
        panel.add(addColorButtons(panel, chooser, "theme_editor.quick_search.unmatched_file", ThemeData.FILE_TABLE_UNMATCHED_FOREGROUND_COLOR,
                                  ThemeData.FILE_TABLE_UNMATCHED_BACKGROUND_COLOR));
        JPanel quickSearchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        quickSearchPanel.add(panel);
        quickSearchPanel.setBorder(BorderFactory.createTitledBorder(Translator.get("theme_editor.quick_search")));

        // Initialises the panel.
        YBoxPanel mainPanel = new YBoxPanel();
        mainPanel.add(chooser);
        mainPanel.addSpace(10);
        mainPanel.add(quickSearchPanel);

        // Wraps everything in a border layout.
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(mainPanel, BorderLayout.NORTH);
        return wrapper;
    }



    // - Modification management ---------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Ignored.
     */
    @Override
    public void commit() {
        fileGroupsPanel.commit();
    }
}

