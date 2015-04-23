/*
 * This file is part of trolCommander, http://www.trolsoft.ru/trolcommander
 * Copyright (C) 2013-2015 Oleg Trifonov
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
import com.mucommander.ui.main.table.CellLabel;
import com.mucommander.ui.main.table.FileTable;
import com.mucommander.ui.main.table.Column;
import com.mucommander.ui.theme.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.table.TableCellRenderer;
import java.awt.Font;

/**
 * @author Oleg Trifonov
 * Created on 03/04/15.
 */
public abstract class BaseCellRenderer implements TableCellRenderer, ThemeListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(BaseCellRenderer.class);

    protected FileTable table;
    protected BaseFileTableModel tableModel;


    /** Custom JLabel that render specific column cells */
    protected CellLabel[] cellLabels;

    protected BaseCellRenderer(FileTable table) {
        this.table = table;
        this.tableModel = table.getFileTableModel();
    }

    protected static int getFileColorIndex(int fileIndex, AbstractFile file, BaseFileTableModel tableModel) {
        // Parent directory.
        if (fileIndex == 0 && tableModel.hasParentFolder()) {
            return ThemeCache.FOLDER;
        }
        // Marked file
        if (tableModel.isFileMarked(fileIndex)) {
            return ThemeCache.MARKED;
        }
        // Symlink
        if (file.isSymlink()) {
            return ThemeCache.SYMLINK;
        }
        // Hidden file
        if (file.isHidden()) {
            return ThemeCache.HIDDEN_FILE;
        }
        // Directory
        if (file.isDirectory()) {
            return ThemeCache.FOLDER;
        }
        // Archive
        if (file.isBrowsable()) {
            return ThemeCache.ARCHIVE;
        }
        // Executable
        if (file.isExecutable()) {
            return ThemeCache.EXECUTABLE;
        }
        // Plain file
        return ThemeCache.PLAIN_FILE;
    }


    /**
     * Returns the font used to render all table cells.
     */
    public static Font getCellFont() {
        return ThemeCache.tableFont;
    }




    /**
     * Sets CellLabels' font to the current one.
     */
    // TODO
    protected void setCellLabelsFont(Font newFont) {
        // Set custom font
        for (Column c : Column.values()) {
            // No need to set extension label's font as this label renders only icons and no text
            if (c == Column.EXTENSION) {
                continue;
            }

            cellLabels[c.ordinal()].setFont(newFont);
        }
    }


    // - Theme listening -------------------------------------------------------------
    // -------------------------------------------------------------------------------
    /**
     * Receives theme color changes notifications.
     */
    @Override
    public void colorChanged(ColorChangedEvent event) {
        table.repaint();
    }

    /**
     * Receives theme font changes notifications.
     */
    @Override
    public void fontChanged(FontChangedEvent event) {
        if (event.getFontId() == Theme.FILE_TABLE_FONT) {
            setCellLabelsFont(ThemeCache.tableFont);
        }
    }

    protected void debug(String s) {
        LOGGER.debug(s);
    }

}
