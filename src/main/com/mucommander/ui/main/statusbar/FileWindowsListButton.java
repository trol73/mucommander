/*
 * This file is part of trolCommander, http://www.trolsoft.ru/en/trolcommander
 * Copyright (C) 2014-2016 Oleg Trifonov
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
package com.mucommander.ui.main.statusbar;

import com.jidesoft.swing.JideSplitButton;
import com.mucommander.ui.main.MainFrame;
import com.mucommander.ui.main.WindowManager;
import com.mucommander.ui.viewer.FileViewersList;
import com.mucommander.utils.FileIconsCache;

import javax.swing.Icon;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;

import java.awt.Window;
import java.util.ArrayList;
import java.util.List;

import static com.mucommander.ui.viewer.FileViewersList.FileRecord;

/**
 * Created on 09/06/16.
 * @author Oleg Trifonov
 */
public class FileWindowsListButton extends JideSplitButton {
    private long lastUpdateTime;
    private FileRecord selectedRecord;
    private MainFrame selectedMainFrame;
    private final boolean includeMainFrames;


    public FileWindowsListButton(boolean includeMainFrames) {
        super();
        this.includeMainFrames = includeMainFrames;
        updateList();
        addActionListener(e -> showSelectedFile());
    }

    public FileWindowsListButton() {
        this(false);
    }

    private void showSelectedFile() {
        if (selectedRecord == null) {
            if (selectedMainFrame != null) {
                setText(mainFrameName(selectedMainFrame));
                setIcon(getIconFrom(selectedMainFrame));
                selectedMainFrame.toFront();
            }
            return;
        }
        JFrame fileFrame = selectedRecord.fileFrameRef.get();
        if (fileFrame != null) {
            fileFrame.toFront();
        }
    }


    private void selectFile(FileRecord fileRecord) {
        setText(fileRecord.shortName);
        setIcon(getIconFrom(fileRecord));
        selectedRecord = fileRecord;
        showSelectedFile();
    }


    public void updateList() {
        removeAll();
        if (includeMainFrames) {
            List<MainFrame> mainFrames = WindowManager.getMainFrames();
            for (MainFrame mainFrame : mainFrames) {
                String name = mainFrameName(mainFrame);
                JMenuItem menuItem = new JMenuItem(name, getIconFrom(mainFrame));
                menuItem.addActionListener(e -> mainFrame.toFront());
                add(menuItem);
            }

        }
        boolean containsSelected = false;
        List<FileRecord> list = new ArrayList<>(FileViewersList.getFiles());
        for (FileRecord fr : list) {
            Window excludedFrame = SwingUtilities.getWindowAncestor(this);
            if (fr.fileFrameRef.get() == excludedFrame) {
                continue;
            }
            JMenuItem menuItem = new JMenuItem(fr.fileName, getIconFrom(fr));
            menuItem.addActionListener(e -> selectFile(fr));
            add(menuItem);

            if (fr == selectedRecord) {
                containsSelected = true;
            }
        }

        if (!containsSelected) {
            selectedRecord = null;
        }

        if (getMenuComponentCount() > 0) {
            if (selectedRecord == null) {
                if (includeMainFrames) {
                    selectedRecord = null;
                    selectedMainFrame = WindowManager.getCurrentMainFrame();
                    setText(mainFrameName(selectedMainFrame));
                    setIcon(getIconFrom(selectedMainFrame));
                } else {
                    selectedRecord = FileViewersList.getFiles().get(0);
                    setText(selectedRecord.shortName);
                    setIcon(getIconFrom(selectedRecord));
                }
            }
            setVisible(true);
        } else {
            setVisible(false);
            selectedRecord = null;
        }
        lastUpdateTime = System.currentTimeMillis();
    }

    private static String mainFrameName(MainFrame mainFrame) {
        return mainFrame.getLeftPanel().getCurrentFolder().getName() + " : " + mainFrame.getRightPanel().getCurrentFolder().getName();
    }

    private Icon getIconFrom(MainFrame mainFrame) {
        return FileIconsCache.getInstance().getIcon(mainFrame.getActivePanel().getCurrentFolder());
    }

    private Icon getIconFrom(FileRecord fileRecord) {
        return fileRecord == null ? null : fileRecord.getIcon();
    }

    @Override
    public boolean isVisible() {
        if (lastUpdateTime < FileViewersList.getLastUpdateTime()) {
            SwingUtilities.invokeLater(this::updateList);
        }
        return super.isVisible();
    }

}
