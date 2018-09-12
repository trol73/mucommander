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
import com.mucommander.conf.MuConfigurations;
import com.mucommander.conf.MuPreference;
import com.mucommander.conf.MuPreferences;
import com.mucommander.process.ProcessRunner;
import com.mucommander.ui.action.*;
import com.mucommander.ui.main.MainFrame;

import javax.swing.*;
import java.awt.event.KeyEvent;
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
        String cmd = getConsoleCommand(currentFolder);
        try {
            ProcessRunner.execute(cmd, currentFolder);
        } catch(Exception e) {
            e.printStackTrace();
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
        String cmd = getTerminalCommand();
        return cmd.replace("$p", folder.getAbsolutePath());
    }

    private static String getTerminalCommand() {
        return useCustomExternalTerminal() ? getCustomExternalTerminal() : getDefaultTerminalCommand();
    }

    private static String getCustomExternalTerminal() {
        return MuConfigurations.getPreferences().getVariable(MuPreference.CUSTOM_EXTERNAL_TERMINAL);
    }

    private static boolean useCustomExternalTerminal() {
        return MuConfigurations.getPreferences().getVariable(MuPreference.USE_CUSTOM_EXTERNAL_TERMINAL, MuPreferences.DEFAULT_USE_CUSTOM_EXTERNAL_TERMINAL);
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

        public MuAction createAction(MainFrame mainFrame, Map<String,Object> properties) {
            return new TerminalAction(mainFrame, properties);
        }

    }

}
