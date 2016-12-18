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

import com.jidesoft.hints.FileIntelliHints;
import com.mucommander.command.Command;
import com.mucommander.command.CommandManager;
import com.mucommander.command.CommandType;
import com.mucommander.commons.collections.AlteredVector;
import com.mucommander.text.Translator;
import com.mucommander.ui.helper.MnemonicHelper;
import com.mucommander.ui.layout.XAlignedComponentPanel;
import com.mucommander.ui.layout.XBoxPanel;
import com.mucommander.ui.layout.YBoxPanel;
import com.mucommander.ui.list.DynamicList;
import com.mucommander.ui.list.SortableListPanel;
import com.mucommander.ui.text.FilePathField;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Oleg Trifonov
 * Created on 10/10/14.
 */
public class CommandsPanel extends JPanel implements ActionListener, DocumentListener, ListSelectionListener {

    private final String commandType;
    private final EditCommandsDialog parent;

    private AlteredVector<CommandWrapper> commands;
    private DynamicList<CommandWrapper> commandsList;
    private JButton btnNew;
    private JButton btnDuplicate;
    private JButton btnRemove;

    private JTextField edtAlias, edtCommand, edtDisplay, edtFilemask;

    private boolean ignoreDocumentListenerEvents;


    private class CommandWrapper {
        private final Command command;

        public CommandWrapper(Command cmd) {
            this.command = cmd;
        }

        @Override
        public String toString() {
            String result;
            if (commandType == null) {
                result = command.getAlias() + ":\t";
            } else {
                result = "";
            }
            if (command.getDisplayName() != null) {
                result += command.getDisplayName() + "\t";
            }
            if (command.getFileMask() != null) {
                result += "[" + command.getFileMask() + "]\t";
            }
            result += command.getCommand();
            return result;
        }

        public Command getCommand() {
            return command;
        }
    }


    public CommandsPanel(EditCommandsDialog parent, String type) {
        super();
        this.parent = parent;
        this.commandType = type;
        initUI();
    }


    // - UI initialisation ------------------------------------------------------
    // --------------------------------------------------------------------------
    private void initUI() {
        setLayout(new BorderLayout());

        // Add buttons: 'remove', 'move up' and 'move down' buttons are enabled
        // only if there is at least one command in the table
        XBoxPanel buttonsPanel = new XBoxPanel();
        JPanel buttonGroupPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        MnemonicHelper mnemonicHelper = new MnemonicHelper();
        List<CommandWrapper> list = new ArrayList<>();
        for (Command cmd : CommandManager.getCommands(commandType)) {
            list.add(new CommandWrapper(cmd));
        }
        commands = new AlteredVector<>(list);
        // create the sortable commands list panel
        SortableListPanel<CommandWrapper> listPanel = new SortableListPanel<>(commands);
        commandsList = listPanel.getDynamicList();
        commandsList.addListSelectionListener(this);

        add(listPanel, BorderLayout.NORTH);

        // Text fields panel
        XAlignedComponentPanel compPanel = new XAlignedComponentPanel();

        ignoreDocumentListenerEvents = true;

        // Add alias field
        edtAlias = new JTextField();
        edtAlias.getDocument().addDocumentListener(this);
        compPanel.addRow(Translator.get("EditCommands.alias")+":", edtAlias, 5);
        if (commandType != null) {
            edtAlias.setText(commandType);
            edtAlias.setEnabled(false);
        }

        // add command field
        this.edtCommand = new FilePathField();
        edtCommand.getDocument().addDocumentListener(this);
        compPanel.addRow(Translator.get("EditCommands.command")+":", edtCommand, 10);

        new FileIntelliHints(edtCommand);

        this.edtDisplay = new FilePathField();
        edtDisplay.getDocument().addDocumentListener(this);
        compPanel.addRow(Translator.get("EditCommands.display_name")+":", edtDisplay, 10);

        this.edtFilemask = new FilePathField();
        edtFilemask.getDocument().addDocumentListener(this);
        compPanel.addRow(Translator.get("EditCommands.filemask")+":", edtFilemask, 10);


        YBoxPanel yPanel = new YBoxPanel(10);
        yPanel.add(compPanel);

        // New button
        btnNew = new JButton(Translator.get("EditCommands.new"));
        btnNew.setMnemonic(mnemonicHelper.getMnemonic(btnNew));
        btnNew.addActionListener(this);
        buttonGroupPanel.add(btnNew);

        // Duplicate bookmark button
        btnDuplicate = new JButton(Translator.get("duplicate"));
        btnDuplicate.setMnemonic(mnemonicHelper.getMnemonic(btnDuplicate));
        btnDuplicate.addActionListener(this);
        buttonGroupPanel.add(btnDuplicate);

        // Remove bookmark button
        btnRemove = new JButton(commandsList.getRemoveAction());
        btnRemove.setMnemonic(mnemonicHelper.getMnemonic(btnRemove));
        btnRemove.addActionListener(this);
        buttonGroupPanel.add(btnRemove);

        buttonsPanel.add(buttonGroupPanel);

        yPanel.add(buttonsPanel);

        add(yPanel, BorderLayout.SOUTH);
        ignoreDocumentListenerEvents = false    ;
        updateComponents();
    }

    /**
     * Updates text fields and buttons' enabled state based on the current selection. Should be called
     * whenever the list selection has changed.
     */
    private void updateComponents() {
        String alias = null;
        String value = null;
        String display = null;
        String filemask = null;

        boolean componentsEnabled = false;

        if (!commandsList.isSelectionEmpty() && !commands.isEmpty()) {
            componentsEnabled = true;

            Command cmd = commandsList.getSelectedValue().getCommand();
            alias = cmd.getAlias();
            value = cmd.getCommand();
            display = cmd.getDisplayName();
            filemask = cmd.getFileMask();
        }
        ignoreDocumentListenerEvents = true;

        if (commandType == null) {
            edtAlias.setText(alias);
        }
        edtCommand.setText(value);
        edtDisplay.setText(display);
        edtFilemask.setText(filemask);
        ignoreDocumentListenerEvents = false;

        btnDuplicate.setEnabled(componentsEnabled);
        btnRemove.setEnabled(componentsEnabled);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();

        // create a new empty command / duplicate the currently selected command
        if (source == btnNew || source == btnDuplicate) {
            parent.enableSave();
            Command newCommand;
            if (source == btnNew) {
                String alias = commandType == null ? "" : commandType;
                newCommand = new Command(alias, "$f");
            } else { // source == btnDuplicate
                Command currentCommand = commandsList.getSelectedValue().getCommand();
                newCommand = new Command(currentCommand);
            }
            commands.add(new CommandWrapper(newCommand));

            int newCommandIndex = commands.size()-1;
            commandsList.selectAndScroll(newCommandIndex);

            updateComponents();

            edtAlias.selectAll();
            edtAlias.requestFocus();
        } else if (source == btnRemove) {
            parent.enableSave();
        }
    }


    /**
     * Called whenever a value in one of the text fields has been modified, and updates the current Command instance to
     * use the new value.
     */
    private void modifyCommand() {
        if (ignoreDocumentListenerEvents || commands.isEmpty()) {
            return;
        }

        int selectedIndex = commandsList.getSelectedIndex();

        // Make sure that the selected index is not out of bounds
        if (!commandsList.isIndexValid(selectedIndex))
            return;

        Command selectedCommand = new Command(edtAlias.getText(), edtCommand.getText(), CommandType.NORMAL_COMMAND, edtDisplay.getText(), edtFilemask.getText());
        commands.setElementAt(new CommandWrapper(selectedCommand), selectedIndex);

        parent.enableSave();
    }


    @Override
    public void insertUpdate(DocumentEvent e) {
        modifyCommand();
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        modifyCommand();
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        modifyCommand();
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        if (!e.getValueIsAdjusting()) {
            updateComponents();
        }
    }


    List<Command> getCommands() {
        List<Command> result = new ArrayList<>();
        for (CommandWrapper cw : commands) {
            result.add(cw.getCommand());
        }
        return result;
    }
}