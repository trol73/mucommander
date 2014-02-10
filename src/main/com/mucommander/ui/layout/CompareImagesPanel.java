/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2014 Oleg Trifonov
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
package com.mucommander.ui.layout;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.ui.icon.IconManager;
import com.mucommander.ui.icon.SpinningDial;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.io.IOException;

/**
 * Created by trol on 24/01/14.
 */
public class CompareImagesPanel extends JPanel {

    private static final int MAX_PREVIEW_WIDTH = 150;
    private static final int MAX_PREVIEW_HEIGHT = 100;

    private Image[] images = new Image[2];
    private int[] imgWidth = new int[2];
    private int[] imgHeight = new int[2];
    private int[] imgSrcWidth = new int[2];
    private int[] imgSrcHeight = new int[2];

    private JLabel[] lblImageSize = new JLabel[2];

    private JDialog parent;
    private ImageIcon arrow;
    private SpinningDial[] dials = new SpinningDial[2];

    private class LoadImagesTask extends SwingWorker<Void, Void> {
        private AbstractFile file;
        private int index;

        public LoadImagesTask(AbstractFile file, int index) {
            super();
            this.file = file;
            this.index = index;
        }

        @Override
        protected Void doInBackground() throws Exception {
            if (file == null) {
                return null;
            }
            try {
                Image image = loadImage(file);
                imgSrcWidth[index] = image.getWidth(null);
                imgSrcHeight[index] = image.getHeight(null);
                image = scaleImage(image);
                images[index] = image;
                imgWidth[index] = image.getWidth(null);
                imgHeight[index] = image.getHeight(null);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void done() {
            synchronized (CompareImagesPanel.this) {
                if (lblImageSize[index] != null) {
                    lblImageSize[index].setText(imgSrcWidth[index] + " x " + imgSrcHeight[index]);
                }
                int panelWidth = 10 + imgWidth[0] + imgWidth[1] + arrow.getIconWidth();
                int panelHeight = Math.max(imgHeight[0], imgHeight[1]);
                panelHeight = Math.max(panelHeight, arrow.getIconHeight());
                panelWidth = Math.max(panelWidth, getWidth());
                setPreferredSize(new Dimension(panelWidth, panelHeight));
                if (dials[index] != null) {
                    dials[index].setAnimated(false);
                }
                repaint();
                CompareImagesPanel.this.revalidate();
                parent.revalidate();
                parent.pack();
            }
        }
    }

    public CompareImagesPanel(AbstractFile file1, AbstractFile file2, JDialog parent, JLabel lblSize1, JLabel lblSize2) {
        super();
        this.parent = parent;
        this.lblImageSize[0] = lblSize1;
        this.lblImageSize[1] = lblSize2;
        new LoadImagesTask(file1, 0).execute();
        new LoadImagesTask(file2, 1).execute();

        dials[0] = new SpinningDial();
        dials[0].setAnimated(true);


        if (file2 != null) {
            dials[1] = new SpinningDial();
            dials[1].setAnimated(true);
        }
        arrow = IconManager.getIcon(IconManager.IconSet.MISC, "replace_arrow.png");
    }


    @Override
    public void paint(Graphics g) {
        super.paint(g);
        if (images[0] != null) {
            g.drawImage(images[0], (MAX_PREVIEW_WIDTH-imgWidth[0])/2, (getHeight() - imgHeight[0])/2, null);
        } else {
            dials[0].paintIcon(this, g, (MAX_PREVIEW_WIDTH-dials[0].getIconWidth())/2, (getHeight() - dials[0].getIconHeight())/2);
        }
        if (images[1] != null) {
            g.drawImage(images[1], getWidth() - (MAX_PREVIEW_WIDTH+imgWidth[1])/2, (getHeight() - imgHeight[1])/2, null);
        } else if (dials[1] != null){
            dials[1].paintIcon(this, g, getWidth() - (MAX_PREVIEW_WIDTH+dials[1].getIconWidth())/2, (getHeight() - dials[1].getIconHeight())/2);
        }
        if (lblImageSize[1] != null) {
            arrow.paintIcon(this, g, (getWidth() - arrow.getIconWidth())/2, (getHeight() - arrow.getIconHeight())/2);
        }
    }

    private static Image loadImage(AbstractFile file) throws IOException {
        return ImageIO.read(file.getInputStream());
    }

    private static Image scaleImage(Image src) {
        final int srcWidth = src.getWidth(null);
        final int srcHeight = src.getHeight(null);
        if (srcWidth <= MAX_PREVIEW_HEIGHT && srcHeight <= MAX_PREVIEW_HEIGHT) {
            return src;
        }
        final double zoomX = 1.0 * srcWidth / MAX_PREVIEW_WIDTH;
        final double zoomY = 1.0 * srcHeight / MAX_PREVIEW_HEIGHT;
        final double zoom = Math.max(zoomX, zoomY);
        int outWidth = (int)(1.0 * srcWidth / zoom);
        int outHeight = (int)(1.0 * srcHeight / zoom);

        return src.getScaledInstance(outWidth, outHeight, Image.SCALE_FAST);
    }

}
