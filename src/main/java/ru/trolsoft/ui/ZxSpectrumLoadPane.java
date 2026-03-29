package ru.trolsoft.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.Random;

public class ZxSpectrumLoadPane extends JPanel {
    private final Color COLOR_BLUE = new Color(0x0001c8);
    private final Color COLOR_YELLOW = new Color(0xbbbf0a);
//    private final Color COLOR_RED = new Color(0xaa0000);
//    private final Color COLOR_CYAN = new Color(0x00aaaa);
    private final Random r = new Random();
    private int randomPrev;
    private final Timer timer;

    public ZxSpectrumLoadPane() {
        super();
        timer = initTimer();
    }

    public ZxSpectrumLoadPane(LayoutManager layout) {
        super(layout);
        timer = initTimer();
    }

    private Timer initTimer() {
        ActionListener updater = evt -> ZxSpectrumLoadPane.this.repaint();
        return new Timer(30, updater);
    }

    public void stop() {
        timer.stop();
        SwingUtilities.invokeLater(this::repaint);
    }

    public void start() {
        timer.start();
    }

    @Override
    public void paint(Graphics g) {
        if (timer.isRunning()) {
            final int dx = 100;
            final int dy = 50;
            final int stripeH = 6;
            final int w = getWidth();
            final int h = getHeight();


            g.setClip(4, 4, w - 8, h - 8);
            int y = 0;
            boolean firstColor = true;
            while (y < h) {
                g.setColor(firstColor ? COLOR_BLUE : COLOR_YELLOW);
                int rectH = nextBit() ? 2 * stripeH : stripeH;
                g.fillRect(0, y, w, rectH);
                y += rectH;
                firstColor = !firstColor;
            }
            g.setClip(dx, dy, w - 2 * dx, h - 2 * dy);
        }
        super.paint(g);
    }

    private boolean nextBit() {
        if (randomPrev == 0) {
            randomPrev = r.nextBoolean() ? 2 : 1;
            return randomPrev == 2;
        }
        boolean res = randomPrev == 2;
        randomPrev = 0;
        return res;
    }
}
