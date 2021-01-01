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
package com.mucommander.ui.action.impl;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.filter.MountedDriveFilter;
import com.mucommander.commons.file.util.FileSet;
import com.mucommander.commons.runtime.OsFamily;
import com.mucommander.utils.text.Translator;
import com.mucommander.ui.action.AbstractActionDescriptor;
import com.mucommander.ui.action.ActionCategory;
import com.mucommander.ui.action.ActionDescriptor;
import com.mucommander.ui.action.TcAction;
import com.mucommander.ui.macosx.AppleScript;
import com.mucommander.ui.main.MainFrame;
import com.mucommander.ui.main.statusbar.TaskWidget;

import javax.swing.*;
import java.util.List;
import java.util.Map;

/**
 *
 * Created on 26/01/16.
 * @author Oleg Trifonov
 */
public class EjectDriveAction extends SelectedFilesAction {

    private EjectDriveAction(MainFrame mainFrame, Map<String, Object> properties) {
        super(mainFrame, properties);
        setSelectedFileFilter(new MountedDriveFilter());
    }

    @Override
    public void performAction(FileSet files) {
        if (files.size() == 1) {
            eject(mainFrame, files.get(0));
            mainFrame.tryRefreshCurrentFolders();
        }
    }

    public static void eject(MainFrame mainFrame, AbstractFile file) {
        if (OsFamily.MAC_OS_X.isCurrent()) {
            new EjectWorker(mainFrame, file.getName()).execute();
        }
    }

    @Override
    public ActionDescriptor getDescriptor() {
        return new Descriptor();
    }


    public static final class Descriptor extends AbstractActionDescriptor {
        public static final String ACTION_ID = "EjectDrive";

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

        public TcAction createAction(MainFrame mainFrame, Map<String, Object> properties) {
            return new EjectDriveAction(mainFrame, properties);
        }
    }


    private static class EjectWorker extends SwingWorker<Void, Void> {
        private final MainFrame mainFrame;
        private final TaskWidget taskWidget;
        private final String fileName;
        private boolean taskWidgetAttached;
        private int progress;

        EjectWorker(MainFrame mainFrame, String fileName) {
            this.mainFrame = mainFrame;
            this.fileName = fileName;
            this.taskWidget = new TaskWidget();
            taskWidget.setText(Translator.get("EjectDrive.label"));
        }

        @Override
        protected Void doInBackground() {
            try {
                publish();
                StringBuilder sb = new StringBuilder();
                publish();
                Thread t = new Thread(() -> {
                    //try {Thread.sleep(5000); } catch (Throwable e) {}
                    AppleScript.execute("tell application \"Finder\"\n" +
                            "   eject disk \"" + fileName + "\"\n" +
                            "end tell", sb);
                });
                t.start();
                while (t.isAlive() || progress < 100) {
                    if (!t.isAlive()) {
                        progress += 10;
                    }
                    Thread.sleep(50);
                    publish();
                }
                progress = 100;
                publish();
            } catch (Throwable ignore) {}
            return null;
        }

        @Override
        protected void process(List<Void> chunks) {
            if (!taskWidgetAttached) {
                mainFrame.getStatusBar().getTaskPanel().addTask(taskWidget);
                mainFrame.getStatusBar().revalidate();
                mainFrame.getStatusBar().repaint();
                taskWidgetAttached = true;
            }
            if (progress < 100) {
                progress += 10;
            }
            taskWidget.setProgress(progress);
        }

        @Override
        protected void done() {
            taskWidget.removeFromPanel();
        }
    }
}
