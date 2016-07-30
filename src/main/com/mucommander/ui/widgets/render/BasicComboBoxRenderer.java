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
package com.mucommander.ui.widgets.render;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.Component;
import java.awt.Dimension;

/**
 * Renderer for ComboBox
 *
 * @author Oleg Trifonov
 * Created on 05/01/14.
 */
public class BasicComboBoxRenderer<T> extends JLabel implements ListCellRenderer<T> {
    /**
     * An empty <code>Border</code>. This field might not be used. To change the
     * <code>Border</code> used by this renderer directly set it using
     * the <code>setBorder</code> method.
     */
    protected static final Border NO_FOCUS_BORDER = new EmptyBorder(1, 1, 1, 1);
    private static final Border SAFE_NO_FOCUS_BORDER = new EmptyBorder(1, 1, 1, 1);

    @Override
    public Component getListCellRendererComponent(JList<? extends T> list, T value, int index, boolean isSelected, boolean cellHasFocus) {
        if (isSelected) {
            setBackground(list.getSelectionBackground());
            setForeground(list.getSelectionForeground());
        } else {
            setBackground(list.getBackground());
            setForeground(list.getForeground());
        }

        setFont(list.getFont());

        if (value instanceof Icon) {
            setIcon((Icon) value);
        } else {
            setText(value == null ? "" : value.toString());
        }
        return this;
    }


    public BasicComboBoxRenderer() {
        super();
        setOpaque(true);
        setBorder(getNoFocusBorder());
    }

    private static Border getNoFocusBorder() {
        return System.getSecurityManager() == null ? NO_FOCUS_BORDER : SAFE_NO_FOCUS_BORDER;
    }

    public Dimension getPreferredSize() {
        Dimension size;

        if (getText() == null || getText().isEmpty()) {
            setText(" ");
            size = super.getPreferredSize();
            setText("");
        } else {
            size = super.getPreferredSize();
        }

        return size;
    }

}
