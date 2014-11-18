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
package ru.trolsoft.ui;

import ru.trolsoft.utils.StrUtils;

import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.EventQueue;
import java.awt.Toolkit;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * Input filed for text and hex values
 */
public class InputField extends JTextField implements DocumentListener {

    public enum FilterType {
        ANY_TEXT,
        HEX_DUMP,
        HEX_LONG,
        DEC_LONG
    }

    private static final String HEX_SYMBOLS = "0123456789ABCDEF";

    private static final String DEFAULT_ENCODING = "windows-1252";

    private FilterType filterType = FilterType.HEX_DUMP;

    private int maxLength = 0xff;

    private boolean filtering = false;

    private String textEncoding;

    private List<InputField> assignedFields;

    public InputField() {
        super();
        init();
    }

    public InputField(int columns) {
        super(columns);
        init();
    }

    public InputField(int columns, FilterType filterType) {
        super(columns);
        this.filterType = filterType;
        init();
    }

    private void init() {
        getDocument().addDocumentListener(this);
        this.textEncoding = DEFAULT_ENCODING;
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
        if (filtering) {
            return;
        }
        // don't filter text value
        if (filterType == null || filterType == FilterType.ANY_TEXT) {
            onChange();
            updateAssignedFields();
            return;
        }
        filtering = true;

        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                String input = getText().toUpperCase();
                String filtered = filterInput(input, maxLength);
                setText(filtered);
                updateAssignedFields();
                onChange();
                filtering = false;
            }
        });
    }



    private String filterInput(String input, int maxLength) {
        switch (filterType) {
            case HEX_DUMP:
                return filterHexString(input, maxLength);
            case DEC_LONG:
                return filterDecLong(input);
            case HEX_LONG:
                return filterHexLong(input);
        }
        return input;
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
                beep();
            }
        }
        return filtered.toString();
    }


    private String filterDecLong(String input) {
        if (input == null) {
            return null;
        }
        StringBuilder filtered = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            char ch = input.charAt(i);
            if (Character.isDigit(ch)) {
                filtered.append(ch);
            }
        }
        if (filtered.length() != input.length()) {
            beep();
        }
        return filtered.toString();
    }

    private String filterHexLong(String input) {
        if (input == null) {
            return null;
        }
        StringBuilder filtered = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            char ch = input.charAt(i);
            if (HEX_SYMBOLS.indexOf(ch) >= 0) {
                filtered.append(ch);
            }
        }
        if (filtered.length() != input.length()) {
            beep();
        }
        return filtered.toString();
    }



    private static void beep() {
        Toolkit.getDefaultToolkit().beep();
    }



    public byte[] getBytes() {
        return StrUtils.hexStringToBytes(getText());
    }

    public void setBytes(byte[] bytes) {
        if (bytes == null) {
            setText("");
        } else {
            setText(StrUtils.bytesToHexStr(bytes, 0, bytes.length));
        }
    }

    public long getValue() {
        switch (filterType) {
            case DEC_LONG:
                return Long.parseLong(getText());
            case HEX_LONG:
                return Long.parseLong(getText(), 16);
        }
        return 0;
    }

    public void setValue(long val) {
        setText(Long.toString(val));
    }


    public void assignField(InputField field) {
        if (assignedFields == null) {
            assignedFields = new ArrayList<>();
        }
        assignedFields.add(field);
    }

    private void setTextWithoutFilter(String text) {
        filtering = true;
        setText(text);
        filtering = false;
    }


    private void updateAssignedFields() {
        if (assignedFields == null) {
            return;
        }
        final String src = getText();
        for (InputField field : assignedFields) {
            String text;
            try {
                text = convert(src, filterType, field.getFilterType());
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                text = "";
            }
            if (!text.equals(field.getText())) {
                field.setTextWithoutFilter(text);
            }
        }
    }


    private String convert(String text, FilterType from, FilterType to) throws UnsupportedEncodingException {
        if (from == to) {
            return text;
        }
        if (from == FilterType.ANY_TEXT && to == FilterType.HEX_DUMP) {
            return StrUtils.bytesToHexString(text.getBytes(textEncoding));
        } else if (from == FilterType.HEX_DUMP && to == FilterType.ANY_TEXT) {
            return new String(StrUtils.hexStringToBytes(text), textEncoding);
        }
        return text;
    }

    public String getTextEncoding() {
        return textEncoding;
    }

    public void setTextEncoding(String textEncoding) {
        this.textEncoding = textEncoding;
    }


    public boolean isEmpty() {
        return getText().isEmpty();
    }

    public void onChange() {

    }


}
