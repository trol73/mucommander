/*
 * This file is part of trolCommander, http://www.trolsoft.ru/en/soft/trolcommander
 * Copyright (C) 2014-2018 Oleg Trifonov
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

import com.mucommander.cache.TextHistory;
import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.runtime.OsFamily;
import com.mucommander.ui.action.impl.UserMenuAction;
import com.mucommander.ui.helper.MenuToolkit;
import com.mucommander.ui.helper.MnemonicHelper;
import com.mucommander.ui.main.MainFrame;
import com.mucommander.ui.main.menu.UserPopupMenu;
import com.mucommander.utils.text.Translator;
import org.intellij.lang.annotations.MagicConstant;
import ru.trolsoft.calculator.CalculatorDialog;
import ru.trolsoft.ui.TMenuSeparator;
import ru.trolsoft.ui.TRadioButtonMenuItem;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.LinkedList;

import static javax.swing.KeyStroke.getKeyStroke;
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
    private JMenu menuTools;

    // Items

    private JMenuItem miFiles;
    private JMenuItem miMainFrame;
    private JMenuItem miAddToBookmarks;
    private JMenuItem miRemoveFromBookmarks;
    private JMenuItem miGotoHeaderSource;
//    private JMenuItem miClose;

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
    private JMenuItem miToggleInvisibleChars;

    private JMenuItem miCalculator;
    private JMenuItem miBuild;
    private JMenuItem miUserMenu;
    private JMenuItem miFormat;
    private FileType fileType;

    private static boolean showInvisibleChars = false;


    TextMenuHelper(TextEditorImpl textEditorImpl, boolean editMode) {
        this.textEditorImpl = textEditorImpl;
        this.editMode = editMode;
        updateInvisibleChars();
    }

    void initMenu(ActionListener actionListener, boolean lineNumbers) {
        // Edit menu
        menuEdit = new JMenu(Translator.get("text_editor.edit"));
        MnemonicHelper menuItemMnemonicHelper = new MnemonicHelper();

        if (editMode) {
            miUndo = MenuToolkit.addMenuItem(menuEdit, i18n("text_editor.undo"), menuItemMnemonicHelper, null, actionListener);
            miRedo = MenuToolkit.addMenuItem(menuEdit, i18n("text_editor.redo"), menuItemMnemonicHelper, null, actionListener);
            menuEdit.addSeparator();
        }
        miCopy = MenuToolkit.addMenuItem(menuEdit, i18n("text_editor.copy"), menuItemMnemonicHelper, null, actionListener);
        if (editMode) {
            miCut = MenuToolkit.addMenuItem(menuEdit, i18n("text_editor.cut"), menuItemMnemonicHelper, null, actionListener);
            miPaste = MenuToolkit.addMenuItem(menuEdit, i18n("text_editor.paste"), menuItemMnemonicHelper, null, actionListener);
        }

        miSelectAll = MenuToolkit.addMenuItem(menuEdit, i18n("text_editor.select_all"), menuItemMnemonicHelper, null, actionListener);
        menuEdit.addSeparator();

        menuEdit.addSeparator();

        if (editMode) {
            miFormat = MenuToolkit.addMenuItem(menuEdit, i18n("text_editor.format"), menuItemMnemonicHelper, getKeyStroke(KeyEvent.VK_F, KeyEvent.SHIFT_DOWN_MASK|getCtrlOrMetaMask()), actionListener);
        }

        // Search menu
        menuSearch = new JMenu(Translator.get("text_editor.search"));
        miFind = MenuToolkit.addMenuItem(menuSearch, i18n("text_editor.find"), menuItemMnemonicHelper, getKeyStroke(KeyEvent.VK_F, getCtrlOrMetaMask()), actionListener);
        miFindNext = MenuToolkit.addMenuItem(menuSearch, i18n("text_editor.find_next"), menuItemMnemonicHelper, getKeyStroke(KeyEvent.VK_F3, 0), actionListener);
        miFindPrevious = MenuToolkit.addMenuItem(menuSearch, i18n("text_editor.find_previous"), menuItemMnemonicHelper, getKeyStroke(KeyEvent.VK_F3, KeyEvent.SHIFT_DOWN_MASK), actionListener);
        if (editMode) {
            menuSearch.addSeparator();
            miReplace = MenuToolkit.addMenuItem(menuSearch, i18n("text_editor.replace_menu"), menuItemMnemonicHelper, getKeyStroke(KeyEvent.VK_F, getCtrlOrMetaMask()|KeyEvent.ALT_MASK), actionListener);
        }
        menuSearch.addSeparator();
        miGotoLine = MenuToolkit.addMenuItem(menuSearch, i18n("text_viewer.goto_line"), menuItemMnemonicHelper, getKeyStroke(KeyEvent.VK_G, getCtrlOrMetaMask()), actionListener);

        // View menu
        menuView = new JMenu(i18n("text_editor.view"));

        miToggleLineWrap = MenuToolkit.addCheckBoxMenuItem(menuView, i18n("text_editor.line_wrap"), menuItemMnemonicHelper, getKeyStroke(KeyEvent.VK_F2, 0), actionListener);
        miToggleLineWrap.setSelected(textEditorImpl.isWrap());
        miToggleLineNumbers = MenuToolkit.addCheckBoxMenuItem(menuView, i18n("text_editor.line_numbers"), menuItemMnemonicHelper, null, actionListener);
        miToggleLineNumbers.setSelected(lineNumbers);
        miToggleInvisibleChars = MenuToolkit.addCheckBoxMenuItem(menuView, i18n("text_editor.invisible_chars"), menuItemMnemonicHelper, null, actionListener);
        miToggleInvisibleChars.setSelected(showInvisibleChars);

        menuView.addSeparator();
        menuViewSyntax = new JMenu(Translator.get("text_editor.syntax"));

        addSyntaxMenu(actionListener, menuItemMnemonicHelper);

        // Tools menu
        addToolsMenu(actionListener, menuItemMnemonicHelper);
    }

    void setupFileMenu(JMenu fileMenu, ActionListener actionListener, AbstractFile currentFile) {
        MnemonicHelper mnemonicHelper = new MnemonicHelper();
        JMenuItem lastItem = fileMenu.getItemCount() > 0 ? fileMenu.getItem(fileMenu.getItemCount()-1) : null;

        int mask = OsFamily.MAC_OS_X.isCurrent() ? KeyEvent.ALT_MASK : KeyEvent.CTRL_MASK;
        miFiles = MenuToolkit.addMenuItem(fileMenu, i18n("file_editor.files"), mnemonicHelper, getKeyStroke(KeyEvent.VK_TAB, mask), actionListener);
        miMainFrame = MenuToolkit.addMenuItem(fileMenu, i18n("file_editor.show_file_manager"), mnemonicHelper, getKeyStroke(KeyEvent.VK_1, KeyEvent.CTRL_MASK), actionListener);
        miAddToBookmarks = MenuToolkit.addMenuItem(fileMenu, i18n("file_editor.add_to_bookmark"), mnemonicHelper, null, actionListener);
        miRemoveFromBookmarks = MenuToolkit.addMenuItem(fileMenu, i18n("file_editor.remove_from_bookmark"), mnemonicHelper, null, actionListener);

        mask = getCtrlOrMetaMask() | KeyEvent.SHIFT_MASK;
        miGotoHeaderSource = MenuToolkit.addMenuItem(fileMenu, i18n("file_editor.goto_header_source"), mnemonicHelper, getKeyStroke(KeyEvent.VK_A, mask), actionListener);
        fileMenu.add(new TMenuSeparator());
        if (lastItem != null) {
            fileMenu.add(lastItem);
        }

        updateFileBookmarksMenuItems(currentFile);
        updateGotoHeaderSourceVisibility();
    }

    private void updateFileBookmarksMenuItems(AbstractFile currentFile) {
        if (currentFile == null) {
            miAddToBookmarks.setVisible(false);
            miRemoveFromBookmarks.setVisible(false);
            return;
        }
        boolean inBookmarks = getBookmarkFilesList().contains(currentFile.getURL().toString());
        miAddToBookmarks.setVisible(!inBookmarks);
        miRemoveFromBookmarks.setVisible(inBookmarks);
    }

    private void addToolsMenu(ActionListener actionListener, MnemonicHelper menuItemMnemonicHelper) {
        menuTools = new JMenu(Translator.get("text_editor.tools"));
        miCalculator = MenuToolkit.addMenuItem(menuTools, Translator.get("Calculator.label"), menuItemMnemonicHelper, getKeyStroke(KeyEvent.VK_F6, 0), actionListener);
        miBuild = MenuToolkit.addMenuItem(menuTools, Translator.get("text_editor.build"), menuItemMnemonicHelper, getKeyStroke(KeyEvent.VK_B, KeyEvent.META_DOWN_MASK), actionListener);
        miUserMenu = MenuToolkit.addMenuItem(menuTools, Translator.get("UserMenu.label"), menuItemMnemonicHelper, getKeyStroke(KeyEvent.VK_F1, 0), actionListener);
    }

    private void addSyntaxMenu(ActionListener actionListener, MnemonicHelper menuItemMnemonicHelper) {
        menuView.add(menuViewSyntax);
        ButtonGroup group = new ButtonGroup();
        for (FileType fileType : FileType.values()) {
            MenuToolkit.addRadioButtonMenuItem(menuViewSyntax, fileType.getName(), menuItemMnemonicHelper,null,
                    actionListener, group);
        }
    }

    @MagicConstant(flags = {KeyEvent.META_MASK, KeyEvent.CTRL_MASK})
    private int getCtrlOrMetaMask() {
        return OsFamily.MAC_OS_X.isCurrent() ? KeyEvent.META_MASK : KeyEvent.CTRL_MASK;
    }


    JMenu getEditMenu() {
        return menuEdit;
    }

    JMenu getViewMenu() {
        return menuView;
    }

    JMenu getSearchMenu() {
        return menuSearch;
    }

    JMenu getToolsMenu() {
        return menuTools;
    }

    public boolean performAction(ActionEvent e, TextViewer textViewerDelegate) {
        Object source = e.getSource();
        if (source == null) {
            return false;
        }
        if (checkSyntaxChangeAction(source)) {
            return true;
        }
        if (source == miFiles) {
            textEditorImpl.showFilesQuickList();
        } else if (source == miMainFrame) {
            showMainFrame();
        } else if (source == miAddToBookmarks) {
            textEditorImpl.addCurrentFileToBookmarks();
            miAddToBookmarks.setVisible(false);
            miRemoveFromBookmarks.setVisible(true);
        } else if (source == miRemoveFromBookmarks) {
            textEditorImpl.removeCurrentFileFromBookmarks();
            miAddToBookmarks.setVisible(true);
            miRemoveFromBookmarks.setVisible(false);
        } else if (source == miGotoHeaderSource) {
            textEditorImpl.switchBetweenHeaderAndSource();
//        } else if (source == miClose) {
//            textEditorImpl.frame.dispose();
        } else if (source == miCopy) {
            textEditorImpl.copy();
        } else if (source == miCut) {
            textEditorImpl.cut();
        } else if (source == miPaste) {
            textEditorImpl.paste();
        } else if (source == miSelectAll) {
            textEditorImpl.selectAll();
        } else if (source == miFind) {
            textEditorImpl.find();
        } else if (source == miReplace) {
            textEditorImpl.replace();
        } else if (source == miFindNext) {
            textEditorImpl.findNext();
        } else if (source == miFindPrevious) {
            textEditorImpl.findPrevious();
        } else if (source == miToggleLineWrap) {
            if (e.getWhen() == 0) {  
                miToggleLineWrap.setSelected(!miToggleLineWrap.isSelected());
            }
            textViewerDelegate.wrapLines(miToggleLineWrap.isSelected());
        } else if (source == miToggleLineNumbers) {
            textViewerDelegate.showLineNumbers(miToggleLineNumbers.isSelected());
        } else if (source == miToggleInvisibleChars) {
            showInvisibleChars = miToggleInvisibleChars.isSelected();
            updateInvisibleChars();
        } else if (source == miGotoLine) {
            textEditorImpl.gotoLine();
        } else if (source == miUndo) {
            textEditorImpl.undo();
        } else if (source == miRedo) {
            textEditorImpl.redo();
        } else if (source == miFormat) {
            TextEditorUtils.formatCode(textEditorImpl);
        } else if (source == miCalculator) {
            new CalculatorDialog(textEditorImpl.frame).showDialog();
        } else if (source == miBuild) {
            textEditorImpl.build();
        } else if (source == miUserMenu) {
            UserPopupMenu menu = UserMenuAction.createMenu(getMainFrame());
            menu.show(textEditorImpl.frame);
        } else {
            return false;
        }
        updateEditActions();
        return true;
    }

    private MainFrame getMainFrame() {
        return textEditorImpl.frame.getMainFrame();
    }

    private void updateInvisibleChars() {
        textEditorImpl.getTextArea().setWhitespaceVisible(showInvisibleChars);
    }

    private boolean checkSyntaxChangeAction(Object source) {
        if (source instanceof TRadioButtonMenuItem) {
            for (int i = 0; i < menuViewSyntax.getItemCount(); i++) {
                JMenuItem item = menuViewSyntax.getItem(i);
                if (source == item) {
                    FileType fileType = FileType.getByName(item.getText());
                    textEditorImpl.setSyntaxType(fileType);
                    setSyntax(fileType);
                    return true;
                }
            }
        }
        return false;
    }

    public void setSyntax(FileType fileType) {
        for (int i = 0; i < menuViewSyntax.getItemCount(); i++) {
            JMenuItem item = menuViewSyntax.getItem(i);
            item.setSelected(item.getText().equals(fileType.getName()));
        }
        updateEditActions();
        this.fileType = fileType;
        updateGotoHeaderSourceVisibility();
    }

    private void updateGotoHeaderSourceVisibility() {
        if (miGotoHeaderSource != null) {
            miGotoHeaderSource.setVisible(fileType == FileType.CPP || fileType == FileType.C);
        }
    }


    /*
     * Check if last editor change fired by syntax change event ant will be ignored in document listener
     * @return true if if last editor change fired by syntax change event ant will be ignored in document listener
     */
//    public boolean checkWaitChangeSyntaxEvent() {
//        boolean result = waitChangeSyntaxEvent;
//        updateEditActions();
//        waitChangeSyntaxEvent = false;
//        return result;
//    }


    void updateEditActions() {
        if (!editMode) {
            return;
        }
        final TextArea textArea = textEditorImpl.getTextArea();
        miUndo.setEnabled(textArea.canUndo());
        miRedo.setEnabled(textArea.canRedo());
        FileType ft = textArea.getFileType();
        miFormat.setVisible(ft == FileType.XML || ft == FileType.JSON);
    }

    void setBuildable(boolean canBuild) {
        miBuild.setEnabled(canBuild);
    }

    private static String i18n(String key, String... params) {
        return Translator.get(key, params);
    }

    private static LinkedList<String> getBookmarkFilesList() {
        return TextHistory.getInstance().getList(TextHistory.Type.EDITOR_BOOKMARKS);
    }

    private void showMainFrame() {
        getMainFrame().toFront();
    }


}
