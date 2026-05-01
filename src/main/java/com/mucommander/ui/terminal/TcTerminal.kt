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

import com.jediterm.terminal.TtyConnector
import com.jediterm.terminal.model.StyleState
import com.jediterm.terminal.model.TerminalTextBuffer
import com.jediterm.terminal.ui.JediTermWidget
import com.jediterm.terminal.ui.TerminalPanel
import com.jediterm.terminal.ui.TerminalSession
import com.jediterm.terminal.ui.TerminalWidget
import com.jediterm.terminal.ui.settings.SettingsProvider
import com.mucommander.cache.WindowsStorage
import com.mucommander.ui.main.MainFrame
import java.awt.event.FocusEvent
import java.awt.event.FocusListener
import java.io.IOException
import javax.swing.JComponent

/**
 * @author Oleg Trifonov
 * Created on 24/10/14.
 */
class TcTerminal(private val mainFrame: MainFrame) {
    private val termWidget: TerminalWidget

    init {
        val settingsProvider: SettingsProvider = TerminalSettingsProvider()
        val ttyConnector = createTtyConnector(getCurrentFolder())

        termWidget = object : JediTermWidget(settingsProvider) {
            override fun createTerminalPanel(
                settingsProvider: SettingsProvider,
                styleState: StyleState,
                textBuffer: TerminalTextBuffer
            ): TerminalPanel =
                JediTerminalPanelEx(settingsProvider, textBuffer, styleState, mainFrame)
        }

        termWidget.component.addFocusListener(object : FocusListener {
            override fun focusGained(e: FocusEvent?) {
                updateTitle()
            }

            override fun focusLost(e: FocusEvent?) {
                mainFrame.updateWindowTitle()
            }
        })

        if (termWidget.canOpenSession()) {
            openSession(termWidget, ttyConnector)
        }
    }


    private fun createTtyConnector(directory: String?): TcTerminalTtyConnector? {
        try {
            return object : TcTerminalTtyConnector(directory) {
                override fun close() {
                    super.close()
                    mainFrame.closeTerminalSession()
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }
    }

    fun openSession(terminal: TerminalWidget, ttyConnector: TtyConnector?) {
        val session: TerminalSession = terminal.createTerminalSession(ttyConnector)
        session.start()
    }


    fun storeHeight(height: Int) {
        WindowsStorage.getInstance().put(STORAGE_KEY, WindowsStorage.Record(0, 0, 0, height))
    }

    fun loadHeight(): Int =
        WindowsStorage.getInstance().get(STORAGE_KEY)?.height ?: -1

    fun getComponent(): JComponent = termWidget.component

    fun show(show: Boolean) {
        termWidget.component.isVisible = show
    }

    private fun getCurrentFolder(): String? {
        val currentFolder = mainFrame.activePanel.currentFolder.absolutePath
        return if (currentFolder.contains("://")) null else currentFolder
    }

    fun updateTitle() {
        mainFrame.jFrame.setTitle(termWidget.terminalDisplay.windowTitle)
    }

    companion object {
        private const val STORAGE_KEY = "TerminalPanel"
    }
}
