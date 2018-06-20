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
package com.mucommander.ui.helper;

import com.mucommander.ui.action.MuAction;
import com.mucommander.ui.menu.JScrollMenu;
import ru.trolsoft.ui.TCheckBoxMenuItem;
import ru.trolsoft.ui.TRadioButtonMenuItem;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.KeyStroke;
import javax.swing.event.MenuListener;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

/**
 * MenuToolkit provides convenient methods that make life easier
 * when creating menus.
 *
 * @author Maxence Bernard
 */
public class MenuToolkit {

    private enum MenuType {
        ITEM, CHECKBOX, RADIOBUTTON
    }

    private MenuToolkit() {

    }

    /**
     * Creates and returns a new JMenu.
     *
     * @param title          title of the menu
     * @param mnemonicHelper an optional (can be null) mnemonic helper which will be used along with
     *                       the title to set a mnemonic to the menu.
     * @param menuListener   an optional (can be null) menu listener which will listen to the events triggered by the menu.
     */
    public static JMenu addMenu(String title, MnemonicHelper mnemonicHelper, MenuListener menuListener) {
        JMenu menu = new JMenu(title);
        initMenu(menu, title, mnemonicHelper, menuListener);
        return menu;
    }

    /**
     * Creates and returns a new JScrollMenu.
     *
     * @param title          title of the menu
     * @param mnemonicHelper an optional (can be null) mnemonic helper which will be used along with
     *                       the title to set a mnemonic to the menu.
     * @param menuListener   an optional (can be null) menu listener which will listen to the events triggered by the menu.
     */
    public static JScrollMenu addScrollableMenu(String title, MnemonicHelper mnemonicHelper, MenuListener menuListener) {
        final JScrollMenu menu = new JScrollMenu(title);
        initMenu(menu, title, mnemonicHelper, menuListener);
        return menu;
    }

    private static void initMenu(JMenu menu, String title, MnemonicHelper mnemonicHelper, MenuListener menuListener) {
        if (mnemonicHelper != null) {
            char mnemonic = mnemonicHelper.getMnemonic(title);
            if (mnemonic != 0)
                menu.setMnemonic(mnemonic);
        }
        if (menuListener != null) {
            menu.addMenuListener(menuListener);
        }
    }

    /**
     * Creates a new JMenuItem and adds it to the given JMenu.
     *
     * @param menu           menu to add the menu item to.
     * @param text           text used by the menu item.
     * @param mnemonicHelper an optional (can be null) mnemonic helper which will be used along with
     *                       the item's text to set a mnemonic to the menu.
     * @param accelerator    an optional (can be null) keyboard shortcut used by the menu item.
     * @param actionListener an optional (can be null) action listener which will listen to the events triggered by the menu item.
     */
    public static JMenuItem addMenuItem(JMenu menu, String text, MnemonicHelper mnemonicHelper, KeyStroke accelerator, ActionListener actionListener) {
        return addMenuItem(menu, text, mnemonicHelper, accelerator, actionListener, MenuType.ITEM);
    }

    /**
     * Creates a new JCheckBoxMenuItem initially unselected and adds it to the given JMenu.
     *
     * @param menu           menu to add the menu item to.
     * @param text           text used by the menu item.
     * @param mnemonicHelper an optional (can be null) mnemonic helper which will be used along with
     *                       the item's text to set a mnemonic to the menu.
     * @param accelerator    an optional (can be null) keyboard shortcut used by the menu item.
     * @param actionListener an optional (can be null) action listener which will listen to the events triggered by the menu item.
     */
    public static JCheckBoxMenuItem addCheckBoxMenuItem(JMenu menu, String text, MnemonicHelper mnemonicHelper, KeyStroke accelerator, ActionListener actionListener) {
        return (JCheckBoxMenuItem) addMenuItem(menu, text, mnemonicHelper, accelerator, actionListener, MenuType.CHECKBOX);
    }

    /**
     * Creates a new JRadioButtonMenuItem initially unselected and adds it to the given JMenu.
     *
     * @param menu           menu to add the menu item to.
     * @param text           text used by the menu item.
     * @param mnemonicHelper an optional (can be null) mnemonic helper which will be used along with
     *                       the item's text to set a mnemonic to the menu.
     * @param accelerator    an optional (can be null) keyboard shortcut used by the menu item.
     * @param actionListener an optional (can be null) action listener which will listen to the events triggered by the menu item.
     */
    public static JRadioButtonMenuItem addRadioButtonMenuItem(JMenu menu, String text, MnemonicHelper mnemonicHelper, KeyStroke accelerator, ActionListener actionListener) {
        return (JRadioButtonMenuItem) addMenuItem(menu, text, mnemonicHelper, accelerator, actionListener, MenuType.RADIOBUTTON);
    }

    /**
     * Creates a new JMenuItem or JCheckBoxMenuItem and adds it to the given JMenu.
     *
     * @param menu           menu to add the menu item to.
     * @param text           text used by the menu item.
     * @param mnemonicHelper an optional (can be null) mnemonic helper which will be used along with
     *                       the item's text to set a mnemonic to the menu.
     * @param accelerator    an optional (can be null) keyboard shortcut used by the menu item.
     * @param actionListener an optional (can be null) action listener which will listen to the events triggered by the menu item.
     * @param menuType       specifies whether the menu item to be created is a JCheckBoxMenuItem, JRadioButtomMenuItem or just a regular JMenuItem.
     */
    private static JMenuItem addMenuItem(JMenu menu, String text, MnemonicHelper mnemonicHelper, KeyStroke accelerator, ActionListener actionListener, MenuType menuType) {
        final JMenuItem menuItem;
        switch (menuType) {
            case CHECKBOX:
                menuItem = new TCheckBoxMenuItem(text, false);
                break;
            case RADIOBUTTON:
                menuItem = new TRadioButtonMenuItem(text, false);
                break;
            case ITEM:
            default:
                menuItem = new JMenuItem(text);
                break;
        }

        if (mnemonicHelper != null) {
            char mnemonic = mnemonicHelper.getMnemonic(text);
            if (mnemonic != 0)
                menuItem.setMnemonic(mnemonic);
        }

        if (accelerator != null)
            menuItem.setAccelerator(accelerator);

        if (actionListener != null)
            menuItem.addActionListener(actionListener);

        menu.add(menuItem);

        return menuItem;
    }

    /**
     * Does things that should be done to all menu items created from
     * <code>MuAction</code>s.
     * <ol>
     * <li>If the provided action has an icon, it would by default get displayed in the menu item.
     * Since icons have nothing to do in menus, let's make sure the menu item has no icon.</li>
     * <li>If the action has a keyboard shortcut that conflicts with
     * the menu's internal ones (enter, space and escape), they will
     * not be used.</li>
     * </ol>
     *
     * @param item menu item to take care of.
     */
    public static void configureActionMenuItem(JMenuItem item) {
        item.setIcon(null);

        KeyStroke stroke = item.getAccelerator();
        if (stroke != null && stroke.getModifiers() == 0 &&
                (stroke.getKeyCode() == KeyEvent.VK_ENTER || stroke.getKeyCode() == KeyEvent.VK_SPACE || stroke.getKeyCode() == KeyEvent.VK_ESCAPE))
            item.setAccelerator(null);
    }

    public static JMenuItem addMenuItem(JMenu menu, MuAction action, MnemonicHelper mnemonicHelper) {
        return addMenuItem(menu, action, mnemonicHelper, MenuType.ITEM);
    }

    public static JCheckBoxMenuItem addCheckBoxMenuItem(JMenu menu, MuAction action, MnemonicHelper mnemonicHelper) {
        return (JCheckBoxMenuItem) addMenuItem(menu, action, mnemonicHelper, MenuType.CHECKBOX);
    }

    public static JRadioButtonMenuItem addRadioButtonMenuItem(JMenu menu, MuAction action, MnemonicHelper mnemonicHelper) {
        return (JRadioButtonMenuItem) addMenuItem(menu, action, mnemonicHelper, MenuType.RADIOBUTTON);
    }

    private static JMenuItem addMenuItem(JMenu menu, MuAction action, MnemonicHelper mnemonicHelper, MenuType menuType) {
        final JMenuItem menuItem;
        switch (menuType) {
            case CHECKBOX:
                menuItem = new TCheckBoxMenuItem(action);
                break;
            case RADIOBUTTON:
                menuItem = new TRadioButtonMenuItem(action);
                break;
            case ITEM:
            default:
                menuItem = new JMenuItem(action);
                break;
        }

        if (mnemonicHelper != null && action != null) {
            char mnemonic = mnemonicHelper.getMnemonic(action.getLabel());
            if (mnemonic != 0) {
                menuItem.setMnemonic(mnemonic);
            }
        }

        // If the provided action has an icon, it would by default get displayed in the menu item.
        // Since icons have nothing to do in menus, let's make sure the menu item has no icon. 
        menuItem.setIcon(null);

        menu.add(menuItem);

        return menuItem;
    }

}
