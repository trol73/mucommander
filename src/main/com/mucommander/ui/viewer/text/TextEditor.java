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
import java.util.Arrays;
import java.util.Stack;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.fife.ui.rtextarea.Gutter;
import org.fife.ui.rtextarea.GutterEx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.FileOperation;
import com.mucommander.utils.text.Translator;
import com.mucommander.ui.dialog.DialogOwner;
import com.mucommander.ui.dialog.InformationDialog;
import com.mucommander.ui.encoding.EncodingListener;
import com.mucommander.ui.encoding.EncodingMenu;
import com.mucommander.ui.viewer.FileEditor;
import com.mucommander.ui.viewer.FileFrame;


/**
 * A simple text editor.
 *
 * @author Maxence Bernard, Nicolas Rinaudo, Arik Hadas
 */
public class TextEditor extends FileEditor implements DocumentListener, EncodingListener {
	private static final Logger LOGGER = LoggerFactory.getLogger(TextEditor.class);

    //private TextMenuHelper menuHelper;
    private TextEditorImpl textEditorImpl;
    private TextViewer textViewerDelegate;
    private StatusBar statusBar;

    private GutterEx gutter;


    TextEditor() {
        textEditorImpl = new TextEditorImpl(true, getStatusBar());
//        Font defaultFont = new Font("Monospaced", Font.PLAIN, 12);
        gutter = new GutterEx(textEditorImpl.getTextArea());
        gutter.setLineNumberFont(textEditorImpl.getTextArea().getFont());
        // TODO
        gutter.setBackground(Color.LIGHT_GRAY);
        gutter.setForeground(Color.black);
        //gutter.setActiveLineRangeColor(new Color(0,0,255));
        setLineNumbersEnabled(TextViewer.isLineNumbers());

        // Set miscellaneous properties.
        setVerticalScrollBarPolicy(VERTICAL_SCROLLBAR_ALWAYS);
        setHorizontalScrollBarPolicy(HORIZONTAL_SCROLLBAR_AS_NEEDED);

    	textViewerDelegate = new TextViewer(textEditorImpl) {
    		
    		@Override
            public void setComponentToPresent(JComponent component) {
    			TextEditor.this.setComponentToPresent(component);
    		}
    		
    		@Override
    		protected void showLineNumbers(boolean show) {
                setLineNumbersEnabled(show);
                TextViewer.setLineNumbers(show);
    			//TextEditor.this.setRowHeaderView(show ? new TextLineNumbersPanel(textEditorImpl.getTextArea()) : null);
    	    }
    		
    		@Override
    		protected void initMenuBarItems() {
                menuHelper = new TextMenuHelper(textEditorImpl, true);
                //menuHelper.initMenu(TextEditor.this, TextEditor.this.getRowHeader().getView() != null);
                menuHelper.initMenu(TextEditor.this, TextViewer.isLineNumbers());
                textEditorImpl.setMenuHelper(menuHelper);
    		}
    	};

        //setComponentToPresent(textEditorImpl.getTextArea());
        setComponentToPresent(textEditorImpl.getEditorComponent());
    }

    @Override
    public void setComponentToPresent(JComponent component) {
		getViewport().add(component);
	}

    private void loadDocument(InputStream in, String encoding, DocumentListener documentListener) throws IOException {
    	textViewerDelegate.loadDocument(in, encoding, documentListener);
        if (getStatusBar() != null) {
            getStatusBar().setEncoding(encoding);
        }
    }
    
    private void write(OutputStream out) throws IOException {
    	//textEditorImpl.write(new BOMWriter(out, textViewerDelegate.getEncoding()));
        textEditorImpl.write(new OutputStreamWriter(out, textViewerDelegate.getEncoding()));
    }

    @Override
    public JMenuBar getMenuBar() {
    	JMenuBar menuBar = super.getMenuBar();

    	// Encoding menu
        EncodingMenu encodingMenu = new EncodingMenu(new DialogOwner(getFrame()), textViewerDelegate.getEncoding());
        encodingMenu.addEncodingListener(this);

        menuBar.add(textViewerDelegate.menuHelper.getEditMenu());
        menuBar.add(textViewerDelegate.menuHelper.getSearchMenu());
        menuBar.add(textViewerDelegate.menuHelper.getViewMenu());
        menuBar.add(textViewerDelegate.menuHelper.getToolsMenu());
        menuBar.add(encodingMenu);

        textEditorImpl.getTextArea().setFocusTraversalKeysEnabled(false);
        textViewerDelegate.setMainKeyListener(textEditorImpl.getTextArea(), menuBar);
    	return menuBar;
    }

    @Override
    protected StatusBar getStatusBar() {
        if (statusBar == null) {
            statusBar = new StatusBar();
        }
        return statusBar;
    }

    @Override
    protected void saveStateOnClose() {
        textViewerDelegate.saveState(getVerticalScrollBar());
        if (getCurrentFile() != null) { // possible if loading was interrupted by Esc
            try {
                getCurrentFile().closePushbackInputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void restoreStateOnStartup() {
        final TextArea textArea = textEditorImpl.getTextArea();
        final TextFilesHistory.FileRecord historyRecord = textViewerDelegate.getHistoryRecord();
        getViewport().setViewPosition(new java.awt.Point(0, historyRecord.getScrollPosition()));
        textArea.gotoLine(historyRecord.getLine(), historyRecord.getColumn());
    }


    ///////////////////////////////
    // FileEditor implementation //
    ///////////////////////////////

    @Override
    protected void saveAs(AbstractFile destFile) throws IOException {
//        OutputStream out = null;
        getStatusBar().setStatusMessage(Translator.get("text_editor.writing"));

        //boolean error;
        try (OutputStream out = destFile.getOutputStream()) {
            write(out);
        } catch (Throwable e) {
            getStatusBar().setStatusMessage(Translator.get("text_editor.cant_save_file"));
            e.printStackTrace();
            return;
        }
        // We get here only if the destination file was updated successfully
        // so we can set that no further save is needed at this stage 
        setSaveNeeded(false);

        // Change the parent folder's date to now, so that changes are picked up by folder auto-refresh (see ticket #258)
        if (destFile.isFileOperationSupported(FileOperation.CHANGE_DATE)) {
            try {
                destFile.getParent().setLastModifiedDate(System.currentTimeMillis());
            } catch (IOException e) {
                LOGGER.debug("failed to change the date of "+destFile, e);
                // Fail silently
            }
        }
        getStatusBar().setStatusMessage(Translator.get("text_editor.saved"));
    }

    @Override
    public void setFrame(final FileFrame frame) {
    	super.setFrame(frame);
    	textEditorImpl.setFrame(frame);
    	//frame.setFullScreen(TextViewer.isFullScreen());

    	getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_M, InputEvent.CTRL_MASK), CUSTOM_FULL_SCREEN_EVENT);
//    	getActionMap().put(CUSTOM_FULL_SCREEN_EVENT, new AbstractAction() {
//    		public void actionPerformed(ActionEvent e){
//    			TextViewer.setFullScreen(!frame.isFullScreen());
//    			frame.setFullScreen(TextViewer.isFullScreen());
//    		}
//    	});
    }

    @Override
    public void show(AbstractFile file) throws IOException {
        TextArea textArea = textEditorImpl.getTextArea();
        textArea.discardAllEdits();
        textViewerDelegate.menuHelper.updateEditActions();

        TextFilesHistory.FileRecord historyRecord = textViewerDelegate.initHistoryRecord(file);
        FileType type = historyRecord.getFileType();
        if (type == null) {
            type = FileType.getFileType(file);
            historyRecord.setFileType(type);
        }
        if (type == FileType.NONE) {
            type = TextEditorUtils.detectFileFormat(file);
        }
        textEditorImpl.prepareForEdit(file);
        textEditorImpl.setSyntaxType(type);
        textViewerDelegate.menuHelper.setSyntax(type);
    	textViewerDelegate.startEditing(file, this);
        textArea.discardAllEdits();
    }


    
    /////////////////////////////////////
    // DocumentListener implementation //
    /////////////////////////////////////
	
    public void changedUpdate(DocumentEvent e) {
        textViewerDelegate.menuHelper.updateEditActions();
        // ignore change event if it was caused by syntax change
//        if (!textViewerDelegate.menuHelper.checkWaitChangeSyntaxEvent()) {
            setSaveNeeded(true);
 //        }
    }
	
    public void insertUpdate(DocumentEvent e) {
        setSaveNeeded(true);
    }

    public void removeUpdate(DocumentEvent e) {
        setSaveNeeded(true);
    }
    
    ///////////////////////////////////
    // ActionListener implementation //
    ///////////////////////////////////

    public void actionPerformed(ActionEvent e) {
        if (textViewerDelegate.menuHelper.performAction(e, textViewerDelegate)) {
            return;
        }
        super.actionPerformed(e);
    }
    
    /////////////////////////////////////
    // EncodingListener implementation //
    /////////////////////////////////////

    public void encodingChanged(Object source, String oldEncoding, String newEncoding) {
    	if (!askSave()) {
    		return;         // Abort if the file could not be saved
        }

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
            setSaveNeeded(false);
        } catch (IOException ex) {
    		InformationDialog.showErrorDialog(getFrame(), Translator.get("read_error"), Translator.get("file_editor.cannot_read_file", getCurrentFile().getName()));
    	}
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
     * Returns the gutter.
     *
     * @return The gutter.
     */
    public Gutter getGutter() {
        return gutter;
    }


    /**
     * Returns <code>true</code> if the line numbers are enabled and visible.
     *
     * @return Whether or not line numbers are visible.
     * @see #setLineNumbersEnabled(boolean)
     */
    private boolean getLineNumbersEnabled() {
        return gutter.getLineNumbersEnabled();
    }


    /**
     * Returns whether the fold indicator is enabled.
     *
     * @return Whether the fold indicator is enabled.
     * @see #setFoldIndicatorEnabled(boolean)
     */
    public boolean isFoldIndicatorEnabled() {
        return gutter.isFoldIndicatorEnabled();
    }


    /**
     * Returns whether the icon row header is enabled.
     *
     * @return Whether the icon row header is enabled.
     * @see #setIconRowHeaderEnabled(boolean)
     */
    public boolean isIconRowHeaderEnabled() {
        return gutter.isIconRowHeaderEnabled();
    }


    /**
     * Toggles whether the fold indicator is enabled.
     *
     * @param enabled Whether the fold indicator should be enabled.
     * @see #isFoldIndicatorEnabled()
     */
    public void setFoldIndicatorEnabled(boolean enabled) {
        gutter.setFoldIndicatorEnabled(enabled);
        checkGutterVisibility();
    }


    /**
     * Toggles whether the icon row header (used for breakpoints, bookmarks,
     * etc.) is enabled.
     *
     * @param enabled Whether the icon row header is enabled.
     * @see #isIconRowHeaderEnabled()
     */
    public void setIconRowHeaderEnabled(boolean enabled) {
        gutter.setIconRowHeaderEnabled(enabled);
        checkGutterVisibility();
    }


    /**
     * Toggles whether or not line numbers are visible.
     *
     * @param enabled Whether or not line numbers should be visible.
     * @see #getLineNumbersEnabled()
     */
    public void setLineNumbersEnabled(boolean enabled) {
        gutter.setLineNumbersEnabled(enabled);
        checkGutterVisibility();
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


    @Override
    public void setSearchedText(String searchedText) {
        textEditorImpl.setupSearchContext(searchedText);
    }


    @Override
    public void setSearchedBytes(byte[] searchedBytes) {
        try {
            textEditorImpl.setupSearchContext(new String(searchedBytes, textViewerDelegate.getEncoding()));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
}
