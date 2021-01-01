/*
 * This file is part of trolCommander, http://www.trolsoft.ru/en/soft/trolcommander
 * Copyright (C) 2013-2016 Oleg Trifonov
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
package com.mucommander.ui.dialog.commands;

import com.mucommander.command.Command;
import com.mucommander.command.CommandException;
import com.mucommander.command.CommandManager;
import com.mucommander.ui.dialog.FocusDialog;
import com.mucommander.ui.layout.XBoxPanel;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Oleg Trifonov
 * Created on 10/10/14.
 */
public class EditCommandsDialog extends FocusDialog implements ActionListener {

    /** Displays the different panels. */
    private JTabbedPane tabbedPane;
    /** Apply button. */
    private JButton btnApply;
    /** OK button. */
    private JButton btnOk;
    /** Cancel button. */
    private JButton btnCancel;

    private List<CommandsPanel> panels = new ArrayList<>();


    public EditCommandsDialog(Frame owner, Component locationRelativeComp) {
        super(owner, i18n("EditCommands.label"), locationRelativeComp);
        initUI();
    }


    /**
     * Initializes the tabbed panel's UI.
     */
    private void initUI() {
        // Initializes the tabbed pane.
        //prefPanels = new ArrayList<>();
        tabbedPane = new JTabbedPane(JTabbedPane.TOP);

        CommandsPanel panel = new CommandsPanel(this, CommandManager.VIEWER_ALIAS);
        tabbedPane.addTab(i18n("EditCommands.group.view"), panel);
        panels.add(panel);
        panel = new CommandsPanel(this, CommandManager.EDITOR_ALIAS);
        tabbedPane.addTab(i18n("EditCommands.group.edit"), panel);
        panels.add(panel);
        panel = new CommandsPanel(this, null);
        tabbedPane.addTab(i18n("EditCommands.group.others"), panel);
        panels.add(panel);


        // Adds the tabbed pane.
        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());
        contentPane.add(tabbedPane, BorderLayout.CENTER);

        // Buttons panel.
        XBoxPanel buttonsPanel = new XBoxPanel();
        buttonsPanel.add(btnApply = new JButton(i18n("apply")));
        buttonsPanel.addSpace(20);
        buttonsPanel.add(btnOk = new JButton(i18n("ok")));
        buttonsPanel.add(btnCancel = new JButton(i18n("cancel")));

        // Disable "commit buttons".
        btnOk.setEnabled(false);
        btnApply.setEnabled(false);

        // Buttons listening.
        btnApply.addActionListener(this);
        btnOk.addActionListener(this);
        btnCancel.addActionListener(this);

        // Aligns the button panel to the right.
        JPanel tempPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        tempPanel.add(buttonsPanel);
        contentPane.add(tempPanel, BorderLayout.SOUTH);

        // Selects OK when enter is pressed
        getRootPane().setDefaultButton(btnOk);
    }


    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();

        // Commit changes
        if (source == btnOk || source == btnApply) {
            commit();
        }

        // Dispose dialog
        if (source == btnOk || source == btnCancel) {
            dispose();
        }
    }


    void enableSave() {
        btnApply.setEnabled(true);
        btnOk.setEnabled(true);
    }


    private void commit() {
        // clear all Normal commands
        CommandManager.removeAllNormalCommands();
        // register all commands from editor
        for (CommandsPanel panel : panels) {
            List<Command> commands = panel.getCommands();
            for (Command cmd : commands) {
                CommandManager.registerCommand(cmd);
            }
        }
        // write file
        try {
            CommandManager.writeCommands();
        } catch (IOException | CommandException e) {
            e.printStackTrace();
        }
    }
}
