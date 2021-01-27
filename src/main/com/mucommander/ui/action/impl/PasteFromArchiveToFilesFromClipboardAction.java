
package com.mucommander.ui.action.impl;

import com.mucommander.commons.file.util.FileSet;
import com.mucommander.ui.action.AbstractActionDescriptor;
import com.mucommander.ui.action.ActionCategory;
import com.mucommander.ui.action.ActionDescriptor;
import com.mucommander.ui.action.TcAction;
import com.mucommander.ui.dnd.ClipboardOperations;
import com.mucommander.ui.dnd.ClipboardSupport;
import com.mucommander.ui.main.MainFrame;
import java.awt.event.KeyEvent;
import java.util.Map;
import javax.swing.KeyStroke;

/**
 * This action cuts the selected / marked files to the system clipboard, allowing to paste
 * them to muCommander.
 *
 * @author Nicholai R. Svarre
 */
public class PasteFromArchiveToFilesFromClipboardAction extends SelectedFilesAction {
    

    private PasteFromArchiveToFilesFromClipboardAction(MainFrame mainFrame, Map<String, Object> properties) {
        super(mainFrame, properties);
    }

    @Override
    public void performAction(FileSet files) {
        ClipboardSupport.setClipboardFiles(files);
        ClipboardSupport.setOperation(ClipboardOperations.ARCHIVE);
    }


    @Override
    public ActionDescriptor getDescriptor() {
        return new Descriptor();
    }

    public static final class Descriptor extends AbstractActionDescriptor {
        public static final String ACTION_ID = "PasteFromArchiveToFilesFromClipboard";

        public String getId() {
            return ACTION_ID;
        }

        public ActionCategory getCategory() {
            return ActionCategory.SELECTION;
        }

        public KeyStroke getDefaultAltKeyStroke() {
            return KeyStroke.getKeyStroke(KeyEvent.VK_D, KeyEvent.META_DOWN_MASK);
        }

        public KeyStroke getDefaultKeyStroke() {
            return KeyStroke.getKeyStroke(KeyEvent.VK_D, KeyEvent.CTRL_DOWN_MASK);
        }

        public TcAction createAction(MainFrame mainFrame, Map<String,Object> properties) {
            return new PasteFromArchiveToFilesFromClipboardAction(mainFrame, properties);
        }
    }
}