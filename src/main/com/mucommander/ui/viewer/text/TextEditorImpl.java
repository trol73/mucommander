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

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.io.BufferPool;
import com.mucommander.commons.io.StreamUtils;
import com.mucommander.ui.theme.*;
import com.mucommander.ui.viewer.text.utils.CodeFormatter;

import javax.swing.JFrame;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.Document;
import java.awt.Color;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.*;

/**
 * Text editor implementation used by {@link TextViewer} and {@link TextEditor}.
 *
 * @author Maxence Bernard, Mariusz Jakubowski, Nicolas Rinaudo, Arik Hadas
 */
class TextEditorImpl implements ThemeListener {

    private static final Insets INSETS = new Insets(4, 3, 4, 3);

	private String searchString;

	private JFrame frame;

    private TextArea textArea;

    private GotoLineDialog dlgGoto;

	/** Indicates whether there is a line separator in the original file */
	private boolean lineSeparatorExists;

	////////////////////
	// Initialization //
	////////////////////

	public TextEditorImpl(boolean isEditable) {
		// Initialize text area
		initTextArea(isEditable);

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

		textArea.addMouseWheelListener(new MouseWheelListener() {

			/**
			 * Mouse events bubble up until finding a component with a relative listener.
			 * That's why in case we get an event that needs to initiate its default behavior,
			 * we just bubble it up to the parent component of the JTextArea.  
			 */
			public void mouseWheelMoved(MouseWheelEvent e) {
				boolean isCtrlPressed = (e.getModifiers() & KeyEvent.CTRL_MASK) != 0;
				if (isCtrlPressed) {
					Font currentFont = textArea.getFont();
					int currentFontSize = currentFont.getSize();
					boolean rotationUp = e.getWheelRotation() < 0;
					if ((!rotationUp && currentFontSize > 1) || rotationUp) {
						Font newFont = new Font(currentFont.getName(), currentFont.getStyle(), currentFontSize + (rotationUp ? 1 : -1));
						textArea.setFont(newFont);
					}
				}
				else {
					textArea.getParent().dispatchEvent(e);
				}
			}
		});
	}

	/////////////////
	// Search code //
	/////////////////

    void find() {
		FindDialog dlgFind = new FindDialog(frame) {
            @Override
            protected void doSearch(String text) {
                if (text != null && text.length() > 0) {
                    searchString = getSearchString().toLowerCase();

                    if (!searchString.isEmpty()) {
                        search(0, true);
                    }
                }
                // Request the focus on the text area which could be lost after the Find dialog was disposed
                textArea.requestFocus();
            }
        };
        dlgFind.setText(searchString);
        dlgFind.showDialog();
	}

	void findNext() {
		search(textArea.getSelectionEnd(), true);
	}

	void findPrevious() {
		search(textArea.getSelectionStart() - 1, false);
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

	private String getTextLC() {
		return textArea.getText().toLowerCase();
	}

	private void search(int startPos, boolean forward) {
		if (searchString == null || searchString.length() == 0)
			return;
		int pos;
		if (forward) {
			pos = getTextLC().indexOf(searchString, startPos);
		} else {
			pos = getTextLC().lastIndexOf(searchString, startPos);
		}
		if (pos >= 0) {
			textArea.select(pos, pos + searchString.length());
		} else {
			// Beep when no match has been found.
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


    public boolean isXmlFile(AbstractFile file) {
        byte bytes[] = BufferPool.getByteArray(256);
        int readBytes;
        try {
            PushbackInputStream is = file.getPushBackInputStream(256);
            readBytes = StreamUtils.readUpTo(is, bytes);
            is.unread(bytes, 0, readBytes);
        } catch (IOException e) {
            e.printStackTrace();
            try {
                file.closePushbackInputStream();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            return false;
        }
        if (readBytes < 5) {
            return false;
        }

        String str = new String(bytes, 0, readBytes).trim().toLowerCase();
        return str.startsWith("<?xml");
    }


    public void formatCode() {
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

}
