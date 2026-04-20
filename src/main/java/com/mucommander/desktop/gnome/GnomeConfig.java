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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;

/**
 * Provides access to the GNOME configuration, using the <code>gconftool</code> command.
 *
 * @author Maxence Bernard
 */
@Slf4j
public class GnomeConfig {
    /** Name of the command to invoke for retrieving configuration values */
    private static final String CONFIG_COMMAND = "gconftool";
    /** Timeout for the configuration command execution in seconds */
    private static final long COMMAND_TIMEOUT = 5;

    /**
     * Returns the GNOME configuration value corresponding to the given key, <code>null</code> if this key has no value.
     *
     * @param key key to the configuration value to retrieve.
     * @return the configuration value corresponding to the given key, <code>null</code> if this key has no value.
     * @throws IOException if an error occurred while invoking the <code>gconftool</code> command, for instance if the
     * command isn't available in the path.
     */
    public static String getValue(String key) throws IOException {
        ProcessBuilder processBuilder = new ProcessBuilder(CONFIG_COMMAND, "-g", key);
        processBuilder.redirectErrorStream(true);
        try {
            Process process = processBuilder.start();

            try (BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line = br.readLine();

                // Wait for process completion with timeout
                if (!process.waitFor(COMMAND_TIMEOUT, TimeUnit.SECONDS)) {
                    process.destroyForcibly();
                    log.warn("Command timed out for key: {}", key);
                    throw new IOException("Command timed out: " + CONFIG_COMMAND);
                }

                int exitCode = process.exitValue();
                if (exitCode != 0) {
                    log.debug("Command returned exit code {} for key: {}", exitCode, key);
                    return null;
                }

                log.debug(CONFIG_COMMAND + " returned '{}' for {}", line, key);
                if (line == null || (line=line.trim()).isEmpty() || line.startsWith("No value set for")) {
                    return null;
                }
                return line;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.debug("Interrupted while retrieving value for {}", key, e);
            throw new IOException("Command interrupted", e);
        } catch(IOException e) {
            log.debug("Error while retrieving value for {}", key, e);
            throw e;
        }
    }
}
