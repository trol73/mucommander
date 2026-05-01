/*
 * This file is part of trolCommander, http://www.trolsoft.ru/en/soft/trolcommander
 * Copyright (C) 2013-2026 Oleg Trifonov
 *
 * trolCommander is free software; you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 *
 * trolCommander is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.mucommander.ui.terminal

import com.jediterm.terminal.CursorShape
import com.jediterm.terminal.model.StyleState
import com.jediterm.terminal.model.TerminalTextBuffer
import com.jediterm.terminal.ui.TerminalPanel
import com.jediterm.terminal.ui.settings.SettingsProvider
import com.mucommander.commons.runtime.OsFamily
import com.mucommander.ui.action.ActionKeymap
import com.mucommander.ui.action.impl.TerminalPanelAction
import com.mucommander.ui.main.MainFrame
import ru.trolsoft.calculator.CalculatorDialog
import java.awt.event.InputEvent
import java.awt.event.KeyEvent
import javax.swing.KeyStroke

/**
 * @author Oleg Trifonov
 * Created on 27/10/14.
 */
class JediTerminalPanelEx internal constructor(
    settingsProvider: SettingsProvider,
    terminalTextBuffer: TerminalTextBuffer,
    styleState: StyleState,
    private val mainFrame: MainFrame
) : TerminalPanel(settingsProvider, terminalTextBuffer, styleState) {
    private val keyModifier: Int = if (OsFamily.MAC_OS_X.isCurrent) KeyEvent.META_DOWN_MASK else KeyEvent.CTRL_DOWN_MASK
    private var lineHeight = 0

    init {
        setDefaultCursorShape(CursorShape.BLINK_VERTICAL_BAR)
    }


    override fun processKeyEvent(e: KeyEvent) {
        val id = e.getID()

        if (id == KeyEvent.KEY_PRESSED) {
            val actionId = ActionKeymap.getRegisteredActionIdForKeystroke(
                KeyStroke.getKeyStroke(
                    e.getKeyCode(),
                    e.modifiersEx,
                    false
                )
            )
            if (TerminalPanelAction.Descriptor.ACTION_ID == actionId) {
                mainFrame.showTerminalPanel(false)
                return
            }
            if (e.modifiersEx == keyModifier) {
                when (e.getKeyCode()) {
                    KeyEvent.VK_UP -> {
                        resizePanel(1)
                        return
                    }

                    KeyEvent.VK_DOWN -> {
                        resizePanel(-1)
                        return
                    }

                    KeyEvent.VK_LEFT -> {
                        resizePanel(-10)
                        return
                    }

                    KeyEvent.VK_RIGHT -> {
                        resizePanel(10)
                        return
                    }

                    KeyEvent.VK_1 -> {
                        mainFrame.leftPanel.fileTable.requestFocus()
                        return
                    }

                    KeyEvent.VK_2 -> {
                        mainFrame.rightPanel.fileTable.requestFocus()
                        return
                    }
                }
            }
            super.processKeyEvent(e)
        } else if (id == KeyEvent.KEY_TYPED) {
            super.processKeyEvent(e)
        }

        if (e.modifiersEx == InputEvent.ALT_DOWN_MASK && e.getKeyCode() == KeyEvent.VK_C) {
            if (id == KeyEvent.KEY_RELEASED) {
                CalculatorDialog(mainFrame.jFrame).showDialog()
            }
            e.consume()
            return
        }

        e.consume()
    }

    private fun resizePanel(delta: Int) {
        var lineHeight = getHeight() / terminalTextBuffer.height
        if (lineHeight > 0 && (lineHeight < this.lineHeight || this.lineHeight == 0)) {
            this.lineHeight = lineHeight
        }
        lineHeight = this.lineHeight
        var height = mainFrame.terminalPanelHeight + delta * lineHeight
        val minHeight = 2 * lineHeight
        val maxHeight = mainFrame.jFrame.getHeight() - minHeight

        if (delta < -2) {
            height = minHeight
        } else if (delta > 2) {
            height = maxHeight
            mainFrame.setTerminalPanelHeight(height)
        } else if (height !in minHeight..maxHeight) {
            return
        }

        mainFrame.setTerminalPanelHeight(height)
    }
}
