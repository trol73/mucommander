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

/**
 * @author Oleg Trifonov
 * Created on 21/06/16.
 */
public class FindDialog extends AbstractSearchDialog {

    /**
     * Our search listener, cached so we can grab its selected text easily.
     */
    protected SearchListener searchListener;


    public FindDialog(Frame owner, SearchListener listener) {
        super(owner, Translator.get("text_viewer.find"), null);

        this.searchListener = listener;

        btnFind.setActionCommand(SearchEvent.Type.FIND.toString());
        edtText.setActionCommand(SearchEvent.Type.FIND.toString());

        // Make a panel containing the "Find" edit box.
        JPanel enterTextPane = new JPanel(new SpringLayout());
        enterTextPane.setBorder(BorderFactory.createEmptyBorder(0,5,5,5));
        edtText.getDocument().addDocumentListener(new FindDocumentListener());
        JPanel temp = new JPanel(new BorderLayout());
        temp.add(edtText, BorderLayout.CENTER);
        enterTextPane.add(lblFind);
        enterTextPane.add(temp);

        makeSpringCompactGrid(enterTextPane, 1, 2,	//rows, cols
                0,0,		//initX, initY
                6, 6);	//xPad, yPad

        // Make a panel containing the inherited search direction radio buttons and the inherited search options.
        JPanel bottomPanel = new JPanel(new BorderLayout());
        temp = new JPanel(new BorderLayout());
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        temp.add(pnlSearchConditions, BorderLayout.LINE_START);
        temp.add(pnlDirection);
        bottomPanel.add(temp, BorderLayout.LINE_START);

        // Now, make a panel containing all the above stuff.
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.add(enterTextPane);
        leftPanel.add(bottomPanel);

        // Make a panel containing the action buttons.
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(2, 1, 5, 5));
        buttonPanel.add(btnFind);
        buttonPanel.add(btnCancel);
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BorderLayout());
        rightPanel.add(buttonPanel, BorderLayout.NORTH);

        // Put everything into a neat little package.
        JPanel contentPane = new JPanel(new BorderLayout());
        contentPane.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 5));
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
        super.actionPerformed(e);
        Object src = e.getSource();
        if (src == btnFind || src == edtText) {
            dispose();
        }
    }

    /**
     * Overrides <code>JDialog</code>'s <code>setVisible</code> method; decides
     * whether or not buttons are enabled.
     *
     * @param visible Whether or not the dialog should be visible.
     */
    @Override
    public void setVisible(boolean visible) {
        if (visible) {
            // Select text entered in the UI
            String text = searchListener.getSelectedText();
            if (text != null) {
                edtText.setText(text);
            }

            String selectedItem = edtText.getText();
            btnFind.setEnabled(selectedItem != null && !selectedItem.isEmpty());
            super.setVisible(true);
            focusFindTextField();
        } else {
            super.setVisible(false);
        }

    }

    /**
     * Listens for changes in the text field (find search field).
     */
    private class FindDocumentListener implements DocumentListener {

        public void insertUpdate(DocumentEvent e) {
            handleToggleButtons();
        }

        public void removeUpdate(DocumentEvent e) {
            if (edtText.getDocument().getLength() == 0) {
                btnFind.setEnabled(false);
            } else {
                handleToggleButtons();
            }
        }

        public void changedUpdate(DocumentEvent e) {
        }

    }


}
