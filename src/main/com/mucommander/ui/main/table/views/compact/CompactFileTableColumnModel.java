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

import com.mucommander.ui.main.table.FileTable;
import com.mucommander.ui.main.table.FileTableHeaderRenderer;
import com.mucommander.ui.main.table.views.full.FileTableConfiguration;

import javax.swing.DefaultListSelectionModel;
import javax.swing.ListSelectionModel;
import javax.swing.event.TableColumnModelListener;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Enumeration;
import java.util.NoSuchElementException;
import java.util.WeakHashMap;

/**
 * @author Oleg Trifonov
 * Created on 04/04/15.
 */
public class CompactFileTableColumnModel implements TableColumnModel, PropertyChangeListener {
    /** Even though we're not using column selection, the table API forces us to return this instance or will crash. */
    private static final ListSelectionModel SELECTION_MODEL = new DefaultListSelectionModel();

    /** If {@link #widthCache} is set to this, it needs to be recalculated. */
    private static final int CACHE_OUT_OF_DATE = -1;


    private final TableColumn[] columns;

    /** Cache for the table's total width. */
    private int widthCache = CACHE_OUT_OF_DATE;

    /** All registered listeners. */
    private final WeakHashMap<TableColumnModelListener, ?> listeners  = new WeakHashMap<>();

    public CompactFileTableColumnModel(int columns, FileTableConfiguration conf) {
        super();
        this.columns = new TableColumn[columns];
        for (int i = 0; i < columns; i++) {
            TableColumn column = new TableColumn();

            column.setCellEditor(null);
            column.setHeaderValue("Name");
            column.addPropertyChangeListener(this);
            column.setMinWidth(200);
            column.setModelIndex(i);

            this.columns[i] = column;
            // Mac OS X 10.5 (Leopard) and up uses JTableHeader properties to render sort indicators on table headers.
            // On other platforms, we use a custom table header renderer.
            if (!FileTable.usesTableHeaderRenderingProperties()) {
                column.setHeaderRenderer(new FileTableHeaderRenderer());
            }
            column.addPropertyChangeListener(this);
        }
    }

    @Override
    public void addColumn(TableColumn aColumn) {

    }

    @Override
    public void removeColumn(TableColumn column) {

    }

    @Override
    public void moveColumn(int columnIndex, int newIndex) {

    }

    /**
     * Ignored.
     */
    @Override
    public void setColumnMargin(int newMargin) {

    }

    @Override
    public int getColumnCount() {
        return columns.length;
    }

    /**
     * Returns an enumeration on all visible columns.
     * @return an enumeration on all visible columns.
     */
    @Override
    public Enumeration<TableColumn> getColumns() {
        return new ColumnEnumeration();
    }

    @Override
    public int getColumnIndex(Object columnIdentifier) {
        return 0;
    }

    @Override
    public TableColumn getColumn(int columnIndex) {
        return columns[columnIndex];
    }

    /**
     * Returns 0.
     * @return 0.
     */
    @Override
    public int getColumnMargin() {
        return 0;
    }

    /**
     * Returns the index of the column at the specified position.
     * @param  x position of the column to look for.
     * @return  the index of the column at the specified position, <code>-1</code> if not found.
     */
    @Override
    public int getColumnIndexAtX(int x) {
        int count = getColumnCount();
        for (int i = 0; i < count; i++) {
            x = x - getColumn(i).getWidth();
            if (x < 0) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public int getTotalColumnWidth() {
        if (widthCache == CACHE_OUT_OF_DATE) {
            computeWidthCache();
        }
        return widthCache;
    }

    /**
     * Computes the model's width.
     */
    private void computeWidthCache() {
        Enumeration<TableColumn> elements = getColumns();
        widthCache = 0;
        while (elements.hasMoreElements()) {
            widthCache += elements.nextElement().getWidth();
        }
    }

    /**
     * Ignored.
     */
    @Override
    public void setColumnSelectionAllowed(boolean flag) {

    }

    @Override
    public boolean getColumnSelectionAllowed() {
        return true;
    }

    /**
     * Returns an integer array of size 0.
     * @return an integer array of size 0.
     */
    @Override
    public int[] getSelectedColumns() {
        return new int[]{0};
    }

    /**
     * Returns <code>0</code>.
     * @return <code>0</code>.
     */
    @Override
    public int getSelectedColumnCount() {
        return 1;
    }

    /**
     * Ignored.
     */
    @Override
    public void setSelectionModel(ListSelectionModel newModel) {
    }

    /**
     * Returns a default list selection model.
     * <p>
     * Ideally, we'd like to return <code>null</code> here, but the table API takes a dim view
     * of this and we're forced to keep a useless reference.
     * </p>
     * @return a default list selection model.
     */
    @Override
    public ListSelectionModel getSelectionModel() {
        return SELECTION_MODEL;
    }

    @Override
    public void addColumnModelListener(TableColumnModelListener listener) {
        listeners.put(listener, null);
    }

    @Override
    public void removeColumnModelListener(TableColumnModelListener listener) {
        listeners.remove(listener);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {

    }


    /**
     * Browses through the model's visible columns

     * @author Oleg Trifonov
     */
    private class ColumnEnumeration implements Enumeration<TableColumn> {
        /** Index of the next available element in the enumeration. */
        private int nextIndex;

        /**
         * Creates a new column enumeration.
         */
        public ColumnEnumeration() {
            nextIndex = 0;
        }

        /**
         * Returns <code>true</code> if there's a next element in the enumeration.
         * @return <code>true</code> if there's a next element in the enumeration, <code>false</code> otherwise.
         */
        public boolean hasMoreElements() {
            return nextIndex < columns.length;
        }

        /**
         * Returns the next element in the enumeration.
         * @return the next element in the enumeration.
         * @throws NoSuchElementException if there is no next element in the enumeration.
         */
        public TableColumn nextElement() {
            // Makes sure we have at least one more element to return.
            if (!hasMoreElements()) {
                throw new NoSuchElementException();
            }

            return columns[nextIndex++];
        }
    }
}
