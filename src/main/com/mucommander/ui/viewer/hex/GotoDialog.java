/*
 * This file is part of trolCommander, http://www.trolsoft.ru/trolcommander
 * Copyright (C) 2014 Oleg Trifonov
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

import com.mucommander.text.Translator;
import com.mucommander.ui.dialog.DialogToolkit;
import com.mucommander.ui.dialog.FocusDialog;
import ru.trolsoft.ui.InputField;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.text.AbstractDocument;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Goto address dialog.
 * @author Oleg Trifonov
 */
public abstract class GotoDialog extends FocusDialog implements ActionListener {

    private final long maxOffset;

    private InputField edtOffset;

    /** The 'OK' button */
    private JButton btnOk;


    public GotoDialog(Frame owner, long maxOffset) {
        super(owner, Translator.get("hex_viewer.goto"), owner);
        this.maxOffset = maxOffset;
        Container contentPane = getContentPane();
        contentPane.add(new JLabel(Translator.get("hex_viewer.goto.offset")+":"), BorderLayout.NORTH);

        edtOffset = new InputField(16, InputField.FilterType.HEX_LONG) {
            @Override
            public void onChange() {
                boolean enabled = !edtOffset.isEmpty() && edtOffset.getValue() <= GotoDialog.this.maxOffset;
                btnOk.setEnabled(enabled);
            }
        };
        edtOffset.setText("1");
        edtOffset.addActionListener(this);
        contentPane.add(edtOffset, BorderLayout.CENTER);

        btnOk = new JButton(Translator.get("ok"));
        JButton cancelButton = new JButton(Translator.get("cancel"));
        contentPane.add(DialogToolkit.createOKCancelPanel(btnOk, cancelButton, getRootPane(), this), BorderLayout.SOUTH);

        // The text field will receive initial focus
        setInitialFocusComponent(edtOffset);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        if ((source == btnOk || source == edtOffset) && btnOk.isEnabled()) {
            doGoto(edtOffset.getValue());
            dispose();
        }
    }

    abstract protected void doGoto(long value);
}
