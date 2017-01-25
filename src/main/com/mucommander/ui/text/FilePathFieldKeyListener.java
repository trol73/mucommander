/*
 * This file is part of trolCommander, http://www.trolsoft.ru/en/soft/trolcommander
 * Copyright (C) 2013-2017 Oleg Trifonov
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
package com.mucommander.ui.text;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 * @author Oleg Trifonov
 * Created on 25/01/17.
 */
public class FilePathFieldKeyListener implements KeyListener {

    private final JTextField textField;
    private final boolean deleteOnFirstAction;

    protected FilePathFieldKeyListener(JTextField textField, boolean deleteOnFirstAction) {
        super();
        this.textField = textField;
        this.deleteOnFirstAction = deleteOnFirstAction;
        textField.addKeyListener(this);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        // Key listener to detect first left/right arrow pressed
        // if user press the left button then move cursor to the start of file name
        //if user press the right button then move cursor to the end of file name

        if (e.getKeyCode() == KeyEvent.VK_LEFT) {
            if (deleteOnFirstAction) {
                textField.removeKeyListener(this);
            }
            if (e.getModifiers() != 0) {
                return;
            }
            int len = textField.getText().length();
            int pos = textField.getCaretPosition();
            String selected = textField.getSelectedText();
            String text = textField.getText();

            if (selected != null && ((len > pos && text.charAt(pos) == '.') || len == pos)) {
                int newPos = pos - selected.length();
                if (newPos >= 0) {
                    textField.setCaretPosition(newPos);
                }
                e.consume();
            }
        } else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
            if (deleteOnFirstAction) {
                textField.removeKeyListener(this);
            }
            if (e.getModifiers() != 0) {
                return;
            }
            int pos = textField.getCaretPosition();
            String selected = textField.getSelectedText();
            String text = textField.getText();

            if (selected != null && text.length() > pos && text.charAt(pos)  == '.') {
                textField.setCaretPosition(pos);
                e.consume();
            }
        }

    }

    @Override
    public void keyTyped(KeyEvent e) {

    }


    @Override
    public void keyReleased(KeyEvent e) {

    }
}
