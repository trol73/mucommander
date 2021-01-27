/*
 * This file is part of trolCommander, http://www.trolsoft.ru/en/soft/trolcommander
 * Copyright (C) 2013-2016 Oleg Trifonov
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
package ru.trolsoft.ui;

import com.mucommander.commons.runtime.OsFamily;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JRadioButtonMenuItem;
import java.awt.Color;

public class TRadioButtonMenuItem extends JRadioButtonMenuItem {

    public TRadioButtonMenuItem() {
        init();
    }

    public TRadioButtonMenuItem(Icon icon) {
        super(icon);
        init();
    }

    public TRadioButtonMenuItem(String text) {
        super(text);
        init();
    }

    public TRadioButtonMenuItem(Action a) {
        super(a);
        init();
    }

    public TRadioButtonMenuItem(String text, Icon icon) {
        super(text, icon);
        init();
    }

    public TRadioButtonMenuItem(String text, boolean selected) {
        super(text, selected);
        init();
    }

    public TRadioButtonMenuItem(Icon icon, boolean selected) {
        super(icon, selected);
        init();
    }

    public TRadioButtonMenuItem(String text, Icon icon, boolean selected) {
        super(text, icon, selected);
        init();
    }

    private void init() {
        if (OsFamily.LINUX.isCurrent()) {
            setOpaque(true);
            setForeground(Color.BLACK);
        }
    }

}