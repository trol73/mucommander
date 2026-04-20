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
package com.mucommander.ui.terminal;

import com.jediterm.terminal.HyperlinkStyle;
import com.jediterm.terminal.TerminalColor;
import com.jediterm.terminal.TextStyle;
import com.jediterm.terminal.emulator.ColorPalette;
import com.jediterm.terminal.emulator.ColorPaletteImpl;
import com.jediterm.terminal.model.TerminalTypeAheadSettings;
import com.jediterm.terminal.ui.settings.DefaultSettingsProvider;
import com.mucommander.commons.runtime.OsFamily;
import com.mucommander.ui.theme.Theme;
import com.mucommander.ui.theme.ThemeManager;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.awt.Color;
import java.awt.Font;

/**
 * Provides terminal settings synchronized with application theme.
 * Extends DefaultSettingsProvider to integrate with JediTerm terminal emulator.
 *
 * @author Oleg Trifonov
 * Created on 27/10/14.
 */
@Slf4j
public class TerminalSettingsProvider extends DefaultSettingsProvider {
    // Cached color palette to avoid recreation on each call
    private ColorPalette colorPalette;

    @Override
    @NotNull
    public TerminalColor getDefaultForeground() {
        Color fg = getThemeColor(Theme.TERMINAL_FOREGROUND_COLOR, Color.GREEN);
        return colorFromAwt(fg);
    }

    @Override
    @NotNull
    public TerminalColor getDefaultBackground() {
        Color bg = getThemeColor(Theme.TERMINAL_BACKGROUND_COLOR, Color.BLACK);
        return colorFromAwt(bg);
    }

    @Override
    @NotNull
    public TextStyle getSelectionColor() {
        Color fg = getThemeColor(Theme.TERMINAL_SELECTED_FOREGROUND_COLOR, Color.WHITE);
        Color bg = getThemeColor(Theme.TERMINAL_SELECTED_BACKGROUND_COLOR, new Color(0x6666ff));
        return new TextStyle(colorFromAwt(fg), colorFromAwt(bg));
    }

    @Override
    @NotNull
    public TextStyle getFoundPatternColor() {
        // Use selection colors for found pattern highlighting
        return getSelectionColor();
    }

    @Override
    @NotNull
    public TextStyle getHyperlinkColor() {
        Color hyperlinkColor = getThemeColor(Theme.TERMINAL_FOREGROUND_COLOR, Color.BLUE);
        return new TextStyle(colorFromAwt(hyperlinkColor), null);
    }

    @Override
    @NotNull
    public HyperlinkStyle.HighlightMode getHyperlinkHighlightingMode() {
        return HyperlinkStyle.HighlightMode.HOVER;
    }

    @Override
    public Font getTerminalFont() {
        Font font = ThemeManager.getCurrentFont(Theme.TERMINAL_FONT);
        return font != null ? font : super.getTerminalFont();
    }

    @Override
    public float getTerminalFontSize() {
        Font font = getTerminalFont();
        return font != null ? font.getSize2D() : super.getTerminalFontSize();
    }

    @Override
    public ColorPalette getTerminalColorPalette() {
        if (colorPalette == null) {
            colorPalette = createColorPalette();
        }
        return colorPalette;
    }

    @Override
    public boolean useInverseSelectionColor() {
        return false;
    }

    @Override
    public boolean copyOnSelect() {
        // Enable copy on select for better UX
        return true;
    }

    @Override
    public boolean pasteOnMiddleMouseClick() {
        return true;
    }

    @Override
    public boolean emulateX11CopyPaste() {
        return false;
    }

    @Override
    public boolean useAntialiasing() {
        return true;
    }

    @Override
    public boolean audibleBell() {
        return false;
    }

    @Override
    public boolean enableMouseReporting() {
        return true;
    }

    @Override
    public int caretBlinkingMs() {
        return 500;
    }

    @Override
    public boolean scrollToBottomOnTyping() {
        return true;
    }

    @Override
    public int maxRefreshRate() {
        return 50;
    }

    @Override
    public boolean DECCompatibilityMode() {
        return true;
    }

    @Override
    public boolean forceActionOnMouseReporting() {
        return false;
    }

    @Override
    public int getBufferMaxLinesCount() {
        return 5000;
    }

    @Override
    public boolean altSendsEscape() {
        return true;
    }

    @Override
    public boolean ambiguousCharsAreDoubleWidth() {
        return false;
    }

    @Override
    @NotNull
    public TerminalTypeAheadSettings getTypeAheadSettings() {
        return TerminalTypeAheadSettings.DEFAULT;
    }

    @Override
    public boolean sendArrowKeysInAlternativeMode() {
        return true;
    }

    /**
     * Safely retrieves color from ThemeManager with fallback value.
     *
     * @param themeId theme color identifier
     * @param defaultColor fallback color if theme manager returns null
     * @return color from theme or default color
     */
    private Color getThemeColor(int themeId, Color defaultColor) {
        try {
            Color color = ThemeManager.getCurrentColor(themeId);
            return color != null ? color : defaultColor;
        } catch (Exception e) {
            log.warn("Failed to get theme color for id={}, using default", themeId, e);
            return defaultColor;
        }
    }

    /**
     * Creates color palette from current theme.
     *
     * @return color palette with ANSI colors from theme
     */
    private ColorPalette createColorPalette() {
        return OsFamily.getCurrent() == OsFamily.WINDOWS ? ColorPaletteImpl.WINDOWS_PALETTE : ColorPaletteImpl.XTERM_PALETTE;
    }

    private static TerminalColor colorFromAwt(Color color) {
        if (color == null) {
            return null;
        }
        return TerminalColor.rgb(color.getRed(), color.getGreen(), color.getBlue());
    }
}