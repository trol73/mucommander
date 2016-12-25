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
package com.mucommander.ui.main.table.views.compact;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.ui.main.table.views.BaseFileTableModel;


/**
 * @author Oleg Trifonov
 * Created on 04/04/15.
 */
public class CompactFileTableModel extends BaseFileTableModel {

    final int columns;

    private int visibleRows;

//    private int rowCount;

    private int offset;

    /** Cell values cache */
    private String cellValuesCache[];

    public CompactFileTableModel(int columns, int visibleRows) {
        super();
        this.columns = columns;
        this.visibleRows = visibleRows;
        //rowCount = calcRowCount();
    }


    @Override
    public synchronized void setupFromModel(BaseFileTableModel model) {
        super.setupFromModel(model);
        initCellValuesCache();
    }

    private int calcRowCount() {
        int files = getFileCount();
        if (parent != null ) {
            files++;
        }
        if (files <= visibleRows) {
            return files;
        }
        int rows = files / columns;
        if (files % columns != 0) {
            rows++;
        }
        return Math.max(visibleRows, rows);
    }


    public synchronized void setCurrentFolder(AbstractFile folder, AbstractFile children[]) {
        super.setCurrentFolder(folder, children);
        //rowCount = calcRowCount();
    }

    @Override
    public void fillCellCache() {
        int len = cellValuesCache.length;
        if (len == 0) {
            return;
        }
        // Special '..' file
        if (parent != null) {
//            cellValuesCache[0] = "..";
            currentFolderDateSnapshot = currentFolder.getLastModifiedDate();
        }

        for (int i = 0; i < len; i++) {
            cellValuesCache[i] = null;
        }
//        int fileIndex = 0;
//        final int indexOffset = parent == null ? 0 : 1;
//        for (int i = indexOffset; i < len; i++) {
//            int cellIndex = fileIndex + indexOffset;
//            cellValuesCache[cellIndex] = null;
//            fileIndex++;
//        }
    }

    /**
     * Init and fill cell cache to speed up table even more
     */
    @Override
    protected void initCellValuesCache() {
        //int files = parent == null ? getFileCount() : getFileCount() + 1;
        this.cellValuesCache = new String[getFileCount()];
    }

    @Override
    public int getRowCount() {
        return visibleRows;
    }

    @Override
    public int getColumnCount() {
        return columns;
    }

    @Override
    public Object getValueAt(int row, int column) {
        int fileIndex = getFileIndexAt(row, column);
        if (parent != null) {
            // Handle special '..' file
            if (fileIndex == 0) {
                return "..";
            }
            fileIndex--;
        }
        // Need to check that file index is not larger than actual number of files
        if (fileIndex < 0 || fileIndex >= fileArrayIndex.length) {
            return null;
        }
        int index = fileArrayIndex[fileIndex];
        String result = cellValuesCache[index];
        if (result == null) {
            result = fillOneCellCache(parent != null ? fileIndex + 1 : fileIndex);
            cellValuesCache[index] = result;
        }
        // TODO preload icons for all visible files
        return result;
        //return fileIndex + ":" + offset + ":" + result;
    }


    /**
     * Returns <code>true</code> if name column has temporarily be made editable by FileTable
     * and given row doesn't correspond to parent file '..', <code>false</code> otherwise.
     */
    @Override
    public boolean isCellEditable(int row, int column) {
        // Name column can temporarily be made editable by FileTable
        // but parent file '..' name should never be editable
        return nameColumnEditable && (parent == null || row != 0 || column != 0 || offset != 0);
    }

    private String fillOneCellCache(int fileIndex) {
        AbstractFile file = getCachedFileAt(fileIndex);
        return file.getName();
    }

    public AbstractFile getFileAt(int row, int column) {
        int index = offset + row + column * visibleRows;
        return (index == 0 && hasParentFolder()) ? parent : getFileAt(index);
    }

    public int getFileIndexAt(int row, int column) {
        if (row < 0 || column < 0) {
            return -1;
        }
        return offset + row + column * visibleRows;
    }

    @Override
    public int getFileRow(int index) {
        return (index - offset) % visibleRows;
    }


    public String getFileNameAt(int row, int column) {
        int index = offset + row + column * visibleRows;
        return (index == 0 && hasParentFolder()) ? ".." : getFileAt(index).getName();
    }


    public int getVisibleRows() {
        return visibleRows;
    }

    public void setVisibleRows(int visibleRows) {
        this.visibleRows = visibleRows;
        //rowCount = calcRowCount();
        fireTableDataChanged();
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

}
