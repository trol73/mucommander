/*
 * This file is part of trolCommander, http://www.trolsoft.ru/en/trolcommander
 * Copyright (C) 2014-2026 Oleg Trifonov
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
package com.mucommander.ui.action.impl;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.ui.action.*;
import com.mucommander.ui.main.MainFrame;
import com.mucommander.ui.main.menu.UserPopupMenu;
import com.mucommander.ui.main.menu.usermenu.LoadUserMenuException;
import com.mucommander.ui.main.menu.usermenu.UserPopupMenuLoader;
import com.mucommander.ui.notifier.AbstractNotifier;
import com.mucommander.ui.notifier.NotificationType;
import com.mucommander.ui.viewer.EditorRegistrar;
import com.mucommander.ui.viewer.text.TextEditor;
import com.mucommander.ui.viewer.text.TextFilesHistory;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.Map;

@Slf4j
public class UserMenuAction extends ParentFolderAction {

    private UserMenuAction(MainFrame mainFrame, Map<String, Object> properties) {
        super(mainFrame, properties);
    }

    @Override
    protected void toggleEnabledState() {}

    @Override
    public void performAction() {
        UserPopupMenu menu = createMenu(mainFrame);
        if (menu != null) {
            menu.show(mainFrame.getJFrame());
        }
    }

    public static UserPopupMenu createMenu(MainFrame mainFrame) {
        AbstractFile currentFolder = mainFrame.getActiveTable().getFileTableModel().getCurrentFolder();
        AbstractFile localMenu = findLocalMenu(currentFolder);
        if (localMenu != null) {
            try {
                return UserPopupMenuLoader.loadMenu(mainFrame, localMenu);
            } catch (LoadUserMenuException ej) {
                openEditorAndShowError(mainFrame, localMenu, ej);
            } catch (IOException e) {
                // TODO status bar
                log.error("Load menu exception", e);
                AbstractNotifier notifier = AbstractNotifier.getNotifier();
                if (notifier != null) {
                    notifier.displayNotification(NotificationType.JOB_ERROR, "Error", e.getMessage());
                }
            } catch (Throwable t) {
                log.error("Load menu error", t);
            }
        }
        return null;
    }

    private static AbstractFile findLocalMenu(AbstractFile folder) {
        if (folder == null) {
            return null;
        }
        AbstractFile menu = getMenuFile(folder);
        return menu != null ? menu : findLocalMenu(folder.getParent());
    }

    private static AbstractFile getMenuFile(AbstractFile folder) {
        if (folder == null || !folder.exists()) {
            return null;
        }
        try {
            AbstractFile result = folder.getChild(".trolcommander-menu.yml");
            if (result != null && result.exists()) {
                return result;
            }
            result = folder.getChild(".trolcommander-menu.yaml");
            return result != null && result.exists() ? result : null;
        } catch (IOException e) {
            return null;
        }
    }

    private static void openEditorAndShowError(MainFrame mainFrame, AbstractFile localMenu, LoadUserMenuException e) {
        TextFilesHistory.FileRecord historyRecord = TextFilesHistory.getInstance().get(localMenu);
        historyRecord.update(e.getLine(), e.getLine(), e.getColumn(), historyRecord.getFileType(), historyRecord.getEncoding());

        Image image = ActionProperties.getActionIcon(EditAction.Descriptor.ACTION_ID).getImage();
        log.info("open frame {} {}:{}", e.getMessage(), e.getLine(), e.getColumn());
        EditorRegistrar.createEditorFrame(mainFrame, localMenu, image,
                (fileFrame) -> {
                    TextEditor textEditor = (TextEditor)fileFrame.getFilePresenter();
                    textEditor.getStatusBar().showMessage(e.getMessage(), 1000);
                });
    }


    @Override
    public ActionDescriptor getDescriptor() {
        return new UserMenuAction.Descriptor();
    }


    public static final class Descriptor extends AbstractActionDescriptor {
        public static final String ACTION_ID = "UserMenu";

        public String getId() {
            return ACTION_ID;
        }

        public ActionCategory getCategory() {
            return ActionCategory.COMMANDS;
        }

        public KeyStroke getDefaultAltKeyStroke() {
            return null;
        }

        public KeyStroke getDefaultKeyStroke() {
            return KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0);
        }

        public TcAction createAction(MainFrame mainFrame, Map<String, Object> properties) {
            return new UserMenuAction(mainFrame, properties);
        }
    }
}
