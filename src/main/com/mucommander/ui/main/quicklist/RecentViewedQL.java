package com.mucommander.ui.main.quicklist;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.DummyFile;
import com.mucommander.commons.file.FileFactory;
import com.mucommander.text.Translator;
import com.mucommander.ui.action.ActionProperties;
import com.mucommander.ui.action.impl.ShowRecentViewedFilesQLAction;
import com.mucommander.ui.action.impl.ViewAction;
import com.mucommander.ui.main.FolderPanel;
import com.mucommander.ui.main.MainFrame;
import com.mucommander.ui.quicklist.QuickListWithIcons;
import com.mucommander.ui.viewer.ViewerRegistrar;
import com.mucommander.ui.viewer.text.TextFilesHistory;

import javax.swing.Icon;
import java.util.List;

/**
 * Created by trol on 01/07/14.
 */
public class RecentViewedQL extends QuickListWithIcons<AbstractFile> {

    private static final int MAX_FILES_IN_LIST = 50;

    private final MainFrame mainFrame;

    public RecentViewedQL(FolderPanel folderPanel) {
        super(folderPanel, ActionProperties.getActionLabel(ShowRecentViewedFilesQLAction.Descriptor.ACTION_ID), Translator.get("recent_viewed_files_quick_list.empty_message"));
        this.mainFrame = folderPanel.getMainFrame();
    }

    @Override
    protected Icon itemToIcon(AbstractFile item) {
        return null;
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
            ViewerRegistrar.createViewerFrame(mainFrame, item, ActionProperties.getActionIcon(ViewAction.Descriptor.ACTION_ID).getImage());
        } else {
            // TODO error message
        }
    }
}
