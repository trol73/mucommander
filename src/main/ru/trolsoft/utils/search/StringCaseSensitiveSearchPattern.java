package ru.trolsoft.utils.search;

import java.io.UnsupportedEncodingException;

/**
 * @author Oleg Trifonov
 * Created on 19/11/14.
 */
public class StringCaseSensitiveSearchPattern implements SearchPattern {

    private final byte data[];

    public StringCaseSensitiveSearchPattern(String s, String charset) throws UnsupportedEncodingException {
        this.data = s.getBytes(charset);
    }

    @Override
    public int length() {
        return data.length;
    }

    @Override
    public boolean checkByte(int index, int val) {
        return (data[index] & 0xff) == val;
    }

    @Override
    public boolean checkSelf(int index1, int index2) {
        return data[index1] == data[index2];
    }
}
