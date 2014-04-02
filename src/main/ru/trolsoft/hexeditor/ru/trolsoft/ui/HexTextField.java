/*
 * This file is part of muCommander, http://www.mucommander.com
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
package ru.trolsoft.hexeditor.ru.trolsoft.ui;

import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.xml.bind.DatatypeConverter;
import java.awt.EventQueue;
import java.awt.Toolkit;

/**
 * Input filed for text and hex values
 */
public class HexTextField extends JTextField implements DocumentListener {

    public enum FilterType {
        ANY_TEXT,
        HEX,
    }

    private static final String HEX_SYMBOLS = "0123456789ABCDEF";


    private FilterType filterType = FilterType.HEX;


    private int maxLength = 0xff;
    private boolean filtering = false;

    public HexTextField() {
        super();
        getDocument().addDocumentListener(this);
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        filterText();
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        filterText();
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        filterText();
    }

    public FilterType getFilterType() {
        return filterType;
    }

    public void setFilterType(FilterType filterType) {
        this.filterType = filterType;
    }

    public int getMaxLength() {
        return maxLength;
    }

    public void setMaxLength(int maxLength) {
        this.maxLength = maxLength;
    }




    private void filterText() {
        if (filterType == null || filterType == FilterType.ANY_TEXT) {
            return;
        }
        if (filtering) {
            return;
        }
        filtering = true;

        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                String input = getText().toUpperCase();
                String filtered = filterHexString(input, maxLength);
                setText(filtered);
                filtering = false;
            }
        });
    }


    private static String filterHexString(String input, int maxBytes) {
        StringBuilder filtered = new StringBuilder();
        int index = 0;

        // filter
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (HEX_SYMBOLS.indexOf(c) >= 0) {
                filtered.append(c);
                if (index++ % 2 == 1 && i != input.length() - 1)
                    filtered.append(' '); // whitespace after each byte
            }
        }
        // limit size
        if (filtered.length() > 3 * maxBytes) {
            filtered.setLength(3 * maxBytes);
            Toolkit.getDefaultToolkit().beep();
        }
        return filtered.toString();
    }



    public byte[] getBytes() {
        String text = getText().replace(" ", "");
        if (text.length() % 2 == 1) {
            text = text.substring(0, text.length() - 1) + "0" + text.charAt(text.length() - 1);
        }

        return DatatypeConverter.parseHexBinary(text);
    }

}
