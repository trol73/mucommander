/*
 * This file is part of trolCommander, http://www.trolsoft.ru/en/soft/trolcommander
 * Copyright (C) 2013-2026 Oleg Trifonov
 *
 * trolCommander is free software; you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 *
 * trolCommander is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.mucommander.ui.terminal

import com.jediterm.terminal.ProcessTtyConnector
import com.mucommander.conf.TcConfigurations
import com.mucommander.conf.TcPreference
import com.mucommander.conf.TcPreferences
import com.mucommander.desktop.DesktopManager
import com.pty4j.PtyProcess
import com.pty4j.PtyProcessBuilder
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.util.*

/**
 * @author Oleg Trifonov
 * Created on 28/10/14.
 */
open class TcTerminalTtyConnector private constructor(process: PtyProcess) :
    ProcessTtyConnector(process, StandardCharsets.UTF_8) {
    private val myDataChunks: MutableList<CharArray?> = ArrayList<CharArray?>()

    internal constructor(directory: String?) : this(createPtyProcess(directory))

    override fun getName(): String = ""

    @Throws(IOException::class)
    override fun read(buf: CharArray, offset: Int, length: Int): Int {
        val len = super.read(buf, offset, length)
        if (len > 0) {
            val arr = buf.copyOfRange(offset, len)
            myDataChunks.add(arr)
        }
        return len
    }

    fun getChunks(): MutableList<CharArray> =
        ArrayList<CharArray>(myDataChunks)


    companion object {
        @Throws(IOException::class)
        private fun createPtyProcess(directory: String?): PtyProcess {
            val envs: MutableMap<String?, String?> = HashMap(System.getenv()).apply {
                put("TERM", "xterm-256color")
            }

            val pref = TcConfigurations.getPreferences()
            var cmd = if (pref.getVariable(TcPreference.TERMINAL_USE_CUSTOM_SHELL,TcPreferences.DEFAULT_TERMINAL_USE_CUSTOM_SHELL)) {
                pref.getVariable(TcPreference.TERMINAL_SHELL)
            } else {
                DesktopManager.getDefaultTerminalShellCommand()
            }

            cmd = cmd.replace("\t", " ").replace(" +".toRegex(), " ")
            val command: Array<String> = cmd.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

            return PtyProcessBuilder()
                .setCommand(command)
                .setEnvironment(envs)
                .setDirectory(directory)
                .setRedirectErrorStream(true)
                .setWindowsAnsiColorEnabled(true)
                .setUnixOpenTtyToPreserveOutputAfterTermination(true)
                .setSpawnProcessUsingJdkOnMacIntel(true)
                .setConsole(false) // Windows only ?
                .start()
        }
    }
}

