/*
 * This file is part of trolCommander, http://www.trolsoft.ru/soft/trolcommander
 * Copyright (C) 2013-2014 Oleg Trifonov
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.mucommander.ui.terminal;

import com.jediterm.terminal.TerminalColor;
import com.jediterm.terminal.TextStyle;
import com.jediterm.terminal.ui.settings.DefaultSettingsProvider;
import com.mucommander.ui.theme.Theme;
import com.mucommander.ui.theme.ThemeManager;

import java.awt.Color;
import java.awt.Font;

/**
 *
 * @author Oleg Trifonov
 * Created on 27/10/14.
 */
public class TerminalSettingsProvider extends DefaultSettingsProvider {


    @Override
    public TextStyle getDefaultStyle() {
        Color fg = ThemeManager.getCurrentColor(Theme.TERMINAL_FOREGROUND_COLOR);
        Color bg = ThemeManager.getCurrentColor(Theme.TERMINAL_BACKGROUND_COLOR);
        return new TextStyle(TerminalColor.awt(fg), TerminalColor.awt(bg));
    }

    @Override
    public TextStyle getSelectionColor() {
        Color fg = ThemeManager.getCurrentColor(Theme.TERMINAL_SELECTED_FOREGROUND_COLOR);
        Color bg = ThemeManager.getCurrentColor(Theme.TERMINAL_SELECTED_BACKGROUND_COLOR);
        return new TextStyle(TerminalColor.awt(fg), TerminalColor.awt(bg));
    }

    @Override
    public Font getTerminalFont() {
        return ThemeManager.getCurrentFont(Theme.TERMINAL_FONT);
    }

    @Override
    public float getTerminalFontSize() {
        return getTerminalFont().getSize();
    }

    public boolean useInverseSelectionColor() {
        return false;
    }

}
