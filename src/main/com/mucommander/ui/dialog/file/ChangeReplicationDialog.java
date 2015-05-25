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
import com.mucommander.commons.file.UnsupportedFileOperationException;
import com.mucommander.commons.file.util.FileSet;
import com.mucommander.job.ChangeFileAttributesJob;
import com.mucommander.text.CustomDateFormat;
import com.mucommander.text.Translator;
import com.mucommander.ui.action.ActionProperties;
import com.mucommander.ui.action.impl.ChangeDateAction;
import com.mucommander.ui.action.impl.ChangeReplicationAction;
import com.mucommander.ui.dialog.DialogToolkit;
import com.mucommander.ui.layout.FluentPanel;
import com.mucommander.ui.layout.YBoxPanel;
import com.mucommander.ui.main.MainFrame;

import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;
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
public class ChangeReplicationDialog extends JobDialog implements ActionListener {

    private IntTextField replication;

    private JCheckBox recurseDirCheckBox;

    private JButton okButton;
    private JButton cancelButton;


    public ChangeReplicationDialog(MainFrame mainFrame, FileSet files) {
        super(mainFrame, ActionProperties.getActionLabel(ChangeReplicationAction.Descriptor.ACTION_ID), files);

        YBoxPanel mainPanel = new YBoxPanel();

        mainPanel.add(new JLabel(ActionProperties.getActionLabel(ChangeReplicationAction.Descriptor.ACTION_ID)+" :"));
        mainPanel.addSpace(5);

        AbstractFile destFile = files.size()==1?files.elementAt(0):files.getBaseFolder();
        boolean canChangeReplication = destFile.isFileOperationSupported(FileOperation.CHANGE_REPLICATION);

        short lastReplication=0;
        try {
            lastReplication=destFile.getReplication();
        } catch (UnsupportedFileOperationException e) {
            e.printStackTrace();
        }
        replication  = new IntTextField(lastReplication, 2);

        JPanel tempPanel = new FluentPanel(new FlowLayout(FlowLayout.LEFT));
        tempPanel.add(new JLabel(Translator.get("replication.number")));
        tempPanel.add(replication);
        mainPanel.add(tempPanel);

        mainPanel.addSpace(10);

        recurseDirCheckBox = new JCheckBox(Translator.get("recurse_directories"));
        mainPanel.add(recurseDirCheckBox);

        mainPanel.addSpace(15);

        // create file details button and OK/cancel buttons and lay them out a single row
        JPanel fileDetailsPanel = createFileDetailsPanel();

        okButton = new JButton(Translator.get("change"));
        cancelButton = new JButton(Translator.get("cancel"));

        mainPanel.add(createButtonsPanel(createFileDetailsButton(fileDetailsPanel),
                DialogToolkit.createOKCancelPanel(okButton, cancelButton, getRootPane(), this)));
        mainPanel.add(fileDetailsPanel);

        getContentPane().add(mainPanel, BorderLayout.NORTH);

        if (!canChangeReplication) {
            replication.setEnabled(false);
            recurseDirCheckBox.setEnabled(false);
            okButton.setEnabled(false);
        }

        getRootPane().setDefaultButton(canChangeReplication?okButton:cancelButton);
        setInitialFocusComponent(canChangeReplication?replication:cancelButton);
        setResizable(true);
    }


    ///////////////////////////////////
    // ActionListener implementation //
    ///////////////////////////////////

    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();

        if (source == okButton) {
            dispose();

            // Change replication
            ProgressDialog progressDialog = new ProgressDialog(mainFrame, Translator.get("progress_dialog.processing_files"));
            ChangeFileAttributesJob job = new ChangeFileAttributesJob(progressDialog, mainFrame, files,
                    (short)replication.getValue(),
                recurseDirCheckBox.isSelected());
            progressDialog.start(job);
        } else if (source == cancelButton) {
            dispose();
        }
    }


    /////////////////////////////////
    // ItemListener implementation //
    /////////////////////////////////

    class IntTextField extends JTextField {
        public IntTextField(int defval, int size) {
            super("" + defval, size);
        }

        protected Document createDefaultModel() {
            return new IntTextDocument();
        }

        public boolean isValid() {
            try {
                Integer.parseInt(getText());
                return true;
            } catch (Exception e) {
                return false;
            }
        }

        public int getValue() {
            try {
                return Integer.parseInt(getText());
            } catch (NumberFormatException e) {
                return 0;
            }
        }

        class IntTextDocument extends PlainDocument {
            public void insertString(int offs, String str, AttributeSet a)
                    throws BadLocationException {
                if (str == null)
                    return;
                String oldString = getText(0, getLength());
                String newString = oldString.substring(0, offs) + str
                        + oldString.substring(offs);
                try {
                    Integer.parseInt(newString + "0");
                    super.insertString(offs, str, a);
                } catch (NumberFormatException e) {
                }
            }
        }
    }
}