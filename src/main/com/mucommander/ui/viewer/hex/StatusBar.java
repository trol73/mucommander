/*
 * This file is part of trolCommander, http://www.trolsoft.ru/en/soft/trolcommander
 * Copyright (C) 2014-2016 Oleg Trifonov
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

package com.mucommander.ui.viewer.hex;

import com.mucommander.ui.main.statusbar.FileWindowsListButton;
import org.fife.ui.StatusBarPanel;
import ru.trolsoft.utils.StrUtils;

import javax.swing.JLabel;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;

/**
 *
 */
public class StatusBar extends org.fife.ui.StatusBar {
    private FileWindowsListButton lbFiles;

    private JLabel lblOffset;
    private JLabel lblEncoding;
    private JLabel lblValue;

    private long maxOffset = -1;


    public StatusBar() {
        super("");

        lbFiles = new FileWindowsListButton(true);
        StatusBarPanel panelWindows = new StatusBarPanel(new BorderLayout());
        panelWindows.add(lbFiles);

        lblOffset = createLabel();
        StatusBarPanel panelOffset = new StatusBarPanel(new BorderLayout(), lblOffset);

        lblEncoding = createLabel();
        StatusBarPanel panelEncoding = new StatusBarPanel(new BorderLayout(), lblEncoding);

        lblValue = createLabel();
        StatusBarPanel panelValue = new StatusBarPanel(new BorderLayout(), lblValue);

        // Make the layout such that different items can be different sizes.
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;

        c.weightx = 0.0;
        addStatusBarComponent(panelWindows, c);
        addStatusBarComponent(panelOffset, c);
        addStatusBarComponent(panelValue, c);
        addStatusBarComponent(panelEncoding, c);
    }

    private JLabel createLabel() {
        JLabel lbl = new JLabel();
        Font fnt = lbl.getFont();
        lbl.setFont(new Font(Font.MONOSPACED, fnt.getStyle(), fnt.getSize()));
        return lbl;
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

    public void clearStatusMessage() {
        setStatusMessage("");
    }
}
