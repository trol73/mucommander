package com.mucommander.ui.viewer.audio;

import javax.swing.JButton;
import javax.swing.JPanel;

/**
 * Created by trol on 14/03/14.
 */
public class AudioPlayer extends JPanel {
    private JButton btnPrev;
    private JButton btnPlay;
    private JButton btnStop;
    private JButton btnPause;
    private JButton btnNext;

    public AudioPlayer() {
        super();

        btnPrev = new JButton("<<");
        btnPlay = new JButton(">");
        btnStop = new JButton("x");
        btnPause = new JButton("||");
        btnNext = new JButton(">>");

        add(btnPrev);
        add(btnPlay);
        add(btnStop);
        add(btnPause);
        add(btnNext);
    }
}
