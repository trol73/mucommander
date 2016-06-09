/*
 * This file is part of trolCommander, http://www.trolsoft.ru/en/trolcommander
 * Copyright (C) 2014-2016 Oleg Trifonov
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
package com.mucommander.ui.main.statusbar;

import com.jidesoft.swing.JideSplitButton;

/**
 * Created on 09/06/16.
 * @author Oleg Trifonov
 */
public class FileWindowsListButton extends JideSplitButton {

    /*

        /*
                    Frame frames[] = Frame.getFrames();
            nbFrames = frames.length;
            boolean firstFrame = true;
            for (int i = 0; i < nbFrames; i++) {
                Frame frame = frames[i];
                // Test if Frame is not hidden (disposed), Frame.getFrames() returns both active and disposed frames
                if (frame.isShowing() && (frame instanceof FileFrame)) {
                    // Add a separator before the first non-MainFrame frame to mark a separation between MainFrames
                    // and other frames
                    if (firstFrame) {
                        windowMenu.add(new JSeparator());
                        firstFrame = false;
                    }
                    // Use frame's window title
                    JMenuItem menuItem = new JMenuItem(frame.getTitle());
                    menuItem.addActionListener(this);
                    windowMenu.add(menuItem);
                    windowMenuFrames.put(menuItem, frame);
                }
            }

    JideSplitButton sb = new JideSplitButton();
    sb.setText("button");
    sb.add("---1");
    sb.add("---2");
    sb.add("---3");
    add(sb);

     */

}
