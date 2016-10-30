/*
 * This file is part of trolCommander, http://www.trolsoft.ru/en/soft/trolcommander
 * Copyright (C) 2013-2016 Oleg Trifonov
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
package com.mucommander.ui.dialog;

import com.mucommander.text.Translator;
import com.mucommander.ui.button.ButtonChoicePanel;
import com.mucommander.ui.layout.YBoxPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author Oleg Trifonov
 * Created on 28/10/16.
 */
public class PasswordDialog extends FocusDialog implements ActionListener {

    private JPasswordField edtPassword;
    private JButton btnOk;
    private JButton btnCancel;
    private volatile Boolean canceled;

    /** Minimum dialog size */
    private final static Dimension MINIMUM_DIALOG_DIMENSION = new Dimension(160, 100);

    /** Maximum dialog size */
    private final static Dimension MAXIMUM_DIALOG_DIMENSION = new Dimension(1024, 500);


    public PasswordDialog(Frame owner, String title, Component locationRelativeComp) {
        super(owner, title, locationRelativeComp);
        init();
    }

    public PasswordDialog(String title) {
        this(null,title, null);
    }


    private void init() {
        // Sets minimum and maximum dimensions for this dialog
        setMinimumSize(MINIMUM_DIALOG_DIMENSION);
        setMaximumSize(MAXIMUM_DIALOG_DIMENSION);

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        YBoxPanel mainPanel = new YBoxPanel();
        edtPassword = new JPasswordField(10);
        mainPanel.add(new JLabel(Translator.get("password")));
        mainPanel.addSpace(5);
        mainPanel.add(edtPassword);
        mainPanel.addSpace(10);

        btnOk = new JButton(Translator.get("ok"));
        btnOk.addActionListener(this);
        btnCancel = new JButton(Translator.get("cancel"));
        btnCancel.addActionListener(this);
        JButton buttons[] = new JButton[]{btnOk, btnCancel};


        setInitialFocusComponent(edtPassword);
        mainPanel.add(new ButtonChoicePanel(buttons, 2, getRootPane()));
        getContentPane().add(mainPanel, BorderLayout.NORTH);

        requestFocus();
        edtPassword.requestFocus();
        pack();
        fixHeight();
    }

    public String getPassword() {
        SwingUtilities.invokeLater(this::showDialog);
        while (canceled == null) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        SwingUtilities.invokeLater(this::dispose);

        return canceled ? null : new String(edtPassword.getPassword());
    }

    @Override
    public void cancel() {
        canceled = true;
        super.cancel();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnOk) {
            canceled = false;
            dispose();
        } else if (e.getSource() == btnCancel) {
            canceled = true;
            dispose();
        }
    }
}
