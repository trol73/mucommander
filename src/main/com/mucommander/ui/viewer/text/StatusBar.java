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
package com.mucommander.ui.viewer.text;

import org.fife.ui.StatusBarPanel;

import javax.swing.JLabel;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;

/**
 *
 * Created by trol on 25/09/14.
 */
public class StatusBar extends org.fife.ui.StatusBar {

    private StatusBarPanel panelPosition;
    private StatusBarPanel panelEncoding;

    private JLabel lblPosition;
    private JLabel lblEncoding;


    public StatusBar() {
        super("");

        lblPosition = new JLabel();
        panelPosition = new StatusBarPanel(new BorderLayout(), lblPosition);

        lblEncoding = new JLabel();
        panelEncoding = new StatusBarPanel(new BorderLayout(), lblEncoding);

        // Make the layout such that different items can be different sizes.
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;

        c.weightx = 0.0;
        addStatusBarComponent(panelPosition, c);
        c.weightx = 0.0;
        addStatusBarComponent(panelEncoding, c);
    }

    public void setPosition(int line, int column) {
        lblPosition.setText(line + " : " + column);
    }

    public void setEncoding(String encoding) {
        lblEncoding.setText(encoding);
    }
}
