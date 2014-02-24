package com.mucommander.ui.viewer.text;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;

import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import java.awt.Graphics;
import java.awt.Rectangle;

/**
 * Created by trol on 08/01/14.
 */
public class TextArea extends RSyntaxTextArea {

    /**
     * The #gotoLine(int) method can't be executed successfully if the model is not painted.
     * In this case the operation wll be postponed after calling #paint() method
     */
    private int postponedCaretPosition = -1;

    /**
     *
     * @param line line number (started from 1)
     * @param column cursor position in the line
     * @return
     */
    public boolean gotoLine(int line, int column) {
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
        } catch (BadLocationException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean gotoLine(int line) {
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

    public void setFileType(FileType fileType) {
        setSyntaxEditingStyle(fileType.getContentType());
    }

    public FileType getFileType() {
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
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    setCaretPosition(pos);
                    forceCurrentLineHighlightRepaint();
                }
            });
        }
    }

/*
    @Override
    public void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
        super.firePropertyChange(propertyName, oldValue, newValue);
System.out.println(">>> changed STR " + propertyName + " " + oldValue + " -> " +newValue);
    }

    @Override
    public void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) {
        super.firePropertyChange(propertyName, oldValue, newValue);
System.out.println(">>> changed BOOL " + propertyName + " " + oldValue + " -> " +newValue);
    }

    @Override
    public void firePropertyChange(String propertyName, char oldValue, char newValue) {
        super.firePropertyChange(propertyName, oldValue, newValue);
System.out.println(">>> changed CHAR " + propertyName + " " + oldValue + " -> " +newValue);
    }

    @Override
    public void firePropertyChange(String propertyName, int oldValue, int newValue) {
        super.firePropertyChange(propertyName, oldValue, newValue);
System.out.println(">>> changed INT " + propertyName + " " + oldValue + " -> " +newValue);
    }

    @Override
    public void firePropertyChange(String propertyName, byte oldValue, byte newValue) {
        super.firePropertyChange(propertyName, oldValue, newValue);
System.out.println(">>> changed BYTE " + propertyName + " " + oldValue + " -> " +newValue);
    }

    @Override
    public void firePropertyChange(String propertyName, short oldValue, short newValue) {
        super.firePropertyChange(propertyName, oldValue, newValue);
System.out.println(">>> changed SHORT " + propertyName + " " + oldValue + " -> " +newValue);
    }

    @Override
    public void firePropertyChange(String propertyName, long oldValue, long newValue) {
        super.firePropertyChange(propertyName, oldValue, newValue);
System.out.println(">>> changed LONG " + propertyName + " " + oldValue + " -> " +newValue);
    }

    @Override
    public void firePropertyChange(String propertyName, float oldValue, float newValue) {
        super.firePropertyChange(propertyName, oldValue, newValue);
System.out.println(">>> changed FLOAT " + propertyName + " " + oldValue + " -> " +newValue);
    }

    @Override
    public void firePropertyChange(String propertyName, double oldValue, double newValue) {
        super.firePropertyChange(propertyName, oldValue, newValue);
System.out.println(">>> changed DOUBLE " + propertyName + " " + oldValue + " -> " +newValue);
    }
*/
}
