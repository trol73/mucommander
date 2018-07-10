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

package com.mucommander.shell;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.WeakHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mucommander.PlatformManager;
import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.FileFactory;
import com.mucommander.conf.MuConfigurations;
import com.mucommander.conf.MuPreference;
import com.mucommander.conf.MuPreferences;
import com.mucommander.io.backup.BackupInputStream;
import com.mucommander.io.backup.BackupOutputStream;

/**
 * Used to manage shell HISTORY.
 * <p>
 * Using this class is fairly basic: you can add elements to the shell HISTORY through
 * {@link #add(String)} and browse it through {@link #getHistoryIterator()}.
 *
 * @author Nicolas Rinaudo
 */
public class ShellHistoryManager {
	private static Logger logger;
	
    // - History configuration -----------------------------------------------
    // -----------------------------------------------------------------------
    /** File in which to store the shell HISTORY. */
    private static final String DEFAULT_HISTORY_FILE_NAME = "shell_history.xml";



    // - Class fields ---------------------------------------------------------------
    // ------------------------------------------------------------------------------
    /** List of shell HISTORY registered LISTENERS. */
    private static final WeakHashMap<ShellHistoryListener, ?> LISTENERS;
    /** Stores the shell HISTORY. */
    private static final String[] HISTORY;
    /** Index of the first element of the HISTORY. */
    private static int historyStart;
    /** Index of the last element of the HISTORY. */
    private static int historyEnd;
    /** Path to the HISTORY file. */
    private static AbstractFile historyFile;



    // - Initialisation -------------------------------------------------------------
    // ------------------------------------------------------------------------------
    /**
     * Prevents instantiations of the class.
     */
    private ShellHistoryManager() {}

    static {
        HISTORY = new String[MuConfigurations.getPreferences().getVariable(MuPreference.SHELL_HISTORY_SIZE, MuPreferences.DEFAULT_SHELL_HISTORY_SIZE)];
        LISTENERS = new WeakHashMap<>();
    }



    // - Listener code --------------------------------------------------------------
    // ------------------------------------------------------------------------------
    /**
     * Registers a listener to changes in the shell HISTORY.
     * @param listener listener to register.
     */
    public static void addListener(ShellHistoryListener listener) {
        LISTENERS.put(listener, null);}

    /**
     * Propagates shell HISTORY events to all registered LISTENERS.
     * @param command command that was added to the shell HISTORY.
     */
    private static void triggerEvent(String command) {
        for (ShellHistoryListener listener : LISTENERS.keySet()) {
            listener.historyChanged(command);
        }
    }



    // - History access -------------------------------------------------------------
    // ------------------------------------------------------------------------------
    /**
     * Completely empties the shell HISTORY.
     */
    public static void clear() {
        // Empties HISTORY.
        historyStart = 0;
        historyEnd   = 0;

        // Notifies LISTENERS.
        for (ShellHistoryListener listener : LISTENERS.keySet()) {
            listener.historyCleared();
        }
    }

    /**
     * Returns a <b>non thread-safe</b> iterator on the HISTORY.
     * @return an iterator on the HISTORY.
     */
    public static Iterator<String> getHistoryIterator() {return new HistoryIterator();}

    /**
     * Adds the specified command to shell HISTORY.
     * @param command command to add to the shell HISTORY.
     */
    public static void add(String command) {
        // Ignores empty commands.
        if (command.trim().isEmpty()) {
            return;
        }
        // Ignores the command if it's the same as the last one.
        // There is no last command if HISTORY is empty.
        if (historyEnd != historyStart) {
            // Computes the index of the previous command.
            int lastIndex = historyEnd == 0 ? HISTORY.length - 1 : historyEnd - 1;
            if (command.equals(HISTORY[lastIndex])) {
                return;
            }
        }

        getLogger().debug("Adding  " + command + " to shell HISTORY.");

        // Updates the HISTORY buffer.
        HISTORY[historyEnd] = command;
        historyEnd++;

        // Wraps around the HISTORY buffer.
        if (historyEnd == HISTORY.length) {
            historyEnd = 0;
        }

        // Clears items from the beginning of the buffer if necessary.
        if (historyEnd == historyStart) {
            if (++historyStart == HISTORY.length) {
                historyStart = 0;
            }
        }

        // Propagates the event.
        triggerEvent(command);
    }



    // - History saving / loading ---------------------------------------------------
    // ------------------------------------------------------------------------------
    /**
     * Sets the path of the shell HISTORY file.
     * @param     path                  where to load the shell HISTORY from.
     * @exception FileNotFoundException if <code>path</code> is not accessible.
     * @see                             #getHistoryFile()
     * @see                             #setHistoryFile(File)
     * @see                             #setHistoryFile(AbstractFile)
     */
    public static void setHistoryFile(String path) throws FileNotFoundException {
        AbstractFile file = FileFactory.getFile(path);
        if (file == null) {
            setHistoryFile(new File(path));
        } else {
            setHistoryFile(file);
        }
    }

    /**
     * Sets the path of the shell HISTORY file.
     * @param     file                  where to load the shell HISTORY from.
     * @exception FileNotFoundException if <code>path</code> is not accessible.
     * @see                             #getHistoryFile()
     * @see                             #setHistoryFile(AbstractFile)
     * @see                             #setHistoryFile(String)
     */
    public static void setHistoryFile(File file) throws FileNotFoundException {
        setHistoryFile(FileFactory.getFile(file != null ? file.getAbsolutePath() : null));
    }

    /**
     * Sets the path of the shell HISTORY file.
     * @param     file                  where to load the shell HISTORY from.
     * @exception FileNotFoundException if <code>path</code> is not accessible.
     * @see                             #getHistoryFile()
     * @see                             #setHistoryFile(File)
     * @see                             #setHistoryFile(String)
     */
    public static void setHistoryFile(AbstractFile file) throws FileNotFoundException {
        // Makes sure file can be used as a shell HISTORY file.
        if (file.isBrowsable()) {
            throw new FileNotFoundException("Not a valid file: " + file.getAbsolutePath());
        }
        historyFile = file;
    }

    /**
     * Returns the path to the shell HISTORY file.
     * <p>
     * This method cannot guarantee the file's existence, and it's up to the caller
     * to deal with the fact that the user might not actually have created a HISTORY file yet.
     * <p>
     * This method's return value can be modified through {@link #setHistoryFile(String)}.
     * If this wasn't called, the default path will be used: {@link #DEFAULT_HISTORY_FILE_NAME}
     * in the {@link com.mucommander.PlatformManager#getPreferencesFolder() preferences} folder.
     *
     * @return             the path to the shell HISTORY file.
     * @throws IOException if an error occurred while locating the default shell HISTORY file.
     * @see                #setHistoryFile(File)
     * @see                #setHistoryFile(String)
     * @see                #setHistoryFile(AbstractFile)
     */
    public static AbstractFile getHistoryFile() throws IOException {
        if (historyFile == null) {
            return PlatformManager.getPreferencesFolder().getChild(DEFAULT_HISTORY_FILE_NAME);
        }
        return historyFile;
    }

    /**
     * Writes the shell HISTORY to hard drive.
     * @throws IOException if an I/O error occurs.
     */
    public static void writeHistory() throws IOException {
        try (BackupOutputStream out = new BackupOutputStream(getHistoryFile())) {
            ShellHistoryWriter.write(out);
        }
    }

    /**
     * Loads the shell HISTORY.
     * @throws Exception if an error occurs.
     */
    public static void loadHistory() throws Exception {
        try (BackupInputStream in = new BackupInputStream(getHistoryFile())) {
            ShellHistoryReader.read(in);
        }
    }

    private static Logger getLogger() {
        if (logger == null) {
            logger = LoggerFactory.getLogger(ShellHistoryManager.class);
        }
        return logger;
    }


    /**
     * Iterator used to browse HISTORY.
     * @author Nicolas Rinaudo
     */
    static class HistoryIterator implements Iterator<String> {
        /** Index in the HISTORY. */
        private int index;

        /**
         * Creates a new HISTORY iterator.
         */
        HistoryIterator() {
            index = ShellHistoryManager.historyStart;
        }

        /**
         * Returns <code>true</code> if there are more elements to iterate through.
         * @return <code>true</code> if there are more elements to iterate through, <code>false</code> otherwise.
         */
        public boolean hasNext() {
            return index != ShellHistoryManager.historyEnd;
        }

        /**
         * Returns the next element in the HISTORY.
         * @return the next element in the HISTORY.
         */
        public String next() throws NoSuchElementException {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }

            String value = ShellHistoryManager.HISTORY[index];
            if (++index == ShellHistoryManager.HISTORY.length) {
                index = 0;
            }
            return value;
        }

        /**
         * Operation not supported.
         */
        public void remove() throws UnsupportedOperationException {
            throw new UnsupportedOperationException();
        }

    }

}
