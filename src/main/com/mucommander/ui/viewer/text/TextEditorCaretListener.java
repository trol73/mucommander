/*
 * This file is part of trolCommander, http://www.trolsoft.ru/en/soft/trolcommander
 * Copyright (C) 2013-2017 Oleg Trifonov
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
package com.mucommander.ui.viewer.text;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.tools.AvrAssemblerCommandsHelper;
import com.mucommander.utils.text.Translator;

import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import java.io.IOException;
import java.util.StringTokenizer;

public class TextEditorCaretListener implements CaretListener {
    final TextEditorImpl textEditor;

    TextEditorCaretListener(TextEditorImpl textEditor) {
        this.textEditor = textEditor;
    }

    @Override
    public void caretUpdate(CaretEvent e) {
        StatusBar statusBar = textEditor.getStatusBar();
        TextArea textArea = textEditor.getTextArea();
        if (statusBar == null) {
            return;
        }
        int line = textArea.getLine();
        int col = textArea.getColumn();
        statusBar.setPosition(line, col);
        statusBar.setStatusMessage("");

        // check if we have 6-digit hex-word on cursor (color)
        String str = textArea.getLineStr(line);
        if (str == null || str.isEmpty()) {
            statusBar.setColor(-1);
            //if (textArea.getFileType() == FileType.ASSEMBLER_AVR) {
            textEditor.selectIncludeFile(null);
            //}
            return;
        }
        checkAssemblerInstruction(str);
        checkColorOnCursor(str, col);
        checkIncludeInstruction(str, col);
    }

    private void checkIncludeInstruction(String str, int col) {
        if (!str.toLowerCase().contains("include")) {
            textEditor.selectIncludeFile(null);
            return;
        }
        if (col > 0) {
            col--;
        }
        try {
            int lastQuote2 = str.indexOf('"', col);
            int lastQuote1 = str.indexOf('\'', col);
            int lastBracket = str.indexOf(">", col);
            String quotedName = null;
            if (lastQuote2 > 0) {
                int firstQuote2 = str.lastIndexOf('"', col);
                if (firstQuote2 > 0) {
                    quotedName = str.substring(firstQuote2 + 1, lastQuote2);
                    if (quotedName.trim().isEmpty()) {
                        quotedName = null;
                    }
                }
            }
            if (quotedName == null && lastBracket > 0) {
                int firstBracket = str.lastIndexOf('<', col);
                if (firstBracket > 0) {
                    quotedName = str.substring(firstBracket + 1, lastBracket);
                    if (quotedName.trim().isEmpty()) {
                        quotedName = null;
                    }
                }
            }
            if (quotedName == null && lastQuote1 > 0) {
                int firstQuote1 = str.lastIndexOf('\'', col);
                if (firstQuote1 > 0) {
                    quotedName = str.substring(firstQuote1 + 1, lastBracket);
                    if (quotedName.trim().isEmpty()) {
                        quotedName = null;
                    }
                }
            }
            AbstractFile includeFile = getIncludeFile(quotedName);
            if (includeFile != null) {
                setStatusMessage("<html>" + Translator.get("text_editor.press_alt_enter_to_open_file") + " <b>" + quotedName + "</b>");
                textEditor.selectIncludeFile(includeFile);
                return;
            }
        } catch (StringIndexOutOfBoundsException ignore) {}
        setStatusMessage(null);
    }

    private void checkAssemblerInstruction(String str) {
        if (!isAvrAssembler()) {
            return;
        }
        StringTokenizer tokenizer = new StringTokenizer(str, " \t\n\r");
        boolean found = false;
        while (tokenizer.hasMoreElements()) {
            String instruction = tokenizer.nextToken();
            if (instruction.endsWith(":") || instruction.startsWith(";") || instruction.startsWith("//")) {
                continue;
            }
            String description = AvrAssemblerCommandsHelper.getCommandDescription(instruction);
            if (description != null) {
                setStatusMessage(description);
                found = true;
                break;
            }
        }
        if (!found) {
            setStatusMessage("");
        }
    }


    private void checkColorOnCursor(String str, int col) {
        if (str.length() < 6 || col >= str.length()) {
            clearStatusColor();
            return;
        }
        char ch = str.charAt(col);
        if (isHexDigit(ch)) {
            String word = "" + ch;
            for (int pos = col-1; pos >= 0; pos--) {
                char c = str.charAt(pos);
                if (isHexDigit(c)) {
                    word = c + word;
                } else {
                    break;
                }
            }
            for (int pos = col+1; pos < str.length(); pos++) {
                char c = str.charAt(pos);
                if (isHexDigit(c)) {
                    word = word + c;
                } else {
                    break;
                }
            }
            if (word.length() == 6) {
                try {
                    setStatusColor(Integer.parseInt(word, 16));
                    return;
                } catch (Exception ignore) { }
            }
        }
        clearStatusColor();
    }


    private static boolean isHexDigit(char ch) {
        return (ch >= '0' && ch <= '9') || (ch >= 'A' && ch <= 'F') || (ch >= 'a' && ch <= 'f');
    }

    private void setStatusMessage(String msg) {
        StatusBar statusBar = textEditor.getStatusBar();
        if (statusBar != null) {
            statusBar.setStatusMessage(msg);
        }
    }

    private void setStatusColor(int color) {
        StatusBar statusBar = textEditor.getStatusBar();
        if (statusBar != null) {
            statusBar.setColor(color);
        }
    }

    private void clearStatusColor() {
        setStatusColor(-1);
    }

    private boolean isAvrAssembler() {
        return textEditor.getTextArea().getFileType() == FileType.ASSEMBLER_AVR;
    }

    private AbstractFile getIncludeFile(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return null;
        }
        try {
            AbstractFile selectedFile = textEditor.getFile().getParent().getChild(fileName);
            if (selectedFile != null && selectedFile.exists()) {
                return selectedFile;
            }
        } catch (IOException ignore) {}
        return null;
    }

}
