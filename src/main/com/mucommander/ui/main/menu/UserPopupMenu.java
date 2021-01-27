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
package com.mucommander.ui.main.menu;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.runtime.OsFamily;
import com.mucommander.conf.TcConfigurations;
import com.mucommander.conf.TcPreference;
import com.mucommander.conf.TcPreferences;
import com.mucommander.desktop.osx.OSXTerminal;
import com.mucommander.process.ExecutorUtils;
import com.mucommander.ui.action.ActionProperties;
import com.mucommander.ui.action.impl.EditAction;
import com.mucommander.ui.main.MainFrame;
import com.mucommander.ui.main.menu.usermenu.UserMenuItem;
import com.mucommander.ui.viewer.EditorRegistrar;
import com.mucommander.utils.text.Translator;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserPopupMenu extends JPopupMenu implements ActionListener, PopupMenuListener {


    private final MainFrame mainFrame;
    private final AbstractFile menuFile;
    private Map<JMenuItem, UserMenuItem> propertiesMap = new HashMap<>();
    private JMenuItem firstItem;


    public UserPopupMenu(MainFrame mainFrame, AbstractFile file) {
        this.mainFrame = mainFrame;
        this.menuFile = file;

        addPopupMenuListener(this);
    }

    public JMenuItem add(JMenu parent, String name, UserMenuItem properties) {
        JMenuItem item = new JMenuItem(name);
        if (parent == null) {
            add(item);
        } else {
            parent.add(item);
        }
        propertiesMap.put(item, properties);
        item.addActionListener(this);
        if (firstItem == null) {
            firstItem = item;
        }
        return item;
    }

    private void selectFirstItem() {
        if (firstItem != null) {
            SwingUtilities.invokeLater(() -> MenuSelectionManager.defaultManager().setSelectedPath(
                    new MenuElement[] {this, firstItem})
            );
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object menuItem = e.getSource();
        if (menuItem instanceof JMenuItem) {
            performCommand(propertiesMap.get(menuItem));
        }
    }

    private void performCommand(UserMenuItem properties) {
        if (properties.command == null) {
            mainFrame.getStatusBar().setStatusInfo(Translator.get("UserMenu.command_not_defined"));
            return;
        }
        switch (properties.console) {
            case NONE:
                executeInBackground(properties);
                break;
            case SHOW:
            case HIDE:
                executeInTerminal(properties);
                break;
            case APPEND:
                executeInNewTerminalTabs(properties);
                break;
        }
    }

    private void executeInNewTerminalTabs(UserMenuItem properties) {
        if (properties.command.isSingle()) {
            OSXTerminal.addNewTabWithCommands(menuFile.getParent(), properties.command.singleCommand);
        } else {
            for (List<String> group : properties.command.commandsList) {
                String[] list = new String[group.size()];
                OSXTerminal.addNewTabWithCommands(menuFile.getParent(), group.toArray(list));
                sleep(200);
            }
        }
    }

    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    private void executeInBackground(UserMenuItem properties) {
        final AbstractFile folder = mainFrame.getActivePanel().getCurrentFolder();
        UserMenuItem.Command cmd = properties.command;
        if (cmd.isSingle()) {
            new Thread(() -> {
                try {
                    ExecutorUtils.execute(cmd.singleCommand, folder);
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        } else {
            for (List<String> group : cmd.commandsList) {
                new Thread(() -> {
                    try {
                        for (String command : group) {
                            ExecutorUtils.execute(command, folder);
                        }
                    } catch (IOException | InterruptedException e) {
                        e.printStackTrace();
                    }
                }).start();
            }
        }
    }

    private void executeInTerminal(UserMenuItem properties) {
        if (properties.command.isSingle()) {
            OSXTerminal.openNewWindowAndRun(menuFile.getParent(), properties.command.singleCommand);
        } else {
            for (List<String> group : properties.command.commandsList) {
                String[] list = new String[group.size()];
                OSXTerminal.openNewWindowAndRun(menuFile.getParent(), group.toArray(list));
                sleep(200);
            }
        }
    }

    private static String getDefaultTerminalCommand() {
        switch (OsFamily.getCurrent()) {
            case WINDOWS:
                return "cmd /c start cmd.exe /K \"cd /d $p\"";
            case LINUX:
                return "";
            case MAC_OS_X:
                return "open -a Terminal .";
        }
        return "";
    }

    private static String getConsoleCommand(AbstractFile folder) {
        String cmd = getTerminalCommand();
        return cmd.replace("$p", folder.getAbsolutePath());
    }

    private static String getTerminalCommand() {
        if (useCustomExternalTerminal()) {
            return getCustomExternalTerminal();
        } else {
            return getDefaultTerminalCommand();
        }
    }

    private static String getCustomExternalTerminal() {
        return TcConfigurations.getPreferences().getVariable(TcPreference.CUSTOM_EXTERNAL_TERMINAL);
    }

    private static boolean useCustomExternalTerminal() {
        return TcConfigurations.getPreferences().getVariable(TcPreference.USE_CUSTOM_EXTERNAL_TERMINAL, TcPreferences.DEFAULT_USE_CUSTOM_EXTERNAL_TERMINAL);
    }



    @Override
    public void processKeyEvent(KeyEvent e, MenuElement[] path, MenuSelectionManager manager) {
        if (e.getKeyCode() == KeyEvent.VK_F4 && e.getModifiers() == 0 && e.getID() == KeyEvent.KEY_PRESSED) {
            openEditor();
            e.consume();
            return;
        }
        super.processKeyEvent(e, path, manager);
    }


    public void show(Component invoker) {
        Dimension size = getPreferredSize();
        int x = (invoker.getWidth() - size.width)/2;
        int y = (invoker.getHeight() - size.height)/2;
        show(invoker, x, y);
        selectFirstItem();
        requestFocus();
    }

    private void openEditor() {
        EditorRegistrar.createEditorFrame(mainFrame, menuFile, ActionProperties.getActionIcon(EditAction.Descriptor.ACTION_ID).getImage());
    }

    @Override
    public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
        mainFrame.getStatusBar().setStatusInfo(Translator.get("UserMenu.press_f4_to_edit_menu"));
    }

    @Override
    public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
        mainFrame.getStatusBar().activePanelChanged(mainFrame.getActivePanel());
    }

    @Override
    public void popupMenuCanceled(PopupMenuEvent e) {

    }


}
