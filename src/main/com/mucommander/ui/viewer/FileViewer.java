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

package com.mucommander.ui.viewer;

import com.mucommander.commons.runtime.OsFamily;
import com.mucommander.ui.main.WindowManager;
import com.mucommander.utils.text.Translator;
import com.mucommander.ui.helper.MenuToolkit;
import com.mucommander.ui.helper.MnemonicHelper;
import com.mucommander.ui.main.quicklist.ViewedAndEditedFilesQL;
import ru.trolsoft.ui.TMenuSeparator;

import javax.swing.*;
import java.awt.Component;
import java.awt.event.*;
import java.util.HashMap;
import java.util.Map;

/**
 * An abstract class to be subclassed by file viewer implementations.
 *
 * <p><b>Warning:</b> the file viewer/editor API may soon receive a major overhaul.
 *
 * @author Maxence Bernard, Arik Hadas
 */
public abstract class FileViewer extends FilePresenter implements ActionListener {

    /**
     * This map used to fix java issues with some menu hot-keys - some accelerators (etc. F2, arrows, Enter, Escape) doesn't work
     * properly in menu
     */
    private Map<KeyStroke, JMenuItem> menuKeyStrokes;
	
    /** Close menu item */
    private JMenuItem miClose;
    private JMenuItem miFiles;
    private JMenuItem miMainFrame;

    /**
     * Creates a new FileViewer.
     */
    public FileViewer() {}
	
    /**
     * Returns the menu bar that controls the viewer's frame. The menu bar should be retrieved using this method and
     * not by calling {@link JFrame#getJMenuBar()}, which may return <code>null</code>.
     *
     * @return the menu bar that controls the viewer's frame.
     */
    public JMenuBar getMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        MnemonicHelper mnemonicHelper = new MnemonicHelper();

        // File menu
        JMenu fileMenu = MenuToolkit.addMenu(Translator.get("file_viewer.file_menu"), mnemonicHelper, null);

        int mask = OsFamily.MAC_OS_X.isCurrent() ? KeyEvent.ALT_MASK : KeyEvent.CTRL_MASK;
        miFiles = MenuToolkit.addMenuItem(fileMenu, Translator.get("file_editor.files"), mnemonicHelper, KeyStroke.getKeyStroke(KeyEvent.VK_TAB, mask), this);
        miMainFrame = MenuToolkit.addMenuItem(fileMenu, Translator.get("file_editor.show_file_manager"), mnemonicHelper, KeyStroke.getKeyStroke(KeyEvent.VK_1, KeyEvent.CTRL_MASK), this);
        fileMenu.add(miMainFrame);

        fileMenu.add(new TMenuSeparator());


        miClose = MenuToolkit.addMenuItem(fileMenu, Translator.get("file_viewer.close"), mnemonicHelper, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), this);
        fileMenu.add(miClose);

        menuBar.add(fileMenu);

        return menuBar;
    }
    
    ///////////////////////////////////
    // ActionListener implementation //
    ///////////////////////////////////

    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        if (source == miClose) {
            getFrame().dispose();
        } else if (source == miFiles) {
            ViewedAndEditedFilesQL viewedAndEditedFilesQL = new ViewedAndEditedFilesQL(getFrame(), getCurrentFile());
            viewedAndEditedFilesQL.show();
        } else if (source == miMainFrame) {
            WindowManager.getMainFrames().get(0).toFront();
        }
    }


    private final KeyListener mainKeyListener = new KeyAdapter() {
        @Override
        public void keyPressed(KeyEvent e) {
            KeyStroke keyStroke = KeyStroke.getKeyStroke(e.getKeyCode(), e.getModifiers(), false);
            JMenuItem menuItem = menuKeyStrokes.get(keyStroke);
            if (menuItem != null) {
                actionPerformed(new ActionEvent(menuItem, 0, null));
                e.consume();
                return;
            }
            super.keyPressed(e);
        }
    };

    /**
     * Set main component that will be listen key codes to fix issue with not workings menu accelerators
     *
     * @param comp
     * @param menuBar
     */
    public void setMainKeyListener(Component comp, JMenuBar menuBar) {
        fillMenuKeyStrokes(menuBar);
        comp.addKeyListener(mainKeyListener);
    }

    private boolean isProblemKey(KeyStroke keyStroke) {
        if (keyStroke == null) {
            return false;
        }
        int keyCode = keyStroke.getKeyCode();
        return (keyCode >= KeyEvent.VK_F1 && keyCode <= KeyEvent.VK_F12) ||
                (keyCode >= KeyEvent.VK_LEFT && keyCode <= KeyEvent.VK_DOWN) ||
                keyCode == KeyEvent.VK_ENTER || keyCode == KeyEvent.VK_TAB;
    }


    /**
     * Fills map for all keycodes that can be not processed properly in swing
     *
     * @param menuBar
     */
    private void fillMenuKeyStrokes(JMenuBar menuBar) {
        menuKeyStrokes = new HashMap<>();
        for (int menuIndex = 0; menuIndex < menuBar.getMenuCount(); menuIndex++) {
            JMenu menu = menuBar.getMenu(menuIndex);
            for (int itemIndex = 0; itemIndex < menu.getItemCount(); itemIndex++) {
                JMenuItem menuItem = menu.getItem(itemIndex);
                if (menuItem == null) {
                    continue;
                }
                KeyStroke keyStroke = menuItem.getAccelerator();
                if (isProblemKey(keyStroke)) {
                    menuKeyStrokes.put(keyStroke, menuItem);
                }
            }
        }
    }
}
