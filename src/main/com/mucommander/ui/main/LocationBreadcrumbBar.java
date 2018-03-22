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

package com.mucommander.ui.main;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.ui.button.NonFocusableButton;
import com.mucommander.ui.button.RolloverButtonAdapter;
import com.mucommander.ui.event.LocationEvent;
import com.mucommander.ui.event.LocationListener;
import com.mucommander.ui.theme.ColorChangedEvent;
import com.mucommander.ui.theme.FontChangedEvent;
import com.mucommander.ui.theme.Theme;
import com.mucommander.ui.theme.ThemeListener;
import com.mucommander.ui.theme.ThemeManager;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JToolBar;
import java.awt.Color;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A TooBar which is located on each panel and used to display the location presented in the panel's file-table,
 * and for letting the user change this location.
 * <p>
 * This TextField support:
 * - location changing progress indicator
 * - Theme settings
 *
 * @author Mikhail Tikhomirov
 */
public class LocationBreadcrumbBar extends JToolBar implements MouseListener, LocationListener, ThemeListener {

    /**
     * FolderPanel this text field is displayed in
     */
    private FolderPanel folderPanel;

    LocationBreadcrumbBar(FolderPanel folderPanel) {
        // Decoration properties
        setBorderPainted(false);
        setFloatable(false);
        putClientProperty("JToolBar.isRollover", Boolean.TRUE);

        setFont(ThemeManager.getCurrentFont(Theme.LOCATION_BAR_FONT));
        setForeground(ThemeManager.getCurrentColor(Theme.LOCATION_BAR_FOREGROUND_COLOR));
        setBackground(ThemeManager.getCurrentColor(Theme.LOCATION_BAR_BACKGROUND_COLOR));

        // Listen to mouse events in order to popup a menu when toolbar is right-clicked
        addMouseListener(this);

        this.folderPanel = folderPanel;

        // Listen to location changes to update popup menu choices and disable this component while the location is
        // being changed
        folderPanel.getLocationManager().addLocationListener(this);

        ThemeManager.addCurrentThemeListener(this);
    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {
        Object source = e.getSource();
        if (source instanceof JButton)
            ((JButton) source).setBorderPainted(true);
    }

    @Override
    public void mouseExited(MouseEvent e) {
        Object source = e.getSource();
        if (source instanceof JButton)
            ((JButton) source).setBorderPainted(false);
    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void locationChanging(LocationEvent e) {
        setEnabled(false);
    }

    @Override
    public void locationChanged(LocationEvent e) {
        // Re-enable component and change the location field's text to the new current folder's path
        folderChangeCompleted();
    }

    @Override
    public void locationCancelled(LocationEvent e) {
        // Re-enable component and change the location field's text to the new current folder's path.
        // If the path was entered in the location field, keep the path to give the user a chance to correct it.
        folderChangeCompleted();
    }

    @Override
    public void locationFailed(LocationEvent e) {
        // Re-enable component and change the location field's text to the new current folder's path.
        // If the path was entered in the location field, keep the path to give the user a chance to correct it.
        folderChangeCompleted();
    }

    /**
     * Re-enable this breadcrumb bar after a folder change was completed, cancelled by the user or has failed.
     */
    private void folderChangeCompleted() {
        setPath(folderPanel.getCurrentFolder());
        setEnabled(true);
    }

    private void setPath(AbstractFile path) {
        removeAll();
        final List<AbstractFile> pathList = new ArrayList<>();
        while (path != null) {
            pathList.add(path);
            path = path.getParent();
        }
        pathList.forEach(file -> {
            final String fileName = file.getName();
            final String fileSeparator = file.getSeparator();
            final String name = fileName.contains(fileSeparator) ? fileName : fileSeparator + fileName;
            final NonFocusableButton button = new NonFocusableButton(new AbstractAction(name) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    folderPanel.tryChangeCurrentFolder(file);
                }
            });
            RolloverButtonAdapter.decorateButton(button);
            button.setMargin(new Insets(0, 0, 0, 0));
            button.putClientProperty("JButton.buttonType", "square");


            button.setFont(ThemeManager.getCurrentFont(Theme.LOCATION_BAR_FONT));
            button.setForeground(ThemeManager.getCurrentColor(Theme.LOCATION_BAR_FOREGROUND_COLOR));
            button.setBackground(ThemeManager.getCurrentColor(Theme.LOCATION_BAR_BACKGROUND_COLOR));

            add(button, 0);
        });
        repaint();
    }

    /**
     * Receives theme color changes notifications.
     */
    @Override
    public void colorChanged(ColorChangedEvent event) {
        final Color color = event.getColor();

        switch (event.getColorId()) {
            case Theme.LOCATION_BAR_FOREGROUND_COLOR:
                setForeground(color);
                break;

            case Theme.LOCATION_BAR_BACKGROUND_COLOR:
                setBackground(color);
                break;
        }
    }

    /**
     * Receives theme font changes notifications.
     */
    @Override
    public void fontChanged(FontChangedEvent event) {
        if (event.getFontId() == Theme.LOCATION_BAR_FONT) {
            setFont(event.getFont());
        }
    }

    @Override
    public void setForeground(Color fg) {
        super.setForeground(fg);
        Arrays.asList(getComponents()).forEach(component -> component.setForeground(fg));
    }

    @Override
    public void setBackground(Color bg) {
        super.setBackground(bg);
        Arrays.asList(getComponents()).forEach(component -> component.setBackground(bg));
    }

    @Override
    public void setFont(Font font) {
        super.setFont(font);
        Arrays.asList(getComponents()).forEach(component -> component.setFont(font));
    }

}
