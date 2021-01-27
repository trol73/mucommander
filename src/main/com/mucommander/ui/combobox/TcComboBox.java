/*
 * This file is part of trolCommander, http://www.trolsoft.ru/en/soft/trolcommander
 * Copyright (C) 2014-2020 Oleg Trifonov
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
package com.mucommander.ui.combobox;

import com.mucommander.ui.dialog.FocusDialog;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;

/**
 * ComboBox with correct Esc handling.
 *
 * Created on 20/10/14.
 * @author Oleg Trifonov
 */
public class TcComboBox<E> extends JComboBox<E> {

    public TcComboBox() {
        super();
    }

    public TcComboBox(java.util.List<E> items) {
        super();
        for (E item : items) {
            addItem(item);
        }
    }

    public TcComboBox(E[] items) {
        super();
        for (E item : items) {
            addItem(item);
        }
    }

    @Override
    public void processKeyEvent(KeyEvent e) {
        boolean popupVisible = isPopupVisible();
        super.processKeyEvent(e);
        // Close parent FocusDialog if ESC pressed
        if (!popupVisible && e.getID() == KeyEvent.KEY_PRESSED && e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            disposeParentFocusDialog();
        }
    }

    private void disposeParentFocusDialog() {
        Container container = getParent();
        while (container != null) {
            if (container instanceof FocusDialog) {
                ((FocusDialog) container).dispose();
                break;
            }
            container = container.getParent();
        }
    }

}
