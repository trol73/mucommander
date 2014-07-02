package com.mucommander.ui.main.quicklist;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.DummyFile;
import com.mucommander.commons.file.FileFactory;
import com.mucommander.text.Translator;
import com.mucommander.ui.action.ActionProperties;
import com.mucommander.ui.action.MuAction;
import com.mucommander.ui.action.impl.EditAction;
import com.mucommander.ui.action.impl.ShowRecentEditedFilesQLAction;
import com.mucommander.ui.action.impl.ViewAction;
import com.mucommander.ui.main.FolderPanel;
import com.mucommander.ui.main.MainFrame;
import com.mucommander.ui.quicklist.QuickListWithIcons;
import com.mucommander.ui.viewer.EditorRegistrar;
import com.mucommander.ui.viewer.text.TextFilesHistory;

import javax.swing.Icon;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by trol on 01/07/14.
 */
public class RecentEditedQL extends QuickListWithIcons<AbstractFile> {


    private static LinkedList<AbstractFile> list = new LinkedList<>();

    private static final int MAX_FILES_IN_LIST = 50;

    private final MainFrame mainFrame;


    public RecentEditedQL(FolderPanel folderPanel)  {
        super(folderPanel, ActionProperties.getActionLabel(ShowRecentEditedFilesQLAction.Descriptor.ACTION_ID), Translator.get("recent_edited_files_quick_list.empty_message"));
        mainFrame = folderPanel.getMainFrame();
    }

    @Override
    protected Icon itemToIcon(AbstractFile item) {
        return MuAction.getStandardIcon(EditAction.class);
    }

    @Override
    protected AbstractFile[] getData() {
        List<AbstractFile> list = TextFilesHistory.getInstance().getLastList(MAX_FILES_IN_LIST);
        return list.toArray(new AbstractFile[list.size()]);
    }

    @Override
    protected void acceptListItem(AbstractFile item) {
        if (item instanceof DummyFile) {
            item = FileFactory.getFile(item.getURL());
        }
        if (item.exists()) {
            EditorRegistrar.createEditorFrame(mainFrame, item, ActionProperties.getActionIcon(ViewAction.Descriptor.ACTION_ID).getImage());
        } else {
            // TODO error message
        }
    }

    public static void addFile(AbstractFile file) {
        if (!list.remove(file) && list.size() > MAX_FILES_IN_LIST) {
            list.removeLast();
        }
        list.addFirst(file);
    }
}
