package com.mucommander.ui.action.impl;

import com.mucommander.ui.action.*;
import com.mucommander.ui.main.MainFrame;
import com.mucommander.ui.main.QuickLists;

import javax.swing.KeyStroke;
import java.awt.event.KeyEvent;
import java.util.Map;

/**
 * This action shows RecentEditedFilesQL on the current active FileTable.
 *
 * @author Oleg Trifonov
 */
public class ShowRecentEditedFilesQLAction extends ShowQuickListAction {

    ShowRecentEditedFilesQLAction(MainFrame mainFrame, Map<String, Object> properties) {
        super(mainFrame, properties);
    }

    @Override
    public void performAction() {
        openQuickList(QuickLists.RECENT_EDITED_FILES);
    }

    @Override
    public ActionDescriptor getDescriptor() {
        return new Descriptor();
    }


    public static final class Descriptor extends AbstractActionDescriptor {
        public static final String ACTION_ID = "ShowRecentEditedFilesQL";

        public String getId() { return ACTION_ID; }

        public ActionCategory getCategory() { return ActionCategory.NAVIGATION; }

        public KeyStroke getDefaultAltKeyStroke() { return null; }

        public KeyStroke getDefaultKeyStroke() { return KeyStroke.getKeyStroke(KeyEvent.VK_8, KeyEvent.ALT_DOWN_MASK); }

        public MuAction createAction(MainFrame mainFrame, Map<String,Object> properties) {
            return new ShowRecentEditedFilesQLAction(mainFrame, properties);
        }
    }

}
