/*
 * This file is part of trolCommander, http://www.trolsoft.ru/soft/trolcommander
 * Copyright (C) 2013-2014 Oleg Trifonov
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.mucommander.ui.dialog.symlink;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.util.SymLinkUtils;
import com.mucommander.job.ui.UserInputHelper;
import com.mucommander.text.Translator;
import com.mucommander.ui.dialog.DialogToolkit;
import com.mucommander.ui.dialog.FocusDialog;
import com.mucommander.ui.dialog.QuestionDialog;
import com.mucommander.ui.layout.YBoxPanel;
import com.mucommander.ui.text.FilePathField;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.FileAlreadyExistsException;

/**
 * Created on 09/06/14.
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


    private static final int RETRY_ACTION = 1;
    private static final int CANCEL_ACTION = 0;



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
        edtName = new FilePathField();
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
            new Thread() {
                @Override
                public void run() {
                    execute();
                }
            }.start();

        } // btnOk
    }


    private void execute() {
        QuestionDialog dialog = null;
        final String targetPath = edtTarget.getText();
        final String linkPath = edtName.getText();
        String errorMessage;
        while (true) {
            try {
                SymLinkUtils.createSymlink(linkPath, targetPath);
                // success
                break;
            } catch (FileAlreadyExistsException e) {
                errorMessage = Translator.get("cannot_write_symlink_already_exists", linkPath);
            } catch (AccessDeniedException e) {
                errorMessage = Translator.get("cannot_write_symlink_access_denied", linkPath);
            } catch (IOException e) {
                errorMessage = Translator.get("cannot_write_symlink", linkPath);
            }

            if (dialog == null) {
                dialog = new QuestionDialog(mainFrame,
                        Translator.get("error"),
                        errorMessage,
                        mainFrame,
                        new String[] {Translator.get("retry"), Translator.get("cancel")},
                        new int[] {RETRY_ACTION, CANCEL_ACTION},
                        0);
            }

            UserInputHelper jobUserInput = new UserInputHelper(null, dialog);
            int action = (Integer)jobUserInput.getUserInput();
            if (action < 0 || action == CANCEL_ACTION) {
                break;
            }
        }
        cancel();
    }

}
