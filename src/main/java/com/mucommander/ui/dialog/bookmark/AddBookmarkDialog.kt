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
package com.mucommander.ui.dialog.bookmark

import com.mucommander.bookmark.Bookmark
import com.mucommander.bookmark.BookmarkManager
import com.mucommander.ui.action.ActionProperties
import com.mucommander.ui.action.impl.AddBookmarkAction
import com.mucommander.ui.dialog.DialogToolkit
import com.mucommander.ui.dialog.FocusDialog
import com.mucommander.ui.layout.XAlignedComponentPanel
import com.mucommander.ui.layout.YBoxPanel
import com.mucommander.ui.main.MainFrame
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import javax.swing.JButton
import javax.swing.JTextField
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

/**
 * This dialog allows the user to add a bookmark and enter a name for it. User can also
 * choose to store login and password information in the bookmark's URL if the bookmark
 * contains login/password information.
 * 
 * @author Maxence Bernard
 */
class AddBookmarkDialog(mainFrame: MainFrame) : FocusDialog(
    mainFrame.jFrame,
    ActionProperties.getActionLabel(AddBookmarkAction.Descriptor.ACTION_ID),
    mainFrame.jFrame
), ActionListener, DocumentListener {
    private val edtName: JTextField
    private val edtLocation: JTextField
    private val cbParent: BookmarkParentComboBox

    private val addButton: JButton
    private val cancelButton: JButton

    init {
        val currentFolder = mainFrame.activePanel.currentFolder

        // Text fields panel
        val compPanel = XAlignedComponentPanel().apply {
            edtName = JTextField(currentFolder.getName()).apply {
                isEditable = true
                document.addDocumentListener(this@AddBookmarkDialog)    // Monitors text changes to disable 'Add' button if name field is empty
            }
            addRow(i18n("name") + ":", edtName, 10)

            edtLocation = JTextField(currentFolder.canonicalPath)
            addRow(i18n("location") + ":", edtLocation, 10)

            cbParent = BookmarkParentComboBox()
            addRow(i18n("parent") + ":", cbParent, 10)
        }
        val mainPanel = YBoxPanel(5).apply {
            add(compPanel)
        }

        contentPane.add(mainPanel, BorderLayout.NORTH)

        addButton = JButton(i18n("add_bookmark_dialog.add"))
        cancelButton = JButton(i18n("cancel"))
        contentPane.add(
            DialogToolkit.createOKCancelPanel(addButton, cancelButton, getRootPane(), this),
            BorderLayout.SOUTH
        )

        // Select text in name field and transfer focus to it for immediate user change
        edtName.selectAll()
        setInitialFocusComponent(edtName)

        // Packs dialog
        minimumSize = MINIMUM_DIALOG_DIMENSION
        maximumSize = MAXIMUM_DIALOG_DIMENSION

        showDialog()
    }


    /**
     * Checks if bookmark name is empty (or white space), and enable/disable 'Add' button
     * accordingly, in order to prevent user from adding a bookmark with an empty name.
     */
    private fun checkEmptyName() {
        if (edtName.getText().isBlank()) {
            if (addButton.isEnabled) {
                addButton.setEnabled(false)
            }
        } else {
            if (!addButton.isEnabled) {
                addButton.setEnabled(true)
            }
        }
    }


    override fun actionPerformed(e: ActionEvent) {
        val source = e.getSource()

        if (source === addButton) {
            // Starts by disposing the dialog
            dispose()

            // Add bookmark and write bookmarks file to disk
            BookmarkManager.addBookmark(
                Bookmark(
                    edtName.getText(),
                    edtLocation.getText(),
                    cbParent.getSelectedParent()
                )
            )
            try {
                BookmarkManager.writeBookmarks(false)
            } catch (_: Exception) {
                // TODO We should probably pop an error dialog here.
            }
        } else if (source === cancelButton) {
            dispose()
        }
    }


    override fun changedUpdate(e: DocumentEvent) = checkEmptyName()
    override fun insertUpdate(e: DocumentEvent) = checkEmptyName()
    override fun removeUpdate(e: DocumentEvent) = checkEmptyName()

    companion object {
        // Dialog's width has to be at least 320
        private val MINIMUM_DIALOG_DIMENSION = Dimension(320, 0)

        // Dialog's width has to be at most 400
        private val MAXIMUM_DIALOG_DIMENSION = Dimension(400, 10000)
    }
}
