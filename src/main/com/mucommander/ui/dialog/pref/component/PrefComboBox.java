/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2012 Maxence Bernard
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

package com.mucommander.ui.dialog.pref.component;

import com.mucommander.ui.dialog.FocusDialog;
import com.mucommander.ui.dialog.pref.PreferencesDialog;

import javax.swing.*;
import java.awt.Container;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.util.List;

/**
 * @author Arik Hadas, Oleg Trifonov
 */
public abstract class PrefComboBox<E> extends JComboBox<E> implements PrefComponent {

	public PrefComboBox() {
		super();
	}

    public PrefComboBox(List<E> items) {
        super();
        addItems(items);
    }

    public PrefComboBox(E[] items) {
        super();
        addItems(items);
    }

    public void addItems(List<E> items) {
        for (E item : items) {
            addItem(item);
        }
    }

    public void addItems(E[] items) {
        for (E item : items) {
            addItem(item);
        }
    }
	
	public void addDialogListener(final PreferencesDialog dialog) {
		addItemListener(new ItemListener() {

			public void itemStateChanged(ItemEvent e) {
				dialog.componentChanged(PrefComboBox.this);
			}
		});		
	}

    @Override
    public void processKeyEvent(KeyEvent e) {
        boolean popupVisible = isPopupVisible();
        super.processKeyEvent(e);
        // Close parent FocusDialog if ESC pressed
        if (!popupVisible && e.getID() == KeyEvent.KEY_PRESSED && e.getKeyCode() == KeyEvent.VK_ESCAPE) {
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
}
