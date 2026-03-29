/*
 * This file is part of trolCommander, http://www.trolsoft.ru/en/soft/trolcommander
 * Copyright (C) 2013-2018 Oleg Trifonov
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
package com.mucommander.ui.viewer.text.tools;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.FileFactory;
import com.mucommander.commons.util.StringUtils;
import com.mucommander.ui.theme.Theme;
import com.mucommander.ui.theme.ThemeManager;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;

import static com.mucommander.commons.util.StringUtils.isNullOrBlank;
import static com.mucommander.commons.util.StringUtils.isNumber;

class ExecOutputTextPane extends JTextPane {
    private final Style styleDefault = addStyle("Default", null);
    private final Style styleFilePath = addStyle("Path", null);
    private final Style stylePosition = addStyle("Position", null);
    private final Style styleMessage = addStyle("Message", null);
    private final Style styleError = addStyle("Error", styleMessage);
    private final Style styleWarning = addStyle("Warning", styleMessage);
    private final Style styleMarker = addStyle("Marker", styleMessage);


    ExecOutputTextPane(Runnable onClose, OnClickFileHandler onFileClickHandler) {
        setCaretPosition(0);
        setEditable(false);

        setupKeyListener(onClose);
        setupMouseListener(onFileClickHandler);
        setupColors();
        setupStyles();
    }


    private void setupKeyListener(Runnable onClose) {
        addKeyListener(new KeyAdapter() {
            private boolean pressed;
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    pressed = true;
                    e.consume();
                } else {
                    pressed = false;
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE && pressed) {
                    if (onClose != null) {
                        onClose.run();
                    }
                    e.consume();
                }
            }
        });
    }

    private void setupMouseListener(OnClickFileHandler onFileClickHandler) {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                String str = getLineForMousePosition(e.getPoint());
                if (isNullOrBlank(str)) {
                    return;
                }
                String[] parts = str.split(":");
                if (parts.length < 3) {
                    return;
                }
                if (onFileClickHandler != null && isNumber(parts[1]) && isFilePath(parts[0])) {
                    AbstractFile file = FileFactory.getFile(parts[0]);
                    int line = Integer.parseInt(parts[1]);
                    int column = parts.length > 3 && isNumber(parts[2]) ? Integer.parseInt(parts[2]) : 1;
                    onFileClickHandler.onClick(file, line, column);
                }

            }
        });
    }

    private void setupColors() {
        setForeground(ThemeManager.getCurrentColor(Theme.SHELL_FOREGROUND_COLOR));
        setCaretColor(ThemeManager.getCurrentColor(Theme.SHELL_FOREGROUND_COLOR));
        setBackground(ThemeManager.getCurrentColor(Theme.SHELL_BACKGROUND_COLOR));
        setSelectedTextColor(ThemeManager.getCurrentColor(Theme.SHELL_SELECTED_FOREGROUND_COLOR));
        setSelectionColor(ThemeManager.getCurrentColor(Theme.SHELL_SELECTED_BACKGROUND_COLOR));
        setFont(ThemeManager.getCurrentFont(Theme.SHELL_FONT));
    }

    private void setupStyles() {
        StyleConstants.setForeground(styleFilePath, new Color(0x5555ff));
        StyleConstants.setUnderline(styleFilePath, true);
        StyleConstants.setForeground(stylePosition, Color.YELLOW);
        StyleConstants.setForeground(styleError, Color.RED);
        StyleConstants.setForeground(styleWarning, Color.CYAN);
        StyleConstants.setForeground(styleMarker, Color.WHITE);
    }


    private void add(String s, Style style) {
        StyledDocument doc = getStyledDocument();
        try {
            doc.insertString(doc.getLength(), s, style);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    void addLine(String line) {
        if (isMarker(line)) {
            add(line, styleMarker);
            update();
            return;
        }
        String[] parts = line.split(":");
        if (parts.length < 3 || !isNumber(parts[1]) || !isFilePath(parts[0])) {
            add(line, styleDefault);
            update();
            return;
        }
        add(parts[0], styleFilePath);
        add(":", styleDefault);
        add(parts[1], stylePosition);
        add(":", styleDefault);
        int index;
        if (isNumber(parts[2])) {
            add(parts[2], stylePosition);
            add(":", styleDefault);
            index = 3;
        } else {
            index = 2;
        }
        Style defaultStyle = styleMessage;
        for (int i = index; i < parts.length; i++) {
            String part = parts[i];
            String lower = part.trim().toLowerCase();
            if (lower.equals("error")) {
                add(part, styleError);
                defaultStyle = styleError;
            } else if (lower.equals("warning") || lower.equals("note")) {
                add(part, styleWarning);
                defaultStyle = styleWarning;
            } else {
                add(part, defaultStyle);
            }
            if (i < parts.length-1 || line.endsWith(":")) {
                add(":", defaultStyle);
            }
        }
        update();
    }

    private void update() {
        setCaretPosition(getText().length());
        getCaret().setVisible(true);
        repaint();
    }

    void clear() {
        setText("");
        setCaretPosition(0);
        getCaret().setVisible(true);
        requestFocus();
    }


    private static boolean isMarker(String s) {
        if (StringUtils.isNullOrBlank(s)) {
            return false;
        }
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            if (" \t^~\n".indexOf(ch) < 0) {
                return false;
            }
        }
        return true;
    }

    private static boolean isFilePath(String s) {
        return new File(s).exists();
    }

    private String getLineForMousePosition(Point p) {
        if (p == null) {
            return null;
        }
        int pos = viewToModel(p);
        try {
            int start = Utilities.getRowStart(this, pos);
            int end = Utilities.getRowEnd(this, pos);
            if (start < 0 || end < start) {
                return null;
            }
            return getDocument().getText(start, end - start);
        } catch (BadLocationException e) {
            return null;
        }
    }


}
