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

import com.mucommander.ui.main.table.Column;
import com.mucommander.ui.main.table.FileTable;
import com.mucommander.ui.main.table.views.compact.CompactFileTableCellRenderer;
import com.mucommander.ui.main.table.views.full.FileTableCellRenderer;

/**
 * @author Oleg Trifonov
 * Created on 03/04/15.
 */
public enum TableViewMode {

    FULL(Column.values().length) {
        @Override
        public BaseCellRenderer createCellRenderer(FileTable table) {
            return new FileTableCellRenderer(table);
        }
    },
    COMPACT(2) {
        @Override
        public BaseCellRenderer createCellRenderer(FileTable table) {
            return new CompactFileTableCellRenderer(table);
        }
    },
    SHORT(3) {
        @Override
        public BaseCellRenderer createCellRenderer(FileTable table) {
            return new CompactFileTableCellRenderer(table);
        }
    };


    private final int columns;

    TableViewMode(int columns) {
        this.columns = columns;
    }

    public int getColumnsCount() {
        return columns;
    }

    public abstract BaseCellRenderer createCellRenderer(FileTable table);


}
