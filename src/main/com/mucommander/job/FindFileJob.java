/*
 * This file is part of muCommander, http://www.mucommander.com
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
package com.mucommander.job;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.util.FileSet;
import com.mucommander.ui.main.MainFrame;
import org.apache.commons.io.filefilter.AbstractFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Job for directory scanning
 */
public class FindFileJob extends FileJob {

    private AbstractFile startDirectory;
    private String fileContent;
    private boolean searchSubdirectories;
    private boolean searchArchives;
    private boolean caseSensitive;
    private boolean ignoreHidden;

    private AbstractFileFilter fileFilter;

    private final List<AbstractFile> list = new ArrayList<>();

    public FindFileJob(MainFrame mainFrame) {
        super(mainFrame);
        setAutoUnmark(false);
    }


    @Override
    protected boolean hasFolderChanged(AbstractFile folder) {
        return false;
    }

    @Override
    protected boolean processFile(AbstractFile file, Object recurseParams) {
        // Stop if interrupted
        if (getState() == INTERRUPTED) {
            return false;
        }

        // If file is a directory, recurs
        if (file.isDirectory() && !file.isSymlink()) {
            searchInFile(file);
            if (!searchSubdirectories && !file.equals(startDirectory)) {
                return true;
            }
            try {
                AbstractFile subFiles[] = file.ls();
                for (int i = 0; i < subFiles.length && getState() != INTERRUPTED; i++) {
                    if (ignoreHidden && file.isHidden()) {
                        continue;
                    }
                    // Notify job that we're starting to process this file (needed for recursive calls to processFile)
                    nextFile(subFiles[i]);
                    processFile(subFiles[i], null);
                }
            } catch(IOException e) {
                // Should we tell the user?
            }
        } else { // If not, increase file counter and bytes total
            if (!ignoreHidden || !file.isHidden()) {
                searchInFile(file);
            }
        }

        if (file.isArchive() && searchArchives) {
            try {
                AbstractFile subFiles[] = file.ls();
                for (int i = 0; i < subFiles.length && getState() != INTERRUPTED; i++) {
                    if (ignoreHidden && file.isHidden()) {
                        continue;
                    }
                    // Notify job that we're starting to process this file (needed for recursive calls to processFile)
                    nextFile(subFiles[i]);
                    processFile(subFiles[i], null);
                }
            } catch(IOException e) {
                // Should we tell the user?
            }
        }

        return true;
    }

    private void searchInFile(AbstractFile file) {
        File f = new File(file.toString());
        if (fileFilter.accept(f) && fileContainsString(file)) {
            synchronized (this) {
                list.add(file);
            }
        }
    }

    private boolean fileContainsString(AbstractFile f) {
        if (fileContent == null || fileContent.isEmpty()) {
            return true;
        }
        if (f.isDirectory()) {
            return false;
        }
        Scanner in = null;
        boolean result = false;
        try {
            in = new Scanner(f.getInputStream());
            while (in.hasNextLine() && !result) {
                String line = in.nextLine();
                if (!caseSensitive) {
                    line = line.toLowerCase();
                }
                result = line.contains(fileContent);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }




    public List<AbstractFile> getResults() {
        return list;
    }

    public void setStartDirectory(AbstractFile startDirectory) {
        this.startDirectory = startDirectory;
        FileSet fs = new FileSet();
        fs.add(startDirectory);
        setFiles(fs);
    }

    public void setup(String fileMask, String fileContent, boolean searchSubdirs, boolean searchArchives, boolean caseSensitive, boolean ignoreHidden) {
        fileMask = fileMask.trim();
        fileMask = fileMask.isEmpty() ? "*" : fileMask;
        this.fileContent = fileContent;
        this.searchSubdirectories = searchSubdirs;
        this.searchArchives = searchArchives;
        this.caseSensitive = caseSensitive;
        this.ignoreHidden = ignoreHidden;

        fileFilter = new WildcardFileFilter(fileMask);
        if (!caseSensitive && fileContent != null) {
            this.fileContent = fileContent.toLowerCase();
        }
    }

}
