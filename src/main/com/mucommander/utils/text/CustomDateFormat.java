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

package com.mucommander.utils.text;

import com.mucommander.commons.conf.ConfigurationEvent;
import com.mucommander.commons.conf.ConfigurationListener;
import com.mucommander.conf.TcConfigurations;
import com.mucommander.conf.TcPreference;
import com.mucommander.conf.TcPreferences;

import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * CustomDateFormat allows custom date formatting, according to the date format stored in the preferences.
 *
 * @author Maxence Bernard
 */
public class CustomDateFormat implements ConfigurationListener {

    /** Singleton instance */
    private static CustomDateFormat singleton;

    /** Custom SimpleDateFormat instance */
    private static SimpleDateFormat dateFormat;


    /**
     * Creates a new CustomDateFormat instance.
     */
    private CustomDateFormat() {}


    /**
     * Forces static fields to be initialized
     */
    public static void init() {
        // create a singleton instance and keep it as a static member of this class.
        // Not doing it so would cause the garbage collector to GC it as MuConfiguration holds
        // weak references of its listeners.
        singleton = new CustomDateFormat();
        TcConfigurations.addPreferencesListener(singleton);

        dateFormat = createDateFormat();
    }


    /**
     * Replace the default '/' separator in the given format string, by the given custom separator.
     *
     * @return the given format string with '/' separator characters replaced by the given separator character.
     */
    public static String replaceDateSeparator(String dateFormatString, String separator) {
        if (separator == null || separator.equals("/")) {
            return dateFormatString;
        } else {
        	  return dateFormatString.replace("/",separator);
        }
    }


    /**
     * Returns the date format stored in the preferences and used by this class to format dates.
     * The format of the returned string is the one used by the <code>java.text.SimpleDateFormat</code> class. 
     */
    public static String getDateFormatString() {
        return replaceDateSeparator(
        		TcConfigurations.getPreferences().getVariable(TcPreference.DATE_FORMAT, TcPreferences.DEFAULT_DATE_FORMAT),
        		TcConfigurations.getPreferences().getVariable(TcPreference.DATE_SEPARATOR, TcPreferences.DEFAULT_DATE_SEPARATOR))
        + " " + TcConfigurations.getPreferences().getVariable(TcPreference.TIME_FORMAT, TcPreferences.DEFAULT_TIME_FORMAT);
    }


    /**
     * Forces CustomDateFormat to update the date format by looking it up in the preferences.
     */
    public static synchronized void updateDateFormat() {
        dateFormat = createDateFormat();
    }


    /**
     * Creates and returns a SimpleDateFormat instance using the date format stored in the preferences.
     */
    private static SimpleDateFormat createDateFormat() {
        return new SimpleDateFormat(getDateFormatString());
    }
	
	
    /**
     * Formats the given with custom date format and returns a formatted date string. 
     *
     * @return a formatted string representing the given date.
     */
    private static synchronized String format(Date date) {
        // Calls to SimpleDateFormat MUST be synchronized otherwise it will start throwing exceptions (verified that!),
        // that is why this method is synchronized.
        // Quote from SimpleDateFormat's Javadoc: "Date formats are not synchronized. It is recommended to create
        // separate format instances for each thread. If multiple threads access a format concurrently, 
        // it must be synchronized externally."
        return dateFormat.format(date);
    }

    /**
     * Formats the given timestamp with custom date format and returns a formatted date string.
     *
     * @return a formatted string representing the given date.
     */
    public static String format(long date) {
        return format(new Date(date));
    }
	

    ///////////////////////////////////
    // ConfigurationListener methods //
    ///////////////////////////////////

    /**
     * Listens to some configuration variables.
     */
    public void configurationChanged(ConfigurationEvent event) {
        String var = event.getVariable();

        if (var.equals(TcPreferences.TIME_FORMAT) || var.equals(TcPreferences.DATE_FORMAT) || var.equals(TcPreferences.DATE_SEPARATOR))
            updateDateFormat();
    }
}
