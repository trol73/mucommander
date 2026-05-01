/*
 * This file is part of trolCommander, http://www.trolsoft.ru/en/soft/trolcommander
 * Copyright (C) 2014-2020 Oleg Trifonov
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

import com.jidesoft.hints.ListDataIntelliHints
import com.mucommander.cache.TextHistory
import com.mucommander.ui.dialog.DialogToolkit
import com.mucommander.ui.dialog.FocusDialog
import com.mucommander.ui.layout.XAlignedComponentPanel
import ru.trolsoft.ui.InputField
import java.awt.BorderLayout
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import javax.swing.JButton
import javax.swing.JFrame

/**
 * This dialog allows the user to enter a string or hex value to be searched for in the hex editor.
 * 
 * @author Oleg Trifonov
 */
abstract class FindDialog internal constructor(frame: JFrame?, encoding: String?) :
    FocusDialog(frame, i18n("hex_viewer.find"), frame), ActionListener {
    /** The text field where a search dump can be entered  */
    private val hexField: InputField

    /** The text field where a search string can be entered  */
    private val textField: InputField

    /** The 'OK' button  */
    private val okButton: JButton

    init {
        val contentPane = getContentPane()

        // Text fields panel
        val compPanel = XAlignedComponentPanel()

        textField = InputField(60, InputField.FilterType.ANY_TEXT).also {
            it.addActionListener(this)
            compPanel.addRow(i18n("hex_view.text") + ":", it, 5)
            val historyText = TextHistory.getInstance().getList(TextHistory.Type.TEXT_SEARCH)
            //        new AutoCompletion(textField, historyText).setStrict(false);
            ListDataIntelliHints(it, historyText).isCaseSensitive = true
            it.text = ""
        }

        hexField = InputField(60, InputField.FilterType.HEX_DUMP).also {
            it.addActionListener(this)
            compPanel.addRow(i18n("hex_viewer.hex") + ":", it, 10)
            val historyHex: MutableList<String?>? = TextHistory.getInstance().getList(TextHistory.Type.HEX_DATA_SEARCH)
            //        new AutoCompletion(hexField, historyHex).setStrict(false);
            ListDataIntelliHints<String?>(it, historyHex).isCaseSensitive = false
            it.text = ""
        }

        textField.bindField(hexField)
        hexField.bindField(textField)
        setEncoding(encoding)

        contentPane.add(compPanel, BorderLayout.CENTER)


        okButton = JButton(i18n("ok"))
        val cancelButton = JButton(i18n("cancel"))
        contentPane.add(
            DialogToolkit.createOKCancelPanel(okButton, cancelButton, getRootPane(), this),
            BorderLayout.SOUTH
        )

        // The text field will receive initial focus
        setInitialFocusComponent(textField)
    }

    private fun setEncoding(encoding: String?) {
        textField.textEncoding = encoding
        hexField.textEncoding = encoding
    }

    var searchBytes: ByteArray?
        get() = hexField.bytes
        set(searchBytes) {
            hexField.setBytes(searchBytes)
        }


    override fun actionPerformed(e: ActionEvent) {
        val source = e.getSource()
        dispose()
        doSearch(if (source === okButton || source === hexField || source === textField) this.searchBytes else null)
    }

    override fun saveState() {
        super.saveState()
        TextHistory.getInstance().add(TextHistory.Type.TEXT_SEARCH, textField.getText(), true)
        TextHistory.getInstance().add(TextHistory.Type.HEX_DATA_SEARCH, hexField.getText(), true)
    }


    /**
     * Search operation listener
     * @param bytes nul if the dialog was cancelled
     */
    protected abstract fun doSearch(bytes: ByteArray?)
}
