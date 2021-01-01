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

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.runtime.OsFamily;
import com.mucommander.conf.TcConfigurations;
import com.mucommander.conf.TcPreference;
import com.mucommander.conf.TcPreferences;
import com.mucommander.desktop.DesktopManager;
import com.mucommander.process.ProcessRunner;
import com.mucommander.ui.action.*;
import com.mucommander.ui.main.MainFrame;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.Map;

/**
 * @author Oleg Trifonov
 * Created on 17/12/13.
 */
public class TerminalAction extends ParentFolderAction {
    /**
     * Creates a new instance of <code>InternalViewAction</code>.
     *
     * @param mainFrame  frame to which the action is attached.
     * @param properties action's properties.
     */
    private TerminalAction(MainFrame mainFrame, Map<String, Object> properties) {
        super(mainFrame, properties);
    }

    @Override
    public void performAction() {
        AbstractFile currentFolder = mainFrame.getActiveTable().getFileTableModel().getCurrentFolder();
        if (OsFamily.LINUX.isCurrent()) {
            performOnLinux(currentFolder);
        } else {
            String cmd = getConsoleCommand(currentFolder);
            try {
                ProcessRunner.execute(cmd, currentFolder);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void performOnLinux(AbstractFile currentFolder) {
        String[] tokens = getTerminalCommand().split(" ");
        for (int i = 0; i < tokens.length; i++) {
            if (tokens[i].contains("$p")) {
                tokens[i] = tokens[i].replace("$p", currentFolder.getAbsolutePath());
            }
        }
        try {
            ProcessRunner.execute(tokens, currentFolder);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String getConsoleCommand(AbstractFile folder) {
        String cmd = getTerminalCommand();
        String path = folder.getAbsolutePath();
        return cmd.replace("$p", path);
    }


    private static String getTerminalCommand() {
        return useCustomExternalTerminal() ? getCustomExternalTerminal() : DesktopManager.getDefaultTerminalAppCommand();
    }

    private static String getCustomExternalTerminal() {
        return TcConfigurations.getPreferences().getVariable(TcPreference.CUSTOM_EXTERNAL_TERMINAL);
    }

    private static boolean useCustomExternalTerminal() {
        return TcConfigurations.getPreferences().getVariable(TcPreference.USE_CUSTOM_EXTERNAL_TERMINAL, TcPreferences.DEFAULT_USE_CUSTOM_EXTERNAL_TERMINAL);
    }

    @Override
    protected void toggleEnabledState() {

    }

    @Override
    public ActionDescriptor getDescriptor() {
        return new Descriptor();
    }


    public static final class Descriptor extends AbstractActionDescriptor {
        public static final String ACTION_ID = "Terminal";

        public String getId() {
            return ACTION_ID;
        }

        public ActionCategory getCategory() {
            return ActionCategory.MISC;
        }

        public KeyStroke getDefaultAltKeyStroke() {
            return null;
        }

        public KeyStroke getDefaultKeyStroke() {
            return KeyStroke.getKeyStroke(KeyEvent.VK_F2, KeyEvent.SHIFT_DOWN_MASK);
        }

        public TcAction createAction(MainFrame mainFrame, Map<String,Object> properties) {
            return new TerminalAction(mainFrame, properties);
        }

    }

}
