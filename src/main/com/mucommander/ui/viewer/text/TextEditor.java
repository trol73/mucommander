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

import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.FileOperation;
import com.mucommander.commons.io.bom.BOMWriter;
import com.mucommander.text.Translator;
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
class TextEditor extends FileEditor implements DocumentListener, EncodingListener {
	private static final Logger LOGGER = LoggerFactory.getLogger(TextEditor.class);


    //private TextMenuHelper menuHelper;
    private TextEditorImpl textEditorImpl;
    private TextViewer textViewerDelegate;


    public TextEditor() {
    	textViewerDelegate = new TextViewer(textEditorImpl = new TextEditorImpl(true)) {
    		
    		@Override
    		protected void setComponentToPresent(JComponent component) {
    			TextEditor.this.setComponentToPresent(component);
    		}
    		
    		@Override
    		protected void showLineNumbers(boolean show) {
    			TextEditor.this.setRowHeaderView(show ? new TextLineNumbersPanel(textEditorImpl.getTextArea()) : null);
    	    }
    		
    		@Override
    		protected void initMenuBarItems() {
                menuHelper = new TextMenuHelper(textEditorImpl, true);
                menuHelper.initMenu(TextEditor.this, TextEditor.this.getRowHeader().getView() != null);
    		}
    	};
    	
    	setComponentToPresent(textEditorImpl.getTextArea());
    }
    
    protected void setComponentToPresent(JComponent component) {
		getViewport().add(component);
	}
    
    void loadDocument(InputStream in, String encoding, DocumentListener documentListener) throws IOException {
    	textViewerDelegate.loadDocument(in, encoding, documentListener);
    }
    
    private void write(OutputStream out) throws IOException {
        // TODO make BOM optional
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
        menuBar.add(textViewerDelegate.menuHelper.getViewMenu());
        menuBar.add(encodingMenu);

        textViewerDelegate.setMainKeyListener(textEditorImpl.getTextArea(), menuBar);
    	return menuBar;
    }

    @Override
    protected void saveStateOnClose() {
        textViewerDelegate.saveState(getVerticalScrollBar());
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
        OutputStream out = null;

        try {
            out = destFile.getOutputStream();
            write(out);
        }
        finally {
            if(out != null) {
                try {out.close();}
                catch(IOException e) {
                    e.printStackTrace();
                    // Ignored
                }
            }
        }

        // We get here only if the destination file was updated successfully
        // so we can set that no further save is needed at this stage 
        setSaveNeeded(false);

        // Change the parent folder's date to now, so that changes are picked up by folder auto-refresh (see ticket #258)
        if(destFile.isFileOperationSupported(FileOperation.CHANGE_DATE)) {
            try {
                destFile.getParent().changeDate(System.currentTimeMillis());
            }
            catch (IOException e) {
                LOGGER.debug("failed to change the date of "+destFile, e);
                // Fail silently
            }
        }
    }

    @Override
    public void setFrame(final FileFrame frame) {
    	super.setFrame(frame);
    	
    	frame.setFullScreen(TextViewer.isFullScreen());

    	getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_M, InputEvent.CTRL_MASK), CUSTOM_FULL_SCREEN_EVENT);
    	getActionMap().put(CUSTOM_FULL_SCREEN_EVENT, new AbstractAction() {
    		public void actionPerformed(ActionEvent e){
    			TextViewer.setFullScreen(!frame.isFullScreen());
    			frame.setFullScreen(TextViewer.isFullScreen());
    		}
    	});
    }

    @Override
    public void show(AbstractFile file) throws IOException {
        textEditorImpl.getTextArea().discardAllEdits();
        textViewerDelegate.menuHelper.updateEditActions();

        TextFilesHistory.FileRecord historyRecord = textViewerDelegate.initHistoryRecord(file);
        FileType type = historyRecord.getFileType();
        if (type == null) {
            type = FileType.getFileType(file);
            historyRecord.setFileType(type);
        }
        textViewerDelegate.menuHelper.setSyntax(type);
        textEditorImpl.getTextArea().setFileType(type);
    	textViewerDelegate.startEditing(file, this);
    }
    
    /////////////////////////////////////
    // DocumentListener implementation //
    /////////////////////////////////////
	
    public void changedUpdate(DocumentEvent e) {
        // ignore change event if it was caused by syntax change
        if (!textViewerDelegate.menuHelper.checkWaitChangeSyntaxEvent()) {
            setSaveNeeded(true);
        }
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
    	if(!askSave())
    		return;         // Abort if the file could not be saved

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
        } catch(IOException ex) {
    		InformationDialog.showErrorDialog(getFrame(), Translator.get("read_error"), Translator.get("file_editor.cannot_read_file", getCurrentFile().getName()));
    	}
    }
}
