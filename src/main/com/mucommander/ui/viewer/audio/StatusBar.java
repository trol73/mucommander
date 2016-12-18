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
package com.mucommander.ui.viewer.audio;

import org.fife.ui.StatusBarPanel;

import javax.swing.JLabel;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;

/**
 * Created on 14/03/14.
 */
public class StatusBar extends org.fife.ui.StatusBar {
    private StatusBarPanel panel;
    private JLabel lbl;

    public StatusBar() {
        super("");

        lbl = new JLabel();
        panel = new StatusBarPanel(new BorderLayout(), lbl);

        // Make the layout such that different items can be different sizes.
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;

        c.weightx = 0.0;
        addStatusBarComponent(panel, c);
    }

    public void set(String s) {
        lbl.setText(s);
    }

}
