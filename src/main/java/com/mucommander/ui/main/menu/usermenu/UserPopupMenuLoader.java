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
package com.mucommander.ui.main.menu.usermenu;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.ui.helper.MnemonicHelper;
import com.mucommander.ui.main.MainFrame;
import com.mucommander.ui.main.menu.UserPopupMenu;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.Yaml;
import ru.trolsoft.ui.TMenuSeparator;

import javax.swing.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class UserPopupMenuLoader {

    public static UserPopupMenu loadMenu(MainFrame mainFrame, AbstractFile file) throws IOException, LoadUserMenuException {
        try (InputStream is = file.getInputStream()) {
            Yaml yaml = new Yaml();
            Map<String, Object> root = yaml.load(is);
            if (root == null || !root.containsKey("menu")) {
                throw new LoadUserMenuException("Invalid YAML structure: 'menu' key not found");
            }
            @SuppressWarnings("unchecked")
            List<Object> items = (List<Object>) root.get("menu");
            MnemonicHelper mnemonicHelper = new MnemonicHelper();
            UserPopupMenu menu = new UserPopupMenu(mainFrame, file);
            loadMenu(menu, null, items, mnemonicHelper);
            return menu;
        } catch (LoadUserMenuException e) {
            throw e;
        } catch (Exception e) {
            throw new LoadUserMenuException(e.getMessage());
        }
    }

    private static void loadMenu(UserPopupMenu menu, JMenu parent, List<Object> items, MnemonicHelper mnemonicHelper) throws LoadUserMenuException {
        if (items == null) {
            return;
        }
        for (Object obj : items) {
            if (obj instanceof String objs && objs.equalsIgnoreCase("separator")) {
                menu.add(new TMenuSeparator());
            } else if (obj instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> item = (Map<String, Object>) obj;
                String name = getItemProp(item, "name");
                Object subItems = item.get("items");
                String key = getItemProp(item, "key");
                if (name != null && subItems != null) { // Submenu
                    JMenu submenu = new JMenu(name);
                    if (key != null && !key.isEmpty()) {
                        submenu.setMnemonic(KeyStroke.getKeyStroke(key).getKeyCode());
                    } else {
                        submenu.setMnemonic(mnemonicHelper.getMnemonic(name));
                    }
                    if (parent == null) {
                        menu.add(submenu);
                    } else {
                        parent.add(submenu);
                    }
                    @SuppressWarnings("unchecked")
                    List<Object> subItemsList = (List<Object>) subItems;
                    loadMenu(menu, submenu, subItemsList, new MnemonicHelper());
                } else if (name != null) {
                    UserMenuItem.Command command = getItemCommand(item);
                    String console = getItemProp(item, "console");
                    UserMenuItem properties = new UserMenuItem(command, UserMenuItem.ConsoleType.fromStr(console));
                    JMenuItem mi = menu.add(parent, name, properties);
                    mi.setMnemonic(mnemonicHelper.getMnemonic(name));
                    if (key != null) {
                        KeyStroke keyStroke = KeyStroke.getKeyStroke(key);
                        mi.setAccelerator(keyStroke);
                    }
                } else {
                    throw new LoadUserMenuException("Invalid item type at index: " + items.indexOf(obj) + ", '" + obj + "'");
                }
            } else {
                throw new LoadUserMenuException("Invalid item type at index: " + items.indexOf(obj) + ", '" + obj + "'");
            }
        }
    }

    private static UserMenuItem.Command getItemCommand(Map<String, Object> item) {
        if (!item.containsKey("command")) {
            return null;
        }
        Object cmd = item.get("command");
        if (cmd instanceof String) {
            return new UserMenuItem.Command((String) cmd);
        } else if (cmd instanceof List) {
            List<List<String>> result = new ArrayList<>();
            @SuppressWarnings("unchecked")
            List<Object> cmdList = (List<Object>) cmd;

            boolean containsArrays = listContainsLists(cmdList);
            if (containsArrays) {
                for (Object o : cmdList) {
                    List<String> group = new ArrayList<>();
                    result.add(group);
                    if (o instanceof List) {
                        @SuppressWarnings("unchecked")
                        List<Object> groupList = (List<Object>) o;
                        for (Object c : groupList) {
                            if (c instanceof String) {
                                group.add((String) c);
                            } else {
                                throw new RuntimeException("invalid command type: " + item);
                            }
                        }
                    }
                }
            } else {
                List<String> group = new ArrayList<>();
                result.add(group);
                for (Object c : cmdList) {
                    if (c instanceof String) {
                        group.add((String) c);
                    } else {
                        throw new RuntimeException("invalid command type: " + item);
                    }
                }
            }
            return new UserMenuItem.Command(result);
        }
        throw new RuntimeException("invalid command type: " + item);
    }

    private static boolean listContainsLists(List<Object> list) {
        for (Object o : list) {
            if (o instanceof List) {
                return true;
            }
        }
        return false;
    }

    @Nullable
    private static String getItemProp(Map<String, Object> item, String name) {
        Object value = item.get(name);
        return value != null ? value.toString() : null;
    }
}