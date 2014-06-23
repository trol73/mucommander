package com.mucommander.ui.action.impl;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.FileOperation;
import com.mucommander.ui.action.*;
import com.mucommander.ui.dialog.symlink.CreateSymlinkDialog;
import com.mucommander.ui.main.MainFrame;

import javax.swing.KeyStroke;
import java.awt.event.KeyEvent;
import java.util.Map;


/**
 * This action brings up the 'Make directory' dialog which allows to create a new directory in the currently active folder.
 *
 * @author Maxence Bernard
 */
public class CreateSymlinkAction extends ParentFolderAction {

    public CreateSymlinkAction(MainFrame mainFrame, Map<String,Object> properties) {
        super(mainFrame, properties);
    }

    @Override
    protected void toggleEnabledState() {
        AbstractFile targetFile = mainFrame.getActiveTable().getSelectedFile();
        if (targetFile == null) {
            AbstractFile f = mainFrame.getActiveTable().getFileTableModel().getFileAt(0);
            if (f != null) {
                targetFile = f.getParent();
            }
        }
        AbstractFile linkPath = mainFrame.getInactivePanel().getCurrentFolder();
        setEnabled(targetFile != null && linkPath != null && linkPath.isFileOperationSupported(FileOperation.CREATE_DIRECTORY));
    }

    @Override
    public void performAction() {
        AbstractFile targetFile = mainFrame.getActiveTable().getSelectedFile();
        if (targetFile == null) {
            targetFile = mainFrame.getActiveTable().getFileTableModel().getFileAt(0).getParent();
        }
        AbstractFile linkPath = mainFrame.getInactivePanel().getCurrentFolder();
        new CreateSymlinkDialog(mainFrame, linkPath, targetFile).showDialog();
    }

    @Override
    public ActionDescriptor getDescriptor() {
        return new Descriptor();
    }

    ///////////////////
    // Inner classes //
    ///////////////////

    public static class Factory implements ActionFactory {

        public MuAction createAction(MainFrame mainFrame, Map<String,Object> properties) {
            return new CreateSymlinkAction(mainFrame, properties);
        }
    }

    public static class Descriptor extends AbstractActionDescriptor {
        public static final String ACTION_ID = "CreateSymlink";

        public String getId() { return ACTION_ID; }

        public ActionCategory getCategory() { return ActionCategory.FILES; }

        public KeyStroke getDefaultAltKeyStroke() { return null; }

        public KeyStroke getDefaultKeyStroke() { return KeyStroke.getKeyStroke(KeyEvent.VK_F7, KeyEvent.ALT_DOWN_MASK); }
    }
}
