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

import com.mucommander.ui.main.statusbar.FileWindowsListButton
import org.fife.ui.StatusBar
import org.fife.ui.StatusBarPanel
import ru.trolsoft.utils.StrUtils
import java.awt.BorderLayout
import java.awt.Font
import java.awt.GridBagConstraints
import javax.swing.JLabel

/**
 * 
 */
class StatusBar : StatusBar("") {
    private val lbFiles: FileWindowsListButton = FileWindowsListButton(true)

    private val lblOffset: JLabel
    private val lblEncoding: JLabel
    private val lblValue: JLabel

    @JvmField
    var maxOffset: Long = -1


    init {
        val panelWindows = StatusBarPanel(BorderLayout())
        panelWindows.add(lbFiles)

        lblOffset = createLabel()
        val panelOffset = StatusBarPanel(BorderLayout(), lblOffset)

        lblEncoding = createLabel()
        val panelEncoding = StatusBarPanel(BorderLayout(), lblEncoding)

        lblValue = createLabel()
        val panelValue = StatusBarPanel(BorderLayout(), lblValue)

        // Make the layout such that different items can be different sizes.
        val c = GridBagConstraints().apply {
            fill = GridBagConstraints.BOTH
            weightx = 0.0
        }
        addStatusBarComponent(panelWindows, c)
        addStatusBarComponent(panelOffset, c)
        addStatusBarComponent(panelValue, c)
        addStatusBarComponent(panelEncoding, c)
    }

    private fun createLabel(): JLabel {
        val lbl = JLabel()
        val fnt = lbl.getFont()
        lbl.setFont(Font(Font.MONOSPACED, fnt.getStyle(), fnt.getSize()))
        return lbl
    }

    fun setOffset(offset: Long) {
        var str = StrUtils.dwordToHexStr(offset)
        if (maxOffset >= 0) {
            str += " / " + StrUtils.dwordToHexStr(maxOffset)
        }
        lblOffset.setText(str)
    }

    fun setEncoding(encoding: String?) {
        lblEncoding.setText(encoding)
    }

    fun setByteValue(v: Byte) {
        val s = StrUtils.byteToBinaryStr(v) + " - " + StrUtils.byteToOctalStr(v)
        lblValue.setText(s)
    }
}
