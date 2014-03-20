package com.mucommander.ui.viewer.audio;

import org.fife.ui.StatusBarPanel;

import javax.swing.JLabel;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;

/**
 * Created by trol on 14/03/14.
 */
public class StatusBar extends org.fife.ui.StatusBar {
    private StatusBarPanel panel;
    private JLabel lbl;

    public StatusBar() {
        super("");

        lbl = new JLabel();
        panel = new StatusBarPanel(new BorderLayout(), lbl);

        // Make the layout such that different items can be different sizes.
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;

        c.weightx = 0.0;
        addStatusBarComponent(panel, c);
    }

    public void set(String s) {
        lbl.setText(s);
    }

}
