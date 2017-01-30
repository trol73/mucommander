/*
 * This file is part of trolCommander, http://www.trolsoft.ru/en/soft/trolcommander
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
package com.mucommander.ui.main.table.views;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.filter.FileFilter;
import com.mucommander.commons.file.impl.CachedFile;
import com.mucommander.commons.file.impl.local.LocalFile;
import com.mucommander.commons.file.util.FileComparator;
import com.mucommander.commons.file.util.FileSet;
import com.mucommander.conf.MuConfigurations;
import com.mucommander.conf.MuPreference;
import com.mucommander.conf.MuPreferences;
import com.mucommander.text.SizeFormat;
import com.mucommander.ui.main.table.CalculateDirectorySizeWorker;
import com.mucommander.ui.main.table.FileTable;
import com.mucommander.ui.main.table.SortInfo;
import com.mucommander.ui.quicksearch.QuickSearch;

import javax.swing.table.AbstractTableModel;
import java.awt.Cursor;
import java.util.*;

/**
 * @author Oleg Trifonov
 * Created on 04/04/15.
 */
public abstract class BaseFileTableModel extends AbstractTableModel {

    private static final Cursor WAIT_CURSOR = new Cursor(Cursor.WAIT_CURSOR);

    /** String used as size information for directories */
    public static final String DIRECTORY_SIZE_STRING = "<DIR>";

    /** String used as size information for directories that queued to size calculation */
    protected static final String QUEUED_DIRECTORY_SIZE_STRING = "<...>";


    /** True if the name column is temporarily editable */
    protected boolean nameColumnEditable;

    /** SizeFormat format used to create the size column's string */
    protected static int sizeFormat;

    /** Contains sort-related variables */
    private SortInfo sortInfo;

    /**
     * QuickSearch object to sorting and filtering matched files
     */
    protected QuickSearch quickSearch;

    /** Index array */
    protected int fileArrayIndex[];
	
    /** The current folder */
    protected AbstractFile currentFolder;

    /** Date of the current folder when it was changed */
    protected long currentFolderDateSnapshot;

    /** The current folder's parent folder, may be null */
    protected AbstractFile parent;

    /** Cached file instances */
    private AbstractFile cachedFiles[];

    /** Combined size of files currently marked */
    private long markedTotalSize;

    /** Number of files currently marked */
    private int nbFilesMarked;

    /** Marked files array */
    private boolean fileMarked[];


    /** Tasks queue for directory size calculate */
    protected final List<AbstractFile> calculateSizeQueue = new LinkedList<>();

    /** Worker to calculate directories sizes */
    private CalculateDirectorySizeWorker calculateDirectorySizeWorker;

    /** True if the table has directories with calculated size */
    protected boolean hasCalculatedDirectories;

    /** Stores marked directories to calculate these size if need */
    private final Set<AbstractFile> markedDirectories = new HashSet<>();

    /** Here will be stored sizes of directories calculated by F3 command */
    protected final Map<AbstractFile, Long> directorySizes = new HashMap<>();

    private FileComparator fileComparator;

    /*
     * First visible row
     */
    //protected int firstVisibleRow;




    static {
        // Initialize the size column format based on the configuration
        setSizeFormat(MuConfigurations.getPreferences().getVariable(MuPreference.DISPLAY_COMPACT_FILE_SIZE,
                                                  MuPreferences.DEFAULT_DISPLAY_COMPACT_FILE_SIZE));
    }


    public abstract void fillCellCache();
    public abstract int getFileRow(int index);

    /**
     * Init and fill cell cache to speed up table even more
     */
    protected abstract void initCellValuesCache();

    /**
     * Returns index of file in directory (index of '..' == 0)
     * @param row table row
     * @param col table column
     * @return index file in the table
     */
    public abstract int getFileIndexAt(int row, int col);


    protected BaseFileTableModel() {
        fileArrayIndex = new int[0];
        fileMarked = new boolean[0];
        // Init arrays to avoid NullPointerExceptions until setCurrentFolder() gets called for the first time
        cachedFiles = new AbstractFile[0];
    }


    public synchronized void setupFromModel(BaseFileTableModel model) {
        if (model == null) {
            return;
        }
        this.sortInfo = model.sortInfo;
        this.fileArrayIndex = model.fileArrayIndex;
        this.currentFolder = model.currentFolder;
        this.currentFolderDateSnapshot = model.currentFolderDateSnapshot;
        this.parent = model.parent;
        this.cachedFiles = model.cachedFiles;
        this.markedTotalSize = model.markedTotalSize;
        this.nbFilesMarked = model.nbFilesMarked;
        this.fileMarked = model.fileMarked;
        stopSizeCalculation();
    }

    /**
     * Sets the SizeFormat format used to create the size column's string.
     *
     * @param compactSize true to use a compact size format, false for full size in bytes 
     */
    public static void setSizeFormat(boolean compactSize) {
        if (compactSize) {
            sizeFormat = SizeFormat.DIGITS_MEDIUM | SizeFormat.UNIT_SHORT;// | SizeFormat.ROUND_TO_KB;
        } else {
            sizeFormat = SizeFormat.DIGITS_FULL;
        }

        sizeFormat |= SizeFormat.INCLUDE_SPACE;
    }

    /**
     * Returns the SizeFormat format used to create the size column's string
     * @return  SizeFormat bit mask
     */
    public static int getSizeFormat() {
        return sizeFormat;
    }

    /**
     * Pre-fetch the attributes that are used by the table renderer and some actions from the given CachedFile.
     * By doing so, the attributes will be available when the associated getters are called and thus the methods won't
     * be I/O bound and will not lock.
     *
     * @param cachedFile a CachedFile instance from which to pre-fetch attributes
     */
    private static void prefetchCachedFileAttributes(AbstractFile cachedFile) {
        cachedFile.isDirectory();
        cachedFile.isBrowsable();
        cachedFile.isHidden();

        // Pre-fetch isSymlink attribute and if the file is a symlink, pre-fetch the canonical file and its attributes
        if (cachedFile.isSymlink()) {
            AbstractFile canonicalFile = cachedFile.getCanonicalFile();
            if (canonicalFile != cachedFile) {  // Cheap test to prevent infinite recursion on bogus file implementations
                prefetchCachedFileAttributes(canonicalFile);
            }
        }
    }


    /**
     * Returns the file located at the given index, not including the parent file.
     * Returns <code>null</code> if fileIndex is lower than 0 or is greater than or equals {@link #getFileCount() getFileCount()}.
     *
     * @param fileIndex index of a file, comprised between 0 and #getFileCount()
     * @return the file located at the given index, not including the parent file
     */
    public synchronized AbstractFile getFileAt(int fileIndex) {
        if (fileIndex == 0 && parent != null) {
            return parent;
        }
        if (parent != null) {
            fileIndex--;
        }
        // Need to check that row index is not larger than actual number of rows
        // because if table has just been changed (rows have been removed),
        // JTable may have an old row count value and may try to repaint rows that are out of bounds.
        if (fileIndex >= 0 && fileIndex < fileArrayIndex.length) {
            return ((CachedFile)cachedFiles[fileArrayIndex[fileIndex]]).getProxiedFile();
        }
        return null;
    }


    /**
     * Returns the actual number of files the current folder contains, including the parent '..' file (if any).
     *
     * @return the actual number of files the current folder contains, including the parent '..' file (if any)
     */
    public synchronized int getFileCount() {
        return cachedFiles.length + (parent != null ? 1 : 0);
    }

    /**
     * Returns the actual number of files the current folder contains, not including the parent '..' file (if any).
     *
     * @return the actual number of files the current folder contains, not including the parent '..' file (if any)
     */
    public synchronized int getFileCountWithoutParent() {
        return cachedFiles.length;
    }

    /**
     * Returns the current folder's children. The returned array contains {@link AbstractFile} instances, and not
     * CachedFile instances contrary to {@link #getCachedFiles()}.
     *
     * @return the current folder's children
     * @see #getCachedFiles()
     */
    public synchronized AbstractFile[] getFiles() {
        int nbFiles = cachedFiles.length;
        AbstractFile[] files = new AbstractFile[nbFiles];
        for (int i=0; i<nbFiles; i++) {
            files[i] = cachedFiles[i] == null ? null : ((CachedFile) cachedFiles[i]).getProxiedFile();
        }

        return files;
    }


    /**
     * Returns the current folder's children. The returned array contains {@link CachedFile} instances, where
     * most attributes have already been fetched and cached.
     *
     * @return the current folder's children, as an array of CachedFile instances
     * @see #getFiles()
     */
    private synchronized AbstractFile[] getCachedFiles() {
        // Clone the array to make sure it can't be modified outside of this class
        AbstractFile[] cachedFilesCopy = new AbstractFile[cachedFiles.length];
        System.arraycopy(cachedFiles, 0, cachedFilesCopy, 0, cachedFiles.length);

        return cachedFilesCopy;
    }

    /**
     * Sorts rows by the current criterion, ascending/descending order and 'folders first' value.
     */
    public synchronized void sortRows() {
        this.fileComparator = createFileComparator(sortInfo);
        sort(0, fileArrayIndex.length - 1);
        this.fileComparator = null;
    }


    //////////////////
    // Sort methods //
    //////////////////

    private FileComparator createFileComparator(SortInfo sortInfo) {
        return new FileComparator(sortInfo.getCriterion().getFileComparatorCriterion(), sortInfo.getAscendingOrder(),
                sortInfo.getFoldersFirst(), quickSearch != null && quickSearch.isActive() ? quickSearch : null);
    }


    /**
     * Quick sort implementation, based on James Gosling's implementation.
     */
    protected void sort(int lo0, int hi0) {
        int lo = lo0;
        int hi = hi0;

        if (lo >= hi) {
            return;
        } else if (lo == hi - 1) {
            // sort a two element list by swapping if necessary
            int loIndex = fileArrayIndex[lo];
            int hiIndex = fileArrayIndex[hi];
            if (compare(loIndex, hiIndex) > 0) {
                fileArrayIndex[lo] = hiIndex;
                fileArrayIndex[hi] = loIndex;
            }
            return;
        }

        // Pick a pivot and move it out of the way
        int pivotIndex = fileArrayIndex[(lo + hi) / 2];
        fileArrayIndex[(lo + hi) / 2] = fileArrayIndex[hi];
        fileArrayIndex[hi] = pivotIndex;

        while (lo < hi) {
            // Search forward from files[lo] until an element is found that
            // is greater than the pivot or lo >= hi
            //while (compare(cachedFiles[fileArrayIndex[lo]], pivot)<=0 && lo < hi) {
            while (compare(fileArrayIndex[lo], pivotIndex) <= 0 && lo < hi) {
                lo++;
            }

            // Search backward from files[hi] until element is found that
            // is less than the pivot, or lo >= hi
            //while (compare(pivot, cachedFiles[fileArrayIndex[hi]])<=0 && lo < hi ) {
            while (compare(pivotIndex, fileArrayIndex[hi]) <= 0 && lo < hi ) {
                hi--;
            }

            // Swap elements files[lo] and files[hi]
            if (lo < hi) {
                int temp = fileArrayIndex[lo];
                fileArrayIndex[lo] = fileArrayIndex[hi];
                fileArrayIndex[hi] = temp;
            }
        }

        // Put the median in the "center" of the list
        fileArrayIndex[hi0] = fileArrayIndex[hi];
        fileArrayIndex[hi] = pivotIndex;

        // Recursive calls, elements files[lo0] to files[lo-1] are less than or
        // equal to pivot, elements files[hi+1] to files[hi0] are greater than pivot.
        sort(lo0, lo-1);
        sort(hi+1, hi0);
    }


    private int compare(int index1, int index2) {
        if (index1 == index2) {
            return 0;
        }
        return fileComparator.compare(cachedFiles[index1], cachedFiles[index2]);
    }



    /**
     * Returns the current folder, i.e. the last folder set using {@link #setCurrentFolder(com.mucommander.commons.file.AbstractFile, com.mucommander.commons.file.AbstractFile[])}.
     *
     * @return the current folder
     */
    public synchronized AbstractFile getCurrentFolder() {
        return currentFolder;
    }

    /**
     * Sets the {@link SortInfo} instance that describes how the associated table is
     * sorted.
     *
     * @param sortInfo SortInfo instance that describes how the associated table is sorted
     */
    public void setSortInfo(SortInfo sortInfo) {
        this.sortInfo = sortInfo;
    }

    public void setQuickSearch(QuickSearch quickSearch) {
        this.quickSearch = quickSearch;
    }



    /**
     * Sets the current folder and its children.
     *
     * @param folder the current folder
     * @param children the current folder's children
     */
    public synchronized void setCurrentFolder(AbstractFile folder, AbstractFile children[]) {
        int nbFiles = children.length;
        this.currentFolder = (folder instanceof CachedFile) ? folder : new CachedFile(folder, true);

        this.parent = currentFolder.getParent();    // Note: the returned parent is a CachedFile instance
        if (parent != null) {
            // Pre-fetch the attributes that are used by the table renderer and some actions.
            prefetchCachedFileAttributes(parent);
        }
        stopSizeCalculation();

        // Initialize file indexes and create CachedFile instances to speed up table display and navigation
        this.cachedFiles = children;
        this.fileArrayIndex = new int[nbFiles];

        // we needn't prefetch local files for performance optimization purposes
        // in the case of local files the lazy initialization will be enough
        boolean needPrefetch = nbFiles > 0 && !(children[0] instanceof LocalFile);

        for (int i = 0; i < nbFiles; i++) {
            AbstractFile child = children[i];
            AbstractFile file = child instanceof CachedFile ? child : new CachedFile(child, true);

            // Pre-fetch the attributes that are used by the table renderer and some actions.
            if (needPrefetch) {
                prefetchCachedFileAttributes(file);
            }

            cachedFiles[i] = file;
            fileArrayIndex[i] = i;
        }

        // Reset marked files
        //this.rowMarked = new boolean[getRowCount()];
        this.fileMarked = new boolean[getFilesCount()];
        this.markedTotalSize = 0;
        this.nbFilesMarked = 0;

        // Init and fill cell cache to speed up table even more
        initCellValuesCache();

        fillCellCache();
    }

    /**
     * Returns the date of the current folder, when it was set using {@link #setCurrentFolder(com.mucommander.commons.file.AbstractFile, com.mucommander.commons.file.AbstractFile[])}.
     * In other words, the returned date is a snapshot of the current folder's date which is never updated.
     *
     * @return Returns the date of the current folder, when it was set using #setCurrentFolder(Abstract, Abstract[])
     */
    public synchronized long getCurrentFolderDateSnapshot() {
        return currentFolderDateSnapshot;
        }

    /**
     * Returns <code>true</code> if the current folder has a parent.
     *
     * @return <code>true</code> if the current folder has a parent
     */
    public synchronized boolean hasParentFolder() {
        return parent != null;
    }

    /**
     * Returns the current folder's parent if there is one, <code>null</code> otherwise.
     *
     * @return the current folder's parent if there is one, <code>null</code> otherwise
     */
    public synchronized AbstractFile getParentFolder() {
        return parent;
    }

    /**
     * Returns the index of the first row that can be marked/unmarked : <code>1</code> if the current folder has a
     * parent folder, <code>0</code> otherwise (parent folder row '..' cannot be marked).
     *
     * @return the index of the first row that can be marked/unmarked
     */
    public int getFirstMarkableIndex() {
        return parent == null ? 0 : 1;
    }
	
    /**
     * Marks/unmarks the given row range, delimited by the provided start row index and end row index (inclusive).
     * End row may be less, greater or equal to the start row.
     *
     * @param start index of the first file to mark/unmark
     * @param end index of the last file to mark/ummark, startRow may be less or greater than startRow
     * @param marked if true, all the files within the range will be marked, unmarked otherwise
     */
    public void setRangeMarked(int start, int end, boolean marked) {
        if (end >= start) {
            for (int i = start; i <= end; i++) {
                setFileMarked(i, marked);
            }
        } else {
            for (int i = start; i >= end; i--) {
                setFileMarked(i, marked);
            }
        }
    }
		

    /**
     * Marks/Unmarks the given file.
     *
     * @param file the file to mark/unmark
     * @param marked <code>true</code> to mark the row, <code>false</code> to unmark it.
     */
    public synchronized void setFileMarked(AbstractFile file, boolean marked) {
        int index = getFileIndex(file);

        if (index >= 0) {
            setFileMarked(index, marked);
        }
    }


    /**
     * Marks/unmarks the files that match the given {@link FileFilter}.
     *
     * @param filter the FileFilter to match the files against
     * @param marked if true, matching files will be marked, if false, they will be unmarked
     */
    public synchronized void setFilesMarked(FileFilter filter, boolean marked) {
        int nbFiles = getFilesCount();
        for (int i = parent == null ? 0 : 1; i < nbFiles; i++) {
            if (filter.match(getCachedFileAt(i))) {
                setFileMarked(i, marked);
            }
        }
    }


    /**
     * Returns a {@link com.mucommander.commons.file.util.FileSet FileSet} with all currently marked files.
     * <p>
     * The returned <code>FileSet</code> is a freshly created instance, so it can be safely modified.
     & However, it won't be kept current : the returned FileSet is just a snapshot
     * which might not reflect the current marked files state after this method has returned and additional
     * files have been marked/unmarked.
     * </p>
     *
     * @return a FileSet containing all the files that are currently marked
     */
    public synchronized FileSet getMarkedFiles() {
        FileSet markedFiles = new FileSet(currentFolder, nbFilesMarked);
        int nbFiles = getFilesCount();

        if (parent == null) {
            for (int i = 0; i < nbFiles; i++) {
                if (fileMarked[fileArrayIndex[i]]) {
                    markedFiles.add(getFileAt(i));
                }
            }
        } else {
            for (int i = 1, iMinusOne = 0; i < nbFiles; i++) {
                if (fileMarked[fileArrayIndex[iMinusOne]]) {
                    markedFiles.add(getFileAt(i));
                }
                iMinusOne = i;
            }
        }

        return markedFiles;
    }
	
	
    /**
     * Returns a CachedFile instance of the file located at the given row index.
     * This method can return the parent folder file ('..') if a parent exists and rowIndex is 0.
     * 
     * <p>Returns <code>null</code> if rowIndex is lower than 0 or is greater than or equals
     * {@link #getFilesCount() getFilesCount()}.</p>
     *
     * @param row a row index, comprised between 0 and #getRowCount()-1
     * @param col a column index, comprised between 0 and #getColumnCount()-1
     * @return a CachedFile instance of the file located at the given row index
     */
    public synchronized AbstractFile getCachedFileAt(int row, int col) {
        int index = getFileIndexAt(row, col);

        if (parent != null) {
            if (index == 0) {
                return parent;
            }
            index--;
        }
		
        // Need to check that row index is not larger than actual number of rows
        // because if table has just been changed (rows have been removed),
        // JTable may have an old row count value and may try to repaint rows that are out of bounds.
        if (index >= 0 && index < fileArrayIndex.length) {
            return cachedFiles[fileArrayIndex[index]];
        }
        return null;
    }

    public synchronized AbstractFile getCachedFileAt(int index) {
        if (parent != null) {
            if (index == 0) {
                return parent;
            }
            index--;
        }

        // Need to check that row index is not larger than actual number of rows
        // because if table has just been changed (rows have been removed),
        // JTable may have an old row count value and may try to repaint rows that are out of bounds.
        if (index >= 0 && index < fileArrayIndex.length) {
            return cachedFiles[fileArrayIndex[index]];
        }
        return null;
    }



    /**
     * Returns the file located at the given row index. 
     * This method can return the parent folder file ('..') if a parent exists and rowIndex is 0.
     *
     * <p>Returns <code>null</code> if rowIndex is lower than 0 or is greater than or equals
     * {@link #getFilesCount() getFilesCount()}.</p>
     *
     * @param row a row index, comprised between 0 and #getRowCount()-1
     * @param col a column index, comprised between 0 and #getColumnCount()-1
     * @return the file located at the given row index
     */
    public synchronized AbstractFile getFileAt(int row, int col) {
        AbstractFile file = getCachedFileAt(row, col);
	
//        if (file == null) {
//            return null;
//        }
        if (file instanceof CachedFile) {
            return ((CachedFile) file).getProxiedFile();
        }
        return file;
    }
	

    /**
     * Returns the index of the row where the given file is located, <code>-1<code> if the file is not in the
     * current folder.
     *
     * @param file the file for which to find the row index
     * @return the index of the file where the given file is located, <code>-1<code> if the file is not in the
     * current folder
     */
    public synchronized int getFileIndex(AbstractFile file) {
        // Handle parent folder file
        if (parent != null && file.equals(parent)) {
            return 0;
        }

        // Use dichotomic binary search rather than a dumb linear search since file array is sorted, complexity is
        // reduced to O(log n) instead of O(n^2)
        int left = parent == null ? 0 : 1;
        int right = getFilesCount() - 1;
        FileComparator fc = createFileComparator(sortInfo);

        while (left <= right) {
            int mid = (right-left)/2 + left;
            AbstractFile midFile = getCachedFileAt(mid);
            if (midFile.equals(file)) {
                return mid;
            } if (fc.compare(file, midFile) < 0) {
                right = mid - 1;
            } else {
                left = mid+1;
            }
        }
		
        return -1;
    }

	


    /**
     * Returns <code>true</code> if the given file is marked (/!\ not selected). If the specified row corresponds to the
     * special '..' parent file, <code>false</code> is always returned.
     *
     * @param row index of a row to test
     * @param col index of a column to test
     * @return <code>true</code> if the given row is marked
     */
    public synchronized boolean isFileMarked(int row, int col) {
        if (row == 0 && col <= 0 && parent != null) {
            return false;
        }
        final int firstOffset = parent == null ? 0 : 1;
        int total = fileArrayIndex.length + firstOffset;
        //return row < total && rowMarked[fileArrayIndex[row - firstOffset]];
        //row -= firstOffset;
        int index = getFileIndexAt(row, col);
        try {
            return index < total && fileMarked[fileArrayIndex[index - firstOffset]];
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public synchronized boolean isFileMarked(int index) {
        if (index == 0 && parent != null) {
            return false;
        }
        final int firstOffset = parent == null ? 0 : 1;
        int total = fileArrayIndex.length + firstOffset;
        try {
            return index < total && fileMarked[fileArrayIndex[index - firstOffset]];
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

	
    /**
     * Makes the name column temporarily editable. This method should only be called by FileTable.
     *
     * @param editable <code>true</code> to make the name column editable, false to prevent it from being edited
     */
    public void setNameColumnEditable(boolean editable) {
        this.nameColumnEditable = editable;
    }



    /**
     * Marks/Unmarks the given row. If the specified row corresponds to the special '..' parent file, the row won't
     * be marked.
     *
     * @param index the file index to mark/unmark
     * @param marked <code>true</code> to mark the row, <code>false</code> to unmark it
     */
    public synchronized void setFileMarked(int index, boolean marked) {
        if (index == 0 && parent != null) {
            return;
        }

        // Return if the row is already marked/unmarked
        final int fileIndex = fileArrayIndex[parent != null ? index - 1 : index];
//        if((marked && rowMarked[fileIndex]) || (!marked && !rowMarked[fileIndex]))
//            return;
        if (marked == fileMarked[fileIndex]) {
            return;
        }

        AbstractFile file = getCachedFileAt(index);

        // Do not call getSize() on directories, it's unnecessary and the value is most likely not cached by CachedFile yet
        long fileSize;

        if (file.isDirectory()) {
            markedDirectories.add(file);
            fileSize = 0;
        } else {
            fileSize = file.getSize();
        }

        // Update :
        // - Combined size of marked files
        // - marked files FileSet
        if (marked) {
            // File size can equal -1 if not available, do not count that in total
            if (fileSize > 0) {
                markedTotalSize += fileSize;
            }
            nbFilesMarked++;
        } else {
            // File size can equal -1 if not available, do not count that in total
            if (fileSize > 0) {
                markedTotalSize -= fileSize;
            }

            nbFilesMarked--;
        }

        fileMarked[fileIndex] = marked;
    }


    /**
     * Returns the number of marked files. This number is pre-calculated so calling this method is much faster than
     * retrieving the list of marked files and counting them.
     *
     * @return the number of marked files
     */
    public int getNbMarkedFiles() {
        return nbFilesMarked;
    }

	
    /**
     * Returns the combined size of marked files. This number consists of two parts:
     * 1) pre-calculated size of files so calling this method is much faster
     * than retrieving the list of marked files and calculating their combined size.
     * 2) calculated size of directories (if that was calculated)
     *
     * @return the combined size of marked files and directories
     */
    public long getTotalMarkedSize() {
        return markedTotalSize + calcMarkedDirectoriesSize();
    }


    /**
     * Add directory to size calculation and start calculation worker if it doesn't busy
     * @param table file table
     * @param file directory to add
     */
    public void startDirectorySizeCalculation(FileTable table, AbstractFile file) {
        if (!file.isDirectory()) {
            return;
        }
        hasCalculatedDirectories = true;
        synchronized (directorySizes) {
            if (directorySizes.containsKey(file)) {
                return;
            }
        }
        synchronized (calculateSizeQueue) {
            if (calculateSizeQueue.contains(file)) {
                return;
            }
            calculateSizeQueue.add(file);
        }
        if (calculateDirectorySizeWorker == null) {
            processNextQueuedFile(table);
        }
    }



    /**
     * Takes a first ask for queue and starts calculation worker
     * @param table file table
     */
    private void processNextQueuedFile(FileTable table) {
        AbstractFile nextFile;
        synchronized (calculateSizeQueue) {
            nextFile = calculateSizeQueue.isEmpty() ? null : calculateSizeQueue.remove(0);
            }
        if (nextFile == null) {
            calculateDirectorySizeWorker = null;
            table.getParent().setCursor(Cursor.getDefaultCursor());
        } else {
            calculateDirectorySizeWorker = new CalculateDirectorySizeWorker(this, table, nextFile);
            table.getParent().setCursor(WAIT_CURSOR);
            calculateDirectorySizeWorker.execute();
        }
    }

    /**
     * Called from size-calculation worker after it finish or requests to repaint table.
     * Updates map of directory sizes and starts next task if worker finished
     *
     * @param path directory to process
     * @param table file table
     * @param size calculated directory size
     * @param finish true if worker completely finish task, false if it will just repaint table
     */
    public void addProcessedDirectory(AbstractFile path, FileTable table, long size, boolean finish) {
        synchronized (directorySizes) {
            directorySizes.put(path, size);
        }
        synchronized (calculateSizeQueue) {
            calculateSizeQueue.remove(path);
        }
        if (finish) {
            processNextQueuedFile(table);
        }
    }


    /**
     * Stops directory calculation, clears calculated size ant tasks queue, interrupts currently executed worker if exists
     */
    private void stopSizeCalculation() {
        synchronized (directorySizes) {
            directorySizes.clear();
        }
        synchronized (calculateSizeQueue) {
            calculateSizeQueue.clear();
        }
        if (calculateDirectorySizeWorker != null) {
            try {
                calculateDirectorySizeWorker.cancel(true);
            } catch (Exception ignore) { }
            calculateDirectorySizeWorker = null;
        }
        synchronized (this) {
            markedDirectories.clear();
        }
        hasCalculatedDirectories = false;
    }


    private long calcMarkedDirectoriesSize() {
        if (!hasCalculatedDirectories) {
            return 0;
        }
        long result = 0;
        synchronized (this) {
            for (AbstractFile file : markedDirectories) {
                Long dirSize;
                synchronized (directorySizes) {
                    dirSize = directorySizes.get(file);
                }
                if (dirSize != null) {
                    result += dirSize;
                }
            }
        }
        return result;
    }

    public String getFileNameAt(int index) {
        return (index == 0 && hasParentFolder()) ? ".." : getFileAt(index).getName();
    }

    public synchronized int getFilesCount() {
        return fileArrayIndex.length + (parent == null ? 0 : 1);
    }

//    public void setFirstVisibleRow(int row) {
//        this.firstVisibleRow = row;
//    }


    public AbstractFile getCurrentCalculatedSizeDirectory() {
        if (calculateDirectorySizeWorker != null) {
            return calculateDirectorySizeWorker.getFile();
        }
        return null;
    }


}
