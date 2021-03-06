/*
 * This file is part of trolCommander, http://www.trolsoft.ru/en/soft/trolcommander
 * Copyright (C) 2014-2020 Oleg Trifonov
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

package com.mucommander.ui.viewer.hex;

import com.jidesoft.hints.ListDataIntelliHints;
import com.mucommander.cache.TextHistory;
import com.mucommander.ui.dialog.DialogToolkit;
import com.mucommander.ui.dialog.FocusDialog;
import com.mucommander.ui.layout.XAlignedComponentPanel;
import ru.trolsoft.ui.InputField;

import javax.swing.JButton;
import javax.swing.JFrame;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

/**
 * This dialog allows the user to enter a string or hex value to be searched for in the hex editor.
 *
 * @author Oleg Trifonov
 */
public abstract class FindDialog extends FocusDialog implements ActionListener {

    /** The text field where a search dump can be entered */
    private final InputField hexField;

    /** The text field where a search string can be entered */
    private final InputField textField;

    /** The 'OK' button */
    private final JButton okButton;


    /**
     * Creates a new FindDialog and shows it to the screen.
     *
     * @param frame the parent frame
     */
    FindDialog(JFrame frame, String encoding) {
        super(frame, i18n("hex_viewer.find"), frame);

        Container contentPane = getContentPane();

        // Text fields panel
        XAlignedComponentPanel compPanel = new XAlignedComponentPanel();

        textField = new InputField(60, InputField.FilterType.ANY_TEXT);
        textField.addActionListener(this);
        compPanel.addRow(i18n("hex_view.text")+":", textField, 5);
        List<String> historyText = TextHistory.getInstance().getList(TextHistory.Type.TEXT_SEARCH);
//        new AutoCompletion(textField, historyText).setStrict(false);
        new ListDataIntelliHints<>(textField, historyText).setCaseSensitive(true);
        textField.setText("");


        hexField = new InputField(60, InputField.FilterType.HEX_DUMP);
        hexField.addActionListener(this);
        compPanel.addRow(i18n("hex_viewer.hex") + ":", hexField, 10);
        List<String> historyHex = TextHistory.getInstance().getList(TextHistory.Type.HEX_DATA_SEARCH);
//        new AutoCompletion(hexField, historyHex).setStrict(false);
        new ListDataIntelliHints<>(hexField, historyHex).setCaseSensitive(false);
        hexField.setText("");

        textField.assignField(hexField);
        hexField.assignField(textField);
        textField.setTextEncoding(encoding);
        hexField.setTextEncoding(encoding);

        contentPane.add(compPanel, BorderLayout.CENTER);


        okButton = new JButton(i18n("ok"));
        JButton cancelButton = new JButton(i18n("cancel"));
        contentPane.add(DialogToolkit.createOKCancelPanel(okButton, cancelButton, getRootPane(), this), BorderLayout.SOUTH);

        // The text field will receive initial focus
        setInitialFocusComponent(textField);
    }


    public byte[] getSearchBytes() {
        return hexField.getBytes();
    }


    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        dispose();
        doSearch(source == okButton || source == hexField || source == textField ? getSearchBytes() : null);
    }

    @Override
    protected void saveState() {
        super.saveState();
        TextHistory.getInstance().add(TextHistory.Type.TEXT_SEARCH, textField.getText(), true);
        TextHistory.getInstance().add(TextHistory.Type.HEX_DATA_SEARCH, hexField.getText(), true);
    }

    void setSearchBytes(byte[] searchBytes) {
        hexField.setBytes(searchBytes);
    }


        /**
         * Search operation listener
         * @param bytes nul if the dialog was cancelled
         */
    protected abstract void doSearch(byte[] bytes);

}
