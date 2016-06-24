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

package com.mucommander.ui.viewer.text;

import com.mucommander.cache.TextHistory;
import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.io.BufferPool;
import com.mucommander.commons.io.StreamUtils;
import com.mucommander.commons.io.bom.BOMInputStream;
import com.mucommander.text.Translator;
import com.mucommander.tools.AvrAssemblerCommandsHelper;
import com.mucommander.ui.theme.*;
import com.mucommander.ui.viewer.text.search.FindDialog;
import com.mucommander.ui.viewer.text.search.ReplaceDialog;
import com.mucommander.ui.viewer.text.search.SearchEvent;
import com.mucommander.ui.viewer.text.search.SearchListener;
import com.mucommander.ui.viewer.text.utils.CodeFormatException;
import com.mucommander.ui.viewer.text.utils.CodeFormatter;
import org.fife.ui.rtextarea.SearchContext;
import org.fife.ui.rtextarea.SearchEngine;
import org.fife.ui.rtextarea.SearchResult;

import javax.swing.JFrame;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.Document;
import java.awt.Color;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.io.*;
import java.util.StringTokenizer;

/**
 * Text editor implementation used by {@link TextViewer} and {@link TextEditor}.
 *
 * @author Maxence Bernard, Mariusz Jakubowski, Nicolas Rinaudo, Arik Hadas, Oleg Trifonov
 */
class TextEditorImpl implements ThemeListener {

    private static final Insets INSETS = new Insets(4, 3, 4, 3);

	JFrame frame;

    private TextArea textArea;

    private SearchContext searchContext;

	/** Indicates whether there is a line separator in the original file */
	private boolean lineSeparatorExists;

    private StatusBar statusBar;

    private CaretListener caretListener = new CaretListener() {
        @Override
        public void caretUpdate(CaretEvent e) {
            if (statusBar != null) {
                int line = textArea.getLine();
                int col = textArea.getColumn();
                statusBar.setPosition(line, col);

                // check if we have 6-digit hex-word on cursor (color)
                String str = textArea.getLineStr(line);
                if (str == null || str.isEmpty()) {
                    statusBar.setColor(-1);
                    if (textArea.getFileType() == FileType.ASSEMBLER_AVR) {
                        statusBar.setStatusMessage("");
                    }
                    return;
                }
                if (textArea.getFileType() == FileType.ASSEMBLER_AVR) {
                    StringTokenizer tokenizer = new StringTokenizer(str, " \t\n\r");
                    boolean found = false;
                    while (tokenizer.hasMoreElements()) {
                        String instruction = tokenizer.nextToken();
                        if (instruction.endsWith(":") || instruction.startsWith(";") || instruction.startsWith("//")) {
                            continue;
                        }
                        String description = AvrAssemblerCommandsHelper.getCommandDescription(instruction);
                        if (description != null) {
                            statusBar.setStatusMessage(description);
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        statusBar.setStatusMessage("");
                    }
                }
                checkColorOnCursor(str, col);
            }
        }
    };


    private boolean checkColorOnCursor(String str, int col) {
        if (str.length() < 6 || col >= str.length()) {
            return false;
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
                statusBar.setColor(Integer.parseInt(word, 16));
                return true;
            } else {
                statusBar.setColor(-1);
            }
        } else {
            statusBar.setColor(-1);
        }
        return false;
    }


    private static boolean isHexDigit(char ch) {
        return (ch >= '0' && ch <= '9') || (ch >= 'A' && ch <= 'F') || (ch >= 'a' && ch <= 'f');
    }


	////////////////////
	// Initialization //
	////////////////////

	TextEditorImpl(boolean isEditable, StatusBar statusBar) {
		// Initialize text area
        initTextArea(isEditable);
        this.statusBar = statusBar;

		// Listen to theme changes to update the text area if it is visible
		ThemeManager.addCurrentThemeListener(this);
	}

	private void initTextArea(boolean isEditable) {
        textArea = new TextArea() {
            @Override
            public Insets getInsets() {
                return INSETS;
            }
        };
        textArea.setCurrentLineHighlightColor(ThemeManager.getCurrentColor(Theme.EDITOR_CURRENT_BACKGROUND_COLOR));
        textArea.setAntiAliasingEnabled(true);
		textArea.setEditable(isEditable);

        try {
            ThemeManager.readEditorTheme(ThemeManager.getCurrentSyntaxThemeName()).apply(textArea);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //textArea.setCodeFoldingEnabled(true);

		// Use theme colors and font
		textArea.setForeground(ThemeManager.getCurrentColor(Theme.EDITOR_FOREGROUND_COLOR));
		textArea.setCaretColor(ThemeManager.getCurrentColor(Theme.EDITOR_FOREGROUND_COLOR));
        Color background = ThemeManager.getCurrentColor(Theme.EDITOR_BACKGROUND_COLOR);
        textArea.setBackground(background);

        for (int i = 1; i <= textArea.getSecondaryLanguageCount(); i++) {
            textArea.setSecondaryLanguageBackground(i, background);
        }
		textArea.setSelectedTextColor(ThemeManager.getCurrentColor(Theme.EDITOR_SELECTED_FOREGROUND_COLOR));
		textArea.setSelectionColor(ThemeManager.getCurrentColor(Theme.EDITOR_SELECTED_BACKGROUND_COLOR));
		textArea.setFont(ThemeManager.getCurrentFont(Theme.EDITOR_FONT));
        textArea.setCodeFoldingEnabled(true);

		textArea.setWrapStyleWord(true);

		textArea.addMouseWheelListener(e -> {
            boolean isCtrlPressed = (e.getModifiers() & KeyEvent.CTRL_MASK) != 0;
            if (isCtrlPressed) {
                Font currentFont = textArea.getFont();
                int currentFontSize = currentFont.getSize();
                boolean rotationUp = e.getWheelRotation() < 0;
                if ((!rotationUp && currentFontSize > 1) || rotationUp) {
                    Font newFont = new Font(currentFont.getName(), currentFont.getStyle(), currentFontSize + (rotationUp ? 1 : -1));
                    textArea.setFont(newFont);
                }
            } else {
                textArea.getParent().dispatchEvent(e);
            }
		});

        textArea.addCaretListener(caretListener);
	}

	/////////////////
	// Search code //
	/////////////////

    void find() {
        SearchListener searchListener = new SearchListener() {
            @Override
            public void searchEvent(SearchEvent e) {
                searchContext = e.getSearchContext();
                String searchString = searchContext.getSearchFor();
                TextHistory.getInstance().add(TextHistory.Type.TEXT_SEARCH, searchString, true);
                SearchResult result = SearchEngine.find(textArea, searchContext);
                if (!result.wasFound()) {
                    beep();
                    setStatusMessage(Translator.get("text_editor.text_not_found"));
                } else {
                    setStatusMessage(Translator.get("text_editor.found") + " " + result.getMarkedCount() + " " + Translator.get("text_editor.matches"));
                    textArea.setCaretPosition(textArea.getSelectionStart());
                }

                // Request the focus on the text area which could be lost after the Find dialog was disposed
                textArea.requestFocus();
            }

            @Override
            public String getSelectedText() {
                return textArea.getSelectedText();
            }
        };
        FindDialog dlg = new FindDialog(frame, searchListener);
        dlg.setSearchString(searchContext != null ? searchContext.getSearchFor() : "");
        dlg.showDialog();
	}


    void findMore(boolean forward) {
        if (searchContext == null) {
            beep();
            return;
        }
        if (!forward && textArea.getCaretPosition() == 0) {
            beep();
            return;
        }
        searchContext.setSearchForward(forward);
        int savedCaretPosition = textArea.getCaretPosition();
        try {
            int pos = textArea.getSelectionStart();
            if (forward) {
                pos += searchContext.getSearchFor().length();
            }
            textArea.setCaretPosition(pos);
        } catch (IllegalArgumentException ignore) {}
        SearchResult result = SearchEngine.find(textArea, searchContext);
        if (!result.wasFound()) {
            textArea.setCaretPosition(savedCaretPosition);
            setStatusMessage(Translator.get("text_editor.text_not_found"));
            beep();
        } else {
            setStatusMessage("");
            textArea.setCaretPosition(textArea.getSelectionStart());
        }
    }

	void findNext() {
        findMore(true);
	}

    void replace() {
        SearchListener searchListener = new SearchListener() {
            @Override
            public void searchEvent(SearchEvent e) {
                searchContext = e.getSearchContext();
                String searchString = searchContext.getSearchFor();
                TextHistory.getInstance().add(TextHistory.Type.TEXT_SEARCH, searchString, true);
                SearchResult result;
                switch (e.getType()) {
                    case FIND:
                        result = SearchEngine.find(textArea, searchContext);
                        break;
                    case REPLACE:
                        result = SearchEngine.replace(textArea, searchContext);
                        break;
                    case REPLACE_ALL:
                        result = SearchEngine.replaceAll(textArea, searchContext);
                        break;
                    default:
                        result = null;
                }
                if (result == null) {
                    return;
                }
                if (!result.wasFound()) {
                    beep();
                    setStatusMessage(Translator.get("text_editor.text_not_found"));
                } else {
                    if (e.getType() == SearchEvent.Type.REPLACE_ALL) {
                        setStatusMessage(Translator.get("text_editor.replaced") + " " + result.getCount() + " " + Translator.get("text_editor.occurrences"));
                    } else {
                        setStatusMessage(Translator.get("text_editor.found") + " " + result.getMarkedCount() + " " + Translator.get("text_editor.matches"));
                    }
                }
            }

            @Override
            public String getSelectedText() {
                return textArea.getSelectedText();
            }
        };
        ReplaceDialog dlg = new ReplaceDialog(frame, searchListener);
        dlg.setSearchString(searchContext != null ? searchContext.getSearchFor() : "");
        dlg.showDialog();
    }


	void findPrevious() {
        findMore(false);
		//search(textArea.getSelectionStart() - 1, false);
	}

    void gotoLine() {
        GotoLineDialog dlgGoto = new GotoLineDialog(frame, textArea.getLineCount()) {
            @Override
            protected void doGoto(int value) {
                textArea.gotoLine(value);
            }
        };
        dlgGoto.showDialog();
    }


	public boolean isWrap() {
		return textArea.getLineWrap();
	}

	////////////////////////////
	// Package-access methods //
	////////////////////////////

	void wrap(boolean isWrap) {
		textArea.setLineWrap(isWrap);
		textArea.repaint();
	}

	void copy() {
		textArea.copy();
	}

	void cut() {
		textArea.cut();
	}

	void paste() {
		textArea.paste();
	}

	void selectAll() {
		textArea.selectAll();
	}

    void undo() {
        textArea.undoLastAction();
    }

    void redo() {
        textArea.redoLastAction();
    }

	void requestFocus() {
		textArea.requestFocus();
	}

    TextArea getTextArea() {
		return textArea;
	}

	void addDocumentListener(DocumentListener documentListener) {
		textArea.getDocument().addDocumentListener(documentListener);
	}

	void read(Reader reader) throws IOException {
		// Feed the file's contents to text area
		textArea.read(reader, null);

		// If there are more than one lines, there is a line separator
		lineSeparatorExists = textArea.getLineCount() > 1;

		// Move cursor to the top
//		textArea.setCaretPosition(0);

	}

	void write(Writer writer) throws IOException {
		Document document = textArea.getDocument();

		// According to the documentation in DefaultEditorKit, the line separator is set to be as the system property
		// if no other line separator exists in the file, but in practice it is not, so this is a workaround for it
		if (!lineSeparatorExists)
			document.putProperty(DefaultEditorKit.EndOfLineStringProperty, System.getProperty("line.separator"));

		try {
			textArea.getUI().getEditorKit(textArea).write(new BufferedWriter(writer), document, 0, document.getLength());
		} catch(BadLocationException e) {
			throw new IOException(e.getMessage());
		}
	}

	//////////////////////////////////
	// ThemeListener implementation //
	//////////////////////////////////

	/**
	 * Receives theme color changes notifications.
	 */
	public void colorChanged(ColorChangedEvent event) {
		switch (event.getColorId()) {
            case Theme.EDITOR_FOREGROUND_COLOR:
                textArea.setForeground(event.getColor());
                break;

            case Theme.EDITOR_BACKGROUND_COLOR:
                textArea.setBackground(event.getColor());
                break;

            case Theme.EDITOR_SELECTED_FOREGROUND_COLOR:
                textArea.setSelectedTextColor(event.getColor());
                break;

            case Theme.EDITOR_SELECTED_BACKGROUND_COLOR:
                textArea.setSelectionColor(event.getColor());
                break;

            case Theme.EDITOR_CURRENT_BACKGROUND_COLOR:
                textArea.setCurrentLineHighlightColor(event.getColor());
                break;
		}
	}

	/**
	 * Receives theme font changes notifications.
	 */
	public void fontChanged(FontChangedEvent event) {
		if (event.getFontId() == Theme.EDITOR_FONT) {
            textArea.setFont(event.getFont());
        }
	}


    FileType detectFileFormat(AbstractFile file) {
        byte bytes[] = BufferPool.getByteArray(256);
        int readBytes;
        try {
            PushbackInputStream is = file.getPushBackInputStream(256);
            BOMInputStream bomIs = new BOMInputStream(is);
            readBytes = StreamUtils.readUpTo(bomIs, bytes);
            is.unread(bytes, 0, readBytes);
        } catch (IOException e) {
            e.printStackTrace();
            try {
                file.closePushbackInputStream();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            return FileType.NONE;
        }
        if (readBytes < 5) {
            return FileType.NONE;
        }

        String str = new String(bytes, 0, readBytes).trim().toLowerCase();
        if (str.startsWith("<?xml")) {
            return FileType.XML;
        } else if (str.startsWith("<?php")) {
            return FileType.PHP;
        } else if (str.startsWith("#!/usr/bin/python")) {
            return FileType.PYTHON;
        } else if (str.startsWith("#!/bin/bash") || str.startsWith("#!/bin/sh") || str.startsWith("#!/usr/bin/env bash")) {
            return FileType.UNIX_SHELL;
        } else if (str.startsWith("<!DOCTYPE html")) {
            return FileType.HTML;
        } else if (str.startsWith("#!/usr/bin/ruby") || str.startsWith("#!/usr/bin/env ruby")) {
            return FileType.RUBY;
        }
        return FileType.NONE;
    }


    private void formatTextArea() throws CodeFormatException {
        String src = textArea.getText();
        String formatted = null;

        switch (textArea.getFileType()) {
            case XML:
                formatted = CodeFormatter.formatXml(src);
                break;
            case JSON:
                formatted = CodeFormatter.formatJson(src);
                break;
        }
        if (formatted != null) {
            textArea.setText(formatted);
        }

    }

    void formatCode() {
        try {
            formatTextArea();
            setStatusMessage("");
        } catch (CodeFormatException e) {
            if (e.getLine() > 0 ) {
                if (e.getRow() > 0) {
                    textArea.gotoLine(e.getLine(), e.getRow());
                } else {
                    textArea.gotoLine(e.getLine());
                }
            }
            setStatusMessage(e.getLocalizedMessage());
        } catch (Exception e) {
            setStatusMessage(Translator.get("error"));
        }
    }

    public StatusBar getStatusBar() {
        return statusBar;
    }

    public void setStatusBar(StatusBar statusBar) {
        this.statusBar = statusBar;
    }

    void setSyntaxType(FileType fileType) {
        textArea.setFileType(fileType);
        if (statusBar != null) {
            statusBar.setSyntax(fileType.getName());
        }
    }

    void setStatusMessage(String message) {
        if (getStatusBar() != null) {
            getStatusBar().setStatusMessage(message);
        }
    }

    private void beep() {
        // The beep method is called from a separate thread because this method seems to lock until the beep has
        // been played entirely. If the 'Find next' shortcut is left pressed, a series of beeps will be played when
        // the end of the file is reached, and we don't want those beeps to played one after the other as to:
        // 1/ not lock the event thread
        // 2/ have those beeps to end rather sooner than later
        new Thread() {
            @Override
            public void run() {
                Toolkit.getDefaultToolkit().beep();
            }
        }.start();
    }


    public void setupSearchContext(String searchStr) {
        searchContext = new SearchContext(searchStr);
    }

}
