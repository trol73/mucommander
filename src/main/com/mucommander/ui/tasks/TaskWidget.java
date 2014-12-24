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
package com.mucommander.ui.tasks;

import com.mucommander.ui.button.NonFocusableButton;

import javax.swing.Icon;
import javax.swing.JLabel;
import java.awt.Dimension;
import java.awt.Insets;

/**
 * @author Oleg Trifonov
 * Created on 08/12/14.
 */
public class TaskWidget extends NonFocusableButton {


    public TaskWidget() {
        super();
        init();
    }

    public TaskWidget(String text) {
        super(text);
        init();
    }

    public TaskWidget(Icon icon) {
        super(icon);
        init();
    }

    public TaskWidget(String text, Icon icon) {
        super(text, icon);
        init();
    }


    private void init() {
        setContentAreaFilled(false);
        int height = new JLabel(getText()).getPreferredSize().height;
        setPreferredSize(new Dimension(getPreferredSize().width, height));
    }


    /**
     * Replace the default insets to be exactly (2,2,2,2).
     */
    @Override
    public Insets getInsets() {
        return new Insets(2, 2, 2, 2);
    }
}
