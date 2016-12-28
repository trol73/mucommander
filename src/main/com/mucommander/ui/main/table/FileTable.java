/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2012 Maxence Bernard
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

package com.mucommander.ui.main.table;

import java.awt.*;
import java.awt.event.*;
import java.util.Iterator;
import java.util.WeakHashMap;

import javax.swing.*;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import com.mucommander.text.SizeFormat;
import com.mucommander.ui.action.impl.*;
import com.mucommander.ui.main.table.views.BaseCellRenderer;
import com.mucommander.ui.main.table.views.BaseFileTableModel;
import com.mucommander.ui.main.table.views.TableViewMode;
import com.mucommander.ui.main.table.views.compact.CompactFileTableColumnModel;
import com.mucommander.ui.main.table.views.compact.CompactFileTableModel;
import com.mucommander.ui.main.table.views.full.FileTableCellRenderer;
import com.mucommander.ui.main.table.views.full.FileTableColumnModel;
import com.mucommander.ui.main.table.views.full.FileTableConfiguration;
import com.mucommander.ui.main.table.views.full.FileTableModel;
import com.mucommander.ui.theme.*;
import com.mucommander.utils.FileIconsCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jidesoft.swing.DefaultOverlayable;
import com.jidesoft.swing.StyledLabelBuilder;

import com.mucommander.commons.collections.Enumerator;
import com.mucommander.commons.conf.ConfigurationEvent;
import com.mucommander.commons.conf.ConfigurationListener;
import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.util.FileSet;
import com.mucommander.commons.runtime.OsFamily;
import com.mucommander.commons.runtime.OsVersion;
import com.mucommander.conf.MuConfigurations;
import com.mucommander.conf.MuPreference;
import com.mucommander.conf.MuPreferences;
import com.mucommander.desktop.DesktopManager;
import com.mucommander.job.MoveJob;
import com.mucommander.text.CustomDateFormat;
import com.mucommander.text.Translator;
import com.mucommander.ui.action.ActionKeymap;
import com.mucommander.ui.action.ActionManager;
import com.mucommander.ui.action.MuAction;
import com.mucommander.ui.dialog.file.AbstractCopyDialog;
import com.mucommander.ui.dialog.file.FileCollisionDialog;
import com.mucommander.ui.dialog.file.ProgressDialog;
import com.mucommander.ui.event.ActivePanelListener;
import com.mucommander.ui.event.TableSelectionListener;
import com.mucommander.ui.icon.FileIcons;
import com.mucommander.ui.icon.IconManager;
import com.mucommander.ui.main.FolderPanel;
import com.mucommander.ui.main.MainFrame;
import com.mucommander.ui.main.menu.TablePopupMenu;
import com.mucommander.ui.quicksearch.QuickSearch;


/**
 * A heavily modified <code>JTable</code> which displays a folder's contents and allows file mouse and keyboard selection,
 * marking and navigation. <code>JTable</code> provides the basics for file selection but its behavior has to be
 * extended to allow file marking.
 *
 * @author Maxence Bernard, Nicolas Rinaudo
 */
public class FileTable extends JTable implements MouseListener, MouseMotionListener, KeyListener,
                                                 ActivePanelListener, ConfigurationListener, ThemeListener {
	private static Logger logger;
	
    // - Column sizes --------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /** Minimum width for 'name' column when in automatic column sizing mode */
    private final static int RESERVED_NAME_COLUMN_WIDTH = 40;
    /** Minimum column width when in automatic column sizing mode */
    private final static int MIN_COLUMN_AUTO_WIDTH = 20;

    private static final int MAX_ROWS_FOR_AUTO_LAYOUT_CALCULATION = 50;
    private static final Dimension INTERCELL_SPACING = new Dimension(0, 0);


    // - Containers ----------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /** Frame containing this file table. */
    private MainFrame   mainFrame;
    /** Folder panel containing this frame. */
    private FolderPanel folderPanel;


    // - UI components -------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /** TableModel instance used by this JTable to get cells' values */
    private BaseFileTableModel tableModel;
    /** TableCellRender instance used by this JTable to render cells */
    private BaseCellRenderer cellRenderer;
    /** CellEditor used to edit filenames when clicked */
    private FilenameEditor        filenameEditor;

    /** Contains sort-related variables */
    private final SortInfo sortInfo = new SortInfo();

    /** Row currently selected */
    private int currentRow;

    /** Column currently selected */
    private int currentColumn;

    // Used when right button is pressed and mouse is dragged
    private boolean markOnRightClick;
    private int     lastDraggedRow = -1;

    // Used by shift+Click
    private int lastRow;

    /** Allows to detect repeated key strokes of mark key (space/insert) */
    private boolean markKeyRepeated;
    /** In case of repeated mark keystrokes, true if last row has already been marked/unmarked */
    private boolean lastRowMarked;

    /** Timestamp of last row selection change */
    private long selectionChangedTimestamp;

    /** Timestamp of last double click */
    private long lastDoubleClickTimestamp;

    /** Is automatic columns sizing enabled ? */
    private boolean autoSizeColumnsEnabled;

    /** Instance of the inner class that handles quick search */
    private QuickSearch<AbstractFile> quickSearch = new FileTableQuickSearch();

    /** TableSelectionListener instances registered to receive selection change events */
    private WeakHashMap<TableSelectionListener, ?> tableSelectionListeners = new WeakHashMap<>();

    /** True when this table is the current or last active table in the MainFrame */
    private boolean isActiveTable;

    /** Timestamp of the last focus gain (in milliseconds) */
    private long focusGainedTime;


    /** Delay in ms after which filename editor can be triggered when current row's filename cell is clicked */
    private final static int EDIT_NAME_CLICK_DELAY = 500;

    /** Timestamp of last double click - workaround for MouseEvent.getClickCount() */
    private long doubleClickTime;

    /** Counts the number of clicks within the double-click interval */
    private int doubleClickCounter = 1;

    /** Interval to wait for the double-click */
    private static int DOUBLE_CLICK_INTERVAL = DesktopManager.getMultiClickInterval();

    /** Wrapper of presentation adjustments for the file-table */
    private FileTableWrapperForDisplay scrollpaneWrapper;

    /** Table that shows the user to refresh if the location doesn't exist */
    private DefaultOverlayable overlayTable;

    private TableViewMode viewMode;

    private FileTableConfiguration conf;

    /**
     * Number of visible rows on table. Calculated on layout and used to compact/short model
     */
    private int pageSize;

    /**
     * Sometimes cursor gets "sticky". These variables used to detect this situation and fix it
     */
    private int lastSelectedRow, lastSelectedCol, lastSelectedEqCnt;



    public FileTable(MainFrame mainFrame, FolderPanel folderPanel, FileTableConfiguration conf) {
        super(new FileTableModel(), new FileTableColumnModel(conf));    // TODO !!!
//super(new CompactFileTableModel(2, 20), new CompactFileTableColumnModel(2, conf));    // TODO !!!
        this.conf = conf;
        tableModel = (BaseFileTableModel)getModel();
        tableModel.setSortInfo(sortInfo);

        ThemeManager.addCurrentThemeListener(this);

        setAutoResizeMode(AUTO_RESIZE_NEXT_COLUMN);

        // Stores the mainframe and folderpanel.
        this.mainFrame   = mainFrame;
        this.folderPanel = folderPanel;

        // Remove all default action mappings as they conflict with corresponding mu actions
        InputMap inputMap = getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        inputMap.clear();
        inputMap.setParent(null);

        // Initializes the table.
        setShowGrid(false);
        setIntercellSpacing(INTERCELL_SPACING);
        filenameEditor = new FilenameEditor(new JTextField());

        setViewMode(TableViewMode.FULL);     // TODO !!!!!!
        setTableHeader(new FileTableHeader(this));

        // Initializes event listening.
        addMouseListener(this);
        folderPanel.addMouseListener(this);
        addMouseMotionListener(this);
        addKeyListener(this);
        mainFrame.addActivePanelListener(this);
        MuConfigurations.addPreferencesListener(this);

        // Mac OS X 10.5 (Leopard) and up uses JTableHeader properties to render sort indicators on table headers
        // instead of a custom header renderer.
        if (usesTableHeaderRenderingProperties()) {
            setTableHeaderRenderingProperties();
        }
        
        // Initialize a wrapper of presentation adjustments for the file-table
        scrollpaneWrapper = new FileTableWrapperForDisplay(this, folderPanel, mainFrame);

        overlayTable = createOverlayableTable();

        addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                overlayTable.repaint();
            }
        });
    }


    /**
     *
     * @param mode - FULL, COMPACT or SHORT
     */
    public synchronized void setViewMode(TableViewMode mode) {
        if (this.viewMode == mode) {
            return;
        }
        final boolean fromConstructor = this.viewMode == null;
        final int selectedFileIndex = fromConstructor ? 0 : getSelectedFileIndex();
        final SortInfo sortInfo = getSortInfo();

        this.viewMode = mode;
        getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        BaseFileTableModel oldModel = (BaseFileTableModel)getModel();
        switch (mode) {
            case FULL:
                if (!fromConstructor) {
                    setModel(new FileTableModel());
                    setColumnModel(new FileTableColumnModel(conf));
                }
                getColumnModel().getColumn(convertColumnIndexToView(Column.NAME.ordinal())).setCellEditor(filenameEditor);
                setAutoSizeColumnsEnabled(MuConfigurations.getPreferences().getVariable(MuPreference.AUTO_SIZE_COLUMNS, MuPreferences.DEFAULT_AUTO_SIZE_COLUMNS));
                break;

            case COMPACT:
            case SHORT:
                if (!fromConstructor) {
                    setModel(new CompactFileTableModel(mode.getColumnsCount(), pageSize > 0 ? pageSize : 10));
                    setColumnModel(new CompactFileTableColumnModel(mode.getColumnsCount(), conf));
                }
                int columnWidth = getWidth()/mode.getColumnsCount();
                for (int col = 0; col < mode.getColumnsCount(); col++) {
                    TableColumn column = getColumnModel().getColumn(col);
                    column.setCellEditor(filenameEditor);
                    column.setWidth(columnWidth);
                }
                break;
        }
        setRowHeight();
        BaseFileTableModel newModel = (BaseFileTableModel)getModel();
        newModel.setupFromModel(oldModel);
        tableModel = newModel;
        cellRenderer = mode.createCellRenderer(this);
        tableModel.setSortInfo(sortInfo);
        if (!fromConstructor) {
            doLayout();
            try {
                selectFile(selectedFileIndex);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        invalidate();

//        sortBy(Column.NAME);

        //sortBy(sortInfo);
        sortBy(sortInfo.getCriterion());
        sortBy(sortInfo.getCriterion(), sortInfo.getAscendingOrder());
        // TODO restore header selection
    }


    public TableViewMode getViewMode() {
        return viewMode;
    }

    private DefaultOverlayable createOverlayableTable() {
        return new DefaultOverlayable(scrollpaneWrapper) {
            private static final long serialVersionUID = 1L;

            {
                addOverlayComponent(createRefreshNonExistingLocationLabel());
            }

            private JLabel createRefreshNonExistingLocationLabel() {
                JLabel label = StyledLabelBuilder.createStyledLabel("{Refresh to reconnect:f:darkGray}");
                label.setIcon(MuAction.getStandardIcon(RefreshAction.class));
                return label;
            }

            @Override
            public boolean requestFocusInWindow() {
                return scrollpaneWrapper.requestFocusInWindow();
            }

            /**
             * Overridden to ensure that the table is always visible.
             */
            @Override
            public void setVisible(boolean visible) {
                if (visible) {
                    super.setVisible(true);
                }
            }
        };
    }
    

    /**
     * Returns the FileTable as a UI component for display purpose.
     * The UI component is actually a JScrollPane that allows the FileTable to scroll and
     * responsible to set its viewing properties as needed.
     *
     * @return the FileTable as a UI component for display purpose
     */
    public JComponent getAsUIComponent() {
        return overlayTable;
    }

    /**
     * Under Mac OS X 10.5 (Leopard) and up, sets client properties on this table's JTableHeader to indicate the current
     * sort criterion/column and sort order (ascending or descending). These properties allow Mac OS X/Java to render
     * the headers accordingly, instead of having to use a {@link FileTableHeaderRenderer custom header renderer}.
     * This method has no effect whatsoever on platforms other where {@link #usesTableHeaderRenderingProperties()}
     * returns <code>false</code>.
     */
    private void setTableHeaderRenderingProperties() {
        if (!usesTableHeaderRenderingProperties()) {
            return;
        }
        JTableHeader tableHeader = getTableHeader();
        if (tableHeader == null) {
            return;
        }

        boolean isActiveTable = isActiveTable();

        // Highlights the selected column
        tableHeader.putClientProperty("JTableHeader.selectedColumn", isActiveTable
            ? convertColumnIndexToView(sortInfo.getCriterion().ordinal()) : null);

        // Displays an ascending/descending arrow
        tableHeader.putClientProperty("JTableHeader.sortDirection", isActiveTable
            ? sortInfo.getAscendingOrder() ? "ascending":"decending"      // descending is misspelled but this is OK
            : null);

            // Note: if this table is not currently active, properties are cleared to remove the highlighting effect.
            // However, clearing the properties does not yield the desired behavior as it does not restore the table
            // header back to normal. This looks like a bug in Apple's implementation.
        }
    
    /**
     * Restores selection when focus is gained.
     * Note: this is not FocusListener implementation method
     */
    private void focusGained() {
        focusGainedTime = System.currentTimeMillis();

        if (isEditing()) {
            filenameEditor.filenameField.requestFocus();
        } else {
            overlayTable.getOverlayComponents()[0].setEnabled(true);
            // Repaints the table to reflect the new focused state
            overlayTable.repaint();
        }
    }

    /**
     * Hides selection when focus is lost.
     * Note: this is not FocusListener implementation method
     */
    private void focusLost() {
        // Repaints the table to reflect the new focused state
        overlayTable.repaint();
    }

    /**
     * Returns <code>true</code> if the current platform is capable of indicating the sort criterion and sort order
     * on the table headers by setting client properties, instead of using a {@link FileTableHeaderRenderer custom header renderer}.
     * At the moment this method returns <code>true</code> only under Mac OS X 10.5 (and up).
     *  
     * @return true if the current platform is capable of indicating the sort criterion and sort order on the table
     * headers by setting client properties.
     */
    public static boolean usesTableHeaderRenderingProperties() {
        return OsFamily.MAC_OS_X.isCurrent() && OsVersion.MAC_OS_X_10_5.isCurrentOrHigher();
    }


    /**
     * Returns the {@link FolderPanel} that contains this FileTable.
     *
     * @return the FolderPanel that contains this FileTable
     */
    public FolderPanel getFolderPanel() {
        return folderPanel;
    }


    /**
     * Returns <code>true/</code> if this table is the active one in the MainFrame.
     * Being the active table doesn't necessarily mean that it currently has focus, the focus can be in some other component
     * of the active {@link FolderPanel}, or nowhere in the MainFrame if the window is not in the foreground.
     *
     * <p>Use {@link #hasFocus()} to test if the table currently has focus.</p>
     *
     * @return true if this table is the active one in the MainFrame
     * @see com.mucommander.ui.main.MainFrame#getActiveTable()
     */
    public boolean isActiveTable() {
        return isActiveTable;
    }

    /**
     * Convenience method that returns this table's model (the one that {@link #getModel()} returns),
     * as a {@link FileTableModel}, to avoid having to cast it.
     *
     * @return this table's model cast as a FileTableModel
     */
    public BaseFileTableModel getFileTableModel() {
        return tableModel;
    }

    /**
     * Returns a {@link SortInfo} instance that holds information about how this table is currently sorted.
     *
     * @return a SortInfo instance that holds information about how this table is currently sorted
     */
    public SortInfo getSortInfo() {
        return sortInfo;
    }

    /**
     * Returns the {@link QuickSearch} inner class instance used by this FileTable.
     *
     * @return the QuickSearch inner class instance used by this FileTable
     */
    public QuickSearch<AbstractFile> getQuickSearch() {
        return quickSearch;
    }

    /**
     * Returns the file that is currently selected (highlighted), <code>null</code> if the parent folder '..' is
     * currently selected.
     *
     * @return the file that is currently selected (highlighted), null if the parent folder '..' is currently selected
     */
    public synchronized AbstractFile getSelectedFile() {
        return getSelectedFile(false, false);
    }

    /**
     * Returns the file that is currently selected (highlighted). If the currently selected file is the
     * parent folder '..', the parent folder is returned only if the corresponding parameter is <code>true</code>.
     *
     * @param includeParentFolder if <code>true</code> and parent folder '..' is currently selected, the parent folder
     * will be returned.
     * @return the file that is currently selected (highlighted)
     */
    public synchronized AbstractFile getSelectedFile(boolean includeParentFolder) {
        return getSelectedFile(includeParentFolder, false);
    }


    /**
     * Returns the file that is currently selected (highlighted), wrapped in a {@link com.mucommander.commons.file.impl.CachedFile}
     * instance if the corresponding parameter is <code>true</code>. If the currently selected file is the
     * parent folder '..', the parent folder is returned only if the corresponding parameter is <code>true</code>.
     *
     * @param includeParentFolder if true and the parent folder '..' is currently selected, the parent folder file
     * will be returned. If false, null will be returned if the parent folder file is currently selected.
     * @param returnCachedFile if true, a CachedFile corresponding to the currently selected file will be returned
     * @return the file that is currently selected (highlighted)
     */
    public synchronized AbstractFile getSelectedFile(boolean includeParentFolder, boolean returnCachedFile) {
        if (tableModel.getRowCount() == 0 || (!includeParentFolder && isParentFolderSelected())) {
            return null;
        }
        return returnCachedFile ? tableModel.getCachedFileAt(currentRow, currentColumn) : tableModel.getFileAt(currentRow, currentColumn);
    }


    /**
     * Returns selected files in a {@link FileSet}. Selected files are either the marked files or the currently selected
     * file if no file is currently marked. The parent folder '..' is never included in the returned set.
     *
     * @return selected files in a FileSet
     */
    public FileSet getSelectedFiles() {
        FileSet selectedFiles = tableModel.getMarkedFiles();
        // if no row is marked, then add selected row if there is one, and if it is not parent folder
        if (selectedFiles.isEmpty()) {
            AbstractFile selectedFile = getSelectedFile();
            if (selectedFile != null) {
                selectedFiles.add(selectedFile);
            }
        }
        return selectedFiles;
    }

    /**
     * Returns <code>true</code> if the currently selected row/file is the parent folder '..' .
     *
     * @return true if the currently selected row/file is the parent folder '..'
     */
    public boolean isParentFolderSelected() {
        int currentFileIndex = tableModel.getFileIndexAt(currentRow, currentColumn);
        return currentFileIndex == 0 && tableModel.hasParentFolder();
    }

    /**
     * Returns <code>true</code> if the given file index is the parent folder '..' .
     *
     * @param index index of the row to test
     * @return true if the given row is the parent folder '..'
     */
    public boolean isParentFolder(int index) {
        return index == 0 && tableModel.hasParentFolder();
    }

    /**
     * Shorthand for {@link #setCurrentFolder(AbstractFile, AbstractFile[], AbstractFile)} called with no specific file
     * to select (default selection).
     *
     * @param folder the new current folder
     * @param children children of the specified folder
     */
    public void setCurrentFolder(AbstractFile folder, AbstractFile[] children) {
        overlayTable.setOverlayVisible(!folder.exists());
        setCurrentFolder(folder, children, null);
    }

    /**
     * Changes the current folder to the specified one and refreshes the table to reflect the folder's contents.
     * The current file selection is also updated, with the following behavior:
     * <ul>
     *   <li>If <code>filetoSelect</code> is not <code>null</code>, the specified file becomes the currently selected
     * file, if it can be found in the new current folder. Previously marked files are cleared.</li>
     *   <li>If it is <code>null</code>:
     *     <ul>
     *       <li>if the current folder is the same as the previous one, the currently selected file and marked files
     * remain the same, provided they still exist.</li>
     *       <li>if the new current folder is the parent of the previous one, the previous current folder is selected.</li>
     *       <li>in any other case, the first row is selected, whether it be the parent directory ('..') or the first
     * file of the current folder if it has no parent.</li>
     *     </ul>
     *   </li>
     * </ul>
     *
     * <p>
     * This method returns only when the folder has actually been changed and the table refreshed.<br>
     * <b>Important:</b> This method should only be called by {@link FolderPanel} and in any case MUST be synchronized
     * externally to ensure this method is never called concurrently by different threads.
     * </p>
     *
     * @param folder the new current folder
     * @param children children of the specified folder
     * @param fileToSelect the file to select, <code>null</code> for the default selection.
     */
    public void setCurrentFolder(AbstractFile folder, AbstractFile children[], AbstractFile fileToSelect) {
        // Stop quick search in case it was being used before folder change
        quickSearch.stop();

        AbstractFile currentFolder = folderPanel.getCurrentFolder();
        // If we're refreshing the current folder, save the current selection and marked files
        // in order to restore them properly.
        FileSet markedFiles  = null;
        if (currentFolder != null && folder.equalsCanonical(currentFolder)) {
            markedFiles = tableModel.getMarkedFiles();
            if (fileToSelect == null) {
                fileToSelect = getSelectedFile();
            }
        }

        // If we're navigating to the current folder's parent, we select the current folder.
        else if (fileToSelect == null) {
            if (tableModel.hasParentFolder() && folder.equals(tableModel.getParentFolder())) {
                fileToSelect = currentFolder;
            }
        }

        // Changes the current folder in the swing thread to make sure that repaints cannot
        // happen in the middle of the operation - this is used to prevent flickering, badly
        // refreshed frames and such unpleasant graphical artifacts.
        Runnable folderChangeThread = new FolderChangeThread(folder, children, markedFiles, fileToSelect);

        // Wait for the getTask to complete, so that we return only when the folder has actually been changed and the
        // table updated to reflect the new folder.
        // Note: we use a wait/notify scheme rather than calling SwingUtilities#invokeAndWait to avoid deadlocks
        // due to AWT thread synchronization issues.
        synchronized(folderChangeThread) {
            SwingUtilities.invokeLater(folderChangeThread);
            while(true) {
                try {
                    // FolderChangeThread will call notify when done
                    folderChangeThread.wait();
                    break;
                } catch (InterruptedException e) {
                    // will keep looping
                }
            }
        }
    }

    /**
     * Sets row height based on current cell's font and border, revalidates and repaints this JTable.
     */
    private void setRowHeight() {
        // JTable.setRowHeight() revalidates and repaints the JTable.
        // Note that it's important here to use the cell editor's font rather than the cell renderer's: if this method is called
        // as a result to a font changed event, we do not know which class' fontChanged event will be called first.
        setRowHeight(2*CellLabel.CELL_BORDER_HEIGHT + Math.max(getFontMetrics(filenameEditor.filenameField.getFont()).getHeight(), (int)FileIcons.getIconDimension().getHeight()));
        // Filename editor's row resize disabled because of Java bug #4398268 which prevents new rows from being visible after setRowHeight(row, height) has been called :/
        //		setRowHeight(Math.max(getFontMetrics(cellRenderer.getCellFont()).getHeight()+cellRenderer.CELL_BORDER_HEIGHT, editorRowHeight));
    }



    /**
     * Returns <code>true</code> if the auto-columns sizing is currently enabled.
     *
     * @return true if the auto-columns sizing is currently enabled
     */
    public boolean isAutoSizeColumnsEnabled() {
        return this.autoSizeColumnsEnabled;
    }


    /**
     * Enables/disables auto-columns sizing, which automatically resizes columns to fit the table's width.
     *
     * @param enabled true to enable auto-columns sizing, false to disable it
     */
    public void setAutoSizeColumnsEnabled(boolean enabled) {
        this.autoSizeColumnsEnabled = enabled;
        if (autoSizeColumnsEnabled) {
            getTableHeader().setResizingAllowed(false);
            // Will invoke doLayout()
            resizeAndRepaint();
        } else {
            getTableHeader().setResizingAllowed(true);
        }
    }



    /**
     * Controls whether folders are displayed first in this FileTable or mixed with regular files.
     * After calling this method, the table is refreshed to reflect the change.
     * 
     * @param enabled if true, folders are displayed before regular files. If false, files are mixed with directories.
     */
    public void setFoldersFirst(boolean enabled) {
        if (sortInfo.getFoldersFirst() != enabled) {
            sortInfo.setFoldersFirst(enabled);
            sortTable();
        }
    }


    /**
     * Selects the given file, does nothing if this table does not contain the file.
     *
     * @param file the file to select
     * @return true if success
     */
    public boolean selectFile(AbstractFile file) {
        int index = tableModel.getFileIndex(file);
        if (index >= 0) {
            selectFile(index);
            return true;
        }
        return false;
    }


    /**
     * Makes the given row the currently selected one.
     *
     * @param index index of the row to select for full model or index of file for compact model
     */
    public void selectFile(int index) {
        if (index < 0) {
            index = 0;
        }
        if (viewMode == TableViewMode.FULL) {
            changeSelection(index, 0, false, false);
        } else {
            CompactFileTableModel compactModel = (CompactFileTableModel)getModel();
            final int cols = compactModel.getColumnCount();
            final int rows = compactModel.getRowCount();
            if (index < rows*cols) {
                compactModel.setOffset(0);
                changeSelection(index % rows, index / rows, false, false);
            } else {
                compactModel.setOffset((index / (rows * cols)) * rows * cols);
                int index0 = index % (rows*cols);
                changeSelection(index0 % rows, index0 / rows, false, false);
            }
        }
    }

    /**
     * Equivalent to calling {@link #setFileMarked(int, boolean, boolean)} with <code>repaint</code> enabled.
     *
     * @param index index of the file to mark/unmark
     * @param marked true to mark the file, false to unmark it
     */
    public void setFileMarked(int index, boolean marked) {
        setFileMarked(index, marked, true);
    }

    /**
     * Sets the given row as marked/unmarked in the table model, repaints the row to reflect the change,
     * and notifies registered {@link com.mucommander.ui.event.TableSelectionListener} that the files currently marked
     * on this FileTable have changed.
     *
     * <p>This method has no effect if the row corresponds to the parent folder row '..' .</p>
     *
     * @param index index of the file to mark/unmark
     * @param marked true to mark the file, false to unmark it
     * @param repaint true to repaint the row after it has been marked/unmarked
     */
    public void setFileMarked(int index, boolean marked, boolean repaint) {
        if (isParentFolder(index)) {
            return;
        }

        tableModel.setFileMarked(index, marked);
        
        if (repaint) {
            int row = tableModel.getFileRow(index);
            repaintRow(row);
        }

        // Notify registered listeners that currently marked files have changed on this FileTable
        fireMarkedFilesChangedEvent();
    }

    /**
     * Equivalent to calling {@link #setFileMarked(AbstractFile, boolean, boolean)} with <code>repaint</code> enabled.
     *
     * @param file file to mark/unmark
     * @param marked true to mark the file, false to unmark it
     */
    public void setFileMarked(AbstractFile file, boolean marked) {
        setFileMarked(file, marked, true);
    }

    /**
     * Sets the given file as marked/unmarked in the table model, repaints the corresponding row to reflect the change,
     * and notifies registered {@link com.mucommander.ui.event.TableSelectionListener} that currently marked files
     * have changed on this FileTable.
     *
     * @param file file to mark/unmark
     * @param marked true to mark the file, false to unmark it
     * @param repaint true to repaint the file's row after it has been marked/unmarked
     */
    public void setFileMarked(AbstractFile file, boolean marked, boolean repaint) {
        int index = tableModel.getFileIndex(file);

        if (index >= 0) {
            setFileMarked(index, marked, repaint);
        }
    }


    /**
     * Marks or unmarks the current selected file (current row) and advance current row to the next one, 
     * with the following exceptions:
     * <ul>
     * <li>if quick search is active, this method does nothing
     * <li>if '..' file is selected, file is not marked but current row is still advanced to the next one
     * <li>if the {@link MarkSelectedFileAction} key event is repeated and the last file has already
     * been marked/unmarked since the key was last released, the file is not marked in order to avoid
     * marked/unmarked flaps when the mark key is kept pressed.
     * </ul>
     *
     * @see MarkSelectedFileAction
     */
    public void markSelectedFile() {
        // Avoids repeated mark/unmark on last row: return if last row has already been marked/unmarked by repeated mark key strokes
        if (markKeyRepeated && lastRowMarked) {
            return;
        }

        final int fileIndex = getFileTableModel().getFileIndexAt(currentRow, currentColumn);
        // Don't mark '..' file but select next row
        if (!isParentFolderSelected()) {
            setFileMarked(fileIndex, !tableModel.isFileMarked(currentRow, currentColumn));
        }

        // Changes selected item to the next one
        if (fileIndex < tableModel.getFilesCount()-1) {
            selectFile(fileIndex + 1);
        } else if (!lastRowMarked) {
            // Need an explicit repaint to repaint the last row since select row is not called
            repaintRow(currentRow);

            // Last row has been marked/unmarked, value will be reset by keyReleased()
            lastRowMarked = true;
        }

        // Any further mark key events will be considered as repeated until keyReleased() has been called
        markKeyRepeated = true;
    }


    /**
     * Marks or unmarks a range of rows, delimited by the provided start row index and end row index (inclusive).
     * End row index can be lower, greater or equals to the start row.
     *
     * @param start index of the first file to repaint
     * @param end index of the last file to mark, can be lower, greater or equals to startRow
     * @param marked if true, the rows will be marked, unmarked otherwise
     */
    public void setRangeMarked(int start, int end, boolean marked) {
        tableModel.setRangeMarked(start, end, marked);
        int startRow = tableModel.getFileRow(start);
        int endRow = tableModel.getFileRow(end);
        if (viewMode == TableViewMode.FULL) {
        repaintRange(startRow, endRow);
        } else {
            repaintRange(0, tableModel.getRowCount()-1);
        }
        fireMarkedFilesChangedEvent();
    }


    /**
     * Repaints the given row.
     *
     * @param row the row to repaint
     */
    private void repaintRow(int row) {
        repaint(0, row * getRowHeight(), getWidth(), rowHeight);
    }

    /**
     * Repaints a range of rows, delimited by the provided start row index and end row index (inclusive).
     * End row index can be lower, greater or equals to the start row.
     *
     * @param startRow index of the first row to repaint
     * @param endRow index of the last row to repaint, can be lower, greater or equals to startRow
     */
    private void repaintRange(int startRow, int endRow) {
        int rowHeight = getRowHeight();
        repaint(0, Math.min(startRow, endRow)*rowHeight, getWidth(), (Math.abs(startRow-endRow)+1)*rowHeight);
    }


    /**
     * Returns the number of rows that a page down/page up action should jump, based on this FileTable's viewport size.
     * The returned number doesn't take into account the number of rows available in this FileTable.
     *
     * @return the number of rows that a page down/page up action should jump
     */
    public int getPageRowIncrement() {
        return getScrollableBlockIncrement(getVisibleRect(), SwingConstants.VERTICAL, 1)/getRowHeight() - 1;
    }

    /**
     * Sorts this FileTable by the given sort criterion, order and 'folders first' value. The criterion and ascending
     * order will be ignored if the corresponding column is not currently visible, but the 'folders first' value will
     * still be taken into account.
     *
     * @param criterion the sort criterion, see {@link Column} for possible values
     * @param ascending true for ascending order, false for descending order
     * @param foldersFirst if true, folders are displayed before regular files. If false, files are mixed with directories.
     */
    public void sortBy(Column criterion, boolean ascending, boolean foldersFirst) {
        // If we're not changing the current sort values, abort.
        if (criterion == sortInfo.getCriterion() && ascending == sortInfo.getAscendingOrder() && foldersFirst == sortInfo.getFoldersFirst()) {
            return;
        }

        sortInfo.setFoldersFirst(foldersFirst);

        // Ignore the sort criterion and order if the corresponding column is not visible
        if (isColumnVisible(criterion)) {
            sortInfo.setCriterion(criterion);
            sortInfo.setAscendingOrder(ascending);

            // Mac OS X 10.5 (Leopard) and up uses JTableHeader properties to render sort indicators on table headers
            if (usesTableHeaderRenderingProperties()) {
                setTableHeaderRenderingProperties();
            }

            // Repaint header
            getTableHeader().repaint();
        }

        // Sorts table while keeping the current file selection
        sortTable();
    }

    /**
     * Calls {@link #sortBy(Column, boolean, boolean)} with the sort information contained in the given {@link SortInfo}.
     *
     * @param sortInfo the information to use to sort this table.
     */
    public void sortBy(SortInfo sortInfo) {
        sortBy(sortInfo.getCriterion(), sortInfo.getAscendingOrder(), sortInfo.getFoldersFirst());
    }


    /**
     * Sorts this FileTable by the given sort criterion and order. The column corresponding to the specified criterion
     * has to be visible when this method is called. If it isn't, this method won't have any effect.
     *
     * @param criterion the sort criterion, see {@link Column} for possible values
     * @param ascending true for ascending order, false for descending order
     */
    public void sortBy(Column criterion, boolean ascending) {
        sortBy(criterion, ascending, sortInfo.getFoldersFirst());
    }

    /**
     * Sorts this FileTable by the given sort criterion. If the criterion is already the current one, the sort order
     * (ascending or descending) will be reversed.
     *
     * @param criterion the sort criterion, see {@link Column} for possible values
     */
    public void sortBy(Column criterion) {
        if (criterion == sortInfo.getCriterion()) {
            reverseSortOrder();
            return;
        }

        sortBy(criterion, sortInfo.getAscendingOrder());
    }

    /**
     * Convenience method that returns this table's <code>javax.swing.table.TableColumnModel</code> cast as a
     * {@link FileTableColumnModel}.
     *
     * This methods return not null for full table view mode
     *
     * @return this table's TableColumnModel cast as a FileTableColumnModel
     */
    public FileTableColumnModel getFileTableColumnModel() {
        return getColumnModel() instanceof FileTableColumnModel ? (FileTableColumnModel)getColumnModel() : null;
    }

    /**
     * Convenience method that returns this table's <code>javax.swing.table.TableColumnModel</code> cast as a
     * {@link CompactFileTableColumnModel}.
     *
     * This methods return not null for compact table view mode
     *
     * @return this table's TableColumnModel cast as a CompactFileTableColumnModel
     */
    public CompactFileTableColumnModel getCompactFileTableColumnModel() {
        return getColumnModel() instanceof CompactFileTableColumnModel ? (CompactFileTableColumnModel)getColumnModel() : null;
    }

    @Override
    public void setColumnModel(TableColumnModel columnModel) {
        // super.setColumnModel() must be called BEFORE the methods below
        super.setColumnModel(columnModel);
        if (filenameEditor != null) {
            if (viewMode == TableViewMode.FULL) {
            columnModel.getColumn(convertColumnIndexToView(Column.NAME.ordinal())).setCellEditor(filenameEditor);
            } else {
                for (int i = 0; i < columnModel.getColumnCount(); i++) {
                    columnModel.getColumn(i).setCellEditor(filenameEditor);
                }
            }
        }

        // Mac OS X 10.5 (Leopard) and up uses JTableHeader properties to render sort indicators on table headers
        if (usesTableHeaderRenderingProperties()) {
            setTableHeaderRenderingProperties();
        }
    }

    /**
     * Returns <code>true</code> if the specified column is currently visible.
     *
     * @param column column, see {@link Column} for possible values
     * @return true if the specified column is currently visible
     */
    public boolean isColumnVisible(Column column) {
        FileTableColumnModel fileTableColumnModel = getFileTableColumnModel();
        return fileTableColumnModel == null || fileTableColumnModel.isColumnVisible(column);
    }

    /**
     * Returns <code>true</code> if the given column can be displayed given the current folder. Certain columns such as
     * {@link Column#OWNER} and {@link Column#GROUP} can be displayed only if current folder's files are capable
     * of supplying this information.
     * Note that the return value does not take into account the column's current enabled state.
     *
     * @param column column, see {@link Column} for possible values
     * @return true if the given column can be displayed given the current folder
     */
    public boolean isColumnDisplayable(Column column) {
        // Check this against the children's file implementation whenever possible: certain file implementations may
        // return different values for the current folder than for its children. For instance, this is the case for file
        // protocols that have a special file implementation for the root folder (s3 is one).
        AbstractFile file = getFileTableModel().getFileAt(0);
        if (file == null) {
            file = folderPanel.getCurrentFolder();
        }

        // The Owner and Group columns are displayable only if current folder has this information
        switch (column) {
            case OWNER:
                return file.canGetOwner();
            case GROUP:
                return file.canGetGroup();
            default:
                return true;
        }
    }

    /**
     * Updates the visibility of all columns based on their enabled state, and for conditional columns on the
     * current folder.
     */
    public void updateColumnsVisibility() {
        FileTableColumnModel columnModel = getFileTableColumnModel();
        if (columnModel != null) {
            // Full mode
            for (Column c : Column.values()) {
                columnModel.setColumnVisible(c, columnModel.isColumnEnabled(c) && isColumnDisplayable(c));
            }
        }
    }

    /**
     * Returns <code>true</code> if the specified column is enabled.
     *
     * @param column column, see {@link Column} for possible values
     * @return true if the specified column is enabled
     */
    public boolean isColumnEnabled(Column column) {
        FileTableColumnModel fileTableColumnModel = getFileTableColumnModel();
        return fileTableColumnModel == null || fileTableColumnModel.isColumnEnabled(column);
    }

    /**
     * Enables/disables the specified column. Disabling a column will make it invisible. Enabling a column will make it
     * visible only if the column can be displayed. See {@link #isColumnDisplayable(Column)} for more information about
     * this.
     *
     * <p>If the current sort criterion corresponds to the specified column and this
     * column is disabled, the sort criterion will be reset to {@link Column#NAME} to prevent the table from being
     * sorted by an invisible column/criterion.</p>
     *
     * @param column column, see {@link Column} for possible values
     * @param enabled true to enable the column, false to disable it.
     */
    public void setColumnEnabled(Column column, boolean enabled) {
        FileTableColumnModel columnModel = getFileTableColumnModel();
        if (columnModel == null) {
            return;
        }
        // Full view mode
        columnModel.setColumnEnabled(column, enabled);

        // Update the visibility of the column
        updateColumnsVisibility();

        // The column may be the current 'sort by' criterion and may have become invisible.
        // If that is the case, change the criterion to NAME.
        if (sortInfo.getCriterion() == column && !columnModel.isColumnVisible(column)) {
            sortBy(Column.NAME);
        }
    }

    public int getColumnPosition(Column column) {
        FileTableColumnModel fileTableColumnModel = getFileTableColumnModel();
        if (fileTableColumnModel == null) {
            return 0;
        }
        return fileTableColumnModel.getColumnPosition(column.ordinal());
    }

    /**
     * Reverses the current sort order, from ascending to descending or vice-versa.
     */
    public void reverseSortOrder() {
        boolean newSortOrder = !sortInfo.getAscendingOrder();

        sortInfo.setAscendingOrder(newSortOrder);

        // Mac OS X 10.5 (Leopard) and up uses JTableHeader properties to render sort indicators on table headers
        if (usesTableHeaderRenderingProperties()) {
            setTableHeaderRenderingProperties();
        }

        // Repaint header
        getTableHeader().repaint();
        
        // Sorts table while keeping current file selected
        sortTable();
    }


    /**
     * Turns on the filename editor on current row.
     */
    public void editCurrentFilename() {
        // Forces CommandBar to return to its normal state as modify key release event is never fired to FileTable
        mainFrame.getCommandBar().setAlternateActionsMode(false);

        // Temporarily enable editing
        tableModel.setNameColumnEditable(true);
        // Filename editor's row resize disabled because of Java bug #4398268 which prevents new rows from being visible after setRowHeight(row, height) has been called :/
        // // Adjust row height to match filename editor's height
        // setRowHeight(row, (int)filenameEditor.filenameField.getPreferredSize().getHeight());
        // Starts editing clicked cell's name column
        if (viewMode == TableViewMode.FULL) {
            editCellAt(currentRow, convertColumnIndexToView(Column.NAME.ordinal()));
        } else {
            editCellAt(currentRow, currentColumn);
        }

        // Saves current/editing row in the filename editor and requests focus on the text field
        filenameEditor.notifyEditing(currentRow, currentColumn);
        // Disable editing
        tableModel.setNameColumnEditable(false);
    }


    /**
     * Sorts this FileTable and repaints it. Marked files and selected file will remain the same, only
     * their position will have changed in the newly sorted table.
     */
    private void sortTable() {
        // Save currently selected file
        AbstractFile selectedFile = tableModel.getFileAt(currentRow, currentColumn);

        // Sort table, doesn't affect marked files
        tableModel.sortRows();

        // Restore selected file
        selectFile(selectedFile);

        // Repaint table
        repaint();
    }


    ////////////////////////////////////
    // TableSelectionListener methods //
    ////////////////////////////////////

    /**
     * Adds the given TableSelectionListener to the list of listeners that are registered to receive
     * notifications when the currently selected file changes.
     *
     * @param listener the TableSelectionListener instance to add to the list of registered listeners.
     */
    public void addTableSelectionListener(TableSelectionListener listener) {
        tableSelectionListeners.put(listener, null);
    }

    /**
     * Removes the given TableSelectionListener from the list of listeners that are registered to receive
     * notifications when the currently selected file changes.
     * The listener will not receive any further notification after this method has been called
     * (or soon after if events are pending).
     *
     * @param listener the TableSelectionListener instance to add to the list of registered listeners.
     */
    public void removeTableSelectionListener(TableSelectionListener listener) {
        tableSelectionListeners.remove(listener);
    }


    /**
     * Notifies all registered listeners that the currently selected file has changed on this FileTable.
     */
    public void fireSelectedFileChangedEvent() {
        for (TableSelectionListener listener : tableSelectionListeners.keySet()) {
            listener.selectedFileChanged(this);
        }
    }

    /**
     * Notifies all registered listeners that the currently marked files have changed on this FileTable.
     */
    public void fireMarkedFilesChangedEvent() {
        for (TableSelectionListener listener : tableSelectionListeners.keySet()) {
            listener.markedFilesChanged(this);
        }
    }



    // - Layout management ---------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    private void doAutoLayout(boolean respectSize) {
        final AbstractFile currentFolder = getFolderPanel().getCurrentFolder();
        final FontMetrics fm = getFontMetrics(FileTableCellRenderer.getCellFont());
        final int dirStringWidth1 = fm.stringWidth(FileTableModel.DIRECTORY_SIZE_STRING);
        final int dirStringWidth2 = fm.stringWidth(SizeFormat.format(1024 * 1024 * 555, FileTableModel.getSizeFormat())); // some big value with big string-length
        final int dirStringWidth3 = fm.stringWidth(SizeFormat.format(1016 * 1024, FileTableModel.getSizeFormat())); // some other big value with big string-length
        final int dirStringWidth = Math.max(Math.max(dirStringWidth1, dirStringWidth2), dirStringWidth3);

        pageSize = getParent().getSize().height / getRowHeight();

        int remainingWidth = getSize().width - RESERVED_NAME_COLUMN_WIDTH;
        FileTableColumnModel fileTableColumnModel = getFileTableColumnModel();
        if (fileTableColumnModel == null) {
            respectSize = false;
        }

        // Calculate columns width in full view mode
        if (viewMode != TableViewMode.FULL) {
            return;
        }
        Iterator<TableColumn> columns = respectSize ? new Enumerator<>(getColumnModel().getColumns()) : fileTableColumnModel.getAllColumns();
        TableColumn nameColumn = null;

        while (columns.hasNext()) {
            TableColumn column = columns.next();
            Column c = Column.valueOf(column.getModelIndex());

            if (c == Column.NAME) {
                nameColumn = column;
            } else {
                int columnWidth;
                if (c == Column.EXTENSION) {
                    columnWidth = (int) FileIcons.getIconDimension().getWidth();
                } else if (c == Column.DATE) {
                    String val = CustomDateFormat.format(currentFolder.getLastModifiedDate());
                    columnWidth = Math.max(MIN_COLUMN_AUTO_WIDTH, fm.stringWidth(val));
                    columnWidth *= 1.1;
                } else if (c == Column.SIZE) {
                    long size = 1000 * 1024 * 1024;
                    String val = SizeFormat.format(size, BaseFileTableModel.getSizeFormat());
                    columnWidth = Math.max(dirStringWidth, fm.stringWidth(val));
                    columnWidth *= 1.1;
                } else if (c == Column.PERMISSIONS) {
                    columnWidth = Math.max(fm.stringWidth("wwww"), fm.stringWidth(currentFolder.getPermissionsString()));
                } else {
                    columnWidth = MIN_COLUMN_AUTO_WIDTH;

                    int rowCount = getModel().getRowCount();
                    for (int rowNum = 0; rowNum < rowCount; rowNum++) {
                        if (rowNum >= MAX_ROWS_FOR_AUTO_LAYOUT_CALCULATION) {
                            break;
                        }
                        String val = (String)getModel().getValueAt(rowNum, column.getModelIndex());
                        int stringWidth = val == null ? 0 : c == Column.SIZE ? dirStringWidth : fm.stringWidth(val);
                        columnWidth = Math.max(columnWidth, stringWidth);
                    }
                }
                if (respectSize) {
                    columnWidth = Math.min(columnWidth, remainingWidth);
                }
                columnWidth +=  2 * CellLabel.CELL_BORDER_WIDTH;
                    
                column.setWidth(columnWidth);

                // Update subtotal
                remainingWidth -= columnWidth;
                if (remainingWidth < 0) {
                    remainingWidth = 0;
                }
            }
        }
        if (nameColumn != null) {
            nameColumn.setWidth(remainingWidth + RESERVED_NAME_COLUMN_WIDTH);
        }
    }

    private void doStaticLayout() {
        final int width = getSize().width;

        pageSize = getParent().getSize().height / getRowHeight();

        // If ve have compact layout type then just use average column width
        if (getFileTableColumnModel() == null) {
            int columns = getColumnModel().getColumnCount();
            int columnWidth = width / getColumnModel().getColumnCount();
            for (int i = 0; i < columns; i++) {
                getColumnModel().getColumn(i).setWidth(columnWidth);
            }

            // update model if need
            BaseFileTableModel model = getFileTableModel();
            if (model instanceof CompactFileTableModel) {
                CompactFileTableModel compactModel = (CompactFileTableModel)model;
                if (compactModel.getVisibleRows() != pageSize) {
                    compactModel.setVisibleRows(pageSize);
                    SwingUtilities.invokeLater(this::invalidate);
                }
            }

            final AbstractFile selectedFile = getSelectedFile();
            ((CompactFileTableModel) getModel()).setVisibleRows(pageSize);

            // Need to restore file selection
            SwingUtilities.invokeLater(() -> {
                if (selectedFile == null && getParent() != null) {
                    try {
                        selectFile(0);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    selectFile(selectedFile);
                }
            });

            invalidate();
            return;
        }

        // Calculate columns width for full panel view type
        if (width - getColumnModel().getTotalColumnWidth() == 0) {
            return;
        }
        TableColumn nameColumn = getColumnModel().getColumn(convertColumnIndexToView(Column.NAME.ordinal()));
        if (nameColumn.getWidth() + width >= RESERVED_NAME_COLUMN_WIDTH) {
            nameColumn.setWidth(nameColumn.getWidth() + width);
        } else {
            nameColumn.setWidth(RESERVED_NAME_COLUMN_WIDTH);
        }
    }

    /**
     * Overrides JTable's doLayout() method to use a custom column layout (if auto-column sizing is enabled).
     */
    @Override
    public void doLayout() {
        FileTableColumnModel fileTableColumnModel = getFileTableColumnModel();
        if (!autoSizeColumnsEnabled) {
            if (getTableHeader().getResizingColumn() != null) {
                super.doLayout();
            } else if (fileTableColumnModel != null && !fileTableColumnModel.wereColumnSizesSet()) {
                doAutoLayout(false);
            } else {
                doStaticLayout();
            }
        } else {    // Custom layout
            doAutoLayout(true);
        }

        // Ensures that current row is visible (within current viewport), and if not adjusts viewport to center it
        Rectangle visibleRect = getVisibleRect();
        final Rectangle cellRect = getCellRect(currentRow, 0, false);
        if (cellRect.y < visibleRect.y || cellRect.y + getRowHeight( ) >visibleRect.y + visibleRect.height) {
            if (scrollpaneWrapper != null) {
                // At this point JViewport is not yet aware of the new FileTable dimensions, calling setViewPosition
                // would not work. Instead, SwingUtilities.invokeLater is used to delay the call after all pending
                // UI events (including JViewport revalidation) have been processed.
                SwingUtilities.invokeLater(() ->
                        scrollpaneWrapper.getViewport().setViewPosition(new Point(0, Math.max(0, cellRect.y-scrollpaneWrapper.getHeight()/2-getRowHeight()/2)))
                );
            }
        }
    }


    /**
     * Method overridden to return a custom TableCellRenderer.
     */
    @Override
    public TableCellRenderer getCellRenderer(int row, int column) {
        return cellRenderer;
    }


    /**
     * Method overridden to consume keyboard events when quick search is active or when a row is being editing
     * in order to prevent registered actions from being fired.
     */
    @Override
    protected boolean processKeyBinding(KeyStroke ks, KeyEvent ke, int condition, boolean pressed) {
        if (quickSearch.isActive() || isEditing()) {
            return true;
        }

//        if(ActionKeymap.isKeyStrokeRegistered(ks))
//            return false;

        return super.processKeyBinding(ks, ke, condition, pressed);
    }

    /**
     * Overrides the changeSelection method from JTable to track the current selected row (the one that has focus)
     * and fire a {@link com.mucommander.ui.event.TableSelectionListener#selectedFileChanged(FileTable)} event
     * to registered listeners. 
     */
    @Override
    public void changeSelection(int row, int column, boolean toggle, boolean extend) {
        // For shift+click
        lastRow = currentRow;
        int lastColumn = currentColumn;
        currentRow = row;
        currentColumn = column;

        super.changeSelection(row, column, toggle, extend);

        // Sometimes cursor gets "sticky".
        // Here we detect this case and fix it by generating RuntimeException
        if (getSelectedRow() == lastSelectedRow && getSelectedColumn() == lastSelectedCol) {
            lastSelectedEqCnt++;

            if (lastSelectedEqCnt == 10) {
                System.out.println("Sticky cursor!");
                throw new RuntimeException("Sticky cursor!");
               /*
                 at com.mucommander.ui.main.table.FileTable.changeSelection(FileTable.java:1432)
                 at javax.swing.plaf.basic.BasicTableUI$Handler.mouseDragged(BasicTableUI.java:1253)
                 at javax.swing.plaf.basic.BasicTableUI$MouseInputHandler.mouseDragged(BasicTableUI.java:818)
                 at java.awt.AWTEventMulticaster.mouseDragged(AWTEventMulticaster.java:319)
                 at java.awt.AWTEventMulticaster.mouseDragged(AWTEventMulticaster.java:319)
                 at java.awt.AWTEventMulticaster.mouseDragged(AWTEventMulticaster.java:319)
                 at java.awt.Component.processMouseMotionEvent(Component.java:6573)
                 at javax.swing.JComponent.superProcessMouseMotionEvent(JComponent.java:3348)
                 at javax.swing.Autoscroller.actionPerformed(Autoscroller.java:176)
                 at javax.swing.Timer.fireActionPerformed(Timer.java:313)
                 at javax.swing.Timer$DoPostEvent.run(Timer.java:245)
                 at java.awt.event.InvocationEvent.dispatch(InvocationEvent.java:311)
                 at java.awt.EventQueue.dispatchEventImpl(EventQueue.java:749)
                 at java.awt.EventQueue.access$500(EventQueue.java:97)
                 at java.awt.EventQueue$3.run(EventQueue.java:702)
                 at java.awt.EventQueue$3.run(EventQueue.java:696)
                 at java.security.AccessController.doPrivileged(Native Method)
                 at java.security.ProtectionDomain$1.doIntersectionPrivilege(ProtectionDomain.java:75)
                 at java.awt.EventQueue.dispatchEvent(EventQueue.java:719)
                 at java.awt.EventDispatchThread.pumpOneEventForFilters(EventDispatchThread.java:201)
                 at java.awt.EventDispatchThread.pumpEventsForFilter(EventDispatchThread.java:116)
                 at java.awt.EventDispatchThread.pumpEventsForHierarchy(EventDispatchThread.java:105)
                 at java.awt.EventDispatchThread.pumpEvents(EventDispatchThread.java:101)
                 at java.awt.EventDispatchThread.pumpEvents(EventDispatchThread.java:93)
                 at java.awt.EventDispatchThread.run(EventDispatchThread.java:82)
               */
            }
        } else {
            lastSelectedEqCnt = 0;
            lastSelectedRow = row;
            lastSelectedCol = column;
        }
        // If file changed
        if (currentRow != lastRow || (currentColumn != lastColumn && viewMode != TableViewMode.FULL)) {
            // Update selection changed timestamp
            selectionChangedTimestamp = System.currentTimeMillis();
            // notify registered TableSelectionListener instances that the currently selected file has changed
            fireSelectedFileChangedEvent();
        }
        //		// Don't refresh status bar if up, down, space or insert key is pressed (repeated key strokes).
        //		// Status bar will be refreshed whenever the key is released.
        //		// We need this limit because refreshing status bar takes time.
        //		if(downKeyDown || upKeyDown || spaceKeyDown || insertKeyDown)
        //			return;
    }


    @Override
    public Dimension getPreferredSize() {
        Container parentComp = getParent();

        // Filename editor's row resize disabled because of Java bug #4398268 which prevents new rows from being visible after setRowHeight(row, height) has been called :/
        /*
          int height;
          if(isEditing())
          height = (tableModel.getRowCount()-1)*getRowHeight() + editorRowHeight;
          else
          height = tableModel.getRowCount()*getRowHeight();

          return new Dimension(parentComp==null?0:parentComp.getWidth(), height);
        */
        return new Dimension(parentComp == null ? 0 : parentComp.getWidth(), tableModel.getRowCount()*getRowHeight());
    }


    @Override
    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }


    /**
     * Overridden for debugging purposes.
     */
    @Override
    public String toString() {
        return getClass().getName()+"@"+hashCode() +" currentFolder="+folderPanel.getCurrentFolder()+" hasFocus="+hasFocus()+" currentRow="+currentRow;
    }

    ///////////////////////////
    // MouseListener methods //
    ///////////////////////////

    public void mouseClicked(MouseEvent e) {
        // Discard mouse events while in 'no events mode'
        if (mainFrame.getNoEventsMode()) {
            return;
        }

        Object source = e.getSource();

        // Under Linux with GNOME and KDE, Java does not honour the  multi/double-click speed preferences
        // (see http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=5076635) and defaults to a 200ms double-click
        // interval, which for most people is too low. Therefore, we cannot rely on MouseEvent#getClickCount() and
        // MouseEvent#getMultiClickInterval() to always work properly and have to detect double-clicks using the
        // proper system multi-click interval returned by DefaultManager#getMultiClickInterval().
        if ((System.currentTimeMillis() - doubleClickTime) < DOUBLE_CLICK_INTERVAL && selectionChangedTimestamp < doubleClickTime) {
            if (doubleClickCounter == 1) {
                doubleClickCounter = 2; // increase only once
                e.consume(); // and make sure this event is not sent anywhere else
            }
        } else {
            /* reset the counter for the double click count */
            doubleClickTime = System.currentTimeMillis();
            doubleClickCounter = 1;
        }

        // If one of the table cells was left clicked...
        if (source == this && DesktopManager.isLeftMouseButton(e)) {
            // Clicking on the selected row's ... :
            // - 'name' label triggers the filename editor
            // - 'date' label triggers the change date dialog
            // - 'permissions' label triggers the change permissions dialog, only if permissions can be changed
            // Timestamp check is used to make sure that this mouse click did not trigger current row selection
            if (doubleClickCounter == 1 && (System.currentTimeMillis() - selectionChangedTimestamp) > EDIT_NAME_CLICK_DELAY) {
                int clickX = e.getX();
                Point p = new Point(clickX, e.getY());
                final int row = rowAtPoint(p);
                final int viewColumn = columnAtPoint(p);

                final Column column = viewMode == TableViewMode.FULL ? Column.valueOf(convertColumnIndexToModel(viewColumn)) : null;
                final boolean isNameColumn = column == null || column == Column.NAME;
                final boolean isDateColumn = column == Column.DATE;
                final boolean isPermissionColumn = column == Column.PERMISSIONS;
                // Test if the clicked row is current row, if column is name column, and if current row is not '..' file
                if (row == currentRow && !isParentFolderSelected() && (isNameColumn || isDateColumn || isPermissionColumn)) {
                    // Test if clicked point is inside the label and abort if not
                    FontMetrics fm = getFontMetrics(FileTableCellRenderer.getCellFont());
                    int labelWidth;
                    if (column != null) {
                        labelWidth = fm.stringWidth((String) tableModel.getValueAt(row, column.ordinal()));
                    } else {
                        labelWidth = fm.stringWidth((String) tableModel.getValueAt(row, viewColumn));
                    }
                    int columnX = (int) getTableHeader().getHeaderRect(viewColumn).getX();
                    if (clickX < columnX+CellLabel.CELL_BORDER_WIDTH || clickX > columnX+labelWidth+CellLabel.CELL_BORDER_WIDTH) {
                        return;
                    }

                    // The following test ensures that this mouse click is not the one that gave the focus to this table.
                    // Not checking for this would cause a single click on the inactive table's current row to trigger
                    // the filename/date/permission editor
                    if (hasFocus() && System.currentTimeMillis() - focusGainedTime > 100) {
                        // create a new thread and sleep long enough to ensure that this click was not the first of a double click
                        new Thread(() -> {
                            try {
                                Thread.sleep(800);
                            } catch (InterruptedException ignore) {}

                            // Do not execute this block (cancel editing) if:
                            // - a double click was made in the last second
                            // - current row changed
                            // - isEditing() is true which could happen if multiple clicks were made
                            if ((System.currentTimeMillis() - lastDoubleClickTimestamp) > 1000 && row == currentRow) {
                                if (isNameColumn) {
                                    if (!isEditing()) {
                                        editCurrentFilename();
                                    }
                                } else if (isDateColumn) {
                                    ActionManager.performAction(ChangeDateAction.Descriptor.ACTION_ID, mainFrame);
                                } else if (isPermissionColumn) {
                                    if (getSelectedFile().getChangeablePermissions().getIntValue() != 0) {
                                        ActionManager.performAction(ChangePermissionsAction.Descriptor.ACTION_ID, mainFrame);
                                    }
                                }
                            }
                        }).start();
                    }
                }
            }
            // Double-clicking on a row opens the file/folder
            else if (doubleClickCounter == 2) { // Note: user can double-click multiple times
                this.lastDoubleClickTimestamp = System.currentTimeMillis();
                ActionManager.performAction(e.isShiftDown()
                        ? OpenNativelyAction.Descriptor.ACTION_ID
                        : OpenAction.Descriptor.ACTION_ID
                    , mainFrame);
            }

        }
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
        // Discard mouse events while in 'no events mode'
        if (mainFrame.getNoEventsMode()) {
            return;
        }

        if (e.getSource() != this) {
            return;
        }

        // Right-click brings a contextual popup menu
        if (DesktopManager.isRightMouseButton(e)) {
            // Find the row that was right-clicked
            int x = e.getX();
            int y = e.getY();
            Point point = new Point(x, y);
            int clickedRow = rowAtPoint(point);
            int clickedCol = columnAtPoint(point);

            // Does the row correspond to the parent '..' folder ? 
            boolean parentFolderClicked = clickedRow == 0 && tableModel.hasParentFolder();

            // Select clicked row if it is not selected already
            if (currentRow != clickedRow) {
                int index = tableModel.getFileIndexAt(clickedRow, clickedCol);
                selectFile(index);
            }

            // Request focus on this FileTable is focus is somewhere else
            if (!hasFocus()) {
                requestFocus();
            }

            // Popup menu where the user right-clicked
            new TablePopupMenu(mainFrame, folderPanel.getCurrentFolder(), parentFolderClicked?null:tableModel.getFileAt(clickedRow, clickedCol), parentFolderClicked, tableModel.getMarkedFiles()).show(this, x, y);
        }
        // Middle-click on a row marks or unmarks it
        // Control left-click also works
        else if (DesktopManager.isMiddleMouseButton(e)) {
            // Used by mouseDragged
            lastDraggedRow = rowAtPoint(e.getPoint());
            int lastDraggedCol = columnAtPoint(e.getPoint());
            markOnRightClick = !tableModel.isFileMarked(lastDraggedRow, lastDraggedCol);

            int lastDraggedFile = tableModel.getFileIndexAt(lastDraggedRow, lastDraggedCol);
            setFileMarked(lastDraggedFile, markOnRightClick);
        } else if (DesktopManager.isLeftMouseButton(e)) {
            if (e.isShiftDown()) {
                // Marks a group of rows, from last current row to clicked row (current row)
                setRangeMarked(currentRow, lastRow, !tableModel.isFileMarked(currentRow, currentColumn));
            } else if (e.isControlDown()) {
                // Marks the clicked file
                int rowNum = rowAtPoint(e.getPoint());
                int colNum = columnAtPoint(e.getPoint());
                int index = tableModel.getFileIndexAt(rowNum, colNum);
                setFileMarked(index, !tableModel.isFileMarked(rowNum, colNum));
            }
        }
    }

    public void mouseReleased(MouseEvent e) {
    }

    /////////////////////////////////
    // MouseMotionListener methods //
    /////////////////////////////////

    public void mouseDragged(MouseEvent e) {
        // Discard mouse motion events while in 'no events mode'
        if (mainFrame.getNoEventsMode()) {
            return;
        }

        // Marks or unmarks every row that was between the last mouseDragged point
        // and the current one
        if (DesktopManager.isMiddleMouseButton(e) && lastDraggedRow >= 0) {
            int draggedRow = rowAtPoint(e.getPoint());
            // Mouse was dragged outside of the FileTable
            if (draggedRow < 0) {
                return;
            }

            setRangeMarked(lastDraggedRow, draggedRow, markOnRightClick);
            lastDraggedRow = draggedRow;
        }
    }


    public void mouseMoved(MouseEvent e) {
    }


    /////////////////////////
    // KeyListener methods //
    /////////////////////////

    public void keyPressed(KeyEvent e) {
        // Handle Left/Right keys for compact modes
        if (viewMode != TableViewMode.FULL) {
            if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                if (getSelectedFileIndex() > 0) {
                    int newFileIndex = Math.max(getSelectedFileIndex() - getRowCount(), 0);
                    selectFile(newFileIndex);
                    //repaintRow(getSelectedRow());
                    repaint(0, 0, getWidth(), getHeight());
                    e.consume();
                }
            } else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                int newFileIndex = Math.min(getSelectedFileIndex() + getRowCount(), getFilesCount() - 1);
                selectFile(newFileIndex);
                //repaintRow(getSelectedRow());
                repaint(0, 0, getWidth(), getHeight());
                e.consume();
            }
        }
    }

    public void keyTyped(KeyEvent e) {
    }

    public void keyReleased(KeyEvent e) {
        // Discard keyReleased events while quick search is active
        if (quickSearch.isActive()) {
            return;
        }

        // Test if the event corresponds to the 'Mark/unmark selected file' action keystroke.
        if (ActionManager.getActionInstance(MarkSelectedFileAction.Descriptor.ACTION_ID, mainFrame).isAccelerator(KeyStroke.getKeyStrokeForEvent(e))) {
            // Reset variables used to detect repeated key strokes
            markKeyRepeated = false;
            lastRowMarked = false;
        }
    }

    /////////////////////////////////
    // ActivePanelListener methods //
    /////////////////////////////////

    public void activePanelChanged(FolderPanel folderPanel) {
        isActiveTable = folderPanel==getFolderPanel();

        // Mac OS X 10.5 (Leopard) and up uses JTableHeader properties to render sort indicators on table headers
        // instead of a custom header renderer. These indicators change when the active table has changed. 
        if (usesTableHeaderRenderingProperties()) {
            setTableHeaderRenderingProperties();
        }
        
        if (isActiveTable) {
        	focusGained();
        } else {
        	focusLost();
        }
    }




    ///////////////////////////////////
    // ConfigurationListener methods //
    ///////////////////////////////////

    /**
     * Listens to certain configuration variables.
     */
    public void configurationChanged(ConfigurationEvent event) {
        switch (event.getVariable()) {
            case MuPreferences.DISPLAY_COMPACT_FILE_SIZE:
            	FileTableModel.setSizeFormat(event.getBooleanValue());
            	tableModel.fillCellCache();
            	resizeAndRepaint();
                break;
            case MuPreferences.DATE_FORMAT:
            case MuPreferences.DATE_SEPARATOR:
            case MuPreferences.TIME_FORMAT:
                // Note: for the update to work properly, CustomDateFormat's configurationChanged() method has to be called
                // before FileTable's, so that CustomDateFormat gets notified of date format first.
                // Since listeners are stored by MuConfiguration in a hash map, order is pretty much random.
                // So CustomDateFormat#updateDateFormat() has to be called before to ensure that is uses the new date format.
                CustomDateFormat.updateDateFormat();
                tableModel.fillCellCache();
                resizeAndRepaint();
                break;
            case MuPreferences.TABLE_ICON_SCALE:
                // Repaint file icons if their size has changed
                // Recalculate row height, revalidate and repaint the table
                setRowHeight();
                break;
            case MuPreferences.USE_SYSTEM_FILE_ICONS:
                // Repaint file icons if the system file icons policy has changed
                repaint();
        }
    }




    /**
     * <p>A Custom CellEditor which provides the following functionalities:
     * <ul>
     * <li>Filename selection (without extension) when filename starts being edited.
     * <li>Can be cancelled by pressing ESCAPE
     * <li>Starts renaming the file when ENTER is pressed
     * </ul>
     *
     * <p>Only once instance per FileTable is created.
     *
     * <p><b>Implementation note:</b> stopCellEditing() and cancelCellEditing() should not be overridden to detect
     * accept/cancel user events as they are totally unrealiable and often not called, for example when clicking
     * on one of the table's headers (many other cases).
     */
    private class FilenameEditor extends DefaultCellEditor {

        private JTextField filenameField;

        /** Row that is currently being edited */
        private int editingRow;
        /** Column that is currently being edited */
        private int editingCol;

        /**
         * Creates a new FilenameEditor instance.
         *
         * @param textField the text field to use for editing filenames
         */
        public FilenameEditor(JTextField textField) {
            super(textField);
            this.filenameField = textField;
            // Sets the font to the same one that's used for cell rendering (user-defined)
            filenameField.setFont(FileTableCellRenderer.getCellFont());
            textField.addKeyListener(
                new KeyAdapter() {
                    // Cancel editing when escape key pressed, this is unfortunately not DefaultCellEditor's default behavior
                    @Override
                    public void keyPressed(KeyEvent e) {
                        int keyCode = e.getKeyCode();
                        if (keyCode == KeyEvent.VK_ESCAPE) {
                            cancelCellEditing();
                        }
                    }
                }
            );
            textField.addActionListener(e -> rename());
            textField.addFocusListener(new FocusListener() {
				
				public void focusLost(FocusEvent e) {
					cancelCellEditing();
					FileTable.this.repaint();
				}
				
				public void focusGained(FocusEvent e) {}
			});
        }
        

        /**
         * Renames the currently edited name cell, only if the filename has changed.
         */
        private void rename() {
            String newName = filenameField.getText();
            AbstractFile fileToRename = tableModel.getFileAt(editingRow, editingCol);

            if (!newName.equals(fileToRename.getName())) {
                AbstractFile current = folderPanel.getCurrentFolder();
                // Starts moving files
                ProgressDialog progressDialog = new ProgressDialog(mainFrame, Translator.get("move_dialog.moving"));
                FileSet files = new FileSet(current);
                files.add(fileToRename);
                MoveJob renameJob = new MoveJob(progressDialog, mainFrame, files, current, newName, FileCollisionDialog.ASK_ACTION, true);
                progressDialog.start(renameJob);
            }
        }


        /**
         * Restores default row height.
         */
/*
        public void restore() {
            // Filename editor's row resize disabled because of Java bug #4398268 which prevents new rows from being visible after setRowHeight(row, height) has been called.
            // Add to that the fact that DefaultCellEditor's stopCellEditing() and cancelCellEditing() are not always called, for instance when table header is clicked.
            //				setRowHeight(currentRow, cellRenderer.getFontMetrics(cellRenderer.getCellFont()).getHeight()+cellRenderer.CELL_BORDER_HEIGHT);
        }
*/

        /**
         * Notifies this editor that the given row's filename cell is being edited. This method has to be called once
         * when a row just started being edited. It will save the row number and select the filename without
         * its extension to make it easier to rename.
         *
         * @param row row which is being edited
         * @param col column which is being edited
         * @see AbstractCopyDialog#selectDestinationFilename(AbstractFile, String, int)
         */
        void notifyEditing(int row, int col) {
            // The editing row has to be saved as it could change after row editing has been started
            this.editingRow = row;
            this.editingCol = col;

            AbstractFile file = tableModel.getFileAt(editingRow, editingCol);
            AbstractCopyDialog.selectDestinationFilename(file, file.getName(), 0).feedToPathField(filenameField);

            // Request focus on text field
            filenameField.requestFocus();
        }


        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            Component result = super.getTableCellEditorComponent(table, value, isSelected, row, column);
            if (viewMode != TableViewMode.FULL) {
                JPanel panel = new JPanel(new BorderLayout());
                panel.add(result, BorderLayout.CENTER);
                final AbstractFile file = tableModel.getFileAt(row, column);
                CellLabel lblIcon = new CellLabel();
                lblIcon.setIcon(FileIconsCache.getInstance().getIcon(file));
                lblIcon.setBackground(ThemeCache.backgroundColors[ThemeCache.ACTIVE][(row % 2 == 0) ? ThemeCache.NORMAL : ThemeCache.ALTERNATE]);
                panel.setBackground(lblIcon.getBackground());
                //lblIcon.setHasSeparator(column < tableModel.getColumnCount() - 1);
                panel.add(lblIcon, BorderLayout.WEST);
                return panel;
            }
            return result;
        }
    }

    /**
     * This inner class adds 'quick search' functionality to the FileTable
     */
    private class FileTableQuickSearch extends QuickSearch<AbstractFile> {

        /**
         * Creates a new QuickSearch instance, only one instance per FileTable should be created.
         */
        private FileTableQuickSearch() {
        	super(FileTable.this);
        }
        
        @Override
		protected void searchStarted() {
        	// Repaint the table to add the 'dim' effect on non-matching files
            scrollpaneWrapper.dimBackground();
		}

		@Override
		protected void searchStopped() {
			mainFrame.getStatusBar().updateSelectedFilesInfo();
            // Removes the 'dim' effect on non-matching files.
            scrollpaneWrapper.undimBackground();
		}
		
		@Override
		protected int getNumOfItems() {
			return tableModel.getFilesCount();
		}

		@Override
		protected String getItemString(int index) {
            return tableModel.getFileNameAt(index);
		}

		@Override
		protected void searchStringBecameEmpty(String searchString) {
			mainFrame.getStatusBar().setStatusInfo(searchString); // TODO: is needed?			
		}

		@Override
		protected void matchFound(int index, String searchString) {
			// Select best match's row
            int currentFileIndex = tableModel.getFileIndexAt(currentRow, currentColumn);
            if (index != currentFileIndex) {
                selectFile(index);
                //centerRow();
            }

            // Display the new search string in the status bar
            // that indicates that the search has yielded a match
            mainFrame.getStatusBar().setStatusInfo(searchString, IconManager.getIcon(IconManager.IconSet.STATUS_BAR, QUICK_SEARCH_OK_ICON), false);
		}

		@Override
		protected void matchNotFound(String searchString) {
			// No file matching the search string, display the new search string with an icon
            // that indicates that the search has failed
            mainFrame.getStatusBar().setStatusInfo(searchString, IconManager.getIcon(IconManager.IconSet.STATUS_BAR, QUICK_SEARCH_KO_ICON), false);
		}
		
        ///////////////////////////////
        // KeyAdapter implementation //
        ///////////////////////////////

		@Override
	    public synchronized void keyPressed(KeyEvent e) {
	    	// Discard key events while in 'no events mode'
	        if (mainFrame.getNoEventsMode()) {
	            return;
            }
	        
	        char keyChar = e.getKeyChar();

	        // If quick search is not active...
	        if (!isActive()) {
	            // Return (do not start quick search) if the key is not a valid quick search input
	            if (!isValidQuickSearchInput(e)) {
	                return;
                }

	            // Return (do not start quick search) if the typed key corresponds to a registered action's accelerator
	            if (ActionKeymap.isKeyStrokeRegistered(KeyStroke.getKeyStrokeForEvent(e))) {
	                return;
                }

	            // Start the quick search and continue to process the current key event
	            start();
	        }

	        // At this point, quick search is active
	        int keyCode = e.getKeyCode();
	        boolean keyHasModifiers = (e.getModifiersEx() & (KeyEvent.SHIFT_DOWN_MASK | KeyEvent.ALT_DOWN_MASK | KeyEvent.CTRL_DOWN_MASK | KeyEvent.META_DOWN_MASK)) != 0;

	        // Backspace removes the last character of the search string
	        if (keyCode == KeyEvent.VK_BACK_SPACE && !keyHasModifiers) {
	            // Search string is empty already
	            if (isSearchStringEmpty()) {
	                return;
                }

	            removeLastCharacterFromSearchString();

	            // Find the row that best matches the new search string and select it
	            findMatch(0, true, true);
	        }
	        // Escape immediately cancels the quick search
	        else if (keyCode == KeyEvent.VK_ESCAPE && !keyHasModifiers) {
	            stop();
	        }
	        // Up/Down jumps to previous/next match
	        // Shift+Up/Shift+Down marks currently selected file and jumps to previous/next match
	        else if ((keyCode == KeyEvent.VK_UP || keyCode == KeyEvent.VK_DOWN) && !keyHasModifiers) {
	            // Find the first row before/after the current row that matches the search string
	            boolean down = keyCode == KeyEvent.VK_DOWN;
                int currentIndex = tableModel.getFileIndexAt(currentRow, currentColumn);
	            findMatch(currentIndex + (down ? 1 : -1), down, false);
	        }
	        // MarkSelectedFileAction and MarkNextRowAction mark the current row and moves to the next match
	        else if (ActionManager.getActionInstance(MarkSelectedFileAction.Descriptor.ACTION_ID, mainFrame).isAccelerator(KeyStroke.getKeyStrokeForEvent(e))
	             || ActionManager.getActionInstance(MarkNextRowAction.Descriptor.ACTION_ID, mainFrame).isAccelerator(KeyStroke.getKeyStrokeForEvent(e))) {

                int currentIndex = tableModel.getFileIndexAt(currentRow, currentColumn);
	            if (!isParentFolderSelected()) { // Don't mark/unmark the '..' file
                    setFileMarked(currentIndex, !tableModel.isFileMarked(currentRow, currentColumn));
                }

	            // Find the first the next row that matches the search string
	            findMatch(currentIndex+1, true, false);
	        }
	        // MarkPreviousRowAction marks the current row and moves to the previous match
	        else if (ActionManager.getActionInstance(MarkPreviousRowAction.Descriptor.ACTION_ID, mainFrame).isAccelerator(KeyStroke.getKeyStrokeForEvent(e))) {
                int currentIndex = tableModel.getFileIndexAt(currentRow, currentColumn);
	            if (!isParentFolderSelected()) { // Don't mark/unmark the '..' file
                    setFileMarked(currentIndex, !tableModel.isFileMarked(currentRow, currentColumn));
                }

	            // Find the first the previous row that matches the search string
	            findMatch(currentIndex-1, false, false);
	        }
	        // If no modifier other than Shift is pressed and the typed character is not a control character (space is ok)
	        // and a valid Unicode character, add it to the current search string
	        else if (isValidQuickSearchInput(e)) {
	            appendCharacterToSearchString(keyChar);

	            // Find the row that best matches the new search string and select it
	            findMatch(0, true, true);
	        } else {
	            // Test if the typed key combination corresponds to a registered action.
	            // If that's the case, the quick search is canceled and the action is performed.
	            String muActionId = ActionKeymap.getRegisteredActionIdForKeystroke(KeyStroke.getKeyStrokeForEvent(e));
	            if (muActionId != null) {
	                // Consume the key event otherwise it would be fired again on the FileTable
	                // (or any other KeyListener on this FileTable)
	                e.consume();

	                // Cancel quick search
	                stop();

	                // Perform the action
	                ActionManager.getActionInstance(muActionId, mainFrame).performAction();
	            }

	            // Do not update last search string's change timestamp
	            return;
	        }

	        // Update last search string's change timestamp
	        setLastSearchStringChange(System.currentTimeMillis());
	    }
    }

    // End of QuickSearch class


    // - Theme listening -------------------------------------------------------------
    // -------------------------------------------------------------------------------
    /**
     * Not used.
     */
    public void colorChanged(ColorChangedEvent event) {}

    /**
     * Receives theme font changes notifications.
     */
    public void fontChanged(FontChangedEvent event) {
        if (event.getFontId() == Theme.FILE_TABLE_FONT) {
            // Changes filename editor's font
            filenameEditor.filenameField.setFont(event.getFont());

            // Recalculate row height, revalidate and repaint the table
            setRowHeight();
        }
    }

    public FileTableConfiguration getConfiguration() {
        FileTableColumnModel fileTableColumnModel = getFileTableColumnModel();
        return fileTableColumnModel == null ? null : fileTableColumnModel.getConfiguration();
    }

    public int getColumnWidth(Column column) {
        FileTableColumnModel fileTableColumnModel = getFileTableColumnModel();
        if (fileTableColumnModel != null) {
            return fileTableColumnModel.getColumnFromId(column.ordinal()).getWidth();
        }
        return getCompactFileTableColumnModel().getColumn(0).getWidth();
    }

    /**
     * This thread performs the change of current folder.
     *
     * @author Nicolas Rinaudo, Maxence Bernard
     */
    private class FolderChangeThread implements Runnable {
        private AbstractFile   folder;
        private AbstractFile[] children;
        private FileSet        markedFiles;
        private AbstractFile   selectedFile;

        private FolderChangeThread(AbstractFile folder, AbstractFile[] children, FileSet markedFiles, AbstractFile selectedFile) {
            this.folder       = folder;
            this.children     = children;
            this.markedFiles  = markedFiles;
            this.selectedFile = selectedFile;
            setName(getClass().getName());
        }

        public void run() {
            try {
                // Set the new current folder.
                tableModel.setCurrentFolder(folder, children);
                // Update the visibility state of conditional columns
                FileTableColumnModel columnModel = getFileTableColumnModel();

                updateColumnsVisibility();

                // The column corresponding to the current 'sort by' criterion may have become invisible.
                // If that is the case, change the criterion to NAME. 
                if (columnModel != null && !columnModel.isColumnVisible(sortInfo.getCriterion())) {
                    sortInfo.setCriterion(Column.NAME);

                    // Mac OS X 10.5 (Leopard) and up uses JTableHeader properties to render sort indicators on table headers
                    if (usesTableHeaderRenderingProperties()) {
                        setTableHeaderRenderingProperties();
                    }
                }

                // Sort the new folder using the current sort criteria, ascending/descending order and
                // 'show folders first' values.
                tableModel.sortRows();

                // Computes the index of the new row selection.
                int indexToSelect;
                int currentIndex = tableModel.getFileIndexAt(currentRow, currentColumn);
                if (selectedFile != null) {
                    // Tries to find the index of the file to select. If it cannot be found (the file might not
                    // exist anymore, for example), use the closest possible row.
                    indexToSelect = tableModel.getFileIndex(selectedFile);
                    if (indexToSelect < 0) {
                        int filesCount = tableModel.getFilesCount();
                        indexToSelect = currentIndex < filesCount ? currentIndex : filesCount - 1;
                    }
                } else {
                    // If no file was marked as needing to be selected, selects the first line.
                    indexToSelect = 0;
                }

                selectFile(indexToSelect);
                fireSelectedFileChangedEvent();

                // Restore previously marked files (if any / current folder hasn't changed)
                if (markedFiles != null) {
                    // Restore previously marked files
                    int nbMarkedFiles = markedFiles.size();
                    for(int i = 0; i < nbMarkedFiles; i++) {
                        int fileIndex = tableModel.getFileIndex(markedFiles.elementAt(i));
                        if (fileIndex != -1) {
                            tableModel.setFileMarked(fileIndex, true);
                        }
                    }
                    // Notify registered listeners that currently marked files have changed on this FileTable
                    fireMarkedFilesChangedEvent();
                }
                resizeAndRepaint();
            } catch (Throwable e) {
                // While no such thing should happen, we want to make absolutely sure no exception
                // is propagated to the AWT event dispatch thread.
                getLogger().warn("Caught exception while changing folder, this should not happen!", e);
            } finally {
                // Notify #setCurrentFolder that we're done changing the folder.
                synchronized(this) {
                    notify();
                }
            }
        }
    }

    public void updateSelectedFilesStatusbar() {
        mainFrame.getStatusBar().updateSelectedFilesInfo();
    }


    /**
     * For full view mode returns current row, for compact mode returns current file index
     * @return full selected file index (that equals to selected row for full view mode)
     */
    public int getSelectedFileIndex() {
        if (viewMode == TableViewMode.FULL) {
            return getSelectedRow();
        } else {
            CompactFileTableModel model = (CompactFileTableModel)getModel();
            return getSelectedRow() + getSelectedColumn() * getRowCount() + model.getOffset();
        }
    }


    public int getFilesCount() {
        return ((BaseFileTableModel)getModel()).getFilesCount();
    }


    private static Logger getLogger() {
        if (logger == null) {
            logger = LoggerFactory.getLogger(FileTable.class);
        }
        return logger;
    }


}
