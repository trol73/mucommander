/*
 * This file is part of trolCommander, http://www.trolsoft.ru/en/soft/trolcommander
 * Copyright (C) 2013-2018 Oleg Trifonov
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
package com.mucommander.ui.main.quicklist;

import com.mucommander.cache.TextHistory;
import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.DummyFile;
import com.mucommander.commons.file.FileFactory;
import com.mucommander.ui.action.ActionProperties;
import com.mucommander.ui.action.MuAction;
import com.mucommander.ui.action.impl.EditAction;
import com.mucommander.ui.action.impl.ShowEditorBookmarksQLAction;
import com.mucommander.ui.main.FolderPanel;
import com.mucommander.ui.main.MainFrame;
import com.mucommander.ui.quicklist.QuickListWithIcons;
import com.mucommander.ui.viewer.EditorRegistrar;
import com.mucommander.utils.text.Translator;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class EditorBookmarksQL extends QuickListWithIcons<AbstractFile> {

    private static final LinkedList<AbstractFile> LIST = new LinkedList<>();

    private static final int MAX_FILES_IN_LIST = 500;

    private final MainFrame mainFrame;


    public EditorBookmarksQL(FolderPanel folderPanel)  {
        super(folderPanel, ActionProperties.getActionLabel(ShowEditorBookmarksQLAction.Descriptor.ACTION_ID),
                Translator.get("editor_bookmarks_quick_list.empty_message"));
        mainFrame = folderPanel.getMainFrame();
    }

    @Override
    protected Icon itemToIcon(AbstractFile item) {
        return MuAction.getStandardIcon(EditAction.class);
    }

    @Override
    protected AbstractFile[] getData() {
        List<String> list =  TextHistory.getInstance().getList(TextHistory.Type.EDITOR_BOOKMARKS);
        AbstractFile[] result = new AbstractFile[list.size()];
        for (int i = 0; i < list.size(); i++) {
            result[i] = FileFactory.getFile(list.get(i));
        }
        return result;
    }

    @Override
    protected void acceptListItem(AbstractFile item) {
        if (item instanceof DummyFile) {
            item = FileFactory.getFile(item.getURL());
        }
        if (item != null && item.exists()) {
            openFileInEditor(item);
        } else {
            // TODO error message
        }
    }

    private void openFileInEditor(AbstractFile file) {
        EditorRegistrar.createEditorFrame(mainFrame, file, ActionProperties.getActionIcon(EditAction.Descriptor.ACTION_ID).getImage());
    }

    public static void addFile(AbstractFile file) {
        if (!LIST.remove(file) && LIST.size() > MAX_FILES_IN_LIST) {
            LIST.removeLast();
        }
        LIST.addFirst(file);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_F4 && e.getModifiers() == 0) {
            e.consume();
            openBookmarkFileInEditor();
        }
        super.keyPressed(e);
    }

    private void openBookmarkFileInEditor() {
        try {
            AbstractFile file = TextHistory.getHistoryFile(TextHistory.Type.EDITOR_BOOKMARKS);
            setVisible(false);
            SwingUtilities.invokeLater(() -> openFileInEditor(file));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
