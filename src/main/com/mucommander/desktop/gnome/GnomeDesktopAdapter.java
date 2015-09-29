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

package com.mucommander.desktop.gnome;

import java.awt.Toolkit;
import java.lang.reflect.Field;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mucommander.command.Command;
import com.mucommander.command.CommandException;
import com.mucommander.command.CommandManager;
import com.mucommander.command.CommandType;
import com.mucommander.commons.file.filter.FileFilter;
import com.mucommander.commons.file.filter.RegexpFilenameFilter;
import com.mucommander.desktop.DefaultDesktopAdapter;
import com.mucommander.desktop.DesktopInitialisationException;
import com.mucommander.desktop.DesktopManager;

/**
 * @author Nicolas Rinaudo, Maxence Bernard
 */
abstract class GnomeDesktopAdapter extends DefaultDesktopAdapter {
	private static final Logger LOGGER = LoggerFactory.getLogger(GnomeDesktopAdapter.class);
	private static final String FILE_MANAGER_NAME = "Gnome";
	private static final String FILE_OPENER = "xdg-open $f";
    private static final String EXE_OPENER        = "$f";
	/** Key to the double-click interval value in the GNOME configuration. */
	private static final String DOUBLE_CLICK_CONFIG_KEY = "/desktop/gnome/peripherals/mouse/double_click";
	/**
	 * Multi-click interval, cached to avoid polling the value every time
	 * {@link #getMultiClickInterval()} is called.
	 */
    private int multiClickInterval;

    @Override
    public abstract boolean isAvailable();

    @Override
	public void init(final boolean install) throws DesktopInitialisationException {
        // Workaround for JDK issue
        try {
    	    Toolkit xToolkit = Toolkit.getDefaultToolkit();
          Field awtAppClassNameField = xToolkit.getClass().getDeclaredField("awtAppClassName");
          awtAppClassNameField.setAccessible(true);
          awtAppClassNameField.set(xToolkit, "trolCommander");
        } catch (Exception ge) {
        	// Just ignore
			/* Ignore */
        }
        // Initialises trash management.
        DesktopManager.setTrashProvider(new GnomeTrashProvider());
        try {
			CommandManager.registerDefaultCommand(new Command(CommandManager.FILE_OPENER_ALIAS, FILE_OPENER,
					CommandType.SYSTEM_COMMAND, null, null));
			CommandManager.registerDefaultCommand(new Command(CommandManager.URL_OPENER_ALIAS, FILE_OPENER,
					CommandType.SYSTEM_COMMAND, null, null));
			CommandManager.registerDefaultCommand(new Command(CommandManager.EXE_OPENER_ALIAS, EXE_OPENER,
					CommandType.SYSTEM_COMMAND, null, null));
			CommandManager.registerDefaultCommand(new Command(CommandManager.FILE_MANAGER_ALIAS, FILE_OPENER,
					CommandType.SYSTEM_COMMAND, FILE_MANAGER_NAME, null));
            FileFilter filter = new RegexpFilenameFilter("[^.]+", true);
            // Disabled actual permissions checking as this will break normal +x files.
            // With this, a +x PDF file will not be opened.
            /*
			 * // Identifies which kind of IMAGE_FILTER should be used to match executable files.
			 * if(JavaVersion.JAVA_1_6.isCurrentOrHigher()) IMAGE_FILTER = new
			 * PermissionsFileFilter(PermissionTypes.EXECUTE_PERMISSION, true); else
            */
            CommandManager.registerDefaultAssociation(CommandManager.EXE_OPENER_ALIAS, filter);
            // Multi-click interval retrieval
            try {
                String value = GnomeConfig.getValue(DOUBLE_CLICK_CONFIG_KEY);
				if (value == null) {
                    multiClickInterval = super.getMultiClickInterval();
				}
                multiClickInterval = Integer.parseInt(value);
			} catch (Exception e) {
            	LOGGER.debug("Error while retrieving double-click interval from gconftool", e);
                multiClickInterval = super.getMultiClickInterval();
            }
		} catch (CommandException e) {
        }
        catch (CommandException e) {
            throw new DesktopInitialisationException(e);
        }
    }

    /**
	 * Returns the <code>/desktop/gnome/peripherals/mouse/double_click</code> GNOME configuration
	 * value. If the returned value is not defined or could not be retrieved, the value of
     * {@link DefaultDesktopAdapter#getMultiClickInterval()} is returned.<br/>
     * The value is retrieved on initialization and never updated thereafter.
     * <p>
	 * Note under Java 1.6 or below, the returned value does not match the one used by Java for
	 * generating multi-clicks (see {@link DefaultDesktopAdapter#getMultiClickInterval()}, as
	 * Java uses the multi-click speed declared in X Window's configuration, not in GNOME's. See
	 * <a href="http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=5076635"> Java Bug 5076635</a>
	 * for more information.
     * </p>
	 * 
	 * @return the <code>/desktop/gnome/peripherals/mouse/double_click</code> GNOME configuration
	 *         value.
     */
    @Override
    public int getMultiClickInterval() {
        return multiClickInterval;
    }
}
