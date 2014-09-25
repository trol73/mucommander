/*
 * This file is part of trolCommander, http://www.mucommander.com
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
public abstract class GotoLineDialog extends FocusDialog implements ActionListener {

    /** The text field where a search string can be entered */
    private InputField edtLineNumber;

    /** The 'OK' button */
    private JButton btnOk;

    private final int maxLines;


    /**
     * Creates a new FindDialog and shows it to the screen.
     *
     * @param editorFrame the parent editor frame
     */
    public GotoLineDialog(JFrame editorFrame, final int maxLines) {
        super(editorFrame, Translator.get("text_viewer.goto_line"), editorFrame);
        this.maxLines = maxLines;

        Container contentPane = getContentPane();
        contentPane.add(new JLabel(Translator.get("text_viewer.line")+":"), BorderLayout.NORTH);

        edtLineNumber = new InputField(16, InputField.FilterType.DEC_LONG) {
            @Override
            public void onChange() {
                boolean enabled = !edtLineNumber.isEmpty() && edtLineNumber.getValue() <= maxLines;
                btnOk.setEnabled(enabled);
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
    }


    ///////////////////////////////////
    // ActionListener implementation //
    ///////////////////////////////////

    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();

        if ( (source == btnOk || source == edtLineNumber) && btnOk.isEnabled() ) {
            doGoto((int)edtLineNumber.getValue());
            dispose();
        }
    }

    abstract protected void doGoto(int value);
}
