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

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.*;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Stack;

import javax.swing.*;
import javax.swing.event.DocumentListener;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.io.EncodingDetector;
import com.mucommander.commons.io.bom.BOMInputStream;
import com.mucommander.conf.MuConfigurations;
import com.mucommander.conf.MuSnapshot;
import com.mucommander.text.Translator;
import com.mucommander.ui.dialog.DialogOwner;
import com.mucommander.ui.dialog.InformationDialog;
import com.mucommander.ui.encoding.EncodingListener;
import com.mucommander.ui.encoding.EncodingMenu;
import com.mucommander.ui.viewer.FileFrame;
import com.mucommander.ui.viewer.FileViewer;
import org.fife.ui.rtextarea.GutterEx;


/**
 * A simple text viewer. Most of the implementation is located in {@link TextEditorImpl}.
 *
 * @author Maxence Bernard, Arik Hadas
 */
public class TextViewer extends FileViewer implements EncodingListener {

	public final static String CUSTOM_FULL_SCREEN_EVENT = "CUSTOM_FULL_SCREEN_EVENT";

	private TextEditorImpl textEditorImpl;

	private static boolean lineWrap = MuConfigurations.getSnapshot().getVariable(MuSnapshot.TEXT_FILE_PRESENTER_LINE_WRAP, MuSnapshot.DEFAULT_LINE_WRAP);

	private static boolean lineNumbers = MuConfigurations.getSnapshot().getVariable(MuSnapshot.TEXT_FILE_PRESENTER_LINE_NUMBERS, MuSnapshot.DEFAULT_LINE_NUMBERS);

    protected TextMenuHelper menuHelper;

    private String encoding;

    private TextFilesHistory.FileRecord historyRecord;
    private GutterEx gutter;
    private StatusBar statusBar;


    TextViewer() {
    	this(new TextEditorImpl(false, null));
        textEditorImpl.setStatusBar(getStatusBar());
    }
    
    TextViewer(TextEditorImpl textEditorImpl) {
    	this.textEditorImpl = textEditorImpl;

        initGutter();

        setComponentToPresent(textEditorImpl.getTextArea());

        showLineNumbers(lineNumbers);
    	textEditorImpl.wrap(lineWrap);

    	initMenuBarItems();
    }


    private void initGutter() {
//        Font defaultFont = new Font("Monospaced", Font.PLAIN, 12);
        gutter = new GutterEx(textEditorImpl.getTextArea());
        gutter.setLineNumberFont(textEditorImpl.getTextArea().getFont());
        // TODO
        gutter.setBackground(Color.LIGHT_GRAY);
        gutter.setForeground(Color.black);
//gutter.setActiveLineRangeColor(new Color(0,0,255));
        showLineNumbers(lineNumbers);

        // Set miscellaneous properties.
        setVerticalScrollBarPolicy(VERTICAL_SCROLLBAR_ALWAYS);
        setHorizontalScrollBarPolicy(HORIZONTAL_SCROLLBAR_AS_NEEDED);
    }

    @Override
    public void setFrame(final FileFrame frame) {
        super.setFrame(frame);

        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_M, InputEvent.CTRL_MASK), CUSTOM_FULL_SCREEN_EVENT);
    }


	static void setLineWrap(boolean lineWrap) {
		TextViewer.lineWrap = lineWrap;
	}

	public static boolean isLineWrap() {
		return lineWrap;
	}

	static void setLineNumbers(boolean lineNumbers) {
		TextViewer.lineNumbers = lineNumbers;
	}

	public static boolean isLineNumbers() {
		return lineNumbers;
	}

    void startEditing(AbstractFile file, DocumentListener documentListener) throws IOException {
        //initHistoryRecord(file);
        // Auto-detect encoding
        try (PushbackInputStream in = file.getPushBackInputStream(EncodingDetector.MAX_RECOMMENDED_BYTE_SIZE)) {
            String encoding = historyRecord.getEncoding() != null ? historyRecord.getEncoding() : EncodingDetector.detectEncoding(in);
            if (textEditorImpl.getStatusBar() != null) {
                textEditorImpl.getStatusBar().setEncoding(encoding);
            }
            // Load the file into the text area
            loadDocument(in, encoding, documentListener);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    void loadDocument(InputStream in, final String encoding, DocumentListener documentListener) throws IOException {
        // If the encoding is UTF-something, wrap the stream in a BOMInputStream to IMAGE_FILTER out the byte-order mark
        // (see ticket #245)
        if (encoding != null && encoding.toLowerCase().startsWith("utf")) {
            in = new BOMInputStream(in);
        }

        // If the given encoding is invalid (null or not supported), default to "UTF-8" 
        this.encoding = encoding == null || !Charset.isSupported(encoding) ? "UTF-8" : encoding;
        if (getStatusBar() != null) {
            getStatusBar().setEncoding(encoding);
        }
        textEditorImpl.read(new BufferedReader(new InputStreamReader(in, this.encoding)));

        // Listen to document changes
        if (documentListener != null) {
            textEditorImpl.addDocumentListener(documentListener);
        }
    }
    
    @Override
    public JMenuBar getMenuBar() {
    	JMenuBar menuBar = super.getMenuBar();
    	
    	// Encoding menu
    	EncodingMenu encodingMenu = new EncodingMenu(new DialogOwner(getFrame()), encoding);
        encodingMenu.addEncodingListener(this);

        menuBar.add(menuHelper.getEditMenu());
        menuBar.add(menuHelper.getSearchMenu());
        menuBar.add(menuHelper.getMenuView());
        menuBar.add(encodingMenu, menuBar);

        setMainKeyListener(textEditorImpl.getTextArea(), menuBar);
        return menuBar;
    }

    @Override
    protected StatusBar getStatusBar() {
        if (statusBar == null) {
            statusBar = new StatusBar();
        }
        return statusBar;
    }

    public void saveState(JScrollBar scrollBar) {
        final TextArea textArea = textEditorImpl.getTextArea();
        historyRecord.setLine(textArea.getLine());
        historyRecord.setColumn(textArea.getColumn());
        historyRecord.setFileType(textArea.getFileType());
        historyRecord.setScrollPosition(scrollBar.getValue());
        historyRecord.setEncoding(encoding);
        TextFilesHistory.getInstance().updateRecord(historyRecord).save();
    }

    @Override
    protected void saveStateOnClose() {
        saveState(getVerticalScrollBar());
        try {
            AbstractFile currentFile = getCurrentFile();
            if (currentFile != null) {
                currentFile.closePushbackInputStream();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void restoreStateOnStartup() {
        final TextArea textArea = textEditorImpl.getTextArea();
        textArea.gotoLine(historyRecord.getLine(), historyRecord.getColumn());
        getViewport().setViewPosition(new java.awt.Point(0, historyRecord.getScrollPosition()));
    }

    String getEncoding() {
    	return encoding;
    }
    
    protected void showLineNumbers(boolean show) {
    	//setRowHeaderView(show ? new TextLineNumbersPanel(textEditorImpl.getTextArea()) : null);
        gutter.setLineNumbersEnabled(show);
        checkGutterVisibility();
        setLineNumbers(show);
    }

    protected void wrapLines(boolean wrap) {
    	textEditorImpl.wrap(wrap);
    	setLineWrap(wrap);
    }

    protected void initMenuBarItems() {
        menuHelper = new TextMenuHelper(textEditorImpl, false);
        //menuHelper.initMenu(TextViewer.this, getRowHeader().getView() != null);
        menuHelper.initMenu(TextViewer.this, lineNumbers);
    }

    ///////////////////////////////
    // FileViewer implementation //
    ///////////////////////////////

    @Override
    public void show(AbstractFile file) throws IOException {
        initHistoryRecord(file);
        FileType type = historyRecord.getFileType();
        if (type == null) {
            type = FileType.getFileType(file);
            historyRecord.setFileType(type);
        }
        // detect XML and PHP files
        if (type == FileType.NONE) {
            type = textEditorImpl.detectFileFormat(file);
        }
        startEditing(file, null);
        menuHelper.setSyntax(type);
        textEditorImpl.setSyntaxType(type);
    }
    
    ///////////////////////////////////
    // ActionListener implementation //
    ///////////////////////////////////

    public void actionPerformed(ActionEvent e) {
        if (menuHelper.performAction(e, this)) {
            return;
        }
      	super.actionPerformed(e);
    }

    /////////////////////////////////////
    // EncodingListener implementation //
    /////////////////////////////////////

    public void encodingChanged(Object source, String oldEncoding, String newEncoding) {
        // Store caret and scrollbar position before change
        TextArea textArea = textEditorImpl.getTextArea();
        int line = textArea.getLine();
        int column = textArea.getColumn();
        int horizontalPos = getHorizontalScrollBar().getValue();
        int verticalPos = getVerticalScrollBar().getValue();

        try {
    		// Reload the file using the new encoding
    		// Note: loadDocument closes the InputStream
    		loadDocument(getCurrentFile().getInputStream(), newEncoding, null);
            // Restore caret and scrollbar
            textArea.gotoLine(line, column);
            getViewport().setViewPosition(new java.awt.Point(horizontalPos, verticalPos));
    	} catch (IOException ex) {
    		InformationDialog.showErrorDialog(getFrame(), Translator.get("read_error"), Translator.get("file_editor.cannot_read_file", getCurrentFile().getName()));
    	}   
    }

    public TextFilesHistory.FileRecord initHistoryRecord(AbstractFile file) {
        historyRecord = TextFilesHistory.getInstance().get(file);
        return historyRecord;
    }


    public TextFilesHistory.FileRecord getHistoryRecord() {
        return historyRecord;
    }


    /**
     * Ensures the gutter is visible if it's showing anything.
     */
    private void checkGutterVisibility() {
        int count = gutter.getComponentCount();
        if (count == 0) {
            if (getRowHeader() != null && getRowHeader().getView() == gutter) {
                setRowHeaderView(null);
            }
        } else {
            if (getRowHeader() == null || getRowHeader().getView() == null) {
                setRowHeaderView(gutter);
            }
        }
    }


    /**
     * Returns the first descendant of a component that is an
     * <code>RTextArea</code>.  This is primarily here to support
     * <code>javax.swing.JLayer</code>s that wrap <code>RTextArea</code>s.
     *
     * @param comp The component to recursively look through.
     * @return The first descendant text area, or <code>null</code> if none
     *         is found.
     */
    private static TextArea getFirstRTextAreaDescendant(Component comp) {
        Stack<Component> stack = new Stack<>();
        stack.add(comp);
        while (!stack.isEmpty()) {
            Component current = stack.pop();
            if (current instanceof TextArea) {
                return (TextArea)current;
            }
            if (current instanceof Container) {
                Container container = (Container)current;
                stack.addAll(Arrays.asList(container.getComponents()));
            }
        }
        return null;
    }


    /**
     * Sets the view for this scroll pane.  This must be an {@link TextArea}.
     *
     * @param view The new view.
     */
    @Override
    public void setViewportView(Component view) {
        TextArea rtaCandidate;

        if (!(view instanceof TextArea)) {
            rtaCandidate = getFirstRTextAreaDescendant(view);
            if (rtaCandidate == null) {
                throw new IllegalArgumentException("view must be either an RTextArea or a JLayer wrapping one");
            }
        } else {
            rtaCandidate = (TextArea)view;
        }
        super.setViewportView(view);
        if (gutter != null) {
            gutter.setTextArea(rtaCandidate);
        }
    }

    @Override
    public void setSearchedText(String searchedText) {
        textEditorImpl.setupSearchContext(searchedText);
    }


    @Override
    public void setSearchedBytes(byte[] searchedBytes) {
        try {
            textEditorImpl.setupSearchContext(new String(searchedBytes, encoding));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
}
