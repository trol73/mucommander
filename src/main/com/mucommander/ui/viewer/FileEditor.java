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

package com.mucommander.ui.viewer;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import com.mucommander.commons.file.*;
import com.mucommander.commons.runtime.OsFamily;
import com.mucommander.job.FileCollisionChecker;
import com.mucommander.utils.text.Translator;
import com.mucommander.ui.dialog.InformationDialog;
import com.mucommander.ui.dialog.QuestionDialog;
import com.mucommander.ui.dialog.file.FileCollisionDialog;
import com.mucommander.ui.helper.MenuToolkit;
import com.mucommander.ui.helper.MnemonicHelper;
import com.mucommander.ui.main.quicklist.ViewedAndEditedFilesQL;
import ru.trolsoft.ui.TMenuSeparator;


/**
 * An abstract class to be subclassed by file editor implementations.
 *
 * <p><b>Warning:</b> the file viewer/editor API may soon receive a major overhaul.
 *
 * @author Maxence Bernard, Arik Hadas
 */
public abstract class FileEditor extends FilePresenter implements ActionListener {
	
    /** Menu items */
    private JMenuItem saveItem;
    private JMenuItem saveAsItem;
    private JMenuItem closeItem;
    private JMenuItem filesItem;
    
    /** Serves to indicate if saving is needed before closing the window, value should only be modified using the setSaveNeeded() method */
    private boolean saveNeeded;

    /**
     * Creates a new FileEditor.
     */
    protected FileEditor() {

    }
	

    protected void setSaveNeeded(boolean saveNeeded) {
    	if (getFrame() == null || this.saveNeeded == saveNeeded) {
            return;
        }
        if (!this.saveNeeded && saveNeeded) {
            getStatusBar().setStatusMessage(Translator.get("text_editor.modified"));
        }
        this.saveNeeded = saveNeeded;

        // Marks/unmarks the window as dirty under Mac OS X (symbolized by a dot in the window closing icon)
        if (OsFamily.MAC_OS_X.isCurrent()) {
            getFrame().getRootPane().putClientProperty("windowModified", saveNeeded);
        }
    }
    
    private boolean trySaveAs() {
        JFileChooser fileChooser = new JFileChooser();
		AbstractFile currentFile = getCurrentFile();
        // Sets selected file in JFileChooser to current file
        if (currentFile.getURL().getScheme().equals(FileProtocols.FILE)) {
            fileChooser.setSelectedFile(new File(currentFile.getAbsolutePath()));
        }
        fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
        int ret = fileChooser.showSaveDialog(getFrame());
		
        if (ret == JFileChooser.APPROVE_OPTION) {
            AbstractFile destFile;
            try {
                destFile = FileFactory.getFile(fileChooser.getSelectedFile().getAbsolutePath(), true);
            } catch (IOException e) {
                InformationDialog.showErrorDialog(getFrame(), Translator.get("write_error"), Translator.get("file_editor.cannot_write"));
                return false;
            }

            // Check for file collisions, i.e. if the file already exists in the destination
            int collision = FileCollisionChecker.checkForCollision(null, destFile);
            if (collision != FileCollisionChecker.NO_COLLOSION) {
                // File already exists in destination, ask the user what to do (cancel, overwrite,...) but
                // do not offer the multiple files mode options such as 'skip' and 'apply to all'.
                int action = new FileCollisionDialog(getFrame(), getFrame()/*mainFrame*/, collision, null, destFile, false, false).getActionValue();

                // User chose to overwrite the file
                if (action == FileCollisionDialog.OVERWRITE_ACTION) {
                    // Do nothing, simply continue and file will be overwritten
                }
                // User chose to cancel or closed the dialog
                else {
                    return false;
                }
            }

            if (trySave(destFile)) {
                setCurrentFile(destFile);
                return true;
            }
        }
        return false;
    }

    // Returns false if an error occurred while saving the file.
    private boolean trySave(AbstractFile destFile) {
        try {
            saveAs(destFile);
            return true;
        } catch (IOException e) {
            if (e instanceof FileNotFoundException) {
                if (!destFile.getPermissions().getBitValue(PermissionAccesses.USER_ACCESS, PermissionTypes.WRITE_PERMISSION)) {
                    return trySaveReadOnly(destFile);
                }
            }
            getStatusBar().setStatusMessage(Translator.get("text_editor.cant_save_file"));
            InformationDialog.showErrorDialog(getFrame(), Translator.get("write_error"), Translator.get("file_editor.cannot_write"));
            return false;
        }
    }


    private boolean trySaveReadOnly(AbstractFile destFile) {
        QuestionDialog dialog = new QuestionDialog((Frame)null, Translator.get("warning"), Translator.get("file_editor.overwrite_readonly"), getFrame(),
                new String[] {Translator.get("file_editor.save_anyway"), Translator.get("file_editor.save_as"), Translator.get("cancel")},
                new int[]  {0, 1, JOptionPane.CLOSED_OPTION},
                0);
        int ret = dialog.getActionValue();
        if (ret == 0) { // Overwrite
            try {
                destFile.changePermission(PermissionAccesses.USER_ACCESS, PermissionTypes.WRITE_PERMISSION, true);
                saveAs(destFile);
                destFile.changePermission(PermissionAccesses.USER_ACCESS, PermissionTypes.WRITE_PERMISSION, false);
                return true;
            } catch (IOException e) {
                getStatusBar().setStatusMessage(Translator.get("text_editor.cant_save_file"));
                InformationDialog.showErrorDialog(getFrame(), Translator.get("write_error"), Translator.get("file_editor.cannot_write"));
            }
        } else if (ret == 1) {  // Save as...
            if (trySaveAs() ) {
                return true;
            }
        }
        getStatusBar().setStatusMessage(Translator.get("text_editor.cant_save_file"));
        return false;
    }

    // Returns true if the file does not have any unsaved change or if the user refused to save the changes,
    // false if the user canceled the dialog or the save failed.
    public boolean askSave() {
        if (!saveNeeded) {
            return true;
        }

        QuestionDialog dialog = new QuestionDialog(getFrame(), null, Translator.get("file_editor.save_warning"), getFrame(),
                                                   new String[] {Translator.get("save"), Translator.get("dont_save"), Translator.get("cancel")},
                                                   new int[]  {JOptionPane.YES_OPTION, JOptionPane.NO_OPTION, JOptionPane.CANCEL_OPTION},
                                                   0);
        int ret = dialog.getActionValue();

        if ((ret == JOptionPane.YES_OPTION && trySave(getCurrentFile())) || ret == JOptionPane.NO_OPTION) {
            setSaveNeeded(false);
            return true;
        }

        return false;       // User canceled or the file couldn't be properly saved
    }
    
    /**
     * Returns the menu bar that controls the editor's frame. The menu bar should be retrieved using this method and
     * not by calling {@link JFrame#getJMenuBar()}, which may return <code>null</code>.
     *
     * @return the menu bar that controls the editor's frame.
     */
    public JMenuBar getMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        MnemonicHelper menuMnemonicHelper = new MnemonicHelper();
        MnemonicHelper menuItemMnemonicHelper = new MnemonicHelper();

        // File menu
        JMenu fileMenu = MenuToolkit.addMenu(Translator.get("file_editor.file_menu"), menuMnemonicHelper, null);
        if (OsFamily.getCurrent() == OsFamily.MAC_OS_X) {
            saveItem = MenuToolkit.addMenuItem(fileMenu, Translator.get("file_editor.save"), menuItemMnemonicHelper, KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.META_DOWN_MASK), this);
        } else {
            saveItem = MenuToolkit.addMenuItem(fileMenu, Translator.get("file_editor.save"), menuItemMnemonicHelper, KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_MASK), this);
        }
        saveAsItem = MenuToolkit.addMenuItem(fileMenu, Translator.get("file_editor.save_as"), menuItemMnemonicHelper, null, this);
        fileMenu.add(new TMenuSeparator());
        int mask = OsFamily.getCurrent() == OsFamily.MAC_OS_X ? KeyEvent.ALT_MASK : KeyEvent.CTRL_MASK;
        filesItem = MenuToolkit.addMenuItem(fileMenu, Translator.get("file_editor.files"), menuItemMnemonicHelper, KeyStroke.getKeyStroke(KeyEvent.VK_TAB, mask), this);
        fileMenu.add(new TMenuSeparator());
        closeItem = MenuToolkit.addMenuItem(fileMenu, Translator.get("file_editor.close"), menuItemMnemonicHelper, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), this);
		
        menuBar.add(fileMenu);

        return menuBar;
    }
    
    ////////////////////////////
    // ActionListener methods //
    ////////////////////////////
	
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
		
        // File menu
        if (source == saveItem) {
            trySave(getCurrentFile());
        } else if (source == saveAsItem) {
            trySaveAs();
        } else if (source == closeItem) {
        	getFrame().dispose();
        } else if (source == filesItem) {
            ViewedAndEditedFilesQL viewedAndEditedFilesQL = new ViewedAndEditedFilesQL(getFrame(), getCurrentFile());
            viewedAndEditedFilesQL.show();
        }
    }

    //////////////////////
    // Abstract methods //
    //////////////////////
	
    /**
     * This method is invoked when the user asked to save current file to the specified file.
     * 
     *
     * @param saveAsFile the file which should be used to save the file currently being edited
     * (path can be different from current file if the user chose 'Save as').
     * @throws IOException if an I/O error occurs.
     */
    protected abstract void saveAs(AbstractFile saveAsFile) throws IOException;
}
