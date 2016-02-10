package com.mucommander.ui.tools;

import com.mucommander.text.Translator;
import com.mucommander.ui.dialog.FocusDialog;

import java.awt.Frame;

/**
 * @author Oleg Trifonov
 * Created on 09/02/16.
 */
public class ToolsSetupDialog extends FocusDialog {
    public ToolsSetupDialog(Frame owner) {
        super(owner, Translator.get("tools_setup_dialog.title"), null);
    }
}
