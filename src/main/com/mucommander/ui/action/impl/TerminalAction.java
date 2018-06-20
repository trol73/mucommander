/*
 * This file is part of trolCommander, http://www.trolsoft.ru/en/soft/trolcommander
 * Copyright (C) 2013-2016 Oleg Trifonov
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
package com.mucommander.ui.action.impl;

import com.mucommander.commons.file.AbstractArchiveEntryFile;
import com.mucommander.commons.file.AbstractArchiveFile;
import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.FileProtocols;
import com.mucommander.commons.file.FileURL;
import com.mucommander.commons.runtime.OsFamily;
import com.mucommander.conf.MuConfigurations;
import com.mucommander.conf.MuPreference;
import com.mucommander.conf.MuPreferences;
import com.mucommander.process.ProcessRunner;
import com.mucommander.ui.action.AbstractActionDescriptor;
import com.mucommander.ui.action.ActionCategory;
import com.mucommander.ui.action.ActionDescriptor;
import com.mucommander.ui.action.MuAction;
import com.mucommander.ui.macosx.AppleScript;
import com.mucommander.ui.main.MainFrame;
import com.mucommander.ui.terminal.MuTerminal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.KeyStroke;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Oleg Trifonov
 * Created on 17/12/13.
 */
public class TerminalAction extends ParentFolderAction {

    /**
     * Logger reference
     */
    private static Logger logger;

    /**
     * Creates a new instance of <code>InternalViewAction</code>.
     *
     * @param mainFrame  frame to which the action is attached.
     * @param properties action's properties.
     */
    TerminalAction(MainFrame mainFrame, Map<String, Object> properties) {
        super(mainFrame, properties);
    }

    @Override
    public void performAction() {
        final AbstractFile currentFolder = mainFrame.getActiveTable().getFileTableModel().getCurrentFolder();
        String cmd = getConsoleCommand(currentFolder);
        try {
            ProcessRunner.execute(cmd, currentFolder);
        } catch (Exception e) {
            getLogger().error(e.getMessage(), e);
        }
    }

    public static String getDefaultTerminalCommand() {
        switch (OsFamily.getCurrent()) {
            case WINDOWS:
                return "cmd /c start cmd.exe /K \"cd /d $p\"";
            case LINUX:
                return "";
            case MAC_OS_X:
                return "open -a Terminal .";
        }
        return "";
    }

    private static String getConsoleCommand(AbstractFile folder) {
        String cmd;
        if (MuConfigurations.getPreferences().getVariable(MuPreference.USE_CUSTOM_EXTERNAL_TERMINAL, MuPreferences.DEFAULT_USE_CUSTOM_EXTERNAL_TERMINAL)) {
            cmd = MuConfigurations.getPreferences().getVariable(MuPreference.CUSTOM_EXTERNAL_TERMINAL);
        } else {
            final FileURL fileURL = folder.getURL();
            if (OsFamily.MAC_OS_X.isCurrent()) {
                try {
                    final File startScript = File.createTempFile("muCommanderTerminalStart", ".scpt");
                    startScript.deleteOnExit();
                    final List<String> script = new ArrayList<>();

                    script.add("tell application \"Terminal\"");
                    script.add("activate");
                    script.add("my makeTab()");

                    final String scheme = fileURL.getScheme();
                    String path = null;
                    if (folder.isArchive()) {
                        path = fileURL.getParent().getPath();
                    } else if (folder.hasAncestor(AbstractArchiveEntryFile.class)) {
                        final AbstractArchiveFile parentArchive = folder.getParentArchive();
                        if (parentArchive != null) {
                            path = parentArchive.getParent().getURL().getPath();
                        }
                    }
                    if (path == null) {
                        path = fileURL.getPath();
                    }
                    if (FileProtocols.FILE.equalsIgnoreCase(scheme)) {
                        script.add("do script \"cd " + path + "\" in tab 1 of front window");
                    } else if (FileProtocols.SFTP.equalsIgnoreCase(scheme)) {
                        script.add("do script \"ssh " + fileURL.getCredentials().getLogin() + "@" + fileURL.getHost() + " -t 'cd " + path + "; bash --login'\" in tab 1 of front window");
                    }

                    script.add("end tell");
                    script.add("on makeTab()");
                    script.add("tell application \"System Events\" to keystroke \"t\" using {command down}");
                    script.add("delay 0.2");
                    script.add("end makeTab");

                    Files.write(startScript.toPath(), script, Charset.forName(AppleScript.getScriptEncoding()), StandardOpenOption.CREATE);
                    cmd = "osascript " + startScript.getAbsolutePath();
                } catch (IOException e) {
                    getLogger().error(e.getMessage(), e);
                    cmd = getDefaultTerminalCommand();
                }
            } else {
                cmd = getDefaultTerminalCommand();
            }
        }
        return cmd.replace("$p", folder.getAbsolutePath());
    }

    @Override
    protected void toggleEnabledState() {

    }

    private static Logger getLogger() {
        if (logger == null) {
            logger = LoggerFactory.getLogger(MuTerminal.class);
        }
        return logger;
    }

    @Override
    public ActionDescriptor getDescriptor() {
        return new Descriptor();
    }

    public static final class Descriptor extends AbstractActionDescriptor {

        public static final String ACTION_ID = "Terminal";

        @Override
        public String getId() {
            return ACTION_ID;
        }

        @Override
        public ActionCategory getCategory() {
            return ActionCategory.MISC;
        }

        @Override
        public KeyStroke getDefaultAltKeyStroke() {
            return null;
        }

        @Override
        public KeyStroke getDefaultKeyStroke() {
            return KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0);
        }

        @Override
        public MuAction createAction(MainFrame mainFrame, Map<String, Object> properties) {
            return new TerminalAction(mainFrame, properties);
        }

    }

}
