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
import com.mucommander.commons.runtime.OsFamily;
import com.mucommander.ui.main.MainFrame;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.AbstractFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import ru.trolsoft.utils.search.*;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * Job for directory scanning
 */
public class FindFileJob extends FileJob {

    private AbstractFile startDirectory;
    private String fileContent;
    private boolean searchSubdirectories;
    private boolean searchArchives;
    private boolean ignoreHidden;
    private SearchPattern searchPattern;

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
        if (getState() == State.INTERRUPTED) {
            return false;
        }
        // If file is a directory, recurs
        if (file.isDirectory() && (!file.isSymlink() || file.equals(startDirectory))) {
            searchInFile(file);
            if (!searchSubdirectories && !file.equals(startDirectory)) {
                return true;
            }
            try {
                AbstractFile subFiles[] = file.ls();
                for (int i = 0; i < subFiles.length && getState() != State.INTERRUPTED; i++) {
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
                for (int i = 0; i < subFiles.length && getState() != State.INTERRUPTED; i++) {
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
        //Profiler.start("check_new");
        if (fileContent == null || fileContent.isEmpty()) {
            return true;
        }
        if (f.isDirectory()) {
            return false;
        }

        try {
            SearchSourceStream source = new InputStreamSource(f.getInputStream());
            long pos = SearchUtils.indexOf(source, searchPattern);
            //Profiler.stop("check_new");
            return pos >= 0;
        } catch (SearchException | IOException e) {
            e.printStackTrace();
            return false;
        }
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

    public void setup(String fileMask, String fileContent, boolean searchSubdirs, boolean searchArchives, boolean caseSensitive, boolean ignoreHidden, String encoding, boolean hexMode, byte[] bytes) {
        fileMask = fileMask.trim();
        fileMask = fileMask.isEmpty() ? "*" : fileMask;
        this.fileContent = fileContent;
        this.searchSubdirectories = searchSubdirs;
        this.searchArchives = searchArchives;
        this.ignoreHidden = ignoreHidden;
        IOCase filterCase;
        if (OsFamily.getCurrent() == OsFamily.MAC_OS_X || OsFamily.getCurrent() == OsFamily.WINDOWS) {
            filterCase = IOCase.INSENSITIVE;
        } else {
            filterCase = IOCase.SENSITIVE;
        }
        fileFilter = new WildcardFileFilter(fileMask, filterCase);
        if (!caseSensitive && fileContent != null) {
            this.fileContent = fileContent.toLowerCase();
        }

        if (hexMode) {
            searchPattern = new BytesSearchPattern(bytes);
        } else {
            try {
                searchPattern = caseSensitive ?
                        new StringCaseSensitiveSearchPattern(fileContent, encoding) :
                        new StringCaseInsensitiveSearchPattern(fileContent, encoding);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

    }

}
