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
package com.mucommander.ui.viewer.image;

import org.fife.ui.StatusBarPanel;

import javax.swing.JLabel;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.util.Date;

/**
 * Created on 11/03/14.
 */
public class StatusBar extends org.fife.ui.StatusBar {

    private StatusBarPanel panelImageSize;
    private StatusBarPanel panelFileNumber;
    private StatusBarPanel panelZoom;
    private StatusBarPanel panelFileSize;
    private StatusBarPanel panelDateTime;

    private JLabel lblImageSize;
    private JLabel lblFileNumber;
    private JLabel lblZoom;
    private JLabel lblFileSize;
    private JLabel lblDateTime;

    private int imageWidth, imageHeight, imageBpp;

    public StatusBar() {
        super("");

        lblImageSize = new JLabel();
        panelImageSize = new StatusBarPanel(new BorderLayout(), lblImageSize);

        lblFileNumber = new JLabel();
        panelFileNumber = new StatusBarPanel(new BorderLayout(), lblFileNumber);

        lblZoom = new JLabel();
        panelZoom = new StatusBarPanel(new BorderLayout(), lblZoom);

        lblFileSize = new JLabel();
        panelFileSize = new StatusBarPanel(new BorderLayout(), lblFileSize);

        lblDateTime = new JLabel();
        panelDateTime = new StatusBarPanel(new BorderLayout(), lblDateTime);

        // Make the layout such that different items can be different sizes.
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;

        c.weightx = 0.0;
        addStatusBarComponent(panelImageSize, c);

        c.weightx = 0.0;
        addStatusBarComponent(panelFileNumber, c);

        c.weightx = 0.0;
        addStatusBarComponent(panelZoom, c);

        c.weightx = 0.0;
        addStatusBarComponent(panelFileSize, c);

        c.weightx = 0.0;
        addStatusBarComponent(panelDateTime, c);

    }

    public void setImageSize(int width, int height) {
        imageWidth = width;
        imageHeight = height;
        updateSizePanel();
    }

    public void setImageBpp(int bpp) {
        imageBpp = bpp;
        updateSizePanel();
    }

    private void updateSizePanel() {
        lblImageSize.setText(imageWidth + " x " + imageHeight + " x " + imageBpp + " BPP");
    }

    public void setFileNumber(int current, int total) {
        lblFileNumber.setText(current + " / " + total);
    }

    public void setZoom(double zoom) {
        long zoomInt = Math.round(zoom*100);
        lblZoom.setText(zoomInt + " %");
    }

    public void setFileSize(long fileSize) {
        lblFileSize.setText(fileSize + " bytes");
    }

    public void setDateTime(long date) {
        lblDateTime.setText(new Date(date).toString());
    }
}
