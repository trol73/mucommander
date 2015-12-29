/*
 * This file is part of trolCommander, http://www.trolsoft.ru/soft/trolcommander
 * Copyright (C) 2013-2015 Oleg Trifonov
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
package com.mucommander.adb;


import com.mucommander.text.Translator;
import com.mucommander.ui.action.MuAction;
import com.mucommander.ui.helper.MnemonicHelper;
import com.mucommander.ui.icon.IconManager;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import java.util.List;

/**
 * An abstract JMenu that contains an item for each Android ADB devices available
 *
 * <p>Note: the items list is refreshed each time the menu is selected. In other words, a new instance of AdbMenu
 * does not have to be created in order to see new devices.</p>
 *
 * Created on 28/12/15.
 * @author Oleg Trifonov
 */
public abstract class AndroidMenu extends JMenu implements MenuListener {

    /**
     * Creates a new instance of <code>AndroidMenu</code>.
     */
    public AndroidMenu() {
        super(Translator.get("adb.android_devices"));

        setIcon(IconManager.getIcon(IconManager.IconSet.FILE, "android.png"));

        // Menu items will be added when menu gets selected
        addMenuListener(this);
    }

    /**
     * Returns the action to perform for the given item.
     *
     * @param deviceSerial the serial number of the device
     * @return the action to perform for the given Android device
     */
    public abstract MuAction getMenuItemAction(String deviceSerial);


    @Override
    public void menuSelected(MenuEvent e) {
        // Remove previous menu items (if any)
        removeAll();

        List<String> androidDevices = AdbUtils.getDevices();
        if (androidDevices == null) {
            return;
        }
        if (androidDevices.isEmpty()) {
            add(new JMenuItem(Translator.get("adb.no_devices"))).setEnabled(false);
            return;
        }
        MnemonicHelper mnemonicHelper = new MnemonicHelper();
        for (String serial : androidDevices) {
            JMenuItem menuItem = new JMenuItem(getMenuItemAction(serial));
            menuItem.setMnemonic(mnemonicHelper.getMnemonic(menuItem.getText()));
            String name = AdbUtils.getDeviceName(serial);
            menuItem.setText(name == null ? serial : name);
            menuItem.setIcon(IconManager.getIcon(IconManager.IconSet.FILE, "android.png"));

            add(menuItem);
        }
    }

    @Override
    public void menuDeselected(MenuEvent e) {

    }

    @Override
    public void menuCanceled(MenuEvent e) {

    }
}
