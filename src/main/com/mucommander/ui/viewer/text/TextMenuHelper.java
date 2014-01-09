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
 * Created by trol on 09/01/14.
 */
public class TextMenuHelper {
    private TextEditorImpl textEditorImpl;

    /** Menu bar */
    // Menus //
    private JMenu editMenu;
    private JMenu viewMenu;
    private JMenu viewMenuSyntax;
    // Items //
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

    private boolean waitChangeSyntaxEvent = false;

    public TextMenuHelper(TextEditorImpl textEditorImpl) {
        this.textEditorImpl = textEditorImpl;
    }

    public void initMenu(ActionListener actionListener, boolean linuNumbers) {
        // Edit menu
        editMenu = new JMenu(Translator.get("text_editor.edit"));
        MnemonicHelper menuItemMnemonicHelper = new MnemonicHelper();

        copyItem = MenuToolkit.addMenuItem(editMenu, Translator.get("text_editor.copy"), menuItemMnemonicHelper, null, actionListener);

        cutItem = MenuToolkit.addMenuItem(editMenu, Translator.get("text_editor.cut"), menuItemMnemonicHelper, null, actionListener);
        pasteItem = MenuToolkit.addMenuItem(editMenu, Translator.get("text_editor.paste"), menuItemMnemonicHelper, null, actionListener);

        selectAllItem = MenuToolkit.addMenuItem(editMenu, Translator.get("text_editor.select_all"), menuItemMnemonicHelper, null, actionListener);
        editMenu.addSeparator();

        if (OsFamily.getCurrent() != OsFamily.MAC_OS_X) {
            findItem = MenuToolkit.addMenuItem(editMenu, Translator.get("text_editor.find"), menuItemMnemonicHelper, KeyStroke.getKeyStroke(KeyEvent.VK_F, KeyEvent.CTRL_MASK), actionListener);
        } else {
            findItem = MenuToolkit.addMenuItem(editMenu, Translator.get("text_editor.find"), menuItemMnemonicHelper, KeyStroke.getKeyStroke(KeyEvent.VK_F, KeyEvent.META_DOWN_MASK), actionListener);
        }
        findNextItem = MenuToolkit.addMenuItem(editMenu, Translator.get("text_editor.find_next"), menuItemMnemonicHelper, KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0), actionListener);
        findPreviousItem = MenuToolkit.addMenuItem(editMenu, Translator.get("text_editor.find_previous"), menuItemMnemonicHelper, KeyStroke.getKeyStroke(KeyEvent.VK_F3, KeyEvent.SHIFT_DOWN_MASK), actionListener);
        editMenu.addSeparator();
        if (OsFamily.getCurrent() != OsFamily.MAC_OS_X) {
            gotoLineItem = MenuToolkit.addMenuItem(editMenu, Translator.get("text_viewer.goto_line"), menuItemMnemonicHelper, KeyStroke.getKeyStroke(KeyEvent.VK_G, KeyEvent.CTRL_MASK), actionListener);
        } else {
            gotoLineItem = MenuToolkit.addMenuItem(editMenu, Translator.get("text_viewer.goto_line"), menuItemMnemonicHelper, KeyStroke.getKeyStroke(KeyEvent.VK_G, KeyEvent.META_MASK), actionListener);
        }

        viewMenu = new JMenu(Translator.get("text_editor.view"));

        toggleLineWrapItem = MenuToolkit.addCheckBoxMenuItem(viewMenu, Translator.get("text_editor.line_wrap"), menuItemMnemonicHelper, null, actionListener);
        toggleLineWrapItem.setSelected(textEditorImpl.isWrap());
        toggleLineNumbersItem = MenuToolkit.addCheckBoxMenuItem(viewMenu, Translator.get("text_editor.line_numbers"), menuItemMnemonicHelper, null, actionListener);
        toggleLineNumbersItem.setSelected(linuNumbers);

        viewMenu.addSeparator();
        viewMenuSyntax = new JMenu(Translator.get("text_editor.syntax"));

        viewMenu.add(viewMenuSyntax);
        for (FileType fileType : FileType.values()) {
            MenuToolkit.addCheckBoxMenuItem(viewMenuSyntax, fileType.getName(), menuItemMnemonicHelper, null, actionListener);
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

        // check style picker
        if (source != null && source instanceof JCheckBoxMenuItem) {
            for (int i = 0; i < viewMenuSyntax.getItemCount(); i++) {
                JCheckBoxMenuItem item = (JCheckBoxMenuItem)viewMenuSyntax.getItem(i);
                if (source == item) {
                    FileType fileType = FileType.getByName(item.getText());
                    waitChangeSyntaxEvent = true;
                    textEditorImpl.getTextArea().setFileType(fileType);
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
            textViewerDelegate.wrapLines(toggleLineWrapItem.isSelected());
        } else if(source == toggleLineNumbersItem) {
            textViewerDelegate.showLineNumbers(toggleLineNumbersItem.isSelected());
        } else if (source == gotoLineItem) {
            textEditorImpl.gotoLine();
        } else {
            return false;
        }
        return true;
    }

    public void setSyntax(FileType fileType) {
        for (int i = 0; i < viewMenuSyntax.getItemCount(); i++) {
            JCheckBoxMenuItem item = (JCheckBoxMenuItem)viewMenuSyntax.getItem(i);
            item.setSelected(item.getText().equals(fileType.getName()));
        }
    }


    /**
     * Check if last editor change fired by syntax change event ant will be ignored in document listener
     * @return
     */
    public boolean checkWaitChangeSyntaxEvent() {
        boolean result = waitChangeSyntaxEvent;
        waitChangeSyntaxEvent = false;
        return result;
    }

}
