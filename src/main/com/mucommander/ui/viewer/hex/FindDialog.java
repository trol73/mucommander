package com.mucommander.ui.viewer.hex;

/**
 * Created by trol on 03/04/14.
 */

import com.jidesoft.hints.ListDataIntelliHints;
import com.jidesoft.swing.AutoCompletion;
import com.mucommander.cache.TextHistory;
import com.mucommander.text.Translator;
import com.mucommander.ui.dialog.DialogToolkit;
import com.mucommander.ui.dialog.FocusDialog;
import com.mucommander.ui.layout.XAlignedComponentPanel;
import ru.trolsoft.ui.InputField;

import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

/**
 * This dialog allows the user to enter a string or hex value to be searched for in the hex editor.
 *
 * @author Oleg Trifonov
 */
public abstract class FindDialog extends FocusDialog implements ActionListener {

    /** The text field where a search dump can be entered */
    private InputField hexField;

    /** The text field where a search string can be entered */
    private InputField textField;

    /** The 'OK' button */
    private JButton okButton;


    /**
     * Creates a new FindDialog and shows it to the screen.
     *
     * @param frame the parent frame
     */
    public FindDialog(JFrame frame, String encoding) {
        super(frame, Translator.get("hex_viewer.find"), frame);

        Container contentPane = getContentPane();

        // Text fields panel
        XAlignedComponentPanel compPanel = new XAlignedComponentPanel();

        textField = new InputField(60, InputField.FilterType.ANY_TEXT);
        textField.addActionListener(this);
        compPanel.addRow(Translator.get("hex_view.text")+":", textField, 5);
        List<String> historyText = TextHistory.getInstance().getList(TextHistory.Type.TEXT_SEARCH);
//        new AutoCompletion(textField, historyText).setStrict(false);
        new ListDataIntelliHints<String>(textField, historyText).setCaseSensitive(true);
        textField.setText("");


        hexField = new InputField(60, InputField.FilterType.HEX_DUMP);
        hexField.addActionListener(this);
        compPanel.addRow(Translator.get("hex_viewer.hex") + ":", hexField, 10);
        List<String> historyHex = TextHistory.getInstance().getList(TextHistory.Type.HEX_DATA_SEARCH);
//        new AutoCompletion(hexField, historyHex).setStrict(false);
        new ListDataIntelliHints<String>(hexField, historyHex).setCaseSensitive(false);
        hexField.setText("");

        textField.assignField(hexField);
        hexField.assignField(textField);
        textField.setTextEncoding(encoding);
        hexField.setTextEncoding(encoding);

        contentPane.add(compPanel, BorderLayout.CENTER);


        okButton = new JButton(Translator.get("ok"));
        JButton cancelButton = new JButton(Translator.get("cancel"));
        contentPane.add(DialogToolkit.createOKCancelPanel(okButton, cancelButton, getRootPane(), this), BorderLayout.SOUTH);

        // The text field will receive initial focus
        setInitialFocusComponent(textField);
    }


    public byte[] getSearchBytes() {
        return hexField.getBytes();
    }


    ///////////////////////////////////
    // ActionListener implementation //
    ///////////////////////////////////

    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        dispose();
        doSearch(source == okButton || source == hexField || source == textField ? getSearchBytes() : null);
    }

    @Override
    protected void saveState() {
        super.saveState();
        TextHistory.getInstance().add(TextHistory.Type.TEXT_SEARCH, textField.getText(), true);
        TextHistory.getInstance().add(TextHistory.Type.HEX_DATA_SEARCH, hexField.getText(), true);
    }

    public void setSearchBytes(byte[] searchBytes) {
        hexField.setBytes(searchBytes);
    }


        /**
         * Search operation listener
         * @param bytes nul if the dialog was cancelled
         */
    protected abstract void doSearch(byte[] bytes);

}
