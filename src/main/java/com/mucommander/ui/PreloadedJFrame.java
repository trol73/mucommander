/*
 * This file is part of muCommander, http://www.mucommander.com
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.mucommander.ui;

import lombok.extern.slf4j.Slf4j;

import java.awt.LayoutManager;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;

import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * A class that extends JFrame to be later on used by core.
 * Since this bundle is loaded almost as the first (as it has no deps), it can be
 * used by core having JFrame "preloaded" aka "cached" in JVM.
*/
@Slf4j
public class PreloadedJFrame extends JFrame {
    private static final int PRELOAD_FRAMES = 2;
    private static final int PRELOAD_PANELS = 6;


    private static final Queue<PreloadedJFrame> preloadedFrame = new ConcurrentLinkedDeque<>();

    private static final Queue<JPanel> preloadedPanels = new ConcurrentLinkedDeque<>();

    private Object mainFrameObject;

    public static void init() {
        new Thread(() -> {
            log.info("Going to pre-create a couple of JFrames...");
            var start = System.currentTimeMillis();
            for (int i = 0; i < PRELOAD_FRAMES; i++) {
                preloadedFrame.add(new PreloadedJFrame());
            }
            log.info("JFrames pre-creation completed in {}ms", (System.currentTimeMillis() - start));

            log.info("Going to pre-create a couple of JPanels...");
            start = System.currentTimeMillis();
            for (int i = 0; i < PRELOAD_PANELS; i++) {
                preloadedPanels.add(new JPanel());
            }
            log.info("JPanel pre-creation completed in {}ms", (System.currentTimeMillis() - start));

        }, "Preload-JFrame").start();
    }

    private void setMainFrameObject(Object mainFrameObj) {
        this.mainFrameObject = mainFrameObj;
    }

    public Object getMainFrameObject() {
        return mainFrameObject;
    }

    public static JFrame getJFrame(Object mainFrame) {
        var result = preloadedFrame.poll();
        if (result == null) {
            result = new PreloadedJFrame();
        }
        result.setMainFrameObject(mainFrame);
        return result;
    }

    public static JPanel getJPanel(LayoutManager layout) {
        var result = preloadedPanels.poll();
        if (result == null) {
            result = new JPanel(layout);
        } else {
            result.setLayout(layout);
            // Re-apply the current L&F defaults. Preloaded panels are created eagerly
            // in a background thread, potentially before the user's chosen L&F is installed.
            result.updateUI();
        }
        return result;
    }

}
