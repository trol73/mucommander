package com.mucommander.job;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.util.FileSet;
import com.mucommander.ui.main.MainFrame;
import org.apache.commons.io.filefilter.AbstractFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Created by trol on 18/12/13.
 */
public class FindFileJob extends FileJob {

    private AbstractFile startDirectory;
    private String fileMask;
    private String fileContent;
    private boolean searchSubdirs;
    private boolean caseSensitive;
    private boolean ignoreHidden;

    private AbstractFileFilter fileFilter;

    private List<AbstractFile> list = new ArrayList<AbstractFile>();

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

        // If file is a directory, recurse
        if (file.isDirectory() && !file.isSymlink()) {
            if (!searchSubdirs && !file.equals(startDirectory)) {
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

        return true;
    }

    private void searchInFile(AbstractFile file) {
        File f = new File(file.toString());
        if (fileFilter.accept(f) && fileContainsString(f)) {
            synchronized (list) {
                list.add(file);
            }
        }
    }

    private boolean fileContainsString(File f) {
        if (fileContent == null || fileContent.isEmpty()) {
            return true;
        }
        Scanner in = null;
        boolean result = false;
        try {
            in = new Scanner(new FileReader(f));
            while (in.hasNextLine() && !result) {
                String line = in.nextLine();
                if (caseSensitive) {
                    result = line.indexOf(fileContent) >= 0;
                } else {
                    line = line.toLowerCase();
                    result = line.indexOf(fileContent) >= 0;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                in.close();
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

    public void setup(String fileMask, String fileContent, boolean searchSubdirs, boolean caseSensitive, boolean ignoreHidden) {
        fileMask = fileMask.trim();
        this.fileMask = fileMask.isEmpty() ? "*" : fileMask;
        this.fileContent = fileContent;
        this.searchSubdirs = searchSubdirs;
        this.caseSensitive = caseSensitive;
        this.ignoreHidden = ignoreHidden;

        fileFilter = new WildcardFileFilter(this.fileMask);
        if (!caseSensitive && fileContent != null) {
            this.fileContent = fileContent.toLowerCase();
        }
    }

}
