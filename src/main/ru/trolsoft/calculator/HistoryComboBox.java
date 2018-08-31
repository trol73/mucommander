/*
 * This file is part of trolCommander, http://www.trolsoft.ru/en/soft/trolcommander
 * Copyright (C) 2013-2016 Oleg Trifonov
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
package ru.trolsoft.calculator;

import com.mucommander.ui.dialog.FocusDialog;

import javax.swing.JComboBox;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;

/**
 * @author Oleg Trifonov
 * Created on 06/06/14.
 */
public class HistoryComboBox extends JComboBox<String> {
    private final FocusDialog parent;

    public HistoryComboBox(FocusDialog parent, List<String> values) {
        super(values.toArray(new String[0]));
        this.parent = parent;
        setEditable(true);
        if (values.size() > 0) {
            String text = values.get(0);
            setSelectedItem(text);
            getEditor().selectAll();
        }

        KeyAdapter keyAdapter = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    if (isPopupVisible()) {
                        hidePopup();
                        e.consume();
                    } else {
                        HistoryComboBox.this.parent.cancel();
                    }
                }
            }
        };
        getEditor().getEditorComponent().addKeyListener(keyAdapter);
    }


    public void addToHistory(String s) {
        for (int i = 0; i < getItemCount(); i++) {
            String item = getItemAt(i);
            if (item.equalsIgnoreCase(s)) {
                removeItem(item);
                break;
            }
        }
        insertItemAt(s, 0);
        setSelectedIndex(0);
    }
}
