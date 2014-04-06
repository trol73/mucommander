/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2014 Oleg Trifonov
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
package com.mucommander.ui.viewer.hex;

import org.fife.ui.StatusBarPanel;
import ru.trolsoft.utils.StrUtils;

import javax.swing.JLabel;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;

/**
 *
 */
public class StatusBar extends org.fife.ui.StatusBar {
    private StatusBarPanel panelOffset;
    private StatusBarPanel panelEncoding;
    private StatusBarPanel panelValue;

    private JLabel lblOffset;
    private JLabel lblEncoding;
    private JLabel lblValue;

    private long maxOffset = -1;


    public StatusBar() {
        super("");

        lblOffset = new JLabel();
        panelOffset = new StatusBarPanel(new BorderLayout(), lblOffset);

        lblEncoding = new JLabel();
        panelEncoding = new StatusBarPanel(new BorderLayout(), lblEncoding);

        lblValue = new JLabel();
        panelValue = new StatusBarPanel(new BorderLayout(), lblValue);

        // Make the layout such that different items can be different sizes.
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;

        c.weightx = 0.0;
        addStatusBarComponent(panelOffset, c);
        c.weightx = 0.0;
        addStatusBarComponent(panelEncoding, c);
        c.weightx = 0.0;
        addStatusBarComponent(panelValue, c);
    }

    public void setOffset(long offset) {
        String str = StrUtils.dwordToHexStr(offset);
        if (maxOffset >= 0) {
            str += " / " + StrUtils.dwordToHexStr(maxOffset);
        }
        lblOffset.setText(str);
    }

    public void setMaxOffset(long maxOffset) {
        this.maxOffset = maxOffset;
    }

    public void setEncoding(String encoding) {
        lblEncoding.setText(encoding);
    }

    public void setByteValue(byte val) {
        String s = StrUtils.byteToBinaryStr(val)  + " - " + StrUtils.byteToOctalStr(val);
        lblValue.setText(s);
    }

}
