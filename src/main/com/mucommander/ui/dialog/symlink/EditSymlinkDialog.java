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
import com.mucommander.text.Translator;
import com.mucommander.ui.dialog.DialogToolkit;
import com.mucommander.ui.dialog.FocusDialog;
import com.mucommander.ui.layout.YBoxPanel;
import com.mucommander.ui.text.FilePathField;

import javax.swing.JButton;
import javax.swing.JLabel;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Created on 23/06/14.
 */
public class EditSymlinkDialog extends FocusDialog implements ActionListener {

    private final Frame mainFrame;
    private final AbstractFile linkPath;
    private final FilePathField edtTarget;
    private final JButton btnOk;

    /**
     * Dialog size constraints
     */
    private static final Dimension MINIMUM_DIALOG_DIMENSION = new Dimension(320, 0);
    /**
     * Dialog width should not exceed 360, height is not an issue (always the same)
     */
    private static final Dimension MAXIMUM_DIALOG_DIMENSION = new Dimension(1024, 320);


    public EditSymlinkDialog(Frame mainFrame, AbstractFile linkPath) {
        super(mainFrame, Translator.get("symboliclinkeditor.edit"), null);
        this.mainFrame = mainFrame;
        this.linkPath = linkPath;

        Container contentPane = getContentPane();

        YBoxPanel yPanel = new YBoxPanel(10);

        edtTarget = new FilePathField();
        String hint = String.format(Translator.get("symboliclinkeditor.target_file_edit"), linkPath.getBaseName())+ ':';
        yPanel.add(new JLabel(hint));
        yPanel.add(edtTarget);

        edtTarget.setText(linkPath.getCanonicalPath());
        edtTarget.addActionListener(this);


        yPanel.addSpace(10);

        contentPane.add(yPanel, BorderLayout.NORTH);

        btnOk = new JButton(Translator.get("ok"));
        JButton btnCancel = new JButton(Translator.get("cancel"));
        contentPane.add(DialogToolkit.createOKCancelPanel(btnOk, btnCancel, getRootPane(), this), BorderLayout.SOUTH);
        btnOk.addActionListener(this);

        // Path field will receive initial focus
        setInitialFocusComponent(edtTarget);

        setMinimumSize(MINIMUM_DIALOG_DIMENSION);
        setMaximumSize(MAXIMUM_DIALOG_DIMENSION);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnOk || e.getSource() == edtTarget) {
            editSymlink(linkPath.getAbsolutePath(), edtTarget.getText());
        }
        cancel();
    }


    private void editSymlink(String link, String target) {
        Path linkPath = FileSystems.getDefault().getPath(link, "");
        Path targetPath = FileSystems.getDefault().getPath(target, "");
        try {
            Files.delete(linkPath);
            Files.createSymbolicLink(linkPath, targetPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
