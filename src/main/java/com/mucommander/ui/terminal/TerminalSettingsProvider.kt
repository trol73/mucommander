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

import com.jediterm.terminal.HyperlinkStyle.HighlightMode
import com.jediterm.terminal.TerminalColor
import com.jediterm.terminal.TerminalColor.rgb
import com.jediterm.terminal.TextStyle
import com.jediterm.terminal.emulator.ColorPalette
import com.jediterm.terminal.emulator.ColorPaletteImpl
import com.jediterm.terminal.model.TerminalTypeAheadSettings
import com.jediterm.terminal.ui.settings.DefaultSettingsProvider
import com.mucommander.commons.runtime.OsFamily
import com.mucommander.ui.theme.Theme
import com.mucommander.ui.theme.ThemeManager
import org.slf4j.LoggerFactory
import java.awt.Color
import java.awt.Font

/**
 * Provides terminal settings synchronized with application theme.
 * Extends DefaultSettingsProvider to integrate with JediTerm terminal emulator.
 * 
 * @author Oleg Trifonov
 * Created on 27/10/14.
 */
class TerminalSettingsProvider : DefaultSettingsProvider() {
    // Cached color palette to avoid recreation on each call
    private var colorPalette: ColorPalette? = null
    private val log = LoggerFactory.getLogger(TerminalSettingsProvider::class.java)

    override fun getDefaultForeground(): TerminalColor =
        getThemeColor(Theme.TERMINAL_FOREGROUND_COLOR, Color.GREEN).toTerm()

    override fun getDefaultBackground(): TerminalColor =
        getThemeColor(Theme.TERMINAL_BACKGROUND_COLOR, Color.BLACK).toTerm()

    override fun getSelectionColor(): TextStyle {
        val fg = getThemeColor(Theme.TERMINAL_SELECTED_FOREGROUND_COLOR, Color.WHITE)
        val bg = getThemeColor(Theme.TERMINAL_SELECTED_BACKGROUND_COLOR, Color(0x6666ff))
        return TextStyle(fg.toTerm(), bg.toTerm())
    }

    override fun getFoundPatternColor(): TextStyle =
        // Use selection colors for found pattern highlighting
        getSelectionColor()

    override fun getHyperlinkColor(): TextStyle {
        val hyperlinkColor = getThemeColor(Theme.TERMINAL_FOREGROUND_COLOR, Color.BLUE)
        return TextStyle(hyperlinkColor.toTerm(), null)
    }

    override fun getHyperlinkHighlightingMode(): HighlightMode =
        HighlightMode.HOVER

    override fun getTerminalFont(): Font =
        ThemeManager.getCurrentFont(Theme.TERMINAL_FONT) ?: super.getTerminalFont()


    override fun getTerminalFontSize(): Float =
        getTerminalFont().getSize2D()


    override fun getTerminalColorPalette(): ColorPalette? {
        if (colorPalette == null) {
            colorPalette = createColorPalette()
        }
        return colorPalette
    }

    override fun useInverseSelectionColor(): Boolean = false
    override fun copyOnSelect(): Boolean = true        // Enable copy on select for better UX
    override fun pasteOnMiddleMouseClick(): Boolean = true
    override fun emulateX11CopyPaste(): Boolean = false
    override fun useAntialiasing(): Boolean = true
    override fun audibleBell(): Boolean = false
    override fun enableMouseReporting(): Boolean = true
    override fun caretBlinkingMs(): Int = 500
    override fun scrollToBottomOnTyping(): Boolean = true
    override fun maxRefreshRate(): Int = 50
    override fun DECCompatibilityMode(): Boolean = true
    override fun forceActionOnMouseReporting(): Boolean = false
    override fun getBufferMaxLinesCount(): Int = 5000
    override fun altSendsEscape(): Boolean = true
    override fun ambiguousCharsAreDoubleWidth(): Boolean = false
    override fun getTypeAheadSettings() = TerminalTypeAheadSettings.DEFAULT

    /**
     * Safely retrieves color from ThemeManager with fallback value.
     * 
     * @param themeId theme color identifier
     * @param defaultColor fallback color if theme manager returns null
     * @return color from theme or default color
     */
    private fun getThemeColor(themeId: Int, defaultColor: Color): Color =
        try {
            ThemeManager.getCurrentColor(themeId) ?: defaultColor
        } catch (e: Exception) {
            log.warn("Failed to get theme color for id={}, using default", themeId, e)
            defaultColor
        }

    /**
     * Creates color palette from current theme.
     * 
     * @return color palette with ANSI colors from theme
     */
    private fun createColorPalette(): ColorPalette =
        if (OsFamily.getCurrent() == OsFamily.WINDOWS) {
            ColorPaletteImpl.WINDOWS_PALETTE
        } else {
            ColorPaletteImpl.XTERM_PALETTE
        }

    private fun Color.toTerm(): TerminalColor =
        rgb(red, green, blue)
}