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

import com.jidesoft.hints.ListDataIntelliHints;
import com.mucommander.cache.TextHistory;
import com.mucommander.ui.dialog.FocusDialog;
import org.fife.ui.rsyntaxtextarea.RSyntaxUtilities;
import org.fife.ui.rtextarea.SearchContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.event.EventListenerList;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Base class for all search dialogs (find and replace)
 *
 * @author Oleg Trifonov
 * Created on 21/06/16.
 */
public class AbstractSearchDialog extends FocusDialog implements ActionListener {

    private static Logger logger;


    /**
     * Listens for properties changing in the search context.
     */
    private class SearchContextListener implements PropertyChangeListener {

        public void propertyChange(PropertyChangeEvent e) {
            handleSearchContextPropertyChanged(e);
        }

    }



    protected SearchContext context;
    private SearchContextListener contextListener;

    // Conditions check boxes and the panel they go in.
    // This should be added in the actual layout of the search dialog.
    protected JCheckBox cbCaseSensitive;
    protected JCheckBox cbWholeWord;
    protected JCheckBox cbRegex;
    protected JPanel pnlSearchConditions;

    protected JTextField edtText;

    protected JButton btnCancel;
    protected JButton btnFind;

    protected JRadioButton rbUp;
    protected JRadioButton rbDown;
    protected JPanel pnlDirection;
    protected JLabel lblFind;

    /**
     * The "mark all" check box.
     */
    private JCheckBox markAllCheckBox;

    /**
     * Folks listening for events in this dialog.
     */
    private EventListenerList listenerList;



    AbstractSearchDialog(Frame owner, String title, Component locationRelativeComp) {
        super(owner, title, locationRelativeComp);
        init();
    }

    private void init() {
        // The user should set a shared instance between all subclass
        // instances, but to be safe we set individual ones.
        contextListener = new SearchContextListener();
        setSearchContext(createDefaultSearchContext());

        // Make a panel containing the option check boxes.
        pnlSearchConditions = new JPanel();
        pnlSearchConditions.setLayout(new BoxLayout(pnlSearchConditions, BoxLayout.Y_AXIS));
        cbCaseSensitive = createCheckBox(i18n("text_viewer.find.case_sensitive"));
        pnlSearchConditions.add(cbCaseSensitive);
        cbWholeWord = createCheckBox(i18n("text_viewer.find.whole_word"));
        pnlSearchConditions.add(cbWholeWord);
        cbRegex = createCheckBox(i18n("text_viewer.find.regexp"));
        pnlSearchConditions.add(cbRegex);

        // Initialize any text fields.
        edtText = new JTextField(20);
        edtText.addActionListener(this);
        List<String> history = TextHistory.getInstance().getList(TextHistory.Type.TEXT_SEARCH);
//        new AutoCompletion(findField, history).setStrict(false);
        edtText.setText("");
        new ListDataIntelliHints<>(edtText, history).setCaseSensitive(true);

        // Initialize other stuff.
        btnCancel = new JButton(i18n("cancel"));
        btnCancel.addActionListener(this);



        listenerList = new EventListenerList();

        // Make a panel containing the "search up/down" radio buttons.
        pnlDirection = new JPanel();
        pnlDirection.setLayout(new BoxLayout(pnlDirection, BoxLayout.LINE_AXIS));
        pnlDirection.setBorder(BorderFactory.createTitledBorder(i18n("text_viewer.find.direction")));

        ButtonGroup bg = new ButtonGroup();
        rbUp = new JRadioButton(i18n("text_viewer.find.up"), false);
        rbDown = new JRadioButton(i18n("text_viewer.find.down"), true);
        rbUp.addActionListener(this);
        rbDown.addActionListener(this);
        bg.add(rbUp);
        bg.add(rbDown);
        pnlDirection.add(rbUp);
        pnlDirection.add(rbDown);

        // Initialize the "mark all" button.
        markAllCheckBox = createCheckBox(i18n("text_viewer.find.mark_all"));

        // Rearrange the search conditions panel.
        pnlSearchConditions.removeAll();
        pnlSearchConditions.setLayout(new BorderLayout());
        JPanel temp = new JPanel();
        temp.setLayout(new BoxLayout(temp, BoxLayout.PAGE_AXIS));
        temp.add(cbCaseSensitive);
        temp.add(cbWholeWord);
        pnlSearchConditions.add(temp, BorderLayout.LINE_START);
        temp = new JPanel();
        temp.setLayout(new BoxLayout(temp, BoxLayout.PAGE_AXIS));
        temp.add(cbRegex);
        temp.add(markAllCheckBox);
        pnlSearchConditions.add(temp, BorderLayout.LINE_END);

        lblFind = new JLabel(i18n("text_viewer.find_button") + ":");

        btnFind = new JButton(i18n("text_viewer.find_button"));
        btnFind.addActionListener(this);
        btnFind.setDefaultCapable(true);
        btnFind.setEnabled(false);

        installKeyboardActions();

        fixHeight();
    }



    @Override
    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();

        if (src == cbCaseSensitive) {
            boolean matchCase = cbCaseSensitive.isSelected();
            context.setMatchCase(matchCase);
        } else if (src == cbWholeWord) {
            boolean wholeWord = cbWholeWord.isSelected();
            context.setWholeWord(wholeWord);
        } else if (src == cbRegex) {
            boolean useRegEx = cbRegex.isSelected();
            context.setRegularExpression(useRegEx);
        } else if (src == rbUp) {
            context.setSearchForward(false);
        } else if (src == rbDown) {
            context.setSearchForward(true);
        } else if (src == markAllCheckBox) {
            boolean checked = markAllCheckBox.isSelected();
            context.setMarkAll(checked);
        } else if (src == btnCancel) {
            dispose();
        } else if (src == btnFind || src == edtText) {
            // Add the item to the combo box's list, if it isn't already there.
            context.setSearchFor(getSearchString());
            fireSearchEvent(e); // Let parent application know
        }
    }

    private JCheckBox createCheckBox(String name) {
        JCheckBox cb = new JCheckBox(name);
        cb.addActionListener(this);
        return cb;
    }


    /**
     * Returns the default search context to use for this dialog.  Applications
     * that create new subclasses of this class can provide customized
     * search contexts here.
     *
     * @return The default search context.
     */
    protected SearchContext createDefaultSearchContext() {
        return new SearchContext();
    }



    /**
     * Makes the "Find text" field active.
     */
    protected void focusFindTextField() {
        edtText.requestFocusInWindow();
        edtText.selectAll();
    }




    /**
     * Returns the search context used by this dialog.
     *
     * @return The search context.
     * @see #setSearchContext(SearchContext)
     */
    public SearchContext getSearchContext() {
        return context;
    }


    /**
     * Returns the text to search for.
     *
     * @return The text the user wants to search for.
     */
    public String getSearchString() {
        return edtText.getText();
    }

    /**
     * Called when the regex checkbox is clicked (or its value is modified
     * via a change to the search context).  Subclasses can override
     * to add custom behavior, but should call the super implementation.
     */
    protected void handleRegExCheckBoxClicked() {
        handleToggleButtons();
        // "Content assist" support
        boolean b = cbRegex.isSelected();
    }


    /**
     * Called whenever a property in the search context is modified.
     * Subclasses should override if they listen for additional properties.
     *
     * @param e The property change event fired.
     */
    protected void handleSearchContextPropertyChanged(PropertyChangeEvent e) {

        // A property changed on the context itself.
        String prop = e.getPropertyName();

        if (SearchContext.PROPERTY_SEARCH_FORWARD.equals(prop)) {
            boolean newValue = (Boolean) e.getNewValue();
            JRadioButton button = newValue ? rbDown : rbUp;
            button.setSelected(true);
        } else if (SearchContext.PROPERTY_MARK_ALL.equals(prop)) {
            boolean newValue = (Boolean) e.getNewValue();
            markAllCheckBox.setSelected(newValue);
        } else if (SearchContext.PROPERTY_MATCH_CASE.equals(prop)) {
            boolean newValue = (Boolean) e.getNewValue();
            cbCaseSensitive.setSelected(newValue);
        } else if (SearchContext.PROPERTY_MATCH_WHOLE_WORD.equals(prop)) {
            boolean newValue = (Boolean) e.getNewValue();
            cbWholeWord.setSelected(newValue);
        } else if (SearchContext.PROPERTY_USE_REGEX.equals(prop)) {
            boolean newValue = (Boolean) e.getNewValue();
            cbRegex.setSelected(newValue);
            handleRegExCheckBoxClicked();
        } else if (SearchContext.PROPERTY_SEARCH_FOR.equals(prop)) {
            String newValue = (String)e.getNewValue();
            String oldValue = getSearchString();
            // Prevents IllegalStateExceptions
            if (!newValue.equals(oldValue)) {
                setSearchString(newValue);
            }
        }

    }


    /**
     * Returns whether any action-related buttons (Find Next, Replace, etc.)
     * should be enabled.  Subclasses can call this method when the "Find What"
     * or "Replace With" text fields are modified.  They can then
     * enable/disable any components as appropriate.
     *
     * @return Whether the buttons should be enabled.
     */
    protected FindReplaceButtonsEnableResult handleToggleButtons() {
        FindReplaceButtonsEnableResult er;

        //String text = getSearchString();
        String text = edtText.getText();
        if (text.isEmpty()) {
            er = new FindReplaceButtonsEnableResult(false, null);
        } else if (cbRegex.isSelected()) {
            try {
                Pattern.compile(text);
                er = new FindReplaceButtonsEnableResult(true, null);
            } catch (PatternSyntaxException pse) {
                er = new FindReplaceButtonsEnableResult(false, pse.getMessage());
            }
        } else {
            er = new FindReplaceButtonsEnableResult(true, null);
        }

        boolean enable = er.getEnable();

        btnFind.setEnabled(enable);

        // setBackground doesn't show up with XP Look and Feel!
        //findTextComboBox.setBackground(enable ?
        //		UIManager.getColor("ComboBox.background") : Color.PINK);
//        edtText.setForeground(enable ? UIManager.getColor("TextField.foreground") : UIUtil.getErrorTextForeground());

//        String tooltip = SearchUtil.getToolTip(er);
//        edtText.setToolTipText(tooltip); // Always set, even if null

        return er;

    }



    boolean matchesSearchFor(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        String searchFor = edtText.getText();
        if (searchFor != null && !searchFor.isEmpty()) {
            boolean matchCase = cbCaseSensitive.isSelected();
            if (cbRegex.isSelected()) {
                int flags = Pattern.MULTILINE; // '^' and '$' are done per line.
                flags = RSyntaxUtilities.getPatternFlags(matchCase, flags);
                Pattern pattern;
                try {
                    pattern = Pattern.compile(searchFor, flags);
                } catch (PatternSyntaxException pse) {
                    pse.printStackTrace(); // Never happens
                    return false;
                }
                return pattern.matcher(text).matches();
            } else {
                if (matchCase) {
                    return searchFor.equals(text);
                }
                return searchFor.equalsIgnoreCase(text);
            }
        }
        return false;
    }



    /**
     * Returns whether the characters on either side of
     * <code>substr(searchIn,startPos,startPos+searchStringLength)</code>
     * are whitespace.  While this isn't the best definition of "whole word",
     * it's the one we're going to use for now.
     */
    public static boolean isWholeWord(CharSequence searchIn, int offset, int len) {
        boolean wsBefore, wsAfter;

        try {
            wsBefore = Character.isWhitespace(searchIn.charAt(offset - 1));
        } catch (IndexOutOfBoundsException e) {
            wsBefore = true;
        }
        try {
            wsAfter  = Character.isWhitespace(searchIn.charAt(offset + len));
        } catch (IndexOutOfBoundsException e) {
            wsAfter = true;
        }
        return wsBefore && wsAfter;
    }


    /**
     * Initializes the UI in this tool bar from a search context.  This is
     * called whenever a new search context is installed on this tool bar
     * (which should practically be never).
     */
    protected void refreshUIFromContext() {
        if (cbCaseSensitive == null || markAllCheckBox == null) {
            return; // First time through, UI not realized yet
        }
        cbCaseSensitive.setSelected(context.getMatchCase());
        cbRegex.setSelected(context.isRegularExpression());
        cbWholeWord.setSelected(context.getWholeWord());

        markAllCheckBox.setSelected(context.getMarkAll());
        boolean searchForward = context.getSearchForward();
        rbUp.setSelected(!searchForward);
        rbDown.setSelected(searchForward);
    }


    /**
     * Overridden to ensure the "Find text" field gets focused.
     */
    @Override
    public void requestFocus() {
        super.requestFocus();
        focusFindTextField();
    }


    /**
     * Sets the search context for this dialog.  You'll usually want to call
     * this method for all search dialogs and give them the same search
     * context, so that their options (match case, etc.) stay in sync with one
     * another.
     *
     * @param context The new search context.  This cannot be <code>null</code>.
     * @see #getSearchContext()
     */
    public void setSearchContext(SearchContext context) {
        if (this.context != null) {
            this.context.removePropertyChangeListener(contextListener);
        }
        this.context = context;
        this.context.addPropertyChangeListener(contextListener);
        refreshUIFromContext();
    }


    /**
     * Sets the <code>java.lang.String</code> to search for.
     *
     * @param text The <code>String</code> to put into the search field.
     */
    public void setSearchString(String text) {
        edtText.setText(text);
        if (text != null) {
            edtText.select(0, text.length());
        }
    }


    /**
     * Adds a {@link SearchListener} to this dialog.  This listener will
     * be notified when find or replace operations are triggered.  For
     * example, for a Replace dialog, a listener will receive notification
     * when the user clicks "Find", "Replace", or "Replace All".
     *
     * @param l The listener to add.
     */
    public void addSearchListener(SearchListener l) {
        listenerList.add(SearchListener.class, l);
    }


    /**
     * Notifies all listeners that have registered interest for notification on
     * this event type. The event instance is lazily created using the
     * <code>event</code> parameter.
     *
     * @param event The <code>ActionEvent</code> object coming from a
     *        child component.
     */
    void fireSearchEvent(ActionEvent event) {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        SearchEvent e = null;
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == SearchListener.class) {
                // Lazily create the event:
                if (e == null) {
                    String command = event.getActionCommand();
                    SearchEvent.Type type = SearchEvent.Type.valueOf(command);
                    e = new SearchEvent(this, type, context);
                }
                ((SearchListener)listeners[i+1]).searchEvent(e);
            }
        }
    }



    /**
     * Adds extra keyboard actions for Find and Replace dialogs.
     */
    private void installKeyboardActions() {
        JRootPane rootPane = getRootPane();
        InputMap im = rootPane.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        ActionMap am = rootPane.getActionMap();

        int modifier = getToolkit().getMenuShortcutKeyMask();
        KeyStroke ctrlF = KeyStroke.getKeyStroke(KeyEvent.VK_F, modifier);
        im.put(ctrlF, "focusSearchForField");
        am.put("focusSearchForField", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                AbstractSearchDialog.this.requestFocus();
            }
        });
    }

    /**
     * Used by makeSpringCompactGrid.  This is ripped off directly from
     * <code>SpringUtilities.java</code> in the Sun Java Tutorial.
     *
     * @param parent The container whose layout must be an instance of
     *        <code>SpringLayout</code>.
     * @return The spring constraints for the specified component contained
     *         in <code>parent</code>.
     */
    private static SpringLayout.Constraints getConstraintsForCell(int row, int col, Container parent, int cols) {
        SpringLayout layout = (SpringLayout) parent.getLayout();
        Component c = parent.getComponent(row * cols + col);
        return layout.getConstraints(c);
    }


    /**
     * This method is ripped off from <code>SpringUtilities.java</code> found
     * on Sun's Java Tutorial pages.  It takes a component whose layout is
     * <code>SpringLayout</code> and organizes the components it contains into
     * a nice grid.
     * Aligns the first <code>rows</code> * <code>cols</code> components of
     * <code>parent</code> in a grid. Each component in a column is as wide as
     * the maximum preferred width of the components in that column; height is
     * similarly determined for each row.  The parent is made just big enough
     * to fit them all.
     *
     * @param parent The container whose layout is <code>SpringLayout</code>.
     * @param rows The number of rows of components to make in the container.
     * @param cols The number of columns of components to make.
     * @param initialX The x-location to start the grid at.
     * @param initialY The y-location to start the grid at.
     * @param xPad The x-padding between cells.
     * @param yPad The y-padding between cells.
     */
    protected static void makeSpringCompactGrid(Container parent, int rows, int cols, int initialX, int initialY, int xPad, int yPad) {
        SpringLayout layout;
        try {
            layout = (SpringLayout)parent.getLayout();
        } catch (ClassCastException cce) {
            getLogger().error("The first argument to makeCompactGrid must use SpringLayout.");
            return;
        }

        // Align all cells in each column and make them the same width.
        Spring x = Spring.constant(initialX);
        for (int c = 0; c < cols; c++) {
            Spring width = Spring.constant(0);
            for (int r = 0; r < rows; r++) {
                width = Spring.max(width, getConstraintsForCell(r, c, parent, cols).getWidth());
            }
            for (int r = 0; r < rows; r++) {
                SpringLayout.Constraints constraints = getConstraintsForCell(r, c, parent, cols);
                constraints.setX(x);
                constraints.setWidth(width);
            }
            x = Spring.sum(x, Spring.sum(width, Spring.constant(xPad)));
        }

        // Align all cells in each row and make them the same height.
        Spring y = Spring.constant(initialY);
        for (int r = 0; r < rows; r++) {
            Spring height = Spring.constant(0);
            for (int c = 0; c < cols; c++) {
                height = Spring.max(height, getConstraintsForCell(r, c, parent, cols).getHeight());
            }
            for (int c = 0; c < cols; c++) {
                SpringLayout.Constraints constraints = getConstraintsForCell(r, c, parent, cols);
                constraints.setY(y);
                constraints.setHeight(height);
            }
            y = Spring.sum(y, Spring.sum(height, Spring.constant(yPad)));
        }

        // Set the parent's size.
        SpringLayout.Constraints pCons = layout.getConstraints(parent);
        pCons.setConstraint(SpringLayout.SOUTH, y);
        pCons.setConstraint(SpringLayout.EAST, x);
    }

    private static Logger getLogger() {
        if (logger == null) {
            logger = LoggerFactory.getLogger(AbstractSearchDialog.class);
        }
        return logger;
    }



}
