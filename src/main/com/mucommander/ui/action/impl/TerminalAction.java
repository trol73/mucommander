package com.mucommander.ui.action.impl;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.runtime.OsFamily;
import com.mucommander.process.ProcessRunner;
import com.mucommander.ui.action.*;
import com.mucommander.ui.main.MainFrame;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.util.Map;

/**
 * Created by trol on 17/12/13.
 */
public class TerminalAction extends ParentFolderAction {
    /**
     * Creates a new instance of <code>InternalViewAction</code>.
     *
     * @param mainFrame  frame to which the action is attached.
     * @param properties action's properties.
     */
    public TerminalAction(MainFrame mainFrame, Map<String, Object> properties) {
        super(mainFrame, properties);
    }

    @Override
    public void performAction() {
        AbstractFile currentFolder = mainFrame.getActiveTable().getFileTableModel().getCurrentFolder();
        String cmd = getConsoleCommand(currentFolder);
        try {
            //ProcessRunner.execute(cmd);
            ProcessRunner.execute(cmd, currentFolder);
        } catch(Exception e) {
            e.printStackTrace();
        }

    }

    private static String getConsoleCommand(AbstractFile folder) {
        switch (OsFamily.getCurrent()) {
            case WINDOWS:
                return "cmd /c start cmd.exe /K \"cd /d " + folder + '"';
            case LINUX:
                break;
            case MAC_OS_X:
                return "open -a Terminal .";// + folder;
        }
        return null;
    }

    @Override
    protected void toggleEnabledState() {

    }

    @Override
    public ActionDescriptor getDescriptor() {
        return new Descriptor();
    }

    // - Factory -------------------------------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    public static class Factory implements ActionFactory {

        public MuAction createAction(MainFrame mainFrame, Map<String,Object> properties) {
            return new TerminalAction(mainFrame, properties);
        }
    }

    public static class Descriptor extends AbstractActionDescriptor {
        public static final String ACTION_ID = "Terminal";

        public String getId() { return ACTION_ID; }

        public ActionCategory getCategory() { return ActionCategory.COMMANDS; }

        public KeyStroke getDefaultAltKeyStroke() { return null; }

        public KeyStroke getDefaultKeyStroke() { return KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0); }
    }

}
