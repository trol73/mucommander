/*
 * This file is part of trolCommander, http://www.trolsoft.ru/en/soft/trolcommander
 * Copyright (C) 2013-2016 Oleg Trifonov
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
package com.mucommander.ui.viewer.text.search;

import com.mucommander.text.Translator;
import org.fife.ui.rtextarea.SearchContext;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;

/**
 * @author Oleg Trifonov
 * Created on 21/06/16.
 */
public class ReplaceDialog extends AbstractSearchDialog {

    private JButton btnReplace;
    private JButton btnReplaceAll;
    private JTextField edtReplace;


    /**
     * Our search listener, cached so we can grab its selected text easily.
     */
    protected SearchListener searchListener;


    public ReplaceDialog(Frame owner, SearchListener listener) {
        super(owner, Translator.get("text_editor.replace"), null);

        this.searchListener = listener;

        btnFind.setActionCommand(SearchEvent.Type.FIND.toString());
        edtText.setActionCommand(SearchEvent.Type.FIND.toString());

        ReplaceDocumentListener replaceDocumentListener = new ReplaceDocumentListener();

        // Create a panel for the "Find what" and "Replace with" text fields.
        JPanel searchPanel = new JPanel(new SpringLayout());

        edtText.getDocument().addDocumentListener(replaceDocumentListener);

        // Create the "Replace with" text field.
        edtReplace = new JTextField(20);
        edtReplace.getDocument().addDocumentListener(replaceDocumentListener);

        // Create the "Replace with" label.
        JLabel lblReplace = new JLabel(Translator.get("text_editor.replace_with") + ":");

        JPanel temp = new JPanel(new BorderLayout());
        temp.add(edtReplace);
        temp.add(edtText, BorderLayout.CENTER);
        JPanel temp2 = new JPanel(new BorderLayout());
        temp2.add(edtReplace);
        temp2.add(edtReplace, BorderLayout.CENTER);

        searchPanel.add(lblFind);
        searchPanel.add(temp);
        searchPanel.add(lblReplace);
        searchPanel.add(temp2);

        makeSpringCompactGrid(searchPanel, 2, 2,	//rows, cols
                5, 0,		//initX, initY
                6, 6);	//xPad, yPad

        // Make a panel containing the inherited search direction radio
        // buttons and the inherited search options.
        JPanel bottomPanel = new JPanel(new BorderLayout());
        temp = new JPanel(new BorderLayout());
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        temp.add(pnlSearchConditions, BorderLayout.LINE_START);
        temp.add(pnlDirection);
        bottomPanel.add(temp, BorderLayout.LINE_START);

        // Now, make a panel containing all the above stuff.
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.add(searchPanel);
        leftPanel.add(bottomPanel);

        // Make a panel containing the action buttons.
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(4, 1, 5, 5));
        btnReplace = new JButton(Translator.get("text_editor.replace_button"));
        btnReplace.setActionCommand(SearchEvent.Type.REPLACE.name());
        btnReplace.addActionListener(this);
        btnReplace.setEnabled(false);
        btnReplaceAll = new JButton(Translator.get("text_editor.replace_all"));
        btnReplaceAll.setActionCommand(SearchEvent.Type.REPLACE_ALL.name());
        btnReplaceAll.addActionListener(this);
        btnReplaceAll.setEnabled(false);
        buttonPanel.add(btnFind);
        buttonPanel.add(btnReplace);
        buttonPanel.add(btnReplaceAll);
        buttonPanel.add(btnCancel);
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.add(buttonPanel, BorderLayout.NORTH);

        // Put it all together!
        JPanel contentPane = new JPanel(new BorderLayout());
        contentPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 5));
        contentPane.add(leftPanel);
        contentPane.add(rightPanel, BorderLayout.LINE_END);
        getContentPane().add(contentPane);
        getRootPane().setDefaultButton(btnFind);
        setLocationRelativeTo(getParent());

        setSearchContext(new SearchContext());
        addSearchListener(listener);
    }


    @Override
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        if (SearchEvent.Type.REPLACE.name().equals(command) || SearchEvent.Type.REPLACE_ALL.name().equals(command)) {
            context.setSearchFor(getSearchString());
            context.setReplaceWith(edtReplace.getText());
            fireSearchEvent(e); // Let parent application know
        } else {
            super.actionPerformed(e);
            if (SearchEvent.Type.FIND.name().equals(command)) {
                handleToggleButtons(); // Replace button could toggle state
            }
        }
    }


    @Override
    protected void handleSearchContextPropertyChanged(PropertyChangeEvent e) {
        String prop = e.getPropertyName();

        if (SearchContext.PROPERTY_REPLACE_WITH.equals(prop)) {
            String newValue = (String)e.getNewValue();
            if (newValue == null) {
                newValue = "";
            }
            String oldValue = edtReplace.getText();
            // Prevents IllegalStateExceptions
            if (!newValue.equals(oldValue)) {
                edtReplace.setText(newValue);
            }
        } else {
            super.handleSearchContextPropertyChanged(e);
        }

    }


    @Override
    protected FindReplaceButtonsEnableResult handleToggleButtons() {
        FindReplaceButtonsEnableResult er = super.handleToggleButtons();
        boolean shouldReplace = er.getEnable();
        btnReplaceAll.setEnabled(shouldReplace);

        // "Replace" is only enabled if text to search for is selected in the UI
        if (shouldReplace) {
            String text = searchListener.getSelectedText();
            shouldReplace = matchesSearchFor(text);
        }
        btnReplace.setEnabled(shouldReplace);

        return er;
    }

    /**
     * Listens for changes in the text field (find search field).
     */
    private class ReplaceDocumentListener implements DocumentListener {

        public void insertUpdate(DocumentEvent e) {
            if (e.getDocument().equals(edtText.getDocument())) {
                handleToggleButtons();
            }
        }

        public void removeUpdate(DocumentEvent e) {
            if (e.getDocument().equals(edtText.getDocument()) && e.getDocument().getLength() == 0) {
                btnFind.setEnabled(false);
                btnReplace.setEnabled(false);
                btnReplaceAll.setEnabled(false);
            } else {
                handleToggleButtons();
            }
        }

        public void changedUpdate(DocumentEvent e) {
        }
    }




}
