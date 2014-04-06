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

import ru.trolsoft.utils.StrUtils;

import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.xml.bind.DatatypeConverter;
import java.awt.EventQueue;
import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.List;

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

    private List<HexTextField> assignedFields;

    public HexTextField() {
        super();
        getDocument().addDocumentListener(this);
    }

    public HexTextField(int columns) {
        super(columns);
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
            updateAssignedFields();
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
                updateAssignedFields();
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
        if (maxBytes > 0) {
            if (filtered.length() > 3 * maxBytes) {
                filtered.setLength(3 * maxBytes);
                Toolkit.getDefaultToolkit().beep();
            }
        }
        return filtered.toString();
    }


    private static byte[] hexStringToBytes(String text) {
        if (text.length() % 2 == 1) {
            text = text.substring(0, text.length() - 1) + "0" + text.charAt(text.length() - 1);
        }
         return DatatypeConverter.parseHexBinary(text);
    }

    private static String bytesToHexString(byte[] bytes) {
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            s.append(StrUtils.byteToHexStr(bytes[i]));
            s.append(' ');
        }
        return s.toString();
    }

    public byte[] getBytes() {
        String text = getText().replace(" ", "");
        return hexStringToBytes(text);
    }

    public static byte[] hexDumpToBytes(String s) {
        return hexStringToBytes(s.replace(" ", ""));
    }

    public void assignField(HexTextField field) {
        if (assignedFields == null) {
            assignedFields = new ArrayList<>();
        }
        assignedFields.add(field);
    }


    private void updateAssignedFields() {
        if (assignedFields == null) {
            return;
        }
        final String src = getText();
        for (HexTextField field : assignedFields) {
            String text = convert(src, filterType, field.getFilterType());
            if (!text.equals(field.getText())) {
//                field.setText(text);
            }
        }
    }

    private static String convert(String text, FilterType from, FilterType to) {
        if (from == to) {
            return text;
        }
        if (from == FilterType.ANY_TEXT && to == FilterType.HEX) {
            return filterHexString(text, 0);
        } else if (from == FilterType.HEX && to == FilterType.ANY_TEXT) {
            return bytesToHexString(hexDumpToBytes(text));
        }
        return text;
    }

}
