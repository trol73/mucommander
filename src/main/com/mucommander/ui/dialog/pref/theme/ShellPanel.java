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

import com.mucommander.RuntimeConstants;
import com.mucommander.text.Translator;
import com.mucommander.ui.chooser.FontChooser;
import com.mucommander.ui.chooser.PreviewLabel;
import com.mucommander.ui.combobox.EditableComboBox;
import com.mucommander.ui.dialog.pref.PreferencesDialog;
import com.mucommander.ui.layout.ProportionalGridPanel;
import com.mucommander.ui.layout.YBoxPanel;
import com.mucommander.ui.theme.ThemeData;

import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * @author Nicolas Rinaudo, Maxence Bernard
 */
class ShellPanel extends ThemeEditorPanel implements PropertyChangeListener {
    private JTextArea        shellPreview;
    private JTabbedPane tabbedPane;
    private EditableComboBox<String> historyPreview;
    private JLabel lblRun, lblOutput;


    // - Initialisation ------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Creates a new file table editor.
     * @param parent   dialog containing the panel.
     * @param themeData themeData being edited.
     */
    public ShellPanel(PreferencesDialog parent, ThemeData themeData) {
        super(parent, Translator.get("theme_editor.shell_tab"), themeData);
        initUI();
    }

    private JComponent createConfigurationPanel(int fontId, int foregroundId, int backgroundId, int selectedForegroundId, int selectedBackgroundId, JComponent fontListener) {
        YBoxPanel mainPanel = new YBoxPanel();

        FontChooser fontChooser = createFontChooser(fontId);
        mainPanel.add(fontChooser);
        mainPanel.addSpace(10);
        addFontChooserListener(fontChooser, fontListener);

        ProportionalGridPanel colorPanel = new ProportionalGridPanel(3);
        addLabelRow(colorPanel, false);

        addColorButtons(colorPanel, fontChooser, "theme_editor.normal", foregroundId, backgroundId).addPropertyChangeListener(this);
        addColorButtons(colorPanel, fontChooser, "theme_editor.selected", selectedForegroundId, selectedBackgroundId).addPropertyChangeListener(this);

        JPanel flowPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        flowPanel.add(colorPanel);
        flowPanel.setBorder(BorderFactory.createTitledBorder(Translator.get("theme_editor.colors")));

        mainPanel.add(flowPanel);

        return createScrollPane(mainPanel);
    }

    private JPanel createPreviewPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(Translator.get("preview")));

        YBoxPanel headerPanel = new YBoxPanel();
        lblRun = new JLabel(Translator.get("run_dialog.run_command_description") + ":");
        headerPanel.add(lblRun);
        headerPanel.add(historyPreview = new EditableComboBox<>(new JTextField("mucommander -v")));
        historyPreview.addItem("trolcommander -v");
        historyPreview.addItem("java -version");

        headerPanel.addSpace(10);
        lblOutput = new JLabel(Translator.get("run_dialog.command_output")+":");
        headerPanel.add(lblOutput);

        panel.add(headerPanel, BorderLayout.NORTH);

        shellPreview = new JTextArea(20, 15);
        JScrollPane scroll = new JScrollPane(shellPreview, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        panel.add(scroll, BorderLayout.CENTER);
        scroll.getViewport().setPreferredSize(shellPreview.getPreferredSize());
        shellPreview.append(RuntimeConstants.APP_STRING);
        shellPreview.append("\nCopyright (C) ");
        shellPreview.append(RuntimeConstants.COPYRIGHT);
        shellPreview.append(" Maxence Bernard\nThis is free software, distributed under the terms of the GNU General Public License.");
        //        shellPreview.setLineWrap(true);
        shellPreview.setCaretPosition(0);

        setForegroundColors();
        setBackgroundColors();

        return panel;
    }


    /**
     * Initialises the panel's UI.
     */
    private void initUI() {
        setLayout(new BorderLayout());

        tabbedPane   = new JTabbedPane();
        JPanel previewPanel = createPreviewPanel();

        tabbedPane.add(Translator.get("theme_editor.shell_tab"),
                       createConfigurationPanel(ThemeData.SHELL_FONT, ThemeData.SHELL_FOREGROUND_COLOR, ThemeData.SHELL_BACKGROUND_COLOR,
                                                ThemeData.SHELL_SELECTED_FOREGROUND_COLOR, ThemeData.SHELL_SELECTED_BACKGROUND_COLOR, shellPreview));
        tabbedPane.add(Translator.get("theme_editor.shell_history_tab"),
                       createConfigurationPanel(ThemeData.SHELL_HISTORY_FONT, ThemeData.SHELL_HISTORY_FOREGROUND_COLOR, ThemeData.SHELL_HISTORY_BACKGROUND_COLOR,
                                                ThemeData.SHELL_HISTORY_SELECTED_FOREGROUND_COLOR, ThemeData.SHELL_HISTORY_SELECTED_BACKGROUND_COLOR, historyPreview));

        tabbedPane.add(Translator.get("theme_editor.terminal_tab"),
                createConfigurationPanel(ThemeData.TERMINAL_FONT, ThemeData.TERMINAL_FOREGROUND_COLOR, ThemeData.TERMINAL_BACKGROUND_COLOR,
                        ThemeData.TERMINAL_SELECTED_FOREGROUND_COLOR, ThemeData.TERMINAL_SELECTED_BACKGROUND_COLOR, shellPreview));

        tabbedPane.addChangeListener(e -> {
            setBackgroundColors();
            setForegroundColors();
            boolean shellMode = tabbedPane.getSelectedIndex() < 2;
            historyPreview.setVisible(shellMode);
            lblRun.setVisible(shellMode);
            lblOutput.setVisible(shellMode);
        });

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        mainPanel.add(previewPanel, BorderLayout.EAST);

        add(mainPanel, BorderLayout.NORTH);

//        ThemeData.addDefaultValuesListener(new ThemeListener() {
//
//            @Override
//            public void colorChanged(ColorChangedEvent event) {
//System.out.println(event);
//                setForegroundColors();
//                setBackgroundColors();
//            }
//
//            @Override
//            public void fontChanged(FontChangedEvent event) {
//            }
//        });
    }


    public void propertyChange(final PropertyChangeEvent event) {
//System.out.println(event);
        // Background color changed.
        if (event.getPropertyName().equals(PreviewLabel.BACKGROUND_COLOR_PROPERTY_NAME))
            setBackgroundColors();

        // Foreground color changed.
        else if(event.getPropertyName().equals(PreviewLabel.FOREGROUND_COLOR_PROPERTY_NAME))
            setForegroundColors();
    }

    private void setBackgroundColors() {
        boolean shellMode = tabbedPane.getSelectedIndex() < 2;
        if (shellMode) {
        shellPreview.setBackground(themeData.getColor(ThemeData.SHELL_BACKGROUND_COLOR));
        shellPreview.setSelectionColor(themeData.getColor(ThemeData.SHELL_SELECTED_BACKGROUND_COLOR));
        historyPreview.setBackground(themeData.getColor(ThemeData.SHELL_HISTORY_BACKGROUND_COLOR));
        historyPreview.setSelectionBackground(themeData.getColor(ThemeData.SHELL_HISTORY_SELECTED_BACKGROUND_COLOR));
        } else {
            shellPreview.setBackground(themeData.getColor(ThemeData.TERMINAL_BACKGROUND_COLOR));
            shellPreview.setSelectionColor(themeData.getColor(ThemeData.TERMINAL_SELECTED_BACKGROUND_COLOR));
    }

    }

    private void setForegroundColors() {
        boolean shellMode = tabbedPane.getSelectedIndex() < 2;
        if (shellMode) {
        shellPreview.setForeground(themeData.getColor(ThemeData.SHELL_FOREGROUND_COLOR));
        shellPreview.setSelectedTextColor(themeData.getColor(ThemeData.SHELL_SELECTED_FOREGROUND_COLOR));
        shellPreview.setCaretColor(themeData.getColor(ThemeData.SHELL_FOREGROUND_COLOR));
        historyPreview.setForeground(themeData.getColor(ThemeData.SHELL_HISTORY_FOREGROUND_COLOR));
        historyPreview.setSelectionForeground(themeData.getColor(ThemeData.SHELL_HISTORY_SELECTED_FOREGROUND_COLOR));
        } else {
            shellPreview.setForeground(themeData.getColor(ThemeData.TERMINAL_FOREGROUND_COLOR));
            shellPreview.setSelectedTextColor(themeData.getColor(ThemeData.TERMINAL_SELECTED_FOREGROUND_COLOR));
            shellPreview.setCaretColor(themeData.getColor(ThemeData.TERMINAL_FOREGROUND_COLOR));
        }
    }



    // - Modification management ---------------------------------------------------------
    // -----------------------------------------------------------------------------------
    @Override
    public void commit() {}
}
