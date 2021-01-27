/*
 * This file is part of trolCommander, http://www.trolsoft.ru/en/soft/trolcommander
 * Copyright (C) 2013-2017 Oleg Trifonov
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
package com.mucommander.ui.viewer.text;

import com.mucommander.ui.main.statusbar.FileWindowsListButton;
import org.fife.ui.StatusBarPanel;

import javax.swing.JLabel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;

/**
 *
 * @author Oleg Trifonov
 * Created on 25/09/14.
 */
public class StatusBar extends org.fife.ui.StatusBar {

    private StatusBarPanel panelColor;
    private StatusBarPanel panelPosition;
    private StatusBarPanel panelEncoding;
    private StatusBarPanel panelSyntax;
    private StatusBarPanel panelWindows;

    private JLabel lblColor;
    private JLabel lblPosition;
    private JLabel lblEncoding;
    private JLabel lblSyntax;
    private FileWindowsListButton lbFiles;

    private int color = -1;

    private String forcedStatusMessage;
    private long forcedStatusMessageTimeout;


    public StatusBar() {
        super("");

        lbFiles = new FileWindowsListButton(true);
        panelWindows = new StatusBarPanel(new BorderLayout());
        panelWindows.add(lbFiles);

        lblColor = new JLabel("          ");
        panelColor = new StatusBarPanel(new BorderLayout(), lblColor);

        lblPosition = new JLabel();
        panelPosition = new StatusBarPanel(new BorderLayout(), lblPosition);

        lblSyntax = new JLabel();
        panelSyntax = new StatusBarPanel(new BorderLayout(), lblSyntax);

        lblEncoding = new JLabel();
        panelEncoding = new StatusBarPanel(new BorderLayout(), lblEncoding);

        // Make the layout such that different items can be different sizes.
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 0.0;

        addStatusBarComponent(panelWindows, c);
        addStatusBarComponent(panelPosition, c);
        addStatusBarComponent(panelColor, c);
        addStatusBarComponent(panelSyntax, c);
        addStatusBarComponent(panelEncoding, c);
    }

    public void setColor(int color) {
        if (this.color == color) {
            return;
        }
        this.color = color;
        panelColor.setVisible(color >= 0);
        panelColor.setBackground(new Color(color));
    }

    void setPosition(int line, int column) {
        lblPosition.setText(line + " : " + column);
    }

    public void setEncoding(String encoding) {
        lblEncoding.setText(encoding);
    }

    public void setSyntax(String syntax) {
        lblSyntax.setText(syntax);
    }

    public void clearStatusMessage() {
        if (forcedStatusMessage != null) {
            if (System.currentTimeMillis() > forcedStatusMessageTimeout) {
                forcedStatusMessage = null;
            }
        }
        setStatusMessage(forcedStatusMessage != null ? forcedStatusMessage : "");
    }

    public void showMessage(String msg, long timeInMillis) {
        forcedStatusMessage = msg;
        forcedStatusMessageTimeout = System.currentTimeMillis() + timeInMillis;
        setStatusMessage(msg);
    }
}
