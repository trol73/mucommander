package com.mucommander.ui.action.impl;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.runtime.OsFamily;
import com.mucommander.ui.action.*;
import com.mucommander.ui.dialog.file.FindFileDialog;
import com.mucommander.ui.main.MainFrame;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.util.Map;

/**
 * Created by trol on 18/12/13.
 */
public class FindFileAction extends ParentFolderAction {

    public FindFileAction(MainFrame mainFrame, Map<String, Object> properties) {
        super(mainFrame, properties);
    }

    @Override
    protected void toggleEnabledState() {

    }

    @Override
    public void performAction() {
        AbstractFile currentFolder = mainFrame.getActiveTable().getFileTableModel().getCurrentFolder();
        new FindFileDialog(mainFrame, currentFolder).showDialog();
    }

    @Override
    public ActionDescriptor getDescriptor() {
        return new Descriptor();
    }

    // - Factory -------------------------------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    public static class Factory implements ActionFactory {

        public MuAction createAction(MainFrame mainFrame, Map<String,Object> properties) {
            return new FindFileAction(mainFrame, properties);
        }
    }


    public static class Descriptor extends AbstractActionDescriptor {
        public static final String ACTION_ID = "FindFile";

        public String getId() { return ACTION_ID; }

        public ActionCategory getCategory() { return ActionCategories.NAVIGATION; }

        public KeyStroke getDefaultAltKeyStroke() { return null; }

        public KeyStroke getDefaultKeyStroke() {
            if (OsFamily.getCurrent() != OsFamily.MAC_OS_X) {
                return KeyStroke.getKeyStroke(KeyEvent.VK_F, KeyEvent.CTRL_DOWN_MASK);
            } else {
                return KeyStroke.getKeyStroke(KeyEvent.VK_F, KeyEvent.META_DOWN_MASK);
            }
        }
    }
}
