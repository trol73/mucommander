/*
 * This file is part of trolCommander, http://www.trolsoft.ru/en/soft/trolcommander
 * Copyright (C) 2013-2016 Oleg Trifonov
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ru.trolsoft.ui;

import javax.swing.JProgressBar;
import java.awt.*;

/**
 * @author Oleg Trifonov
 * Created on 06/07/16.
 */
public class TProgressBar extends JProgressBar {

    private static final Color GRADIENT_ENDING_COLOR = new Color(0xc0c0c0);
    private static final Color BORDER_COLOR = new Color(0x736a60);
    private static final Color DISABLED_BORDER_COLOR = new Color(0xbebebe);
    public static final Color PREFERRED_PROGRESS_COLOR = new Color(0x1869A6);//new Color(0x3889F6);

    private static final Composite TRANSPARENT = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.45f);
    private static final Composite VERY_TRANSPARENT = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.25f);
    private static final Composite NOT_TRANSPARENT = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f);

    private static GradientPaint gradient;


    public TProgressBar() {
        setFont(getFont().deriveFont(Font.BOLD));
    }


    @Override
    protected void paintComponent(Graphics g) {
        int w = getWidth() - 1;
        int h = getHeight() - 1;

        if (gradient == null) {
            gradient = new GradientPaint(0.0f, 0.0f, Color.WHITE, 0.0f, h, GRADIENT_ENDING_COLOR);
        }
        Graphics2D g2d = (Graphics2D) g;
        // Clean background
        if (isOpaque()) {
            g2d.setColor(getBackground());
            g2d.fillRect(0, 0, getWidth(), getHeight());
        }

        // Control Border
        g2d.setColor(isEnabled() ? BORDER_COLOR : DISABLED_BORDER_COLOR);
        g2d.drawLine(1, 0, w - 1, 0);
        g2d.drawLine(1, h, w - 1, h);
        g2d.drawLine(0, 1, 0, h - 1);
        g2d.drawLine(w, 1, w, h - 1);

        // Fill in the progress
        int min = getMinimum();
        int max = getMaximum();
        int total = max - min;
        float dx = (float) (w - 2) / (float) total;
        int value = getValue();
        int progress = value >= max ? w - 1 : (int) (dx * value);

        g2d.setColor(PREFERRED_PROGRESS_COLOR);
        g2d.fillRect(1, 1, progress, h - 1);

        // A gradient over the progress fill
        g2d.setPaint(gradient);
        g2d.setComposite(TRANSPARENT);
        g2d.fillRect(1, 1, w - 1, (h >> 1));
        final float FACTOR = 0.20f;
        g2d.fillRect(1, h - (int) (h * FACTOR), w - 1, (int) (h * FACTOR));

        g2d.setComposite(VERY_TRANSPARENT);
        final int n = 10;
        int delta = w/n;
        int i = 0;
        if (isEnabled()) {
            for (int xx = delta; xx < w; xx += delta) {
                i++;
                if (value > i*n) {
                    //g2d.setColor(getBackground());
                    //g2d.drawLine(xx-1, 1, xx-1, h - 1);
                    g2d.setColor(Color.GRAY);
                    g2d.drawLine(xx, 1, xx, h - 1);
                }
                g2d.setColor(Color.WHITE);
                g2d.drawLine(xx + 1, 1, xx + 1, h - 1);
            }
        } else {
            for (int xx = 0; xx < w; xx += delta) {
                i++;
                if (value > i*n) {
                    g2d.setColor(Color.RED);
                    g2d.drawLine(xx, h - 1, xx + h, 1);
                }
                g2d.setColor(Color.WHITE);
                g2d.drawLine(xx + 1, h - 1, xx + 1 + h, 1);
            }
        }
        FontMetrics fm = g.getFontMetrics();
        int stringW = fm.stringWidth(getString());
        int stringH = ((h - fm.getHeight()) / 2) + fm.getAscent();

        g2d.setComposite(NOT_TRANSPARENT);
        g2d.setColor(getForeground());
        g2d.setFont(getFont());
        g2d.drawString(getString(), (w - stringW)/2, stringH);
    }
}
