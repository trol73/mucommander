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

import com.mucommander.text.Translator;
import com.mucommander.ui.helper.MenuToolkit;
import com.mucommander.ui.helper.MnemonicHelper;

import javax.swing.*;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;

/**
 * An abstract class to be subclassed by file viewer implementations.
 *
 * <p><b>Warning:</b> the file viewer/editor API may soon receive a major overhaul.</p>
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
    private JMenuItem closeItem;
    
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
        MnemonicHelper menuMnemonicHelper = new MnemonicHelper();
        MnemonicHelper menuItemMnemonicHelper = new MnemonicHelper();

        // File menu
        JMenu fileMenu = MenuToolkit.addMenu(Translator.get("file_viewer.file_menu"), menuMnemonicHelper, null);
        closeItem = MenuToolkit.addMenuItem(fileMenu, Translator.get("file_viewer.close"), menuItemMnemonicHelper, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), this);
        fileMenu.add(closeItem);
        
        menuBar.add(fileMenu);

        return menuBar;
    }
    
    ///////////////////////////////////
    // ActionListener implementation //
    ///////////////////////////////////

    public void actionPerformed(ActionEvent e) {
        if(e.getSource()==closeItem)
            getFrame().dispose();
    }


    /**
     * Set main component that will be listen key codes to fix issue with not workings menu accelerators
     *
     * @param comp
     * @param menuBar
     */
    protected void setMainKeyListener(Component comp, JMenuBar menuBar) {
        fillMenuKeyStrokes(menuBar);
        comp.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                KeyStroke keyStrole = KeyStroke.getKeyStroke(e.getKeyCode(), e.getModifiers(), false);
                JMenuItem menuItem = menuKeyStrokes.get(keyStrole);
                if (menuItem != null) {
                    actionPerformed(new ActionEvent(menuItem, 0, null));
                    e.consume();
                    return;
                }
                super.keyPressed(e);
            }
        });

    }

    /**
     * Fills map for all keycodes that can be not processed properly in swing
     *
     * @param menuBar
     */
    protected void fillMenuKeyStrokes(JMenuBar menuBar) {
        menuKeyStrokes = new HashMap<KeyStroke, JMenuItem>();
        for (int menuIndex = 0; menuIndex < menuBar.getMenuCount(); menuIndex++) {
            JMenu menu = menuBar.getMenu(menuIndex);
            for (int itemIndex = 0; itemIndex < menu.getItemCount(); itemIndex++) {
                JMenuItem menuItem = menu.getItem(itemIndex);
                if (menuItem == null) {
                    continue;
                }
                KeyStroke keyStroke = menuItem.getAccelerator();
                if (keyStroke == null) {
                    continue;
                }
                int keyCode = keyStroke.getKeyCode();
                if ((keyCode >= KeyEvent.VK_F1 && keyCode <= KeyEvent.VK_F12) || (keyCode >= KeyEvent.VK_LEFT && keyCode <= KeyEvent.VK_DOWN)
                        || keyCode == KeyEvent.VK_ENTER) {
                    menuKeyStrokes.put(keyStroke, menuItem);
                }
            }
        }
    }
}
