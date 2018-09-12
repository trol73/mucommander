/*
 * This file is part of trolCommander, http://www.trolsoft.ru/en/soft/trolcommander
 * Copyright (C) 2014-2018 Oleg Trifonov
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
package com.mucommander.desktop.osx;

import com.mucommander.ui.macosx.AppleScript;

public class OSXTerminal {



    public static boolean addNewTabWithCommands(String ...commands) {
        String scriptStart = "on run argv\n" +
                //log "Item 1: " & item 1 of argv -- Folder path
        //"set folderName to item 1 of argv\n" +
        //"log \"Folder Name: \" & folderName\n" +
        "tell application \"Terminal\"\n" +
        "activate\n" +
        "set window_id to id of first window whose frontmost is true\n" +
                // repeat with i from 1 to 2
        "tell application \"System Events\"\n" +
            "keystroke \"t\" using {command down}\n" +
        "end tell\n" +
        "delay 0.2\n" +

        "set window_id to id of first window whose frontmost is true\n";

        String cmdStart = "do script \"";
        String cmdEnd = "\" in window id window_id of application \"Terminal\"\n";
        String scriptEnd = "end tell\n" +
            "end run\n";

        StringBuilder script = new StringBuilder().append(scriptStart);
        for (String cmd : commands) {
            script.append(cmdStart).append(cmd).append(cmdEnd);
        }
        script.append(scriptEnd);
        return AppleScript.execute(script.toString(), null);
    }
}
