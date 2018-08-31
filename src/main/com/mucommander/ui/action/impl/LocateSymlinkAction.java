package com.mucommander.ui.action.impl;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.filter.AttributeFileFilter;
import com.mucommander.commons.file.util.FileSet;
import com.mucommander.ui.action.*;
import com.mucommander.ui.main.MainFrame;

import javax.swing.KeyStroke;
import java.util.Map;

/**
 *
 * @author Oleg Trifonov
 */
public class LocateSymlinkAction extends SelectedFilesAction {

    private LocateSymlinkAction(MainFrame mainFrame, Map<String, Object> properties) {
        super(mainFrame, properties);

        setSelectedFileFilter(new AttributeFileFilter(AttributeFileFilter.FileAttribute.SYMLINK));
    }

    @Override
    public void performAction(FileSet files) {
        AbstractFile link = mainFrame.getActiveTable().getSelectedFile();
        AbstractFile target = link.getCanonicalFile();
        mainFrame.getInactivePanel().tryChangeCurrentFolder(target.getParent(), target, false);
    }


    @Override
    public ActionDescriptor getDescriptor() {
        return new Descriptor();
    }


    public static final class Descriptor extends AbstractActionDescriptor {
        public static final String ACTION_ID = "LocateSymlink";

        public String getId() {
            return ACTION_ID;
        }

        public ActionCategory getCategory() {
            return ActionCategory.FILES;
        }

        public KeyStroke getDefaultAltKeyStroke() {
            return null;
        }

        public KeyStroke getDefaultKeyStroke() {
            return null;
        }

        public MuAction createAction(MainFrame mainFrame, Map<String,Object> properties) {
            return new LocateSymlinkAction(mainFrame, properties);
        }
    }
}

