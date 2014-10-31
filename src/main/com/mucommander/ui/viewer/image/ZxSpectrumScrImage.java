/*
 * This file is part of trolCommander, http://www.trolsoft.ru/soft/trolcommander
 * Copyright (C) 2013-2014 Oleg Trifonov
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
package com.mucommander.ui.viewer.image;


import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created on 23/02/14.
 */
public class ZxSpectrumScrImage {

    public static final int WIDTH = 256;
    public static final int HEIGHT = 192;

    public static final int DEFAULT_ZOOM = 1;

    //private static final int ROWS =

    public static final int SCR_IMAGE_FILE_SIZE = 6912;

    private static final Color[] PALETTE_BRIGHT_0 = {
        new Color(0x000000),
        new Color(0x0000cd),
        new Color(0xcd0000),
        new Color(0xff00ff),
        new Color(0x00cd00),
        new Color(0x00cdcd),
        new Color(0xcdcd00),
        new Color(0xcdcdcd)
    };
    private static final Color[] PALETTE_BRIGHT_1 = {
        new Color(0x000000),
        new Color(0x0000ff),
        new Color(0xff0000),
        new Color(0xff00ff),
        new Color(0x00ff00),
        new Color(0x00ffff),
        new Color(0xffff00),
        new Color(0xffffff)
    };


    public static BufferedImage load(InputStream is, int zoomFactor) {
        byte[] data = new byte[SCR_IMAGE_FILE_SIZE];
        int readTotal = 0;
        try {
            while (readTotal < SCR_IMAGE_FILE_SIZE) {
                int bytesRead = is.read(data, readTotal, SCR_IMAGE_FILE_SIZE - readTotal);
                if (bytesRead < 0) {
                    break;
                }
                readTotal += bytesRead;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (is != null) {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (readTotal != SCR_IMAGE_FILE_SIZE) {
            return null;
        }
        return load(data, zoomFactor);
    }



    public static BufferedImage load(byte[] data, int zoomFactor) {
        if (data == null || data.length != SCR_IMAGE_FILE_SIZE) {
            return null;
        }
        if (zoomFactor < 1) {
            zoomFactor = 1;
        }
        BufferedImage result = new BufferedImage(WIDTH*zoomFactor, HEIGHT*zoomFactor, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = result.createGraphics();


        // read colour data (attributes)
        byte[] ink = new byte[32*24];
        byte[] paper = new byte[32*24];
        boolean[] bright = new boolean[32*24];
        for (int row = 0; row < 24; row++) {
            for (int col = 0; col < 32; col++) {
                int offset = WIDTH * HEIGHT / 8 + (row*32)+col;
                byte val = data[offset];

                ink[row*32+col]   = (byte)( val & 0b00000111);
                paper[row*32+col] = (byte)((val & 0b00111000)>>3);
                bright[row*32+col] = ((val & 0b11000000)>>6) != 0;
            }
        }

        // render pixels
        for (int y = 0; y < HEIGHT; y++) {
            // display address 010[L4][L3][R2][R1][R0][L2][L1][L0][C4][C3][C2][C1][C0]
            int line = y / 8;       // line number (0..23, 5 bits)
            int row = y % 8;        // pixel row in line (0..8, 3 bits)
            final int lPart = ((line & 0x18)<<8)|((line & 0x7)<<5);
            final int rPart = (row & 0x7)<<8;
            for (int x = 0; x < WIDTH; x++) {
                int col = x / 8;    // column number (5 bits)
                int cPart = col & 0x1F;
                int bit = 7 - (x % 8);
                int address = (((0x4000 | lPart) | rPart) | cPart) - 16384;
                int attrIndex = ((y/8)*32)+(x/8);
                int inkIndex = ink[attrIndex];
                int paperIndex = paper[attrIndex];
                boolean istBright = bright[attrIndex];
                if (((data[address]&(0x1<<bit))>>bit) != 0) {
                    g.setColor(PALETTE_BRIGHT_0[inkIndex]);
                } else {
                    if (istBright) {
                        g.setColor(PALETTE_BRIGHT_1[paperIndex]);
                    } else {
                        g.setColor(PALETTE_BRIGHT_0[paperIndex]);
                    }
                }
                g.fillRect(x*zoomFactor, y*zoomFactor, zoomFactor, zoomFactor);
            }
        }
        return result;
    }


    public static BufferedImage load(byte[] data) {
        return load(data, DEFAULT_ZOOM);
    }

    public static BufferedImage load(InputStream is) {
        return load(is, DEFAULT_ZOOM);
    }
}
