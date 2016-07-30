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
package com.mucommander.ui.viewer;

import com.mucommander.commons.file.FileFactory;
import com.mucommander.ui.action.MuAction;
import com.mucommander.ui.action.impl.EditAction;
import com.mucommander.ui.action.impl.ViewAction;
import com.mucommander.ui.viewer.text.TextEditor;

import javax.swing.Icon;
import java.awt.Frame;
import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created on 09/06/16.
 * @author Oleg Trifonov
 */
public class FileViewersList {
    private static final List<FileRecord> files = new ArrayList<>();
    private static long lastUpdateTime;

    /**
     *
     */
    public static class FileRecord {
        final public String fileName;
        final public String shortName;
        final public Class viewerClass;
        final public WeakReference<FileFrame> fileFrameRef;

        public FileRecord(String fileName, FileFrame fileFrame) {
            this.fileName = fileName;
            this.shortName = new File(fileName).getName();
            this.viewerClass = fileFrame.getFilePresenter().getClass();
            this.fileFrameRef = new WeakReference<>(fileFrame);
        }

        @Override
        public String toString() {
            return fileName;
        }

        public Icon getIcon() {
            //Icon icon = FileIconsCache.getInstance().getIcon(fileRecord.fileName);
            if (viewerClass == TextEditor.class) {
                return MuAction.getStandardIcon(EditAction.class);
            } else {
                return MuAction.getStandardIcon(ViewAction.class);
            }
        }

    }

    private static void buildFilesList(List<FileRecord> fileNames) {
        fileNames.clear();
        Frame frames[] = Frame.getFrames();
        for (Frame frame : frames) {
            // Test if Frame is not hidden (disposed), Frame.getFrames() returns both active and disposed frames
            if (frame.isShowing() && (frame instanceof FileFrame)) {
                // Use frame's window title
                fileNames.add(new FileRecord(frame.getTitle(), (FileFrame)frame));
            }
        }
    }



    public static void update() {
        buildFilesList(files);
        lastUpdateTime = System.currentTimeMillis();
    }

    public static List<FileRecord> getFiles() {
        return files;
    }

    public static long getLastUpdateTime() {
        return lastUpdateTime;
    }



}
