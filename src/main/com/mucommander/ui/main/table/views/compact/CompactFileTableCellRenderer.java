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
package com.mucommander.ui.main.table.views.compact;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.ui.icon.CustomFileIconProvider;
import com.mucommander.ui.icon.FileIcons;
import com.mucommander.ui.icon.IconManager;
import com.mucommander.ui.main.table.CellLabel;
import com.mucommander.ui.main.table.FileGroupResolver;
import com.mucommander.ui.main.table.FileTable;
import com.mucommander.ui.main.table.views.BaseCellRenderer;
import com.mucommander.ui.quicksearch.QuickSearch;
import com.mucommander.ui.theme.ThemeCache;
import com.mucommander.utils.FileIconsCache;

import javax.swing.JTable;
import javax.swing.table.TableColumn;
import java.awt.Color;
import java.awt.Component;

/**
 * @author Oleg Trifonov
 * Created on 04/04/15.
 */
public class CompactFileTableCellRenderer extends BaseCellRenderer {

    private CellLabel emptyLabel = new CellLabel();

    public CompactFileTableCellRenderer(FileTable table) {
        super(table);

        this.cellLabels = new CellLabel[table.getColumnCount()];
        for (int i = 0; i < cellLabels.length; i++) {
            this.cellLabels[i] = new CellLabel();
        }
    }


    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        // Need to check that row index is not out of bounds because when the folder
        // has just been changed, the JTable may try to repaint the old folder and
        // ask for a row index greater than the length if the old folder contained more files
        if (row < 0 || row >= tableModel.getRowCount()) {
            return null;
        }
        CompactFileTableModel model = (CompactFileTableModel)tableModel;
        // Sanity check.
        final AbstractFile file = model.getFileAt(row, column);
        if (file == null) {
            debug("tableModel.getCachedFileAtRow( " + row + ") RETURNED NULL !");
//            emptyLabel.setupText("", 0);
//            emptyLabel.setIcon(null);
//            final QuickSearch search = this.table.getQuickSearch();
//            int matchesColorIndex;
//            if (table.hasFocus() && search.isActive()) {
//                matchesColorIndex = ThemeCache.NORMAL;
//            } else {
//                matchesColorIndex = (row % 2 == 0) ? ThemeCache.NORMAL : ThemeCache.ALTERNATE;
//            }
            final int focusedIndex = table.hasFocus() ? ThemeCache.ACTIVE : ThemeCache.INACTIVE;
//            emptyLabel.setBackground(ThemeCache.backgroundColors[focusedIndex][matchesColorIndex]);
            emptyLabel.setBackground(ThemeCache.backgroundColors[focusedIndex][ThemeCache.NORMAL]);
            emptyLabel.setHasSeparator(column < tableModel.getColumnCount()-1);
            return emptyLabel;
        }

        final QuickSearch search = this.table.getQuickSearch();
        final boolean matches = !table.hasFocus() || !search.isActive() || (file != tableModel.getParentFolder() && search.matches(file.getName()));

        // Retrieves the various indexes of the colors to apply.
        // Selection only applies when the table is the active one
        final int selectedIndex = (isSelected && ((FileTable)table).isActiveTable()) ? ThemeCache.SELECTED : ThemeCache.NORMAL;
        final int focusedIndex = table.hasFocus() ? ThemeCache.ACTIVE : ThemeCache.INACTIVE;
        final int fileIndex = model.getFileIndexAt(row, column);
        final int colorIndex = getFileColorIndex(fileIndex, file, tableModel);

        final CellLabel label = cellLabels[column];

        label.setIcon(fileIndex == 0 && tableModel.hasParentFolder()
                ? IconManager.getIcon(IconManager.IconSet.FILE, CustomFileIconProvider.PARENT_FOLDER_ICON_NAME, FileIcons.getScaleFactor())
                : FileIconsCache.getInstance().getIcon(file));


        String text = (String)value;

        final TableColumn tableColumn = table.getColumnModel().getColumn(column);
        label.setupText(text, tableColumn.getWidth());

        // Set foreground color
        Color foregroundColor;
        if (matches || isSelected) {
            int group = (selectedIndex == ThemeCache.SELECTED) ? -1 : FileGroupResolver.getInstance().resolve(file);
            if (group >= 0 && colorIndex != ThemeCache.MARKED) {
                foregroundColor = ThemeCache.groupColors[group];
            } else {
                foregroundColor = ThemeCache.foregroundColors[focusedIndex][selectedIndex][colorIndex];
            }
        } else {
            foregroundColor = ThemeCache.unmatchedForeground;
        }
        label.setForeground(foregroundColor);

        // Set background color depending on whether the row is selected or not, and whether the table has focus or not
        if (selectedIndex == ThemeCache.SELECTED) {
            label.setBackground(ThemeCache.backgroundColors[focusedIndex][ThemeCache.SELECTED], ThemeCache.backgroundColors[focusedIndex][ThemeCache.SECONDARY]);
        } else if (matches) {
            int matchesColorIndex;
            if (table.hasFocus() && search.isActive()) {
                matchesColorIndex = ThemeCache.NORMAL;
            } else {
                matchesColorIndex = (row % 2 == 0) ? ThemeCache.NORMAL : ThemeCache.ALTERNATE;
            }
            label.setBackground(ThemeCache.backgroundColors[focusedIndex][matchesColorIndex]);
        } else {
            label.setBackground(ThemeCache.unmatchedBackground);
        }

        if (selectedIndex == ThemeCache.SELECTED) {
            label.setOutline(table.hasFocus() ? ThemeCache.activeOutlineColor : ThemeCache.inactiveOutlineColor);
        } else {
            label.setOutline(null);
        }
        label.setHasSeparator(column < tableModel.getColumnCount()-1);

        return label;
    }
}
