package com.mucommander.ui.viewer.hex;

import com.mucommander.text.Translator;
import com.mucommander.ui.dialog.DialogToolkit;
import com.mucommander.ui.dialog.FocusDialog;
import ru.trolsoft.ui.InputField;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.text.AbstractDocument;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by trol on 03/04/14.
 */
public abstract class GotoDialog extends FocusDialog implements ActionListener {

    private final long maxOffset;

    private InputField edtOffset;

    /** The 'OK' button */
    private JButton btnOk;


    public GotoDialog(Frame owner, long maxOffset) {
        super(owner, Translator.get("hex_viewer.goto"), owner);
        this.maxOffset = maxOffset;
        Container contentPane = getContentPane();
        contentPane.add(new JLabel(Translator.get("hex_viewer.goto.offset")+":"), BorderLayout.NORTH);

        edtOffset = new InputField(16, InputField.FilterType.HEX_LONG) {
            @Override
            public void onChange() {
                boolean enabled = !edtOffset.isEmpty() && edtOffset.getValue() <= GotoDialog.this.maxOffset;
                btnOk.setEnabled(enabled);
            }
        };
        edtOffset.setText("1");
        edtOffset.addActionListener(this);
        contentPane.add(edtOffset, BorderLayout.CENTER);

        btnOk = new JButton(Translator.get("ok"));
        JButton cancelButton = new JButton(Translator.get("cancel"));
        contentPane.add(DialogToolkit.createOKCancelPanel(btnOk, cancelButton, getRootPane(), this), BorderLayout.SOUTH);

        // The text field will receive initial focus
        setInitialFocusComponent(edtOffset);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        if ((source == btnOk || source == edtOffset) && btnOk.isEnabled()) {
            doGoto(edtOffset.getValue());
            dispose();
        }
    }

    abstract protected void doGoto(long value);
}
