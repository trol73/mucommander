/*
 * This file is part of trolCommander, http://www.trolsoft.ru/en/soft/trolcommander
 * Copyright (C) 2013-2017 Oleg Trifonov
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
package ru.trolsoft.utils;


import java.io.IOException;
import java.io.InputStream;

/**
 * @author Oleg Trifonov
 * Created on 11/01/17.
 */
public class ImageSizeDetector {

    public enum ImageType {
        GIF,
        PNG,
        JPEG,
        BMP,
        TIFF
    }

    private int height;
    private int width;
    private ImageType type;

    public ImageSizeDetector(InputStream is) throws IOException {
        process(is);
    }

    private void process(InputStream is) throws IOException {
        int c1 = is.read();
        int c2 = is.read();
        int c3 = is.read();

        width = height = -1;

        if (c1 == 'G' && c2 == 'I' && c3 == 'F') { // GIF
            is.skip(3);
            width = readWordLH(is);
            height = readWordLH(is);
            type = ImageType.GIF;
        } else if (c1 == 0xFF && c2 == 0xD8) { // JPG
            while (c3 == 255) {
                int marker = is.read();
                int len = readWordHL(is);
                if (marker == 192 || marker == 193 || marker == 194) {
                    is.skip(1);
                    height = readWordHL(is);
                    width = readWordHL(is);
                    type = ImageType.JPEG;
                    break;
                }
                is.skip(len - 2);
                c3 = is.read();
            }
        } else if (c1 == 137 && c2 == 80 && c3 == 78) { // PNG
            is.skip(15);
            width = readWordHL(is);
            is.skip(2);
            height = readWordHL(is);
            type = ImageType.PNG;
        } else if (c1 == 66 && c2 == 77) { // BMP
            is.skip(15);
            width = readWordLH(is);
            is.skip(2);
            height = readWordLH(is);
            type = ImageType.BMP;
        } else {
            int c4 = is.read();
            if ((c1 == 'M' && c2 == 'M' && c3 == 0 && c4 == 42) || (c1 == 'I' && c2 == 'I' && c3 == 42 && c4 == 0)) { //TIFF
                boolean bigEndian = c1 == 'M';
                int entries;
                int ifd = readDword(is, bigEndian);
                is.skip(ifd - 8);
                entries = readWord(is, bigEndian);
                for (int i = 1; i <= entries; i++) {
                    int tag = readWord(is, bigEndian);
                    int fieldType = readWord(is, bigEndian);
                    //long count =
                    readDword(is, bigEndian);
                    int valOffset;
                    if ((fieldType == 3 || fieldType == 8)) {
                        valOffset = readWord(is, bigEndian);
                        is.skip(2);
                    } else {
                        valOffset = readDword(is, bigEndian);
                    }
                    if (tag == 256) {
                        width = valOffset;
                    } else if (tag == 257) {
                        height = valOffset;
                    }
                    if (width != -1 && height != -1) {
                        type = ImageType.TIFF;
                        break;
                    }
                }
            }
        }
    }


    private static int readWordLH(InputStream is) throws IOException {
        int b1 = is.read();
        int b2 = is.read();
        return b2 >= 0 ? b1 + (b2 << 8) : -1;
    }

    private static int readWordHL(InputStream is) throws IOException {
        int b1 = is.read();
        int b2 = is.read();
        return b2 >= 0 ? b2 + (b1 << 8) : -1;
    }

    private static int readWord(InputStream is, boolean hiLo) throws IOException {
        return hiLo ? readWordHL(is) : readWordLH(is);
    }

    private static int readDwordLH(InputStream is) throws IOException {
        int b1 = is.read();
        int b2 = is.read();
        int b3 = is.read();
        int b4 = is.read();
        return b4 >= 0 ? b1 + (b2 << 8)  + (b3 << 16) + (b4 << 24): -1;
    }

    private static int readDwordHL(InputStream is) throws IOException {
        int b1 = is.read();
        int b2 = is.read();
        int b3 = is.read();
        int b4 = is.read();
        return b4 >= 0 ? b4 + (b3 << 8)  + (b2 << 16) + (b1 << 24): -1;
    }

    private static int readDword(InputStream is, boolean hiLo) throws IOException {
        return hiLo ? readDwordHL(is) : readDwordLH(is);
    }


    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public ImageType getType() {
        return type;
    }

    @Override
    public String toString() {
        return type + "\t Width : " + width + "\t Height : " + height;
    }

}
