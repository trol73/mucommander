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

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.desktop.DesktopManager;
import com.mucommander.ui.border.MutableLineBorder;
import com.mucommander.ui.dnd.FileDropTargetListener;
import com.mucommander.ui.main.FolderPanel;
import com.mucommander.ui.main.MainFrame;
import com.mucommander.ui.main.menu.TablePopupMenu;
import com.mucommander.ui.theme.ColorChangedEvent;
import com.mucommander.ui.theme.FontChangedEvent;
import com.mucommander.ui.theme.Theme;
import com.mucommander.ui.theme.ThemeListener;
import com.mucommander.ui.theme.ThemeManager;

import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.border.Border;
import java.awt.Color;
import java.awt.Point;
import java.awt.dnd.DropTarget;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

/**
 * This class is responsible for the viewing aspects of a FileTable component:
 * 1. Wraps the FileTable with a JScrollPane which allows it to scroll.
 * 2. Sets the colors of the FileTable.
 * 3. Sets other presentation aspects of the FileTable component.
 * 4. Initiates a popup window on right click on the FileTable component.
 *
 * @author Arik Hadas
 */
public class FileTableWrapperForDisplay extends JScrollPane implements FocusListener, ThemeListener {

    /**
     * The FileTable being wrapped for display
     */
    private final FileTable fileTable;
    /**
     * Colors relevant for the FileTable or its ScrollPane wrapper
     */
    private Color borderColor;
    private Color unfocusedBorderColor;
    private Color backgroundColor;
    private Color unfocusedBackgroundColor;
    private Color unmatchedBackgroundColor;

    FileTableWrapperForDisplay(final FileTable fileTable, final FolderPanel folderPanel, final MainFrame mainFrame) {
        super(fileTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        this.fileTable = fileTable;

        backgroundColor = ThemeManager.getCurrentColor(Theme.FILE_TABLE_BACKGROUND_COLOR);
        unmatchedBackgroundColor = ThemeManager.getCurrentColor(Theme.FILE_TABLE_UNMATCHED_BACKGROUND_COLOR);
        unfocusedBorderColor = ThemeManager.getCurrentColor(Theme.FILE_TABLE_INACTIVE_BORDER_COLOR);
        unfocusedBackgroundColor = ThemeManager.getCurrentColor(Theme.FILE_TABLE_INACTIVE_BACKGROUND_COLOR);

        // Sets the table border.
        setBorder(new MutableLineBorder(unfocusedBorderColor, 1));
        borderColor = ThemeManager.getCurrentColor(Theme.FILE_TABLE_BORDER_COLOR);

        // Set scroll pane's background color to match the one of this panel and FileTable
        getViewport().setBackground(unfocusedBackgroundColor);
        fileTable.setBackground(unfocusedBackgroundColor);

        // Remove default action mappings that conflict with corresponding mu actions
        InputMap inputMap = getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        inputMap.clear();
        inputMap.setParent(null);

        fileTable.addFocusListener(this);

        // Enable drop support to copy/move/change current folder when files are dropped on the FileTable
        FileDropTargetListener dropTargetListener = new FileDropTargetListener(fileTable.getFolderPanel(), false);
        fileTable.setDropTarget(new DropTarget(fileTable, dropTargetListener));
        setDropTarget(new DropTarget(this, dropTargetListener));

        // Listens to theme events
        ThemeManager.addCurrentThemeListener(this);

        // Catch mouse events on the ScrollPane
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                // Left-click requests focus on the FileTable
                if (DesktopManager.isLeftMouseButton(e)) {
                    fileTable.requestFocus();
                }
                // Right-click brings a contextual popup menu
                else if (DesktopManager.isRightMouseButton(e)) {
                    if (!fileTable.hasFocus()) {
                        fileTable.requestFocus();
                    }
                    AbstractFile currentFolder = folderPanel.getCurrentFolder();
                    new TablePopupMenu(mainFrame, currentFolder, null, false, fileTable.getFileTableModel().getMarkedFiles()).show(FileTableWrapperForDisplay.this, e.getX(), e.getY());
                }
            }
        });

        addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                int rotation = e.getWheelRotation();
                if (rotation != 0) {
                    move(rotation);
                }
            }

            private void move(int move) {
                Point pt = viewport.getViewPosition();

                pt.y += move;
                pt.y = Math.max(0, pt.y);
                pt.y = Math.min(getMaxYExtent(), pt.y);
                viewport.setViewPosition(pt);
            }

            private int getMaxYExtent() {
                int result = viewport.getView().getHeight() - viewport.getHeight();
                return result < 0 ? 0 : result;
            }
        });

    }

    @Override
    public void setVisible(boolean visible) {
        if (visible) {
            super.setVisible(true);
        }
    }

    @Override
    public boolean requestFocusInWindow() {
        return fileTable.requestFocusInWindow();
    }

    /**
     * Dims the scrollpane's background, called by {@link com.mucommander.ui.quicksearch.QuickSearch} when a quick search is started.
     */
    void dimBackground() {
        fileTable.setBackground(unmatchedBackgroundColor);
        getViewport().setBackground(unmatchedBackgroundColor);
    }

    /**
     * Stops dimming the scrollpane's background (returns to a normal background color), called by
     * {@link com.mucommander.ui.quicksearch.QuickSearch} when a quick search is over.
     */
    void undimBackground() {
        // Identifies the new background color.
        Color newColor = fileTable.hasFocus() ? backgroundColor : unfocusedBackgroundColor;

        // If the old and new background color differ, set the new background
        // color.
        // Otherwise, repaint the table - if we were to skip that step, quicksearch
        // cancellation might result in a corrupt display.
        if (newColor.equals(getViewport().getBackground())) {
            fileTable.repaint();
        } else {
            fileTable.setBackground(newColor);
            getViewport().setBackground(newColor);
        }
    }

    ///////////////////////////
    // FocusListener methods //
    ///////////////////////////

    @Override
    public void focusGained(FocusEvent e) {
        setBorderColor(borderColor);
        getViewport().setBackground(backgroundColor);
        fileTable.setBackground(backgroundColor);
        getViewport().repaint();
    }

    @Override
    public void focusLost(FocusEvent e) {
        setBorderColor(unfocusedBorderColor);
        getViewport().setBackground(unfocusedBackgroundColor);
        fileTable.setBackground(unfocusedBackgroundColor);
    }

    private void setBorderColor(Color color) {
        Border border = getBorder();
        // Some (rather evil) look and feels will change borders outside of muCommander's control,
        // this check is necessary to ensure no exception is thrown.
        if (border instanceof MutableLineBorder) {
            ((MutableLineBorder) border).setLineColor(color);
        }
    }

    // - Theme listening -------------------------------------------------------------
    // -------------------------------------------------------------------------------

    /**
     * Receives theme color changes notifications.
     */
    @Override
    public void colorChanged(ColorChangedEvent event) {
        switch (event.getColorId()) {
            case Theme.FILE_TABLE_BORDER_COLOR:
                borderColor = event.getColor();
                if (fileTable.hasFocus()) {
                    setBorderColor(borderColor);
                    repaint();
                }
                break;
            case Theme.FILE_TABLE_INACTIVE_BORDER_COLOR:
                unfocusedBorderColor = event.getColor();
                if (!fileTable.hasFocus()) {
                    setBorderColor(unfocusedBorderColor);
                    repaint();
                }
                break;
            case Theme.FILE_TABLE_BACKGROUND_COLOR:
                backgroundColor = event.getColor();
                if (fileTable.hasFocus()) {
                    getViewport().setBackground(backgroundColor);
                    fileTable.setBackground(backgroundColor);
                }
                break;
            case Theme.FILE_TABLE_INACTIVE_BACKGROUND_COLOR:
                unfocusedBackgroundColor = event.getColor();
                if (!fileTable.hasFocus()) {
                    getViewport().setBackground(unfocusedBackgroundColor);
                    fileTable.setBackground(unfocusedBackgroundColor);
                }
                break;

            case Theme.FILE_TABLE_UNMATCHED_BACKGROUND_COLOR:
                unmatchedBackgroundColor = event.getColor();
                break;
        }
    }

    /**
     * Not used.
     */
    @Override
    public void fontChanged(FontChangedEvent event) {
    }

}
