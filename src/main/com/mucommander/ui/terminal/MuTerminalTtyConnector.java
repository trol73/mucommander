/*
 * This file is part of trolCommander, http://www.trolsoft.ru/soft/trolcommander
 * Copyright (C) 2013-2014 Oleg Trifonov
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
package com.mucommander.ui.terminal;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.jediterm.pty.PtyProcessTtyConnector;
import com.jediterm.terminal.LoggingTtyConnector;
import com.mucommander.commons.runtime.OsFamily;
import com.mucommander.desktop.DesktopManager;
import com.pty4j.PtyProcess;
import com.pty4j.unix.Pty;
import com.pty4j.unix.UnixPtyProcess;
import com.pty4j.util.PtyUtil;
import com.pty4j.windows.WinPtyProcess;
import com.sun.jna.Platform;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author Oleg Trifonov
 * Created on 28/10/14.
 */
public class MuTerminalTtyConnector extends PtyProcessTtyConnector implements LoggingTtyConnector {

    private final List<char[]> myDataChunks = Lists.newArrayList();
    private final PtyProcess process;



    public MuTerminalTtyConnector(String directory) throws IOException {
        this(createPtyProcess(directory));
    }


    public MuTerminalTtyConnector(PtyProcess process) throws IOException {
        super(process, Charset.forName("UTF-8"));
        this.process = process;
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
        return Lists.newArrayList(myDataChunks);
    }


    private static PtyProcess createPtyProcess(String directory) throws IOException {
        Map<String, String> envs = Maps.newHashMap(System.getenv());
        envs.put("TERM", "xterm-256color");
        String[] command;

        switch (OsFamily.getCurrent()) {
            case WINDOWS:
                command = new String[]{"cmd.exe"};
                break;
            default:
                command = new String[]{DesktopManager.getDefaultShellTerminal(), "--login"};
        }
        if (Platform.isWindows()) {
            return new WinPtyProcess(command, PtyUtil.toStringArray(envs), directory);
        }
        return new UnixPtyProcess(command, PtyUtil.toStringArray(envs), directory, new Pty(false));
//        return PtyProcess.exec(command, envs, null);
    }

    PtyProcess getPtyProcess() {
        return process;
    }

}

