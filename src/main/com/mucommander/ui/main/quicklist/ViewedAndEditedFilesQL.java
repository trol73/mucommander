/*
 * This file is part of trolCommander, http://www.trolsoft.ru/en/soft/trolcommander
 * Copyright (C) 2013-2016 Oleg Trifonov
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

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.FileFactory;
import com.mucommander.commons.runtime.OsFamily;
import com.mucommander.text.Translator;
import com.mucommander.ui.quicklist.QuickListContainer;
import com.mucommander.ui.quicklist.QuickListWithIcons;
import com.mucommander.ui.viewer.FileFrame;
import com.mucommander.ui.viewer.FileViewersList;

import javax.swing.Icon;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;

/**
 * @author Oleg Trifonov
 * Created on 28/07/16.
 */
public class ViewedAndEditedFilesQL extends QuickListWithIcons<AbstractFile> {

    private List<FileViewersList.FileRecord> files = FileViewersList.getFiles();
    private AbstractFile currentFile;
    private int currentFileIndex = -1;

    public ViewedAndEditedFilesQL(QuickListContainer container, AbstractFile currentFile) {
        super(container, Translator.get("file_editor.files.list"), "");
        this.currentFile = currentFile;

        dataList.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_TAB) {
                    selectNext();
                    e.consume();
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                int mask = KeyEvent.CTRL_MASK;//OsFamily.getCurrent() == OsFamily.MAC_OS_X ? KeyEvent.ALT_MASK : KeyEvent.CTRL_MASK;
                if (e.getKeyCode() == mask) {
                    setVisible(false);
                    acceptListItem(dataList.getSelectedValue());
                }
            }
        });
    }

    @Override
    protected Icon itemToIcon(AbstractFile item) {
        for (FileViewersList.FileRecord rec : files) {
            if (rec.fileName.equals(item.getAbsolutePath())) {
                return rec.getIcon();
            }
        }
        return null;
    }

    @Override
    protected AbstractFile[] getData() {
        AbstractFile[] result = new AbstractFile[files.size()];
        for (int i = 0 ; i < files.size(); i++) {
            result[i] = FileFactory.getFile(files.get(i).fileName);
            if (result[i].equals(currentFile)) {
                currentFileIndex = i;
            }
        }
        return result;
    }

    @Override
    public void show() {
        super.show();
        if (currentFileIndex >= 0) {
            dataList.setSelectedIndex(currentFileIndex);
        }
    }


    @Override
    protected void acceptListItem(AbstractFile item) {
        if (item == dataList.getSelectedValue()) {
            FileFrame frame = files.get(dataList.getSelectedIndex()).fileFrameRef.get();
            if (frame != null) {
                frame.toFront();
                return;
            }
        }
        // theoretically these next code will execute newer
        for (FileViewersList.FileRecord rec : files) {
            if (rec.fileName.equals(item.getAbsolutePath())) {
                FileFrame frame = rec.fileFrameRef.get();
                if (frame != null) {
                    frame.toFront();
                }
                return;
            }
        }
    }


    public void selectNext() {
        int selected = dataList.getSelectedIndex();
        selected++;
        if (selected >= dataList.getModel().getSize()) {
            selected = 0;
        }
        dataList.setSelectedIndex(selected);
    }
}
