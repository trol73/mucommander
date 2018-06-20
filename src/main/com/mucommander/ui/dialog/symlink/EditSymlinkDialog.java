/*
 * This file is part of trolCommander, http://www.trolsoft.ru/en/soft/trolcommander
 * Copyright (C) 2013-2016 Oleg Trifonov
 *
 * trolCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * trolCommander is distributed in the hope that it will be useful,
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

/**
 * Action for symlink editing.
 * Created on 23/06/14.
 */
public class EditSymlinkDialog extends FocusDialog implements ActionListener {

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
        super(mainFrame, i18n("symboliclinkeditor.edit"), null);
        this.linkPath = linkPath;

        Container contentPane = getContentPane();

        YBoxPanel yPanel = new YBoxPanel(10);

        edtTarget = new FilePathField();
        edtTarget.setDefaultLocation(linkPath.getParent());
        String hint = String.format(i18n("symboliclinkeditor.target_file_edit"), linkPath.getBaseName()) + ':';
        yPanel.add(new JLabel(hint));
        yPanel.add(edtTarget);

        edtTarget.setText(SymLinkUtils.getTargetPath(linkPath));
        edtTarget.addActionListener(this);

        yPanel.addSpace(10);

        contentPane.add(yPanel, BorderLayout.NORTH);

        btnOk = new JButton(i18n("ok"));
        JButton btnCancel = new JButton(i18n("cancel"));
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
            SymLinkUtils.editSymlink(linkPath, edtTarget.getText());
        }
        cancel();
    }

}
