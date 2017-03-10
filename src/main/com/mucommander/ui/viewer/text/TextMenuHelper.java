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
import ru.trolsoft.calculator.CalculatorDialog;

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
    // Menus
    private JMenu menuEdit;
    private JMenu menuView;
    private JMenu menuViewSyntax;
    private JMenu menuSearch;
    // Items
    private JMenuItem miUndo;
    private JMenuItem miRedo;
    private JMenuItem miCopy;
    private JMenuItem miCut;
    private JMenuItem miPaste;
    private JMenuItem miSelectAll;

    private JMenuItem miFind;
    private JMenuItem miFindNext;
    private JMenuItem miFindPrevious;
    private JMenuItem miReplace;

    private JMenuItem miGotoLine;
    private JMenuItem miToggleLineWrap;
    private JMenuItem miToggleLineNumbers;
    private JMenuItem miCalculator;
    private JMenuItem miFormat;

    TextMenuHelper(TextEditorImpl textEditorImpl, boolean editMode) {
        this.textEditorImpl = textEditorImpl;
        this.editMode = editMode;
    }

    void initMenu(ActionListener actionListener, boolean lineNumbers) {
        // Edit menu
        menuEdit = new JMenu(Translator.get("text_editor.edit"));
        MnemonicHelper menuItemMnemonicHelper = new MnemonicHelper();

        if (editMode) {
            miUndo = MenuToolkit.addMenuItem(menuEdit, Translator.get("text_editor.undo"), menuItemMnemonicHelper, null, actionListener);
            miRedo = MenuToolkit.addMenuItem(menuEdit, Translator.get("text_editor.redo"), menuItemMnemonicHelper, null, actionListener);
            menuEdit.addSeparator();
        }
        miCopy = MenuToolkit.addMenuItem(menuEdit, Translator.get("text_editor.copy"), menuItemMnemonicHelper, null, actionListener);
        if (editMode) {
            miCut = MenuToolkit.addMenuItem(menuEdit, Translator.get("text_editor.cut"), menuItemMnemonicHelper, null, actionListener);
            miPaste = MenuToolkit.addMenuItem(menuEdit, Translator.get("text_editor.paste"), menuItemMnemonicHelper, null, actionListener);
        }

        miSelectAll = MenuToolkit.addMenuItem(menuEdit, Translator.get("text_editor.select_all"), menuItemMnemonicHelper, null, actionListener);
        menuEdit.addSeparator();

        menuEdit.addSeparator();

        if (editMode) {
            miFormat = MenuToolkit.addMenuItem(menuEdit, Translator.get("text_editor.format"), menuItemMnemonicHelper, KeyStroke.getKeyStroke(KeyEvent.VK_F, KeyEvent.SHIFT_DOWN_MASK|getCtrlOrMetaMask()), actionListener);
        }

        menuSearch = new JMenu(Translator.get("text_editor.search"));
        miFind = MenuToolkit.addMenuItem(menuSearch, Translator.get("text_editor.find"), menuItemMnemonicHelper, KeyStroke.getKeyStroke(KeyEvent.VK_F, getCtrlOrMetaMask()), actionListener);
        miFindNext = MenuToolkit.addMenuItem(menuSearch, Translator.get("text_editor.find_next"), menuItemMnemonicHelper, KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0), actionListener);
        miFindPrevious = MenuToolkit.addMenuItem(menuSearch, Translator.get("text_editor.find_previous"), menuItemMnemonicHelper, KeyStroke.getKeyStroke(KeyEvent.VK_F3, KeyEvent.SHIFT_DOWN_MASK), actionListener);
        if (editMode) {
            menuSearch.addSeparator();
            miReplace = MenuToolkit.addMenuItem(menuSearch, Translator.get("text_editor.replace_menu"), menuItemMnemonicHelper, KeyStroke.getKeyStroke(KeyEvent.VK_F, getCtrlOrMetaMask()|KeyEvent.ALT_MASK), actionListener);
        }
        menuSearch.addSeparator();
        miGotoLine = MenuToolkit.addMenuItem(menuSearch, Translator.get("text_viewer.goto_line"), menuItemMnemonicHelper, KeyStroke.getKeyStroke(KeyEvent.VK_G, getCtrlOrMetaMask()), actionListener);

        menuView = new JMenu(Translator.get("text_editor.view"));

        miToggleLineWrap = MenuToolkit.addCheckBoxMenuItem(menuView, Translator.get("text_editor.line_wrap"), menuItemMnemonicHelper, KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0), actionListener);
        miToggleLineWrap.setSelected(textEditorImpl.isWrap());
        miToggleLineNumbers = MenuToolkit.addCheckBoxMenuItem(menuView, Translator.get("text_editor.line_numbers"), menuItemMnemonicHelper, null, actionListener);
        miToggleLineNumbers.setSelected(lineNumbers);

        menuView.addSeparator();
        miCalculator = MenuToolkit.addMenuItem(menuView, Translator.get("Calculator.label"), menuItemMnemonicHelper, KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0), actionListener);
        menuView.addSeparator();
        menuViewSyntax = new JMenu(Translator.get("text_editor.syntax"));

        menuView.add(menuViewSyntax);
        for (FileType fileType : FileType.values()) {
            MenuToolkit.addCheckBoxMenuItem(menuViewSyntax, fileType.getName(), menuItemMnemonicHelper, null, actionListener);
        }
    }

    private int getCtrlOrMetaMask() {
        if (OsFamily.getCurrent() != OsFamily.MAC_OS_X) {
            return KeyEvent.CTRL_MASK;
        } else {
            return KeyEvent.META_MASK;
        }
    }


    JMenu getEditMenu() {
        return menuEdit;
    }

    JMenu getMenuView() {
        return menuView;
    }

    JMenu getSearchMenu() {
        return menuSearch;
    }

    public boolean performAction(ActionEvent e, TextViewer textViewerDelegate) {
        Object source = e.getSource();
        if (source == null) {
            return false;
        }
        // check style picker
        if (source instanceof JCheckBoxMenuItem) {
            for (int i = 0; i < menuViewSyntax.getItemCount(); i++) {
                JCheckBoxMenuItem item = (JCheckBoxMenuItem) menuViewSyntax.getItem(i);
                if (source == item) {
                    FileType fileType = FileType.getByName(item.getText());
                    textEditorImpl.setSyntaxType(fileType);
                    setSyntax(fileType);
                    return true;
                }
            }
        }

        if (source == miCopy) {
            textEditorImpl.copy();
        } else if(source == miCut) {
            textEditorImpl.cut();
        } else if(source == miPaste) {
            textEditorImpl.paste();
        } else if(source == miSelectAll) {
            textEditorImpl.selectAll();
        } else if(source == miFind) {
            textEditorImpl.find();
        } else if (source == miReplace) {
            textEditorImpl.replace();
        } else if(source == miFindNext) {
            textEditorImpl.findNext();
        } else if(source == miFindPrevious) {
            textEditorImpl.findPrevious();
        } else if(source == miToggleLineWrap) {
            if (e.getWhen() == 0) {  
                miToggleLineWrap.setSelected(!miToggleLineWrap.isSelected());
            }
            textViewerDelegate.wrapLines(miToggleLineWrap.isSelected());
        } else if(source == miToggleLineNumbers) {
            textViewerDelegate.showLineNumbers(miToggleLineNumbers.isSelected());
        } else if (source == miGotoLine) {
            textEditorImpl.gotoLine();
        } else if (source == miUndo) {
            textEditorImpl.undo();
        } else if (source == miRedo) {
            textEditorImpl.redo();
        } else if (source == miFormat) {
            textEditorImpl.formatCode();
        } else if (source == miCalculator) {
            new CalculatorDialog(textEditorImpl.frame).showDialog();
        } else {
            return false;
        }
        updateEditActions();
        return true;
    }

    public void setSyntax(FileType fileType) {
        for (int i = 0; i < menuViewSyntax.getItemCount(); i++) {
            JCheckBoxMenuItem item = (JCheckBoxMenuItem) menuViewSyntax.getItem(i);
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
        miUndo.setEnabled(textArea.canUndo());
        miRedo.setEnabled(textArea.canRedo());
        FileType ft = textArea.getFileType();
        miFormat.setVisible(ft == FileType.XML || ft == FileType.JSON);
    }


}
