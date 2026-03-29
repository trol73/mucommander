/*
 * This file is part of trolCommander, http://www.trolsoft.ru/en/soft/trolcommander
 * Copyright (C) 2013-2016 Oleg Trifonov
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
package com.mucommander.ui.dialog.file;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.ui.main.table.*;
import com.mucommander.ui.theme.ThemeCache;
import com.mucommander.utils.FileIconsCache;

import javax.swing.*;
import java.awt.*;

/**
 * @author Oleg Trifonov
 * Created on 11.01.15.
 */
public class FindFileResultRenderer implements ListCellRenderer<AbstractFile> {

    private final CellLabel cellLabel = new CellLabel();

    @Override
    public Component getListCellRendererComponent(JList<? extends AbstractFile> list, AbstractFile value, int index, boolean isSelected, boolean cellHasFocus) {
        // Need to check that row index is not out of bounds because when the folder
        ListModel<? extends AbstractFile> model = list.getModel();
        if (value == null) {
            return null;
        }

        // Retrieves the various indexes of the colors to apply.
        // Selection only applies when the table is the active one
        final int selectedIndex = isSelected ? ThemeCache.SELECTED : ThemeCache.NORMAL;
        final int colorIndex = getColorIndex(value);

        cellLabel.setIcon(FileIconsCache.getInstance().getIcon(value));

        String text = value.getAbsolutePath();
        Color foregroundColor;
        if (isSelected) {
            foregroundColor = ThemeCache.foregroundColors[ThemeCache.ACTIVE][ThemeCache.SELECTED][colorIndex];
        } else {
            int group = FileGroupResolver.getInstance().resolve(value);
            foregroundColor = group >= 0 ? ThemeCache.groupColors[group] : ThemeCache.foregroundColors[ThemeCache.ACTIVE][ThemeCache.NORMAL][colorIndex];
        }

        cellLabel.setForeground(foregroundColor);

        // Set the label's text, before calculating it width
        cellLabel.setText(text);

        cellLabel.setToolTipText(text);

        // Set background color depending on whether the row is selected or not, and whether the table has focus or not
        if (isSelected) {
            cellLabel.setBackground(ThemeCache.backgroundColors[ThemeCache.ACTIVE][ThemeCache.SELECTED], ThemeCache.backgroundColors[ThemeCache.ACTIVE][ThemeCache.SECONDARY]);
        } else {
            int matchesColorIndex = (index % 2 == 0) ? ThemeCache.NORMAL : ThemeCache.ALTERNATE;
            cellLabel.setBackground(ThemeCache.backgroundColors[ThemeCache.ACTIVE][matchesColorIndex], ThemeCache.backgroundColors[ThemeCache.ACTIVE][matchesColorIndex]);
        }

        if (selectedIndex == ThemeCache.SELECTED) {
            cellLabel.setOutline(cellHasFocus ? ThemeCache.activeOutlineColor : ThemeCache.inactiveOutlineColor);
        } else {
            cellLabel.setOutline(null);
        }

        return cellLabel;
    }

    private static int getColorIndex(AbstractFile file) {
        // Symlink.
        if (file.isSymlink()) {
            return ThemeCache.SYMLINK;
        }

        // Hidden file/folder.
        if (file.isHidden()) {
            return file.isDirectory() ? ThemeCache.HIDDEN_FOLDER : ThemeCache.HIDDEN_FILE;
        }

        // Directory.
        if (file.isDirectory()) {
            return ThemeCache.FOLDER;
        }

        // Archive.
        if (file.isBrowsable()) {
            return ThemeCache.ARCHIVE;
        }

        // Plain file.
        return ThemeCache.PLAIN_FILE;
    }
}
