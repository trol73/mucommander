/*
 * This file is part of trolCommander, http://www.trolsoft.ru/en/soft/trolcommander
 * Copyright (C) 2013-2025 Oleg Trifonov
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

package com.mucommander.ui.macosx;

import com.mucommander.TrolCommander;
import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.FileFactory;
import com.mucommander.ui.main.FolderPanel;
import com.mucommander.ui.main.WindowManager;

import java.awt.*;
import java.awt.desktop.*;
import java.io.File;


/**
 * This class registers the About, Preferences and Quit handlers using the com.apple.eawt API available
 */
class EAWTHandler {

    EAWTHandler() {
        Desktop desktop = Desktop.getDesktop();
        desktop.setAboutHandler(this::showAbout);
        desktop.setPreferencesHandler(this::showPreferences);
        desktop.setQuitHandler(this::handleQuitRequestWith);
        desktop.setOpenFileHandler(this::openFiles);
    }

    private void showAbout(AboutEvent e) {
        OSXIntegration.showAbout();
    }

    private void showPreferences(PreferencesEvent e) {
        OSXIntegration.showPreferences();
    }

    private void handleQuitRequestWith(QuitEvent quitEvent, QuitResponse quitResponse) {
        if (OSXIntegration.doQuit()) {
            quitResponse.performQuit();
        } else {
            quitResponse.cancelQuit();
        }
    }

    public void openFiles(OpenFilesEvent openFilesEvent) {
        // Wait until the application has been launched. This step is required to properly handle the case where the
        // application is launched with a file to open, for instance when drag-n-dropping a file to the Dock icon
        // when trolCommander is not started yet. In this case, this method is called while Launcher is still busy
        // launching the application (no mainframe exists yet).
        TrolCommander.waitUntilLaunched();
        for (File f : openFilesEvent.getFiles()) {
            AbstractFile file = FileFactory.getFile(f.toString());
            if (file == null) {
                continue;
            }
            FolderPanel activePanel = WindowManager.getCurrentMainFrame().getActivePanel();
            if (file.isBrowsable()) {
                activePanel.tryChangeCurrentFolder(file);
            } else {
                activePanel.tryChangeCurrentFolder(file.getParent(), file, false);
            }
        }

    }
}