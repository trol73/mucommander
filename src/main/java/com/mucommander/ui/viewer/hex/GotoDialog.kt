/*
 * This file is part of trolCommander, http://www.trolsoft.ru/en/soft/trolcommander
 * Copyright (C) 2014-2016 Oleg Trifonov
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
package com.mucommander.ui.viewer.hex

import com.mucommander.ui.dialog.DialogToolkit
import com.mucommander.ui.dialog.FocusDialog
import ru.trolsoft.ui.InputField
import java.awt.BorderLayout
import java.awt.Frame
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.util.function.LongConsumer
import javax.swing.JButton
import javax.swing.JLabel

/**
 * Goto address dialog.
 * @author Oleg Trifonov
 */
class GotoDialog internal constructor(owner: Frame?, private val maxOffset: Long, private val action: LongConsumer) :
    FocusDialog(owner, i18n("hex_viewer.goto"), owner), ActionListener {
    private val edtOffset: InputField
    private val btnOk: JButton

    init {
        val contentPane = getContentPane()
        contentPane.add(JLabel(i18n("hex_viewer.goto.offset") + ":"), BorderLayout.NORTH)

        edtOffset = object : InputField(16, FilterType.HEX_LONG) {
            override fun onChange() {
                val enabled = !edtOffset.isEmpty && edtOffset.getValue() <= this@GotoDialog.maxOffset
                btnOk.setEnabled(enabled)
            }
        }.also {
            it.text = "1"
            it.addActionListener(this)
            contentPane.add(it, BorderLayout.CENTER)
        }
        btnOk = JButton(i18n("ok"))
        val cancelButton = JButton(i18n("cancel"))
        contentPane.add(DialogToolkit.createOKCancelPanel(btnOk, cancelButton, getRootPane(), this), BorderLayout.SOUTH)

        // The text field will receive initial focus
        setInitialFocusComponent(edtOffset)
    }

    override fun actionPerformed(e: ActionEvent) {
        val source = e.getSource()
        if ((source === btnOk || source === edtOffset) && btnOk.isEnabled) {
            action.accept(edtOffset.getValue())
            dispose()
        }
    }
}
