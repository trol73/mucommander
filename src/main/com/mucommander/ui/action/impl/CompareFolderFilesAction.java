/*
 * This file is part of trolCommander, http://www.trolsoft.ru/en/soft/trolcommander
 * Copyright (C) 2013-2017 Oleg Trifonov
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
package com.mucommander.ui.action.impl;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.ui.action.AbstractActionDescriptor;
import com.mucommander.ui.action.ActionCategory;
import com.mucommander.ui.action.ActionDescriptor;
import com.mucommander.ui.action.MuAction;
import com.mucommander.ui.main.MainFrame;
import com.mucommander.ui.main.table.FileTable;
import com.mucommander.ui.main.table.views.BaseFileTableModel;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

/**
 * This action compares the content of the 2 MainFrame's file tables and marks the files with different size or content.
 *
 * Created on 10/07/17.
 * @author Oleg Trifonov
 */

public class CompareFolderFilesAction extends MuAction {

    CompareFolderFilesAction(MainFrame mainFrame, Map<String, Object> properties) {
        super(mainFrame, properties);
    }

    @Override
    public void performAction() {
        FileTable activeTable = mainFrame.getActiveTable();
        FileTable inactiveTableTable = mainFrame.getInactiveTable();

        BaseFileTableModel activeTableModel = activeTable.getFileTableModel();
        BaseFileTableModel inactiveTableModel = inactiveTableTable.getFileTableModel();

        if (compare(activeTableModel, inactiveTableModel)) {
            activeTable.repaint();
        }

        // Notify registered listeners that currently marked files have changed on the file tables
        activeTable.fireMarkedFilesChangedEvent();
    }

    private boolean compare(BaseFileTableModel firstTableModel, BaseFileTableModel secondTableModel) {
        boolean result = false;
        int nbFilesFirst = firstTableModel.getFileCount();
        int nbFilesSecond = secondTableModel.getFileCount();

        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return false;
        }
        for (int i = 0; i < nbFilesFirst; i++) {
            AbstractFile tempFile = firstTableModel.getFileAt(i);
            if (tempFile.isDirectory()) {
                continue;
            }

            String tempFileName = tempFile.getName();
            int fileIndex = -1;
            for (int j = 0; j < nbFilesSecond; j++) {
                if (secondTableModel.getFileAt(j).getName().equals(tempFileName)) {
                    fileIndex = j;
                    break;
                }
            }
            if (fileIndex < 0 || !checkEqual(digest, secondTableModel.getFileAt(fileIndex), tempFile)) {
                firstTableModel.setFileMarked(tempFile, true);
                result = true;
            }
        }
        return result;
    }

    private boolean checkEqual(MessageDigest digest, AbstractFile file1, AbstractFile file2) {
        if (file1.getSize() != file2.getSize()) {
            return false;
        }
        String checksum1 = getChecksum(digest, file1);
        String checksum2 = getChecksum(digest, file2);
        return checksum1 != null && checksum1.equals(checksum2);
    }

    private String getChecksum(MessageDigest digest, AbstractFile file) {
        digest.reset();
        try (InputStream is = file.getInputStream()) {
            return AbstractFile.calculateChecksum(is, digest);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public ActionDescriptor getDescriptor() {
        return new Descriptor();
    }


    public static final class Descriptor extends AbstractActionDescriptor {
        public static final String ACTION_ID = "CompareFolderFiles";

        public String getId() { return ACTION_ID; }

        public ActionCategory getCategory() { return ActionCategory.SELECTION; }

        public KeyStroke getDefaultAltKeyStroke() { return null; }

        public KeyStroke getDefaultKeyStroke() {
//            if (!OsFamily.MAC_OS_X.isCurrent()) {
                return KeyStroke.getKeyStroke(KeyEvent.VK_B, KeyEvent.CTRL_DOWN_MASK);
//            } else {
//                return KeyStroke.getKeyStroke(KeyEvent.VK_M, KeyEvent.META_DOWN_MASK);
//            }
        }

        public MuAction createAction(MainFrame mainFrame, Map<String,Object> properties) {
            return new CompareFolderFilesAction(mainFrame, properties);
        }

    }
}
