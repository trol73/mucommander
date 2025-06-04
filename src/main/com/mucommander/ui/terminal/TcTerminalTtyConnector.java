/*
 * This file is part of trolCommander, http://www.trolsoft.ru/en/soft/trolcommander
 * Copyright (C) 2013-2020 Oleg Trifonov
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
package com.mucommander.ui.terminal;

import com.jediterm.terminal.ProcessTtyConnector;
import com.mucommander.conf.TcConfigurations;
import com.mucommander.conf.TcPreference;
import com.mucommander.conf.TcPreferences;
import com.mucommander.conf.TcPreferencesAPI;
import com.mucommander.desktop.DesktopManager;
import com.pty4j.PtyProcess;
import com.pty4j.PtyProcessBuilder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * @author Oleg Trifonov
 * Created on 28/10/14.
 */
public class TcTerminalTtyConnector extends ProcessTtyConnector {
    private final List<char[]> myDataChunks = new ArrayList<>();


    TcTerminalTtyConnector(String directory) throws IOException {
        this(createPtyProcess(directory));
    }


    private TcTerminalTtyConnector(PtyProcess process) {
        super(process, StandardCharsets.UTF_8);
    }

    @Override
    public String getName() {
        return "";
    }

    @Override
    public int read(char[] buf, int offset, int length) throws IOException {
        int len = super.read(buf, offset, length);
        if (len > 0) {
            char[] arr = Arrays.copyOfRange(buf, offset, len);
            myDataChunks.add(arr);
        }
        return len;
    }

    public List<char[]> getChunks() {
        return new ArrayList<>(myDataChunks);
    }


    private static PtyProcess createPtyProcess(String directory) throws IOException {
        Map<String, String> envs = new HashMap<>(System.getenv());
        envs.put("TERM", "xterm-256color");

        TcPreferencesAPI pref = TcConfigurations.getPreferences();
        String cmd;
        if (pref.getVariable(TcPreference.TERMINAL_USE_CUSTOM_SHELL, TcPreferences.DEFAULT_TERMINAL_USE_CUSTOM_SHELL)) {
            cmd = pref.getVariable(TcPreference.TERMINAL_SHELL);
        } else {
            cmd = DesktopManager.getDefaultTerminalShellCommand();
        }

        cmd = cmd.replaceAll("\t", " ").replaceAll(" +", " ");
        String[] command = cmd.split(" ");

        return new PtyProcessBuilder()
                .setCommand(command)
                .setEnvironment(envs)
                .setDirectory(directory)
                .setRedirectErrorStream(true)
                .setWindowsAnsiColorEnabled(true)
                .setUnixOpenTtyToPreserveOutputAfterTermination(true)
                .setSpawnProcessUsingJdkOnMacIntel(true)
                .setConsole(false)   // Windows only ?
                .start();
    }

}

