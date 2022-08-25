/*
 * This file is part of trolCommander, http://www.trolsoft.ru/en/soft/trolcommander
 * Copyright (C) 2013-2016 Oleg Trifonov
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

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rtextarea.RTextAreaEditorKit;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import java.awt.*;

/**
 * @author Oleg Trifonov
 * Created on 08/01/14.
 */
public class TextArea extends RSyntaxTextArea implements DocumentListener {

    private static final String DIRTY_PROPERTY	= "TextEditorPane.dirty";

    /**
     * The #gotoLine(int) method can't be executed successfully if the model is not painted.
     * In this case the operation wll be postponed after calling #paint() method
     */
    private int postponedCaretPosition = -1;

    /**
     * Whether the file is dirty.
     */
    private boolean dirty;

    TextArea() {
        dirty = false;
        getDocument().addDocumentListener(this);
    }

    /**
     *
     * @param line line number (started from 1)
     * @param column cursor position in the line
     * @return true on success
     */
    boolean gotoLine(int line, int column) {
        try {
            int pos = getLineStartOffset(line - 1) + column - 1;
            setCaretPosition(pos);
            Rectangle temp = modelToView(pos);
            if (temp == null) {
                postponedCaretPosition = pos;
            } else {
                forceCurrentLineHighlightRepaint();
            }
            return true;
        } catch (IllegalArgumentException | BadLocationException e) {
            System.out.println("Invalid line: " + line + ":" + column + " (" + e.getMessage() + ")");
            return false;
        }
    }

    boolean gotoLine(int line) {
        return gotoLine(line, 1);
    }


    /**
     *
     * @return current line number (started from 1)
     */
    public int getLine() {
        int dot = getCaretPosition();
        Element map = getDocument().getDefaultRootElement();
        return map.getElementIndex(dot) + 1;
    }

    void setFileType(FileType fileType) {
        setSyntaxEditingStyle(fileType.getContentType());
    }

    FileType getFileType() {
        return FileType.getByContentType(getSyntaxEditingStyle());
    }


    /**
     *
     * @return current cursor position in line (started from 1)
     */
    public int getColumn() {
        Element map = getDocument().getDefaultRootElement();
        int dot = getCaretPosition();
        int line = map.getElementIndex(dot);
        int lineStartOffset = map.getElement(line).getStartOffset();
        return dot - lineStartOffset + 1;
    }


    @Override
    public void paint(Graphics g) {
        super.paint(g);

        // if gotoLine method executed before that model was painted then recall it
        if (postponedCaretPosition >= 0) {
            final int pos = postponedCaretPosition;
            postponedCaretPosition = -1;
            SwingUtilities.invokeLater(() -> {
                setCaretPosition(pos);
                forceCurrentLineHighlightRepaint();
            });
        }
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        if (!dirty) {
            setDirty(true);
        }
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        if (!dirty) {
            setDirty(true);
        }
    }

    @Override
    public void changedUpdate(DocumentEvent e) {

    }

    /**
     * Returns whether the text in this editor has unsaved changes.
     *
     * @return Whether the text has unsaved changes.
     * @see #setDirty(boolean)
     */
    public boolean isDirty() {
        return dirty;
    }

    /**
     * Sets whether this text in this editor has unsaved changes.
     * This fires a property change event of type {@link #DIRTY_PROPERTY}.<p>
     *
     * Applications will usually have no need to call this method directly; the
     * only time you might have a need to call this method directly is if you
     * have to initialize an instance of TextEditorPane with content that does
     * not come from a file. <code>TextEditorPane</code> automatically sets its
     * own dirty flag when its content is edited, when its encoding is changed,
     * or when its line ending property is changed.  It is cleared whenever
     * <code>load()</code>, <code>reload()</code>, <code>save()</code>, or
     * <code>saveAs()</code> are called.
     *
     * @param dirty Whether or not the text has been modified.
     * @see #isDirty()
     */
    public void setDirty(boolean dirty) {
        if (this.dirty != dirty) {
            this.dirty = dirty;
            firePropertyChange(DIRTY_PROPERTY, !dirty, dirty);
        }
    }

    /**
     * Sets the document for this editor.
     *
     * @param doc The new document.
     */
    @Override
    public void setDocument(Document doc) {
        Document old = getDocument();
        if (old != null) {
            old.removeDocumentListener(this);
        }
        try {
            super.setDocument(doc);
        } catch (Exception e) {
            // Sometime RSyntaxTextArea can crash for python files on code folding parsing
            setCodeFoldingEnabled(false);
            super.setDocument(doc);
        }
        doc.addDocumentListener(this);
    }


    /**
     * Sets the line separator sequence to use when this file is saved (e.g.
     * "<code>\n</code>", "<code>\r\n</code>" or "<code>\r</code>").
     *
     * Besides parameter checking, this method is preferred over
     * <code>getDocument().putProperty()</code> because can set the editor's
     * dirty flag when the line separator is changed.
     *
     * @param separator The new line separator.
     * @param setDirty Whether the dirty flag should be set if the line
     *        separator is changed.
     * @throws NullPointerException If <code>separator</code> is
     *         <code>null</code>.
     * @throws IllegalArgumentException If <code>separator</code> is not one
     *         of "<code>\n</code>", "<code>\r\n</code>" or "<code>\r</code>".
     * @see #getLineSeparator()
     */
    public void setLineSeparator(String separator, boolean setDirty) {
        if (separator == null) {
            throw new NullPointerException("terminator cannot be null");
        }
        if (!"\r\n".equals(separator) && !"\n".equals(separator) && !"\r".equals(separator)) {
            throw new IllegalArgumentException("Invalid line terminator");
        }
        Document doc = getDocument();
        Object old = doc.getProperty(RTextAreaEditorKit.EndOfLineStringProperty);
        if (!separator.equals(old)) {
            doc.putProperty(RTextAreaEditorKit.EndOfLineStringProperty, separator);
            if (setDirty) {
                setDirty(true);
            }
        }
    }

    /**
     * Returns the line separator used when writing this file (e.g.
     * "<code>\n</code>", "<code>\r\n</code>", or "<code>\r</code>").<p>
     *
     * Note that this value is an <code>Object</code> and not a
     * <code>String</code> as that is the way the {@link Document} interface
     * defines its property values.  If you always use
     * {@link #setLineSeparator(String)} to modify this value, then the value
     * returned from this method will always be a <code>String</code>.
     *
     * @return The line separator.  If this value is <code>null</code>, then
     *         the system default line separator is used (usually the value
     *         of <code>System.getProperty("line.separator")</code>).
     * @see #setLineSeparator(String)
     * @see #setLineSeparator(String, boolean)
     */
    public Object getLineSeparator() {
        return getDocument().getProperty(RTextAreaEditorKit.EndOfLineStringProperty);
    }


    /**
     * Sets the line separator sequence to use when this file is saved (e.g.
     * "<code>\n</code>", "<code>\r\n</code>" or "<code>\r</code>").
     *
     * Besides parameter checking, this method is preferred over
     * <code>getDocument().putProperty()</code> because it sets the editor's
     * dirty flag when the line separator is changed.
     *
     * @param separator The new line separator.
     * @throws NullPointerException If <code>separator</code> is
     *         <code>null</code>.
     * @throws IllegalArgumentException If <code>separator</code> is not one
     *         of "<code>\n</code>", "<code>\r\n</code>" or "<code>\r</code>".
     * @see #getLineSeparator()
     */
    public void setLineSeparator(String separator) {
        setLineSeparator(separator, true);
    }


    String getLineStr(int line) {
        try {
            int posStart = getLineStartOffset(line - 1);
            int posEnd = getLineEndOffset(line - 1);
            int len = posEnd - posStart;
            if (len > 2048) {
                return null;
            }
            return getDocument().getText(posStart, len);
        } catch (Exception ignore) {
            return null;
        }
    }

}
