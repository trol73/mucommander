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
import com.mucommander.text.Translator;
import com.mucommander.ui.dialog.DialogToolkit;
import com.mucommander.ui.dialog.FocusDialog;
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
            SymLinkUtils.createSymlink(edtName.getText(), edtTarget.getText());
        }
        cancel();
    }

}
