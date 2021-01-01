/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2012 Maxence Bernard
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

package com.mucommander.ui.dialog.file;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.util.FileSet;
import com.mucommander.desktop.AbstractTrash;
import com.mucommander.desktop.DesktopManager;
import com.mucommander.job.DeleteJob;
import com.mucommander.ui.action.ActionManager;
import com.mucommander.ui.action.ActionProperties;
import com.mucommander.ui.action.impl.DeleteAction;
import com.mucommander.ui.action.impl.PermanentDeleteAction;
import com.mucommander.ui.dialog.DialogToolkit;
import com.mucommander.ui.layout.InformationPane;
import com.mucommander.ui.layout.YBoxPanel;
import com.mucommander.ui.main.MainFrame;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;


/**
 * Confirmation dialog invoked when the user wants to delete currently selected files. It allows to choose between two
 * different ways of deleting files: move them to the trash or permanently erase them. The former choice is only given
 * if a trash is available on the current platform and capable of moving the selected files.
 * The choice (use trash or not) is saved in the preferences and reused next time this dialog is invoked.   
 *
 * @see com.mucommander.ui.action.impl.DeleteAction
 * @author Maxence Bernard
 */
public class DeleteDialog extends JobDialog implements ItemListener, ActionListener {

    /** Should files be moved to the trash or permanently erased */
    private boolean moveToTrash;

    /** Allows to control whether files should be moved to trash when deleted or permanently erased */
    private JCheckBox cbMoveToTrash;

    /** Informs the user about the consequences of deleting files, based on the current 'Move to trash' choice */
    private final InformationPane informationPane;

    /** The button that confirms deletion */
    private final JButton btnDelete;

    /** Dialog size constraints */
    private final static Dimension MINIMUM_DIALOG_DIMENSION = new Dimension(360, 0);


    public DeleteDialog(MainFrame mainFrame, FileSet files, boolean deletePermanently) {
        super(mainFrame, ActionProperties.getActionLabel(DeleteAction.Descriptor.ACTION_ID), files);

        this.mainFrame = mainFrame;

        YBoxPanel mainPanel = new YBoxPanel();

        // Allow 'Move to trash' option only if:
        // - the current platform has a trash
        // - the base folder is not an archive
        // - the base folder of the to-be-deleted files is not a trash folder or one of its children
        // - the base folder can be moved to the trash (the eligibility conditions should be the same as the files to-be-deleted)
        AbstractTrash trash = DesktopManager.getTrash();
        AbstractFile baseFolder = files.getBaseFolder();
        if (trash != null && !baseFolder.isArchive() && !trash.isTrashFile(baseFolder) && trash.canMoveToTrash(baseFolder)) {
            moveToTrash = !deletePermanently;

            cbMoveToTrash = new JCheckBox(i18n("delete_dialog.move_to_trash.option"), moveToTrash);
            cbMoveToTrash.addItemListener(this);
        }

        informationPane = new InformationPane();
        mainPanel.add(informationPane);
        mainPanel.addSpace(10);

        // add panel with one file above buttons
        JPanel fileDetailsPanel = createFileDetailsPanel(files.size() > 1);
        if (files.size() == 1) {
            mainPanel.add(fileDetailsPanel);
        }

        // create file details button and OK/cancel buttons and lay them out a single row
        btnDelete = new JButton(i18n("delete"));
        JButton cancelButton = new JButton(i18n("cancel"));

        mainPanel.add(createButtonsPanel(files.size() > 1 ? createFileDetailsButton(fileDetailsPanel) : null,
                DialogToolkit.createOKCancelPanel(btnDelete, cancelButton, getRootPane(), this)));

        // add panel with multiple fil list below buttons
        if (files.size() > 1) {
            mainPanel.add(fileDetailsPanel);
        }

        if (cbMoveToTrash != null) {
            mainPanel.add(cbMoveToTrash);
        }

        getContentPane().add(mainPanel);

        // Give initial keyboard focus to the 'Delete' button
        setInitialFocusComponent(btnDelete);

        // Call dispose() when dialog is closed
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        updateDialog();
        if (files.size() > 1) {
            // Size dialog and show it to the screen
            setMinimumSize(MINIMUM_DIALOG_DIMENSION);
            //setResizable(false);
        } else {
            Dimension d = getContentPane().getPreferredSize();
            getContentPane().setMaximumSize(new Dimension(getContentPane().getMaximumSize().width, getContentPane().getPreferredSize().height));
            setMinimumSizeDialog(new Dimension(d.width*6/5, d.height*6/5));
            int maxWidth = Math.max(d.width*4, mainFrame.getWidth());
            setMaximumSizeDialog(new Dimension(maxWidth, d.height*3/2));
        }
    }


    /**
     * Updates the information pane to reflect the current 'Move to trash' choice.
     */
    private void updateDialog() {
        String textId = buildTitleId();
        informationPane.getMainLabel().setText(i18n(textId));
        String messageId = buildMessageId();
        informationPane.getCaptionLabel().setText(i18n(messageId));
        informationPane.setIcon(moveToTrash ? null : InformationPane.getPredefinedIcon(InformationPane.WARNING_ICON));
        setTitle(ActionManager.getActionInstance(moveToTrash ? DeleteAction.Descriptor.ACTION_ID:PermanentDeleteAction.Descriptor.ACTION_ID, mainFrame).getLabel());
    }

    @NotNull
    private String buildMessageId() {
        if (moveToTrash) {
            if (files.size() == 1) {
                AbstractFile file = files.get(0);
                return file.isSymlink() ? "this_operation_cannot_be_undone" : "delete_dialog.move_to_trash.confirmation_details_1";
            } else {
                return "delete_dialog.move_to_trash.confirmation_details";
            }
        } else {
            return "this_operation_cannot_be_undone";
        }
    }

    @NotNull
    private String buildTitleId() {
        boolean singleFileMode = files.size() == 1;
        if (singleFileMode) {
            AbstractFile file = files.get(0);
            if (file.isSymlink()) {
                return "delete_dialog.permanently_delete.symlink_confirmation_1";
            } else {
                return moveToTrash ? "delete_dialog.move_to_trash.confirmation_1" : "delete_dialog.permanently_delete.confirmation_1";
            }
        }
        return moveToTrash ? "delete_dialog.move_to_trash.confirmation" : "delete_dialog.permanently_delete.confirmation";
    }


    /////////////////////////////////
    // ItemListener implementation //
    /////////////////////////////////

    public void itemStateChanged(ItemEvent e) {
        moveToTrash = cbMoveToTrash.isSelected();
        updateDialog();
        pack();
    }


    ///////////////////////////////////
    // ActionListener implementation //
    ///////////////////////////////////

    public void actionPerformed(ActionEvent e) {
        // Start by disposing this dialog
        dispose();

        if (e.getSource() == btnDelete) {
            // Starts deleting files
            ProgressDialog progressDialog = new ProgressDialog(mainFrame, i18n("delete_dialog.deleting"));
            if (getReturnFocusTo() != null) {
                progressDialog.returnFocusTo(getReturnFocusTo());
            }
            DeleteJob deleteJob = new DeleteJob(progressDialog, mainFrame, files, moveToTrash);
            progressDialog.start(deleteJob);
        }
    }
}
