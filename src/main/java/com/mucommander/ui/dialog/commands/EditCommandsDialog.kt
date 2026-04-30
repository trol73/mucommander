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

import com.mucommander.command.CommandException
import com.mucommander.command.CommandManager
import com.mucommander.ui.dialog.FocusDialog
import com.mucommander.ui.layout.XBoxPanel
import org.slf4j.LoggerFactory
import java.awt.BorderLayout
import java.awt.Component
import java.awt.FlowLayout
import java.awt.Frame
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.io.IOException
import javax.swing.JButton
import javax.swing.JPanel
import javax.swing.JTabbedPane

/**
 * @author Oleg Trifonov
 * Created on 10/10/14.
 */
class EditCommandsDialog(
    owner: Frame,
    locationRelativeComp: Component
) :
    FocusDialog(owner, i18n("EditCommands.label"), locationRelativeComp), ActionListener {

    private val log = LoggerFactory.getLogger(EditCommandsDialog::class.java)

    private val btnApply: JButton
    private val btnOk: JButton
    private val btnCancel: JButton
    private val panels = mutableListOf<CommandsPanel>()




    init {
        // Initializes the tabbed pane.
        val tabbedPane = JTabbedPane(JTabbedPane.TOP)

        CommandsPanel(this, CommandManager.VIEWER_ALIAS).also {
            tabbedPane.addTab(i18n("EditCommands.group.view"), it)
            panels.add(it)
        }
        CommandsPanel(this, CommandManager.EDITOR_ALIAS).also {
            tabbedPane.addTab(i18n("EditCommands.group.edit"), it)
            panels.add(it)

        }
        CommandsPanel(this, null).also {
            tabbedPane.addTab(i18n("EditCommands.group.others"), it)
            panels.add(it)
        }

        // Adds the tabbed pane
        val contentPane = contentPane.apply {
            setLayout(BorderLayout())
            add(tabbedPane, BorderLayout.CENTER)
        }

        // Buttons panel.
        val buttonsPanel = XBoxPanel().apply {
            btnApply = JButton(i18n("apply")).apply {
                setEnabled(false)
                addActionListener(this@EditCommandsDialog)
            }
            add(btnApply)
            addSpace(20)
            btnOk = JButton(i18n("ok")).apply {
                setEnabled(false)
                addActionListener(this@EditCommandsDialog)
            }
            add(btnOk)
            btnCancel = JButton(i18n("cancel")).apply {
                addActionListener(this@EditCommandsDialog)
            }
            add(btnCancel)
        }

        // Aligns the button panel to the right.
        val tempPanel = JPanel(FlowLayout(FlowLayout.RIGHT)).apply {
            add(buttonsPanel)
        }
        contentPane.add(tempPanel, BorderLayout.SOUTH)

        getRootPane().setDefaultButton(btnOk)
    }


    override fun actionPerformed(e: ActionEvent) {
        val source = e.getSource()

        if (source === btnOk || source === btnApply) {
            commit()
        }

        if (source === btnOk || source === btnCancel) {
            dispose()
        }
    }


    fun enableSave() {
        btnApply.setEnabled(true)
        btnOk.setEnabled(true)
    }


    private fun commit() {
        // clear all Normal commands
        CommandManager.removeAllNormalCommands()
        // register all commands from editor
        panels.forEach {
            for (cmd in it.getCommands()) {
                CommandManager.registerCommand(cmd)
            }
        }
        // write file
        try {
            CommandManager.writeCommands()
        } catch (e: IOException) {
            log.error("Commands write error", e)
        } catch (e: CommandException) {
            log.error("Commands error", e)
        }
    }
}
