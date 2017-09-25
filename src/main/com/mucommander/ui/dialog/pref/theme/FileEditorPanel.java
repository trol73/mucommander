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
import com.mucommander.ui.dialog.pref.PreferencesDialog;
import com.mucommander.ui.layout.ProportionalGridPanel;
import com.mucommander.ui.layout.YBoxPanel;
import com.mucommander.ui.theme.ThemeData;
import com.mucommander.ui.theme.ThemeId;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import ru.trolsoft.hexeditor.data.MemoryByteBuffer;
import ru.trolsoft.hexeditor.ui.HexTable;
import ru.trolsoft.hexeditor.ui.ViewerHexTableModel;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Random;

/**
 * @author Nicolas Rinaudo, Maxence Bernard
 */
class FileEditorPanel extends ThemeEditorPanel implements ThemeId {
    // - Instance fields -----------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /** Used to textPreview the editor's theme. */
    private RSyntaxTextArea textPreview;

    private JScrollPane scrollHex;
    private HexTable hexPreview;

    private final PropertyChangeListener textPropertyChangeListener = event -> {
        final String name = event.getPropertyName();
        if (name.equals(PreviewLabel.BACKGROUND_COLOR_PROPERTY_NAME)) {
            setTextBackgroundColors();
        } else if (name.equals(PreviewLabel.FOREGROUND_COLOR_PROPERTY_NAME)) {
            setTextForegroundColors();
        }
    };

    private final PropertyChangeListener hexPropertyChangeListener = event -> {
        final String name = event.getPropertyName();
        if (name.equals(PreviewLabel.BACKGROUND_COLOR_PROPERTY_NAME) || name.equals(PreviewLabel.FOREGROUND_COLOR_PROPERTY_NAME)) {
            setHexColors();
        }
    };

    // - Initialisation ------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Creates a new file table editor.
     * @param parent    dialog containing the panel.
     * @param themeData  themeData being edited.
     */
    FileEditorPanel(PreferencesDialog parent, ThemeData themeData) {
        super(parent, Translator.get("theme_editor.editor_tab"), themeData);
        initUI();
    }



    // - UI initialisation ---------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Creates the JPanel that contains all of the color configuration elements.
     * @param fontChooser font chooser used by the editor panel.
     * @return the JPanel that contains all of the color configuration elements.
     */
    private JPanel createTextColorsPanel(FontChooser fontChooser) {
        // Initialisation
        ProportionalGridPanel gridPanel = new ProportionalGridPanel(3);

        // Header
        addLabelRow(gridPanel, false);

        PreviewLabel label = new PreviewLabel();

        // Color buttons
        addColorButtons(gridPanel, fontChooser, "theme_editor.normal",
                        EDITOR_FOREGROUND_COLOR, EDITOR_BACKGROUND_COLOR, label).addPropertyChangeListener(textPropertyChangeListener);
        addColorButtons(gridPanel, fontChooser, "theme_editor.selected",
                        EDITOR_SELECTED_FOREGROUND_COLOR, EDITOR_SELECTED_BACKGROUND_COLOR, label).addPropertyChangeListener(textPropertyChangeListener);

        addColorButtons(gridPanel, fontChooser, "theme_editor.current",
                -1, EDITOR_CURRENT_BACKGROUND_COLOR, label).addPropertyChangeListener(textPropertyChangeListener);

        label.addPropertyChangeListener(textPropertyChangeListener);
        //butt.addUpdatedPreviewComponent(label);

        // Wraps everything in a flow layout.
        JPanel colorsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        colorsPanel.add(gridPanel);
        colorsPanel.setBorder(BorderFactory.createTitledBorder(Translator.get("theme_editor.colors")));

        return colorsPanel;
    }

    /**
     * Creates the JPanel that contains all of the color configuration elements.
     * @param fontChooser font chooser used by the editor panel.
     * @return the JPanel that contains all of the color configuration elements.
     */
    private JPanel createHexColorsPanel(FontChooser fontChooser) {
        // Initialisation
        ProportionalGridPanel gridPanel = new ProportionalGridPanel(3);

        // Header
        addLabelRow(gridPanel, false);

        PreviewLabel label = new PreviewLabel();

        // Color buttons
        addColorButtons(gridPanel, fontChooser, "theme_editor.normal_hex",
                HEX_VIEWER_HEX_FOREGROUND_COLOR, HEX_VIEWER_BACKGROUND_COLOR, label).addPropertyChangeListener(hexPropertyChangeListener);
        addColorButtons(gridPanel, fontChooser, "theme_editor.normal_ascii",
                HEX_VIEWER_ASCII_FOREGROUND_COLOR, -1, label).addPropertyChangeListener(hexPropertyChangeListener);
        addColorButtons(gridPanel, fontChooser, "theme_editor.normal_offset",
                HEX_VIEWER_OFFSET_FOREGROUND_COLOR, -1, label).addPropertyChangeListener(hexPropertyChangeListener);
        addColorButtons(gridPanel, fontChooser, "theme_editor.alternate",
                -1, HEX_VIEWER_ALTERNATE_BACKGROUND_COLOR, label).addPropertyChangeListener(hexPropertyChangeListener);
        addColorButtons(gridPanel, fontChooser, "theme_editor.selected_hex",
                -1, HEX_VIEWER_SELECTED_BACKGROUND_COLOR, label).addPropertyChangeListener(hexPropertyChangeListener);
        addColorButtons(gridPanel, fontChooser, "theme_editor.selected_ascii",
                -1, HEX_VIEWER_SELECTED_ASCII_BACKGROUND_COLOR, label).addPropertyChangeListener(hexPropertyChangeListener);


        label.addPropertyChangeListener(hexPropertyChangeListener);
        //butt.addUpdatedPreviewComponent(label);

        // Wraps everything in a flow layout.
        JPanel colorsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        colorsPanel.add(gridPanel);
        colorsPanel.setBorder(BorderFactory.createTitledBorder(Translator.get("theme_editor.colors")));

        return colorsPanel;
    }

    /**
     * Initialises the panel's UI.
     */
    private void initUI() {
        JTabbedPane tabbedPane = new JTabbedPane();
//        JPanel textEditorPanel = createTextEditorPanel();

        tabbedPane.add(Translator.get("theme_editor.text_editor_tab"), createTextViewerPanel());
        tabbedPane.add(Translator.get("theme_editor.hex_viewer_tab"), createHexViewerPanel());

        setLayout(new BorderLayout());
        add(tabbedPane, BorderLayout.NORTH);
//        // Layout.
//        setLayout(new BorderLayout());
//        add(mainPanel, BorderLayout.NORTH);
    }

    private JPanel createTextViewerPanel() {
        // Font chooser and textPreview initialisation.
        JPanel mainPanel = new JPanel(new BorderLayout());
        FontChooser fontChooser = createFontChooser(EDITOR_FONT);
        mainPanel.add(createTextPreviewPanel(), BorderLayout.CENTER);
        addFontChooserListener(fontChooser, textPreview);

        // Configuration panel initialisation.
        YBoxPanel configurationPanel = new YBoxPanel();
        configurationPanel.add(fontChooser);
        configurationPanel.addSpace(10);
        configurationPanel.add(createTextColorsPanel(fontChooser));
        mainPanel.add(configurationPanel, BorderLayout.WEST);

        return mainPanel;
    }

    private JPanel createHexViewerPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        FontChooser fontChooser = createFontChooser(HEX_VIEWER_FONT);
        panel.add(createHexPreviewPanel(), BorderLayout.CENTER);
        addFontChooserListener(fontChooser, hexPreview);

        // Configuration panel initialisation.
        YBoxPanel configurationPanel = new YBoxPanel();
        configurationPanel.add(fontChooser);
        configurationPanel.addSpace(10);
        configurationPanel.add(createHexColorsPanel(fontChooser));
        panel.add(configurationPanel, BorderLayout.WEST);

        return panel;
    }

    /**
     * Creates the file editor textPreview panel.
     * @return the file editor textPreview panel.
     */
    private JPanel createTextPreviewPanel() {
        // Initialises the textPreview text area.
        textPreview = new RSyntaxTextArea(15, 15);

        // Initialises colors.
        setTextBackgroundColors();
        setTextForegroundColors();
        // Creates the panel.
        JPanel panel = new JPanel(new BorderLayout());
        JScrollPane scroll = new JScrollPane(textPreview, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        panel.add(scroll, BorderLayout.CENTER);
        scroll.getViewport().setPreferredSize(textPreview.getPreferredSize());
        panel.setBorder(BorderFactory.createTitledBorder(Translator.get("preview")));

        loadText();
        textPreview.setCaretPosition(0);

        return panel;
    }


    private JPanel createHexPreviewPanel() {
        MemoryByteBuffer dataBuffer = new MemoryByteBuffer(250);
        ViewerHexTableModel model = new ViewerHexTableModel(dataBuffer, 8);
        try {
            model.load();
        } catch (IOException ignore) {}
        Random rnd = new Random();
        for (int i = 0; i < dataBuffer.getCapacity(); i++) {
            dataBuffer.setByte(i, rnd.nextInt());
        }
        hexPreview = new HexTable(model);

        JPanel panel = new JPanel(new BorderLayout());
        scrollHex = new JScrollPane(hexPreview, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        panel.add(scrollHex, BorderLayout.CENTER);
        scrollHex.getViewport().setPreferredSize(new Dimension(300, 200));
        panel.setBorder(BorderFactory.createTitledBorder(Translator.get("preview")));

        setHexColors();

        return panel;
    }


    private void setTextBackgroundColors() {
        Color background = themeData.getColor(EDITOR_BACKGROUND_COLOR);
        textPreview.setBackground(background);
        for (int i = 1; i <= textPreview.getSecondaryLanguageCount(); i++) {
            textPreview.setSecondaryLanguageBackground(i, background);
        }
        textPreview.setSelectionColor(themeData.getColor(EDITOR_SELECTED_BACKGROUND_COLOR));
        textPreview.setCurrentLineHighlightColor(themeData.getColor(EDITOR_CURRENT_BACKGROUND_COLOR));
    }

    private void setTextForegroundColors() {
        textPreview.setForeground(themeData.getColor(EDITOR_FOREGROUND_COLOR));
        textPreview.setCaretColor(themeData.getColor(EDITOR_FOREGROUND_COLOR));
        textPreview.setSelectedTextColor(themeData.getColor(EDITOR_SELECTED_FOREGROUND_COLOR));
    }

    private void setHexColors() {
        hexPreview.setBackground(themeData.getColor(HEX_VIEWER_BACKGROUND_COLOR));
        hexPreview.setForeground(themeData.getColor(HEX_VIEWER_HEX_FOREGROUND_COLOR));
        hexPreview.setAlternateBackground(themeData.getColor(HEX_VIEWER_ALTERNATE_BACKGROUND_COLOR));
        hexPreview.setOffsetColumnColor(themeData.getColor(HEX_VIEWER_OFFSET_FOREGROUND_COLOR));
        hexPreview.setAsciiColumnColor(themeData.getColor(HEX_VIEWER_ASCII_FOREGROUND_COLOR));
        hexPreview.setAsciiSelectionBackgroundColor(themeData.getColor(HEX_VIEWER_SELECTED_ASCII_BACKGROUND_COLOR));
        hexPreview.setSelectionBackground(themeData.getColor(HEX_VIEWER_SELECTED_BACKGROUND_COLOR));
        hexPreview.setFont(themeData.getFont(HEX_VIEWER_FONT));
        hexPreview.setAlternateRowBackground(true);

        hexPreview.getTableHeader().setFont(new Font("Monospaced", Font.PLAIN, 12));

        scrollHex.getViewport().setBackground(hexPreview.getBackground());
    }



    // - Misc. ---------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    private void loadText() {
        try (InputStreamReader in = new InputStreamReader(FileEditorPanel.class.getResourceAsStream(RuntimeConstants.LICENSE))) {
            char[] buffer = new char[2048];
            int count;  // Number of characters read from the last read operation.
            while ((count = in.read(buffer)) >= 0) {
                textPreview.append(new String(buffer, 0, count));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    // - Modification management ---------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Ignored.
     */
    @Override
    public void commit() {}
}
