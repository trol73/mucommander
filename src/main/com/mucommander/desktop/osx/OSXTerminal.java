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

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.ui.macosx.AppleScript;

public class OSXTerminal {



    public static boolean addNewTabWithCommands(AbstractFile currentFolder, String ...commands) {
        // do script "cd " & dir & ";clear" in front window
        String scriptStart = "on run argv\n" +
        "tell application \"Terminal\"\n" +
        "if not (exists window 1) then reopen\n" +
        "activate\n" +
        "set window_id to id of first window whose frontmost is true\n" +
                // repeat with i from 1 to 2
        "tell application \"System Events\"\n" +
            "keystroke \"t\" using {command down}\n" +
        "end tell\n" +
        "delay 0.5\n" +

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
        return AppleScript.execute(script.toString(), null, currentFolder);
    }

    public static boolean openNewWindowAndRun(AbstractFile currentFolder, String ...commands) {
        StringBuilder script = new StringBuilder();
        script.append("tell application \"Terminal\"\n");
        script.append("activate\n");
        script.append("do script \"").append("cd '").append(currentFolder.getAbsolutePath()).append("'").append("\"\n");
        for (String cmd : commands) {
            script.append("do script \"").append(cmd).append("\"\n");
        }
        script.append("end tell\n");
System.out.println(script);
        return AppleScript.execute(script.toString(), null);
    }
}
