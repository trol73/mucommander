/*
 * This file is part of trolCommander, http://www.trolsoft.ru/en/soft/trolcommander
 * Copyright (C) 2014-2026 Oleg Trifonov
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
package com.mucommander.desktop.macos

import com.mucommander.commons.file.AbstractFile
import com.mucommander.conf.TcConfigurations
import com.mucommander.conf.TcPreference
import com.mucommander.conf.TcPreferences
import com.mucommander.ui.macosx.AppleScript

object OSXTerminal {

    @JvmStatic
    fun addNewTabWithCommands(currentFolder: AbstractFile, vararg commands: String): Boolean =
        if (getTerminalType() == TerminalApp.ITERM2) {
            addNewITermTabWithCommands(currentFolder, *commands)
        } else {
            addNewTerminalTabWithCommands(currentFolder, *commands)
        }

    @JvmStatic
    fun openNewWindowAndRun(currentFolder: AbstractFile, vararg commands: String): Boolean =
        if (getTerminalType() == TerminalApp.ITERM2) {
            openNewITermWindowAndRun(currentFolder, *commands)
        } else {
            openNewTerminalWindowAndRun(currentFolder, *commands)
        }


    private fun addNewTerminalTabWithCommands(currentFolder: AbstractFile, vararg commands: String): Boolean {
        val dir = currentFolder.absolutePath.replace("'", "'\\''")

        val script = StringBuilder().apply {
            append("on run argv\n")
            append("tell application \"Terminal\"\n")
            append("if not (exists window 1) then reopen\n")
            append("activate\n")
            append("do script \"cd '").append(dir).append("' && clear\" in front window\n")
            append("delay 0.3\n")
            for (cmd in commands) {
                val escapedCmd = cmd.replace("\"", "\\\"")
                append("do script \"").append(escapedCmd).append("\" in front window\n")
                append("delay 0.3\n")
            }
            append("end tell\n")
            append("end run\n")
        }.toString()
        return AppleScript.execute(script, null, currentFolder)
    }

    private fun addNewITermTabWithCommands(currentFolder: AbstractFile, vararg commands: String): Boolean {
        val dir = currentFolder.absolutePath.replace("'", "'\\''")

        val script = StringBuilder().apply {
            append("on run argv\n")
            append("tell application \"iTerm2\"\n")
            append("activate\n")

            // Create tab or new window
            append("if (count of windows) > 0 then\n")
            append("    tell current window\n")
            append("        create tab with default profile\n")
            append("        tell current session\n")

            append("            write text \"cd '").append(dir).append("'\"\n")

            //        script.append("            write text \"clear\"\n");
            for (cmd in commands) {
                val escapedCmd = cmd.replace("\"", "\\\"").replace("\\", "\\\\")
                append("            write text \"").append(escapedCmd).append("\"\n")
                append("            delay 0.15\n")
            }

            append("        end tell\n")
            append("    end tell\n")
            append("else\n")
            // If window doesn't exist
            append("    create window with default profile\n")
            append("    tell current session of current window\n")
            append("        write text \"cd '").append(dir).append("'\"\n")
            //        script.append("        write text \"clear\"\n");
            for (cmd in commands) {
                val escapedCmd = cmd.replace("\"", "\\\"").replace("\\", "\\\\")
                append("        write text \"").append(escapedCmd).append("\"\n")
                append("        delay 0.15\n")
            }
            append("    end tell\n")
            append("end if\n")

            append("end tell\n")
            append("end run\n")
        }.toString()

        return AppleScript.execute(script, null, currentFolder)
    }


    private fun openNewTerminalWindowAndRun(currentFolder: AbstractFile, vararg commands: String): Boolean {
        val escapedPath = currentFolder.absolutePath
            .replace("\\", "\\\\")
            .replace("'", "'\\\\''")

        val script = StringBuilder().apply {
            append("tell application \"Terminal\"\n")
            append("activate\n")

            val cmdBuilder = StringBuilder()
            cmdBuilder.append("cd '").append(escapedPath).append("'")
            for (cmd in commands) {
                val escapedCmd = cmd.replace("\\", "\\\\").replace("'", "'\\\\''")
                cmdBuilder.append("; ").append(escapedCmd)
            }

            append("do script \"").append(cmdBuilder).append("\"\n")
            append("end tell\n")
        }.toString()

        return AppleScript.execute(script, null, currentFolder)
    }

    private fun openNewITermWindowAndRun(currentFolder: AbstractFile, vararg commands: String): Boolean {
        val dir = currentFolder.absolutePath.replace("'", "'\\''")

        val script = StringBuilder().apply {
            append("tell application \"iTerm2\"\n")
            append("activate\n")

            append("create window with default profile\n")
            append("tell current session of current window\n")

            append("    write text \"cd '").append(dir).append("'\"\n")

            //        script.append("    write text \"clear\"\n");
            for (cmd in commands) {
                val escapedCmd = cmd.replace("\"", "\\\"").replace("\\", "\\\\")
                append("    write text \"").append(escapedCmd).append("\"\n")
                append("    delay 0.15\n")
            }

            append("end tell\n")
            append("end tell\n")
        }.toString()
        return AppleScript.execute(script, null, currentFolder)
    }

    private fun getTerminalType(): TerminalApp {
        val term = TcConfigurations.getPreferences().getVariable(TcPreference.EXTERNAL_TERMINAL_TYPE, TcPreferences.DEFAULT_TERMINAL_TYPE)
        return if (term == TcPreferences.TERMINAL_ITERM && OSXApplications.iTermInstalled())
            TerminalApp.ITERM2 else TerminalApp.TERMINAL
    }

    internal enum class TerminalApp {
        TERMINAL, ITERM2
    }
}



