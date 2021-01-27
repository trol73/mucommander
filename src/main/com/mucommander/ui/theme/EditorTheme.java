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
package com.mucommander.ui.theme;

import org.fife.ui.rsyntaxtextarea.*;
import org.fife.ui.rsyntaxtextarea.Theme;

import java.awt.Font;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created on 04/01/14.
 */
public class EditorTheme {
    private org.fife.ui.rsyntaxtextarea.Theme theme;

    private EditorTheme() {
    }

    private EditorTheme(org.fife.ui.rsyntaxtextarea.Theme theme) {
        this.theme = theme;
    }

    public EditorTheme(RSyntaxTextArea textArea) {
        theme = new Theme(textArea);
    }

    public static EditorTheme load(InputStream in) throws IOException {
        return new EditorTheme(org.fife.ui.rsyntaxtextarea.Theme.load(in));
    }

    public static EditorTheme load(InputStream in, Font baseFont) throws IOException {
        return new EditorTheme(org.fife.ui.rsyntaxtextarea.Theme.load(in, baseFont));
    }

    public void apply(RSyntaxTextArea textArea) {
        theme.apply(textArea);
    }

    public void save(OutputStream out) throws IOException {
        theme.save(out);
    }

}
