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


package com.mucommander.ui.macosx;

import com.apple.eawt.*;
import com.mucommander.Launcher;
import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.FileFactory;
import com.mucommander.ui.main.FolderPanel;
import com.mucommander.ui.main.WindowManager;

import java.io.File;


/**
 * This class registers the About, Preferences and Quit handlers using the com.apple.eawt API available
 * under Java 1.4 and up.
 *
 * @author Maxence Bernard
 */
class EAWTHandler implements AboutHandler, PreferencesHandler, AppReOpenedListener, OpenFilesHandler,
        PrintFilesHandler, QuitHandler {

    public EAWTHandler() {
        Application app = Application.getApplication();

        // Enable the 'About' menu item
        app.setAboutHandler(this);

        // Enable the 'Preferences' menu item
        app.setPreferencesHandler(this);

        app.setOpenFileHandler(this);
        app.setPrintFileHandler(this);
        app.setQuitHandler(this);
    }

    @Override
    public void handleAbout(AppEvent.AboutEvent aboutEvent) {
        OSXIntegration.showAbout();
    }

    @Override
    public void appReOpened(AppEvent.AppReOpenedEvent appReOpenedEvent) {
        // No-op
    }

    @Override
    public void openFiles(AppEvent.OpenFilesEvent openFilesEvent) {
        // Wait until the application has been launched. This step is required to properly handle the case where the
        // application is launched with a file to open, for instance when drag-n-dropping a file to the Dock icon
        // when muCommander is not started yet. In this case, this method is called while Launcher is still busy
        // launching the application (no mainframe exists yet).
        Launcher.waitUntilLaunched();
        for (File f : openFilesEvent.getFiles()) {
            AbstractFile file = FileFactory.getFile(f.toString());
            FolderPanel activePanel = WindowManager.getCurrentMainFrame().getActivePanel();
            if(file.isBrowsable())
                activePanel.tryChangeCurrentFolder(file);
            else
                activePanel.tryChangeCurrentFolder(file.getParent(), file, false);
        }

    }

    @Override
    public void handlePreferences(AppEvent.PreferencesEvent preferencesEvent) {
        OSXIntegration.showPreferences();
    }

    @Override
    public void printFiles(AppEvent.PrintFilesEvent printFilesEvent) {
        // No-op
    }

    @Override
    public void handleQuitRequestWith(AppEvent.QuitEvent quitEvent, QuitResponse quitResponse) {
        // Accept or reject the request to quit based on user's response
        if (OSXIntegration.doQuit()) {
            quitResponse.performQuit();
        } else {
            quitResponse.cancelQuit();
        }
    }
}