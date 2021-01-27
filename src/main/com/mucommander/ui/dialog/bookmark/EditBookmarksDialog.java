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

package com.mucommander.ui.dialog.bookmark;

import com.mucommander.bookmark.Bookmark;
import com.mucommander.bookmark.BookmarkManager;
import com.mucommander.commons.collections.AlteredVector;
import com.mucommander.ui.action.ActionProperties;
import com.mucommander.ui.action.impl.EditBookmarksAction;
import com.mucommander.ui.dialog.FocusDialog;
import com.mucommander.ui.helper.MnemonicHelper;
import com.mucommander.ui.layout.XAlignedComponentPanel;
import com.mucommander.ui.layout.XBoxPanel;
import com.mucommander.ui.layout.YBoxPanel;
import com.mucommander.ui.list.DynamicList;
import com.mucommander.ui.list.SortableListPanel;
import com.mucommander.ui.main.MainFrame;
import com.mucommander.ui.text.FilePathField;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.Document;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


/**
 * This dialog contains a list of all bookmarks and allows the user to edit, remove, duplicate, go to and reorder them.
 *
 * <p>If the contents of this list is modified, bookmarks will be saved to disk when this dialog is disposed.
 *
 * @author Maxence Bernard
 */
public class EditBookmarksDialog extends FocusDialog implements ActionListener, ListSelectionListener, DocumentListener {

    private final MainFrame mainFrame;

    private final JButton btnNew;
    private final JButton btnDuplicate;
    private final JButton btnRemove;
    private final JButton btnGoto;
    private final JButton btnClose;

    private final JTextField edtName;
    private final JLabel locationLabel;
    private final JTextField edtLocation;
    private final BookmarkParentComboBox cbParent;
    // separatorNoticePrefix is required to keep the size of the 1st column
    private final JLabel separatorNoticePrefix;
    private final JLabel separatorNoticeLabel;

    private final AlteredVector<Bookmark> bookmarks;
    private final DynamicList<Bookmark> bookmarkList;

    private int currentListIndex;
    private Bookmark currentBookmarkSave;

    private boolean ignoreDocumentListenerEvents;

    // Dialog's size has to be at least 400x300
    private final static Dimension MINIMUM_DIALOG_DIMENSION = new Dimension(440,330);	

    // Dialog's size has to be at most 600x400
    private final static Dimension MAXIMUM_DIALOG_DIMENSION = new Dimension(600,400);



    public EditBookmarksDialog(MainFrame mainFrame) {
        super(mainFrame, ActionProperties.getActionLabel(EditBookmarksAction.Descriptor.ACTION_ID), mainFrame);

        this.mainFrame = mainFrame;

        Container contentPane = getContentPane();

        // Retrieve bookmarks list
        this.bookmarks = BookmarkManager.getBookmarks();

        // Temporarily suspend bookmark change events, otherwise an event would be fired for each character
        // typed in the name / location fields. Events will be resumed when this dialog is disposed
        BookmarkManager.setFireEvents(false);

        // create the sortable bookmarks list panel
        SortableListPanel<Bookmark> listPanel = new SortableListPanel<>(bookmarks);
        this.bookmarkList = listPanel.getDynamicList();

        contentPane.add(listPanel, BorderLayout.CENTER);

        // Text fields panel
        XAlignedComponentPanel compPanel = new XAlignedComponentPanel();

        // Add bookmark name field
        this.edtName = new JTextField();
        edtName.getDocument().addDocumentListener(this);
        compPanel.addRow(i18n("name")+":", edtName, 5);

        // create a path field with auto-completion capabilities
        this.edtLocation = new FilePathField();
        this.locationLabel = new JLabel(i18n("location")+":");
        edtLocation.getDocument().addDocumentListener(this);
        compPanel.addRow(locationLabel, edtLocation, 10);

        this.cbParent = new BookmarkParentComboBox();
        cbParent.addActionListener(this);
        compPanel.addRow(i18n("parent")+":", cbParent, 5);

        this.separatorNoticePrefix = new JLabel();
        this.separatorNoticeLabel = new JLabel(" "+i18n("edit_bookmarks_dialog.is_separator"));
        compPanel.addRow(separatorNoticePrefix, separatorNoticeLabel, 10);

        YBoxPanel yPanel = new YBoxPanel(10);
        yPanel.add(compPanel);

        // Add buttons: 'remove', 'move up' and 'move down' buttons are enabled
        // only if there is at least one bookmark in the table
        XBoxPanel buttonsPanel = new XBoxPanel();
        JPanel buttonGroupPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        MnemonicHelper mnemonicHelper = new MnemonicHelper();

        // New bookmark button
        btnNew = new JButton(i18n("edit_bookmarks_dialog.new"));
        btnNew.setMnemonic(mnemonicHelper.getMnemonic(btnNew));
        btnNew.addActionListener(this);
        buttonGroupPanel.add(btnNew);

        // Duplicate bookmark button
        btnDuplicate = new JButton(i18n("duplicate"));
        btnDuplicate.setMnemonic(mnemonicHelper.getMnemonic(btnDuplicate));
        btnDuplicate.addActionListener(this);
        buttonGroupPanel.add(btnDuplicate);

        // Remove bookmark button
        btnRemove = new JButton(bookmarkList.getRemoveAction());
        btnRemove.setMnemonic(mnemonicHelper.getMnemonic(btnRemove));
        buttonGroupPanel.add(btnRemove);

        // Go to bookmark button
        btnGoto = new JButton(i18n("go_to"));
        btnGoto.setMnemonic(mnemonicHelper.getMnemonic(btnGoto));
        btnGoto.addActionListener(this);
        buttonGroupPanel.add(btnGoto);

        buttonsPanel.add(buttonGroupPanel);

        // Button that closes the window
        btnClose = new JButton(i18n("close"));
        btnClose.setMnemonic(mnemonicHelper.getMnemonic(btnClose));
        btnClose.addActionListener(this);

        buttonsPanel.add(Box.createHorizontalGlue());
        buttonsPanel.add(btnClose);

        yPanel.add(buttonsPanel);

        contentPane.add(yPanel, BorderLayout.SOUTH);

        // Set initial text components and buttons' enabled state
        updateComponents();

        // Listen to selection changes to reflect the change
        bookmarkList.addListSelectionListener(this);

        // table will receive initial focus
        setInitialFocusComponent(bookmarkList);
		
        // Selects OK when enter is pressed
        getRootPane().setDefaultButton(btnClose);

        // Packs dialog
        setMinimumSize(MINIMUM_DIALOG_DIMENSION);
        setMaximumSize(MAXIMUM_DIALOG_DIMENSION);
		
        // Call dispose() on close and write bookmarks file
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        
        showDialog();
    }


    /**
     * Updates text fields and buttons' enabled state based on the current selection. Should be called
     * whenever the list selection has changed.
     */
    private void updateComponents() {
        String nameValue = null;
        String locationValue = null;
        String parentValue = null;

        boolean componentsEnabled = false;

        if (!bookmarkList.isSelectionEmpty() && !bookmarks.isEmpty()) {
            componentsEnabled = true;

            Bookmark b = bookmarkList.getSelectedValue();
            nameValue = b.getName();
            locationValue = b.getLocation();
            parentValue = b.getParent();
        }

        // Ignore text field events while setting values
        ignoreDocumentListenerEvents = true;

        edtName.setText(nameValue);
        edtName.setEnabled(componentsEnabled);

        edtLocation.setText(locationValue);
        edtLocation.setEnabled(componentsEnabled);

        cbParent.setChildName(nameValue);
        cbParent.setSelectedParent(parentValue);

        ignoreDocumentListenerEvents = false;

        btnGoto.setEnabled(componentsEnabled && locationValue != null && !locationValue.isEmpty());
        btnDuplicate.setEnabled(componentsEnabled);
        btnRemove.setEnabled(componentsEnabled);

        updateSeparatorNoticeVisibility();
    }

    /**
     * Updates visibility of `The specified name defines a separator` notice
     */
    private void updateSeparatorNoticeVisibility() {
        String nameFieldValue = edtName.getText();
        boolean isSeparator = nameFieldValue != null && nameFieldValue.equals(BookmarkManager.BOOKMARKS_SEPARATOR);
        if (separatorNoticeLabel.isVisible() == isSeparator) {
            return;
        }

        separatorNoticePrefix.setVisible(isSeparator);
        separatorNoticeLabel.setVisible(isSeparator);
        if (isSeparator) {
            // inherit the preferred sizes of locationXXXX controls,
            // to keep the layout still
            separatorNoticePrefix.setPreferredSize(locationLabel.getPreferredSize());
            separatorNoticeLabel.setPreferredSize(edtLocation.getPreferredSize());
        }
        locationLabel.setVisible(!isSeparator);
        edtLocation.setVisible(!isSeparator);
    }


    /**
     * Called whenever a value in one of the text fields has been modified, and updates the current Bookmark instance to
     * use the new value.
     *
     * @param sourceDocument the javax.swing.text.Document of the JTextField that was modified
     */
    private void modifyBookmark(Document sourceDocument) {
        if (ignoreDocumentListenerEvents || bookmarks.isEmpty()) {
            return;
        }

        int selectedIndex = bookmarkList.getSelectedIndex();

        // Make sure that the selected index is not out of bounds
        if (!bookmarkList.isIndexValid(selectedIndex)) {
            return;
        }

        Bookmark selectedBookmark = bookmarks.elementAt(selectedIndex);

        if (currentBookmarkSave == null) {
            // create a clone of the current bookmark in order to cancel any modifications made to it if the dialog
            // is cancelled.
            try {
                currentBookmarkSave = (Bookmark)selectedBookmark.clone();
            } catch(CloneNotSupportedException ignored) {}

            this.currentListIndex = selectedIndex;
        }

        // Update name
        if (sourceDocument == edtName.getDocument()) {
            String name = edtName.getText();
            if (name.trim().isEmpty()) {
                name = getFreeNameVariation(i18n("untitled"));
            }

            selectedBookmark.setName(name);
            bookmarkList.itemModified(selectedIndex, false);
            updateSeparatorNoticeVisibility();
        }
        // Update location
        else if (sourceDocument == edtLocation.getDocument()) {
            String location = edtLocation.getText();
            selectedBookmark.setLocation(location);
            bookmarkList.itemModified(selectedIndex, false);
            btnGoto.setEnabled(location != null && !location.isEmpty());
        } else {
            selectedBookmark.setParent(cbParent.getSelectedParent());
            bookmarkList.itemModified(selectedIndex, false);
        }
    }


    /**
     * Returns the first variation of the given name that is not already used by another bookmark, e.g. :
     * <br>"music" -> "music (2)" if there already is bookmark with the "music" name
     * <br>"music (2)" -> "music (3)" and so on...
     */
    private String getFreeNameVariation(String name) {
        if (!containsName(name)) {
            return name;
        }

        int len = name.length();
        char c;
        int num = 2;
        if (len>4 && name.charAt(len-1)==')'
                    && (c=name.charAt(len-2))>='0' && c<='9'
                    && name.charAt(len-3)=='('
                    && name.charAt(len-4)==' ')
        {
            num = (c-'0')+1;
            name = name.substring(0, len-4);
        }


        String newName;
        while (containsName(newName=(name+" ("+num+++")")));

        return newName;
    }


    /**
     * Returns true if the bookmarks list contains a bookmark that has the specified name.
     */
    private boolean containsName(String name) {
        int nbBookmarks = bookmarks.size();
        for (int i = 0; i < nbBookmarks; i++) {
            if (bookmarks.elementAt(i).getName().equals(name)) {
                return true;
            }
        }

        return false;
    }



    /**
     * Overrides dispose() to write bookmarks to disk (if needed).
     */
    @Override
    public void dispose() {
        super.dispose();

        // Rollback current bookmark's modifications if the dialog was cancelled
        if (currentBookmarkSave!=null) {
            bookmarks.setElementAt(currentBookmarkSave, currentListIndex);
            currentBookmarkSave = null;
        }

        // Resume bookmark change events
        BookmarkManager.setFireEvents(true);

        // Write bookmarks file to disk, only if changes were made to bookmarks
        try {
            BookmarkManager.writeBookmarks(false);
        } catch(Exception e) {
            // We should probably pop an error here.
            e.printStackTrace();
        }
    }


	@Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
		
        // Dispose the dialog (bookmarks save is performed in dispose())
        if (source == btnClose)  {
            // Do not rollback current bookmark's modifications on dispose()
            currentBookmarkSave = null;

            dispose();
        } else if (source == btnNew || source == btnDuplicate) {
            addBookmark(source == btnDuplicate);
        } else if (source== btnGoto) {
            // Dispose dialog first
            dispose();
            // Change active panel's folder
            mainFrame.getActivePanel().tryChangeCurrentFolder((bookmarkList.getSelectedValue()).getLocation());
        } else if (source == cbParent) {
            modifyBookmark(null);
        }
    }


    private void addBookmark(boolean duplicate) {
        Bookmark newBookmark;
        if (duplicate) {
            try {
                Bookmark currentBookmark = bookmarkList.getSelectedValue();
                newBookmark = (Bookmark)currentBookmark.clone();
                newBookmark.setName(getFreeNameVariation(currentBookmark.getName()));
            } catch (CloneNotSupportedException ex) {
                return;
            }
        } else {
            newBookmark = new Bookmark(getFreeNameVariation(i18n("untitled")), "", null);
        }

        bookmarks.add(newBookmark);

        int newBookmarkIndex = bookmarks.size()-1;
        bookmarkList.selectAndScroll(newBookmarkIndex);

        updateComponents();

        edtName.selectAll();
        edtName.requestFocus();
    }

	@Override
    public void valueChanged(ListSelectionEvent e) {
        if (e.getValueIsAdjusting()) {
            return;
        }

        // Reset current bookmark's save
        currentBookmarkSave = null;
//        currentListIndex = bookmarkList.getSelectedIndex();

        // Update components to reflect the new selection
        updateComponents();
    }


    @Override
    public void changedUpdate(DocumentEvent e) {
        modifyBookmark(e.getDocument());
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        modifyBookmark(e.getDocument());
    }

    public void removeUpdate(DocumentEvent e) {
        modifyBookmark(e.getDocument());
    }
}
