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
package ru.trolsoft.calculator

import com.mucommander.ui.combobox.TcComboBox
import com.mucommander.ui.dialog.FocusDialog
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent

/**
 * @author Oleg Trifonov
 * Created on 06/06/14.
 */
class HistoryComboBox(
    private val parent: FocusDialog,
    values: List<String>
) : TcComboBox<String>(values.toTypedArray<String>()) {
    init {
        setEditable(true)
        if (!values.isEmpty()) {
            setSelectedItem(values.first())
            getEditor().selectAll()
        }

        val keyAdapter: KeyAdapter = object : KeyAdapter() {
            override fun keyPressed(e: KeyEvent) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    if (isPopupVisible) {
                        hidePopup()
                        e.consume()
                    } else {
                        this@HistoryComboBox.parent.cancel()
                    }
                }
            }
        }
        getEditor().editorComponent.addKeyListener(keyAdapter)
    }


    fun addToHistory(s: String) {
        for (i in 0..< itemCount) {
            val item = getItemAt(i)
            if (item.equals(s, ignoreCase = true)) {
                removeItem(item)
                break
            }
        }
        insertItemAt(s, 0)
        setSelectedIndex(0)
    }
}
