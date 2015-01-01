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
package com.mucommander.ui.main.statusbar;

import com.mucommander.ui.button.NonFocusableButton;
import com.mucommander.ui.dialog.FocusDialog;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * @author Oleg Trifonov
 * Created on 08/12/14.
 */
public class TaskWidget extends JProgressBar {//NonFocusableButton {

    private static final Insets INSETS = new Insets(2, 2, 2, 2);

    private Color colorBorder = new Color(0x555555);
    private Color colorForeground = new Color(0xbbbbff);
    private Color colorText = new Color(0x000000);

    private int progress;

    TaskPanel taskPanel;
    private FocusDialog progressDialog;


    public TaskWidget() {
        super();
        init();
    }

    public TaskWidget(String text) {
        super();
        setString(text);
        init();
    }

    public TaskWidget(Icon icon) {
        super();
        init();
    }

    public TaskWidget(String text, Icon icon) {
        super();
        setText(text);
        init();
    }


    private void init() {
        setStringPainted(true);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                    if (progressDialog != null) {
                        setVisible(false);
                        progressDialog.showDialog();
                        //progressDialog.setVisible(true);
                    }
            }
        });
    }


    /**
     * Replace the default insets to be exactly (2,2,2,2).
     */
    @Override
    public Insets getInsets() {
        return INSETS;
    }

    public void removeFromPanel() {
        if (taskPanel != null) {
            taskPanel.removeWidget(this);
        }
    }


    public void setProgress(int progress) {
        this.progress = progress;
        setValue(progress);
    }


    public FocusDialog getProgressDialog() {
        return progressDialog;
    }


    public void setProgressDialog(FocusDialog progressDialog) {
        this.progressDialog = progressDialog;
    }


    public void setText(String s) {
        setString(s);

        Dimension dim = new JLabel(s).getPreferredSize();
        setPreferredSize(new Dimension(dim.width + 20, getPreferredSize().height));
        setMaximumSize(new Dimension(dim.width + 20, getMaximumSize().height));

        if (getParent() != null) {
            getParent().revalidate();
        }
    }


    public String getText() {
        return getString();
    }
}
