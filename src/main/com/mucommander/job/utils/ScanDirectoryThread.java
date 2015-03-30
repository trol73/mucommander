/*
 * This file is part of trolCommander, http://www.trolsoft.ru/soft/trolcommander
 * Copyright (C) 2013-2014 Oleg Trifonov
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
package com.mucommander.job.utils;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.util.FileSet;

import java.io.IOException;

/**
 * Thread to calculating the total size of files
 */
public class ScanDirectoryThread extends Thread {

    private final FileSet files;
    private long totalBytes;
    private boolean completed;
    private long executionTime;
    private long filesCount;
    private boolean interrupted;
    private final boolean calcSize;

    public ScanDirectoryThread(FileSet files) {
        this.files = files;
        this.calcSize = true;
        setName("ScanDirectoryThread " + files.getBaseFolder());
    }

    public ScanDirectoryThread(FileSet files, boolean calcSize) {
        this.files = files;
        this.calcSize = calcSize;
        setName("ScanDirectoryThread " + files.getBaseFolder());
    }

    @Override
    public void run() {
        executionTime = System.currentTimeMillis();
        for (AbstractFile file : files) {
            if (interrupted) {
                break;
            }
            try {
                processFile(file);
            } catch (Throwable ignore) {}
        }
        completed = true;
        executionTime = System.currentTimeMillis() - executionTime;
//System.out.println("finished  " + totalBytes + " " + filesCount + "    time " + executionTime);
    }

    private void processFile(AbstractFile file) {
        if (interrupted) {
            return;
        }
        filesCount++;
        if (file.isSymlink()) {
            return; // ignore symlinks
        }
        if (file.isDirectory()) {
            try {
                AbstractFile[] subfiles = file.ls();
                for (AbstractFile subfile : subfiles ) {
                    if (interrupted) {
                        return;
                    }
                    processFile(subfile);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            if (calcSize) {
                totalBytes += file.getSize();
            }
        }
    }

    public long getTotalBytes() {
        return totalBytes;
    }

    public boolean isCompleted() {
        return completed;
    }

    public long getFilesCount() {
        return filesCount;
    }

    public void interrupt() {
        interrupted = true;
    }
}
