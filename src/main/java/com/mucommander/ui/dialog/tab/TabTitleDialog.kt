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
package com.mucommander.ui.dialog.tab

import com.mucommander.ui.action.ActionProperties
import com.mucommander.ui.action.impl.SetTabTitleAction
import com.mucommander.ui.dialog.DialogToolkit
import com.mucommander.ui.dialog.FocusDialog
import com.mucommander.ui.layout.XBoxPanel
import com.mucommander.ui.main.FolderPanel
import com.mucommander.ui.main.MainFrame
import com.mucommander.ui.text.SizeConstrainedDocument
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import javax.swing.*

/**
 * This dialog allow the user to enter a title for the currently selected tab.
 * Empty title means that the tab title will be based on the current location
 * presented in the tab.
 * 
 * @author Arik Hadas
 */
class TabTitleDialog(
    mainFrame: MainFrame,
    /** The FolderPanel to which this tab belongs  */
    private val folderPanel: FolderPanel
) : FocusDialog(
    mainFrame.jFrame,
    ActionProperties.getActionLabel(SetTabTitleAction.Descriptor.ACTION_ID),
    folderPanel.panel
), ActionListener {
    private val okButton = JButton(i18n("ok"))

    /** The text field in which the title is entered  */
    private val titleTextField = JTextField().apply {
        setDocument(SizeConstrainedDocument(31))
        text = folderPanel.tabs.getCurrentTab().getTitle()
        selectAll()
    }

    init {
        val cancelButton = JButton(i18n("cancel"))

        contentPane.apply {
            setLayout(BorderLayout())
            // Add customization panel
            add(createInnerPanel(), BorderLayout.CENTER)
            // Aligns the button panel to the right.
            add(DialogToolkit.createOKCancelPanel(okButton, cancelButton, rootPane, this@TabTitleDialog), BorderLayout.SOUTH)
        }

        minimumSize = MINIMUM_SIZE
    }

    private fun createInnerPanel(): JPanel =
        XBoxPanel().apply {
            add(JLabel(i18n("title") + ":"))
            addSpace(10)
            add(titleTextField) //, BorderLayout.CENTER);
            setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5))
        }

    private fun changeTabTitle() {
        val title = titleTextField.getText().trim()
        folderPanel.tabs.setTitle(title.ifEmpty { null })
    }

    override fun actionPerformed(e: ActionEvent) {
        dispose()

        if (e.getSource() === okButton) {
            changeTabTitle()
        }
    }

    companion object {
        /** Ensure the dialog width is at least 300  */
        private val MINIMUM_SIZE = Dimension(250, 0)
    }
}
