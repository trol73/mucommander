/*
 * This file is part of trolCommander, http://www.trolsoft.ru/en/soft/trolcommander
 * Copyright (C) 2013-2026 Oleg Trifonov
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
package com.mucommander.ui.dialog.commands

import com.jidesoft.hints.FileIntelliHints
import com.mucommander.command.Command
import com.mucommander.command.CommandManager
import com.mucommander.command.CommandType
import com.mucommander.commons.collections.AlteredVector
import com.mucommander.ui.helper.MnemonicHelper
import com.mucommander.ui.layout.XAlignedComponentPanel
import com.mucommander.ui.layout.XBoxPanel
import com.mucommander.ui.layout.YBoxPanel
import com.mucommander.ui.list.DynamicList
import com.mucommander.ui.list.SortableListPanel
import com.mucommander.ui.text.FilePathField
import com.mucommander.utils.text.Translator
import java.awt.BorderLayout
import java.awt.FlowLayout
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import javax.swing.JButton
import javax.swing.JPanel
import javax.swing.JTextField
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import javax.swing.event.ListSelectionEvent
import javax.swing.event.ListSelectionListener

/**
 * @author Oleg Trifonov
 * Created on 10/10/14.
 */
class CommandsPanel(private val parent: EditCommandsDialog, private val commandType: String?) : JPanel(),
    ActionListener, DocumentListener, ListSelectionListener {
    private val commands: AlteredVector<CommandWrapper>
    private val commandsList: DynamicList<CommandWrapper>
    private val btnNew: JButton
    private val btnDuplicate: JButton
    private val btnRemove: JButton

    private val edtAlias: JTextField
    private val edtCommand: JTextField
    private val edtDisplay: JTextField
    private val edtFilemask: JTextField

    private var ignoreDocumentListenerEvents = false


    private inner class CommandWrapper(val command: Command) {
        override fun toString(): String {
            var result: String = if (commandType == null) {
                command.alias + ":\t"
            } else {
                ""
            }
            command.displayName?.let {
                result += it + "\t"
            }
            command.fileMask?.let {
                result += "[" + command.fileMask + "]\t"
            }
            result += command.command
            return result
        }
    }

    init {
        setLayout(BorderLayout())

        val mnemonicHelper = MnemonicHelper()
        val list = mutableListOf<CommandWrapper>().apply {
            for (cmd in CommandManager.getCommands(commandType)) {
                add(CommandWrapper(cmd))
            }
        }

        commands = AlteredVector<CommandWrapper>(list)
        // create the sortable commands list panel
        val listPanel = SortableListPanel(commands)
        commandsList = listPanel.dynamicList.apply {
            addListSelectionListener(this@CommandsPanel)
        }

        add(listPanel, BorderLayout.CENTER)

        ignoreDocumentListenerEvents = true

        // Add alias field
        edtAlias = JTextField().apply {
            document.addDocumentListener(this@CommandsPanel)
        }

        // Text fields panel
        val compPanel = XAlignedComponentPanel().apply {
            addRow(Translator.get("EditCommands.alias") + ":", edtAlias, 5)
            if (commandType != null) {
                edtAlias.text = commandType
                edtAlias.setEnabled(false)
            }

            edtCommand = FilePathField().apply {
                document.addDocumentListener(this@CommandsPanel)
            }
            addRow(Translator.get("EditCommands.command") + ":", edtCommand, 10)

            edtDisplay = FilePathField().apply {
                document.addDocumentListener(this@CommandsPanel)
            }
            addRow(Translator.get("EditCommands.display_name") + ":", edtDisplay, 10)

            edtFilemask = FilePathField().apply {
                document.addDocumentListener(this@CommandsPanel)
            }
            addRow(Translator.get("EditCommands.filemask") + ":", edtFilemask, 10)
        }

        FileIntelliHints(edtCommand)


        val yPanel = YBoxPanel(10).apply {
            add(compPanel)
        }

        val buttonGroupPanel = JPanel(FlowLayout(FlowLayout.LEFT)).apply {
            btnNew = JButton(Translator.get("EditCommands.new")).also {
                it.setMnemonic(mnemonicHelper.getMnemonic(it))
                it.addActionListener(this@CommandsPanel)
                add(it)
            }
            btnDuplicate = JButton(Translator.get("duplicate")).also {
                it.setMnemonic(mnemonicHelper.getMnemonic(it))
                it.addActionListener(this@CommandsPanel)
                add(it)
            }
            btnRemove = JButton(commandsList.removeAction).also {
                it.setMnemonic(mnemonicHelper.getMnemonic(it))
                it.addActionListener(this@CommandsPanel)
                add(it)
            }
        }

        // Add buttons: 'remove', 'move up' and 'move down' buttons are enabled only if there is at least one command in the table
        val buttonsPanel = XBoxPanel().apply {
            add(buttonGroupPanel)
        }

        yPanel.add(buttonsPanel)

        add(yPanel, BorderLayout.SOUTH)
        ignoreDocumentListenerEvents = false
        updateComponents()
    }

    /**
     * Updates text fields and buttons' enabled state based on the current selection. Should be called
     * whenever the list selection has changed.
     */
    private fun updateComponents() {
        var alias: String? = null
        var value: String? = null
        var display: String? = null
        var filemask: String? = null

        var componentsEnabled = false

        if (!commandsList.isSelectionEmpty && !commands.isEmpty()) {
            componentsEnabled = true

            val cmd = commandsList.getSelectedValue()!!.command
            alias = cmd.alias
            value = cmd.command
            display = cmd.displayName
            filemask = cmd.fileMask
        }
        ignoreDocumentListenerEvents = true

        if (commandType == null) {
            edtAlias.text = alias
        }
        edtCommand.text = value
        edtDisplay.text = display
        edtFilemask.text = filemask
        ignoreDocumentListenerEvents = false

        btnDuplicate.setEnabled(componentsEnabled)
        btnRemove.setEnabled(componentsEnabled)
    }

    override fun actionPerformed(e: ActionEvent) {
        val source = e.getSource()

        // create a new empty command / duplicate the currently selected command
        if (source === btnNew || source === btnDuplicate) {
            parent.enableSave()
            val newCommand: Command?
            if (source === btnNew) {
                val alias = commandType ?: ""
                newCommand = Command(alias, $$"$f")
            } else { // source == btnDuplicate
                val currentCommand = commandsList.getSelectedValue()!!.command
                newCommand = Command(currentCommand)
            }
            commands.add(CommandWrapper(newCommand))

            val newCommandIndex = commands.size - 1
            commandsList.selectAndScroll(newCommandIndex)

            updateComponents()

            edtAlias.selectAll()
            edtAlias.requestFocus()
        } else if (source === btnRemove) {
            parent.enableSave()
        }
    }


    /**
     * Called whenever a value in one of the text fields has been modified, and updates the current Command instance to
     * use the new value.
     */
    private fun modifyCommand() {
        if (ignoreDocumentListenerEvents || commands.isEmpty()) {
            return
        }

        val selectedIndex = commandsList.selectedIndex

        // Make sure that the selected index is not out of bounds
        if (!commandsList.isIndexValid(selectedIndex)) return

        val selectedCommand = Command(
            edtAlias.getText(),
            edtCommand.getText(),
            CommandType.NORMAL_COMMAND,
            edtDisplay.getText(),
            edtFilemask.getText()
        )
        commands.setElementAt(CommandWrapper(selectedCommand), selectedIndex)

        parent.enableSave()
    }


    override fun insertUpdate(e: DocumentEvent?) = modifyCommand()

    override fun removeUpdate(e: DocumentEvent?) = modifyCommand()

    override fun changedUpdate(e: DocumentEvent?) = modifyCommand()

    override fun valueChanged(e: ListSelectionEvent) {
        if (!e.valueIsAdjusting) {
            updateComponents()
        }
    }

    fun getCommands(): List<Command> = mutableListOf<Command>().apply {
        for (cw in commands) {
            add(cw.command)
        }
    }

}