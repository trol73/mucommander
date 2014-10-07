/*
 * This file is part of trolCommander, http://www.trolsoft.ru/soft/trolcommander
 * Copyright (C) 2014 Oleg Trifonov
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

import com.mucommander.conf.MuConfigurations;
import com.mucommander.conf.MuPreference;
import com.mucommander.conf.MuPreferencesAPI;
import com.mucommander.text.Translator;
import com.mucommander.ui.chooser.PreviewLabel;
import com.mucommander.ui.dialog.pref.PreferencesDialog;
import com.mucommander.ui.layout.ProportionalGridPanel;
import com.mucommander.ui.theme.ThemeData;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.BorderLayout;

/**
 * @author Oleg Trifonov
 */
public class FileGroupsPanel extends ThemeEditorPanel {

    private static final int NUMBER_OF_GROUPS = 10;

    private ColorButton[] normalColors = new ColorButton[NUMBER_OF_GROUPS];
    private ColorButton[] selectedColors = new ColorButton[NUMBER_OF_GROUPS];
    private JTextField[] fileMasks = new JTextField[NUMBER_OF_GROUPS];

    private final DocumentListener documentListener;

    // - Initialisation ------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Creates a new <code>FilePanel</code>.
     * @param parent   dialog containing the panel
     * @param data     theme to edit.
     */
    public FileGroupsPanel(final PreferencesDialog parent, ThemeData data) {
        super(parent, Translator.get("theme_editor.file_groups"), data);

        documentListener = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {parent.setCommitButtonsEnabled(true);}

            @Override
            public void removeUpdate(DocumentEvent e) {parent.setCommitButtonsEnabled(true);}

            @Override
            public void changedUpdate(DocumentEvent e) {}
        };

        FilePreviewPanel preview = new FilePreviewPanel(themeData, true);
        JPanel gridPanel = new ProportionalGridPanel(4);

        // Header
        gridPanel.add(new JLabel());
        gridPanel.add(createCaptionLabel("theme_editor.normal_color"));
        gridPanel.add(createCaptionLabel("theme_editor.selected_color"));
        gridPanel.add(createCaptionLabel("theme_editor.filemask"));
        MuPreferencesAPI prefs = MuConfigurations.getPreferences();
        for (int i = 0; i < NUMBER_OF_GROUPS; i++) {
            MuPreference preference = MuPreference.values()[MuPreference.FILE_GROUP_1_MASK.ordinal() + i];
            String mask = prefs.getVariable(preference);
            gridPanel.add(createCaptionLabelWithTitle(Translator.get("theme_editor.group_") + " " + (i+1)));
            normalColors[i]  = new ColorButton(parent, themeData, ThemeData.FILE_GROUP_1_FOREGROUND_COLOR + i, PreviewLabel.FOREGROUND_COLOR_PROPERTY_NAME, preview);
            gridPanel.add(normalColors[i]);
            selectedColors[i] = new ColorButton(parent, themeData, ThemeData.FILE_GROUP_1_SELECTED_FOREGROUND_COLOR + i, PreviewLabel.FOREGROUND_COLOR_PROPERTY_NAME, preview);
            gridPanel.add(selectedColors[i]);
            fileMasks[i] = new JTextField(24);
            fileMasks[i].setText(mask);
            gridPanel.add(fileMasks[i]);
            fileMasks[i].getDocument().addDocumentListener(documentListener);
        }


        setLayout(new BorderLayout());
        add(gridPanel, BorderLayout.WEST);
        add(preview, BorderLayout.EAST);
    }


    @Override
    protected void commit() {
        MuPreferencesAPI prefs = MuConfigurations.getPreferences();
        for (int i = 0; i < NUMBER_OF_GROUPS; i++) {
            MuPreference preference = MuPreference.values()[MuPreference.FILE_GROUP_1_MASK.ordinal() + i];
            prefs.setVariable(preference, fileMasks[i].getText().trim());
        }
    }
}
