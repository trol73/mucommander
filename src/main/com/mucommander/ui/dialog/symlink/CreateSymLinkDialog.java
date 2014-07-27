package com.mucommander.ui.dialog.symlink;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.text.Translator;
import com.mucommander.ui.dialog.DialogToolkit;
import com.mucommander.ui.dialog.FocusDialog;
import com.mucommander.ui.layout.XAlignedComponentPanel;
import com.mucommander.ui.layout.YBoxPanel;
import com.mucommander.ui.text.FilePathField;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextField;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Created by trol on 09/06/14.
 */
public class CreateSymLinkDialog extends FocusDialog implements ActionListener {

    /**
     * Dialog size constraints
     */
    private static final Dimension MINIMUM_DIALOG_DIMENSION = new Dimension(320, 0);
    /**
     * Dialog width should not exceed 360, height is not an issue (always the same)
     */
    private static final Dimension MAXIMUM_DIALOG_DIMENSION = new Dimension(1024, 320);

    private final Frame mainFrame;
    private final AbstractFile linkPath;
    private final AbstractFile targetFile;
    private final FilePathField edtTarget;
    private final JTextField edtName;
    private final JButton btnOk;



    /**
     *
     * @param mainFrame
     * @param linkPath path of root directory for created symbolic link or symbolic link itself
     * @param targetFile existing filename (filename symlink will point to)
     */
    public CreateSymLinkDialog(Frame mainFrame, AbstractFile linkPath, AbstractFile targetFile) {
        super(mainFrame, Translator.get("symboliclinkeditor.create"), null);
        this.mainFrame = mainFrame;
        this.linkPath = linkPath;
        this.targetFile = targetFile;

        Container contentPane = getContentPane();

        YBoxPanel yPanel = new YBoxPanel(10);

        yPanel.add(new JLabel(Translator.get("symboliclinkeditor.target_file_create") + ':'));
        edtTarget = new FilePathField();
        yPanel.add(edtTarget);
        yPanel.addSpace(10);

        yPanel.add(new JLabel(Translator.get("symboliclinkeditor.link_name") + ':'));
        edtName = new JTextField();
        yPanel.add(edtName);

        contentPane.add(yPanel, BorderLayout.NORTH);

        btnOk = new JButton(Translator.get("ok"));
        JButton btnCancel = new JButton(Translator.get("cancel"));
        contentPane.add(DialogToolkit.createOKCancelPanel(btnOk, btnCancel, getRootPane(), this), BorderLayout.SOUTH);

        // Path field will receive initial focus
        setInitialFocusComponent(edtTarget);

        setMinimumSize(MINIMUM_DIALOG_DIMENSION);
        setMaximumSize(MAXIMUM_DIALOG_DIMENSION);

        edtName.setText(linkPath.getAbsolutePath() + targetFile.getBaseName());
        edtTarget.setText(targetFile.getAbsolutePath());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnOk) {
            createSymlink(edtName.getText(), edtTarget.getText());
        }
        cancel();
    }

    private void createSymlink(String link, String target) {
        Path linkPath = FileSystems.getDefault().getPath(link, "");
        Path targetPath = FileSystems.getDefault().getPath(target, "");
        try {
            Files.createSymbolicLink(linkPath, targetPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
