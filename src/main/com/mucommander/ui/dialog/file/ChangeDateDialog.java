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
import com.mucommander.commons.file.FileOperation;
import com.mucommander.commons.file.util.FileSet;
import com.mucommander.job.ChangeFileAttributesJob;
import com.mucommander.utils.text.CustomDateFormat;
import com.mucommander.ui.action.ActionProperties;
import com.mucommander.ui.action.impl.ChangeDateAction;
import com.mucommander.ui.dialog.DialogToolkit;
import com.mucommander.ui.layout.FluentPanel;
import com.mucommander.ui.layout.YBoxPanel;
import com.mucommander.ui.main.MainFrame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Date;

/**
 * This dialog allows the user to change the date of the currently selected/marked file(s). By default, the date is now
 * but a specific date can be specified.
 *
 * @author Maxence Bernard
 */
public class ChangeDateDialog extends JobDialog implements ActionListener, ItemListener {

    private final JRadioButton nowRadioButton;
    private final JSpinner dateSpinner;
    private final JCheckBox cdRecurseDir;
    private final JButton btnOk;
    private final JButton btnCancel;


    public ChangeDateDialog(MainFrame mainFrame, FileSet files) {
        super(mainFrame, ActionProperties.getActionLabel(ChangeDateAction.Descriptor.ACTION_ID), files);

        YBoxPanel mainPanel = new YBoxPanel();

        mainPanel.add(new JLabel(ActionProperties.getActionLabel(ChangeDateAction.Descriptor.ACTION_ID)+" :"));
        mainPanel.addSpace(5);

        ButtonGroup buttonGroup = new ButtonGroup();

        AbstractFile destFile = files.size()==1?files.elementAt(0):files.getBaseFolder();
        boolean canChangeDate = destFile.isFileOperationSupported(FileOperation.CHANGE_DATE);

        nowRadioButton = new JRadioButton(i18n("change_date_dialog.now"));
        nowRadioButton.setSelected(true);
        nowRadioButton.addItemListener(this);

        mainPanel.add(new FluentPanel(new FlowLayout(FlowLayout.LEFT)).add(nowRadioButton));

        buttonGroup.add(nowRadioButton);
        JRadioButton specificDateRadioButton = new JRadioButton(i18n("change_date_dialog.specific_date"));
        buttonGroup.add(specificDateRadioButton);

        this.dateSpinner = new JSpinner(new SpinnerDateModel());
        dateSpinner.setEditor(new JSpinner.DateEditor(dateSpinner, CustomDateFormat.getDateFormatString()));
        // Use the selected file's date if there is only one file, if not use base folder's date.
        dateSpinner.setValue(new Date(destFile.getLastModifiedDate()));
        // Spinner is disabled until the 'Specific date' radio button is selected 
        dateSpinner.setEnabled(false);

        mainPanel.add(new FluentPanel(new FlowLayout(FlowLayout.LEFT))
                          .add(specificDateRadioButton)
                          .add(dateSpinner));

        mainPanel.addSpace(10);
        cdRecurseDir = new JCheckBox(i18n("recurse_directories"));
        mainPanel.add(cdRecurseDir);

        mainPanel.addSpace(15);

        // create file details button and OK/cancel buttons and lay them out a single row
        JPanel fileDetailsPanel = createFileDetailsPanel();

        btnOk = new JButton(i18n("change"));
        btnCancel = new JButton(i18n("cancel"));

        mainPanel.add(createButtonsPanel(createFileDetailsButton(fileDetailsPanel),
                DialogToolkit.createOKCancelPanel(btnOk, btnCancel, getRootPane(), this)));
        mainPanel.add(fileDetailsPanel);

        getContentPane().add(mainPanel, BorderLayout.NORTH);

        if (!canChangeDate) {
            nowRadioButton.setEnabled(false);
            specificDateRadioButton.setEnabled(false);
            dateSpinner.setEnabled(false);
            cdRecurseDir.setEnabled(false);
            btnOk.setEnabled(false);
        }

        getRootPane().setDefaultButton(canChangeDate? btnOk : btnCancel);
        setInitialFocusComponent(canChangeDate?nowRadioButton: btnCancel);
        setResizable(false);
    }


    ///////////////////////////////////
    // ActionListener implementation //
    ///////////////////////////////////

    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();

        if (source == btnOk) {
            dispose();

            // Starts copying files
            ProgressDialog progressDialog = new ProgressDialog(mainFrame, i18n("progress_dialog.processing_files"));
            ChangeFileAttributesJob job = new ChangeFileAttributesJob(progressDialog, mainFrame, files,
                nowRadioButton.isSelected() ? System.currentTimeMillis() : ((SpinnerDateModel)dateSpinner.getModel()).getDate().getTime(),
                cdRecurseDir.isSelected());
            progressDialog.start(job);
        } else if (source == btnCancel) {
            dispose();
        }
    }


    /////////////////////////////////
    // ItemListener implementation //
    /////////////////////////////////

    // Enable/disables the date spinner component when the radio button selection has changed  

    public void itemStateChanged(ItemEvent e) {
        if (e.getSource() == nowRadioButton) {
            dateSpinner.setEnabled(!nowRadioButton.isSelected());
        }
    }
}