package com.mucommander.ui.viewer.text;

import com.mucommander.text.Translator;
import com.mucommander.ui.dialog.FocusDialog;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by trol on 17/09/14.
 */
public class ReplaceDialog extends FocusDialog implements ActionListener {

    public ReplaceDialog(Frame editorFrame) {
        super(editorFrame, Translator.get("text_editor.replace"), editorFrame);
    }

    @Override
    public void actionPerformed(ActionEvent e) {

    }
}
