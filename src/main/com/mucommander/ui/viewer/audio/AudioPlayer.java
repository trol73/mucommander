/*
 * This file is part of trolCommander, http://www.trolsoft.ru/en/soft/trolcommander
 * Copyright (C) 2013-2016 Oleg Trifonov
 *
 * trolCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * trolCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.mucommander.ui.viewer.audio;

import javax.swing.JButton;
import javax.swing.JPanel;

/**
 * Created on 14/03/14.
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
