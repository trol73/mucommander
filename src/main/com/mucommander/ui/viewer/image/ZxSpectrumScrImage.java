package com.mucommander.ui.viewer.image;

import com.mucommander.commons.file.AbstractFile;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by trol on 23/02/14.
 */
public class ZxSpectrumScrImage {

    public static final int WIDTH = 256;
    public static final int HEIGHT = 192;

    //private static final int ROWS =

    public static final int SCR_IMAGE_FILE_SIZE = 6912;

    private static final Color[] PALETTE = {
        new Color(0, 0, 0),
        new Color(0, 0, 0xff),
        new Color(0xff, 0, 0),
        new Color(0xff, 0, 0xff),
        new Color(0, 0xff, 0),
        new Color(0, 0xff, 0xff),
        new Color(0xff, 0xff, 0),
        new Color(0xff, 0xff, 0xff)
    };


    public static Image load(AbstractFile f) {
        if (f.getSize() != SCR_IMAGE_FILE_SIZE) {
            return null;
        }
        byte[] data = new byte[SCR_IMAGE_FILE_SIZE];
        int readTotal = 0;
        InputStream is = null;
        try {
            is = f.getInputStream();
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
        return load(data);
    }



    public static Image load(byte[] data, int zoomFactor) {
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
        for (int row = 0; row < 24; row++) {
            for (int col = 0; col < 32; col++) {
                int offset = WIDTH * HEIGHT / 8 + (row*32)+col;
                byte val = data[offset];
                ink[row*32+col] = (byte)(val & 0x7);
                paper[row*32+col] = (byte)((val & 0x38)>>3);
            }
        }

        // render pixels
        for (int y = 0; y < HEIGHT; y++) {
            // display address 010[L4][L3][R2][R1][R0][L2][L1][L0][C4][C3][C2][C1][C0]
            // where C is column number (5 bits)
            // where L is line number (0..23, 5 bits)
            // where R is pixel row in line (0..8, 3 bits)
            int l = y / 8;
            int r = y % 8;
            final int lPatt = ((l & 0x18)<<8)|((l & 0x7)<<5);
            final int rPatt = (r&0x7)<<8;
            for (int x = 0; x < WIDTH; x++) {
                int C = x / 8;
                int cPatt = C & 0x1F;
                int bit = 7 - (x % 8);
                int addr = (((0x4000 | lPatt) | rPatt) | cPatt) - 16384;
                int inkIndex = ink[((y/8)*32)+(x/8)];
                int paperIndex = paper[((y/8)*32)+(x/8)];

                if (((data[addr]&(0x1<<bit))>>bit) != 0) {
                    g.setColor(PALETTE[inkIndex]);
                } else {
                    g.setColor(PALETTE[paperIndex]);
                }
                g.fillRect(x*zoomFactor, y*zoomFactor, zoomFactor, zoomFactor);
            }
        }
        return result;
    }


    public static Image load(byte[] data) {
        return load(data, 2);
    }
}
