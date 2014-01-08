/*
 * This file is part of muCommander, http://www.mucommander.com
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
package com.mucommander.ui.viewer.text;

import com.mucommander.text.Translator;
import com.mucommander.ui.dialog.DialogToolkit;
import com.mucommander.ui.dialog.FocusDialog;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * This dialog allows the user to enter a line number to be jumped for in the text editor.
 *
 * @author Oleg Trifonov
 */
public class GotoLineDialog extends FocusDialog implements ActionListener {

    /** The text field where a search string can be entered */
    private JTextField lineNumberField;

    /** The 'OK' button */
    private JButton okButton;

    /** true if the dialog was validated by the user */
    private boolean wasValidated;

    /**
     * Creates a new FindDialog and shows it to the screen.
     *
     * @param editorFrame the parent editor frame
     */
    public GotoLineDialog(JFrame editorFrame) {
        super(editorFrame, Translator.get("text_viewer.goto_line"), editorFrame);

        Container contentPane = getContentPane();
        contentPane.add(new JLabel(Translator.get("text_viewer.line")+":"), BorderLayout.NORTH);

        lineNumberField = new JTextField(16);
        lineNumberField.setText("1");
        lineNumberField.addActionListener(this);
        AbstractDocument doc = (AbstractDocument)lineNumberField.getDocument();
        doc.addDocumentListener(new Listener());
        doc.setDocumentFilter(new NumberDocumentFilter());

        contentPane.add(lineNumberField, BorderLayout.CENTER);

        okButton = new JButton(Translator.get("ok"));
        JButton cancelButton = new JButton(Translator.get("cancel"));
        contentPane.add(DialogToolkit.createOKCancelPanel(okButton, cancelButton, getRootPane(), this), BorderLayout.SOUTH);

        // The text field will receive initial focus
        setInitialFocusComponent(lineNumberField);

        showDialog();
    }

    /**
     * Returns <code>true</code> if the dialog was validated by the user, i.e. the user pressed the 'OK' button
     * or the 'Enter' key in the text field.
     *
     * @return <code>true</code> if the dialog was validated by the user
     */
    public boolean wasValidated() {
        return wasValidated;
    }

    /**
     * Returns the line number entered by the user in the text field.
     *
     * @return the line number entered by the user in the text field
     */
    public int getLine() {
        return Integer.parseInt(lineNumberField.getText());
    }


    ///////////////////////////////////
    // ActionListener implementation //
    ///////////////////////////////////

    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();

        wasValidated = source== okButton || source== lineNumberField;

        dispose();
    }

    /**
     * A document filter that only lets the user enter digits.
     */
    private class NumberDocumentFilter extends DocumentFilter {

        private String fix(String str) {
            if (str!=null) {
                int origLength = str.length();
                for (int i=0; i<str.length(); i++) {
                    if (!Character.isDigit(str.charAt(i))) {
                        str = str.substring(0, i) + str.substring(i+1);
                        i--;
                    }
                }
                if (origLength!=str.length()) {
                    UIManager.getLookAndFeel().provideErrorFeedback(GotoLineDialog.this);
                }
            }
            return str;
        }

        @Override
        public void insertString(FilterBypass fb, int offset, String string,
                                 AttributeSet attr) throws BadLocationException {
            fb.insertString(offset, fix(string), attr);
        }

        @Override
        public void replace(DocumentFilter.FilterBypass fb, int offset,
                            int length, String text, AttributeSet attr)
                throws BadLocationException {
            fb.replace(offset, length, fix(text), attr);
        }

    }


    private class Listener implements DocumentListener {

        @Override
        public void insertUpdate(DocumentEvent e) {
            okButton.setEnabled(lineNumberField.getDocument().getLength()>0);
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            okButton.setEnabled(lineNumberField.getDocument().getLength()>0);
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
        }
    }
}
