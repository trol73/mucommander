package com.mucommander.ui.macosx;


import com.mucommander.TrolCommander;
import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.FileFactory;
import com.mucommander.ui.main.FolderPanel;
import com.mucommander.ui.main.WindowManager;

import java.awt.*;
import java.awt.desktop.*;
import java.io.File;

class Java9DesktopHandler implements AboutHandler, PreferencesHandler, QuitHandler, OpenFilesHandler {
    Java9DesktopHandler() {
        Desktop desktop = Desktop.getDesktop();
        desktop.setAboutHandler(this);
        desktop.setPreferencesHandler(this);
    }


    @Override
    public void handleAbout(AboutEvent e) {
        OSXIntegration.showAbout();
    }

    @Override
    public void handlePreferences(PreferencesEvent e) {
        OSXIntegration.showPreferences();
    }

    @Override
    public void handleQuitRequestWith(QuitEvent e, QuitResponse response) {
        // Accept or reject the request to quit based on user's response
        if (OSXIntegration.doQuit()) {
            response.performQuit();
        } else {
            response.cancelQuit();
        }
    }

    @Override
    public void openFiles(OpenFilesEvent e) {
        TrolCommander.waitUntilLaunched();
        for (File f : e.getFiles()) {
            AbstractFile file = FileFactory.getFile(f.toString());
            if (file == null) {
                continue;
            }
            FolderPanel activePanel = WindowManager.getCurrentMainFrame().getActivePanel();
            if(file.isBrowsable()) {
                activePanel.tryChangeCurrentFolder(file);
            } else {
                activePanel.tryChangeCurrentFolder(file.getParent(), file, false);
            }
        }
    }
}
