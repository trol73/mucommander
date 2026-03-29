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

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.util.FileSet;
import com.mucommander.commons.file.util.PathUtils;
import com.mucommander.commons.runtime.OsFamily;
import com.mucommander.job.MakeDirectoryFileJob;
import com.mucommander.ui.action.ActionManager;
import com.mucommander.ui.action.ActionProperties;
import com.mucommander.ui.action.impl.EditAction;
import com.mucommander.ui.action.impl.MkdirAction;
import com.mucommander.ui.action.impl.MkfileAction;
import com.mucommander.ui.chooser.SizeChooser;
import com.mucommander.ui.dialog.DialogToolkit;
import com.mucommander.ui.dialog.FocusDialog;
import com.mucommander.ui.dialog.InformationDialog;
import com.mucommander.ui.helper.FocusRequester;
import com.mucommander.ui.layout.YBoxPanel;
import com.mucommander.ui.main.MainFrame;
import com.mucommander.ui.text.FilePathField;
import com.mucommander.ui.viewer.EditorRegistrar;
import org.jetbrains.annotations.NotNull;


/**
 * Dialog invoked when the user wants to create a new folder or an empty file in the current folder.
 *
 * @see MkdirAction
 * @see MkfileAction
 * @author Maxence Bernard
 */
public class MakeDirectoryFileDialog extends FocusDialog implements ActionListener, ItemListener {

    private final MainFrame mainFrame;
	
    private final JTextField pathField;

    private JCheckBox cbAllocateSpace;
    private JCheckBox cbOpenTextEditor;
    private JCheckBox cbMakeExecutable;
    private SizeChooser allocateSpaceChooser;

    private final JButton btnOk;

    private final boolean mkfileMode;
    private boolean autoExecutableSelect;
    private static boolean openInTextEditor = true;

    /**
     * Dialog size constraints
     */
    private final static Dimension MINIMUM_DIALOG_DIMENSION = new Dimension(320, 0);

    /**
     * Dialog width should not exceed 360, height is not an issue (always the same)
     */
    private final static Dimension MAXIMUM_DIALOG_DIMENSION = new Dimension(400, 10000);

    private JCheckBox convertWhiteSpaceCheckBox;
    /**
     * As a developer, it is annoy to meet with Folder name that contains whitespace
     * 
     */
    private String oldDirName;


    /**
     * Creates a new Mkdir/Mkfile dialog.
     *
     * @param mkfileMode if true, the dialog will operate in 'mkfile' mode, if false in 'mkdir' mode
     */
    public MakeDirectoryFileDialog(MainFrame mainFrame, boolean mkfileMode) {
        super(mainFrame, ActionManager.getActionInstance(mkfileMode ? MkfileAction.Descriptor.ACTION_ID : MkdirAction.Descriptor.ACTION_ID, mainFrame).getLabel(), mainFrame);
        this.mainFrame = mainFrame;
        this.mkfileMode = mkfileMode;
        setStorageSuffix(mkfileMode ? "file" : "dir");

        Container contentPane = getContentPane();

        YBoxPanel mainPanel = new YBoxPanel();
        mainPanel.add(new JLabel(ActionProperties.getActionTooltip(mkfileMode ? MkfileAction.Descriptor.ACTION_ID : MkdirAction.Descriptor.ACTION_ID)+" :"));

        // Create a path field with auto-completion capabilities
        pathField = new FilePathField();
        pathField.addActionListener(this);

        // Sets the initial selection.
        AbstractFile currentFile = mainFrame.getActiveTable().getSelectedFile();
        if (currentFile != null) {
            String initialValue = makeInitialValue(currentFile);
            if (initialValue != null) {
                pathField.setText(initialValue);
            }
        }
        pathField.setSelectionStart(0);
        pathField.setSelectionEnd(pathField.getText().length());
        mainPanel.add(pathField);

        if (mkfileMode) {
            JPanel allocPanel = new JPanel(new BorderLayout());

            cbAllocateSpace = new JCheckBox(i18n("mkfile_dialog.allocate_space")+":", false);
            cbAllocateSpace.addItemListener(this);
            allocPanel.add(cbAllocateSpace, BorderLayout.WEST);

            allocateSpaceChooser = new SizeChooser(false);
            allocateSpaceChooser.setEnabled(false);
            allocPanel.add(allocateSpaceChooser, BorderLayout.EAST);

            mainPanel.add(allocPanel);

            cbOpenTextEditor = new JCheckBox(i18n("mkfile_dialog.open_in_editor"), false);
            cbOpenTextEditor.addItemListener(this);
            cbOpenTextEditor.setSelected(openInTextEditor);
            mainPanel.add(cbOpenTextEditor);

            if (OsFamily.getCurrent().isUnixBased()) {
                cbMakeExecutable = new JCheckBox(i18n("mkfile_dialog.make_executable"), false);
                cbMakeExecutable.addItemListener(this);
                mainPanel.add(cbMakeExecutable);

                pathField.getDocument().addDocumentListener(new DocumentListener() {
                    @Override
                    public void insertUpdate(DocumentEvent e) {
                        check();
                    }

                    @Override
                    public void removeUpdate(DocumentEvent e) {
                        check();
                    }

                    @Override
                    public void changedUpdate(DocumentEvent e) {
                        check();
                    }

                    private void check() {
                        if (!autoExecutableSelect && pathField.getText().endsWith(".sh")) {
                            cbMakeExecutable.setSelected(true);
                            autoExecutableSelect = true;
                        }
                    }
                });
            }
        } else {
            JPanel convertWhitespacePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            convertWhitespacePanel.add(new JLabel(i18n("mkfile_dialog.convert_whitespace")));
            this.convertWhiteSpaceCheckBox = new JCheckBox();
            convertWhiteSpaceCheckBox.addItemListener(arg0 -> {
					if (convertWhiteSpaceCheckBox.isSelected()) {
						oldDirName = pathField.getText();
						pathField.setText(oldDirName.replaceAll(" ", "_"));
					} else {
						pathField.setText(oldDirName);
					}
				});
            convertWhitespacePanel.add(convertWhiteSpaceCheckBox);
            mainPanel.add(convertWhitespacePanel);
       }
        
        mainPanel.addSpace(10);
        contentPane.add(mainPanel, BorderLayout.NORTH);
        
        btnOk = new JButton(i18n("create"));
        JButton cancelButton = new JButton(i18n("cancel"));
        contentPane.add(DialogToolkit.createOKCancelPanel(btnOk, cancelButton, getRootPane(), this), BorderLayout.SOUTH);

        // Path field will receive initial focus
        setInitialFocusComponent(pathField);

        setMinimumSize(MINIMUM_DIALOG_DIMENSION);
        setMaximumSize(MAXIMUM_DIALOG_DIMENSION);
    }

    private String makeInitialValue(AbstractFile currentFile) {
        if (mkfileMode) {
            return currentFile.getName();
        } else {
            return currentFile.getNameWithoutExtension();
        }
    }


    /**
     * Starts an {@link com.mucommander.job.MakeDirectoryFileJob}. This method is trigged by the 'OK' button or the return key.
     */
    private void startJob() {
        String enteredPath = pathField.getText();

        // Resolves destination folder
        PathUtils.ResolvedDestination resolvedDest = PathUtils.resolveDestination(enteredPath, mainFrame.getActivePanel().getCurrentFolder(), false);
        // The path entered doesn't correspond to any existing folder
        if (resolvedDest == null) {
            InformationDialog.showErrorDialog(mainFrame, i18n("invalid_path", enteredPath));
            return;
        }

        // Checks if the directory already exists and reports the error if that's the case
        int destinationType = resolvedDest.getDestinationType();
        if (destinationType == PathUtils.ResolvedDestination.EXISTING_FOLDER) {
            InformationDialog.showErrorDialog(mainFrame, i18n("directory_already_exists", enteredPath));
            return;
        }

        // Don't check for existing regular files, MakeDirectoryFileJob will take of it and popup a FileCollisionDialog 
        AbstractFile destFile = resolvedDest.getDestinationFile();

        FileSet fileSet = new FileSet(destFile.getParent());
        // Job's FileSet needs to contain at least one file
        fileSet.add(destFile);

        ProgressDialog progressDialog = new ProgressDialog(mainFrame, getTitle());

        MakeDirectoryFileJob job;
        job = buildJob(fileSet, progressDialog);

        progressDialog.start(job);
    }

    @NotNull
    private MakeDirectoryFileJob buildJob(FileSet fileSet, ProgressDialog progressDialog) {
        if (mkfileMode) {
            long allocateSpace = cbAllocateSpace.isSelected() ? allocateSpaceChooser.getValue() : -1;
            boolean executable = cbMakeExecutable != null && cbMakeExecutable.isSelected();
            openInTextEditor = cbOpenTextEditor.isSelected();
            return new MakeDirectoryFileJob(progressDialog, mainFrame, fileSet, allocateSpace, executable) {
                @Override
                protected boolean processFile(AbstractFile file, Object recurseParams) {
                    boolean result = super.processFile(file, recurseParams);
                    if (result && openInTextEditor) {
                        Image icon = ActionProperties.getActionIcon(EditAction.Descriptor.ACTION_ID).getImage();
                        EditorRegistrar.createEditorFrame(mainFrame, file, icon, FocusRequester::requestFocus);
                    }
                    return result;
                }
            };
        } else {
            return new MakeDirectoryFileJob(progressDialog, mainFrame, fileSet);
        }
    }


    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        dispose();
		
        // OK Button
        if (source == btnOk || source == pathField) {
            startJob();
        }
    }


    public void itemStateChanged(ItemEvent e) {
        allocateSpaceChooser.setEnabled(cbAllocateSpace.isSelected());
        if (e.getItem() == cbAllocateSpace && cbAllocateSpace.isSelected()) {
            cbOpenTextEditor.setSelected(false);
        } else if (e.getItem() == cbOpenTextEditor && cbOpenTextEditor.isSelected()) {
            cbAllocateSpace.setSelected(false);
        }
    }
}
