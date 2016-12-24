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
package ru.trolsoft.macosx;

import sun.swing.ImageIconUIResource;

import javax.swing.*;
import java.awt.*;
import java.awt.image.ImageObserver;
import java.lang.reflect.Field;
import java.net.URL;

/**
 * Created on 23/12/16.
 * @author Oleg Trifonov
 */
public class RetinaImageIcon extends ImageIcon {
    public static final boolean IS_RETINA = checkRetina();

    /**
     * Creates an RetinaImageIcon from the specified URL.
     *
     * @param location the URL for the image
     */
    public RetinaImageIcon(URL location) {
        super(location);
    }

    public RetinaImageIcon(Image img) {
        super(img);
    }

    /**
     *
     * @return true if Retina display found
     */
    private static boolean checkRetina() {
        boolean isRetina = false;
        GraphicsDevice graphicsDevice = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();

        try {
            Field field = graphicsDevice.getClass().getDeclaredField("scale");
            if (field != null) {
                field.setAccessible(true);
                Object scale = field.get(graphicsDevice);
                if (scale instanceof Integer && (Integer) scale == 2) {
                    isRetina = true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isRetina;
    }


    @Override
    public int getIconWidth() {
        return IS_RETINA ? super.getIconWidth() / 2 : super.getIconWidth();
    }

    @Override
    public int getIconHeight() {
        return IS_RETINA ? super.getIconHeight() / 2 : super.getIconHeight();
    }


    @Override
    public synchronized void paintIcon(Component c, Graphics g, int x, int y) {
        if (IS_RETINA) {
            ImageObserver observer = getImageObserver();
            if (observer == null) {
                observer = c;
            }
            Image image = getImage();
            int width = image.getWidth(observer);
            int height = image.getHeight(observer);
            final Graphics2D g2d = (Graphics2D)g.create(x, y, width, height);
            g2d.scale(0.5, 0.5);
            g2d.drawImage(image, 0, 0, observer);
            g2d.scale(1, 1);
            g2d.dispose();
        } else {
            super.paintIcon(c, g, x, y);
        }
    }

    public ImageIcon buildDisabledIcon() {
        return new RetinaImageIcon(GrayFilter.createDisabledImage(getImage()));
    }

}
