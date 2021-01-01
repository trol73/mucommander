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

import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mucommander.commons.conf.Configuration;
import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.FileFactory;
import com.mucommander.commons.file.FileURL;
import com.mucommander.conf.TcConfigurations;
import com.mucommander.conf.TcPreference;
import com.mucommander.conf.TcPreferences;
import com.mucommander.conf.TcPreferencesAPI;
import com.mucommander.conf.TcSnapshot;
import com.mucommander.ui.main.FolderPanel.FolderPanelType;
import com.mucommander.ui.main.MainFrame;
import com.mucommander.ui.main.table.Column;
import com.mucommander.ui.main.table.views.full.FileTableConfiguration;

/**
 * 
 * @author Arik Hadas
 */
public abstract class MainFrameBuilder {
	private static Logger logger;

	public abstract MainFrame[] build();
	
	public int getSelectedFrame() { return 0; }
	
	/**
     * Retrieves the user's initial path for the specified frame.
     * <p>
     * If the path found in preferences is either illegal or does not exist, this method will
     * return the user's home directory - we assume this will always exist, which might be a bit
     * of a leap of faith.
     *
     * @param  folderPanelType panel for which the initial path should be returned (either {@link com.mucommander.ui.main.FolderPanel.FolderPanelType#LEFT} or
     *               {@link com.mucommander.ui.main.FolderPanel.FolderPanelType#RIGHT}).
     * @return       the user's initial path for the specified frame.
     */ 
    protected AbstractFile[] getInitialPaths(FolderPanelType folderPanelType, int window) {
        boolean       isCustom;    // Whether the initial path is a custom one or the last used folder.
        String[]      folderPaths; // Paths to the initial folders.
        
        // Snapshot configuration
        Configuration snapshot = TcConfigurations.getSnapshot();
        // Preferences configuration
        TcPreferencesAPI preferences = TcConfigurations.getPreferences();
        
        // Checks which kind of initial path we're dealing with.
        isCustom = preferences.getVariable(TcPreference.STARTUP_FOLDERS, TcPreferences.DEFAULT_STARTUP_FOLDERS).equals(TcPreferences.STARTUP_FOLDERS_CUSTOM);

        // Handles custom initial paths.
        if (isCustom) {
        	folderPaths = new String[] {(folderPanelType == FolderPanelType.LEFT ? preferences.getVariable(TcPreference.LEFT_CUSTOM_FOLDER) :
        		preferences.getVariable(TcPreference.RIGHT_CUSTOM_FOLDER))};
        }
        // Handles "last folder" initial paths.
        else {
        	// Set initial path to each tab
        	int nbFolderPaths = snapshot.getIntegerVariable(TcSnapshot.getTabsCountVariable(window, folderPanelType == FolderPanelType.LEFT));
        	folderPaths = new String[nbFolderPaths];
        	for (int i=0; i<nbFolderPaths;++i) {
                folderPaths[i] = snapshot.getVariable(TcSnapshot.getTabLocationVariable(window, folderPanelType == FolderPanelType.LEFT, i));
            }
        }

        List<AbstractFile> initialFolders = new LinkedList<>(); // Initial folders
        AbstractFile folder;
        
        for (String folderPath : folderPaths) {
        	// TODO: consider whether to search for workable path in case the folder doesn't exist
        	if (folderPath != null && (folder = FileFactory.getFile(folderPath)) != null && folder.exists())
        		initialFolders.add(folder);
        }
        
        // If the initial path is not legal or does not exist, defaults to the user's home.
        AbstractFile[] results = initialFolders.size() == 0 ?
        		new AbstractFile[] {FileFactory.getFile(System.getProperty("user.home"))} :
                initialFolders.toArray(new AbstractFile[0]);

        getLogger().debug("initial folders:");
        for (AbstractFile result:results) {
            getLogger().debug("\t"+result);
        }
        
        return results;
    }

    /**
     * Retrieves the user's initial path for the specified frame.
     * <p>
     * If the path found in preferences is either illegal or does not exist, this method will
     * return the user's home directory - we assume this will always exist, which might be a bit
     * of a leap of faith.
     *
     * @param  folderPanelType panel for which the initial path should be returned (either {@link com.mucommander.ui.main.FolderPanel.FolderPanelType#LEFT} or
     *               {@link #@link com.mucommander.ui.main.FolderPanel.FolderPanelType.RIGHT}).
     * @return       the user's initial path for the specified frame.
     */
    FileURL getInitialPath(FolderPanelType folderPanelType) {
        // Preferences configuration
        TcPreferencesAPI preferences = TcConfigurations.getPreferences();
        
        // Checks which kind of initial path we're dealing with.
        boolean isCustom = preferences.getVariable(TcPreference.STARTUP_FOLDERS, TcPreferences.DEFAULT_STARTUP_FOLDERS).equals(TcPreferences.STARTUP_FOLDERS_CUSTOM);

        String customPath = null;
        // Handles custom initial paths.
        if (isCustom) {
        	customPath = (folderPanelType == FolderPanelType.LEFT ? 
        			preferences.getVariable(TcPreference.LEFT_CUSTOM_FOLDER)
        			: preferences.getVariable(TcPreference.RIGHT_CUSTOM_FOLDER));
        }

        AbstractFile result;
        if (customPath == null || (result = FileFactory.getFile(customPath)) == null || !result.exists()) {
            result = getHomeFolder();
        }

        getLogger().debug("initial folder: " + result);
        
        return result.getURL();
    }

    FileTableConfiguration getFileTableConfiguration(FolderPanelType folderPanelType, int window) {
        FileTableConfiguration conf = new FileTableConfiguration();

        // Loop on columns
        for (Column c  : Column.values()) {
            if (c != Column.NAME) {       // Skip the special name column (always visible, width automatically calculated)
            	// Sets the column's initial visibility.
            	conf.setEnabled(c,
            			TcConfigurations.getSnapshot().getVariable(
            					TcSnapshot.getShowColumnVariable(window, c, folderPanelType == FolderPanelType.LEFT),
            					c.showByDefault()
    					)
    			);

                // Sets the column's initial width.
                conf.setWidth(c, TcConfigurations.getSnapshot().getIntegerVariable(TcSnapshot.getColumnWidthVariable(window, c, folderPanelType == FolderPanelType.LEFT)));
            }

            // Sets the column's initial order
            conf.setPosition(c, TcConfigurations.getSnapshot().getVariable(
                                    TcSnapshot.getColumnPositionVariable(window, c, folderPanelType == FolderPanelType.LEFT),
                                    c.ordinal())
            );
        }

        return conf;
    }
    
    AbstractFile getHomeFolder() {
    	return FileFactory.getFile(System.getProperty("user.home"));
    }


    private static Logger getLogger() {
        if (logger == null) {
            logger = LoggerFactory.getLogger(MainFrameBuilder.class);
        }
        return logger;
    }

}
