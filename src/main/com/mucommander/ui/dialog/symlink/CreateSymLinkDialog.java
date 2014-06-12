package com.mucommander.ui.dialog.symlink;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.text.Translator;
import com.mucommander.ui.dialog.FocusDialog;
import com.mucommander.ui.layout.XAlignedComponentPanel;
import com.mucommander.ui.layout.YBoxPanel;
import com.mucommander.ui.text.FilePathField;

import javax.swing.JTextField;
import java.awt.Component;
import java.awt.Container;
import java.awt.Frame;

/**
 * Created by trol on 09/06/14.
 */
public class CreateSymLinkDialog extends FocusDialog {

    private final Frame mainFrame;
    private final AbstractFile path;
//    private final FilePathField edtTarget;
//    private final JTextField edtName;

    /**
     *
     * @param mainFrame
     * @param path path of root directory for created symbolic link or symbolic link itself
     * @param create true if case of creation a new symbolic link, false in the case of edition existing
     */
    public CreateSymLinkDialog(Frame mainFrame, AbstractFile path, boolean create) {
        super(mainFrame, Translator.get("symboliclinkeditor.title_create"), null);
        this.mainFrame = mainFrame;
        this.path = path;

        Container contentPane = getContentPane();

        YBoxPanel yPanel = new YBoxPanel(10);

        // Text fields panel
        XAlignedComponentPanel compPanel = new XAlignedComponentPanel();
    }
}
