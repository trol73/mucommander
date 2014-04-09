package com.mucommander.ui.viewer.hex;

/**
 * Created by trol on 03/04/14.
 */

import com.mucommander.text.Translator;
import com.mucommander.ui.dialog.DialogToolkit;
import com.mucommander.ui.dialog.FocusDialog;
import com.mucommander.ui.layout.XAlignedComponentPanel;
import ru.trolsoft.hexeditor.ru.trolsoft.ui.HexTextField;

import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * This dialog allows the user to enter a string or hex value to be searched for in the hex editor.
 *
 * @author Oleg Trifonov
 */
public abstract class FindDialog extends FocusDialog implements ActionListener {

    private static String text;

    /** The text field where a search dump can be entered */
    private HexTextField hexField;

    /** The text field where a search string can be entered */
    private HexTextField textField;

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
        //contentPane.add(new JLabel(Translator.get("hex_viewer.find")+":"), BorderLayout.NORTH);

        // Text fields panel
        XAlignedComponentPanel compPanel = new XAlignedComponentPanel();

        textField = new HexTextField(60);
        //textField.setText(text);
        textField.addActionListener(this);
        textField.setFilterType(HexTextField.FilterType.ANY_TEXT);
        compPanel.addRow(Translator.get("hex_view.text")+":", textField, 5);

        hexField = new HexTextField(60);
//        hexField.setText(text);
        hexField.setFilterType(HexTextField.FilterType.HEX);
        hexField.addActionListener(this);
        compPanel.addRow(Translator.get("hex_viewer.hex")+":", hexField, 10);

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


    /**
     * Returns the search string entered by the user in the text field.
     *
     * @return the search string entered by the user in the text field
     */
    public String getSearchString() {
        return hexField.getText();
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
        text = hexField.getText();
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
