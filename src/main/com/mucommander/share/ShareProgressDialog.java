package com.mucommander.share;

import com.mucommander.job.FileJob;
import com.mucommander.ui.dialog.file.ProgressDialog;
import com.mucommander.ui.main.MainFrame;

/**
 *
 * @author Mathias
 */
public class ShareProgressDialog extends ProgressDialog{
    

    public ShareProgressDialog(MainFrame mainFrame, String title) {
        super(mainFrame, title);
    }

    @Override
    public void start(FileJob job) {
        super.start(job);
        super.currentSpeedLabel.setVisible(false);
        super.collapseExpandButton.setVisible(false);
        super.collapseExpandButton.setExpandedState(false);
        super.pauseResumeButton.setVisible(false);
        super.currentFileProgressBar.setVisible(false);
        super.totalTransferredLabel.setVisible(false);

    }


}
