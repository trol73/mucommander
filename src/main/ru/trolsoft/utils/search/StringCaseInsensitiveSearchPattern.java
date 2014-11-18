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
package ru.trolsoft.utils.search;


import java.io.UnsupportedEncodingException;

/**
 * @author Oleg Trifonov
 * Created on 19/11/14.
 */
public class StringCaseInsensitiveSearchPattern implements SearchPattern {
    private final byte[] data;
    private final byte[] dataAlt;

    public StringCaseInsensitiveSearchPattern(String s, String charset) throws UnsupportedEncodingException {
        this.data = s.toLowerCase().getBytes(charset);
        this.dataAlt = s.toUpperCase().getBytes(charset);
    }

    @Override
    public int length() {
        return data.length;
    }

    @Override
    public boolean checkByte(int index, int val) {
        return (data[index] & 0xff) == val || (dataAlt[index] & 0xff) == val;
    }

    @Override
    public boolean checkSelf(int index1, int index2) {
        return data[index1] == data[index2] || dataAlt[index1] == dataAlt[index2] || data[index1] == dataAlt[index2] || dataAlt[index1] == data[index2];
    }
}
