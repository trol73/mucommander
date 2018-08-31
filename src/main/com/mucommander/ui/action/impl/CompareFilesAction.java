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
import com.mucommander.commons.file.FileOperation;
import com.mucommander.commons.file.filter.AbstractFileFilter;
import com.mucommander.commons.file.filter.AndFileFilter;
import com.mucommander.commons.file.filter.FileOperationFilter;
import com.mucommander.commons.file.impl.local.LocalFile;
import com.mucommander.commons.file.util.FileSet;
import com.mucommander.commons.runtime.OsFamily;
import com.mucommander.process.ExecutorUtils;
import com.mucommander.ui.action.AbstractActionDescriptor;
import com.mucommander.ui.action.ActionCategory;
import com.mucommander.ui.action.ActionDescriptor;
import com.mucommander.ui.action.MuAction;
import com.mucommander.ui.main.MainFrame;

import javax.swing.KeyStroke;
import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * Created on 01/02/16.
 * @author Oleg Trifonov
 */
public class CompareFilesAction extends SelectedFilesAction {

    private CompareFilesAction(MainFrame mainFrame, Map<String, Object> properties) {
        super(mainFrame, properties);
        setSelectedFileFilter(new AndFileFilter(
            new FileOperationFilter(FileOperation.READ_FILE),
            new AbstractFileFilter() {
                @Override
                public boolean accept(AbstractFile file) {
                    if (supported()) {
                        AbstractFile leftFile = mainFrame.getLeftPanel().getFileTable().getSelectedFile();
                        AbstractFile rightFile = mainFrame.getRightPanel().getFileTable().getSelectedFile();
                        return  leftFile != null && !leftFile.isDirectory() && rightFile != null && !rightFile.isDirectory() &&
                                leftFile instanceof LocalFile && rightFile instanceof LocalFile;
                    }
                    return false;
                }
            }
        ));
    }

    @Override
    public void performAction(FileSet files) {
        String leftFile = mainFrame.getLeftPanel().getFileTable().getSelectedFile().getAbsolutePath().replace(" ", "\\ ");
        String rightFile = mainFrame.getRightPanel().getFileTable().getSelectedFile().getAbsolutePath().replace(" ", "\\ ");
        compareTwoFiles(leftFile, rightFile);
    }

    public static void compareTwoFiles(String fiel1, String file2) {
        new Thread(() -> {
            try {
                ExecutorUtils.execute("/usr/bin/opendiff " + fiel1 + " " + file2);
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public static boolean supported() {
        return OsFamily.MAC_OS_X.isCurrent() && new File("/usr/bin/opendiff").exists();
    }

    @Override
    public ActionDescriptor getDescriptor() {
        return new Descriptor();
    }

    public static final class Descriptor extends AbstractActionDescriptor {
        public static final String ACTION_ID = "CompareFiles";

        public String getId() {
            return ACTION_ID;
        }

        public ActionCategory getCategory() {
            return ActionCategory.FILES;
        }

        public KeyStroke getDefaultAltKeyStroke() {
            return null;
        }

        public KeyStroke getDefaultKeyStroke() {
            return null;
        }

        public MuAction createAction(MainFrame mainFrame, Map<String, Object> properties) {
            return new CompareFilesAction(mainFrame, properties);
        }

    }
}
