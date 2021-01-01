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

package com.mucommander.ui.main.frame;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.net.MalformedURLException;

import com.mucommander.commons.conf.Configuration;
import com.mucommander.commons.file.FileURL;
import com.mucommander.conf.TcConfigurations;
import com.mucommander.conf.TcPreference;
import com.mucommander.conf.TcPreferences;
import com.mucommander.conf.TcSnapshot;
import com.mucommander.ui.main.FolderPanel.FolderPanelType;
import com.mucommander.ui.main.MainFrame;
import com.mucommander.ui.main.tabs.ConfFileTableTab;

/**
 * 
 * @author Arik Hadas
 */
public class DefaultMainFramesBuilder extends MainFrameBuilder {
	//private static final Logger LOGGER = LoggerFactory.getLogger(DefaultMainFramesBuilder.class);
	
	private final Configuration snapshot = TcConfigurations.getSnapshot();
	
	public DefaultMainFramesBuilder() { }

	@Override
	public int getSelectedFrame() {
		return Math.max(snapshot.getIntegerVariable(TcSnapshot.getSelectedWindow()), 0);
	}

	@Override
	public MainFrame[] build() {
		int nbFrames = snapshot.getIntegerVariable(TcSnapshot.getWindowsCount());

        // if there is no window saved in the snapshot file or custom folders are set, open one window with default settings
		if (nbFrames == 0 || isCustomStartupFolders()) {
			MainFrame mainFrame = new MainFrame(
					new ConfFileTableTab(getInitialPath(FolderPanelType.LEFT)),
					getFileTableConfiguration(FolderPanelType.LEFT, -1),
					new ConfFileTableTab(getInitialPath(FolderPanelType.RIGHT)),
					getFileTableConfiguration(FolderPanelType.RIGHT, -1));
			
			Dimension screenSize   = Toolkit.getDefaultToolkit().getScreenSize();
	        // Full screen bounds are not reliable enough, in particular under Linux+Gnome
	        // so we simply make the initial window 4/5 of screen's size, and center it.
	        // This should fit under any window manager / platform
	        int x      = screenSize.width / 10;
	        int y      = screenSize.height / 10;
	        int width  = (int)(screenSize.width * 0.8);
	        int height = (int)(screenSize.height * 0.8);

	        mainFrame.setBounds(new Rectangle(x, y, width, height));

	        return new MainFrame[] {mainFrame};
		}
		else {
			MainFrame[] mainFrames = new MainFrame[nbFrames];
			for (int i = 0; i < mainFrames.length; ++i) {
				mainFrames[i] = createMainFrame(i);
			}

			return mainFrames;
		}
	}

	private boolean isCustomStartupFolders() {
		return TcConfigurations.getPreferences().getVariable(TcPreference.STARTUP_FOLDERS).equals(TcPreferences.STARTUP_FOLDERS_CUSTOM);
	}

	private MainFrame createMainFrame(int index) {
		int nbTabsInLeftPanel = snapshot.getIntegerVariable(TcSnapshot.getTabsCountVariable(index, true));
		ConfFileTableTab[] leftTabs = new ConfFileTableTab[nbTabsInLeftPanel];
		for (int i = 0; i < nbTabsInLeftPanel; ++i) {
			leftTabs[i] = new ConfFileTableTab(
					snapshot.getBooleanVariable(TcSnapshot.getTabLockedVariable(index, true, i)),
					restoreFileURL(snapshot.getVariable(TcSnapshot.getTabLocationVariable(index, true, i))),
					snapshot.getVariable(TcSnapshot.getTabTitleVariable(index, true, i)));
		}

		int nbTabsInRightPanel = snapshot.getIntegerVariable(TcSnapshot.getTabsCountVariable(index, false));
		ConfFileTableTab[] rightTabs = new ConfFileTableTab[nbTabsInRightPanel];
		for (int i = 0; i < nbTabsInRightPanel; ++i) {
            rightTabs[i] = new ConfFileTableTab(
                    snapshot.getBooleanVariable(TcSnapshot.getTabLockedVariable(index, false, i)),
                    restoreFileURL(snapshot.getVariable(TcSnapshot.getTabLocationVariable(index, false, i))),
                    snapshot.getVariable(TcSnapshot.getTabTitleVariable(index, false, i)));
        }

		MainFrame mainFrame = new MainFrame(
				leftTabs,
				getInitialSelectedTab(FolderPanelType.LEFT, index),
				getFileTableConfiguration(FolderPanelType.LEFT, index),
				rightTabs,
				getInitialSelectedTab(FolderPanelType.RIGHT, index),
				getFileTableConfiguration(FolderPanelType.RIGHT, index));

		// Retrieve last saved window bounds
		Dimension screenSize   = Toolkit.getDefaultToolkit().getScreenSize();
        int x      = TcConfigurations.getSnapshot().getIntegerVariable(TcSnapshot.getX(index));
        int y      = TcConfigurations.getSnapshot().getIntegerVariable(TcSnapshot.getY(index));
        int width  = TcConfigurations.getSnapshot().getIntegerVariable(TcSnapshot.getWidth(index));
        int height = TcConfigurations.getSnapshot().getIntegerVariable(TcSnapshot.getHeight(index));

        // Retrieves the last known size of the screen.
        int lastScreenWidth  = TcConfigurations.getSnapshot().getIntegerVariable(TcSnapshot.SCREEN_WIDTH);
        int lastScreenHeight = TcConfigurations.getSnapshot().getIntegerVariable(TcSnapshot.SCREEN_HEIGHT);

        // If no previous location was saved, or if the resolution has changed,
        // reset the window's dimensions to their default values.
        if (x == -1 || y == -1 || width == -1 || height == -1 ||
           screenSize.width != lastScreenWidth ||  screenSize.height != lastScreenHeight
           || width + x > screenSize.width + 5 || height + y > screenSize.height + 5) {

            // Full screen bounds are not reliable enough, in particular under Linux+Gnome
            // so we simply make the initial window 4/5 of screen's size, and center it.
            // This should fit under any window manager / platform
            x      = screenSize.width / 10;
            y      = screenSize.height / 10;
            width  = (int)(screenSize.width * 0.8);
            height = (int)(screenSize.height * 0.8);
        }

        mainFrame.setBounds(new Rectangle(x, y, width, height));
        
        return mainFrame;
	}
	
    private int getInitialSelectedTab(FolderPanelType folderPanelType, int window) {
    	// Checks which kind of initial path we're dealing with.
    	boolean isCustom = TcConfigurations.getPreferences().getVariable(TcPreference.STARTUP_FOLDERS, TcPreferences.DEFAULT_STARTUP_FOLDERS).equals(TcPreferences.STARTUP_FOLDERS_CUSTOM);
    	
    	return isCustom ? 
    		0 :
    		TcConfigurations.getSnapshot().getIntegerVariable(TcSnapshot.getTabsSelectionVariable(window, folderPanelType == FolderPanelType.LEFT));
    }
    
    private FileURL restoreFileURL(String url) {
    	try {
			return FileURL.getFileURL(url);
		} catch (MalformedURLException e) {
			return getHomeFolder().getURL();
		}
    }
}
