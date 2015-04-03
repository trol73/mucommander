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
package com.mucommander.ui.viewer.text;

import com.mucommander.commons.runtime.OsFamily;
import com.mucommander.text.Translator;
import com.mucommander.ui.helper.MenuToolkit;
import com.mucommander.ui.helper.MnemonicHelper;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

/**
 * Helping class for menu creation in viewer and editor
 */
public class TextMenuHelper {
    private final TextEditorImpl textEditorImpl;
    private final boolean editMode;

    /** Menu bar */
    // Menus //
    private JMenu editMenu;
    private JMenu viewMenu;
    private JMenu viewMenuSyntax;
    // Items //
    private JMenuItem undoItem;
    private JMenuItem redoItem;
    private JMenuItem copyItem;
    private JMenuItem cutItem;
    private JMenuItem pasteItem;
    private JMenuItem selectAllItem;
    private JMenuItem findItem;
    private JMenuItem findNextItem;
    private JMenuItem findPreviousItem;
    private JMenuItem gotoLineItem;
    private JMenuItem toggleLineWrapItem;
    private JMenuItem toggleLineNumbersItem;
    private JMenuItem formatItem;

    public TextMenuHelper(TextEditorImpl textEditorImpl, boolean editMode) {
        this.textEditorImpl = textEditorImpl;
        this.editMode = editMode;
    }

    public void initMenu(ActionListener actionListener, boolean lineNumbers) {
        // Edit menu
        editMenu = new JMenu(Translator.get("text_editor.edit"));
        MnemonicHelper menuItemMnemonicHelper = new MnemonicHelper();

        if (editMode) {
            undoItem = MenuToolkit.addMenuItem(editMenu, Translator.get("text_editor.undo"), menuItemMnemonicHelper, null, actionListener);
            redoItem = MenuToolkit.addMenuItem(editMenu, Translator.get("text_editor.redo"), menuItemMnemonicHelper, null, actionListener);
            editMenu.addSeparator();
        }
        copyItem = MenuToolkit.addMenuItem(editMenu, Translator.get("text_editor.copy"), menuItemMnemonicHelper, null, actionListener);
        if (editMode) {
            cutItem = MenuToolkit.addMenuItem(editMenu, Translator.get("text_editor.cut"), menuItemMnemonicHelper, null, actionListener);
            pasteItem = MenuToolkit.addMenuItem(editMenu, Translator.get("text_editor.paste"), menuItemMnemonicHelper, null, actionListener);
        }

        selectAllItem = MenuToolkit.addMenuItem(editMenu, Translator.get("text_editor.select_all"), menuItemMnemonicHelper, null, actionListener);
        editMenu.addSeparator();

        findItem = MenuToolkit.addMenuItem(editMenu, Translator.get("text_editor.find"), menuItemMnemonicHelper, KeyStroke.getKeyStroke(KeyEvent.VK_F, getCtrlOrMetaMask()), actionListener);
        findNextItem = MenuToolkit.addMenuItem(editMenu, Translator.get("text_editor.find_next"), menuItemMnemonicHelper, KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0), actionListener);
        findPreviousItem = MenuToolkit.addMenuItem(editMenu, Translator.get("text_editor.find_previous"), menuItemMnemonicHelper, KeyStroke.getKeyStroke(KeyEvent.VK_F3, KeyEvent.SHIFT_DOWN_MASK), actionListener);
        editMenu.addSeparator();
        gotoLineItem = MenuToolkit.addMenuItem(editMenu, Translator.get("text_viewer.goto_line"), menuItemMnemonicHelper, KeyStroke.getKeyStroke(KeyEvent.VK_G, getCtrlOrMetaMask()), actionListener);

        if (editMode) {
            formatItem = MenuToolkit.addMenuItem(editMenu, Translator.get("text_editor.format"), menuItemMnemonicHelper, KeyStroke.getKeyStroke(KeyEvent.VK_F, KeyEvent.SHIFT_DOWN_MASK|getCtrlOrMetaMask()), actionListener);
        }
        viewMenu = new JMenu(Translator.get("text_editor.view"));


        toggleLineWrapItem = MenuToolkit.addCheckBoxMenuItem(viewMenu, Translator.get("text_editor.line_wrap"), menuItemMnemonicHelper, KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0), actionListener);
        toggleLineWrapItem.setSelected(textEditorImpl.isWrap());
        toggleLineNumbersItem = MenuToolkit.addCheckBoxMenuItem(viewMenu, Translator.get("text_editor.line_numbers"), menuItemMnemonicHelper, null, actionListener);
        toggleLineNumbersItem.setSelected(lineNumbers);

        viewMenu.addSeparator();
        viewMenuSyntax = new JMenu(Translator.get("text_editor.syntax"));

        viewMenu.add(viewMenuSyntax);
        for (FileType fileType : FileType.values()) {
            MenuToolkit.addCheckBoxMenuItem(viewMenuSyntax, fileType.getName(), menuItemMnemonicHelper, null, actionListener);
        }
    }

    private int getCtrlOrMetaMask() {
        if (OsFamily.getCurrent() != OsFamily.MAC_OS_X) {
            return KeyEvent.CTRL_MASK;
        } else {
            return KeyEvent.META_MASK;
        }
    }


    public JMenu getEditMenu() {
        return editMenu;
    }

    public JMenu getViewMenu() {
        return viewMenu;
    }

    public boolean performAction(ActionEvent e, TextViewer textViewerDelegate) {
        Object source = e.getSource();
        if (source == null) {
            return false;
        }
        // check style picker
        if (source instanceof JCheckBoxMenuItem) {
            for (int i = 0; i < viewMenuSyntax.getItemCount(); i++) {
                JCheckBoxMenuItem item = (JCheckBoxMenuItem)viewMenuSyntax.getItem(i);
                if (source == item) {
                    FileType fileType = FileType.getByName(item.getText());
                    textEditorImpl.setSyntaxType(fileType);
                    setSyntax(fileType);
                    return true;
                }
            }
        }

        if (source == copyItem) {
            textEditorImpl.copy();
        } else if(source == cutItem) {
            textEditorImpl.cut();
        } else if(source == pasteItem) {
            textEditorImpl.paste();
        } else if(source == selectAllItem) {
            textEditorImpl.selectAll();
        } else if(source == findItem) {
            textEditorImpl.find();
        } else if(source == findNextItem) {
            textEditorImpl.findNext();
        } else if(source == findPreviousItem) {
            textEditorImpl.findPrevious();
        } else if(source == toggleLineWrapItem) {
            if (e.getWhen() == 0) {  
              toggleLineWrapItem.setSelected(!toggleLineWrapItem.isSelected());
            }
            textViewerDelegate.wrapLines(toggleLineWrapItem.isSelected());
        } else if(source == toggleLineNumbersItem) {
            textViewerDelegate.showLineNumbers(toggleLineNumbersItem.isSelected());
        } else if (source == gotoLineItem) {
            textEditorImpl.gotoLine();
        } else if (source == undoItem) {
            textEditorImpl.undo();
        } else if (source == redoItem) {
            textEditorImpl.redo();
        } else if (source == formatItem) {
            textEditorImpl.formatCode();
        } else {
            return false;
        }
        updateEditActions();
        return true;
    }

    public void setSyntax(FileType fileType) {
        for (int i = 0; i < viewMenuSyntax.getItemCount(); i++) {
            JCheckBoxMenuItem item = (JCheckBoxMenuItem)viewMenuSyntax.getItem(i);
            item.setSelected(item.getText().equals(fileType.getName()));
        }
        updateEditActions();
    }


    /**
     * Check if last editor change fired by syntax change event ant will be ignored in document listener
     * @return true if if last editor change fired by syntax change event ant will be ignored in document listener
     */
//    public boolean checkWaitChangeSyntaxEvent() {
//        boolean result = waitChangeSyntaxEvent;
//        updateEditActions();
//        waitChangeSyntaxEvent = false;
//        return result;
//    }


    public void updateEditActions() {
        if (!editMode) {
            return;
        }
        final TextArea textArea = textEditorImpl.getTextArea();
        undoItem.setEnabled(textArea.canUndo());
        redoItem.setEnabled(textArea.canRedo());
        FileType ft = textArea.getFileType();
        formatItem.setVisible(ft == FileType.XML || ft == FileType.JSON);
    }


}
