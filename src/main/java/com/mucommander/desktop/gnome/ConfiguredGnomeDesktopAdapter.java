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

import com.mucommander.process.ProcessRunner;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Nicolas Rinaudo
 */
@Slf4j
public class ConfiguredGnomeDesktopAdapter extends GnomeDesktopAdapter {

    public String toString() {
        return "Gnome Desktop";
    }

    @Override
    public boolean isAvailable() {
        String desktopSession = System.getenv("DESKTOP_SESSION");
        if ("gnome".equalsIgnoreCase(desktopSession)) {
            return true;
        }

        desktopSession = System.getenv("XDG_CURRENT_DESKTOP");
        if (desktopSession != null) {
            desktopSession = desktopSession.toLowerCase();
            if (desktopSession.contains("gnome"))
                return true;
            if (desktopSession.contains("unity"))
                return true;
        }

        desktopSession = System.getenv("GNOME_DESKTOP_SESSION_ID");
        return desktopSession != null && !desktopSession.trim().isEmpty();
    }

    @Override
    protected String getFileOpenerCommand() {
        try {
            ProcessRunner.execute(GVFS_OPEN);
            return GVFS_OPEN;
        } catch(Exception ignore) {
            log.debug(GVFS_OPEN + " not found");
        }
        try {
            ProcessRunner.execute(GNOME_OPEN);
            return GNOME_OPEN;
        } catch(Exception ignore) {
            log.debug(GNOME_OPEN + " not found");
        }
        try {
            ProcessRunner.execute(XDG_OPEN);
            return XDG_OPEN;
        } catch(Exception ignore) {
            log.debug(XDG_OPEN + " not found");
        }

        return null;
    }

}
