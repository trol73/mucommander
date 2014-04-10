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
import ru.trolsoft.ui.InputField;

import javax.swing.*;
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
    private InputField edtLineNumber;

    /** The 'OK' button */
    private JButton btnOk;

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

        edtLineNumber = new InputField(16, InputField.FilterType.DEC_LONG) {
            @Override
            public void onChange() {
                btnOk.setEnabled(!edtLineNumber.isEmpty());
            }
        };
        edtLineNumber.setText("1");
        edtLineNumber.addActionListener(this);

        contentPane.add(edtLineNumber, BorderLayout.CENTER);

        btnOk = new JButton(Translator.get("ok"));
        JButton cancelButton = new JButton(Translator.get("cancel"));
        contentPane.add(DialogToolkit.createOKCancelPanel(btnOk, cancelButton, getRootPane(), this), BorderLayout.SOUTH);

        // The text field will receive initial focus
        setInitialFocusComponent(edtLineNumber);

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
        return Integer.parseInt(edtLineNumber.getText());
    }


    ///////////////////////////////////
    // ActionListener implementation //
    ///////////////////////////////////

    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();

        wasValidated = source== btnOk || source== edtLineNumber;

        dispose();
    }
}
