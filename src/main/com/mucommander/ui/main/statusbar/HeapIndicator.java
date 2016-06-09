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

import com.mucommander.cache.TextHistory;
import com.mucommander.cache.WindowsStorage;
import com.mucommander.ui.theme.ColorChangedEvent;
import com.mucommander.ui.theme.FontChangedEvent;
import com.mucommander.ui.theme.ThemeListener;
import com.mucommander.utils.FileIconsCache;

import javax.swing.JLabel;
import javax.swing.Timer;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/**
 * @author Oleg Trifonov
 * Created on 13/11/14.
 */
public class HeapIndicator extends JLabel implements ActionListener, ThemeListener, MouseListener {

    private Timer timer;
    private int refreshInterval;

    private long usedMem;
    private long totalMem;
    private Color colorBorder = new Color(0x555555);
    private Color colorForeground = new Color(0x8888ff);

    public HeapIndicator() {
        super("");
        setHorizontalAlignment(CENTER);
        setRefreshInterval(1000*10);
        update();
        setMinimumSize(new Dimension(80, 0));
        setMaximumSize(new Dimension(80, 100));
        addMouseListener(this);
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        return new Dimension(d.width+4, d.height+2);
    }

    @Override
    public void paint(Graphics g) {
        int width = getWidth();
        int height = getHeight();

        g.setColor(colorBorder);
        g.drawRect(0, 0, width-1, height-1);
        g.setColor(colorForeground);
        int x2 = (int)(width*((float)usedMem/(float)totalMem));
        g.fillRect(1, 1, x2-1, height-2);

        super.paint(g);
    }

    private static long bytesToKb(long bytes) {
        return bytes / 1024L;
    }

    private static long bytesToMb(long bytes) {
        return bytes / 1024L / 1024L;
    }

    /**
     * Updates heap memory information.
     */
    private void update() {
        totalMem = Runtime.getRuntime().totalMemory();
        usedMem = totalMem - Runtime.getRuntime().freeMemory();
        int percent = (int)(100*usedMem/totalMem);
        //setText(" " + bytesToMb(usedMem) + " MB ");
        setText(" " + bytesToMb(totalMem) + " MB ");
        setToolTipText("Memory used " + bytesToMb(usedMem) + "MB  from " + bytesToMb(totalMem) + "MB  " + percent + "%");
    }

    protected void installTimer(int interval) {
        if (timer == null) {
            timer = new Timer(interval, this);
        } else {
            timer.stop();
            timer.setDelay(interval);
        }
        timer.start();
    }

    protected void uninstallTimer() {
        if (timer != null) {
            timer.stop();
            timer.removeActionListener(this);
            timer = null;
        }
    }

    public void setRefreshInterval(int interval) {
        this.refreshInterval = interval;
        installTimer(interval);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        update();
        repaint();
    }

    @Override
    public void setVisible(boolean visible) {
        if (visible) {
            installTimer(refreshInterval);
        } else {
            uninstallTimer();
        }
        super.setVisible(visible);
    }

    @Override
    public void colorChanged(ColorChangedEvent event) {

    }

    @Override
    public void fontChanged(FontChangedEvent event) {

    }

    @Override
    public void mouseClicked(MouseEvent e) {
        TextHistory.getInstance().clear();
        WindowsStorage.getInstance().clear();
        FileIconsCache.getInstance().clear();
        System.gc();
        update();
        repaint();
    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }
}
