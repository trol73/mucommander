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

import javax.swing.*;
import java.awt.*;

/**
 * JCheckBoxMenuItem wrapper with fixed color (fix issues on linux with Cinnamon)
 *
 * @author Oleg Trifonov
 * Created on 21.09.16.
 */
public class TCheckBoxMenuItem extends JCheckBoxMenuItem {


    public TCheckBoxMenuItem() {
        super();
        init();
    }

    public TCheckBoxMenuItem(Icon icon) {
        super(icon);
        init();
    }

    public TCheckBoxMenuItem(String text) {
        super(text);
        init();
    }

    public TCheckBoxMenuItem(Action a) {
        super(a);
        init();
    }

    public TCheckBoxMenuItem(String text, Icon icon) {
        super(text, icon);
        init();
    }

    public TCheckBoxMenuItem(String text, boolean b) {
        super(text, b);
        init();
    }

    public TCheckBoxMenuItem(String text, Icon icon, boolean b) {
        super(text, icon, b);
        init();
    }


    private void init() {
        if (OsFamily.LINUX.isCurrent()) {
            setOpaque(true);
            setForeground(Color.BLACK);
        }

    }
}
